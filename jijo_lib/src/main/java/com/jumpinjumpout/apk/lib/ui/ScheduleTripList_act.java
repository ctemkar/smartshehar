package com.jumpinjumpout.apk.lib.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.DatabaseHandler;
import com.jumpinjumpout.apk.lib.R;
import com.jumpinjumpout.apk.lib.ScheduleAdapter;

import java.util.ArrayList;

/**
 * Created by Soumen on 09-10-2015.
 * Allows to add/edit scheduled trips that run automatically when app is started
 */
public abstract class ScheduleTripList_act extends Activity {
    private static final String TAG = "ScheduleTripList_act: ";
    Button btnaddSchdhule;
    ListView lvsehduletrip1;
    private final int ACT_RESULT_ADD_SCHDHULE = 1;
    private final int ACT_RESULT_EDIT_SCHEDULE = 2;
    public final int ACT_RESULT_RECENT_TRIP = 11;
    DatabaseHandler db;
    ArrayList<CTrip> alScheduleResult;

    public interface BtnClickListener {
        public abstract void onBtnClick(int position);
        public abstract void onDestinationClick(int position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schdhule_list);
        db = new DatabaseHandler(this);
        init();

        btnaddSchdhule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ScheduleTripList_act.this,
                        ScheduleTripCreate_act.class);
                startActivityForResult(i, ACT_RESULT_ADD_SCHDHULE);
            }
        });

        setListClickListener();


    }

    private void setListClickListener() {
        alScheduleResult = db.getSchdhuleList();
        if (alScheduleResult.size() == 0) {
            Toast.makeText(ScheduleTripList_act.this,
                    "No Schedules. Click add schedule to create a new schedule.",
                    Toast.LENGTH_LONG).show();
        } else {
            lvsehduletrip1.setAdapter(new ScheduleAdapter(ScheduleTripList_act.this, alScheduleResult,
                    new BtnClickListener() {

                        @Override
                        public void onBtnClick(int position) {
                            Log.v(TAG, "Position: " + position);
                            runScheduleTrip(alScheduleResult.get(position).getstart_address(),
                                    Double.parseDouble(alScheduleResult.get(position).getstart_Lat()),
                                    Double.parseDouble(alScheduleResult.get(position).getstart_Lng()),
                                    alScheduleResult.get(position).getdestination_address(),
                                    Double.parseDouble(alScheduleResult.get(position).getdestination_Lat()),
                                    Double.parseDouble(alScheduleResult.get(position).getdestination_Lng()));
                            // Call your function which creates and shows the dialog here
                            //                           changeMoneda(position);
                        }
                        @Override
                        public void onDestinationClick(int position) {
                            Log.v(TAG, "Position: " + position);
                            CTrip sc = alScheduleResult.get(position);
                            Gson gson = new Gson();
                            String json = gson.toJson(sc);
                            Intent i = new Intent(ScheduleTripList_act.this,
                                    ScheduleTripCreate_act.class);
                            i.putExtra("oSchedule", json);
                            startActivityForResult(i, ACT_RESULT_EDIT_SCHEDULE);
                        }


                    }
            ));
        }
        lvsehduletrip1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position,
                                    long arg3) {
                CTrip sc = alScheduleResult.get(position);
                Gson gson = new Gson();
                String json = gson.toJson(sc);
                Intent i = new Intent(ScheduleTripList_act.this,
                        ScheduleTripCreate_act.class);
                i.putExtra("oSchedule", json);
                startActivityForResult(i, ACT_RESULT_EDIT_SCHEDULE);
            }
        });
    }

    private void init() {
        btnaddSchdhule = (Button) findViewById(R.id.btnaddSchdhule);
        lvsehduletrip1 = (ListView) findViewById(R.id.lvsehduletrip1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String sAddr = "";

        if (requestCode == ACT_RESULT_ADD_SCHDHULE) {
            if (resultCode == RESULT_OK) {
                db = new DatabaseHandler(this);
                ArrayList<CTrip> pPResultsList = db.getSchdhuleList();
                if (pPResultsList.size() == 0) {
                    Toast.makeText(ScheduleTripList_act.this, "No Schedule trips created", Toast.LENGTH_LONG).show();
                } else {
                    lvsehduletrip1.setAdapter(new ScheduleAdapter(ScheduleTripList_act.this, pPResultsList));
                    ((ScheduleAdapter) lvsehduletrip1.getAdapter()).notifyDataSetChanged();
                    setListClickListener();

                }
            }
        }
        if (requestCode == ACT_RESULT_EDIT_SCHEDULE) {
            if (resultCode == RESULT_OK) {
                ArrayList<CTrip> pPResultsList = db.getSchdhuleList();
                lvsehduletrip1.setAdapter(new ScheduleAdapter(ScheduleTripList_act.this, pPResultsList));
                setListClickListener();
                ((ScheduleAdapter) lvsehduletrip1.getAdapter()).notifyDataSetChanged();
                if (pPResultsList.size() == 0) {
                    Toast.makeText(ScheduleTripList_act.this, "You have deleted all schedules", Toast.LENGTH_LONG).show();
                }

            }
        }


    } // onActivityResult

    @Override
    protected void onResume() {
        super.onResume();
        setListClickListener();
    }

    public void clickNewTrip(View view) {
        newTrip();
    }

    abstract protected void newTrip();
    abstract protected void runScheduleTrip(String startaddress, double startlat, double startlng, String destaddress, double destlat, double destlng);
}
