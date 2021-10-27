package com.jumpinjumpout.apk.driver.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.ui.IconGenerator;
import com.jumpinjumpout.apk.driver.CGlobals_driver;
import com.jumpinjumpout.apk.driver.Constants_driver;
import com.jumpinjumpout.apk.driver.LocationService_cab;
import com.jumpinjumpout.apk.driver.MyApplication;
import com.jumpinjumpout.apk.driver.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.lib.ui.Driver_act;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lib.app.util.MyLocation;

public class ForHire_act extends Driver_act {
    protected static final String TAG = "ForHire_act: ";
    protected Handler handler = new Handler();
    boolean isImage = false;
    CTrip cTripDriver;
    List<CTrip> mTrips;
    String booking_time, planned_start_datetime, triptime, trip_end_notification_sent;
    int secs_to_confirm, driver_appuser_id, passenger_appuser_id, shared_cab, is_for_hire, allowstrangernotifications,
            cab_trip_id, hasdriver_started;
    Bitmap bmp = null;
    boolean isForHire = false;
    String trip_action, userName = "";
    boolean tripcancel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_floating_action_menu_forhire);
        create();
        driverCreate();
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        mlActivePassengerMarkers = new ArrayList<Marker>();

        getSharedPreferencesEditor()
                .putString(Constants_driver.PREF_INVITE_USER_TO_APP, "");
        getSharedPreferencesEditor()
                .commit();

        mTvFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

            }
        });

        mButtonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canceldialog();
            }
        });

        final String response = MyApplication.getInstance().getPersistentPreference().
                getString(Constants_driver.PREF_SAVE_RESPONSE_PASSENGER, "");
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isForHire = true;
                Intent intent = new Intent(ForHire_act.this, ForHireSharedTrip_act.class);
                intent.putExtra("RESPONSEVALUE", "1");
                MyApplication.getInstance().getPersistentPreferenceEditor().
                        putBoolean("IS_FOR_HIRE_DRIVER", isForHire);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                startActivity(intent);
            }
        });
        CGlobals_lib.showGPSDialog = true;
    } // onCreate


    MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            CGlobals_driver.getInstance().setMyLocation(location, false);
            /*if (location != null
                    && (moFromAddress.hasLatitude() && moFromAddress
                    .hasLongitude())
                    && isInTrip()) {
                float results[] = new float[1];
                Location.distanceBetween(moToAddress.getLatitude(),
                        moToAddress.getLongitude(), location.getLatitude(),
                        location.getLongitude(), results);
            }*/
            CGlobals_driver.getInstance().sendUpdatePosition(mCurrentLocation, ForHire_act.this);
        }
    };

    @Override
    public void onClickInviteUsers(View v) {
    }

    protected Runnable runnable = new Runnable() {
        @Override
        public void run() {
            checkPassengerCancelTrip();
        }
    };

    protected void showActivePassengerMarkers(String result) {

    }

    private void checkPassengerCancelTrip() {
        final int cabtripid = MyApplication.getInstance().getPersistentPreference().
                getInt("CAB_TRIP_ID_DRIVER", 0);
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.GET_CAB_TRIP_STATUS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        passengerCancelTrip(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.postDelayed(runnable,
                        Constants_driver.DRIVER_UPDATE_INTERVAL_CANCELTRIP);
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(ForHire_act.this, sErr, Toast.LENGTH_SHORT).show();
                try {
                    SSLog.d(TAG,
                            "Failed to mVehicleNoVerify :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "mVehicleNoVerify - " + sErr,
                            error.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("cabtripid", String.valueOf(cabtripid));
                params = CGlobals_driver.getInstance().getMinAllMobileParams(params,
                        Constants_driver.GET_CAB_TRIP_STATUS_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_driver.GET_CAB_TRIP_STATUS_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "hasDriverAccepted", e);
                }
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void passengerCancelTrip(String response) {
        if (TextUtils.isEmpty(response)) {
            handler.postDelayed(runnable,
                    Constants_driver.DRIVER_UPDATE_INTERVAL_CANCELTRIP);
            return;
        }
        if (response.equals("-1")) {
            handler.postDelayed(runnable,
                    Constants_driver.DRIVER_UPDATE_INTERVAL_CANCELTRIP);
            return;
        }
        try {
            JSONObject jResponse = new JSONObject(response);
            trip_action = jResponse.isNull("trip_action") ? ""
                    : jResponse.getString("trip_action");
            if (trip_action.equals("A") && !tripcancel && !isInTrip()) {
                if (!ForHire_act.this.isFinishing()) {
                    String massage = "Customer has cancelled the trip.";
                    new AlertDialog.Builder(ForHire_act.this)
                            .setTitle("Cancel Trip")
                            .setMessage(massage)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    tripcancel = true;
                                    Intent intent = new Intent(ForHire_act.this, Dashboard_act.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .show();
                }
            }
        } catch (Exception e) {
            handler.postDelayed(runnable,
                    Constants_driver.DRIVER_UPDATE_INTERVAL_CANCELTRIP);
            e.printStackTrace();
        }
        handler.postDelayed(runnable,
                Constants_driver.DRIVER_UPDATE_INTERVAL_CANCELTRIP);
    }

    @Override
    protected void showResponseThumbImageClick(CTrip cTrip) {
    }

    @Override
    protected void setTripAction(String sTripStatus) {
        super.setTripAction(sTripStatus);
    }

    @Override
    protected void onResume() {
        MyLocation myLocation = new MyLocation(
                MyApplication.mVolleyRequestQueue, ForHire_act.this, mGoogleApiClient);
        myLocation.getLocation(this, locationResult);
        CGlobals_lib.getInstance().turnGPSOn1(ForHire_act.this, mGoogleApiClient);
        hasDriverMode = true;
        CGlobals_driver.getInstance().sendUpdatePosition(
                mCurrentLocation, ForHire_act.this);
        super.onResume();
        mMap.setOnCameraChangeListener(null);
        mRlFromTo.setVisibility(View.GONE);
        mLlMarkerLayout.setVisibility(View.GONE);
        mIvRedPin.setVisibility(View.GONE);
        mIvGreenPin.setVisibility(View.GONE);
        mTvSetLocation.setVisibility(View.GONE);
        mFlPassengerInfo.setVisibility(View.VISIBLE);
        mButtonGo.setVisibility(View.VISIBLE);
        mButtonStart.setVisibility(View.VISIBLE);
        mButtonGo.setText("Cancel");
        mButtonStart.setText("Start Trip");
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        int dpValue = 40;
        float d = ForHire_act.this.getResources().getDisplayMetrics().density;
        int margin = (int) (dpValue * d);
        lp.setMargins(0, 0, 0, margin);
        findViewById(com.jumpinjumpout.apk.lib.R.id.map).setLayoutParams(lp);
        bookCabPassenger();
        handler.postDelayed(runnable,
                Constants_driver.DRIVER_UPDATE_INTERVAL_CANCELTRIP);
    }

    private void bookCabPassenger() {
        String response = MyApplication.getInstance().getPersistentPreference().
                getString(Constants_driver.PREF_SAVE_RESPONSE_PASSENGER, "");
        Type type = new TypeToken<String>() {
        }.getType();
        String passengerinfo = new Gson().fromJson(response, type);
        try {
            JSONObject jResponse = new JSONObject(passengerinfo);
            cTripDriver = new CTrip(passengerinfo, ForHire_act.this);
            secs_to_confirm = jResponse.isNull("secs_to_confirm") ? 0
                    : jResponse.getInt("secs_to_confirm");
            driver_appuser_id = jResponse.isNull("driver_appuser_id") ? -1
                    : jResponse.getInt("driver_appuser_id");
            passenger_appuser_id = jResponse.isNull("passenger_appuser_id") ? -1
                    : jResponse.getInt("passenger_appuser_id");
            shared_cab = jResponse.isNull("shared_cab") ? -1
                    : jResponse.getInt("shared_cab");
            is_for_hire = jResponse.isNull("is_for_hire") ? -1
                    : jResponse.getInt("is_for_hire");
            allowstrangernotifications = jResponse.isNull("allowstrangernotifications") ? -1
                    : jResponse.getInt("allowstrangernotifications");
            booking_time = jResponse.isNull("booking_time") ? ""
                    : jResponse.getString("booking_time");
            cab_trip_id = jResponse.isNull("cab_trip_id") ? -1
                    : jResponse.getInt("cab_trip_id");
            planned_start_datetime = jResponse.isNull("planned_start_datetime") ? ""
                    : jResponse.getString("planned_start_datetime");
            triptime = jResponse.isNull("triptime") ? ""
                    : jResponse.getString("triptime");
            hasdriver_started = jResponse.isNull("hasdriver_started") ? -1
                    : jResponse.getInt("hasdriver_started");
            trip_end_notification_sent = jResponse.isNull("trip_end_notification_sent") ? ""
                    : jResponse.getString("trip_end_notification_sent");
            String sAddress = cTripDriver.getFrom().substring(0, 25) + "...";
            IconGenerator tc = new IconGenerator(this);
            bmp = tc.makeIcon(sAddress);
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(cTripDriver.getFromLat() + .001, cTripDriver.getFromLng()))
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp)));
            markerFrom = mMap
                    .addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(cTripDriver.getFromLat(), cTripDriver
                                            .getFromLng()))
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .alpha(0.8f).draggable(true)
                            .title(cTripDriver.getFrom()));

            findViewById(com.jumpinjumpout.apk.lib.R.id.llOneLine).setVisibility(View.VISIBLE);
            findViewById(com.jumpinjumpout.apk.lib.R.id.llTwoLine).setVisibility(View.GONE);
            findViewById(com.jumpinjumpout.apk.lib.R.id.llThreeLine).setVisibility(View.GONE);
            findViewById(com.jumpinjumpout.apk.lib.R.id.llPassengerInfo).setVisibility(View.GONE);
            findViewById(com.jumpinjumpout.apk.lib.R.id.llWaitingPassenger).setVisibility(View.GONE);
            findViewById(com.jumpinjumpout.apk.lib.R.id.ivPhoto).setVisibility(View.GONE);
            findViewById(com.jumpinjumpout.apk.lib.R.id.llJoinPassenger).setVisibility(View.GONE);
            String sMsg = "Passengers " + passengerTODriverDistance()+" km away";
            showStatusMessages(sMsg);
           /* if (!TextUtils.isEmpty(cTripDriver.getUserProfileImageFileName()) && !TextUtils.isEmpty(cTripDriver.getUserProfileImagePath())) {

                String url = Constants_driver.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + cTripDriver.getUserProfileImagePath() +
                        cTripDriver.getUserProfileImageFileName();
                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                if (Build.VERSION.SDK_INT < 11) {
                                    Toast.makeText(ForHire_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                } else {
                                    mIvPhoto.setImageBitmap(bitmap);
                                    mIvPhoto.setVisibility(View.VISIBLE);
                                    isImage = true;
                                }
                            }
                        }, 0, 0, null,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                isImage = false;
                            }
                        });
                MyApplication.getInstance().addToRequestQueue(request);
            }
            if (!isImage) {
                Bitmap bitmap = cTripDriver.getContactThnumbnail();
                if (bitmap != null) {
                    mIvPhoto.setImageBitmap(bitmap);
                }
            }
            mTvInfoBandLine4.setVisibility(View.VISIBLE);
            mTvInfoBandLine1.setVisibility(View.VISIBLE);
            mTvTime.setVisibility(View.VISIBLE);
            mTvMins.setVisibility(View.GONE);
            mtvJoinedCountNumber.setVisibility(View.VISIBLE);
            mTvJoinedCount.setVisibility(View.VISIBLE);
            mTvJumpedInCount.setVisibility(View.VISIBLE);
            mTvInfoBandLine4.setText((TextUtils.isEmpty(cTripDriver.getName()) ? ""
                    : cTripDriver.getName()));
            userName = cTripDriver.getName();
            mTvInfoBandLine1.setText(Html.fromHtml(passengerTODriverDistance()));
            mTvTime.setText((TextUtils.isEmpty(bookTime(booking_time)) ? ""
                    : bookTime(booking_time)));
//            mtvJoinedCountNumber.setText(triptime);
//            mTvJoinedCount.setText("mins");
            mTvJumpedInCount.setText(Html.fromHtml("Trip distance <br>" + String.valueOf(cTripDriver.getMiTripDistance() / 1000) + " Km"));*/
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CGlobals_lib.REQUEST_LOCATION) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    CGlobals_lib.showGPSDialog = false;
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    private String bookTime(String passengerbooking_time) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(passengerbooking_time));
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            String sTodayDate = df.format(c.getTime());
            return sTodayDate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getGeoCountryCode() {
        return MyApplication.getInstance().getPreference()
                .getString(Constants_driver.PREF_CURRENT_COUNTRY, "");
    }


    protected boolean showPath() {
        return isInTrip();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
    }

    @Override
    public void endTrip() {
    }

    public void driverTripSummary() {
    }

    protected void StartLocationService() {

    }

    protected void StopLocationService() {
        stopService(new Intent(this, LocationService_cab.class));
    }

    public void showStartDialog(int iTripId) {
        mIsGettingDirections = false;
        startTrip();
        progressCancel();

    }

    protected boolean hasTripEnded(Location location, double lat,
                                   double lng) {
        return false;

    }

    protected void inviteUsers() {
    }

    protected void sendTripAction(String sTripAction) {
        CGlobals_driver.getInstance().sendTripAction(sTripAction, ForHire_act.this);
    }

    public SharedPreferences.Editor getSharedPreferencesEditor() {
        return MyApplication.getInstance().getPersistentPreferenceEditor();
    }

    public void getInformation() {
    }


    protected String getTripUrl() {
        return Constants_driver.TRIP_CREATE_DRIVER_URL;
    }

    protected String getTripPathUrl() {
        return Constants_driver.TRIP_CREATE_TRIP_PATH_DRIVER_URL;
    }

    protected int getAppUserId() {
        return CGlobals_driver.getInstance().getAppUserId();
    }

    protected String getTripType() {
        return Constants_driver.TRIP_TYPE;
    }

    @Override
    protected String profileImageUrl() {
        return Constants_driver.GET_USER_PROFILE_IMAGE_FILE_NAME_URL;
    }

    @Override
    protected String passengertripsummayUrl() {
        return Constants_driver.JUMP_IN_URL;
    }

    protected String getEmailId() {
        return CGlobals_driver.getInstance().getEmailId();
    }

    public void showThumbImageClick() {/*
        try {
            String response = MyApplication.getInstance().getPersistentPreference().
                    getString(Constants_driver.PREF_SAVE_RESPONSE_PASSENGER, "");
            Type type = new TypeToken<String>() {
            }.getType();
            String passengerinfo = new Gson().fromJson(response, type);
            cTripDriver = new CTrip(passengerinfo, ForHire_act.this);
            String sptrip = CGlobals_lib.getInstance().getSharedPreferences(getApplicationContext()).
                    getString(Constants_lib.TRIP_TO_ADDRESS, "");
            final Dialog dialog = new Dialog(ForHire_act.this, R.style.DIALOG);
            dialog.setContentView(R.layout.passengerinfo_image);
            dialog.setTitle(cTripDriver.getName());

            final TextView mifromadd, mitoadd, milocation, miinternet,
                    mitripprogess;
            final ImageView mProImage;
            ImageButton mCall, mWhatsApp, mSMS;

            mProImage = (ImageView) dialog.findViewById(R.id.ivPassengerImage);
            miinternet = (TextView) dialog.findViewById(R.id.internet);
            milocation = (TextView) dialog.findViewById(R.id.clocation);
            mifromadd = (TextView) dialog.findViewById(R.id.tvName);
            mitoadd = (TextView) dialog.findViewById(R.id.tvPhoneNo);
            mitripprogess = (TextView) dialog.findViewById(R.id.tvKmAway);
            mCall = (ImageButton) dialog.findViewById(R.id.btnCall);
            mWhatsApp = (ImageButton) dialog.findViewById(R.id.btnWhatApp);
            mSMS = (ImageButton) dialog.findViewById(R.id.btnSMS);

            if (cTripDriver.getlTimeDiff() < 1) {
                miinternet.setText("Online");
                miinternet.setVisibility(View.VISIBLE);
            } else {
                miinternet.setText("Last online " + cTripDriver.getlTimeDiff() + " min. ago");
                miinternet.setVisibility(View.VISIBLE);
            }
            if (cTripDriver.getlTimeDifflocation() < 30) {
                milocation.setText("Current location available");
                milocation.setVisibility(View.VISIBLE);
            } else {
                milocation.setText("Last location recd. " + cTripDriver.getlTimeDifflocation() / 60 + " min. ago");
                milocation.setVisibility(View.VISIBLE);
            }

            if (isInTrip()) {
                mitoadd.setVisibility(View.GONE);
                mifromadd.setVisibility(View.GONE);
                mitripprogess.setText(passengerTODriverDistance() + " Km away");
                mitripprogess.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(cTripDriver.getUserProfileImageFileName()) && !TextUtils.isEmpty(cTripDriver.getUserProfileImagePath())) {
                    String url = Constants_driver.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + cTripDriver.getUserProfileImagePath() +
                            cTripDriver.getUserProfileImageFileName();
                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    if (Build.VERSION.SDK_INT < 11) {
                                        Toast.makeText(ForHire_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                    } else {
                                        mProImage.setRotation(cTripDriver.getMiImageRotation());
                                        mProImage.setImageBitmap(bitmap);
                                        isImage = true;
                                    }
                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                    isImage = false;
                                }
                            });
                    MyApplication.getInstance().addToRequestQueue(request);
                }
                if (!isImage) {
                    Bitmap bitmap = cTripDriver.getContactThnumbnail();
                    if (bitmap != null) {
                        mProImage.setImageBitmap(bitmap);
                    }
                }
                mCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + cTripDriver.getPhoneNo()));
                        startActivity(callIntent);
                    }
                });
                mWhatsApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendAppMsg(cTripDriver.getPhoneNo());
                    }
                });
                mSMS.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent smsVIntent = new Intent(Intent.ACTION_VIEW);
                        smsVIntent.setType("vnd.android-dir/mms-sms");
                        smsVIntent.putExtra("address", cTripDriver.getPhoneNo());
                        try {
                            startActivity(smsVIntent);
                        } catch (Exception ex) {
                            Toast.makeText(ForHire_act.this, "Your sms has failed...",
                                    Toast.LENGTH_LONG).show();
                            ex.printStackTrace();
                        }
                    }
                });
            }
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

    public void sendAppMsg(String phNo) {
        Uri uri = Uri.parse("smsto:" + phNo.trim());
        Intent intent = new Intent(Intent.ACTION_SEND, uri);
        intent.setType("text/plain");
        String text = "About my Jump in Jump out trip...";
        intent.setPackage("com.whatsapp");
        if (intent != null) {
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(intent, text));
        } else {
            Toast.makeText(this, "App not found", Toast.LENGTH_SHORT).show();
        }
    }

    protected void callLastInvitedTrip() {
    }

    private String passengerTODriverDistance() {
        String distanceFromMe = "";
        if (CGlobals_lib.getInstance().getMyLocation(ForHire_act.this) != null) {
            LatLng latlng = new LatLng(CGlobals_lib.getInstance().getMyLocation(ForHire_act.this).getLatitude(),
                    CGlobals_lib.getInstance().getMyLocation(ForHire_act.this).getLongitude());

            if (cTripDriver.getFromLat() != -999 && cTripDriver.getFromLng() != -999 && latlng != null) {
                float results[] = new float[1];
                Location.distanceBetween(cTripDriver.getFromLat(),
                        cTripDriver.getFromLng(), latlng.latitude,
                        latlng.longitude, results);
                try {
                    /*if (cTripDriver.getMiPassed() == 1) {
                        mTvInfoBandLine2.setText(cTripDriver.getMiPassed() > 0 ? " (Passed)" : "");
                        mTvInfoBandLine2.setVisibility(View.VISIBLE);
                        mTvInfoBandLine2.setTextColor(getResources().getColor(R.color.red));
                    } else if (results[0] < Constants_driver.NEAR_BE_READY_DISTANCE) {
                        distanceFromMe = "Be Ready!";
                        mTvInfoBandLine2.setText(distanceFromMe);
                        mTvInfoBandLine2.setVisibility(View.VISIBLE);
                    }*/
                    distanceFromMe = String.format("%.2f", results[0] / 1000);
                    return distanceFromMe;
                } catch (Exception e) {
                    SSLog.e(TAG, "getView", e);
                }
            }
        }
        return null;
    }

    protected void startTrip() {
    }

    protected String getUpdatePositionUrl() {
        return Constants_driver.UPDATE_POSITON_DRIVER_URL;
    }

    @Override
    protected void setTripActionFloatingbuttonHide() {

    }

    protected boolean isLoginflag() {
        return true;
    }

    private void canceldialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ForHire_act.this);
        alertDialog.setTitle("Are you sure do you cancel your trip?");
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                driverCancelCabTrip();
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        if (!ForHire_act.this.isFinishing()) {
            alertDialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        canceldialog();
    }

    private void driverCancelCabTrip() {

        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.CANCEL_CAB_TRIP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getCancelCabTrip(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(ForHire_act.this, sErr, Toast.LENGTH_SHORT).show();
                try {
                    SSLog.d(TAG,
                            "Failed to mVehicleNoVerify :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "mVehicleNoVerify - " + sErr,
                            error.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("cabtripid", String.valueOf(cab_trip_id));
                params = CGlobals_driver.getInstance().getMinAllMobileParams(params,
                        Constants_driver.CANCEL_CAB_TRIP_URL);
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void getCancelCabTrip(String response) {
        ForHire_act.this.finish();
    }

    protected void goFacebook() {
    }

    protected void goTwitter() {
    }

    protected void perfClear() {

    }

    protected boolean hasBusDriver() {
        return false;
    }

    protected boolean hasGetaCabTrip() {
        return false;
    }
}
