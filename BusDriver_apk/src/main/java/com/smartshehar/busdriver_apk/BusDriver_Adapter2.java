package com.smartshehar.busdriver_apk;

/**
 * Created by user pc on 25-07-2015.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by user pc on 20-07-2015.
 */
public class BusDriver_Adapter2 extends BaseAdapter {
    private static ArrayList<BusDriver_Result> trafficViolationResultArrayList;

    private LayoutInflater mInflater;

    public BusDriver_Adapter2(Context context, ArrayList<BusDriver_Result> results) {
        trafficViolationResultArrayList = results;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return trafficViolationResultArrayList.size();
    }

    public Object getItem(int position) {
        return trafficViolationResultArrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        // View v = convertView;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView
                    .findViewById(R.id.textView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(trafficViolationResultArrayList.get(position).getStopname());

        return convertView;
    }

    static class ViewHolder {
        TextView textView;
    }
}