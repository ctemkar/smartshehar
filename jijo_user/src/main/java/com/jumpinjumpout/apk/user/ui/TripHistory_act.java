/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jumpinjumpout.apk.user.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.MapView;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.TripHistoryList_Adapter;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class TripHistory_act extends ActionBarActivity {

    private static String TAG = "TripHistoryList_act: ";
    protected TripHistoryList_Adapter mListAdapter;
    protected RecyclerView mRecyclerView;
    ArrayList<CTrip> cTripArrayList;
    CTrip cTrip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.triphistory_act);

        mRecyclerView = (RecyclerView) findViewById(R.id.card_list);

        // Determine the number of columns to display, based on screen width.
        int rows = getResources().getInteger(R.integer.map_grid_cols);
        GridLayoutManager layoutManager = new GridLayoutManager(this, rows, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);



        // Delay attaching Adapter to RecyclerView until we can ensure that we have correct
        // Google Play service version (in onResume).
    }

    protected abstract TripHistoryList_Adapter createMapListAdapter();

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        if (mListAdapter != null) {
            for (MapView m : mListAdapter.getMapViews()) {
                m.onLowMemory();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mListAdapter != null) {
            for (MapView m : mListAdapter.getMapViews()) {
                m.onPause();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTripHistory();


        if (mListAdapter != null) {
            for (MapView m : mListAdapter.getMapViews()) {
                m.onResume();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mListAdapter != null) {
            for (MapView m : mListAdapter.getMapViews()) {
                m.onDestroy();
            }
        }

        super.onDestroy();
    }

    // Create Trip History Function
    private void getTripHistory() {
        cTripArrayList = new ArrayList<CTrip>();
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.TRIP_HISTORY_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getMyTripHistory(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Toast.makeText(
                            TripHistory_act.this.getBaseContext(),
                            getString(R.string.retry_internet),
                            Toast.LENGTH_LONG).show();

                    SSLog.e(TAG, "getTripHistory:-   ",
                            error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "getTripHistory:-  ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.TRIP_HISTORY_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.TRIP_HISTORY_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "getTripHistory", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendUpdatePosition

    private void getMyTripHistory(String response) {
        if (TextUtils.isEmpty(response) || response.trim().equals("-1")) {
            return;
        }
        try {
            JSONArray aJson = new JSONArray(response);
            for (int i = 0; i < aJson.length(); i++) {
                cTrip = new CTrip(aJson.getJSONObject(i)
                        .toString(), getApplicationContext());
                cTripArrayList.add(cTrip);
            }
            mListAdapter = createMapListAdapter();
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            if (resultCode == ConnectionResult.SUCCESS) {
                mRecyclerView.setAdapter(mListAdapter);
            } else {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 1).show();
            }
        } catch (Exception e) {
            SSLog.e(TAG, "getTripHistory", e);
        }
    }

    /**
     * Show a full mapView when a mapView card is selected. This method is attached to each CardView
     * displayed within this activity's RecyclerView.
     *
     * @param view The view (CardView) that was clicked.
     */
    public abstract void showMapDetails(View view);
}
