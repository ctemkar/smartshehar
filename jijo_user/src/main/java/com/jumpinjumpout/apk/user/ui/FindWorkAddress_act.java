package com.jumpinjumpout.apk.user.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.jumpinjumpout.apk.user.FindWorkAddress_Adapter;
import com.jumpinjumpout.apk.user.FindWorkAddress_Result;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by user pc on 25-09-2015.
 */
public class FindWorkAddress_act extends Fragment {

    protected static final String TAG = "FindWorkAddress_act: ";
    private int iMAppUser;
    private String sMUserName, sMFullName, sMEmail, sMPhoneNo;
    final ArrayList<FindWorkAddress_Result> results = new ArrayList<FindWorkAddress_Result>();
    private ListView listView;
    private TextView tvselectall;
    private Button btnAddFriend;
    private ProgressDialog pDialog;
    FindWorkAddress_Adapter boxAdapter;
    public static final String ARG_OBJECT = "object";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.findworkaddress_act, container, false);
        Bundle args = getArguments();
        String i = Integer.toString(args.getInt(ARG_OBJECT));
        listView = (ListView) rootView.findViewById(R.id.listView);
        tvselectall = (TextView) rootView.findViewById(R.id.tvselectall);
        btnAddFriend = (Button) rootView.findViewById(R.id.btnAddFriend);
        getcommonhomework();
        boxAdapter = new FindWorkAddress_Adapter(getActivity(), results);
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        tvselectall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < results.size(); i++) {
                    listView.setItemChecked(i, true);
                }
            }
        });

        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Add Friend")
                        .setMessage("Are you sure you want to send request?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String result = "";
                                for (FindWorkAddress_Result p : boxAdapter.getBox()) {
                                    if (p.box) {
                                        result = String.valueOf(p.getiAppUserId());
                                        sendFriendRequest(result);
                                        Log.i("jijo", result);
                                    }
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        return rootView;
    }

    public void getcommonhomework() {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.COMMON_HOME_WORK_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        getMatchUser(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(
                        getActivity().getBaseContext(),
                        getString(R.string.retry_internet),
                        Toast.LENGTH_LONG).show();

                SSLog.e(TAG, "checkUserName :-   ",
                        error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.COMMON_HOME_WORK_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url = Constants_user.COMMON_HOME_WORK_URL;
                try {
                    url = url + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "checkUserName", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    public void getMatchUser(String response) {
        if (TextUtils.isEmpty(response)) {
            return;
        }
        if (response.equals("-1")) {
            return;
        }
        try {
            JSONArray aJson = new JSONArray(response);
            for (int i = 0; i < aJson.length(); i++) {
                JSONObject mjTrip = (JSONObject) aJson.get(i);
                iMAppUser = isNullNotDefined(mjTrip, "appuser_id") ? -1
                        : mjTrip.getInt("appuser_id");
                sMUserName = isNullNotDefined(mjTrip, "username") ? ""
                        : mjTrip.getString("username");
                sMFullName = isNullNotDefined(mjTrip, "fullname") ? ""
                        : mjTrip.getString("fullname");
                sMEmail = isNullNotDefined(mjTrip, "email") ? ""
                        : mjTrip.getString("email");
                sMPhoneNo = isNullNotDefined(mjTrip, "phoneno") ? ""
                        : mjTrip.getString("phoneno");
                if (TextUtils.isEmpty(sMFullName)) {
                    continue;
                }
                FindWorkAddress_Result findWorkAddress_result = new FindWorkAddress_Result();
                findWorkAddress_result.setiAppUserId(iMAppUser);
                findWorkAddress_result.setsUserName(sMUserName);
                findWorkAddress_result.setsFullName(sMFullName);
                findWorkAddress_result.setsEmail(sMEmail);
                findWorkAddress_result.setsPhoneNumber(sMPhoneNo);
                results.add(findWorkAddress_result);
            }
            listView.setAdapter(new FindWorkAddress_Adapter(
                    getActivity(), results));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFriendRequest(final String fromAppUserId) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.INSERT_FRIEND_DATA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(
                        getActivity(),
                        "Cannot connect to the internet.\nPlease check your connection and try again",
                        Toast.LENGTH_LONG).show();

                SSLog.e(TAG, "checkUserName :-   ",
                        error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("toappuserid", fromAppUserId);
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.INSERT_FRIEND_DATA_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url = Constants_user.INSERT_FRIEND_DATA_URL;
                try {
                    url = url + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "checkUserName", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    public boolean isNullNotDefined(JSONObject jo, String jkey) {
        if (!jo.has(jkey)) {
            return true;
        }
        if (jo.isNull(jkey)) {
            return true;
        }
        return false;
    }
}
