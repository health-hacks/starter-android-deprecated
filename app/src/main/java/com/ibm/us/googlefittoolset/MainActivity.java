package com.ibm.us.googlefittoolset;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;

import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Subscription;

import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.google.android.gms.fitness.result.DataReadResult;

import com.google.android.gms.fitness.request.DataReadRequest;

import com.google.android.gms.fitness.result.SessionReadResult;
import com.ibm.us.googlefittoolset.model.HealthData;
import com.ibm.us.googlefittoolset.model.HealthDataValue;
import com.ibm.us.googlefittoolset.services.GoogleFitManager;
import com.ibm.us.googlefittoolset.services.LogsManager;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = "GoogleFitToolSet";

    TextView mainInfoTextView;
    TextView listInfoTextView;

    // List View for displaying Google Fit data
    ListView listView;
    ArrayAdapter<String> listAdapter;
    int defaultResourceID = android.R.layout.simple_list_item_1;

    String[] healthDataList = {"weight", "step"};
    ArrayList<String> curListContent = null;

    // menu / weight / step
    boolean displayingMenu = true;

    //Realm
    private Realm realm;
    private RealmConfiguration realmConfig;

    //GoogleFit
    private GoogleFitManager gfManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainInfoTextView = (TextView) findViewById(R.id.tv_main_info);
        mainInfoTextView.setText("> Welcome to Google Toolset! \n");
        mainInfoTextView.setMovementMethod(new ScrollingMovementMethod());

        listInfoTextView = (TextView) findViewById(R.id.tv_list_info);
        listInfoTextView.setText("Data types:");

        curListContent = new ArrayList<String>(Arrays.asList(healthDataList));

        listAdapter = new ArrayAdapter<String>(this, defaultResourceID, curListContent);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(this);

        BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null ) {
                    String str = intent.getStringExtra(LogsManager.extraMessageName);
                    //Get all your data from intent and do what you want
                    mainInfoTextView.append(str);
                }
            }
        };

        // setup logs manager
        LogsManager.sharedInstance.appContext = this;
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(LogsManager.broadcastIntentName));

        // Open the Realm for the UI thread.
        realmConfig = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfig);
        realm = Realm.getDefaultInstance();

        // setup google fit manager
        gfManager = new GoogleFitManager(this);

        Log.e(TAG, realm.toString());
    }

    // Create a message handling object as an anonymous class.
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        if(displayingMenu){
            String backItem = "< BACK";
            List<String> newValues = new ArrayList<String>();
            newValues.add(backItem);

            String dataLabel = healthDataList[position];

            RealmResults<HealthDataValue> healthDataValues = realm.where(HealthDataValue.class)
                    .equalTo("label", dataLabel).findAll();

            for(HealthDataValue result : healthDataValues){
                newValues.add("Date: " + result.healthObject.date.toString() + "\n"
                        + dataLabel +": " + result.value);
            }

            updateListAdapter(newValues);
            listInfoTextView.setText(dataLabel + " data: ");
            displayingMenu = !displayingMenu;
        }
        else {
            if(position==0){
                //back to main menu
                updateListAdapter(Arrays.asList(healthDataList));
                listInfoTextView.setText("Data types: ");
                displayingMenu = !displayingMenu;
            }
        }

    }

    private void updateListAdapter(List<String> newResults) {
        listAdapter.clear();

        for(String value : newResults) {
            listAdapter.add(value);
        }

        listAdapter.notifyDataSetChanged();
    }
}
