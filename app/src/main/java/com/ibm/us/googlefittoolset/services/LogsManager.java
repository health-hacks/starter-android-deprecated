package com.ibm.us.googlefittoolset.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by ismails on 3/20/17.
 */

public class LogsManager {
    public static String broadcastIntentName = "event-log";
    public static String extraMessageName = "message";
    public Context appContext;

    public static LogsManager sharedInstance = new LogsManager();

    public void sendMessage(String message) {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent(LogsManager.broadcastIntentName);
        // You can also include some extra data.
        intent.putExtra(LogsManager.extraMessageName, message);
        LocalBroadcastManager.getInstance(this.appContext).sendBroadcast(intent);
    }
}
