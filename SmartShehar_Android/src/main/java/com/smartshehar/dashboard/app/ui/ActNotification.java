package com.smartshehar.dashboard.app.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.smartshehar.dashboard.app.CGlobals_db;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import java.util.HashMap;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;

public class ActNotification extends AppCompatActivity {
    private static String TAG = "ActNotification";
    Connectivity mConnectivity;
    int iShowNotification ;
    SwitchCompat sc_notification_flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_notification);
        sc_notification_flag = (SwitchCompat) findViewById(R.id.sc_notification_flag);
        iShowNotification = CGlobals_lib_ss.getInstance()
                .getPersistentPreference(ActNotification.this).getInt(Constants_dp.PREF_SHOW_NOTIFICATION,1);
        if(iShowNotification==1)
        {
            sc_notification_flag.setChecked(true);
        }else {
            sc_notification_flag.setChecked(false);
        }
//        Bundle bundle = getIntent().getParcelableExtra("bundle");
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(ActNotification.this)) {
            if (!mConnectivity.connectionError(ActNotification.this)) {
                if (mConnectivity.isGPSEnable(ActNotification.this)) {
                    Log.d(TAG, "Internet Connection");
                }
            }
        }

        if (savedInstanceState == null) {
            FragNotification fragment = new FragNotification();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
        sc_notification_flag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    iShowNotification =1;
                }
                else{
                    iShowNotification =0;
                }
                CGlobals_lib_ss.getInstance()
                        .getPersistentPreferenceEditor(	ActNotification.this).putInt(Constants_dp.PREF_SHOW_NOTIFICATION, iShowNotification)
                        .commit();
                updateNotificationFlag();
            }
        });
        CGlobals_lib_ss.getInstance().getPersistentPreferenceEditor(ActNotification.this).
                putBoolean(Constants_dp.PREF_NOTIFICATION_CLEAR_FLAG, true).apply();
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ActNotification.this.finish();
    }



    private void updateNotificationFlag()
    {
        try {
            if (Connectivity.checkConnected(ActNotification.this)) {

                final String url = Constants_dp.UPDATE_NOTIFICATON_FLAG_URL;

                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                if (!response.trim().equals("-1")) {
                                    try {


                                        Log.d(TAG, "" + response);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        SSLog.e(TAG, "updateNotificationFlag ", error);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("notificationflag", String.valueOf(iShowNotification));
                        params = CGlobals_db.getInstance(ActNotification.this).getBasicMobileParams(params,
                                url, ActNotification.this);

                        return CGlobals_db.getInstance(ActNotification.this).checkParams(params);
                    }
                };
                CGlobals_db.getInstance(ActNotification.this).getRequestQueue(ActNotification.this).add(postRequest);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notification,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.refreshNotification:
                finish();
                overridePendingTransition( 0, 0);
                startActivity(getIntent());
                overridePendingTransition( 0, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
