package com.smartshehar.cabe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by soumen on 24-09-2016.
 */

public class ShareAddressList_Adapter extends BaseAdapter {
    private static ArrayList<CTripCabE> CTrip_CED_Array_List;

    private LayoutInflater mInflater;
    Context context;

    public ShareAddressList_Adapter(Context context, ArrayList<CTripCabE> results) {
        CTrip_CED_Array_List = results;
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    public int getCount() {
        return CTrip_CED_Array_List.size();
    }

    public Object getItem(int position) {
        return CTrip_CED_Array_List.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    public View getView(final int position, View convertView, ViewGroup parent) {
        ShareAddressList_Adapter.ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(
                    R.layout.simplerow, null);
            holder = new ShareAddressList_Adapter.ViewHolder();

            holder.rowTextView = (TextView) convertView.findViewById(R.id.rowTextView);

            convertView.setTag(holder);
        } else {
            holder = (ShareAddressList_Adapter.ViewHolder) convertView.getTag();
        }
        holder.rowTextView.setText(CTrip_CED_Array_List.get(position).getArea_Fixed() + " "
                + CTrip_CED_Array_List.get(position).getFormatted_Address_Fixed()
                + " (" + CTrip_CED_Array_List.get(position).getPick_Drop_Point_Fixed() + ")");
        return convertView;
    }

    static class ViewHolder {
        TextView rowTextView;
    }
}