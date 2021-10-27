package com.jumpinjumpout.apk.user.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;

import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.user.Constants_user;

abstract public class TripsList_frag extends ListFragment {

    protected String msActiveMessage = "", msNoActiveMessage = "";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    void setNoUserMessage(String sTriptype) {
        try {
            if (sTriptype.equals(Constants_user.TRIP_TYPE_USER)) {
                msActiveMessage = getActivity().getString(R.string.activeUsers);
                msNoActiveMessage = getActivity().getString(
                        R.string.findjumpin);
            } else {
                msActiveMessage = getActivity().getString(
                        R.string.availableCabs);
                msNoActiveMessage = getActivity().getString(R.string.findjumpin);
            }
        } catch (Exception e) {

        }
    }
}