package com.jumpinjumpout.apk.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jumpinjumpout.apk.R;

import java.util.ArrayList;

/**
 * Created by user pc on 05-10-2015.
 */
public class FriendRequestAcceptReject_Adapter extends BaseAdapter {

    Context mContext;
    ArrayList<FriendRequestAcceptReject_Result> friendRequestAcceptRejectResults;

    public FriendRequestAcceptReject_Adapter(Context context, ArrayList<FriendRequestAcceptReject_Result> friendRequestAcceptRejectResults1) {
        mContext = context;
        friendRequestAcceptRejectResults = friendRequestAcceptRejectResults1;

    }
    @Override
    public int getCount() {
        return friendRequestAcceptRejectResults.size();
    }

    @Override
    public Object getItem(int position) {
        return friendRequestAcceptRejectResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.friendrequestacceptreject_item, null);
        } else {
            view = convertView;
        }

        TextView tvFriendName = (TextView) view.findViewById(R.id.tvFriendName);

        tvFriendName.setText(friendRequestAcceptRejectResults.get(position).getsFullName());

        return view;
    }
}