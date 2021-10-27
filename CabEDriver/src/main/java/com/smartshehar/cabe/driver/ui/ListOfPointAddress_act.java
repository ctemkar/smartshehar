package com.smartshehar.cabe.driver.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.smartshehar.cabe.driver.CTrip_CED;
import com.smartshehar.cabe.driver.Constants_CED;
import com.smartshehar.cabe.driver.DatabaseHandler_CabE;
import com.smartshehar.cabe.driver.Fixed_Address;
import com.smartshehar.cabe.driver.R;
import com.smartshehar.cabe.driver.ShareAddressList_Adapter;

import org.json.JSONArray;

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
    ArrayList<Fixed_Address> arrayList;
    CTrip_CED cTripced;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listofaddress_act);
        lvPointAddress = (ListView) findViewById(R.id.lvPointAddress);
        arrayList = new ArrayList<>();
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
            DatabaseHandler_CabE db = new DatabaseHandler_CabE(ListOfPointAddress_act.this);
            arrayList = db.getFixedAddress();
            if (arrayList.size() == 0) {
                getShareListAddress();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseHandler_CabE db = new DatabaseHandler_CabE(ListOfPointAddress_act.this);
        arrayList = db.getFixedAddress();
        if (arrayList.size() > 0) {
            lvPointAddress.setAdapter(new ShareAddressList_Adapter(ListOfPointAddress_act.this, arrayList));
        } else {
            getShareListAddress2();
        }
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
    }

    private void getShareListAddress() {
        final String url = Constants_CED.GET_FIXED_ADDRESS_URL;
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
        try {
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    cTripced = new CTrip_CED(jsonArray.getJSONObject(i).toString(), ListOfPointAddress_act.this);
                    DatabaseHandler_CabE db = new DatabaseHandler_CabE(ListOfPointAddress_act.this);
                    db.addFixedAddress(String.valueOf(cTripced.getFixed_Address_id()), cTripced.getArea_Fixed(),
                            cTripced.getLandmark_Fixed(), cTripced.getPick_Drop_Point_Fixed(),
                            cTripced.getFormatted_Address_Fixed(), cTripced.getLocality_Fixed(),
                            cTripced.getSublocality_Fixed(), cTripced.getPostal_Code_Fixed(),
                            cTripced.getRoute_Fixed(), cTripced.getNeighborhood_Fixed(),
                            cTripced.getAdministrative_Area_Level_2_Fixed(), cTripced.getAdministrative_Area_Level_1_Fixed(),
                            cTripced.getLatitude_Fixed(), cTripced.getLongitude_Fixed());
                    // arrayList.add(cTripced);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //lvPointAddress.setAdapter(new ShareAddressList_Adapter(ListOfPointAddress_act.this, arrayList));
        } catch (Exception e) {
            SSLog_SS.e(TAG, "getVehicleAllDtetails", e, ListOfPointAddress_act.this);
        }
    }

    private void getShareListAddress2() {
        final String url = Constants_CED.GET_FIXED_ADDRESS_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                getShareListAddressresult2(response);
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

    private void getShareListAddressresult2(String response) {
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    cTripced = new CTrip_CED(jsonArray.getJSONObject(i).toString(), ListOfPointAddress_act.this);
                    Fixed_Address fixed_address = new Fixed_Address();
                    fixed_address.setFixed_address_id(String.valueOf(cTripced.getFixed_Address_id()));
                    fixed_address.setArea(cTripced.getArea_Fixed());
                    fixed_address.setLandmark(cTripced.getLandmark_Fixed());
                    fixed_address.setPick_Drop_Point(cTripced.getPick_Drop_Point_Fixed());
                    fixed_address.setFormatted_Address(cTripced.getFormatted_Address_Fixed());
                    fixed_address.setLocality(cTripced.getLocality_Fixed());
                    fixed_address.setSublocality(cTripced.getSublocality_Fixed());
                    fixed_address.setPostal_code(cTripced.getPostal_Code_Fixed());
                    fixed_address.setRoute(cTripced.getRoute_Fixed());
                    fixed_address.setNeighborhood(cTripced.getNeighborhood_Fixed());
                    fixed_address.setAdministrative_area_level_2(cTripced.getAdministrative_Area_Level_2_Fixed());
                    fixed_address.setAdministrative_area_level_1(cTripced.getAdministrative_Area_Level_1_Fixed());
                    fixed_address.setLatitude(cTripced.getLatitude_Fixed());
                    fixed_address.setLongitude(cTripced.getLongitude_Fixed());
                    arrayList.add(fixed_address);
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
