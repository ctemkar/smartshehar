package com.smartshehar.android.app.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.gson.Gson;
import com.smartshehar.android.app.CGlobals_trains;
import com.smartshehar.android.app.Constants_trains;
import com.smartshehar.android.app.Station;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.AppRater;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Eula;
import lib.app.util.UpgradeApp;
import lib.app.util.ui.ActFeedback;
import lib.app.util.ui.SSActivity;

public class ActTrainDashboard extends SSActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static String TAG = "ActTrainDashboard: ";
    //	private static String MEGABLOCK_URL = "http://smartshehar-com.myvps1-smartshehar-com.vps.ezhostingserver.com/alpha/trainapp/megablock.html";
    private CGlobals_trains mApp = null;
    ProgressDialog mProgressDialog;
    String mWorkTrains, mHomeTrains;
    private int miWorkStationId, miHomeStationId;
    ImageView mBtnWork, mBtnHome;
    TextView mTvWork, mTvHome, mTvMegaBlock;
    TextView mTvLoc;
    LinearLayout mLlHome, mLlWork;
    Button mHomeWorkBtn;
    Station moStation;
    Location mLocation;
    GoogleApiClient mGoogleApiClient = null;
    String mAppCode;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = CGlobals_trains.getInstance();
        mApp.init(this);
        mApp.mCH.userPing(getString(R.string.pageDashboard), "");

//        ActionBar ab = getSupportActionBar();
//        assert ab != null;
//        ab.setIcon(R.mipmap.ic_train_black_bg);
//        ab.setHomeButtonEnabled(true);
        /*ab.*/
        setTitle(getString(R.string.app_name));
        setContentView(R.layout.act_train_dashboard);
        mTvLoc = (TextView) findViewById(R.id.loc);
        mTvMegaBlock = (TextView) findViewById(R.id.tvMegaBlock);

        if (CGlobals_trains.mbIsAdmin)
            mTvLoc.setVisibility(View.VISIBLE);
        updateMyLocation(CGlobals_lib_ss.getInstance().getMyLocation(ActTrainDashboard.this));

        mTvWork = (TextView) ActTrainDashboard.this.findViewById(R.id.work);
        mTvHome = (TextView) findViewById(R.id.tvHome);
        mBtnWork = (ImageView) findViewById(R.id.btnWork);
        mBtnHome = (ImageView) findViewById(R.id.btnHome);
        mLlHome = (LinearLayout) findViewById(R.id.llHome);
        mLlWork = (LinearLayout) findViewById(R.id.llWork);

        mHomeWorkBtn = (Button) findViewById(R.id.setuphomework);


        try {
            mProgressDialog = new ProgressDialog(ActTrainDashboard.this);
            mProgressDialog.setMessage("Getting Schedule");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(ActTrainDashboard.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(ActTrainDashboard.this)
                .addOnConnectionFailedListener(ActTrainDashboard.this)
                .build();
        mGoogleApiClient.connect();

        mBtnWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActTrainDashboard.this, ActTrainRoute.class);
                intent.putExtra("startStationId", miHomeStationId);
                intent.putExtra("destStationId", miWorkStationId);
                startActivity(intent);
            }
        });
        mBtnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActTrainDashboard.this, ActTrainRoute.class);
                intent.putExtra("startStationId", miWorkStationId);
                intent.putExtra("destStationId", miHomeStationId);
                startActivity(intent);
            }
        });

        findViewById(R.id.fastinfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActTrainDashboard.this, ActFastInfo.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.route).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActTrainDashboard.this, ActTrainRoute.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.railmap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActTrainDashboard.this, ActTrainMap.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.trainfares).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActTrainDashboard.this, ActFares.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.preferences).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActTrainDashboard.this, ActPref.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String smartshehar_Website = "http://www.smartshehar.com ";
                String smartshehar_Twitter = "https://twitter.com/smartshehar";
                String smartshehar_Facebook = "http://www.facebook.com/smartshehar ";
                String smartshehar_Feedback_email = "userfeedback@smartshehar.com";
                String smartshehar_Packagname = "market://details?id=" + getPackageName();
                Intent feedback = new Intent(ActTrainDashboard.this, ActFeedback.class);
                feedback.putExtra("app_label", getString(R.string.app_name));
                feedback.putExtra("appCode", getString(R.string.appCode));
                feedback.putExtra("versionName", mApp.packageInfo.versionName);
                feedback.putExtra("USER_FEEDBACK_EMAIL", smartshehar_Feedback_email);
                feedback.putExtra("FACEBOOK_POST", smartshehar_Facebook);
                feedback.putExtra("TWITTER_POST", smartshehar_Twitter);
                feedback.putExtra("WEB_SITE", smartshehar_Website);
                feedback.putExtra("PACKAGE_NAME", smartshehar_Packagname);
                startActivity(feedback);
            }
        });

        findViewById(R.id.setuphomework).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActTrainDashboard.this, ActPref.class);
                startActivity(intent);
            }
        });
        View.OnClickListener megaBlockListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connectivity mConnectivity = new Connectivity();
                if (!Connectivity.checkConnected(ActTrainDashboard.this)) {
                    if (!mConnectivity.connectionError(ActTrainDashboard.this)) {
                        Log.d(TAG, "Internet Connection");

                    }
                }else{
                    Intent intent = new Intent(ActTrainDashboard.this, ActMegBlock.class);
                    startActivity(intent);
                }

            }
        };
        mAppCode = getIntent().getStringExtra("appCode");
        mTvMegaBlock.setOnClickListener(megaBlockListener);
        findViewById(R.id.megablock).setOnClickListener(megaBlockListener);
        RefreshUI();
        if (!mAppCode.equalsIgnoreCase("D")) {

            new Eula(this, getString(R.string.appNameShort),
                    getString(R.string.eula),
                    getString(R.string.updates)).show();

            AppRater.app_launched(this, getString(R.string.app_name),
                    mApp.packageInfo.packageName);
            UpgradeApp.app_launched(this, mApp.packageInfo, CGlobals_trains.mUserInfo,
                    getString(R.string.app_name),
                    getString(R.string.appNameShort), mApp.packageInfo.versionCode,
                    CGlobals_trains.PHP_PATH);

        }

    } // OnCreate

    void RefreshUI() {
        if (CGlobals_trains.haveNetworkConnection() > 0) {
            checkMegaBlock();
        } else {
            mTvMegaBlock.setText(getString(R.string.startInternet));
            mTvMegaBlock.setTypeface(Typeface.DEFAULT);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_act_train_dashboard, menu);
        MenuItem item = menu.findItem(R.id.menu_share);
        if (mAppCode.equalsIgnoreCase("D")) {

            item.setVisible(false);
        } else {
            item.setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            Toast.makeText(this, "Refreshing ...", Toast.LENGTH_SHORT).show();
            RefreshUI();
            homeWorkSchedule();
        } else if (item.getItemId() == R.id.menu_share) {

            mApp.getBestLocation(ActTrainDashboard.this);
//            mApp.getInstance().mCH.userPing(getString(R.string.atShare), "Share Train App");
            String message = getString(R.string.androidAppLink);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(share, "Share "
                    + getString(R.string.app_name)));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {

        mbAutoLocation = true;// settings.getBoolean("checkBoxAutoLocation", true);
        if (mbAutoLocation) {
            setupLocationListeners();
            startLocationListeners();
            updateMyLocation(CGlobals_lib_ss.getInstance().getMyLocation(ActTrainDashboard.this));
        } else {
            stopLocationListeners();
        }
        homeWorkSchedule();
//        CGlobals_trains.getInstance().turnGPSOn(ActTrainDashboard.this, mGoogleApiClient);
        RefreshUI();
        super.onResume();
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
        stopLocationListeners();
    }


    void homeWorkSchedule() {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            miHomeStationId = Integer.parseInt(settings.getString("home_station", "-1"));
            miWorkStationId = Integer.parseInt(settings.getString("work_station", "-1"));
        } catch (Exception e) {
            SSLog.e(TAG, "Failed to parse home or work station id", e.toString());
            return;
        }
        if (miHomeStationId == -1 || miWorkStationId == -1) {
            mHomeWorkBtn.setVisibility(View.VISIBLE);
            mLlHome.setVisibility(View.GONE);
            mLlWork.setVisibility(View.GONE);

        } else {
            mHomeWorkBtn.setVisibility(View.GONE);
            new GetRoutesTask().execute("");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private class GetRoutesTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            try {
                UpdateMegBlockInfo();
                Calendar c = Calendar.getInstance();
                int curHours = c.get(Calendar.HOUR_OF_DAY);
                int curMinutes = c.get(Calendar.MINUTE);

                int MINS_LATE = 10;
                mWorkTrains = mApp.mDBHelper.getTrains(curHours * 60 + curMinutes - MINS_LATE, "l_work");
                mHomeTrains = mApp.mDBHelper.getTrains(curHours * 60 + curMinutes - MINS_LATE, "l_home");
                if (TextUtils.isEmpty(mWorkTrains) || TextUtils.isEmpty(mHomeTrains)) {
                    mApp.mDBHelper.createHomeWorkTable(miHomeStationId, miWorkStationId);
                    mWorkTrains = mApp.mDBHelper.getTrains(curHours * 60 + curMinutes - MINS_LATE, "l_work");
                    mHomeTrains = mApp.mDBHelper.getTrains(curHours * 60 + curMinutes - MINS_LATE, "l_home");
                }

                if (mApp.mCH != null) {
                    mApp.mCH.userPing(getString(R.string.pageDashboard), "H:" + Integer.toString(miHomeStationId));
                    mApp.mCH.userPing(getString(R.string.pageDashboard), "W:" + Integer.toString(miWorkStationId));
                }
///	    		mProgressDialog.setTitle("Work Trains ");
            } catch (Exception e) {
                SSLog.e("homework: ", "GetRoutesTask ", e.getMessage());
            }
            return "Success";
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                if (!isFinishing()) {
                    mProgressDialog.show();
                    mProgressDialog.hide();
                }
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

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ActTrainDashboard.this);
                if (TextUtils.isEmpty(mWorkTrains) || TextUtils.isEmpty(mHomeTrains)) {
                    mLlWork.setVisibility(View.GONE);
                    mLlHome.setVisibility(View.GONE);
                    mTvLoc.setVisibility(View.VISIBLE);
                    mTvLoc.setText(R.string.RefreshHomeWork);

//	        	   CGlobals_trains.mIsWorkHomeDefined = false;
                } else {
                    mHomeWorkBtn.setVisibility(View.GONE);
                    mTvLoc.setVisibility(View.GONE);


                    try {
                        miHomeStationId = Integer.parseInt(settings.getString("home_station", "-1"));
                        miWorkStationId = Integer.parseInt(settings.getString("work_station", "-1"));
                        Station workStation = mApp.mDBHelper.getStationFromId(miWorkStationId);
                        String workPrompt = workStation.msStationAbbr != null ? " To Work (" + workStation.msStationAbbr + ")" : " To W: ";

//                        mBtnWork.setText(workPrompt);
                        mTvWork.setText(Html.fromHtml(mWorkTrains));
//	        	   mTvWork.setText((mWorkTrains));
                        Station homeStation = mApp.mDBHelper.getStationFromId(miHomeStationId);
                        String homePrompt = homeStation.msStationAbbr != null ? " To Home (" + homeStation.msStationAbbr + ")" : " To W: ";
//                        mBtnHome.setText(homePrompt);
                        mTvHome.setText(Html.fromHtml(mHomeTrains));
//		    	   mTvHome.setText(mHomeTrains);
                        mLlHome.setVisibility(View.VISIBLE);
                        mLlWork.setVisibility(View.VISIBLE);
                        mBtnHome.setVisibility(View.VISIBLE);
                        Log.d(TAG, "workPrompt " + workPrompt + " homePrompt " + homePrompt);
//		           hsvWork.setVisibility(View.VISIBLE);
//		           hsvHome.setVisibility(View.VISIBLE);
//		    		hsvHome.smoothScrollTo(0, mTvHome.getTop());
//		    		hsvWork.smoothScrollTo(0, mTvWork.getTop());
                    } catch (Exception e) {
                        SSLog.e(TAG, "Failed to parse home or work station id", e.toString());
                        return;
                    }

                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                }
            } catch (Exception e) {
                SSLog.e(TAG, "PostExecute - ", e.getMessage());
                mHomeWorkBtn.setVisibility(View.VISIBLE);
            }
            try {
                if (mProgressDialog.isShowing() && mProgressDialog != null)
                    mProgressDialog.dismiss();
            } catch (Exception e) {
                SSLog.e(TAG, "onPostExecute", e.getMessage());
            }
            super.onPostExecute(result);
        }


    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(ActTrainDashboard.this, ActBannerAd.class);
//        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public void updateMyLocation(final Location loc) {
        Thread th = new Thread(new Runnable() {
            public void run() {
                mApp.mCurrentLocation = loc;
                mLocation = mApp.mCurrentLocation;
                if (mLocation == null)
                    return;
                moStation = mApp.mDBHelper.getNearestStation(mLocation.getLatitude(), mLocation.getLongitude(),
                        ActTrainDashboard.this);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (mTvLoc != null && moStation != null) {

                            if (moStation.miStationId == miWorkStationId) {
                                mLlWork.setVisibility(View.GONE);
                            }
                            if (moStation.miStationId == miHomeStationId) {
                                mLlHome.setVisibility(View.GONE);
                            }

                        }
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        th.start();

    }


    @Override
    public void onStart() {
        super.onStart();
//      EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        //     EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }


    private static void UpdateMegBlockInfo() {
        new Thread(new Runnable() {
            public void run() {

            }
        }).start();


    } // getServerMessage()

    private void checkMegaBlock() {
        try {
            final String url = Constants_trains.MEGABLOCK_PHP;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (!response.trim().equals("-1")) {
                                megaBlockDetails(response);
                            } else {
                                mTvMegaBlock.setText("Mega Block info: N/A (Is your internet on?)");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//
                    Log.d(TAG, "error is " + error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params = CGlobals_trains.getInstance().getBasicMobileParams(params,
                            url, ActTrainDashboard.this);

                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_trains.getInstance().checkParams(params);
                }
            };
            CGlobals_trains.getInstance().getRequestQueue(ActTrainDashboard.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }

    private void megaBlockDetails(String result) {

        JSONArray ja;
        String sBlockOn = "";
        String msMegabolck;
        try {

            ja = new JSONArray(result);
            mApp.maoMegaBlock.clear();
            JSONObject joMega;
            String sMega = "";
            Log.d(TAG, "sMega "+sMega);
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
            String delim = "";
            for (int i = 0; i < ja.length(); i++) {
                try {
                    CGlobals_trains.MegaBlock megablock = mApp.new MegaBlock();
                    joMega = ja.getJSONObject(i);
                    sMega += joMega.getString("display") + "\n";
//                    megablock.fromDt = new Date();
                    megablock.fromDt = joMega.getString("fromdatetime");
//                    megablock.toDt = new Date();
                    megablock.toDt = joMega.getString("todatetime");
                    megablock.lineCode = joMega.getString("linecode");
                    megablock.iUp = joMega.getInt("up");
                    megablock.iDn = joMega.getInt("down");
                    megablock.sMessage = joMega.isNull("message") ? "" : joMega.getString("message");
                    megablock.sDisplay = joMega.getString("display");
                    megablock.fromstation = joMega.getString("fromstation");
                    megablock.tostation = joMega.getString("tostation");
                    megablock.iSlow = joMega.getInt("slow");
                    megablock.iFast = joMega.getInt("fast");
                    megablock.link = joMega.getString("link");
                    if (joMega.getInt("repeatdays") > 0)
                        megablock.iRepeatDays = joMega.getInt("repeatdays");
                    mApp.maoMegaBlock.add(megablock);
                    if (!sBlockOn.contains(megablock.lineCode)) {
                        sBlockOn = sBlockOn + delim + megablock.lineCode;
                        delim = ", ";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            msMegabolck = new Gson().toJson(mApp.maoMegaBlock);
            SharedPreferences spfMegablocks = getSharedPreferences(
                    Constants_trains.MEGABLOCK, MODE_PRIVATE);
            SharedPreferences.Editor speRecentStopsEditor;
            speRecentStopsEditor = spfMegablocks.edit();

            speRecentStopsEditor.putString(Constants_trains.MEGABLOCK, msMegabolck);
            speRecentStopsEditor.commit();
            CGlobals_trains.msBlockOn = sBlockOn;
            if (mApp.maoMegaBlock.size() > 0) {
                mTvMegaBlock.setVisibility(View.VISIBLE);
                mTvMegaBlock.setTextSize(20);
                mTvMegaBlock.setTypeface(Typeface.DEFAULT_BOLD);
                mTvMegaBlock.setText("Mega Block: " + sBlockOn);
            } else {
                mTvMegaBlock.setTypeface(Typeface.DEFAULT);
                mTvMegaBlock.setTextSize(16);
                mTvMegaBlock.setText(R.string.nomegablock);
//   	        	mTvMegaBlock.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            msMegabolck = new Gson().toJson(mApp.maoMegaBlock);
            SharedPreferences spfMegablocks = getSharedPreferences(
                    Constants_trains.MEGABLOCK, MODE_PRIVATE);
            SharedPreferences.Editor speRecentStopsEditor;
            speRecentStopsEditor = spfMegablocks.edit();

            speRecentStopsEditor.putString(Constants_trains.MEGABLOCK, msMegabolck);
            speRecentStopsEditor.commit();
            mTvMegaBlock.setTypeface(Typeface.DEFAULT);
            mTvMegaBlock.setTextSize(16);
            mTvMegaBlock.setText(getString(R.string.nomegablock));
            SSLog.e(TAG, "PostExecute - ", e.getMessage());
//    		   mTvMegaBlock.setText("Mega Block info: N/A (Is your internet on?)");
        }

    }


} // SSDashboard