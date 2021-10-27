package com.jumpinjumpout.apk.driver.ui;

import android.content.Intent;
import android.location.Location;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jumpinjumpout.apk.driver.CGlobals_driver;
import com.jumpinjumpout.apk.lib.ui.ScheduleTripList_act;

import lib.app.util.CAddress;

/**
 * Created by ctemkar on 23/10/2015.
 * Driver speicific code for shceduled trips
 */
public class ScheduleTripListDriver_act extends ScheduleTripList_act {
    @Override
    protected void runScheduleTrip(String startaddress, double startlat, double startlng, String destaddress, double destlat, double destlng) {
        Intent intent = new Intent(ScheduleTripListDriver_act.this, ForHireSharedTrip_act.class);
        CAddress ostartAddress = new CAddress(startaddress, startlat, startlng);
        CAddress odestAddress = new CAddress(destaddress, destlat, destlng);
        Gson gson = new Gson();
        final String json1 = gson.toJson(ostartAddress);
        final String json2 = gson.toJson(odestAddress);

        Location currentLocation = CGlobals_driver.getInstance().getBestLocation(ScheduleTripListDriver_act.this);
        float[] dist = new float[1];
        if (ostartAddress == null) {
            Toast.makeText(ScheduleTripListDriver_act.this, "Use the left menu at the top, go to Settings and set your Home address",
                    Toast.LENGTH_LONG).show();
            return;
        } else {
            if (currentLocation != null) {
                Location.distanceBetween(ostartAddress.getLatitude(), ostartAddress.getLongitude(),
                        odestAddress.getLatitude(),
                        odestAddress.getLongitude(), dist);

                if (dist[0] < 1000) {
                    Toast.makeText(ScheduleTripListDriver_act.this, "You are already close to home",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        if (currentLocation != null) {
            intent.putExtra("SCHEDULETRIP", "1");
            intent.putExtra("START", json1);
            intent.putExtra("DEST", json2);
        } else {
            Toast.makeText(ScheduleTripListDriver_act.this, "Cannot use this feature, location not available!",
                    Toast.LENGTH_LONG).show();

        }
        startActivity(intent);
    }
    protected void newTrip() {
        Intent intent = new Intent(ScheduleTripListDriver_act.this, ForHireSharedTrip_act.class);
        startActivity(intent);
    }

}
