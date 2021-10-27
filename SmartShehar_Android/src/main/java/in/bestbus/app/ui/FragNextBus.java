package in.bestbus.app.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
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

import in.bestbus.app.CGlobals_BA;
import in.bestbus.app.CStop;
import lib.app.util.SSLog_SS;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragNextBus extends Fragment {
    private final String URL = CGlobals_BA.PHP_PATH + "/expectedbusandtime.php";
    CGlobals_BA mApp;
    Button mBtnClear;
    private CStop moStartStop = null;
    private AutoCompleteTextView mStartView;
    private TextView mTvStatus;
    private TextView tvNoArrivingBuses;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mApp = CGlobals_BA.getInstance();
        return inflater.inflate(R.layout.fragnextbus, container, false);
    }

    @Override
    public void onStart() {
        mBtnClear = (Button) getView().findViewById(R.id.btnClear);
        mTvStatus = (TextView) getView().findViewById(R.id.locStatus);
        mStartView = (AutoCompleteTextView) getView().findViewById(R.id.autocomplete_startstop);
        tvNoArrivingBuses = (TextView) getView().findViewById(R.id.tvNoArrivingBuses);
        Location loc = mApp.getMyLocation(getActivity().getApplicationContext());
        if (loc != null)
            updateMyLocation(loc);

        super.onStart();
    }

    void updateMyLocation(final Location location) {
        // Now get the places at this location, if this fragment is still bound
        // to an activity
        TableLayout tl = (TableLayout) getView().findViewById(R.id.tblArrivingBuses);
        tl.post(new Runnable() {
            @Override
            public void run() {
                moStartStop = mApp.mDBHelperBus.getNearestStop(
                        location.getLatitude(),
                        location.getLongitude());
                if (moStartStop == null)
                    return;
                setStartStop(moStartStop.msStopNameDetail);
                mTvStatus.setVisibility(View.GONE);
                mStartView.setText(moStartStop.msStopNameDetail);
                mStartView.setContentDescription(moStartStop.msStopNameDetail);
                // set current time
                mBtnClear.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        mStartView.setText("");
                    }
                });


            }
        });
    }

    void setStartStop(String sStartStopFull) {

        CStop retStop = mApp.mDBHelperBus.getStopFromSearchStr(sStartStopFull);
        if (retStop != null) {
            moStartStop = retStop;

            mStartView.setThreshold(1000);
            mStartView.setText(moStartStop.msStopNameDetail);
            mStartView.setText(sStartStopFull);
            mTvStatus.setVisibility(View.GONE);
            mStartView.dismissDropDown();
            new GetArrivingBuses().execute("");

        }
    }

    private void displayArrivingBuses(String jsonArrivingBuses) {
        if (TextUtils.isEmpty(jsonArrivingBuses)) {
//			mTvStatus.setVisibility(View.GONE);
//			mTvStatus.setText("No buses found at this stop");
            return;
        }
        mTvStatus.setVisibility(View.GONE);
        String sBusNo = "", sDestinationStop = "", sEta = "";

        LayoutInflater inflater = getActivity().getLayoutInflater();

        TableLayout tl = (TableLayout) getView().findViewById(R.id.tblArrivingBuses);

        tl.removeAllViews();
        mTvStatus.setVisibility(View.GONE);
        try {
            JSONArray jObject = new JSONArray(jsonArrivingBuses);
            int len = jObject.length();
            tvNoArrivingBuses.setVisibility(View.VISIBLE);
            if (len > 0)
                tvNoArrivingBuses.setVisibility(View.GONE);
            for (int i = 0; i < jObject.length(); i++) {
                JSONObject jArrivingBuses = jObject.getJSONObject(i);
                sBusNo = jArrivingBuses.getString("b");
                sDestinationStop = jArrivingBuses.getString("sd");
                sEta = jArrivingBuses.getString("eta");
                LinearLayout trd = (LinearLayout) inflater.inflate(
                        R.layout.arrivingbusrow, tl, false);
                TextView tvArrivingBusNo = (TextView) trd.findViewById(R.id.tvArrivingBusNo);
                final TextView tvDestinationStop = (TextView) trd.findViewById(R.id.tvDestinationStop);
                final TextView tvArrivingBusEta = (TextView) trd.findViewById(R.id.tvArrivingBusEta);

                tvArrivingBusNo.setText(sBusNo);
                tvArrivingBusNo.setContentDescription(sBusNo);
                tvDestinationStop.setText(sDestinationStop);
                tvDestinationStop.setContentDescription("towards: " + sDestinationStop);
//			tvArrivingBusEta.setTypeface(tf);
                tvArrivingBusEta.setText(sEta + " mins");
                tvArrivingBusEta.setContentDescription(sEta);

                tl.addView(trd);
            }
        } catch (Exception e) {
            SSLog_SS.e("displayArrivingBuses: ", e.getMessage());
        }


    }

    private class GetArrivingBuses extends AsyncTask<String, Integer, Long> {
        private String sArrivingBuses = "";

        protected Long doInBackground(String... sbusno) {
            try {
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                Location location = mApp.getMyLocation(getActivity().getApplicationContext());
                if (moStartStop.miStopId > 0)
                    nameValuePairs.add(new BasicNameValuePair("stopid", Integer.toString(moStartStop.miStopId)));
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
            Activity activity = getActivity();
            if (activity != null) {
                Toast.makeText(activity, "Retrieved Arriving Buses: ",
                        Toast.LENGTH_SHORT).show();
                //			displayArrivingBuses("[{busno:'35', destinationstop:'[Marol] Maroshi Depot', eta:'5 mins'}]");
                displayArrivingBuses(sArrivingBuses);
            }
            super.onPostExecute(result);
        }
    }


}
