package com.jumpinjumpout.apk.driver.ui;

import android.content.Intent;
import android.view.View;

import com.google.gson.Gson;
import com.jumpinjumpout.apk.driver.Constants_driver;
import com.jumpinjumpout.apk.lib.ui.RecentTrip_act;

import lib.app.util.CAddress;

/**
 * Created by user pc on 30-11-2015.
 */
public class RecentTripDriver_act extends RecentTrip_act {

    @Override
    protected void runResentTrip(String sFrom, String sTo, double dfromLat, double dfromLng, double dtoLat, double dtoLng) {
        Intent intent = new Intent(RecentTripDriver_act.this, ForHireSharedTrip_act.class);
        CAddress oFromAddress = new CAddress(sFrom, dfromLat, dfromLng);
        CAddress oToAddress = new CAddress(sTo, dtoLat, dtoLng);
        Gson gson = new Gson();
        final String json3 = gson.toJson(oFromAddress);
        final String json4 = gson.toJson(oToAddress);
        if (oFromAddress == null && oToAddress == null) {
            mLvReSentTrip.setVisibility(View.GONE);
            noTrip_List.setVisibility(View.VISIBLE);
            return;
        } else {
            intent.putExtra(Constants_driver.RECENT_TRIP, "1");
            intent.putExtra("FROM_RESENT", json3);
            intent.putExtra("TO_RESENT", json4);
        }
        startActivity(intent);
    }

    @Override
    protected void newTrip() {
        Intent intent = new Intent(RecentTripDriver_act.this, ForHireSharedTrip_act.class);
        startActivity(intent);
    }

}
