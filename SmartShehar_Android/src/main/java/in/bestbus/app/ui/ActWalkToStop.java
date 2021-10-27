package in.bestbus.app.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.smartshehar.dashboard.app.R;

import java.util.List;
import java.util.Locale;

import in.bestbus.app.CGlobals_BA;
import lib.app.util.SSLog_SS;

public class ActWalkToStop extends AppCompatActivity {
    public CheckBox dontShowAgain;
    ProgressDialog mProgressDialog;
    String msNearBy;
    TextView mTvStatus;
    SharedPreferences mSettings;
    private Location mMostRecentLocation;
    private CGlobals_BA mApp = null;
    /**
     * Called when the activity is first created.
     */
//        private boolean mAlternateTitle = false;
    private String URL = "file:///android_asset/www/walktostop.html";
    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApp = CGlobals_BA.getInstance();
        mApp.init(this);
        mApp.getMyLocation(ActWalkToStop.this);
        mApp.mCallHome.userPing(getString(R.string.pageWalkToStop), "");

        ActionBar ab = getSupportActionBar();
        ///       ab.setIcon(R.drawable.ss_logo32);
        ab.setHomeButtonEnabled(true);
        setContentView(R.layout.actwalktostop);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            double startLat = extras.getDouble("startlat");
            double startLon = extras.getDouble("startlon");
            double destLat = extras.getDouble("destlat");
            double destLon = extras.getDouble("destlon");
            URL = URL + "?startlat=" + Double.toString(startLat) +
                    "&startlon=" + Double.toString(startLon) +
                    "&destlat=" + Double.toString(destLat) +
                    "&destlon=" + Double.toString(destLon);
//        URL = "https://maps.google.com/maps?saddr=" + startLat + 
//        			", " + startLon + "&daddr=" + destLat + ", " + destLon;
        }
        setupWebView();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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

    /**
     * Sets up the WebView object and loads the URL of the page
     **/
    @SuppressLint("JavascriptInterface")
    private void setupWebView() {
        mMostRecentLocation = mApp.getMyLocation(ActWalkToStop.this);
        final String centerURL = "javascript:centerAt(" +
                mMostRecentLocation.getLatitude() + "," +
                mMostRecentLocation.getLongitude() + ")";
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        //Wait for the page to load then send the location information
        webView.setWebViewClient(new WebViewClient() {
            /* On Android 1.1 shouldOverrideUrlLoading() will be called every time the user clicks a link,
             * but on Android 1.5 it will be called for every page load, even if it was caused by calling loadUrl()! */
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse(url));
                    startActivity(intent);
                } else if (url.startsWith("google.navigation:")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse(url)));
                    } catch (Exception e) {
                        Toast.makeText(ActWalkToStop.this, "You need to install Google Navigation to use this feature", Toast.LENGTH_SHORT).show();
                    }
                } else if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                }
                return true;

            }
        });


        webView.loadUrl(URL);
//	   webView.loadUrl("file:///android_assets/police.html");
        /** Allows JavaScript calls to access application resources **/
        webView.addJavascriptInterface(new JavaScriptInterface(), "android");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dashboard, menu);
        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
        } else if (item.getItemId() == R.id.menu_share) {
            mApp.getMyLocation(ActWalkToStop.this);
            mApp.mCallHome.userPing(getString(R.string.atShare), "");
            fireTrackerEvent(getString(R.string.atShare));
            String message = getString(R.string.androidAppLink);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(share, "Share " + getString(R.string.appTitle)));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void fireTrackerEvent(String label) {
    }

    @Override
    protected void onResume() {
        if (mApp.haveNetworkConnection() == 0) {
            try {
                mTvStatus = (TextView) findViewById(R.id.tvStatus);
                mTvStatus.setText(R.string.no_internet);
            } catch (Exception e) {
                SSLog_SS.e(" Emergency - Resume: ", e.getMessage());
            }
        }
        super.onResume();
    }

//	private void updateMyLocation(Location location) {
//		mApp.mCurrentLocation = location;
//	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.actwalktostop);
    }

    String getAddress(Location location) {
        if (location == null)
            return "";
        try {
            Geocoder geo = new Geocoder(ActWalkToStop.this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (addresses.isEmpty()) {
                return "";
            } else {
                if (addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    address += ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();
                    return address;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
        return "";
    }

    /**
     * Sets up the interface for getting access to Latitude and Longitude data from device
     **/
    @SuppressLint("JavascriptInterface")
    private class JavaScriptInterface {

    }


} // Activity_BeSafe_Emergency





