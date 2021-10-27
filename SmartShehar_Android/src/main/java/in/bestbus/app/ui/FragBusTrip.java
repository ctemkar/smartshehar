package in.bestbus.app.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.util.ArrayList;
import java.util.Calendar;

import in.bestbus.app.CBus;
import in.bestbus.app.CConnection;
import in.bestbus.app.CGlobals_BA;
import in.bestbus.app.CStop;
import in.bestbus.app.Constants_bus;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.SSLog_SS;

// Buses from source to destination
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragBusTrip extends Fragment {

    //	private ProgressDialog mProgressDialog;
    private final static String TAG = "ActTrip: ";
    private final static double AVERAGE_WALKING_SPEED_METERS_PER_MIN = .02;
    public CGlobals_BA mApp;
    int miProgress = 0;
    ProgressBar mProgressBar;
//	private static final int ID_ROUTE = 1;
//	private static final int ID_WALKTOSTOP = 2;
//	private static final int ID_ROUTEMAP = 3;

    TextView mTvStatus, lblConnecting, lblDirect;
    TableLayout tblDirect, tblConnection;
    Activity mActivity;
    private Location mMyLocation;
    Connectivity mConnectivity;
    String sDirectStationBus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConnectivity = new Connectivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = getActivity();
        mApp = CGlobals_BA.getInstance();
        try {
            mApp.mCallHome.userPing(getString(R.string.fragFindBus), "");
        } catch (Exception e) {
            SSLog.e(TAG, "onCreateView: userPing ", e);
        }
        Bundle extras = mActivity.getIntent().getExtras();
        if (extras != null) {
            sDirectStationBus = extras.getString("DIRECT_STATION_BUS");
        }
        View v = inflater.inflate(R.layout.fragbustrip, container, false);
        setupUI(v);
        return v;
    }

    void setupUI(View v) {
        mTvStatus = (TextView) v.findViewById(R.id.tvStatus);
        tblDirect = (TableLayout) v.findViewById(R.id.tblDirectBuses);
        tblConnection = (TableLayout) v.findViewById(R.id.tblConnection);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        lblConnecting = (TextView) v.findViewById(R.id.lblConnecting);
        lblDirect = (TextView) v.findViewById(R.id.lblDirect);
        if (sDirectStationBus != null) {
            if (sDirectStationBus.equals("1")) {
                String sBusNearStationName = CGlobals_lib_ss.getInstance().getPersistentPreference(mActivity).
                        getString(Constants_bus.DIRECT_STATIONS_TO_BUSES_STNAME, "");
                lblDirect.setText(getString(R.string.directBuses) + " To " + sBusNearStationName);
            } else {
                lblDirect.setText(getString(R.string.directBuses));
            }
        } else {
            lblDirect.setText(getString(R.string.directBuses));
        }
    }

    @Override
    public void onResume() {
        try {
//			iStartId = mApp.moStartStop.miStopId;
//			iDestId = mApp.moDestStop.miStopId;
            mProgressBar.setVisibility(View.VISIBLE);
            mTvStatus.setVisibility(View.VISIBLE);
            mTvStatus.setText("Getting buses from: " + mApp.moStartStop.msStopNameDetail
                    + " to " + mApp.moDestStop.msStopNameDetail);
            mMyLocation = mApp.getMyLocation(getActivity().getApplicationContext());
            miProgress = 10;
            mProgressBar.setProgress(10);
            new GetTripBuses().execute("");
        } catch (Exception e) {
            SSLog_SS.e(TAG + " onResume - ", e.getMessage());
        }
        super.onResume();
    }

    public void updateMyLocation(Location location) {
        // Update your current location
        if (mApp.isBetterLocation(location, mApp.getMyLocation(getActivity().getApplicationContext()))) {
            mApp.mCurrentLocation = location;
        }

        return;
    } // updateMyLocation

    private void displayDirectBuses(ArrayList<CBus> aBusesAtStop) {
        tblDirect.removeAllViews();
        if (aBusesAtStop == null || aBusesAtStop.size() == 0) {
            TextView tv = new TextView(mActivity);
            mProgressBar.setVisibility(View.GONE);
            tv.setText("No Direct buses from nearby stops");
            tv.setTextColor(Color.BLACK);
            tblDirect.addView(tv);
            return;
        }
        LayoutInflater inflater = mActivity.getLayoutInflater();
        if (aBusesAtStop.size() > 0) {
            RelativeLayout llWalk = (RelativeLayout) inflater.inflate(R.layout.walktostoprow, tblDirect, false);
            TextView tvWalkToStop = (TextView) llWalk.findViewById(R.id.tvWalkToStop);
            final CStop fromStop = mApp.mDBHelperBus.getStopFromStopCode(aBusesAtStop.get(0).miFromStopCode);
            tvWalkToStop.setText(fromStop.msStopNameDetail);
            TextView tvWalkToStopDist = (TextView) llWalk.findViewById(R.id.tvWalkToStopDist);
            float[] results = new float[1];
            if (mMyLocation != null) {
                Location.distanceBetween(mMyLocation.getLatitude(),
                        mMyLocation.getLongitude(), fromStop.lat, fromStop.lon, results);
                tvWalkToStopDist.setText(Integer.toString((int) (results[0] *
                        AVERAGE_WALKING_SPEED_METERS_PER_MIN) + 1) + " mins");
                llWalk.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (!mConnectivity.connectionError(mActivity, getString(R.string.appTitle))) {
                            Intent intent = new Intent(mActivity, ActWalkToStop.class);
                            intent.putExtra("startlat", mMyLocation.getLatitude());
                            intent.putExtra("startlon", mMyLocation.getLongitude());
                            intent.putExtra("destlat", fromStop.lat);
                            intent.putExtra("destlon", fromStop.lon);
                            startActivity(intent);
                        }
                    }
                });

                tblDirect.addView(llWalk);
            }
        }
        for (final CBus oBus : aBusesAtStop) {
            RelativeLayout trd = (RelativeLayout) inflater.inflate(
                    R.layout.busrow, tblDirect, false);
            TextView tvBusNo = (TextView) trd.findViewById(R.id.tvBusNo);
            final TextView tvRoute = (TextView) trd.findViewById(R.id.tvRouteUp);
            final TextView tvFrequency = (TextView) trd
                    .findViewById(R.id.tvFreqUp);
            final TextView tvEta = (TextView) trd
                    .findViewById(R.id.tvEtaUp);

            tvBusNo.setTextColor(oBus.busColor);
            tvBusNo.setTypeface(mApp.typefaceHindi, Typeface.NORMAL);
            tvBusNo.setText(oBus.msBusLabel + "\n" + oBus.msBusNoDevanagari);
            tvBusNo.setContentDescription(oBus.msBusLabel);
            //CStop toStop = mApp.mDBHelperBus.getStopFromStopCode(oBus.miToStopCode);
            tvRoute.setText("to " + oBus.msLastStop);
            tvRoute.setContentDescription("to " + oBus.msLastStop);
            tvFrequency.setText("");
            if (!TextUtils.isEmpty(oBus.msFrequencyUp)) {
                tvFrequency.setText(oBus.msFrequencyUp + "min");
                tvFrequency.setContentDescription(oBus.msFrequencyUp + "min");
            }
            tvEta.setText("");
            if (!TextUtils.isEmpty(oBus.msEtaUp)) {
                tvEta.setText(oBus.msEtaUp);
                tvEta.setContentDescription(oBus.msEtaUp);
            }
            trd.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    String selectedBus = "Getting route for: " + oBus.msBusLabel;
                    Toast.makeText(mActivity.getApplicationContext(),
                            selectedBus, Toast.LENGTH_SHORT).show();
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        public void run() {
                            mApp.mCallHome.userPing(getString(R.string.atTITrip),
                                    oBus.msBusLabel);
                            Intent intent = new Intent(getActivity(), ActBusJourney.class);
                            if (intent != null) {
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
/*
                            ActionBar actionBar = getSherlockActivity()
									.getSupportActionBar();
							actionBar.setSelectedNavigationItem(1);
*/
                        }
                    });
                    fireTrackerEvent(getString(R.string.atTITrip) + " : "
                            + oBus.msBusLabel);
                    mApp.msRouteCode = oBus.msRouteCode;
                    mApp.msBusNo = oBus.msBusNo;
                    mApp.msBusLabel = oBus.msBusLabel;
                    mApp.mdStopLat = oBus.stopLat;
                    mApp.mdStopLon = oBus.stopLon;
                    mApp.msBusDirection = oBus.msDirection;
//					mQuickAction.show(arg0);

                }
            });
            tblDirect.addView(trd);
        }
        mProgressBar.setVisibility(View.GONE);
        mTvStatus.setVisibility(View.GONE);
    }

    private void displayConnectingBuses(ArrayList<CConnection> aConnectingBuses) {
        String msCurrentStartRouteCode = "", msCurrentDestRouteCode = "";

        tblConnection.removeAllViews();
        if (aConnectingBuses == null || aConnectingBuses.size() == 0) {
            TextView tv = new TextView(mActivity);
            mProgressBar.setVisibility(View.GONE);
            tv.setText("No Direct buses from nearby stops");
            tv.setTextColor(Color.BLACK);
            tblConnection.addView(tv);
            return;
        }
        LayoutInflater inflater = mActivity.getLayoutInflater();
        if (aConnectingBuses.size() > 0) {
            RelativeLayout llWalk = (RelativeLayout) inflater.inflate(R.layout.walktostoprow, tblConnection, false);
            TextView tvWalkToStop = (TextView) llWalk.findViewById(R.id.tvWalkToStop);
            final CStop fromStop = mApp.mDBHelperBus.getStopFromStopCode(aConnectingBuses.get(0).miStartStopcode);
            tvWalkToStop.setText(fromStop.msStopNameDetail);
            TextView tvWalkToStopDist = (TextView) llWalk.findViewById(R.id.tvWalkToStopDist);
            float[] results = new float[1];
            if (mMyLocation != null) {
                Location.distanceBetween(mMyLocation.getLatitude(),
                        mMyLocation.getLongitude(), fromStop.lat, fromStop.lon, results);
                tvWalkToStopDist.setText(Integer.toString((int) (results[0] *
                        AVERAGE_WALKING_SPEED_METERS_PER_MIN) + 1) + " mins");
                llWalk.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (!mConnectivity.connectionError(mActivity, getString(R.string.appTitle))) {
                            Intent intent = new Intent(mActivity, ActWalkToStop.class);
                            intent.putExtra("startlat", mMyLocation.getLatitude());
                            intent.putExtra("startlon", mMyLocation.getLongitude());
                            intent.putExtra("destlat", fromStop.lat);
                            intent.putExtra("destlon", fromStop.lon);
                            startActivity(intent);
                        }
                    }
                });

                tblConnection.addView(llWalk);
            }
        }
        int miCurrentStopCode = -1;
        int i = 0, j = 1;
        for (final CConnection oConnection : aConnectingBuses) {
            if (!oConnection.moBus1.msRouteCode.equals(msCurrentStartRouteCode)) {
                RelativeLayout trd = (RelativeLayout) inflater.inflate(
                        R.layout.busrow, tblConnection, false);
                TextView tvBusNo = (TextView) trd.findViewById(R.id.tvBusNo);
                final RelativeLayout rlUp = (RelativeLayout) trd.findViewById(R.id.rlUp);
                final TextView tvFrequencyUp = (TextView) trd.findViewById(R.id.tvFreqUp);
                final TextView tvRouteUp = (TextView) trd.findViewById(R.id.tvRouteUp);
                final TextView tvEtaUp = (TextView) trd.findViewById(R.id.tvEtaUp);
                final TextView tvAcTimeUp = (TextView) trd.findViewById(R.id.tvAcTimeUp);
                TextView tv = new TextView(mActivity);
                tv.setText("Option " + j);
                tv.setTextColor(Color.BLACK);
                tv.setGravity(Gravity.CENTER);
                tblConnection.addView(tv);
                j = j + 1;
                tvBusNo.setText(oConnection.moBus1.msBusLabel);
                tvBusNo.setTextColor(oConnection.moBus1.busColor);
                tvBusNo.setTypeface(mApp.typefaceHindi, Typeface.NORMAL);
                tvBusNo.setText(oConnection.moBus1.msBusLabel + "\n" + oConnection.moBus1.msBusNoDevanagari);
                tvBusNo.setContentDescription(oConnection.moBus1.msBusLabel);
                CStop oConnectionStop = mApp.mDBHelperBus.getStopFromStopCode(oConnection.miConnectionStopcode);
                tvRouteUp.setText(" to " + oConnectionStop.msStopNameDetail + " from Start");
                tvRouteUp.setContentDescription(" to " + oConnectionStop.msStopName);
                tvFrequencyUp.setText("");
                if (!TextUtils.isEmpty(oConnection.moBus1.msFrequencyUp)) {
                    tvFrequencyUp.setText(oConnection.moBus1.msFrequencyUp);
                    tvFrequencyUp.setContentDescription(oConnection.moBus1.msFrequencyUp);
                }
                tvEtaUp.setText("");
                if (!TextUtils.isEmpty(oConnection.moBus1.msEtaUp)) {
                    tvEtaUp.setText(oConnection.moBus1.msEtaUp);
                    tvEtaUp.setContentDescription(oConnection.moBus1.msEtaUp);
                }
                rlUp.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        Handler handler = new Handler();
                        handler.post(new Runnable() {
                            public void run() {
                                mApp.mCallHome.userPing(getString(R.string.atTITrip),
                                        oConnection.moBus1.msBusLabel);
                            }
                        });
                        fireTrackerEvent(getString(R.string.atTITrip) + " : "
                                + oConnection.moBus1.msBusLabel);
                        mApp.msRouteCode = oConnection.moBus1.msRouteCode;
                        mApp.msBusNo = oConnection.moBus1.msBusLabel;
                        mApp.mdStopLat = oConnection.moBus1.mdLatu;
                        mApp.mdStopLon = oConnection.moBus1.mdLonu;
                        mApp.msBusDirection = "U";
                    }
                });
                tblConnection.addView(trd);
                msCurrentStartRouteCode = oConnection.moBus1.msRouteCode;

            }
            if (miCurrentStopCode != oConnection.miDestStopcode) {
                TextView tv = new TextView(mActivity);
                tv.setText("Change bus");
                tv.setTextColor(Color.BLACK);
                tblConnection.addView(tv);
            }
            i++;
            if (!oConnection.moBus2.msRouteCode.equals(msCurrentDestRouteCode)) {
                RelativeLayout trd = (RelativeLayout) inflater.inflate(
                        R.layout.busrow, tblConnection, false);
                TextView tvBusNo = (TextView) trd.findViewById(R.id.tvBusNo);
                final RelativeLayout rlUp = (RelativeLayout) trd.findViewById(R.id.rlUp);

                final TextView tvFrequencyUp = (TextView) trd.findViewById(R.id.tvFreqUp);
                final TextView tvRouteUp = (TextView) trd.findViewById(R.id.tvRouteUp);
                final TextView tvEtaUp = (TextView) trd.findViewById(R.id.tvEtaUp);
                final TextView tvAcTimeUp = (TextView) trd.findViewById(R.id.tvAcTimeUp);
                tvBusNo.setText(oConnection.moBus2.msBusLabel);
                tvBusNo.setTextColor(oConnection.moBus2.busColor);
                tvBusNo.setTypeface(mApp.typefaceHindi, Typeface.NORMAL);
                tvBusNo.setText(oConnection.moBus2.msBusLabel + "\n" + oConnection.moBus2.msBusNoDevanagari);
                tvBusNo.setContentDescription(oConnection.moBus2.msBusLabel);
                CStop oDestinationStop = mApp.mDBHelperBus.getStopFromStopCode(oConnection.miDestStopcode);
                tvRouteUp.setText(" to " + oDestinationStop.msStopNameDetail);
                tvRouteUp.setContentDescription(" to " + oDestinationStop.msStopNameDetail);
                tvFrequencyUp.setText("");
                if (!TextUtils.isEmpty(oConnection.moBus2.msFrequencyUp)) {
                    tvFrequencyUp.setText(oConnection.moBus2.msFrequencyUp);
                    tvFrequencyUp.setContentDescription(oConnection.moBus2.msFrequencyUp);
                }
                tvEtaUp.setText("");
                if (!TextUtils.isEmpty(oConnection.moBus2.msEtaUp)) {
                    tvEtaUp.setText(oConnection.moBus2.msEtaUp);
                    tvEtaUp.setContentDescription(oConnection.moBus2.msEtaUp);
                }
                rlUp.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        Handler handler = new Handler();
                        handler.post(new Runnable() {
                            public void run() {
                                mApp.mCallHome.userPing(getString(R.string.atTITrip),
                                        oConnection.moBus2.msBusLabel);
                            }
                        });
                        fireTrackerEvent(getString(R.string.atTITrip) + " : "
                                + oConnection.moBus2.msBusLabel);
                        mApp.msRouteCode = oConnection.moBus2.msRouteCode;
                        mApp.msBusNo = oConnection.moBus2.msBusLabel;
                        mApp.mdStopLat = oConnection.moBus2.mdLatu;
                        mApp.mdStopLon = oConnection.moBus2.mdLonu;
                        mApp.msBusDirection = "U";
                    }
                });
                tblConnection.addView(trd);
            }
            msCurrentDestRouteCode = oConnection.moBus2.msRouteCode;
        }

        mProgressBar.setVisibility(View.GONE);
        mTvStatus.setVisibility(View.GONE);
    }

    public void fireTrackerEvent(String label) {
/*		AnalyticsUtils.getInstance(mActivity).trackEvent(
                getString(R.string.fragFindBusLong),
				getString(R.string.fragFindBus), label, 0);
*/
    }

    private class GetTripBuses extends AsyncTask<String, Integer, String> {
        ArrayList<CBus> aDirectBusesNear = new ArrayList<CBus>();
        ArrayList<CConnection> aConnectingBuses = new ArrayList<CConnection>();

        // ArrayList<CBus> aNearBusesAtStop = new ArrayList<CBus>();
        @Override
        protected String doInBackground(String... sUrl) {
            try {
                publishProgress(10);
                aDirectBusesNear.clear();
                Calendar c = Calendar.getInstance();
                double tm = c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 100.00;
                aDirectBusesNear.clear();
                aDirectBusesNear = getDirectBusesNear(mApp.moStartStop.lat, mApp.moStartStop.lon,
                        mApp.moDestStop.lat, mApp.moDestStop.lon,
                        mApp.dow(c.get(Calendar.DAY_OF_WEEK)), tm, Constants_bus.DIRECT_BUSES_WALKING_DISTANCE);
                int sz = aDirectBusesNear.size();
                if (sz > 0) {
                    publishProgress(50);
                    int i = 1;
                    for (CBus b : aDirectBusesNear) {
                        b = mApp.mDBHelperBus.getFrequency(b, mApp.dow(c.get(Calendar.DAY_OF_WEEK)));
                        publishProgress(i * 40 / sz);

                    }
                }
                if (sz == 0) {
                    aConnectingBuses.clear();
                    aConnectingBuses = getConnectingBuses(mApp.moStartStop.lat, mApp.moStartStop.lon,
                            mApp.moDestStop.lat, mApp.moDestStop.lon,
                            mApp.dow(c.get(Calendar.DAY_OF_WEEK)), tm, 1000);

                }

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
                //publishProgress(90);
                displayDirectBuses(aDirectBusesNear);
                if (aDirectBusesNear.size() == 0) {
                    displayConnectingBuses(aConnectingBuses);
                    lblConnecting.setVisibility(View.VISIBLE);
                    tblConnection.setVisibility(View.VISIBLE);
                } else {
                    hideConnectingBuses();
                }
                //publishProgress(100);
            } catch (Exception e) {
                SSLog_SS.e("GetTripBuses: PostExecute - ", e.getMessage());
            }
            super.onPostExecute(result);
        }

        public void hideConnectingBuses() {
            lblConnecting.setVisibility(View.GONE);
            tblConnection.setVisibility(View.GONE);
        }

        public ArrayList<CBus> getDirectBusesNear(double dStartLat, double dStartLon,
                                                  double dDestLat, double dDestLon, int iDow, double tm, double dRadialDist) {
            ArrayList<CBus> aNearBuses = new ArrayList<CBus>();
            ArrayList<CStop> aStopsNearStart;
            ArrayList<CStop> aStopsNearDest;
            aStopsNearStart = mApp.mDBHelperBus.getNearStops(dStartLat, dStartLon, dRadialDist);
            if (aStopsNearStart == null) {
                aStopsNearStart = new ArrayList<CStop>();
                if (mApp.moStartStop != null)
                    aStopsNearStart.add(mApp.moStartStop);
            }
            aStopsNearDest = mApp.mDBHelperBus.getNearStops(dDestLat, dDestLon, dRadialDist);
            if (aStopsNearDest == null) {
                aStopsNearDest = new ArrayList<CStop>();
                if (mApp.moStartStop != null)
                    aStopsNearDest.add(mApp.moDestStop);
            }
            int iNoBuses = 0;
            int sz = aStopsNearStart.size();
            int i = 0;
            for (CStop sn : aStopsNearStart) {
                for (CStop sd : aStopsNearDest) {
                    ArrayList<CBus> aNB = mApp.mDBHelperBus.getDirectBuses(sn.miStopId, sd.miStopId, iDow, tm);
                    for (CBus nb : aNB) {
                        aNearBuses.add(nb);
                        iNoBuses++;
                    }
                    if (iNoBuses > 0)
                        return aNearBuses;
                }
                if (++i % sz > 0)
                    publishProgress(1);
            }

            return aNearBuses;
        }

        public ArrayList<CConnection> getConnectingBuses(double dStartLat, double dStartLon,
                                                         double dDestLat, double dDestLon, int iDow, double tm, double dRadialDist) {
            ArrayList<CStop> aStopsNearStart;
            ArrayList<CStop> aStopsNearDest;
            aStopsNearStart = mApp.mDBHelperBus.getNearStops(dStartLat, dStartLon, dRadialDist);
            if (aStopsNearStart == null) {
                aStopsNearStart = new ArrayList<CStop>();
                if (mApp.moStartStop != null)
                    aStopsNearStart.add(mApp.moStartStop);
            }
            aStopsNearDest = mApp.mDBHelperBus.getNearStops(dDestLat, dDestLon, dRadialDist);
            if (aStopsNearDest == null) {
                aStopsNearDest = new ArrayList<CStop>();
                if (mApp.moStartStop != null)
                    aStopsNearDest.add(mApp.moDestStop);
            }
            int iNoBuses = 0;
            int sz = aStopsNearStart.size();
            int i = 0;
            for (CStop sn : aStopsNearStart) {
                for (CStop sd : aStopsNearDest) {
                    ArrayList<CConnection> aNB =
                            mApp.mDBHelperBus.getConnectingBuses(sn.miStopCode, sd.miStopCode,
                                    sn.lat, sn.lon,
                                    sd.lat, sd.lon, iDow, tm);
                    for (CConnection nb : aNB) {
                        aConnectingBuses.add(nb);
                        iNoBuses++;
                    }
                    if (iNoBuses > 3)
                        return aConnectingBuses;
                }
                if (++i % sz > 0)
                    publishProgress(1);
            }

            return aConnectingBuses;
        }


    }

}
