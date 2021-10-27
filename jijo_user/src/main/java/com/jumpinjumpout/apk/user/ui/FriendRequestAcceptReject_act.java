package com.jumpinjumpout.apk.user.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.FriendRequestAcceptReject_Adapter;
import com.jumpinjumpout.apk.user.FriendRequestAcceptReject_Result;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by user pc on 05-10-2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FriendRequestAcceptReject_act extends Fragment {

    protected static final String TAG = "FriendRequestAcceptReject_act: ";
    public static final String ARG_OBJECT = "object";
    private ListView lvFriendRequestList;
    private ProgressDialog pDialog;
    private int miFriendId, miAppUser_id_ME, miAppUser_id_Friend;
    private String sUserName, sFullName;
    final ArrayList<FriendRequestAcceptReject_Result> results = new ArrayList<FriendRequestAcceptReject_Result>();

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.friendrequestacceptreject_act, container, false);
        lvFriendRequestList = (ListView) rootView.findViewById(R.id.lvFriendRequestList);
        Bundle args = getArguments();
        String i = Integer.toString(args.getInt(ARG_OBJECT));
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        lvFriendRequestList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Friend Request")
                        .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                getAcceptReject(results.get(position).getiFriendid(), 1, 0);
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                getAcceptReject(results.get(position).getiFriendid(), 0, 1);
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getFriendRequest();
    }

    public void getFriendRequest() {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.GET_FRIEND_PENDING_REQUEST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        getAllFriend(response);
                    }
                }, new Response.ErrorListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(
                        getActivity(),
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
                        Constants_user.GET_FRIEND_PENDING_REQUEST_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url = Constants_user.GET_FRIEND_PENDING_REQUEST_URL;
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void getAllFriend(String response) {
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
                miFriendId = isNullNotDefined(mjTrip, "friends_id") ? -1
                        : mjTrip.getInt("friends_id");
                miAppUser_id_ME = isNullNotDefined(mjTrip, "appuser_id_me") ? -1
                        : mjTrip.getInt("appuser_id_me");
                miAppUser_id_Friend = isNullNotDefined(mjTrip, "appuser_id_friend") ? -1
                        : mjTrip.getInt("appuser_id_friend");
                sUserName = isNullNotDefined(mjTrip, "username") ? ""
                        : mjTrip.getString("username");
                sFullName = isNullNotDefined(mjTrip, "fullname") ? ""
                        : mjTrip.getString("fullname");
                FriendRequestAcceptReject_Result requestAcceptReject_result = new FriendRequestAcceptReject_Result();
                requestAcceptReject_result.setiFriendid(miFriendId);
                requestAcceptReject_result.setiAppuserIdMe(miAppUser_id_ME);
                requestAcceptReject_result.setiAppuserIdFriend(miAppUser_id_Friend);
                requestAcceptReject_result.setsUserName(sUserName);
                requestAcceptReject_result.setsFullName(sFullName);
                results.add(requestAcceptReject_result);
            }
            lvFriendRequestList.setAdapter(new FriendRequestAcceptReject_Adapter(
                    getActivity(), results));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAcceptReject(final int iFrindid, final int iAccept, final int iReject) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.UPDATE_HOME_WORK_PENDING_REQUEST_URL,
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
                params.put("friendsid", String.valueOf(iFrindid));
                params.put("accepted", String.valueOf(iAccept));
                params.put("rejected", String.valueOf(iReject));
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.UPDATE_HOME_WORK_PENDING_REQUEST_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url = Constants_user.UPDATE_HOME_WORK_PENDING_REQUEST_URL;
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
