package com.smartshehar.dashboard.app;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lib.app.util.Constants_lib_ss;


public class SSLog {
    private static String TAG = "SSLog: ";
    public static RequestQueue mVolleyRequestQueue;
    static Context mContext = null;
    static String sEmail = "";

    public static void setContext(Context context, String email) {
        mContext = context;
        sEmail = email;
    }

    public static void d(String tag, String msg) {
        int len = tag.length() <= 20 ? tag.length() : 20;
        tag = tag.substring(0, len);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
          /*  if (BuildConfig.DEBUG) {
                Log.d(TAG, tag + msg);
            }*/
            // sendLogToServer("d", tag, "", msg);
        }
    }

    public static void i(String tag, String msg) {
        int len = tag.length() <= 20 ? tag.length() : 20;
        tag = tag.substring(0, len);
        if (Log.isLoggable(TAG, Log.INFO)) {
           /* if (BuildConfig.DEBUG) {
                Log.i(TAG, tag + msg);
            }*/
        }
    }

    /*
     * public static void e(String tag, String msg) { if (Log.isLoggable(TAG,
     * Log.ERROR)) { Log.e(TAG, tag + msg); sendLogToServer("e", tag, "", msg);
     * } }
     */
    public static void e(String tag, String sFunctionName, Exception e) {
        int len = tag.length() <= 20 ? tag.length() : 20;
        tag = tag.substring(0, len);
        if (Log.isLoggable(tag, Log.ERROR)) {
            /*if (BuildConfig.DEBUG) {
                Log.e(tag, tag + sFunctionName + e.getMessage());
            }*/
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) {
                sb.append(element.toString());
                sb.append("\n");
            }

            sendLogToServer("e", tag,
                    Thread.currentThread().getStackTrace()[2].getLineNumber()
                            + ": " + sFunctionName,
                    e.getMessage() + "\n" + sb.toString());
           /* if (BuildConfig.DEBUG) {
                Log.e(tag,
                        Thread.currentThread().getStackTrace()[2].getLineNumber()
                                + ": " + sFunctionName + e.getMessage() + "\n"
                                + sb.toString());
            }*/
        }

    }

    public static void e(String tag, String sFunctionName, String e) {
        int len = tag.length() <= 20 ? tag.length() : 20;
        tag = tag.substring(0, len);
        if (Log.isLoggable(tag, Log.ERROR)) {
           /* if (BuildConfig.DEBUG) {
                Log.e(tag, tag + sFunctionName + e);
                Log.e(tag, sFunctionName + e);
            }*/
            sendLogToServer("e", tag, sFunctionName, e);

        }

    }

    public static void v(String tag, String msg) {
        int len = tag.length() <= 20 ? tag.length() : 20;
        tag = tag.substring(0, len);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
           /* if (BuildConfig.DEBUG) {
                Log.v(TAG, tag + msg);
            }*/
        }
    }

    public static void w(String tag, String msg) {
        int len = tag.length() <= 20 ? tag.length() : 20;
        tag = tag.substring(0, len);
        if (Log.isLoggable(TAG, Log.WARN)) {
            Log.w(TAG, tag + msg);
        }
    }

    public static void sendLogToServer(final String sLogType,
                                       final String sTag, final String sFunctionName, final String sLogMessage) {
        int len = sTag.length() <= 20 ? sTag.length() : 20;
        final String tag = sTag.substring(0, len);
        if (mContext == null) {
            return;
        }
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_lib_ss.SEND_LOG_TO_SERVER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // handler.postDelayed(runnable,
                        // Constants.UPDATE_INTERVAL);

                        // response
                        Log.d("Response", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error

                SSLog.e(TAG, "sendLogToServer :-   ",
                        error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                SimpleDateFormat df = new SimpleDateFormat(
                        Constants_lib_ss.STANDARD_DATE_FORMAT, Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                Map<String, String> params = new HashMap<>();
                if (!TextUtils.isEmpty(sEmail))
                    params.put("e", sEmail);
                if (!TextUtils.isEmpty(sLogType))
                    params.put("l", sLogType);
                if (!TextUtils.isEmpty(tag))
                    params.put("t", tag);
                if (!TextUtils.isEmpty(sFunctionName))
                    params.put("f", sFunctionName);
                if (!TextUtils.isEmpty(sLogMessage))
                    params.put("m", sLogMessage);
                String mDate = df.format(cal.getTime());
                if (!TextUtils.isEmpty(mDate))
                    params.put("d", mDate);

                return params;
            }
        };
        try {
            postRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (mVolleyRequestQueue == null) {
                mVolleyRequestQueue = Volley.newRequestQueue(mContext);
            }
            mVolleyRequestQueue.add(postRequest);
        } catch (Exception e) {
            SSLog.e(TAG, "mRequestQueue may not be initialized - ", e);
        }
    } // sendUpdatePosition
}

