package com.smartshehar.android.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.smartshehar.android.app.ui.SearchStationActivity;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import lib.app.util.CGlobals_lib_ss;



public class StationAdapter extends BaseAdapter implements Filterable {
    private static final String TAG = "StationAdapter";
    Context ctx;
    ArrayList<String> recentStationList;

    private ArrayList<String> filteredData = null;
    public static String mStation;
    private static final String SRECENTSTATIONS = "Station List";

    public StationAdapter(Context context) {
        this.ctx = context;
        filteredData = new ArrayList<>();
        filteredData.addAll(SearchStationActivity.maRecentStation);
        recentStationList = new ArrayList<String>();
    }


    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View row;
        row = inflater.inflate(R.layout.item_station, parent, false);
        TextView tvAddressLine1;
        tvAddressLine1 = (TextView) row.findViewById(R.id.tvAddressLine1);
        tvAddressLine1.setText(filteredData.get(position));
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Intent returnIntent = new Intent();
                mStation = filteredData.get(position);
                addRecentStation(mStation);
                returnStation(mStation);
            }
        });
        return row;
    }

    private void returnStation(String addr) {


        Activity mActivity = (Activity) ctx;
        Bundle args = new Bundle();
        args.putString("station", addr);

        Intent intent = new Intent();
        intent.putExtra("data", args);
        mActivity.setResult(Activity.RESULT_OK, intent);
        mActivity.finish();
    }

    void addRecentStation(String sStationFull) {
        try {
            String sRecentStations = CGlobals_lib_ss.getInstance().getPersistentPreference(ctx)
                    .getString(SRECENTSTATIONS,
                            null);
            if (sRecentStations != null) {

                String s;
                if (sRecentStations != null && sRecentStations.trim().length() > 0) {
                    String as[] = sRecentStations.split(";");
                    int iLen = as.length;
                    for (int i = 0; i < iLen; i++) {
                        s = as[i];
                        String SELECTRECENT = "Select Station";
                        if (s != null && !s.equalsIgnoreCase(SELECTRECENT)) {

                            try {

                                recentStationList.add(s);
                                //  }
                            } catch (Exception e) {
                                SSLog.e("TrainApp: ", "getRecentStations - ",
                                        e.getMessage());

                            }
                        }
                    }
                }
            }
            if (recentStationList.size() > 0) {
                for (int j = 0; j < recentStationList.size(); j++) {
                    if (sStationFull.equals(recentStationList.get(j))) {
                        recentStationList.remove(j);
                        break;
                    }
                }
            }
            if (recentStationList.size() < 6) {
                recentStationList.add(sStationFull);
            }
            if (recentStationList.size() >= 6) {
                recentStationList.remove(0);
            }

            writeRecentStations(recentStationList);
        } catch (Exception e) {
            Log.d(TAG, "addRecentStation " + e.toString());
            SSLog.e(TAG, "addRecentStation: ", e);
        }
    }


    private void writeRecentStations(final ArrayList<String> maListStation) {

        if (maListStation == null) {
            return;
        }
        try {
            StringBuilder sb = new StringBuilder();
            String delim = "";
            int iLen = maListStation.size();
            if (iLen < 1)
                return;
            String s;
            for (int i = 0; i < iLen; i++) {
                s = maListStation.get(i);
                if (!TextUtils.isEmpty(s)) {
                    sb.append(delim + s);
                    delim = ";";
                }
            }

            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ctx)
                    .putString(SRECENTSTATIONS, sb.toString()).apply();

        } catch (Exception e) {
            SSLog.e(TAG, "writeRecentStations ", e);
            e.printStackTrace();

        }
    }

    @Override
    public Filter getFilter() {
        return myFilter;
    }

    Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            try {
                final ArrayList<String> list = new ArrayList<>();
                list.addAll(SearchStationActivity.maRecentStation);
                int count = list.size();
                final ArrayList<String> nlist = new ArrayList<>(count);

                String station;
                if (constraint.length() == 0) {
                    filteredData.addAll(SearchStationActivity.maRecentStation);
                } else {
                    for (int i = 0; i < count; i++) {
                        station = list.get(i);
                        if (org.apache.commons.lang3.StringUtils.containsIgnoreCase(station, constraint)) {

                            nlist.add(station);
                        }
                    }
                    LinkedHashSet<String> lhs = new LinkedHashSet<String>();
                    lhs.addAll(nlist);
                    nlist.clear();
                    nlist.addAll(lhs);


                }
                filterResults.values = nlist;
                filterResults.count = nlist.size();
            } catch (Exception e) {
                e.printStackTrace();
                SSLog.e(TAG, "myFilter: ", e);
            }

            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence contraint,
                                      FilterResults results) {
            try {
                filteredData.clear();
                filteredData = (ArrayList<String>) results.values;
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
//                   filteredData.addAll(maRecentList);
                    notifyDataSetInvalidated();
                }
            } catch (Exception e) {
                Log.d(TAG, "publishResults " + e.toString());
                SSLog.e(TAG, "publishResults: ", e);
            }
        }
    };
}
