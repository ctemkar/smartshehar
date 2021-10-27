package com.jumpinjumpout.apk.user.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.ClearableEditText;
import com.jumpinjumpout.apk.lib.R;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.CMemberGroup;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;
import com.jumpinjumpout.apk.user.MyGroupsAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.jumpinjumpout.apk.lib.SSLog;


public class SearchGroup_act extends Activity {
	private final String TAG = "SearchGroup_act";
	ProgressDialog mProgressDialog;
	// Declare Variables
	ListView addressListView;
	MyGroupsAdapter mMyGroupsAdapter;
	CGlobals_lib mApp;
	ClearableEditText editsearch;
	private boolean isGettingGroups = false;

	@SuppressLint("InflateParams")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.searchaddress_act);
		ActionBar actionBar = getActionBar(); // you can use ABS or the
														// non-bc ActionBar
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_HOME_AS_UP); // what's mainly important here
													// is DISPLAY_SHOW_CUSTOM.
													// the rest is optional
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// inflate the view that we created before
		View v = inflater.inflate(R.layout.actionbar_search, null);
		editsearch = (ClearableEditText) v.findViewById(R.id.search_box);
		editsearch.setVisibility(View.VISIBLE);
		editsearch.setHint("Enter group name");
		editsearch.addTextChangedListener(textWatcher);
		addressListView = (ListView) findViewById(R.id.listview);
		mApp = CGlobals_lib.getInstance();
		mProgressDialog = new ProgressDialog(SearchGroup_act.this);
		mProgressDialog.setMessage("Getting groups");
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		getMyGroups();
		actionBar.setCustomView(v);
	} // create

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (mProgressDialog != null)
			if (mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}
		super.onPause();
	}

	public void onDestroy() {
		super.onDestroy();
		if (mProgressDialog != null)
			if (mProgressDialog.isShowing()) {
				mProgressDialog.cancel();
			}
	}
	// EditText TextWatcher
	private TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			String text = editsearch.getText().toString()
					.toLowerCase(Locale.getDefault());
			mMyGroupsAdapter.getFilter().filter(text);
			SSLog.i(TAG, "filtered");
		}

		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}

	};

	public void getMyGroups() {
		if (!isFinishing()) {
			mProgressDialog.setMessage("Creating trip. Please wait ...");
			mProgressDialog.show();
		}
		final String sUrl = Constants_user.GET_MY_GROUPS_URL;
		isGettingGroups = true;
		StringRequest postRequest = new StringRequest(Request.Method.POST,
				sUrl, new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						isGettingGroups = false;
						if (mProgressDialog != null)
							if (mProgressDialog.isShowing()) {
								mProgressDialog.cancel();
							}
						populateGroupList(response);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						if (mProgressDialog != null)
							if (mProgressDialog.isShowing()) {
								mProgressDialog.cancel();
							}
						isGettingGroups = false;
						try {
							SSLog.e(TAG,
									"getMyGroups Response.ErrorListener - ",
									error.getMessage());
						} catch (Exception e) {
							SSLog.e(TAG,
									" getMyGroups Response.ErrorListener (2) - ",
									e);
						}
					}
				}) {
			@Override
			protected Map<String, String> getParams() {
				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
				Calendar cal = Calendar.getInstance();
				Map<String, String> params = new HashMap<String, String>();
				params.put(
						"appuserid",
						Integer.toString(MyApplication.getInstance()
								.getPersistentPreference()
								.getInt(Constants_user.PREF_APPUSERID, -1)));
				params.put("email", CGlobals_user.msGmail);
				params.put("clientdatetime", df.format(cal.getTime()));
				String delim = "";
				StringBuilder getParams = new StringBuilder();
				for (Map.Entry<String, String> entry : params.entrySet()) {
					getParams.append(delim + entry.getKey() + "="
							+ entry.getValue());
					delim = "&";
				}
				String debugUrl = "";
				try {
					debugUrl = sUrl + "?" + getParams.toString() + "&verbose=Y";
					SSLog.d(TAG, sUrl);
				} catch (Exception e) {
					SSLog.e(TAG, "getPassengers map - ", e);
				}
				return CGlobals_user.getInstance().checkParams(params);
			}
		};
		try {
			MyApplication.getInstance().addVolleyRequest(postRequest, false);
		} catch (Exception e) {
			SSLog.e(TAG, "getActiveuser CGlobals.getInstance().mVolleyReq..", e);
		}
	} // getMyGroups

	void populateGroupList(String response) {
		try {
			JSONArray JA = new JSONArray(response);
			JSONObject json = null;
			String sGroupName, sGroupDescription;
			ArrayList<CMemberGroup> recentAddress = new ArrayList<CMemberGroup>();
			for (int i = 0; i < JA.length(); i++) {
				json = JA.getJSONObject(i);
				sGroupName = json.getString("group_name");
				sGroupDescription = json.isNull("group_desc") ? "" : json
						.getString("group_desc");
				recentAddress.add(new CMemberGroup(sGroupName,
						sGroupDescription));
			}
			mMyGroupsAdapter = new MyGroupsAdapter(SearchGroup_act.this,
					recentAddress, SearchGroup_act.this);
			addressListView.setAdapter(mMyGroupsAdapter);
		} catch (Exception e) {
			Log.e("Fail 3", e.toString());
		}

	}

}