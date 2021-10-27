package com.jumpinjumpout.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jumpinjumpout.Constants_dp;
import com.jumpinjumpout.CustomAdapter;
import com.jumpinjumpout.DriverInfo;
import com.jumpinjumpout.www.driverprofile.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;


public class RecyclerViewFragment extends
        Fragment {

    private static final String TAG = "RecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    //    JSONArray resultRow = null;
    private TextView txtRemainingRows,txtLastDate;
    SimpleDateFormat ft;
    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;


    protected RecyclerView mRecyclerView;
    public static CustomAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    public static ArrayList<DriverInfo> maDriverInfo = new ArrayList<>();
    public static ArrayList<DriverInfo> maoDInfo = new ArrayList<>();
    String mLastSyncDate;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "on create fragment ");
        // Initialize dataset, this data would usually come from a local content provider or
        // remote server.

    }

    public static RecyclerViewFragment newInstance() {
        RecyclerViewFragment fragment = new RecyclerViewFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.activity_recycler_view_fragment, container, false);
        rootView.setTag(TAG);
        mLastSyncDate =  CGlobals_lib_ss.getInstance()
                .getPersistentPreference(getActivity().getApplicationContext()).
                        getString(Constants_dp.PREFS_LAST_SYNC_DTAE, "");

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        txtRemainingRows = (TextView) rootView.findViewById(R.id.txtRemainingRows);
        txtLastDate = (TextView) rootView.findViewById(R.id.txtLastDate);
        Log.d(TAG, "set recycler view");
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

        try {
            if(!TextUtils.isEmpty(mLastSyncDate))
            {
                ft = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT);
                Date date = ft.parse(mLastSyncDate);
                ft = new SimpleDateFormat("dd MMM, yyyy 'at' hh:mm a");
                mLastSyncDate = ft.format(date);
                txtLastDate.setText("Last Sync "+mLastSyncDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return rootView;
    }


    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
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

    public static class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnItemClickListener mListener;

        public interface OnItemClickListener {
            void onItemClick(View view, int position);
        }

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildLayoutPosition(childView));
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //datafromserver();
        dataList();
    }



    private void dataList() {
        try {
            if (maDriverInfo == null)
                return;
            if (maDriverInfo.size() > 0)
                maDriverInfo.clear();
            maDriverInfo = DriverList.mApp.mDBHelper.getDriverProfile();
            maoDInfo = DriverList.mApp.mDBHelper.getDriverProfileList();
            if (maoDInfo.size() <= 0)
                txtRemainingRows.setText("Everything is current");
            else if (maoDInfo.size() == 1)
                txtRemainingRows.setText(maoDInfo.size() + " Driver's entry is pending");
            else
                txtRemainingRows.setText(maoDInfo.size() + " Driver's entries are pending");

            if (maDriverInfo.size() != 0) {
                mAdapter = new CustomAdapter(getActivity().getApplicationContext(), maDriverInfo);
                mRecyclerView.setAdapter(mAdapter);

            } else {
                Toast.makeText(getActivity().getApplication(), "List is empty. Please connect with the Internet.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {

            e.printStackTrace();
            Log.d(TAG, "error is " + e.toString());
        }
    }
}