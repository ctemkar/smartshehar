package com.jumpinjumpout.apk.user.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.lib.ui.SearchAddress_act;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.CAddress;

/**
 * Created by jijo_soumen on 11/01/2016.
 */
public class UserAccountLoginSample_act extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleApiClient.ConnectionCallbacks {

    public static EditText msFull_Name, msAge;
    private TextView msTvHome, msTvWork, tvNotNow;
    public static ImageView msIvShowImage;
    public ImageView msIvMeImage;
    public static Spinner msSGender;
    public LoginButton loginButton;
    private SignInButton signIn_btn;
    private Button msBtnUserInfoSubmit;
    private static final int REQUEST_PHOTO_USER = 777;
    protected final int ACTRESULT_WORKADDRESS = 177;
    protected final int ACTRESULT_HOMEADDRESS = 277;
    private static final String[] selectGender = {"GENDER", "MALE", "FEMALE"};
    String mCurrentPhotoPath, imageFileName;
    private static String TAG = "UserAccountLoginSample_act: ";
    GoogleApiClient google_api_client;
    GoogleApiAvailability google_api_availability;
    private static final int SIGN_IN_CODE = 0;
    private ConnectionResult connection_result;
    private boolean is_intent_inprogress;
    private boolean is_signInBtn_clicked;
    private int request_code;
    ProgressDialog progress_dialog;
    String strImag;
    String personName, personPhotoUrl, sEmail, sDOB, sTagLine, sAboutMe, msGender = "", sName, sGender;
    int sAge;
    Preference mPrefHome, mPrefWork;
    CAddress caWorkAddress, caHomeAddress;
    private CallbackManager mCallbackManager;
    private Profile profile;
    private AccessTokenTracker tokenTracker;
    private ProfileTracker profileTracker;
    String get_gender, get_email, get_birthday, get_Profile_Pic;
    private static final int PROFILE_PIC_SIZE = 120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buidNewGoogleApiClient();
        setContentView(R.layout.useraccountlogin_sample);
        init();
        custimizeSignBtn();
        setBtnClickListeners();
        ArrayAdapter<String> adaptergender = new ArrayAdapter<String>(
                UserAccountLoginSample_act.this,
                android.R.layout.simple_spinner_item, selectGender);
        adaptergender
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        msSGender.setAdapter(adaptergender);
        progress_dialog = new ProgressDialog(UserAccountLoginSample_act.this);
        progress_dialog.setMessage("Signing in....");

        msBtnUserInfoSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserInformationServer();
            }
        });

        msIvMeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        msIvShowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        tvNotNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.getInstance().getPersistentPreferenceEditor()
                        .putBoolean(Constants_user.PREF_LOGIN_LOGOUT_FLAG, true);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                Intent intent = new Intent(UserAccountLoginSample_act.this, Dashboard_act.class);
                startActivity(intent);
                finish();
            }
        });

        msTvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addressIntent = new Intent(UserAccountLoginSample_act.this, SearchAddress_act.class);
                addressIntent.putExtra("VALUE_SETTING2", "2");
                startActivityForResult(addressIntent, ACTRESULT_HOMEADDRESS);
            }
        });

        msTvWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addressIntent = new Intent(UserAccountLoginSample_act.this, SearchAddress_act.class);
                addressIntent.putExtra("VALUE_SETTING1", "1");
                startActivityForResult(addressIntent, ACTRESULT_WORKADDRESS);
            }
        });
        mCallbackManager = CallbackManager.Factory.create();
        tokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken1) {

            }
        };
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile profile, Profile profile1) {
                /*textView.setText(displayMessage(profile1));*/
            }
        };

        tokenTracker.startTracking();
        profileTracker.startTracking();
        loginButton.setCompoundDrawables(null, null, null, null);
        loginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday, user_friends"));
        loginButton.registerCallback(mCallbackManager, mFacebookCallback);

    }

    private void sendUserInformationServer() {
        String fullname = msFull_Name.getText().toString();
        String getage = msAge.getText().toString();
        String getGender = msSGender.getSelectedItem().toString();
        String imageString = strImag;
        if (fullname.trim().equals("")
                || getage.trim().equals("")
                || getGender.trim().equals("GENDER") || TextUtils.isEmpty(imageString)) {
            new AlertDialog.Builder(UserAccountLoginSample_act.this)
                    .setMessage("All fields are mandatory")
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog,
                                        int which) {
                                    dialog.cancel();
                                }
                            })
                    .setIcon(android.R.drawable.ic_dialog_alert).show();
        } else {
            getUserProfile(fullname, CGlobals_user.getInstance().msGmail, Integer.parseInt(getage), imageString, getGender);
        }
    }

    private void setPic() {
        try {
            File auxFile = new File(mCurrentPhotoPath);
            getReducedImage(auxFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getReducedImage(File mediaFile) {
        Bitmap b = decodeFileWithRotationIfNecessary(mediaFile);
        File f = getfileFromBitmap(b, mediaFile.getPath());
        msIvShowImage.setImageBitmap(b);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 90, bao);
        byte[] ba = bao.toByteArray();
        strImag = Base64.encodeToString(ba, Base64.DEFAULT);
    }

    private File getfileFromBitmap(Bitmap b, String path) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);


        // you can create a new file name "test.jpg" in sdcard folder.
        File f = new File(path);
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            // remember close de FileOutput
            fo.close();
            return f;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.v(TAG, "Exception caught");
            return null;
        }
        // write the bytes in file
    }

    private Bitmap decodeFileWithRotationIfNecessary(File f) {
        final int IMAGE_MAX_SIZE = 400;
        Bitmap b = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);

            BitmapFactory.decodeStream(fis, null, o);

            fis.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.round(Math.log(IMAGE_MAX_SIZE
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (IOException e) {
            Log.v(TAG, "error in bitmap conversion");
            e.printStackTrace();

        }

        Bitmap bMapRotate = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
                b.getHeight(), getMatrix(f), true);

        return bMapRotate;
    }

    private Matrix getMatrix(File f) {
        Matrix mat = new Matrix();
        mat.postRotate(90);
        try {
            ExifInterface exif = new ExifInterface(f.getPath());

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, -1);

            switch (orientation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    Log.v(TAG, "flip horizontal");


                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:

                    Log.v(TAG, "flip vertical");
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    Log.v(TAG, "rotate 180");
                    mat.postRotate(90);

                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    Log.v(TAG, "rotate 90");

                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    Log.v(TAG, "rotate 270");
                    mat.postRotate(180);

                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    Log.v(TAG, "transpose");

                    break;
                case ExifInterface.ORIENTATION_UNDEFINED:
                    Log.v(TAG, "undefined");

                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                    Log.v(TAG, "normal");
                    mat.postRotate(270);

                    break;
                default:
                    Log.v(TAG, "default");
                    //  mat.postRotate(0);

                    break;
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.v(TAG, "error in finding exif information");
        }

        return mat;
    }

    private void init() {
        msFull_Name = (EditText) findViewById(R.id.full_name);
        msAge = (EditText) findViewById(R.id.age);
        msTvHome = (TextView) findViewById(R.id.tvHome);
        msTvWork = (TextView) findViewById(R.id.tvWork);
        msIvShowImage = (ImageView) findViewById(R.id.ivShowImage);
        msIvMeImage = (ImageView) findViewById(R.id.ivMeImage);
        msSGender = (Spinner) findViewById(R.id.gender);
        signIn_btn = (SignInButton) findViewById(R.id.sign_in_button);
        msBtnUserInfoSubmit = (Button) findViewById(R.id.btnUserInfoSubmit);
        tvNotNow = (TextView) findViewById(R.id.tvNotNow);
        loginButton = (LoginButton) findViewById(R.id.login_button);
    }

    private void buidNewGoogleApiClient() {

        google_api_client = new GoogleApiClient.Builder(UserAccountLoginSample_act.this)
                .addConnectionCallbacks(UserAccountLoginSample_act.this)
                .addOnConnectionFailedListener(UserAccountLoginSample_act.this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    private void custimizeSignBtn() {
        signIn_btn.setSize(SignInButton.SIZE_STANDARD);
        signIn_btn.setScopes(new Scope[]{Plus.SCOPE_PLUS_LOGIN});

    }

    private void setBtnClickListeners() {
        // Button listeners
        signIn_btn.setOnClickListener(UserAccountLoginSample_act.this);
        findViewById(R.id.sign_out_button).setOnClickListener(UserAccountLoginSample_act.this);
    }

    protected void onStart() {
        super.onStart();
        google_api_client.connect();
    }

    protected void onStop() {
        super.onStop();
        profileTracker.stopTracking();
        tokenTracker.stopTracking();
        if (google_api_client.isConnected()) {
            google_api_client.disconnect();
        }
    }

    protected void onResume() {
        super.onResume();
        if (google_api_client.isConnected()) {
            google_api_client.connect();
        }
        try {
            get_gender = MyApplication.getInstance().getPersistentPreference().getString("GENDER_FACEBOOK", "");
            get_email = MyApplication.getInstance().getPersistentPreference().getString("EMAIL_FACEBOOK", "");
            profile = Profile.getCurrentProfile();
            get_Profile_Pic = String.valueOf(profile.getProfilePictureUri(120, 120));
            msFull_Name.setText(displayMessage(profile));
            if (get_gender.equals("MALE")) {
                msSGender.setSelection(1);
            } else if (get_gender.equals("FEMALE")) {
                msSGender.setSelection(2);
            } else {
                msSGender.setSelection(0);
            }
            setProfilePicFacebook(get_Profile_Pic);
            SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd");
            Date birthDate = null;
            try {
                birthDate = curFormaterDB.parse(get_birthday);
                sAge = calculateAge(birthDate);
                msAge.setText(String.valueOf(sAge));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String displayMessage(Profile profile) {
        StringBuilder stringBuilder = new StringBuilder();
        if (profile != null) {
            stringBuilder.append(profile.getFirstName());
        } else {
            stringBuilder.append("");
        }
        return stringBuilder.toString();
    }

    private void setProfilePicFacebook(String profile_pic) {
        profile_pic = profile_pic.substring(0,
                profile_pic.length() - 2)
                + PROFILE_PIC_SIZE;
        new LoadProfilePicFacebook(msIvShowImage).execute(profile_pic);
    }

    private class LoadProfilePicFacebook extends AsyncTask<String, Void, Bitmap> {
        ImageView bitmap_img;

        public LoadProfilePicFacebook(ImageView bitmap_img) {
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
                get_gender = MyApplication.getInstance().getPersistentPreference().getString("GENDER_FACEBOOK", "");
                get_email = MyApplication.getInstance().getPersistentPreference().getString("EMAIL_FACEBOOK", "");
                sAge = MyApplication.getInstance().getPersistentPreference().getInt("BIRTHDAY_FACEBOOK", 0);
                profile = Profile.getCurrentProfile();
                getUserProfile(profile.getName(), get_email, sAge, strImag, get_gender);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            google_api_availability.getErrorDialog(UserAccountLoginSample_act.this, result.getErrorCode(), request_code).show();
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
    protected void onActivityResult(int requestCode, int responseCode,
                                    Intent intent) {
        // Check which request we're responding to
        mCallbackManager.onActivityResult(requestCode, responseCode, intent);
        if (requestCode == REQUEST_PHOTO_USER && responseCode == RESULT_OK) {

            if (intent != null) {
                Bundle extra = intent.getBundleExtra("data");
                byte[] ba = extra.getByteArray("originalByte");
                imageFileName = extra.getString("imageName");
                mCurrentPhotoPath = extra.getString("imageFilePath");
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PATH_NAME", mCurrentPhotoPath);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("IMAGE_PATH_NAME", imageFileName);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                setPic();

            }
        }
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

        String sAddr = "";
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
        if (requestCode == ACTRESULT_WORKADDRESS) {
            if (responseCode == RESULT_OK) {
                sAddr = intent.getStringExtra("setting_address");
                if (!TextUtils.isEmpty(sAddr)) {
                    try {
                        Type type = new TypeToken<CAddress>() {
                        }.getType();
                        caWorkAddress = new Gson().fromJson(sAddr, type);
                        String json = new Gson().toJson(caWorkAddress);
                        prefs.putString("workAddress", json);
                        prefs.commit();
                        mPrefWork.setSummary(caWorkAddress.getAddress());
                        sendHomeWorkAddress();
                        msTvWork.setText(sAddr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        if (requestCode == ACTRESULT_HOMEADDRESS) {
            if (responseCode == RESULT_OK) {
                sAddr = intent.getStringExtra("setting_address");
                if (!TextUtils.isEmpty(sAddr)) {
                    try {
                        Type type = new TypeToken<CAddress>() {
                        }.getType();
                        caHomeAddress = new Gson().fromJson(sAddr, type);
                        String json = new Gson().toJson(caHomeAddress);
                        prefs.putString("homeAddress", json);
                        prefs.commit();
                        mPrefHome.setSummary(caHomeAddress.getAddress());
                        sendHomeWorkAddress();
                        msTvHome.setText(sAddr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        if (responseCode == RESULT_CANCELED) {
            Toast.makeText(UserAccountLoginSample_act.this, "Aborted", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendHomeWorkAddress() {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.ADD_HOME_WORK_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SSLog.e(TAG, "sendHomeWorkAddress :-   ", error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("work", caWorkAddress.getAddress());
                params.put("worklat", String.valueOf(caWorkAddress.getLatitude()));
                params.put("worklng", String.valueOf(caWorkAddress.getLongitude()));
                params.put("home", caHomeAddress.getAddress());
                params.put("homelat", String.valueOf(caHomeAddress.getLatitude()));
                params.put("homelng", String.valueOf(caHomeAddress.getLongitude()));
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.ADD_HOME_WORK_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }
                String url1 = Constants_user.ADD_HOME_WORK_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "sendHomeWorkAddress", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
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
                Toast.makeText(UserAccountLoginSample_act.this, "start sign process", Toast.LENGTH_SHORT).show();
                gPlusSignIn();
                break;
            case R.id.sign_out_button:
                Toast.makeText(UserAccountLoginSample_act.this, "Sign Out from G+", Toast.LENGTH_LONG).show();
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
        if (connection_result.hasResolution()) {
            try {
                is_intent_inprogress = true;
                connection_result.startResolutionForResult(UserAccountLoginSample_act.this, SIGN_IN_CODE);
                Log.d("resolve error", "sign in error resolved");
            } catch (IntentSender.SendIntentException e) {
                is_intent_inprogress = false;
                google_api_client.connect();
            }
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
                Toast.makeText(UserAccountLoginSample_act.this,
                        "No Personal info mention", Toast.LENGTH_LONG).show();
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
        sEmail = Plus.AccountApi.getAccountName(google_api_client);
        msFull_Name.setText(personName);
        msAge.setText(String.valueOf(sAge));
        if (msGender.equals("MALE")) {
            msSGender.setSelection(1);
        } else if (msGender.equals("FEMALE")) {
            msSGender.setSelection(2);
        } else {
            msSGender.setSelection(0);
        }
        setProfilePic(personPhotoUrl);
        progress_dialog.dismiss();
        Toast.makeText(this, "Person information is shown!", Toast.LENGTH_LONG).show();
    }

    private void setProfilePic(String profile_pic) {
        profile_pic = profile_pic.substring(0,
                profile_pic.length() - 2)
                + PROFILE_PIC_SIZE;
        new LoadProfilePic(msIvShowImage).execute(profile_pic);
    }

    private void changeUI(boolean signedIn) {
        if (signedIn) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        } else {

            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        }
    }

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

            bitmap_img.setImageBitmap(result_img);
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            result_img.compress(Bitmap.CompressFormat.PNG, 90, bao);
            byte[] ba = bao.toByteArray();
            strImag = Base64.encodeToString(ba, Base64.DEFAULT);
            getUserProfile(personName, sEmail, sAge, strImag, msGender);
        }
    }

    private void getUserProfile(final String spersonName, final String semail, final int ssAge,
                                final String sstrImag,
                                final String smsGender) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.GET_USER_PROFILE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getMyProfile(response, spersonName, semail, ssAge, sstrImag, smsGender);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(
                        UserAccountLoginSample_act.this,
                        getString(R.string.retry_internet),
                        Toast.LENGTH_LONG).show();
                SSLog.e(TAG, "getUserProfile :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();

                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.GET_USER_PROFILE_URL);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.GET_USER_PROFILE_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "getMemberDetails", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);

    } // getUserProfile

    private void getMyProfile(String response, String spersonName,
                              String email, int sAge, String strImag, String msGender) {
        String user_Name;
        response = response.trim();
        if (response.trim().equals("-1")) {
            sendUserMasterLogin(spersonName, email, sAge, strImag, msGender);
        } else {
            MyApplication.getInstance().getPersistentPreferenceEditor()
                    .putBoolean(Constants_user.PREF_LOGIN_LOGOUT_FLAG, true);
            MyApplication.getInstance().getPersistentPreferenceEditor().commit();
            Intent intent = new Intent(UserAccountLoginSample_act.this, Dashboard_act.class);
            startActivity(intent);
            finish();
        }
        if (TextUtils.isEmpty(response)) {
            sendUserMasterLogin(spersonName, email, sAge, strImag, msGender);
        } else {
            MyApplication.getInstance().getPersistentPreferenceEditor()
                    .putBoolean(Constants_user.PREF_LOGIN_LOGOUT_FLAG, true);
            MyApplication.getInstance().getPersistentPreferenceEditor().commit();
            Intent intent = new Intent(UserAccountLoginSample_act.this, Dashboard_act.class);
            startActivity(intent);
            finish();
        }

        JSONObject person;
        try {
            person = new JSONObject(response);
            user_Name = person.isNull("username") ? "" : person
                    .getString("username");
            if (TextUtils.isEmpty(user_Name)) {
                sendUserMasterLogin(spersonName, email, sAge, strImag, msGender);
            } else {
                MyApplication.getInstance().getPersistentPreferenceEditor()
                        .putBoolean(Constants_user.PREF_LOGIN_LOGOUT_FLAG, true);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                Intent intent = new Intent(UserAccountLoginSample_act.this, Dashboard_act.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            SSLog.e(TAG, "getMyProfile", e);
        }
    }

    private void sendUserMasterLogin(final String personName, final String email, final int sAge, final String strImag,
                                     final String msGender) {
        final String sAppUserId = Integer.toString(MyApplication.getInstance()
                .getPersistentPreference()
                .getInt(Constants_user.PREF_APPUSERID, -1));
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.UPDATE_APPUSER_DETAIL_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        MyApplication.getInstance().getPersistentPreferenceEditor()
                                .putBoolean(Constants_user.PREF_LOGIN_LOGOUT_FLAG, true);
                        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                        Intent intent = new Intent(UserAccountLoginSample_act.this, Dashboard_act.class);
                        startActivity(intent);
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(UserAccountLoginSample_act.this, sErr, Toast.LENGTH_SHORT).show();
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
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.UPDATE_APPUSER_DETAIL_URL);
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendUserMasterLogin

    private int calculateAge(Date birthDate) {
        int years = 0;
        int months = 0;
        int days = 0;
        Calendar birthDay = Calendar.getInstance();
        birthDay.setTimeInMillis(birthDate.getTime());
        long currentTime = System.currentTimeMillis();
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(currentTime);
        years = now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
        int currMonth = now.get(Calendar.MONTH) + 1;
        int birthMonth = birthDay.get(Calendar.MONTH) + 1;
        months = currMonth - birthMonth;
        if (months < 0) {
            years--;
            months = 12 - birthMonth + currMonth;
            if (now.get(Calendar.DATE) < birthDay.get(Calendar.DATE))
                months--;
        } else if (months == 0 && now.get(Calendar.DATE) < birthDay.get(Calendar.DATE)) {
            years--;
            months = 11;
        }
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

    private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            AccessToken accessToken = loginResult.getAccessToken();
            profile = Profile.getCurrentProfile();
            /*textView.setText(displayMessage(profile));*/
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(
                                JSONObject object,
                                GraphResponse response) {

                            try {
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
                                MyApplication.getInstance().getPersistentPreferenceEditor().putString("GENDER_FACEBOOK", get_gender);
                                MyApplication.getInstance().getPersistentPreferenceEditor().putString("EMAIL_FACEBOOK", get_email);
                                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                                profile = Profile.getCurrentProfile();
                                get_Profile_Pic = String.valueOf(profile.getProfilePictureUri(120, 120));
                                msFull_Name.setText(profile.getName());
                                if (get_gender.equals("MALE")) {
                                    msSGender.setSelection(1);
                                } else if (get_gender.equals("FEMALE")) {
                                    msSGender.setSelection(2);
                                } else {
                                    msSGender.setSelection(0);
                                }
                                setProfilePic(get_Profile_Pic);
                                SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd");
                                Date birthDate = null;
                                try {
                                    birthDate = curFormaterDB.parse(get_birthday);
                                    sAge = calculateAge(birthDate);
                                    msAge.setText(String.valueOf(sAge));
                                    MyApplication.getInstance().getPersistentPreferenceEditor().putInt("BIRTHDAY_FACEBOOK", sAge);
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

        }

        @Override
        public void onError(FacebookException e) {

        }
    };


}
