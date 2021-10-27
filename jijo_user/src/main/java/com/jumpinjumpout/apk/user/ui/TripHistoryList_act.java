package com.jumpinjumpout.apk.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.user.TripHistoryList_Adapter;

/**
 * Created by user pc on 01-12-2015.
 */
public class TripHistoryList_act extends TripHistory_act {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    protected TripHistoryList_Adapter createMapListAdapter() {
        TripHistoryList_Adapter adapter = new TripHistoryList_Adapter();
        adapter.setMapLocations(cTripArrayList);
        return adapter;
    }


    @Override
    public void showMapDetails(View view) {
        CTrip mapLocation = (CTrip) view.getTag();
        Gson gson = new Gson();
        String json = gson.toJson(mapLocation);
        Intent intent = new Intent(this, TripHistoryFullMap_act.class);
        intent.putExtra(TripHistoryFullMap_act.EXTRA_ARRAYLIST, json);
        startActivity(intent);
    }

}
