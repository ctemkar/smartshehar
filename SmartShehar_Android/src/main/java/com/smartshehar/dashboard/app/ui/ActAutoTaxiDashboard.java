package com.smartshehar.dashboard.app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.R;

import lib.app.util.MyLocation;

public class ActAutoTaxiDashboard extends AppCompatActivity {
    SharedPreferences.Editor mEditor;
    private static final String TAG = "ActAutoTaxiDashboard";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_autotaxi);

        CGlobals_db.getInstance(ActAutoTaxiDashboard.this).init(ActAutoTaxiDashboard.this);
        CGlobals_db.getInstance(ActAutoTaxiDashboard.this).getMyLocation(ActAutoTaxiDashboard.this);
        CGlobals_db.getInstance(ActAutoTaxiDashboard.this).getBestLocation(ActAutoTaxiDashboard.this);
        SharedPreferences mPref = getSharedPreferences(CGlobals_db.PREFS_NAME, 0);
        mEditor = mPref.edit();
        String msCity = CGlobals_db.getCity(ActAutoTaxiDashboard.this);
        CGlobals_db.setCity(msCity, ActAutoTaxiDashboard.this);
        if (TextUtils.isEmpty(msCity)) {
            Toast.makeText(ActAutoTaxiDashboard.this,"Please select city",Toast.LENGTH_LONG).show();
            finish();
        }
//        CGlobals_db.getInstance(ActAutoTaxiDashboard.this).mCH.userPing("SSD", getString(R.string.pageDashboard));


//        AppRater.app_launched(this, getString(R.string.appTitle), CGlobals_db.getInstance(ActAutoTaxiDashboard.this).mPackageInfo.packageName);

       /* UpgradeApp.app_launched(this, CGlobals_db.getInstance(ActAutoTaxiDashboard.this).mPackageInfo, CGlobals_db.getInstance(ActAutoTaxiDashboard.this).mUserInfo,
                getString(R.string.appTitle),
                getString(R.string.appNameShort), CGlobals_db.getInstance(ActAutoTaxiDashboard.this).mPackageInfo.versionCode,
                Constants_dp.PHP_PATH);*/

     /*   new Eula(this, getString(R.string.appNameShort),
                getString(R.string.eula),
                getString(R.string.updates)).show();*/


        if (new MyLocation().getLocation(ActAutoTaxiDashboard.this, onLocationResult)) {
            Log.d(TAG, "MyLocation");
        }

        findViewById(R.id.meter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActAutoTaxiDashboard.this, ActMeter.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.farecalculator).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActAutoTaxiDashboard.this, ActFareCalculator.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.estimatedfare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(ActAutoTaxiDashboard.this, ActEstimatedCost.class);
                startActivity(intent);

            }
        });


    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {

        @Override
        public void gotLocation(Location location) {
            if (location == null) {
                if (CGlobals_db.getInstance(ActAutoTaxiDashboard.this).mCurrentLocation != null) {
                    location = CGlobals_db.getInstance(ActAutoTaxiDashboard.this).mCurrentLocation;
                    Log.d(TAG,"location "+location);

                } else {
                    location = CGlobals_db.mFakeLocation;
                    Log.d(TAG,"location "+location);
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPlayServices()) {
            // Then we're good to go!
            Log.d(TAG,"Then we're good to go!");
        }
    }


    private boolean checkPlayServices() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                showErrorDialog(status);
            } else {
                Toast.makeText(this, "This device is not supported.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Google Play Services must be installed.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    	/*public void pageClicked(View v) {
	        Intent intent = new Intent(ActDashboard.this, ActPage.class);
	        startActivity(intent);
    	}*/
} // SSDashboard





