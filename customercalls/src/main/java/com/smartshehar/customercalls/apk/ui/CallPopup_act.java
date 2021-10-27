package com.smartshehar.customercalls.apk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.customercalls.apk.CCallInfo;
import com.smartshehar.customercalls.apk.R;

import java.lang.reflect.Type;

/**
 * Created by ctemkar on 08/03/2016.
 * Shows contact info after call is over
 */
public class CallPopup_act extends Activity {
    CCallInfo moCallInfo = null;

    TextView mTvTitle, mTvPhoneNo;
    TextView mTvAddressLine1, mTvLandmark1;
    public static Activity CallPopA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CallPopA = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.call_popup);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        String sPhoneNo;
//        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Toast.makeText(getApplicationContext(), "Cannot find information about this customer",
                        Toast.LENGTH_SHORT).show();
                return;
            } else {
                Type type = new TypeToken<CCallInfo>() {
                }.getType();
                Gson gson = new Gson();
                moCallInfo = gson.fromJson(extras.getString("callinfo"), type);
            }

        if (moCallInfo != null) {

            mTvTitle = (TextView) findViewById(R.id.tvTitle);
            mTvPhoneNo = (TextView) findViewById(R.id.tvPhoneNo);
            mTvAddressLine1 = (TextView) findViewById(R.id.tvAddressLine1);
            mTvLandmark1 = (TextView) findViewById(R.id.tvLandmark1);
            mTvTitle.setText(moCallInfo.getName());
            mTvPhoneNo.setText(moCallInfo.getPhoneNo());
            mTvAddressLine1.setText(moCallInfo.getAddress());
            mTvLandmark1.setText(moCallInfo.getLandmark1());
            findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }

    }
    // since activity is Singletop and we need to get the latest extras
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //now getIntent() should always return the last received intent
    }

}