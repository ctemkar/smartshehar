package com.smartshehar.dashboard.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.smartshehar.dashboard.app.CIssue;
import com.smartshehar.dashboard.app.Constants_dp;
import com.smartshehar.dashboard.app.ListAdapter;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;

public class MunicipalViolation_act extends IssueAct {

    public static String TAG = "MunicipalAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ivNoPlate.setImageResource(R.drawable.ic_municipal_issue);

        getWardList();
        etVehicleNo.setVisibility(View.GONE);
        tvWard.setVisibility(View.VISIBLE);
        findViewById(R.id.changeWard).setVisibility(View.VISIBLE);
        txtTypeOffence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MunicipalViolation_act.this, ActMunicipalCategorySelection.class);
                startActivityForResult(intent, Constants_lib_ss.TYPELIST_SUBTYPE);
            }
        });
        findViewById(R.id.changeWard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.rlWard).setVisibility(View.VISIBLE);
                findViewById(R.id.changeWard).setVisibility(View.GONE);
                tvWard.setVisibility(View.GONE);
            }
        });
        btnSubmitIssue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    issueAddress = etIssueAdd.getText().toString();
                    mWardno = spnWardNo.getSelectedItem().toString();
                    issueCat = txtTypeOffence.getText().toString();

                    if (TextUtils.isEmpty(issueAddress)) {

                        showSnackbar("This complaint can not be submitted wihout address");
                        return;
                    }
                    if (TextUtils.isEmpty(issue_time)) {
                        showSnackbar("This complaint can not be submitted wihout time");
                        return;
                    }
                    if (TextUtils.isEmpty(issueCat)) {
                        showSnackbar("This complaint can not be submitted wihout Issue");
                        return;
                    }
                    if (imageVerification) {
                        if (baOne == null) {
                            showSnackbar("This complaint can not be submitted wihout a picture");
                            return;
                        }
                    }
                    if (mWardno.equalsIgnoreCase("Select")) {
                        showSnackbar("This complaint can not be submitted wihout a Ward no.");
                        return;
                    }
                    btnSubmitIssue.setVisibility(View.GONE);
                    pbSubmit.setVisibility(View.VISIBLE);
                    isBtnDoneFlag = true;
                    db.addIssue(new CIssue(iUniqueKey, issueAddress,
                            issue_time, sIssueDescription, mVehicleNo, 0,
                            sIssueitemcode, latitude, longitude, mWardno, "", "", "", cityname,
                            sublocality, postalcode, route, neighborhood, administrative_area_level_2, administrative_area_level_1));
                    if (!Connectivity.checkConnected(MunicipalViolation_act.this)) {
                        db.sentToserver(iUniqueKey, 0);
                        showInformationalDialog(MunicipalViolation_act.this, getString(R.string.internet_error_submitting_issue));
                    } else {
                        checkAddrTime();
                    }
                } catch (Exception e) {
                    SSLog.e(TAG, "setOnClickListener ", e.toString());
                }
            }
        });
    }

    protected String getTrafficType() {
        return "MU_";
    }

    private void getWardList() {
        try {
            if (mWardList.size() > 0)
                mWardList.clear();
            mWardList.add("Select");
            mWardList.add("I do not know");
            mWardList.add("A");
            mWardList.add("B");
            mWardList.add("C");
            mWardList.add("D");
            mWardList.add("E");
            mWardList.add("F/N");
            mWardList.add("F/S");
            mWardList.add("G/N");
            mWardList.add("G/S");
            mWardList.add("H/E");
            mWardList.add("H/W");
            mWardList.add("K/E");
            mWardList.add("K/W");
            mWardList.add("L");
            mWardList.add("M/E");
            mWardList.add("M/W");
            mWardList.add("N");
            mWardList.add("P/N");
            mWardList.add("P/S");
            mWardList.add("R/C");
            mWardList.add("R/N");
            mWardList.add("R/S");
            mWardList.add("S");
            mWardList.add("T");

            wardNoAdapter = new ListAdapter(MunicipalViolation_act.this, mWardList);
            spnWardNo.setAdapter(wardNoAdapter);
            mWardno = CGlobals_lib_ss.getInstance().getPersistentPreference(MunicipalViolation_act.this).
                    getString(Constants_dp.PREF_WARD, "");
            if (!TextUtils.isEmpty(mWardno)) {
                spnWardNo.setSelection(getIndex(spnWardNo, mWardno));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   /* private void getWard() {
        final String url = Constants_dp.GET_WARD_FROM_LAT_LNG_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response.trim();
                        JSONObject jobj;
                        if (!response.trim().equals("-1") || !TextUtils.isEmpty(response)) {
                            try {
                                jobj = new JSONObject(response);
                                mWardno = jobj.getString("ward");
                                if (!TextUtils.isEmpty(mWardno)) {
                                    tvWard.setText(mWardno);
                                    spnWardNo.setSelection(getIndex(spnWardNo, mWardno));
                                    mWardno = CGlobals_lib_ss.getInstance().getPersistentPreference(MunicipalViolation_act.this).
                                            getString(Constants_dp.PREF_WARD, "");
                                } else {
                                    findViewById(R.id.changeWard).setVisibility(View.VISIBLE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                SSLog.e(TAG, "getWard", e.getMessage());
                            }
                        } else {
                            findViewById(R.id.changeWard).setVisibility(View.VISIBLE);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                findViewById(R.id.changeWard).setVisibility(View.VISIBLE);
                SSLog.e(TAG, "getWard", error.getMessage());

            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();

                params = CGlobals_db.getInstance(MunicipalViolation_act.this).getBasicMobileParams(params,
                        url, MunicipalViolation_act.this);
                return CGlobals_db.getInstance(MunicipalViolation_act.this).checkParams(params);
            }
        };
        CGlobals_db.getInstance(MunicipalViolation_act.this).getRequestQueue(MunicipalViolation_act.this).add(postRequest);
    }*/
}
