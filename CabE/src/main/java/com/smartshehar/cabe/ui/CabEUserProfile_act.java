package com.smartshehar.cabe.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.cabe.CTripCabE;
import com.smartshehar.cabe.Constants_CabE;
import com.smartshehar.cabe.R;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.CAddress;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;
import lib.app.util.SearchAddress_act;

/**
 * Created by jijo_soumen on 30/04/2016.
 * CabE create new passenger Profile
 */
public class CabEUserProfile_act extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "UserProfile_act: ";
    private CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;
    private EditText cleFirstName, cleLastName;
    TextView cleAge, tvHomeAddress, tvWorkAddress, tvPhpLinkId;
    private ImageView ivDateofBirth;
    private Spinner spGender;
    private Button btnSubmitProfile;
    String sFirstName, sLastName, sAge, sGender;
    private static final String[] selectGender = {"GENDER", "MALE", "FEMALE"}; // Spinner
    private int year, month, day;
    static final int DATE_DIALOG_ID = 3636;
    CTripCabE cTripCabE;
    Button sign_out_button;
    GoogleApiClient google_api_client;
    protected final int ACTRESULT_HOME_ADDRESS = 19;
    protected final int ACTRESULT_WORK_ADDRESS = 29;
    CAddress cAddressHome, cAddressWork;
    // CSms mSms = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buidNewGoogleApiClient();
        setContentView(R.layout.cabeuserprofile_act);
        init();
        btnSubmitProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* mSms = new CSms(CabEUserProfile_act.this);
                mSms.sendSMS("09820843471", "Cab-e Trip SMS");*/
                clickSubmitButton();
            }
        });
        ivDateofBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createdDialog(DATE_DIALOG_ID).show();
            }
        });
        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        ArrayAdapter<String> adaptergender = new ArrayAdapter<>(CabEUserProfile_act.this,
                android.R.layout.simple_spinner_item, selectGender);
        adaptergender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(adaptergender);
        boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEUserProfile_act.this);
        if (!isInternetCheck) {
            getUserProfile();
        } else {
            snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        //set Home and Work
        String sHomeAddress = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEUserProfile_act.this)
                .getString(Constants_CabE.PERF_HOME_ADDRESS, "");
        String sWorkAddress = CGlobals_lib_ss.getInstance().getPersistentPreference(CabEUserProfile_act.this)
                .getString(Constants_CabE.PERF_WORK_ADDRESS, "");
        if (!TextUtils.isEmpty(sHomeAddress)) {
            Type typehome = new TypeToken<CAddress>() {
            }.getType();
            cAddressHome = new Gson().fromJson(sHomeAddress, typehome);
            tvHomeAddress.setText(cAddressHome.getAddress());
        } else {
            tvHomeAddress.setText(getString(R.string.home_address));
        }
        if (!TextUtils.isEmpty(sWorkAddress)) {
            Type typeWork = new TypeToken<CAddress>() {
            }.getType();
            cAddressWork = new Gson().fromJson(sWorkAddress, typeWork);
            tvWorkAddress.setText(cAddressWork.getAddress());
        } else {
            tvWorkAddress.setText(getString(R.string.work_address));
        }
        tvHomeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CabEUserProfile_act.this,
                        SearchAddress_act.class);
                startActivityForResult(i, ACTRESULT_HOME_ADDRESS);
            }
        });

        tvWorkAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CabEUserProfile_act.this,
                        SearchAddress_act.class);
                startActivityForResult(i, ACTRESULT_WORK_ADDRESS);
            }
        });
    }

    private void init() {
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        cleFirstName = (EditText) findViewById(R.id.cleFirstName);
        cleLastName = (EditText) findViewById(R.id.cleLastName);
        cleAge = (TextView) findViewById(R.id.cleAge);
        ivDateofBirth = (ImageView) findViewById(R.id.ivDateofBirth);
        spGender = (Spinner) findViewById(R.id.spGender);
        btnSubmitProfile = (Button) findViewById(R.id.btnSubmitProfile);
        sign_out_button = (Button) findViewById(R.id.sign_out_button);
        tvHomeAddress = (TextView) findViewById(R.id.tvHomeAddress);
        tvWorkAddress = (TextView) findViewById(R.id.tvWorkAddress);
        tvPhpLinkId = (TextView) findViewById(R.id.tvPhpLinkId);
        if (tvPhpLinkId != null) {
            tvPhpLinkId.setText(Constants_CabE.ALPHA);
        }
    }

    private void clickSubmitButton() {
        sFirstName = cleFirstName.getText().toString();
        sLastName = cleLastName.getText().toString();
        sAge = cleAge.getText().toString();
        sGender = spGender.getSelectedItem().toString();
        if (TextUtils.isEmpty(sFirstName.trim())) {
            cleFirstName.setBackgroundResource(R.drawable.thin_frame_red);
            Animation shake = AnimationUtils.loadAnimation(CabEUserProfile_act.this, R.anim.shake);
            cleFirstName.startAnimation(shake);
        } else {
            cleFirstName.setBackgroundResource(R.drawable.no_frame);
        }
        if (TextUtils.isEmpty(sLastName.trim())) {
            cleLastName.setBackgroundResource(R.drawable.thin_frame_red);
            Animation shake = AnimationUtils.loadAnimation(CabEUserProfile_act.this, R.anim.shake);
            cleLastName.startAnimation(shake);
        } else {
            cleLastName.setBackgroundResource(R.drawable.no_frame);
        }
        if (TextUtils.isEmpty(sAge.trim())) {
            cleAge.setBackgroundResource(R.drawable.thin_frame_red);
            Animation shake = AnimationUtils.loadAnimation(CabEUserProfile_act.this, R.anim.shake);
            cleAge.startAnimation(shake);
        } else {
            cleAge.setBackgroundResource(R.drawable.no_frame);
        }
        if (sFirstName.trim().equals("") || sLastName.trim().equals("") ||
                sAge.trim().equals("") || sGender.trim().equals("GENDER")) {
            snackbar = Snackbar
                    .make(coordinatorLayout, "All fields are mandatory", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
            snackbar.show();
        } else {
            boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabEUserProfile_act.this);
            if (!isInternetCheck) {
                sendUserProfile(sFirstName, sLastName, sGender, sAge, "");
            } else {
                snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
    }

    private void sendUserProfile(final String firstname, final String lastname,
                                 final String gender, final String age, final String userprofileimagefilename) {
        final String url = Constants_CabE.ADD_CABE_USER_DATA_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (TextUtils.isEmpty(response) || response.trim().equals("-1")) {
                            return;
                        }
                        Toast.makeText(CabEUserProfile_act.this, "Successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "getCabShow :-   ", error, CabEUserProfile_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<>();
                if (!TextUtils.isEmpty(firstname)) {
                    params.put("fn", firstname);
                }
                if (!TextUtils.isEmpty(lastname)) {
                    params.put("ln", lastname);
                }
                if (!TextUtils.isEmpty(gender)) {
                    params.put("gen", gender);
                }
                if (!TextUtils.isEmpty(userprofileimagefilename)) {
                    params.put("upi", userprofileimagefilename);
                }
                if (!TextUtils.isEmpty(age)) {
                    params.put("dob", age);
                }
                if (!TextUtils.isEmpty(cAddressHome.getAddress())) {
                    params.put("home", cAddressHome.getAddress());
                }
                if (!TextUtils.isEmpty(cAddressWork.getAddress())) {
                    params.put("work", cAddressWork.getAddress());
                }
                if (cAddressHome.getLatitude()!=0.0) {
                    params.put("homelat", String.valueOf(cAddressHome.getLatitude()));
                }
                if (cAddressHome.getLongitude()!=0.0) {
                    params.put("homelng", String.valueOf(cAddressHome.getLongitude()));
                }
                if (cAddressWork.getLatitude()!=0.0) {
                    params.put("worklat", String.valueOf(cAddressWork.getLatitude()));
                }
                if (cAddressWork.getLongitude() != 0.0) {
                    params.put("worklng", String.valueOf(cAddressWork.getLongitude()));
                }
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEUserProfile_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                try {
                    String url1 = url + "?verbose=Y&" + getParams.toString();
                    Log.i(TAG, "url  " + url1);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getFoundDriver", e, CabEUserProfile_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabEUserProfile_act.this);
    }

    protected Dialog createdDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                // set date picker as current date
                return new DatePickerDialog(this, datePickerListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        // when dialog box is closed, below method will be called.
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;
            // set selected date into Text View
            cleAge.setText(new StringBuilder().append(year).append("-").append(month + 1).append("-").append(day).append(""));
        }
    };

    private void getUserProfile() {
        final String url = Constants_CabE.GET_USER_PROFILE_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getResponseUserProfile(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, error.getMessage());
                } else {
                    SSLog_SS.e(TAG, "getCabShow :-   ", error, CabEUserProfile_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<>();
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabEUserProfile_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                try {
                    String url1 = url + "?verbose=Y&" + getParams.toString();
                    Log.i(TAG, "url  " + url1);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getFoundDriver", e, CabEUserProfile_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabEUserProfile_act.this);
    }

    private void getResponseUserProfile(String response) {
        if (TextUtils.isEmpty(response) || response.trim().equals("-1")) {
            return;
        }
        cTripCabE = new CTripCabE(response, CabEUserProfile_act.this);
        if (TextUtils.isEmpty(cTripCabE.getFirstName())) {
            cleFirstName.setText("");
        } else {
            cleFirstName.setText(cTripCabE.getFirstName());
        }
        if (TextUtils.isEmpty(cTripCabE.getLastName())) {
            cleLastName.setText("");
        } else {
            cleLastName.setText(cTripCabE.getLastName());
        }
        if (TextUtils.isEmpty(cTripCabE.getDOB())) {
            cleAge.setText("");
        } else {
            cleAge.setText(cTripCabE.getDOB());
        }

        if (TextUtils.isEmpty(cTripCabE.getGender())) {
            spGender.setSelection(0);
        } else {
            switch (cTripCabE.getGender()) {
                case "Male":
                    spGender.setSelection(1);
                    break;
                case "MALE":
                    spGender.setSelection(1);
                    break;
                case "Female":
                    spGender.setSelection(2);
                    break;
                case "FEMALE":
                    spGender.setSelection(2);
                    break;
            }
        }
    }

    public void onClickGmailLogout(View view) {
        gPlusSignOut();
    }

    private void gPlusSignOut() {
        if (google_api_client.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(google_api_client);
            google_api_client.disconnect();
            google_api_client.connect();
            changeUI();
        }
    }

    private void changeUI() {
        sign_out_button = (Button) findViewById(R.id.sign_out_button);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEUserProfile_act.this)
                .putBoolean(Constants_CabE.PREF_ISLOGIN_MY_APP, false);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEUserProfile_act.this).commit();
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEUserProfile_act.this)
                .putBoolean(Constants_CabE.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, false);
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEUserProfile_act.this).commit();
        Intent intent = new Intent(CabEUserProfile_act.this, CabERegistration_act.class);
        startActivity(intent);
        finish();
    }

    private void buidNewGoogleApiClient() {
        google_api_client = new GoogleApiClient.Builder(CabEUserProfile_act.this)
                .addConnectionCallbacks(CabEUserProfile_act.this)
                .addOnConnectionFailedListener(CabEUserProfile_act.this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
        google_api_client.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (google_api_client.isConnected()) {
            google_api_client.connect();
        }
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (google_api_client.isConnected()) {
            google_api_client.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        google_api_client.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String sAddr;
        if (requestCode == ACTRESULT_HOME_ADDRESS) {
            if (resultCode == RESULT_OK) {
                sAddr = data.getStringExtra("add");
                Type type = new TypeToken<CAddress>() {
                }.getType();
                cAddressHome = new Gson().fromJson(sAddr, type);
                tvHomeAddress.setText(cAddressHome.getAddress());
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEUserProfile_act.this)
                        .putString(Constants_CabE.PERF_HOME_ADDRESS, sAddr);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEUserProfile_act.this).commit();
            }
        }
        if (requestCode == ACTRESULT_WORK_ADDRESS) {
            if (resultCode == RESULT_OK) {
                sAddr = data.getStringExtra("add");
                Type type = new TypeToken<CAddress>() {
                }.getType();
                cAddressWork = new Gson().fromJson(sAddr, type);
                tvWorkAddress.setText(cAddressWork.getAddress());
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEUserProfile_act.this)
                        .putString(Constants_CabE.PERF_WORK_ADDRESS, sAddr);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabEUserProfile_act.this).commit();
            }
        }
    }
}
