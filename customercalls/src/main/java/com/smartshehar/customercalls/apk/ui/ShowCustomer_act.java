package com.smartshehar.customercalls.apk.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartshehar.customercalls.apk.CCallInfo;
import com.smartshehar.customercalls.apk.CGlobals_cc;
import com.smartshehar.customercalls.apk.COrderHeader;
import com.smartshehar.customercalls.apk.Constants_cc;
import com.smartshehar.customercalls.apk.CustomerCallsSQLiteDB;
import com.smartshehar.customercalls.apk.R;

import java.lang.reflect.Type;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by ctemkar on 08/03/2016.
 * Shows contact info after call is over
 */
public class ShowCustomer_act extends AppCompatActivity {
    CCallInfo moCallInfo = null;
    COrderHeader moOrderHeader = null;
    CoordinatorLayout mCoordinatorLayout;
    CustomerCallsSQLiteDB mCustomerDB;
    Button mBtnDelete, mBtnIgnore, mBtnOk, mBtnEdit;
    private String TAG = "ShowCustomer :";
    EditText mEtOrderAmount;
    String sPhoneNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_customer);
        CGlobals_cc.getInstance().init(ShowCustomer_act.this);
        mBtnDelete = (Button) findViewById(R.id.btnDelete);
        mBtnIgnore = (Button) findViewById(R.id.btnIgnore);
        mBtnOk = (Button) findViewById(R.id.btnOk);
        mBtnEdit = (Button) findViewById(R.id.btnEdit);
        mEtOrderAmount = (EditText) findViewById(R.id.etOrderAmount);
        mCustomerDB = new CustomerCallsSQLiteDB(this);

        try {
            CallPopup_act.CallPopA.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        mEtOrderAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count > 0)
                    mBtnIgnore.setVisibility(View.GONE);
                else mBtnIgnore.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();


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
            sPhoneNo = moCallInfo.getPhoneNo();

            String orderamount = extras.getString("orderamount");
            if (!TextUtils.isEmpty(orderamount)) {

                mEtOrderAmount.setText(orderamount);
                mEtOrderAmount.setEnabled(false);
                mBtnOk.setVisibility(View.GONE);
            }
            CCallInfo oCallInfo = mCustomerDB.getRowByPhone(sPhoneNo);
            boolean numberFoundLocalList = false;
            if (oCallInfo != null && !TextUtils.isEmpty(oCallInfo.getNationalNumber()))
                numberFoundLocalList = true;

            // Found local, don't have to call server
            if (numberFoundLocalList) {
                moCallInfo = oCallInfo;
                mBtnIgnore.setVisibility(View.GONE);
                mBtnEdit.setVisibility(View.VISIBLE);
            } else {
                mBtnIgnore.setVisibility(View.VISIBLE);
                mBtnEdit.setVisibility(View.GONE);
            }

        }

        if (moCallInfo != null) {
            TextView tvName = (TextView) findViewById(R.id.tvName);
            if (tvName != null) {
                if (TextUtils.isEmpty(moCallInfo.getName()))
                    tvName.setText(R.string.unknownPerson);
                else
                    tvName.setText(moCallInfo.getName());
            }
        } else
            moCallInfo = new CCallInfo();

        setTextView((TextView) findViewById(R.id.tvPhoneNo), moCallInfo.getPhoneNo());
        setTextView((TextView) findViewById(R.id.tvCompany), moCallInfo.getCompany());
        setTextView((TextView) findViewById(R.id.tvFlatFloorWing), moCallInfo.getFlatFloorBuildingWing());
        setTextView((TextView) findViewById(R.id.tvComplex), moCallInfo.getComplex());
        setTextView((TextView) findViewById(R.id.tvRoad), moCallInfo.getRoad());
        setTextView((TextView) findViewById(R.id.tvArea), moCallInfo.getArea());
        setTextView((TextView) findViewById(R.id.tvLandmark1), moCallInfo.getLandmark1());

    }

    void setTextView(TextView tv, String sValue) {
        if (TextUtils.isEmpty(sValue)) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(sValue);
        }

    }


    public void clickIgnore(View v) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Ignore number");
        builder.setMessage("Ignore this number in the future?");
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        moCallInfo.setPhoneNo(sPhoneNo);
                        mCustomerDB.addCustomer(moCallInfo);
                        mCustomerDB.setIgnore(sPhoneNo, 1);
                        finish();
                    }
                });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

  /*  public void clickDelete(View v) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Delete number");
        builder.setMessage("Remove number from CustomerCP Calls app?");

        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!TextUtils.isEmpty(moCallInfo.getNationalNumber()))
                            mCustomerDB.setDelete(moCallInfo.getNationalNumber(),
                                    moCallInfo.getCountryCode(), 1);
*//*
                        Snackbar.make(mCoordinatorLayout,
                                "Row for " + moCallInfo.getName() +
                                        ", " + moCallInfo.getPhoneNo() + " deleted",
                                Snackbar.LENGTH_LONG);
*//*
                        finish();
                    }
                });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }*/

    public void clickEdit(View v) {
        Intent intent = new Intent(ShowCustomer_act.this, EditCustomer_act.class);
        Gson gson = new Gson();
        String json = gson.toJson(moCallInfo);
        intent.putExtra("callinfo", json);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent != null)
            setIntent(intent);
    }

    public void clickOk(View view) {

        if (moCallInfo == null)
            finish();
        addOrderAmount();

    }

    private void addOrderAmount() {
        String sOrderAmount = mEtOrderAmount.getText().toString();
        if (TextUtils.isEmpty(sOrderAmount))
            return;

        moOrderHeader = new COrderHeader();
        moOrderHeader.setMiOrderAmount(Integer.valueOf(sOrderAmount));
        moOrderHeader.setMiStorePhoneId(CGlobals_cc.mStorePhoneId);
        moOrderHeader.setMsCountryCode(moCallInfo.getCountryCode());
        moOrderHeader.setMsPhone(moCallInfo.getNationalNumber());
        moOrderHeader.setMiOrderDate(System.currentTimeMillis());
        moOrderHeader.setMiCustomerCallId(moCallInfo.getMiCustomerCallId());
        mCustomerDB.addOrder(moOrderHeader);
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_cc.ADD_ORDER_HEADER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response);
                        if (!TextUtils.isEmpty(response.trim())) {
                            Log.d(TAG, "succ");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    SSLog_SS.e(TAG + " clickOk", error.toString());
                } catch (Exception e) {
                    SSLog_SS.e(TAG + " clickOk", e.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params;
                params = CGlobals_cc.getInstance().common();
                params = moOrderHeader.paramsPut(params);

                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        Constants_cc.ADD_ORDER_HEADER_URL, getApplicationContext());

                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_cc.ADD_ORDER_HEADER_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    Log.d(TAG, "url  " + url);

                } catch (Exception e) {
                    SSLog_SS.e(TAG + " clickOk   ", e.getMessage());
                }

                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        if (moCallInfo.getCustomerId() > 0) {
            CGlobals_lib_ss.getInstance().getRequestQueue(getApplicationContext()).add(postRequest);
            Intent intent = new Intent(ShowCustomer_act.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(ShowCustomer_act.this, EditCustomer_act.class);
            Gson gson = new Gson();
            moCallInfo.setPhoneNo(sPhoneNo);
            String json = gson.toJson(moCallInfo);
            intent.putExtra("callinfo", json);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moCallInfo.setPhoneNo(sPhoneNo);
        mCustomerDB.addCustomer(moCallInfo);
    }
}