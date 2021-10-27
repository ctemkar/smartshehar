package com.smartshehar.dashboard.app.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.smartshehar.dashboard.app.CFare;
import com.smartshehar.dashboard.app.CFareParams;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ActFareCalculator extends AppCompatActivity implements OnScrollListener {
    FareListAdapter mListAdapter;
    Spinner mSpinnerVehicleType;//, mSpinnerCity;
    private ArrayList<CFareParams> maoFareParams;
    private CFareParams moFareParams;
    ArrayAdapter<String> mVehicleDataAdapter, mCityAdapter;
    TextView tvFooter;
    private String msCity;
    ArrayList<String> mCityList;
    ArrayList<String> mVehicleList;
    ListView list;
    public static String TAG = "ActFareCalculator";

    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            ActionBar ab = getSupportActionBar();
            assert ab != null;
            ab.setHomeButtonEnabled(true);
            msCity = CGlobals_db.getCity(ActFareCalculator.this);
            ab.setTitle("Fare calculator ");
            setContentView(R.layout.actfarecalulator);

            mListAdapter = new FareListAdapter(this);


            // mSpinnerCity = (Spinner) findViewById( R.layout.sherlock_spinner_item);
            mSpinnerVehicleType = (Spinner) findViewById(R.id.spinnerVehicleType);
            tvFooter = (TextView) findViewById(R.id.tvFooter);
            setVehicle();
            addListenerOnSpinnerItemSelection();
            list = (ListView) findViewById(R.id.list);
            mCityList = new ArrayList<>();
            mCityList = CGlobals_db.mDBHelper.getCityList();
            Context context = ActFareCalculator.this.getApplicationContext();
            if (msCity.length() > 0) {
                mCityAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, mCityList);
                mCityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                ActionBar.OnNavigationListener navigationListener = new ActionBar.OnNavigationListener() {

                    @Override
                    public boolean onNavigationItemSelected(int itemPosition, long itemId) {

                        String city = mCityList.get(itemPosition);
                        CGlobals_db.setCity(city, ActFareCalculator.this);
                        msCity = city;
                        maoFareParams = CGlobals_db.mDBHelper.getCityFareParameters(msCity);
                        moFareParams.objClone(maoFareParams.get(0));
                        mListAdapter = new FareListAdapter(ActFareCalculator.this);
                        list.setAdapter(mListAdapter);
                        setVehicle();
                        return true;
                    }
                };
                ab.setListNavigationCallbacks(mCityAdapter, navigationListener);
            }

            int pos = 0;
            for (int i = 0; i < mCityList.size(); i++) {
                if (msCity.equalsIgnoreCase(mCityList.get(i))) {
                    pos = i;
                    break;
                }

            }
            getSupportActionBar().setSelectedNavigationItem(pos);
            list.setAdapter(mListAdapter);
            list.setOnScrollListener(this);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ActFareCalculator.this, "ActFareCalculator in onCreate() " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    int position = 0;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;
        position = list.getAdapter().getCount() - 16;
        if (loadMore) {

            mListAdapter.count += visibleItemCount; // or any other amount
//            mListAdapter.notifyDataSetChanged();
            list.setAdapter(mListAdapter);
            list.setSelection(position);
//            list.smoothScrollToPosition(position);
            if (mListAdapter.count == 104) {
                Toast.makeText(this, "Going far?", Toast.LENGTH_SHORT).show();
            } else if (mListAdapter.count > 20000) {
                Toast.makeText(this, "To the moon, eh?", Toast.LENGTH_SHORT).show();
            }

        }
    }

    class FareListAdapter extends BaseAdapter {
        int count = 40; /* starting amount */
        Context context;
        String[] data;
        private LayoutInflater inflater = null;

        public FareListAdapter(Context context) {
            this.context = context;
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return count;
        }

        public Object getItem(int pos) {
            return pos;
        }

        public long getItemId(int pos) {
            return pos;
        }

        public View getView(int pos, View v, ViewGroup p) {
/*
                    TextView view = new TextView(ActFareCalculator.this);
	                view.setText("entry " + cf.msFare);
	                return view;
*/
            CFare cf = new CFare(moFareParams, moFareParams.msVehicleType,
                    pos * 100, 0);
            View vi = v;
            if (vi == null)
                vi = inflater.inflate(R.layout.rowfarecalculator, null);
            TextView dist = (TextView) vi.findViewById(R.id.faredistance);
            dist.setText(CGlobals_db.showDistance(cf.miDist));
            TextView tvFare = (TextView) vi.findViewById(R.id.fare);
            tvFare.setText(cf.msFare);
            TextView tvNightfare = (TextView) vi.findViewById(R.id.nightfare);
            tvNightfare.setText(cf.msNightFare);
            return vi;
        }
    }


    public void addListenerOnSpinnerItemSelection() {
        mSpinnerVehicleType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
//               	aoCFareParams = CGlobals_db.mDBHelper.getCityFareParameters(CGlobals_db.msCity);
                if (item != null) {
                    Toast.makeText(ActFareCalculator.this, item.toString(),
                            Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < maoFareParams.size(); i++) {
                        if (item.toString().equals(maoFareParams.get(i).msVehicleTypeDescription)) {
                            moFareParams.objClone(maoFareParams.get(i));
                            mListAdapter = new FareListAdapter(ActFareCalculator.this);
                            list.setAdapter(mListAdapter);
                            tvFooter.setText("Min Dist: " +
                                    CGlobals_db.showDistance(moFareParams.iMinimumDistance) +
                                    ", Min. Fare: " + moFareParams.fMinimumFare);
                            DecimalFormat df = new DecimalFormat("####0.00");
                            TextView tvRatePerDist = (TextView) findViewById(R.id.ratePerDist);
                            tvRatePerDist.setText(df.format(moFareParams.fFarePerKm) + " per "
                                    + moFareParams.msDistanceUnit);

                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(ActFareCalculator.this, "Nothing selected",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setVehicle() {
        mVehicleList = new ArrayList<>();
        maoFareParams = new ArrayList<>();
        moFareParams = new CFareParams();
        maoFareParams = CGlobals_db.mDBHelper.getCityFareParameters(msCity);

        for (int i = 0; i < maoFareParams.size(); i++) {
            mVehicleList.add(maoFareParams.get(i).msVehicleTypeDescription);
        }
        moFareParams.objClone(maoFareParams.get(0));
        mVehicleDataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mVehicleList);
        mVehicleDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerVehicleType.setAdapter(mVehicleDataAdapter);

    }

}
