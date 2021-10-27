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

public class ActSearchStop extends Activity {
    private final String TAG = "ActSearchStop: ";
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
                if (!TextUtils.isEmpty(editsearch.getText().toString())) {
                    stopListAdapter.filter(text);
                }
            } catch (Exception e) {
                SSLog.e(TAG, "textWatcher: ", e);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
            // TODO Auto-generated method stub

        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stopname_listview);
        stopListView = (ListView) findViewById(R.id.listview);
        mApp = CGlobals_BA.getInstance();
//		mApp.init(this);
        mProgressDialog = new ProgressDialog(ActSearchStop.this);
        mProgressDialog.setMessage("Getting stop information");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        new GetStops().execute("");

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
            SSLog.e(TAG, "OptionsMenu: ", e);
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
        private ArrayList<CStop> aoStop = new ArrayList<CStop>(),
                aoNearStops = new ArrayList<CStop>();

        protected Long doInBackground(String... sbusno) {
            try {
                if (mApp.maoStop.size() < 1) {
                    mApp.maoStop = mApp.mDBHelperBus.getAllStops();
                }
                CStop stop;
                try {
                    for (String s : mApp.masRecentStops) {
                        stop = mApp.mDBHelperBus.getStopFromSearchStr(s);
                        stop.miStopColor = CStop.RECENT_STOP_COLOR;
                        aoStop.add(stop);
                    }
                } catch (Exception e) {
                    SSLog.e(TAG, "GetStops", e);
                }
                try {
                    if (mApp.moStartStop != null && mApp.moStartStop != null) {
                        aoNearStops = mApp.mDBHelperBus.getNearStopsArray(mApp.moStartStop.lat,
                                mApp.moStartStop.lon, CStop.EASY_WALK_DISTANCE);
                        for (CStop s : aoNearStops) {
                            aoStop.add(s);
                        }
                    }
                } catch (Exception e) {
                    SSLog.e(TAG, "GetStops", e);
                }
                for (CStop s : mApp.maoStop) {
                    aoStop.add(s);
                }
            } catch (Exception e) {
                SSLog.e(TAG, "GetStops", e);
            }
            return (long) 1;
        }

        @Override
        protected void onPreExecute() {
            try {
                if (!isFinishing())
                    mProgressDialog.show();
            } catch (Exception e) {
                SSLog.e(TAG, "GetStops", e);
            }
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Long result) {
            try {
                ArrayList<CStop> cbusarr = new ArrayList<CStop>();
                for (CStop oBus : aoStop) {
                    cbusarr.add(oBus);
                }
                if (cbusarr.size() > 0) {
                    stopListAdapter = new StopListArrayAdapter(ActSearchStop.this, aoStop, ActSearchStop.this);
                    stopListView.setAdapter(stopListAdapter);
                }
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