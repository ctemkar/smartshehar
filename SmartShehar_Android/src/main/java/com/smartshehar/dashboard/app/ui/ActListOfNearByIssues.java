package com.smartshehar.dashboard.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.dashboard.app.R;

import java.lang.reflect.Type;

import lib.app.util.CAddress;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;

public class ActListOfNearByIssues extends AppCompatActivity {
    private static String TAG = "ActListOfIssuse";
    public static LatLng mLatLng;
    Connectivity mConnectivity;
    public static CAddress oAddr;
    public static int mShowAllVal = 0, mMyIssueVal = 0;
    public static String PVVal = "",
            MVVal = "",
            RTVal = "",
            AUTVal = "",
            RDVal = "",
            CLVal = "",
            ENVal = "",
            SFVal = "",
            WEVal = "",
            OGVal = "",
            RSVal = "",
            mPostalCode = "", mFromDate, mToDate, closedVal = "0", resolvedVal = "0", mIssueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_near_by_issuse);

        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (bundle != null) {
            mLatLng = bundle.getParcelable("location");
            String showAll = bundle.getString("showall");
            if (!TextUtils.isEmpty(showAll))
                mShowAllVal = Integer.valueOf(showAll);
            String myIssue = bundle.getString("myissue");
            if (!TextUtils.isEmpty(myIssue))
                mMyIssueVal = Integer.valueOf(myIssue);

            PVVal = bundle.getString(getString(R.string.PVVal));
            MVVal = bundle.getString(getString(R.string.MVVal));
            RTVal = bundle.getString(getString(R.string.RTVal));
            AUTVal = bundle.getString(getString(R.string.AUTVal));
            RDVal = bundle.getString(getString(R.string.RDVal));
            CLVal = bundle.getString(getString(R.string.CLVal));
            ENVal = bundle.getString(getString(R.string.ENVal));
            SFVal = bundle.getString(getString(R.string.SFVal));
            WEVal = bundle.getString(getString(R.string.WEVal));
            OGVal = bundle.getString(getString(R.string.OGVal));
            RSVal = bundle.getString(getString(R.string.RSVal));


            mIssueId = bundle.getString("issueid");
            mPostalCode = bundle.getString("postalcode");
            mFromDate = bundle.getString("fromissuedate");
            mToDate = bundle.getString("toissuedate");
            closedVal = bundle.getString("closed");
            resolvedVal = bundle.getString("resolved");

            if (mLatLng == null) {
                Toast.makeText(ActListOfNearByIssues.this, "Location not found", Toast.LENGTH_LONG).show();
                finish();
            }

        } else {
            Toast.makeText(ActListOfNearByIssues.this, "Oops! Something is wrong", Toast.LENGTH_LONG).show();
            finish();
        }
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(ActListOfNearByIssues.this)) {
            if (!mConnectivity.connectionError(ActListOfNearByIssues.this)) {
                if (mConnectivity.isGPSEnable(ActListOfNearByIssues.this)) {
                    Log.d(TAG, "Internet Connection");
                }
            }
        }
        if (savedInstanceState == null) {
            FragListOfNearByIssues fragment = new FragListOfNearByIssues();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActListOfNearByIssues.this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String sAddr;

//        if (requestCode == Constants_lib_ss.FINDADDRESS_FROM) {

        if (resultCode == Activity.RESULT_OK) {
            sAddr = data.getStringExtra("add");
            Type type = new TypeToken<CAddress>() {
            }.getType();
            oAddr = new Gson().fromJson(sAddr, type);
            Log.d(TAG, "oAddr " + oAddr);
            if (oAddr.getLatitude() != Constants_lib_ss.INVALIDLAT
                    && oAddr.getLongitude() != Constants_lib_ss.INVALIDLNG) {

                if (sAddr.equals("")) {
                    Toast.makeText(ActListOfNearByIssues.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                }
                FragListOfNearByIssues.address = oAddr.getAddress();
                FragListOfNearByIssues.tvNearByLocation.setText(oAddr.getAddress());
            }
        }
//        }
    }
}
