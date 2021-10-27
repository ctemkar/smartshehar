package com.smartshehar.cabe.driver.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.smartshehar.cabe.driver.Constants_CED;
import com.smartshehar.cabe.driver.MyApplication_CED;
import com.smartshehar.cabe.driver.R;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.SSLog_SS;


/**
 * Created by jijo_soumen on 09/03/2016.
 * user login Facebook or Gmail login
 * and send login details to server
 */
public class CabERegistration_act extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks {

    private static String TAG = "CabERegistration_act: ";
    TextView tvCabEUserFirstName, tvCabEUserLastName, tvCabEEmailId, tvCabENext1;
    private CallbackManager mCallbackManager;
    private AccessTokenTracker tokenTracker;
    private ProfileTracker profileTracker;
    String get_gender, get_email, get_birthday, email, sDOB, sGender, get_Profile_Pic,
            personPhotoUrl;
    Toolbar toolbar;
    Connectivity mConnectivity;
    GoogleApiClient google_api_client;
    SignInButton signIn_btn;
    private static final int SIGN_IN_CODE = 0;
    private ConnectionResult connection_result;
    private boolean is_intent_inprogress, is_signInBtn_clicked;
    int request_code, sAge, years, months, days;
    ProgressDialog progress_dialog;
    private boolean isLoginMyApp;
    private CoordinatorLayout coordinatorLayout;
    Snackbar snackbar;
    Button sign_out_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buidNewGoogleApiClient();
        setContentView(R.layout.caberegistration_act);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        mConnectivity = new Connectivity();
        if (!mConnectivity.connectionError(CabERegistration_act.this, getString(R.string.app_label))) {
            Log.d(TAG, "No Internet Connection");
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
        }
        if (loginButton != null) {
            loginButton.setReadPermissions(Collections.singletonList("public_profile, email, user_birthday, user_friends"));
        }
        if (loginButton != null) {
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
            actionBar.setTitle("Cab-e Manager");
        }
        tvCabENext1 = (TextView) findViewById(R.id.tvCabENext1);
        tvCabENext1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLoginMyApp = CGlobals_lib_ss.getInstance().getPersistentPreference(CabERegistration_act.this)
                        .getBoolean(Constants_CED.PREF_ISLOGIN_MY_APP, false);
                if (isLoginMyApp) {
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                            .putBoolean(Constants_CED.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, true);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
                    Intent intent = new Intent(CabERegistration_act.this, DashBoard_Driver_act.class);
                    startActivity(intent);
                    finish();
                } else {
                    snackbar = Snackbar
                            .make(coordinatorLayout, "Sign Up Google+", Snackbar.LENGTH_INDEFINITE)
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
                .getBoolean(Constants_CED.PREF_ISLOGIN_MY_APP, false);
        if (isLoginMyApp) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putBoolean(Constants_CED.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, true);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
            Intent intent = new Intent(CabERegistration_act.this, DashBoard_Driver_act.class);
            startActivity(intent);
            finish();
        } else {
            snackbar = Snackbar
                    .make(coordinatorLayout, "Sign Up Google+", Snackbar.LENGTH_INDEFINITE)
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
    @SuppressLint("SimpleDateFormat")
    private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {

                            try {
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                                        .putBoolean(Constants_CED.PREF_ISLOGIN_MY_APP, true);
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                                        .putBoolean(Constants_CED.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, true);
                                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
                                Intent intent = new Intent(CabERegistration_act.this, DashBoard_Driver_act.class);
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
                                MyApplication_CED.getInstance().getPersistentPreferenceEditor()
                                        .putString("GENDER_FACEBOOK", get_gender);
                                MyApplication_CED.getInstance().getPersistentPreferenceEditor()
                                        .putString("EMAIL_FACEBOOK", get_email);
                                MyApplication_CED.getInstance().getPersistentPreferenceEditor().commit();
                                Profile profile = Profile.getCurrentProfile();
                                get_Profile_Pic = String.valueOf(profile.getProfilePictureUri(120, 120));
                                tvCabEUserFirstName.setText(profile.getFirstName());
                                tvCabEUserLastName.setText(profile.getLastName());
                                tvCabEEmailId.setText(get_email);
                                SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd");
                                Date birthDate;
                                try {
                                    birthDate = curFormaterDB.parse(get_birthday);
                                    sAge = calculateAge(birthDate);
                                    MyApplication_CED.getInstance().getPersistentPreferenceEditor().
                                            putInt("BIRTHDAY_FACEBOOK", sAge);
                                    MyApplication_CED.getInstance().getPersistentPreferenceEditor().commit();
                                } catch (Exception e) {
                                    e.printStackTrace();
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
                    .putBoolean(Constants_CED.PREF_ISLOGIN_MY_APP, false);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
        }

        @Override
        public void onError(FacebookException e) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putBoolean(Constants_CED.PREF_ISLOGIN_MY_APP, false);
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
        if (requestCode == CGlobals_lib_ss.REQUEST_LOCATION_LIB) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    CGlobals_lib_ss.showGPSDialog = false;
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    @SuppressLint("SimpleDateFormat")
    public void onResume() {
        super.onResume();
        try {
            get_gender = MyApplication_CED.getInstance().getPersistentPreference().getString("GENDER_FACEBOOK", "");
            get_email = MyApplication_CED.getInstance().getPersistentPreference().getString("EMAIL_FACEBOOK", "");
            Profile profile = Profile.getCurrentProfile();
            get_Profile_Pic = String.valueOf(profile.getProfilePictureUri(120, 120));
            tvCabEUserFirstName.setText(profile.getFirstName());
            tvCabEUserLastName.setText(profile.getLastName());
            tvCabEEmailId.setText(get_email);
            SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd");
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
            days = now.get(Calendar.DATE) - birthDay.get(Calendar.DATE);
        else if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
            int today = now.get(Calendar.DAY_OF_MONTH);
            now.add(Calendar.MONTH, -1);
            days = now.getActualMaximum(Calendar.DAY_OF_MONTH) - birthDay.get(Calendar.DAY_OF_MONTH) + today;
        } else {
            days = 0;
            if (months == 12) {
                years++;
                months = 0;
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
        sign_out_button = (Button) findViewById(R.id.sign_out_button);
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
            Log.d(TAG, "onConnectionFailed");
 /*           GoogleApiAvailability google_api_availability = null;
            if (google_api_availability != null) {
                google_api_availability.getErrorDialog(this, result.getErrorCode(), request_code).show();
            }
            return;
 */
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
            } else {
                Log.d(TAG, "getProfileInfo");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("SimpleDateFormat")
    private void setPersonalInfo(Person currentPerson) {
        sDOB = currentPerson.getBirthday();
        //String sTagLine = currentPerson.getTagline();
        //String sAboutMe = currentPerson.getAboutMe();
        sGender = String.valueOf(currentPerson.getGender());
        // String sName = String.valueOf(currentPerson.getName().getGivenName());
        SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd");
        Date birthDate;
        try {
            birthDate = curFormaterDB.parse(sDOB);
            sAge = calculateAge(birthDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //String personName = currentPerson.getDisplayName();
        personPhotoUrl = currentPerson.getImage().getUrl();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        email = Plus.AccountApi.getAccountName(google_api_client);
        tvCabEUserFirstName.setText(currentPerson.getName().getGivenName());
        tvCabEUserLastName.setText(currentPerson.getName().getFamilyName());
        tvCabEEmailId.setText(email);
        progress_dialog.dismiss();
    }

    private void changeUI(boolean signedIn) {
        signIn_btn = (SignInButton) findViewById(R.id.sign_in_button);
        sign_out_button = (Button) findViewById(R.id.sign_out_button);
        if (signedIn) {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putBoolean(Constants_CED.PREF_ISLOGIN_MY_APP, true);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putBoolean(Constants_CED.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, true);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
            Intent intent = new Intent(CabERegistration_act.this, DashBoard_Driver_act.class);
            startActivity(intent);
            finish();
            signIn_btn.setVisibility(View.GONE);
            sign_out_button.setVisibility(View.VISIBLE);
        } else {
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putBoolean(Constants_CED.PREF_ISLOGIN_MY_APP, false);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this)
                    .putBoolean(Constants_CED.PREF_CABE_GOOGLE_FACEBOOK_LOGIN_DONE, false);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CabERegistration_act.this).commit();
            signIn_btn.setVisibility(View.VISIBLE);
            sign_out_button.setVisibility(View.GONE);
        }
    }
}
