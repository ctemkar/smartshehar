package com.jumpinjumpout.apk.user;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.ui.BookingSeat_act;

import java.lang.reflect.Type;
import java.util.ArrayList;


/**
 * ****** Adapter class extends with BaseAdapter and implements with OnClickListener ***********
 */
public class LongDistanceTrips_Adapter extends BaseAdapter implements OnClickListener {
    private static String TAG = "TripsAdapter";
    boolean isImage = false;
    private ArrayList<CNotification> maCNotification;
    public Resources res;
    CTrip trip = null;
    Context context;
    String msFromAddress;
    int i = 0;
    ArrayList<CTrip> data;
    String msTicketDestinationAddress = "";
    boolean hasDestination = false;
    private boolean bIsFavourite = false;

    public LongDistanceTrips_Adapter(Context c, ArrayList<CTrip> d, String address) {
        this.context = c;
        this.msFromAddress = address;
        data = d;
        populateListFromNotifications();
    }

    public int getCount() {

        if (data.size() <= 0)
            return 1;
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {

        public ImageView image;
        TextView mTvDescription, mTvNotificationMessage;
        public TextView mTvTripStatus, tvSeatamount, tvTotalSeat;
        public ImageView mTvClickTrack;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            vi = mInflater.inflate(R.layout.longdistance_item, null);
            holder = new ViewHolder();
            holder.mTvDescription = (TextView) vi.findViewById(R.id.tvDescription);
            holder.mTvTripStatus = (TextView) vi.findViewById(R.id.tvTripStatus);
            holder.mTvClickTrack = (ImageView) vi.findViewById(R.id.tvClickTrack);
            holder.mTvNotificationMessage = (TextView) vi.findViewById(R.id.tvNotificationMessage);
            holder.tvTotalSeat = (TextView) vi.findViewById(R.id.tvTotalSeat);
            holder.tvSeatamount = (TextView) vi.findViewById(R.id.tvSeatamount);
            vi.setTag(holder);
        } else
            holder = (ViewHolder) vi.getTag();

        if (data.size() <= 0) {

        } else {
            trip = null;
            trip = (CTrip) data.get(position);
            trip.setMyAddress(msFromAddress);

            String json = new Gson().toJson(trip);
            MyApplication.getInstance().getPersistentPreferenceEditor().putString(Constants_user.PREF_LONG_DISTANCE_CTRIP,json);
            MyApplication.getInstance().getPersistentPreferenceEditor().commit();

            String enterSeat = MyApplication.getInstance().getPersistentPreference().
                    getString(Constants_user.PREF_ENTER_SEAT_BOOK, "");
            if (!TextUtils.isEmpty(enterSeat)) {
                int enterSeat1 = Integer.parseInt(enterSeat);
                if (enterSeat1 > trip.getSeating()) {
                    Toast.makeText(context, "trip not ava...", Toast.LENGTH_LONG).show();
                } else {
                    MyApplication.getInstance().getPersistentPreferenceEditor().putInt("COMMERCIAL_VEHICLE_ID", trip.getCommercial_Vehicle_Id());
                    MyApplication.getInstance().getPersistentPreferenceEditor().commit();

                    CNotification cNotification = getNotification(trip.getTripId());
                    if (cNotification != null) {
                        String sNotification = cNotification.getNotificationTime() + " " + cNotification.getNotificationMessage();
                        holder.mTvNotificationMessage.setText(Html.fromHtml(sNotification));
                        holder.mTvNotificationMessage.setVisibility(View.VISIBLE);
                    } else {
                        holder.mTvNotificationMessage.setVisibility(View.GONE);
                    }

                    if (trip.getSeating() > 0) {
                        holder.tvTotalSeat.setText("Seating: " + String.valueOf(trip.getSeating()));
                        holder.tvTotalSeat.setVisibility(View.VISIBLE);
                    } else {
                        holder.tvTotalSeat.setVisibility(View.GONE);
                    }


                    if (!TextUtils.isEmpty(trip.getUserProfileImageFileName()) && !TextUtils.isEmpty(trip.getUserProfileImagePath())) {
                        String url = Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + trip.getUserProfileImagePath() +
                                trip.getUserProfileImageFileName();
                        ImageRequest request = new ImageRequest(url,
                                new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap bitmap) {
                                        if (Build.VERSION.SDK_INT < 11) {
                                            Toast.makeText(context, context.getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                        } else {
                                            holder.mTvClickTrack.setImageBitmap(bitmap);
                                            isImage = true;
                                        }
                                    }
                                }, 0, 0, null,
                                new Response.ErrorListener() {
                                    public void onErrorResponse(VolleyError error) {
                                        isImage = false;
                                    }
                                });
                        MyApplication.getInstance().addToRequestQueue(request);
                    }
                    if (!isImage) {
                        Bitmap bitmap = trip.getContactThnumbnail();
                        if (bitmap != null) {
                            holder.mTvClickTrack.setImageBitmap(bitmap);
                        }
                    }

                    if (trip.getHasDriveMoved()) {
                        holder.mTvDescription.setText(Html.fromHtml("Cab " + trip.getVehicleNo() + " left for " + " <br>" +
                                        trip.getDescription() + " at " + trip.getPlannedStartTime() +
                                        (trip.getMiPassed() > 0 ? " (Passed)" : "")),
                                TextView.BufferType.SPANNABLE);
                    } else {
                        holder.mTvDescription.setText(Html.fromHtml("Cab " + trip.getVehicleNo() + " will be leaving for " + " <br>" +
                                        trip.getDescription() + " at " + trip.getPlannedStartTime() +
                                        (trip.getMiPassed() > 0 ? " (Passed)" : "")),
                                TextView.BufferType.SPANNABLE);
                    }
                    holder.mTvDescription.setVisibility(View.VISIBLE);

                    if (CGlobals_user.getInstance().getMyLocation(context) != null) {
                        LatLng latlng = new LatLng(CGlobals_user.getInstance().getMyLocation(context).getLatitude(),
                                CGlobals_user.getInstance().getMyLocation(context).getLongitude());
                        String distanceFromMe = "";
                        if (trip.getLatLng() != null && latlng != null) {
                            float results[] = new float[1];
                            Location.distanceBetween(trip.getLatLng().latitude,
                                    trip.getLatLng().longitude, latlng.latitude,
                                    latlng.longitude, results);
                            try {
                                if (results[0] > 20) {
                                    distanceFromMe = String.format("%.2f", results[0] / 1000) + " km.";
                                } else {
                                    distanceFromMe = "Near";
                                }
                            } catch (Exception e) {
                                SSLog.e(TAG, "getView", e);
                            }
                        }
                        if (!TextUtils.isEmpty(distanceFromMe.trim())) {
                            holder.mTvTripStatus.setText(distanceFromMe);
                            holder.mTvTripStatus.setVisibility(View.VISIBLE);
                        } else {
                            holder.mTvTripStatus.setVisibility(View.GONE);
                        }
                    }

                    holder.mTvClickTrack.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            trip = null;
                            trip = (CTrip) data.get(position);
                            final Dialog dialog = new Dialog(context, R.style.DIALOG);
                            dialog.setContentView(R.layout.active_user_more_information);
                            dialog.setTitle("More Information ...");

                            TextView misLastActive, mimsFrom, miTvDescription, mimsPhoneno, miTvDriverDistance, miTvStartLandmark,
                                    miTvTripNote, mimsName, miVehicleType;

                            misLastActive = (TextView) dialog.findViewById(R.id.tvsLastActive);
                            miVehicleType = (TextView) dialog.findViewById(R.id.tvVehicleType);
                            miTvDescription = (TextView) dialog.findViewById(R.id.tvTvDescription);
                            mimsFrom = (TextView) dialog.findViewById(R.id.tvmsFrom);
                            mimsName = (TextView) dialog.findViewById(R.id.tvmsName);
                            mimsPhoneno = (TextView) dialog.findViewById(R.id.tvmsPhoneno);
                            miTvDriverDistance = (TextView) dialog.findViewById(R.id.tvDriverDistance);
                            miTvStartLandmark = (TextView) dialog.findViewById(R.id.tvStartLandmark);
                            miTvTripNote = (TextView) dialog.findViewById(R.id.tvTripNote);

                            misLastActive.setText(Html.fromHtml("<font color=\"red\"><strong>Trip Information</strong></font>"));
                            misLastActive.setVisibility(View.VISIBLE);

                            if (!TextUtils.isEmpty(trip.getFrom().trim())) {
                                mimsFrom.setText("Start address: " + trip.getFrom());
                                mimsFrom.setVisibility(View.VISIBLE);
                            } else {
                                mimsFrom.setVisibility(View.GONE);
                            }
                            if (!TextUtils.isEmpty(trip.getTo().trim())) {
                                miTvDescription.setText(Html.fromHtml("Destination: " + trip.getTo()));
                                miTvDescription.setVisibility(View.VISIBLE);
                            } else {
                                miTvDescription.setVisibility(View.GONE);
                            }

                            if (CGlobals_user.getInstance().getMyLocation(context) != null) {
                                LatLng latlng = new LatLng(CGlobals_user.getInstance().getMyLocation(context).getLatitude(),
                                        CGlobals_user.getInstance().getMyLocation(context).getLongitude());
                                String distanceFromMe = "";
                                if (trip.getLatLng() != null && latlng != null) {
                                    float results[] = new float[1];
                                    Location.distanceBetween(trip.getLatLng().latitude,
                                            trip.getLatLng().longitude, latlng.latitude,
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
                                    miTvDriverDistance.setText(distanceFromMe);
                                    miTvDriverDistance.setVisibility(View.VISIBLE);
                                } else {
                                    miTvDriverDistance.setVisibility(View.GONE);
                                }
                            }
                            if (!TextUtils.isEmpty(trip.getStartLandmark().trim())) {
                                miTvStartLandmark.setText(trip.getStartLandmark());
                                miTvStartLandmark.setVisibility(View.VISIBLE);
                            } else {
                                miTvStartLandmark.setVisibility(View.GONE);
                            }
                            if (!TextUtils.isEmpty(trip.getTripNotes().trim())) {
                                miTvTripNote.setText(trip.getTripNotes());
                                miTvTripNote.setVisibility(View.VISIBLE);
                            } else {
                                miTvTripNote.setVisibility(View.GONE);
                            }
                            mimsName.setText(Html.fromHtml("<font color=\"red\"><strong>Vehicle Information</strong></font>"));
                            mimsName.setVisibility(View.VISIBLE);
                            if (!TextUtils.isEmpty(trip.getVehicleType().trim())) {
                                miVehicleType.setText("Vehicle: " + trip.getVehicleColor() + " " + trip.getVehicleCompany() + " " +
                                        trip.getVehicleNo() + " " + trip.getVehicleType());
                                miVehicleType.setVisibility(View.VISIBLE);
                            } else {
                                miVehicleType.setVisibility(View.GONE);
                            }
                            if (!TextUtils.isEmpty(trip.getPhoneNo().trim())) {
                                mimsPhoneno.setText("Driver Phone No: " + trip.getPhoneNo());
                                mimsPhoneno.setVisibility(View.VISIBLE);
                            } else {
                                mimsPhoneno.setVisibility(View.GONE);
                            }
                            dialog.show();

                        }
                    });

                    holder.mTvDescription.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                msTicketDestinationAddress = MyApplication.getInstance().getPersistentPreference()
                                        .getString("PASSENGER_DESTINATION_ADDRESS", "");
                                if (!TextUtils.isEmpty(msTicketDestinationAddress)) {
                                    hasDestination = true;
                                } else {
                                    hasDestination = false;
                                }
                                trip = null;
                                trip = (CTrip) data.get(position);
                                MyApplication.getInstance().getPersistentPreferenceEditor()
                                        .putInt(Constants_user.PREF_TRIP_ID, trip.getTripId());

                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putInt(Constants_user.PREF_TRIP_PASSOUT, trip.getMiPassed());

                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putBoolean(Constants_user.HAS_DESTINATION, hasDestination);

                                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                                Intent i = new Intent(context, BookingSeat_act.class);
                                context.startActivity(i);
                                SSLog.d(TAG, trip.msHtmlDescription);
                            } catch (Exception e) {
                                SSLog.d(TAG,
                                        " OnItemClick: probably no items in list " + e.getMessage());
                            }
                        }
                    });
                }
            }
        }
        return vi;
    }

    private void sendFavouritetoServer(boolean isFavourite) {

    }

    @Override
    public void onClick(View v) {
        Log.v("CustomAdapter", "=====Row button clicked=====");
    }

    void populateListFromNotifications() {
        try {
            String sNotificationValue = MyApplication.getInstance()
                    .getPersistentPreference()
                    .getString(Constants_user.PREF_NOTIFICATION_LIST_SAVED, "");
            ArrayList<String> arraySNotification = new ArrayList<String>();
            maCNotification = new ArrayList<CNotification>();
            if (TextUtils.isEmpty(sNotificationValue)) {

            } else {

                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                arraySNotification = new Gson().fromJson(sNotificationValue, type);
                for (int i = 0; i < arraySNotification.size() && i < Constants_user.MAX_NOTIFICATIONS; i++) {

                    maCNotification.add(new CNotification(arraySNotification.get(i).toString(), context));

                }
            }

        } catch (Exception e) {

            SSLog.e(TAG, "init", e);
        }


    }

    private CNotification getNotification(int tripId) {

        for (CNotification cn : maCNotification) {
            if (cn.getTripId() == tripId) {
                return cn;
            }
        }
        return null;
    }
}