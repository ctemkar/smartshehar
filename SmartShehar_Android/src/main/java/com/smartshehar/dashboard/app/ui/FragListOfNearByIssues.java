package com.smartshehar.dashboard.app.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.ListOfNearByIssuesAdapter;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;
import com.smartshehar.dashboard.app.CIssue;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.CAddress;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.GeoHelper;
import lib.app.util.SearchAddress_act;


public class FragListOfNearByIssues extends
        android.support.v4.app.Fragment {

    private static final String TAG = "FragListOfNearByIssues";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    Connectivity mConnectivity;
    private ProgressDialog pd;
    Location mLocation;
    public static String address;
    public static ArrayList<CIssue> maViolationInfos = new ArrayList<>();
    Button btnRefresh;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RecyclerView mRecyclerView;
    protected ListOfNearByIssuesAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    public static TextView tvNearByLocation;
    LinearLayout llNearLoaction;
    boolean mAddChange = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnectivity = new Connectivity();
        pd = new ProgressDialog(this.getActivity().getWindow().getContext());

    }

    public void callNearByIssuePhp(final double lat, final double lng) {
        try {
            if (Connectivity.checkConnected(getActivity().getApplicationContext())) {
                btnRefresh.setVisibility(View.GONE);
                pd.setMessage("Connecting..."); //
                pd.show();
                final String url = Constants_dp.ISSUE_REPORT_URL;
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                if (!response.trim().equals("-1")) {
                                    getIssueArray(response);
                                } else {
                                    if (pd != null)
                                        pd.dismiss();
                                    Toast.makeText(getActivity().getApplication(), "No issue Found", Toast.LENGTH_LONG).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (pd != null)
                            pd.dismiss();
                        Log.d(TAG, "error is " + error.toString());
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("submitreport", "1");
                        params.put("markerlat", String.format("%.9f", lat));
                        params.put("markerlng", String.format("%.9f", lng));
                        params.put("showall", String.valueOf(ActListOfNearByIssues.mShowAllVal));
                        params.put("myissue", String.valueOf(ActListOfNearByIssues.mMyIssueVal));
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.mPostalCode))
                            params.put("postalcode", ActListOfNearByIssues.mPostalCode);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.mFromDate))
                            params.put("fromissuedate", ActListOfNearByIssues.mFromDate);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.mToDate))
                            params.put("toissuedate", ActListOfNearByIssues.mToDate);
                        if(!TextUtils.isEmpty(ActListOfNearByIssues.mIssueId))
                            params.put("issueid",ActListOfNearByIssues.mIssueId);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.PVVal))
                            params.put("PV",ActListOfNearByIssues.PVVal);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.MVVal))
                            params.put("MV",ActListOfNearByIssues.MVVal);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.RTVal))
                            params.put("RT",ActListOfNearByIssues.RTVal);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.AUTVal))
                            params.put("AT",ActListOfNearByIssues.AUTVal);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.RDVal))
                            params.put("RD",ActListOfNearByIssues.RDVal);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.CLVal))
                            params.put("CL",ActListOfNearByIssues.CLVal);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.ENVal))
                            params.put("EN",ActListOfNearByIssues.ENVal);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.SFVal))
                            params.put("SF",ActListOfNearByIssues.SFVal);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.WEVal))
                            params.put("WE",ActListOfNearByIssues.WEVal);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.OGVal))
                            params.put("OG",ActListOfNearByIssues.OGVal);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.RSVal))
                            params.put("RS",ActListOfNearByIssues.RSVal);


                        if (!TextUtils.isEmpty(ActListOfNearByIssues.closedVal))
                            params.put("closed", ActListOfNearByIssues.closedVal);
                        if (!TextUtils.isEmpty(ActListOfNearByIssues.resolvedVal))
                            params.put("resolved", ActListOfNearByIssues.resolvedVal);
                        params.put("limit", "15");
                        params = CGlobals_db.getInstance(getActivity()).getBasicMobileParams(params,
                                url, getActivity());

                        Log.d(TAG, "PARAM IS " + params);
                        return CGlobals_db.getInstance(getActivity()).checkParams(params);
                    }
                };
                CGlobals_db.getInstance(getActivity()).getRequestQueue(getActivity()).add(postRequest);
            } else {
                btnRefresh.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }

    private void getIssueArray(String data) {
        if (pd != null) {
            pd.dismiss();

        }
        if (maViolationInfos.size() > 0)
            maViolationInfos.clear();
        JSONArray resultRow;
        JSONObject jObj;
        String sLat, sLng;
        Double dLat, dLng;
        if (!TextUtils.isEmpty(data)) {
            try {

                resultRow = new JSONArray(data);

                for (int j = 0; j < resultRow.length(); j++) {
                    jObj = resultRow.getJSONObject(j);
                    CIssue mViolationInfo = new CIssue();
                    mViolationInfo.setID(Integer.parseInt(
                            jObj.isNull("issue_id") ? "0" : jObj.getString("issue_id")));
                    mViolationInfo.setUniqueKey(Long.parseLong(
                            jObj.isNull("unique_key") ? "0" : jObj.getString("unique_key")));
                    mViolationInfo.setAddress(
                            jObj.isNull("issue_address") ? "" : jObj.getString("issue_address"));
                    mViolationInfo.setTimeOffence(
                            jObj.isNull("issue_time") ? "" : jObj.getString("issue_time"));
                    mViolationInfo.setTypeOffence(
                            jObj.isNull("issue_item_description") ? "" : jObj.getString("issue_item_description"));
                    mViolationInfo.setImageName(
                            jObj.isNull("issue_image_name") ? "" : jObj.getString("issue_image_name"));
                    mViolationInfo.set_imagepath(
                            jObj.isNull("issue_image_path") ? "" : jObj.getString("issue_image_path"));
                    mViolationInfo.setVehicle(
                            jObj.isNull("vehicle_no") ? "" : jObj.getString("vehicle_no"));
                    mViolationInfo.setIssue_lookup_item_code(
                            jObj.isNull("issue_item_code") ? "" : jObj.getString("issue_item_code"));
                    mViolationInfo.set_sentToServer(Integer.parseInt(
                            jObj.isNull("submit_report") ? "" : jObj.getString("submit_report")));
                    mViolationInfo.setLikedcount(Integer.parseInt(
                            jObj.isNull("likedCnt") ? "0" : jObj.getString("likedCnt")));
                    mViolationInfo.setUnlikedcount(Integer.parseInt(
                            jObj.isNull("unlikedCnt") ? "0" : jObj.getString("unlikedCnt")));
                    mViolationInfo.setLiked(Integer.parseInt(
                            jObj.isNull("liked") ? "0" : jObj.getString("liked")));
                    sLat = jObj.isNull("lat") ? "" : jObj.getString("lat");
                    sLng = jObj.isNull("lng") ? "" : jObj.getString("lng");

                    if (!TextUtils.isEmpty(sLat)) {
                        dLat = Double.valueOf(sLat);
                        mViolationInfo.setLat(dLat);
                    }
                    if (!TextUtils.isEmpty(sLng)) {
                        dLng = Double.valueOf(sLng);
                        mViolationInfo.setLat(dLng);
                    }
                    mViolationInfo.setGroup_name(jObj.isNull("group_name") ? "" : jObj.getString("group_name"));
                    maViolationInfos.add(mViolationInfo);

                }
                mAdapter = new ListOfNearByIssuesAdapter(getActivity(), maViolationInfos);
                mRecyclerView.setAdapter(mAdapter);

            } catch (Exception e) {
//                Toast.makeText(ViewIssueOnMapAct.this, "error is " + e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        tvNearByLocation.setText(address);
    }

    /*public static FragListOfNearByIssues newInstance() {
        FragListOfNearByIssues fragment = new FragListOfNearByIssues();
        fragment.setRetainInstance(true);
        return fragment;
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.frag_list_of_near_by_issues, container, false);
        try {
            rootView.setTag(TAG);
            tvNearByLocation = (TextView) rootView.findViewById(R.id.tvNearByLocation);
            llNearLoaction = (LinearLayout) rootView.findViewById(R.id.llNearLoaction);
            llNearLoaction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAddChange = true;
                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchAddress_act.class);
                    startActivityForResult(intent, Constants_lib_ss.FINDADDRESS_FROM);
                }
            });
            if (!TextUtils.isEmpty(address))
                tvNearByLocation.setText(address);

            btnRefresh = (Button) rootView.findViewById(R.id.btnRefresh);
            btnRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Connectivity.checkConnected(getActivity().getApplicationContext())) {
                        if (!mConnectivity.connectionError(getActivity().getWindow().getContext())) {
                            if (mConnectivity.isGPSEnable(getActivity().getWindow().getContext())) {
                                Log.d(TAG, "Internet Connection");
                            }
                        }
                    }
                    refresh();
                }
            });


            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

            mLayoutManager = new LinearLayoutManager(getActivity());

            mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

            if (savedInstanceState != null) {
                // Restore saved layout manager type.
                mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                        .getSerializable(KEY_LAYOUT_MANAGER);
            }
            setRecyclerViewLayoutManager(mCurrentLayoutManagerType);
            callNearByIssuePhp(ActListOfNearByIssues.mLatLng.latitude, ActListOfNearByIssues.mLatLng.longitude);
        } catch (Exception e) {
            e.printStackTrace();
            SSLog.e(TAG,"onCreateView",e);
        }
        return rootView;
    }

    private GeoHelper.GeoHelperResult onGeoHelperResult = new GeoHelper.GeoHelperResult() {
        @Override
        public void gotAddress(CAddress addr) {
            if (addr.hasLatitude() || addr.hasLongitude()) {
                address = addr.getAddress();

            } else {
                address = CGlobals_db.getAddress(mLocation, getActivity().getApplicationContext());
            }
            if(TextUtils.isEmpty(address))
                address = CGlobals_lib_ss.getInstance()
                        .getPersistentPreference(getActivity().getApplicationContext())
                        .getString(Constants_dp.PREF_GET_LAST_ADDRESS, "");
        }
    };


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
        try {
            if (!mAddChange) {
                mConnectivity = new Connectivity();
                getLocation();
            }
            if (mAddChange) {
                if (ActListOfNearByIssues.oAddr != null) {
                    callNearByIssuePhp(ActListOfNearByIssues.oAddr.getLatitude(), ActListOfNearByIssues.oAddr.getLongitude());
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "onResume " + e.toString());
            SSLog.e(TAG, "onResume", e);
        }
    }

    private void refresh() {
        mAddChange = false;
        getLocation();
        callNearByIssuePhp(ActListOfNearByIssues.mLatLng.latitude, ActListOfNearByIssues.mLatLng.longitude);
    }

    private void getLocation() {
        mLocation = new Location("location");
        mLocation.setLatitude(ActListOfNearByIssues.mLatLng.latitude);
        mLocation.setLongitude(ActListOfNearByIssues.mLatLng.longitude);

        if (mLocation != null) {
            GeoHelper geoHelper = new GeoHelper();
            geoHelper.getAddress(getActivity().getApplicationContext(), mLocation, onGeoHelperResult);
        }
    }
}