package com.smartshehar.cabe.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.smartshehar.cabe.CGlobals_CabE;
import com.smartshehar.cabe.CTripCabE;
import com.smartshehar.cabe.Constants_CabE;
import com.smartshehar.cabe.MyApplication_CabE;
import com.smartshehar.cabe.R;
import com.smartshehar.cabe.TripHistory_Adapter;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by jijo_soumen on 25/04/2016.
 * CabE Passenger Trip History
 * show all own trip history information
 */
public abstract class TripHistory_act extends AppCompatActivity {

    private static String TAG = "TripHistory_act: ";
    protected RecyclerView mRecyclerView;
    protected TripHistory_Adapter mListAdapter;
    ArrayList<CTripCabE> cTripArrayList;
    CTripCabE cTrip;
    boolean isInternetCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDialog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
                .penaltyLog()
                .build());*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.triphistory_act);
        mRecyclerView = (RecyclerView) findViewById(R.id.card_list);
        int rows = getResources().getInteger(R.integer.map_grid_cols);
        GridLayoutManager layoutManager = new GridLayoutManager(TripHistory_act.this, rows, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    protected abstract TripHistory_Adapter createMapListAdapter();

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(TripHistory_act.this);
        if (isInternetCheck) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(TripHistory_act.this);
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
            if (!TripHistory_act.this.isFinishing()) {
                alert11.show();
            }
            return;
        }
        boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(TripHistory_act.this);
        if (!isInternetCheck) {
            getTripHistory();
        } else {
            Toast.makeText(TripHistory_act.this, getString(R.string.retry_internet), Toast.LENGTH_LONG).show();
        }
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Create Trip History Function
    private void getTripHistory() {
        cTripArrayList = new ArrayList<>();
        // Send Data to Server Database Using Volley Library
        final String url = Constants_CabE.TRIP_HISTORY_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
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
                            TripHistory_act.this,
                            "Can not load your trip history. Please check your internet connection and retry.",
                            Toast.LENGTH_LONG).show();
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "getTripHistory:-   ", error, TripHistory_act.this);
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "getTripHistory:-  ", e, TripHistory_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<>();
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, TripHistory_act.this);
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
                    SSLog_SS.e(TAG, "getTripHistory", e, TripHistory_act.this);
                }
                return CGlobals_CabE.getInstance().checkParams(params);
            }
        };
        MyApplication_CabE.getInstance().addVolleyRequest(postRequest, false);
    } // sendUpdatePosition

    private void getMyTripHistory(String response) {
        if (TextUtils.isEmpty(response) || response.trim().equals("-1")) {
            customDialog("No Trip History");
            return;
        }
        try {
            JSONArray aJson = new JSONArray(response);
            for (int i = 0; i < aJson.length(); i++) {
                cTrip = new CTripCabE(aJson.getJSONObject(i)
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
            SSLog_SS.e(TAG, "getTripHistory", e, TripHistory_act.this);
        }
    }

    private void customDialog(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(TripHistory_act.this);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        if (!TripHistory_act.this.isFinishing()) {
            alert11.show();
        }
    }

    public abstract void showMapDetails(View view);
}
