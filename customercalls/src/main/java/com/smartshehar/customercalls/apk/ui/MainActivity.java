package com.smartshehar.customercalls.apk.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.smartshehar.customercalls.apk.CGlobals_cc;
import com.smartshehar.customercalls.apk.Constants_cc;
import com.smartshehar.customercalls.apk.R;
import com.smartshehar.customercalls.apk.SyncDataService;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "mainActivity: ";
    ListView mListView;
    SimpleCursorAdapter mAdapter;
    ProgressDialog mProgressDialog;
    Connectivity mConnectivity;
    ImageView mBtnCallLog, mBtnCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_listview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        CGlobals_lib_ss.getInstance().init(MainActivity.this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.getWindow().setGravity(Gravity.BOTTOM);
        mProgressDialog.setMessage("Connecting. Please wait ...");
        sendUserAccess();
        // start service to sync data //
        startService(new Intent(this, SyncDataService.class));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();*/
                    Intent intent = new Intent(MainActivity.this, EditCustomer_act.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
        mBtnCallLog = (ImageView) findViewById(R.id.btnCallLog);
        mBtnCustomer = (ImageView) findViewById(R.id.btnCustomer);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle); // setDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }
        mListView = (ListView) findViewById(R.id.listCallLog);
        addCallLogFrag();
        mBtnCallLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCallLogFrag();
            }
        });
        mBtnCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCustomerFrag();
            }
        });
    }

    private void addCallLogFrag() {
        mBtnCallLog.setImageResource(R.mipmap.ic_calllog_blue);
        mBtnCustomer.setImageResource(R.mipmap.ic_customer_gray);
        Customer_Frag frag = new Customer_Frag();
        if(frag!=null)
            getSupportFragmentManager().beginTransaction().
                    remove(frag).commit();

        CallLog_Frag myFragment = new CallLog_Frag();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentPlaceHolder, myFragment);
        transaction.commit();

    }

    private void addCustomerFrag() {
        mBtnCallLog.setImageResource(R.mipmap.ic_calllog_gray);
        mBtnCustomer.setImageResource(R.mipmap.ic_customer_blue);

        CallLog_Frag frag = new CallLog_Frag();
        if(frag!=null)
        getSupportFragmentManager().beginTransaction().remove(frag).commit();


        Customer_Frag myFragment = new Customer_Frag();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentPlaceHolder, myFragment);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_sync) {
            CGlobals_cc.syncCallLog(MainActivity.this.getApplicationContext());
//            if (Connectivity.checkConnected(MainActivity.this)) {
//                if (!mConnectivity.connError(MainActivity.this.getApplicationContext())) {
//                    CGlobals_cc.syncCallLog(MainActivity.this.getApplicationContext());
//                }
//            }else{
//                Toast.makeText(MainActivity.this, getString(R.string.interneterror),Toast.LENGTH_LONG).show();
//            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    public void sendUserAccess() {

        mProgressDialog.setMessage("Connecting...");
        mProgressDialog.show();
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_cc.USER_ACCESS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        userAccessSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                userAccessError(error);
                SSLog_SS.e(TAG, "sendUserAccess :-   " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("app", Constants_cc.APP_CODE);
                params = CGlobals_lib_ss.getInstance().getAllMobileParams(params,
                        Constants_cc.USER_ACCESS_URL, Constants_cc.APP_CODE, MainActivity.this);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                String url = Constants_cc.USER_ACCESS_URL + "?";
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                url = url + getParams;
                Log.i(TAG, url);
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, MainActivity.this);

    } // sendUserAccess

    private void userAccessSuccess(String response) {
        String sAppUserId, sStorePhoneId, sStoreId, sAccountId;
        int iAppUserId, iStorePhoneId, iStoreId, iAccountId; //, iAppUsageId = -1;
        try {
            if (!response.trim().equals("-1")) {
                cancelProgress();
                Log.d(TAG, "Useraccess_url - " + response);
                JSONObject jResponse = new JSONObject(response);
                Log.d(TAG, response);

                sAppUserId = jResponse.isNull("appuser_id") ? "-1" : jResponse
                        .getString("appuser_id");
                iAppUserId = Integer.parseInt(sAppUserId);
                sStorePhoneId = jResponse.isNull("store_phone_id") ? "-1" : jResponse
                        .getString("store_phone_id");
                iStorePhoneId = Integer.parseInt(sStorePhoneId);
                sStoreId = jResponse.isNull("store_id") ? "-1" : jResponse
                        .getString("store_id");
                iStoreId = Integer.parseInt(sStoreId);
                sAccountId = jResponse.isNull("account_id") ? "-1" : jResponse
                        .getString("account_id");
                iAccountId = Integer.parseInt(sAccountId);

                if (iStorePhoneId == -1)
                    failureAlert(getString(R.string.user_access_failed), getString(R.string.no_registration));

                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(MainActivity.this).putInt(Constants_lib_ss.PREF_APPUSERID, iAppUserId).commit();
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(MainActivity.this).putInt(Constants_lib_ss.PREF_STORE_PHONE_ID, iStorePhoneId).commit();
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(MainActivity.this).putInt(Constants_lib_ss.PREF_STORE_ID, iStoreId).commit();
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(MainActivity.this).putInt(Constants_lib_ss.PREF_ACCOUNT_ID, iAccountId).commit();
            }
        } catch (Exception e) {
            SSLog_SS.e(TAG, "sendUserAccess Response: " + response + e.getMessage());
        }
    } // userAccessSuccess

    private void userAccessError(VolleyError error) {
        Log.e(TAG, "User access failed: " + error.getMessage());
        cancelProgress();
    }

    private void failureAlert(String title, String msg) {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(msg);

        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        builder.show();

    }

    void cancelProgress() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }
    }
}