package com.jumpinjumpout;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jumpinjumpout.ui.ActDriverProfileRegistration;
import com.jumpinjumpout.ui.ActFollowUp;
import com.jumpinjumpout.www.driverprofile.R;

import java.util.ArrayList;


public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private static final String TAG = "CustomAdapter:-";
    Context ctx;
    ArrayList<DriverInfo> maDriverInfo;
    ArrayList<DriverInfo> filteredData = null;


    public CustomAdapter(Context context, ArrayList<DriverInfo> maDriverInfo) {
        this.ctx = context;
        this.maDriverInfo = maDriverInfo;
        filteredData = new ArrayList<>();
        this.filteredData.addAll(maDriverInfo);
    }

//    private static final String url = "https://farm6.static.flickr.com/5473/12316534935_b423073b4b_b.jpg";

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtDriverName, txtPhoneNo, txtbacked;
        LinearLayout llMenu;
        RelativeLayout rl_parent, rlSteps;
        ImageView ivStepone, ivSteptwo, ivStepthree,
                ivStepfour, ivMenu, ivWarning;


        public ViewHolder(View v) {
            super(v);

            txtDriverName = (TextView) v.findViewById(R.id.txtDriverName);
            txtPhoneNo = (TextView) v.findViewById(R.id.txtPhoneNo);
            rl_parent = (RelativeLayout) v.findViewById(R.id.rl_parent);
            txtbacked = (TextView) v.findViewById(R.id.txtbacked);
            rlSteps = (RelativeLayout) v.findViewById(R.id.rlSteps);
            ivStepone = (ImageView) v.findViewById(R.id.ivStepone);
            ivSteptwo = (ImageView) v.findViewById(R.id.ivSteptwo);
            ivStepthree = (ImageView) v.findViewById(R.id.ivStepthree);
            ivStepfour = (ImageView) v.findViewById(R.id.ivStepfour);
            llMenu = (LinearLayout) v.findViewById(R.id.llMenu);
            ivMenu = (ImageView) v.findViewById(R.id.ivMenu);
            ivWarning = (ImageView) v.findViewById(R.id.ivWarning);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_layout, viewGroup, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        viewHolder.txtDriverName.setText(filteredData.get(position).driver_firstname + " " + filteredData.get(position).driver_lastname);
        viewHolder.txtPhoneNo.setText(String.valueOf(filteredData.get(position).driver_phoneno));
        viewHolder.rl_parent.setOnClickListener(null);
        viewHolder.rl_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PrefUtils.setKioskModeActive(false, ctx);
                    Bundle args = new Bundle();
                    args.putString("driverphoneno", filteredData.get(position).getDriver_phoneno());
                    args.putBoolean("savedData", true);

                    Intent intent = new Intent(ctx, ActDriverProfileRegistration.class);
                    intent.putExtra("data", args);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    ctx.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "onClick(View v) " + e.toString());
                }
            }
        });
        viewHolder.llMenu.setTag(position);
        viewHolder.llMenu.setOnClickListener(null);
        viewHolder.txtbacked.setTag(position);
        viewHolder.txtbacked.setVisibility(View.GONE);
        viewHolder.ivStepone.setVisibility(View.VISIBLE);
        viewHolder.ivSteptwo.setVisibility(View.VISIBLE);
        viewHolder.ivStepthree.setVisibility(View.VISIBLE);
        viewHolder.ivStepfour.setVisibility(View.VISIBLE);
        viewHolder.ivMenu.setTag(position);
        viewHolder.ivMenu.setOnClickListener(null);
        viewHolder.ivWarning.setTag(position);
        viewHolder.ivWarning.setVisibility(View.GONE);
        viewHolder.llMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("driverphoneno", filteredData.get(position).getDriver_phoneno());
                Intent intent = new Intent(ctx, ActFollowUp.class);
                intent.putExtra("data", args);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
            }
        });
        viewHolder.ivMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("driverphoneno", filteredData.get(position).getDriver_phoneno());
                Intent intent = new Intent(ctx, ActFollowUp.class);
                intent.putExtra("data", args);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
            }
        });


        viewHolder.rlSteps.setTag(position);
        viewHolder.rlSteps.setVisibility(View.VISIBLE);


        viewHolder.ivStepone.setTag(position);
        viewHolder.ivSteptwo.setTag(position);
        viewHolder.ivStepthree.setTag(position);
        viewHolder.ivStepfour.setTag(position);

        viewHolder.ivStepone.setImageResource(R.mipmap.ic_gray_one);
        viewHolder.ivSteptwo.setImageResource(R.mipmap.ic_gray_two);
        viewHolder.ivStepthree.setImageResource(R.mipmap.ic_gray_three);
        viewHolder.ivStepfour.setImageResource(R.mipmap.ic_gray_four);


        if (!TextUtils.isEmpty(filteredData.get(position).getPayment_amount())) {
            if (filteredData.get(position).getPayment_amount().equals(ctx.getString(R.string.five_hundred)) ||
                    filteredData.get(position).getPayment_amount().equals(ctx.getString(R.string.three_thousand))) {
                viewHolder.ivStepone.setImageResource(R.mipmap.ic_green_one);

            } else if (filteredData.get(position).getPayment_amount().equals(ctx.getString(R.string.three_thousand_five_hundred))) {
                viewHolder.ivStepone.setImageResource(R.mipmap.ic_green_one);
                viewHolder.ivSteptwo.setImageResource(R.mipmap.ic_green_two);


            } else {
                viewHolder.ivStepone.setImageResource(R.mipmap.ic_gray_one);
                viewHolder.ivSteptwo.setImageResource(R.mipmap.ic_gray_two);

            }
        } else {
            viewHolder.ivStepone.setImageResource(R.mipmap.ic_gray_one);
            viewHolder.ivSteptwo.setImageResource(R.mipmap.ic_gray_two);
        }

        if (!TextUtils.isEmpty(filteredData.get(position).getPrivate_verification())) {
            if (filteredData.get(position).getPrivate_verification().equals("1")) {
                viewHolder.ivStepthree.setImageResource(R.mipmap.ic_green_three);
            } else {
                viewHolder.ivStepthree.setImageResource(R.mipmap.ic_gray_three);
            }
        }

        if (!TextUtils.isEmpty(filteredData.get(position).getPolice_verification())) {
            if (filteredData.get(position).getPolice_verification().equals("1")) {
                viewHolder.ivStepfour.setImageResource(R.mipmap.ic_green_four);
            } else {
                viewHolder.ivStepfour.setImageResource(R.mipmap.ic_gray_four);
            }
        }
        if (!TextUtils.isEmpty(filteredData.get(position).getDriver_backout_reason())) {
            if (!filteredData.get(position).getDriver_backout_reason().equals("-1")) {
                viewHolder.txtbacked.setVisibility(View.VISIBLE);
                viewHolder.ivStepone.setVisibility(View.GONE);
                viewHolder.ivSteptwo.setVisibility(View.GONE);
                viewHolder.ivStepthree.setVisibility(View.GONE);
                viewHolder.ivStepfour.setVisibility(View.GONE);
            }

        }
        if(filteredData.get(position).getSent_to_server_flag().equals("0"))
        {
            viewHolder.ivWarning.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public Filter getMyFilter() {
        return myFilter;
    }

    Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            final ArrayList<DriverInfo> list = maDriverInfo;
            int count = list.size();

            final ArrayList<DriverInfo> nlist = new ArrayList<DriverInfo>(count);


            DriverInfo mcDriverInfo;
            if (constraint.length() == 0) {
                filteredData.addAll(maDriverInfo);
            } else {
                for (int i = 0; i < count; i++) {
                    mcDriverInfo = list.get(i);
                    if (org.apache.commons.lang3.StringUtils.containsIgnoreCase(mcDriverInfo.getDriver_firstname(), constraint) ||
                            org.apache.commons.lang3.StringUtils.containsIgnoreCase(mcDriverInfo.getDriver_lastname(), constraint) ||
                            org.apache.commons.lang3.StringUtils.containsIgnoreCase(mcDriverInfo.getDriver_phoneno(), constraint)) {
                        nlist.add(mcDriverInfo);
                    }
                }
            }
            filterResults.values = nlist;
            filterResults.count = nlist.size();

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence contraint,
                                      FilterResults results) {
            filteredData.clear();
            filteredData = (ArrayList<DriverInfo>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                filteredData.addAll(maDriverInfo);
                notifyDataSetChanged();
            }
        }
    };
}
