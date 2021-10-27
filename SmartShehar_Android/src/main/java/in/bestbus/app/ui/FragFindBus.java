package in.bestbus.app.ui;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartshehar.dashboard.app.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

import in.bestbus.app.CBus;
import in.bestbus.app.CGlobals_BA;
import in.bestbus.app.CStop;
import in.bestbus.app.StopListAdapter;
import lib.app.util.CTime;
import lib.app.util.SSLog_SS;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragFindBus extends Fragment {
    private final static String RECENT_TRIPS_TAG = "RecentTrips";
    private final static int MAX_RECENT_TRIPS = 10;
    private static final int ID_ROUTE = 1;
    private static final int ID_WALKTOSTOP = 2;
    private static final int ID_ROUTEMAP = 3;
    private final String TAG = "FragFindBus: ";
    private final String SELECTRECENT = "Select Stop";
    private final String SELECTSTOPS = "Stop List";
    private final int MAXRECENTSTOPS = 5;
    private final String URL = CGlobals_BA.PHP_PATH + "/expectedbusandtime.php";
    ArrayList<String> masRecentStops;
    Spinner mStartSpinner;
    ArrayAdapter<String> mStartSpinnerAdapter;
    boolean firstTimeStartSpinner = true;
    boolean bShowingTrains = false; // getting trains to show in background
    ArrayList<String> maStation = null; //, maStop = null;
    ProgressBar mProgressBar;
    int iCurrentStopId = -1;
    ArrayList<CBus> maBusesAtStop;
    Cursor startListCursor, destListCursor;
    RelativeLayout mrlTowards;
    String msMergeId = "";
    String msTowardsStations;
    CTime moTm;
    int iGroupIdx;
    Location myLocation;
    // UI objects
//	LinearLayout mllStartStation;
    Button mBtnClearStart, mBtnClearDest;
    Button mBtnRecentTrips, mBtnReverseTrip, mBtnGoTrip;
    TableLayout tblBusesAtStop;
    //QuickAction mQuickAction;
    int miProgress = 0;
    CurrentLayoutData mCurrentLayoutData;
    private CGlobals_BA mApp = null;
    private ArrayList<Trip> maoRecentTrips;
    private ArrayList<String> masRecentTripsList;
    private ArrayAdapter<String> mRecentTripsAdapter;
    //	Button mBtnRecentStops;
    private TextView mTvBusesToStation, mTvDistance;

    ;
    private AutoCompleteTextView mStartView, mDestView;
    private int mDoW;
    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mActivity = getActivity();

//		mApp = (CGlobals_BA) mActivity.getApplication();
        mApp = CGlobals_BA.getInstance();
        mApp.init(mActivity);
//		mApp.mCH.userPing(getString(R.string.fragFindBus));
        maStation = new ArrayList<String>();

        maBusesAtStop = new ArrayList<CBus>();
///		mApp.mbAutoRefreshLocation = false;
        mCurrentLayoutData = new CurrentLayoutData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragfindbus, container, false);


        setupUI(v);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menufragfind, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_location) {
            /*if (mActivity != null)
                Toast.makeText(mActivity, "Getting location...", Toast.LENGTH_SHORT).show();*/
            iCurrentStopId = -1;
            mApp.mbAutoRefreshLocation = true;
            Location location = mApp.getMyLocation(getActivity().getApplicationContext());
            updateMyLocation(location);
        } else if (item.getItemId() == R.id.menu_refresh) {
            iCurrentStopId = -1;
            mApp.mbAutoRefreshLocation = false;
            String start = mStartView.getText().toString();
            if (!TextUtils.isEmpty(start))
                setStartStop(start);
            if (mActivity != null)
                Toast.makeText(mActivity, "Refreshing ...", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        disableInput();
//        mProgressBar. ();
        writeRecentTrips();
        super.onPause();
    }

    @Override
    public void onResume() {

        mProgressBar.setIndeterminate(false);
        mProgressBar.setMax(100);
        if ((maStation == null || maStation.size() == 0))
            maStation = mApp.mDBHelperBus.getStationNames();
//		if (maStop == null || maStop.size() == 0)
//		maStop = mApp.mDBHelper.getStopNames();
        masRecentStops = getRecentStops();
        if (mApp.mbAutoRefreshLocation)
            updateMyLocation(mApp.getMyLocation(getActivity().getApplicationContext()));

        if (mApp.moStartStop != null &&
                mApp.moStartStop.miStopId == mCurrentLayoutData.miStartStopId) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(10);
            displayBuses(mCurrentLayoutData.aBusesAtStop);
            if (TextUtils.isEmpty(mCurrentLayoutData.sBusesToStation))
                new getBusesToNearestStation().execute("");
            else
                mTvBusesToStation.setText(mCurrentLayoutData.sBusesToStation);
            new getBusFrequencies().execute("");

            mProgressBar.setVisibility(View.GONE);
        } else {
            iCurrentStopId = -1;
            refreshStartStop();
        }
        enableInput();
        //keyboardShowHide(false);

        super.onResume();
    }

    ArrayList<String> getRecentStops() {
        ArrayList<String> aRS = new ArrayList<String>();
        SharedPreferences spRecentStations = mActivity
                .getSharedPreferences(SELECTSTOPS, Context.MODE_PRIVATE);

        String sRecentStops = spRecentStations.getString(SELECTSTOPS, null);
        SSLog_SS.i("BusApp: TrainHome - ", sRecentStops);
        if (sRecentStops != null) {
            boolean bStopThere = false;
            String s;
            if (sRecentStops != null & sRecentStops.trim().length() > 0) {
                String as[] = sRecentStops.split(";");
                int iLen = as.length;
                for (int i = 0; i < iLen; i++) {
                    s = as[i];
                    if (s != null && !s.equalsIgnoreCase(SELECTRECENT)) {
                        bStopThere = false;

                        for (int j = 0; j < i; j++) {
                            // s2 = as[j];
                            if (s.equals(as[j])) {
                                bStopThere = true;
                                break;
                            }
                        }
                        try {
                            if (!bStopThere) {
                                // SSLog.i("BusApp: Recent Station: ", s);
                                // if (oStop != null)
                                aRS.add(s);
                            }
                        } catch (Exception e) {
                            SSLog_SS.e("BusApp: getRecentStations - ",
                                    e.getMessage());
                            SSLog_SS.e("BusApp: getRecentStations - mApp - ",
                                    mApp == null ? " null" : " ");

                        }
                    }
                }
            }
        }
        // Add all the stops for VI users for(String st : maStop)
        //aRS.add(st);
        //
        return aRS;
    } // getRecentStops

    void setupUI(View v) {
        if (mApp == null) {

        }
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mTvDistance = (TextView) v.findViewById(R.id.tvDistamce);
        mTvDistance.setText("");
        mTvBusesToStation = (TextView) v.findViewById(R.id.tvBusesToStation);
//		mStartSpinner = (Spinner) v.findViewById(R.id.start_spinner);
        mBtnRecentTrips = (Button) v.findViewById(R.id.btnRecentTrips);
        mBtnReverseTrip = (Button) v.findViewById(R.id.btnReverseTrip);
        mBtnGoTrip = (Button) v.findViewById(R.id.btnGoTrip);
        mBtnClearStart = (Button) v.findViewById(R.id.btnClear);
        mBtnClearDest = (Button) v.findViewById(R.id.btnClearDest);
        mStartView = (AutoCompleteTextView) v.findViewById(
                R.id.autocomplete_startstop);
        mDestView = (AutoCompleteTextView) v.findViewById(
                R.id.autocomplete_deststop);
//		mllStartStation = (LinearLayout) v.findViewById(R.id.llStartStation);
        tblBusesAtStop = (TableLayout) v.findViewById(
                R.id.tblBuseAtStopTable);


        StopListAdapter startAdapter, destAdapter;
        masRecentTripsList = new ArrayList<String>();
        maoRecentTrips = readRecentTrips();


        mStartSpinnerAdapter = new ArrayAdapter<String>(mActivity,
                R.layout.listitem, masRecentStops);

        mStartSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

//		mStartSpinner.setAdapter(mStartSpinnerAdapter);
        mBtnRecentTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mApp.mbAutoRefreshLocation = false;
                if (masRecentTripsList != null && masRecentTripsList.size() > 0) {
                    new AlertDialog.Builder(mActivity)
                            .setTitle("Recent Trips")
                            .setAdapter(mRecentTripsAdapter, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        Toast.makeText(mActivity, masRecentTripsList.get(which),
                                                Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        SSLog_SS.e(TAG, e.getMessage());
                                    }
                                    dialog.dismiss();
                                    //mApp.mCH.userPing(getString(R.string.atRecentTrip));
                                    fireTrackerEvent(getString(R.string.atRecentTrip));
                                    setStartDest(maoRecentTrips.get(which).oStartStop, maoRecentTrips.get(which).oDestStop);
                                }


                            }).create().show();
                } else
                    Toast.makeText(mActivity, "No Recent Trips",
                            Toast.LENGTH_LONG).show();

            }
        });

        mBtnClearStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mStartView.setText("");
                mTvDistance.setText("");
                mStartView.setThreshold(0);
                mStartView.requestFocus();
                //keyboardShowHide(true);
                mApp.mbAutoRefreshLocation = false;
            }
        });

        mBtnClearDest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mDestView.setText("");
                mApp.mbAutoRefreshLocation = false;
            }
        });
        mBtnGoTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showRoutes(mApp.moStartStop, mApp.moDestStop, false);
            }
        });

        mBtnReverseTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String sStart = mStartView.getText().toString();
                String sDest = mDestView.getText().toString();
                if (!TextUtils.isEmpty(sStart))
                    mApp.moDestStop = mApp.mDBHelperBus.getStopFromSearchStr(sStart);
                if (!TextUtils.isEmpty(sDest))
                    mApp.moStartStop = mApp.mDBHelperBus.getStopFromSearchStr(sDest);
                mDestView.setText(sStart);
                mStartView.setText(sDest);
                showRoutes(mApp.moStartStop, mApp.moDestStop, false);
                mApp.mbAutoRefreshLocation = false;
                //mApp.mCH.userPing(getString(R.string.atFlipTrip));
                fireTrackerEvent(getString(R.string.atFlipTripLong));

            }
        });


        startListCursor = mApp.mDBHelperBus.getStopNamesCursor("");
        destListCursor = mApp.mDBHelperBus.getStopNamesCursor("");
        startAdapter = new StopListAdapter(mActivity, startListCursor, mApp);
        destAdapter = new StopListAdapter(mActivity, destListCursor, mApp);
//		if (TextUtils.isEmpty(mStartView.getText().toString()))
//			mApp.mbAutoRefreshLocation = true;

        mStartView.setAdapter(startAdapter);
        mStartView.invalidate();
        mStartView.clearFocus();
        mStartView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // your code here....
                mStartView.setThreshold(0);
                mActivity
                        .getWindow()
                        .setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return false;
            }
        });
        mStartView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                mApp.mbAutoRefreshLocation = false;
                //keyboardShowHide(false);
                mStartView.setThreshold(1000);

                mStartView.post(new Runnable() {
                    @Override
                    public void run() {
                        setStartStop(mStartView.getText().toString());
                        mStartView.dismissDropDown();
                        mStartView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager keyboard = (InputMethodManager) mActivity
                                        .getSystemService(
                                                Context.INPUT_METHOD_SERVICE);
                                keyboard.hideSoftInputFromWindow(
                                        mStartView.getWindowToken(), 0);
                            }
                        }, 200);
                    }
                });
            }
        });
        mDestView.setAdapter(destAdapter);
        mDestView.invalidate();
        mDestView.clearFocus();
        mDestView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // your code here....
                mDestView.setThreshold(0);
                mActivity
                        .getWindow()
                        .setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return false;
            }
        });
        mDestView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                mApp.mbAutoRefreshLocation = false;
                //keyboardShowHide(false);
                mDestView.setThreshold(1000);
                mDestView.post(new Runnable() {
                    @Override
                    public void run() {
                        setDestStop(mDestView.getText().toString());
                        mDestView.dismissDropDown();
                        mDestView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager keyboard = (InputMethodManager) mActivity
                                        .getSystemService(
                                                Context.INPUT_METHOD_SERVICE);
                                keyboard.hideSoftInputFromWindow(
                                        mDestView.getWindowToken(), 0);
                            }
                        }, 200);
                    }
                });
            }
        });

//		mllStartStation.setVisibility(LinearLayout.VISIBLE);

        mStartView.setThreshold(1000);
//		mBtnRecentStops.requestFocus();
        mActivity.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // keyboardShowHide(false);
    } // setupUI ends

    private void refreshStartStop() {
        Location location = mApp.getMyLocation(getActivity().getApplicationContext());
        if (mApp.moStartStop != null)
            setStartStop(mApp.moStartStop.msStopNameDetail);
        else if (location != null) {
            if (mApp.mbAutoRefreshLocation)
                updateMyLocation(location);
        }
        mStartView.dismissDropDown();
    }

    public void updateMyLocation(Location location) {
        // Now get the places at this location, if this fragment is still bound
        // to an activity
        if (!mApp.mbAutoRefreshLocation || location == null)
            return;
        float[] res = new float[1];
        if (myLocation != null) {
            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                    myLocation.getLatitude(), myLocation.getLongitude(), res);
//			if((int)res[0] < 50)
//				return;
        }

        myLocation = location;
//		if (!mApp.isBetterLocation(location, mApp.mCurrentLocation))
//			return;
        tblBusesAtStop.post(new Runnable() {
            @Override
            public void run() {
                Location location = mApp.getMyLocation(getActivity().getApplicationContext());
                if (location != null)
                    mApp.moStartStop = mApp.mDBHelperBus.getNearestStop(location.getLatitude(),
                            location.getLongitude());
                if (mApp.moStartStop == null)
                    return;
                setStartStop(mApp.moStartStop.msStopNameDetail);
                mTvDistance.setText(String.format("(%.2f km)", mApp.moStartStop.mdDist / 1000));

//				tvStartStation.setText(mApp.moStartStop.msStopNameDetail);
//				tvStartStation.setContentDescription(mApp.moStartStop.msStopNameDetail);
                mStartView.setText(mApp.moStartStop.msStopNameDetail);
                mStartView.setContentDescription(mApp.moStartStop.msStopNameDetail);
                // set current time
            }
        });
    }

    void setStartStop(String sStartStopFull) {

        CStop retStop = mApp.mDBHelperBus.getStopFromSearchStr(sStartStopFull);
        if (retStop != null) {
            mApp.moStartStop = retStop;

            mStartView.setThreshold(1000);
            mStartView.setText(mApp.moStartStop.msStopNameDetail);
            showBusesAtStop(mApp.moStartStop.miStopId);
            addRecentStation(mApp.moStartStop.msStopNameDetail);
            mStartView.setText(sStartStopFull);
            mStartView.dismissDropDown();

        }
    }

    void setDestStop(String sDestStop) {
        CStop retStop = mApp.mDBHelperBus.getStopFromSearchStr(sDestStop);
        if (retStop != null) {
            mApp.moDestStop = retStop;
            mDestView.setThreshold(1000);
            mDestView.setText(mApp.moDestStop.msStopNameDetail);
            mDestView.dismissDropDown();
            showRoutes(mApp.moStartStop, mApp.moDestStop, false);
        }
    }

    void addRecentStation(String sStationFull) {
        if (masRecentStops == null)
            return;
        masRecentStops.add(0, sStationFull);
        int iLen = masRecentStops.size();
        if (iLen > MAXRECENTSTOPS)
            masRecentStops.remove(iLen - 1);
        // remove the same Station lower in the list
        iLen = masRecentStops.size();
        String sf;
        for (int i = 1; i < iLen; i++) {
            sf = masRecentStops.get(i);
            if (sf.equalsIgnoreCase(sStationFull)) {
                masRecentStops.remove(i);
                break;
            }
        }
    } // addRecentStation

    private void showBusesAtStop(int iStopId) {
        if (iCurrentStopId != iStopId) {
            iCurrentStopId = iStopId;
            Calendar c = Calendar.getInstance();
            mDoW = mApp.dow(c.get(Calendar.DAY_OF_WEEK));
            miProgress = 0;
            new getBusesToNearestStation().execute("");
            new GetBusesNearYou().execute("");
        }

        // new GetArrivingBuses().execute("");

    } // showBusesTowards

    private void displayBuses(ArrayList<CBus> aBusesAtStop) {
        if (mActivity == null)
            return;

        LayoutInflater inflater = mActivity.getLayoutInflater();
        String sCurStopName = "";

        tblBusesAtStop.removeAllViews();
        for (final CBus oBus : aBusesAtStop) {
            if (!oBus.msStopName.equals(sCurStopName)) {
                LinearLayout trstop = (LinearLayout) inflater.inflate(
                        R.layout.stoprow, tblBusesAtStop, false);
                TextView tvStopName = (TextView) trstop
                        .findViewById(R.id.tvStopName);
                tvStopName.setText(aBusesAtStop.get(0).msStopName);
                sCurStopName = oBus.msStopName;
            }
            LinearLayout trd = (LinearLayout) inflater.inflate(
                    R.layout.busrowupdn, tblBusesAtStop, false);

            TextView tvBusNo = (TextView) trd.findViewById(R.id.tvBusNo);
//			TextView tvBusNoDevanagari = (TextView) trd.findViewById(R.id.tvBusNoHindi);
            final LinearLayout rlUp = (LinearLayout) trd.findViewById(R.id.rlUp);
            final LinearLayout rlDn = (LinearLayout) trd.findViewById(R.id.rlDn);

            final TextView tvFrequencyUp = (TextView) trd.findViewById(R.id.tvFreqUp);
            final TextView tvRouteUp = (TextView) trd.findViewById(R.id.tvRouteUp);
            final TextView tvRouteDn = (TextView) trd.findViewById(R.id.tvRouteDn);
            final TextView tvEtaUp = (TextView) trd.findViewById(R.id.tvEtaUp);
            final TextView tvEtaDn = (TextView) trd.findViewById(R.id.tvEtaDn);
            final TextView tvAcTimeUp = (TextView) trd.findViewById(R.id.tvAcTimeUp);
            final TextView tvAcTimeDn = (TextView) trd.findViewById(R.id.tvAcTimeDn);
            tvBusNo.setText(oBus.msBusLabel);
            tvBusNo.setTextColor(oBus.busColor);
//			tvBusNo.setTypeface(mApp.typefaceHindi, Typeface.NORMAL);
//			tvBusNo.setText(tvBusNo.getText().toString() + "\n" + oBus.msBusNoDevanagari);
            tvBusNo.setTypeface(mApp.typefaceHindi, Typeface.NORMAL);
            tvBusNo.setText(oBus.msBusLabel + "\n" + oBus.msBusNoDevanagari);
            tvBusNo.setContentDescription(oBus.msBusLabel);
            tvRouteUp.setText(" to " + oBus.msLastStop);
            tvRouteDn.setText(" to " + oBus.msFirstStop);
            tvRouteUp.setContentDescription(" to " + oBus.msLastStop);
            tvRouteDn.setContentDescription(" to " + oBus.msFirstStop);
            tvFrequencyUp.setText("");
            if (!TextUtils.isEmpty(oBus.msFrequencyUp)) {
                tvFrequencyUp.setText(oBus.msFrequencyUp);
                tvFrequencyUp.setContentDescription(oBus.msFrequencyUp);
            }
            tvEtaUp.setText("");
            if (!TextUtils.isEmpty(oBus.msEtaUp)) {
                tvEtaUp.setText(oBus.msEtaUp);
                tvEtaUp.setContentDescription(oBus.msEtaUp);
            }
            tvEtaDn.setText("");
            if (!TextUtils.isEmpty(oBus.msEtaDn)) {
                tvEtaDn.setText(oBus.msEtaDn);
                tvEtaDn.setContentDescription(oBus.msEtaDn);
            }
            tvAcTimeUp.setText("");
            if (!TextUtils.isEmpty(oBus.msAcTimeUp)) {
                tvAcTimeUp.setVisibility(View.VISIBLE);
                tvAcTimeUp.setText(oBus.msAcTimeUp);
                tvAcTimeUp.setContentDescription(oBus.msAcTimeUp);
            }
            tvAcTimeDn.setText("");
            if (!TextUtils.isEmpty(oBus.msAcTimeDn)) {
                tvAcTimeDn.setVisibility(View.VISIBLE);
                tvAcTimeDn.setText(oBus.msAcTimeDn);
                tvAcTimeDn.setContentDescription(oBus.msAcTimeDn);
            }
            rlUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    String selectedBus = "Getting route for: " + oBus.msBusLabel;
                    Toast.makeText(mActivity.getApplicationContext(),
                            selectedBus, Toast.LENGTH_SHORT).show();
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        public void run() {

                        }
                    });
                    mApp.msRouteCode = oBus.msRouteCode;
                    mApp.msBusNo = oBus.msBusLabel;
                    mApp.mdStopLat = oBus.mdLatu;
                    mApp.mdStopLon = oBus.mdLonu;
                    mApp.msBusDirection = "U";
                    //mQuickAction.show(arg0);
                }
            });
            rlDn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    String selectedBus = "Getting route for: " + oBus.msBusLabel;
                    Toast.makeText(mActivity.getApplicationContext(),
                            selectedBus, Toast.LENGTH_SHORT).show();
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        public void run() {

                        }
                    });

                    mApp.msRouteCode = oBus.msRouteCode;
                    mApp.msBusNo = oBus.msBusLabel;
                    mApp.mdStopLat = oBus.mdLatd;
                    mApp.mdStopLon = oBus.mdLond;
                    mApp.msBusDirection = "D";
                }
            });
            tblBusesAtStop.addView(trd);
        }
    }

    private void displayArrivingBuses(String jsonArrivingBuses) {
        if (TextUtils.isEmpty(jsonArrivingBuses)) {
            // mTvStatus.setVisibility(View.GONE);
            // mTvStatus.setText("No buses found at this stop");
            return;
        }
        // / mLlRowTitle.setVisibility(View.GONE);
        String sBusNo = "", sDestinationStop = "", sEta = "";

        LayoutInflater inflater = mActivity.getLayoutInflater();

        TableLayout tl = (TableLayout) getView().findViewById(
                R.id.llArrivingBuses);

        tblBusesAtStop.removeAllViews();
        try {
            JSONArray jObject = new JSONArray(jsonArrivingBuses);

            for (int i = 0; i < jObject.length(); i++) {
                JSONObject jArrivingBuses = jObject.getJSONObject(i);
                sBusNo = jArrivingBuses.getString("b");
                sDestinationStop = jArrivingBuses.getString("sd");
                sEta = jArrivingBuses.getString("eta");
                LinearLayout trd = (LinearLayout) inflater.inflate(
                        R.layout.arrivingbusrow, tl, false);
                TextView tvArrivingBusNo = (TextView) trd
                        .findViewById(R.id.tvArrivingBusNo);
                final TextView tvDestinationStop = (TextView) trd
                        .findViewById(R.id.tvDestinationStop);
                final TextView tvArrivingBusEta = (TextView) trd
                        .findViewById(R.id.tvArrivingBusEta);

                tvArrivingBusNo.setText(sBusNo);
                tvArrivingBusNo.setContentDescription(sBusNo);
                tvDestinationStop.setText(sDestinationStop);
                tvDestinationStop.setContentDescription("towards: "
                        + sDestinationStop);
                // tvArrivingBusEta.setTypeface(tf);
                tvArrivingBusEta.setText(sEta + " mins");
                tvArrivingBusEta.setContentDescription(sEta);

                tblBusesAtStop.addView(trd);
            }
        } catch (Exception e) {
            SSLog_SS.e("displayArrivingBuses: ", e.getMessage());
        }

    }

    private void layoutDone() {
        if (miProgress < 80)
            return;
        mProgressBar.setProgress(100);
        mProgressBar.setVisibility(View.GONE);
        mCurrentLayoutData.miStartStopId = mApp.moStartStop.miStopId;
        if (maBusesAtStop != null)
            mCurrentLayoutData.aBusesAtStop = maBusesAtStop;
    }

    public void fireTrackerEvent(String label) {
///    	EasyTracker.getTracker().sendEvent(getString(R.string.fragFindBusLong),
///				getString(R.string.fragFindBus), label, (long) 0);

    }

    // Add a trip chosen by user
    void addRecentTrip(CStop oStartStation, CStop oDestStation) {
        if (maoRecentTrips == null)
            maoRecentTrips = new ArrayList<Trip>();
        if (masRecentTripsList == null)
            masRecentTripsList = new ArrayList<String>();
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);

        maoRecentTrips.add(0, new Trip(oStartStation, oDestStation,
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))); // add as first trip FIFO
        int iLen = maoRecentTrips.size();
        if (iLen > MAX_RECENT_TRIPS)
            maoRecentTrips.remove(iLen - 1);
        // remove the same Station lower in the list
        iLen = maoRecentTrips.size();
        Trip trip;
        for (int i = 1; i < iLen; i++) {
            trip = maoRecentTrips.get(i);
            if (trip.oStartStop.miStopId == oStartStation.miStopId &&
                    trip.oDestStop.miStopId == oDestStation.miStopId) {
                maoRecentTrips.remove(i);
                break;
            }
        }
        masRecentTripsList.clear();
        for (Trip tr : maoRecentTrips) {
            masRecentTripsList.add(tr.msTrip + "\n - " + tr.oDestStop.msStopNameDetail);
        }
        mRecentTripsAdapter = new ArrayAdapter<String>(mActivity,
                R.layout.multiline_spinner_dropdown_item, masRecentTripsList);

    } // addRecentTrips

    ;

    private void writeRecentTrips() {
        new Thread(new Runnable() {
            public void run() {
                StringBuilder sb = new StringBuilder();
                String lineDelim = "";
                int iLen = maoRecentTrips.size();
                if (iLen < 1)
                    return;
                String s;
                for (int i = 0; i < iLen; i++) {
                    s = Integer.toString(maoRecentTrips.get(i).oStartStop.miStopId)
                            + ";" + Integer.toString(maoRecentTrips.get(i).oDestStop.miStopId);
                    if (!TextUtils.isEmpty(s)) {
                        sb.append(lineDelim + s);
                        lineDelim = "\n";
                    }
                }
                SharedPreferences spRecentTrips = mActivity.getSharedPreferences(
                        RECENT_TRIPS_TAG, Context.MODE_PRIVATE);
                SharedPreferences.Editor speRecentStopsEditor;
                speRecentStopsEditor = spRecentTrips.edit();

                speRecentStopsEditor.putString(RECENT_TRIPS_TAG, sb.toString());
                speRecentStopsEditor.commit();
            }
        }).start();

    } // writeRecentTrips

    ArrayList<Trip> readRecentTrips() {
        ArrayList<Trip> aRS = new ArrayList<Trip>();
        SharedPreferences spRecentStations = mActivity.getSharedPreferences(
                RECENT_TRIPS_TAG, Context.MODE_PRIVATE);

        String sRecentTrips = spRecentStations.getString(RECENT_TRIPS_TAG,
                null);
        SSLog_SS.i(TAG + " readRecentTrips - ", sRecentTrips);
        if (sRecentTrips == null)
            return aRS;
        boolean bStopThere = false;
        String stationPair;
        String[] s;

        int iStart, iDest;
        if (sRecentTrips != null & sRecentTrips.trim().length() > 0) {
            String as[] = sRecentTrips.split("\n");
            int iLen = as.length;
            for (int i = 0; i < iLen; i++) {
                stationPair = as[i];
                if (stationPair != null && !stationPair.equalsIgnoreCase(SELECTRECENT)) {
                    bStopThere = false;

                    try {
                        if (!bStopThere) {
                            SSLog_SS.i(TAG + " Recent Trip - ", stationPair);
                            // if (oStop != null)
                            s = stationPair.split(";");
                            iStart = Integer.valueOf(s[0]);
                            iDest = Integer.valueOf(s[1]);
                            aRS.add(new Trip(iStart, iDest));
                        }
                    } catch (Exception e) {
                        SSLog_SS.e("TrainApp: getRecentTrip - ",
                                e.getMessage());
                    }
                }
            }
        }
        masRecentTripsList.clear();
        for (Trip tr : aRS) {
            masRecentTripsList.add(tr.msTrip);
//			masRecentTripsList.add(tr.oStartStop.msStopName + "\n" + tr.oDestStop.msStopName);
        }
        mRecentTripsAdapter = new ArrayAdapter<String>(mActivity,
                R.layout.multiline_spinner_dropdown_item, masRecentTripsList);

        return aRS;
    } // readRecentTrips

    private void showRoutes(CStop startStop, CStop destStop, boolean bSilent) {
        CStop oDestStop = null;
        if (destStop == null) {
            boolean bFailed = true;
            String sDest = mDestView.getText().toString();
            if (!TextUtils.isEmpty(sDest))
                oDestStop = mApp.mDBHelperBus.getStopFromSearchStr(sDest);
            if (oDestStop != null)
                destStop = oDestStop;
            if (bFailed && oDestStop == null) {
                if (!bSilent)
                    Toast.makeText(mActivity.getApplicationContext(), "Invalid destination Stop",
                            Toast.LENGTH_SHORT).show();
                return;
            }

        }
        mApp.moStartStop = startStop;
        mApp.moDestStop = destStop;

        if (mApp.moStartStop.miStopId == mApp.moDestStop.miStopId) {
            if (!bSilent)
                Toast.makeText(mActivity.getApplicationContext(), "Same Start and Destination CStop",
                        Toast.LENGTH_SHORT).show();
            return;
        }
        addRecentTrip(startStop, destStop);
        fireTrackerEvent(getString(R.string.atShowRoute) + "_" +
                mApp.moStartStop.msStopNameDetail + " - " + mApp.moDestStop.msStopNameDetail);
        Intent intent = new Intent(getActivity(), ActBusTrip.class);
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }


    } // showRoutes

    void enableInput() {
        mStartView.setEnabled(true); // mStartView.setInputType(InputType.TYPE_NULL);
        mDestView.setEnabled(true); // mDestView.setInputType(InputType.TYPE_NULL);

    }

    void disableInput() {
        mStartView.setEnabled(false); // mStartView.setInputType(InputType.TYPE_NULL);
        mDestView.setEnabled(false); // mDestView.setInputType(InputType.TYPE_NULL);

    }

    private void setStartDest(CStop oStartStop, CStop oDestStop) {
        mApp.moStartStop = oStartStop;
        mApp.moDestStop = oDestStop;
        mStartView.setText(oStartStop.msStopNameDetail);
        mDestView.setText(oDestStop.msStopNameDetail);
        mApp.mbAutoRefreshLocation = false;
        showRoutes(mApp.moStartStop, mApp.moDestStop, false);

    }

    class CurrentLayoutData {
        int miStartStopId;
        ArrayList<CBus> aBusesAtStop;
        String sBusesToStation;
        TableLayout mTableLayout;

        public CurrentLayoutData() {
            sBusesToStation = "";
            aBusesAtStop = new ArrayList<CBus>();
        }
    }

    @SuppressWarnings("unused")
    private class GetArrivingBuses extends AsyncTask<String, Integer, Long> {
        private String sArrivingBuses = "";

        protected Long doInBackground(String... sbusno) {
            try {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                Location location = mApp.getMyLocation(getActivity().getApplicationContext());
                if (mApp.moStartStop.miStopId > 0)
                    nameValuePairs.add(new BasicNameValuePair("stopid", Integer
                            .toString(mApp.moStartStop.miStopId)));
                nameValuePairs.add(new BasicNameValuePair("email",
                        CGlobals_BA.msGmail));
                nameValuePairs.add(new BasicNameValuePair("imei", CGlobals_BA.mIMEI));
                if (location != null) {
                    nameValuePairs.add(new BasicNameValuePair("lat", Double
                            .toString(location.getLatitude())));
                    nameValuePairs.add(new BasicNameValuePair("lon", Double
                            .toString(location.getLongitude())));
                }
                Calendar cal = Calendar.getInstance();
                cal.setFirstDayOfWeek(Calendar.MONDAY);

                nameValuePairs.add(new BasicNameValuePair("y", Integer
                        .toString(cal.get(Calendar.YEAR))));
                nameValuePairs.add(new BasicNameValuePair("o", Integer
                        .toString(cal.get(Calendar.MONTH))));
                nameValuePairs.add(new BasicNameValuePair("d", Integer
                        .toString(cal.get(Calendar.DAY_OF_MONTH))));
                nameValuePairs.add(new BasicNameValuePair("h", Integer
                        .toString(cal.get(Calendar.HOUR_OF_DAY))));
                nameValuePairs.add(new BasicNameValuePair("m", Integer
                        .toString(cal.get(Calendar.MINUTE))));
                nameValuePairs.add(new BasicNameValuePair("s", Integer
                        .toString(cal.get(Calendar.SECOND))));
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URL);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                sArrivingBuses = total.toString();
                SSLog_SS.i(CGlobals_BA.TAG, total.toString());
            } catch (Exception e) {
                SSLog_SS.e("LogBus: ", "Error in http connection " + e.toString());
            }
            return (long) 1;
        }

        @Override
        protected void onPostExecute(Long result) {
            Toast.makeText(mActivity, "Retrieved Arriving Buses: ",
                    Toast.LENGTH_SHORT).show();
            // displayArrivingBuses("[{busno:'35', destinationstop:'[Marol] Maroshi Depot', eta:'5 mins'}]");
            displayArrivingBuses(sArrivingBuses);
            super.onPostExecute(result);
        }
    }

    private class GetBusesNearYou extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            Calendar c = Calendar.getInstance();
            int dow = mApp.dow(c.get(Calendar.DAY_OF_WEEK));
            if (isCancelled())
                return "Canceled";
            publishProgress(15);
            try {
                maBusesAtStop.clear();
                double tm = c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 100.00;

                maBusesAtStop = mApp.mDBHelperBus.getBusesNearYou(
                        mApp.moStartStop.miStopId, dow, tm);
                publishProgress(20);

            } catch (Exception e) {
                SSLog_SS.e(TAG, e.getMessage());
            }
            return "Success";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            miProgress += progress[0];
            mProgressBar.setProgress(miProgress);
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                new getBusFrequencies().execute("");
                displayBuses(maBusesAtStop);
                layoutDone();
            } catch (Exception e) {
                SSLog_SS.e(TAG + " GetBusesAtStop: OnPostExecute - ", e.getMessage());
            }
            super.onPostExecute(result);
        }

        ;
    } // GetBusesAtStop

    private class getBusFrequencies extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {
            int sz = maBusesAtStop.size();
            int i = 1;
            try {
                for (CBus b : maBusesAtStop) {
                    b = mApp.mDBHelperBus.getFrequency(b, mDoW);
                    publishProgress(i * 40 / sz);

                }
            } catch (Exception e) {
                SSLog_SS.e(TAG + " GetBusesAtStop.DoInBackground - ", e.getMessage());
            }
            publishProgress(5);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            miProgress += progress[0];
            mProgressBar.setProgress(miProgress);
        }

        @Override
        protected void onPostExecute(String result) {
            displayBuses(maBusesAtStop);
            layoutDone();
            super.onPostExecute(result);
        }

        ;
    } // getBusFrequencies

    private class getBusesToNearestStation extends AsyncTask<String, Integer, String> {
        private String sBusesToStation;

        @Override
        protected String doInBackground(String... params) {
            Calendar c = Calendar.getInstance();
            int dow = mApp.dow(c.get(Calendar.DAY_OF_WEEK));
            double tm = c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 100.00;
//			if(mApp.mbAutoRefreshLocation && loc != null) { // Nearest station from current location
//				sBusesToStation = mApp.mDBHelper.getBusesToStation(loc.getLatitude(),
//						loc.getLongitude(),  dow, tm);
//			} else { // nearest station from selected station
            if (mApp.moStartStop.lat > 0 && mApp.moStartStop.lon > 0)
                sBusesToStation = mApp.mDBHelperBus.getBusesToStation(mApp.moStartStop.lat,
                        mApp.moStartStop.lon, dow, tm);
//			}
            publishProgress(30);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            miProgress += progress[0];
            mProgressBar.setProgress(miProgress);
        }

        @Override
        protected void onPreExecute() {
            mTvBusesToStation.setText("Getting buses to nearest station ...");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            mTvBusesToStation.setText(sBusesToStation);
            layoutDone();
            super.onPostExecute(result);
        }
    } // getBusesToNearestStation

    class Trip {
        CStop oStartStop, oDestStop;
        String msTrip;

        Trip(CStop startstop, CStop deststop, int iHourOfDay, int iMin) {
            oStartStop = startstop;
            oDestStop = deststop;

            init();
        }

        Trip(int startstopid, int ideststopid) {
            oStartStop = new CStop();
            oDestStop = new CStop();
            oStartStop = mApp.mDBHelperBus.getStopFromId(startstopid);
            oDestStop = mApp.mDBHelperBus.getStopFromId(ideststopid);
            init();
        }

        private void init() {
            msTrip = oStartStop.msStopNameDetail + "\n - " + oDestStop.msStopNameDetail;

        }
    }


} // FragFindBus
