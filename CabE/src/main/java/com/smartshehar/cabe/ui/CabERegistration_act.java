package com.smartshehar.cabe.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.smartshehar.cabe.Constants_CabE;
import com.smartshehar.cabe.MyApplication_CabE;
import com.smartshehar.cabe.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.SSLog_SS;


/**
 * Created by jijo_soumen on 09/03/2016.
 * Sign in with social apps Facebook, Google plus for register passenger
 */
public class CabERegistration_act extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks {

    private static String TAG = "CabERegistration_act: ";
    TextView tvCabEUserFirstName, tvCabEUserLastName, tvCabEEmailId;
    private CallbackManager mCallbackManager;
    Profile profile;
    private AccessTokenTracker tokenTracker;
    private ProfileTracker profileTracker;
    String get_gender, get_email, get_birthday, get_Profile_Pic;
    Toolbar toolbar;
    TextView tvCabENext1;
    Connectivity mConnectivity;
    GoogleApiClient google_api_client;
    GoogleApiAvailability google_api_availability;
    SignInButton signIn_btn;
    private static final int SIGN_IN_CODE = 0;
    private ConnectionResult connection_result;
    private boolean is_intent_inprogress;
    private boolean is_signInBtn_clicked;
    private int request_code;
    ProgressDialog progress_dialog;
    String personName, personPhotoUrl, email, sDOB, sTagLine, sAboutMe, msGender = "", sName, sGender;
    int sAge;
    private boolean isLoginMyApp;
    private CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buidNewGoogleApiClient();
        setContentView(R.layout.caberegistration_act);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        mConnectivity = new Connectivity();
        if (!mConnectivity.connectionError(CabERegistration_act.this, getString(R.string.app_label))) {
            Log.d(TAG, "Connection Error");
        }
        initToolBar();
        tvCabEUserFirstName = (TextView) findViewById(R.id.tvCabEUserFirstName);
        tvCabEUserLastName = (TextView) findViewById(R.id.tvCabEUserLastName);
        tvCabEEmailId = (TextView) findViewById(R.id.tvCabEEmailId);
        mCallbackManager = CallbackManager.Factory.create();
        tokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken1) {

            }
        };
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile profile, Profile profile1) {

            }
        };

        tokenTracker.startTracking();
        profileTracker.startTracking();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        if (loginButton != null) {
            loginButton.setCompoundDrawables(null, null, null, null);
            loginButton.setReadPermissions(Collections.singletonList("public_profile, email, user_birthday, user_friends"));
            loginButton.registerCallback(mCallbackManager, mFacebookCallback);
        }
        custimizeSignBtn();
        setBtnClickListeners();
        progress_dialog = new ProgressDialog(this);
        progress_dialog.setMessage("Signing in....");
    }

    // AppCompat ToolBar
    public void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        CabERegistration_act.this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Cab-e Sign Up");
        }
        tvCabENext1 = (TextView) findViewById(R.id.tvCabENext1);
        tvCabENext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoginMyApp = CGlobals_lib_ss.getInstance().getPersistentPreference(CabERegistration_act.this)
                        .getBoolean(Constants_CabE.PREF_ISLOGIN_MY_APP, false);
                if (isLoginMyApp) {
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                            .putBoolean(Constants_CabE.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, true);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
                    Intent intent = new Intent(CabERegistration_act.this, CabEMain_act.class);
                    startActivity(intent);
                    finish();
                } else {
                    snackbar = Snackbar
                            .make(coordinatorLayout, "Sign Up Facebook or Google+", Snackbar.LENGTH_INDEFINITE)
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

    public void onClickNext21(View view) {
        isLoginMyApp = CGlobals_lib_ss.getInstance().getPersistentPreference(CabERegistration_act.this)
                .getBoolean(Constants_CabE.PREF_ISLOGIN_MY_APP, false);
        if (isLoginMyApp) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putBoolean(Constants_CabE.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, true);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
            Intent intent = new Intent(CabERegistration_act.this, CabEMain_act.class);
            startActivity(intent);
            finish();
        } else {
            snackbar = Snackbar
                    .make(coordinatorLayout, "Sign Up Facebook or Google+", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Cancel", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });
            snackbar.show();
        }
    }

// FaceBook Login Code

    private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            profile = Profile.getCurrentProfile();
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {

                            try {
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                                        .putBoolean(Constants_CabE.PREF_ISLOGIN_MY_APP, true);
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                                        .putBoolean(Constants_CabE.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, true);
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
                                Intent intent = new Intent(CabERegistration_act.this, CabEMain_act.class);
                                startActivity(intent);
                                finish();
                                try {
                                    get_gender = object.getString("gender");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    get_email = object.getString("email");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                try {
                                    get_birthday = object.getString("birthday");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                MyApplication_CabE.getInstance().getPersistentPreferenceEditor()
                                        .putString("GENDER_FACEBOOK", get_gender);
                                MyApplication_CabE.getInstance().getPersistentPreferenceEditor()
                                        .putString("EMAIL_FACEBOOK", get_email);
                                MyApplication_CabE.getInstance().getPersistentPreferenceEditor().commit();
                                Profile profile = Profile.getCurrentProfile();
                                try {
                                    get_Profile_Pic = String.valueOf(profile.getProfilePictureUri(120, 120));
                                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                                            .putString(Constants_CabE.GMAIL_FACEBOOK_IMAGE_URL, get_Profile_Pic);
                                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                tvCabEUserFirstName.setText(profile.getFirstName());
                                tvCabEUserLastName.setText(profile.getLastName());
                                tvCabEEmailId.setText(get_email);
                                SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                Date birthDate;
                                try {
                                    birthDate = curFormaterDB.parse(get_birthday);
                                    sAge = calculateAge(birthDate);
                                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).
                                            putInt("BIRTHDAY_FACEBOOK", sAge);
                                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabERegistration_act.this);
                                if (!isInternetCheck) {
                                    addUserData("F", get_email, profile.getFirstName(), profile.getLastName(),
                                            get_gender, get_birthday, get_Profile_Pic);
                                } else {
                                    snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_SHORT);
                                    snackbar.show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.v("LoginActivity", response.toString());
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender,birthday");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putBoolean(Constants_CabE.PREF_ISLOGIN_MY_APP, false);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
        }

        @Override
        public void onError(FacebookException e) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putBoolean(Constants_CabE.PREF_ISLOGIN_MY_APP, false);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_CODE) {
            request_code = requestCode;
            if (resultCode != RESULT_OK) {
                is_signInBtn_clicked = false;
                progress_dialog.dismiss();

            }

            is_intent_inprogress = false;

            if (!google_api_client.isConnecting()) {
                google_api_client.connect();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            get_gender = MyApplication_CabE.getInstance().getPersistentPreference().getString("GENDER_FACEBOOK", "");
            get_email = MyApplication_CabE.getInstance().getPersistentPreference().getString("EMAIL_FACEBOOK", "");
            Profile profile = Profile.getCurrentProfile();
            get_Profile_Pic = String.valueOf(profile.getProfilePictureUri(120, 120));
            tvCabEUserFirstName.setText(profile.getFirstName());
            tvCabEUserLastName.setText(profile.getLastName());
            tvCabEEmailId.setText(get_email);
            SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date birthDate;
            try {
                birthDate = curFormaterDB.parse(get_birthday);
                sAge = calculateAge(birthDate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    protected void onPause() {
        super.onPause();
        if ((progress_dialog != null) && progress_dialog.isShowing())
            progress_dialog.dismiss();
    }

    @Override
    public void onStop() {
        super.onStop();
        profileTracker.stopTracking();
        tokenTracker.stopTracking();
        if (google_api_client.isConnected()) {
            google_api_client.disconnect();
        }
    }

    private int calculateAge(Date birthDate) {
        int years;
        int months;
        //create calendar object for birth day
        Calendar birthDay = Calendar.getInstance();
        birthDay.setTimeInMillis(birthDate.getTime());
        //create calendar object for current day
        long currentTime = System.currentTimeMillis();
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(currentTime);
        //Get difference between years
        years = now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
        int currMonth = now.get(Calendar.MONTH) + 1;
        int birthMonth = birthDay.get(Calendar.MONTH) + 1;
        //Get difference between months
        months = currMonth - birthMonth;
        //if month difference is in negative then reduce years by one and calculate the number of months.
        if (months < 0) {
            years--;
            months = 12 - birthMonth + currMonth;
            if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE))
                months--;
        } else if (months == 0 && now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
            years--;
            months = 11;
        }
        //Calculate the days
        if (now.get(Calendar.DATE) > birthDay.get(Calendar.DATE))
            Log.d(TAG, "Calculate the days");
        else if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
            now.add(Calendar.MONTH, -1);
        } else {
            if (months == 12) {
                years++;
            }
        }
        return years;
    }

    // Google Plus Login Code

    private void buidNewGoogleApiClient() {

        google_api_client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    private void custimizeSignBtn() {
        signIn_btn = (SignInButton) findViewById(R.id.sign_in_button);
        if (signIn_btn != null) {
            signIn_btn.setSize(SignInButton.SIZE_STANDARD);
        }
        if (signIn_btn != null) {
            signIn_btn.setScopes(new Scope[]{Plus.SCOPE_PLUS_LOGIN});
        }
    }

    private void setBtnClickListeners() {
        // Button listeners
        signIn_btn.setOnClickListener(this);
        Button sign_out_button = (Button) findViewById(R.id.sign_out_button);
        if (sign_out_button != null) {
            sign_out_button.setOnClickListener(this);
        }
    }

    protected void onStart() {
        super.onStart();
        google_api_client.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (!result.hasResolution()) {
            google_api_availability.getErrorDialog(this, result.getErrorCode(), request_code).show();
            return;
        }
        if (!is_intent_inprogress) {
            connection_result = result;
            if (is_signInBtn_clicked) {
                resolveSignInError();
            }
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        is_signInBtn_clicked = false;
        getProfileInfo();
        changeUI(true);
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        google_api_client.connect();
        changeUI(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                gPlusSignIn();
                break;
            case R.id.sign_out_button:
                gPlusSignOut();
                break;
        }
    }

    private void gPlusSignIn() {
        if (!google_api_client.isConnecting()) {
            Log.d("user connected", "connected");
            is_signInBtn_clicked = true;
            progress_dialog.show();
            resolveSignInError();
        }
    }

    private void resolveSignInError() {
        try {
            if (connection_result.hasResolution()) {
                try {
                    is_intent_inprogress = true;
                    connection_result.startResolutionForResult(this, SIGN_IN_CODE);
                    Log.d("resolve error", "sign in error resolved");
                } catch (IntentSender.SendIntentException e) {
                    is_intent_inprogress = false;
                    google_api_client.connect();
                }
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "resolveSignInError: ", e, CabERegistration_act.this);
        }
    }

    private void gPlusSignOut() {
        if (google_api_client.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(google_api_client);
            google_api_client.disconnect();
            google_api_client.connect();
            changeUI(false);
        }
    }

    private void getProfileInfo() {

        try {

            if (Plus.PeopleApi.getCurrentPerson(google_api_client) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(google_api_client);
                setPersonalInfo(currentPerson);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setPersonalInfo(Person currentPerson) {
        sDOB = currentPerson.getBirthday();
        sTagLine = currentPerson.getTagline();
        sAboutMe = currentPerson.getAboutMe();
        sGender = String.valueOf(currentPerson.getGender());
        sName = String.valueOf(currentPerson.getName().getGivenName());
        switch (sGender) {
            case "0":
                msGender = "MALE";
                break;
            case "1":
                msGender = "FEMALE";
                break;
            default:
                msGender = "";
                break;
        }
        SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date birthDate;
        try {
            birthDate = curFormaterDB.parse(sDOB);
            sAge = calculateAge(birthDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        personName = currentPerson.getDisplayName();
        try {
            personPhotoUrl = currentPerson.getImage().getUrl();
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putString(Constants_CabE.GMAIL_FACEBOOK_IMAGE_URL, personPhotoUrl);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        email = Plus.AccountApi.getAccountName(google_api_client);
        tvCabEUserFirstName.setText(currentPerson.getName().getGivenName());
        tvCabEUserLastName.setText(currentPerson.getName().getFamilyName());
        tvCabEEmailId.setText(email);
        boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(CabERegistration_act.this);
        if (!isInternetCheck) {
            addUserData("G", "", currentPerson.getName().getGivenName(), currentPerson.getName().getFamilyName(),
                    msGender, sDOB, personPhotoUrl);
        } else {
            snackbar = Snackbar.make(coordinatorLayout, getString(R.string.retry_internet), Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        progress_dialog.dismiss();
    }

    private void changeUI(boolean signedIn) {
        if (signedIn) {
            progress_dialog.show();
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putBoolean(Constants_CabE.PREF_ISLOGIN_MY_APP, true);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putBoolean(Constants_CabE.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, true);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
            Intent intent = new Intent(CabERegistration_act.this, CabEMain_act.class);
//            progress_dialog.dismiss();
            startActivity(intent);
            finish();
            signIn_btn = (SignInButton) findViewById(R.id.sign_in_button);
            if (signIn_btn != null) {
                signIn_btn.setVisibility(View.GONE);
            }
            Button sign_out_button = (Button) findViewById(R.id.sign_out_button);
            if (sign_out_button != null) {
                sign_out_button.setVisibility(View.VISIBLE);
            }
        } else {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putBoolean(Constants_CabE.PREF_ISLOGIN_MY_APP, false);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putBoolean(Constants_CabE.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, false);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
            signIn_btn = (SignInButton) findViewById(R.id.sign_in_button);
            if (signIn_btn != null) {
                signIn_btn.setVisibility(View.VISIBLE);
            }
            Button sign_out_button = (Button) findViewById(R.id.sign_out_button);
            if (sign_out_button != null) {
                sign_out_button.setVisibility(View.GONE);
            }
        }
    }

    private void addUserData(final String logintype, final String facebookemail, final String firstname,
                             final String lastname, final String gender, final String dateOfBirth, final String userprofileimagefilename) {
       final String url = Constants_CabE.ADD_USER_DATA_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "getCabShow :-   ", error, CabERegistration_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<>();
                params.put("lt", logintype);
                params.put("fbe", facebookemail);
                params.put("fn", firstname);
                params.put("ln", lastname);
                params.put("gen", gender);
                params.put("upi", userprofileimagefilename);
                params.put("dob", dateOfBirth);
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        url, CabERegistration_act.this);
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
                    SSLog_SS.e(TAG, "getFoundDriver", e, CabERegistration_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, CabERegistration_act.this);
    }

}
