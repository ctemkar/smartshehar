package com.smartshehar.dashboard.app.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Locale;


public class TimePickerClass extends DialogFragment implements
        TimePickerDialog.OnTimeSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        try {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // exectue on changing time
        String sTime = String.format(Locale.getDefault(), "%d:%02d",
                hourOfDay, minute);
        this.mListener.gotTimeResult(sTime); //pass selected time to  interface method
    }
    // define interface
    public static interface OnCompleteListener {
        public abstract void gotTimeResult(String sTime);
    }

    private OnCompleteListener mListener;


    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + "implement OnCompleteListener");
        }
    }
}

