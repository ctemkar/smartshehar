package com.smartshehar.dashboard.app;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import lib.app.util.Connectivity;

public class PowerButtonService extends Service {

    Connectivity mConnectivity;
    public static final String TAG = "SendDraftDataService";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mConnectivity = new Connectivity();
        PowerButtonReceiver mReceiver = new PowerButtonReceiver(this.getBaseContext());
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return START_STICKY;

    }
}