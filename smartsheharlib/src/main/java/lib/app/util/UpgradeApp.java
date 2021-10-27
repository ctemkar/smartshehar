package lib.app.util;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UpgradeApp {

    private static String APP_CODE;
    private static String APP_TITLE;
    private static String PACKAGE_NAME;
    private final static int DAYS_UNTIL_PROMPT = 0;
    private final static int LAUNCHES_UNTIL_PROMPT = 10;
    private static String versionCode;
    private static String TAG = "Ssl:UpgradeApp - ";

    public static void app_launched(Context mContext, PackageInfo packageinfo, UserInfo uinfo,
                                    String apptitle, String appcode,
                                    String vCode, String phppath) {
        APP_CODE = appcode;
        APP_TITLE = apptitle;
        PACKAGE_NAME = packageinfo.packageName;
//        String PHP_PATH = "http://www.smartshehar.com/svr";
        versionCode = vCode;
        SharedPreferences prefs = mContext.getSharedPreferences("upgrade", 0);
//        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
//            if (launch_count >= 2) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {

                getServerMesssage(mContext, editor);
                boolean isUpgrade = prefs.getBoolean("upgrade", false);
                if (isUpgrade) {
                    showUpgradeDialog(mContext, editor, phppath);
                    editor.putLong("launch_count", 0); // Reset launch count
                }

            }
        }
        Log.d(TAG, "INFO " + uinfo);
        editor.apply();
    }

    public static void showUpgradeDialog(final Context mContext,
                                         final Editor editor, String PHP_PATH) {
        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle("Upgrade App.");

        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(mContext);
        tv.setText("You have an older version of " + APP_TITLE +
                "\n Please upgrade it to use all the latest features." +
                "\nThank you for using SmartShehar products.");
        tv.setWidth(500);
        tv.setPadding(4, 0, 4, 10);
        ll.addView(tv);

        Button b1 = new Button(mContext);
        b1.setText("Upgrade ");
        b1.setWidth(500);
        final String sURL = "https://play.google.com/store/apps/details?id=" + PACKAGE_NAME;
        b1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(sURL)));

                dialog.dismiss();
            }
        });
        ll.addView(b1);

        Button b2 = new Button(mContext);
        b2.setText("Remind me later");
        b2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        b2.setWidth(500);
        ll.addView(b2);

        Log.d(TAG, "EDITOR " + editor + " PHP_PATH " + PHP_PATH);
        dialog.setContentView(ll);
        dialog.show();
    }

    private static void getServerMesssage(final Context mContext, final Editor editor) {
        new Thread(new Runnable() {
            public void run() {
                final Calendar cal = Calendar.getInstance();
                final String url = Constants_lib_ss.UPGRADE_APP_URL;
                StringRequest postRequest = new StringRequest(Request.Method.POST,
                        url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (!response.equals("-1")) {
                                    JSONObject jo = null;
                                    try {
                                        jo = new JSONObject(response);
                                        if (jo.length() > 0) {
                                            editor.putBoolean("upgrade", true);
                                            editor.commit();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                            Log.e(TAG, "");
                        } else {
                            SSLog_SS.e(TAG, "getCabShow :-   ", error, mContext);
                        }
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        // Put Value Using HashMap
                        Map<String, String> params = new HashMap<>();
                        params.put("app", APP_CODE);
                        params.put("versioncode", versionCode);
                        params.put("imei", UserInfo.mIMEI);
                        params.put("y", Integer.toString(cal.get(Calendar.YEAR)));
                        params.put("o", Integer.toString(cal.get(Calendar.MONTH)));
                        params.put("d", Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
                        params.put("h", Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
                        params.put("m", Integer.toString(cal.get(Calendar.MINUTE)));
                        params.put("s", Integer.toString(cal.get(Calendar.SECOND)));
                        params = CGlobals_lib_ss.getInstance().getMinMobileParams(params,
                                url, mContext);
                        String delim = "";
                        StringBuilder getParams = new StringBuilder();
                        for (Map.Entry<String, String> entry : params.entrySet()) {
                            getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                            delim = "&";
                        }
                        String url1 = url;
                        try {
                            String url = url1 + "?verbose=Y&" + getParams.toString();
                            Log.i(TAG, "url  " + url);
                        } catch (Exception e) {
                            SSLog_SS.e(TAG, "getFoundDriver", e, mContext);
                        }
                        return CGlobals_lib_ss.getInstance().checkParams(params);
                    }
                };
                CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, false, mContext);


            }
        }).start();


    } // getServerMessage()
} // UpgradeApp

