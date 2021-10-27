package lib.app.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;


public class CSms {
    //    private String TAG = "CSms: ";
    private Activity mActivity;
    private int i;
    private String msPhone;

    public CSms(Activity activity) {
        mActivity = activity;

    }

    public CSms(Activity activity, final String phone, final String msg) {
        mActivity = activity;
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(mActivity.getBaseContext(),
                    "Invalid Phone no. - " + phone + " SMS NOT SENT!!!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        msPhone = phone;
        // Split message -
        int l = msg.length();
        int noofchars;
        int mNextIndex = 0;
        ArrayList<String> masMessage = new ArrayList<>();
        int FIRST_LENGTH = 0;
        int MAX_SMS_LENGTH = 160;
        if (l > MAX_SMS_LENGTH) {
            int mult = (int) Math.ceil(l / (double) MAX_SMS_LENGTH);
            FIRST_LENGTH = l - MAX_SMS_LENGTH * (mult - 1);

            // FIRST_LENGTH = (int)((l / 2) - 10);
            masMessage.add(msg.substring(i, FIRST_LENGTH));
        }
        for (i = FIRST_LENGTH; i < l; i = i + MAX_SMS_LENGTH) {
            noofchars = l - i > MAX_SMS_LENGTH ? MAX_SMS_LENGTH : l - i;
            masMessage.add(msg.substring(i, i + noofchars));
        }

        // sendSMS(phone, asMessage.get(0)); // Send the first part

        // for (String s: asMessage) {
        // msMessage = s;
        try {
            // sendSMS(phone, msg.substring(i, i + noofchars));
            mNextIndex++;
            String sMessagePart = masMessage.get(mNextIndex);
            sendSMS(phone, sMessagePart);
        } catch (Exception e) {
//            SSLog.e(TAG, "CSms - ", e);
        }
        // }
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
                    Toast.makeText(
                            mActivity.getBaseContext(),
                            "Check if a valid phone number was entered and you have balance",
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
        public void onReceive(Context arg0, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(mActivity.getBaseContext(), "SMS delivered",
                            Toast.LENGTH_SHORT).show();
                    String msg = intent.getStringExtra(msPhone);

                    // Process the sms format and extract body &amp; phoneNumber
                    msg = msg.replace("\n", "");
                    String body = msg.substring(msg.lastIndexOf(":") + 1,
                            msg.length());
                    String pNumber = msg.substring(0, msg.lastIndexOf(":"));

                    // Add it to the list or do whatever you wish to
                    Log.e("onResume", "" + msg + body + pNumber);

                    Toast.makeText(mActivity.getBaseContext(), body, Toast.LENGTH_LONG).show();

                    // check body content with your validation code mine is
                    // success123

                    if (body.equalsIgnoreCase("success123")) {

                        Toast.makeText(mActivity.getBaseContext(),
                                "Authentication Success.", Toast.LENGTH_LONG).show();
                        // mobNoVeryfyTv.setText("Authentication Success.");

                    }
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(mActivity.getBaseContext(), "SMS not delivered",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            arg0.unregisterReceiver(smsDeliveredReceiver);

        }
    };

    // ---sends an SMS message to another device---
    public void sendSMS(String pNumber, String message) {
        if (TextUtils.isEmpty(pNumber) || TextUtils.isEmpty(message)) {
            return;
        }

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent.getBroadcast(mActivity, 0, new Intent(SENT), 0);
        PendingIntent.getBroadcast(mActivity, 0, new Intent(DELIVERED), 0);
        Intent sentIntent = new Intent(SENT);
        Intent deliveredIntent = new Intent(DELIVERED);
        PendingIntent sentPI = PendingIntent.getBroadcast(
                mActivity.getApplicationContext(), 0, sentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent deliverPI = PendingIntent.getBroadcast(
                mActivity.getApplicationContext(), 0, deliveredIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // ---when the SMS has been sent---
        mActivity.registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String result = "";

                switch (getResultCode()) {

                    case Activity.RESULT_OK:
                        result = "SMS sent";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        result = "SMS failed, please check balance";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        result = "Please make sure you have signal - Radio off";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        result = "No PDU defined";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        result = "No service";
                        break;
                }
                Toast.makeText(mActivity.getApplicationContext(), result,
                        Toast.LENGTH_LONG).show();
            }
        }, new IntentFilter(SENT));

/* Register for Delivery event */
        mActivity.registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(mActivity.getApplicationContext(), "Delivered",
                        Toast.LENGTH_LONG).show();
                unregisterReceivers();
            }

        }, new IntentFilter(DELIVERED));

    SmsManager sms = SmsManager.getDefault();
    sms.sendTextMessage(pNumber,null,message,sentPI,deliverPI);
}

    public void unregisterReceivers() {
        try {
            mActivity.unregisterReceiver(smsDeliveredReceiver);
        } catch (Exception e) {
            // SSLog.e(TAG, "unregisterReceivers - ", e);
        }
        try {
            mActivity.unregisterReceiver(smsStatusReceiver);
        } catch (Exception e) {
            //SSLog.e(TAG, "unregisterReceivers - ", e);
        }
    }

} // CSms
