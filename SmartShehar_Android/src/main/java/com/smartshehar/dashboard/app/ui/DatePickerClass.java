package com.smartshehar.dashboard.app.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import java.util.Calendar;


public class DatePickerClass extends DialogFragment implements
        DatePickerDialog.OnDateSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
// get defualt day, month, and year //
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        // Create a new instance of DatePickerDialog and return it

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        // execute on changing  date
        String sDate = String.valueOf(dayOfMonth) + "-" + (monthOfYear + 1) + "-" + year;
        this.mListener.gotDateResult(sDate); //pass changed date to  interface method

    }
    // define interface
    public static interface OnDateListener {
        public abstract void gotDateResult(String sDate);
    }

    private OnDateListener mListener;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnDateListener)activity;
        }
        catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + "implement OnDateListener");
        }
    }
}

