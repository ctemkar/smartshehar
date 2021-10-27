package com.smartshehar.customercalls.apk;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by ctemkar on 12/03/2016.
 * Global objects and functions for CustomerCalls app
 */

@SuppressWarnings("deprecation")
public class CGlobals_cc {
    public static final String TAG = "CGlobals_cc: ";
    private static CGlobals_cc instance;
    public static int mStorePhoneId;
    public static int mStoreId;
    public static int mAccountId;

    // Restrict the constructor from being instantiated
    private CGlobals_cc() {
    }

    public static synchronized CGlobals_cc getInstance() {
        if (instance == null) {
            instance = new CGlobals_cc();
        }
        return instance;
    }

   /* public ArrayList<CCallInfo> readCallLog(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPrefs.getString(Constants_cc.PREF_CALL_LOG, null);
        Type type = new TypeToken<ArrayList<CCallInfo>>() {
        }.getType();
        ArrayList<CCallInfo> aoCallLog = gson.fromJson(json, type);
        if (aoCallLog == null) {
            aoCallLog = new ArrayList<>();
        }
        Iterator itr = aoCallLog.iterator();
        CCallInfo oCallInfo;
        while (itr.hasNext()) {
            oCallInfo = (CCallInfo) itr.next();
            try {
                if (oCallInfo == null || oCallInfo.hasNoData()) {
                    itr.remove();
                } else if (oCallInfo.IsIgnore() || oCallInfo.IsDeleted()) {
                    itr.remove();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return aoCallLog;
    }*/

    /*public void writeCallLog(Context context, ArrayList<CCallInfo> aoCallLog) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(aoCallLog);
        editor.putString(Constants_cc.PREF_CALL_LOG, json);
        editor.apply();

    }*/

    public CStore getStore() {
        CStore oStore = new CStore();


        return oStore;
    }

    public void init(Context context) {
        mStorePhoneId = CGlobals_lib_ss.getInstance().getPersistentPreference(context).getInt(Constants_lib_ss.PREF_STORE_PHONE_ID, -1);
        mStoreId = CGlobals_lib_ss.getInstance().getPersistentPreference(context).getInt(Constants_lib_ss.PREF_STORE_ID, -1);
        mAccountId = CGlobals_lib_ss.getInstance().getPersistentPreference(context).getInt(Constants_lib_ss.PREF_ACCOUNT_ID, -1);
    }

    public Map<String, String> common() {
        Map<String, String> params = new HashMap<>();
        params.put("storephoneid", String.valueOf(CGlobals_cc.mStorePhoneId));
        params.put("storeid", String.valueOf(CGlobals_cc.mStoreId));
        params.put("accountid", String.valueOf(CGlobals_cc.mAccountId));
        return params;
    }


    public static void syncCallLog(final Context context) {
        final CustomerCallsSQLiteDB db = new CustomerCallsSQLiteDB(context);
        ArrayList<CCallHandle> maHandles = db.getCallLog();
        for (final CCallHandle cCallHandle : maHandles) {

            StringRequest postRequest = new StringRequest(Request.Method.POST,
                    Constants_cc.FIND_CUSTOMER_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println("succ" + response);
                            if (!TextUtils.isEmpty(response.trim())) {
                                try {
                                    JSONObject jObj = new JSONObject(response);
                                    String callid = jObj.isNull("call_log_id") ? "-1" : jObj.getString("call_log_id");
                                    db.updateCallLog(CustomerCallsSQLiteDB.CUSTOMER_CALL_LOG_TABLE_NAME,
                                            CustomerCallsSQLiteDB.CUSTOMER_CALL_LOG_SENT_TO_SERVER_DATETIME,
                                            System.currentTimeMillis(), CustomerCallsSQLiteDB.CUSTOMER_CALL_LOG_ID_SERVER,
                                            Integer.valueOf(callid),
                                            CustomerCallsSQLiteDB.CUSTOMER_CALL_LOG_ID,
                                            cCallHandle.getMiCallLogId());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Toast.makeText(mContext, "Connection failed. Please try again", Toast.LENGTH_LONG).show();
                    try {
                        SSLog_SS.e(TAG + " searchNumber", error.toString());
                    } catch (Exception e) {
                        SSLog_SS.e(TAG + " searchNumber", e.getMessage());
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    // Put Value Using HashMap
                    Map<String, String> params;
                    params = CGlobals_cc.getInstance().common();
                    params.put("c", cCallHandle.getMsCountryCode());
                    params.put("ph", cCallHandle.getMsNationalNumber());
                    params.put("callhandled", String.valueOf(cCallHandle.getMiCallHandled()));
                    params.put("storephoneid", String.valueOf(cCallHandle.getMiStorePhoneId()));
                    params.put("calldatetime", String.valueOf(cCallHandle.getMiCallLogDate()));
                    params.put("ic", String.valueOf(1));
                    params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                            Constants_cc.FIND_CUSTOMER_URL, context);
                    String delim = "";
                    StringBuilder getParams = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                        delim = "&";
                    }
                    String url1 = Constants_cc.FIND_CUSTOMER_URL;
                    try {
                        String url = url1 + "?" + getParams.toString()
                                + "&verbose=Y";
                        Log.d(TAG, "url  " + url);

                    } catch (Exception e) {
                        SSLog_SS.e(TAG + " FindCustomer - ", e.getMessage());
                    }
                    return CGlobals_lib_ss.getInstance().checkParams(params);
                }
            };
            CGlobals_lib_ss.getInstance().getRequestQueue(context).add(postRequest);
        }
        syncCustomerData(context);
    }

    public static void syncCustomerData(final Context context) {
        final CustomerCallsSQLiteDB db = new CustomerCallsSQLiteDB(context);
        ArrayList<CCallInfo> maCallInfo = db.getCustomerList();
        for (final CCallInfo moCallInfo : maCallInfo) {
            StringRequest postRequest = new StringRequest(Request.Method.POST,
                    Constants_cc.ADD_CUSTOMER_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println("succ" + response);
                            if (!TextUtils.isEmpty(response.trim())) {
                                db.updateCustomer(CustomerCallsSQLiteDB.CUSTOMER_TABLE_NAME,
                                        CustomerCallsSQLiteDB.CUSTOMER_SENT_TO_SERVER_DATETIME,
                                        System.currentTimeMillis(), CustomerCallsSQLiteDB.CUSTOMER_COLUMN_ID_LOCAL,
                                        moCallInfo.getGetMiCustomerLocalId());
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Toast.makeText(mContext, "Connection failed. Please try again", Toast.LENGTH_LONG).show();
                    try {
                        SSLog_SS.e(TAG + " searchNumber", error.toString());
                    } catch (Exception e) {
                        SSLog_SS.e(TAG + " searchNumber", e.getMessage());
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    // Put Value Using HashMap
                    Map<String, String> params;
                    params = CGlobals_cc.getInstance().common();
                    params = moCallInfo.paramsPut(params);
                    params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                            Constants_cc.ADD_CUSTOMER_URL, context);
                    String delim = "";
                    StringBuilder getParams = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                        delim = "&";
                    }
                    String url1 = Constants_cc.ADD_CUSTOMER_URL;
                    try {
                        String url = url1 + "?" + getParams.toString()
                                + "&verbose=Y";
                        Log.d(TAG, "url  " + url);

                    } catch (Exception e) {
                        SSLog_SS.e(TAG + " FindCustomer - ", e.getMessage());
                    }
                    return CGlobals_lib_ss.getInstance().checkParams(params);
                }
            };
            CGlobals_lib_ss.getInstance().getRequestQueue(context).add(postRequest);
        }
        syncOrderData(context);
    }
    public static void syncOrderData(final Context context) {
        final CustomerCallsSQLiteDB db = new CustomerCallsSQLiteDB(context);
        ArrayList<COrderHeader> maOrder = db.getOrder();
        for (final COrderHeader mOO : maOrder) {
            StringRequest postRequest = new StringRequest(Request.Method.POST,
                    Constants_cc.ADD_ORDER_HEADER_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println("succ" + response);
                            if (!TextUtils.isEmpty(response.trim())) {
                                db.updateOrder(CustomerCallsSQLiteDB.ORDER_HEADER_TABLE_NAME,
                                        CustomerCallsSQLiteDB.ORDER_HEADER_COLUMN_SENT_TO_SERVER,
                                        System.currentTimeMillis(), CustomerCallsSQLiteDB.ORDER_HEADER_COLUMN_ID,
                                        mOO.getMiOrederId());                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Toast.makeText(mContext, "Connection failed. Please try again", Toast.LENGTH_LONG).show();
                    try {
                        SSLog_SS.e(TAG + " searchNumber", error.toString());
                    } catch (Exception e) {
                        SSLog_SS.e(TAG + " searchNumber", e.getMessage());
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    // Put Value Using HashMap
                    Map<String, String> params;
                    params = CGlobals_cc.getInstance().common();
                    params = mOO.paramsPut(params);
                    params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                            Constants_cc.ADD_ORDER_HEADER_URL, context);
                    String delim = "";
                    StringBuilder getParams = new StringBuilder();
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                        delim = "&";
                    }
                    String url1 = Constants_cc.ADD_ORDER_HEADER_URL;
                    try {
                        String url = url1 + "?" + getParams.toString()
                                + "&verbose=Y";
                        Log.d(TAG, "url  " + url);

                    } catch (Exception e) {
                        SSLog_SS.e(TAG + " FindCustomer - ", e.getMessage());
                    }
                    return CGlobals_lib_ss.getInstance().checkParams(params);
                }
            };
            CGlobals_lib_ss.getInstance().getRequestQueue(context).add(postRequest);
        }
    }

} // CGlobals_cc

