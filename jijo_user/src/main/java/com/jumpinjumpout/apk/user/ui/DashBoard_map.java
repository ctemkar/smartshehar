package com.jumpinjumpout.apk.user.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CGlobals_user;

import lib.app.util.CAddress;
import lib.app.util.Constants_lib_ss;
import lib.app.util.FetchAddressIntentService;

/**
 * Created by jijo_soumen on 15/01/2016.
 */
public class DashBoard_map extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMyLocationChangeListener {

    private static String TAG = "DashBoard_map: ";
    private TextView tvDasStartAddress;
    private ImageView ivDasGpsLocation, ivHome1, ivHome2, ivHome3, ivWork1, ivWork2, ivWork3, ivFab1, ivFab2, ivFab3;
    private LinearLayout llFindaRide, llShareaRide, llGetaCab;
    private Button btnAvailableRides, btnCabsAround, btnUserUsingApp;
    public GoogleApiClient mGoogleApiClient;
    private AddressResultReceiver mResultReceiver;
    protected String mAddressOutput;
    protected GoogleMap mMap;
    public static final float DEFAULT_ZOOM = 15;
    public Location mCurrentLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashborad_sample);
        init();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(DashBoard_map.this)
                .addOnConnectionFailedListener(DashBoard_map.this)
                .build();
        mGoogleApiClient.connect();
        mResultReceiver = new AddressResultReceiver(new Handler());
        if (mMap == null) {
            setUpMapIfNeeded();
        }
        mCurrentLocation = CGlobals_user.getInstance().getMyLocation(DashBoard_map.this);
        if (mCurrentLocation != null) {
            startIntentServiceCurrentAddress(mCurrentLocation);
        }
        tvDasStartAddress.setHint("#Getting address..");

        ivDasGpsLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentLocation = CGlobals_user.getInstance().getBestLocation(DashBoard_map.this);
                if (mCurrentLocation != null) {
                    startIntentServiceCurrentAddress(mCurrentLocation);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Internet problem. Please try again...", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        llFindaRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentFindaRide = new Intent(DashBoard_map.this, Active_trips_act.class);
                startActivity(intentFindaRide);
            }
        });

        llShareaRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentFindaRide = new Intent(DashBoard_map.this, RecentTripUser_act.class);
                startActivity(intentFindaRide);
            }
        });

        llGetaCab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentFindaRide = new Intent(DashBoard_map.this, GetaCab_act.class);
                startActivity(intentFindaRide);
            }
        });

        ivHome1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickGoHome();
            }
        });

        ivHome2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickGoHome();
            }
        });

        ivHome3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickGoHome();
            }
        });

        ivWork1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickGoWork();
            }
        });

        ivWork2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickGoWork();
            }
        });

        ivWork3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickGoWork();
            }
        });
    }

    private void init() {
        tvDasStartAddress = (TextView) findViewById(R.id.tvDasStartAddress);
        ivDasGpsLocation = (ImageView) findViewById(R.id.ivDasGpsLocation);
        ivHome1 = (ImageView) findViewById(R.id.ivHome1);
        ivHome2 = (ImageView) findViewById(R.id.ivHome2);
        ivHome3 = (ImageView) findViewById(R.id.ivHome3);
        ivWork1 = (ImageView) findViewById(R.id.ivWork1);
        ivWork2 = (ImageView) findViewById(R.id.ivWork2);
        ivWork3 = (ImageView) findViewById(R.id.ivWork3);
        ivFab1 = (ImageView) findViewById(R.id.ivFab1);
        ivFab2 = (ImageView) findViewById(R.id.ivFab2);
        ivFab3 = (ImageView) findViewById(R.id.ivFab3);
        llFindaRide = (LinearLayout) findViewById(R.id.llFindaRide);
        llShareaRide = (LinearLayout) findViewById(R.id.llShareaRide);
        llGetaCab = (LinearLayout) findViewById(R.id.llGetaCab);
        btnAvailableRides = (Button) findViewById(R.id.btnAvailableRides);
        btnCabsAround = (Button) findViewById(R.id.btnCabsAround);
        btnUserUsingApp = (Button) findViewById(R.id.btnUserUsingApp);
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(
                    com.jumpinjumpout.apk.lib.R.id.map)).getMap();
            if (mMap != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.setOnMyLocationChangeListener(DashBoard_map.this);
                Location location = CGlobals_lib.getInstance().getMyLocation(
                        this);
                if (location != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location
                                    .getLongitude()), DEFAULT_ZOOM));
                }
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                    }
                });
            }
        }
    } // setupMapIfNeeded

    @Override
    public void onMyLocationChange(Location location) {
        if (location == null) {
            return;
        } else {
            startIntentServiceCurrentAddress(location);
        }
        mCurrentLocation = CGlobals_lib.getInstance().isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        CGlobals_lib.getInstance().setMyLocation(location, true);
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the
            // intent service.
            mAddressOutput = resultData
                    .getString(Constants_lib_ss.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (resultCode == Constants_lib.SUCCESS_RESULT) {
                tvDasStartAddress.setText(mAddressOutput);
            }
        }
    }

    protected void startIntentServiceCurrentAddress(Location location) {

        try {
            Intent intent = new Intent(DashBoard_map.this,
                    FetchAddressIntentService.class);
            intent.putExtra(Constants_lib_ss.RECEIVER, mResultReceiver);
            intent.putExtra(Constants_lib_ss.LOCATION_DATA_EXTRA, location);
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // startIntentServiceCurrentAddress

    public void onClickGoHome() {
        Intent intent = new Intent(DashBoard_map.this, FriendPool_act.class);
        CAddress oHomeAddress = CGlobals_user.getInstance().getHomeAddress(this);
        Location currentLocation = CGlobals_user.getInstance().getBestLocation(DashBoard_map.this);
        float[] dist = new float[1];
        if (oHomeAddress == null) {
            Toast.makeText(DashBoard_map.this, "Use the left menu at the top, go to Settings and set your Home address",
                    Toast.LENGTH_LONG).show();
            Intent intent1 = new Intent(DashBoard_map.this, Settings_act.class);
            startActivity(intent1);
            return;
        } else {
            if (currentLocation != null) {
                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                        oHomeAddress.getLatitude(),
                        oHomeAddress.getLongitude(), dist);
                if (dist[0] < 1000) {
                    Toast.makeText(DashBoard_map.this, "You are already close to home",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        if (currentLocation != null) {
            intent.putExtra("DEST", "HOME");
        } else {
            Toast.makeText(DashBoard_map.this, "Cannot use this feature, location not available!",
                    Toast.LENGTH_LONG).show();
        }
        startActivity(intent);
    }

    public void onClickGoWork() {
        Intent intent = new Intent(DashBoard_map.this, FriendPool_act.class);
        CAddress oWorkAddress = CGlobals_user.getInstance().getWorkAddress(DashBoard_map.this);
        Location currentLocation = CGlobals_user.getInstance().getBestLocation(DashBoard_map.this);
        float[] dist = new float[1];
        if (oWorkAddress == null) {
            Toast.makeText(DashBoard_map.this, "Use the left menu at the top, go to Settings and set your Work address",
                    Toast.LENGTH_LONG).show();
            Intent intent1 = new Intent(DashBoard_map.this, Settings_act.class);
            startActivity(intent1);
            return;
        } else {
            if (currentLocation != null) {
                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                        oWorkAddress.getLatitude(),
                        oWorkAddress.getLongitude(), dist);
                if (dist[0] < 1000) {
                    Toast.makeText(DashBoard_map.this, "You are already close to work",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        if (currentLocation != null) {
            intent.putExtra("DEST", "WORK");
        } else {
            Toast.makeText(DashBoard_map.this, "Cannot use this feature, location not available!",
                    Toast.LENGTH_LONG).show();
        }
        startActivity(intent);
    }
}
