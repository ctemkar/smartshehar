package com.jumpinjumpout.apk.lib;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class PassengerAdapter extends BaseAdapter {
    private static String TAG = "PassengerAdapter";
    private boolean isImage = false;
    private ArrayList<CTrip> data;
    private static LayoutInflater inflater;
    public Resources res;
    private CTrip tempValues = null;
    private Context context;
    private int i = 0;
    private float LeastDistanceFromMe = -1;
    private String sUrl;
    private boolean isJumpin;
    private String passengertripsummaryurl;
    private String sTripTime = "";
    private double sTripDistance;
    String distanceFromMe = "";
    float results[] = new float[1];
    float results1[] = new float[1];
    Dialog dDialog;

    public PassengerAdapter(Context c, ArrayList<CTrip> d, String url, String passengertripsummary, Dialog dialog) {
        this.context = c;
        data = d;
        sUrl = url;
        passengertripsummaryurl = passengertripsummary;
        dDialog = dialog;
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {

        TextView mPassengerName, mPassengerDistance;
        ImageView proImage;
        Button mIvJumpInDriver;
        LinearLayout mllJumpinout;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            inflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.passenger_item_list, null);
            holder = new ViewHolder();
            holder.mllJumpinout = (LinearLayout) convertView.findViewById(R.id.llJumpinout);
            holder.mPassengerName = (TextView) convertView.findViewById(R.id.passengerName);
            holder.mPassengerDistance = (TextView) convertView.findViewById(R.id.passengerDistance);
            holder.proImage = (ImageView) convertView.findViewById(R.id.proImage);
            holder.mIvJumpInDriver = (Button) convertView.findViewById(R.id.ivJumpInDriver);

            convertView.setTag(holder);
            holder.mIvJumpInDriver.setTag(position);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        tempValues = null;
        tempValues = (CTrip) data.get(position);
        if (tempValues.hasJumpedOut()) {
            holder.mllJumpinout.setVisibility(View.GONE);
            return convertView;
        }
        if (tempValues.hasJumpedIn()) {
            holder.mIvJumpInDriver.setText("Jump Out!");
            holder.mIvJumpInDriver.setVisibility(View.VISIBLE);
        } else {
            holder.mIvJumpInDriver.setText("Jump In!");
            holder.mIvJumpInDriver.setVisibility(View.VISIBLE);
        }


        if (!TextUtils.isEmpty(tempValues.getUserProfileImageFileName()) && !TextUtils.isEmpty(tempValues.getUserProfileImagePath())) {
            String url = sUrl + tempValues.getUserProfileImagePath() +
                    tempValues.getUserProfileImageFileName();
            if (holder.proImage.getDrawable() == null) {

            }
            holder.proImage.setImageBitmap(null);
            ImageRequest request = new ImageRequest(url,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            if (Build.VERSION.SDK_INT < 11) {
                                Toast.makeText(context, context.getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                holder.proImage.setImageBitmap(bitmap);
                            } else {
                                holder.proImage.setImageBitmap(bitmap);
                                isImage = true;
                            }
                        }
                    }, 0, 0, null,
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            isImage = false;
                        }
                    });
            CGlobals_lib.getInstance().addVolleyRequest(request, false, context);
        }

        if (!isImage) {
            holder.proImage.setImageBitmap(null);
            Bitmap bitmap = tempValues.getContactThnumbnail();
            if (bitmap != null) {
                holder.proImage.setImageBitmap(bitmap);
            }
        }
        holder.mPassengerName.setText(tempValues.getName());
        holder.mPassengerDistance.setText(CGlobals_lib.getInstance().getDistanceText(LeastDistanceFromMe));

        if (CGlobals_lib.getInstance().getMyLocation(context) != null) {
            LatLng latlng = new LatLng(CGlobals_lib.getInstance().getMyLocation(context).getLatitude(),
                    CGlobals_lib.getInstance().getMyLocation(context).getLongitude());

            if (tempValues.getLatLng() != null && latlng != null) {

                Location.distanceBetween(tempValues.getLatLng().latitude,
                        tempValues.getLatLng().longitude, latlng.latitude,
                        latlng.longitude, results);
                try {
                    if (results[0] > 20) {
                        distanceFromMe = "About " + String.format("%.2f", results[0] / 1000) + " km. from you";
                    } else {
                        distanceFromMe = "Near you";
                    }
                } catch (Exception e) {
                    SSLog.e(TAG, "getView", e);
                }
            }

            if (!TextUtils.isEmpty(distanceFromMe.trim())) {
                holder.mPassengerDistance.setText(distanceFromMe);
                holder.mPassengerDistance.setVisibility(View.VISIBLE);
            } else {
                holder.mPassengerDistance.setVisibility(View.GONE);
            }
        }
        holder.mIvJumpInDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = (Integer) v.getTag();
                tempValues = (CTrip) data.get(pos);
                holder.mIvJumpInDriver.setVisibility(View.VISIBLE);
                if (CGlobals_lib.getInstance().getMyLocation(context) != null) {
                    LatLng latlng = new LatLng(CGlobals_lib.getInstance().getMyLocation(context).getLatitude(),
                            CGlobals_lib.getInstance().getMyLocation(context).getLongitude());
                    if (tempValues.getLatLng() != null && latlng != null) {

                        Location.distanceBetween(tempValues.getLatLng().latitude,
                                tempValues.getLatLng().longitude, latlng.latitude,
                                latlng.longitude, results1);
                    }
                }

                if (!tempValues.hasJumpedIn() && !tempValues.hasJumpedOut()) {
                    if (results1[0] < Constants_lib.JUMP_IN_DISTANCE) {
                        getconfermationscreen("jumpin", holder, passengertripsummaryurl, tempValues, results1[0], sUrl, context);
                    } else {
                        getconfermation("jumpin", holder, passengertripsummaryurl, tempValues, results1[0], sUrl, context);
                    }
                } else if (tempValues.hasJumpedIn() && !tempValues.hasJumpedOut()) {
                    callJumpInJumpOutUrl("jumpout", holder, passengertripsummaryurl, tempValues, context);
                }
                dDialog.cancel();
            }
        });
        //   }
        return convertView;
    }

    private void getconfermationscreen(final String jumpinjumpout, final ViewHolder holder, String passengertripsummaryURL, final CTrip cTrip, final float result, final String sUrl, final Context ccontext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ccontext);
        builder.setCancelable(true);
        builder.setMessage("Confirm jump in passenger");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callJumpInJumpOutUrl("jumpin", holder, passengertripsummaryurl, tempValues, context);
                holder.mIvJumpInDriver.setText("Jump Out!");
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void getconfermation(final String jumpinjumpout, final ViewHolder holder, String passengertripsummaryURL, final CTrip cTrip, final float result, final String sUrl, final Context ccontext) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ccontext);
        builder.setCancelable(true);
        builder.setMessage("The passenger seems to be far away (" + CGlobals_lib.getInstance().getDistanceText(result)
                + "). You cannot jump them in");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void callJumpInJumpOutUrl(final String jumpinjumpout, final ViewHolder holder, String passengertripsummaryURL, final CTrip cTrip, final Context context) {
        final int iTripId = CGlobals_lib.getInstance().getSharedPreferences(context)
                .getInt(Constants_lib.PREF_TRIP_ID_INT, 0);
        final String openTripUrl = passengertripsummaryURL;
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                openTripUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        if (jumpinjumpout.equals("jumpout")) {
                            //calculateDistanceFare(response, holder, cTrip, context);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    SSLog.e(TAG, "callOpenTrip :-   ",
                            error.getMessage());

                } catch (Exception e) {
                    SSLog.e(TAG, "callOpenTrip -  ", e);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("tripid", String.valueOf(iTripId));
                params.put("passengerappuserid", String.valueOf(cTrip.getAppUserId()));
                if (jumpinjumpout.equals("jumpin")) {
                    params.put("jumpin", "1");
                } else if (jumpinjumpout.equals("jumpout")) {
                    params.put("jumpout", "1");
                }
                params = CGlobals_lib.getInstance().getBasicMobileParams(params,
                        openTripUrl, context);

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";

                }

                String url1 = openTripUrl;

                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog.e(TAG, "callOpenTrip", e);
                }
                return CGlobals_lib.getInstance().checkParams(params);

            }

        };
        CGlobals_lib.getInstance().addVolleyRequest(postRequest, false, context);
    }

    private void calculateDistanceFare(String response, final ViewHolder holder, CTrip cTrip, final Context context) {
        if (TextUtils.isEmpty(response)) {
            return;
        }
        if (response.equals("-1")) {
            return;
        }
        JSONObject person;
        try {
            person = new JSONObject(response);
            sTripDistance = person.isNull("trip_distance") ? 0.0 : person
                    .getDouble("trip_distance");
            sTripTime = person.isNull("trip_time") ? "" : person
                    .getString("trip_time");

            holder.mIvJumpInDriver.setVisibility(View.GONE);

            final Dialog dialog = new Dialog(context, R.style.DIALOG);
            dialog.setContentView(R.layout.trip_fare);

            final TextView tvBaseFare, tvDistance, tvTime, tvTotal,
                    tvTax, tvSubTotal, tvDiscount, tvMainTotal, tvDriverName;
            final ImageView ivDriverImage;

            tvBaseFare = (TextView) dialog.findViewById(R.id.tvBaseFare);
            tvDistance = (TextView) dialog.findViewById(R.id.tvDistance);
            tvTime = (TextView) dialog.findViewById(R.id.tvTime);
            tvTotal = (TextView) dialog.findViewById(R.id.tvTotal);
            tvTax = (TextView) dialog.findViewById(R.id.tvTax);
            tvSubTotal = (TextView) dialog.findViewById(R.id.tvSubTotal);
            tvDiscount = (TextView) dialog.findViewById(R.id.tvDiscount);
            tvMainTotal = (TextView) dialog.findViewById(R.id.tvMainTotal);
            tvDriverName = (TextView) dialog.findViewById(R.id.tvDriverName);

            ivDriverImage = (ImageView) dialog.findViewById(R.id.ivDriverImage);

            try {
                tvBaseFare.setText("20");
                tvDistance.setText(String.valueOf(sTripDistance));
                tvTime.setText(sTripTime);
                float smDistancefare;
                if (sTripDistance <= 1.50) {
                    smDistancefare = 20.00f;
                } else {
                    smDistancefare = Float.valueOf((float) sTripDistance) * Float.valueOf("14.84");
                }
                tvTotal.setText(String.format("%.2f", smDistancefare));

                float sTax = (Float.valueOf(smDistancefare) * Float.valueOf("14.00")) / Float.valueOf("100");
                tvTax.setText(String.format("%.2f", sTax));

                float sSubtotal = Float.valueOf(smDistancefare) + Float.valueOf(sTax);
                tvSubTotal.setText(String.format("%.2f", sSubtotal));

                float sdis = (Float.valueOf(sSubtotal) * Float.valueOf("10")) / Float.valueOf("100");
                tvDiscount.setText(String.format("%.2f", sdis));

                float sMainTotal = Float.valueOf(sSubtotal) - Float.valueOf(sdis);
                tvMainTotal.setText(String.format("%.2f", sMainTotal));

                tvDriverName.setText("Passenger name " + cTrip.getName());
                if (!TextUtils.isEmpty(cTrip.getUserProfileImageFileName()) && !TextUtils.isEmpty(cTrip.getUserProfileImagePath())) {
                    String url = sUrl + cTrip.getUserProfileImagePath() +
                            cTrip.getUserProfileImageFileName();
                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    if (Build.VERSION.SDK_INT < 11) {
                                        Toast.makeText(context, context.getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                        ivDriverImage.setImageBitmap(bitmap);
                                    } else {
                                        ivDriverImage.setImageBitmap(bitmap);
                                        isImage = true;
                                    }
                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                    isImage = false;
                                }
                            });
                    CGlobals_lib.getInstance().addVolleyRequest(request, false, context);
                }
                if (!isImage) {
                    Bitmap bitmap = tempValues.getContactThnumbnail();
                    if (bitmap != null) {
                        ivDriverImage.setImageBitmap(bitmap);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            dialog.show();
        } catch (Exception e) {
            SSLog.e(TAG, "calculateDistanceFare", e);
        }
    } // calculateDistanceFare

}