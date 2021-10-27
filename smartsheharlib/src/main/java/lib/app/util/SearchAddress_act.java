package lib.app.util;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Locale;

import smartsheharcom.www.smartsheharlib.R;


public class SearchAddress_act extends AppCompatActivity {
    // EditText TextWatcher
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {

            String text = editsearch.getText().toString()
                    .toLowerCase(Locale.getDefault());
            addressListAdapter.getFilter().filter(text);
            //  SSLog.i(TAG, "filtered");
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
    //	private String msProgressMessage;
    //	private final String TAG = "ActSearchBus: ";
    ProgressDialog mProgressDialog;
    // Declare Variables
    ListView addressListView;
    AddressListArrayAdapter addressListAdapter;
    CGlobals_lib_ss mApp;
    ClearableEditText editsearch;
    ArrayList<CAddress> recentAddress = new ArrayList<>();
    Connectivity mConnectivity;

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stopname_listview);
        ActionBar actionBar = getSupportActionBar(); // you can use ABS or the non-bc ActionBar
        assert actionBar != null;
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_HOME_AS_UP); // what's mainly important here is DISPLAY_SHOW_CUSTOM. the rest is optional
        actionBar.setHomeButtonEnabled(true);
        Bundle extras = getIntent().getExtras();
        String cc = "";
        if (extras != null) {
            cc = extras.getString("cc");
        }
        mConnectivity = new Connectivity();
        if (!mConnectivity.checkConnected(SearchAddress_act.this)) {
            if (!mConnectivity.connectionError(SearchAddress_act.this, getString(R.string.require_internet))) {
                String TAG = "SearchAddress_act";
                Log.d(TAG, "Internet Connection");
            }
        }

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflate the view that we created before
        View v = inflater.inflate(R.layout.actionbar_search, null);
        editsearch = (ClearableEditText) v.findViewById(R.id.search_box);

        editsearch.setVisibility(View.VISIBLE);
        editsearch.setHint("Enter street address");

        editsearch.addTextChangedListener(textWatcher);

        addressListView = (ListView) findViewById(R.id.listview);
        mApp = CGlobals_lib_ss.getInstance();
        mProgressDialog = new ProgressDialog(SearchAddress_act.this);
        mProgressDialog.setMessage("Getting stop information");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        recentAddress = CGlobals_lib_ss.getInstance().readRecentAddresses(
                this);
//        int len = mApp.masRecentAddresses.size();
//        String adr;
//        for (int i = 0; i < len; i++) {
//            adr = mApp.masRecentAddresses.get(i);
//            recentAddress.add(new CAddress(adr, "", 0, 0));
//        }

        if (TextUtils.isEmpty(cc)) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            cc = tm.getSimCountryIso();
            Log.d("Country code:", cc);
        }
        // Location location = CGlobals_lib.getInstance().getBestLocation(this);
        //CAddress addr = CGlobals_lib.getInstance().getAddress(new LatLng(location.getLatitude(), location.getLongitude()));
        addressListAdapter = new AddressListArrayAdapter(SearchAddress_act.this,
                recentAddress, SearchAddress_act.this, cc);

        addressListView.setAdapter(addressListAdapter);
        actionBar.setCustomView(v);
//		new GetStops().execute("");

    } // create

    @Override
    protected void onResume() {
        super.onResume();
        InputMethodManager keyboard = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(editsearch.getWindowToken(), 0);
    }

    @Override
    protected void onPause() {
        if (mProgressDialog != null)
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        super.onPause();
        InputMethodManager keyboard = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(editsearch.getWindowToken(), 0);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null)
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}