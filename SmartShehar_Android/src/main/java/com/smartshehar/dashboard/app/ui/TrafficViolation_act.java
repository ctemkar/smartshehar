package com.smartshehar.dashboard.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.smartshehar.dashboard.app.CIssue;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;


public class TrafficViolation_act extends IssueAct/*Issue_act*/ {

    public static String TAG = "TrafficAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ivNoPlate.setImageResource(R.drawable.ic_traffice_issue);

        etVehicleNo.setVisibility(View.VISIBLE);
        findViewById(R.id.rlWard).setVisibility(View.GONE);
        txtWard.setVisibility(View.GONE);
        tvWard.setVisibility(View.GONE);
        txtTypeOffence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(TrafficViolation_act.this, ActTrafficCategorySelection.class);
                startActivityForResult(intent, Constants_lib_ss.TYPELIST_SUBTYPE);
            }
        });
        btnSubmitIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    issueAddress = etIssueAdd.getText().toString();
                    issueCat = txtTypeOffence.getText().toString();
                    mVehicleNo = etVehicleNo.getText().toString();
                    if (TextUtils.isEmpty(issueAddress)) {
                        showSnackbar( "This complaint can not be submitted wihout address");
                        return;
                    }
                    if(TextUtils.isEmpty(issue_time))
                    {
                        showSnackbar( "This complaint can not be submitted wihout time");
                        return;
                    }
                    if (TextUtils.isEmpty(issueCat)) {
                        showSnackbar( "This complaint can not be submitted wihout Issue");
                        return;
                    }
                    if (imageVerification) {
                        if (baOne == null) {
                            showSnackbar( "This complaint can not be submitted wihout a picture");
                            return;
                        }
                    }
                    if(vehicleNumber){
                        if (TextUtils.isEmpty(mVehicleNo)) {
                            showSnackbar( "This complaint can not be submitted wihout Vehicle no.");
                            return;
                        }
                    }
                    btnSubmitIssue.setVisibility(View.GONE);
                    pbSubmit.setVisibility(View.VISIBLE);
                    isBtnDoneFlag = true;
                    db.addIssue(new CIssue(iUniqueKey, issueAddress,
                            issue_time, sIssueDescription, mVehicleNo, 0,
                            sIssueitemcode, latitude, longitude, mWardno, "", "", "", cityname,
                            sublocality, postalcode, route, neighborhood, administrative_area_level_2, administrative_area_level_1));
                    if (!Connectivity.checkConnected(TrafficViolation_act.this)) {
                        db.sentToserver(iUniqueKey, 0);
                        showInformationalDialog(TrafficViolation_act.this, getString(R.string.internet_error_submitting_issue));
                    } else {
                        checkAddrTime();
                    }
                } catch (Exception e) {
                    SSLog.e(TAG, "setOnClickListener ", e);
                }
            }
        });
    }

    protected String getTrafficType() {
        return "TR_";
    }



}