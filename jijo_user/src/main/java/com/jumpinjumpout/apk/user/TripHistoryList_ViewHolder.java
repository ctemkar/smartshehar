package com.jumpinjumpout.apk.user;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.SSLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TripHistoryList_ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

    private final static String TAG = "TripHistoryList_ViewHolder: ";
    public TextView title;
    public TextView tvTripDateTime, tvDriverCarModel, tvTripFareRs;
    ImageView ivDriverImageShow;
    protected GoogleMap mGoogleMap;
    protected CTrip cTripHistory;
    public MapView mapView;
    private Context mContext;
    List<LatLng> polyz;
    ProgressDialog pDialog;
    protected LatLngBounds.Builder mZoomFitBounds;

    public TripHistoryList_ViewHolder(Context context, View view) {
        super(view);
        mContext = context;
        title = (TextView) view.findViewById(R.id.title);
        tvTripDateTime = (TextView) view.findViewById(R.id.tvTripDateTime);
        tvDriverCarModel = (TextView) view.findViewById(R.id.tvDriverCarModel);
        tvTripFareRs = (TextView) view.findViewById(R.id.tvTripFareRs);
        ivDriverImageShow = (ImageView) view.findViewById(R.id.ivDriverImageShow);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(null);
        mapView.getMapAsync(this);
    }

    public void setMapLocation(CTrip mapLocation) {
        cTripHistory = mapLocation;
        if (mGoogleMap != null) {
            updateMapContents();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        MapsInitializer.initialize(mContext);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        if (cTripHistory != null) {
            updateMapContents();
        }
    }

    protected void updateMapContents() {

        mGoogleMap.clear();
        mGoogleMap
                .addMarker(new MarkerOptions()
                        .position(
                                new LatLng(cTripHistory.getFromLat(), cTripHistory
                                        .getFromLng()))
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .alpha(0.8f).draggable(true)
                        .title(cTripHistory.getFrom()));
        mGoogleMap
                .addMarker(new MarkerOptions()
                        .position(
                                new LatLng(cTripHistory.getToLat(), cTripHistory
                                        .getToLng()))
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .alpha(0.8f).draggable(true)
                        .title(cTripHistory.getTo()));

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(cTripHistory.getFromLat(), cTripHistory.getFromLng()),
                17));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
        new GetDirection().execute();
    }


    class GetDirection extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage("Loading route. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {

            String stringUrl = "http://maps.googleapis.com/maps/api/directions/json?origin=" + cTripHistory.getFromLat() + "," + cTripHistory.getFromLng() + "&destination=" +
                    cTripHistory.getToLat() + "," + cTripHistory.getToLng() + "&sensor=false";
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(stringUrl);
                HttpURLConnection httpconn = (HttpURLConnection) url
                        .openConnection();
                if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader input = new BufferedReader(
                            new InputStreamReader(httpconn.getInputStream()),
                            8192);
                    String strLine = null;

                    while ((strLine = input.readLine()) != null) {
                        response.append(strLine);
                    }
                    input.close();
                }
                String jsonOutput = response.toString();
                JSONObject jsonObject = new JSONObject(jsonOutput);
                // routesArray contains ALL routes
                JSONArray routesArray = jsonObject.getJSONArray("routes");
                // Grab the first route
                JSONObject route = routesArray.getJSONObject(0);
                JSONObject poly = route.getJSONObject("overview_polyline");
                String polyline = poly.getString("points");
                polyz = decodePoly(polyline);
            } catch (Exception e) {
            }
            return null;
        }

        protected void onPostExecute(String file_url) {

            try {
                for (int i = 0; i < polyz.size() - 1; i++) {
                    LatLng src = polyz.get(i);
                    LatLng dest = polyz.get(i + 1);
                    mGoogleMap.addPolyline(new PolylineOptions()
                            .add(new LatLng(src.latitude, src.longitude),
                                    new LatLng(dest.latitude, dest.longitude))
                            .width(2).color(Color.BLACK).geodesic(true));
                }
                mZoomFitBounds = new LatLngBounds.Builder();
                for (LatLng latLng : polyz) {
                    mZoomFitBounds
                            .include(new LatLng(latLng.latitude, latLng.longitude));
                }
                if (mZoomFitBounds != null) {
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                            mZoomFitBounds.build(), 40));
                }
                pDialog.dismiss();
            } catch (Exception e) {
                SSLog.e(TAG, "GetDirection: ", e);
            }

        }
    }

    /* Method to decode polyline points */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
