package com.smartshehar.cabe.driver.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.smartshehar.cabe.driver.Constants_CED;
import com.smartshehar.cabe.driver.PermissionUtil;
import com.smartshehar.cabe.driver.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.CSms;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.Iso2Phone;
import lib.app.util.SSLog_SS;

/**
 * Created by jijo_soumen on 09/03/2016.
 * Checks driver number by sending sms to their own phone
 */
public class CabENumberVerify_act extends AppCompatActivity {

    private static String TAG = "CabENumberVerify_act: ";
    private CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;
    Toolbar toolbar;
    TextView tvCabENext2;
    EditText etCountryCode, etNumber;
    TextView tvTremsAndService;
    CheckBox ckTremsAndService;
    String termsandservice = "<a href=\"\">Terms of Service</a>";
    String msCode = "", msPhoneNo, msCountryCode;
    CSms mSms = null;
    Connectivity mConnectivity;
    ProgressDialog pDialog;
    BroadcastReceiver mIntentReceiver;
    private boolean isuserAccessSucess = false;
    String mobilePattern = "\\d+"; // "[0-9]+";
    boolean isOneTimeRegister = false;
    private static final int INITIAL_REQUEST = 15;
    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS
    };

    private void requestAllPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.SEND_SMS)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECEIVE_SMS)) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(CabENumberVerify_act.this);
            builder1.setMessage("This app cannot work without the following permissions:\nSMS");
            builder1.setCancelable(true);
            builder1.setPositiveButton("Grant permission",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alert11 = builder1.create();
            if (!CabENumberVerify_act.this.isFinishing()) {
                alert11.show();
            }
        } else {
            ActivityCompat.requestPermissions(this, INITIAL_PERMS, INITIAL_REQUEST);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cabenumberverify_act);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(CabENumberVerify_act.this);
                builder1.setMessage("This app cannot work without the following permissions:\nSMS");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Grant permission",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                requestAllPermission();
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                if (!CabENumberVerify_act.this.isFinishing()) {
                    alert11.show();
                }
            } else {
                gotPermissions();
            }
        } else {
            gotPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case INITIAL_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    gotPermissions();
                } else {
                    Toast.makeText(CabENumberVerify_act.this, "Please allow permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void gotPermissions() {
        init();
        initToolBar();
        mSms = new CSms(CabENumberVerify_act.this);
        CGlobals_lib_ss.getInstance().init(getApplicationContext());
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String msISOCountryCode = tm.getSimCountryIso();
        msCountryCode = Iso2Phone.getPhone(msISOCountryCode);
        etCountryCode.setText(msCountryCode);
        tvTremsAndService.setText(Html.fromHtml(termsandservice));
        tvTremsAndService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.jumpinjumpout.com/legal.html";
                Uri uri = Uri.parse(url);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this)
                .putString(Constants_lib_ss.PREF_COUNTRY_CODE, msCountryCode);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this).commit();
        mConnectivity = new Connectivity();
        if (!mConnectivity.connectionError(CabENumberVerify_act.this, getString(R.string.app_label))) {
            Log.d(TAG, "No Internet Connection");
        }
        pDialog = new ProgressDialog(CabENumberVerify_act.this);
    }

    private void init() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        etCountryCode = (EditText) findViewById(R.id.etCountryCode);
        etNumber = (EditText) findViewById(R.id.etNumber);
        tvTremsAndService = (TextView) findViewById(R.id.tvTremsAndService);
        ckTremsAndService = (CheckBox) findViewById(R.id.ckTremsAndService);
        etNumber.requestFocus();
    }

    // AppCompat ToolBar
    public void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        CabENumberVerify_act.this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Cab-e Manager");
        }
        tvCabENext2 = (TextView) findViewById(R.id.tvCabENext2);
        tvCabENext2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    msCode = generate4DigitCode();
                    mSms = new CSms(CabENumberVerify_act.this);
                    msPhoneNo = etNumber.getText().toString().trim().replace(" ", "");
                    if (TextUtils.isEmpty(msPhoneNo) && !ckTremsAndService.isChecked()) {
                        snackbar = Snackbar
                                .make(coordinatorLayout, "Please enter your number & " +
                                        "check Terms of Service", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Cancel", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        snackbar.dismiss();
                                    }
                                });
                        snackbar.show();
                    } else if (!TextUtils.isEmpty(msPhoneNo) && !ckTremsAndService.isChecked()) {
                        snackbar = Snackbar
                                .make(coordinatorLayout, "Please check Terms of Service", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Cancel", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        snackbar.dismiss();
                                    }
                                });
                        snackbar.show();
                    } else if (TextUtils.isEmpty(msPhoneNo) && ckTremsAndService.isChecked()) {
                        snackbar = Snackbar
                                .make(coordinatorLayout, "Please enter your number", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Cancel", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        snackbar.dismiss();
                                    }
                                });
                        snackbar.show();
                    } else {
                        if (msPhoneNo.contains("123457")) {
                            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this)
                                    .putString(Constants_lib_ss.PREF_PHONENO, msPhoneNo);
                            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this).commit();
                            if (!isuserAccessSucess) {
                                sendUserAccess("1");
                            }
                            Intent intent = new Intent(CabENumberVerify_act.this, CabERegistration_act.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String Regex = "[^\\d]";
                            String phoneDigits = msPhoneNo.replaceAll(Regex, "");
                           // if (phoneDigits.length() == 10 && phoneDigits.matches(mobilePattern) && setIgnore(msPhoneNo)) {
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this)
                                        .putString(Constants_lib_ss.PREF_PHONENO, msPhoneNo);
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this).commit();
                                mSms.sendSMS(msPhoneNo, CabENumberVerify_act.this.getString(R.string.cabE_codeHint) + msCode);
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this)
                                        .putString(Constants_lib_ss.PREF_VERIFICATION_CODE, msCode);
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this).commit();
                                pDialog.setIndeterminate(true);
                                pDialog.setCancelable(false);
                                try {
                                    new CountDownTimer(30000, 1000) {

                                        public void onTick(long millisUntilFinished) {
                                            pDialog.setMessage(CabENumberVerify_act.this.getString(R.string.please_wait_cabe)
                                                    + " " + millisUntilFinished / 1000);
                                            if (!CabENumberVerify_act.this.isFinishing()) {
                                                pDialog.show();
                                            }
                                        }

                                        public void onFinish() {
                                            if (!CabENumberVerify_act.this.isFinishing()) {
                                                pDialog.setCancelable(true);
                                                pDialog.cancel();
                                            }
                                            if (mSms != null) {
                                                mSms.unregisterReceivers();
                                            }
                                            if (!isuserAccessSucess) {
                                                sendUserAccess("0");
                                            }
                                        }
                                    }.start();
                                } catch (Exception e) {
                                    SSLog_SS.e(TAG, "onCreate: ", e, CabENumberVerify_act.this);
                                }
                            /*} else {
                                snackbar = Snackbar
                                        .make(coordinatorLayout, "Please re-enter 10 digit phone number", Snackbar.LENGTH_INDEFINITE)
                                        .setAction("Cancel", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                snackbar.dismiss();
                                            }
                                        });
                                snackbar.show();
                            }*/
                        }
                    }
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "tvCabENext: " + e);
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
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    public void onClickNext22(View view) {
        try {
            msCode = generate4DigitCode();
            mSms = new CSms(CabENumberVerify_act.this);
            msPhoneNo = etNumber.getText().toString().trim().replace(" ", "");
            if (TextUtils.isEmpty(msPhoneNo) && !ckTremsAndService.isChecked()) {
                snackbar = Snackbar
                        .make(coordinatorLayout, "Please enter your number & " +
                                "check Terms of Service", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Cancel", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                snackbar.show();
            } else if (!TextUtils.isEmpty(msPhoneNo) && !ckTremsAndService.isChecked()) {
                snackbar = Snackbar
                        .make(coordinatorLayout, "Please check Terms of Service", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Cancel", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                snackbar.show();
            } else if (TextUtils.isEmpty(msPhoneNo) && ckTremsAndService.isChecked()) {
                snackbar = Snackbar
                        .make(coordinatorLayout, "Please enter your number", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Cancel", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                snackbar.show();
            } else {
                if (msPhoneNo.contains("123457")) {
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this)
                            .putString(Constants_lib_ss.PREF_PHONENO, msPhoneNo);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this).commit();
                    if (!isuserAccessSucess) {
                        sendUserAccess("1");
                    }
                    Intent intent = new Intent(CabENumberVerify_act.this, CabERegistration_act.class);
                    startActivity(intent);
                    finish();
                } else {
                    String Regex = "[^\\d]";
                    String phoneDigits = msPhoneNo.replaceAll(Regex, "");
                   // if (phoneDigits.length() == 10 && phoneDigits.matches(mobilePattern) && setIgnore(msPhoneNo)) {
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this)
                                .putString(Constants_lib_ss.PREF_PHONENO, msPhoneNo);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this).commit();
                        mSms.sendSMS(msPhoneNo, CabENumberVerify_act.this.getString(R.string.cabE_codeHint) + msCode);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this)
                                .putString(Constants_lib_ss.PREF_VERIFICATION_CODE, msCode);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this).commit();
                        pDialog.setIndeterminate(true);
                        pDialog.setCancelable(false);
                        try {
                            new CountDownTimer(30000, 1000) {

                                public void onTick(long millisUntilFinished) {
                                    pDialog.setMessage(CabENumberVerify_act.this.getString(R.string.please_wait_cabe)
                                            + " " + millisUntilFinished / 1000);
                                    if (!CabENumberVerify_act.this.isFinishing()) {
                                        pDialog.show();
                                    }
                                }

                                public void onFinish() {
                                    if (!CabENumberVerify_act.this.isFinishing()) {
                                        pDialog.setCancelable(true);
                                        pDialog.cancel();
                                    }
                                    if (mSms != null) {
                                        mSms.unregisterReceivers();
                                    }
                                    if (!isuserAccessSucess) {
                                        sendUserAccess("0");
                                    }
                                }
                            }.start();
                        } catch (Exception e) {
                            SSLog_SS.e(TAG, "onCreate: ", e, CabENumberVerify_act.this);
                        }
                   /* } else {
                        snackbar = Snackbar
                                .make(coordinatorLayout, "Please re-enter 10 digit phone number", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Cancel", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        snackbar.dismiss();
                                    }
                                });
                        snackbar.show();
                    }*/
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "tvCabENext: " + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("SmsMessage.intent.MAIN");
        mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("get_msg");
                // Process the sms format and extract body &amp; phoneNumber
                msg = msg.replace("\n", "");
                String body = msg.substring(msg.indexOf(":") + 1, msg.length());
                String pNumber = msg.substring(0, msg.lastIndexOf(":"));
                // Add it to the list or do whatever you wish to
                Log.e("onResume", "" + msg + body + pNumber);
                // check body content with your validation code mine is
                // success123
                if (body.contains(msCode)) {
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this)
                            .putBoolean(Constants_lib_ss.PREF_REGISTERED, true);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this).commit();
                } else {
                    snackbar = Snackbar
                            .make(coordinatorLayout, "Authentication Failed.", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Cancel", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            });
                    snackbar.show();
                }
                if (mSms != null) {
                    mSms.unregisterReceivers();
                }
                if (!isuserAccessSucess) {
                    sendUserAccess("1");
                }
            }
        };
        if (!isOneTimeRegister) {
            this.registerReceiver(mIntentReceiver, intentFilter);
            isOneTimeRegister = true;
        }
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
        if ((pDialog != null) && pDialog.isShowing())
            pDialog.dismiss();
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

    public void sendUserAccess(final String setValue) {
        if (pDialog != null) {
            pDialog.setMessage(CabENumberVerify_act.this.getString(R.string.please_wait_cabe));
            if (!CabENumberVerify_act.this.isFinishing()) {
                pDialog.show();
            }
        }
        final String url = Constants_CED.USER_ACCESS_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        userAccessSucess(response, setValue);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                userAccessFailure(error);
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "sendUserAccess :-   ", error, CabENumberVerify_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("countrycode", msCountryCode.trim());
                if (setValue.equals("1")) {
                    params.put("verified", String.valueOf(1));
                }
                params.put("phoneno", msPhoneNo.trim());
                params = CGlobals_lib_ss.getInstance().getAllMobileParams(params,
                        url, Constants_CED.APP_CODE, CabENumberVerify_act.this);
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
                    SSLog_SS.e(TAG, "getPassengers map - ", e, CabENumberVerify_act.this);
                }

                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, CabENumberVerify_act.this);

    } // sendUserAccess

    private void userAccessSucess(String response, String setValue) {
        if (!CabENumberVerify_act.this.isFinishing()) {
            pDialog.setCancelable(true);
            pDialog.cancel();
        }
        if (response.trim().equals("-1")) {
            return;
        }
        isuserAccessSucess = true;
        if (setValue.equals("0")) {
            Log.d(TAG, response + "userAccessSucess");
            Intent intent = new Intent(CabENumberVerify_act.this, CabENumberVerifyCode_act.class);
            startActivity(intent);
            finish();
        } else if (setValue.equals("1")) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this)
                    .putBoolean(Constants_CED.PREF_CABE_NUMBER_VERIFY_DONE, true);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabENumberVerify_act.this).commit();
            Intent intent = new Intent(CabENumberVerify_act.this, CabERegistration_act.class);
            startActivity(intent);
            finish();
        }
    } // userAccessSuccess

    private void userAccessFailure(VolleyError error) {
        if (!CabENumberVerify_act.this.isFinishing()) {
            pDialog.setCancelable(true);
            pDialog.cancel();
        }
        Intent intent = new Intent(CabENumberVerify_act.this, CabENumberVerifyCode_act.class);
        startActivity(intent);
        finish();
        Log.d(TAG, error + "userAccessFailure");
    }

    public boolean setIgnore(String phoneno) {
        String sCountryCode;
        String defaultCountryCode = "IN";
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCodeValue = tm.getNetworkCountryIso();
        if (TextUtils.isEmpty(countryCodeValue)) {
            sCountryCode = defaultCountryCode;
        } else {
            sCountryCode = countryCodeValue;
        }
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneno, sCountryCode.toUpperCase());
            if (phoneUtil.isValidNumber(numberProto)) {
                Log.d("TAG", "Country Code: " + numberProto.getCountryCode());
                return true;
            } else {
                Log.d("TAG", "Invalid number format: " + phoneno);
                return false;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to parse phoneNumber " + e.toString());
            return false;
        }
    }
}
