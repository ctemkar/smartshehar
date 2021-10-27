package com.jumpinjumpout.apk.user.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.ui.SearchAddress_act;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;
import com.paytm.pgsdk.PaytmMerchant;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import lib.app.util.CAddress;
import lib.app.util.Constants_lib_ss;
import lib.app.util.FetchAddressIntentService;
import lib.app.util.MyLocation;

abstract public class Actrips_act extends Activity
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    static String TAG = "Actrips_act: ";
    static String msTripType = Constants_user.TRIP_TYPE_USER;
    public static boolean isClickButton = false;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    static Calendar startDateTime;
    private final int ACTRESULT_FROM = 1;
    private final int ACTRESULT_TO = 2;
    public TextView tvStartTime;
    public static TextView btnLongDistance;
    public static TextView mTvFrom, mTvTo;
    protected boolean mIsFromSelected = true;
    public ProgressDialog mProgressDialog;
    private static boolean mFilterTrip = false;
    public static TextView mTextfriend, mTextdriver;
    public static double fromLat = Constants_user.INVALIDLAT,
            fromLng = Constants_user.INVALIDLNG;
    public static double toLat = Constants_user.INVALIDLAT,
            toLng = Constants_user.INVALIDLNG;
    public static String mtvToAddress;
    private AddressResultReceiver mResultReceiver;
    public boolean isLocation = false;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    public static LinearLayout llfriendcabButton;
    private int randomInt = 0;
    private PaytmPGService Service = null;

    static public ArrayList<CTrip> userTrips;
    static public ArrayList<CTrip> cabTrips;

    protected abstract void getCheckTripPathNotification();

    protected abstract void getActiveTrips();

    protected abstract String longdistancemarathivehiclecategory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.active_trips);
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        if (!MyApplication.getInstance().getPersistentPreference().getBoolean(Constants_user.PREF_GOTIT_ACTIVE_TRIPS, false)) {
            doGotIt();
        }
        mResultReceiver = new AddressResultReceiver(new Handler());

        mProgressDialog = new ProgressDialog(Actrips_act.this);
        mProgressDialog.setMessage("Please wait");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        CGlobals_lib.getInstance().maoRecentAddress = CGlobals_lib.getInstance()
                .readRecentAddresses(this);
        CGlobals_user.getInstance().setFromAddr(new CAddress());
        CGlobals_user.getInstance().setToAddr(new CAddress());
        mTvFrom = (TextView) findViewById(R.id.ac_from);
        mTvTo = (TextView) findViewById(R.id.ac_to);
        Button mButtonGoNow = (Button) findViewById(R.id.btnGoNow);
        tvStartTime = (TextView) findViewById(R.id.tvStartTime);
        llfriendcabButton = (LinearLayout) findViewById(R.id.llfriendcabButton);
        mTextfriend = (TextView) findViewById(R.id.btnfriend);
        mTextdriver = (TextView) findViewById(R.id.btndriver);
        btnLongDistance = (TextView) findViewById(R.id.btnLongDistance);
        btnLongDistance.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Actrips_act.this, LongDistanceCar_act.class);
                startActivity(intent);
            }
        });

        mCurrentLocation = CGlobals_user.getInstance().getMyLocation(Actrips_act.this);
        if (mCurrentLocation != null) {
            fromLat = mCurrentLocation.getLatitude();
            fromLng = mCurrentLocation.getLongitude();
            startIntentService(mCurrentLocation);
            MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLAT", (float) fromLat);
            MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLNG", (float) fromLng);
            MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        }

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager) getSystemService(ns);
        mNotificationManager.cancel(Constants_user.SERVER_NOTIFICATION_ID);

        SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.getDefault());
        String sTime = sdf.format(Calendar.getInstance().getTime());
        tvStartTime.setText(sTime);
        userTrips = new ArrayList<CTrip>();
        cabTrips = new ArrayList<CTrip>();
        mTextfriend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                isClickButton = false;
                msTripType = Constants_user.TRIP_TYPE_USER;
                refresh();
                resetButtonColors(msTripType);
            }
        });

        mTextdriver.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                isClickButton = true;
                msTripType = Constants_user.TRIP_TYPE_COMMERCIAL;
                refresh();
                resetButtonColors(msTripType);
            }
        });

        mButtonGoNow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mFilterTrip = true;
            }
        });
        CGlobals_user.getInstance().setDisplayTripType(Constants_user.COMMERCIAL);

        mTvFrom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Actrips_act.this,
                        SearchAddress_act.class);
                startActivityForResult(i, ACTRESULT_FROM);
            }
        });
        mTvTo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Actrips_act.this,
                        SearchAddress_act.class);
                startActivityForResult(i, ACTRESULT_TO);
            }
        });
        CGlobals_lib.showGPSDialog = true;
        MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(Constants_user.PREF_NOTIFICATION_CLEAR_FLAG, true);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
    } // onCreate

    public static void showCabButton() {
        mTextfriend.setVisibility(View.VISIBLE);
        mTextdriver.setVisibility(View.VISIBLE);
    }

    public static void hideCabButton() {
        mTextfriend.setVisibility(View.GONE);
        mTextdriver.setVisibility(View.GONE);
    }

    public static void hideAllButton() {
        mTextfriend.setVisibility(View.GONE);
        mTextdriver.setVisibility(View.GONE);
        btnLongDistance.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        MyLocation myLocation = new MyLocation(
                MyApplication.mVolleyRequestQueue, Actrips_act.this, mGoogleApiClient);
        myLocation.getLocation(this, onLocationResult);
        CGlobals_lib.getInstance().turnGPSOn1(Actrips_act.this, mGoogleApiClient);
        CGlobals_user.getInstance().runRightActivity(this);
        CGlobals_user.getInstance().sendUpdatePosition(
                CGlobals_user.getInstance().getMyLocation(Actrips_act.this), Actrips_act.this);
        resetButtonColors(msTripType);
        refresh();
        super.onResume();
        MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(Constants_user.PREF_NOTIFICATION_CLEAR_FLAG, true);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
    }


    public void resetButtonColors(String sTripType) {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.thin_frame, null);
        Drawable drawableSelected = ResourcesCompat.getDrawable(getResources(), R.drawable.frame_selected, null);
        mTextfriend.setBackgroundDrawable(drawable);
        mTextdriver.setBackgroundDrawable(drawable);
        if (sTripType.equals(Constants_user.TRIP_TYPE_USER)) {
            mTextfriend.setBackgroundDrawable(drawableSelected);
        }
        if (sTripType.equals(Constants_user.TRIP_TYPE_COMMERCIAL)) {
            mTextdriver.setBackgroundDrawable(drawableSelected);
        }
    }


    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
    }


    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String sTime = String.format(Locale.getDefault(), "%d:%02d",
                    hourOfDay, minute);

            startDateTime = Calendar.getInstance();
            startDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            startDateTime.set(Calendar.MINUTE, minute);
            ((Actrips_act) getActivity()).tvStartTime.setText(sTime);
            ((Actrips_act) getActivity()).refresh();
        }
    }


    abstract protected void doGotIt();


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String sAddr = "";
        CAddress oAddr = null;
        if (requestCode == ACTRESULT_FROM) {
            if (resultCode == RESULT_OK) {
                sAddr = data.getStringExtra("street_address");
                fromLat = data.getDoubleExtra("lat", Constants_user.INVALIDLAT);
                fromLng = data.getDoubleExtra("lng", Constants_user.INVALIDLNG);
                oAddr = new CAddress();
                oAddr.setAddress(sAddr);
                oAddr.setLatitude(fromLat);
                oAddr.setLongitude(fromLng);
                isLocation = true;
                mTvFrom.setText(sAddr);
                if (!TextUtils.isEmpty(sAddr)) {
                    mTvFrom.setText(sAddr);
                    if (!isFinishing()) {
                    }

                    mIsFromSelected = false;
                }
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLAT", (float) fromLat);
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLNG", (float) fromLng);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_START_ADDRESS", sAddr);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
            }
            if (resultCode == RESULT_CANCELED) {
                // Write your code if there's no result
            }
        }
        if (requestCode == ACTRESULT_TO) {
            if (resultCode == RESULT_OK) {

                sAddr = data.getStringExtra("street_address");
                toLat = data.getDoubleExtra("lat", Constants_user.INVALIDLAT);
                toLng = data.getDoubleExtra("lng", Constants_user.INVALIDLNG);
                oAddr = new CAddress();
                oAddr.setAddress(sAddr);
                oAddr.setLatitude(toLat);
                oAddr.setLongitude(toLng);
                mTvTo.setText(sAddr);
                if (!TextUtils.isEmpty(sAddr)) {
                    mTvTo.setText(sAddr);
                    if (!isFinishing()) {

                    }
                }
                mtvToAddress = mTvTo.getText().toString();
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLAT", (float) toLat);
                MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLNG", (float) toLng);
                MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_DESTINATION_ADDRESS", sAddr);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                getCheckTripPathNotification();
            }
            if (resultCode == RESULT_CANCELED) {
                // Write your code if there's no result
            }
        }

        if (requestCode == CGlobals_lib.REQUEST_LOCATION) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");
                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    CGlobals_lib.showGPSDialog = false;
                    finish();
                    break;
                default:
                    break;
            }
        }
        getActiveTrips();
        if (!TextUtils.isEmpty(sAddr)) {
            CGlobals_lib.getInstance().addRecentAddress(oAddr);
        }

    } // onActivityResult


    @Override
    protected void onPause() {
        CGlobals_lib.getInstance()
                .writeRecentAddresses(getApplicationContext());
        super.onPause();
        System.gc();
    }

    public static boolean getFilterTrip() {
        return mFilterTrip;
    }

    public abstract void refresh();

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the
            // intent service.
            String mAddressOutput = resultData
                    .getString(Constants_lib_ss.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (resultCode == Constants_user.SUCCESS_RESULT && !isLocation) {
                mTvFrom.setText(mAddressOutput);
                MyApplication.getInstance().getPersistentPreferenceEditor()
                        .putString("PASSENGER_START_ADDRESS", mAddressOutput);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
            }

        }
    }

    protected void startIntentService(Location location) {

        try {
            Intent intent = new Intent(Actrips_act.this,
                    FetchAddressIntentService.class);
            intent.putExtra(Constants_lib_ss.RECEIVER,
                    mResultReceiver);
            intent.putExtra(Constants_lib_ss.LOCATION_DATA_EXTRA,
                    location);
            startService(intent);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    } // startIntentServiceCurrentAddress

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        startIntentService(mCurrentLocation);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected!");

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            if (location != null) {
                CGlobals_user.getInstance().setMyLocation(location, false);
                CGlobals_user.getInstance().sendUpdatePosition(
                        CGlobals_user.getInstance().getMyLocation(Actrips_act.this), getApplicationContext());
            }
        }


    };

    public void getPaytm() {
        try {
            Random randomGenerator = new Random();
            randomInt = randomGenerator.nextInt(1000);
            Service = PaytmPGService.getStagingService();
            PaytmMerchant Merchant = new PaytmMerchant(
                    "http://www.jumpinjumpout.com/prod/external/pm/generateChecksum.php",
                    "http://www.jumpinjumpout.com/prod/external/pm/verifyChecksum.php");

            Map<String, String> paramMap = new HashMap<String, String>();
            //paramMap.put("REQUEST_TYPE", "DEFAULT");
            paramMap.put("ORDER_ID", String.valueOf(randomInt));
            paramMap.put("MID", "jumpin91593054142241");
            paramMap.put("CUST_ID", "CUST123");
            paramMap.put("CHANNEL_ID", "WAP");
            paramMap.put("INDUSTRY_TYPE_ID", "Retail");
            paramMap.put("WEBSITE", "jumpinwap");
            paramMap.put("TXN_AMOUNT", "1");
            paramMap.put("THEME", "merchant");
            paramMap.put("EMAIL", "soumen.maity@techsindia.com");
            paramMap.put("MOBILE_NO", "8452983096");

            PaytmOrder Order = new PaytmOrder(paramMap);
            Service.initialize(Order, Merchant, null);
            Service.startPaymentTransaction(this, false, false, new PaytmPaymentTransactionCallback() {

                @Override
                public void someUIErrorOccurred(String inErrorMessage) {
                    Log.i("Paytm", "Paytm :");
                }

                @Override
                public void onTransactionSuccess(Bundle inResponse) {
                    Log.i("Paytm", "Paytm :");
                }

                @Override
                public void onTransactionFailure(String inErrorMessage, Bundle inResponse) {
                    Log.i("Paytm", "Paytm :");
                }

                @Override
                public void networkNotAvailable() {
                    Log.i("Paytm", "Paytm :");
                }

                @Override
                public void clientAuthenticationFailed(String inErrorMessage) {
                    Log.i("Paytm", "Paytm :");
                }

                @Override
                public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                    Log.i("Paytm", "Paytm :");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLAT", (float) Constants_user.INVALIDLAT);
        MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("FROMLNG", (float) Constants_user.INVALIDLNG);
        MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_START_ADDRESS", "");
        MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLAT", (float) Constants_user.INVALIDLAT);
        MyApplication.getInstance().getPersistentPreferenceEditor().putFloat("TOLNG", (float) Constants_user.INVALIDLNG);
        MyApplication.getInstance().getPersistentPreferenceEditor().putString("PASSENGER_DESTINATION_ADDRESS", "");
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
    }

    public void showpDialog() {
        if (!isFinishing()) {
            mProgressDialog.setMessage("Please wait ...");
            mProgressDialog.show();
        }
    }

    public void hidepDialog() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }
    }
}