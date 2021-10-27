package com.smartshehar.cabe;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.smartshehar.cabe.ui.CabEMain_act;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Deque;

import lib.app.util.CGlobals_lib_ss;

/**
 * Created by soumen on 10-11-2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private int iMessage = 0;
    private static int mNotifyNo = 0;
    static String msContentText = "";
    public static Deque<JijoNotification> arrayDeque = new ArrayDeque<JijoNotification>();

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @SuppressLint("NewApi")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData().get("message"));
            sendNotification(remoteMessage);
        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    Intent intent;
    int upgrade_app;
    String link;

    private void sendNotification(RemoteMessage messageBody) {
        String message = messageBody.getData().get("message");
        String title = messageBody.getData().get("title");
        String tickerText = messageBody.getData().get("tickerText");
        String vibrate = messageBody.getData().get("vibrate");
        String sound = messageBody.getData().get("sound");
        String subtitle = messageBody.getData().get("subtitle");
        String smallIcon = messageBody.getData().get("smallIcon");
        String largeIcon = messageBody.getData().get("largeIcon");
        String other = messageBody.getData().get("other");
        String notification_type = messageBody.getData().get("notification_type");
        String notification_category = messageBody.getData().get("notification_category");
        try {
            JSONObject jResponse = new JSONObject(other);
            link = CGlobals_CabE.isNullNotDefined(jResponse, "link") ?
                    "" : jResponse.getString("link");

            upgrade_app = CGlobals_CabE.isNullNotDefined(jResponse, "upgrade_app") ?
                    0 : jResponse.getInt("upgrade_app");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (notification_type.equals("TA_B")) {
            CGlobals_CabE.getInstance().isNewNotification = true;
        }

        if (upgrade_app == 1) {
            Uri uri = Uri.parse(link);
            intent = new Intent(Intent.ACTION_VIEW, uri);
        } else {
            intent = new Intent(this, CabEMain_act.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotifyNo++;
        if (mNotifyNo > 9) {
            mNotifyNo = 1;
            msContentText = "";
            arrayDeque.pollLast();
        }
        boolean isClearFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(this).
                getBoolean(Constants_CabE.PREF_NOTIFICATION_CLEAR_FLAG, false);
        if (isClearFlag) {
            arrayDeque.clear();
            isClearFlag = false;
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).
                    putBoolean(Constants_CabE.PREF_NOTIFICATION_CLEAR_FLAG, isClearFlag);
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

        notificationManager.notify(Constants_CabE.SERVER_NOTIFICATION_ID /* ID of notification */,
                builder.build());
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