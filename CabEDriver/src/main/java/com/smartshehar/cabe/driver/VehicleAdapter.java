package com.smartshehar.cabe.driver;

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
 * Vehicle Adapter
 */
public class VehicleAdapter extends BaseAdapter {
    private static ArrayList<VehicleResult> vehicle_Array_List;

    private LayoutInflater mInflater;
    Context context;

    public VehicleAdapter(Context context, ArrayList<VehicleResult> results) {
        vehicle_Array_List = results;
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    public int getCount() {
        return vehicle_Array_List.size();
    }

    public Object getItem(int position) {
        return vehicle_Array_List.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(
                    R.layout.vehicle_list_item, null);
            holder = new ViewHolder();

            holder.mtvvehicleNumber = (TextView) convertView.findViewById(R.id.tvvehicleNumber);

            holder.mtvvehicleCompany = (TextView) convertView.findViewById(R.id.tvvehicleCompany);

            holder.mtvvehicleModel = (TextView) convertView.findViewById(R.id.tvvehicleModel);

            holder.mtvvehicleSeating = (TextView) convertView.findViewById(R.id.tvvehicleSeating);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mtvvehicleNumber.setText(vehicle_Array_List.get(position)
                .getFull_Vehicleno());
        holder.mtvvehicleCompany.setText(vehicle_Array_List.get(position)
                .getVehicleCompany());
        holder.mtvvehicleModel.setText(vehicle_Array_List.get(position)
                .getVehicleModel());
        holder.mtvvehicleSeating.setText(vehicle_Array_List.get(position)
                .getVehicleSeating() + "\n" + vehicle_Array_List.get(position).getServiceCode());

        return convertView;
    }

    static class ViewHolder {
        TextView mtvvehicleNumber, mtvvehicleCompany, mtvvehicleModel, mtvvehicleSeating;
    }
}