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

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TripHistoryFullMap_act extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_ARRAYLIST = "EXTRA_ARRAYLIST";
    CTrip cTrip;
    ProgressDialog pDialog;
    List<LatLng> polyz;
    protected LatLngBounds.Builder mZoomFitBounds;
    public TextView tvTripDriverName;
    public TextView tvTripDateTime, tvDriverCarModel, tvTripFareRs;
    public ImageView ivDriverImageShow;
    boolean isImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.triphistoryfullmap_act);
        ActionBar actionBar = getSupportActionBar();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        FrameLayout flDriverInfo = (FrameLayout) findViewById(R.id.flHistoryDriverInfo);
        View hiddenPassengerInfo = getLayoutInflater().inflate(
                R.layout.triphistorydriver_info, flDriverInfo, false);
        flDriverInfo.addView(hiddenPassengerInfo);

        tvTripDriverName = (TextView) findViewById(R.id.tvTripDriverName);
        tvTripDateTime = (TextView) findViewById(R.id.tvTripDateTime);
        tvDriverCarModel = (TextView) findViewById(R.id.tvDriverCarModel);
        tvTripFareRs = (TextView) findViewById(R.id.tvTripFareRs);
        ivDriverImageShow = (ImageView) findViewById(R.id.ivDriverImageShow);
        actionBar.setTitle("My Trip History");
        actionBar.setHomeButtonEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);

                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this)
                            .addNextIntentWithParentStack(upIntent)
                            .startActivities();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onMapReady(GoogleMap map) {
        String arrayJson = getIntent().getStringExtra(EXTRA_ARRAYLIST);
        Gson gson = new Gson();
        Type type = new TypeToken<CTrip>() {
        }.getType();
        cTrip = gson.fromJson(arrayJson, type);
        map.addMarker(new MarkerOptions()
                        .position(
                                new LatLng(cTrip.getFromLat(), cTrip
                                        .getFromLng()))
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .alpha(0.8f).draggable(true)
                        .title(cTrip.getFrom()));
        map.addMarker(new MarkerOptions()
                        .position(
                                new LatLng(cTrip.getToLat(), cTrip
                                        .getToLng()))
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .alpha(0.8f).draggable(true)
                        .title(cTrip.getTo()));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(cTrip.getFromLat(), cTrip.getFromLng()),
                17));
        map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);

        tvTripDriverName.setText(cTrip.getMsFullName());
        tvTripDateTime.setText(cTrip.getMsTripActionTime1());
        tvDriverCarModel.setText(cTrip.getVehicleCompany() + " " + cTrip.getVehicleModel() + "\n" + cTrip.getVehicleNo());
        tvTripFareRs.setText("Fare:  Rs " + cTrip.getTripFare());

        if (!TextUtils.isEmpty(cTrip.getUserProfileImageFileName()) &&
                !TextUtils.isEmpty(cTrip.getUserProfileImagePath())) {
            String url = Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + cTrip.getUserProfileImagePath() +
                    cTrip.getUserProfileImageFileName();
            ImageRequest request = new ImageRequest(url,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            if (Build.VERSION.SDK_INT < 11) {
                            } else {
                                ivDriverImageShow.setImageBitmap(bitmap);
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
            Bitmap bitmap = cTrip.getContactThnumbnail();
            if (bitmap != null) {
                ivDriverImageShow.setImageBitmap(bitmap);
            }
        }

        new GetDirection(map).execute();
    }

    class GetDirection extends AsyncTask<String, String, String> {
        GoogleMap mMap;

        public GetDirection(GoogleMap map) {
            mMap = map;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TripHistoryFullMap_act.this);
            pDialog.setMessage("Loading route. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {

            String stringUrl = "http://maps.googleapis.com/maps/api/directions/json?origin=" + cTrip.getFromLat() +
                    "," + cTrip.getFromLng() + "&destination=" +
                    cTrip.getToLat() + "," + cTrip.getToLng() + "&sensor=false";
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(stringUrl);
                HttpURLConnection httpconn = (HttpURLConnection) url
                        .openConnection();
                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader input = new BufferedReader(
                            new InputStreamReader(httpconn.getInputStream()),
                            8192);
                    String strLine = null;

                    while ((strLine = input.readLine()) != null) {
                        response.append(strLine);
                    }
                    input.close();
                }

                String jsonOutput = response.toString();

                JSONObject jsonObject = new JSONObject(jsonOutput);

                // routesArray contains ALL routes
                JSONArray routesArray = jsonObject.getJSONArray("routes");
                // Grab the first route
                JSONObject route = routesArray.getJSONObject(0);

                JSONObject poly = route.getJSONObject("overview_polyline");
                String polyline = poly.getString("points");
                polyz = decodePoly(polyline);

            } catch (Exception e) {

            }

            return null;

        }

        protected void onPostExecute(String file_url) {

            for (int i = 0; i < polyz.size() - 1; i++) {
                LatLng src = polyz.get(i);
                LatLng dest = polyz.get(i + 1);
                mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude),
                                new LatLng(dest.latitude, dest.longitude))
                        .width(2).color(Color.BLACK).geodesic(true));

            }
            mZoomFitBounds = new LatLngBounds.Builder();
            for (LatLng latLng : polyz) {
                mZoomFitBounds
                        .include(new LatLng(latLng.latitude, latLng.longitude));
            }
            if (mZoomFitBounds != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        mZoomFitBounds.build(), 40));

            }
            pDialog.dismiss();

        }
    }

    /* Method to decode polyline points */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
