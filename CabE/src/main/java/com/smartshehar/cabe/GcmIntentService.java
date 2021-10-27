package com.smartshehar.cabe;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.cabe.ui.CabEMain_act;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;


public class GcmIntentService extends IntentService {

    public static final String TAG = "GcmIntentService: ";
    NotificationManager mNotificationManager;
    private int mnMessages = 0;
    boolean mVibrateNotification = false;
    CGlobals_CabE mApp;
    private static int mNotifyNo = 0;
    CNotification trip = null;
    boolean showNotification = true;
    String sName = null;
    static String msContentText = "";

    public GcmIntentService() {
        super("GcmIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        mApp = CGlobals_CabE.getInstance();
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
                    sendNotification(extras.getString("Notice"));
                    Log.i(TAG, "Received: " + extras.toString());
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        CGlobals_lib_ss.getInstance().isNewNotification = true;
        mVibrateNotification = false;
        boolean receiveNotifications = CGlobals_lib_ss.getInstance()
                .getPersistentPreference(this)
                .getBoolean(Constants_CabE.PREF_RECEIVE_NOTIFICATIONS, false);
        boolean getCabAlerts = CGlobals_lib_ss.getInstance()
                .getPersistentPreference(this)
                .getBoolean(Constants_CabE.PREF_CAB_ALERTS, false);
        Uri soundUri = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        JSONObject jMsg;
        String sTripType = "";
        boolean itsMe = false;
        try {
            if (msg == null) {
                return;
            }
            if (TextUtils.isEmpty(msg.trim()) || msg.trim().equals("-1")) {
                return;
            }
            jMsg = new JSONObject(msg);
            String sNotificationCategory = jMsg.isNull("notification_category") ? ""
                    : jMsg.getString("notification_category");
            if (sNotificationCategory
                    .equals(Constants_CabE.NOTIFICATON_CATEGORY_UPGRADE_APP)) {
                upgradeNotification();
                return;
            }
            trip = new CNotification(msg, getApplicationContext());
            String sNotificationValue = CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(this)
                    .getString(Constants_CabE.PREF_NOTIFICATION_LIST_SAVED, "");
            ArrayList<String> notificationResultArrayList = new ArrayList<>();
            if (!TextUtils.isEmpty(sNotificationValue)) {
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                notificationResultArrayList = new Gson().fromJson(sNotificationValue, type);
            }
            if (notificationResultArrayList == null) {
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this)
                        .putString(Constants_CabE.PREF_NOTIFICATION_LIST_SAVED, "");
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).commit();
            }
            if (notificationResultArrayList != null) {
                notificationResultArrayList.add(0, msg);
            }
            if (notificationResultArrayList != null) {
                while (notificationResultArrayList.size() > Constants_CabE.MAX_NOTIFICATIONS) {
                    notificationResultArrayList.remove(notificationResultArrayList.size() - 1);
                }
            }
            String json = new Gson().toJson(notificationResultArrayList);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this)
                    .putString(Constants_CabE.PREF_NOTIFICATION_LIST_SAVED, json);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).commit();
        } catch (Exception exception) {
            SSLog_SS.e(TAG, "sendNotification: ", exception, this);
        }
        if (trip == null) {
            return;
        }
        if (trip.getNotificationType().equals("TB")
                && sTripType.equals(Constants_CabE.COMMERCIAL) && !getCabAlerts) {
            return;
        } else {
            showNotification = true;
        }
        if (CGlobals_lib_ss.getInstance().getPersistentPreference(this)
                .getString(Constants_CabE.PREF_PHONENO, "").equals(trip.getPhoneNo().trim())) {
            itsMe = true;
        } else {

            try {
                if (!TextUtils.isEmpty(trip.msUserName)) {
                    sName = trip.msUserName;
                }
                if (TextUtils.isEmpty(sName)) {
                    sName = trip.getPhoneNo();
                }
                if (TextUtils.isEmpty(sName)) {
                    sName = trip.msDriverEmail;
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "sendNotification - sName - ", e, this);
            }
        }
        if (itsMe) {
            sName = "Me";
            return;
        }
        Intent viewIntent = getRightIntent();

        if (trip.getNotificationCategory()
                .equals(Constants_CabE.NOTIFICATON_CATEGORY_DRIVER_TRIP) ||
                trip.getNotificationCategory()
                        .equals(Constants_CabE.NOTIFICATON_CATEGORY_COMMERCIAL_DRIVER_TRIP)) {
            if (trip.getNotificationType()
                    .equals(Constants_CabE.NOTIFICATON_TYPE_UPDATE_POSITION)) {
                showNotification = false;
                Location loc = mApp.getMyLocation(this);
                viewIntent.putExtra(
                        Constants_CabE.NOTIFICATON_TYPE_UPDATE_POSITION, true);
                soundUri = Uri.parse("android.resource://" + getPackageName()
                        + "/" + R.raw.piano_f);
                if (trip.getDriverLat() != Constants_CabE.INVALIDLAT
                        && trip.getDriverLng() != Constants_CabE.INVALIDLNG) {
                    if (loc != null) {
                        float results[] = new float[1];
                        Location.distanceBetween(loc.getLatitude(),
                                loc.getLongitude(), trip.getDriverLat(), trip.getDriverLng(),
                                results);
                        if (results[0] < Constants_CabE.DEFAULT_WALKING_DISTANCE * 1000) {
                            showNotification = true;
                        }
                    }
                } else {
                    showNotification = true;
                }
            } else if (trip.getNotificationType()
                    .equals(Constants_CabE.NOTIFICATON_TYPE_DRIVER_MOVED)) {
                showNotification = true;
                viewIntent.putExtra(Constants_CabE.NOTIFICATON_TYPE_DRIVER_MOVED,
                        true);
                soundUri = Uri.parse("android.resource://" + getPackageName()
                        + "/" + R.raw.falling_asteroid);
                mVibrateNotification = true;

                Location loc = mApp.getMyLocation(this);
                if (trip.getDriverLat() != Constants_CabE.INVALIDLAT
                        && trip.getDriverLng() != Constants_CabE.INVALIDLNG) {
                    if (loc != null) {
                        float results[] = new float[1];
                        Location.distanceBetween(loc.getLatitude(),
                                loc.getLongitude(), trip.getDriverLat(), trip.getDriverLng(),
                                results);
                    }
                }
            } else if (trip.getNotificationType()
                    .equals(Constants_CabE.NOTIFICATON_TYPE_JUMP_IN)) {

                showNotification = true;
                viewIntent.putExtra(Constants_CabE.NOTIFICATON_TYPE_JUMP_IN,
                        true);
                soundUri = Uri.parse("android.resource://" + getPackageName()
                        + "/" + R.raw.piano_c);
                mVibrateNotification = true;

            } else if (trip.getNotificationMessage()
                    .equals("Trip created")) {
                showNotification = true;
                viewIntent.putExtra(Constants_CabE.NOTIFICATON_TYPE_JUMP_IN,
                        true);
                soundUri = Uri.parse("android.resource://" + getPackageName()
                        + "/" + R.raw.muffled_laser);
                mVibrateNotification = true;
            }
        }
        showNotification = receiveNotifications;
        if (!showNotification) {
            return;
        }
        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        viewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, viewIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotifyNo++;
        if (mNotifyNo > 9) {
            mNotifyNo = 1;
            msContentText = "";
        }
        if (!TextUtils.isEmpty(trip.getName())) {
            sName = trip.getName();
        }
        boolean isClearFlag = CGlobals_lib_ss.getInstance().getPersistentPreference(this).
                getBoolean(Constants_CabE.PREF_NOTIFICATION_CLEAR_FLAG, false);
        if (isClearFlag) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).
                    putBoolean(Constants_CabE.PREF_NOTIFICATION_CLEAR_FLAG, false);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).commit();
        }
        msContentText += trip.getNotificationText() + "\n";
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("CabE trip")
                .setStyle(
                        new NotificationCompat.BigTextStyle()
                                .bigText(trip.getNotificationText()))
                .setNumber(++mnMessages)
                .setSound(soundUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentText(trip.getNotificationText());
        if (mVibrateNotification) {
            mBuilder.setLights(Color.RED, 500, 500);
            long[] pattern = {500, 500, 500, 500};
            mBuilder.setVibrate(pattern);
        }
        mBuilder.setContentIntent(contentIntent);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("CabE trip");
        mBuilder.setStyle(inboxStyle);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.ledARGB = 0xff00ff00;
        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;
        mNotificationManager.notify(Constants_CabE.SERVER_NOTIFICATION_ID,
                notification);
    }

    @SuppressWarnings("deprecation")
    public void upgradeNotification() {
        String sMessage = "New version available. \nDownload now";
        String appPackageName = "com.jumpinjumpout.apk";
        Intent marketIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appPackageName));
        marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                marketIntent, 0);
        CGlobals_lib_ss
                .getInstance()
                .getPersistentPreferenceEditor(this)
                .putInt(Constants_CabE.PREF_VERSION_CODE,
                        CGlobals_CabE.miAppVersionCode);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(this).commit();
        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Jump.in.Jump.out")
                .setStyle(
                        new NotificationCompat.BigTextStyle().bigText(sMessage))
                .setNumber(++mnMessages)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentText(sMessage);
        mBuilder.setContentIntent(contentIntent);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(Constants_CabE.SERVER_NOTIFICATION_ID,
                notification);
    }

    private Intent getRightIntent() {
        return new Intent(this, CabEMain_act.class);
    }
} // GcmIntentService