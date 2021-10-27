package com.jumpinjumpout.apk.user.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.Contact_Results;
import com.jumpinjumpout.apk.user.Contact_Trip_Adapter;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import lib.app.util.CSms;
import lib.app.util.ContactInfo;


public class InviteUserToApp extends AddContact_act {

    TextView inCommunity, inpeople;
    Button done, notNow;
    protected int INVITE_COMMUNITY = 5;
    ListView gplist;
    LinearLayout ll;
    TextView tv;
    String community_id_gp, community_name_gp, community_desc_gp,
            community_type_gp, gp_desc;
    Bitmap bitmap_Community_Image = null;
    byte image_InByte_Community_Image[];
    private int tpID;
    CSms mSms = null;
    String member_Name, member_PhoneNo, member_EmailId, member_CountryCode;
    Contact_Results gresuli;
    private String tripMemberId;
    String memberAppuserId, memberPhoneno;
    String openTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_user_to_app);

        inCommunity = (TextView) findViewById(R.id.ingroup);
        inpeople = (TextView) findViewById(R.id.inpeople);
        gplist = (ListView) findViewById(R.id.gplist);
        done = (Button) findViewById(R.id.done);
        notNow = (Button) findViewById(R.id.notNow);
        ll = (LinearLayout) findViewById(R.id.no_trip_list);
        tv = (TextView) findViewById(R.id.ttrip);

        Intent intenttrip = getIntent();
        tpID = intenttrip.getIntExtra("TRIPID", 0);
        openTrip = intenttrip.getStringExtra("OPENTRIP");
        System.out.println("tpID   " + tpID);
        notNow.setText("Open trip");
        notNow.setBackgroundResource(R.drawable.btn_open);

        inCommunity.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intentingroup = new Intent(InviteUserToApp.this,
                        CommunityShowAll_act.class);

                intentingroup.putExtra(Constants_user.CHANGE_VALUE,
                        Constants_user.VALUE_TRUE);

                startActivityForResult(intentingroup, INVITE_COMMUNITY);

            }
        });

        inpeople.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(mContactAccessor.getPickContactIntent(), PICK_CONTACT_REQUEST);
            }
        });

        done.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (contactResults.size() == 0) {

                    Toast.makeText(
                            getApplicationContext(),
                            "Select a person or community or hit back or cancel",
                            Toast.LENGTH_LONG).show();

                } else {
                    getMemberAlreadyIsInTrip();
                    hideKeyboard();
                    Intent intent = new Intent();
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }

            }

        });

        notNow.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                createOpenTrip();
                hideKeyboard();
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        gplist.setVisibility(View.VISIBLE);

        ll.setVisibility(View.GONE);

        tv.setVisibility(View.GONE);
        if (requestCode == INVITE_COMMUNITY) {

            if (resultCode == RESULT_CANCELED) {

                return;
            }

            bitmap_Community_Image = BitmapFactory.decodeResource(
                    getResources(), R.mipmap.community);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap_Community_Image.compress(Bitmap.CompressFormat.JPEG, 100,
                    stream);

            image_InByte_Community_Image = stream.toByteArray();

            community_id_gp = data.getStringExtra("group_id_item");
            community_name_gp = data.getStringExtra("group_Name_item");
            community_desc_gp = data.getStringExtra("group_Desc_item");
            community_type_gp = data.getStringExtra("group_Type_item");
            gresuli = new Contact_Results();
            if (community_type_gp.equals("P")) {

                gp_desc = "Private Community";

            }
            gresuli.setContactName(community_name_gp);
            gresuli.setContactPhone(gp_desc);
            gresuli.setContactImage(image_InByte_Community_Image);
            gresuli.setID(community_id_gp);
            contactResults.add(gresuli);

            sendInviteCommunity();

        }
        updateList();


    }

    protected void updateList() {
        Gson gson = new Gson();
        String json = gson.toJson(contactResults);
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putString(Constants_user.PREF_INVITE_USER_CURRENT_TO_APP, json);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();

        gplist.setAdapter(new Contact_Trip_Adapter(InviteUserToApp.this,
                contactResults));
        ll.setVisibility(View.GONE);

        tv.setVisibility(View.GONE);
        gplist.setVisibility(View.VISIBLE);
    }

    protected void sendInvitePeople(final ContactInfo contactInfo) {

        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.ADD_TRIP_MEMBER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        getTripMemberDetailsId(response);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    SSLog.e(TAG, "sendInvitePeople :-   ",
                            error.getMessage());

                } catch (Exception e) {
                    SSLog.e(TAG, "sendInvitePeople -  ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();

                params.put("tripid", String.valueOf(tpID));
                params.put("membername", contactInfo.getDisplayName());
                if (!TextUtils.isEmpty(contactInfo.getPhoneNumber())) {
                    params.put("memberphoneno", contactInfo.getPhoneNumber());
                }

                if (!TextUtils.isEmpty(contactInfo.getEmail())) {
                    params.put("memberemail", contactInfo.getEmail());
                }

                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.ADD_TRIP_MEMBER_URL);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }

                String url1 = Constants_user.ADD_TRIP_MEMBER_URL;

                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "sendInvitePeople", e);
                }
                return CGlobals_user.getInstance().checkParams(params);

            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);

    }

    private void sendInviteCommunity() {

        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.ADD_TRIP_COMMUNITY_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        getTripMemberDetailsId(response);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    SSLog.e(TAG, "sendInviteCommunity :-   ",
                            error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "sendInviteCommunity -  ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();

                params.put("tripid", String.valueOf(tpID));
                params.put(Constants_user.PARAMETER_COMMUNITY_ID,
                        community_id_gp);
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.ADD_TRIP_COMMUNITY_URL);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }

                String url1 = Constants_user.ADD_TRIP_COMMUNITY_URL;

                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "sendInviteCommunity", e);
                }
                return CGlobals_user.getInstance().checkParams(params);

            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);

    }

    private void getTripMemberDetailsId(String response) {

        if (TextUtils.isEmpty(response.trim()) || response.trim().equals("-1")) {
            return;
        }

        JSONObject jResponse;
        try {
            jResponse = new JSONObject(response.trim());
            tripMemberId = jResponse.getString("trip_member_details_id");

            MyApplication.getInstance().getPersistentPreferenceEditor()
                    .putString(Constants_user.PREF_TRIP_MEMBER_ID, tripMemberId);
            MyApplication.getInstance().getPersistentPreferenceEditor().commit();

            Contact_Results gresuli1 = new Contact_Results();
            gresuli1.setMemberTripID(tripMemberId);

        } catch (Exception e) {

            Toast.makeText(getApplicationContext(),
                    "Invalid Community name, try again..", Toast.LENGTH_SHORT)
                    .show();
            SSLog.e(TAG, "groupResult", e);
        }

    }

    private void invitetripmember() {

        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.INVITE_TRIP_MEMBER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        noAppRows();
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    SSLog.e(TAG, "invitetripmember :-   ",
                            error.getMessage());

                } catch (Exception e) {
                    SSLog.e(TAG, "invitetripmember  -  ", e);
                }

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();

                params.put("tripid", String.valueOf(tpID));

                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.INVITE_TRIP_MEMBER_URL);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }

                String url1 = Constants_user.INVITE_TRIP_MEMBER_URL;

                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "invitetripmember", e);
                }
                return CGlobals_user.getInstance().checkParams(params);

            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);

    }

    private void noAppRows() {

        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.DO_NOT_HAVE_APP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        sendSMS(response);

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    SSLog.e(TAG, "noAppRows :-   ", error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "noAppRows -  ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();

                params.put("tripid", String.valueOf(tpID));

                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.DO_NOT_HAVE_APP_URL);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }

                String url1 = Constants_user.DO_NOT_HAVE_APP_URL;

                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "noAppRows", e);
                }
                return CGlobals_user.getInstance().checkParams(params);

            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);

    }

    private void sendSMS(String response) {

        if (TextUtils.isEmpty(response) || response.trim().equals("-1")) {
            return;
        }

        try {

            JSONArray aJson = new JSONArray(response);

            for (int i = 0; i < aJson.length(); i++) {

                JSONObject person = (JSONObject) aJson.get(i);

                member_Name = person.isNull("member_name") ? "" : person
                        .getString("member_name");
                member_PhoneNo = person.isNull("member_phoneno") ? "" : person
                        .getString("member_phoneno");
                member_EmailId = person.isNull("member_email") ? "" : person
                        .getString("member_email");
                member_CountryCode = person.isNull("member_country_code") ? ""
                        : person.getString("member_country_code");

                mSms = new CSms(InviteUserToApp.this);
                mSms.sendSMS(member_PhoneNo,
                        InviteUserToApp.this.getString(R.string.invitesms));

            }

        } catch (Exception e) {
            SSLog.e(TAG, "sendSMS", e);
        }

    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                hideKeyboard();
            }
        }, 500);
        boolean isListEmpty = false;
        String sListValue = MyApplication.getInstance()
                .getPersistentPreference()
                .getString(Constants_user.PREF_INVITE_USER_CURRENT_TO_APP, "");
        if (TextUtils.isEmpty(sListValue)) {
            isListEmpty = true;
        } else {
            Gson gson1 = new Gson();
            contactResults = gson1.fromJson(sListValue,
                    new TypeToken<ArrayList<Contact_Results>>() {
                    }.getType());
            if (contactResults.size() == 0) {
                isListEmpty = true;
            } else {

                gplist.setVisibility(View.VISIBLE);
                ll.setVisibility(View.GONE);
                tv.setVisibility(View.GONE);

                gplist.setAdapter(new Contact_Trip_Adapter(InviteUserToApp.this,
                        contactResults));

            }
        }
        if (isListEmpty) {
            gplist.setVisibility(View.GONE);
            ll.setVisibility(View.VISIBLE);
            tv.setVisibility(View.VISIBLE);
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        try {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager inputManager = (InputMethodManager) this
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                        InputMethodManager.RESULT_HIDDEN);
                inputManager.hideSoftInputFromWindow(
                        view.getApplicationWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void sendMemberInfo(ContactInfo contactInfo) {

    }

    protected void getMemberAlreadyIsInTrip() {

        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.GET_TRIP_MEMBERS_DRIVING_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        checkMemberAlreadyIsInTrip(response);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SSLog.e(TAG, "getMemberAlreadyIsInTrip :-   ", error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();

                params.put("tripid", String.valueOf(tpID));

                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.GET_TRIP_MEMBERS_DRIVING_URL);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }

                String url1 = Constants_user.GET_TRIP_MEMBERS_DRIVING_URL;

                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "getMemberAlreadyIsInTrip", e);
                }
                return CGlobals_user.getInstance().checkParams(params);

            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);

    }

    private void checkMemberAlreadyIsInTrip(String response) {

        if (TextUtils.isEmpty(response) || response.trim().equals("-1")) {
            invitetripmember();
            return;
        }

        JSONObject jResponse;
        try {
            jResponse = new JSONObject(response.trim());
            memberAppuserId = jResponse.isNull("appuser_id") ? "" : jResponse
                    .getString("appuser_id");
            memberPhoneno = jResponse.isNull("member_phoneno") ? "" : jResponse
                    .getString("member_phoneno");
            for (int i = 0; i < contactResults.size(); i++) {
                if (contactResults.get(i).getContactPhone().equals(memberPhoneno)) {
                    Toast.makeText(InviteUserToApp.this, "Member driving " + memberPhoneno, Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e) {
            SSLog.e(TAG, "sendSMS", e);
        }
        invitetripmember();
    }


    private void createOpenTrip() {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.OPEN_TRIP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    SSLog.e(TAG, "createOpenTrip :-   ",
                            error.getMessage());

                } catch (Exception e) {
                    SSLog.e(TAG, "createOpenTrip -  ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("tripid", String.valueOf(tpID));
                params = CGlobals_user.getInstance().getBasicMobileParams(params,
                        Constants_user.OPEN_TRIP_URL,InviteUserToApp.this);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }

                String url1 = Constants_user.OPEN_TRIP_URL;

                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "createOpenTrip", e);
                }
                return CGlobals_user.getInstance().checkParams(params);

            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            LayoutInflater myInflator = getLayoutInflater();
            View myLayout = myInflator.inflate(R.layout.invite_later,
                    (ViewGroup) findViewById(R.id.toastlayout));

            Toast myToast = new Toast(getApplicationContext());
            myToast.setDuration(Toast.LENGTH_LONG);
            myToast.setView(myLayout);
            myToast.show();

            hideKeyboard();
            finish();
            return true;
        }
        return false;
    }

}
