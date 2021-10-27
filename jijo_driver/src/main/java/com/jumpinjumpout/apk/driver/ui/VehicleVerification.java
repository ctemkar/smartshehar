package com.jumpinjumpout.apk.driver.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.driver.CGlobals_driver;
import com.jumpinjumpout.apk.driver.Constants_driver;
import com.jumpinjumpout.apk.driver.MyApplication;
import com.jumpinjumpout.apk.driver.R;
import com.jumpinjumpout.apk.driver.VehicleAdapter;
import com.jumpinjumpout.apk.driver.VehicleResult;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.ClearableEditText;
import com.jumpinjumpout.apk.lib.SSLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.Connectivity;

/**
 * Created by user pc on 04-05-2015.
 */
public class VehicleVerification extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static String TAG = "VehicleVerification: ";
    private Button btnverify;
    private ClearableEditText etCarNo;
    ArrayList<VehicleResult> aVehicleResults = new ArrayList<VehicleResult>();
    String msVehicleNo, sVehicleID, sShiftid, sFullName, sPhoneNo, sAge, sSex;
    String sCommercialVehicleId, sCompanyId, sVehicleNo, sVehicleCompany, sVehicleModel, sVehicleType, sVehicleColor, sSeating,
            sVehicleDistinctiveMarks, sDateRegistered, sIsBooked, msPremium_Seats, msWindow_Seats, msStandard_Seats,
            msPremium_Seat_Fare, msWindow_Seat_Fare, msStandard_Seat_Fare, msVehicle_Category;
    private ProgressDialog mProgressDialog;
    GoogleApiClient mGoogleApiClient;
    Connectivity mConnectivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_verification);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        mConnectivity = new Connectivity();
        if (!mConnectivity.checkConnected(VehicleVerification.this)) {
            if (!mConnectivity.connectionError(VehicleVerification.this, getString(R.string.app_label))) {
                Log.d(TAG, "Internet Connection");
            }
        }
        etCarNo = (ClearableEditText) findViewById(R.id.etCarNo);
        btnverify = (Button) findViewById(R.id.btnverify);
        btnverify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(etCarNo.getText().toString())) {
                    Toast.makeText(VehicleVerification.this, getString(R.string.pleaseEnteryourvehicleno), Toast.LENGTH_SHORT).show();
                } else {
                    msVehicleNo = etCarNo.getText().toString();
                    hideKeyboard();
                    mVehicleNoVerify();
                }
            }
        });

        mProgressDialog = new ProgressDialog(VehicleVerification.this);
        mProgressDialog.setMessage("Please wait");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    } //onCreate

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void mVehicleNoVerify() {
        showpDialog();
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.SELECT_COMMERCIAL_VEHICLE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getVehicleAllDtetails(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidepDialog();
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(VehicleVerification.this, sErr, Toast.LENGTH_SHORT).show();
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

                params.put("vehicleno", msVehicleNo);
                params = CGlobals_driver.getInstance().getMinAllMobileParams(params,
                        Constants_driver.SELECT_COMMERCIAL_VEHICLE_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_driver.SELECT_COMMERCIAL_VEHICLE_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "mVehicleNoVerify", e);
                }
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } //mVehicleNoVerify

    private void getVehicleAllDtetails(String response) {
        hidepDialog();
        if (response.trim().equals("-1")) {
            Toast.makeText(VehicleVerification.this, getString(R.string.vehiclenonotfound), Toast.LENGTH_LONG).show();
            return;
        }
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(response);
            aVehicleResults.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jResponse = jsonArray.getJSONObject(i);
                sCommercialVehicleId = isNullNotDefined(jResponse, "commercial_vehicle_id") ? ""
                        : jResponse.getString("commercial_vehicle_id");
                sCompanyId = isNullNotDefined(jResponse, "company_id") ? ""
                        : jResponse.getString("company_id");
                sVehicleNo = isNullNotDefined(jResponse, "vehicleno") ? ""
                        : jResponse.getString("vehicleno");
                sVehicleCompany = isNullNotDefined(jResponse, "vehicle_company") ? ""
                        : jResponse.getString("vehicle_company");
                sVehicleModel = isNullNotDefined(jResponse, "vehicle_model") ? ""
                        : jResponse.getString("vehicle_model");
                sVehicleType = isNullNotDefined(jResponse, "vehicle_type") ? ""
                        : jResponse.getString("vehicle_type");
                sVehicleColor = isNullNotDefined(jResponse, "vehicle_color") ? ""
                        : jResponse.getString("vehicle_color");
                sSeating = isNullNotDefined(jResponse, "seating") ? ""
                        : jResponse.getString("seating");
                sVehicleDistinctiveMarks = isNullNotDefined(jResponse, "vehicle_distinctive_marks") ? ""
                        : jResponse.getString("vehicle_distinctive_marks");
                sDateRegistered = isNullNotDefined(jResponse, "date_registered") ? ""
                        : jResponse.getString("date_registered");
                sIsBooked = isNullNotDefined(jResponse, "is_booked") ? ""
                        : jResponse.getString("is_booked");

                msPremium_Seats = isNullNotDefined(jResponse, "premium_seats") ? ""
                        : jResponse.getString("premium_seats");
                msWindow_Seats = isNullNotDefined(jResponse, "window_seats") ? ""
                        : jResponse.getString("window_seats");
                msStandard_Seats = isNullNotDefined(jResponse, "standard_seats") ? ""
                        : jResponse.getString("standard_seats");
                msPremium_Seat_Fare = isNullNotDefined(jResponse, "premium_seat_fare") ? ""
                        : jResponse.getString("premium_seat_fare");
                msWindow_Seat_Fare = isNullNotDefined(jResponse, "window_seat_fare") ? ""
                        : jResponse.getString("window_seat_fare");
                msStandard_Seat_Fare = isNullNotDefined(jResponse, "standard_seat_fare") ? ""
                        : jResponse.getString("standard_seat_fare");

                msVehicle_Category = isNullNotDefined(jResponse, "vehicle_category") ? ""
                        : jResponse.getString("vehicle_category");

                VehicleResult vResult = new VehicleResult();
                vResult.setVehicleID(sCommercialVehicleId);
                vResult.setCompanyId(sCompanyId);
                vResult.setVehicleNo(sVehicleNo);
                vResult.setVehicleCompany(sVehicleCompany);
                vResult.setVehicleModel(sVehicleModel);
                vResult.setVehicleType(sVehicleType);
                vResult.setVehicleColor(sVehicleColor);
                vResult.setVehicleSeating(sSeating);
                vResult.setVehicleDistinctiveMarks(sVehicleDistinctiveMarks);
                vResult.setVehicleDateRegistered(sDateRegistered);
                vResult.setVehicleIsBooked(sIsBooked);
                vResult.setPremium_Seats(msPremium_Seats);
                vResult.setWindow_Seats(msWindow_Seats);
                vResult.setStandard_Seats(msStandard_Seats);
                vResult.setPremium_Seat_Fare(msPremium_Seat_Fare);
                vResult.setWindow_Seat_Fare(msWindow_Seat_Fare);
                vResult.setStandard_Seat_Fare(msStandard_Seat_Fare);
                vResult.setVehicle_Category(msVehicle_Category);
                aVehicleResults.add(vResult);
                String json = new Gson().toJson(vResult);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PERF_VEHICLE_DETAILS", json);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
            }
        } catch (Exception e) {
            SSLog.e(TAG, "getVehicleAllDtetails", e);
            Toast.makeText(VehicleVerification.this, "Vehicle verification error", Toast.LENGTH_LONG).show();
        }
        if (aVehicleResults.size() == 1) {
            sVehicleID = sCommercialVehicleId;
            goLogin();
        } else {
            final Dialog dialog = new Dialog(VehicleVerification.this);
            dialog.setContentView(R.layout.vehicle_list_show);
            dialog.setTitle("Vehicle List...");
            ListView mlvVehicleList;
            mlvVehicleList = (ListView) dialog.findViewById(R.id.listVehicle);
            mlvVehicleList.setAdapter(new VehicleAdapter(VehicleVerification.this, aVehicleResults));
            mlvVehicleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                      @Override
                                                      public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                                                          dialog.cancel();
                                                          AlertDialog.Builder alertDialog = new AlertDialog.Builder(VehicleVerification.this);
                                                          alertDialog.setTitle("Confirm ...");
                                                          alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                              public void onClick(DialogInterface dialog, int which) {
                                                                  sVehicleID = aVehicleResults.get(i).getVehicleID();
                                                                  goLogin();
                                                              }
                                                          });
                                                          alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                              public void onClick(DialogInterface dialog, int which) {
                                                                  dialog.cancel();
                                                              }
                                                          });
                                                          if (!VehicleVerification.this.isFinishing()) {
                                                              alertDialog.show();
                                                          }
                                                      }
                                                  }

            );
            dialog.show();
        }
    } //getVehicleAllDtetails

    private void goLogin() {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.COMMERCIAL_DRIVER_START_SHIFT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        getLogin(response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(VehicleVerification.this, sErr, Toast.LENGTH_SHORT).show();
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

                params.put("commercialvehicleid", sVehicleID);
                params = CGlobals_driver.getInstance().getMinAllMobileParams(params,
                        Constants_driver.COMMERCIAL_DRIVER_START_SHIFT_URL);
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);


    } // goLogin

    private void getLogin(String response) {
        VehicleResult vResult = new VehicleResult();
        response = response.replaceAll("(\\r|\\n)", "");
        if (response.trim().equals(-1) && TextUtils.isEmpty(response.trim())) {
            Toast.makeText(VehicleVerification.this, "Login failed", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject jResponse;
        try {
            jResponse = new JSONObject(response);

            sShiftid = jResponse.getString("shift_id");
            sFullName = jResponse.getString("fullname");
            sPhoneNo = jResponse.getString("phoneno");
            sAge = jResponse.getString("age");
            sSex = jResponse.getString("sex");


            vResult.setFullName(sFullName);
            vResult.setPhoneNo(sPhoneNo);
            vResult.setAge(sAge);
            vResult.setSex(sSex);
            vResult.setShift(sShiftid);
            hidepDialog();
            String sVDetails = MyApplication.getInstance().getPersistentPreference().getString("PERF_VEHICLE_DETAILS", "");
            if (!TextUtils.isEmpty(sVDetails)) {
                Type type = new TypeToken<VehicleResult>() {
                }.getType();
                VehicleResult vehicleResult = new Gson().fromJson(sVDetails, type);
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(VehicleVerification.this);
                alertDialog.setTitle("Vehicle Confirmation ...");
                alertDialog.setMessage("Number- " + vehicleResult.getVehicleNo() +
                        "\nCompany- " + vehicleResult.getVehicleCompany() +
                        "\nModel- " + vehicleResult.getVehicleModel() +
                        "\nColor- " + vehicleResult.getVehicleColor() +
                        "\nSeating- " + vehicleResult.getVehicleSeating());

                alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent(VehicleVerification.this, Dashboard_act.class);
                        intent.putExtra(Constants_driver.INTENT_SHIFT_ID, sShiftid);
                        intent.putExtra(Constants_driver.INTENT_VEHICLE_ID, sVehicleID);
                        startActivity(intent);
                        MyApplication.getInstance().getPersistentPreferenceEditor()
                                .putString(Constants_driver.PREF_VEHICLE_ID, sVehicleID);
                        MyApplication.getInstance().getPersistentPreferenceEditor()
                                .putString(Constants_driver.PREF_SHIFT_ID, sShiftid);
                        MyApplication.getInstance().getPersistentPreferenceEditor()
                                .putBoolean(Constants_driver.PREF_LOGIN_LOGOUT_FLAG, true);
                        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                        finish();
                        hideKeyboard();
                    }

                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        hideKeyboard();
                    }

                });
                if (!VehicleVerification.this.isFinishing()) {
                    alertDialog.show();
                }
            }

        } catch (Exception e) {
            SSLog.e(TAG, "getLogin", e);
            Toast.makeText(VehicleVerification.this, "user Login failed", Toast.LENGTH_LONG).show();
        }

        String json = new Gson().toJson(vResult);
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putString("VALUE_SAVED", json);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();


    } //getLogin

    public boolean isNullNotDefined(JSONObject jo, String jkey) {

        if (!jo.has(jkey)) {
            return true;
        }
        if (jo.isNull(jkey)) {
            return true;
        }
        return false;

    } //isNullNotDefined

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            System.exit(0);
            return false;
        }
        return true;
    }

    private void showpDialog() {
        try {
            if (!isFinishing()) {
                mProgressDialog.setMessage("Please wait ...");
                mProgressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hidepDialog() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        try {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager inputManager = (InputMethodManager) this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                        InputMethodManager.RESULT_HIDDEN);
                inputManager.hideSoftInputFromWindow(
                        view.getApplicationWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }
}
