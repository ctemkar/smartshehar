package com.smartshehar.android.app.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.smartshehar.android.app.CGlobals_trains;
import com.smartshehar.android.app.StationAdapter;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.util.ArrayList;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.ClearableEditText;



public class SearchStationActivity extends AppCompatActivity {
    private static final String TAG = "SearchStationActivity";
    ListView stationListView;
    StationAdapter addressListAdapter;
    static CGlobals_trains mApp;
    ClearableEditText editsearch;
    public static ArrayList<String> maRecentStation;
    private static final String SRECENTSTATIONS = "Station List";
    static SharedPreferences spRecentStations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_station);
        ActionBar actionBar = getSupportActionBar(); // you can use ABS or the non-bc ActionBar
        assert actionBar != null;
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_HOME_AS_UP);

        spRecentStations = getSharedPreferences(
                SRECENTSTATIONS, MODE_PRIVATE);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflate the view that we created before
        View v = inflater.inflate(R.layout.actionbar_search, null);
        editsearch = (ClearableEditText) v.findViewById(R.id.search_box);

        editsearch.setVisibility(View.VISIBLE);
        editsearch.setHint("Enter station name");

        editsearch.addTextChangedListener(textWatcher);

        stationListView = (ListView) findViewById(R.id.lvStation);
        mApp = CGlobals_trains.getInstance();
        mApp.init(this);
//        mApp.mCH.userPing(getString(R.string.pageSearchStation), "");

        maRecentStation = getRecentStations();
        if (maRecentStation.size() == 0) {
            maRecentStation = mApp.mDBHelper.getStationNames();
        }

        addressListAdapter = new StationAdapter(SearchStationActivity.this);
        stationListView.setAdapter(addressListAdapter);
        actionBar.setCustomView(v);
    }


    private ArrayList<String> getRecentStations() {
        ArrayList<String> aRS = new ArrayList<>();
        String sRecentStations = CGlobals_lib_ss.getInstance().getPersistentPreference(SearchStationActivity.this)
                .getString(SRECENTSTATIONS,
                        null);
        SSLog.i("TrainApp: TrainHome - ", sRecentStations);
        if (sRecentStations != null) {
            // boolean bStopThere;
            String s;
            if (sRecentStations != null && sRecentStations.trim().length() > 0) {
                String as[] = sRecentStations.split(";");
                int iLen = as.length;
                for (int i = iLen - 1; i >= 0; i--) {
                    s = as[i];
                    String SELECTRECENT = "Select Station";
                    if (s != null && !s.equalsIgnoreCase(SELECTRECENT)) {

                        try {
                            SSLog.i("TrainApp: Recent Station: ", s);
                            aRS.add(s);
                        } catch (Exception e) {
                            SSLog.e("TrainApp: ", "getRecentStations - ",
                                    e.getMessage());
                            SSLog.e("TrainApp: ", "getRecentStations - mApp - ",
                                    mApp == null ? " null" : " ");
                        }
                    }
                }
                ArrayList<String> mStation ;
                mStation = mApp.mDBHelper.getStationNames();
                for (int j = 0; j < mStation.size(); j++) {
                    aRS.add(mStation.get(j));
                }
            }
        }
        return aRS;
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {

            try {
                String text = editsearch.getText().toString();
                addressListAdapter.getFilter().filter(text);
                stationListView.requestLayout();
            } catch (Exception e) {
                SSLog.e(TAG, "afterTextChanged: ", e);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {


        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {


        }

    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
