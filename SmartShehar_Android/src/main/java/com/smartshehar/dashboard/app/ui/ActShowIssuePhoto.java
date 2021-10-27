package com.smartshehar.dashboard.app.ui;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSApp;

import lib.app.util.CGlobals_lib_ss;


public class ActShowIssuePhoto extends DialogFragment {
    ImageView ivIssuePhoto;
    public static String mImageName, mImagePath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vw_issue_photo, null, false);
        ivIssuePhoto = (ImageView) view.findViewById(R.id.ivIssuePhoto);


        String url = CGlobals_lib_ss.getPath() + mImagePath + mImageName;
        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {

                        ivIssuePhoto.setImageBitmap(bitmap);

                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        SSApp.getInstance().addToRequestQueue(request);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCancelable(true);
        getDialog().setCanceledOnTouchOutside(true);

        return view;
    }

}








