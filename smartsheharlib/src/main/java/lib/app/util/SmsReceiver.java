package lib.app.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;


public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        if (extras == null)
            return;

        // To display a Toast whenever there is an SMS.
        // Toast.makeText(mContext,"Recieved",Toast.LENGTH_LONG).show();

        Object[] pdus = (Object[]) extras.get("pdus");
        assert pdus != null;
        for (Object pdu : pdus) {
            SmsMessage SMessage = SmsMessage.createFromPdu((byte[]) pdu);
            if (SMessage != null) {
                String sender = SMessage.getOriginatingAddress();
                String body = SMessage.getMessageBody();
                Intent in = new Intent("SmsMessage.intent.MAIN").putExtra(
                        "get_msg", sender + ":" + body);
                context.sendBroadcast(in);
            }
        }
    }
}
