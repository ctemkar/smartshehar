package com.jumpinjumpout.apk.driver.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.driver.CGlobals_driver;
import com.jumpinjumpout.apk.driver.Constants_driver;
import com.jumpinjumpout.apk.driver.MyApplication;
import com.jumpinjumpout.apk.driver.R;
import com.jumpinjumpout.apk.driver.VehicleResult;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.SSLog;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by jijo_soumen on 06/01/2016.
 */
public class CheckSeatAndFare_act extends Activity {

    private static String TAG = "CheckSeatAndFare_act: ";
    private EditText etPremiumsSeats, etPremiumsFare, etWindowSeats, etWindowFare,
            etStandardSeats, etStandardFare;
    private Button btnSeatFareSubmit;
    private String msPremiumsSeats, msPremiumsFare, msWindowSeats, msWindowFare,
            msStandardSeats, msStandardFare, msTotalSeats, sCommercialVehicleid;
    TextView etTotalSeats;

    int sPremiumsSeats = 0, sWindowSeats = 0, sStandardSeats = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkseatandfare_act);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sVDetails = MyApplication.getInstance().getPersistentPreference().getString("PERF_VEHICLE_DETAILS", "");
        String sSeatFare = MyApplication.getInstance().getPersistentPreference().getString("COMMERCIAL_VEHICLE_SEATFARE", "");
        if (!TextUtils.isEmpty(sSeatFare)) {
            Type type1 = new TypeToken<HashMap<String, String>>() {
            }.getType();
            HashMap<String, String> hashMap = new Gson().fromJson(sSeatFare, type1);
            etPremiumsSeats.setText(hashMap.get("premiumseats"));
            etPremiumsFare.setText(hashMap.get("premiumseatfare"));
            etWindowSeats.setText(hashMap.get("windowseats"));
            etWindowFare.setText(hashMap.get("windowseatfare"));
            etStandardSeats.setText(hashMap.get("standardseats"));
            etStandardFare.setText(hashMap.get("standardseatfare"));
            etTotalSeats.setText(hashMap.get("seating"));
            sCommercialVehicleid = hashMap.get("commercialvehicleid");
        } else if (!TextUtils.isEmpty(sVDetails)) {
            Type type = new TypeToken<VehicleResult>() {
            }.getType();
            VehicleResult vehicleResult = new Gson().fromJson(sVDetails, type);
            etPremiumsSeats.setText(vehicleResult.getPremium_Seats());
            etPremiumsFare.setText(vehicleResult.getPremium_Seat_Fare());
            etWindowSeats.setText(vehicleResult.getWindow_Seats());
            etWindowFare.setText(vehicleResult.getWindow_Seat_Fare());
            etStandardSeats.setText(vehicleResult.getStandard_Seats());
            etStandardFare.setText(vehicleResult.getStandard_Seat_Fare());
            etTotalSeats.setText(vehicleResult.getVehicleSeating());
            sCommercialVehicleid = vehicleResult.getVehicleID();
        }
        etStandardSeats.addTextChangedListener(textWatcher);
    }

    // EditText TextWatcher
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            try {
                if (etPremiumsSeats.getText().toString() != null || !TextUtils.isEmpty(etPremiumsSeats.getText().toString())) {
                    sPremiumsSeats = Integer.parseInt(etPremiumsSeats.getText().toString());
                }
                if (etWindowSeats.getText().toString() != null || !TextUtils.isEmpty(etWindowSeats.getText().toString())) {
                    sWindowSeats = Integer.parseInt(etWindowSeats.getText().toString());
                }
                if (etStandardSeats.getText().toString() != null || !TextUtils.isEmpty(etStandardSeats.getText().toString())) {
                    sStandardSeats = Integer.parseInt(etStandardSeats.getText().toString());
                }
                String total = String.valueOf(sPremiumsSeats + sWindowSeats + sStandardSeats);
                etTotalSeats.setText(total);
                SSLog.i(TAG, "filtered");
            } catch (Exception e) {
                SSLog.e(TAG, "EditText TextWatcher", e);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }

    };

    private void init() {
        etPremiumsSeats = (EditText) findViewById(R.id.etPremiumsSeats);
        etPremiumsFare = (EditText) findViewById(R.id.etPremiumsFare);
        etWindowSeats = (EditText) findViewById(R.id.etWindowSeats);
        etWindowFare = (EditText) findViewById(R.id.etWindowFare);
        etStandardSeats = (EditText) findViewById(R.id.etStandardSeats);
        etStandardFare = (EditText) findViewById(R.id.etStandardFare);
        etTotalSeats = (TextView) findViewById(R.id.etTotalSeats);
        btnSeatFareSubmit = (Button) findViewById(R.id.btnSeatFareSubmit);
    }

    public void onClickSeatFareSubmit(View view) {
        msPremiumsSeats = etPremiumsSeats.getText().toString();
        msPremiumsFare = etPremiumsFare.getText().toString();
        msWindowSeats = etWindowSeats.getText().toString();
        msWindowFare = etWindowFare.getText().toString();
        msStandardSeats = etStandardSeats.getText().toString();
        msStandardFare = etStandardFare.getText().toString();
        msTotalSeats = etTotalSeats.getText().toString();

        sendSeatFareSubmit();
    }

    private void sendSeatFareSubmit() {
        final int tripid = CGlobals_lib.getInstance().getSharedPreferences(CheckSeatAndFare_act.this)
                .getInt(Constants_lib.PREF_TRIP_ID_INT, -1);
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.UPDATE_COMMERCIAL_VEHICLE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        /*MyApplication.getInstance().getPersistentPreferenceEditor()
                                .putBoolean(Constants_driver.PREF_UPDTE_SEATS_AND_FARE,true);
                        MyApplication.getInstance().getPersistentPreferenceEditor().commit();*/
                        Intent intent = new Intent();
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(CheckSeatAndFare_act.this, sErr, Toast.LENGTH_SHORT).show();
                try {
                    SSLog.d(TAG,
                            "Failed to sendSeatFareSubmit :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "sendSeatFareSubmit - " + sErr,
                            error.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("commercialvehicleid", sCommercialVehicleid);
                params.put("premiumseats", msPremiumsSeats);
                params.put("premiumseatfare", msPremiumsFare);
                params.put("windowseats", msWindowSeats);
                params.put("windowseatfare", msWindowFare);
                params.put("standardseats", msStandardSeats);
                params.put("standardseatfare", msStandardFare);
                params.put("seating", msTotalSeats);
                params.put("tripid", String.valueOf(tripid));
                params = CGlobals_driver.getInstance().getMinAllMobileParams(params,
                        Constants_driver.UPDATE_COMMERCIAL_VEHICLE_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                Gson gson = new Gson();
                String json = gson.toJson(params);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("COMMERCIAL_VEHICLE_SEATFARE", json);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                String url1 = Constants_driver.UPDATE_COMMERCIAL_VEHICLE_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "sendSeatFareSubmit", e);
                }
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

}
