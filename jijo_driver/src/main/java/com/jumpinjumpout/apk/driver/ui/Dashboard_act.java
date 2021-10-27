package com.jumpinjumpout.apk.driver.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.apk.driver.ActionBarAdapter;
import com.jumpinjumpout.apk.driver.CGlobals_driver;
import com.jumpinjumpout.apk.driver.Constants_driver;
import com.jumpinjumpout.apk.driver.MyApplication;
import com.jumpinjumpout.apk.driver.NavItem;
import com.jumpinjumpout.apk.driver.R;
import com.jumpinjumpout.apk.driver.VehicleResult;
import com.jumpinjumpout.apk.driver.ViewTimedDialog;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.CTrip;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.DatabaseHandler;
import com.jumpinjumpout.apk.lib.SSLog;
import com.jumpinjumpout.apk.lib.ui.ScheduleTripCreate_act;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CAddress;
import lib.app.util.Connectivity;
import lib.app.util.ui.ActFeedback;

/**
 * Created on 04-08-2015.
 */
public class Dashboard_act extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, TextToSpeech.OnInitListener {

    public static final String TAG = "Dashborad_act: ";
    String spVehicleid, spShiftid;
    public ImageView mbtnLogout;
    boolean loginLogoutFlag = false;
    private static final int VEHICLE_VERIFICATION_CODE = 1;
    String sShiftId, sVehicle, setAcceptRejectFlag = "0";
    private Switch mButtonToggle;
    boolean switchForHire = false;
    int booking_driver_id, cab_trip_id;
    protected Handler handler = new Handler();
    Connectivity mConnectivity;
    GoogleApiClient mGoogleApiClient;
    DatabaseHandler db;
    boolean checkDay;
    boolean isacceptreject = false;

    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();
    ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    RelativeLayout mDrawerPane;
    ArrayList<CTrip> pPResultsList;
    private TextToSpeech mTts;
    //private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String sVDetails = MyApplication.getInstance().getPersistentPreference().getString("PERF_VEHICLE_DETAILS", "");
        if (!TextUtils.isEmpty(sVDetails)) {
            Type type = new TypeToken<VehicleResult>() {
            }.getType();
            VehicleResult vehicleResult = new Gson().fromJson(sVDetails, type);
            if (vehicleResult.getVehicle_Category().equals(Constants_driver.VEHICLE_CATEGORY_LONG_DISTANCE_CAR)) {
                Intent intent = new Intent(Dashboard_act.this, ForHireSharedTrip_act.class);
                startActivity(intent);
                finish();
            }
        }
        setContentView(R.layout.dashboard_act);
        findViewById(R.id.llForHire).setVisibility(View.VISIBLE);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        mConnectivity = new Connectivity();
        db = new DatabaseHandler(this);
        mbtnLogout = (ImageView) findViewById(R.id.btnLogout);
        mbtnLogout.setVisibility(View.VISIBLE);
        mbtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Dashboard_act.this);
                alertDialog.setTitle("Are you sure you want to logout?");
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        goLogout();
                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                if (!Dashboard_act.this.isFinishing()) {
                    alertDialog.show();
                }
            }
        });

        mButtonToggle = (Switch) findViewById(R.id.btntoggle);

        mButtonToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bn) {

                MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(Constants_driver.PREF_SET_SWITCH_FLAG, bn);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                if (bn) {
                    findViewById(R.id.btncreatetrip).setVisibility(View.GONE);
                    findViewById(R.id.ivforhire).setBackgroundResource(R.drawable.ic_forhire);
                } else {
                    findViewById(R.id.btncreatetrip).setVisibility(View.VISIBLE);
                    findViewById(R.id.ivforhire).setBackgroundResource(R.drawable.ic_busy);
                }
                sendFlag(Constants_driver.IS_FOR_HIRE, bn, Dashboard_act.this);
            }
        });
        //Action Menu Bar
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mNavItems.add(new NavItem("Add Schedule", "", R.drawable.ic_schedule));
        mNavItems.add(new NavItem("Feedback", "Contact Us,Rate Us, FB,...", R.mipmap.ic_feedback));

        // DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Populate the Navigtion Drawer with options
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
        /*BitmapDrawable background = new BitmapDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.actionbar_image));
        background.setTileModeX(android.graphics.Shader.TileMode.REPEAT);
        ab.setBackgroundDrawable(background);*/
        ActionBar ab = getActionBar();
        ab.setTitle("");
        ab.setDisplayShowHomeEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
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
        CGlobals_lib.showGPSDialog = true;
        mTts = new TextToSpeech(this, this);
    }

    private void selectItemFromDrawer(int position) {
        mDrawerList.setItemChecked(position, true);
        switch (position) {
            case 0:
                if (!mConnectivity.connectionError(Dashboard_act.this, getString(R.string.app_label))) {
                    Intent intent_ScheduleTrip = new Intent(Dashboard_act.this,
                            ScheduleTripCreate_act.class);
                    startActivity(intent_ScheduleTrip);
                }
                break;

            case 1:
                if (!mConnectivity.connectionError(Dashboard_act.this, getString(R.string.app_label))) {
                    String jijo_Website = "http://www.jumpinjumpout.com";
                    String jijo_Twitter = "https://twitter.com/jumpinjumpout";
                    String jijo_Feacbook = "https://www.facebook.com/JumpInJumpOut";
                    String jijo_Feedback_email = "userfeedback@jumpinjumpout.com";
                    Intent intent_Feedback = new Intent(Dashboard_act.this,
                            ActFeedback.class);
                    intent_Feedback.putExtra("app_label",
                            getString(R.string.app_label));
                    intent_Feedback.putExtra("appCode", getString(R.string.appCode));
                    intent_Feedback.putExtra("versionName",
                            CGlobals_driver.getInstance().mPackageInfo.versionName);

                    intent_Feedback.putExtra("USER_FEEDBACK_EMAIL", jijo_Feedback_email);
                    intent_Feedback.putExtra("FACEBOOK_POST", jijo_Feacbook);
                    intent_Feedback.putExtra("TWITTER_POST", jijo_Twitter);
                    intent_Feedback.putExtra("WEB_SITE", jijo_Website);
                    intent_Feedback.putExtra("PACKAGE_NAME", "market://details?id=" + getPackageName());
                    startActivity(intent_Feedback);
                }
                break;
        }
        // Close the drawer
        mDrawerLayout.closeDrawer(mDrawerPane);
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
                            getString(Constants_driver.PREF_SCHEDULED_TRIPS_I, "");
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
                                putString(Constants_driver.PREF_SCHEDULED_TRIPS, json);
                        MyApplication.getInstance().getPersistentPreferenceEditor().
                                putString(Constants_driver.PREF_SCHEDULED_TRIPS_I, jsonI);
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
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS_I, jsonI);
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
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS_I, jsonI);
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
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS_I, jsonI);
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
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS_I, jsonI);
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
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS_I, jsonI);
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
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS_I, jsonI);
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
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS, json);
                                MyApplication.getInstance().getPersistentPreferenceEditor().
                                        putString(Constants_driver.PREF_SCHEDULED_TRIPS_I, jsonI);
                                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                            }
                        }
                    }
                } else {
                    handler.postDelayed(runnableUpdatePosition,
                            Constants_driver.DRIVER_UPDATE_INTERVAL_GETACAB);
                }
            }
        }
    }


    protected Runnable runnableUpdatePosition = new Runnable() {
        @Override
        public void run() {
            loginLogoutFlag = MyApplication.getInstance().getPersistentPreference()
                    .getBoolean(Constants_driver.PREF_LOGIN_LOGOUT_FLAG, false);
            if (loginLogoutFlag) {
                checkScheduledTrip();
            }
        }
    };

    protected Runnable runnable = new Runnable() {
        @Override
        public void run() {
            switchForHire = MyApplication.getInstance().getPersistentPreference().
                    getBoolean(Constants_driver.PREF_SET_SWITCH_FLAG, false);
            if (switchForHire) {
                goForeHireBook();
            } else {
                handler.postDelayed(runnable,
                        Constants_driver.DRIVER_UPDATE_INTERVAL_GETACAB);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        pPResultsList = db.getSchdhuleList();
        //CGlobals_lib.getInstance().turnGPSOn1(Dashboard_act.this, mGoogleApiClient);
        vehicleVerification();
        switchForHire = MyApplication.getInstance().getPersistentPreference().
                getBoolean(Constants_driver.PREF_SET_SWITCH_FLAG, false);
        handler.postDelayed(runnableUpdatePosition,
                Constants_driver.DRIVER_UPDATE_INTERVAL_GETACAB);
        handler.postDelayed(runnable,
                Constants_driver.DRIVER_UPDATE_INTERVAL_GETACAB);
        if (switchForHire) {
            findViewById(R.id.ivforhire).setBackgroundResource(R.drawable.ic_forhire);
        } else {
            findViewById(R.id.ivforhire).setBackgroundResource(R.drawable.ic_busy);
        }
        mButtonToggle = (Switch) findViewById(R.id.btntoggle);
        mButtonToggle.setChecked(switchForHire);
        loginLogoutFlag = MyApplication.getInstance().getPersistentPreference()
                .getBoolean(Constants_driver.PREF_LOGIN_LOGOUT_FLAG, false);
        if (loginLogoutFlag) {
            checkScheduledTrip();
        }
    }

    @Override
    protected void onPause() {
        /*if (null != mp) {
            mp.release();
        }*/
        super.onPause();
    }

    private void goLogout() {

        spShiftid = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_driver.PREF_SHIFT_ID, "");

        spVehicleid = MyApplication.getInstance().getPersistentPreference()
                .getString(Constants_driver.PREF_SHIFT_ID, "");

        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.COMMERCIAL_DRIVER_END_SHIFT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getLogout(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(Dashboard_act.this, sErr, Toast.LENGTH_SHORT).show();
                try {
                    SSLog.d(TAG,
                            "Failed to mVehicleNoVerify :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "mVehicleNoVerify - " + sErr,
                            error.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("shiftid", spShiftid);
                params.put("commercialvehicleid", spVehicleid);
                params = CGlobals_driver.getInstance().getMinAllMobileParams(params,
                        Constants_driver.COMMERCIAL_DRIVER_END_SHIFT_URL);
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void getLogout(String response) {
        if (response.trim().equals("-1") && TextUtils.isEmpty(response.trim())) {
            Toast.makeText(Dashboard_act.this, "!server error", Toast.LENGTH_SHORT).show();
            return;
        }
        MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(Constants_driver.PREF_SET_SWITCH_FLAG, false);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putBoolean(Constants_driver.PREF_LOGIN_LOGOUT_FLAG, false);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
        System.exit(0);
    }

    public void onClickCreateTrip(View view) {
        if (!mConnectivity.connectionError(Dashboard_act.this, getString(R.string.app_label))) {
            Intent intent = new Intent(Dashboard_act.this, RecentTripDriver_act.class);
            startActivity(intent);
        }
    }

    public void sendFlag(final String flagName, final boolean flagValue, final Context context) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.SET_APPUSER_FLAG_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                SSLog.e(TAG, "sendFlag :-   " + flagName,
                        error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("flagname", flagName);
                params.put("flagvalue", flagValue ? "1" : "0");
                params = CGlobals_driver.getInstance().getAllMobileParams(params, Constants_driver.SET_APPUSER_FLAG_URL, context);
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    } // sendForHire

    private void goForeHireBook() {

        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.CHECKED_IF_BOOK_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        goForeHire(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handler.postDelayed(runnable,
                        Constants_driver.DRIVER_UPDATE_INTERVAL_GETACAB);
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(Dashboard_act.this, sErr, Toast.LENGTH_SHORT).show();
                try {
                    SSLog.d(TAG,
                            "Failed to mVehicleNoVerify :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "mVehicleNoVerify - " + sErr,
                            error.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params = CGlobals_driver.getInstance().getMinAllMobileParams(params,
                        Constants_driver.CHECKED_IF_BOOK_URL);
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    private void goForeHire(String response) {

        if (TextUtils.isEmpty(response)) {
            handler.postDelayed(runnable,
                    Constants_driver.DRIVER_UPDATE_INTERVAL_GETACAB);
            return;
        }
        if (response.equals("-1")) {
            handler.postDelayed(runnable,
                    Constants_driver.DRIVER_UPDATE_INTERVAL_GETACAB);
            return;
        }
        try {
            String msg = "accept customer request";
            switchForHire = MyApplication.getInstance().getPersistentPreference().
                    getBoolean(Constants_driver.PREF_SET_SWITCH_FLAG, false);
            if (switchForHire && loginLogoutFlag && !isacceptreject) {
                Gson gson = new Gson();
                String json = gson.toJson(response);
                MyApplication.getInstance().getPersistentPreferenceEditor().
                        putString(Constants_driver.PREF_SAVE_RESPONSE_PASSENGER, json);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                JSONObject jResponse = new JSONObject(response);
                booking_driver_id = jResponse.isNull("booked_driver_id") ? 0
                        : jResponse.getInt("booked_driver_id");
                cab_trip_id = jResponse.isNull("cab_trip_id") ? 0
                        : jResponse.getInt("cab_trip_id");
                MyApplication.getInstance().getPersistentPreferenceEditor().
                        putInt("CAB_TRIP_ID_DRIVER", cab_trip_id);
                MyApplication.getInstance().getPersistentPreferenceEditor().commit();
                isacceptreject = true;
                String sSpeech = "Passenger Waiting. Please accept ride";
                say(sSpeech);
                Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                v.vibrate(5000);
                int resId = R.raw.loude;
                /*if (mp != null) {
                    mp.release();
                }
                mp = MediaPlayer.create(this, resId);
                mp.start();*/
                final ViewTimedDialog timedDialog = new ViewTimedDialog(this, "Customer Request",
                        msg,
                        "Cancel", "Accept", 20);
                timedDialog.show();
                timedDialog.setOnDialogListener(new ViewTimedDialog.DialogListener() {

                    @Override
                    public void onPositiveClick() {
                        /*if (null != mp) {
                            mp.release();
                        }*/
                        timedDialog.dismissDialog();
                        setAcceptRejectFlag = "0";
                        isacceptreject = false;
                        setAcceptReject(setAcceptRejectFlag, booking_driver_id, cab_trip_id);
                        handler.postDelayed(runnable,
                                Constants_driver.DRIVER_UPDATE_INTERVAL_GETACAB);
                    }

                    @Override
                    public void onNegativeClick() {
                        try {
                            // switchForHire = false;
                           /* MyApplication.getInstance().getPersistentPreferenceEditor().
                                    putBoolean(Constants_driver.PREF_SET_SWITCH_FLAG, switchForHire);*/
                            isacceptreject = false;
                            Intent intent = new Intent(Dashboard_act.this, ForHire_act.class);
                            startActivity(intent);
                            timedDialog.dismissDialog();
                            setAcceptRejectFlag = "1";
                            setAcceptReject(setAcceptRejectFlag, booking_driver_id, cab_trip_id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
            } else {
                handler.postDelayed(runnable,
                        Constants_driver.DRIVER_UPDATE_INTERVAL_GETACAB);
            }
        } catch (Exception e) {
            handler.postDelayed(runnable,
                    Constants_driver.DRIVER_UPDATE_INTERVAL_GETACAB);
            e.printStackTrace();
        }
    }

    private void vehicleVerification() {
        loginLogoutFlag = MyApplication.getInstance().getPersistentPreference()
                .getBoolean(Constants_driver.PREF_LOGIN_LOGOUT_FLAG, false);

        if (!loginLogoutFlag) {
            Intent intent1 = new Intent(Dashboard_act.this, MasterLogin_Act.class);
            startActivity(intent1);
            finish();
        }

    }

   /* @Override
    protected void onStop() {
        super.onStop();
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putBoolean(Constants_driver.PREF_LOGIN_LOGOUT_FLAG, false);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(Constants_driver.PREF_SET_SWITCH_FLAG, false);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
    }*/

    @Override
    public void onDestroy() {
       /* if (null != mp) {
            mp.release();
        }*/
        super.onDestroy();
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        MyApplication.getInstance().getPersistentPreferenceEditor()
                .putBoolean(Constants_driver.PREF_LOGIN_LOGOUT_FLAG, false);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
        MyApplication.getInstance().getPersistentPreferenceEditor().putBoolean(Constants_driver.PREF_SET_SWITCH_FLAG, false);
        MyApplication.getInstance().getPersistentPreferenceEditor().commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CGlobals_lib.REQUEST_LOCATION) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // All required changes were successfully made
                    Log.d(TAG, "YES");

                    break;
                case Activity.RESULT_CANCELED:
                    // The user was asked to change settings, but chose not to
                    Log.d(TAG, "NO");
                    CGlobals_lib.showGPSDialog = false;
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Dashboard_act.this);
        alertDialog.setTitle("Please press log out button to log out");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        if (!Dashboard_act.this.isFinishing()) {
            alertDialog.show();
        }
    }

    private void setAcceptReject(final String setAcceptRejectFlag, final int bookingdriver_id, final int cabtrip_id) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_driver.ACCEPT_REJECT_CAB_TRIP_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(Dashboard_act.this, sErr, Toast.LENGTH_SHORT).show();
                try {
                    SSLog.d(TAG,
                            "Failed to mVehicleNoVerify :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "mVehicleNoVerify - " + sErr,
                            error.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("bookeddriverid", String.valueOf(bookingdriver_id));
                params.put("accepted", setAcceptRejectFlag);
                params.put("cabtripid", String.valueOf(cabtrip_id));
                params = CGlobals_driver.getInstance().getMinAllMobileParams(params,
                        Constants_driver.ACCEPT_REJECT_CAB_TRIP_URL);
                return CGlobals_driver.getInstance().checkParams(params);
            }
        };
        MyApplication.getInstance().addVolleyRequest(postRequest, false);
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
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
                break;
            /*case R.id.menu_schedule:
                Intent intent4 = new Intent(Dashboard_act.this,
                        ScheduleTripCreate_act.class);
                startActivity(intent4);
                break;
            case R.id.menu_trackmybus:
                Intent intent6 = new Intent(Dashboard_act.this,
                        TrackMyBus_act.class);
                startActivity(intent6);
                break;*/
            case R.id.menu_share:
                CGlobals_driver.getInstance().getMyLocation(Dashboard_act.this);
                String message = getString(R.string.androidAppLink);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(share, "Share Jump in Jump out"));
                break;
        }
        return super.onOptionsItemSelected(item);
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

    public void runScheduleTrip(String startaddress, double startlat, double startlng, String destaddress, double destlat, double destlng) {
        Intent intent = new Intent(Dashboard_act.this, ForHireSharedTrip_act.class);
        CAddress ostartAddress = new CAddress(startaddress, startlat, startlng);
        CAddress odestAddress = new CAddress(destaddress, destlat, destlng);
        Gson gson = new Gson();
        final String json1 = gson.toJson(ostartAddress);
        final String json2 = gson.toJson(odestAddress);

        Location currentLocation = CGlobals_driver.getInstance().getBestLocation(Dashboard_act.this);
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
            intent.putExtra("DEST", json2);
        } else {
            Toast.makeText(Dashboard_act.this, "Cannot use this feature, location not available!",
                    Toast.LENGTH_LONG).show();

        }
        startActivity(intent);
    }

    // Was this trip already run today?
    boolean tobeRunToday() {
        String json = MyApplication.getInstance().getPersistentPreference().
                getString(Constants_driver.PREF_SCHEDULED_TRIPS, "");
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

    @SuppressWarnings("deprecation")
    void say(String sSay) {
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                String.valueOf(AudioManager.STREAM_NOTIFICATION));
        mTts.speak(sSay, TextToSpeech.QUEUE_ADD, hash);

    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Change this to match your
            // locale
            mTts.setLanguage(Locale.US);
        } else {
        }
    }
}
