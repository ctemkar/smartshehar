package com.smartshehar.dashboard.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;


public class PowerButtonReceiver extends BroadcastReceiver {
    static int countPower = 0;
    private static final int POWER_TIMEOUT = 2000;
    private Handler handler = new Handler();
    private Runnable powerCounterReset = new PowerTimeoutReset();
    private static final String TAG = "PowerButtonReceiver";
    SharedPreferences mSettings;
    Context mContext;
    private CGlobals_db mApp = null;

    public PowerButtonReceiver() {
    }

    public PowerButtonReceiver(Context context) {
        this.mContext = context;
        mApp = CGlobals_db.getInstance(mContext);
        mApp.init(mContext);

        mSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        countPower++;
        Log.d(TAG, "countPower " + countPower);
        if (countPower == 3) {
            //record insert
            getEmergencyContactDetails();
        } else {
            resetPowerTimeout();
        }
    }

    private void resetPowerTimeout() {

        startPowerTimeout();
    }

    private void startPowerTimeout() {
        handler.postDelayed(powerCounterReset, POWER_TIMEOUT);
    }

    private class PowerTimeoutReset implements Runnable {

        public void run() {
            countPower = 0;
        }

    }

    private void getEmergencyContactDetails() {

        countPower = 0;
        String msMyNo , msCC1Phone ,
                msCC1 , msCC2Phone, msCC2 ,
                msCC3Phone , msCC3;
        try {
            mSettings = PreferenceManager.getDefaultSharedPreferences(mContext);
            if (mSettings != null) {
                msMyNo = mSettings.getString(Constants_dp.KEY_PREF_MY_NUMBER, "");
                msCC1Phone = mSettings.getString(Constants_dp.KEY_PREF_CC1_PHONE, "");
                msCC1 = mSettings.getString(Constants_dp.KEY_PREF_CC1, "");
                msCC2Phone = mSettings.getString(Constants_dp.KEY_PREF_CC2_PHONE, "");
                msCC2 = mSettings.getString(Constants_dp.KEY_PREF_CC2, "");
                msCC3Phone = mSettings.getString(Constants_dp.KEY_PREF_CC3_PHONE, "");
                msCC3 = mSettings.getString(Constants_dp.KEY_PREF_CC3, "");

//                if (!TextUtils.isEmpty(msMyNo))
//                    sendEmergencySms(msMyNo, msMyEmail);
                if (!TextUtils.isEmpty(msCC1Phone) && !TextUtils.isEmpty(msMyNo))
                    sendEmergencySms(msCC1Phone, msCC1);
                if (!TextUtils.isEmpty(msCC2Phone) && !TextUtils.isEmpty(msMyNo))
                    sendEmergencySms(msCC2Phone, msCC2);
                if (!TextUtils.isEmpty(msCC3Phone) && !TextUtils.isEmpty(msMyNo))
                    sendEmergencySms(msCC3Phone, msCC3);
            }
        } catch (Exception e) {
            SSLog.e(TAG, "getEmergencyContactDetails", e);
        }
    }

    private void sendEmergencySms(String sNo, String sEmail) {
        Location location = mApp.getMyLocation(mContext);
        if (TextUtils.isEmpty(sNo))
            return;
        String msg = "Emergency SMS ";
//		if(!TextUtils.isEmpty(sName))
//			msg += " from " + sName + " ";
        msg += " from - maps.google.com/maps?z=16&t=m&q=loc:" +
                String.format("%.4f", location.getLatitude()) +
                "+" + String.format("%.4f", location.getLongitude());
        if (location != null) {
//			msg += " lat, long - " + String.format("%.4f", location.getLatitude()) +
//				", " + String.format("%.4f", location.getLongitude());
            msg += " via SmartShehar Safety Shield http://goo.gl/NBu8A ";

        }
        if (!TextUtils.isEmpty(sEmail)) {
            msg += ". Check " + sEmail + "";
        }
        new CSms(mContext, sNo, msg);
    } // sendEmergencySms
}
