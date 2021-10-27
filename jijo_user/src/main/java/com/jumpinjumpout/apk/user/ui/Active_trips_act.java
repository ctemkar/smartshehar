package com.jumpinjumpout.apk.user.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CAddress;

/**
 * Created by user pc on 23-04-2015.
 */
public class Active_trips_act extends Actrips_act {

    CTrip trip = null;
    JSONArray majActiveUsers = null;
    static public String msStatus = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showpDialog();
        getActiveTrips();
    }

    public void removeActiveUserFragment() {
        try {
            ActiveTripList_frag fragment = (ActiveTripList_frag) getFragmentManager()
                    .findFragmentById(R.id.active_trips_list_fragment_container);
            if (fragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager
                        .beginTransaction();
                fragmentTransaction.remove(fragment);
                fragmentTransaction.commit();
            }
        } catch (Exception e) {
            SSLog.e(TAG, "removeActiveUserFragment: ", e);
        }
    }

    public void addActiveUsersFragment() {
        try {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            ActiveTripList_frag fragment = new ActiveTripList_frag();
            fragmentTransaction.add(R.id.active_trips_list_fragment_container,
                    fragment);
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Exception e) {
            SSLog.e(TAG, "addActiveUsersFragment: ", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.active_trips_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action mPassengerTrip click
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                getActiveTrips();
                break;
            case R.id.menu_help:
                doGotIt();
                break;
            case R.id.menu_location:
                mCurrentLocation = CGlobals_user.getInstance().getBestLocation(Active_trips_act.this);
                if (mCurrentLocation != null) {
                    fromLat = mCurrentLocation.getLatitude();
                    fromLng = mCurrentLocation.getLongitude();
                    isLocation = false;
                    startIntentService(mCurrentLocation);
                } else {
                    CGlobals_lib.getInstance().turnGPSOn1(Active_trips_act.this, mGoogleApiClient);
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void refresh() {
        if (TextUtils.isEmpty(mTvTo.getText().toString())) {
            CGlobals_user.getInstance().setFromAddr(new CAddress());
        }
        removeActiveUserFragment();
        addActiveUsersFragment();
    }

    public void doGotIt() {
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
    protected void onPause() {
        removeActiveUserFragment();
        super.onPause();
    }

    void formList(String response) {
        try {
            userTrips = new ArrayList<CTrip>();
            cabTrips = new ArrayList<CTrip>();
            if (userTrips.size() > 0 || cabTrips.size() > 0) {
                userTrips.clear();
                cabTrips.clear();
            }
            if (response.trim().equals("-1")) {
                refresh();
                return;
            }
            showpDialog();
            majActiveUsers = new JSONArray(response);
            int nPassengers = majActiveUsers.length();
            if (nPassengers == 0) {
            }
            boolean isAllowStrangers = MyApplication
                    .getInstance()
                    .getPersistentPreference()
                    .getBoolean(Constants_user.PREF_NOTIFICATION_STRANGER,
                            false);
            boolean isCabAvailable = false;
            for (int j = 0; j < nPassengers; j++) {
                trip = new CTrip(majActiveUsers.getJSONObject(j).toString(), Active_trips_act.this);
                if (trip.getCabInPlay() > 0) {
                }

                if (!trip.getIsFriend() && !isAllowStrangers
                        && !trip.isCommercial())
                    continue;
                if (Actrips_act.getFilterTrip() && trip.getInPath() < 1) {
                    continue;
                }
                if (trip.isTripCommercial().equals(Constants_user.TRIP_TYPE_USER)) {
                    Actrips_act.userTrips.add(trip);
                } else if (trip.isTripCommercial().equals(Constants_user.TRIP_TYPE_COMMERCIAL)) {
                    Actrips_act.cabTrips.add(trip);
                    isCabAvailable = true;
                }
            }
            if (isCabAvailable) {
                showCabButton();
            } else {
                hideCabButton();
            }
            hidepDialog();
        } catch (JSONException e) {
            hidepDialog();
            String sErr = "";
            if (Active_trips_act.this == null) {
                sErr = " getActivity returned null";
            } else {
                sErr = " getActivity.getApplicationContext returned null";
            }
            SSLog.e(TAG, "formList - " + response + ", " + sErr, e);
        } catch (Exception e) {
            hidepDialog();
            SSLog.e(TAG, "formList - ", e);
            if (Actrips_act.msTripType.equals(Constants_user.TRIP_TYPE_USER)) {
                if (Actrips_act.cabTrips == null) {
                } else {
                    msStatus = "Something went wrong. Please go back to dashboard and try again";
                }
            } else if (Actrips_act.msTripType.equals(Constants_user.TRIP_TYPE_COMMERCIAL)) {
                if (Actrips_act.cabTrips == null) {
                    msStatus = "Something went wrong. Please go back to dashboard and try again";
                } else {
                }
            }
        }
        refresh();
    } // formList

    protected void getActiveTrips() {
        showpDialog();
        userTrips = new ArrayList<CTrip>();
        cabTrips = new ArrayList<CTrip>();
        if (userTrips.size() > 0 || cabTrips.size() > 0) {
            userTrips.clear();
            cabTrips.clear();
        }
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());

        if (fromLat == Constants_user.INVALIDLAT
                || fromLng == Constants_user.INVALIDLNG) {
            return;
        }
        final String url = Constants_user.ACTIVE_USERS_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        gotActiveCommercial(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    hidepDialog();
                    getActiveUsersError(error);
                    SSLog.e(TAG, Constants_user.ACTIVE_USERS_URL
                                    + ": Response.ErrorListener - ",
                            error.getMessage());
                } catch (Exception e) {
                    hidepDialog();
                    SSLog.e(TAG,
                            " getActiveUsersCommercial Response.ErrorListener (2) - ",
                            e);
                }
            }

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("triptype", msTripType);
                params.put("distance", Double
                        .toString(Constants_user.DEFAULT_WALKING_DISTANCE));
                params.put("filtertrip", getFilterTrip() ? "1"
                        : "0");
                params.put("lat",
                        String.format("%.9f", fromLat));
                params.put("lng",
                        String.format("%.9f", fromLng));
                if (toLat != Constants_user.INVALIDLAT && toLng != Constants_user.INVALIDLNG
                        && !TextUtils.isEmpty(mTvTo.getText().toString())) {
                    params.put("tolat",
                            String.format("%.9f", toLat));
                    params.put("tolng",
                            String.format("%.9f", toLng));
                } else {
                    params.put("tolat",
                            String.format("%.9f", fromLat));
                    params.put("tolng",
                            String.format("%.9f", fromLng));
                }
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        url);
                if (startDateTime != null) {
                    params.put("plannedstartdatetime", df.format(startDateTime.getTime()));
                } else {
                    params.put("plannedstartdatetime", params.get("clientdatetime"));
                }
                String delim = "";
                StringBuilder getParams = new StringBuilder();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String debugUrl = "";
                try {
                    debugUrl = url + "?" + getParams.toString() + "&verbose=Y";
                    System.out.println("debugUrl" + debugUrl);
                } catch (Exception e) {
                    SSLog.e(TAG, "getPassengers map - ", e);
                }

                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        try {
            MyApplication.getInstance().addVolleyRequest(postRequest, false);
        } catch (Exception e) {
            SSLog.e(TAG, "getActiveuser CGlobals.getInstance().mVolleyReq..", e);
            CGlobals_user.getInstance().init(Active_trips_act.this);
        }
    }

    @Override
    protected String longdistancemarathivehiclecategory() {
        return null;
    }

    private void gotActiveCommercial(String response) {
        hidepDialog();
        if (response.trim().equals("-1")) {
            hideCabButton();
            return;
        } else if (TextUtils.isEmpty(response)) {
            hideCabButton();
            return;
        } else {
            formList(response);
        }
    }

    private void getActiveUsersError(VolleyError error) {
        if (error instanceof NetworkError) {
            msStatus = "";
            if (!MyApplication.getInstance().getConnectivity().connectionError(Active_trips_act.this, getString(R.string.app_label))) {
            }
        } else if (error instanceof ServerError) {
        } else if (error instanceof AuthFailureError) {
        } else if (error instanceof ParseError) {
        } else if (error instanceof NoConnectionError) {
        } else if (error instanceof TimeoutError) {
        } else {
            String sErr = error != null ? error.getMessage() : "";
            msStatus = "Error: " + sErr;
            if (!TextUtils.isEmpty(sErr)) {
                SSLog.e(TAG, " getActiveUsersError - ", error.getMessage());
            } else {
                SSLog.e(TAG, " getActiveUsersError - ", sErr);
            }
        }

        Toast.makeText(Active_trips_act.this,
                "Request failed. Please touch refresh to try again",
                Toast.LENGTH_SHORT).show();
        /*userTrips.clear();
        cabTrips.clear();
        getActiveTrips();*/
    } // getActiveUsersError

    @Override
    protected void getCheckTripPathNotification() {
    }
}
