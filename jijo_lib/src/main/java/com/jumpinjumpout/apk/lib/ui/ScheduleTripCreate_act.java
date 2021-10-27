package com.jumpinjumpout.apk.lib.ui;


import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.DatabaseHandler;
import com.jumpinjumpout.apk.lib.R;
import com.jumpinjumpout.apk.lib.SSLog;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CAddress;


/**
 * Created by user pc on 09-10-2015.
 */
public class ScheduleTripCreate_act extends Activity {
    private static final String TAG = "ScheduleTripCreate_act: ";
    TextView mTvFrom, mTvTo;
    TextView mTvSetTime;
    CheckBox tbSun, tbMon, cbTue, cbWed, tbThu, tbFri, cbSat;
    Button btnSubmitSchedule, btnCancelSchedule;
    public static double fromLat = Constants_lib.INVALIDLAT,
            fromLng = Constants_lib.INVALIDLNG;

    public static double toLat = Constants_lib.INVALIDLAT,
            toLng = Constants_lib.INVALIDLNG;
    private final int ACTRESULT_FROM = 11;
    private final int ACTRESULT_TO = 22;
    private int rowId;
    int sun = 0, mon = 0, tue = 0, wed = 0, thu = 0, fri = 0, sat = 0, miScheduleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schdhule_act);
        init();
        CGlobals_lib.getInstance().maoRecentAddress = CGlobals_lib.getInstance()
                .readRecentAddresses(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String datas = extras.getString("oSchedule");
            if (extras.getString("RECENT_SCHEDULE_TRIP_VALUE") != null) {
                if (extras.getString("RECENT_SCHEDULE_TRIP_VALUE").equals("1")) {
                    String recentSchedule = extras.getString("RECENT_SCHEDULE_TRIP");
                    findViewById(R.id.btnDelete).setVisibility(View.GONE);
                    findViewById(R.id.btnSubmitSchedule).setVisibility(View.VISIBLE);
                    Gson gson = new Gson();
                    Type type = new TypeToken<CTrip>() {
                    }.getType();
                    CTrip sc = gson.fromJson(recentSchedule, type);
                    mTvFrom.setText(sc.getFrom());
                    mTvTo.setText(sc.getTo());
                    fromLat = sc.getFromLat();
                    fromLng = sc.getFromLng();
                    toLat = sc.getToLat();
                    toLng = sc.getToLng();
                }
            } else if (datas != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<CTrip>() {
                }.getType();
                CTrip sc = gson.fromJson(datas, type);
                rowId = sc.getId();
                mTvFrom.setText(sc.getstart_address());
                mTvTo.setText(sc.getdestination_address());
                fromLat = Double.parseDouble(sc.getstart_Lat());
                fromLng = Double.parseDouble(sc.getstart_Lng());
                toLat = Double.parseDouble(sc.getdestination_Lat());
                toLng = Double.parseDouble(sc.getdestination_Lng());
                mTvSetTime.setText(sc.getStime());
                mTvSetTime.setClickable(false);
                mTvFrom.setClickable(false);
                mTvTo.setClickable(false);
                boolean sel = sc.getsun();
                if (sel) {
                    tbSun.setChecked(true);
                }
                sel = sc.getmon();
                if (sel) {
                    tbMon.setChecked(true);
                }
                sel = sc.gettue();
                if (sel) {
                    cbTue.setChecked(true);
                }
                sel = sc.getwed();
                if (sel) {
                    cbWed.setChecked(true);
                }
                sel = sc.getthu();
                if (sel) {
                    tbThu.setChecked(true);
                }
                sel = sc.getfri();
                if (sel) {
                    tbFri.setChecked(true);
                }
                sel = sc.getsat();
                if (sel) {
                    cbSat.setChecked(true);
                }
                tbSun.setClickable(false);
                tbMon.setClickable(false);
                cbTue.setClickable(false);
                cbWed.setClickable(false);
                tbThu.setClickable(false);
                tbFri.setClickable(false);
                cbSat.setClickable(false);
                findViewById(R.id.btnSubmitSchedule).setVisibility(View.GONE);
                findViewById(R.id.btnCancelSchedule).setVisibility(View.GONE);
            } else {
                findViewById(R.id.btnDelete).setVisibility(View.GONE);
            }

        } else {
            findViewById(R.id.btnDelete).setVisibility(View.GONE);
        }
        mTvSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });

        mTvTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ScheduleTripCreate_act.this,
                        SearchAddress_act.class);
                startActivityForResult(i, ACTRESULT_TO);
            }
        });

        tbSun.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sun = 1;
                } else {
                    sun = 0;
                }
            }
        });

        tbMon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mon = 1;
                } else {
                    mon = 0;
                }
            }
        });

        cbTue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tue = 1;
                } else {
                    tue = 0;
                }
            }
        });

        cbWed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wed = 1;
                } else {
                    wed = 0;
                }
            }
        });

        tbThu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    thu = 1;
                } else {
                    thu = 0;
                }
            }
        });

        tbFri.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fri = 1;
                } else {
                    fri = 0;
                }
            }
        });

        cbSat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sat = 1;
                } else {
                    sat = 0;
                }
            }
        });
    }

    @Override
    protected void onPause() {
        CGlobals_lib.getInstance()
                .writeRecentAddresses(getApplicationContext());
        super.onPause();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String sAddr = "";
        CAddress oAddr = null;

        if (requestCode == ACTRESULT_FROM) {
            if (resultCode == RESULT_OK) {
                sAddr = data.getStringExtra("street_address");
                fromLat = data.getDoubleExtra("lat", Constants_lib.INVALIDLAT);
                fromLng = data.getDoubleExtra("lng", Constants_lib.INVALIDLNG);
                oAddr = new CAddress();
                oAddr.setAddress(sAddr);
                oAddr.setLatitude(fromLat);
                oAddr.setLongitude(fromLng);
                mTvFrom.setText(sAddr);
            }
            if (resultCode == RESULT_CANCELED) {
                // Write your code if there's no result
            }
        }
        if (requestCode == ACTRESULT_TO) {
            if (resultCode == RESULT_OK) {

                sAddr = data.getStringExtra("street_address");
                toLat = data.getDoubleExtra("lat", Constants_lib.INVALIDLAT);
                toLng = data.getDoubleExtra("lng", Constants_lib.INVALIDLNG);
                oAddr = new CAddress();
                oAddr.setAddress(sAddr);
                oAddr.setLatitude(toLat);
                oAddr.setLongitude(toLng);
                mTvTo.setText(sAddr);
            }
            if (resultCode == RESULT_CANCELED) {
                // Write your code if there's no result
            }
        }
        if (!TextUtils.isEmpty(sAddr)) {
            CGlobals_lib.getInstance().addRecentAddress(oAddr);
        }

    } // onActivityResult

    public void onClickSubmit(View view) {
        DatabaseHandler db = new DatabaseHandler(this);
        String start = mTvFrom.getText().toString();
        String destination = mTvTo.getText().toString();
        String time = CGlobals_lib.getInstance().getSharedPreferences(ScheduleTripCreate_act.this).
                getString("plannedstartdatetime", "");
        if (TextUtils.isEmpty(start)) {
            Toast.makeText(ScheduleTripCreate_act.this, "Enter start address", Toast.LENGTH_LONG).show();
            return;
        } else if (TextUtils.isEmpty(destination)) {
            Toast.makeText(ScheduleTripCreate_act.this, "Enter destination address", Toast.LENGTH_LONG).show();
            return;
        } else if (TextUtils.isEmpty(time)) {
            Toast.makeText(ScheduleTripCreate_act.this, "Enter Time", Toast.LENGTH_LONG).show();
            return;
        } else if (TextUtils.isEmpty(time) && TextUtils.isEmpty(destination) && TextUtils.isEmpty(start)) {
            Toast.makeText(ScheduleTripCreate_act.this, "Please enter all fields", Toast.LENGTH_LONG).show();
            return;
        } else {
            String currentscheduledatetime = CGlobals_lib.getInstance().getSharedPreferences(ScheduleTripCreate_act.this)
                    .getString("plannedstartdatetime", "");
            SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat formatter1 = new SimpleDateFormat("HH:mm");
            SimpleDateFormat postFormater = new SimpleDateFormat("yyyy-MM-dd");
            long currentTime = -1;
            String scheduleTodayDate = "", scheduleTodayDate1 = "";
            Date date1, date;
            try {

                date = curFormaterDB.parse(currentscheduledatetime);
                scheduleTodayDate1 = formatter1.format(date);
                currentTime = formatter1.parse(scheduleTodayDate1).getTime(); // Db schedule time

                date1 = curFormaterDB.parse(currentscheduledatetime);
                scheduleTodayDate = postFormater.format(date1);
            } catch (Exception e) {
                SSLog.e(TAG, "(checkScheduledTrip) ", e);
            }
            db.schdhuleTripInsert(start, String.valueOf(fromLat), String.valueOf(fromLng), destination, String.valueOf(toLat),
                    String.valueOf(toLng), time, sun, mon, tue, wed, thu, fri, sat, (int) currentTime, scheduleTodayDate);
            Intent intent = new Intent();
            addTripScheduleServer(start, String.valueOf(fromLat), String.valueOf(fromLng), destination, String.valueOf(toLat),
                    String.valueOf(toLng), time, sun, mon, tue, wed, thu, fri, sat);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    public void onClickCancel(View view) {
        finish();
    }

    public void onClickDelete(View view) {
        DatabaseHandler db = new DatabaseHandler(this);
        db.deleteSchedule(rowId);
        deleteSchedule();
        Toast.makeText(ScheduleTripCreate_act.this, "Schedule row deleted", Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void init() {
        mTvFrom = (TextView) findViewById(R.id.etStart);
        mTvTo = (TextView) findViewById(R.id.etDestination);
        mTvSetTime = (TextView) findViewById(R.id.tvSetTime);
        tbSun = (CheckBox) findViewById(R.id.cbSun);
        tbSun.setClickable(true);
        tbMon = (CheckBox) findViewById(R.id.cbMon);
        tbMon.setClickable(true);
        cbTue = (CheckBox) findViewById(R.id.cbTue);
        cbTue.setClickable(true);
        cbWed = (CheckBox) findViewById(R.id.cbWed);
        cbWed.setClickable(true);
        tbThu = (CheckBox) findViewById(R.id.cbThu);
        tbThu.setClickable(true);
        tbFri = (CheckBox) findViewById(R.id.cbFri);
        tbFri.setClickable(true);
        cbSat = (CheckBox) findViewById(R.id.cbSat);
        cbSat.setClickable(true);
        btnSubmitSchedule = (Button) findViewById(R.id.btnSubmitSchedule);
        btnCancelSchedule = (Button) findViewById(R.id.btnCancelSchedule);

    }

    public void onClickStart(View view) {
        Intent i = new Intent(ScheduleTripCreate_act.this,
                SearchAddress_act.class);
        startActivityForResult(i, ACTRESULT_FROM);

    }

    public void onClickDestination(View view) {
        Intent i = new Intent(ScheduleTripCreate_act.this,
                SearchAddress_act.class);
        startActivityForResult(i, ACTRESULT_TO);

    }

    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {
        private Calendar startDateTime;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            DialogFragment dialogFragment = new DialogFragment();
            dialogFragment.setCancelable(false);
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String sTime = String.format(Locale.getDefault(), "%d:%02d",
                    hourOfDay, minute);

            startDateTime = Calendar.getInstance();
            startDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            startDateTime.set(Calendar.MINUTE, minute);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault());
            if (1 <= minute && minute < 9) {
                if (hourOfDay > 12) {
                    ((ScheduleTripCreate_act) getActivity()).mTvSetTime.setText(String.valueOf(hourOfDay - 12) + ":0" + (String.valueOf(minute) + " pm"));
                }
                if (hourOfDay == 12) {
                    ((ScheduleTripCreate_act) getActivity()).mTvSetTime.setText("12" + ":0" + (String.valueOf(minute) + " pm"));
                }
                if (hourOfDay < 12) {
                    if (hourOfDay != 0)
                        ((ScheduleTripCreate_act) getActivity()).mTvSetTime.setText(String.valueOf(hourOfDay) + ":0" + (String.valueOf(minute) + " am"));
                    else
                        ((ScheduleTripCreate_act) getActivity()).mTvSetTime.setText("12" + ":0" + (String.valueOf(minute) + " am"));
                }
            } else {
                if (hourOfDay > 12) {
                    ((ScheduleTripCreate_act) getActivity()).mTvSetTime.setText(String.valueOf(hourOfDay - 12) + ":" + (String.valueOf(minute) + " pm"));
                }
                if (hourOfDay == 12) {
                    ((ScheduleTripCreate_act) getActivity()).mTvSetTime.setText("12" + ":" + (String.valueOf(minute) + " pm"));
                }
                if (hourOfDay < 12) {
                    if (hourOfDay != 0)
                        ((ScheduleTripCreate_act) getActivity()).mTvSetTime.setText(String.valueOf(hourOfDay) + ":" + (String.valueOf(minute) + " am"));
                    else
                        ((ScheduleTripCreate_act) getActivity()).mTvSetTime.setText("12" + ":" + (String.valueOf(minute) + " am"));
                }
            }
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getActivity()).
                    putString("plannedstartdatetime", df.format(startDateTime.getTime()));
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getActivity()).
                    putString("PSRFE_SET_SCHEDULE_TIME", sTime);
            CGlobals_lib.getInstance().getSharedPreferencesEditor(getActivity()).commit();
        }
    }

    public void addTripScheduleServer(final String start, final String fromlat, final String fromlng, final String destination,
                                      final String destlat, final String destlng, String ctime,
                                      final int sun, final int mon, final int tue, final int wed, final int thu, final int fri, final int sat) {
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());

        final String url = Constants_lib.ADD_TRIP_SCHEDULE_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }
                        JSONObject jResponse;
                        try {
                            jResponse = new JSONObject(response);
                            miScheduleId = jResponse.getInt("schedule_id");
                            CGlobals_lib.getInstance().getSharedPreferencesEditor(ScheduleTripCreate_act.this).
                                    putInt(Constants_lib.PREF_SCHEDULE_ID, miScheduleId);
                            CGlobals_lib.getInstance().getSharedPreferencesEditor(ScheduleTripCreate_act.this).commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {

                    SSLog.e(TAG, url
                                    + ": Response.ErrorListener - ",
                            error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG,
                            " addTripScheduleServer Response.ErrorListener (2) - ",
                            e);
                }
            }

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("startaddress", start);
                params.put("startlat", fromlat);
                params.put("startlng", fromlng);
                params.put("destinationaddress", destination);
                params.put("destlat", destlat);
                params.put("destlng", destlng);
                params.put("sun", String.valueOf(sun));
                params.put("mon", String.valueOf(mon));
                params.put("tue", String.valueOf(tue));
                params.put("wed", String.valueOf(wed));
                params.put("thu", String.valueOf(thu));
                params.put("fri", String.valueOf(fri));
                params.put("sat", String.valueOf(sat));
                String pTime = CGlobals_lib.getInstance().getSharedPreferences(ScheduleTripCreate_act.this).
                        getString("plannedstartdatetime", "");
                params.put("scheduledatetime", pTime);


                params = CGlobals_lib.getInstance().getBasicMobileParams(params,
                        url, ScheduleTripCreate_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }
                @SuppressWarnings("unused")
                String debugUrl = "";
                try {
                    debugUrl = url + "?" + getParams.toString() + "&verbose=Y";
                    System.out.println("debugUrl" + debugUrl);
                } catch (Exception e) {
                    SSLog.e(TAG, "getPassengers map - ", e);
                }

                return CGlobals_lib.getInstance().checkParams(params);
            }
        };
        try {
            CGlobals_lib.getInstance().addVolleyRequest(postRequest, false, ScheduleTripCreate_act.this);

        } catch (Exception e) {
            SSLog.e(TAG, "getActiveuser CGlobals.getInstance().mVolleyReq..", e);
        }
    }

    private void deleteSchedule() {
        final int sScheduleId = CGlobals_lib.getInstance().getSharedPreferences(ScheduleTripCreate_act.this).
                getInt(Constants_lib.PREF_SCHEDULE_ID, 0);
        final String url = Constants_lib.DELETE_TRIP_SCHEDULE_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    SSLog.e(TAG, url
                                    + ": Response.ErrorListener - ",
                            error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG,
                            " addTripScheduleServer Response.ErrorListener (2) - ",
                            e);
                }
            }

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("scheduleid", String.valueOf(sScheduleId));
                params = CGlobals_lib.getInstance().getBasicMobileParams(params,
                        url, ScheduleTripCreate_act.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();

                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }
                String debugUrl = "";
                try {
                    debugUrl = url + "?" + getParams.toString() + "&verbose=Y";
                    System.out.println("debugUrl" + debugUrl);
                } catch (Exception e) {
                    SSLog.e(TAG, "getPassengers map - ", e);
                }

                return CGlobals_lib.getInstance().checkParams(params);
            }
        };
        try {
            CGlobals_lib.getInstance().addVolleyRequest(postRequest, false, ScheduleTripCreate_act.this);
        } catch (Exception e) {
            SSLog.e(TAG, "getActiveuser CGlobals.getInstance().mVolleyReq..", e);
        }
    }


}
