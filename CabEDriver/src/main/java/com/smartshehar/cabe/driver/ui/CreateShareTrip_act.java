package com.smartshehar.cabe.driver.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.cabe.driver.Constants_CED;
import com.smartshehar.cabe.driver.DatabaseHandler_CabE;
import com.smartshehar.cabe.driver.Fixed_Address;
import com.smartshehar.cabe.driver.R;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * chack share cab availability to start and destination point
 * Created by soumen on 23-09-2016.
 */

public class CreateShareTrip_act extends AppCompatActivity {

    private static final String TAG = "CreateShareTrip_act: ";
    TextView tvSetStart, tvSetDestination, tvSetTime;
    Button btnCreate;
    private final int ACT_START_ADDRESS = 17;
    private final int ACT_DESTINATION_ADDRESS = 27;
    Fixed_Address fixed_addressFrom, fixed_addressETo;
    DatabaseHandler_CabE db;
    protected ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pointtopointtripdetails);
        init();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        tvSetStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this)
                        .putInt("SAVE_ACTIVITY_RESULT_VALUE", 1);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this).commit();
                Intent intent = new Intent(CreateShareTrip_act.this, ListOfPointAddress_act.class);
                startActivityForResult(intent, ACT_START_ADDRESS);
            }
        });

        tvSetDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this)
                        .putInt("SAVE_ACTIVITY_RESULT_VALUE", 2);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this).commit();
                Intent intent = new Intent(CreateShareTrip_act.this, ListOfPointAddress_act.class);
                startActivityForResult(intent, ACT_DESTINATION_ADDRESS);
            }
        });

        tvSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "timePicker");
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startAdd = tvSetStart.getText().toString();
                String destinationAdd = tvSetDestination.getText().toString();
                String sTime = tvSetTime.getText().toString();
                if (TextUtils.isEmpty(startAdd) || TextUtils.isEmpty(destinationAdd) || TextUtils.isEmpty(sTime)) {
                    Toast.makeText(CreateShareTrip_act.this, "Please set all Text", Toast.LENGTH_LONG).show();
                } else {
                    createTrip();
                }
            }
        });
    }

    private void init() {
        tvSetStart = (TextView) findViewById(R.id.tvSetStart);
        tvSetDestination = (TextView) findViewById(R.id.tvSetDestination);
        tvSetTime = (TextView) findViewById(R.id.tvSetTime);
        btnCreate = (Button) findViewById(R.id.btnCreate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // MyService Broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageBroadcastReceiverDashBoard,
                new IntentFilter(Constants_CED.ERVICE_DRIVER_ALL_PHP));
        Intent intentResponseService = new Intent(Constants_CED.ERVICE_DRIVER_ALL_PHP);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentResponseService);
        CGlobals_lib_ss.getInstance().countError1 = 1;
        CGlobals_lib_ss.getInstance().countError2 = 1;
        CGlobals_lib_ss.getInstance().countError3 = 1;
        CGlobals_lib_ss.getInstance().countError4 = 1;
        CGlobals_lib_ss.getInstance().countError5 = 1;
        CGlobals_lib_ss.getInstance().countError6 = 1;
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageBroadcastReceiverDashBoard);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "onPause: ", e, CreateShareTrip_act.this);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CreateShareTrip_act.this, DashBoard_Driver_act.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String sAddr = "";
        if (requestCode == ACT_START_ADDRESS) if (resultCode == RESULT_OK) {
            try {
                sAddr = data.getStringExtra("add");
                if (!TextUtils.isEmpty(sAddr)) {
                    Type type = new TypeToken<Fixed_Address>() {
                    }.getType();
                    fixed_addressFrom = new Gson().fromJson(sAddr, type);
                    tvSetStart.setText(fixed_addressFrom.getArea() + " "
                            + fixed_addressFrom.getFormatted_Address()
                            + " (" + fixed_addressFrom.getPick_Drop_Point() + ")");
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this)
                            .putString(Constants_CED.PREF_DRIVER_FIXED_ADDRESS_FROM, sAddr);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this).commit();
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "ActivityResult: ", e, CreateShareTrip_act.this);
            }

        }
        if (requestCode == ACT_DESTINATION_ADDRESS) {
            if (resultCode == RESULT_OK) {
                try {
                    sAddr = data.getStringExtra("add");
                    if (!TextUtils.isEmpty(sAddr)) {
                        Type type = new TypeToken<Fixed_Address>() {
                        }.getType();
                        fixed_addressETo = new Gson().fromJson(sAddr, type);
                        tvSetDestination.setText(fixed_addressETo.getArea() + " "
                                + fixed_addressETo.getFormatted_Address()
                                + " (" + fixed_addressETo.getPick_Drop_Point() + ")");
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this)
                                .putString(Constants_CED.PREF_DRIVER_FIXED_ADDRESS_TO, sAddr);
                        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this).commit();
                    }
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "ActivityResult: ", e, CreateShareTrip_act.this);
                }
            }
        }
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
            ((CreateShareTrip_act) getActivity()).tvSetTime.setText(sTime);
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(getActivity())
                    .putString(Constants_CED.PREF_DRIVER_FIXED_SHARE_DATE_TIME, df.format(startDateTime.getTime()));
            CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(getActivity()).commit();
        }
    }

    public void createTrip() {
        try {
            progressMessage("please wait...");
            String fromaddress = CGlobals_lib_ss.getInstance().getPersistentPreference(CreateShareTrip_act.this)
                    .getString(Constants_CED.PREF_DRIVER_FIXED_ADDRESS_FROM, "");
            String toaddress = CGlobals_lib_ss.getInstance().getPersistentPreference(CreateShareTrip_act.this)
                    .getString(Constants_CED.PREF_DRIVER_FIXED_ADDRESS_TO, "");
            if (TextUtils.isEmpty(fromaddress) || TextUtils.isEmpty(toaddress)) {
                Toast.makeText(CreateShareTrip_act.this, "Enter Start and Destination ", Toast.LENGTH_LONG).show();
                return;
            }
            Type type = new TypeToken<Fixed_Address>() {
            }.getType();
            fixed_addressFrom = new Gson().fromJson(fromaddress, type);
            fixed_addressETo = new Gson().fromJson(toaddress, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String sShareDateTime = CGlobals_lib_ss.getInstance().getPersistentPreference(CreateShareTrip_act.this)
                .getString(Constants_CED.PREF_DRIVER_FIXED_SHARE_DATE_TIME, "");
        final String url = Constants_CED.TRIP_CREATE_DRIVER_URL;
        final StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SSLog_SS.d("ResponseTripId  ", response);
                        createTripResponse(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                String sDateTime = df.format(cal.getTime());
                Location location = CGlobals_lib_ss.getInstance().getMyLocation(CreateShareTrip_act.this);
                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(CreateShareTrip_act.this)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                db = new DatabaseHandler_CabE(CreateShareTrip_act.this);
                db.addCreateTrip(location, appuserid, Constants_CED.TRIP_TYPE_SHARE_DRIVER, Constants_CED.TRIP_ACTION_CREATE,
                        CGlobals_lib_ss.msGmail, sDateTime, fixed_addressFrom.getFormatted_Address(),
                        fixed_addressFrom.getLocality(),
                        Double.toString(fixed_addressFrom.getLatitude()),
                        Double.toString(fixed_addressFrom.getLongitude()),
                        fixed_addressETo.getFormatted_Address(), fixed_addressETo.getLocality(),
                        Double.toString(fixed_addressETo.getLatitude()),
                        Double.toString(fixed_addressETo.getLongitude()), sShareDateTime);
                try {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "createTrip - ", error, CreateShareTrip_act.this);
                    }
                } catch (Exception e) {
                    if (CGlobals_lib_ss.getInstance().checkerror(error)) {
                        Log.e(TAG, "");
                    } else {
                        SSLog_SS.e(TAG, "createTrip - ", e, CreateShareTrip_act.this);
                    }
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripaction", Constants_CED.TRIP_ACTION_CREATE);
                params.put("triptype", Constants_CED.TRIP_TYPE_SHARE_DRIVER);
                params.put("fromaddress", fixed_addressFrom.getFormatted_Address());
                params.put("fromsublocality", fixed_addressFrom.getLocality());
                params.put("fromlat", Double.toString(fixed_addressFrom.getLatitude()));
                params.put("fromlng", Double.toString(fixed_addressFrom.getLongitude()));
                params.put("toaddress", fixed_addressETo.getFormatted_Address());
                params.put("tosublocality", fixed_addressETo.getLocality());
                params.put("tolat", Double.toString(fixed_addressETo.getLatitude()));
                params.put("tolng", Double.toString(fixed_addressETo.getLongitude()));
                if (!TextUtils.isEmpty(sShareDateTime)) {
                    params.put("plannedstartdatetime", sShareDateTime);
                }
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        url, getApplicationContext());
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, CreateShareTrip_act.this);

    } // createTrip

    protected void createTripResponse(String response) {
        int iTripId;
        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            progressCancel();
            return;
        } else {
            System.out.println("response createTripResponse   " + response);
            try {
                JSONObject oJson = new JSONObject(response);
                iTripId = oJson.isNull("trip_id") ? -1 : Integer.parseInt(oJson
                        .getString("trip_id"));
                if (iTripId > 0) {
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this)
                            .putInt(Constants_CED.PREF_TRIP_ID_INT, iTripId);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this)
                            .commit();
                }
                progressCancel();
                Intent intent = new Intent(CreateShareTrip_act.this, CabEMainDriver_act.class);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this).
                        putString(Constants_CED.CAB_TRIP_USER_TYPE_DRIVER, Constants_CED.TRIP_TYPE_SHARE_DRIVER);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this)
                        .putInt(Constants_CED.PERF_SET_BUSY_CREATE_TRIP_DRIVER, 2);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this).
                        putString(Constants_CED.PREF_SAVE_RESPONSE_PASSENGER, "");
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this).
                        putBoolean(Constants_CED.PREF_ISIN_TRIP_MAIN, true);
                CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(CreateShareTrip_act.this).commit();
                startActivity(intent);
                finish();
            } catch (Exception e) {
                progressCancel();
                SSLog_SS.e(TAG, "createTripResponse -  ", e, CreateShareTrip_act.this);
            }
        }
    } // createTripResponse

    public BroadcastReceiver mMessageBroadcastReceiverDashBoard = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String response = intent.getStringExtra("newresponse");
            int errorvalue = intent.getIntExtra("newerrorvalue", 0);
            if (errorvalue == 2) {
                createTripResponse(response);
            }
        }
    };

    protected void progressCancel() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }

    }

    protected void progressMessage(String msg) {
        if (!isFinishing()) {
            mProgressDialog.setMessage(msg);
            mProgressDialog.show();
        }
    }
}
