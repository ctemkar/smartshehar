package com.smartshehar.android.app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smartshehar.dashboard.app.R;

import java.util.ArrayList;


public class TrainFaresAdapter extends BaseAdapter {
    private static final String TAG = "StationAdapter";
    Context ctx;
    private ArrayList<DBHelperTrain.Fare> maFares;

    public TrainFaresAdapter(Context ctx, ArrayList<DBHelperTrain.Fare> maFares) {
        this.ctx = ctx;
        this.maFares = maFares;
    }


    @Override
    public int getCount() {
        return maFares.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        View row;
        row = inflater.inflate(R.layout.farerow, parent, false);
        try {

            TextView linecode, tostation, fare, fare1st, farepass1m,
                    farepass3m, farepass1m1st, farepass3m1st, farepass6m, farepass6m1st,
                    farepass1y, farepass1y1st;
            linecode = (TextView) row.findViewById(R.id.linecode);
            tostation = (TextView) row.findViewById(R.id.tostation);
            fare = (TextView) row.findViewById(R.id.fare);
            fare1st = (TextView) row.findViewById(R.id.fare1st);
            farepass1m = (TextView) row.findViewById(R.id.farepass1m);
            farepass3m = (TextView) row.findViewById(R.id.farepass3m);
            farepass1m1st = (TextView) row.findViewById(R.id.farepass1m1st);
            farepass3m1st = (TextView) row.findViewById(R.id.farepass3m1st);
            farepass6m = (TextView) row.findViewById(R.id.farepass6m);
            farepass6m1st = (TextView) row.findViewById(R.id.farepass6m1st);
            farepass1y = (TextView) row.findViewById(R.id.farepass1y);
            farepass1y1st = (TextView) row.findViewById(R.id.farepass1y1st);


            linecode.setText(maFares.get(position).msLineCode);
            tostation.setText(maFares.get(position).msTo);

            fare.setText(maFares.get(position).msFare);
            fare1st.setText(maFares.get(position).msFare1st);
            farepass1m.setText(maFares.get(position).msFarePass1m);
            farepass3m.setText(maFares.get(position).msFarePass3m);
            farepass1m1st.setText(maFares.get(position).msFarePass1m1st);
            farepass3m1st.setText(maFares.get(position).msFarePass3m1st);
            farepass6m.setText(maFares.get(position).msFarePass6m);
            farepass6m1st.setText(maFares.get(position).msFarePass6m1st);
            farepass1y.setText(maFares.get(position).msFarePass1y);
            farepass1y1st.setText(maFares.get(position).msFarePass1y1st);

        } catch (Exception e) {
            Log.d(TAG, "Er in get view " + e.toString());
            e.printStackTrace();
        }

        return row;
    }
}
