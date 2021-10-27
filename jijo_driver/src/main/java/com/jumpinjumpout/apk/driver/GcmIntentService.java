package com.jumpinjumpout.apk.driver;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.jumpinjumpout.apk.driver.ui.ForHireSharedTrip_act;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.SSLog;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Deque;


public class GcmIntentService extends IntentService {

    public static final String TAG = "GcmIntentService: ";
    private NotificationManager mNotificationManager;
    private int mnMessages = 0;
    private boolean mVibrateNotification = false;
    CGlobals_driver mApp;
    MyApplication myApplication;
    private static int mNotifyNo = 0;
    private static String msContentText = "", msNotification = "";
    private String tripnotiTime = "";
    public static Deque<JijoNotification> arrayDeque = new ArrayDeque<JijoNotification>();
    CTrip trip = null;
    String sUserName = "";
    String sname = "";

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        myApplication = MyApplication.getInstance();
        mApp = CGlobals_driver.getInstance();
        mApp.getBestLocation(this);
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

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
                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                sendNotification(extras.getString("Notice"));
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mVibrateNotification = false;
        boolean receiveNotifications = MyApplication.getInstance()
                .getPersistentPreference()
                .getBoolean(Constants_driver.PREF_RECEIVE_NOTIFICATIONS, false);
        boolean getCabAlerts = MyApplication.getInstance()
                .getPersistentPreference()
                .getBoolean(Constants_driver.PREF_CAB_ALERTS, false);
        Uri soundUri = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        JSONObject jMsg = null;
        String sPhoneNo = "", sName = "", content = "", sJoinAddress = "";
        double dDriverLat = Constants_driver.INVALIDLAT, dDriverLng = Constants_driver.INVALIDLNG;
        String sNotificationCategory = "", sNotificationType = "", sTripActionTime = "";
        boolean showNotification = true;
        String sTripType = "";
        boolean itsMe = false;
        int iJoined = 0;
        try {
            jMsg = new JSONObject(msg);

            trip = new CTrip(msg, this);

            sNotificationCategory = jMsg.isNull("notification_category") ? ""
                    : jMsg.getString("notification_category");
            sNotificationType = jMsg.isNull("notification_type") ? "" : jMsg
                    .getString("notification_type");
            sPhoneNo = jMsg.isNull("phoneno") ? "" : jMsg.getString("phoneno");
            sName = getContactName(this, sPhoneNo.trim());
            content = jMsg.isNull("message") ? "" : jMsg.getString("message");
            sJoinAddress = jMsg.isNull("join_address") ? "" : jMsg
                    .getString("join_address");
            dDriverLat = jMsg.isNull("driver_lat") ? Constants_driver.INVALIDLAT
                    : jMsg.getDouble("driver_lat");
            dDriverLng = jMsg.isNull("driver_lng") ? Constants_driver.INVALIDLNG
                    : jMsg.getDouble("driver_lng");
            sTripActionTime = jMsg.isNull("trip_action_time") ? "" : jMsg
                    .getString("trip_action_time");
            sTripType = jMsg.isNull("triptype") ? "" : jMsg
                    .getString("triptype");
            iJoined = jMsg.isNull("track_notify") ? 0 : jMsg
                    .getInt("track_notify");


        } catch (Exception e) {
            e.printStackTrace();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        try {
            if (!TextUtils.isEmpty(sTripActionTime)) {
                cal.setTime(sdf.parse(sTripActionTime));
                SimpleDateFormat rdf = new SimpleDateFormat("HH:mm");
                tripnotiTime = rdf.format(cal.getTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sNotificationType.equals(Constants_lib.NOTIFICATION_TYPE_BEGIN)) {
            content = "Cab to ";
        }

        if (sNotificationType.equals("TB")
                && sTripType.equals(Constants_driver.TRIP_TYPE) && !getCabAlerts) {
            return;
        } else {
            showNotification = true;
        }
        if (myApplication.getPersistentPreference()
                .getString(Constants_driver.PREF_PHONENO, "").equals(sPhoneNo)) {
            itsMe = true;
        } else {

            try {
                if (!TextUtils.isEmpty(sName)) {
                    sName = sName;
                }
                if (TextUtils.isEmpty(sName)) {
                    if (!TextUtils.isEmpty(trip.msUserName)) {
                        sName = trip.msUserName;
                    }
                }
                if (TextUtils.isEmpty(sName)) {
                    sName = sPhoneNo;
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
        }
        Intent viewIntent = new Intent(this, ForHireSharedTrip_act.class);
        if (sNotificationCategory
                .equals(Constants_driver.NOTIFICATON_CATEGORY_UPGRADE_APP)) {
            upgradeNotification();
            return;
        }
        if (sNotificationCategory
                .equals(Constants_driver.NOTIFICATON_CATEGORY_DRIVER_TRIP)) {
            if (sNotificationType
                    .equals(Constants_driver.NOTIFICATON_TYPE_UPDATE_POSITION)) {
                showNotification = false;
                Location loc = mApp.getMyLocation(this);
                viewIntent.putExtra(
                        Constants_driver.NOTIFICATON_TYPE_UPDATE_POSITION, true);
                soundUri = Uri.parse("android.resource://" + getPackageName()
                        + "/" + R.raw.piano_f);

                if (dDriverLat != Constants_driver.INVALIDLAT
                        && dDriverLng != Constants_driver.INVALIDLNG) {
                    if (loc != null) {
                        float results[] = new float[1];
                        Location.distanceBetween(loc.getLatitude(),
                                loc.getLongitude(), dDriverLat, dDriverLng,
                                results);
                        if (results[0] < Constants_driver.DEFAULT_WALKING_DISTANCE * 1000) {
                            showNotification = true;
                        }
                    }
                } else {
                    showNotification = true;
                }
            } else if (sNotificationType
                    .equals(Constants_driver.NOTIFICATON_TYPE_DRIVER_MOVED)) {
                showNotification = true;
                viewIntent.putExtra(Constants_driver.NOTIFICATON_TYPE_DRIVER_MOVED,
                        true);
                soundUri = Uri.parse("android.resource://" + getPackageName()
                        + "/" + R.raw.falling_asteroid);
                mVibrateNotification = true;

                Location loc = mApp.getMyLocation(this);
                if (dDriverLat != Constants_driver.INVALIDLAT
                        && dDriverLng != Constants_driver.INVALIDLNG) {
                    if (loc != null) {
                        float results[] = new float[1];
                        Location.distanceBetween(loc.getLatitude(),
                                loc.getLongitude(), dDriverLat, dDriverLng,
                                results);
                    }
                }
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
        if (sNotificationType.equals("PN")) {
            if (!TextUtils.isEmpty(trip.getMsUserUserName())) {
                sUserName = trip.getMsUserUserName();
            }
            if (!TextUtils.isEmpty(trip.getName())) {
                sname = trip.getName();
            } else {
                if (!TextUtils.isEmpty(trip.getPhoneNo())) {
                    sname = trip.getPhoneNo();
                } else {
                    sname = "";
                }
            }
            msNotification = tripnotiTime + " " + content + " - " + (TextUtils.isEmpty(trip.getMsUserUserName()) ? sname : sUserName);
        } else {
            msNotification = tripnotiTime + " " + content + " - " + trip.getToSubLocality();
        }
        String sNotification = msNotification + "\n";
        if (msContentText.contains(sNotification)) {
            return;
        }
        msContentText += msNotification + "\n";
        arrayDeque.addFirst(new JijoNotification(msNotification));

        Notification.Builder mBuilder = new Notification.Builder(
                this)
                .setSmallIcon(R.mipmap.ic_trip_notification)
                .setContentTitle("Jump in Jump out")
                .setStyle(
                        new Notification.BigTextStyle()
                                .bigText(msContentText))
                .setNumber(++mnMessages)
                .setSound(soundUri)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText(msNotification);
        if (mVibrateNotification) {
            mBuilder.setLights(Color.RED, 500, 500);
            long[] pattern = {500, 500, 500, 500};
            mBuilder.setVibrate(pattern);
        }
        mBuilder.setContentIntent(contentIntent);
        Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
        inboxStyle.setBigContentTitle("Jump in Jump out");
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

        mNotificationManager.notify(Constants_driver.SERVER_NOTIFICATION_ID,
                notification);
    }

    public String getContactName(Context context, String phoneNo) {
        String contactName = "";

        if (!TextUtils.isEmpty(phoneNo)) {

            //ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNo));

            try {
                Cursor cursor = context.getContentResolver().query(uri,
                        new String[]{PhoneLookup.DISPLAY_NAME}, null, null,
                        null);
                if (cursor == null) {
                    return null;
                }
                contactName = "";
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(cursor
                            .getColumnIndex(PhoneLookup.DISPLAY_NAME));
                }

                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e) {
                SSLog.e(TAG, "getContactName", e);
            }
        }
        return contactName;
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
                .putInt(Constants_driver.PREF_VERSION_CODE,
                        CGlobals_driver.miAppVersionCode);

        MyApplication.getInstance().getPersistentPreferenceEditor().commit();

        NotificationManager notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder mBuilder = new Notification.Builder(
                this)
                .setSmallIcon(R.mipmap.ic_trip_notification)
                .setContentTitle("Jump.in.Jump.out")
                .setStyle(
                        new Notification.BigTextStyle().bigText(sMessage))
                .setNumber(++mnMessages)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText(sMessage);

        mBuilder.setContentIntent(contentIntent);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(Constants_driver.SERVER_NOTIFICATION_ID,
                notification);

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