package com.smartshehar.dashboard.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;
import com.smartshehar.dashboard.app.SSLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lib.app.util.CAddress;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.FetchAddressIntentService;
import lib.app.util.MyLocation;
import lib.app.util.SearchAddress_act;


public class FullMapShowTrainBus_act extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMyLocationChangeListener {

    private static String TAG = "FullMap_T_B: ";
    GoogleApiClient mGoogleApiClient = null;
    boolean mIsMapFirstTime = false;
    protected GoogleMap mMap;
    public static final float DEFAULT_ZOOM = 15;
    Location mCurrentLocation;
    private LatLngBounds.Builder bounds;
    Connectivity mConnectivity;
    private LinearLayout mLlFrom, mLlTo;
    private TextView mTvFrom, mTvTo;
/*    private String cityname = "";
    private String sublocality = "";
    private String formatted_address = "";
    private String route = "";
    private String neighborhood = "";
    private String administrative_area_level_2 = "";
    private String administrative_area_level_1 = "";
    private double latitude = Constants_lib_ss.INVALIDLAT, longitude = Constants_lib_ss.INVALIDLNG;*/
    protected List<LatLng> mLatLngTripPath;
    protected Marker markerFrom = null, markerTo = null;
    protected CAddress moFromAddress = new CAddress(),
            moToAddress = new CAddress();
    private AddressResultReceiver mResultReceiver;
    protected String mAddressOutput;
    protected String msTripPath = "";
    private Button mBtnGo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullmapshow_act);
        doGotIt(false);
        mGoogleApiClient = new GoogleApiClient.Builder(FullMapShowTrainBus_act.this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(FullMapShowTrainBus_act.this)
                .addOnConnectionFailedListener(FullMapShowTrainBus_act.this)
                .build();
        mGoogleApiClient.connect();
        MyLocation myLocation = new MyLocation(
                SSApp.mRequestQueue, FullMapShowTrainBus_act.this, mGoogleApiClient);
        myLocation.getLocation(this, onLocationResult);
        init();
        mResultReceiver = new AddressResultReceiver(new Handler());
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(FullMapShowTrainBus_act.this)) {
            if (!mConnectivity.connectionError(FullMapShowTrainBus_act.this)) {
                if (mConnectivity.isGPSEnable(FullMapShowTrainBus_act.this)) {
                    Log.d(TAG,"Internet connection");
                }
            }
        }
        setUpMapIfNeeded();
        mLlFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FullMapShowTrainBus_act.this, SearchAddress_act.class);
                startActivityForResult(intent, Constants_lib_ss.FINDADDRESS_FROM);
            }
        });
        mLlTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FullMapShowTrainBus_act.this, SearchAddress_act.class);
                startActivityForResult(intent, Constants_lib_ss.FINDADDRESS_TO);
            }
        });
        mBtnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTvFrom != null && mTvTo != null) {
                    drawFromMarker(moFromAddress);
                    drawToMarker(moToAddress);
                    plotPathFromPolyLine(msTripPath);
                }
            }
        });
    }

    private void init() {
        mLlFrom = (LinearLayout) findViewById(R.id.llFrom);
        mLlTo = (LinearLayout) findViewById(R.id.llTo);
        mTvFrom = (TextView) findViewById(R.id.tvFrom);
        mTvTo = (TextView) findViewById(R.id.tvTo);
        mBtnGo = (Button) findViewById(R.id.btnGo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String sAddr;
        CAddress oAddr;
        if (requestCode == Constants_lib_ss.FINDADDRESS_FROM) {

            if (resultCode == Activity.RESULT_OK) {
                sAddr = data.getStringExtra("add");
                Type type = new TypeToken<CAddress>() {
                }.getType();
                oAddr = new Gson().fromJson(sAddr, type);

                if (oAddr.getLatitude() != Constants_lib_ss.INVALIDLAT
                        && oAddr.getLongitude() != Constants_lib_ss.INVALIDLNG) {

                    if (sAddr.equals("")) {
                        Toast.makeText(FullMapShowTrainBus_act.this, "Please check your internet connection", Toast.LENGTH_LONG).show();

                    }
                    mTvFrom.setText(oAddr.getAddress());
                    moFromAddress = new CAddress();
                    moFromAddress.setLatitude(oAddr.getLatitude());
                    moFromAddress.setLongitude(oAddr.getLongitude());
                    drawFromMarker(moFromAddress);
                    //setData(oAddr);
                }
            }
        }
        if (requestCode == Constants_lib_ss.FINDADDRESS_TO) {

            if (resultCode == Activity.RESULT_OK) {
                sAddr = data.getStringExtra("add");
                Type type = new TypeToken<CAddress>() {
                }.getType();
                oAddr = new Gson().fromJson(sAddr, type);

                if (oAddr.getLatitude() != Constants_lib_ss.INVALIDLAT
                        && oAddr.getLongitude() != Constants_lib_ss.INVALIDLNG) {

                    if (sAddr.equals("")) {
                        Toast.makeText(FullMapShowTrainBus_act.this, "Please check your internet connection", Toast.LENGTH_LONG).show();

                    }
                    mTvTo.setText(oAddr.getAddress());
                    moToAddress = new CAddress();
                    moToAddress.setLatitude(oAddr.getLatitude());
                    moToAddress.setLongitude(oAddr.getLongitude());
                    drawToMarker(moToAddress);
                    // setData(oAddr);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTvFrom != null && mTvTo != null) {
            drawFromMarker(moFromAddress);
            drawToMarker(moToAddress);
            plotPathFromPolyLine(msTripPath);
        }

    }

    protected void plotPathFromPolyLine(String sPolyLine) {
        try {
            mLatLngTripPath = PolyUtil.decode(sPolyLine);
            CGlobals_db.getInstance(FullMapShowTrainBus_act.this).mDirectionsPolyline = new PolylineOptions()
                    .addAll(mLatLngTripPath);
            CGlobals_db.getInstance(FullMapShowTrainBus_act.this).mDirectionsPolyline.width(4);
            CGlobals_db.getInstance(FullMapShowTrainBus_act.this).mDirectionsPolyline.color(Color.BLACK);
            bounds = new LatLngBounds.Builder();
            for (LatLng latLng : mLatLngTripPath) {
                bounds
                        .include(new LatLng(latLng.latitude, latLng.longitude));
            }

            mMap.addPolyline(CGlobals_db.getInstance(FullMapShowTrainBus_act.this).mDirectionsPolyline);
        } catch (Exception e) {
            SSLog.e(TAG, "plotPathFromPolyLine:- ", e);
        }

    }
    private void doGotIt(boolean alwayaShow) {
        CGlobals_db.getInstance(this).gotIt(this,
                Constants_dp.PREF_GOTIT_ACTIVE_TRIPS, alwayaShow, getString(R.string.helplinetext1), "");
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            try {
                if (location == null) {
                    Log.d(TAG,"No Location found");
                } else {
                    CGlobals_db.getInstance(FullMapShowTrainBus_act.this).setMyLocation(location, false,FullMapShowTrainBus_act.this);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    protected void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            mIsMapFirstTime = true;
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(
                    R.id.map)).getMap();
            if (mMap != null) {
                if (ActivityCompat.checkSelfPermission(FullMapShowTrainBus_act.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(FullMapShowTrainBus_act.this,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return ;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.setOnMyLocationChangeListener(FullMapShowTrainBus_act.this);
                // mMap.setTrafficEnabled(true);
                Location location = CGlobals_db.getInstance(FullMapShowTrainBus_act.this).getMyLocation(
                        this);
                if (location != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location
                                    .getLongitude()), DEFAULT_ZOOM));
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    moFromAddress = new CAddress();
                    moFromAddress.setLatitude(lat);
                    moFromAddress.setLongitude(lng);
                    drawFromMarker(moFromAddress);
                    startIntentServiceCurrentAddress(location);
                }
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mapLoaded();
                    }
                });

            }
        }
    } // setupMapIfNeeded

    private void mapLoaded() {
        if (mCurrentLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation
                            .getLongitude()), DEFAULT_ZOOM));
        }
        showNear();

    }

    @Override
    public void onMyLocationChange(Location location) {
        mCurrentLocation = location;
    }

    private void showNear() {
        if (mCurrentLocation == null)
            return;
        mMap.clear();
        bounds = new LatLngBounds.Builder();
        bounds.include(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        StringBuilder sbValue = new StringBuilder(sbMethod("bus_station"));
        PlacesTask placesTask = new PlacesTask();
        placesTask.execute(sbValue.toString());
        sbValue = new StringBuilder(sbMethod("train_station|subway_station"));
        placesTask = new PlacesTask();
        placesTask.execute(sbValue.toString());
    }

    public StringBuilder sbMethod(String type) {
        //use your current location here
        double mLatitude = mCurrentLocation.getLatitude();
        double mLongitude = mCurrentLocation.getLongitude();

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=").append(mLatitude).append(",").append(mLongitude);
//        sb.append("&radius=3000");
        sb.append("&rankby=distance");
//        sb.append("&types=" + "subway_station");
        sb.append("&types=").append(type);
        sb.append("&sensor=true");
        sb.append("&key=AIzaSyAqpE1l1BgCartirn08RLtUffml9gR0Zpk");

        Log.d("Map", "api: " + sb.toString());

        return sb;
    }

    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParserTask
            parserTask.execute(result);
        }
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

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d(TAG, e.toString());
            SSLog.e(TAG,"downloadUrl ",e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            Place_JSON placeJson = new Place_JSON();

            try {
                jObject = new JSONObject(jsonData[0]);

                places = placeJson.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            Log.d("Map", "list size: " + list.size());
            // Clears all the existing markers;
           /* HashMap<String, String> nearestBusStop = null;
            HashMap<String, String> nearestTrainStation = null;
            HashMap<String, String> nearestMetro = null;*/
            float /*distBusStop = -1, distTrainStation = -1,*/ distMetro = -1;
            float[] dist = new float[1];
            for (int i = 0; i < list.size(); i++) {
                HashMap<String, String> hmPlace = list.get(i);
                double lat = Double.parseDouble(hmPlace.get("lat"));
                double lng = Double.parseDouble(hmPlace.get("lng"));
                Location.distanceBetween(lat, lng, mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude(), dist);
                if (hmPlace.get("type").equals("subway_station")) {
                    if (distMetro == -1 || dist[0] < distMetro) {
                        distMetro = dist[0];
                       /* nearestMetro = hmPlace;*/
                    }
                }

            }


            for (int i = 0; i < list.size(); i++) {
                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();
                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);
                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));
                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));
                bounds.include(new LatLng(lat, lng));
                // Getting name
                String name = hmPlace.get("place_name");
                Log.d("Map", "place: " + name);
                // Getting vicinity
                String vicinity = hmPlace.get("vicinity");
                LatLng latLng = new LatLng(lat, lng);
                // Setting the position for the marker
                markerOptions.position(latLng);
                markerOptions.title(name + " : " + vicinity);
                float color = Float.parseFloat(hmPlace.get("color"));
//                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(color));
                // Placing a marker on the touched position
//                Marker m = mMap.addMarker(markerOptions);
                if (i > 2) {
                    break;
                }
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));
        }
    }

    public class Place_JSON {

        /**
         * Receives a JSONObject and returns a list
         */
        public List<HashMap<String, String>> parse(JSONObject jObject) {

            JSONArray jPlaces = null;
            try {
                /** Retrieves all the elements in the 'places' array */
                jPlaces = jObject.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /** Invoking getPlaces with the array of json object
             * where each json object represent a place
             */
            return getPlaces(jPlaces);
        }

        private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
            int placesCount = jPlaces.length();
            List<HashMap<String, String>> placesList = new ArrayList<>();
            HashMap<String, String> place;

            /** Taking each place, parses and adds to list object */
            for (int i = 0; i < placesCount; i++) {
                try {
                    /** Call getPlace with place JSON object to parse the place */
                    place = getPlace((JSONObject) jPlaces.get(i));
                    placesList.add(place);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return placesList;
        }

        /**
         * Parsing the Place JSON object
         */
        private HashMap<String, String> getPlace(JSONObject jPlace) {

            HashMap<String, String> place = new HashMap<>();
            String placeName = "-NA-";
            String vicinity = "-NA-";
            String latitude ;
            String longitude ;
            String reference;
            String type ;
            float color = BitmapDescriptorFactory.HUE_MAGENTA;
            try {
                // Extracting Place name, if available
                if (!jPlace.isNull("name")) {
                    placeName = jPlace.getString("name");
                }
                JSONArray typesArray = jPlace.getJSONArray("types");
                type = typesArray.getString(0);
                if (type.equals("train_station")) {
                    color = BitmapDescriptorFactory.HUE_CYAN;
                } else if (type.equals("subway_station")) {
                    color = BitmapDescriptorFactory.HUE_CYAN;
                }
                // Extracting Place Vicinity, if available
                if (!jPlace.isNull("vicinity")) {
                    vicinity = jPlace.getString("vicinity");
                }

                latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
                reference = jPlace.getString("reference");

                place.put("place_name", placeName);
                place.put("vicinity", vicinity);
                place.put("lat", latitude);
                place.put("lng", longitude);
                place.put("reference", reference);
                place.put("type", type);
                place.put("color", String.valueOf(color));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return place;
        }
    }

    public void drawFromMarker(CAddress addr) {

        try {
            if (!addr.hasLatitude() || !addr.hasLongitude()) {
                // Toast.makeText(AbstractMapFragment_act.this.getBaseContext(),
                // "No Start point", Toast.LENGTH_SHORT).show();
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

        if (markerFrom == null) {
            markerFrom = mMap
                    .addMarker(new MarkerOptions()
                            .position(
                                    new LatLng(addr.getLatitude(), addr
                                            .getLongitude()))
                            .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .alpha(0.8f).draggable(true)
                            .title(addr.getAddress()));
        } else {
            markerFrom.setPosition(new LatLng(moFromAddress.getLatitude(),
                    moFromAddress.getLongitude()));
        }
    } // drawFromMarker

    public void drawToMarker(CAddress addr) {
        if (!addr.hasLatitude() || !addr.hasLongitude()) {
            // Toast.makeText(AbstractMapFragment_act.this.getBaseContext(),
            // "No Destination point", Toast.LENGTH_SHORT).show();
            return;
        }
        if (markerTo != null) {
            markerTo.remove();
        }
        markerTo = null;
        if (markerTo == null) {
            markerTo = mMap.addMarker(new MarkerOptions()
                    .position(
                            new LatLng(moToAddress.getLatitude(), moToAddress
                                    .getLongitude()))
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .alpha(0.8f).title(addr.getAddress()));
        } else {
            markerTo.setPosition(new LatLng(moToAddress.getLatitude(), moToAddress
                    .getLongitude()));
        }

    } // drawToMarker

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
            if (resultCode == Constants_lib_ss.SUCCESS_RESULT) {
                if (!TextUtils.isEmpty(mAddressOutput)) {
                    if (mTvFrom != null) {
                        mTvFrom.setText(mAddressOutput);
                    }
                }


            }

        }
    }

    protected void startIntentServiceCurrentAddress(Location location) {

        try {
            Intent intent = new Intent(FullMapShowTrainBus_act.this,
                    FetchAddressIntentService.class);
            intent.putExtra(Constants_lib_ss.RECEIVER, mResultReceiver);
            intent.putExtra(Constants_lib_ss.LOCATION_DATA_EXTRA, location);
            startService(intent);
        } catch (Exception e) {

            e.printStackTrace();
        }
    } // startIntentServiceCurrentAddress
}
