package com.smartshehar.dashboard.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent myIntent = new Intent(context, PowerButtonService.class);
        context.startService(myIntent);
    }
}
