package com.jumpinjumpout.apk.driver.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.jumpinjumpout.apk.driver.CGlobals_driver;
import com.jumpinjumpout.apk.driver.Constants_driver;
import com.jumpinjumpout.apk.driver.MyApplication;
import com.jumpinjumpout.apk.driver.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.SSLog;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.Connectivity;

public class MasterLogin_Act extends AppCompatActivity implements OnConnectionFailedListener, View.OnClickListener, ConnectionCallbacks {

    private static String TAG = "MasterLogin_Act: ";
    GoogleApiClient google_api_client;
    GoogleApiAvailability google_api_availability;
    SignInButton signIn_btn;
    private static final int SIGN_IN_CODE = 0;
    private static final int PROFILE_PIC_SIZE = 120;
    private ConnectionResult connection_result;
    private boolean is_intent_inprogress;
    private boolean is_signInBtn_clicked;
    private int request_code;
    ProgressDialog progress_dialog;
    String strImag;
    String personName, personPhotoUrl, email, sDOB, sTagLine, sAboutMe, msGender = "", sName, sGender;
    int sAge;
    Connectivity mConnectivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buidNewGoogleApiClient();
        setContentView(R.layout.masterlogin_act);
        mConnectivity = new Connectivity();
        //Customize sign-in button.a red button may be displayed when Google+ scopes are requested
        if (!mConnectivity.connectionError(MasterLogin_Act.this, getString(R.string.app_label))) {
        }
        custimizeSignBtn();
        setBtnClickListeners();
        progress_dialog = new ProgressDialog(this);
        progress_dialog.setMessage("Signing in....");

    }

    /*
    create and  initialize GoogleApiClient object to use Google Plus Api.
    While initializing the GoogleApiClient object, request the Plus.SCOPE_PLUS_LOGIN scope.
    */

    private void buidNewGoogleApiClient() {

        google_api_client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    /*
      Customize sign-in button. The sign-in button can be displayed in
      multiple sizes and color schemes. It can also be contextually
      rendered based on the requested scopes. For example. a red button may
      be displayed when Google+ scopes are requested, but a white button
      may be displayed when only basic profile is requested. Try adding the
      Plus.SCOPE_PLUS_LOGIN scope to see the  difference.
    */

    private void custimizeSignBtn() {

        signIn_btn = (SignInButton) findViewById(R.id.sign_in_button);
        signIn_btn.setSize(SignInButton.SIZE_STANDARD);
        signIn_btn.setScopes(new Scope[]{Plus.SCOPE_PLUS_LOGIN});

    }

    /*
      Set on click Listeners on the sign-in sign-out and disconnect buttons
     */

    private void setBtnClickListeners() {
        // Button listeners
        signIn_btn.setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);
    }

    protected void onStart() {
        super.onStart();
        google_api_client.connect();
    }

    protected void onStop() {
        super.onStop();
        if (google_api_client.isConnected()) {
            google_api_client.disconnect();
        }
    }

    protected void onResume() {
        super.onResume();
        if (google_api_client.isConnected()) {
            google_api_client.connect();
            //CGlobals_lib.getInstance().turnGPSOn1(MasterLogin_Act.this, google_api_client);
        }
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
    public void onConnectionFailed(ConnectionResult result) {
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

    /*
      Will receive the activity result and check which request we are responding to

     */
    @Override
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        // Check which request we're responding to
        if (requestCode == SIGN_IN_CODE) {
            request_code = requestCode;
            if (responseCode != RESULT_OK) {
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
            case R.id.disconnect_button:
                gPlusRevokeAccess();

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

    /*
      Method to resolve any signin errors
     */

    private void resolveSignInError() {
        try {
            if (connection_result.hasResolution()) {
                try {
                    is_intent_inprogress = true;
                    connection_result.startResolutionForResult(this, SIGN_IN_CODE);
                    Log.d("resolve error", "sign in error resolved");
                } catch (SendIntentException e) {
                    is_intent_inprogress = false;
                    google_api_client.connect();
                }
            }
        } catch (Exception e) {
            SSLog.e(TAG, "resolveSignInError: ", e);
        }
    }

    /*
      Sign-out from Google+ account
     */

    private void gPlusSignOut() {
        if (google_api_client.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(google_api_client);
            google_api_client.disconnect();
            google_api_client.connect();
            changeUI(false);
        }
    }

    /*
     Revoking access from Google+ account
     */

    private void gPlusRevokeAccess() {
        if (google_api_client.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(google_api_client);
            Plus.AccountApi.revokeAccessAndDisconnect(google_api_client)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status arg0) {
                            Log.d("CameraActivity", "User access revoked!");
                            buidNewGoogleApiClient();
                            google_api_client.connect();
                            changeUI(false);
                        }

                    });
        }
    }

    /*
     get user's information name, email, profile pic,Date of birth,tag line and about me
     */

    private void getProfileInfo() {

        try {

            if (Plus.PeopleApi.getCurrentPerson(google_api_client) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(google_api_client);
                setPersonalInfo(currentPerson);

            } else {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
     set the User information into the views defined in the layout
     */
    private void setPersonalInfo(Person currentPerson) {
        sDOB = currentPerson.getBirthday();
        sTagLine = currentPerson.getTagline();
        sAboutMe = currentPerson.getAboutMe();
        sGender = String.valueOf(currentPerson.getGender());
        sName = String.valueOf(currentPerson.getName().getGivenName());
        if (sGender.equals("0")) {
            msGender = "MALE";
        } else if (sGender.equals("1")) {
            msGender = "FEMALE";
        } else {
            msGender = "";
        }
        SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd");
        Date birthDate = null;
        try {
            birthDate = curFormaterDB.parse(sDOB);
            sAge = calculateAge(birthDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        personName = currentPerson.getDisplayName();
        personPhotoUrl = currentPerson.getImage().getUrl();
        email = Plus.AccountApi.getAccountName(google_api_client);
        TextView user_name = (TextView) findViewById(R.id.userName);
        user_name.setText("Name: " + personName);
        TextView gemail_id = (TextView) findViewById(R.id.emailId);
        gemail_id.setText("Email Id: " + email);
        TextView dob = (TextView) findViewById(R.id.dob);
        dob.setText(/*"DOB: " + currentPerson.getBirthday() + */" age: " + sAge);
        TextView tag_line = (TextView) findViewById(R.id.tag_line);
        tag_line.setText(/*"Tag Line: " + currentPerson.getTagline() + */" Name: " + sName);
        tag_line.setVisibility(View.GONE);
        TextView about_me = (TextView) findViewById(R.id.about_me);
        about_me.setText(/*"About Me: " + currentPerson.getAboutMe() + */" Gender: " + msGender);
        setProfilePic(personPhotoUrl);
        progress_dialog.dismiss();
        findViewById(R.id.btnVehicle).setVisibility(View.VISIBLE);
        findViewById(R.id.btnVehicle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserProfile(personName, personPhotoUrl, email, sAge, sTagLine, sAboutMe, strImag, msGender, sName);
                Intent intent1 = new Intent(MasterLogin_Act.this, VehicleVerification.class);
                startActivity(intent1);
                finish();
            }
        });
    }

    /*
     By default the profile pic url gives 50x50 px image.
     If you need a bigger image we have to change the query parameter value from 50 to the size you want
    */

    private void setProfilePic(String profile_pic) {
        profile_pic = profile_pic.substring(0,
                profile_pic.length() - 2)
                + PROFILE_PIC_SIZE;
        ImageView user_picture = (ImageView) findViewById(R.id.profile_pic);
        new LoadProfilePic(user_picture).execute(profile_pic);
    }

    /*
     Show and hide of the Views according to the user login status
     */

    private void changeUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }

   /*
    Perform background operation asynchronously, to load user profile picture with new dimensions from the modified url
    */

    private class LoadProfilePic extends AsyncTask<String, Void, Bitmap> {
        ImageView bitmap_img;

        public LoadProfilePic(ImageView bitmap_img) {
            this.bitmap_img = bitmap_img;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap new_icon = null;
            try {
                InputStream in_stream = new java.net.URL(url).openStream();
                new_icon = BitmapFactory.decodeStream(in_stream);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return new_icon;
        }

        protected void onPostExecute(Bitmap result_img) {

            try {
                bitmap_img.setImageBitmap(result_img);
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                result_img.compress(Bitmap.CompressFormat.PNG, 90, bao);
                byte[] ba = bao.toByteArray();
                strImag = Base64.encodeToString(ba, Base64.DEFAULT);
            } catch (Exception e) {
                Toast.makeText(MasterLogin_Act.this, getString(R.string.retry_internet222), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getUserProfile(final String personName, final String personPhotoUrl, final String email, final int sAge,
                                final String sTagLine, final String sAboutMe, final String strImag,
                                final String msGender, final String sName) {
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.GET_USER_PROFILE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getMyProfile(response, personName, personPhotoUrl, email, sAge,
                                sTagLine, sAboutMe, strImag, msGender, sName);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(
                        MasterLogin_Act.this,
                        getString(R.string.retry_internet),
                        Toast.LENGTH_LONG).show();
                SSLog.e(TAG, "getUserProfile :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();

                params = CGlobals_driver.getInstance().getMinAllMobileParams(params,
                        Constants_driver.GET_USER_PROFILE_URL);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_driver.GET_USER_PROFILE_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "getMemberDetails", e);
                }
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);

    } // getUserProfile

    private void getMyProfile(String response, final String personName, final String personPhotoUrl, final String email, final int sAge,
                              final String sTagLine, final String sAboutMe, final String strImag,
                              final String msGender, final String sName) {
        String user_Name;
        response = response.trim();
        if (response.trim().equals("-1")) {
            sendUserMasterLogin(personName, personPhotoUrl, email, sAge, sTagLine, sAboutMe, strImag, msGender, sName);
        }
        if (TextUtils.isEmpty(response)) {
            sendUserMasterLogin(personName, personPhotoUrl, email, sAge, sTagLine, sAboutMe, strImag, msGender, sName);
        }

        JSONObject person;
        try {
            person = new JSONObject(response);

            user_Name = person.isNull("username") ? "" : person
                    .getString("username");
            if (TextUtils.isEmpty(user_Name)) {
                sendUserMasterLogin(personName, personPhotoUrl, email, sAge, sTagLine, sAboutMe, strImag, msGender, sName);
            }
        } catch (Exception e) {
            SSLog.e(TAG, "getMyProfile", e);
        }
    }

    private void sendUserMasterLogin(final String personName, final String personPhotoUrl, final String email, final int sAge,
                                     final String sTagLine, final String sAboutMe, final String strImag,
                                     final String msGender, final String sName) {
        final String sAppUserId = Integer.toString(MyApplication.getInstance()
                .getPersistentPreference()
                .getInt(Constants_driver.PREF_APPUSERID, -1));
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.UPDATE_APPUSER_DETAIL_DRIVER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(
                        MasterLogin_Act.this,
                        getString(R.string.retry_internet),
                        Toast.LENGTH_LONG).show();
                try {
                    SSLog.d(TAG,
                            "sendUserMasterLogin :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "sendUserMasterLogin - " + sErr,
                            error.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("username", email);
                params.put("fullname", personName);
                params.put("age", String.valueOf(sAge));
                params.put("sex", msGender);
                params.put("imagerotation", "0");
                if (!TextUtils.isEmpty(strImag)) {
                    params.put("userprofileimage", strImag);
                }
                params.put("userprofileimagefilename", "photo" + sAppUserId + ".jpg");
                params = CGlobals_driver.getInstance().getMinAllMobileParams(params,
                        Constants_driver.UPDATE_APPUSER_DETAIL_DRIVER_URL);
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendUserMasterLogin

    private int calculateAge(Date birthDate) {
        int years = 0;
        int months = 0;
        int days = 0;
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
        //Create new Age object
        return years;
    }
}