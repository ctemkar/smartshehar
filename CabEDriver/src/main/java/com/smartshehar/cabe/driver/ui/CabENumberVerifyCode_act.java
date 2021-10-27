package com.smartshehar.cabe.driver.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.smartshehar.cabe.driver.Constants_CED;
import com.smartshehar.cabe.driver.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.CSms;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by jijo_soumen on 11/03/2016.
 * user get phone number verify code and verify phone number
 */
public class CabENumberVerifyCode_act extends AppCompatActivity {

    private static String TAG = "CabEVerifyCode_act: ";
    private CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;
    Toolbar toolbar;
    TextView tvCabENext3;
    EditText etNumberCode;
    TextView tvReSendCode;
    CSms mSms = null;
    String msCode = "", msPhoneNo = "", msEnterCode = "";
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cabenumberverifycode_act);
        init();
        initToolBar();
        pDialog = new ProgressDialog(CabENumberVerifyCode_act.this);
    }

    private void init() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        etNumberCode = (EditText) findViewById(R.id.etNumberCode);
        tvReSendCode = (TextView) findViewById(R.id.tvReSendCode);
        etNumberCode.requestFocus();
    }

    // AppCompat ToolBar
    public void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        CabENumberVerifyCode_act.this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Cab-e Manager");
        }
        tvCabENext3 = (TextView) findViewById(R.id.tvCabENext3);
        tvCabENext3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                msPhoneNo = CGlobals_lib_ss.getInstance().getPersistentPreference(CabENumberVerifyCode_act.this)
                        .getString(Constants_lib_ss.PREF_PHONENO, "");
                msCode = CGlobals_lib_ss.getInstance().getPersistentPreference(CabENumberVerifyCode_act.this)
                        .getString(Constants_lib_ss.PREF_VERIFICATION_CODE, "");
                msEnterCode = etNumberCode.getText().toString();
                if (!TextUtils.isEmpty(msEnterCode)) {
                    if (msEnterCode.equals(msCode)) {
                        sendUserAccess();
                    } else {
                        snackbar = Snackbar
                                .make(coordinatorLayout, "Please enter SMS code", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Cancel", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        snackbar.dismiss();
                                    }
                                });
                        snackbar.show();
                    }
                } else {
                    snackbar = Snackbar
                            .make(coordinatorLayout, "Please back & re-enter 10 digit phone number", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Cancel", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            });
                    snackbar.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CabENumberVerifyCode_act.this, CabENumberVerify_act.class);
        startActivity(intent);
        finish();
    }

    public void onClickNext23(View view) {
        msPhoneNo = CGlobals_lib_ss.getInstance().getPersistentPreference(CabENumberVerifyCode_act.this)
                .getString(Constants_lib_ss.PREF_PHONENO, "");
        msCode = CGlobals_lib_ss.getInstance().getPersistentPreference(CabENumberVerifyCode_act.this)
                .getString(Constants_lib_ss.PREF_VERIFICATION_CODE, "");
        msEnterCode = etNumberCode.getText().toString();
        if (!TextUtils.isEmpty(msEnterCode)) {
            if (msEnterCode.equals(msCode)) {
                sendUserAccess();
            } else {
                snackbar = Snackbar
                        .make(coordinatorLayout, "Please enter SMS code", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Cancel", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                snackbar.show();
            }
        } else {
            snackbar = Snackbar
                    .make(coordinatorLayout, "Please back & re-enter 10 digit phone number", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Cancel", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
            snackbar.show();
        }
    }

    public void onClickResendCode(View view) {
        try {
            msCode = generate4DigitCode();
            msPhoneNo = CGlobals_lib_ss.getInstance().getPersistentPreference(CabENumberVerifyCode_act.this)
                    .getString(Constants_lib_ss.PREF_PHONENO, "");
            if (!TextUtils.isEmpty(msCode) && !TextUtils.isEmpty(msPhoneNo)) {
                mSms.sendSMS(msPhoneNo, CabENumberVerifyCode_act.this.getString(R.string.cabE_codeHint) + msCode);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerifyCode_act.this)
                        .putString(Constants_lib_ss.PREF_VERIFICATION_CODE, msCode);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerifyCode_act.this).commit();
            } else {
                snackbar = Snackbar
                        .make(coordinatorLayout, "Invalid phone number. Please back & re-enter 10 digit phone number", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Cancel", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                snackbar.show();
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "ResendCode: ", e, CabENumberVerifyCode_act.this);
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

    protected void onPause() {
        if (mSms != null) {
            mSms.unregisterReceivers();
        }
        super.onPause();
    }

    String generate4DigitCode() {
        Random r = new Random();
        List<Integer> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int x = r.nextInt(9999);
            while (codes.contains(x))
                x = r.nextInt(9999);
            codes.add(x);
        }
        return String.format(Locale.getDefault(), "%04d", codes.get(0));
    }

    public void sendUserAccess() {
        if (pDialog != null) {
            pDialog.setMessage(CabENumberVerifyCode_act.this.getString(R.string.please_wait_cabe));
            if (!CabENumberVerifyCode_act.this.isFinishing()) {
                pDialog.show();
            }
        }
        final String url = Constants_CED.USER_ACCESS_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        userAccessSucess(response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                userAccessFailure(error);
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "sendUserAccess :-   ", error, CabENumberVerifyCode_act.this);
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (!TextUtils.isEmpty(CGlobals_lib_ss.getInstance().getPersistentPreference(CabENumberVerifyCode_act.this).getString(
                        Constants_CED.PREF_REG_ID, ""))) {
                    params.put("gcmregid", CGlobals_lib_ss.getInstance().getPersistentPreference(CabENumberVerifyCode_act.this).getString(
                            Constants_CED.PREF_REG_ID, ""));
                }
                params.put("verified", String.valueOf(1));
                params = CGlobals_lib_ss.getInstance().getAllMobileParams(params,
                        url, Constants_CED.APP_CODE, CabENumberVerifyCode_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";

                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString();
                    Log.e(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getPassengers map - ", e, CabENumberVerifyCode_act.this);
                }

                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, CabENumberVerifyCode_act.this);

    } // sendUserAccess

    private void userAccessSucess(String response) {
        if (!CabENumberVerifyCode_act.this.isFinishing()) {
            pDialog.setCancelable(true);
            pDialog.cancel();
        }
        String sAppUserId = "-1";
        int iAppUserId = -1;
        try {
            if (response.trim().equals("-1")) {

                snackbar = Snackbar.make(coordinatorLayout, "Your device not registered", Snackbar.LENGTH_LONG);
                snackbar.show();
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerifyCode_act.this)
                        .putBoolean(Constants_CED.PREF_CABE_NUMBER_VERIFY_DONE, false);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerifyCode_act.this).commit();
                Intent intent2 = new Intent(CabENumberVerifyCode_act.this, CabERegistration_act.class);
                startActivity(intent2);
                finish();
                return;
            }
            if (!response.trim().equals("-1")) {
                Log.d(TAG, "Useraccess_url - " + response);
                JSONObject jResponse = new JSONObject(response);
                Log.d(TAG, response);
                sAppUserId = jResponse.isNull("appuser_id") ? "-1" : jResponse
                        .getString("appuser_id");
                if (!sAppUserId.equals("-1")) {
                    iAppUserId = Integer.parseInt(sAppUserId);
                }
            }
            if (sAppUserId.trim().equals("-1")) { // registration probably

                snackbar = Snackbar.make(coordinatorLayout, "Your device not registered", Snackbar.LENGTH_LONG);
                snackbar.show();
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerifyCode_act.this)
                        .putBoolean(Constants_CED.PREF_CABE_NUMBER_VERIFY_DONE, false);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerifyCode_act.this).commit();
                Intent intent2 = new Intent(CabENumberVerifyCode_act.this, CabERegistration_act.class);
                startActivity(intent2);
                finish();
            } else {
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerifyCode_act.this).
                        putInt(Constants_lib_ss.PREF_APPUSERID, iAppUserId);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerifyCode_act.this)
                        .putBoolean(Constants_CED.PREF_CABE_NUMBER_VERIFY_DONE, true);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerifyCode_act.this).commit();
                CGlobals_lib_ss.getInstance().miAppUserId = iAppUserId;
                Intent intent2 = new Intent(CabENumberVerifyCode_act.this, CabERegistration_act.class);
                startActivity(intent2);
                finish();
            }
        } catch (Exception e) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerifyCode_act.this)
                    .putBoolean(Constants_CED.PREF_CABE_NUMBER_VERIFY_DONE, false);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerifyCode_act.this).commit();
            Intent intent2 = new Intent(CabENumberVerifyCode_act.this, CabERegistration_act.class);
            startActivity(intent2);
            finish();
            SSLog_SS.e(TAG, "sendUserAccess Response: " + response, e, CabENumberVerifyCode_act.this);
            Log.d("Response", response);
            snackbar = Snackbar.make(coordinatorLayout, "Bad data received, try after some time", Snackbar.LENGTH_LONG);
            snackbar.show();

        }

    } // userAccessSuccess

    private void userAccessFailure(VolleyError error) {
        if (!CabENumberVerifyCode_act.this.isFinishing()) {
            pDialog.setCancelable(true);
            pDialog.cancel();
        }
        snackbar = Snackbar.make(coordinatorLayout, "Your device not registered", Snackbar.LENGTH_LONG);
        snackbar.show();
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerifyCode_act.this)
                .putBoolean(Constants_CED.PREF_CABE_NUMBER_VERIFY_DONE, false);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerifyCode_act.this).commit();
        Intent intent2 = new Intent(CabENumberVerifyCode_act.this, CabERegistration_act.class);
        startActivity(intent2);
        finish();
        try {
            SSLog_SS.e(TAG, "userAccessFailure :-   ", error, CabENumberVerifyCode_act.this);
            CGlobals_lib_ss.getInstance().getVolleyError(error);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "sendUserAccess - Response.ErrorListener", e, CabENumberVerifyCode_act.this);
        }
    }

}
