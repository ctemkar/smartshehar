package com.smartshehar.dashboard.app.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.smartshehar.dashboard.app.CFareParams;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;


public class ActTripSummary extends AppCompatActivity { // implements SensorEventListener

    TextView mTvStartTime, mTvEndTime, mTvFrom, mTvTo;
    TextView mTvDistEst, mTvFareEst, mTvFarePhoneNight, mTvFareEstNight;
    TextView mTvDistPhone, mTvFarePhone;
    EditText mEtVehicleNo, mEtVehicleMeter, mEtFareCharged, mEtMeterDistance;
    Button mBtnComplain, mBtnDone;
    boolean mbIsDirty;
    ProgressDialog mProgressDialog;
    String msFrom, msTo;
    private String TAG = "ActTripSummary";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setHomeButtonEnabled(true);
//		setTheme(R.style.Theme_Sherlock);
        setContentView(R.layout.tripsummary);
//			AnalyticsUtils.getInstance(this).trackPageView(getString(R.string.pageMyTripSummaryLong)); 

        if (CGlobals_db.getInstance(ActTripSummary.this).mCH == null)
            CGlobals_db.getInstance(ActTripSummary.this).init(ActTripSummary.this, CFareParams.AUTO);
      /*  try {
//            CGlobals_db.getInstance(ActTripSummary.this).mCH.userPing("SSD", getString(R.string.pageMyTripSummaryLong));
        } catch (Exception e) {
            SSLog.e(TAG, "TrainRoute: doInBackground - ", e.getMessage());
        }*/
        CGlobals_db.getInstance(ActTripSummary.this).getMyLocation(ActTripSummary.this);

    } // onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tripsummarymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_tripdelete) {
            Toast.makeText(this, "Deleting Trip", Toast.LENGTH_SHORT).show();
            deleteTrip();
        }
        return super.onOptionsItemSelected(item);
    }


    private void deleteTrip() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                CGlobals_db.getInstance(ActTripSummary.this).mDBHelperLocalAutoTaxi.deleteTrip(CGlobals_db.moLastTrip._id);
                            }
                        }, 5);
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    void setupUI() {
        mTvFarePhoneNight = (TextView) findViewById(R.id.tvFarePhoneNight);
        mTvFareEstNight = (TextView) findViewById(R.id.tvFareEstNight);
        mTvFrom = (TextView) findViewById(R.id.tvFromLatLon);
        mTvTo = (TextView) findViewById(R.id.tvToLatLon);
        mTvStartTime = (TextView) findViewById(R.id.tvStartTime);
        mTvEndTime = (TextView) findViewById(R.id.tvDestTime);
        mTvDistEst = (TextView) findViewById(R.id.tvDistEst);
        mTvFareEst = (TextView) findViewById(R.id.tvFareEst);
        mTvDistPhone = (TextView) findViewById(R.id.tvDistPhone);
        mTvFarePhone = (TextView) findViewById(R.id.tvFarePhone);
        mBtnComplain = (Button) findViewById(R.id.btnComplain);
        mBtnDone = (Button) findViewById(R.id.btnSubmitIssue);
        mEtVehicleNo = (EditText) findViewById(R.id.etVehicleNo);
        mEtVehicleMeter = (EditText) findViewById(R.id.etVehicleMeter);
        mEtFareCharged = (EditText) findViewById(R.id.etChargedFare);
        mEtMeterDistance = (EditText) findViewById(R.id.etMeterDist);
        mProgressDialog = new ProgressDialog(ActTripSummary.this);
        mProgressDialog.setMessage("Getting Trip Information");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);


        mBtnComplain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveTrip();
                finish();
            }
        });
        mBtnDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveTrip();
                finish();
            }
        });
        showKeyboard(false);
    } // setupUI

    void init() {

        mbIsDirty = false;
        if (CGlobals_db.moLastTrip != null)
            new PopTripTask().execute("");
        else {
            Toast.makeText(ActTripSummary.this, getString(R.string.noSavedTrip), Toast.LENGTH_SHORT).show();
            finish();
        }

    } // init


    @Override
    public void onResume() {
        setupUI();
        init();
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mbIsDirty)
            saveTrip();
        mProgressDialog.dismiss();
        super.onPause();
    } // onPause

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    private class PopTripTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... sUrl) {
            try {
                publishProgress(10);
                CRoute mRoute ;
                if (TextUtils.isEmpty(CGlobals_db.moLastTrip.msStartAddr) ||
                        TextUtils.isEmpty(CGlobals_db.moLastTrip.msDestAddr) || CGlobals_db.moLastTrip.miEstimatedDist == 0) {
                    mRoute = getEstimatedDistance(CGlobals_db.moLastTrip.mdStartLat,
                            CGlobals_db.moLastTrip.mdStartLon,
                            CGlobals_db.moLastTrip.mdDestLat, CGlobals_db.moLastTrip.mdDestLon);
                    CGlobals_db.moLastTrip.miEstimatedDist = mRoute.miDist;
                    if (!TextUtils.isEmpty(mRoute.msStartAddress)) {
                        CGlobals_db.moLastTrip.msStartAddr = mRoute.msStartAddress;
                        CGlobals_db.moLastTrip.mRowValues.put("startaddr", CGlobals_db.moLastTrip.msStartAddr);
                    }
                    if (!TextUtils.isEmpty(mRoute.msDestAddress)) {
                        CGlobals_db.moLastTrip.msDestAddr = mRoute.msDestAddress;
                        CGlobals_db.moLastTrip.mRowValues.put("destaddr", CGlobals_db.moLastTrip.msDestAddr);
                    }
                    mbIsDirty = true;
                }
                publishProgress(70);
                msFrom = CGlobals_db.moLastTrip.msStartAddr + "(" + CGlobals_db.moLastTrip.msStartLat + ", " +
                        CGlobals_db.moLastTrip.msStartLon + ")";
                msTo = CGlobals_db.moLastTrip.msDestAddr + "(" + CGlobals_db.moLastTrip.msDestLat + ", " +
                        CGlobals_db.moLastTrip.msDestLon + ")";
                if (!TextUtils.isEmpty(CGlobals_db.moLastTrip.msStartAddr) &&
                        !TextUtils.isEmpty(CGlobals_db.moLastTrip.msDestAddr)) {
                    int mTripId = CGlobals_db.getInstance(ActTripSummary.this).mDBHelperLocalAutoTaxi.getTripId(CGlobals_db.moLastTrip.mlStartMs);
                    if (mTripId != -1)
                        CGlobals_db.getInstance(ActTripSummary.this).mDBHelperLocalAutoTaxi.updateTrip(CGlobals_db.moLastTrip, mTripId);
                }

            } catch (Exception e) {
                SSLog.e(TAG, "TrainRoute: doInBackground - ", e.getMessage());
            }
            return null;
        } // doInBackground

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isFinishing())
                mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            double d;
            String s;
            try {
//				publishProgress(80);
                if (!TextUtils.isEmpty(CGlobals_db.moLastTrip.msStartDateTime))
                    mTvStartTime.setText(CGlobals_db.moLastTrip.msStartDateTime);
                if (!TextUtils.isEmpty(CGlobals_db.moLastTrip.msDestDateTime))
                    mTvEndTime.setText(CGlobals_db.moLastTrip.msDestDateTime);
                if (CGlobals_db.moLastTrip.miEstimatedDist > 0)
                    mTvDistEst.setText(new DecimalFormat("###.##").format(CGlobals_db.moLastTrip.miEstimatedDist / 1000.000));
                else
                    mTvDistEst.setText("N/A");
                if (CGlobals_db.moLastTrip.miPhoneDist >= 0)
                    mTvDistPhone.setText(new DecimalFormat("###.##").format(CGlobals_db.moLastTrip.miPhoneDist / 1000.000));
                else
                    mTvDistPhone.setText("N/A");

                if (CGlobals_db.moLastTrip.mdPhoneFare >= 0) {
                    d = Double.parseDouble(new DecimalFormat("###.##").format(CGlobals_db.moLastTrip.mdPhoneFare));
                    s = String.valueOf(Math.round(d));
                    mTvFarePhone.setText(s);
                } else
                    mTvFarePhone.setText("N/A");

                if (CGlobals_db.mdNightFare >= 0) {
                    d = Double.parseDouble(new DecimalFormat("###.##").format(CGlobals_db.mdNightFare));
                    s = String.valueOf(Math.round(d));
                    mTvFarePhoneNight.setText(s);
                } else
                    mTvFarePhoneNight.setText("N/A");

                if (CGlobals_db.mEstimatedTotalDayFare >= 0) {
                    d = Double.parseDouble(new DecimalFormat("###.##").format(CGlobals_db.mEstimatedTotalDayFare));
                    s = String.valueOf(Math.round(d));
                    mTvFareEst.setText(s);
                } else
                    mTvFareEst.setText("N/A");
                if (CGlobals_db.mEstimatedToatalNightFare >= 0) {
                    d = Double.parseDouble(new DecimalFormat("###.##").format(CGlobals_db.mEstimatedToatalNightFare));
                    s = String.valueOf(Math.round(d));
                    mTvFareEstNight.setText(s);
                } else
                    mTvFareEstNight.setText("N/A");

                mTvFrom.setText((CGlobals_db.moLastTrip.msStartAddr != null ? CGlobals_db.moLastTrip.msStartAddr : "N/A ")
                        + "(" + CGlobals_db.moLastTrip.msStartLat + ", " +
                        CGlobals_db.moLastTrip.msStartLon + ")");
                mTvTo.setText((CGlobals_db.moLastTrip.msDestAddr != null ? CGlobals_db.moLastTrip.msDestAddr : "N/A ")
                        + "(" + CGlobals_db.moLastTrip.msDestLat + ", " +
                        CGlobals_db.moLastTrip.msDestLon + ")");
                if (!TextUtils.isEmpty(CGlobals_db.moLastTrip.msVehicleNo))
                    mEtVehicleNo.setText(CGlobals_db.moLastTrip.msVehicleNo);
                if (CGlobals_db.moLastTrip.miMeterDistance > 0)
                    mEtMeterDistance.setText(Double.toString(CGlobals_db.moLastTrip.miMeterDistance / 1000.00));
                if (CGlobals_db.moLastTrip.mdFareCharged > 0)
                    mEtFareCharged.setText(Double.toString(CGlobals_db.moLastTrip.mdFareCharged));
                if (CGlobals_db.moLastTrip.mdVehicleMeter > 0)
                    mEtVehicleMeter.setText(Double.toString(CGlobals_db.moLastTrip.mdVehicleMeter));
            } catch (Exception e) {
                SSLog.e(TAG, "TrainRoute: PostExecute - ", e.getMessage());
            }
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            super.onPostExecute(result);
        }

    } // PopTripTask

    class CRoute {
        String msStartAddress, msDestAddress;
        int miDist;
    }

    // Get the driving distance betweeen start and end using Google
    private CRoute getEstimatedDistance(double dStartLat, double dStartLon,
                                        double dDestLat, double dDestLon) {
        CRoute route = new CRoute();
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        String sUrl = "http://maps.googleapis.com/maps/api/directions/json?origin=" +
                Double.toString(dStartLat) + "," + Double.toString(dStartLon) + "&destination=" +
                Double.toString(dDestLat) + "," + Double.toString(dDestLon) + "&sensor=false";
        HttpGet httpGet = new HttpGet(sUrl);
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                try {
                    JSONObject jo = new JSONObject(builder.toString());
                    JSONObject joVal ;

                    JSONArray ja = jo.getJSONArray("routes");
                    jo = ja.getJSONObject(0);
                    ja = jo.getJSONArray("legs");
                    jo = ja.getJSONObject(0);
                    route.msStartAddress = jo.getString("start_address");
                    route.msDestAddress = jo.getString("end_address");
                    joVal = jo.getJSONObject("distance");
                    route.miDist = joVal.getInt("value");


                } catch (JSONException e) {
                    Log.e("log_tag", "Error parsing data " + e.toString());
                }

            } else {
                Log.d(TAG, "Failed to download file");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            SSLog.e(TAG, "ClientProtocolException ", e);
        } catch (IOException e) {
            e.printStackTrace();
            SSLog.e(TAG,"IOException ",e);
        }
        return route;
    } // getDistance, address from lat, lon


    void saveTrip() {
        try {
            if (!TextUtils.isEmpty(mEtVehicleNo.getText().toString()))
                CGlobals_db.moLastTrip.msVehicleNo = mEtVehicleNo.getText().toString();
            CGlobals_db.moLastTrip.mdVehicleMeter = 0;
            CGlobals_db.moLastTrip.mdFareCharged = 0;
            CGlobals_db.moLastTrip.miMeterDistance = 0;
            if (!TextUtils.isEmpty(mEtVehicleMeter.getText().toString()))
                CGlobals_db.moLastTrip.mdVehicleMeter = Double.parseDouble(mEtVehicleMeter.getText().toString());
            if (!TextUtils.isEmpty(mEtFareCharged.getText().toString()))
                CGlobals_db.moLastTrip.mdFareCharged = Double.parseDouble(mEtFareCharged.getText().toString());
            if (!TextUtils.isEmpty(mEtMeterDistance.getText().toString()))
                CGlobals_db.moLastTrip.miMeterDistance = (int) (Double.parseDouble(mEtMeterDistance.getText().toString()) * 1000);
        } catch (Exception e) {
            SSLog.e(TAG, "ActTripSummary: saveTrip - ", e);
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                CGlobals_db.getInstance(ActTripSummary.this).getMyLocation(ActTripSummary.this);
                CGlobals_db.moLastTrip.updateContent();
                CGlobals_db.getInstance(ActTripSummary.this).mDBHelperLocalAutoTaxi.updateTrip(CGlobals_db.moLastTrip, CGlobals_db.moLastTrip._id);
            }
        }, 10);

    }

    void showKeyboard(boolean bShow) {
        if (bShow) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE);

            View cFocus = getCurrentFocus();
            if (imm != null && cFocus != null)
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            else
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }
/*	@Override
    public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}
	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}*/
}