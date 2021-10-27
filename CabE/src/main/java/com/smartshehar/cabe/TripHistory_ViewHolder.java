package com.smartshehar.cabe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;
import lib.app.util.VolleySingleton;

/**
 * Created by jijo_soumen on 25/04/2016.
 * Trip History ViewHolder
 */
public class TripHistory_ViewHolder extends RecyclerView.ViewHolder {

    private final static String TAG = "TripHistoryList_ViewHolder: ";
    //public TextView title;
    public TextView tvTripDateTime, tvDriverCarModel, tvTripFareRs, tvTripDistance;
    public ImageView ivDriverImageShow;
    NetworkImageView ivDriverMap;
    private Context mContext;
    CTripCabE cTrip;

    public TripHistory_ViewHolder(Context context, View view) {
        super(view);
        mContext = context;
        //title = (TextView) view.findViewById(R.id.tvTitle);
        tvTripDateTime = (TextView) view.findViewById(R.id.tvTripDateTime);
        tvDriverCarModel = (TextView) view.findViewById(R.id.tvDriverCarModel);
        tvTripFareRs = (TextView) view.findViewById(R.id.tvTripFareRs);
        tvTripDistance = (TextView) view.findViewById(R.id.tvTripDistance);
        ivDriverImageShow = (ImageView) view.findViewById(R.id.ivDriverImageShow);
        ivDriverMap = (NetworkImageView) view.findViewById(R.id.ivDriverMap);
    }

    public void setMapLocation(CTripCabE mapLocation) {
        updateMapContents(mapLocation);
    }

    String STATIC_MAP_API_ENDPOINT;

    protected void updateMapContents(CTripCabE cTripHistory) {
        try {
            String marker_me = "color:green|label:1|" + cTripHistory.getFromLat() + "," + cTripHistory.getFromLng();
            String marker_dest = "color:red|label:2|" + cTripHistory.getToLat() + "," + cTripHistory.getToLng();
            marker_me = URLEncoder.encode(marker_me, "UTF-8");
            marker_dest = URLEncoder.encode(marker_dest, "UTF-8");
            STATIC_MAP_API_ENDPOINT = "http://maps.googleapis.com/maps/api/staticmap?size=500x200&format=png&maptype=roadmap" + "&markers="
                    + marker_me + "&markers=" + marker_dest;
            Log.d("STATICMAPS", STATIC_MAP_API_ENDPOINT);
            getNetworkImageView().setImageUrl(STATIC_MAP_API_ENDPOINT, VolleySingleton.getInstance(mContext).getImageLoader());
        } catch (Exception e) {
            SSLog_SS.e(TAG, "updateMapContents: ", e, mContext);
        }
    }

    /*public void tripPathHistory(final CTripCabE cTripHistory, final Context context) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_CabE.TRIP_PATH_HISTORY_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getMyTripPathHistory(response, cTripHistory, context);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    Toast.makeText(
                            context,
                            "Can not load your trip history. Please check your internet connection and retry.",
                            Toast.LENGTH_LONG).show();

                    SSLog_SS.e(TAG, "getTripHistory:-   ", error, context);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getTripHistory:-  ", e, context);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<>();
                params.put("tripid", String.valueOf(cTripHistory.getTripId()));
                params = CGlobals_lib_ss.getInstance().getMinMobileParamsShort(params,
                        Constants_CabE.TRIP_PATH_HISTORY_URL, context);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_CabE.TRIP_PATH_HISTORY_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getTripHistory", e, context);
                }
                return CGlobals_CabE.getInstance().checkParams(params);
            }
        };
        MyApplication_CabE.getInstance().addVolleyRequest(postRequest, false);
    }

    private void getMyTripPathHistory(String response, CTripCabE cTripHistory, Context context) {
        STATIC_MAP_API_ENDPOINT = "";
        if (TextUtils.isEmpty(response) || response.equals("-1")) {
            return;
        }
        String path = "";
        try {
            JSONArray aJson = new JSONArray(response);
            for (int i = 0; i < aJson.length(); i++) {
                cTrip = new CTripCabE(aJson.getJSONObject(i)
                        .toString(), context);
                path = path + "|" + cTrip.getTripHistoryPathLat() + "," + cTrip.getTripHistoryPathLng();
            }
            String marker_me = "color:green|label:1|" + cTripHistory.getFromLat() + "," + cTripHistory.getFromLng();
            String marker_dest = "color:red|label:2|" + cTripHistory.getToLat() + "," + cTripHistory.getToLng();
            marker_me = URLEncoder.encode(marker_me, "UTF-8");
            marker_dest = URLEncoder.encode(marker_dest, "UTF-8");
            STATIC_MAP_API_ENDPOINT = "http://maps.googleapis.com/maps/api/staticmap?size=500x200&format=png&maptype=roadmap" + "&markers="
                    + marker_me + "&markers=" + marker_dest + "&path=color:0x0000ff|weight:4" + path;
            Log.d("STATICMAPS", STATIC_MAP_API_ENDPOINT);
            getNetworkImageView().setImageUrl(STATIC_MAP_API_ENDPOINT, VolleySingleton.getInstance(context).getImageLoader());
        } catch (Exception e) {
            SSLog_SS.e(TAG, "updateMapContents: ", e, mContext);
        }
    }*/

    public NetworkImageView getNetworkImageView() {
        return ivDriverMap;
    }
}