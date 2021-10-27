package com.smartshehar.cabe.driver;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by soumen on 07-11-2016.
 * my service all time running
 */

public class MyService_CED extends Service {

    private static final String TAG = "MyService_CED: ";
    protected Handler handler = new Handler();
    int mStartMode;
    IBinder mBinder;
    boolean mAllowRebind;

    public MyService_CED() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
    }

    @Override
    public void onCreate() {
        handler.postDelayed(runnableService, 0);
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return mStartMode;
    }

    @Override
    public void onDestroy() {
    }

    protected Runnable runnableService = new Runnable() {
        @Override
        public void run() {
            String sServerUrl = "";
            boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(MyService_CED.this);
            DatabaseHandler_CabE db = new DatabaseHandler_CabE(MyService_CED.this);
            ArrayList<MissingLocation_CabE> pPResultsList = db.getSentToServer();
            if (pPResultsList != null) {
                if (pPResultsList.size() > 0) {
                    if (!isInternetCheck) {
                        CGlobals_CED.getInstance().sendMissingLatLon(pPResultsList, MyService_CED.this);
                    }
                }
            }
            ArrayList<CabEDriver_params> cabEDriver_paramses = db.sentDataToServer();
            if (!isInternetCheck) {
                if (cabEDriver_paramses.size() > 0) {
                    if (cabEDriver_paramses.get(0).getPhp_name_code().equals("TAD")) {
                        sServerUrl = Constants_CED.TRIP_ACTION_DRIVER_URL;
                    } else if (cabEDriver_paramses.get(0).getPhp_name_code().equals("SFH")) {
                        sServerUrl = Constants_CED.SET_FOR_HIRE_URL;
                    } else if (cabEDriver_paramses.get(0).getPhp_name_code().equals("DES")) {
                        sServerUrl = Constants_CED.DRIVER_END_SHIFT_URL;
                    } else if (cabEDriver_paramses.get(0).getPhp_name_code().equals("CTD")) {
                        sServerUrl = Constants_CED.CANCEL_TRIP_DRIVER_URL;
                    } else if (cabEDriver_paramses.get(0).getPhp_name_code().equals("TCD")) {
                        sServerUrl = Constants_CED.TRIP_CREATE_DRIVER_URL;
                    } else if (cabEDriver_paramses.get(0).getPhp_name_code().equals("UTC")) {
                        sServerUrl = Constants_CED.UPDATE_TRIP_COST_URL;
                    } else if (cabEDriver_paramses.get(0).getPhp_name_code().equals("GTP")) {
                        sServerUrl = Constants_CED.GET_TRIP_PATH_URL;
                    } else if (cabEDriver_paramses.get(0).getPhp_name_code().equals("CTP")) {
                        sServerUrl = Constants_CED.CREATE_TRIP_PATH_URL;
                    } else if (cabEDriver_paramses.get(0).getPhp_name_code().equals("JI")) {
                        sServerUrl = Constants_CED.JUMP_IN_URL;
                    }
                    if (!TextUtils.isEmpty(sServerUrl)) {
                        sendTripAction(cabEDriver_paramses, sServerUrl);
                    }
                }
            }
            handler.postDelayed(runnableService,
                    Constants_CED.DRIVER_ACTION_SEND);
        }
    };

    public void sendTripAction(final ArrayList<CabEDriver_params> cabEDriver_paramses, final String sUrl) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                sUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                DatabaseHandler_CabE db = new DatabaseHandler_CabE(MyService_CED.this);
                db.deleteRowLocalTable(cabEDriver_paramses.get(0).getID());
                if (TextUtils.isEmpty(response) || response.equals("-1")) {
                    sendMessage(response, 0);
                    return;
                }
                if (cabEDriver_paramses.get(0).getPhp_name_code().equals("TCD")) {
                    sendMessage(response, 2);
                } else if (cabEDriver_paramses.get(0).getPhp_name_code().equals("GTP")) {
                    sendMessage(response, 3);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sendMessage("", 0);
                // error
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "sendTripAction :-   ", error, MyService_CED.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                if (cabEDriver_paramses.get(0).getTrip_id() != 0) {
                    params.put("tripid", String.valueOf(cabEDriver_paramses.get(0).getTrip_id()));
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getTrip_type())) {
                    params.put("triptype", cabEDriver_paramses.get(0).getTrip_type());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getTrip_action())) {
                    params.put("tripaction", cabEDriver_paramses.get(0).getTrip_action());
                }
                if (cabEDriver_paramses.get(0).getAppuser_id() != 0) {
                    params.put("i", String.valueOf(cabEDriver_paramses.get(0).getAppuser_id()));
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getEmail_id())) {
                    params.put("e", cabEDriver_paramses.get(0).getEmail_id());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getCurrent_datetime())) {
                    params.put("ct", cabEDriver_paramses.get(0).getCurrent_datetime());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getProvider())) {
                    params.put("p", cabEDriver_paramses.get(0).getProvider());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getLat())) {
                    params.put("l", cabEDriver_paramses.get(0).getLat());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getLng())) {
                    params.put("o", cabEDriver_paramses.get(0).getLng());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getAccuracy())) {
                    params.put("a", cabEDriver_paramses.get(0).getAccuracy());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getAltitude())) {
                    params.put("al", cabEDriver_paramses.get(0).getAltitude());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getBearing())) {
                    params.put("b", cabEDriver_paramses.get(0).getBearing());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getSpeed())) {
                    params.put("s", cabEDriver_paramses.get(0).getSpeed());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getLoctime())) {
                    params.put("lt", cabEDriver_paramses.get(0).getLoctime());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getImei())) {
                    params.put("imei", cabEDriver_paramses.get(0).getImei());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getShifid())) {
                    params.put("shiftid", cabEDriver_paramses.get(0).getShifid());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getVehicle_id())) {
                    params.put("vehicleid", cabEDriver_paramses.get(0).getVehicle_id());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getIs_for_hire())) {
                    params.put("isforhire", cabEDriver_paramses.get(0).getIs_for_hire());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getSave_datetime())) {
                    params.put("fhdt", cabEDriver_paramses.get(0).getSave_datetime());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getCancel_reason_code())) {
                    params.put("cancelreasoncode", cabEDriver_paramses.get(0).getCancel_reason_code());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getFrom_address())) {
                    params.put("fromaddress", cabEDriver_paramses.get(0).getFrom_address());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getFrom_sublocality())) {
                    params.put("fromsublocality", cabEDriver_paramses.get(0).getFrom_sublocality());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getFrom_lat())) {
                    params.put("fromlat", cabEDriver_paramses.get(0).getFrom_lat());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getFrom_lng())) {
                    params.put("fromlng", cabEDriver_paramses.get(0).getFrom_lng());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getTo_address())) {
                    params.put("toaddress", cabEDriver_paramses.get(0).getTo_address());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getTo_sublocality())) {
                    params.put("tosublocality", cabEDriver_paramses.get(0).getTo_sublocality());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getTo_lat())) {
                    params.put("tolat", cabEDriver_paramses.get(0).getTo_lat());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getTo_lng())) {
                    params.put("tolng", cabEDriver_paramses.get(0).getTo_lng());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getPlanned_start_datetime())) {
                    params.put("plannedstartdatetime", cabEDriver_paramses.get(0).getPlanned_start_datetime());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getTrip_cost())) {
                    params.put("tripcost", cabEDriver_paramses.get(0).getTrip_cost());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getTrip_distance())) {
                    params.put("tripdistance", cabEDriver_paramses.get(0).getTrip_distance());
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getTrip_path())) {
                    params.put("path", cabEDriver_paramses.get(0).getTrip_path());
                }
                if (cabEDriver_paramses.get(0).getPassenger_appuser_id() != 0) {
                    params.put("passengerappuserid", String.valueOf(cabEDriver_paramses.get(0).getPassenger_appuser_id()));
                }
                if (!TextUtils.isEmpty(cabEDriver_paramses.get(0).getJump_inout())) {
                    if (cabEDriver_paramses.get(0).getJump_inout().equals("jumpin")) {
                        params.put("jumpin", String.valueOf(1));
                    } else if (cabEDriver_paramses.get(0).getJump_inout().equals("jumpout")) {
                        params.put("jumpout", String.valueOf(1));
                    }
                }
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                try {
                    String url1 = sUrl + "?" + getParams.toString();
                    Log.d(TAG, url1);
                    SSLog_SS.d(TAG, url1);
                    System.out.println("url1 My Service : " + url1);
                } catch (Exception e) {
                    Log.e(TAG, Arrays.toString(e.getStackTrace()));
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, MyService_CED.this);
    } // sendTripAction

    private void sendMessage(String message, int errorvalue) {
        Intent intent = new Intent(Constants_CED.ERVICE_DRIVER_ALL_PHP);
        intent.putExtra("newresponse", message);
        intent.putExtra("newerrorvalue", errorvalue);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}