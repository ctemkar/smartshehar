package com.smartshehar.android.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.smartshehar.dashboard.app.SSLog;

import java.util.ArrayList;




public class CSms {
    private int MAX_SMS_LENGTH = 160;
    private int NEXT_SMS_DELAY = 3000;
    private Activity mActivity;
    private int i, noofchars;
    private int mNextIndex = 0;
    private String msPhone;
    private ArrayList<String> masMessage = new ArrayList<String>();
    private static String TAG = "CMS: ";

    public CSms(Activity activity, final String phone, final String msg) {
        mActivity = activity;
        msPhone = phone;
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(mActivity.getBaseContext(), "Invalid Phone no. - " + phone + " SMS NOT SENT!!!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // Split message -
        int l = msg.length();
        noofchars = 0;
        mNextIndex = 0;
        masMessage = new ArrayList<String>();
        int FIRST_LENGTH = 0;
        if (l > MAX_SMS_LENGTH) {
            int mult = (int) Math.ceil(l / (double) MAX_SMS_LENGTH);
            FIRST_LENGTH = l - MAX_SMS_LENGTH * (mult - 1);

//			FIRST_LENGTH = (int)((l / 2) - 10);
            masMessage.add(msg.substring(i, FIRST_LENGTH));
        }
        for (i = FIRST_LENGTH; i < l; i = i + MAX_SMS_LENGTH) {
            noofchars = l - i > MAX_SMS_LENGTH ? MAX_SMS_LENGTH : l - i;
            masMessage.add(msg.substring(i, i + noofchars));
        }

//		sendSMS(phone, asMessage.get(0)); // Send the first part

//		for (String s: asMessage) {
//			msMessage = s;
        try {
//	    				sendSMS(phone, msg.substring(i, i + noofchars));
            String sMessagePart = masMessage.get(mNextIndex++);
            sendSMS(phone, sMessagePart);
        } catch (Exception e) {
            Log.e("CSms: ", e.getMessage());
        }
//    	}
    }

    private BroadcastReceiver smsStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(mActivity.getBaseContext(), "SMS sent",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(mActivity.getBaseContext(), "Check if a valid phone number was entered and you have balance",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(mActivity.getBaseContext(), "No service",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(mActivity.getBaseContext(), "Null PDU",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(mActivity.getBaseContext(), "Radio off",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            arg0.unregisterReceiver(smsStatusReceiver);

        }
    };

    private BroadcastReceiver smsDeliveredReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(mActivity.getBaseContext(), "SMS delivered",
                            Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(mActivity.getBaseContext(), "SMS not delivered",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            arg0.unregisterReceiver(smsDeliveredReceiver);

        }
    };

    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(mActivity, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(mActivity, 0,
                new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        try {
            mActivity.registerReceiver(smsStatusReceiver, new IntentFilter(SENT));
            if (mNextIndex < masMessage.size()) {
                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendSMS(msPhone, masMessage.get(mNextIndex++));
                        } catch (Exception e) {
                            Log.e("CSms: ", e.getMessage());
                        }
                    }
                }, NEXT_SMS_DELAY);

            }
        } catch (Exception e) {
            SSLog.e(TAG, "sendSms", e.getMessage());
        }
        //---when the SMS has been delivered---
        try {
            mActivity.registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
        } catch (Exception e) {
            SSLog.e(TAG, "sendSms", e.getMessage());
        }

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    public void unregisterReceivers() {
        try {
            mActivity.unregisterReceiver(smsDeliveredReceiver);
        } catch (Exception e) {
            SSLog.e(TAG, "unregisterReceivers", e.getMessage());
        }
        try {
            mActivity.unregisterReceiver(smsStatusReceiver);
        } catch (Exception e) {
            SSLog.e(TAG, "unregisterReceivers", e.getMessage());
        }
    }

} // CSms

