package com.smartshehar.dashboard.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartshehar.dashboard.app.ui.ActShowIssuePhoto;

import java.util.ArrayList;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.VolleySingleton;


public class IpCommentAdapter extends RecyclerView.Adapter<IpCommentAdapter.ViewHolder> {
    private static final String TAG = "IpCommentAdapter";

    Context ctx;
    ArrayList<CIpComments> mList;
    android.app.FragmentManager manager;

    public IpCommentAdapter(Context context, ArrayList<CIpComments> mList,android.app.FragmentManager manager) {
        this.ctx = context;
        this.mList = mList;
        this.manager = manager;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIpComment, tvIpDate;
        private final NetworkImageView ip_image;
        public ViewHolder(View v) {
            super(v);
            tvIpComment = (TextView) v.findViewById(R.id.tvIpComment);
            tvIpDate = (TextView) v.findViewById(R.id.tvIpDate);
            ip_image = (NetworkImageView) v.findViewById(R.id.ip_image);
        }
        public NetworkImageView getNetworkImageView() {
            return ip_image;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_ip_comments, viewGroup, false);
        Log.d(TAG, "item_layout inflated");

        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        if (mList.size() > 0) {
            viewHolder.tvIpComment.setText(mList.get(position).getComment());
            viewHolder.tvIpDate.setText(mList.get(position).getDate());
            if (!TextUtils.isEmpty(mList.get(position).getsImageName())) {
                viewHolder.getNetworkImageView().setVisibility(View.VISIBLE);
                String url = CGlobals_lib_ss.getPath() + mList.get(position).getsImagePath() + mList.get(position).getsImageName();
                viewHolder.getNetworkImageView().setImageUrl(url, VolleySingleton.getInstance(ctx).getImageLoader());
                viewHolder.getNetworkImageView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                            ActShowIssuePhoto.mImageName = mList.get(position).getsImageName();
                            ActShowIssuePhoto.mImagePath = mList.get(position).getsImagePath();
                            ActShowIssuePhoto newFragment = new ActShowIssuePhoto();
                            newFragment.show(manager, "viewImage");

                    }
                });
            }

        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


}

