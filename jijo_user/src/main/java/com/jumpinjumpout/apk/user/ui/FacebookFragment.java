package com.jumpinjumpout.apk.user.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user pc on 05-12-2015.
 */
public class FacebookFragment extends Fragment {

    private static String TAG = "FacebookFragment: ";
    private TextView textView, text1;
    private ImageView user_picture;
    private CallbackManager mCallbackManager;
    private Profile profile;
    private AccessTokenTracker tokenTracker;
    private ProfileTracker profileTracker;
    String get_gender, get_email, get_birthday, get_Profile_Pic, strImag;
    int sAge;
    private static final int PROFILE_PIC_SIZE = 120;

    public FacebookFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallbackManager = CallbackManager.Factory.create();
        tokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken1) {

            }
        };
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile profile, Profile profile1) {
                textView.setText(displayMessage(profile1));
            }
        };

        tokenTracker.startTracking();
        profileTracker.startTracking();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.facebookfragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView = (TextView) view.findViewById(R.id.text);
        text1 = (TextView) view.findViewById(R.id.text1);
        user_picture = (ImageView) view.findViewById(R.id.profile_pic);
        textView.setText("You are not logged in");
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setCompoundDrawables(null, null, null, null);
        loginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday, user_friends"));
        loginButton.setFragment(this);
        loginButton.registerCallback(mCallbackManager, mFacebookCallback);
    }

    private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            AccessToken accessToken = loginResult.getAccessToken();
            profile = Profile.getCurrentProfile();
            textView.setText(displayMessage(profile));
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
                                Profile profile = Profile.getCurrentProfile();
                                get_Profile_Pic = String.valueOf(profile.getProfilePictureUri(120, 120));
                                textView.setText(displayMessage(profile));
                                text1.setText(profile.getId()
                                        + "\n" + profile.getFirstName()
                                        + "\n" + profile.getLastName()
                                        + "\n" + profile.getMiddleName()
                                        + "\n" + profile.getName()
                                        + "\n" + profile.getLinkUri()
                                        + "\n" + get_gender
                                        + "\n" + get_email
                                        + "\n" + get_birthday);
                                setProfilePic(get_Profile_Pic);
                                SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd");
                                Date birthDate = null;
                                try {
                                    birthDate = curFormaterDB.parse(get_birthday);
                                    sAge = calculateAge(birthDate);
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

    private void setProfilePic(String profile_pic) {
        profile_pic = profile_pic.substring(0,
                profile_pic.length() - 2)
                + PROFILE_PIC_SIZE;
        new LoadProfilePic(user_picture).execute(profile_pic);
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
            get_gender = MyApplication.getInstance().getPersistentPreference().getString("GENDER_FACEBOOK", "");
            get_email = MyApplication.getInstance().getPersistentPreference().getString("EMAIL_FACEBOOK", "");
            sAge = MyApplication.getInstance().getPersistentPreference().getInt("BIRTHDAY_FACEBOOK", 0);
            Profile profile = Profile.getCurrentProfile();
            /*sendUserMasterLogin(profile.getName(), get_email, sAge,
                    strImag, get_gender, profile.getId());*/
            getUserProfile(profile.getName(), get_email, sAge,
                    strImag, get_gender, profile.getId());
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            get_gender = MyApplication.getInstance().getPersistentPreference().getString("GENDER_FACEBOOK", "");
            get_email = MyApplication.getInstance().getPersistentPreference().getString("EMAIL_FACEBOOK", "");
            Profile profile = Profile.getCurrentProfile();
            get_Profile_Pic = String.valueOf(profile.getProfilePictureUri(120, 120));
            textView.setText(displayMessage(profile));
            text1.setText(profile.getId()
                    + "\n" + profile.getFirstName()
                    + "\n" + profile.getLastName()
                    + "\n" + profile.getMiddleName()
                    + "\n" + profile.getName()
                    + "\n" + profile.getLinkUri()
                    + "\n" + get_gender
                    + "\n" + get_email);
            setProfilePic(get_Profile_Pic);
            SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd");
            Date birthDate = null;
            try {
                birthDate = curFormaterDB.parse(get_birthday);
                sAge = calculateAge(birthDate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        profileTracker.stopTracking();
        tokenTracker.stopTracking();
    }

    private String displayMessage(Profile profile) {
        StringBuilder stringBuilder = new StringBuilder();
        if (profile != null) {
            stringBuilder.append("Logged In " + profile.getFirstName());
            Toast.makeText(getActivity(), "Start Playing with the data " + profile.getFirstName(), Toast.LENGTH_SHORT).show();
        } else {
            stringBuilder.append("You are not logged in");
        }
        return stringBuilder.toString();
    }

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
        return years;
    }


    private void getUserProfile(final String personName, final String email, final int sAge,
                                final String strImag,
                                final String msGender, final String sName) {
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.GET_USER_PROFILE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getMyProfile(response, personName, email, sAge,
                                strImag, msGender, sName);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(
                        getActivity().getBaseContext(),
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

    private void getMyProfile(String response, String name, String email, int sAge,
                              String strImag,
                              String msGender, String sName) {
        String user_Name;
        response = response.trim();
        if (response.trim().equals("-1")) {
            sendUserMasterLogin(name, email, sAge,
                    strImag, msGender, sName);
        }
        if (TextUtils.isEmpty(response)) {
            sendUserMasterLogin(name, email, sAge,
                    strImag, msGender, sName);
        }
        JSONObject person;
        try {
            person = new JSONObject(response);
            user_Name = person.isNull("username") ? "" : person
                    .getString("username");
            if (TextUtils.isEmpty(user_Name)) {
                sendUserMasterLogin(name, email, sAge,
                        strImag, msGender, sName);
            }
        } catch (Exception e) {
            SSLog.e(TAG, "getMyProfile", e);
        }
    }

    private void sendUserMasterLogin(final String personName, final String email, final int sAge,
                                     final String strImag,
                                     final String msGender, final String sName) {
        final String sAppUserId = Integer.toString(MyApplication.getInstance()
                .getPersistentPreference()
                .getInt(Constants_user.PREF_APPUSERID, -1));
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.UPDATE_APPUSER_DETAIL_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(getActivity(), sErr, Toast.LENGTH_SHORT).show();
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
                params.put("sex", msGender.toUpperCase());
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


}