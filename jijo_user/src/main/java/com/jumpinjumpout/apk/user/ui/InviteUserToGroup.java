package com.jumpinjumpout.apk.user.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.jumpinjumpout.apk.user.Contact_Adapter;
import com.jumpinjumpout.apk.user.Contact_Results;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.ContactAccessor;
import lib.app.util.ContactInfo;


public class InviteUserToGroup extends AddContact_act {

    protected static final int PICK_CONTACT_REQUEST = 88;
    ListView dataList;
    protected final ContactAccessor mContactAccessor = ContactAccessor.getInstance();
    protected static String TAG = AddContact_act.class.getSimpleName();
    private ProgressDialog pDialog;
    private static final String group_id_item = "id";
    private static final String group_Name_item = "create_group_name";
    private static final String group_Desc_item = "create_group_desc";
    private static final String group_Type_item = "create_group_type";
    ImageButton findcontact, editfiled;
    ImageView contactimage;
    TextView selectGroupdisp, toaddressdisp, fromaddressdisp, groupnamedisp,
            descGroupdisp, contactname, contactnumber, bar, bar2;
    String contact_name_list, contact_memberId_list, contact_phone_list,
            contact_email_list;
    byte imageInByte[];
    String contact_Name, contact_Image;
    Bitmap bitmap = null;
    String contact_Email = "", memberID;
    String add_Group_ID, add_Group_Name, add_Group_Desc, add_Group_Type;
    private static final String group_NUMBER = "NUMBER";
    private static final String group_id_Edit = "group_id_item";
    private static final String group_Name_Edit = "group_Name_item";
    private static final String group_Desc_Edit = "group_Desc_item";
    private static final String group_Type_Edit = "group_Type_item";
    protected static final int EDIT_GROUP = 3;
    String number, edit_Id, edit_name, edit_desc, edit_type, group_main_ID, edit_desc_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_group_contact);
        selectGroupdisp = (TextView) findViewById(R.id.selectGroupdisp);
        fromaddressdisp = (TextView) findViewById(R.id.fromaddressdisp);
        toaddressdisp = (TextView) findViewById(R.id.toaddressdisp);
        groupnamedisp = (TextView) findViewById(R.id.groupnamedisp);
        descGroupdisp = (TextView) findViewById(R.id.descGroupdisp);
        bar = (TextView) findViewById(R.id.bar);
        bar2 = (TextView) findViewById(R.id.bar2);
        contactname = (TextView) findViewById(R.id.contactname);
        contactnumber = (TextView) findViewById(R.id.contactnumber);
        contactimage = (ImageView) findViewById(R.id.contactimage);
        editfiled = (ImageButton) findViewById(R.id.editfiled);
        dataList = (ListView) findViewById(R.id.contactlist);
        findcontact = (ImageButton) findViewById(R.id.findcontact);
        Button btnshow_all_list = (Button) findViewById(R.id.done);
        Button delete_Group = (Button) findViewById(R.id.delete);
        Intent intent_Group_Edit = getIntent();
        number = intent_Group_Edit.getStringExtra(group_NUMBER);
        if (number != null) {
            if (number.equals(Constants_user.VALUE_TRUE)) {
                editfiled.setVisibility(View.VISIBLE);
                bar.setVisibility(View.VISIBLE);
                findcontact.setVisibility(View.VISIBLE);
                bar2.setVisibility(View.VISIBLE);
                edit_Id = intent_Group_Edit.getStringExtra(group_id_Edit);
                edit_name = intent_Group_Edit.getStringExtra(group_Name_Edit);
                edit_desc = intent_Group_Edit.getStringExtra(group_Desc_Edit);
                edit_type = intent_Group_Edit.getStringExtra(group_Type_Edit);
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
                getMemberDetails();
            }
        } else {
            editfiled.setVisibility(View.INVISIBLE);
            bar.setVisibility(View.INVISIBLE);
            findcontact.setVisibility(View.VISIBLE);
            bar2.setVisibility(View.VISIBLE);
            Intent get_group_intent = getIntent();
            add_Group_ID = get_group_intent.getStringExtra(group_id_item);
            add_Group_Name = get_group_intent.getStringExtra(group_Name_item);
            add_Group_Desc = get_group_intent.getStringExtra(group_Desc_item);
            add_Group_Type = get_group_intent.getStringExtra(group_Type_item);
            selectGroupdisp.setText("TYPE: " + add_Group_Type);
            fromaddressdisp.setVisibility(View.GONE);
            toaddressdisp.setVisibility(View.GONE);
            groupnamedisp.setText(add_Group_Name);
            descGroupdisp.setText(add_Group_Desc);
        }
        findcontact.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivityForResult(mContactAccessor.getPickContactIntent(), PICK_CONTACT_REQUEST);
            }
        });
        editfiled.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent_Edit = new Intent(InviteUserToGroup.this,
                        CommunityCreate_act.class);
                intent_Edit.putExtra("NUMBER", Constants_user.VALUE_TRUE);
                intent_Edit.putExtra("edit_Id", edit_Id);
                intent_Edit.putExtra("edit_name", edit_name);
                intent_Edit.putExtra("edit_desc", edit_desc);
                intent_Edit.putExtra("edit_type", edit_type);
                startActivityForResult(intent_Edit, EDIT_GROUP);
            }
        });
        delete_Group.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (number != null) {
                    if (number.equals(Constants_user.VALUE_TRUE)) {
                        group_main_ID = edit_Id;
                        delete_Group();
                    }
                } else {
                    group_main_ID = add_Group_ID;
                    delete_Group();
                }
            }
        });

        btnshow_all_list.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (number != null) {
                    if (number.equals(Constants_user.VALUE_TRUE)) {
                        group_main_ID = edit_Id;
                        doneCllPhp();
                        finish();
                    }
                } else {
                    group_main_ID = add_Group_ID;
                    doneCllPhp();
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_GROUP) {
            if (resultCode == RESULT_CANCELED) {
                return;
            }
            edit_desc_result = data.getStringExtra("groupdesc_edit_result");
            descGroupdisp.setText(edit_desc_result);
            edit_desc = edit_desc_result;

        }

    }

    // Create sendGroupInfo Function
    public void sendMemberInfo(final ContactInfo contactInfo) {
        if (number != null) {
            if (number.equals(Constants_user.VALUE_TRUE)) {
                group_main_ID = edit_Id;
            }
        } else {
            group_main_ID = add_Group_ID;
        }
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.ADD_COMMUNITY_MEMBER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        getmemberId(response);
                        getMemberDetails();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SSLog.e(TAG, "sendMemberInfo :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants_user.PARAMETER_COMMUNITY_ID, group_main_ID);
                params.put("membername", contactInfo.getDisplayName());
                if (!TextUtils.isEmpty(contactInfo.getPhoneNumber())) {
                    params.put("memberphoneno", contactInfo.getPhoneNumber());
                }
                if (!TextUtils.isEmpty(contactInfo.getEmail())) {
                    params.put("memberemail", contactInfo.getEmail());
                }
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.ADD_COMMUNITY_MEMBER_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.ADD_COMMUNITY_MEMBER_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "sendMemberInfo", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendUpdatePosition

    private void getmemberId(String response) {
        if (TextUtils.isEmpty(response)) {
            return;
        }
        JSONObject jResponse;
        try {
            jResponse = new JSONObject(response);
            memberID = jResponse.getString("member_community_details_id");
            System.out.println("succ" + memberID);
            Contact_Results conmember = new Contact_Results();
            conmember.setMemberID(memberID);
        } catch (JSONException e) {
            SSLog.e(TAG, "getmemberId", e);
        }
    }

    // Create delete_Group Function
    public void delete_Group() {
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.DELETE_COMMUNITY_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        setResult(2);
                        Toast.makeText(getApplicationContext(),
                                "Deleting Community..", Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SSLog.e(TAG, "sendMemberInfo :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants_user.PARAMETER_COMMUNITY_ID, group_main_ID);
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.DELETE_COMMUNITY_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.DELETE_COMMUNITY_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "delete_Group", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // delete_Group

    // Create sendGroupInfo Function
    public void doneCllPhp() {
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.COMMUNITY_DONE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SSLog.e(TAG, "doneCllPhp :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put(Constants_user.PARAMETER_COMMUNITY_ID, group_main_ID);
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.COMMUNITY_DONE_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.COMMUNITY_DONE_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "doneCllPhp", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendUpdatePosition

    public void getMemberDetails() {
        showpDialog();
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.GET_COMMUNITY_MEMBERS_DETAIL_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        getAllMemberDetails(response);
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    hidepDialog();
                    SSLog.e(TAG, "getMemberDetails :-   ",
                            error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "getMemberDetails :-  ", e);
                }
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
    }

    private void getAllMemberDetails(String response) {
        if (TextUtils.isEmpty(response)) {
            return;
        }
        final ArrayList<Contact_Results> results = new ArrayList<Contact_Results>();
        int iMemberAppUserId;
        try {
            JSONArray aJson = new JSONArray(response);
            for (int i = 0; i < aJson.length(); i++) {
                JSONObject person = (JSONObject) aJson.get(i);
                contact_memberId_list = person
                        .getString("member_community_details_id");
                contact_name_list = person.getString("member_name");
                contact_phone_list = person.getString("member_phoneno");
                contact_email_list = person.getString("member_email");
                iMemberAppUserId = person.isNull("appuser_id") ? -1 : Integer
                        .parseInt(person.getString("appuser_id"));
                if (iMemberAppUserId == CGlobals_user.getInstance().getAppUserId()) {
                    contact_name_list = "You";
                    contact_phone_list = "";
                }
                Contact_Results gresuli = new Contact_Results();
                gresuli.setID(edit_Id);
                gresuli.setMemberID(contact_memberId_list);
                gresuli.setContactName(contact_name_list);
                gresuli.setContactPhone(contact_phone_list);
                gresuli.setContactEmail(contact_email_list);
                results.add(gresuli);
            }
            dataList.setAdapter(new Contact_Adapter(
                    InviteUserToGroup.this, results));
            hidepDialog();
        } catch (Exception e) {
            SSLog.e(TAG, "getAllMemberDetails", e);
            hidepDialog();
        }
    }

    private void showpDialog() {
        try {
            if (!isFinishing()) {
                pDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hidepDialog() {
        try {
            if (pDialog != null) {
                if (pDialog.isShowing()) {
                    pDialog.cancel();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideKeyboard();
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

    @Override
    protected void sendInvitePeople(ContactInfo contactInfo) {
    }

    protected void updateList() {
        dataList.setAdapter(new Contact_Adapter(
                InviteUserToGroup.this, contactResults));
    }
}
