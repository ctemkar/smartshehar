package com.jumpinjumpout.apk.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.LongDistanceTrips_Adapter;

import org.json.JSONArray;


/**
 * Created by user pc on 23-04-2015.
 */
public class LongDistanceTripList_frag extends TripsList_frag {

    protected static final String TAG = "LongDistanceTripList_frag: ";
    JSONArray majActiveUsers = null;
    LongDistanceTrips_Adapter adapter;
    TextView mTvStatus;
    Button mBtnShare;
    RelativeLayout mRlFromTo;
    TextView mTvFrom, mTvTo;
    private boolean isGettingCommercialUsers;
    String msResponse = "";
    ListView mListView;
    private String sTextDestination = "Cound not find matching trips for your from and to address\n" +
            "Touch here to see trips starting near you";


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.active_users_list_fragment, container,
                false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mTvStatus = (TextView) view.findViewById(R.id.tvStatus);
        mBtnShare = (Button) view.findViewById(R.id.btnShare);
        mRlFromTo = (RelativeLayout) view.findViewById(R.id.rlFromTo);
        mTvFrom = (TextView) view.findViewById(R.id.ac_from);
        mTvTo = (TextView) view.findViewById(R.id.ac_to);
        mTvStatus.setVisibility(View.VISIBLE);
        mBtnShare.setVisibility(View.GONE);
        mTvStatus.setText("Finding Jump.ins for you");
        mListView = getListView();

        mBtnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireTrackerEvent(getString(R.string.atShare));
                String message = getString(R.string.androidAppLink);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(share, "Share Jump in Jump out"));
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    public void fireTrackerEvent(String label) {
    }

    @Override
    public void onResume() {
        CGlobals_user.getInstance().init(getActivity().getApplicationContext());
        init();
        super.onResume();
    }

    void init() {
        try {

            adapter = new LongDistanceTrips_Adapter(getActivity(), LongDistanceCar_act.userTrips,
                    LongDistanceCar_act.ac_from.getText().toString());

            setListAdapter(adapter);
            msActiveMessage = getActivity().getString(R.string.activeUsers);
            msNoActiveMessage = getActivity().getString(R.string.findjumpin);
            try {
                if (LongDistanceCar_act.userTrips.size() > 0) {
                    mTvStatus.setText("");
                    mTvStatus.setVisibility(View.GONE);
                    mBtnShare.setVisibility(View.GONE);
                } else {
                    setNoUserMessage(CGlobals_user.getInstance().getDisplayTripType());
                    // Change Text message 'No cabs available'
                    mTvStatus.setText(Html.fromHtml(msNoActiveMessage));
                    mTvStatus.setVisibility(View.VISIBLE);
                    mBtnShare.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                SSLog.e(TAG, "onResume", e);
            }
        } catch (Exception e) {

            SSLog.e(TAG, "init", e);
        }
    }


}
