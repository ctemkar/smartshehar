package com.smartshehar.dashboard.app.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.smartshehar.dashboard.app.CGeo;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.GcmIntentService;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;
import com.smartshehar.dashboard.app.SSLog;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.MyLocation;
import lib.app.util.ui.ActFeedback;


public class Act_SafetyShield_Dashboard extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "SafetyShield_Dash: ";

    public CheckBox dontShowAgain;
    protected CheckBox checkbox;
    private CGlobals_lib_ss mApp = null;
    ProgressDialog mProgressDialog;

//    private Location mCurrentLocation;

    //	GoogleAnalyticsTracker mTracker;
    int CAMERA_PIC_REQUEST = 1337;
    public static final String KEY_PREF_MY_NAME = "pref_myname";
    public static final String KEY_PREF_MY_NUMBER = "pref_mynumber";
    public static final String KEY_PREF_MY_EMAIL = "pref_myemail";
    public static final String KEY_PREF_CC1 = "pref_cc1";
    public static final String KEY_PREF_CC1_PHONE = "pref_cc1_phone";
    public static final String KEY_PREF_CC1_MSG = "pref_cc1_msg";
    public static final String KEY_PREF_CC2 = "pref_cc2";
    public static final String KEY_PREF_CC2_PHONE = "pref_cc2_phone";
    public static final String KEY_PREF_CC2_MSG = "pref_cc2_msg";
    public static final String KEY_PREF_CC3 = "pref_cc3";
    public static final String KEY_PREF_CC3_PHONE = "pref_cc3_phone";
    public static final String KEY_PREF_CC3_MSG = "pref_cc3_msg";
    GoogleApiClient mGoogleApiClient = null;
    LocationManager locationManagerNetwork;
    LocationManager locationManagerGPS;
    LocationManager locationManagerWiFi;

    LocationListener locationListenerNetwork;
    LocationListener locationListenerGPS;
    LocationListener locationListenerWiFi;
    Connectivity mConnectivity;
    String msMyName, msMyNo, msMyEmail, msCC1Phone, msCC1, msCC1Msg, msCC2Phone, msCC2,
            msCC2Msg, msCC3Phone, msCC3, msCC3Msg;
    TextView mTvStatus;
    private boolean isInternetOn = true;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ///          	AnalyticsUtils.getInstance(this).trackPageView(getString(R.string.pageBeSafeDashboardLong));
        mConnectivity = new Connectivity();
        // check whether gps is on or off
        mGoogleApiClient = new GoogleApiClient.Builder(Act_SafetyShield_Dashboard.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(Act_SafetyShield_Dashboard.this)
                .addOnConnectionFailedListener(Act_SafetyShield_Dashboard.this)
                .build();
        mGoogleApiClient.connect();
//        mCurrentLocation = CGlobals_db.getInstance(Act_SafetyShield_Dashboard.this).getBestLocation(Act_SafetyShield_Dashboard.this);

        mApp = CGlobals_lib_ss.getInstance();
        mApp.init(this);
        mApp.getMyLocation();
//        mApp.mCH.userPing(getString(R.string.pageBeSafeDashboardLong), "");

        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setHomeButtonEnabled(true);
        setContentView(R.layout.activity_saftey_shield_dashboard);
        mTvStatus = (TextView) findViewById(R.id.tvStatus);
        getSettings();
//        AppRater.app_launched(this, getString(R.string.pageDashboardLong), mApp.mPackageInfo.packageName);
        setupLocationListeners();
        startLocationListeners();
        showHelp();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

        if (!gps_enabled) {
           /* Dialog dialog = createDialog();
            dialog.show();*/
            CGlobals_db.getInstance(Act_SafetyShield_Dashboard.this).turnGPSOn(Act_SafetyShield_Dashboard.this, mGoogleApiClient);

        }
        new UpdateAddress().execute(mApp.getMyLocation());
        if (new MyLocation().getLocation(Act_SafetyShield_Dashboard.this, onLocationResult)) {
            Log.d(TAG, "MyLocation");
        }
        findViewById(R.id.emergency).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Act_SafetyShield_Dashboard.this, Activity_BeSafe_Emergency.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.soundalarm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Act_SafetyShield_Dashboard.this, ActSoundAlarm.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.walkwithme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Act_SafetyShield_Dashboard.this, Act_WalkWithMe.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.police).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Act_SafetyShield_Dashboard.this, Activity_Hospital.class);
                intent.putExtra(Constants_dp.NEARBY, Constants_dp.POLICE);
                startActivity(intent);
            }
        });

        findViewById(R.id.hospital).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Act_SafetyShield_Dashboard.this, Activity_Hospital.class);
                intent.putExtra(Constants_dp.NEARBY, Constants_dp.HOSPITAL);
                startActivity(intent);
            }
        });

        findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Act_SafetyShield_Dashboard.this, Activity_Settings.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Act_SafetyShield_Dashboard.this, ActFeedback.class);
                intent.putExtra("appTitle", getString(R.string.appTitle));
                intent.putExtra("appCode", getString(R.string.appCode));
                intent.putExtra("versionName", mApp.mPackageInfo.versionName);

                startActivity(intent);
            }
        });

    }

    private void getSettings() {
        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(Act_SafetyShield_Dashboard.this);
        msMyName = mSettings.getString(KEY_PREF_MY_NAME, "");
        msMyNo = mSettings.getString(KEY_PREF_MY_NUMBER, "");
        msMyEmail = mSettings.getString(KEY_PREF_MY_EMAIL, SSApp.mGmail);
        msCC1Phone = mSettings.getString(KEY_PREF_CC1_PHONE, "");
        msCC1 = mSettings.getString(KEY_PREF_CC1, "");
        msCC1Msg = mSettings.getString(KEY_PREF_CC1_MSG, "");
        msCC2Phone = mSettings.getString(KEY_PREF_CC2_PHONE, "");
        msCC2 = mSettings.getString(KEY_PREF_CC2, "");
        msCC2Msg = mSettings.getString(KEY_PREF_CC2_MSG, "");
        msCC3Phone = mSettings.getString(KEY_PREF_CC3_PHONE, "");
        msCC3 = mSettings.getString(KEY_PREF_CC3, "");
        msCC3Msg = mSettings.getString(KEY_PREF_CC3_MSG, "");
        String msg = getWarnings();
        if (!TextUtils.isEmpty(msg))
            mTvStatus.setText(Html.fromHtml(msg));
        else
            mTvStatus.setText("");

    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {

        @Override
        public void gotLocation(Location location) {
            if (location == null) {
                if (CGlobals_lib_ss.mCurrentLocation != null) {
                    location = CGlobals_lib_ss.mCurrentLocation;
                    Log.d(TAG, "location " + location);
                } else {
                    location = SSApp.mFakeLocation;
                    Log.d(TAG, "location " + location);
                }
            }
        }

    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    // Send picture to server by the php file
    private boolean SendPictureToServer(final Bitmap bitmapOrg) {
        isInternetOn = true;
        if (CGlobals_lib_ss.haveNetworkConnection() == 0) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            isInternetOn = true;
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
//			        	Toast.makeText(getApplicationContext(), "*** Warning - Photo not sent, internet not available ***",
//			        			Toast.LENGTH_LONG).show();
                            isInternetOn = false;
                            break;
                    }
                }
            };
            if (!isInternetOn)
                return false;

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Start your internet connection and Hit OK?")
                    .setTitle("Internet connection needed to proceed")
                    .setPositiveButton("Ok", dialogClickListener)
                    .setNegativeButton("Cancel", dialogClickListener)
                    .show();
        }

        Thread t = new Thread() {
            public void run() {
                postImage(bitmapOrg);
            }
        };
        t.start();
        return true;
    } // SendPictureToServer

    private void postImage(final Bitmap bitmapOrg) {
        final String url = SSApp.PHP_PATH + "/postImage.php";
        try {

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {

                                Log.d(TAG, "Image Sent ");
                            } else {

                                Log.d(TAG, "Image not Sent ");
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    SSLog.e(TAG, "postImage", error);
                    Log.d(TAG, "error is " + error.toString());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    Location location = mApp.getMyLocation();
                    Calendar cal = Calendar.getInstance();
                    String sTime = Long.toString(cal.getTimeInMillis());
                    ByteArrayOutputStream bao = new ByteArrayOutputStream();
                    bitmapOrg.compress(Bitmap.CompressFormat.PNG, 100, bao);
                    byte[] ba = bao.toByteArray();
                    String ba1 = Base64.encodeToString(ba, Base64.DEFAULT);

                    params.put("cc", SSApp.mGmail);
                    params.put("imei", SSApp.mIMEI);
                    params.put("image", ba1);
                    params.put("filename",
                            SSApp.mGmail + "_" + sTime + ".jpg");

                    try {
                        if (location != null) {
                            params.put("lat", Double.toString(location.getLatitude()));
                            params.put("lon", Double.toString(location.getLongitude()));
                        }
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                        e.printStackTrace(); // getFromLocation() may sometimes fail
                    }

                    params.put("y", Integer.toString(cal.get(Calendar.YEAR)));
                    params.put("o", Integer.toString(cal.get(Calendar.MONTH)));
                    params.put("d", Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
                    params.put("h", Integer.toString(cal.get(Calendar.HOUR_OF_DAY)));
                    params.put("m", Integer.toString(cal.get(Calendar.MINUTE)));
                    params.put("s", Integer.toString(cal.get(Calendar.SECOND)));
                    SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(Act_SafetyShield_Dashboard.this);
                    String msName = mSettings.getString(Constants_dp.KEY_PREF_MY_NAME, "");
                    String msPhone = mSettings.getString(Constants_dp.KEY_PREF_MY_NUMBER, "");
                    params.put("name", msName);
                    params.put("phone", msPhone);


                    String sCC1 = mSettings.getString(KEY_PREF_CC1, "");
                    String sCC2 = mSettings.getString(KEY_PREF_CC2, "");
                    String sCC3 = mSettings.getString(KEY_PREF_CC3, "");
                    StringBuilder sTo = new StringBuilder();
                    if (!TextUtils.isEmpty(sCC1))
                        sTo.append(sCC1);
                    if (!TextUtils.isEmpty(sCC2))
                        sTo.append(", ").append(sCC2);
                    if (!TextUtils.isEmpty(sCC3))
                        sTo.append(", ").append(sCC3);
                    params.put("email", sTo.toString());


                    try {
                        Geocoder geo = new Geocoder(Act_SafetyShield_Dashboard.this.getApplicationContext(), Locale.getDefault());
                        assert location != null;
                        List<Address> addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses.isEmpty()) {
                            Toast.makeText(Act_SafetyShield_Dashboard.this, "Waiting for Location", Toast.LENGTH_LONG).show();
                        } else {
                            if (addresses.size() > 0) {
                                String address = addresses.get(0).getAddressLine(0);
                                address += ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                                params.put("address", address);
                                Toast.makeText(getApplicationContext(), "Address:- " + address, Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // getFromLocation() may sometimes fail
                    }
                    params = CGlobals_db.getInstance(Act_SafetyShield_Dashboard.this).getBasicMobileParams(params,
                            url, Act_SafetyShield_Dashboard.this);
                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_db.getInstance(Act_SafetyShield_Dashboard.this).checkParams(params);
                }
            };
            CGlobals_db.getInstance(Act_SafetyShield_Dashboard.this).getRequestQueue(Act_SafetyShield_Dashboard.this).add(postRequest);

        } catch (Exception e) {
            mTvStatus.setText(e.toString());
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap thumbnail;
        if (requestCode == CAMERA_PIC_REQUEST) {
            if (data != null && data.getExtras() != null) {
                thumbnail = (Bitmap) data.getExtras().get("data");
                boolean isPictureSent = SendPictureToServer(thumbnail);
                if (!isPictureSent) {
                    Toast.makeText(getApplicationContext(), "*** Warning - Photo not sent, internet not available ***",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Picture sent to server. You should get an email soon",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(Act_SafetyShield_Dashboard.this, "Picture Not taken", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CGlobals_db.REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");

                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    CGlobals_db.showGPSDialog = false;
                    CGlobals_db.alertToTurnOnGps(Act_SafetyShield_Dashboard.this);
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Setup location listeners
    void setupLocationListeners() {
        // Acquire a reference to the system Location Manager
        locationManagerNetwork = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        locationManagerGPS = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        locationManagerWiFi = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListenerNetwork = new LocationListener() {
            public void onLocationChanged(Location location) {
                updateMyLocation(location);
            }

            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        locationListenerGPS = new LocationListener() {
            public void onLocationChanged(Location location) {
                updateMyLocation(location);
            }

            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        locationListenerWiFi = new LocationListener() {
            public void onLocationChanged(Location location) {
                updateMyLocation(location);
            }

            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        // startLocationListeners();
    }

    private void updateMyLocation(Location location) {
        mApp.mCurrentLocation = location;
    }


    void startLocationListeners() {
        if (ActivityCompat.checkSelfPermission(Act_SafetyShield_Dashboard.this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(Act_SafetyShield_Dashboard.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (locationManagerNetwork != null && locationListenerNetwork != null)
            locationManagerNetwork.removeUpdates(locationListenerNetwork);
        if (locationManagerGPS != null && locationListenerGPS != null)
            locationManagerGPS.removeUpdates(locationListenerGPS);
        if (locationManagerWiFi != null && locationListenerWiFi != null)
            locationManagerWiFi.removeUpdates(locationListenerWiFi);
        try {
            if (locationManagerNetwork != null)
                locationManagerNetwork.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 350, 5,
                        locationListenerNetwork);

            assert locationManagerGPS != null;
            locationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    3500, 10, locationListenerGPS);
            // locationManagerWiFi.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
            // 3500, 10, locationListenerWiFi);
        } catch (Exception e) {
            SSLog.e(" Activity BeSafe Dashboard - startLocationListeners", "Error", e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSettings();
        String msg = getWarnings();
        if (!TextUtils.isEmpty(msg))
            mTvStatus.setText(Html.fromHtml(msg));
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSettings();
        String msg = getWarnings();
        if (!TextUtils.isEmpty(msg))
            mTvStatus.setText(Html.fromHtml(msg));
    }

    // Warn required data not entered for sending email, sms, etc.
    String getWarnings() {
        String msg = "";

        if (TextUtils.isEmpty(msMyName) || TextUtils.isEmpty(msMyNo) || TextUtils.isEmpty(msMyEmail) ||
                TextUtils.isEmpty(msCC1Phone) || TextUtils.isEmpty(msCC1)) {
            msg = "You have not entered basic information ";
            if (TextUtils.isEmpty(msMyName))
                msg += "Your name, ";
            if (TextUtils.isEmpty(msMyNo))
                msg += "Contact no., ";
            if (TextUtils.isEmpty(msMyEmail))
                msg += "Email id, ";
            if (TextUtils.isEmpty(msCC1Phone))
                msg += "Emergency Contact 1, ";
            if (TextUtils.isEmpty(msCC1))
                msg += "Emergency Email 1, ";

            msg += ".  App will have limited functionality";
        }

        if (CGlobals_lib_ss.haveNetworkConnection() == 0) {
            msg = "\n" + getString(R.string.no_internet);
        }
        return msg;
    }

    void showHelp() {
        final String PREFS_NAME = "BeSafe_once";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String skipMessage = settings.getString("skipMessage", "NOT checked");
        if (skipMessage.equals("checked"))
            return;

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.optionalshow);
        dialog.setTitle(getString(R.string.pageDashboardLong) + " Help");
        TextView helptext = (TextView) dialog.findViewById(R.id.helptext);
        helptext.setText(Html.fromHtml(getString(R.string.helpText)));
        // set the custom dialog components - text, image and button
        dontShowAgain = (CheckBox) dialog.findViewById(R.id.skip);
        dontShowAgain.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String checkBoxResult;
                if (((CheckBox) v).isChecked()) {
                    checkBoxResult = "checked";
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("skipMessage", checkBoxResult);
                    editor.apply();
                }

            }
        });
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, close the custom dialog

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        if (!skipMessage.equalsIgnoreCase("checked"))
            dialog.show();
     /*   new Eula(this, getString(R.string.appNameShort), getString(R.string.eula),
                getString(R.string.updates)).show();*/
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "CONNECTED");
    }



    private class UpdateAddress extends AsyncTask<Location, Integer, Long> {

        CGeo geo;

        protected Long doInBackground(Location... locs) {
            Location loc = mApp.getMyLocation();
            geo = new CGeo(Act_SafetyShield_Dashboard.this);
            boolean ret = geo.getAddress(loc);
            if (ret)
                return (long) 1;
            else
                return (long) 0;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            if (result > 0) {
                TextView tvAddr = (TextView) findViewById(R.id.tvAddr);
                if (!TextUtils.isEmpty(geo.mAddr.firstAddressString)) {
                    tvAddr.setText(geo.mAddr.firstAddressString);
                } else {
                    tvAddr.setText(getString(R.string.no_location));
                }

            }

        }
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    // starting the service to register with GCM
    private void registerGCM() {
        Intent intent = new Intent(this, GcmIntentService.class);
        intent.putExtra("key", "register");
        startService(intent);
    }


} // Activity_BeSafe_Dashboard

