package com.smartshehar.cabe.driver.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.cabe.driver.CGlobals_CED;
import com.smartshehar.cabe.driver.Constants_CED;
import com.smartshehar.cabe.driver.DatabaseHandler_CabE;
import com.smartshehar.cabe.driver.R;
import com.smartshehar.cabe.driver.VehicleAdapter;
import com.smartshehar.cabe.driver.VehicleResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by jijo_soumen on 16/03/2016.
 * Allows driver to pick a vehicle that he has been authenticated for
 */
public class VehicleVerification_CED_act extends AppCompatActivity {

    private static String TAG = "VehicleVerify_CED_act: ";
    Connectivity mConnectivity;
    private EditText cleVerifyCar;
    private CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;
    String msVehicleNo, sdriver_vehicle_id, sCompanyId, sVehicleNo, sFull_Vehicleno, sVehicleCompany, sVehicleModel,
            sVehicleType, sVehicleColor, sSeating, sVehicleDistinctiveMarks, sDateRegistered, sIsBooked,
            msCompany_Name, msCompany_Address, msDriver_Appuser_Id, msRate_Per_Km, msMinimum_Fare,
            msVehicle_Category, sShiftid, sPhoneNo, sDateofBirth, sFirstName, sLastName, sGender, sServiceCode;
    int sIs_Login, driver_appuser_id = -1;
    private ProgressDialog mProgressDialog;
    ArrayList<VehicleResult> aVehicleResults = new ArrayList<>();
    private boolean isLoginOne = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicleverification_ced_act);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(VehicleVerification_CED_act.this)) {
            if (!mConnectivity.connectionError(VehicleVerification_CED_act.this, getString(R.string.app_label))) {
                Log.d(TAG, "Internet Connection");
            }
        }
        init();
        mProgressDialog = new ProgressDialog(VehicleVerification_CED_act.this);
        mProgressDialog.setMessage("Please wait");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
    }

    public void init() {
        cleVerifyCar = (EditText) findViewById(R.id.cleVerifyCar);
    }

    public void onClickCarVerify(View view) {
        InputMethodManager keyboard = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(cleVerifyCar.getWindowToken(), 0);
        cleVerifyCar.setCursorVisible(true);
        if (TextUtils.isEmpty(cleVerifyCar.getText().toString())) {
            snackbar = Snackbar.make(coordinatorLayout, getString(R.string.vehicleno_cabe), Snackbar.LENGTH_LONG);
            snackbar.show();
        } else {
            msVehicleNo = cleVerifyCar.getText().toString();
            boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(VehicleVerification_CED_act.this);
            if (!isInternetCheck) {
                mVehicleNoVerify(msVehicleNo);
            } else {
                snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }

    private void mVehicleNoVerify(final String msVehicleNo) {
        showpDialog();
        final String url = Constants_CED.SELECT_VEHICLE_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getVehicleAllDtetails(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidepDialog();
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.d(TAG,
                                "Failed to mVehicleNoVerify :-  "
                                        + error.getMessage());
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "mVehicleNoVerify - " + sErr,
                                error, VehicleVerification_CED_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("vehicleno", msVehicleNo);
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, VehicleVerification_CED_act.this);
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
                    SSLog_SS.e(TAG, "mVehicleNoVerify", e, VehicleVerification_CED_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, VehicleVerification_CED_act.this);
    } //mVehicleNoVerify

    private void getVehicleAllDtetails(String response) {
        hidepDialog();
        if (response.trim().equals("-1") || response.trim().equals("0") || TextUtils.isEmpty(response)) {
            snackbar = Snackbar.make(coordinatorLayout, getString(R.string.vehiclenonotfound_cabe), Snackbar.LENGTH_LONG);
            snackbar.show();
            return;
        }
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(response);
            aVehicleResults.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jResponse = jsonArray.getJSONObject(i);
                try {
                    driver_appuser_id = isNullNotDefined(jResponse, "driver_appuser_id") ? -1
                            : jResponse.getInt("driver_appuser_id");
                    sIs_Login = isNullNotDefined(jResponse, "is_login") ? -1
                            : jResponse.getInt("is_login");
                    sdriver_vehicle_id = isNullNotDefined(jResponse, "driver_vehicle_id") ? ""
                            : jResponse.getString("driver_vehicle_id");
                    sCompanyId = isNullNotDefined(jResponse, "company_id") ? ""
                            : jResponse.getString("company_id");
                    sVehicleNo = isNullNotDefined(jResponse, "vehicleno") ? ""
                            : jResponse.getString("vehicleno");
                    sFull_Vehicleno = isNullNotDefined(jResponse, "full_vehicleno") ? ""
                            : jResponse.getString("full_vehicleno");
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
                    try {
                        sServiceCode = isNullNotDefined(jResponse, "service_code") ? ""
                                : jResponse.getString("service_code");
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this)
                                .putString(Constants_CED.PERF_SERVICE_CODE_BOOKING_DRIVER, sServiceCode);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this).commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    msCompany_Name = isNullNotDefined(jResponse, "company_name") ? ""
                            : jResponse.getString("company_name");
                    msCompany_Address = isNullNotDefined(jResponse, "company_address") ? ""
                            : jResponse.getString("company_address");
                    msDriver_Appuser_Id = isNullNotDefined(jResponse, "driver_appuser_id") ? ""
                            : jResponse.getString("driver_appuser_id");
                    msRate_Per_Km = isNullNotDefined(jResponse, "rate_per_km") ? ""
                            : jResponse.getString("rate_per_km");
                    msMinimum_Fare = isNullNotDefined(jResponse, "minimum_fare") ? ""
                            : jResponse.getString("minimum_fare");
                    msVehicle_Category = isNullNotDefined(jResponse, "vehicle_category") ? ""
                            : jResponse.getString("vehicle_category");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                VehicleResult vResult = new VehicleResult();
                vResult.setVehicleID(sdriver_vehicle_id);
                vResult.setCompanyId(sCompanyId);
                vResult.setVehicleNo(sVehicleNo);
                vResult.setFull_Vehicleno(sFull_Vehicleno);
                vResult.setVehicleCompany(sVehicleCompany);
                vResult.setVehicleModel(sVehicleModel);
                vResult.setVehicleType(sVehicleType);
                vResult.setVehicleColor(sVehicleColor);
                vResult.setVehicleSeating(sSeating);
                vResult.setServiceCode(sServiceCode);
                vResult.setVehicleDistinctiveMarks(sVehicleDistinctiveMarks);
                vResult.setVehicleDateRegistered(sDateRegistered);
                vResult.setVehicleIsBooked(sIsBooked);
                vResult.setCompany_Name(msCompany_Name);
                vResult.setCompany_Address(msCompany_Address);
                vResult.setDriver_Appuser_Id(msDriver_Appuser_Id);
                vResult.setRate_Per_Km(msRate_Per_Km);
                vResult.setMinimum_Fare(msMinimum_Fare);
                vResult.setVehicle_Category(msVehicle_Category);
                aVehicleResults.add(vResult);
                String json = new Gson().toJson(vResult);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this)
                        .putString(Constants_CED.PERF_VEHICLE_DETAILS, json);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this).commit();
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "getVehicleAllDtetails", e, VehicleVerification_CED_act.this);
            snackbar = Snackbar.make(coordinatorLayout, getString(R.string.vehiclenonotfound_cabe), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        final int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(VehicleVerification_CED_act.this).
                getInt(Constants_lib_ss.PREF_APPUSERID, -1);
        if (sIs_Login == 1 && driver_appuser_id != appuserid && driver_appuser_id != -1) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(VehicleVerification_CED_act.this);
            alertDialog.setTitle("Somebody else has logged into the car.\n" +
                    "If you have the car with you, please get them to logout.\n" +
                    "You will not be able to use this car till the other person logs out.");
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            if (!VehicleVerification_CED_act.this.isFinishing()) {
                alertDialog.show();
            }
        } else if (sIs_Login == 1 && driver_appuser_id == appuserid && driver_appuser_id != -1) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(VehicleVerification_CED_act.this);
            alertDialog.setTitle("you already login other vehicle. please logout other vehicle.");
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String spShiftid = CGlobals_lib_ss.getInstance().getPersistentPreference(VehicleVerification_CED_act.this)
                            .getString(Constants_CED.PREF_SHIFT_ID, "");
                    String spVehicleid = CGlobals_lib_ss.getInstance().getPersistentPreference(VehicleVerification_CED_act.this)
                            .getString(Constants_CED.PREF_VEHICLE_ID, "");
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault());
                    Calendar cal = Calendar.getInstance();
                    String sDateTime = df.format(cal.getTime());
                    DatabaseHandler_CabE db = new DatabaseHandler_CabE(VehicleVerification_CED_act.this);
                    db.addDriverShifEnd(appuserid, CGlobals_lib_ss.msGmail,
                            sDateTime, CGlobals_lib_ss.mIMEI, spShiftid, spVehicleid);
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    dialog.cancel();
                }
            });
            if (!VehicleVerification_CED_act.this.isFinishing()) {
                alertDialog.show();
            }
        } else {
            if (aVehicleResults.size() == 1) {
                String sVDetails = CGlobals_lib_ss.getInstance().getPersistentPreference(VehicleVerification_CED_act.this)
                        .getString(Constants_CED.PERF_VEHICLE_DETAILS, "");
                if (!TextUtils.isEmpty(sVDetails)) {
                    Type type = new TypeToken<VehicleResult>() {
                    }.getType();
                    VehicleResult vehicleResult = new Gson().fromJson(sVDetails, type);
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(VehicleVerification_CED_act.this);
                    alertDialog.setTitle("Vehicle Confirmation ...");
                    alertDialog.setMessage("Number- " + vehicleResult.getFull_Vehicleno() +
                            "\nCompany- " + vehicleResult.getVehicleCompany() +
                            "\nModel- " + vehicleResult.getVehicleModel() +
                            "\nColor- " + vehicleResult.getVehicleColor() +
                            "\nSeating- " + vehicleResult.getVehicleSeating() +
                            "\nservice-code- " + vehicleResult.getServiceCode());

                    alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (!isLoginOne) {
                                isLoginOne = true;
                                boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(VehicleVerification_CED_act.this);
                                if (!isInternetCheck) {
                                    goLogin(sdriver_vehicle_id);
                                } else {
                                    snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }
                            }
                        }

                    });
                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }

                    });
                    if (!VehicleVerification_CED_act.this.isFinishing()) {
                        alertDialog.show();
                    }
                }
            } else {
                final Dialog dialog = new Dialog(VehicleVerification_CED_act.this);
                dialog.setContentView(R.layout.vehicle_list_show);
                dialog.setTitle("Vehicle List...");
                ListView mlvVehicleList;
                mlvVehicleList = (ListView) dialog.findViewById(R.id.listVehicle);
                mlvVehicleList.setAdapter(new VehicleAdapter(VehicleVerification_CED_act.this, aVehicleResults));
                mlvVehicleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                          @Override
                                                          public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                                                              dialog.cancel();
                                                              AlertDialog.Builder alertDialog = new AlertDialog.Builder(VehicleVerification_CED_act.this);
                                                              alertDialog.setTitle("Confirm ...");
                                                              alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                                  public void onClick(DialogInterface dialog, int which) {
                                                                      String sVDetails = CGlobals_lib_ss.getInstance().getPersistentPreference(VehicleVerification_CED_act.this)
                                                                              .getString(Constants_CED.PERF_VEHICLE_DETAILS, "");
                                                                      if (!TextUtils.isEmpty(sVDetails)) {
                                                                          Type type = new TypeToken<VehicleResult>() {
                                                                          }.getType();
                                                                          VehicleResult vehicleResult = new Gson().fromJson(sVDetails, type);
                                                                          AlertDialog.Builder alertDialog = new AlertDialog.Builder(VehicleVerification_CED_act.this);
                                                                          alertDialog.setTitle("Vehicle Confirmation ...");
                                                                          alertDialog.setMessage("Number- " + vehicleResult.getFull_Vehicleno() +
                                                                                  "\nCompany- " + vehicleResult.getVehicleCompany() +
                                                                                  "\nModel- " + vehicleResult.getVehicleModel() +
                                                                                  "\nColor- " + vehicleResult.getVehicleColor() +
                                                                                  "\nSeating- " + vehicleResult.getVehicleSeating() +
                                                                                  "\nservice-code- " + vehicleResult.getServiceCode());

                                                                          alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                                              public void onClick(DialogInterface dialog, int which) {
                                                                                  if (!isLoginOne) {
                                                                                      isLoginOne = true;
                                                                                      boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(VehicleVerification_CED_act.this);
                                                                                      if (!isInternetCheck) {
                                                                                          goLogin(aVehicleResults.get(i).getVehicleID());
                                                                                      } else {
                                                                                          snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_LONG);
                                                                                          snackbar.show();
                                                                                      }
                                                                                  }
                                                                              }

                                                                          });
                                                                          alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                                              public void onClick(DialogInterface dialog, int which) {
                                                                                  dialog.cancel();
                                                                              }

                                                                          });
                                                                          if (!VehicleVerification_CED_act.this.isFinishing()) {
                                                                              alertDialog.show();
                                                                          }
                                                                      }
                                                                  }
                                                              });
                                                              alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                                  public void onClick(DialogInterface dialog, int which) {
                                                                      dialog.cancel();
                                                                  }
                                                              });
                                                              if (!VehicleVerification_CED_act.this.isFinishing()) {
                                                                  alertDialog.show();
                                                              }
                                                          }
                                                      }
                );
                dialog.show();
            }
        }
    } //getVehicleAllDtetails

    private void goLogin(final String sVehicleID) {
        final String url = Constants_CED.DRIVER_START_SHIFT_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getLogin(response, sVehicleID);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib_ss.getInstance().getVolleyError(error);
                try {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.d(TAG,
                                "Failed to mVehicleNoVerify :-  "
                                        + error.getMessage());
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "mVehicleNoVerify - " + sErr,
                                e, VehicleVerification_CED_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("vehicleid", sVehicleID);
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, VehicleVerification_CED_act.this);
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
                    SSLog_SS.e(TAG, "mVehicleNoVerify", e, VehicleVerification_CED_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, VehicleVerification_CED_act.this);
    } // goLogin

    private void getLogin(String response, final String sVehicleID) {
        VehicleResult vResult = new VehicleResult();
        response = response.replaceAll("(\\r|\\n)", "");
        if (response.trim().equals("-1") && TextUtils.isEmpty(response.trim())) {
            snackbar = Snackbar
                    .make(coordinatorLayout, "Login failed", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Cancel", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
            snackbar.show();
            return;
        }

        JSONObject jResponse;
        try {
            jResponse = new JSONObject(response);

            sShiftid = jResponse.getString("shift_id");
            sFirstName = jResponse.getString("firstname");
            sLastName = jResponse.getString("lastname");
            sPhoneNo = jResponse.getString("phoneno");
            sDateofBirth = jResponse.getString("dob");
            sGender = jResponse.getString("gender");
            vResult.setFirstName(sFirstName);
            vResult.setLastName(sLastName);
            vResult.setPhoneNo(sPhoneNo);
            vResult.setDateofBirth(sDateofBirth);
            vResult.setSex(sGender);
            vResult.setShift(sShiftid);
            Intent intent = new Intent(VehicleVerification_CED_act.this, DashBoard_Driver_act.class);
            intent.putExtra(Constants_CED.INTENT_SHIFT_ID, sShiftid);
            intent.putExtra(Constants_CED.INTENT_VEHICLE_ID, sVehicleID);
            startActivity(intent);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this)
                    .putString(Constants_CED.PREF_VEHICLE_ID, sVehicleID);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this)
                    .putString(Constants_CED.PREF_SHIFT_ID, sShiftid);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this)
                    .putBoolean(Constants_CED.PREF_LOGIN_LOGOUT_FLAG, true);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this).commit();
            finish();
            hidepDialog();
        } catch (Exception e) {
            SSLog_SS.e(TAG, "getLogin", e, VehicleVerification_CED_act.this);
            snackbar = Snackbar
                    .make(coordinatorLayout, "user Login failed", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Cancel", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
            snackbar.show();
        }

        String json = new Gson().toJson(vResult);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this)
                .putString(Constants_CED.PERF_VALUE_SAVED, json);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this).commit();


    } //getLogin

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

    public boolean isNullNotDefined(JSONObject jo, String jkey) {

        return !jo.has(jkey) || jo.isNull(jkey);

    } //isNullNotDefined
/*
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(VehicleVerification_CED_act.this, DashBoard_Driver_act.class);
        startActivity(intent);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this)
                .putString(Constants_CED.PREF_VEHICLE_ID, "");
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this)
                .putString(Constants_CED.PREF_SHIFT_ID, "");
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this)
                .putBoolean(Constants_CED.PREF_LOGIN_LOGOUT_FLAG, false);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(VehicleVerification_CED_act.this).commit();
        finish();
    }
    */
}
