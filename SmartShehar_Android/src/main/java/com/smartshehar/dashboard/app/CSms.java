package com.smartshehar.dashboard.app;

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

public class CSms {
    private Context mContext;
	public  CSms(Context context, String phone, String msg) {
        mContext = context;

		if(TextUtils.isEmpty(phone)) {
			Toast.makeText(mContext, "Invalid Phone no. - " + phone + " SMS NOT SENT!!!",
                Toast.LENGTH_SHORT).show();
			return;
		}
		// Split message -
		int l = msg.length();
		int noofchars, i;
        int MAX_SMS_LENGTH = 160;
        for(i = 0; i < l; i = i + MAX_SMS_LENGTH) {
			noofchars = l - i > MAX_SMS_LENGTH ? MAX_SMS_LENGTH : l - i;
			try {
				sendSMS(phone, msg.substring(i, i + noofchars));
			} catch (Exception e) {
				Log.e("CSms: ", e.getMessage());
			}
		}
	}

    private BroadcastReceiver smsStatusReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent arg1) {

            switch (getResultCode())
            {
                case Activity.RESULT_OK:
                    Toast.makeText(mContext, "SMS sent",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(mContext, "Check if a valid phone number was entered and you have balance",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(mContext, "No service",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(mContext, "Null PDU",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(mContext, "Radio off",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            arg0.unregisterReceiver(smsStatusReceiver);

        }
    };

    private BroadcastReceiver smsDeliveredReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode())
            {
                case Activity.RESULT_OK:
                    Toast.makeText(mContext, "SMS delivered",
                            Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(mContext, "SMS not delivered",
                            Toast.LENGTH_SHORT).show();
                    break;                        
            }
            arg0.unregisterReceiver(smsDeliveredReceiver);

        }
     };
    
	//---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {        
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
 
        PendingIntent sentPI = PendingIntent.getBroadcast(mContext, 0,
            new Intent(SENT), 0);
 
        PendingIntent deliveredPI = PendingIntent.getBroadcast(mContext, 0,
            new Intent(DELIVERED), 0);
 
        //---when the SMS has been sent---
        try {
            mContext.registerReceiver(smsStatusReceiver, new IntentFilter(SENT));
        } catch (Exception e) {
        	SSLog.e("CSms: sendSms","error ", e.getMessage());
        }

        //---when the SMS has been delivered---
        try {
            mContext.registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
	    } catch (Exception e) {
	    	SSLog.e("CSms: sendSms","error", e.getMessage());
	    }

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);        
    }	
	
	
}
