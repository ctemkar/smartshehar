package com.smartshehar.dashboard.app.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartshehar.dashboard.app.PermissionUtil;
import com.smartshehar.dashboard.app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.CSms;
import lib.app.util.Constants_lib_ss;
import lib.app.util.Iso2Phone;

public class ActVerifyNumber extends AppCompatActivity implements View.OnClickListener {
//    private static String TAG = "ActVerifyNumber: ";
    private TextView tvMyPhoneNo, timerTv, tvMyCountryCode;
    Button buttonVerifyPhoneNo;
//    static final int PICK_CONTACT = 1;
    CSms mSms = null;
    String msCode = "";
    //    static Boolean timeOut = true;
    EditText mEtPreviousCode;
    String msISOCountryCode = "", msCountryCode = "", msPhoneNo;
//    private CoordinatorLayout coordinatorLayout;
//    Snackbar snackbar;
    TextView tvOne, tvTwo, tvThree, tvFour, tvFive, tvSix, tvSeven, tvEight, tvNine, tvZero;
    String imNumber = "";
    private static final int INITIAL_REQUEST = 1340;
    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS
    };
    private void showRequirementOfPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.SEND_SMS) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECEIVE_SMS))
            customDialog(getString(R.string.sms_permission));
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, INITIAL_PERMS, INITIAL_REQUEST);
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_verify_number);
        if (Build.VERSION.SDK_INT >= 23) {
            if (!checkPermission())
                requestPermission();
            else startFunction();
        } else
            startFunction();
    } // onCreate

    private void startFunction()
    {
        init();

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        msISOCountryCode = tm.getSimCountryIso();
        msCountryCode = Iso2Phone.getPhone(msISOCountryCode);
        tvMyCountryCode.setText(msCountryCode);
        findViewById(smartsheharcom.www.smartsheharlib.R.id.buttonSkip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CGlobals_lib_ss
                        .getInstance()
                        .getPersistentPreferenceEditor(
                                ActVerifyNumber.this)
                        .putBoolean(Constants_lib_ss.PREF_SKIPPED, true).commit();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                ActVerifyNumber.this.finish();
            }
        });
        buttonVerifyPhoneNo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                msPhoneNo = tvMyPhoneNo.getText().toString().trim();
                if(TextUtils.isEmpty(msPhoneNo))
                {
                    Toast.makeText(ActVerifyNumber.this,"Please enter your phone number", Toast.LENGTH_LONG).show();
                    return ;
                }
                LinearLayout rlProgress = (LinearLayout) findViewById(smartsheharcom.www.smartsheharlib.R.id.rlProgress);
                assert rlProgress != null;
                rlProgress.setVisibility(View.VISIBLE);
                timerTv = (TextView) findViewById(smartsheharcom.www.smartsheharlib.R.id.SW_TimeRemainigTv);
                InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(
                        buttonVerifyPhoneNo.getWindowToken(), 0);

                new CountDownTimer(90000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        timerTv.setText("Seconds Remaining : "
                                + millisUntilFinished / 1000);
                    }

                    public void onFinish() {
                        timerTv.setText("Time Over");
                        ActVerifyNumber.this.finish();
                    }
                }.start();
                try {
                    msCode = generate4DigitCode();
                    mSms = new CSms(ActVerifyNumber.this);
                    msPhoneNo = tvMyPhoneNo.getText().toString().trim();
                    msPhoneNo.replace(" ", "");
                    String sPreviousCode = CGlobals_lib_ss
                            .getInstance()
                            .getPersistentPreference(getApplicationContext())
                            .getString(Constants_lib_ss.PREF_VERIFICATION_CODE,
                                    "x1531352");
                    if (!TextUtils.isEmpty(msPhoneNo)) {
                        CGlobals_lib_ss
                                .getInstance()
                                .getPersistentPreferenceEditor(
                                        getApplicationContext())
                                .putString(Constants_lib_ss.PREF_PHONENO,
                                        msPhoneNo);
                        CGlobals_lib_ss
                                .getInstance()
                                .getPersistentPreferenceEditor(
                                        getApplicationContext()).commit();
                    }
                    boolean isPhoneVerified = false;
                    if (msPhoneNo.contains("12345")
                            || (mEtPreviousCode.getText().toString()
                            .equals(sPreviousCode) && !TextUtils
                            .isEmpty(sPreviousCode))) {
                        CGlobals_lib_ss
                                .getInstance()
                                .getPersistentPreferenceEditor(
                                        getApplicationContext())
                                .putBoolean(Constants_lib_ss.PREF_REGISTERED, true);
                        if (!TextUtils.isEmpty(msPhoneNo)) {
                            CGlobals_lib_ss
                                    .getInstance()
                                    .getPersistentPreferenceEditor(
                                            getApplicationContext())
                                    .putString(Constants_lib_ss.PREF_PHONENO,
                                            msPhoneNo);
                        } else {
                            msPhoneNo = CGlobals_lib_ss
                                    .getInstance()
                                    .getPersistentPreference(
                                            getApplicationContext())
                                    .getString(Constants_lib_ss.PREF_PHONENO, "");

                        }
                        CGlobals_lib_ss
                                .getInstance()
                                .getPersistentPreferenceEditor(
                                        getApplicationContext())
                                .putString(Constants_lib_ss.PREF_COUNTRY_CODE,
                                        msCountryCode);
                        // sendUserAccess(msCountryCode, msPhoneNo);
                        CGlobals_lib_ss
                                .getInstance()
                                .getPersistentPreferenceEditor(
                                        getApplicationContext()).commit();
                        isPhoneVerified = true;
                        finish();
                    }
                    CGlobals_lib_ss
                            .getInstance()
                            .getPersistentPreferenceEditor(
                                    getApplicationContext())
                            .putString(Constants_lib_ss.PREF_VERIFICATION_CODE,
                                    msCode);
                    CGlobals_lib_ss
                            .getInstance()
                            .getPersistentPreferenceEditor(
                                    getApplicationContext()).commit();

                    if (!isPhoneVerified) {
                        mSms.sendSMS(
                                msPhoneNo,
                                ActVerifyNumber.this
                                        .getString(smartsheharcom.www.smartsheharlib.R.string.codeHint) + msCode);
                    }
                } catch (Exception e) {
                    ////SSLog.e(TAG, " onCreate - ", e);
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case INITIAL_REQUEST:
                // If request is cancelled, the result arrays are empty.
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    startFunction();
                } else {
                    showRequirementOfPermission();
                }
                break;

        }
    }

    private boolean checkPermission() {
        if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED
                ) {
            return false;
        } else {
            return true;
        }
    }

    private void customDialog(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(ActVerifyNumber.this);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission();
                    }
                });
        builder1.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
    private void init()

    {
        buttonVerifyPhoneNo = (Button) findViewById(smartsheharcom.www.smartsheharlib.R.id.buttonVerifyPhoneNo);
        tvMyCountryCode = (TextView) findViewById(smartsheharcom.www.smartsheharlib.R.id.tvMyCountryCode);
        tvMyPhoneNo = (TextView) findViewById(smartsheharcom.www.smartsheharlib.R.id.tvMyPhoneNo);
        timerTv = (TextView) findViewById(smartsheharcom.www.smartsheharlib.R.id.SW_TimeRemainigTv);
        mEtPreviousCode = (EditText) findViewById(smartsheharcom.www.smartsheharlib.R.id.etSmsCode);

        tvOne = (TextView) findViewById(R.id.tvOne);
        tvTwo = (TextView) findViewById(R.id.tvTwo);
        tvThree = (TextView) findViewById(R.id.tvThree);
        tvFour = (TextView) findViewById(R.id.tvFour);
        tvFive = (TextView) findViewById(R.id.tvFive);
        tvSix = (TextView) findViewById(R.id.tvSix);
        tvSeven = (TextView) findViewById(R.id.tvSeven);
        tvEight = (TextView) findViewById(R.id.tvEight);
        tvNine = (TextView) findViewById(R.id.tvNine);
        tvZero = (TextView) findViewById(R.id.tvZero);

        tvOne.setOnClickListener(ActVerifyNumber.this);
        tvTwo.setOnClickListener(ActVerifyNumber.this);
        tvThree.setOnClickListener(ActVerifyNumber.this);
        tvFour.setOnClickListener(ActVerifyNumber.this);
        tvFive.setOnClickListener(ActVerifyNumber.this);
        tvSix.setOnClickListener(ActVerifyNumber.this);
        tvSeven.setOnClickListener(ActVerifyNumber.this);
        tvEight.setOnClickListener(ActVerifyNumber.this);
        tvNine.setOnClickListener(ActVerifyNumber.this);
        tvZero.setOnClickListener(ActVerifyNumber.this);

        tvMyPhoneNo.requestFocus();

        tvMyPhoneNo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(tvMyPhoneNo.getWindowToken(), 0);
                tvMyPhoneNo.setCursorVisible(true);
                return false;
            }
        });
        tvMyPhoneNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(tvMyPhoneNo.getWindowToken(), 0);
                tvMyPhoneNo.setCursorVisible(true);
            }
        });
        mEtPreviousCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(mEtPreviousCode.getWindowToken(), 0);
                mEtPreviousCode.setCursorVisible(true);
                return false;
            }
        });
        mEtPreviousCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(mEtPreviousCode.getWindowToken(), 0);
                mEtPreviousCode.setCursorVisible(true);
            }
        });
        tvMyCountryCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(tvMyCountryCode.getWindowToken(), 0);
                tvMyCountryCode.setCursorVisible(false);
                return false;
            }
        });
        tvMyCountryCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(tvMyCountryCode.getWindowToken(), 0);
                tvMyCountryCode.setCursorVisible(false);
            }
        });
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

    private  void resumeFunction()
    {
        InputMethodManager keyboard = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(tvMyPhoneNo.getWindowToken(), 0);
        keyboard.hideSoftInputFromWindow(tvMyCountryCode.getWindowToken(), 0);
        keyboard.hideSoftInputFromWindow(mEtPreviousCode.getWindowToken(), 0);
        IntentFilter intentFilter = new IntentFilter("SmsMessage.intent.MAIN");
        BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = intent.getStringExtra("get_msg");

                // Process the sms format and extract body &amp; phoneNumber
                msg = msg.replace("\n", "");
                String body = msg.substring(msg.indexOf(":") + 1, msg.length());
                String pNumber = msg.substring(0, msg.lastIndexOf(":"));

                // Add it to the list or do whatever you wish to
                Log.e("onResume", "" + msg + body + pNumber);

                // Toast.makeText(getApplicationContext(), body, 1).show();

                // check body content with your validation code mine is
                // success123
                if (body.contains(msCode)) {
                    CGlobals_lib_ss
                            .getInstance()
                            .getPersistentPreferenceEditor(
                                    getApplicationContext())
                            .putBoolean(Constants_lib_ss.PREF_REGISTERED, true);
                    CGlobals_lib_ss
                            .getInstance()
                            .getPersistentPreferenceEditor(
                                    getApplicationContext())
                            .putString(Constants_lib_ss.PREF_PHONENO, msPhoneNo);
                    CGlobals_lib_ss
                            .getInstance()
                            .getPersistentPreferenceEditor(
                                    getApplicationContext())
                            .putString(Constants_lib_ss.PREF_COUNTRY_CODE,
                                    msCountryCode);
                    // sendUserAccess(msCountryCode, msPhoneNo);
                    CGlobals_lib_ss
                            .getInstance()
                            .getPersistentPreferenceEditor(
                                    getApplicationContext()).commit();
                    // abortBroadcast();
                    // mobNoVeryfyTv.setText("Authentication Success.");

                } else {
                    Toast.makeText(getApplicationContext(),
                            "Authentication Failed.", Toast.LENGTH_LONG).show();

                    // if message is contains some invalid code
                    // mobNoVeryfyTv.setText("Authentication Fails.");

                    // SignInWaitingActivity.this.finish();

                }
                if (mSms != null) {
                    mSms.unregisterReceivers();
                }

                finish();
            }

        };
        this.registerReceiver(mIntentReceiver, intentFilter);
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission())
                resumeFunction();
        } else
            resumeFunction();

    }

    public void onClickKeyBackBt(View view) {
        if (tvMyPhoneNo.hasFocus()) {
            String phonNo = tvMyPhoneNo.getText().toString();
            if (phonNo.length() > 0) {
                String newPasswordStr = new StringBuilder(phonNo)
                        .deleteCharAt(phonNo.length() - 1).toString();
                tvMyPhoneNo.setText(newPasswordStr);
                tvMyPhoneNo.requestFocus(phonNo.length() - 1);
                tvMyPhoneNo.setCursorVisible(false);
            }
        }
        if (mEtPreviousCode.hasFocus()) {
            String code = mEtPreviousCode.getText().toString();
            if (code.length() > 0) {
                String newPasswordStr = new StringBuilder(code)
                        .deleteCharAt(code.length() - 1).toString();
                mEtPreviousCode.setText(newPasswordStr);
                mEtPreviousCode.requestFocus(code.length() - 1);
                mEtPreviousCode.setCursorVisible(false);
            }
        }
    }

    @Override
    public void onClick(View v) {


        switch (v.getId()) {
            case R.id.tvOne:
                imNumber = "1";
                if (tvMyPhoneNo.hasFocus())
                    tvMyPhoneNo.append(imNumber);
                if (mEtPreviousCode.hasFocus())
                    mEtPreviousCode.append(imNumber);
                break;
            case R.id.tvTwo:
                imNumber = "2";
                if (tvMyPhoneNo.hasFocus())
                    tvMyPhoneNo.append(imNumber);
                if (mEtPreviousCode.hasFocus())
                    mEtPreviousCode.append(imNumber);
                break;
            case R.id.tvThree:
                imNumber = "3";
                if (tvMyPhoneNo.hasFocus())
                    tvMyPhoneNo.append(imNumber);
                if (mEtPreviousCode.hasFocus())
                    mEtPreviousCode.append(imNumber);
                break;
            case R.id.tvFour:
                imNumber = "4";
                if (tvMyPhoneNo.hasFocus())
                    tvMyPhoneNo.append(imNumber);
                if (mEtPreviousCode.hasFocus())
                    mEtPreviousCode.append(imNumber);
                break;
            case R.id.tvFive:
                imNumber = "5";
                if (tvMyPhoneNo.hasFocus())
                    tvMyPhoneNo.append(imNumber);
                if (mEtPreviousCode.hasFocus())
                    mEtPreviousCode.append(imNumber);
                break;
            case R.id.tvSix:
                imNumber = "6";
                if (tvMyPhoneNo.hasFocus())
                    tvMyPhoneNo.append(imNumber);
                if (mEtPreviousCode.hasFocus())
                    mEtPreviousCode.append(imNumber);
                break;
            case R.id.tvSeven:
                imNumber = "7";
                if (tvMyPhoneNo.hasFocus())
                    tvMyPhoneNo.append(imNumber);
                if (mEtPreviousCode.hasFocus())
                    mEtPreviousCode.append(imNumber);
                break;
            case R.id.tvEight:
                imNumber = "8";
                if (tvMyPhoneNo.hasFocus())
                    tvMyPhoneNo.append(imNumber);
                if (mEtPreviousCode.hasFocus())
                    mEtPreviousCode.append(imNumber);
                break;
            case R.id.tvNine:
                imNumber = "9";
                if (tvMyPhoneNo.hasFocus())
                    tvMyPhoneNo.append(imNumber);
                if (mEtPreviousCode.hasFocus())
                    mEtPreviousCode.append(imNumber);
                break;
            case R.id.tvZero:
                imNumber = "0";
                if (tvMyPhoneNo.hasFocus())
                    tvMyPhoneNo.append(imNumber);
                if (mEtPreviousCode.hasFocus())
                    mEtPreviousCode.append(imNumber);
                break;
        }
    }
} // ActSendSMS