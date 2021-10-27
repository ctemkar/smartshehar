package com.jumpinjumpout.apk.user.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.GroupCustomAdapter;
import com.jumpinjumpout.apk.user.GroupResults;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class CommunityShowAll_act extends Activity {

    LinearLayout no_Group_List; // initialize LinearLayout
    ListView all_Group_List; // initialize ListView
    private static String TAG = CommunityShowAll_act.class.getSimpleName();
    // Progress dialog
    private ProgressDialog mProgressDialog;
    String id, name, desc, type, full_Name, mine_flag;
    int active_user_group;
    String user_Name, user_Full_Name, user_Age, user_Gender;
    TextView create_group;// , join_group;
    private int RESULT_UPDATE_USER_DETAILS = 1;
    private int RESULT_ADD_CONTACT = 2;
    private int RESULT_VIEW_GROUP = 3;
    String change_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_all_group);
        all_Group_List = (ListView) findViewById(R.id.group_Name_List);
        no_Group_List = (LinearLayout) findViewById(R.id.no_group_list);
        create_group = (TextView) findViewById(R.id.create_group);
        Intent intentgpinvitation = getIntent();
        change_value = intentgpinvitation
                .getStringExtra(Constants_user.CHANGE_VALUE);
        create_group.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent_create_group = new Intent(
                        CommunityShowAll_act.this, CommunityCreate_act.class);
                startActivity(intent_create_group);
            }
        });
        mProgressDialog = new ProgressDialog(CommunityShowAll_act.this);
        mProgressDialog.setMessage("Please wait");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

    }

    // Create sendGroupInfo Function
    public void getMyGroups() {
        showpDialog();
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.GET_MY_COMMUNITIES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getMyCommunities(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    hidepDialog();
                    Toast.makeText(
                            CommunityShowAll_act.this.getBaseContext(),
                            getString(R.string.retry_internet),
                            Toast.LENGTH_LONG).show();

                    SSLog.e(TAG, "getMyGroups: ErrorListener :-   ",
                            error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "getMyGroups: ErrorListener  -  ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.GET_MY_COMMUNITIES_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.GET_MY_COMMUNITIES_URL;
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
    } // sendUpdatePosition

    private void getMyCommunities(String response) {
        if (TextUtils.isEmpty(response) || response.trim().equals("-1")) {
            hidepDialog();
            all_Group_List.setVisibility(View.GONE);
            no_Group_List.setVisibility(View.VISIBLE);
            return;
        }
        final ArrayList<GroupResults> results = new ArrayList<GroupResults>();
        try {
            JSONArray aJson = new JSONArray(response);
            all_Group_List.setVisibility(View.VISIBLE);
            no_Group_List.setVisibility(View.GONE);
            for (int i = 0; i < aJson.length(); i++) {
                JSONObject person = (JSONObject) aJson.get(i);
                id = person.getString(Constants_user.COL_COMMUNITY_ID);
                name = person.isNull("community_name") ? "" : person
                        .getString("community_name");
                desc = person.isNull("community_desc") ? "" : person
                        .getString("community_desc");
                type = person.isNull("community_type") ? "" : person
                        .getString("community_type");
                full_Name = person.isNull("fullname") ? "" : person
                        .getString("fullname");
                mine_flag = person.isNull("mine") ? "0" : person
                        .getString("mine");
                active_user_group = person.isNull("silent") ? 0 : person
                        .getInt("silent");
                MyApplication.getInstance().getPersistentPreferenceEditor().putInt(Constants_user.PERF_GROUP_NOTIFICATION, active_user_group);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                GroupResults gresuli = new GroupResults();
                gresuli.setID(id);
                gresuli.setGroupName(name);
                gresuli.setGroupDesc(desc);
                gresuli.setGroupType(type);
                gresuli.setFullName(full_Name);
                gresuli.setMine(mine_flag);
                gresuli.setActive(String.valueOf(active_user_group));
                results.add(gresuli);
            }
            all_Group_List.setAdapter(new GroupCustomAdapter(
                    CommunityShowAll_act.this, results));
            hidepDialog();
            all_Group_List.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    String group_id_item = results.get(position).getID();
                    String group_Name_item = results.get(position)
                            .getGroupName();
                    String group_Desc_item = results.get(position)
                            .getGroupDesc();
                    String group_Type_item = results.get(position)
                            .getGroupType();
                    String group_mine = results.get(position).getMine();
                    String user_Active_Group = results.get(position)
                            .getActive();
                    if (change_value != null) {
                        if (change_value.equals(Constants_user.VALUE_TRUE)) {
                            Intent intent_GPI = new Intent();
                            intent_GPI.putExtra("group_id_item", group_id_item);
                            intent_GPI.putExtra("group_Name_item",
                                    group_Name_item);
                            intent_GPI.putExtra("group_Desc_item",
                                    group_Desc_item);
                            intent_GPI.putExtra("group_Type_item",
                                    group_Type_item);
                            setResult(Activity.RESULT_OK, intent_GPI);
                            finish();
                        }
                    } else {
                        if (group_mine.equals(Constants_user.VALUE_TRUE)) {

                            Intent intent_contact_show = new Intent(
                                    CommunityShowAll_act.this,
                                    InviteUserToGroup.class);
                            intent_contact_show.putExtra("NUMBER",
                                    Constants_user.VALUE_TRUE);
                            intent_contact_show.putExtra("group_id_item",
                                    group_id_item);
                            intent_contact_show.putExtra("group_Name_item",
                                    group_Name_item);
                            intent_contact_show.putExtra("group_Desc_item",
                                    group_Desc_item);
                            intent_contact_show.putExtra("group_Type_item",
                                    group_Type_item);

                            startActivityForResult(intent_contact_show,
                                    RESULT_ADD_CONTACT);
                        } else {
                            Intent intent_contact_show = new Intent(
                                    CommunityShowAll_act.this,
                                    ViewCommunity_act.class);
                            intent_contact_show.putExtra("group_id_item",
                                    group_id_item);
                            intent_contact_show.putExtra("group_Name_item",
                                    group_Name_item);
                            intent_contact_show.putExtra("group_Desc_item",
                                    group_Desc_item);
                            intent_contact_show.putExtra("group_Type_item",
                                    group_Type_item);
                            intent_contact_show.putExtra("user_Active_Group",
                                    user_Active_Group);
                            startActivityForResult(intent_contact_show,
                                    RESULT_VIEW_GROUP);
                        }
                    }
                }
            });

        } catch (Exception e) {
            SSLog.e(TAG, "getMyGroups", e);
            hidepDialog();
            all_Group_List.setVisibility(View.GONE);
            no_Group_List.setVisibility(View.VISIBLE);
        }
    }

    // Create sendGroupInfo Function
    public void getUserProfile() {
        showpDialog();
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.GET_USER_PROFILE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getMyProfile(response);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidepDialog();
                Toast.makeText(
                        CommunityShowAll_act.this.getBaseContext(),
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
    } // sendUpdatePosition

    private void getMyProfile(String response) {
        response = response.trim();
        if (response.trim().equals("-1")) {
            hidepDialog();
            return;
        }

        System.out.println("getMyProfile" + response);
        JSONObject person;
        try {
            person = new JSONObject(response);
            user_Name = person.isNull("username") ? "" : person
                    .getString("username");
            user_Full_Name = person.isNull("fullname") ? "" : person
                    .getString("fullname");
            user_Age = person.isNull("age") ? "" : person.getString("age");
            user_Gender = person.isNull("sex") ? "" : person.getString("sex");
            System.out.println(user_Name);
            if (TextUtils.isEmpty(user_Name)) {
                Toast.makeText(getApplicationContext(),
                        R.string.createUserProfile, Toast.LENGTH_LONG).show();
                Intent intent_User_Result = new Intent(
                        CommunityShowAll_act.this, UpdateUserDetails_act.class);
                startActivityForResult(intent_User_Result,
                        RESULT_UPDATE_USER_DETAILS);
            } else {
                getMyGroups();
            }
        } catch (Exception e) {
            SSLog.e(TAG, "getMyProfile", e);
            hidepDialog();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed here it is 2
        if (requestCode == RESULT_UPDATE_USER_DETAILS) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(
                        getApplicationContext(),
                        "You need to create a user profile to use this feature",
                        Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (resultCode <= 0) {
                getUserProfile();
            } else {
                getMyGroups();
            }
        } else {
            getMyGroups();
        }
    }

    private void showpDialog() {
        if (!isFinishing()) {
            mProgressDialog.setMessage("Please wait ...");
            mProgressDialog.show();
        }
    }

    private void hidepDialog() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }
    }

    @Override
    protected void onResume() {
        getUserProfile();
        hidepDialog();
        hideKeyboard();
        super.onResume();
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.RESULT_HIDDEN);
        }
    }

}
