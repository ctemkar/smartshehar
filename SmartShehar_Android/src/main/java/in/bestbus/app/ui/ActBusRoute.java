package in.bestbus.app.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.bestbus.app.CBusJourney;
import in.bestbus.app.CGlobals_BA;
import in.bestbus.app.CSpeed;
import in.bestbus.app.Constants_bus;
import in.bestbus.app.Journey;
import in.bestbus.app.JourneyAdapter;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.SSLog_SS;

public class ActBusRoute extends AppCompatActivity {
    private final String TAG = "ActBusRoute: ";
    private final String URL = CGlobals_BA.PHP_PATH + "/updatebuslocation.php";
    CGlobals_BA mApp;
    ProgressBar mProgressBar;
    Location myLocation;
    Cursor busListCursor;
    ArrayList<Journey> journey_data;

    LinearLayout llJourney;
    LinearLayout mllStartStation, mllStartWheel; //, mllStartTimeWheel;
    JourneyAdapter journeyListAdapter;
    String sStartSerial, sEndSerial;
    boolean bInUpdate = false;
    ImageView mBtnClear;
    Button mBtnSchedule;
    private ListView listJourney;
    private WebView wvBusRouteMap;
    private String sLoggingMode;
    private String sBusNo = "";
    private AutoCompleteTextView busView;
    private RadioGroup mRadioDirection;
    private RadioButton mRadioUp, radioDn;
    ImageView tvEtaBus;
    TextView tvEtaShow;
    Connectivity mConnectivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_busroute);
        mConnectivity = new Connectivity();
        tvEtaBus = (ImageView) findViewById(R.id.tvEtaBus);
        tvEtaShow = (TextView) findViewById(R.id.tvEtaShow);
        mApp = CGlobals_BA.getInstance();
        mApp.init(this);
        mApp.mCallHome.userPing(getString(R.string.pageRoute), "");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    } // onCreate

    @Override
    public void onResume() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setupUI();
        if (!TextUtils.isEmpty(mApp.msRouteCode)) {
            setBusNo(mApp.msBusNo);
        }
        resetButtonColors(Constants_bus.BUS_STOP);
        super.onResume();
        ActionBar ab = getSupportActionBar();
        ab.setTitle("Bus " + mApp.msBusNo);
        findViewById(R.id.tvBusList).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtonColors(Constants_bus.BUS_STOP);
                if (!TextUtils.isEmpty(mApp.msRouteCode)) {
                    wvBusRouteMap.setVisibility(View.GONE);
                    listJourney.setVisibility(View.VISIBLE);
                    setBusNo(mApp.msBusNo);
                }
            }
        });

        findViewById(R.id.tvBusMap).setOnClickListener(new OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                listJourney.setVisibility(View.GONE);
                wvBusRouteMap.setVisibility(View.VISIBLE);
                resetButtonColors(Constants_bus.BUS_MAP);
                wvBusRouteMap.getSettings().setJavaScriptEnabled(true);
                wvBusRouteMap.getSettings().setLoadWithOverviewMode(true);
                wvBusRouteMap.getSettings().setUseWideViewPort(true);
                wvBusRouteMap.getSettings().setGeolocationEnabled(true);
                wvBusRouteMap.getSettings().setBuiltInZoomControls(true);
                wvBusRouteMap.getSettings().setDisplayZoomControls(true);
                wvBusRouteMap.getSettings().setSupportZoom(true);
                wvBusRouteMap.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                wvBusRouteMap.setWebChromeClient(new WebChromeClient());
                wvBusRouteMap.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }

                    @Override
                    public void onPageFinished(WebView view, final String url) {
                    }
                });
                wvBusRouteMap.loadUrl("http://smartshehar.com/alpha/smartsheharapp/v17/busroute.html?routecode=" + mApp.msRouteCode);
            }
        });
    }

    public void resetButtonColors(String sTripType) {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.thin_frame, null);
        Drawable drawableSelected = ResourcesCompat.getDrawable(getResources(), R.drawable.frame_selected, null);
        findViewById(R.id.tvBusList).setBackgroundDrawable(drawable);
        findViewById(R.id.tvBusMap).setBackgroundDrawable(drawable);
        if (sTripType.equals(Constants_bus.BUS_STOP)) {
            findViewById(R.id.tvBusList).setBackgroundDrawable(drawableSelected);
        }
        if (sTripType.equals(Constants_bus.BUS_MAP)) {
            findViewById(R.id.tvBusMap).setBackgroundDrawable(drawableSelected);
        }
    }

    void setupUI() {
        listJourney = (ListView) findViewById(R.id.tblJourney);
        wvBusRouteMap = (WebView) findViewById(R.id.wvBusRouteMap);
       // llJourney.setVisibility(View.VISIBLE);
        busListCursor = mApp.mDBHelperBus.getBusNosCursor("");
        busView.invalidate();
        busView.clearFocus();
        busView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(ActBusRoute.this, ActSearchBus.class);
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

        tvEtaBus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String buslable = busView.getText().toString();
                if (TextUtils.isEmpty(buslable)) {
                    Toast.makeText(ActBusRoute.this, "No ETA", Toast.LENGTH_LONG).show();
                } else {
                    if (!mConnectivity.checkConnected(ActBusRoute.this)) {
                        if (!mConnectivity.connectionError(ActBusRoute.this, getString(R.string.appTitle))) {
                            showEtaOfBusNumber(buslable);
                        }
                    }
                }
            }
        });

        setBusNo(mApp.msBusNo);
        showJourney(mApp.msRouteCode);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onStart() {
        listJourney = (ListView) findViewById(R.id.tblJourney);
        llJourney = (LinearLayout) findViewById(R.id.llJourney);
        busView = (AutoCompleteTextView) findViewById(R.id.autocomplete_busno);
        mBtnClear = (ImageView) findViewById(R.id.btnClear);
        mBtnSchedule = (Button) findViewById(R.id.btnSchedule);
        mRadioDirection = (RadioGroup) findViewById(R.id.radioDirection);
        mRadioUp = (RadioButton) findViewById(R.id.radioUp);
        radioDn = (RadioButton) findViewById(R.id.radioDn);
        mRadioUp.setSelected(false);
        radioDn.setSelected(false);
        super.onStart();
    } // onStart

    @Override
    public void onStop() {
        super.onStop();
    }

    public void keyboardShowHide(boolean bShow) {
        if (bShow) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            View cFocus = getCurrentFocus();
            if (imm != null && cFocus != null)
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            else
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        }
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
        Location loc = mApp.getMyLocation(ActBusRoute.this);
        float[] res = new float[1];
        double dist = -1;
        int nearestStopSerial = -1;
        for (int i = 0; i < len; i++) {
            cb = aBusJourney.get(i);
            if (loc != null) {
                Location.distanceBetween(loc.getLatitude(), loc.getLongitude(),
                        cb.mdLat, cb.mdLon, res);

                if ((int) res[0] < dist || dist == -1) {
                    dist = (int) res[0];
                    nearestStopSerial = i;
                }
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
        journeyListAdapter = new JourneyAdapter(ActBusRoute.this,
                R.layout.journeyrow, journey_data, buslable, mApp.msBusDirection);
        journeyListAdapter.notifyDataSetChanged();
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
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
//		         String result=data.getStringExtra("result");
                String sRouteCode = data.getStringExtra("routecode");
                mApp.msBusNo = data.getStringExtra("busno");
                busView.setText(data.getStringExtra("buslabel"));
                showJourney(sRouteCode);

            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private class UpdateBusLocation extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... sbusno) {
            bInUpdate = true;
            try {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                Location location = mApp.getMyLocation(ActBusRoute.this);
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
                SSLog_SS.e(TAG, "Error in http connection " + e.toString());
            }
            return (long) 1;
        }

        @Override
        protected void onPostExecute(Long result) {
            bInUpdate = false;
            super.onPostExecute(result);
        }
    }

    ArrayList<CSpeed> aDriverSpeed = new ArrayList<CSpeed>();
    int iCurrentStopCode;
    double busCurrentLat, busCurrentLng, speed;
    String busCLable, tripstatus;
    int fromStopSerial, toStopSerial;
    double iMyDistanceUp, iMyDistanceDown;
    String sLastAccess = "";

    private void showEtaOfBusNumber(final String buslable) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_bus.GET_BUS_LOCATION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        if (TextUtils.isEmpty(response)) {
                            Toast.makeText(ActBusRoute.this, "No ETA", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (response.equals("-1")) {
                            Toast.makeText(ActBusRoute.this, "No ETA", Toast.LENGTH_LONG).show();
                            return;
                        }

                        try {
                            JSONArray aJson = new JSONArray(response);
                            for (int i = 0; i < aJson.length(); i++) {
                                JSONObject person = (JSONObject) aJson.get(i);
                                busCurrentLat = person.isNull("lat") ? -999 : person
                                        .getDouble("lat");
                                busCurrentLng = person.isNull("lng") ? -999 : person
                                        .getDouble("lng");
                                busCLable = person.isNull("buslabel") ? "" : person
                                        .getString("buslabel");
                                fromStopSerial = person.isNull("fromstopserial") ? 0 : person
                                        .getInt("fromstopserial");
                                toStopSerial = person.isNull("tostopserial") ? 0 : person
                                        .getInt("tostopserial");
                                tripstatus = person.isNull("tripstatus") ? "" : person
                                        .getString("tripstatus");

                                speed = person.isNull("speed") ? -999 : person
                                        .getDouble("speed");
                                sLastAccess = person
                                        .isNull("clientaccessdatetime") ? ""
                                        : person
                                        .getString("clientaccessdatetime");

                                try {
                                    if (tripstatus.equals("B")) {
                                        iMyDistanceDown = CGlobals_BA.getInstance().mDBHelperBus.
                                                getNearCurrentBusStopsUp(busCurrentLat,
                                                        busCurrentLng, busCLable,
                                                        fromStopSerial, toStopSerial, iCurrentStopCode, "D");
                                        if (iMyDistanceDown == 0.0) {
                                            Toast.makeText(ActBusRoute.this, "No ETA", Toast.LENGTH_LONG).show();
                                        } else {
                                            tvEtaShow.setText(addSpeed(speed, sLastAccess, iMyDistanceDown) + "\n" +
                                                    CGlobals_BA.getInstance().getDistanceText((float) iMyDistanceDown * 1000));
                                        }
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(ActBusRoute.this, "No ETA", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(ActBusRoute.this, "No ETA", Toast.LENGTH_LONG).show();
                            SSLog_SS.e(TAG + " busLocation", e.getMessage());
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    SSLog_SS.e(TAG + " callBusTrip", error.toString());
                } catch (Exception e) {
                    SSLog_SS.e(TAG + " callBusTrip", e.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("buslabel", buslable);
                if (mApp.msBusDirection.equals("D")) {
                    params.put("direction", "D");
                } else if (mApp.msBusDirection.equals("U")) {
                    params.put("direction", "U");
                }
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        Constants_bus.GET_BUS_LOCATION_URL, ActBusRoute.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_bus.GET_BUS_LOCATION_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog_SS.e(TAG + " callBusTrip", e.getMessage());
                }
                return CGlobals_BA.getInstance().checkParams(params);
            }
        };
        CGlobals_BA.getInstance().getRequestQueue(ActBusRoute.this).add(postRequest);
    }

    private String addSpeed(double speed, String lastAccess, double dDistanceFromMe) {
        Calendar now = Calendar.getInstance();
        double dTotalSpeed = 0.0, dAvgSpeed = 0.0;
        int i, j = 0, eTA = 0, eTA1 = 0, eTA2 = 0;
        CSpeed cSpeed = new CSpeed(speed, lastAccess);
        aDriverSpeed.add(cSpeed);
        Date lastDriverLocationTime = aDriverSpeed.get(aDriverSpeed.size() - 1).getTime();
        Date firstDriverLocationTime = aDriverSpeed.get(0).getTime();
        long dif = (lastDriverLocationTime.getTime() - firstDriverLocationTime.getTime()) / 1000;
        //if (dif >= 20) {
        for (i = aDriverSpeed.size() - 1; i > 0; i--) {
            if (aDriverSpeed.get(i).getSpeed() > 0.83) {
                dTotalSpeed += aDriverSpeed.get(i).getSpeed();
                j++;
            }
        }
        dAvgSpeed = (dTotalSpeed / j) * 3.6;
        Log.d("AVGSpeed: ", String.valueOf(dAvgSpeed));
        if (dDistanceFromMe > 0) {
            eTA1 = (int) ((dDistanceFromMe / dAvgSpeed) * 60);
            if (eTA1 < 1) {
                eTA2 = (int) (((dDistanceFromMe / dAvgSpeed) * 60) * 60);
            } else {
                eTA = eTA1;
            }
        }
        now.add(Calendar.MINUTE, eTA);
        now.add(Calendar.SECOND, eTA2);
        String newTimeString = String.format("%1$tH:%1$tM", now);
        return newTimeString;
    }
}
