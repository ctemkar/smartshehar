package lib.app.util.ui;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.ContactsContract.PhoneLookup;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lib.app.util.CAddress;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;
import lib.app.util.FetchAddressIntentService;
import lib.app.util.PinInfo_lib;
import lib.app.util.SSLog_SS;
import smartsheharcom.www.smartsheharlib.R;

public abstract class AbstractMapFragment_lib_act extends AppCompatActivity implements OnMapReadyCallback,
        SensorEventListener, LocationSource,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final float DEFAULT_ZOOM = 14;
    private static final int ACCESS_FINE_LOCATION_INTENT_ID = 7;
    protected boolean mInDirectionMode = false;
    protected ProgressDialog mProgressDialog;
    protected HashMap<Marker, PinInfo_lib> mPinHashMap = new HashMap<>();

    protected static final String TAG = "Abstract Map: ";
    protected final int ACTRESULT_FROM = 1;
    protected final int ACTRESULT_TO = 2;
    protected static final String FROM = "FROM", TO = "TO";
    protected GoogleMap mMap;
    protected Marker markerFrom = null, markerTo = null;
    protected ImageView mivZoomMyLocation;
    public TextView mTvFrom, mTvTo;
    protected ImageView mIvNoConnection, mIvNoLocation;
    protected LinearLayout mRlFromTo;
    private boolean mAutoRefreshFromLocation;
    protected CAddress moFromAddress = new CAddress(),
            moToAddress = new CAddress();
    LocationRequest mLocationRequest;
    // Milliseconds per second
    protected static final int MILLISECONDS_PER_SECOND = 1000;
    // The fastest update frequency, in seconds
    protected static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    protected static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
            * FASTEST_INTERVAL_IN_SECONDS;
    // Define an object that holds accuracy and frequency parameters
    protected String mDistance = "", mDuration = "";
    protected int iDuration = 0;
    protected float mDeclination;
    protected float[] mRotationMatrix;
    static boolean mIsMapFirstTime = false;
    protected LatLngBounds.Builder mBounds, mFriendsBounds, mZoomFitBounds;
    protected boolean doubleBackToExitPressedOnce;
    protected boolean mIsFromSelected = true;
    public Location mCurrentLocation = null;
    protected boolean mIsGettingDirections;
    protected String msTripPath = "";
    protected boolean isTripFrozen = false; // Do not change any input fields of
    protected List<LatLng> mLatLngTripPath; // Trip path polyline points

    private AddressResultReceiver mResultReceiver;
    protected String mAddressOutput;
    public GoogleApiClient mGoogleApiClient;
    private static final long INTERVAL = 1000 * 10;
    public RelativeLayout mLlMarkerLayout;

    public LinearLayout mLlFrom, mLlTo;
    protected ImageView mIvGreenPin, mIvRedPin;
    protected PolylineOptions mDirectionsPolyline;
    public boolean isZoomLocationAddress = false;
    public boolean isgotFirstLocation = false;

    protected abstract void callDriver(JSONArray jsonArray);

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
    } // onCreate

    protected void create() {
        SetupUI();
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        mResultReceiver = new AddressResultReceiver(new Handler());
    }

    private void SetupUI() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Getting route");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mLlFrom = (LinearLayout) findViewById(R.id.llFrom);
        mLlTo = (LinearLayout) findViewById(R.id.llTo);
        mivZoomMyLocation = (ImageView) findViewById(R.id.ivZoomMyLocation);
        mTvFrom = (TextView) findViewById(R.id.tvFrom);
        mTvTo = (TextView) findViewById(R.id.tvTo);
        mLlMarkerLayout = (RelativeLayout) findViewById(R.id.lLlocationMarker);
        mRlFromTo = (LinearLayout) findViewById(R.id.rlFromTo);
        mIvNoConnection = (ImageView) findViewById(R.id.ivNoNetConnection);
        mIvNoLocation = (ImageView) findViewById(R.id.ivNoLocation);
        mIvGreenPin = (ImageView) findViewById(R.id.ivGreenPin);
        mIvRedPin = (ImageView) findViewById(R.id.imageView2);
    } // SetuUI

    protected void init() {
        mBounds = new LatLngBounds.Builder();
        mFriendsBounds = new LatLngBounds.Builder();
        if (mMap == null) {
            setUpMapIfNeeded();
        } else {
            mapReady();
        }
    } // init

    protected void setLocation(final Location location) {
        if (!isAutoRefreshFromLocation()) {
            mAutoRefreshFromLocation = false;
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    moFromAddress.setLatitude(lat);
                    moFromAddress.setLongitude(lng);
                    if (mTvFrom != null && TextUtils.isEmpty(mTvFrom.getText())
                            && !isTripFrozen) {
                        moFromAddress = new CAddress();
                        moFromAddress.setLatitude(lat);
                        moFromAddress.setLongitude(lng);
                        mTvFrom.setHint(R.string.myLocation);
                        setAutoRefreshFromLocation(false);
                    }
                }
            }
        });
    }

    public void setFrom(CAddress oFromAddress, boolean showMarker) {
        moFromAddress = oFromAddress;
        if (!showMarker) {
            mTvFrom.setText(oFromAddress.getAddress());
        } else {
            setFrom(oFromAddress.getAddress(), new LatLng(oFromAddress.getLatitude(), oFromAddress.getLongitude()));
        }
    }

    public void setFrom(String sFrom, LatLng latlng) {
        if (!isTripFrozen) {
            mTvFrom.setText(sFrom);
            if (latlng != null) {
                CAddress addr = new CAddress();
                addr.setAddress(sFrom);
                addr.setLatitude(latlng.latitude);
                addr.setLongitude(latlng.longitude);
                moFromAddress = addr;
                drawFromMarker(moFromAddress);
            } else {
                Toast.makeText(
                        AbstractMapFragment_lib_act.this.getBaseContext(),
                        "Cannot get your current location\n. Please turn on network and gps locaion",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setTo(CAddress oToAddress, boolean showMarker) {
        moToAddress = oToAddress;
        if (!showMarker) {
            mTvTo.setText(oToAddress.getAddress());
        } else {
            setTo(oToAddress.getAddress(), new LatLng(oToAddress.getLatitude(), oToAddress.getLongitude()));
        }
    }

    public void setTo(String sto, LatLng latlng) {
        Log.d(TAG, String.valueOf(latlng));
        if (!isTripFrozen) {
            mTvTo.setText(sto);
            Toast.makeText(
                    AbstractMapFragment_lib_act.this.getBaseContext(),
                    "Cannot get your current location\n. Please turn on network and gps locaion",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickZoomMyLocation(View v) {
        onClickZoomMyLocation();
    }

    public void onClickZoomMyLocation() {
        isZoomLocationAddress = false;
       /* Location location = CGlobals_lib_ss.getInstance().getMyLocation(AbstractMapFragment_lib_act.this);
        mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;*/
        if (mCurrentLocation != null) {
            mMap.animateCamera(CameraUpdateFactory
                    .newLatLng(new LatLng(mCurrentLocation
                            .getLatitude(), mCurrentLocation
                            .getLongitude())));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation
                    .getLatitude(), mCurrentLocation
                    .getLongitude()), 17));
            mIsFromSelected = true;
        }
        mapReady();
    }

    @Override
    protected void onResume() {
        super.onResume();
    } // onResume

    protected void onPause() {
        CGlobals_lib_ss.getInstance()
                .writeRecentAddresses(getApplicationContext());
        clearMap();
        super.onPause();
    } // onPause

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            mIsMapFirstTime = true;
            /*mMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.map)).getMap();*/
            /*MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(AbstractMapFragment_lib_act.this);*/
            SupportMapFragment map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
            map.getMapAsync(this);
        }

    } // setupMapIfNeeded

    public void drawFromMarker(CAddress addr) {

        try {
            if (!addr.hasLatitude() || !addr.hasLongitude()) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (markerFrom != null) {
            markerFrom.remove();
        }
        markerFrom = null;
        markerFrom = mMap
                .addMarker(new MarkerOptions()
                        .position(
                                new LatLng(addr.getLatitude(), addr
                                        .getLongitude()))
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .alpha(0.8f).draggable(true)
                        .title(addr.getAddress()));
    } // drawFromMarker

    public void drawToMarker(CAddress addr) {
        try {
            if (!addr.hasLatitude() || !addr.hasLongitude()) {
                return;
            }
            if (markerTo != null) {
                markerTo.remove();
            }
            markerTo = null;
            markerTo = mMap.addMarker(new MarkerOptions()
                    .position(
                            new LatLng(moToAddress.getLatitude(), moToAddress
                                    .getLongitude()))
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .alpha(0.8f).title(addr.getAddress()));
            markerTo.setPosition(new LatLng(moToAddress.getLatitude(), moToAddress
                    .getLongitude()));
        } catch (Exception e) {
            Log.e(TAG, "drawToMarker");
        }
    } // drawToMarker

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(mRotationMatrix,
                    event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mRotationMatrix, orientation);
            double bearing = Math.toDegrees(orientation[0]) + mDeclination;
            updateCamera(bearing);
        }
    }

    private void updateCamera(double bearing) {
        CameraPosition oldPos = mMap.getCameraPosition();
        CameraPosition.builder(oldPos).bearing((float) bearing).build();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onDestroy() {
        super.onDestroy();
    }

    protected void hideViews() {
        mRlFromTo.setVisibility(View.GONE);
    }

    public void removeMarkers() {
        if (mPinHashMap == null)
            return;
        Log.d(TAG, "Size of HashMap : " + mPinHashMap.size());
        Iterator<Map.Entry<Marker, PinInfo_lib>> it = mPinHashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Marker, PinInfo_lib> pairs = it
                    .next();
            Marker m = pairs.getKey();
            m.remove();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
        mPinHashMap.clear();
    }

    public static String getContactName(Context context, String phoneNo) {
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNo));
        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor
                    .getColumnIndex(PhoneLookup.DISPLAY_NAME));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(this.getClass().getSimpleName(), "onConnectionFailed()");

    }

    @Override
    public void onConnected(Bundle arg0) {
    }

    public void clearMap() {
        if (CGlobals_lib_ss.getInstance().mDirectionsPolyline != null) {
            CGlobals_lib_ss.getInstance().mDirectionsPolyline = new PolylineOptions();
        }
        try {
            if (mPinHashMap != null) {
                mPinHashMap.clear();
            }
            if (mMap != null) {
                mMap.clear();
            }
            removeMarkers();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    protected void plotPathFromPolyLine(String sPolyLine) {
        try {
            mLatLngTripPath = PolyUtil.decode(sPolyLine);
            CGlobals_lib_ss.getInstance().mDirectionsPolyline = new PolylineOptions()
                    .addAll(mLatLngTripPath);
            CGlobals_lib_ss.getInstance().mDirectionsPolyline.width(4);
            CGlobals_lib_ss.getInstance().mDirectionsPolyline.color(Color.BLACK);
            mZoomFitBounds = new LatLngBounds.Builder();
            for (LatLng latLng : mLatLngTripPath) {
                mZoomFitBounds
                        .include(new LatLng(latLng.latitude, latLng.longitude));
            }

            mMap.addPolyline(CGlobals_lib_ss.getInstance().mDirectionsPolyline);
        } catch (Exception e) {
            SSLog_SS.e(TAG, " plotPathFromPolyLine ", e, AbstractMapFragment_lib_act.this);
        }

    }

    protected void progressMessage(String msg) {
        if (!isFinishing()) {
            mProgressDialog.setMessage(msg);
            mProgressDialog.show();
        }
    }

    protected void progressCancel() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }

    }

    protected abstract String getGeoCountryCode();

    protected abstract void gotGoogleMapLocation(Location location);

    public boolean isAutoRefreshFromLocation() {
        return mAutoRefreshFromLocation;
    }

    public void setAutoRefreshFromLocation(boolean mAutoRefreshFromLocation) {
        this.mAutoRefreshFromLocation = mAutoRefreshFromLocation;
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Display the address string or an error message sent from the
            // intent service.
            try {
                mAddressOutput = resultData
                        .getString(Constants_lib_ss.RESULT_DATA_KEY);
                // Show a toast message if an address was found.
                if (resultCode == Constants_lib_ss.SUCCESS_RESULT) {
                    if (!TextUtils.isEmpty(mAddressOutput)) {
                        Location location = CGlobals_lib_ss.getInstance().getMyLocation(AbstractMapFragment_lib_act.this);
                        moFromAddress.setLatitude(location.getLatitude());
                        moFromAddress.setLongitude(location.getLongitude());
                        moFromAddress.setAddress(mAddressOutput);
                        setFrom(moFromAddress, false);
                        moFromAddress.setAddress(mAddressOutput);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "AddressResultReceiver: " + e);
            }
        }
    }

    protected void startIntentServiceCurrentAddress(Location location) {
        try {
            if (isTripFrozen || !isAutoRefreshFromLocation()) {
                return;
            }
            Intent intent = new Intent(AbstractMapFragment_lib_act.this,
                    FetchAddressIntentService.class);
            intent.putExtra(Constants_lib_ss.RECEIVER, mResultReceiver);
            intent.putExtra(Constants_lib_ss.LOCATION_DATA_EXTRA, location);
            startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // startIntentServiceCurrentAddress

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    iStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            SSLog_SS.e(TAG, "downloading url- ", e, AbstractMapFragment_lib_act.this);
            Toast.makeText(AbstractMapFragment_lib_act.this, getString(R.string.failedToConnect), Toast.LENGTH_LONG).show();
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }

    // Fetches data from url passed
    public class GoogleDirection extends AsyncTask<String, Void, String> {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                // Fetching the data from web service
                data = AbstractMapFragment_lib_act.this.downloadUrl(url[0]);
            } catch (Exception e) {
                mIsGettingDirections = false;
                SSLog_SS.e(TAG, "Background Task ", e, AbstractMapFragment_lib_act.this);
            }
            return data;
        }

        @Override
        protected void onPreExecute() {
            if (!isFinishing())
                mProgressDialog.show();
            mProgressDialog.setMessage(getString(R.string.gettingDirections));
            Log.d(TAG, "DownloadTask: PreExecute");
            super.onPreExecute();
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONArray aoPoint = new JSONArray();
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray routesArray = jsonObject.getJSONArray("routes");
                // int i = 1; // Need to process only first index
                for (int i = 0; i < routesArray.length(); i++) {
                    aoPoint = new JSONArray();
                    JSONObject route = routesArray.getJSONObject(i);
                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);

                    JSONObject durationObject = leg.getJSONObject("duration");
                    mDuration = durationObject.getString("text");
                    iDuration = durationObject.getInt("value");
                    iDuration = (int) Math.round((double) iDuration / 60.0);
                    JSONObject distanceObject = leg.getJSONObject("distance");
                    mDistance = distanceObject.getString("value");
                    JSONObject polyline = route.getJSONObject("overview_polyline");
                    msTripPath = polyline.getString("points");
                    String json = new Gson().toJson(leg);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(getApplicationContext())
                            .putString(Constants_lib_ss.TRIP_JSON_OBJECT,
                                    json);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(getApplicationContext())
                            .putString(Constants_lib_ss.TRIP_FROM_ADDRESS,
                                    leg.getString("start_address"));
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(getApplicationContext())
                            .putString(Constants_lib_ss.TRIP_TO_ADDRESS,
                                    leg.getString("end_address"));
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(getApplicationContext())
                            .commit();
                    mLatLngTripPath = PolyUtil.decode(polyline.getString("points"));
                    mDirectionsPolyline = new PolylineOptions()
                            .addAll(mLatLngTripPath);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(getApplicationContext())
                            .putString(Constants_lib_ss.TRIP_PATH, msTripPath);
                    CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(getApplicationContext())
                            .commit();
                    System.out.println("msTripPath Create    " + msTripPath);
                    String joinpath = CGlobals_lib_ss.getInstance().getPersistentPreference(getApplicationContext())
                            .getString(Constants_lib_ss.TRIP_PATH, "");
                    System.out.println("msTripPath join   " + joinpath);
                    mDirectionsPolyline.width(4);
                    mDirectionsPolyline.color(Color.BLACK);
                    mMap.addPolyline(mDirectionsPolyline);
                    mZoomFitBounds = new LatLngBounds.Builder();
                    for (LatLng latLng : mLatLngTripPath) {
                        try {
                            JSONObject oPoint = new JSONObject();
                            oPoint.put("lat", latLng.latitude);
                            oPoint.put("lng", latLng.longitude);
                            aoPoint.put(oPoint);
                            mZoomFitBounds.include(new LatLng(latLng.latitude,
                                    latLng.longitude));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                callDriver(aoPoint);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(AbstractMapFragment_lib_act.this.getBaseContext(),
                        "Failed to get directions, please try again",
                        Toast.LENGTH_SHORT).show();
                progressCancel();
            }
            mIsGettingDirections = false;
            // Invokes the thread for parsing the JSON data
            Log.d(TAG, "DownloadTask: DONE");
        }
    } // DownloadTask

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }
        CGlobals_lib_ss.setMyLocation(location, true, AbstractMapFragment_lib_act.this);
        mCurrentLocation = CGlobals_lib_ss.isBetterLocation(location, mCurrentLocation) ? location
                : mCurrentLocation;
        //startIntentServiceCurrentAddress(mCurrentLocation);
        if (!isgotFirstLocation) {
            mapReady();
        }
        isgotFirstLocation = true;
        gotGoogleMapLocation(location);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            //mMap.setLocationSource(this);
            Location location = CGlobals_lib_ss.getInstance().getMyLocation(
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
        mapReady();
    }

    protected abstract void mapReady();
} // AbstractMapFragmentActivity

