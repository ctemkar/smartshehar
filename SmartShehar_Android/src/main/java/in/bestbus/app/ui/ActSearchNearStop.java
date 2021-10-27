package in.bestbus.app.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.util.ArrayList;
import java.util.Locale;

import in.bestbus.app.CGlobals_BA;
import in.bestbus.app.CStop;
import in.bestbus.app.StopListArrayAdapter;
import lib.app.util.SSLog_SS;

public class ActSearchNearStop extends Activity {
    private final String TAG = "ActSearchNearStop: ";
    ProgressDialog mProgressDialog;
//	private String msProgressMessage;

    // Declare Variables
    ListView stopListView;
    StopListArrayAdapter stopListAdapter;
    CGlobals_BA mApp;
    Cursor busListCursor;
    EditText editsearch;
    // EditText TextWatcher
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            try {
                String text = editsearch.getText().toString()
                        .toLowerCase(Locale.getDefault());
                if (!TextUtils.isEmpty(text)) {
                    stopListAdapter.filter(text);
                }
            } catch (Exception e) {
                SSLog_SS.e(TAG, "TextWatcher: ", e, ActSearchNearStop.this);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stopname_listview);
        stopListView = (ListView) findViewById(R.id.listview);
        mApp = CGlobals_BA.getInstance();
//		mApp.init(this);
        mProgressDialog = new ProgressDialog(ActSearchNearStop.this);
        mProgressDialog.setMessage("Getting stop information");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        new GetStops().execute("");

        // Locate the ListView in listview_main.xml
//		stopListAdapter = new StopListArrayAdapter(this, maoStop, ActSearchNearStop.this);
//        stopListView.setAdapter(stopListAdapter);

    } // create

    @Override
    protected void onResume() {
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

    // Create the options menu
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Get the options menu view from menu.xml in menu folder
        try {
            getMenuInflater().inflate(R.menu.menu_bus_search, menu);
            // Locate the EditText in menu.xml
            editsearch = (EditText) menu.findItem(R.id.menu_search).getActionView();
            // Capture Text in EditText
            editsearch.addTextChangedListener(textWatcher);
        } catch (Exception e) {
            SSLog_SS.e(TAG, "OptionsMenu: ", e, ActSearchNearStop.this);
        }
        // Show the search menu item in menu.xml
        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        menuSearch.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            // Menu Action Collapse
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Empty EditText to remove text filtering
                editsearch.setText("");
                editsearch.clearFocus();
                return true;
            }

            // Menu Action Expand
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Focus on EditText
                editsearch.requestFocus();
                // Force the keyboard to show on EditText focus
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                return true;
            }
        });
        editsearch.requestFocus();
        // Force the keyboard to show on EditText focus
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        return true;
    }

    private class GetStops extends AsyncTask<String, Integer, Long> {
        ArrayList<CStop> maoStop = new ArrayList<CStop>();

        protected Long doInBackground(String... sbusno) {
            try {
                mApp.mbAutoRefreshLocation = false;
                maoStop = mApp.mDBHelperBus.getNearStopsArray(mApp.moStartStop.lat,
                        mApp.moStartStop.lon, CStop.EASY_WALK_DISTANCE);
            } catch (Exception e) {
                SSLog.e(TAG, "GetStops", e.getMessage().toString());
            }
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
            try {
                stopListAdapter = new StopListArrayAdapter(ActSearchNearStop.this,
                        maoStop, ActSearchNearStop.this);
//	        adapter = new BusListAdapter(ActSearchBus.this, busListCursor, mApp);
                // Binds the Adapter to the ListView
                stopListView.setAdapter(stopListAdapter);
                if (mProgressDialog != null) {
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.cancel();
                    }
                }
            } catch (Exception e) {
                SSLog.e(TAG, "GetStops", e.getMessage().toString());
            }
            super.onPostExecute(result);
        }
    }
}