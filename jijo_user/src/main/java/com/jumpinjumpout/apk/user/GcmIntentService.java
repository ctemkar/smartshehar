package com.jumpinjumpout.apk.user;

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
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.ui.FriendPool_act;
import com.jumpinjumpout.apk.user.ui.Notification_act;
import com.jumpinjumpout.apk.user.ui.ShareTripTrackDriver_act;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;


public class GcmIntentService extends IntentService {
    public static final String TAG = "GcmIntentService: ";

    NotificationManager mNotificationManager;
    //NotificationCompat.Builder builder;
    private int mnMessages = 0;
    boolean mVibrateNotification = false;
    CGlobals_user mApp;
    MyApplication myApplication;
    private static int mNotifyNo = 0;
    private static String msContentText = "";
    public static Deque<JijoNotification> arrayDeque = new ArrayDeque<JijoNotification>();
    CNotification trip = null;
    boolean showNotification = true;

    public GcmIntentService() {
        super("GcmIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        myApplication = MyApplication.getInstance();
        mApp = CGlobals_user.getInstance();
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
        CGlobals_lib.getInstance().isNewNotification = true;
        Intent sendableIntenttrack = new Intent("com.jumpinjumpout.apk.user.ui.TrackDriver_act");
        LocalBroadcastManager.getInstance(this).
                sendBroadcast(sendableIntenttrack);
        mVibrateNotification = false;
        boolean receiveNotifications = MyApplication.getInstance()
                .getPersistentPreference()
                .getBoolean(Constants_user.PREF_RECEIVE_NOTIFICATIONS, false);
        boolean getCabAlerts = MyApplication.getInstance()
                .getPersistentPreference()
                .getBoolean(Constants_user.PREF_CAB_ALERTS, false);
        Uri soundUri = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        JSONObject jMsg = null;
        String sTripType = "";
        boolean itsMe = false;
        String sName = null;
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
                    .equals(Constants_user.NOTIFICATON_CATEGORY_UPGRADE_APP)) {
                upgradeNotification();
                return;
            }

            trip = new CNotification(msg, getApplicationContext());

            if (trip.getNotificationType().equals("CCTD")) {
                CGlobals_user.getInstance().isCabCancelDriverNiotification = true;
                Intent sendableIntent = new Intent("com.jumpinjumpout.apk.user.ui.GetaCab_act");
                LocalBroadcastManager.getInstance(this).
                        sendBroadcast(sendableIntent);
            }

            String sNotificationValue = MyApplication.getInstance()
                    .getPersistentPreference()
                    .getString(Constants_user.PREF_NOTIFICATION_LIST_SAVED, "");

            ArrayList<String> notificationResultArrayList = new ArrayList<String>();

            if (!TextUtils.isEmpty(sNotificationValue)) {
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                notificationResultArrayList = new Gson().fromJson(sNotificationValue, type);
            }
            if (notificationResultArrayList == null) {
                MyApplication.getInstance().getPersistentPreferenceEditor()
                        .putString(Constants_user.PREF_NOTIFICATION_LIST_SAVED, "");
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
            }

            notificationResultArrayList.add(0, msg);

            while (notificationResultArrayList.size() > Constants_user.MAX_NOTIFICATIONS) {
                notificationResultArrayList.remove(notificationResultArrayList.size() - 1);
            }

            String json = new Gson().toJson(notificationResultArrayList);
            MyApplication.getInstance().getPersistentPreferenceEditor()
                    .putString(Constants_user.PREF_NOTIFICATION_LIST_SAVED, json);
            MyApplication.getInstance().getPersistentPreferenceEditor().commit();

        } catch (JSONException eJSONException) {
            SSLog.e(TAG, "sendNotification: ", eJSONException);
        } catch (Exception eException) {
            SSLog.e(TAG, "sendNotification: ", eException);
        }
        if (trip == null) {
            return;
        }
        if (trip.getNotificationType().equals("TB")
                && sTripType.equals(Constants_user.COMMERCIAL) && !getCabAlerts) {
            return;
        } else {
            showNotification = true;
        }
        if (myApplication.getPersistentPreference()
                .getString(Constants_user.PREF_PHONENO, "").equals(trip.getPhoneNo().trim())) {
            itsMe = true;
        } else {

            try {
                if (!TextUtils.isEmpty(sName)) {
                    sName = trip.getName();
                }
                if (TextUtils.isEmpty(sName)) {
                    if (!TextUtils.isEmpty(trip.msUserName)) {
                        sName = trip.msUserName;
                    }
                }
                if (TextUtils.isEmpty(sName)) {
                    sName = trip.getPhoneNo();
                }
                if (TextUtils.isEmpty(sName)) {
                    sName = trip.msDriverEmail;
                }
            } catch (Exception e) {
                SSLog.e(TAG, "sendNotification - sName - ", e);
            }
        }
        if (itsMe) {
            sName = "Me";
            return;
        }
        Intent viewIntent = getRightIntent();

        if (trip.getNotificationCategory()
                .equals(Constants_user.NOTIFICATON_CATEGORY_DRIVER_TRIP) ||
                trip.getNotificationCategory()
                        .equals(Constants_user.NOTIFICATON_CATEGORY_COMMERCIAL_DRIVER_TRIP)) {
            if (trip.getNotificationType()
                    .equals(Constants_user.NOTIFICATON_TYPE_UPDATE_POSITION)) {
                showNotification = false;
                Location loc = mApp.getMyLocation(this);
                viewIntent.putExtra(
                        Constants_user.NOTIFICATON_TYPE_UPDATE_POSITION, true);
                soundUri = Uri.parse("android.resource://" + getPackageName()
                        + "/" + R.raw.piano_f);

                if (trip.getDriverLat() != Constants_user.INVALIDLAT
                        && trip.getDriverLng() != Constants_user.INVALIDLNG) {
                    if (loc != null) {
                        float results[] = new float[1];
                        Location.distanceBetween(loc.getLatitude(),
                                loc.getLongitude(), trip.getDriverLat(), trip.getDriverLng(),
                                results);
                        if (results[0] < Constants_user.DEFAULT_WALKING_DISTANCE * 1000) {
                            showNotification = true;
                        }
                    }
                } else {
                    showNotification = true;
                }
            } else if (trip.getNotificationType()
                    .equals(Constants_user.NOTIFICATON_TYPE_DRIVER_MOVED)) {
                showNotification = true;
                viewIntent.putExtra(Constants_user.NOTIFICATON_TYPE_DRIVER_MOVED,
                        true);
                soundUri = Uri.parse("android.resource://" + getPackageName()
                        + "/" + R.raw.falling_asteroid);
                mVibrateNotification = true;

                Location loc = mApp.getMyLocation(this);
                if (trip.getDriverLat() != Constants_user.INVALIDLAT
                        && trip.getDriverLng() != Constants_user.INVALIDLNG) {
                    if (loc != null) {
                        float results[] = new float[1];
                        Location.distanceBetween(loc.getLatitude(),
                                loc.getLongitude(), trip.getDriverLat(), trip.getDriverLng(),
                                results);
                    }
                }
            } else if (trip.getNotificationType()
                    .equals(Constants_user.NOTIFICATON_TYPE_JUMP_IN)) {

                showNotification = true;
                viewIntent.putExtra(Constants_user.NOTIFICATON_TYPE_JUMP_IN,
                        true);
                soundUri = Uri.parse("android.resource://" + getPackageName()
                        + "/" + R.raw.piano_c);
                mVibrateNotification = true;

            } else if (trip.getNotificationMessage()
                    .equals("Trip created")) {
                showNotification = true;
                viewIntent.putExtra(Constants_user.NOTIFICATON_TYPE_JUMP_IN,
                        true);
                soundUri = Uri.parse("android.resource://" + getPackageName()
                        + "/" + R.raw.muffled_laser);
                mVibrateNotification = true;
            }
        }
        if (receiveNotifications) {
            showNotification = true;
        } else {
            showNotification = false;
        }
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
            arrayDeque.pollLast();
        }
        String sNotification = trip.getNotificationText() + "\n";

        /*if (msContentText.contains(sNotification)) {
            return;
        }*/

        double dCurrentLat = CGlobals_user.getInstance().getMyLocation(this).getLatitude();
        double dCurrentLng = CGlobals_user.getInstance().getMyLocation(this).getLongitude();
        if (trip.equals(Constants_lib.NOTIFICATION_TYPE_BEGIN)) {
            if (Math.abs(trip.getPassengerLat() - dCurrentLat) >= Constants_user.CHECKNOTIFICATIONDISTANCE &&
                    Math.abs(trip.getPassengerLng() - dCurrentLng) >= Constants_user.CHECKNOTIFICATIONDISTANCE) {
                return;
            } else {
                trip.getCheckTripPathNotification(this);
            }
        }
        if (!TextUtils.isEmpty(trip.getName())) {
            sName = trip.getName();
        }
        boolean isClearFlag = MyApplication.getInstance().getPersistentPreference().
                getBoolean(Constants_user.PREF_NOTIFICATION_CLEAR_FLAG, false);
        if (isClearFlag) {
            arrayDeque.clear();
            isClearFlag = false;
            MyApplication.getInstance().getPersistentPreferenceEditor().
                    putBoolean(Constants_user.PREF_NOTIFICATION_CLEAR_FLAG, isClearFlag);
            MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        }
        msContentText += trip.getNotificationText() + "\n";
        arrayDeque.addFirst(new JijoNotification(trip.getNotificationText()));

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this)
                .setSmallIcon(R.mipmap.ic_trip_notification)
                .setContentTitle("JumpInJumpOut trip")
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
        // .setContentText(sTripActionTime + " " + title);
        mBuilder.setContentIntent(contentIntent);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("JumpInJumpOut trip");
        // String prevNotification = "";
        for (JijoNotification js : arrayDeque) {
            inboxStyle.addLine(js.getContentLine());
        }
        mBuilder.setStyle(inboxStyle);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.ledARGB = 0xff00ff00;
        notification.ledOnMS = 300;
        notification.ledOffMS = 1000;

        mNotificationManager.notify(Constants_user.SERVER_NOTIFICATION_ID,
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
        MyApplication
                .getInstance()
                .getPersistentPreferenceEditor()
                .putInt(Constants_user.PREF_VERSION_CODE,
                        CGlobals_user.miAppVersionCode);

        MyApplication.getInstance().getPersistentPreferenceEditor().commit();

        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this)
                .setSmallIcon(R.mipmap.ic_trip_notification)
                .setContentTitle("Jump.in.Jump.out")
                .setStyle(
                        new NotificationCompat.BigTextStyle().bigText(sMessage))
                .setNumber(++mnMessages)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentText(sMessage);

        mBuilder.setContentIntent(contentIntent);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(Constants_user.SERVER_NOTIFICATION_ID,
                notification);

    }

    private Intent getRightIntent() {
        Intent intent = null;
        boolean hasJoinedTrip = MyApplication.getInstance().getPersistentPreference()
                .getBoolean(Constants_user.PREF_JOINED_TRIP, false);
        if (CGlobals_lib.getInstance().isInTrip(getApplicationContext())) {
            intent = new Intent(this, FriendPool_act.class);
        } else if (hasJoinedTrip) {
            MyApplication.getInstance().getPersistentPreferenceEditor().
                    putBoolean(Constants_user.GET_A_CAB_VALUE, false);
            MyApplication.getInstance().getPersistentPreferenceEditor().commit();
            intent = new Intent(this, ShareTripTrackDriver_act.class);
        } else {
            intent = new Intent(this, Notification_act.class);
        }
        return intent;
    }
} // GcmIntentService

class JijoNotification {
    String msContentLine;

    public JijoNotification(String sContentLine) {
        msContentLine = sContentLine;
    }

    public String getContentLine() {
        return msContentLine;
    }
}