package com.smartshehar.cabe.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.smartshehar.cabe.R;

import java.util.Calendar;
import java.util.Locale;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * chack share cab availability to start and destination point
 * Created by soumen on 23-09-2016.
 */

public class CreateShareTrip_act extends AppCompatActivity {

    private static final String TAG = "CreateShareTrip_act: ";
    TextView tvSetStart, tvSetDestination;
    Button btnCheckAvailability;
    private final int ACT_START_ADDRESS = 17;
    private final int ACT_DESTINATION_ADDRESS = 27;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pointtopointtripdetails);
        init();
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

        btnCheckAvailability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void init() {
        tvSetStart = (TextView) findViewById(R.id.tvSetStart);
        tvSetDestination = (TextView) findViewById(R.id.tvSetDestination);
        btnCheckAvailability = (Button) findViewById(R.id.btnCheckAvailability);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String sShareAddress;
        double dShareAddressLat, dShareAddressLng;
        if (requestCode == ACT_START_ADDRESS) if (resultCode == RESULT_OK) {
            try {
                sShareAddress = data.getStringExtra("ADDRESS");
                dShareAddressLat = data.getDoubleExtra("ADDRESS_LAT", Constants_lib_ss.INVALIDLAT);
                dShareAddressLng = data.getDoubleExtra("ADDRESS_LNG", Constants_lib_ss.INVALIDLNG);
                tvSetStart.setText(sShareAddress);
            } catch (Exception e) {
                SSLog_SS.e(TAG, "ActivityResult: ", e, CreateShareTrip_act.this);
            }

        }
        if (requestCode == ACT_DESTINATION_ADDRESS) {
            if (resultCode == RESULT_OK) {
                try {
                    sShareAddress = data.getStringExtra("ADDRESS");
                    dShareAddressLat = data.getDoubleExtra("ADDRESS_LAT", Constants_lib_ss.INVALIDLAT);
                    dShareAddressLng = data.getDoubleExtra("ADDRESS_LNG", Constants_lib_ss.INVALIDLNG);
                    tvSetDestination.setText(sShareAddress);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "ActivityResult: ", e, CreateShareTrip_act.this);
                }
            }
        }
    }
}
