package com.smartshehar.android.app.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.android.app.CGlobals_trains;
import com.smartshehar.android.app.Constants_trains;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.lang.reflect.Type;
import java.util.ArrayList;

import lib.app.util.Connectivity;


public class ActMegBlock extends AppCompatActivity {

    public static String TAG = "ActMegBlock";
    public static ArrayList<CGlobals_trains.MegaBlock> megaBlockArrayList = new ArrayList<>();
    Connectivity mConnectivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_meg_block);
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(ActMegBlock.this)) {
            if (!mConnectivity.connectionError(ActMegBlock.this,getString(R.string.requires_internet))) {
                Log.d(TAG, "Internet Connection");
            }
        }
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(getString(R.string.home_megablock));

        SharedPreferences spRecentStations = getSharedPreferences(
                Constants_trains.MEGABLOCK, MODE_PRIVATE);

        String sMegaBlockStations = spRecentStations.getString(Constants_trains.MEGABLOCK, "");
        Type type = new TypeToken<ArrayList<CGlobals_trains.MegaBlock>>() {
        }.getType();
        if (!TextUtils.isEmpty(sMegaBlockStations)) {
            megaBlockArrayList = new Gson().fromJson(sMegaBlockStations, type);
        }

        if (savedInstanceState == null) {
            try{
                FragMegaBlock fragment = new FragMegaBlock();
                if (megaBlockArrayList.size() > 0) {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.container, fragment)
                            .commit();
                }
                else{
                    showAlertDialog(ActMegBlock.this,
                            getString(R.string.appTitle), getString(R.string.nomegablock));
                }
            }catch (Exception e)
            {
                SSLog.e(TAG, "onCreate ", e);
            }
        }
    }
    private  void showAlertDialog(Context context, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();


        alertDialog.setTitle(title);

        alertDialog.setMessage(message);

        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

}
