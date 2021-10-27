package com.smartshehar.android.app.ui;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.smartshehar.android.app.CGlobals_trains;
import com.smartshehar.android.app.Station;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.util.ArrayList;



public class ActPref extends PreferenceActivity {
    private static int DEFAULT_STATION_ID = 9;
    private CGlobals_trains mApp;
    ProgressDialog mProgressDialog;

    int miHomeStationId = DEFAULT_STATION_ID, miWorkStationId = DEFAULT_STATION_ID;
    int miCurrentHomeId = -1, miCurrentWorkId = -1;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.train_preferences);
//		((MyApplication) getApplication()).getTracker(MyApplication.TrackerName.APP_TRACKER);

        mApp = CGlobals_trains.getInstance();
        mApp.init(this);
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        miHomeStationId = Integer.parseInt(settings.getString("home_station", "-1"));
        miWorkStationId = Integer.parseInt(settings.getString("work_station", "-1"));
        miCurrentHomeId = miHomeStationId;
        miCurrentWorkId = miWorkStationId;

        final ListPreference listPreferenceHomeStation = (ListPreference) findPreference("home_station");
        final ListPreference listPreferenceWorkStation = (ListPreference) findPreference("work_station");
        String DEFAULT_STATION_NAME = "DADAR";
        Station homeStation = new Station(DEFAULT_STATION_ID, DEFAULT_STATION_NAME),
                workStation = new Station(DEFAULT_STATION_ID, DEFAULT_STATION_NAME);
        final ArrayList<Station> stationList = CGlobals_trains.mDBHelper.getStations();
        try {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle("Processing Settings ...");
            mProgressDialog.setMessage("Processing Settings ...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        } catch (Exception e) {
            e.printStackTrace();

        }


        if (listPreferenceHomeStation != null) {
            CharSequence entries[] = new String[stationList.size()];
            CharSequence entryValues[] = new String[stationList.size()];
            int i = 0;
            for (Station hstation : stationList) {
                if (miHomeStationId == hstation.miStationId)
                    homeStation = hstation;
                if (miWorkStationId == hstation.miStationId)
                    workStation = hstation;
                entries[i] = hstation.msStationName;
                entryValues[i] = Integer.toString(hstation.miStationId);
                i++;
            }
            listPreferenceHomeStation.setEntries(entries);
            listPreferenceHomeStation.setEntryValues(entryValues);
        }
        ArrayList<Station> workStationList = CGlobals_trains.mDBHelper.getStationNamesTo(miHomeStationId);
        if (listPreferenceWorkStation != null) {
            CharSequence entries[] = new String[workStationList.size()];
            CharSequence entryValues[] = new String[workStationList.size()];
            int i = 0;
            for (Station hstation : workStationList) {
                entries[i] = hstation.msStationName;
                entryValues[i] = Integer.toString(hstation.miStationId);
                i++;
            }
            listPreferenceWorkStation.setEntries(entries);
            listPreferenceWorkStation.setEntryValues(entryValues);
        }
        assert listPreferenceHomeStation != null;
        if (!TextUtils.isEmpty(homeStation.msStationName))
            listPreferenceHomeStation.setSummary(homeStation.msStationName);
        assert listPreferenceWorkStation != null;
        if ( !TextUtils.isEmpty(workStation.msStationName))
            listPreferenceWorkStation.setSummary(workStation.msStationName);
        listPreferenceHomeStation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (listPreferenceWorkStation != null) {
                    CharSequence entries[] = new String[stationList.size()];
                    CharSequence entryValues[] = new String[stationList.size()];
                    int i = 0;
                    for (Station station : stationList) {
                        entries[i] = station.msStationName;
                        entryValues[i] = Integer.toString(station.miStationId);
                        i++;
                    }
                    listPreferenceWorkStation.setEntries(entries);
                    listPreferenceWorkStation.setEntryValues(entryValues);
                }
                miHomeStationId = Integer.parseInt(newValue.toString());
                ArrayList<Station> workStationList = CGlobals_trains.mDBHelper.getStationNamesTo(miHomeStationId);
                if (listPreferenceWorkStation != null) {
                    CharSequence entries[] = new String[workStationList.size()];
                    CharSequence entryValues[] = new String[workStationList.size()];
                    int i = 0;
                    for (Station hstation : workStationList) {
                        entries[i] = hstation.msStationName;
                        entryValues[i] = Integer.toString(hstation.miStationId);
                        i++;
                    }
                    listPreferenceWorkStation.setEntries(entries);
                    listPreferenceWorkStation.setEntryValues(entryValues);
                }

                for (Station station : stationList) {
                    if (newValue.toString().equals(Integer.toString(station.miStationId)))
                        listPreferenceHomeStation.setSummary(station.msStationName);
                }
                if (miCurrentHomeId != miHomeStationId || miCurrentWorkId != miWorkStationId ||
                        !CGlobals_trains.mIsWorkHomeDefined)
                    new GetRoutesTask().execute("");

                return true;
            }
        });

        listPreferenceWorkStation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
///                ArrayList<String> workStationList = mApp.mDBHelper.getStationNames();
                miWorkStationId = Integer.parseInt(newValue.toString());
                for (Station station : stationList) {
                    if (newValue.toString().equals(Integer.toString(station.miStationId)))
                        listPreferenceWorkStation.setSummary(station.msStationName);
                }
                if (miCurrentHomeId != miHomeStationId || miCurrentWorkId != miWorkStationId ||
                        !CGlobals_trains.mIsWorkHomeDefined)
                    new GetRoutesTask().execute("");
                return true;
            }
        });
     /*    final CheckBoxPreference checkboxPref = (CheckBoxPreference) getPreferenceManager().findPreference("checkBoxAutoLocation");

       checkboxPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SSLog.d("MyApp", "Pref " + preference.getKey() + " changed to " + newValue.toString());
                return true;
            }
        });*/


    }

    private class GetRoutesTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            try {
                mProgressDialog.setTitle("Processing");
                mProgressDialog.setMessage("Home/Work Routes");
                CGlobals_trains.mDBHelper.createHomeWorkTable(miHomeStationId, miWorkStationId);
                mApp.mCH.userPing("H:" + Integer.toString(miHomeStationId), "");
                mApp.mCH.userPing("W:" + Integer.toString(miWorkStationId), "");
            } catch (Exception e) {
                SSLog.e("ActPref: ", "GetRoutesTask - ", e.getMessage());
            }
            return "Success";
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                if (!isFinishing())
                    mProgressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            try {
                mProgressDialog.setProgress(progress[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            try {

///	    	   displayTrainsTowards(aTrainsAtStation);

                CGlobals_trains.mIsWorkHomeDefined = true;
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            } catch (Exception e) {
                SSLog.e("TrainRoute: ", "PostExecute - ", e.getMessage());
            }
            super.onPostExecute(result);
        }

    }

    @Override
    public void onBackPressed() {
        if (!CGlobals_trains.mIsWorkHomeDefined)
            new GetRoutesTask().execute("");
        super.onBackPressed();
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            if (mProgressDialog != null)
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
//		GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        //Stop the analytics tracking
//		GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

} // ActPref.java

