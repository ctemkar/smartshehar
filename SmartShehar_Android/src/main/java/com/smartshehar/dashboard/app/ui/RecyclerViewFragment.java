package com.smartshehar.dashboard.app.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.CIssue;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.DataBaseHandler;
import com.smartshehar.dashboard.app.MyIssuesAdapter;
import com.smartshehar.dashboard.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;


public class RecyclerViewFragment extends
        Fragment {
    DataBaseHandler db = null;
    private static final String TAG = "RecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    JSONArray resultRow = null;
    public static final String ARG_SUBMIT = "submitFlag";
    int submitFlag;
    //    String offenceDetailId;
 /*   HashMap<String, String> hmp = new HashMap<>();*/


    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RecyclerView mRecyclerView;
    protected MyIssuesAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    public static ArrayList<CIssue> imageArry = new ArrayList<>();
    CIssue result1 = new CIssue();
    private ProgressDialog pd;
    Connectivity mConnectivity;
    TextView tvListEmpty;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d(TAG, "on create fragment ");
        // Initialize dataset, this data would usually come from a local content provider or
        // remote server.
        db = new DataBaseHandler(getActivity());
        mConnectivity = new Connectivity();
        pd = new ProgressDialog(this.getActivity().getWindow().getContext());
    }

/*    public static RecyclerViewFragment newInstance() {
        RecyclerViewFragment fragment = new RecyclerViewFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.activity_recycler_view_fragment, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        tvListEmpty = (TextView) rootView.findViewById(R.id.tvListEmpty);
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
        Bundle extras = getArguments();
        submitFlag = extras.getInt(ARG_SUBMIT);
        mRecyclerView.setVisibility(View.GONE);
        //showMyComplaints();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_recycler_view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refreshComplaint) {
            if (!Connectivity.checkConnected(getActivity().getApplicationContext())) {
                if (!mConnectivity.connectionError(getActivity().getWindow().getContext())) {
                    if (mConnectivity.isGPSEnable(getActivity().getWindow().getContext())) {
                        Log.d(TAG, "Internet Connection");
                    }
                }
            }
            tvListEmpty.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            showMyComplaints();

        }
        return super.onOptionsItemSelected(item);
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

   /* public static class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnItemClickListener mListener;

        public interface OnItemClickListener {
            public void onItemClick(View view, int position);
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
                mListener.onItemClick(childView, view.getChildPosition(childView));
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
    }*/

    public void showMyComplaints() {
        try {
            /*myComplaintFlag = true;
            groupComplaintFlag = false;*/
            if (Connectivity.checkConnected(getActivity().getApplicationContext())) {
                pd.setMessage("Connecting..."); //
                pd.show();
                final String url = Constants_dp.ISSUE_REPORT_URL;

                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                if (!response.trim().equals("-1")) {
                                    dataList(response);
                                    Log.d(TAG, "" + response);
                                } else {
                                    if (pd != null)
                                        pd.dismiss();
                                    mRecyclerView.setVisibility(View.GONE);
                                    tvListEmpty.setVisibility(View.VISIBLE);

                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (pd != null)
                            pd.dismiss();
                        mRecyclerView.setVisibility(View.GONE);
                        tvListEmpty.setVisibility(View.VISIBLE);
                        tvListEmpty.setText("List is Empty. Pleas check your Internet Connection also");
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {

                        Map<String, String> params = new HashMap<>();
                        params.put("myissue", "1");
                        params = CGlobals_db.getInstance(getActivity().getApplication()).getBasicMobileParams(params,
                                url, getActivity());

                        return CGlobals_db.getInstance(getActivity()).checkParams(params);
                    }
                };
                CGlobals_db.getInstance(getActivity()).getRequestQueue(getActivity()).add(postRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }


    private void dataList(String response) {

        try {
            if (pd != null) {
                pd.dismiss();

            }
            if (TextUtils.isEmpty(response)) {
                return;
            }
            mRecyclerView.setVisibility(View.VISIBLE);
            tvListEmpty.setVisibility(View.GONE);
            imageArry.clear();
            resultRow = new JSONArray(response);
            // last inserted row
            getFirstRow();
            int resolved, unresolved, closed, opened;
            for (int j = 0; j < resultRow.length(); j++) {
                JSONObject jObj = resultRow.getJSONObject(j);
                CIssue result = new CIssue();
                result.setID(Integer.parseInt(jObj.getString("issue_id")));
                result.setUniqueKey(Long.parseLong(jObj.getString("unique_key")));
                result.setAddress(jObj.isNull("issue_address") ? "" : jObj.getString("issue_address"));

                result.setTimeOffence(jObj.isNull("issue_time") ? "" : jObj.getString("issue_time"));
                result.setTypeOffence(jObj.isNull("issue_item_description") ? "" : jObj.getString("issue_item_description"));
                result.setImageName(jObj.isNull("issue_image_name") ? "" : jObj.getString("issue_image_name"));
                result.set_imagepath(jObj.isNull("issue_image_path") ? "" : jObj.getString("issue_image_path"));
                result.setVehicle(jObj.isNull("vehicle_no") ? "" : jObj.getString("vehicle_no"));
                result.setIssue_lookup_item_code(jObj.isNull("issue_item_code") ? "" : jObj.getString("issue_item_code"));
                result.set_sentToServer(Integer.parseInt(jObj.getString("submit_report")));
                result.set_approved(Integer.parseInt(jObj.getString("sent_to_authority")));
                result.setRejected(Integer.parseInt(jObj.getString("rejected")));
                result.set_lettersubmitted(Integer.parseInt(jObj.getString("letter_upload_notification")));
                resolved = Integer.parseInt(jObj.getString("resolved"));
                unresolved = Integer.parseInt(jObj.getString("unresolved"));
                if (resolved == 1 || unresolved == 1)
                    result.set_resolved_unresolved(1);
                else
                    result.set_resolved_unresolved(0);
                closed = Integer.parseInt(jObj.getString("closed"));
                opened = Integer.parseInt(jObj.getString("opened"));
                result.set_closed(closed);
                result.set_open(opened);
                result.setGroup_name(jObj.isNull("group_name") ? "" :jObj.getString("group_name"));
                if (result.getUniqueKey() == result1.getUniqueKey()) {
                    continue;
                }
                imageArry.add(result);
            }
            Gson gson = new Gson();
            String json = gson.toJson(imageArry);
            CGlobals_lib_ss.getInstance()
                    .getPersistentPreferenceEditor(getActivity().getApplicationContext()).putString("PERF_RECYCLER_VIEW_FRAGMENT", json).commit();
        } catch (JSONException e) {
            mRecyclerView.setVisibility(View.GONE);
            tvListEmpty.setVisibility(View.VISIBLE);
            getFirstRow();
            e.printStackTrace();
        } catch (Exception e) {
            mRecyclerView.setVisibility(View.GONE);
            tvListEmpty.setVisibility(View.VISIBLE);
            e.printStackTrace();
        } finally {

            try {
                if (imageArry.size() <= 0) {
                    Toast.makeText(getActivity().getApplication(), "List is Empty", Toast.LENGTH_LONG).show();
                } else {
                    mAdapter = new MyIssuesAdapter(getActivity(), imageArry);
                    mRecyclerView.setAdapter(mAdapter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getFirstRow() {

    /*    if (submitFlag == 1) {
            sViolation = CGlobals_lib_ss.getInstance()
                    .getPersistentPreference(getActivity().getApplicationContext()).
                            getString(Constants_dp.PREF_VIOLATION_ALL_VALUE, "");
            if (TextUtils.isEmpty(sViolation)) {
                Log.d(TAG, "No issues");
            } else {
                Gson gson1 = new Gson();
                hmp = gson1.fromJson(sViolation,
                        new TypeToken<HashMap<String, String>>() {
                        }.getType());
                result1.setUniqueKey(Long.parseLong(hmp.get("uniquekey")));
                result1.setAddress(hmp.get("offenceaddress"));
                result1.setTimeOffence(hmp.get("offencedatetime"));
                result1.setTypeOffence(hmp.get("offencetype"));
                result1.setImageName(hmp.get("imagename"));
                result1.set_imagepath(hmp.get("currentimagepath"));
                result1.setVehicle(hmp.get("vehicleno"));
                result1.setIssue_lookup_item_code(hmp.get("subtypecode"));
                imageArry.add(result1);
            }

        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecyclerView.setVisibility(View.GONE);
        showMyComplaints();

    }
}