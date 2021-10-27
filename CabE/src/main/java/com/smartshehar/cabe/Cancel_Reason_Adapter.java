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
 * Created by jijo_soumen on 16/03/2016.
 * Earning_History Adapter
 */
public class Cancel_Reason_Adapter extends BaseAdapter {
    private static ArrayList<CTripCabE> CTripCabE_Array_List;

    private LayoutInflater mInflater;
    Context context;

    public Cancel_Reason_Adapter(Context context, ArrayList<CTripCabE> results) {
        CTripCabE_Array_List = results;
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    public int getCount() {
        return CTripCabE_Array_List.size();
    }

    public Object getItem(int position) {
        return CTripCabE_Array_List.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(
                    R.layout.passenger_cancel_trip, null);
            holder = new ViewHolder();

            holder.tvCancelResonItem = (TextView) convertView.findViewById(R.id.tvCancelResonItem);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tvCancelResonItem.setText(CTripCabE_Array_List.get(position).getCancel_Reason());
        return convertView;
    }

    static class ViewHolder {
        TextView tvCancelResonItem;
    }
}