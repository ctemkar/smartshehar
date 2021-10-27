package com.jumpinjumpout.apk.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.jumpinjumpout.apk.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.jumpinjumpout.apk.lib.SSLog;


public class Contact_Trip_Adapter extends BaseAdapter {
	private static ArrayList<Contact_Results> contact_Array_List;

	private static String TAG = Contact_Trip_Adapter.class.getSimpleName();

	private LayoutInflater mInflater;
	Context context;

	public Contact_Trip_Adapter(Context context,
			ArrayList<Contact_Results> results) {
		contact_Array_List = results;
		mInflater = LayoutInflater.from(context);
		this.context = context;
	}

	public int getCount() {
		return contact_Array_List.size();
	}

	public Object getItem(int position) {
		return contact_Array_List.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.add_group_list, null);
			holder = new ViewHolder();
			holder.contact_name = (TextView) convertView
					.findViewById(R.id.contactname);
			holder.contact_number = (TextView) convertView
					.findViewById(R.id.contactnumber);
			holder.contactimage = (ImageView) convertView
					.findViewById(R.id.contactimage);

			holder.contact_Delete = (ImageButton) convertView
					.findViewById(R.id.contact_Delete);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.contact_name.setText(contact_Array_List.get(position)
				.getContactName());

		holder.contact_number.setText(contact_Array_List.get(position)
				.getContactPhone());


		holder.contact_Delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("Contact_Results" + position);

              final  String tripMemberId = MyApplication.getInstance().getPersistentPreference()
                        .getString(Constants_user.PREF_TRIP_MEMBER_ID, "");

				// Send Data to Server Database Using Volley Library
				StringRequest postRequest = new StringRequest(
						Request.Method.POST,
						Constants_user.DELETE_TRIP_MEMBER_URL,
						new Response.Listener<String>() {
							@Override
							public void onResponse(String response) {
								System.out.println("succ" + response.toString());

								try {
									contact_Array_List.remove(position);
								} catch (Exception e) {
									e.printStackTrace();
								}
								notifyDataSetChanged();

                                Gson gson = new Gson();
                                String json = gson.toJson(contact_Array_List);
                                MyApplication.getInstance().getPersistentPreferenceEditor()
                                        .putString(Constants_user.PREF_INVITE_USER_CURRENT_TO_APP, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().commit();

								Toast.makeText(context,
										"Delete Community Member..",
										Toast.LENGTH_SHORT).show();

							}
						}, new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								try {
									SSLog.e(TAG,
											"Response.ErrorListener :-   ",
											error.getMessage());
								} catch (Exception e) {
									SSLog.e(TAG, "Contact Ddelete", e);
								}
							}
						}) {
					@Override
					protected Map<String, String> getParams() {
						// Put Value Using HashMap
						Map<String, String> params = new HashMap<String, String>();

						params.put("tripmemberdetailsid", tripMemberId);
						params.put("passengeremail", contact_Array_List.get(position).getContactEmail());
						params.put("passengerphone", contact_Array_List.get(position).getContactPhone());

						params = CGlobals_user.getInstance().getMinMobileParams(
								params, Constants_user.DELETE_TRIP_MEMBER_URL);

						String delim = "";
						StringBuilder getParams = new StringBuilder();
						for (Map.Entry<String, String> entry : params
								.entrySet()) {
							getParams.append(delim + entry.getKey() + "="
									+ entry.getValue());
							delim = "&";

						}

						String url1 = Constants_user.DELETE_TRIP_MEMBER_URL;

						try {
							String url = url1 + "?" + getParams.toString();
							System.out.println("url  " + url);

						} catch (Exception e) {
							SSLog.e(TAG, "Contact Ddelete", e);
						}
						return CGlobals_user.getInstance().checkParams(params);

					}

				};
				MyApplication.getInstance()
						.addVolleyRequest(postRequest, false);
			}

		});

		return convertView;
	}

	static class ViewHolder {
		TextView contact_name;
		TextView contact_number;

		ImageView contactimage;
		ImageButton contact_Delete;
	}
}