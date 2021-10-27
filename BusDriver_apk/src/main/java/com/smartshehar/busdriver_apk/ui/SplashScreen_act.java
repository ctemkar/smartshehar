package com.smartshehar.busdriver_apk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.SSLog;
import com.smartshehar.busdriver_apk.R;


public class SplashScreen_act extends Activity {

    protected static final String TAG = "SplashScreen_act: ";
    private static int SPLASH_TIME_OUT = 3000;
    private static final int INITIAL_REQUEST = 1337;
    private static final String[] INITIAL_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.ACCESS_WIFI_STATE,
            android.Manifest.permission.READ_PHONE_STATE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen_act);
        if (Build.VERSION.SDK_INT >= 23) {
            if (!canAccessFINELocation() && !canAccessCOARSELocation()) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            } else {
                handleMashmallow();
            }
        } else {
            handleMashmallow();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case INITIAL_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    handleMashmallow();
                } else {
                    Toast.makeText(SplashScreen_act.this, "Please allow permission", Toast.LENGTH_LONG).show();
                    customDialog("Please allow permission");

                }
                return;
            }
        }
    }

    private boolean canAccessFINELocation() {
        return (hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean canAccessCOARSELocation() {
        return (hasPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    @SuppressLint("NewApi")
    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }

    private void customDialog(String message) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(SplashScreen_act.this);
        builder1.setMessage(message);
        builder1.setCancelable(true);
        builder1.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        builder1.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void handleMashmallow() {
        CGlobals_lib.getInstance().init(getApplicationContext());
        SSLog.setContext(getApplicationContext(), CGlobals_lib.msGmail);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen_act.this, BusDriver_act.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }


}
