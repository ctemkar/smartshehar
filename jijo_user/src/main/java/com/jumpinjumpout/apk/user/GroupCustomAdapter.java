package com.jumpinjumpout.apk.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jumpinjumpout.apk.R;

import java.util.ArrayList;

public class GroupCustomAdapter extends BaseAdapter {
	private static ArrayList<GroupResults> grouphArrayList;

	private LayoutInflater mInflater;

	public GroupCustomAdapter(Context context, ArrayList<GroupResults> results) {
		grouphArrayList = results;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return grouphArrayList.size();
	}

	public Object getItem(int position) {
		return grouphArrayList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.show_all_group_list, null);
			holder = new ViewHolder();
			holder.groupname = (TextView) convertView
					.findViewById(R.id.group_Name);
			holder.groupdesc = (TextView) convertView
					.findViewById(R.id.group_Desc);
			holder.grouptype = (TextView) convertView
					.findViewById(R.id.Group_Type);
			holder.me = (TextView) convertView.findViewById(R.id.me);
			holder.user_name = (TextView) convertView
					.findViewById(R.id.user_name);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.groupname.setText(grouphArrayList.get(position).getGroupName());
		holder.groupdesc.setText(grouphArrayList.get(position).getGroupDesc());
		if (grouphArrayList.get(position).getGroupType().equals("P")) {
			holder.grouptype.setText("Private");
		}

		if (grouphArrayList.get(position).getMine()
				.equals(Constants_user.VALUE_TRUE)) {
			holder.me.setVisibility(View.VISIBLE);
			holder.user_name.setText(grouphArrayList.get(position)
					.getFullName() + "(group admin)");
			holder.user_name.setVisibility(View.GONE);
		} else {
			if (grouphArrayList.get(position).getActive()
					.equals(Constants_user.VALUE_TRUE)) {
				holder.me.setText("Silent");

			} else if (grouphArrayList.get(position).getActive()
					.equals(Constants_user.VALUE_FLASE)) {
				holder.me.setText("");
			}

			holder.user_name.setText("Created by "
					+ grouphArrayList.get(position).getFullName());
		}

		return convertView;
	}

	static class ViewHolder {
		TextView groupname;
		TextView groupdesc;
		TextView grouptype, user_name;
		TextView me;
	}
}