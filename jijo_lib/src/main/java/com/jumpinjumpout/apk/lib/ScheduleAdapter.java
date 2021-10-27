package com.jumpinjumpout.apk.lib;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jumpinjumpout.apk.lib.ui.ScheduleTripList_act;

import java.util.ArrayList;

/**
 * Created by Soumen on 09-10-2015.
 * Custom adapter for Schedule list
 */
public class ScheduleAdapter extends BaseAdapter {
    private static ArrayList<CTrip> mList;
    private ScheduleTripList_act.BtnClickListener mClickListener = null;
    private static String TAG = ScheduleAdapter.class.getSimpleName();

    private LayoutInflater mInflater;

    public ScheduleAdapter(Context context,
                           ArrayList<CTrip> results) {
        super();
        mList = results;
        mInflater = LayoutInflater.from(context);
    }
    public ScheduleAdapter(Context context,
                           ArrayList<CTrip> results, ScheduleTripList_act.BtnClickListener listener) {
        super();
        mList = results;
        mInflater = LayoutInflater.from(context);
        mClickListener = listener;
    }

    public int getCount() {
        return mList.size();
    }

    public Object getItem(int position) {
        return mList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.schdhule_item, null);
            holder = new ViewHolder();
            holder.startAddress = (TextView) convertView
                    .findViewById(R.id.textView1);
            holder.destinationAddress = (TextView) convertView
                    .findViewById(R.id.textView2);
            holder.tvScheduleTime = (TextView) convertView
                    .findViewById(R.id.tvScheduleTime);
            holder.btnGo = (Button) convertView
                    .findViewById(R.id.btnGo);
            holder.btnGo.setTag(position);
            holder.destinationAddress.setTag(position);
            holder.cbSun = (CheckBox) convertView
                    .findViewById(R.id.cbSun);
            holder.cbMon = (CheckBox) convertView
                    .findViewById(R.id.cbMon);
            holder.cbTue = (CheckBox) convertView
                    .findViewById(R.id.cbTue);
            holder.cbWed = (CheckBox) convertView
                    .findViewById(R.id.cbWed);
            holder.cbThu = (CheckBox) convertView
                    .findViewById(R.id.cbThu);
            holder.cbFri = (CheckBox) convertView
                    .findViewById(R.id.cbFri);
            holder.cbSat = (CheckBox) convertView
                    .findViewById(R.id.cbSat);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.startAddress.setText(mList.get(position)
                .getstart_address());
        holder.destinationAddress.setText("To " + mList.get(position)
                .getdestination_address());
        holder.tvScheduleTime.setText(mList.get(position).getStime());
        boolean sel = mList.get(position).getsun();
        if (sel) {
            holder.cbSun.setChecked(true);
        }
        sel = mList.get(position).getmon();
        if (sel) {
            holder.cbMon.setChecked(true);
        }
        sel = mList.get(position).gettue();
        if (sel) {
            holder.cbTue.setChecked(true);
        }
        sel = mList.get(position).getwed();
        if (sel) {
            holder.cbWed.setChecked(true);
        }
        sel = mList.get(position).getthu();
        if (sel) {
            holder.cbThu.setChecked(true);
        }
        sel = mList.get(position).getfri();
        if (sel) {
            holder.cbFri.setChecked(true);
        }
        sel = mList.get(position).getsat();
        if (sel) {
            holder.cbSat.setChecked(true);
        }

        holder.btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null)
                    mClickListener.onBtnClick((Integer) v.getTag());
            }
        });
        holder.destinationAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null)
                    mClickListener.onDestinationClick((Integer) v.getTag());
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView startAddress;
        TextView destinationAddress;
        TextView tvScheduleTime;
        Button btnGo;
        CheckBox cbSun, cbMon, cbTue, cbWed, cbThu, cbFri, cbSat;

    }

}
