package lib.app.util;

import android.content.Context;
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

public class SSLog_SS {
    private static String TAG = "SSLog_SS: ";
    public static RequestQueue mVolleyRequestQueue;
    static Context mContext = null;
    static String sEmail = "";

    public static void setContext(Context context, String email) {
        mContext = context;
        sEmail = email;
    }

    @SuppressWarnings("unused")
    public static void d(String tag, String msg) {
        if (tag.length() > 22)
            tag = tag.substring(0, 22);
        if (false)
            return;
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, tag + msg);
        }
    }

    @SuppressWarnings("unused")
    public static void i(String tag, String msg) {
        if (tag.length() > 22)
            tag = tag.substring(0, 22);
        if (false)
            return;
        if (Log.isLoggable(tag, Log.INFO)) {
            Log.i(tag, tag + msg);

        }
    }

    public static void e(String tag, String msg) {
        if (tag.length() > 22)
            tag = tag.substring(0, 22);
        if (Log.isLoggable(tag, Log.ERROR)) {
            Log.e(tag, tag + msg);

        }
    }

    public static void v(String tag, String msg) {
        if (tag.length() > 22)
            tag = tag.substring(0, 22);
        if (Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, tag + msg);
        }
    }

    public static void w(String tag, String msg) {
        if (tag.length() > 22)
            tag = tag.substring(0, 22);
        if (Log.isLoggable(tag, Log.WARN)) {
            Log.w(tag, tag + msg);
        }
    }

    public static void e(String tag, String sFunctionName, Exception e, Context context) {
        boolean isInternetCheck = !CGlobals_lib_ss.getInstance().checkConnected(context);
        if (isInternetCheck) {
            return;
        }
        int len = tag.length() <= 20 ? tag.length() : 20;
        tag = tag.substring(0, len);
        if (Log.isLoggable(tag, Log.ERROR)) {
            StringBuilder sb = new StringBuilder();
            for (StackTraceElement element : e.getStackTrace()) {
                sb.append(element.toString());
                sb.append("\n");
            }

            sendLogToServer("e", tag,
                    Thread.currentThread().getStackTrace()[2].getLineNumber()
                            + ": " + sFunctionName,
                    e.getMessage() + "\n" + sb.toString(), context);
        }
    }

    public static void sendLogToServer(final String sLogType,
                                       final String sTag, final String sFunctionName,
                                       final String sLogMessage, final Context mContext) {
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
                if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                    Log.e(TAG, "");
                } else {
                    SSLog_SS.e(TAG, "sendLogToServer :-   ", error, mContext);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                SimpleDateFormat df = new SimpleDateFormat(
                        Constants_lib_ss.STANDARD_DATE_FORMAT, Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                Map<String, String> params = new HashMap<>();
                params.put("l", sLogType);
                params.put("t", tag);
                params.put("f", sFunctionName);
                params.put("m", sLogMessage);
                params.put("d", df.format(cal.getTime()));
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        Constants_lib_ss.SEND_LOG_TO_SERVER_URL, mContext);


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
            SSLog_SS.e(TAG, "mRequestQueue may not be initialized - ", e, mContext);
        }
    } // sendUpdatePosition
}
