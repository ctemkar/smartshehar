package com.smartshehar.cabe.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
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
import com.smartshehar.cabe.CGlobals_CabE;
import com.smartshehar.cabe.CTripCabE;
import com.smartshehar.cabe.Constants_CabE;
import com.smartshehar.cabe.CustomVolleyRequestQueue;
import com.smartshehar.cabe.MyApplication_CabE;
import com.smartshehar.cabe.R;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by jijo_soumen on 25/04/2016.
 * CabE Passenger Trip History With Map and show information
 */
public class TripHistoryMap_act extends AppCompatActivity implements OnMapReadyCallback {

    private static String TAG = "TripHistoryMap_act: ";
    public static final String EXTRA_ARRAYLIST = "EXTRA_ARRAYLIST";
    public TextView tvTripDriverName;
    public TextView tvTripDateTime, tvDriverCarModel, tvTripFareRs, tvTripDistance;
    NetworkImageView ivDriverImageShow;
    ImageLoader mImageLoader;
    ProgressDialog pDialog;
    boolean isInternetCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.triphistorymap_act);
        ActionBar actionBar = getSupportActionBar();
        pDialog = new ProgressDialog(TripHistoryMap_act.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        FrameLayout flDriverInfo = (FrameLayout) findViewById(R.id.flHistoryDriverInfo);
        if (flDriverInfo != null) {
            flDriverInfo.setVisibility(View.VISIBLE);
        }
        View hiddenPassengerInfo = getLayoutInflater().inflate(
                R.layout.triphistorydriver_info, flDriverInfo, false);
        if (flDriverInfo != null) {
            flDriverInfo.addView(hiddenPassengerInfo);
        }
        mImageLoader = CustomVolleyRequestQueue.getInstance(TripHistoryMap_act.this).getImageLoader();
        tvTripDriverName = (TextView) findViewById(R.id.tvTripDriverName);
        tvTripDateTime = (TextView) findViewById(R.id.tvTripDateTime);
        tvDriverCarModel = (TextView) findViewById(R.id.tvDriverCarModel);
        tvTripFareRs = (TextView) findViewById(R.id.tvTripFareRs);
        tvTripDistance = (TextView) findViewById(R.id.tvTripDistance);
        ivDriverImageShow = (NetworkImageView) findViewById(R.id.ivDriverImageShow);
        if (actionBar != null) {
            actionBar.setTitle("Cab-e History");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
    }

    public void onMapReady(GoogleMap map) {
        isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(TripHistoryMap_act.this);
        if (!isInternetCheck) {
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.setMessage(TripHistoryMap_act.this.getString(R.string.please_wait_cabe));
            if (!TripHistoryMap_act.this.isFinishing()) {
                pDialog.show();
            }
        } else {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(TripHistoryMap_act.this);
            builder1.setMessage("No Internet Connection.");
            builder1.setCancelable(true);
            builder1.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder1.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            if (!TripHistoryMap_act.this.isFinishing()) {
                alert11.show();
            }
        }
        new getCabeTripHistory(map).execute();
    }

    private class getCabeTripHistory extends AsyncTask<String, Integer, String> {
        CTripCabE cTripCabE, cTrip;
        int height, padding, width;
        LatLngBounds bounds;
        PolylineOptions polylineOptions;
        ArrayList<LatLng> arrayPoints = new ArrayList<>();
        GoogleMap googleMap;

        public getCabeTripHistory(GoogleMap map) {
            googleMap = map;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String arrayJson = getIntent().getStringExtra(EXTRA_ARRAYLIST);
                Gson gson = new Gson();
                Type type = new TypeToken<CTripCabE>() {
                }.getType();
                cTripCabE = gson.fromJson(arrayJson, type);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new LatLng(cTripCabE.getFromLat(), cTripCabE.getFromLng()));
                builder.include(new LatLng(cTripCabE.getToLat(), cTripCabE.getToLng()));
                bounds = builder.build();
                width = getResources().getDisplayMetrics().widthPixels;
                height = getResources().getDisplayMetrics().heightPixels - 180;
                padding = (int) (width * 0.12);
                isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(TripHistoryMap_act.this);
            } catch (Exception e) {
                SSLog_SS.e(TAG, "CabeTripHistory: ", e, TripHistoryMap_act.this);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (!TripHistoryMap_act.this.isFinishing()) {
                    pDialog.setCancelable(true);
                    pDialog.cancel();
                }
                googleMap.addMarker(new MarkerOptions()
                        .position(
                                new LatLng(cTripCabE.getFromLat(), cTripCabE.getFromLng()))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_start_location))
                        .alpha(0.8f).draggable(true)
                        .title(cTripCabE.getFrom()));
                googleMap.addMarker(new MarkerOptions()
                        .position(
                                new LatLng(cTripCabE.getToLat(), cTripCabE.getToLng()))
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_end_location))
                        .alpha(0.8f).draggable(true)
                        .title(cTripCabE.getTo()));
                googleMap.setPadding(0, 0, 0, 200);
                googleMap.animateCamera((CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)));
                tvTripDriverName.setText(cTripCabE.getDriverNmae());
                tvTripDateTime.setText(cTripCabE.getBooking_Time());
                tvDriverCarModel.setText(cTripCabE.getVehicle() + "\n" + cTripCabE.getVehicleNo());
                tvTripFareRs.setText("Fare:  Rs " + cTripCabE.getTrip_Cost());
                tvTripDistance.setText("Distance: " + cTripCabE.getTrip_Distance());
                if (isInternetCheck) {
                    return;
                }
                if (!TextUtils.isEmpty(cTripCabE.getImage_Name()) &&
                        !TextUtils.isEmpty(cTripCabE.getImage_Path())) {
                    String url = Constants_CabE.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + cTripCabE.getImage_Path() +
                            cTripCabE.getImage_Name();
                    mImageLoader.get(url, ImageLoader.getImageListener(ivDriverImageShow,
                            R.drawable.ic_driver, R.drawable.ic_driver));
                    ivDriverImageShow.setImageUrl(url, mImageLoader);
                } else {
                    String url = "https://www.google.co.in";
                    mImageLoader.get(url, ImageLoader.getImageListener(ivDriverImageShow,
                            R.drawable.ic_driver, R.drawable.ic_driver));
                    ivDriverImageShow.setImageUrl(url, mImageLoader);
                }
                try {
                    final String url = Constants_CabE.TRIP_PATH_HISTORY_URL;
                    StringRequest postRequest = new StringRequest(Request.Method.POST,
                            url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (!TripHistoryMap_act.this.isFinishing()) {
                                        pDialog.setCancelable(true);
                                        pDialog.cancel();
                                    }
                                    if (TextUtils.isEmpty(response) || response.equals("-1")) {
                                        return;
                                    }
                                    try {
                                        JSONArray aJson = new JSONArray(response);
                                        for (int i = 0; i < aJson.length(); i++) {
                                            cTrip = new CTripCabE(aJson.getJSONObject(i)
                                                    .toString(), getApplicationContext());
                                            polylineOptions = new PolylineOptions();
                                            polylineOptions.color(Color.RED);
                                            polylineOptions.width(5);
                                            arrayPoints.add(new LatLng(cTrip.getTripHistoryPathLat(), cTrip.getTripHistoryPathLng()));
                                            polylineOptions.addAll(arrayPoints);
                                            googleMap.addPolyline(polylineOptions);
                                        }
                                    } catch (Exception e) {
                                        SSLog_SS.e(TAG, "getTripHistory:-  ", e, TripHistoryMap_act.this);
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            try {
                                Toast.makeText(
                                        TripHistoryMap_act.this,
                                        "Can not load your trip history. Please check your internet connection and retry.",
                                        Toast.LENGTH_LONG).show();
                                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                                    Log.e(TAG, "");
                                } else {
                                    SSLog_SS.e(TAG, "getTripHistory:-   ", error, TripHistoryMap_act.this);
                                }
                            } catch (Exception e) {
                                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                                    Log.e(TAG, "");
                                } else {
                                    SSLog_SS.e(TAG, "getTripHistory:-  ", e, TripHistoryMap_act.this);
                                }
                            }
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            // Put Value Using HashMap
                            Map<String, String> params = new HashMap<>();
                            params.put("triptype", cTripCabE.getTripType());
                            params.put("tripid", String.valueOf(cTripCabE.getTripId()));
                            params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                                    url, TripHistoryMap_act.this);
                            String delim = "";
                            StringBuilder getParams = new StringBuilder();
                            for (Map.Entry<String, String> entry : params.entrySet()) {
                                getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                                delim = "&";
                            }
                            try {
                                String url1 = url + "?" + getParams.toString();
                                System.out.println("url  " + url1);
                            } catch (Exception e) {
                                SSLog_SS.e(TAG, "getTripHistory", e, TripHistoryMap_act.this);
                            }
                            return CGlobals_CabE.getInstance().checkParams(params);
                        }
                    };
                    MyApplication_CabE.getInstance().addVolleyRequest(postRequest, false);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "CabeTripHistory: ", e, TripHistoryMap_act.this);
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "CabeTripHistory: ", e, TripHistoryMap_act.this);
            }
            super.onPostExecute(result);
        }
    }
}