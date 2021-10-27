package com.jumpinjumpout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jumpinjumpout.ui.ActDriverProfileRegistration;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, ActDriverProfileRegistration.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
    }
}