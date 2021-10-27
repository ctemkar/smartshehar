package com.smartshehar.dashboard.app;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lib.app.util.Connectivity;

public class SendDraftDataService extends Service {

    Connectivity mConnectivity;
    public static final String TAG = "SendDraftDataService";


    String issueid = "";
    Handler handler = new Handler();
    DataBaseHandler db = null;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mConnectivity = new Connectivity();
        db = new DataBaseHandler(SendDraftDataService.this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // starts service
        // handler is used to execute piece of code after perticular time.
        handler.postDelayed(runnableSendDataToServer, Constants_dp.CHECKE_CONNECTION_INTERVAL);

        return START_STICKY;
    }

    protected Runnable runnableSendDataToServer = new Runnable() {
        @Override
        public void run() {
            checkConnection();
            handler.postDelayed(this,  Constants_dp.CHECKE_CONNECTION_INTERVAL);
        }
    };

    public void checkConnection() {

        mConnectivity = new Connectivity();


        if (Connectivity.checkConnected(SendDraftDataService.this)) {
            if (!mConnectivity.connError(SendDraftDataService.this)) {
                final List<CIssue> draftList = db.getDraftList();
                sendDraftDriverDetails(draftList);
            }
        }
    }

    private void sendDraftDriverDetails(List<CIssue> list) {
        for (int i = 0; i < list.size(); i++) {
            final CIssue info = list.get(i);
            final HashMap<String, String> hmp = new HashMap<>();
            hmp.put("uniquekey", Long.toString(info.getUniqueKey()));
            hmp.put("issueaddress", info.getAddress());
            hmp.put("issuedatetime", info.getTimeOffence());
            if(!TextUtils.isEmpty(info.getVehicle()))
                hmp.put("vehicleno", info.getVehicle());
            if (!TextUtils.isEmpty(info.getTypeOffence()))
                hmp.put("issuetype", info.getTypeOffence());
            if (!TextUtils.isEmpty(info.getItemcode()))
                hmp.put("issueitemcode", info.getItemcode());
            hmp.put("discard_report", String.valueOf("0"));
            hmp.put("latitude", Double.toString(info.getLat()));
            hmp.put("longitude", Double.toString(info.getLng()));
            if (!TextUtils.isEmpty(info.getLocality()))
                hmp.put("locality", info.getLocality());
            if (!TextUtils.isEmpty(info.getSublocality()))
                hmp.put("sublocality", info.getSublocality());
            if (!TextUtils.isEmpty(info.getPostal_code()))
                hmp.put("postal_code", info.getPostal_code());
            if (!TextUtils.isEmpty(info.getRoute()))
                hmp.put("route", info.getRoute());
            if (!TextUtils.isEmpty(info.getNeighborhood()))
                hmp.put("neighborhood", info.getNeighborhood());
            if (!TextUtils.isEmpty(info.getAdministrative_area_level_2()))
                hmp.put("administrative_area_level_2", info.getAdministrative_area_level_2());
            if (!TextUtils.isEmpty(info.getAdministrative_area_level_1()))
                hmp.put("administrative_area_level_1", info.getAdministrative_area_level_1());
            if (!TextUtils.isEmpty(info.getWardno()))
                hmp.put("wardno", info.getWardno());
            if (!TextUtils.isEmpty(info.getGroup_id()))
                hmp.put("groupid", info.getGroup_id());
            if (!TextUtils.isEmpty(info.getMla_id()))
                hmp.put("mlaid", info.getMla_id());
            byte[] byteImage = info.getImage();
            String stringImage = null;
            if (byteImage != null) {
                stringImage = Base64.encodeToString(byteImage, Base64.DEFAULT);
            }
            final HashMap<String, String> hmpImg = new HashMap<>();
            if (!TextUtils.isEmpty(stringImage))
                hmpImg.put("issueimage", stringImage);
            if (!TextUtils.isEmpty(info.getImageName()))
                hmpImg.put("issueimagefilename", info.getImageName());
            if (!TextUtils.isEmpty(info.get_creationDateTime()))
                hmpImg.put("creationdatetime", info.get_creationDateTime());
            if (!TextUtils.isEmpty(Long.toString(info.getIssueuniquekey())))
                hmpImg.put("issueuniquekey", Long.toString(info.getIssueuniquekey()));
            sendDataToServer(hmp, Constants_dp.ADD_ISSUE_URL, info.getUniqueKey(), hmpImg);

        }
    }

    private void sendDataToServer(final HashMap<String, String> hmp, final String url, final long uniquekey, final HashMap<String, String> hmpImg) {
        try {
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1") || response != null) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    issueid = jsonObject.getString("issue_id");

                                    if (!TextUtils.isEmpty(issueid)) {

                                        sendImagesToServer(hmpImg, Constants_dp.ADD_ISSUE_IMAGES_URL, uniquekey ,issueid);

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.d(TAG, "Failed!");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "e" + error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    String column, value;
                    Map<String, String> params = new HashMap<>();
                    Set set = hmp.entrySet();
                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry me = (Map.Entry) iterator.next();
                        column = me.getKey().toString();
                        value = me.getValue().toString();
                        params.put(column, value);
                    }
                    params = CGlobals_db.getInstance(SendDraftDataService.this).getBasicMobileParams(params,
                            url, SendDraftDataService.this);

                    return CGlobals_db.getInstance(SendDraftDataService.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(SendDraftDataService.this).getRequestQueue(SendDraftDataService.this).add(postRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendImagesToServer(final HashMap<String, String> hmp, final String url, final long uniquekey, final String issue_id) {
        try {
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                try {
                                    db.sentToserver(uniquekey, 1);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                db.sentToserver(uniquekey, 0);

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    db.sentToserver(uniquekey, 0);
                    Log.d(TAG, "e" + error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    String column, value;
                    Map<String, String> params = new HashMap<>();

                    Set set = hmp.entrySet();
                    Iterator iterator = set.iterator();
                    while (iterator.hasNext()) {
                        Map.Entry me = (Map.Entry) iterator.next();
                        column = me.getKey().toString();
                        value = me.getValue().toString();
                        params.put(column, value);
                    }
                    params.put("issueid", issue_id);
                    params = CGlobals_db.getInstance(SendDraftDataService.this).getBasicMobileParams(params,
                            url, SendDraftDataService.this);

                    return CGlobals_db.getInstance(SendDraftDataService.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(SendDraftDataService.this).getRequestQueue(SendDraftDataService.this).add(postRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}