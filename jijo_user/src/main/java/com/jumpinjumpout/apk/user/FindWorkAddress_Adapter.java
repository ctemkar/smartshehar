package com.jumpinjumpout.apk.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.jumpinjumpout.apk.R;

import java.util.ArrayList;

/**
 * Created by user pc on 02-10-2015.
 */
public class FindWorkAddress_Adapter extends BaseAdapter {
    private static ArrayList<FindWorkAddress_Result> findworkaddress_array_list;

    private static String TAG = FindWorkAddress_Adapter.class.getSimpleName();

    private LayoutInflater mInflater;
    Context context;
    private int count;
    private boolean[] thumbnailsselection;
    FindWorkAddress_Result p;

    public FindWorkAddress_Adapter(Context context,
                                   ArrayList<FindWorkAddress_Result> results) {
        findworkaddress_array_list = results;
        mInflater = LayoutInflater.from(context);
        this.context = context;
        count = findworkaddress_array_list.size();
        thumbnailsselection = new boolean[count];
    }

    public int getCount() {
        return findworkaddress_array_list.size();
    }

    public Object getItem(int position) {
        return findworkaddress_array_list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.findworkaddress_item, null);
            holder = new ViewHolder();

            holder.tvUserName = (TextView) convertView
                    .findViewById(R.id.tvUserName);
            holder.btnAddFriend = (CheckBox) convertView
                    .findViewById(R.id.btnAddFriend);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        p = getProduct(position);
        holder.tvUserName.setText(findworkaddress_array_list.get(position)
                .getsFullName());
        holder.btnAddFriend.setOnCheckedChangeListener(myCheckChangList);
        holder.btnAddFriend.setTag(position);
        holder.btnAddFriend.setChecked(p.box);
        return convertView;
    }

    static class ViewHolder {
        TextView tvUserName;
        CheckBox btnAddFriend;
    }

    FindWorkAddress_Result getProduct(int position) {
        return ((FindWorkAddress_Result) getItem(position));
    }

    public ArrayList<FindWorkAddress_Result> getBox() {
        ArrayList<FindWorkAddress_Result> box = new ArrayList<FindWorkAddress_Result>();
        for (FindWorkAddress_Result p : findworkaddress_array_list) {
            if (p.box)
                box.add(p);
        }
        return box;
    }

    CompoundButton.OnCheckedChangeListener myCheckChangList = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            getProduct((Integer) buttonView.getTag()).box = isChecked;
        }
    };

}