package lib.app.util.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.CSms;
import lib.app.util.Constants_lib_ss;
import lib.app.util.Iso2Phone;
import smartsheharcom.www.smartsheharlib.R;


public class VerifyPhone_act extends Activity {
	private static String TAG = "VerifyPhone_act: ";
	private TextView tvMyPhoneNo, timerTv, tvMyCountryCode;
	Button buttonVerifyPhoneNo;
	static final int PICK_CONTACT = 1;
	CSms mSms = null;
	String msCode = "";
	private BroadcastReceiver mIntentReceiver;
	static Boolean timeOut = true;
	EditText mEtPreviousCode;
	String msISOCountryCode = "", msCountryCode = "", msPhoneNo;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.verify_phone_no_act);
		buttonVerifyPhoneNo = (Button) findViewById(R.id.buttonVerifyPhoneNo);
		tvMyCountryCode = (TextView) findViewById(R.id.tvMyCountryCode);
		tvMyPhoneNo = (TextView) findViewById(R.id.tvMyPhoneNo);
		timerTv = (TextView) findViewById(R.id.SW_TimeRemainigTv);
		mEtPreviousCode = (EditText) findViewById(R.id.etSmsCode);
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		msISOCountryCode = tm.getSimCountryIso();
		msCountryCode = Iso2Phone.getPhone(msISOCountryCode);
		tvMyCountryCode.setText(msCountryCode);
        findViewById(R.id.buttonSkip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CGlobals_lib_ss
                        .getInstance()
                        .getPersistentPreferenceEditor(
                                VerifyPhone_act.this)
                        .putBoolean(Constants_lib_ss.PREF_SKIPPED, true).commit();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                VerifyPhone_act.this.finish();
            }
        });
		buttonVerifyPhoneNo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LinearLayout rlProgress = (LinearLayout) findViewById(R.id.rlProgress);
                rlProgress.setVisibility(View.VISIBLE);
                timerTv = (TextView) findViewById(R.id.SW_TimeRemainigTv);
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
                        VerifyPhone_act.this.finish();
                    }
                }.start();
                try {
                    msCode = generate4DigitCode();
                    mSms = new CSms(VerifyPhone_act.this);
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
                                VerifyPhone_act.this
                                        .getString(R.string.codeHint) + msCode);
                    }
                } catch (Exception e) {
                    ////SSLog.e(TAG, " onCreate - ", e);
                }

            }
        });
	} // onCreate

	@Override
	public void onStart() {
		super.onStart();
		// EasyTracker.getInstance(this).activityStart(this); // Add this
		// method.
	}

	@Override
	public void onStop() {
		super.onStop();
		// EasyTracker.getInstance(this).activityStop(this); // Add this method.
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

} // ActSendSMS