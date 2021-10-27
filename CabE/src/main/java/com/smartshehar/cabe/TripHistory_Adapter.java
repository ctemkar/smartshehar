package com.smartshehar.cabe;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.net.URLEncoder;
import java.util.ArrayList;

import lib.app.util.SSLog_SS;
import lib.app.util.VolleySingleton;

/**
 * Created by jijo_soumen on 25/04/2016.
 * Trip History Adapter
 */
public class TripHistory_Adapter extends RecyclerView.Adapter<TripHistory_Adapter.ViewHolder> {
    static String TAG = "TripHistory_Adapter: ";
    public ArrayList<CTripCabE> tripArrayList;
    CTripCabE mapCtrip;
    Context mContext;
    String STATIC_MAP_API_ENDPOINT;
    ImageLoader mImageLoader;
    String marker_me, marker_dest;

    public TripHistory_Adapter(Context context, ArrayList<CTripCabE> mapLocations) {
        this.mContext = context;
        this.tripArrayList = mapLocations;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        //public TextView title;
        public TextView tvTripDateTime, tvDriverCarModel, tvTripFareRs, tvTripDistance;
        NetworkImageView ivDriverMap;
        NetworkImageView ivDriverImageShow;


        public ViewHolder(View view) {
            super(view);
            //title = (TextView) view.findViewById(R.id.tvTitle);
            tvTripDateTime = (TextView) view.findViewById(R.id.tvTripDateTime);
            tvDriverCarModel = (TextView) view.findViewById(R.id.tvDriverCarModel);
            tvTripFareRs = (TextView) view.findViewById(R.id.tvTripFareRs);
            tvTripDistance = (TextView) view.findViewById(R.id.tvTripDistance);
            ivDriverImageShow = (NetworkImageView) view.findViewById(R.id.ivDriverImageShow);
            ivDriverMap = (NetworkImageView) view.findViewById(R.id.ivDriverMap);

        }

        public NetworkImageView getNetworkImageView() {
            return ivDriverMap;
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.triphistorymap_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        mapCtrip = tripArrayList.get(position);

        viewHolder.itemView.setTag(mapCtrip);

        //viewHolder.title.setText(mapCtrip.getDriverNmae());
        viewHolder.tvTripDateTime.setText(mapCtrip.getBooking_Time());
        viewHolder.tvDriverCarModel.setText(mapCtrip.getVehicle() + "\n" + mapCtrip.getVehicleNo());
        viewHolder.tvTripFareRs.setText("Fare: " + mContext.getResources().getString(R.string.rs) + mapCtrip.getTrip_Cost());
        viewHolder.tvTripDistance.setText("Distance: " + mapCtrip.getTrip_Distance() + " KM");
        mImageLoader = CustomVolleyRequestQueue.getInstance(mContext)
                .getImageLoader();
        if (!TextUtils.isEmpty(mapCtrip.getImage_Name()) &&
                !TextUtils.isEmpty(mapCtrip.getImage_Path())) {
            String url = Constants_CabE.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + mapCtrip.getImage_Path() +
                    mapCtrip.getImage_Name();
            mImageLoader.get(url, ImageLoader.getImageListener(viewHolder.ivDriverImageShow,
                    R.drawable.ic_driver, R.drawable.ic_driver));
            viewHolder.ivDriverImageShow.setImageUrl(url, mImageLoader);
        } else {
            String url = "https://www.google.co.in";
            mImageLoader.get(url, ImageLoader.getImageListener(viewHolder.ivDriverImageShow,
                    R.drawable.ic_driver, R.drawable.ic_driver));
            viewHolder.ivDriverImageShow.setImageUrl(url, mImageLoader);
        }
        try {
            marker_me = "color:green|label:1|" + mapCtrip.getFromLat() + "," + mapCtrip.getFromLng();
            marker_dest = "color:red|label:2|" + mapCtrip.getToLat() + "," + mapCtrip.getToLng();
            marker_me = URLEncoder.encode(marker_me, "UTF-8");
            marker_dest = URLEncoder.encode(marker_dest, "UTF-8");
            STATIC_MAP_API_ENDPOINT = "http://maps.googleapis.com/maps/api/staticmap?size=500x200&format=png&maptype=roadmap" + "&markers="
                    + marker_me + "&markers=" + marker_dest;
            viewHolder.getNetworkImageView().setImageUrl(STATIC_MAP_API_ENDPOINT, VolleySingleton.getInstance(mContext).getImageLoader());
        } catch (Exception e) {
            SSLog_SS.e(TAG, "updateMapContents: ", e, mContext);
        }
    }

    @Override
    public int getItemCount() {
        return tripArrayList == null ? 0 : tripArrayList.size();
    }
}