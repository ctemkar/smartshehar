package in.bestbus.app.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import in.bestbus.app.CDirection;
import in.bestbus.app.CGlobals_BA;
import in.bestbus.app.CStop;
import lib.app.util.SSLog_SS;

public class ActLogBus<ActFind> extends AppCompatActivity {
    private final String URL = CGlobals_BA.PHP_PATH + "/updatebuslocation.php";
    ArrayList<String> masRecentStops;
    Spinner mStartSpinner;
    ArrayAdapter<String> routeSpinnerAdapter;
    boolean firstTimeStartSpinner = true;
    ArrayList<String> maStation;
    ArrayList<String> masRoutes, masAll, masLtd, masAS, masACExp, masCor;
    ArrayList<CDirection> aTowards;
    ProgressDialog mProgressDialog;
    Cursor busesCursor;
    LinearLayout mllStartStation;
    Button mBtnClear;
    Button mBtnLogBus;
    TextView mTvStatus;
    boolean mbICS = false;
    ArrayList<String> aBusRoute;
    ArrayList<String> aBusSerial;
    String sStartSerial, sEndSerial;
    String sBusNoView;
    private CStop moStartStop = null;
    private CGlobals_BA mApp = null;
    private AutoCompleteTextView mBusNoView;
    private Spinner mSpinnerStart, mSpinnerEnd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actlogbus);

        ActionBar ab = getSupportActionBar();
        ab.setIcon(R.mipmap.ic_bus);
        ab.setHomeButtonEnabled(true);

        mApp = CGlobals_BA.getInstance();
        mApp.init(this);
        mApp.mCallHome.userPing(getString(R.string.pageLogBus), "");
//      	AnalyticsUtils.getInstance(this).trackPageView(getString(R.string.pageLogBusLong)); 


        masRoutes = mApp.mDBHelperBus.getBuses();
        masAll = mApp.mDBHelperBus.getBuses();
        routeSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, masRoutes);
        mBusNoView = (AutoCompleteTextView) findViewById(R.id.autocomplete_busno);
        mBusNoView.setAdapter(routeSpinnerAdapter);
        mTvStatus = (TextView) findViewById(R.id.tvStatus);
        mSpinnerStart = (Spinner) findViewById(R.id.spinnerStart);
        mSpinnerEnd = (Spinner) findViewById(R.id.spinnerEnd);
        mBtnLogBus = (Button) findViewById(R.id.btnLogBus);

        setupUI();
    } // onCreate

    @Override
    public void onResume() {
        keyboardShowHide(false);

        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        //EasyTracker.getInstance(this).activityStart(this);  // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        //EasyTracker.getInstance(this).activityStop(this);  // Add this method.
    }

    void setupUI() {
        if (mApp == null) {
        }

        mBusNoView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // your code here....
                mBusNoView.setThreshold(0);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                return false;
            }
        });

        mBusNoView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                keyboardShowHide(false);
                mBusNoView.setThreshold(1000);
                mBusNoView.post(new Runnable() {
                    @Override
                    public void run() {
                        mBusNoView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                InputMethodManager keyboard = (InputMethodManager)
                                        getSystemService(Context.INPUT_METHOD_SERVICE);
                                keyboard.hideSoftInputFromWindow(mBusNoView.
                                        getWindowToken(), 0);
                                if (aBusRoute != null)
                                    aBusRoute.clear();
                                if (aBusSerial != null)
                                    aBusSerial.clear();
                                String sRouteCode = mApp.mDBHelperBus.getRouteCodeFromBusNo(mBusNoView.getText().toString());
                                aBusRoute = mApp.mDBHelperBus.getBusRoute(sRouteCode);
                                mApp.msBusNo = mBusNoView.getText().toString();
                                mApp.msRouteCode = sRouteCode;
                                aBusSerial = mApp.mDBHelperBus.getBusSerial(sRouteCode);
                                ArrayAdapter<String> startAdapter = new ArrayAdapter<String>(ActLogBus.this,
                                        android.R.layout.simple_spinner_dropdown_item, aBusRoute);
                                ArrayAdapter<String> endAdapter = new ArrayAdapter<String>(ActLogBus.this,
                                        android.R.layout.simple_spinner_dropdown_item, aBusRoute);
                                mSpinnerStart.setAdapter(startAdapter);
                                mSpinnerEnd.setAdapter(endAdapter);
                                mSpinnerEnd.setSelection(aBusRoute.size() - 1);
                            }
                        }, 10);
                    }
                });
                mTvStatus.setVisibility(View.VISIBLE);

            }
        });
        mSpinnerStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                if (aBusSerial.size() > position)
                    sStartSerial = aBusSerial.get(position);
            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        mSpinnerEnd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                if (aBusSerial.size() > position)
                    sEndSerial = aBusSerial.get(arg0.getSelectedItemPosition());
            }

            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });


        mBtnLogBus.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sBusNoView = mBusNoView.getText().toString();
                new LogBusToServer().execute(sBusNoView);
                String sBusNo = mBusNoView.getText().toString();
                boolean inValidData = false;
                if (TextUtils.isEmpty(sBusNo)) {
                    Toast.makeText(ActLogBus.this, "Please enter a bus no: " + mBusNoView.getText().toString(),
                            Toast.LENGTH_SHORT).show();
                    inValidData = true;
                }

                if (!inValidData) {
                    Toast.makeText(ActLogBus.this, "Started logging bus: " + mBusNoView.getText().toString(),
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ActLogBus.this, ActBusJourney.class);
                    String sRouteCode = mApp.mDBHelperBus.getBusRouteCodeFromBusNo(mBusNoView.getText().toString());
                    intent.putExtra("routecode", sRouteCode);
                    intent.putExtra("startserial", sStartSerial);
                    intent.putExtra("endserial", sEndSerial);
                    intent.putExtra("busno", mBusNoView.getText().toString());
                    intent.putExtra("loggingmode", CGlobals_BA.DRIVER);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        final RadioButton radio_all = (RadioButton) findViewById(R.id.radio_all);
        final RadioButton radio_limited = (RadioButton) findViewById(R.id.radio_limited);
        final RadioButton radio_as = (RadioButton) findViewById(R.id.radio_as);
        final RadioButton radio_acexp = (RadioButton) findViewById(R.id.radio_acexp);
        final RadioButton radio_cor = (RadioButton) findViewById(R.id.radio_cor);
        OnClickListener radio_listener = new OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks
                RadioButton rb = (RadioButton) v;
                masRoutes.clear();
                String rt = rb.getText().toString();
                for (String s : masAll) {
                    if (rt.equalsIgnoreCase("all")) {
                        masRoutes.add(s);
                    } else {
                        if (rt.equals("C") && s.substring(0, 1).equals("C"))
                            masRoutes.add(s);
                        if (rt.equals("Ltd") && s.indexOf("LTD") > -1)
                            masRoutes.add(s);
                    }
                }
                ActLogBus.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        routeSpinnerAdapter.notifyDataSetChanged();

                    }
                });
            }
        };

        radio_all.setOnClickListener(radio_listener);
        radio_limited.setOnClickListener(radio_listener);
        radio_as.setOnClickListener(radio_listener);
        radio_acexp.setOnClickListener(radio_listener);
        radio_cor.setOnClickListener(radio_listener);

        mBusNoView.setThreshold(1000);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    } // setupUI ends

    void refreshLocation(Location location) {

    }
/*
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.menutrainhome, menu);

		// Calling super after populating the menu is necessary here to ensure
		// that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
//			Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
Intent intent = new Intent(this, ActBusDashboard.class);
startActivity(intent);
ActLogBus.this.finish();
    return true;			

		case R.id.menu_refresh:
			Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
//			getActionBarHelper().setRefreshActionItemState(true);
//			setProgressBarIndeterminateVisibility (Boolean.TRUE);
			getWindow().getDecorView().postDelayed(new Runnable() {
				@Override
				public void run() {
					setupUI();
					setProgressBarIndeterminateVisibility (Boolean.FALSE);
				}
			}, 500);
			break;


		}
		return super.onOptionsItemSelected(item);
	}

*/

    @Override
    public void finish() {
        if (mApp == null)
            SSLog_SS.e("BusApp: - ", "mApp is Null");
        else if (mApp.stackActivity == null)
            SSLog_SS.e("BusApp: - ", "stackActivity is Null");
        else {
            setResult(RESULT_CANCELED);
            super.finish();
        }
    }


    /*void setStartStop(String sStartStopFull) {

        CStop retStop = mApp.mDBHelperBus.getStopFromSearchStr(sStartStopFull);
        if (retStop != null) {
            moStartStop = retStop;

            mBusNoView.setThreshold(1000);
            mBusNoView.setText(moStartStop.msStopNameDetail);
            mBusNoView.setText(sStartStopFull);
            mTvStatus.setVisibility(View.GONE);
            mBusNoView.dismissDropDown();

        }
    }*/


    void keyboardShowHide(boolean bShow) {
        if (bShow) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE);
            View cFocus = getCurrentFocus();
            if (imm != null && cFocus != null)
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            else
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


//		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the tracker when it is no longer needed.
    }

    public void fireTrackerEvent(String label) {

    }


    @SuppressWarnings("unused")
    private class LogBusToServer extends AsyncTask<String, Integer, Long> {
        protected Long doInBackground(String... sbusno) {
            try {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                Location location = mApp.getMyLocation(ActLogBus.this);
                if (mBusNoView != null)
                    nameValuePairs.add(new BasicNameValuePair("busno", sBusNoView));
                nameValuePairs.add(new BasicNameValuePair("email", CGlobals_BA.msGmail));
                nameValuePairs.add(new BasicNameValuePair("imei", CGlobals_BA.mIMEI));
                if (location != null) {
                    nameValuePairs.add(new BasicNameValuePair("lat", Double.toString(location.getLatitude())));
                    nameValuePairs.add(new BasicNameValuePair("lon", Double.toString(location.getLongitude())));
                }
                nameValuePairs.add(new BasicNameValuePair("startserial", sStartSerial));
                nameValuePairs.add(new BasicNameValuePair("endserial", sEndSerial));
                nameValuePairs.add(new BasicNameValuePair("routecode", mApp.msRouteCode));
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

            super.onPostExecute(result);
        }
    }
} // ActLogBus 

