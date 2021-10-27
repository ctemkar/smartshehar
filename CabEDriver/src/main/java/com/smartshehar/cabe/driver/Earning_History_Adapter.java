package com.smartshehar.cabe.driver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jijo_soumen on 16/03/2016.
 * Earning_History Adapter
 */
public class Earning_History_Adapter extends BaseAdapter {
    private static ArrayList<Weekly_History_Result> CTrip_CED_Array_List;

    private LayoutInflater mInflater;
    Context context;
    SimpleDateFormat df1;
    Date dateObj;

    public Earning_History_Adapter(Context context, ArrayList<Weekly_History_Result> results) {
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

    @SuppressLint({"InflateParams", "SetTextI18n", "DefaultLocale"})
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(
                    R.layout.history_earning_item, null);
            holder = new ViewHolder();

            holder.tvWeeklyDayName = (TextView) convertView.findViewById(R.id.tvWeeklyDayName);

            holder.tvWeeklyDayTotalTrip = (TextView) convertView.findViewById(R.id.tvWeeklyDayTotalTrip);

            holder.tvWeeklyTotalTripCost = (TextView) convertView.findViewById(R.id.tvWeeklyTotalTripCost);

            holder.tvWeeklyTripDistance = (TextView) convertView.findViewById(R.id.tvWeeklyTripDistance);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        try {
            df1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String[] days = new String[]{"", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
            Calendar calendar = Calendar.getInstance();
            dateObj = df1.parse(CTrip_CED_Array_List.get(position).getCurrentDate());
            calendar.setTime(dateObj);
            String day = days[calendar.get(Calendar.DAY_OF_WEEK)];
            holder.tvWeeklyDayName.setText(day);
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.tvWeeklyDayTotalTrip.setText("Trip: " +
                String.valueOf(CTrip_CED_Array_List.get(position).getTotalTrip()));
        holder.tvWeeklyTotalTripCost.setText(context.getResources().getString(R.string.rs) +
                String.valueOf(CTrip_CED_Array_List.get(position).getCost()));
        holder.tvWeeklyTripDistance.setText("Distance: " +
                String.format("%.2f", CTrip_CED_Array_List.get(position).getDistance()));
        return convertView;
    }

    static class ViewHolder {
        TextView tvWeeklyDayName, tvWeeklyDayTotalTrip, tvWeeklyTotalTripCost, tvWeeklyTripDistance;
    }
}