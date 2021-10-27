package com.smartshehar.cabe.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;
import com.smartshehar.cabe.CTripCabE;
import com.smartshehar.cabe.TripHistory_Adapter;

/**
 * Created by jijo_soumen on 25/04/2016.
 * CabE Passenger Trip History List
 */
public class TripHistoryList_act extends TripHistory_act {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    protected TripHistory_Adapter createMapListAdapter() {
        return new TripHistory_Adapter(this, cTripArrayList);
    }


    @Override
    public void showMapDetails(View view) {
        CTripCabE mapLocation = (CTripCabE) view.getTag();
        Gson gson = new Gson();
        String json = gson.toJson(mapLocation);
        Intent intent = new Intent(this, TripHistoryMap_act.class);
        intent.putExtra(TripHistoryMap_act.EXTRA_ARRAYLIST, json);
        startActivity(intent);
    }

}