package com.smartshehar.dashboard.app.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.R;

import java.util.HashMap;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;

public class ActUserRegistration extends AppCompatActivity {
    private static String TAG = "UserRegistration";
    Connectivity mConnectivity;
    EditText etName, etAge, etUserName, etContactNo;
    Spinner spGender;
    private String user_Name;
    private String user_Gender;
    private String user_Full_Name;
    private String user_Age;
    private String user_Contact_No;
    private static final String[] selectGender = {"GENDER", "MALE", "FEMALE"};
    ProgressDialog pd;
    Snackbar snackbar;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(ActUserRegistration.this)) {
            if (!mConnectivity.connectionError(ActUserRegistration.this)) {
                if (mConnectivity.isGPSEnable(ActUserRegistration.this)) {
                    Log.d(TAG, "Internet Connection");
                }
            }
        }
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        pd = new ProgressDialog(ActUserRegistration.this);
        etName = (EditText) findViewById(R.id.etName);
        etAge = (EditText) findViewById(R.id.etAge);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etContactNo = (EditText) findViewById(R.id.etContactNo);
        spGender = (Spinner) findViewById(R.id.spGender);

        user_Name = CGlobals_lib_ss.getInstance()
                .getPersistentPreference(ActUserRegistration.this).getString(Constants_dp.PREF_USER_NAME, "");
        user_Full_Name = CGlobals_lib_ss.getInstance()
                .getPersistentPreference(ActUserRegistration.this).getString(Constants_dp.PREF_USER_FULL_NAME, "");
        user_Age = CGlobals_lib_ss.getInstance()
                .getPersistentPreference(ActUserRegistration.this).getString(Constants_dp.PREF_USER_AGE, "");
        user_Contact_No = CGlobals_lib_ss.getInstance()
                .getPersistentPreference(ActUserRegistration.this).getString(Constants_lib_ss.PREF_PHONENO, "");
//                SSApp.getInstance().getPersistentPreference().getString(Constants_lib_ss.PREF_PHONENO, "").toString();
        user_Gender = CGlobals_lib_ss.getInstance()
                .getPersistentPreference(ActUserRegistration.this).getString(Constants_dp.PREF_USER_GENDER, "");

        // Set Spinner Value and select spinner value
        ArrayAdapter<String> adaptergender = new ArrayAdapter<>(
                ActUserRegistration.this,
                android.R.layout.simple_spinner_item, selectGender);
        adaptergender
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(adaptergender);


        etUserName.setText(user_Name);
        etName.setText(user_Full_Name);
        etAge.setText(user_Age);
        etContactNo.setText(user_Contact_No);
        if (TextUtils.isEmpty(user_Contact_No)) {
            etContactNo.setEnabled(true);

        }
        for (int i = 0; i < selectGender.length; i++) {
            if (user_Gender.equals(selectGender[i])) {
                spGender.setSelection(i);
            }
        }
        etContactNo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && TextUtils.isEmpty(user_Contact_No)) {
                    Intent intent = new Intent(ActUserRegistration.this, ActVerifyNumber.class);
                    startActivityForResult(intent, Constants_lib_ss.Request_Code);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants_lib_ss.Request_Code) {
            if (resultCode == RESULT_OK) {
                user_Contact_No = CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(ActUserRegistration.this).getString(Constants_lib_ss.PREF_PHONENO, "");
                etContactNo.setText(user_Contact_No);
                etContactNo.setEnabled(false);
            }
        }
    }

    public void onSubmit(View v) {
        user_Name = etUserName.getText().toString().trim();
        user_Full_Name = etName.getText().toString().trim();
        user_Age = etAge.getText().toString().trim();
        user_Contact_No = etContactNo.getText().toString().trim();
        user_Gender = spGender.getSelectedItem().toString();
        if (!mConnectivity.connectionError(ActUserRegistration.this)) {

            if (TextUtils.isEmpty(user_Name)) {
                showSnackbar("Please enter user name");
                return;
            }
            if (TextUtils.isEmpty(user_Contact_No)) {
                showSnackbar("Please enter contact no.");
                return;
            }
            if (user_Contact_No.length() != 10) {
                showSnackbar("Contact no. must have 10 digits");
                return;
            }
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(ActUserRegistration.this).putString(Constants_dp.PREF_USER_NAME, user_Name).commit();
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(ActUserRegistration.this).putString(Constants_dp.PREF_USER_FULL_NAME, user_Full_Name).commit();
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(ActUserRegistration.this).putString(Constants_dp.PREF_USER_AGE, user_Age).commit();
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(ActUserRegistration.this).putString(Constants_dp.PREF_USER_GENDER, user_Gender).commit();
            sendDataToServer();
        }
    }

    private void sendDataToServer() {

        try {

            pd.setMessage("Connecting..."); //
            pd.show();
            final String url = Constants_dp.UPDATE_APPUSER_DETAILS_URL;

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                Log.d(TAG, "" + response);
                                CGlobals_lib_ss.getInstance()
                                        .getPersistentPreferenceEditor(ActUserRegistration.this).putBoolean(Constants_dp.PREF_USER_REGISTRATION, true).commit();
                                ActUserRegistration.this.finish();
                            } else {
                                showSnackbar("Registration failed.");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//
                    if (pd != null)
                        pd.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params.put("username", String.valueOf(user_Name));
                    params.put("phoneno", String.valueOf(user_Contact_No));
                    if (!TextUtils.isEmpty(user_Gender) && !TextUtils.isEmpty("GENDER"))
                        params.put("sex", String.valueOf(user_Gender));
                    if (!TextUtils.isEmpty(user_Full_Name))
                        params.put("fullname", String.valueOf(user_Full_Name));
                    if (!TextUtils.isEmpty(user_Age))
                        params.put("age", String.valueOf(user_Age));

                    params = CGlobals_db.getInstance(ActUserRegistration.this).getBasicMobileParams(params,
                            url, ActUserRegistration.this);

                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_db.getInstance(ActUserRegistration.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(ActUserRegistration.this).getRequestQueue(ActUserRegistration.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }
    private void showSnackbar(String msg)
    {
        snackbar = Snackbar
                .make(coordinatorLayout,msg, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);

        snackbar.show();
    }
}