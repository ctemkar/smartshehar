package com.smartshehar.cabe.driver;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.smartshehar.cabe.driver.ui.CabEDriverForHire_act;
import com.smartshehar.cabe.driver.ui.CabEMainDriver_act;
import com.smartshehar.cabe.driver.ui.DashBoard_Driver_act;

import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Deque;

import lib.app.util.CGlobals_lib_ss;

/**
 * Created by soumen on 10-11-2016.
 * Notification Message Service
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService: ";
    Intent intent;
    int upgrade_app;
    String link;
    private int iMessage = 0;
    private static int mNotifyNo = 0;
    static String msContentText = "";
    public static Deque<JijoNotification> arrayDeque = new ArrayDeque<JijoNotification>();

    @SuppressLint("NewApi")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData().get("message"));
            sendNotification(remoteMessage);
        }
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    private void sendNotification(RemoteMessage messageBody) {
        String message = messageBody.getData().get("message");
        String title = messageBody.getData().get("title");
        String tickerText = messageBody.getData().get("tickerText");
        /*String vibrate = messageBody.getData().get("vibrate");
        String sound = messageBody.getData().get("sound");
        String subtitle = messageBody.getData().get("subtitle");
        String smallIcon = messageBody.getData().get("smallIcon");
        String largeIcon = messageBody.getData().get("largeIcon");*/
        String other = messageBody.getData().get("other");
        String notification_type = messageBody.getData().get("notification_type");
        String notification_category = messageBody.getData().get("notification_category");
        try {
            JSONObject jResponse = new JSONObject(other);
            link = CGlobals_CED.isNullNotDefined(jResponse, "link") ?
                    "" : jResponse.getString("link");

            upgrade_app = CGlobals_CED.isNullNotDefined(jResponse, "upgrade_app") ?
                    0 : jResponse.getInt("upgrade_app");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (upgrade_app == 1) {
            Uri uri = Uri.parse(link);
            intent = new Intent(Intent.ACTION_VIEW, uri);
        } else {
            intent = getRightIntent();
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        mNotifyNo++;
        if (mNotifyNo > 9) {
            mNotifyNo = 1;
            msContentText = "";
            arrayDeque.pollLast();
        }
        boolean isClearFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(this).
                getBoolean(Constants_CED.PREF_NOTIFICATION_CLEAR_FLAG, false);
        if (isClearFlag) {
            arrayDeque.clear();
            isClearFlag = false;
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).
                    putBoolean(Constants_CED.PREF_NOTIFICATION_CLEAR_FLAG, isClearFlag);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).commit();
        }
        msContentText = message + "\n";
        arrayDeque.addFirst(new JijoNotification(msContentText));

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(title)
                .setContentText(msContentText)
                .setNumber(++iMessage)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCategory(Notification.CATEGORY_EVENT)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri)
                .setGroup("group");

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(title);
        for (JijoNotification js : arrayDeque) {
            inboxStyle.addLine(js.getContentLine());
        }
        builder.setStyle(inboxStyle);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(Constants_CED.SERVER_NOTIFICATION_ID /* ID of notification */,
                builder.build());
    }

    private Intent getRightIntent() {
        Intent intent = null;
        if (CGlobals_CED.getInstance().isInTrip(getApplicationContext())) {
            intent = new Intent(this, CabEDriverForHire_act.class);
        } else if (CGlobals_CED.SERVICE_CURRENT == CGlobals_CED.SERVICE_CMD) {
            intent = new Intent(this, CabEMainDriver_act.class);
        } else if (CGlobals_CED.SERVICE_CURRENT == CGlobals_CED.SERVICE_CMD) {
            intent = new Intent(this, DashBoard_Driver_act.class);
        }
        return intent;
    }
}

class JijoNotification {
    private String msContentLine;

    JijoNotification(String sContentLine) {
        msContentLine = sContentLine;
    }

    String getContentLine() {
        return msContentLine;
    }
}