package in.bestbus.app.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import in.bestbus.app.BusListAdapter;
import in.bestbus.app.CBusJourney;
import in.bestbus.app.CDirection;
import in.bestbus.app.CGlobals_BA;
import in.bestbus.app.Journey;
import in.bestbus.app.JourneyAdapter;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragBusJourney extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final String TAG = "FragBusJourney: ";
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 3;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private final String URL = CGlobals_BA.PHP_PATH + "/updatebuslocation.php";
    Spinner startSpinner, destSpinner;
    Spinner mLineSpinner, mSpeedSpinner;
    ArrayAdapter<String> mLineSpinnerAdapter, mSpeedSpinnerAdapter;
    boolean firstTimeStartSpinner = true, firstTimeDestSpinner = true;
    ArrayList<String> aStation;
    ArrayList<CDirection> aTowards;
    ArrayList<Journey> journey_data;
    Cursor stationListCursor;
    LinearLayout llJourney;
    LinearLayout mllStartStation, mllStartWheel; //, mllStartTimeWheel;
    JourneyAdapter journeyListAdapter;
    String sTowardsStation = "";
    Cursor busListCursor;
    ImageView mBtnClear;
    int iGroupIdx;
    String sStartSerial, sEndSerial;
    boolean bInUpdate = false;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient = null;
    private CGlobals_BA mApp = null;
    private AutoCompleteTextView busView;
    private RadioGroup mRadioDirection;
    private RadioButton mRadioUp, radioDn;
    private ListView listJourney;
    private String sLoggingMode;
    private String sBusNo = "";
    private View mFragView;
    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        setHasOptionsMenu(true);
    }

    /*@Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menufragfind, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }*/

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_list) {
            *//*if (getActivity() != null)
                Toast.makeText(getActivity(), "Getting location...", Toast.LENGTH_SHORT).show();*//*
        } else if (item.getItemId() == R.id.menu_map) {
            if (getActivity() != null)
                Toast.makeText(getActivity(), "Refreshing ...", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mApp = CGlobals_BA.getInstance();
        try {
            mApp.mCallHome.userPing(getString(R.string.fragFindBus), "");
        } catch (Exception e) {
            SSLog.e(TAG, "onCreateView: ", e);
        }
        mLocationRequest = LocationRequest.create();
        mActivity = getActivity();
        // Use high accuracy
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        mFragView = inflater.inflate(R.layout.fragjourney, container, false);
        listJourney = (ListView) mFragView.findViewById(R.id.tblJourney);
        llJourney = (LinearLayout) mFragView.findViewById(R.id.llJourney);
        busView = (AutoCompleteTextView) mFragView.findViewById(R.id.autocomplete_busno);
        mBtnClear = (ImageView) mFragView.findViewById(R.id.btnClear);
        mRadioDirection = (RadioGroup) mFragView.findViewById(R.id.radioDirection);
        mRadioUp = (RadioButton) mFragView.findViewById(R.id.radioUp);
        radioDn = (RadioButton) mFragView.findViewById(R.id.radioDn);
        mRadioUp.setSelected(false);
        radioDn.setSelected(false);
        return mFragView;
    }

    @Override
    public void onStart() {
        super.onStart();

    } // onStart


    @Override
    public void onResume() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!TextUtils.isEmpty(mApp.msRouteCode) && !TextUtils.isEmpty(mApp.msBusNo)) {
            setBusNo(mApp.msBusNo);
            showJourney(mApp.msRouteCode);
        }
        super.onResume();
        setupUI();
    }

    void setupUI() {
        llJourney.setVisibility(View.VISIBLE);
        try {
            busListCursor = mApp.mDBHelperBus.getBusNosCursor("");
        } catch (Exception e) {
            SSLog.e(TAG, "setupUI: ", e);
        }

        BusListAdapter startAdapter =
                new BusListAdapter(getActivity(), busListCursor, mApp);

        busView.setAdapter(startAdapter);
        busView.invalidate();
        busView.clearFocus();
        busView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getActivity(), ActSearchBus.class);
                startActivityForResult(i, 1);
            }
        });
        mBtnClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                busView.setText("");
            }
        });
        mRadioUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mRadioUp.post(new Runnable() {
                    @Override
                    public void run() {
                        mApp.msBusDirection = "U";
                        showJourney(mApp.msRouteCode);
                    }
                });
            }
        });
        radioDn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                radioDn.post(new Runnable() {
                    @Override
                    public void run() {
                        mApp.msBusDirection = "D";
                        showJourney(mApp.msRouteCode);
                    }
                });
            }
        });

        busView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // your code here....
                busView.setThreshold(0);
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return false;
            }
        });

        busView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                keyboardShowHide(false);
                busView.setThreshold(1000);
                busView.post(new Runnable() {
                    @Override
                    public void run() {
                        setBusNo(busView.getText().toString());
                        busView.dismissDropDown();
                        busView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager keyboard = (InputMethodManager)
                                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                keyboard.hideSoftInputFromWindow(busView.
                                        getWindowToken(), 0);
                            }
                        }, 200);
                    }

                });
///				mTvStatus.setVisibility(View.VISIBLE);

            }
        });
        setBusNo(mApp.msBusNo);
        showJourney(mApp.msRouteCode);
    }

    private void setBusNo(String busno) {
        mRadioDirection.clearCheck();
        if (mApp.msBusDirection.equals("U"))
            mRadioDirection.check(R.id.radioUp);
        else
            mRadioDirection.check(R.id.radioDn);

        String sRouteCode = mApp.mDBHelperBus.getRouteCodeFromBusNo(busno);
        if (sRouteCode != null) {
            busView.setThreshold(1000);
            busView.setText(busno);
            busView.dismissDropDown();
            showJourney(sRouteCode);
            mApp.msRouteCode = sRouteCode;

        }
    }

    private void showJourney(String sRouteCode) {
        mApp.mCallHome.userPing(getString(R.string.atBusJourney), "");

        if (sRouteCode == null)
            return;
        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.MONDAY);

        ArrayList<CBusJourney> aBusJourney = mApp.mDBHelperBus.getBusJourney(sRouteCode,
                mApp.msBusDirection, dow(c.get(Calendar.DAY_OF_WEEK)),
                c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE) / 100.00);
        int len = aBusJourney.size();
        if (len == 0)
            return;
        CBusJourney cb;
        journey_data = new ArrayList<Journey>();
        Location loc = mApp.getMyLocation(getActivity().getApplicationContext());
        float[] res = new float[1];
        double dist = -1;
        int nearestStopSerial = -1;
        for (int i = 0; i < len; i++) {
            cb = aBusJourney.get(i);
            Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                    cb.mdLat, cb.mdLon, res);

            if ((int) res[0] < dist || dist == -1) {
                dist = (int) res[0];
                nearestStopSerial = i;
            }
            journey_data.add(new Journey(Integer.toString(i + 1),
                    cb.msStopnameDetail, cb.msLandmarkList, Color.WHITE,
                    cb.mdLat, cb.mdLon, cb.mistopCode));

        }
        if (nearestStopSerial >= 0) {
            CGlobals_BA.iNearestStopIndex = nearestStopSerial;
            Journey j = journey_data.get(nearestStopSerial);
            j.color = Color.CYAN;
        }
        String buslable = busView.getText().toString();
        journeyListAdapter = new JourneyAdapter(getActivity(),
                R.layout.journeyrow, journey_data, buslable, mApp.msBusDirection);
        listJourney.setAdapter(journeyListAdapter);
        listJourney.setSelection(nearestStopSerial);
        if (mApp.msBusDirection.equals("U")) {
            mRadioUp.setText("Up to " + journey_data.get(journey_data.size() - 1).stopnameDetail);
            radioDn.setText("Down to " + journey_data.get(0).stopnameDetail);
        } else if (mApp.msBusDirection.equals("D")) {
            mRadioUp.setText("Up to " + journey_data.get(0).stopnameDetail);
            radioDn.setText("Down to " + journey_data.get(journey_data.size() - 1).stopnameDetail);
        }

    } // showTrainsAtStation


    private int dow(int iCDow) {
        return iCDow == 1 ? 7 : iCDow - 1;
    }

    protected void updateMyLocation(Location location) {
        // Update your current location
        mApp.mCurrentLocation = location;
        int len = 0;
        Journey j;
        float[] res = new float[1];
        double dist = -1;
        int nearestStopSerial = -1;
        // Refresh the ArrayList of stops
        if (location != null) {
            if (sLoggingMode != null) {
                if (sLoggingMode.equals(CGlobals_BA.DRIVER))
                    if (!bInUpdate)
                        new UpdateBusLocation().execute(mApp.msRouteCode);
            }
            if (journey_data != null) {
                len = journey_data.size();
                for (int i = 0; i < len; i++) {
                    j = journey_data.get(i);
//					j.color = Color.BLUE;
                    Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                            j.mdLat, j.mdLon, res);
                    if ((int) res[0] < dist || dist == -1) {
                        dist = (int) res[0];
                        nearestStopSerial = i;
                    }

                }
                j = journey_data.get(nearestStopSerial);
                j.color = Color.CYAN;
                CGlobals_BA.iNearestStopIndex = nearestStopSerial;
                journeyListAdapter.notifyDataSetChanged();
            }
//			showJourney(mApp.msRouteCode);
//			refreshListView();
        }

    }

    public void hideFragment() {
        llJourney.setVisibility(View.GONE);
    }

    public void keyboardShowHide(boolean bShow) {
        if (bShow) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            View cFocus = getActivity().getCurrentFocus();
            if (imm != null && cFocus != null)
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            else
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(getActivity(), "Cannot get location. \nPlease turn on internet and/or GPS", Toast.LENGTH_SHORT).show();
//		mApp.showGPSDisabledAlertToUser(getActivity());
    }

    @Override
    public void onConnected(Bundle arg0) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mApp.mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mApp.mCurrentLocation != null) {
            startPeriodicUpdates();
            // Toast.makeText(getActivity(), "Got Location!", Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(getActivity(), "Cannot get location. Please turn on GPS", Toast.LENGTH_SHORT).show();
//    		mApp.showGPSDisabledAlertToUser(getActivity());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mApp.mbAutoRefreshLocation && location != null)
            updateMyLocation(CGlobals_lib_ss.getInstance().getMyLocation(getActivity()));
    }

    private void startPeriodicUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == getActivity().RESULT_OK) {
//		         String result=data.getStringExtra("result");
                String sRouteCode = ActSearchBus.sRouteCode;
                mApp.msBusNo = ActSearchBus.sBusNo;
                busView.setText(ActSearchBus.sBusLable);
                showJourney(sRouteCode);

            }
            if (resultCode == getActivity().RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private class UpdateBusLocation extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... sbusno) {
            bInUpdate = true;
            try {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                Location location = mApp.getMyLocation(getActivity().getApplicationContext());
                nameValuePairs.add(new BasicNameValuePair("licenseno", CGlobals_BA.mIMEI));
                nameValuePairs.add(new BasicNameValuePair("busno", sBusNo));
                nameValuePairs.add(new BasicNameValuePair("startserial", sStartSerial));
                nameValuePairs.add(new BasicNameValuePair("endserial", sEndSerial));


                nameValuePairs.add(new BasicNameValuePair("routecode", mApp.msRouteCode));
                nameValuePairs.add(new BasicNameValuePair("email", CGlobals_BA.msGmail));

                nameValuePairs.add(new BasicNameValuePair("imei", CGlobals_BA.mIMEI));
                if (location != null) {
                    nameValuePairs.add(new BasicNameValuePair("lat", Double.toString(location.getLatitude())));
                    nameValuePairs.add(new BasicNameValuePair("lon", Double.toString(location.getLongitude())));
                }
                Calendar cal = Calendar.getInstance();
                cal.setFirstDayOfWeek(Calendar.MONDAY);

                nameValuePairs.add(new BasicNameValuePair("y", Integer.toString(cal.get(Calendar.YEAR))));
                nameValuePairs.add(new BasicNameValuePair("o", Integer.toString(cal.get(Calendar.MONTH))));
                nameValuePairs.add(new BasicNameValuePair("d", Integer.toString(cal.get(Calendar.DAY_OF_MONTH))));
                nameValuePairs.add(new BasicNameValuePair("h", Integer.toString(cal.get(Calendar.HOUR_OF_DAY))));
                nameValuePairs.add(new BasicNameValuePair("m", Integer.toString(cal.get(Calendar.MINUTE))));
                nameValuePairs.add(new BasicNameValuePair("s", Integer.toString(cal.get(Calendar.SECOND))));
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URL);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                SSLog_SS.i(CGlobals_BA.TAG, is.toString());
            } catch (Exception e) {
                SSLog_SS.e("LogBus: ", "Error in http connection " + e.toString());
            }
            return (long) 1;
        }

        @Override
        protected void onPostExecute(Long result) {
            bInUpdate = false;

            super.onPostExecute(result);
        }
    }

}


