package com.smartshehar.customercalls.apk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.customercalls.apk.CCallInfo;
import com.smartshehar.customercalls.apk.CGlobals_cc;
import com.smartshehar.customercalls.apk.Constants_cc;
import com.smartshehar.customercalls.apk.CustomerCallsSQLiteDB;
import com.smartshehar.customercalls.apk.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by ctemkar on 08/03/2016.
 * Shows contact info after call is over
 */
public class EditCustomer_act extends Activity {
    private static final String TAG = "EditCustomer_act: ";
    CCallInfo moCallInfo = null;
    EditText mEtCountryCode, mEtPhoneNo;
    EditText mEtCustomerInfoAll, etFirstName, etLastName, etCompany, etFlatNo, etFloor, etWing,
            etBuilding, etComplex, etRoad, etArea, etLandmark;
    CustomerCallsSQLiteDB mCustomerDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_customer);
        mCustomerDB = new CustomerCallsSQLiteDB(this);
        mEtCustomerInfoAll = (EditText) findViewById(R.id.etCustomerInfoAll);
        mEtCountryCode = (EditText) findViewById(R.id.etCountryCode);
        mEtPhoneNo = (EditText) findViewById(R.id.etPhoneNo);
        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etCompany = (EditText) findViewById(R.id.etCompany);
        etFlatNo = (EditText) findViewById(R.id.etFlatNo);
        etFloor = (EditText) findViewById(R.id.etFloor);
        etWing = (EditText) findViewById(R.id.etWing);
        etBuilding = (EditText) findViewById(R.id.etBuilding);
        etComplex = (EditText) findViewById(R.id.etComplex);
        etRoad = (EditText) findViewById(R.id.etRoad);
        etArea = (EditText) findViewById(R.id.etArea);
        etLandmark = (EditText) findViewById(R.id.etLandmark1);
        moCallInfo = null;

    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if (extras != null && moCallInfo == null) {
            Type type = new TypeToken<CCallInfo>() {
            }.getType();
            Gson gson = new Gson();
            moCallInfo = gson.fromJson(extras.getString("callinfo"), type);
            if (moCallInfo != null) {
                if (TextUtils.isEmpty(moCallInfo.getCustomerInfoAll()) &&
                        (!TextUtils.isEmpty(moCallInfo.getFirstName()) ||
                                (!TextUtils.isEmpty(moCallInfo.getFlatNo())))) {
                    mEtCustomerInfoAll.setVisibility(View.GONE);
                }
                mEtCountryCode.setText(moCallInfo.getCountryCode());
                mEtPhoneNo.setText(moCallInfo.getNationalNumber());
                etFirstName.setText(moCallInfo.getFirstName());
                etLastName.setText(moCallInfo.getLastName());
                etCompany.setText(moCallInfo.getCompany());
                etFlatNo.setText(moCallInfo.getFlatNo());
                etFloor.setText(moCallInfo.getFloor());
                etWing.setText(moCallInfo.getWing());
                etBuilding.setText(moCallInfo.getBulding());
                etComplex.setText(moCallInfo.getComplex());
                etRoad.setText(moCallInfo.getRoad());
                etArea.setText(moCallInfo.getArea());
                etLandmark.setText(moCallInfo.getLandmark1());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        addCustomer();
    }

    public void clickDone(View v) {
        addCustomer();
    }

    private void addCustomer() {

        if (moCallInfo == null)
            moCallInfo = new CCallInfo();
        moCallInfo.setCountryCode(mEtCountryCode.getText().toString());
        moCallInfo.setNationalNumber(mEtPhoneNo.getText().toString());
        moCallInfo.setFirstName(etFirstName.getText().toString());
        moCallInfo.setLastName(etLastName.getText().toString());
        moCallInfo.setCompany(etCompany.getText().toString());
        moCallInfo.setFlatNo(etFlatNo.getText().toString());
        moCallInfo.setBulding(etBuilding.getText().toString());
        moCallInfo.setFloor(etFloor.getText().toString());
        moCallInfo.setWing(etWing.getText().toString());
        moCallInfo.setComplexOrColony(etComplex.getText().toString());
        moCallInfo.setRoad(etRoad.getText().toString());
        moCallInfo.setArea(etArea.getText().toString());
        moCallInfo.setLandmark1(etLandmark.getText().toString());
        moCallInfo.setDateUpdated(System.currentTimeMillis());
        mCustomerDB.addCustomer(moCallInfo);

        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_cc.ADD_CUSTOMER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response);
                        try {
                            if (!TextUtils.isEmpty(response.trim())) {
                                JSONObject jObj = new JSONObject(response);
                                String sCustomerId = jObj.isNull("customer_id") ? "" : jObj.getString("customer_id");
                                if (!TextUtils.isEmpty(sCustomerId)) {
                                    moCallInfo.setCustomerId(Long.valueOf(sCustomerId));
                                    mCustomerDB.addCustomer(moCallInfo);

                                }
                            }
                            if (response.trim().equals("-1")) {
                                Log.i(TAG, ("No response (-1)"));
                                Intent intent = new Intent(EditCustomer_act.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            SSLog_SS.e(TAG + "addCustomer", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(mContext, "Connection failed. Please try again", Toast.LENGTH_LONG).show();
                try {
                    SSLog_SS.e(TAG + " searchNumber", error.toString());
                } catch (Exception e) {
                    SSLog_SS.e(TAG + " searchNumber", e.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params;
                params = CGlobals_cc.getInstance().common();
                params = moCallInfo.paramsPut(params);
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        Constants_cc.ADD_CUSTOMER_URL, getApplicationContext());
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_cc.ADD_CUSTOMER_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    Log.d(TAG, "url  " + url);

                } catch (Exception e) {
                    SSLog_SS.e(TAG + " FindCustomer - ", e.getMessage());
                }

                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        gotoMainActivity();
    }



    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //now getIntent() should always return the last received intent
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        addCustomer();
        gotoMainActivity();
    }

    void gotoMainActivity() {
        Intent intent = new Intent(EditCustomer_act.this, MainActivity.class);
        startActivity(intent);
        finish();

    }
}
