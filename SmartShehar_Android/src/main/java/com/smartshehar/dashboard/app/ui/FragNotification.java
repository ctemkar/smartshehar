package com.smartshehar.dashboard.app.ui;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.dashboard.app.CNotification;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.NotificationAdapter;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;


public class FragNotification extends
        android.support.v4.app.Fragment {

    private static final String TAG = "FragNotification";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    Connectivity mConnectivity;
    //    private ProgressDialog pd;
    public static ArrayList<CNotification> mCNotificationArrayList = new ArrayList<>();

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RecyclerView mRecyclerView;
    protected NotificationAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnectivity = new Connectivity();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.frag_notification, container, false);
        rootView.setTag(TAG);


        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);
        if (mCNotificationArrayList.size() > 0)
            mCNotificationArrayList.clear();
        getNotification();
        return rootView;
    }

    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void getNotification() {
        try {
            if (mCNotificationArrayList.size() > 0)
                mCNotificationArrayList.clear();
            String sNotificationValue = CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(getActivity().getApplicationContext()).getString(Constants_dp.PREF_NOTIFICATION_LIST_SAVED, "");

            ArrayList<String> arraySNotification;

            if (!TextUtils.isEmpty(sNotificationValue)) {

                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                JSONObject jObj;
                arraySNotification = new Gson().fromJson(sNotificationValue, type);
                CNotification mCNotification;
                String mIssueType, mIssueTime, mMessage = "",/* mNotificationSubType,*/ mIssueAddress, mUniquekey,
                        mIssueSubType ,mIssueId="";
                for (int i = 0; i < arraySNotification.size() && i < Constants_dp.MAX_NOTIFICATIONS; i++) {


                    jObj = new JSONObject(arraySNotification.get(i));

                    if(mIssueId.equalsIgnoreCase(jObj.isNull("issueid") ? "" : jObj.getString("issueid")) &&
                            mMessage.equalsIgnoreCase(jObj.isNull("message") ? "" : jObj.getString("message"))  )
                    {
                        continue;
                    }

                    mIssueType = jObj.isNull("issue_category") ? "" : jObj.getString("issue_category");
                    mIssueSubType = jObj.isNull("issue_subcategory") ? "" : jObj.getString("issue_subcategory");
                    mIssueTime = jObj.isNull("issue_time") ? "" : jObj.getString("issue_time");
                    mMessage = jObj.isNull("message") ? "" : jObj.getString("message");
                    mIssueAddress = jObj.isNull("issue_address") ? "" : jObj.getString("issue_address");
                    mUniquekey = jObj.isNull("uniquekey") ? "" : jObj.getString("uniquekey");
                    mIssueId = jObj.isNull("issueid") ? "" : jObj.getString("issueid");
                    mCNotification = new CNotification(mIssueType, mIssueSubType, mIssueTime, mMessage,

                            mIssueAddress, Long.parseLong(mUniquekey),Integer.valueOf(mIssueId));


                    mCNotificationArrayList.add(mCNotification);


                }
                if (arraySNotification.size() > 0) {
                    mAdapter = new NotificationAdapter(getActivity(), mCNotificationArrayList);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG, "getNotification ", e);
        }
    }

}