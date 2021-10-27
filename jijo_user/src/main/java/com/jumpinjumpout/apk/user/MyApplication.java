package com.jumpinjumpout.apk.user;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.FacebookSdk;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.SSLog;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import lib.app.util.Connectivity;

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
	Connectivity mConnectivity;

	public Connectivity getConnectivity() {
		return mConnectivity;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);
		FacebookSdk.sdkInitialize(getApplicationContext());
		mConnectivity = new Connectivity();
        HashMap<String,String> ACRAData = new HashMap<String,String>();
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
        FacebookSdk.sdkInitialize(getApplicationContext());
        gettingHashKey();
	}

    public void gettingHashKey(){

        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.jumpinjumpout.apk.user",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

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

	public SharedPreferences getPersistentPreference() {
		if (mPrefsVersionPersistent == null) {
			mPrefsVersionPersistent = getApplicationContext()
					.getSharedPreferences(
							Constants_user.PREFS_VERSION_PERSISTENT,
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

	public void addVolleyRequest(JsonArrayRequest postRequest,
			boolean clearBeforeQuery) {
		clearBeforeQuery = false; // disabled for testing.
		try {
			postRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
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
