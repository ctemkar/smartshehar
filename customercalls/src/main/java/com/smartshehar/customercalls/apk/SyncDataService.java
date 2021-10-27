package com.smartshehar.customercalls.apk;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import lib.app.util.Connectivity;

/**
 * Created by asmita on 03/05/2016.
 */
public class SyncDataService extends Service {

    public static final String TAG = "SyncDataService";
    Connectivity mConnectivity;
    Handler handler = new Handler();
    CustomerCallsSQLiteDB db;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mConnectivity = new Connectivity();
        CGlobals_cc.getInstance().init(SyncDataService.this);
        db = new CustomerCallsSQLiteDB(SyncDataService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.postDelayed(runnableSendDataToServer, Constants_cc.CHECKE_CONNECTION_INTERVAL);
        return START_STICKY;
    }

    protected Runnable runnableSendDataToServer = new Runnable() {
        @Override
        public void run() {
            if (Connectivity.checkConnected(SyncDataService.this)) {
                if (!mConnectivity.connError(SyncDataService.this)) {
                    CGlobals_cc.syncCallLog(SyncDataService.this.getApplicationContext());
                }
            }

            handler.postDelayed(this, Constants_cc.CHECKE_CONNECTION_INTERVAL);
        }
    };


}
