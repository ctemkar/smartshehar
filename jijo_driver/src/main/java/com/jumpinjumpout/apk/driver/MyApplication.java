package com.jumpinjumpout.apk.driver;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jumpinjumpout.apk.lib.SSLog;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.io.InputStream;
import java.util.HashMap;

// @ReportsCrashes(formKey = "", // will not be used
// mailTo = "apperror@jumpinjumpout.com", mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text)
@ReportsCrashes(
        formUri = "http://www.smartshehar.com/alpha/smartsheharapp/v17/svr/php/acra/acra_ss_report.php",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)

public class MyApplication extends Application {
    private String TAG = "MyApplication: ";
    private static MyApplication sInstance;
    public static RequestQueue mVolleyRequestQueue;
    private SharedPreferences mPrefs = null, mPrefsVersionPersistent = null;
    SharedPreferences.Editor mEditor = null, mEditorVersionPersistent = null;

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);

        HashMap<String, String> ACRAData = new HashMap<String, String>();
        AccountManager manager = (AccountManager) this
                .getSystemService(Context.ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        String msGmail = null;

        for (Account account : list) {
            if (account.type.equalsIgnoreCase("com.google")) {
                msGmail = account.name;
                break;
            }
        }

        ACRA.getErrorReporter().putCustomData("useremail", msGmail);
        ACRA.getErrorReporter().putCustomData("email", msGmail);
        Log.d("MyApplication", "onCreate()");
        sInstance = this;
        super.onCreate();
    }

    public static synchronized MyApplication getInstance() {
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mVolleyRequestQueue == null) {

            // Instantiate the cache
            Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB
            // cap

            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());

            // Instantiate the RequestQueue with the cache and network.
            mVolleyRequestQueue = new RequestQueue(cache, network);
            mVolleyRequestQueue.start();
            mVolleyRequestQueue = Volley
                    .newRequestQueue(getApplicationContext());
        }

        return mVolleyRequestQueue;
    }

    public static Bitmap loadContactPhoto(Context context, long id) {
        //ContentResolver cr = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, id);
        InputStream input;
        input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);
        if (input == null) {
            return null;
        }
        return BitmapFactory.decodeStream(input);
    }

    public SharedPreferences getPreference() {
        if (mPrefs == null) {
            mPrefs = getApplicationContext().getSharedPreferences(
                    Constants_driver.PREFS_APP, Context.MODE_PRIVATE);
        }
        return mPrefs;
    }

    public SharedPreferences.Editor getPreferenceEditor() {
        if (mEditor == null) {
            mEditor = getPreference().edit();
        }
        return mEditor;
    }

    public SharedPreferences getPersistentPreference() {
        if (mPrefsVersionPersistent == null) {
            mPrefsVersionPersistent = getApplicationContext()
                    .getSharedPreferences(
                            Constants_driver.PREFS_VERSION_PERSISTENT,
                            Context.MODE_PRIVATE);
        }
        return mPrefsVersionPersistent;
    }

    public SharedPreferences.Editor getPersistentPreferenceEditor() {
        if (mEditorVersionPersistent == null) {
            mEditorVersionPersistent = getPersistentPreference().edit();
        }
        return mEditorVersionPersistent;
    }

    public void addVolleyRequest(StringRequest postRequest,
                                 boolean clearBeforeQuery) {
        try {
            postRequest.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 2,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            if (clearBeforeQuery) {
                getRequestQueue().cancelAll(new RequestQueue.RequestFilter() {
                    @Override
                    public boolean apply(Request<?> request) {
                        return true;
                    }
                });
            }
            getRequestQueue().add(postRequest);
        } catch (Exception e) {
            SSLog.e(TAG, "mVolleyRequestQueue may not be initialized - ", e);
        }
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

} // MyApplication
