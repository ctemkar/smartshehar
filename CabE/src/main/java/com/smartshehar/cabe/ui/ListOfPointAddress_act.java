package com.smartshehar.cabe.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.smartshehar.cabe.CTripCabE;
import com.smartshehar.cabe.Constants_CabE;
import com.smartshehar.cabe.R;
import com.smartshehar.cabe.ShareAddressList_Adapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * show this activity fixed route address list come to server
 * Created by soumen on 24-09-2016.
 */

public class ListOfPointAddress_act extends AppCompatActivity {

    private static final String TAG = "ListAddress_act: ";
    ListView lvPointAddress;
    ArrayList<CTripCabE> arrayList;
    CTripCabE cTripced;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listofaddress_act);
        lvPointAddress = (ListView) findViewById(R.id.lvPointAddress);
        lvPointAddress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String json = new Gson().toJson(arrayList.get(position));
                Intent returnIntent = new Intent();
                returnIntent.putExtra("add", json);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
        boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(ListOfPointAddress_act.this);
        if (!isInternetCheck) {
            getShareListAddress();
        } else {
            Toast.makeText(ListOfPointAddress_act.this, getString(R.string.retry_internet), Toast.LENGTH_LONG).show();
        }
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
    }

    private void getShareListAddress() {
        final String url = Constants_CabE.GET_FIXED_ADDRESS_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                getShareListAddressresult(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "sendFlag :-   ",
                            error, ListOfPointAddress_act.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params, url,
                        ListOfPointAddress_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString();
                    Log.e(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getPassengers map - ", e, ListOfPointAddress_act.this);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, ListOfPointAddress_act.this);
    }

    private void getShareListAddressresult(String response) {
        JSONArray jsonArray;
        arrayList = new ArrayList<>();
        try {
            jsonArray = new JSONArray(response);
            arrayList.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    cTripced = new CTripCabE(jsonArray.getJSONObject(i).toString(), ListOfPointAddress_act.this);
                    arrayList.add(cTripced);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            lvPointAddress.setAdapter(new ShareAddressList_Adapter(ListOfPointAddress_act.this, arrayList));
        } catch (Exception e) {
            SSLog_SS.e(TAG, "getVehicleAllDtetails", e, ListOfPointAddress_act.this);
        }
    }
}
