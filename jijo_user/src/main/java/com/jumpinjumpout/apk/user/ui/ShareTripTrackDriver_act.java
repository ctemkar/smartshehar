package com.jumpinjumpout.apk.user.ui;

import android.os.Bundle;
import android.widget.Toast;

import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;

/**
 * Created by jijo_soumen on 04/03/2016.
 */
public class ShareTripTrackDriver_act extends TrackDriver_act {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        MyApplication.getInstance();
        hasJoinedTrip = MyApplication.getInstance().getPersistentPreference()
                .getBoolean(Constants_user.PREF_JOINED_TRIP, false);
        if (joinCancel) {

        } else {
            if (!hasJoinedTrip || hasTripEnded) {
                super.onBackPressed();
                return;
            }
        }
        Toast.makeText(this, "Presss home to switch to a different app",
                Toast.LENGTH_SHORT).show();
    }
}
