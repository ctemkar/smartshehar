package com.jumpinjumpout.apk.lib.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.gson.Gson;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.DatabaseHandler;
import com.jumpinjumpout.apk.lib.R;
import com.jumpinjumpout.apk.lib.RecentTripAdapter;
import java.util.ArrayList;

public abstract class RecentTrip_act extends Activity {

    protected static final String TAG = "RecentTrips_act: ";
    private ArrayList<CTrip> maoRecentTrips = new ArrayList<CTrip>();
    private ArrayList<CTrip> maoScheduleTrips = new ArrayList<CTrip>();
    protected ListView mLvReSentTrip;
    protected LinearLayout noTrip_List;
    FloatingActionButton new_trip;
    DatabaseHandler db;
    private final int ACT_RESULT_EDIT_RECENTSCHEDULE = 2345;
    private final int ACT_RESULT_ADD_RECENTSCHDHULE = 111654;


    public interface BtnClickListener {
        public abstract void onBtnClick(int position);

        public abstract void onTVMakeSchedule(int position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recent_floatingaction);
        db = new DatabaseHandler(this);
        mLvReSentTrip = (ListView) findViewById(R.id.lvReSentTrip);
        noTrip_List = (LinearLayout) findViewById(R.id.noTrip_List);
        new_trip = (FloatingActionButton) findViewById(R.id.new_trip);
        new_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newTrip();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListClickListener();
    }

    public void setListClickListener() {
        maoRecentTrips = CGlobals_lib.getInstance().readRecentTrip(RecentTrip_act.this);
        maoScheduleTrips = db.getSchdhuleList();
        for (int i = 0; i < maoScheduleTrips.size(); i++) {
            maoRecentTrips.add(i, maoScheduleTrips.get(i));
        }
        if (maoRecentTrips.size() == 0) {
            mLvReSentTrip.setVisibility(View.GONE);
            noTrip_List.setVisibility(View.VISIBLE);
            return;
        }
        mLvReSentTrip.setVisibility(View.VISIBLE);
        noTrip_List.setVisibility(View.GONE);
        mLvReSentTrip.setAdapter(new RecentTripAdapter(RecentTrip_act.this,
                maoRecentTrips, new BtnClickListener() {
            @Override
            public void onBtnClick(int position) {
                if (!TextUtils.isEmpty(maoRecentTrips.get(position).getFrom())) {
                    runResentTrip(maoRecentTrips.get(position).getFrom(),
                            maoRecentTrips.get(position).getTo(),
                            maoRecentTrips.get(position).getFromLat(),
                            maoRecentTrips.get(position).getFromLng(),
                            maoRecentTrips.get(position).getToLat(),
                            maoRecentTrips.get(position).getToLng());
                } else if (!TextUtils.isEmpty(maoRecentTrips.get(position).getstart_address())) {
                    runResentTrip(maoRecentTrips.get(position).getstart_address(),
                            maoRecentTrips.get(position).getdestination_address(),
                            Double.parseDouble(maoRecentTrips.get(position).getstart_Lat()),
                            Double.parseDouble(maoRecentTrips.get(position).getstart_Lng()),
                            Double.parseDouble(maoRecentTrips.get(position).getdestination_Lat()),
                            Double.parseDouble(maoRecentTrips.get(position).getdestination_Lng()));
                }
            }

            @Override
            public void onTVMakeSchedule(int position) {
                addSchedule(maoRecentTrips.get(position));
            }
        }));

        mLvReSentTrip.setAdapter(new RecentTripAdapter(RecentTrip_act.this, maoRecentTrips,
                new BtnClickListener() {

                    @Override
                    public void onBtnClick(int position) {
                        Log.v(TAG, "Position: " + position);
                        if (!TextUtils.isEmpty(maoRecentTrips.get(position).getstart_address())) {
                            runResentTrip(maoRecentTrips.get(position).getstart_address(),
                                    maoRecentTrips.get(position).getdestination_address(),
                                    Double.parseDouble(maoRecentTrips.get(position).getstart_Lat()),
                                    Double.parseDouble(maoRecentTrips.get(position).getstart_Lng()),
                                    Double.parseDouble(maoRecentTrips.get(position).getdestination_Lat()),
                                    Double.parseDouble(maoRecentTrips.get(position).getdestination_Lng()));
                        } else if (!TextUtils.isEmpty(maoRecentTrips.get(position).getFrom())) {
                            runResentTrip(maoRecentTrips.get(position).getFrom(),
                                    maoRecentTrips.get(position).getTo(),
                                    maoRecentTrips.get(position).getFromLat(),
                                    maoRecentTrips.get(position).getFromLng(),
                                    maoRecentTrips.get(position).getToLat(),
                                    maoRecentTrips.get(position).getToLng());
                        }
                    }

                    @Override
                    public void onTVMakeSchedule(int position) {
                        addSchedule(maoRecentTrips.get(position));
                    }
                }
        ));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACT_RESULT_ADD_RECENTSCHDHULE) {
            if (resultCode == RESULT_OK) {
                db = new DatabaseHandler(this);
                setListClickListener();
                ((RecentTripAdapter) mLvReSentTrip.getAdapter()).notifyDataSetChanged();
            }
        }
        if (requestCode == ACT_RESULT_EDIT_RECENTSCHEDULE) {
            if (resultCode == RESULT_OK) {
                db = new DatabaseHandler(this);
                setListClickListener();
                ((RecentTripAdapter) mLvReSentTrip.getAdapter()).notifyDataSetChanged();
            }
        }
    } // onActivityResult

    public void addSchedule(CTrip maoRecentTrips22) {
        if (!TextUtils.isEmpty(maoRecentTrips22.getFrom())) {
            Gson gson = new Gson();
            String json = gson.toJson(maoRecentTrips22);
            Intent i = new Intent(RecentTrip_act.this,
                    ScheduleTripCreate_act.class);
            i.putExtra("RECENT_SCHEDULE_TRIP", json);
            i.putExtra("RECENT_SCHEDULE_TRIP_VALUE", "1");
            startActivityForResult(i, ACT_RESULT_ADD_RECENTSCHDHULE);
        } else if (!TextUtils.isEmpty(maoRecentTrips22.getstart_address())) {
            Gson gson1 = new Gson();
            String json1 = gson1.toJson(maoRecentTrips22);
            Intent i1 = new Intent(RecentTrip_act.this,
                    ScheduleTripCreate_act.class);
            i1.putExtra("oSchedule", json1);
            startActivityForResult(i1, ACT_RESULT_EDIT_RECENTSCHEDULE);
        }
    }

    abstract protected void newTrip();

    protected abstract void runResentTrip(String sFrom, String sTo, double dfromLat, double dfromLng, double dtoLat, double dtoLng);
}