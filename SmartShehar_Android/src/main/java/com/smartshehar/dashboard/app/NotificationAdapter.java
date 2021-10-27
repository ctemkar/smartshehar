package com.smartshehar.dashboard.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartshehar.dashboard.app.ui.ActFollowupForm;

import java.util.ArrayList;


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private static final String TAG = "NotificationAdapter";
    ArrayList<CNotification> mIssueList;
    Context context;

    public NotificationAdapter(Context context, ArrayList<CNotification> mIssueList) {
        this.context = context;
        this.mIssueList = mIssueList;

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtIssueType, txtIssueTime, txtIssueAddress, txtNotificationType,
                txtIssueSubType;
        LinearLayout llDisplay;

        public ViewHolder(View v) {
            super(v);
            txtIssueType = (TextView) v.findViewById(R.id.txtIssueType);
            txtIssueSubType = (TextView) v.findViewById(R.id.txtIssueSubType);
            txtIssueTime = (TextView) v.findViewById(R.id.txtIssueTime);
            txtIssueAddress = (TextView) v.findViewById(R.id.txtIssueAddress);
            txtNotificationType = (TextView) v.findViewById(R.id.txtNotificationType);
            llDisplay = (LinearLayout) v.findViewById(R.id.llDisplay);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notification, viewGroup, false);
        Log.d(TAG, "item_layout inflated");

        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        if (mIssueList.size() > 0) {
            CNotification cNotification = mIssueList.get(position);
            viewHolder.txtIssueAddress.setText("Location: " + cNotification.getmAddress());
            viewHolder.txtIssueType.setText("Issue is " + cNotification.getmSubType());
            viewHolder.txtIssueTime.setText("Time: " + cNotification.getmTime());
            viewHolder.txtNotificationType.setText(cNotification.getmMessage());
            viewHolder.txtIssueSubType.setText("Sub-Category: " + cNotification.getmSubType());

        }
        viewHolder.llDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(context, ActFollowupForm.class);
                    intent.putExtra(ActFollowupForm.ARG_ISSUE_ID, mIssueList.get(position).getmIssueId());
                    context.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mIssueList.size();
    }


}

