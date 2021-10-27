package com.jumpinjumpout.apk.user.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user pc on 17-11-2015.
 */
public class NotificationDistance_act extends Actrips_act {

    private int miTripId;
    private TextView tvNotiSubmit;
    protected List<LatLng> mLatLngTripPath;
    private String slatlngtripath;
    private int isCheckDestination = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout myLayout = (FrameLayout) findViewById(R.id.flNotiDes);
        View hiddenInfo = getLayoutInflater().inflate(
                R.layout.notification_destination, myLayout, false);
        myLayout.addView(hiddenInfo);
        slatlngtripath = MyApplication.getInstance().getPersistentPreference()
                .getString("LATLNG_TRIP_PATH", "");
        Type type = new TypeToken<List<LatLng>>() {
        }.getType();
        mLatLngTripPath = new Gson().fromJson(slatlngtripath, type);
        tvNotiSubmit = (TextView) findViewById(R.id.tvNotiSubmit);
        tvNotiSubmit.setText(getString(R.string.pleaseenterdestinationaddress));
        tvNotiSubmit.setVisibility(View.VISIBLE);
    }

    protected String longdistancemarathivehiclecategory() {
        String lDMVC = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.PREF_MARATHI_LONG_DISTANCE_VEHICLE_CATEGORY, "");
        return lDMVC;
    }

    protected void getCheckTripPathNotification() {
        miTripId = MyApplication.getInstance().getPersistentPreference()
                .getInt(Constants_user.PREF_TRIP_ID, -2);
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.CHECK_ISINTRIP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        checkLocation(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    SSLog.e(TAG, "getMyGroups: ErrorListener :-   ",
                            error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "getMyGroups: ErrorListener  -  ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("lat",
                        String.format("%.9f", fromLat));
                params.put("lng",
                        String.format("%.9f", fromLng));
                params.put("tolat",
                        String.format("%.9f", toLat));
                params.put("tolng",
                        String.format("%.9f", toLng));
                params.put("tripid", String.valueOf(miTripId));
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.CHECK_ISINTRIP_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.CHECK_ISINTRIP_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "getMemberDetails", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    @Override
    protected void getActiveTrips() {
    }

    public void checkLocation(String response) {
        if (TextUtils.isEmpty(response)) {
            return;
        }
        JSONObject person;
        try {
            person = new JSONObject(response);
            isCheckDestination = person.isNull("isintrip") ? 0 : person
                    .getInt("isintrip");
            if (isCheckDestination == 0) {
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLAT", (float) Constants_user.INVALIDLAT);
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLNG", (float) Constants_user.INVALIDLNG);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_START_ADDRESS", "");
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLAT", (float) Constants_user.INVALIDLAT);
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLNG", (float) Constants_user.INVALIDLNG);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_DESTINATION_ADDRESS", "");
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                tvNotiSubmit.setText(getString(R.string.notmatchnearestnode));
                tvNotiSubmit.setVisibility(View.VISIBLE);
                notiTextClick();
                return;
            } else if (isCheckDestination == 1) {
                Intent intent = new Intent();
                intent.putExtra("FROMLAT", fromLat);
                intent.putExtra("FROMLNG", fromLng);
                intent.putExtra("TOLAT", toLat);
                intent.putExtra("TOLNG", toLng);
                setResult(Activity.RESULT_OK, intent);
                finish();
            } else {
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLAT", (float) Constants_user.INVALIDLAT);
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLNG", (float) Constants_user.INVALIDLNG);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_START_ADDRESS", "");
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLAT", (float) Constants_user.INVALIDLAT);
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLNG", (float) Constants_user.INVALIDLNG);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_DESTINATION_ADDRESS", "");
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                tvNotiSubmit.setText(getString(R.string.notmatchnearestnode));
                tvNotiSubmit.setVisibility(View.VISIBLE);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void notiTextClick() {
        tvNotiSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLAT", (float) Constants_user.INVALIDLAT);
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLNG", (float) Constants_user.INVALIDLNG);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_START_ADDRESS", "");
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLAT", (float) Constants_user.INVALIDLAT);
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLNG", (float) Constants_user.INVALIDLNG);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_DESTINATION_ADDRESS", "");
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                Intent intent = new Intent(NotificationDistance_act.this, Active_trips_act.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideAllButton();
    }

    @Override
    protected void doGotIt() {

        String gotmessage = "You will see trips created by your friends and your commuting community members here." +
                " Only trips passing near your current location are shown." +
                "\n\nYou can filter the rides by changing your start-location (Jump.in point)" +
                " and/or entering a destination location (Jump.out point)";
        if (!isFinishing()) {
            CGlobals_user.getInstance().gotIt(this,
                    Constants_user.PREF_GOTIT_ACTIVE_TRIPS, "", gotmessage);
        }
    }

    @Override
    public void refresh() {
    }
}
