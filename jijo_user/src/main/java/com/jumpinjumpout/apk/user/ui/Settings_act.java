package com.jumpinjumpout.apk.user.ui;

/**
 * Created by Chetan Temkar on 16-05-2015.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.lib.ui.SearchAddress_act;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.CAddress;

public class Settings_act extends PreferenceActivity {
    protected static final String TAG = "Settings_act: ";
    protected final int ACTRESULT_WORKADDRESS = 1;
    protected final int ACTRESULT_HOMEADDRESS = 2;
    protected final int USER_PROFILE = 3;
    Preference mPrefHome, mPrefWork, mUserProfile;
    CAddress caWorkAddress, caHomeAddress;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        mPrefWork = findPreference("workAddress");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String sAddresswork = pref.getString("workAddress", "Select Work address");
        if (!TextUtils.isEmpty(sAddresswork)) {
            Type type = new TypeToken<CAddress>() {
            }.getType();
            try {
                caWorkAddress = gson.fromJson(sAddresswork, type);
                mPrefWork.setSummary(caWorkAddress.getAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mPrefWork.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent addressIntent = new Intent(Settings_act.this, SearchAddress_act.class);
                addressIntent.putExtra("VALUE_SETTING1", "1");
                startActivityForResult(addressIntent, ACTRESULT_WORKADDRESS);
                return false;
            }
        });

        mPrefHome = findPreference("homeAddress");
        String sAddresshome = pref.getString("homeAddress", "Select Home addres");
        if (!TextUtils.isEmpty(sAddresshome)) {
            try {
                Type type = new TypeToken<CAddress>() {
                }.getType();
                caHomeAddress = new Gson().fromJson(sAddresshome, type);
                mPrefHome.setSummary(caHomeAddress.getAddress());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mPrefHome.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent addressIntent = new Intent(Settings_act.this, SearchAddress_act.class);
                addressIntent.putExtra("VALUE_SETTING2", "2");
                startActivityForResult(addressIntent, ACTRESULT_HOMEADDRESS);
                return false;
            }
        });


        mUserProfile = findPreference("userprofile");
        mUserProfile.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent addressIntent = new Intent(Settings_act.this, UpdateUserDetails_act.class);
                startActivityForResult(addressIntent, USER_PROFILE);
                return false;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String sAddr = "";
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
        if (requestCode == ACTRESULT_WORKADDRESS) {
            if (resultCode == RESULT_OK) {
                sAddr = data.getStringExtra("setting_address");
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
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(Settings_act.this, "Aborted", Toast.LENGTH_SHORT).show();
        }
        if (requestCode == USER_PROFILE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(Settings_act.this, "Complete your profile", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == ACTRESULT_HOMEADDRESS) {
            if (resultCode == RESULT_OK) {
                sAddr = data.getStringExtra("setting_address");
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
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(Settings_act.this, "Aborted", Toast.LENGTH_SHORT).show();
        }

    } // onActivityResult

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
}