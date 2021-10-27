package com.smartshehar.cabe;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import lib.app.util.SSLog_SS;


public class CNotification extends CTripCabE {

    public static final String TAG = "CNotification: ";
    String sNotificationCategory = "";
    String sNotificationType = "";
    String sJumpin_Datetime = "";
    String sJumpout_Datetime = "";
    String msNotificationMessage = "";
    String msNotificationTime = "";
    String sJoinAddress = "";
    double dDriverLat = Constants_CabE.INVALIDLAT, dDriverLng = Constants_CabE.INVALIDLNG;
    int iJoined = 0;
    double dPassengerLat = Constants_CabE.INVALIDLAT, dPassengerLng = Constants_CabE.INVALIDLNG;
    String sToAddress;

    public CNotification(String sjDriver, Context context) {
        init(sjDriver, context);
        try {
            JSONObject jMsg = new JSONObject(sjDriver);
            msNotificationMessage = isNullNotDefined(jMsg, "message") ? ""
                    : jMsg.getString("message");
            sJoinAddress = jMsg.isNull("join_address") ? "" : jMsg
                    .getString("join_address");
            msNotificationTime = isNullNotDefined(jMsg,
                    "trip_action_time") ? "" : jMsg
                    .getString("trip_action_time");
            sNotificationCategory = jMsg.isNull("notification_category") ? ""
                    : jMsg.getString("notification_category");
            sNotificationType = jMsg.isNull("notification_type") ? "" : jMsg
                    .getString("notification_type");

            sJumpin_Datetime = jMsg.isNull("jumpin_datetime") ? "" : jMsg
                    .getString("jumpin_datetime");

            sJumpout_Datetime = jMsg.isNull("jumpout_datetime") ? "" : jMsg
                    .getString("jumpout_datetime");

            dDriverLat = jMsg.isNull("driver_lat") ? Constants_CabE.INVALIDLAT
                    : jMsg.getDouble("driver_lat");
            dDriverLng = jMsg.isNull("driver_lng") ? Constants_CabE.INVALIDLNG
                    : jMsg.getDouble("driver_lng");
            iJoined = jMsg.isNull("track_notify") ? 0 : jMsg
                    .getInt("track_notify");

            dPassengerLat = jMsg.isNull("pass_lat") ? Constants_CabE.INVALIDLAT
                    : jMsg.getDouble("pass_lat");
            dPassengerLng = jMsg.isNull("pass_lng") ? Constants_CabE.INVALIDLNG
                    : jMsg.getDouble("pass_lng");
            msName = getContactName(context, msPhoneno.trim());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public double getDriverLat() {
        return dDriverLat;
    }

    public double getDriverLng() {
        return dDriverLng;
    }

    public double getPassengerLat() {
        return dPassengerLat;
    }

    public double getPassengerLng() {
        return dPassengerLng;
    }

    public String getNotificationCategory() {
        return sNotificationCategory;
    }

    public String getNotificationType() {
        return sNotificationType;
    }

    public String getNotificationMessage() {
        return this.msNotificationMessage;
    }

    public String getNotificationTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        try {
            switch (sNotificationType) {
                case "JI":
                    if (!TextUtils.isEmpty(sJumpin_Datetime)) {
                        cal.setTime(sdf.parse(sJumpin_Datetime));
                        SimpleDateFormat rdf = new SimpleDateFormat("HH:mm");
                        msNotificationTime = rdf.format(cal.getTime());
                    }
                    break;
                case "JO":
                    if (!TextUtils.isEmpty(sJumpout_Datetime)) {
                        cal.setTime(sdf.parse(sJumpout_Datetime));
                        SimpleDateFormat rdf = new SimpleDateFormat("HH:mm");
                        msNotificationTime = rdf.format(cal.getTime());
                    }
                    break;
                default:
                    if (!TextUtils.isEmpty(msNotificationTime)) {
                        cal.setTime(sdf.parse(msNotificationTime));
                        SimpleDateFormat rdf = new SimpleDateFormat("HH:mm");
                        msNotificationTime = rdf.format(cal.getTime());
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.msNotificationTime;
    }

    public String getContactName(Context context, String phoneNo) {
        String contactName = "";

        if (!TextUtils.isEmpty(phoneNo)) {

            //ContentResolver cr = context.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(phoneNo));

            try {
                Cursor cursor = context.getContentResolver().query(uri,
                        new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null,
                        null);
                if (cursor == null) {
                    return null;
                }
                contactName = "";
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(cursor
                            .getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                }

                if (!cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "getContactName", e, context);
            }
        }
        return contactName;
    }

    public String getNotificationText() {
        String sUserName = "";
        String sname;
        if (!TextUtils.isEmpty(getMsUserUserName())) {
            sUserName = getMsUserUserName();
        }
        if (!TextUtils.isEmpty(getName())) {
            sname = getName();
        } else {
            if (!TextUtils.isEmpty(getPhoneNo())) {
                sname = getPhoneNo();
            } else {
                sname = "";
            }
        }

        if (getTo().length() < 20) {
            sToAddress = getTo();
        } else {
            sToAddress = getTo().substring(0, 20);
            sToAddress = sToAddress + "...";
        }

        String sNotification;
        switch (sNotificationType) {
            case Constants_CabE.NOTIFICATION_TYPE_BEGIN:
                sNotification = getNotificationTime() + " " + (TextUtils.isEmpty(getMsUserUserName()) ? sname : sUserName)
                        + " " + (TextUtils.isEmpty(msToSubLocality) ? " to " + sToAddress : " to " + msToSubLocality) + " "
                        + getNotificationMessage();
                break;
            case "PN":
                sNotification = getNotificationTime() + " " + (TextUtils.isEmpty(getMsFullName()) ? sname : sUserName)
                        + " - " + getNotificationMessage();
                break;
            case "JI":
                sNotification = getNotificationTime() + " " + getNotificationMessage();
                break;
            case "JO":
                sNotification = getNotificationTime() + " " + getNotificationMessage();
                break;
            default:
                sNotification = getNotificationTime() + " " + getNotificationMessage()
                        + (TextUtils.isEmpty(msToSubLocality) ? " " + sToAddress : " " + msToSubLocality) + " "
                        + (TextUtils.isEmpty(getMsFullName()) ? sname : sUserName);
                break;
        }
        return sNotification;
    }

     /*public void getCheckTripPathNotification(final Context context) {

       StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_CabE.CHECK_ISINTRIP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        checkLocation(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    SSLog_SS.e(TAG, "getMyGroups:- ", error, context);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getMyGroups:- ", e, context);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("lat", String.valueOf(CGlobals_lib_ss.getInstance().getMyLocation(context).getLatitude()));
                params.put("lng", String.valueOf(CGlobals_lib_ss.getInstance().getMyLocation(context).getLongitude()));
                params.put("tolat", String.valueOf(CGlobals_lib_ss.getInstance().getMyLocation(context).getLatitude()));
                params.put("tolng", String.valueOf(CGlobals_lib_ss.getInstance().getMyLocation(context).getLongitude()));
                params.put("tripid", String.valueOf(getTripId()));
                params = CGlobals_lib_ss.getInstance().getMinMobileParams(params,
                        Constants_CabE.CHECK_ISINTRIP_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_CabE.CHECK_ISINTRIP_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getMemberDetails", e, context);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, context);
    }

    public void checkLocation(String response) {
        if (TextUtils.isEmpty(response)) {
            return;
        }
        if (response.equals("false")) {
            return;
        }
    }*/
}
