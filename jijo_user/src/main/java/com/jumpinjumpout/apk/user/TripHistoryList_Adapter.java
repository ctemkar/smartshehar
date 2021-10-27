/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jumpinjumpout.apk.user;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.maps.MapView;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CTrip;

import java.util.ArrayList;
import java.util.HashSet;

public class TripHistoryList_Adapter extends RecyclerView.Adapter<TripHistoryList_ViewHolder> {
    protected HashSet<MapView> mMapViews = new HashSet<>();
    public ArrayList<CTrip> tripArrayList;
    boolean isImage = false;
    CTrip mapCtrip;

    public void setMapLocations(ArrayList<CTrip> mapLocations) {
        tripArrayList = mapLocations;
    }

    @Override
    public TripHistoryList_ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        final View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.triphistorymap_item, viewGroup, false);
        TripHistoryList_ViewHolder viewHolder = new TripHistoryList_ViewHolder(viewGroup.getContext(), view);

        mMapViews.add(viewHolder.mapView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final TripHistoryList_ViewHolder viewHolder, int position) {
        mapCtrip = tripArrayList.get(position);

        viewHolder.itemView.setTag(mapCtrip);

        viewHolder.title.setText(mapCtrip.getMsFullName());
        viewHolder.tvTripDateTime.setText(mapCtrip.getMsTripActionTime1());
        viewHolder.tvDriverCarModel.setText(mapCtrip.getVehicleCompany() + " " + mapCtrip.getVehicleModel() + "\n" + mapCtrip.getVehicleNo());
        viewHolder.tvTripFareRs.setText("Fare:  Rs " + mapCtrip.getTripFare());

        if (!TextUtils.isEmpty(mapCtrip.getUserProfileImageFileName()) &&
                !TextUtils.isEmpty(mapCtrip.getUserProfileImagePath())) {
            String url = Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + mapCtrip.getUserProfileImagePath() +
                    mapCtrip.getUserProfileImageFileName();
            ImageRequest request = new ImageRequest(url,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            if (Build.VERSION.SDK_INT < 11) {
                            } else {
                                viewHolder.ivDriverImageShow.setImageBitmap(bitmap);
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
            Bitmap bitmap = mapCtrip.getContactThnumbnail();
            if (bitmap != null) {
                viewHolder.ivDriverImageShow.setImageBitmap(bitmap);
            }
        }

        viewHolder.setMapLocation(mapCtrip);
    }

    @Override
    public int getItemCount() {
        return tripArrayList == null ? 0 : tripArrayList.size();
    }

    public HashSet<MapView> getMapViews() {
        return mMapViews;
    }
}
