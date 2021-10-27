package com.jumpinjumpout.apk.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.jumpinjumpout.apk.R;

import java.util.ArrayList;

public class ViewAdapter extends BaseAdapter {
	private static ArrayList<View_Contact_Results> contact_Array_List;

	public RequestQueue mVolleyRequestQueue; // initialize volley

	private LayoutInflater mInflater;
	Context context;

	public ViewAdapter(Context context, ArrayList<View_Contact_Results> results) {
		contact_Array_List = results;
		mInflater = LayoutInflater.from(context);
		this.context = context;
		mVolleyRequestQueue = Volley.newRequestQueue(context);
	}

	public int getCount() {
		return contact_Array_List.size();
	}

	public Object getItem(int position) {
		return contact_Array_List.get(position);
	}

	public void update_Member_list(ArrayList<View_Contact_Results> result1) {
		contact_Array_List.clear();
		contact_Array_List = result1;
		this.notifyDataSetChanged();
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.view_me_invited_group_list, null);
			holder = new ViewHolder();
			holder.contact_name = (TextView) convertView
					.findViewById(R.id.contactname);
			holder.contact_number = (TextView) convertView
					.findViewById(R.id.contactnumber);
			holder.contactimage = (ImageView) convertView
					.findViewById(R.id.contactimage);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.contact_name.setText(contact_Array_List.get(position)
				.getContactName());

		holder.contact_number.setText(contact_Array_List.get(position)
				.getContactPhone());
		return convertView;
	}

	static class ViewHolder {
		TextView contact_name;
		TextView contact_number;
		ImageView contactimage;
	}
}