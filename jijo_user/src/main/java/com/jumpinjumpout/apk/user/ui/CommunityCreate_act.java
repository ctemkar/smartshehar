package com.jumpinjumpout.apk.user.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class CommunityCreate_act extends Activity {

	private static final String group_id_EDIT = "edit_Id";
	private static final String group_name_EDIT = "edit_name";
	private static final String group_desc_EDIT = "edit_desc";
	private static final String group_type_EDIT = "edit_type";
	private static final String group_NUMBER_EDIT = "NUMBER";

	String edit_id_Value, number_value, edit_name_Value, edit_desc_Value,
			edit_type_Value;
	String groupdesc_edit;

	public RequestQueue mVolleyRequestQueue; // initialize volley

	// initialize Edit Text

	private static String TAG = CommunityCreate_act.class.getSimpleName();

	EditText group_name, group_description;// start_address,

	RadioGroup radioGroup1;// RadioGroup initialize

	RadioButton open_group; // RadioButton initialize

	Button next; // Button initialize

	String groupname, groupdesc, grouptype, selectGroup, groupId;

	// Progress dialog
	private ProgressDialog pDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_group_form);
		// Create Object EditText
		group_name = (EditText) findViewById(R.id.group_name);
		group_description = (EditText) findViewById(R.id.group_description);
		// Create Object Button
		next = (Button) findViewById(R.id.create);

		// Create Object RadioGroup

		radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);

		open_group = (RadioButton) findViewById(R.id.open_group);

		Intent get_group_intent = getIntent();

		number_value = get_group_intent.getStringExtra(group_NUMBER_EDIT);

		if (number_value != null) {
			if (number_value.equals("1")) {
				next.setText("Done");
				edit_id_Value = get_group_intent.getStringExtra(group_id_EDIT);
				edit_name_Value = get_group_intent
						.getStringExtra(group_name_EDIT);
				edit_desc_Value = get_group_intent
						.getStringExtra(group_desc_EDIT);
				edit_type_Value = get_group_intent
						.getStringExtra(group_type_EDIT);
				group_name.setText(edit_name_Value);
				group_name.setClickable(false);
				group_name.setFocusable(false);
				group_description.setText(edit_desc_Value);

			}
		}
		// Create Object Volley library
		mVolleyRequestQueue = Volley.newRequestQueue(this);
		// Create Click function Button
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get value Every Filed And Store Data in String
				if (group_name.length() == 0 || group_description.length() == 0) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							CommunityCreate_act.this);
					builder1.setMessage("All fields required");
					builder1.setCancelable(true);
					builder1.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
					AlertDialog alert11 = builder1.create();
					if (!CommunityCreate_act.this.isFinishing()) {
						alert11.show();
					}
				} else if (number_value != null) {
					if (number_value.equals("1")) {
						groupdesc_edit = group_description.getText().toString();
						pDialog = new ProgressDialog(CommunityCreate_act.this);
						pDialog.setMessage("Please wait...");
						pDialog.setCancelable(false);
						updateGroupInfo();
					}
				} else {
					// Get data from Edit Text Filed
					groupname = group_name.getText().toString();
					groupdesc = group_description.getText().toString();
					int selectedId = radioGroup1.getCheckedRadioButtonId();
					// find the radio button by returned id
					open_group = (RadioButton) findViewById(selectedId);
					selectGroup = open_group.getText().toString();
					grouptype = selectGroup.substring(0, 1);
					sendGroupInfo();
				}
				InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				keyboard.hideSoftInputFromWindow(next.getWindowToken(), 0);
			}
		});
	}

	// Create sendGroupInfo Function
	public void updateGroupInfo() {
		showpDialog();
		// Send Data to Server Database Using Volley Library
		StringRequest postRequest = new StringRequest(Request.Method.POST,
				Constants_user.UPDATE_COMMUNITY_URL,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						System.out.println("succ" + response.toString());
						sendActivityResult(response);
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						try {
							hidepDialog();
							Toast.makeText(
									CommunityCreate_act.this.getBaseContext(),
									getString(R.string.retry_internet),
									Toast.LENGTH_LONG).show();
							
							SSLog.e(TAG, "updateGroupInfo :-   ",
									error.getMessage());
						} catch (Exception e) {
							SSLog.e(TAG, "updateGroupInfo -  ", e);
						}
					}
				}) {
			@Override
			protected Map<String, String> getParams() {
				// Put Value Using HashMap
				Map<String, String> params = new HashMap<String, String>();
				params.put(Constants_user.PARAMETER_COMMUNITY_ID, edit_id_Value);
				params.put("communityname", edit_name_Value);
				params.put("communitydesc", groupdesc_edit);
				params.put("communitytype", edit_type_Value);
				params = CGlobals_user.getInstance().getBasicMobileParams(params,
						Constants_user.UPDATE_COMMUNITY_URL,CommunityCreate_act.this);
				String delim = "";
				StringBuilder getParams = new StringBuilder();
				for (Map.Entry<String, String> entry : params.entrySet()) {
					getParams.append(delim + entry.getKey() + "="
							+ entry.getValue());
					delim = "&";
				}
				String url1 = Constants_user.UPDATE_COMMUNITY_URL;
				try {
					String url = url1 + "?" + getParams.toString();
					System.out.println("url  " + url);
				} catch (Exception e) {
					SSLog.e(TAG, "updateGroupInfo", e);
				}
				return CGlobals_user.getInstance().checkParams(params);
			}
		};
		mVolleyRequestQueue.add(postRequest);
	} // sendUpdatePosition

	private void sendActivityResult(String response) {
		Intent intent = new Intent();
		intent.putExtra("groupdesc_edit_result", groupdesc_edit);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	// Create sendGroupInfo Function
	public void sendGroupInfo() {
		// Send Data to Server Database Using Volley Library
		StringRequest postRequest = new StringRequest(Request.Method.POST,
				Constants_user.CREATE_COMMUNITY_URL,
				new Response.Listener<String>() {
					@SuppressLint("CommitPrefEdits")
					@Override
					public void onResponse(String response) {
						System.out.println("succ");
						groupResult(response);
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						try {
							Toast.makeText(
									CommunityCreate_act.this.getBaseContext(),
									getString(R.string.retry_internet),
									Toast.LENGTH_LONG).show();
							
							SSLog.e(TAG, "sendGroupInfo :-   ",
									error.getMessage());
							
						} catch (Exception e) {
							SSLog.e(TAG, "sendGroupInfo -  ", e);
						}
					}
				}) {
			@Override
			protected Map<String, String> getParams() {
				// Put Value Using HashMap
				Map<String, String> params = new HashMap<String, String>();
				params.put("communityname", groupname);
				params.put("communitydesc", groupdesc);
				params.put("communitytype", grouptype);
				params = CGlobals_user.getInstance().getBasicMobileParams(params,
						Constants_user.CREATE_COMMUNITY_URL,CommunityCreate_act.this);
				String delim = "";
				StringBuilder getParams = new StringBuilder();
				for (Map.Entry<String, String> entry : params.entrySet()) {
					getParams.append(delim + entry.getKey() + "="
							+ entry.getValue());
					delim = "&";
				}
				String url1 = Constants_user.CREATE_COMMUNITY_URL;
				try {
					String url = url1 + "?" + getParams.toString();
					System.out.println("url  " + url);

				} catch (Exception e) {
					SSLog.e(TAG, "sendGroupInfo", e);
				}
				return CGlobals_user.getInstance().checkParams(params);
			}
		};
		MyApplication.getInstance().addVolleyRequest(postRequest, false);
	} // sendUpdatePosition

	private void groupResult(String response) {
		if (TextUtils.isEmpty(response.trim())) {
			return;
		}
		JSONObject jResponse;
		try {
			jResponse = new JSONObject(response.trim());
			groupId = jResponse.getString(Constants_user.COL_COMMUNITY_ID);
			System.out.println("succ" + groupId);

			MyApplication.getInstance().getPersistentPreferenceEditor()
					.putString(Constants_user.PREF_GROUP_ID, groupId);
			MyApplication.getInstance().getPersistentPreferenceEditor().commit();

			String group_Id_text = MyApplication.getInstance().getPersistentPreference()
					.getString(Constants_user.PREF_GROUP_ID, "");
			System.out.println("group_Id_text   " + group_Id_text);

			Intent i = new Intent(CommunityCreate_act.this,
					InviteUserToGroup.class);
			i.putExtra("id", group_Id_text);
			i.putExtra("create_group_name", groupname);
			i.putExtra("create_group_desc", groupdesc);
			i.putExtra("create_group_type", selectGroup);
			startActivity(i);
			finish();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
					"Invalid Community name, try again..", Toast.LENGTH_SHORT)
					.show();
			SSLog.e(TAG, "groupResult", e);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		hidepDialog();
	}

	private void showpDialog() {
		if (!isFinishing()) {
			pDialog.show();
		}
	}

	private void hidepDialog() {
		if (pDialog != null) {
			if (pDialog.isShowing()) {
				pDialog.cancel();
			}
		}
	}

}
