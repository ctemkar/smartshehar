package com.smartshehar.dashboard.app;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.dashboard.app.ui.ActNotification;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import lib.app.util.CGlobals_lib_ss;

public class GcmIntentService extends IntentService {
    private static final String TAG = GcmIntentService.class.getSimpleName();

    private boolean mVibrateNotification = false;
    private int mnMessages = 0;
    CGlobals_db mApp;
    SSApp myApplication;
    private static int mNotifyNo = 0;
    private static String msContentText = "";
    private NotificationManager mNotificationManager;
    CNotification mcNotification = null;
    NotificationCompat.Builder builder;
    public static Deque<SSAppNotification> arrayDeque = new ArrayDeque<>();

    public GcmIntentService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        myApplication = SSApp.getInstance();
        mApp = CGlobals_db.getInstance(GcmIntentService.this);
        mApp.getBestLocation(this);
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
        if (extras != null) {
            if (!extras.isEmpty()) { // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
                        .equals(messageType)) {
                    sendNotification("Send error: " + extras.toString());
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
                        .equals(messageType)) {
                    sendNotification("Deleted messages on server: "
                            + extras.toString());
                    // If it's a regular GCM message, do some work.
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                        .equals(messageType)) {
                    // This loop represents the service doing some work.
                    Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                    // Post notification of received message.
                    if(!TextUtils.isEmpty(extras.getString("Notice"))) {
                        sendNotification(extras.getString("Notice"));
                    }
                    Log.i(TAG, "Received: " + extras.toString());
                }

               /* handleChatIntent(intent);*/

            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        boolean mVibrateNotification = false;
        boolean receiveNotifications = CGlobals_lib_ss.getInstance()
                .getPersistentPreference(GcmIntentService.this)
                .getBoolean(Constants_dp.PREF_RECEIVE_NOTIFICATIONS, false);

        Uri soundUri = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        JSONObject jMsg;
        boolean showNotification = true;

        String mIssueType, mIssueSubType, mIssueTime, mMessage = null,
                mIssueAddress, mUniquekey, mIssueId;

        try {

            if (TextUtils.isEmpty(msg.trim()) || msg.trim().equals("-1")) {
                return;
            }

            // json object  receive
            jMsg = new JSONObject(msg);

            mMessage = jMsg.isNull("message") ? "" : jMsg.getString("message");
            mIssueAddress = jMsg.isNull("issue_address") ? "" : jMsg.getString("issue_address");
            mIssueType = jMsg.isNull("issue_category") ? "" : jMsg.getString("issue_category");
            mIssueSubType = jMsg.isNull("issue_subcategory") ? "" : jMsg.getString("issue_subcategory");
            mIssueTime = jMsg.isNull("issue_time") ? "" : jMsg.getString("issue_time");
            mUniquekey = jMsg.isNull("uniquekey") ? "" : jMsg.getString("uniquekey");
            mIssueId = jMsg.isNull("issueid") ? "" : jMsg.getString("issueid");

            // save notification
            mcNotification = new CNotification(mIssueType, mIssueSubType, mIssueTime,
                    mMessage,
                    mIssueAddress, Long.parseLong(mUniquekey), Integer.valueOf(mIssueId));

            String sNotificationValue = CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(GcmIntentService.this)
                    .getString(Constants_dp.PREF_NOTIFICATION_LIST_SAVED, "");

            ArrayList<String> notificationResultArrayList = new ArrayList<>();

            if (!TextUtils.isEmpty(sNotificationValue)) {
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                notificationResultArrayList = new Gson().fromJson(sNotificationValue, type);
            }
            if (notificationResultArrayList == null) {
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(GcmIntentService.this).
                        putString(Constants_dp.PREF_NOTIFICATION_LIST_SAVED, "").commit();

            }

            notificationResultArrayList.add(0, msg);
            while (notificationResultArrayList.size() > Constants_dp.MAX_NOTIFICATIONS) {
                notificationResultArrayList.remove(notificationResultArrayList.size() - 1);
            }
            String json = new Gson().toJson(notificationResultArrayList);
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(GcmIntentService.this).
                    putString(Constants_dp.PREF_NOTIFICATION_LIST_SAVED, json).commit();
//            }

        } catch (JSONException eJSONException) {
            SSLog.e(TAG, "sendNotification: ", eJSONException);
        } catch (Exception eException) {
            SSLog.e(TAG, "sendNotification: ", eException);
        }
        if (mcNotification == null) {
            return;
        }


        if (receiveNotifications) {
            showNotification = true;
        }
        if (!showNotification) {
            return;
        }
        ///go to notification activity
        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Intent viewIntent = new Intent(this, ActNotification.class);
        viewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        final PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotifyNo++;
        if (mNotifyNo > 9) {
            mNotifyNo = 1;
            msContentText = "";
            arrayDeque.pollLast();

        }
        //notification text
        String sNotification = mcNotification.getmType() + "\n";


        if (!TextUtils.isEmpty(mcNotification.getmType())) {
            mIssueType = mcNotification.getmType();
        }
        // text messages
        msContentText += mcNotification.getmType() + "\n";
        arrayDeque.addFirst(new SSAppNotification(mMessage));
        // show notification
        mnMessages = mnMessages++;
        Log.d(TAG, "mnMessage " + mnMessages + "   " + mMessage);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentText(mMessage)
                .setContentTitle("Smartshehar")
                .setSmallIcon(R.mipmap.ic_logo)
                .setNumber(mnMessages)
                .setSound(soundUri);


        if (mVibrateNotification) {
            mBuilder.setLights(Color.RED, 500, 500);
            long[] pattern = {500, 500, 500, 500};
            mBuilder.setVibrate(pattern);
        }
        /**/
        boolean isClearFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(GcmIntentService.this).
                getBoolean(Constants_dp.PREF_NOTIFICATION_CLEAR_FLAG, false);
        if (isClearFlag) {
            arrayDeque.clear();
            isClearFlag = false;
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(GcmIntentService.this).
                    putBoolean(Constants_dp.PREF_NOTIFICATION_CLEAR_FLAG, isClearFlag).apply();

        }
        /**/
        mBuilder.setContentIntent(contentIntent);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Smartshehar");
        for (SSAppNotification ssApp : arrayDeque) {
            inboxStyle.addLine(ssApp.getContentLine());
        }
        mBuilder.setStyle(inboxStyle);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.ledARGB = 0xff00ff00;
        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;

        mNotificationManager.notify(Constants_dp.SERVER_NOTIFICATION_ID,
                notification);


    }

    class SSAppNotification {
        String msContentLine;

        public SSAppNotification(String sContentLine) {
            msContentLine = sContentLine;
        }

        public String getContentLine() {
            return msContentLine;
        }
    }
    public static final String KEY = "key";
    public static final String TOPIC = "topic";
    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";
 /*   protected void handleChatIntent(Intent intent) {
        String key = intent.getStringExtra(KEY);
        switch (key) {
            case SUBSCRIBE:
                // subscribe to a topic
                String topic = intent.getStringExtra(TOPIC);
                subscribeToTopic(topic);
                break;
            case UNSUBSCRIBE:
                String topic1 = intent.getStringExtra(TOPIC);
                unsubscribeFromTopic(topic1);
                break;
            default:
                // if key is not specified, register with GCM
//                registerGCM();
        }

    }*/

    /**
     * Subscribe to a topic
     */
    public void subscribeToTopic(String topic) {
        GcmPubSub pubSub = GcmPubSub.getInstance(getApplicationContext());
        InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
        String token = null;
        try {
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            if (token != null) {
                pubSub.subscribe(token, "/topics/" + topic, null);
                Log.e(TAG, "Subscribed to topic: " + topic);
            } else {
                Log.e(TAG, "error: gcm registration id is null");
            }
        } catch (IOException e) {
            Log.e(TAG, "Topic subscribe error. Topic: " + topic + ", error: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Topic subscribe error. Topic: " + topic + ", error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void unsubscribeFromTopic(String topic) {
        GcmPubSub pubSub = GcmPubSub.getInstance(getApplicationContext());
        InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
        String token = null;
        try {
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            if (token != null) {
                pubSub.unsubscribe(token, "");
                Log.e(TAG, "Unsubscribed from topic: " + topic);
            } else {
                Log.e(TAG, "error: gcm registration id is null");
            }
        } catch (IOException e) {
            Log.e(TAG, "Topic unsubscribe error. Topic: " + topic + ", error: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Topic subscribe error. Topic: " + topic + ", error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

} // GcmIntentService

