package com.jumpinjumpout.apk.user.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
import com.jumpinjumpout.apk.user.MyApplication;
import com.jumpinjumpout.apk.user.ViewAdapter;
import com.jumpinjumpout.apk.user.View_Contact_Results;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ViewCommunity_act extends Activity {

    ListView dataList;
    private static String TAG = ViewCommunity_act.class.getSimpleName();
    private ProgressDialog pDialog;
    TextView selectGroupdisp, toaddressdisp, fromaddressdisp, groupnamedisp,
            descGroupdisp, text;
    private static final String group_id_Edit = "group_id_item";
    private static final String group_Name_Edit = "group_Name_item";
    private static final String group_Desc_Edit = "group_Desc_item";
    private static final String group_Type_Edit = "group_Type_item";
    private static final String group_Active_Edit = "user_Active_Group";
    String edit_Id, edit_name, edit_desc, edit_type, edit_Active;
    String contact_name_list, contact_phone_list, contact_email_list;
    int contact_memberId_list, mMemberGroupDetailsId;
    Button leave, ok;
    TextView unmute, mute;
    String value_mute_unmute, view_Phone_No, view_Email_Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_me_invited_group);

        selectGroupdisp = (TextView) findViewById(R.id.selectGroupdisp);
        fromaddressdisp = (TextView) findViewById(R.id.fromaddressdisp);
        toaddressdisp = (TextView) findViewById(R.id.toaddressdisp);
        groupnamedisp = (TextView) findViewById(R.id.groupnamedisp);
        descGroupdisp = (TextView) findViewById(R.id.descGroupdisp);
        dataList = (ListView) findViewById(R.id.contactlist);
        ok = (Button) findViewById(R.id.ok);
        leave = (Button) findViewById(R.id.leave);
        text = (TextView) findViewById(R.id.text);
        unmute = (TextView) findViewById(R.id.unmute);
        mute = (TextView) findViewById(R.id.mute);
        Intent intent_Group_Edit = getIntent();
        edit_Id = intent_Group_Edit.getStringExtra(group_id_Edit);
        edit_name = intent_Group_Edit.getStringExtra(group_Name_Edit);
        edit_desc = intent_Group_Edit.getStringExtra(group_Desc_Edit);
        edit_type = intent_Group_Edit.getStringExtra(group_Type_Edit);
        edit_Active = intent_Group_Edit.getStringExtra(group_Active_Edit);
        if (edit_Active.equals(Constants_user.VALUE_TRUE)) {
            unmute.setVisibility(View.VISIBLE);
            text.setText(R.string.unmute_text);
        } else if (edit_Active.equals(Constants_user.VALUE_FLASE)) {
            mute.setVisibility(View.VISIBLE);
            text.setText(R.string.mute_text);
        }
        if (edit_type.equals("P")) {
            selectGroupdisp.setText("Private Community");
        }
        fromaddressdisp.setVisibility(View.GONE);
        toaddressdisp.setVisibility(View.GONE);
        groupnamedisp.setText(edit_name);
        descGroupdisp.setText(edit_desc);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        sendGroupInfo();
        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setResult(3);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                finish();

            }
        });
        leave.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                View_Contact_Results gresuli1 = new View_Contact_Results();
                view_Phone_No = gresuli1.getContactPhone();
                view_Email_Id = gresuli1.getContactEmail();
                sendLeaveGroup();
                setResult(3);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                finish();
            }

        });

        mute.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                value_mute_unmute = Constants_user.VALUE_TRUE;
                sendMuteGroup();
                setResult(3);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                Toast.makeText(getApplicationContext(),
                        getApplicationContext().getString(R.string.mute_toast),
                        Toast.LENGTH_SHORT).show();
                finish();

            }

        });

        unmute.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                value_mute_unmute = Constants_user.VALUE_FLASE;
                sendMuteGroup();
                setResult(3);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
                Toast.makeText(
                        getApplicationContext(),
                        getApplicationContext()
                                .getString(R.string.unmute_toast),
                        Toast.LENGTH_SHORT).show();
                finish();

            }

        });

    }

    // Create sendGroupInfo Function
    public void sendGroupInfo() {
        showpDialog();
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.GET_COMMUNITY_MEMBERS_DETAIL_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getMyCommunitiesView(response);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidepDialog();
                Toast.makeText(
                        ViewCommunity_act.this.getBaseContext(),
                        getString(R.string.retry_internet),
                        Toast.LENGTH_LONG).show();
                SSLog.e(TAG, "sendGroupInfo :-   ", error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();

                params.put(Constants_user.PARAMETER_COMMUNITY_ID, edit_Id);

                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.GET_COMMUNITY_MEMBERS_DETAIL_URL);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.GET_COMMUNITY_MEMBERS_DETAIL_URL;
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

    private void getMyCommunitiesView(String response) {
        System.out.println("succ" + response.toString());
        if (TextUtils.isEmpty(response) && response.equals("-1")) {
            return;
        }
        final ArrayList<View_Contact_Results> results = new ArrayList<View_Contact_Results>();
        int iMemberAppUserId;
        try {
            JSONArray aJson = new JSONArray(response);
            for (int i = 0; i < aJson.length(); i++) {
                JSONObject person = (JSONObject) aJson.get(i);
                contact_name_list = person.getString("member_name");
                contact_phone_list = person.getString("member_phoneno");
                contact_email_list = person.getString("member_email");
                contact_memberId_list = Integer.parseInt(person
                        .getString("member_community_details_id"));
                iMemberAppUserId = person.isNull("appuser_id") ? -1 : Integer
                        .parseInt(person.getString("appuser_id"));
                if (iMemberAppUserId > 0) {
                    if (CGlobals_user.getInstance().getAppUserId() == iMemberAppUserId) {
                        mMemberGroupDetailsId = contact_memberId_list;
                    }
                }
                if (iMemberAppUserId == CGlobals_user.getInstance().getAppUserId()) {

                    contact_name_list = "You";
                    contact_phone_list = "";
                }
                View_Contact_Results gresuli = new View_Contact_Results();
                gresuli.setID(edit_Id);
                gresuli.setContactName(contact_name_list);
                gresuli.setContactPhone(contact_phone_list);
                gresuli.setContactEmail(contact_email_list);
                gresuli.setMemberID(contact_memberId_list);
                results.add(gresuli);
            }
            dataList.setAdapter(new ViewAdapter(ViewCommunity_act.this, results));
            hidepDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
        hidepDialog();
    }
    private void sendMuteGroup() {

        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.COMMUNITY_MEMBER_ACTIVE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SSLog.e(TAG, "sendMuteGroup :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();

                params.put(Constants_user.PARAMETER_COMMUNITY_ID, edit_Id);

                params.put("memberdetailid",
                        String.valueOf(mMemberGroupDetailsId));

                params.put("silent", value_mute_unmute);

                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.COMMUNITY_MEMBER_ACTIVE_URL);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }
                String url1 = Constants_user.COMMUNITY_MEMBER_ACTIVE_URL;

                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "leaveGroup", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void sendLeaveGroup() {
        final// Send Data to Server Database Using Volley Library
                StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.DELETE_MEMBER_WITH_APPUSERID_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SSLog.e(TAG, "sendLeaveGroup :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();

                params.put(Constants_user.PARAMETER_COMMUNITY_ID, edit_Id);

                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.DELETE_MEMBER_WITH_APPUSERID_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }

                String url1 = Constants_user.DELETE_MEMBER_WITH_APPUSERID_URL;

                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "Contact Ddelete", e);
                }
                return CGlobals_user.getInstance().checkParams(params);

            }

        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void showpDialog() {
        if (!isFinishing()) {
            pDialog.show();
        }
    }

    private void hidepDialog() {
        if (pDialog != null) {
            if (pDialog.isShowing()) {
                pDialog.cancel();
            }
        }
    }

}
