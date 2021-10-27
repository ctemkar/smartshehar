package in.bestbus.app.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.util.ArrayList;
import java.util.Locale;

import in.bestbus.app.BusListArrayAdapter;
import in.bestbus.app.CBus;
import in.bestbus.app.CGlobals_BA;
import in.bestbus.app.NavItem;
import lib.app.util.Connectivity;

public class ActSearchBus extends AppCompatActivity {
    private final String TAG = "ActSearchBus: ";
    ProgressDialog mProgressDialog;
    // Declare Variables
    ListView busListView;
    private TextView tvBusNoList, tvBusStop;
    EditText etBusNoList;
    BusListArrayAdapter busListAdapter;
    CGlobals_BA mApp;
    Cursor busListCursor;
    // EditText TextWatcher
    static String sRouteCode, sBusNo, sBusLable;
    //Action menu bar
    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();
    ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    RelativeLayout mDrawerPane;
    Toolbar toolbar;
    Connectivity mConnectivity;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            try {
                String text = etBusNoList.getText().toString()
                        .toLowerCase(Locale.getDefault());
                if (!TextUtils.isEmpty(text)) {
                    busListAdapter.filter(text);
                }
            } catch (Exception e) {
                SSLog.e(TAG, "TextWatcher: ", e);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
        }

    };

    public interface ListClickListener {
        public abstract void onListClick(int position);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.busno_listview);
        mConnectivity = new Connectivity();
        busListView = (ListView) findViewById(R.id.listview);
        tvBusStop = (TextView) findViewById(R.id.tvBusStop);
        tvBusNoList = (TextView) findViewById(R.id.tvBusNoList);
        mApp = CGlobals_BA.getInstance();
        mApp.init(this);
        mProgressDialog = new ProgressDialog(ActSearchBus.this);
        mProgressDialog.setMessage("Getting bus information");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        tvBusStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActSearchBus.this, ActFindBus.class);
                startActivity(intent);
            }
        });

        tvBusNoList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        initToolBar();
    }

    public void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ActSearchBus.this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Bus");
        etBusNoList = (EditText) findViewById(R.id.etBusNoList);
        etBusNoList.addTextChangedListener(textWatcher);
        /*etBusNoList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                removeActiveUserFragment();
                busListView.setVisibility(View.VISIBLE);
                return false;
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bus_search, menu);
        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        menuSearch.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /*public void removeActiveUserFragment() {
        try {
            FragBusJourney fragment = (FragBusJourney) getFragmentManager()
                    .findFragmentById(R.id.headlines_fragment);
            if (fragment != null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager
                        .beginTransaction();
                fragmentTransaction.remove(fragment);
                fragmentTransaction.commit();
            }
        } catch (Exception e) {
            SSLog.e(TAG, "removeActiveUserFragment: ", e);
        }
    }*/

    /*public void addActiveUsersFragment() {
        try {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            FragBusJourney fragment = new FragBusJourney();
            fragmentTransaction.add(R.id.headlines_fragment,
                    fragment);
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Exception e) {
            SSLog.e(TAG, "addActiveUsersFragment: ", e);
        }
    }*/

    @Override
    protected void onResume() {
        new GetBuses().execute("");
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mProgressDialog != null)
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null)
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
    }

    private class GetBuses extends AsyncTask<String, Integer, Long> {
        ArrayList<CBus> aoBus = new ArrayList<CBus>();

        protected Long doInBackground(String... sbusno) {
            if (mApp.maoBus.size() == 0)
                mApp.maoBus = mApp.mDBHelperBus.getAllBuses();
            return (long) 1;
        }

        @Override
        protected void onPreExecute() {
            if (!isFinishing())
                mProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Long result) {
            for (CBus b : mApp.maoBus) {
                aoBus.add(b);
            }
            busListAdapter = new BusListArrayAdapter(ActSearchBus.this, aoBus, ActSearchBus.this, new ListClickListener() {
                @Override
                public void onListClick(int position) {
                    sBusLable = aoBus.get(position).msBusLabel;
                    sBusNo = aoBus.get(position).msBusNo;
                    sRouteCode = aoBus.get(position).msRouteCode;
                    CGlobals_BA.getInstance().msRouteCode = aoBus.get(position).msRouteCode;
                    CGlobals_BA.getInstance().msBusNo = aoBus.get(position).msBusNo;
                    CGlobals_BA.getInstance().msBusLabel = aoBus.get(position).msBusLabel;
                    Intent intent = new Intent(ActSearchBus.this, ActBusRoute.class);
                    startActivity(intent);
                   // addActiveUsersFragment();
                   // busListView.setVisibility(View.GONE);
                    //etBusNoList.setText(sBusLable);
                }
            });
            // Binds the Adapter to the ListView
            busListAdapter.notifyDataSetChanged();
            busListView.setAdapter(busListAdapter);
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.cancel();
                }
            }
            super.onPostExecute(result);
        }
    }
}