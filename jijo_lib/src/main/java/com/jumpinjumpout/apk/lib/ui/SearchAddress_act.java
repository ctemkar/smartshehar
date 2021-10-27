package com.jumpinjumpout.apk.lib.ui;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.lib.AddressListArrayAdapter;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.ClearableEditText;
import com.jumpinjumpout.apk.lib.R;
import com.jumpinjumpout.apk.lib.SSLog;

import java.lang.reflect.Type;
import java.util.Locale;

import lib.app.util.CAddress;

public class SearchAddress_act extends Activity {

    private final String TAG = "SearchAddress_act";
    ProgressDialog mProgressDialog;
    ListView addressListView;
    AddressListArrayAdapter addressListAdapter;
    CGlobals_lib mApp;
    ClearableEditText editsearch;
    TextView tvAddHome, tvAddWork;
    LinearLayout llHome, llWork;
    String cc = "", sValueSetting1 = "", sValueSetting2 = "";
    public static boolean isSetHome = false;
    public static boolean isSetWork = false;


    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchaddress_act);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME
                | ActionBar.DISPLAY_HOME_AS_UP); // what's mainly important here is DISPLAY_SHOW_CUSTOM. the rest is optional
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        tvAddHome = (TextView) findViewById(R.id.addHome);
        tvAddWork = (TextView) findViewById(R.id.addWork);
        llHome = (LinearLayout) findViewById(R.id.ll1);
        llWork = (LinearLayout) findViewById(R.id.ll2);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            cc = extras.getString("cc");
            sValueSetting1 = extras.getString("VALUE_SETTING1");
            sValueSetting2 = extras.getString("VALUE_SETTING2");
        }
        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflate the view that we created before
        View v = inflater.inflate(R.layout.actionbar_search, null);
        editsearch = (ClearableEditText) v.findViewById(R.id.search_box);

        editsearch.setVisibility(View.VISIBLE);
        editsearch.setHint("Enter street address");
        editsearch.addTextChangedListener(textWatcher);
        addressListView = (ListView) findViewById(R.id.listview);
        mApp = CGlobals_lib.getInstance();
        mProgressDialog = new ProgressDialog(SearchAddress_act.this);
        mProgressDialog.setMessage("Getting stop information");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
        if (TextUtils.isEmpty(cc)) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            cc = tm.getSimCountryIso();
            Log.d("Country code:", cc);
        }

        String sResultHome = null;
        String sResultWork = null;
        try {
            sResultHome = pref.getString("homeAddress", "");
            sResultWork = pref.getString("workAddress", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(sResultWork) && !TextUtils.isEmpty(sResultHome)) {
            llHome.setVisibility(View.GONE);
            llWork.setVisibility(View.GONE);
        }

        tvAddHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTvAddHome();
            }
        });

        tvAddWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTvAddWork();
            }
        });

        if (!TextUtils.isEmpty(sResultWork)) {
            Type type = new TypeToken<CAddress>() {
            }.getType();

            try {
                CAddress caWorkAddress = new Gson().fromJson(sResultWork, type);

                llWork.setVisibility(View.GONE);

            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        } else {
            llWork.setVisibility(View.VISIBLE);
        }


        if (!TextUtils.isEmpty(sResultHome)) {
            Type type = new TypeToken<CAddress>() {
            }.getType();

            try {
                CAddress caHomeAddress = new Gson().fromJson(sResultHome, type);

                llHome.setVisibility(View.GONE);


            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        } else {
            llHome.setVisibility(View.VISIBLE);
        }

        if (sValueSetting1 != null) {
            if (sValueSetting1.equals("1")) {
                llHome.setVisibility(View.GONE);
            }
        }
        if (sValueSetting2 != null) {
            if (sValueSetting2.equals("2")) {
                llWork.setVisibility(View.GONE);
            }
        }

        actionBar.setCustomView(v);
        actionBar.setHomeButtonEnabled(true);
        mProgressDialog.hide();


    } // create

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        addressListAdapter = new AddressListArrayAdapter(
                SearchAddress_act.this, CGlobals_lib.getInstance()
                .readRecentAddresses(this),
                SearchAddress_act.this, cc);

        addressListView.setAdapter(addressListAdapter);
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
        InputMethodManager keyboard = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(editsearch.getWindowToken(), 0);

    }

    // EditText TextWatcher
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {

            try {
                llHome.setVisibility(View.GONE);
                llWork.setVisibility(View.GONE);
                String text = editsearch.getText().toString()
                        .toLowerCase(Locale.getDefault());
                addressListAdapter.getFilter().filter(text);
                SSLog.i(TAG, "filtered");
            } catch (Exception e) {
                SSLog.e(TAG, "EditText TextWatcher", e);
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

    private void setTvAddHome() {
        isSetHome = true;
        tvAddHome.setText("Type your Home Address or\n" +
                "Select one of your recent addresses from below");
        llWork.setVisibility(View.GONE);

    }

    private void setTvAddWork() {
        isSetWork = true;
        tvAddWork.setText("Type your Work Address or\n" +
                "Select one of your recent addresses from below");
        llHome.setVisibility(View.GONE);

    }


}