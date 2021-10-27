package com.jumpinjumpout.apk.user.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.DatabaseHandler;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.lib.ui.ScheduleTripCreate_act;
import com.jumpinjumpout.apk.lib.ui.VerifyPhone_act;
import com.jumpinjumpout.apk.user.ActionBarAdapter;
import com.jumpinjumpout.apk.user.CGlobals_user;
import com.jumpinjumpout.apk.user.ColorTool;
import com.jumpinjumpout.apk.user.Constants_user;
import com.jumpinjumpout.apk.user.MyApplication;
import com.jumpinjumpout.apk.user.NavItem;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lib.app.util.AppRater;
import lib.app.util.CAddress;
import lib.app.util.Connectivity;
import lib.app.util.MyLocation;
import lib.app.util.UpgradeApp;
import lib.app.util.ui.ActFeedback;

public class Dashboard_act extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnTouchListener {
    private static String TAG = "Dashboard_Act: ";
    protected CGlobals_user mApp = null;
    protected CheckBox checkbox;
    Connectivity mConnectivity;
    private boolean mIsGpsEnabled, mIsNetworkLocationEnabled;
    MyApplication myApplication;
    private String user_Full_Name;
    private String user_Email;
    private String user_profile_imagefile_name;
    private int has_user_profile = 0, user_profile_image_Rotate = 0;
    private String user_profile_image_path;
    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();
    ImageView mIvProfileImage;
    TextView mTvUserName, mTvEmail;
    RelativeLayout profileBox;
    CallbackManager callbackManager;
    AccessTokenTracker accessTokenTracker;
    AccessToken accessToken;
    ProfileTracker profileTracker;
    Bitmap hotspots = null;
    DatabaseHandler db;
    boolean checkDay;
    protected Handler handler = new Handler();
    ArrayList<CTrip> pPResultsList;
    boolean loginLogoutFlag = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_list);
        mConnectivity = new Connectivity();
        db = new DatabaseHandler(this);
        ImageView iv = (ImageView) findViewById(R.id.image);
        if (iv != null) {
            iv.setOnTouchListener(this);
        }
        mIvProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
        mTvUserName = (TextView) findViewById(R.id.tvUserName);
        mTvEmail = (TextView) findViewById(R.id.tvEmail);
        profileBox = (RelativeLayout) findViewById(R.id.profileBox);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
            }
        };
        // If the access token is available already assign it.
        accessToken = AccessToken.getCurrentAccessToken();

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                // App code
            }
        };
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.jumpinjumpout.apk.alpha",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        } catch (Exception e) {
        }
        //schedule
        mNavItems.add(new NavItem("Group", "Commuting Communities", R.mipmap.ic_community));
        mNavItems.add(new NavItem("Add Schedule", "", R.mipmap.ic_schedule));
        mNavItems.add(new NavItem("My Trip History", "", R.drawable.in_history));
        //mNavItems.add(new NavItem("Google+ login", "", R.mipmap.ic_googleplus));
        //mNavItems.add(new NavItem("Facebook login", "", R.mipmap.ic_facebook));
        mNavItems.add(new NavItem("Setting", "Add address, Options ...", R.mipmap.ic_settings));
        mNavItems.add(new NavItem("Help", "Quick start,FAQs,...", R.mipmap.ic_help));
        mNavItems.add(new NavItem("Feedback", "Contact Us,Rate Us, FB,...", R.mipmap.ic_feedback));


        // DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Populate the Navigtion Drawer with options
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mDrawerList = (ListView) findViewById(R.id.navList);
        ActionBarAdapter adapter = new ActionBarAdapter(Dashboard_act.this, mNavItems);
        mDrawerList.setAdapter(adapter);
        // Drawer Item click listeners
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position);
            }
        });
        final ActionBar actionBar = getActionBar();
        BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.actionbar_image));
        background.setTileModeX(android.graphics.Shader.TileMode.REPEAT);
        actionBar.setBackgroundDrawable(background);
        actionBar.setTitle("");
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(Dashboard_act.this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d(TAG, "onDrawerClosed: " + getTitle());
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        myApplication = MyApplication.getInstance();
        mApp = CGlobals_user.getInstance();
        MyLocation myLocation1 = new MyLocation(MyApplication.mVolleyRequestQueue);
        myLocation1.getLocation(this, locationResult);
        mApp.getMyLocation(Dashboard_act.this);
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager) getSystemService(ns);
        mNotificationManager.cancel(Constants_user.SERVER_NOTIFICATION_ID);
        mApp.getBestLocation(Dashboard_act.this);
        if (!mConnectivity.checkConnected(Dashboard_act.this)) {
            if (!mConnectivity.connectionError(Dashboard_act.this, getString(R.string.app_label))) {
                Log.d(TAG, "Internet Connection");
            }
        }
        mIsGpsEnabled = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            mIsGpsEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.e(TAG, ex.getStackTrace().toString());
        }
        try {
            mIsNetworkLocationEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.e(TAG, ex.getStackTrace().toString());
        }
        if (!mIsGpsEnabled) {
        }
        int statusCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (statusCode == ConnectionResult.SUCCESS) {
            // Continue with your regular activity/fragment configuration.
        } else {
            // Hide the map fragment so the default error message is not
            // visible.
            // Show a custom error message
            showErrorMessage(statusCode);
        }
        if (mApp.mPackageInfo == null) {
            mApp.init(this);
        }
        AppRater.app_launched(this, getString(R.string.app_label),
                mApp.mPackageInfo.packageName);


        UpgradeApp.app_launched(this, mApp.mPackageInfo, mApp.mUserInfo,
                getString(R.string.app_label),
                getString(R.string.appNameShort),
                mApp.mPackageInfo.versionCode, Constants_user.UPGRADE_APP_URL);
        profileBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Dashboard_act.this, UpdateUserDetails_act.class);
                startActivity(i);
            }
        });

    }


    private void checkScheduledTrip() {
        for (int i = 0; i < pPResultsList.size(); i++) {
            Calendar calendar1 = Calendar.getInstance();
            int dayofWeek = calendar1.get(Calendar.DAY_OF_WEEK);
            if (!pPResultsList.get(i).isScheduleActiveToday(dayofWeek)) // Is schedule active today?
                continue;
            SimpleDateFormat curFormaterDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat formatter1 = new SimpleDateFormat("HH:mm");
            String currentDate = formatter1.format(calendar1.getTime());
            String datadb = pPResultsList.get(i).getSTime1();
            long currentTime = -1;
            long dbTime = -1;
            Date dateDB;
            String dBTIME;
            try {
                dateDB = curFormaterDB.parse(datadb);
                dBTIME = formatter1.format(dateDB);
                currentTime = formatter1.parse(currentDate).getTime();
                dbTime = formatter1.parse(dBTIME).getTime();

            } catch (Exception e) {
                SSLog.e(TAG, "(checkScheduledTrip) ", e);
            }
            if (currentTime != -1 && dbTime != -1 && tobeRunToday()) {
                long lDiff = (currentTime - dbTime) / 1000;
                if (lDiff > -5 * 60 && lDiff <= 10 * 60) {
                    String currentscheduledatetime = CGlobals_lib.getInstance().getSharedPreferences(Dashboard_act.this)
                            .getString(Constants_lib.PREF_SCHEDULE_TRIP_CREATE, "");
                    String tripAllSchedule = MyApplication.getInstance().getPersistentPreference().
                            getString(Constants_user.PREF_SCHEDULED_TRIPS_I, "");
                    if (tripAllSchedule.equals("") || TextUtils.isEmpty(tripAllSchedule)) {
                        CTrip sr = pPResultsList.get(i);
                        runScheduleTrip(pPResultsList.get(i).getstart_address(), Double.parseDouble(pPResultsList.get(i).getstart_Lat())
                                , Double.parseDouble(pPResultsList.get(i).getstart_Lng()), pPResultsList.get(i).getdestination_address()
                                , Double.parseDouble(pPResultsList.get(i).getdestination_Lat()), Double.parseDouble(pPResultsList.get(i).getdestination_Lng()));
                        sr.setLastRunDate(new Date());
                        Gson gson = new Gson();
                        String json = gson.toJson(pPResultsList);
                        String jsonI = gson.toJson(sr);
                        MyApplication.getInstance().getPersistentPreferenceEditor().
                                putString(Constants_user.PREF_SCHEDULED_TRIPS, json);
                        MyApplication.getInstance().getPersistentPreferenceEditor().
                                putString(Constants_user.PREF_SCHEDULED_TRIPS_I, jsonI);
                        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                    } else {
                        SimpleDateFormat curFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        SimpleDateFormat postFormater = new SimpleDateFormat("yyyy-MM-dd");
                        String scheduleTodayDate = "";
                        String dBscheduleTodayDate = "";
                        Date date1, date2;
                        try {
                            date1 = curFormater.parse(currentscheduledatetime);
                            scheduleTodayDate = postFormater.format(date1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Type type = new TypeToken<CTrip>() {
                        }.getType();
                        CTrip arlTripAllSchedule = new Gson().fromJson(tripAllSchedule, type);
                        String dateDB1 = arlTripAllSchedule.getSTime1();
                        SimpleDateFormat curFormater1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        SimpleDateFormat postFormater1 = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            date2 = curFormater1.parse(dateDB1);
                            dBscheduleTodayDate = postFormater1.format(date2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (arlTripAllSchedule.getId() == pPResultsList.get(i).getId()
                                && dBscheduleTodayDate.equals(scheduleTodayDate)) {
                            Log.e("dzb", "zdnb");
                        } else {
                            checkDay = pPResultsList.get(i).getsun();
                            if (checkDay && dayofWeek == 1) {
                                CTrip sr = pPResultsList.get(i);
                                runScheduleTrip(pPResultsList.get(i).getstart_address(), Double.parseDouble(pPResultsList.get(i).getstart_Lat())
                                        , Double.parseDouble(pPResultsList.get(i).getstart_Lng()), pPResultsList.get(i).getdestination_address()
                                        , Double.parseDouble(pPResultsList.get(i).getdestination_Lat()), Double.parseDouble(pPResultsList.get(i).getdestination_Lng()));
                                sr.setLastRunDate(new Date());
                                Gson gson = new Gson();
                                String json = gson.toJson(pPResultsList);
                                String jsonI = gson.toJson(sr);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS_I, jsonI);
                                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                            }
                            checkDay = pPResultsList.get(i).getmon();
                            if (checkDay && dayofWeek == 2) {
                                CTrip sr = pPResultsList.get(i);
                                runScheduleTrip(pPResultsList.get(i).getstart_address(), Double.parseDouble(pPResultsList.get(i).getstart_Lat())
                                        , Double.parseDouble(pPResultsList.get(i).getstart_Lng()), pPResultsList.get(i).getdestination_address()
                                        , Double.parseDouble(pPResultsList.get(i).getdestination_Lat()), Double.parseDouble(pPResultsList.get(i).getdestination_Lng()));
                                sr.setLastRunDate(new Date());
                                Gson gson = new Gson();
                                String json = gson.toJson(pPResultsList);
                                String jsonI = gson.toJson(sr);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS_I, jsonI);
                                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                            }
                            checkDay = pPResultsList.get(i).gettue();
                            if (checkDay && dayofWeek == 3) {
                                CTrip sr = pPResultsList.get(i);
                                runScheduleTrip(pPResultsList.get(i).getstart_address(), Double.parseDouble(pPResultsList.get(i).getstart_Lat())
                                        , Double.parseDouble(pPResultsList.get(i).getstart_Lng()), pPResultsList.get(i).getdestination_address()
                                        , Double.parseDouble(pPResultsList.get(i).getdestination_Lat()), Double.parseDouble(pPResultsList.get(i).getdestination_Lng()));
                                sr.setLastRunDate(new Date());
                                Gson gson = new Gson();
                                String json = gson.toJson(pPResultsList);
                                String jsonI = gson.toJson(sr);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS_I, jsonI);
                                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                            }
                            checkDay = pPResultsList.get(i).getwed();
                            if (checkDay && dayofWeek == 4) {
                                CTrip sr = pPResultsList.get(i);
                                runScheduleTrip(pPResultsList.get(i).getstart_address(), Double.parseDouble(pPResultsList.get(i).getstart_Lat())
                                        , Double.parseDouble(pPResultsList.get(i).getstart_Lng()), pPResultsList.get(i).getdestination_address()
                                        , Double.parseDouble(pPResultsList.get(i).getdestination_Lat()), Double.parseDouble(pPResultsList.get(i).getdestination_Lng()));
                                sr.setLastRunDate(new Date());
                                Gson gson = new Gson();
                                String json = gson.toJson(pPResultsList);
                                String jsonI = gson.toJson(sr);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS_I, jsonI);
                                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                            }
                            checkDay = pPResultsList.get(i).getthu();
                            if (checkDay && dayofWeek == 5) {
                                CTrip sr = pPResultsList.get(i);
                                runScheduleTrip(pPResultsList.get(i).getstart_address(), Double.parseDouble(pPResultsList.get(i).getstart_Lat())
                                        , Double.parseDouble(pPResultsList.get(i).getstart_Lng()), pPResultsList.get(i).getdestination_address()
                                        , Double.parseDouble(pPResultsList.get(i).getdestination_Lat()), Double.parseDouble(pPResultsList.get(i).getdestination_Lng()));
                                sr.setLastRunDate(new Date());
                                Gson gson = new Gson();
                                String json = gson.toJson(pPResultsList);
                                String jsonI = gson.toJson(sr);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS_I, jsonI);
                                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                            }
                            checkDay = pPResultsList.get(i).getfri();
                            if (checkDay && dayofWeek == 6) {
                                CTrip sr = pPResultsList.get(i);
                                runScheduleTrip(pPResultsList.get(i).getstart_address(), Double.parseDouble(pPResultsList.get(i).getstart_Lat())
                                        , Double.parseDouble(pPResultsList.get(i).getstart_Lng()), pPResultsList.get(i).getdestination_address()
                                        , Double.parseDouble(pPResultsList.get(i).getdestination_Lat()), Double.parseDouble(pPResultsList.get(i).getdestination_Lng()));
                                sr.setLastRunDate(new Date());
                                Gson gson = new Gson();
                                String json = gson.toJson(pPResultsList);
                                String jsonI = gson.toJson(sr);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS_I, jsonI);
                                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                            }
                            checkDay = pPResultsList.get(i).getsat();
                            if (checkDay && dayofWeek == 7) {
                                CTrip sr = pPResultsList.get(i);
                                runScheduleTrip(pPResultsList.get(i).getstart_address(), Double.parseDouble(pPResultsList.get(i).getstart_Lat())
                                        , Double.parseDouble(pPResultsList.get(i).getstart_Lng()), pPResultsList.get(i).getdestination_address()
                                        , Double.parseDouble(pPResultsList.get(i).getdestination_Lat()), Double.parseDouble(pPResultsList.get(i).getdestination_Lng()));
                                sr.setLastRunDate(new Date());
                                Gson gson = new Gson();
                                String json = gson.toJson(pPResultsList);
                                String jsonI = gson.toJson(sr);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_user.PREF_SCHEDULED_TRIPS_I, jsonI);
                                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                            }
                        }
                    }
                } else {
                    handler.postDelayed(runnableUpdatePosition,
                            Constants_user.DRIVER_UPDATE_INTERVAL);
                }
            }
        }
    }

    protected Runnable runnableUpdatePosition = new Runnable() {
        @Override
        public void run() {
            checkScheduledTrip();
        }
    };


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Log.d(TAG, "action bar clicked");
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                Toast.makeText(this, "Jumpin Jumpout Home", Toast.LENGTH_SHORT)
                        .show();
                runSmartShehar();
                break;
            case R.id.menu_share:
                mApp.getMyLocation(Dashboard_act.this);
                String message = getString(R.string.androidAppLink);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(share, "Share Jump in Jump out"));
                break;
            case R.id.menu_notification:
                Intent i = new Intent(Dashboard_act.this, Notification_act.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectItemFromDrawer(int position) {
        mDrawerList.setItemChecked(position, true);
        switch (position) {
            case 0:
                if (!mConnectivity.connectionError(Dashboard_act.this, getString(R.string.app_label))) {
                    Intent intentCommunity = new Intent(Dashboard_act.this,
                            CommunityShowAll_act.class);
                    startActivity(intentCommunity);
                }
                break;
            case 1:
                if (!mConnectivity.connectionError(Dashboard_act.this, getString(R.string.app_label))) {
                    Intent intentSchedule = new Intent(Dashboard_act.this,
                            ScheduleTripCreate_act.class);
                    startActivity(intentSchedule);
                }
                break;
            case 2:
                if (!mConnectivity.connectionError(Dashboard_act.this, getString(R.string.app_label))) {
                    Intent intentHistoryList = new Intent(Dashboard_act.this,
                            TripHistoryList_act.class);
                    startActivity(intentHistoryList);
                }
                break;
           /* case 3:
                if (!mConnectivity.connectionError(Dashboard_act.this, getString(R.string.app_label))) {
                    Intent intentFacebook = new Intent(Dashboard_act.this,
                            GooglePlusLogin_act.class);
                    startActivity(intentFacebook);
                }
                break;
            case 4:
                if (!mConnectivity.connectionError(Dashboard_act.this, getString(R.string.app_label))) {
                    Intent intentGooglePlus = new Intent(Dashboard_act.this,
                            FacebookLogin_act.class);
                    startActivity(intentGooglePlus);
                }
                break;*/

            case 3:
                if (!mConnectivity.connectionError(Dashboard_act.this, getString(R.string.app_label))) {
                    Intent intentSettings = new Intent(Dashboard_act.this,
                            Settings_act.class);
                    startActivity(intentSettings);
                }
                break;
            case 4:
                if (!mConnectivity.connectionError(Dashboard_act.this, getString(R.string.app_label))) {
                    Intent intentHelp = new Intent(Dashboard_act.this,
                            Help_act.class);
                    startActivity(intentHelp);
                }
                break;

            case 5:
                if (!mConnectivity.connectionError(Dashboard_act.this, getString(R.string.app_label))) {
                    String jijo_Website = "http://www.jumpinjumpout.com";
                    String jijo_Twitter = "https://twitter.com/jumpinjumpout";
                    String jijo_Feacbook = "https://www.facebook.com/JumpInJumpOut";
                    String jijo_Feedback_email = "userfeedback@jumpinjumpout.com";
                    Intent intentFeedback = new Intent(Dashboard_act.this,
                            ActFeedback.class);
                    intentFeedback.putExtra("app_label",
                            getString(R.string.app_label));
                    intentFeedback.putExtra("appCode", getString(R.string.appCode));
                    intentFeedback.putExtra("versionName",
                            CGlobals_user.getInstance().mPackageInfo.versionName);

                    intentFeedback.putExtra("USER_FEEDBACK_EMAIL", jijo_Feedback_email);
                    intentFeedback.putExtra("FACEBOOK_POST", jijo_Feacbook);
                    intentFeedback.putExtra("TWITTER_POST", jijo_Twitter);
                    intentFeedback.putExtra("WEB_SITE", jijo_Website);
                    intentFeedback.putExtra("PACKAGE_NAME", "https://play.google.com/store/apps/details?id=" + getPackageName());
                    startActivity(intentFeedback);
                }
                break;
        }
        // Close the drawer
        mDrawerLayout.closeDrawer(mDrawerPane);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        //userAccountLoginSample();
        AppEventsLogger.activateApp(this);
        getUserProfile();
        if (mApp.mExitApp) {
            mApp.mExitApp = false;
            finish();
        } else {
            Intent intent = null;
            boolean hasJoinedTrip = MyApplication.getInstance().getPersistentPreference()
                    .getBoolean(Constants_user.PREF_JOINED_TRIP, false);
            if (CGlobals_lib.getInstance().isInTrip(getApplicationContext())) {
                intent = new Intent(Dashboard_act.this, FriendPool_act.class);
                startActivity(intent);
            } else if (hasJoinedTrip) {
                MyApplication.getInstance().getPersistentPreferenceEditor().
                        putBoolean(Constants_user.GET_A_CAB_VALUE, false);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                intent = new Intent(Dashboard_act.this, ShareTripTrackDriver_act.class);
                startActivity(intent);
            }
            CGlobals_user.mbRegistered = MyApplication.getInstance().getPersistentPreference()
                    .getBoolean(Constants_user.PREF_REGISTERED, false);
            if (!CGlobals_user.mbRegistered) {
                intent = new Intent(this, VerifyPhone_act.class);
                startActivity(intent);
            }
            DisplayHideMessage();
        }
        super.onResume();
        pPResultsList = db.getSchdhuleList();
        handler.postDelayed(runnableUpdatePosition,
                Constants_user.DRIVER_UPDATE_INTERVAL);
        checkScheduledTrip();
    }

    /*private void userAccountLoginSample() {
        loginLogoutFlag = MyApplication.getInstance().getPersistentPreference()
                .getBoolean(Constants_user.PREF_LOGIN_LOGOUT_FLAG, false);
        if (!loginLogoutFlag) {
            Intent intent1 = new Intent(Dashboard_act.this, UserAccountLoginSample_act.class);
            startActivity(intent1);
            finish();
        }

    }*/

    @Override
    protected void onPause() {
        super.onPause();
        mIvProfileImage.setImageBitmap(null);
        hotspots = null;
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
        System.gc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIvProfileImage.setImageBitmap(null);
        hotspots = null;
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    @SuppressWarnings("deprecation")
    void runSmartShehar() {
        final PackageManager pm = getPackageManager();
        String appPackageName = "com.smartshehar.dashboard.app";
        Intent appStartIntent = pm.getLaunchIntentForPackage(appPackageName);
        try {
            if (null != appStartIntent) {
                startActivity(appStartIntent);
            } else {
                Intent marketIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + appPackageName));
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                        | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                startActivity(marketIntent);
            }
        } catch (ActivityNotFoundException e) {
            SSLog.e(TAG, "Cannot find or load Jijo", e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void showErrorMessage(final int statusCode) {
        GooglePlayServicesUtil.getErrorDialog(statusCode, this, 0).show();
    }

    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(this, "Cannot get location. Please turn on GPS",
                Toast.LENGTH_SHORT).show();
        mApp.showGPSDisabledAlertToUser(this);
    }

    @Override
    public void onProviderDisabled(String arg0) {
    }

    @Override
    public void onProviderEnabled(String arg0) {
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.d(this.getClass().getSimpleName(), "onConnected()");
    }

    @Override
    public void onLocationChanged(Location location) {
        mApp.setMyLocation(location, true);
    }

    MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            mApp.setMyLocation(location, true);
            mApp.sendUpdatePosition(location, getApplicationContext());
        }
    };

    private void DisplayHideMessage() {
    }


    @Override
    public void onConnectionSuspended(int arg0) {
    }

    private void getUserProfile() {
        // Send Data to Server Database Using Volley Library
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_user.GET_USER_PROFILE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getMyProfile(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(
                        Dashboard_act.this.getBaseContext(),
                        getString(R.string.retry_internet),
                        Toast.LENGTH_LONG).show();
                SSLog.e(TAG, "getUserProfile :-   ", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params = CGlobals_user.getInstance().getMinMobileParams(params,
                        Constants_user.GET_USER_PROFILE_URL);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_user.GET_USER_PROFILE_URL;
                try {
                    String url = url1 + "?" + getParams.toString();
                    System.out.println("url  " + url);
                } catch (Exception e) {
                    SSLog.e(TAG, "getMemberDetails", e);
                }
                return CGlobals_user.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendUpdatePosition

    private void getMyProfile(String response) {
        response = response.trim();
        if (response.trim().equals("-1")) {
            return;
        }
        JSONObject person;
        try {
            person = new JSONObject(response);
            user_Full_Name = person.isNull("fullname") ? "" : person
                    .getString("fullname");
            user_profile_imagefile_name = person.isNull("userprofileimagefilename") ? "" : person
                    .getString("userprofileimagefilename");
            has_user_profile = person.isNull("has_user_profile") ? 0 : person.getInt("has_user_profile");
            user_profile_image_path = person.isNull("userprofileimagepath") ? "" : person.getString("userprofileimagepath");
            user_Email = person.isNull("email") ? "" : person.getString("email");
            user_profile_image_Rotate = person.isNull("image_rotation") ? 0 : person.getInt("image_rotation");
            mTvUserName.setText(user_Full_Name);
            mTvEmail.setText(user_Email);
            if (TextUtils.isEmpty(user_profile_imagefile_name) && TextUtils.isEmpty(user_profile_image_path)) {
            } else {
                String url = Constants_user.GET_USER_PROFILE_IMAGE_FILE_NAME_URL + user_profile_image_path +
                        user_profile_imagefile_name;
                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                if (Build.VERSION.SDK_INT < 11) {
                                    Toast.makeText(Dashboard_act.this, getString(R.string.api11not), Toast.LENGTH_LONG).show();
                                } else {
                                    mIvProfileImage.setImageBitmap(bitmap);
                                }
                            }
                        }, 0, 0, null,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                mIvProfileImage.setBackgroundResource(R.mipmap.ic_unknowuser);
                            }
                        });
                MyApplication.getInstance().addToRequestQueue(request);
            }
        } catch (Exception e) {
            SSLog.e(TAG, "getMyProfile", e);
        }
    }

    public void onClickGoHome(View v) {
        Intent intent = new Intent(Dashboard_act.this, FriendPool_act.class);
        CAddress oHomeAddress = CGlobals_user.getInstance().getHomeAddress(this);
        Location currentLocation = CGlobals_user.getInstance().getBestLocation(Dashboard_act.this);
        float[] dist = new float[1];
        if (oHomeAddress == null) {
            Toast.makeText(Dashboard_act.this, "Use the left menu at the top, go to Settings and set your Home address",
                    Toast.LENGTH_LONG).show();
            Intent intent1 = new Intent(Dashboard_act.this, Settings_act.class);
            startActivity(intent1);
            return;
        } else {
            if (currentLocation != null) {
                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                        oHomeAddress.getLatitude(),
                        oHomeAddress.getLongitude(), dist);
                if (dist[0] < 1000) {
                    Toast.makeText(Dashboard_act.this, "You are already close to home",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        if (currentLocation != null) {
            intent.putExtra("DEST", "HOME");
        } else {
            Toast.makeText(Dashboard_act.this, "Cannot use this feature, location not available!",
                    Toast.LENGTH_LONG).show();
        }
        startActivity(intent);
    }

    public void onClickGoWork(View v) {
        Intent intent = new Intent(Dashboard_act.this, FriendPool_act.class);
        CAddress oWorkAddress = CGlobals_user.getInstance().getWorkAddress(Dashboard_act.this);
        Location currentLocation = CGlobals_user.getInstance().getBestLocation(Dashboard_act.this);
        float[] dist = new float[1];
        if (oWorkAddress == null) {
            Toast.makeText(Dashboard_act.this, "Use the left menu at the top, go to Settings and set your Work address",
                    Toast.LENGTH_LONG).show();
            Intent intent1 = new Intent(Dashboard_act.this, Settings_act.class);
            startActivity(intent1);
            return;
        } else {
            if (currentLocation != null) {
                Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                        oWorkAddress.getLatitude(),
                        oWorkAddress.getLongitude(), dist);
                if (dist[0] < 1000) {
                    Toast.makeText(Dashboard_act.this, "You are already close to work",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        if (currentLocation != null) {

            intent.putExtra("DEST", "WORK");
        } else {
            Toast.makeText(Dashboard_act.this, "Cannot use this feature, location not available!",
                    Toast.LENGTH_LONG).show();
        }
        startActivity(intent);
    }


    public boolean onTouch(View v, MotionEvent ev) {
        boolean handledHere = false;

        final int action = ev.getAction();

        final int evX = (int) ev.getX();
        final int evY = (int) ev.getY();
        int nextImage = -1; // resource id of the next image to display
        // If we cannot find the imageView, return.
        ImageView imageView = (ImageView) v.findViewById(R.id.image);
        if (imageView == null) return false;
        // When the action is Down, see if we should show the "pressed" image for the default image.
        // We do this when the default image is showing. That condition is detectable by looking at the
        // tag of the view. If it is null or contains the resource number of the default image, display the pressed image.
        Integer tagNum = (Integer) imageView.getTag();
        int currentResource = (tagNum == null) ? R.drawable.jijo_dashboard_background : tagNum.intValue();
        // Now that we know the current resource being displayed we can handle the DOWN and UP events.
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (currentResource == R.drawable.jijo_dashboard_background) {
                    handledHere = true;
                } else handledHere = true;
                break;
            case MotionEvent.ACTION_UP:
                // On the UP, we do the click action.
                // The hidden image (image_areas) has three different hotspots on it.
                // The colors are red, blue, and yellow.
                // Use image_areas to determine which region the user touched.
                int touchColor = getHotspotColor(R.id.image_areas, evX, evY);
                // Compare the touchColor to the expected values. Switch to a different image, depending on what color was touched.
                // Note that we use a Color Tool object to test whether the observed color is close enough to the real color to
                // count as a match. We do this because colors on the screen do not match the map exactly because of scaling and
                // varying pixel density.
                ColorTool ct = new ColorTool();
                int tolerance = 25;
                if (ct.closeMatch(Color.CYAN, touchColor, tolerance)) {
                    onClickGoHome(v);
                } else if (ct.closeMatch(Color.MAGENTA, touchColor, tolerance)) {
                    Intent intent = new Intent(Dashboard_act.this,
                            RecentTripUser_act.class);
                    startActivity(intent);
                } else if (ct.closeMatch(Color.YELLOW, touchColor, tolerance)) {
                    Intent intent = new Intent(Dashboard_act.this,
                            Active_trips_act.class);
                    startActivity(intent);
                } else if (ct.closeMatch(Color.GREEN, touchColor, tolerance)) {
                    onClickGoWork(v);
                } else if (ct.closeMatch(Color.rgb(255, 142, 54), touchColor, tolerance)) {
                    Intent intent = new Intent(Dashboard_act.this,
                            GetaCab_act.class);
                    startActivity(intent);
                }
                // If the next image is the same as the last image, go back to the default.
                if (currentResource == nextImage) {
                    nextImage = R.drawable.jijo_dashboard_background;
                }
                handledHere = true;
                break;
            default:
                handledHere = false;
        } // end switch

        if (handledHere) {
            if (nextImage > 0) {
                imageView.setImageResource(nextImage);
                imageView.setTag(nextImage);
            }
        }
        return handledHere;
    }

    public int getHotspotColor(int hotspotId, int x, int y) {
        try {
            ImageView img = (ImageView) findViewById(hotspotId);
            if (img == null) {
                Log.d("ImageAreasActivity", "Hot spot image not found");
                return 0;
            } else {
                img.setDrawingCacheEnabled(true);
                hotspots = Bitmap.createBitmap(img.getDrawingCache());
                if (hotspots == null) {
                    Log.d("ImageAreasActivity", "Hot spot bitmap was not created");
                    return 0;
                } else {
                    img.setDrawingCacheEnabled(false);
                    int i = hotspots.getPixel(x, y);
                    hotspots = null;
                    return i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private MyLocation.LocationResult onLocationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            if (location != null) {
                CGlobals_user.getInstance().setMyLocation(location, false);
                CGlobals_user.getInstance().sendUpdatePosition(
                        CGlobals_user.getInstance().getMyLocation(Dashboard_act.this), getApplicationContext());
            }
        }
    };

    public void runScheduleTrip(String startaddress, double startlat, double startlng, String destaddress, double destlat, double destlng) {
        Intent intent = new Intent(Dashboard_act.this, FriendPool_act.class);
        CAddress ostartAddress = new CAddress(startaddress, startlat, startlng);
        CAddress odestAddress = new CAddress(destaddress, destlat, destlng);
        Gson gson = new Gson();
        final String json1 = gson.toJson(ostartAddress);
        final String json2 = gson.toJson(odestAddress);

        Location currentLocation = CGlobals_user.getInstance().getBestLocation(Dashboard_act.this);
        float[] dist = new float[1];
        if (ostartAddress == null) {
            Toast.makeText(Dashboard_act.this, "Use the left menu at the top, go to Settings and set your Home address",
                    Toast.LENGTH_LONG).show();
            return;
        } else {
            if (currentLocation != null) {
                Location.distanceBetween(ostartAddress.getLatitude(), ostartAddress.getLongitude(),
                        odestAddress.getLatitude(),
                        odestAddress.getLongitude(), dist);

                if (dist[0] < 1000) {
                    Toast.makeText(Dashboard_act.this, "You are already close to home",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        if (currentLocation != null) {
            intent.putExtra("SCHEDULETRIP", "1");
            intent.putExtra("START", json1);
            intent.putExtra("DESTINATION", json2);
        } else {
            Toast.makeText(Dashboard_act.this, "Cannot use this feature, location not available!",
                    Toast.LENGTH_LONG).show();
        }
        startActivity(intent);
    }

    // Was this trip already run today?
    boolean tobeRunToday() {
        String json = MyApplication.getInstance().getPersistentPreference().
                getString(Constants_user.PREF_SCHEDULED_TRIPS, "");
        if (TextUtils.isEmpty(json)) {
            return true;
        }
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<List<CTrip>>() {
            }.getType();
            ArrayList<CTrip> pPResultsList = gson.fromJson(json, type);
            Date today = new Date();
            for (CTrip sr : pPResultsList) {
                Date lastRunDate = sr.getLastRunDate();
                if (lastRunDate.equals(today)) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

} // SSDashboard

