package com.jumpinjumpout.apk.user.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jijo_soumen on 08/01/2016.
 */
public class BookingSeat_act extends Activity {

    private final static String TAG = "BookingSeat_act: ";
    private TextView tvPremiumsSeats, tvPremiumsFare, tvWindowSeats, tvWindowFare, tvStandardSeats, tvStandardFare;
    private EditText etPremiumsSeats, etWindowSeats, etStandardSeats;
    private Button btnBookSeat;
    private CTrip cTrip;
    private String msPremiumsSeats, msWindowSeats, msStandardSeats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookingseat_act);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sGSON = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_user.PREF_LONG_DISTANCE_CTRIP, "");
        Type type = new TypeToken<CTrip>() {
        }.getType();
        cTrip = new Gson().fromJson(sGSON, type);
        tvPremiumsSeats.setText(cTrip.getPremium_Seats());
        tvPremiumsFare.setText(cTrip.getPremium_Seat_Fare());
        tvWindowSeats.setText(cTrip.getWindow_Seats());
        tvWindowFare.setText(cTrip.getWindow_Seat_Fare());
        tvStandardSeats.setText(cTrip.getStandard_Seats());
        tvStandardFare.setText(cTrip.getStandard_Seat_Fare());
    }

    private void init() {
        tvPremiumsSeats = (TextView) findViewById(R.id.tvPremiumsSeats);
        tvPremiumsFare = (TextView) findViewById(R.id.tvPremiumsFare);
        tvWindowSeats = (TextView) findViewById(R.id.tvWindowSeats);
        tvWindowFare = (TextView) findViewById(R.id.tvWindowFare);
        tvStandardSeats = (TextView) findViewById(R.id.tvStandardSeats);
        tvStandardFare = (TextView) findViewById(R.id.tvStandardFare);
        etPremiumsSeats = (EditText) findViewById(R.id.etPremiumsSeats);
        etWindowSeats = (EditText) findViewById(R.id.etWindowSeats);
        etStandardSeats = (EditText) findViewById(R.id.etStandardSeats);
        btnBookSeat = (Button) findViewById(R.id.btnBookSeat);
    }

    public void onClickBookSeat(View view) {
        msPremiumsSeats = etPremiumsSeats.getText().toString();
        msWindowSeats = etWindowSeats.getText().toString();
        msStandardSeats = etStandardSeats.getText().toString();
        if (TextUtils.isEmpty(msPremiumsSeats)) {
            msPremiumsSeats = "0";
        }
        if (TextUtils.isEmpty(msWindowSeats)) {
            msWindowSeats = "0";
        }
        if (TextUtils.isEmpty(msStandardSeats)) {
            msStandardSeats = "0";
        }
        sendSeatBook(msPremiumsSeats, msWindowSeats, msStandardSeats);
    }

    private void sendSeatBook(final String msPremiumsSeats, final String msWindowSeats, final String msStandardSeats) {
        final int tripId = MyApplication.getInstance().getPersistentPreference()
                .getInt(Constants_user.PREF_TRIP_ID, -1);
        final String url = Constants_user.ADD_TRANSACTION_SEATS_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        seatBookResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    SSLog.e(TAG, Constants_user.ADD_TRANSACTION_SEATS_URL
                                    + ": sendSeatBook - ",
                            error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG,
                            " sendSeatBook - ",
                            e);
                }
            }

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("windowseatfare", cTrip.getWindow_Seat_Fare());
                params.put("standardseats", msStandardSeats);
                params.put("commercialvehicleid", String.valueOf(cTrip.getCommercial_Vehicle_Id()));
                params.put("windowseats", msWindowSeats);
                params.put("premiumseats", msPremiumsSeats);
                params.put("seating", "0");
                params.put("standardseatfare", cTrip.getStandard_Seat_Fare());
                params.put("tripid", String.valueOf(tripId));
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        url);
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
            SSLog.e(TAG, "sendSeatBook..", e);
            CGlobals_user.getInstance().init(BookingSeat_act.this);
        }
    }

    public void seatBookResponse(String response) {
        MyApplication.getInstance().getPersistentPreferenceEditor().
                putBoolean(Constants_user.GET_A_CAB_VALUE, false);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        Intent i = new Intent(BookingSeat_act.this, ShareTripTrackDriver_act.class);
        startActivity(i);
        finish();
    }
}
