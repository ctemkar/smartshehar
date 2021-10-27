package com.jumpinjumpout.apk.lib;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.jumpinjumpout.apk.lib.ui.RecentTrip_act;

import java.util.ArrayList;

/**
 * Created by user pc on 30-11-2015.
 */
public class RecentTripAdapter extends BaseAdapter {

    private static ArrayList<CTrip> resentArrayList;
    private LayoutInflater mInflater;
    Context mContext;
    private RecentTrip_act.BtnClickListener mClickListener = null;

    public RecentTripAdapter(Context context, ArrayList<CTrip> results,
                             RecentTrip_act.BtnClickListener listener) {
        super();
        mContext = context;
        resentArrayList = results;
        mInflater = LayoutInflater.from(context);
        mClickListener = listener;
    }

    public int getCount() {
        return resentArrayList.size();
    }

    public Object getItem(int position) {
        return resentArrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        // View v = convertView;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.recenttrip_item, null);
            holder = new ViewHolder();
            holder.fromtrip = (TextView) convertView.findViewById(R.id.tvResentFrom);
            holder.totrip = (TextView) convertView.findViewById(R.id.tvRescentTo);
            holder.tvmakeSchedule = (TextView) convertView.findViewById(R.id.tvmakeSchedule);
            holder.btnResentGo = (Button) convertView.findViewById(R.id.btnResentGo);
            holder.btnResentGo.setTag(position);
            holder.tvmakeSchedule.setTag(position);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (!TextUtils.isEmpty(resentArrayList.get(position).getFrom())) {
            holder.fromtrip.setText(resentArrayList.get(position).getFrom());
            holder.tvmakeSchedule.setText("make a schedule");
            holder.tvmakeSchedule.setVisibility(View.VISIBLE);
        } else {
            holder.fromtrip.setText(resentArrayList.get(position).getstart_address());
            holder.tvmakeSchedule.setText("schedule");
            holder.tvmakeSchedule.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(resentArrayList.get(position).getTo())) {
            holder.totrip.setText("To " + resentArrayList.get(position).getTo());
        } else {
            holder.totrip.setText("To " + resentArrayList.get(position).getdestination_address());
        }
        holder.btnResentGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onBtnClick(position);
                }
            }
        });

        holder.tvmakeSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onTVMakeSchedule(position);
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView fromtrip;
        TextView totrip, tvmakeSchedule;
        Button btnResentGo;
    }
}