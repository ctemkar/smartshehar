package com.jumpinjumpout.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jumpinjumpout.CGlobals_dp;
import com.jumpinjumpout.CMember;
import com.jumpinjumpout.Constants_dp;
import com.jumpinjumpout.DriverInfo;
import com.jumpinjumpout.www.driverprofile.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Connectivity;
import lib.app.util.Constants_lib_ss;

public class ActUploadData extends AppCompatActivity {
    Connectivity mConnectivity;
    public static final String TAG = "ActUploadData";
    CGlobals_dp mApp;
    RelativeLayout rlBackup, rlRestore;
    TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_upload_data);
        mConnectivity = new Connectivity();
        if (!Connectivity.checkConnected(ActUploadData.this)) {
            if (!mConnectivity.connectionError(ActUploadData.this,
                    getString(R.string.no_iternet_to_sync))) {
                Log.d(TAG, "Internet Connection");
            }
        }
        mApp = CGlobals_dp.getInstance(ActUploadData.this);
        rlBackup = (RelativeLayout) findViewById(R.id.rlBackup);
        rlRestore = (RelativeLayout) findViewById(R.id.rlRestore);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        rlBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnection();
            }
        });
        rlRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mConnectivity.connError(ActUploadData.this)) {
                    if (mApp != null) {
                        if (mApp.mDBHelper != null) {
                            txtStatus.setVisibility(View.GONE);
                            datafromserver();
                        }
                    }
                } else showAlert();
            }
        });

    }
    public void checkConnection() {
        if (!mConnectivity.connError(ActUploadData.this)) {
            if (mApp != null) {
                if (mApp.mDBHelper != null) {
                    String lastSyncDateTime = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT).format(new Date());
                    CGlobals_lib_ss.getInstance()
                            .getPersistentPreferenceEditor(ActUploadData.this).
                            putString(Constants_dp.PREFS_LAST_SYNC_DTAE, lastSyncDateTime).apply();
                    ArrayList<DriverInfo> maDriverList = mApp.mDBHelper.getDriverProfileList();
                    for (DriverInfo mDriverInfo : maDriverList) {
                        HashMap<String, String> hmp = new HashMap<>();
                        if (!TextUtils.isEmpty(String.valueOf(mDriverInfo.getUnique_key())))
                            hmp.put("uniquekey", String.valueOf(String.valueOf(mDriverInfo.getUnique_key())));
                        if (!TextUtils.isEmpty(mDriverInfo.getClientdatetime()))
                            hmp.put("clientdatetime", mDriverInfo.getClientdatetime());
                        if (!TextUtils.isEmpty(mDriverInfo.getGoogle_address()))
                            hmp.put("googleaddress", mDriverInfo.getGoogle_address());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_firstname()))
                            hmp.put("driverfirstname", mDriverInfo.getDriver_firstname());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_lastname()))
                            hmp.put("driverlastname", mDriverInfo.getDriver_lastname());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_dob()))
                            hmp.put("driverdob", mDriverInfo.getDriver_dob());
                        if (!TextUtils.isEmpty(mDriverInfo.getT_address_1()))
                            hmp.put("taddress1", mDriverInfo.getT_address_1());
                        if (!TextUtils.isEmpty(mDriverInfo.getT_address_2()))
                            hmp.put("taddress2", mDriverInfo.getT_address_2());
                        if (!TextUtils.isEmpty(mDriverInfo.getT_city_town()))
                            hmp.put("tcitytown", mDriverInfo.getT_city_town());
                        if (!TextUtils.isEmpty(mDriverInfo.getT_state()))
                            hmp.put("tstate", mDriverInfo.getT_state());
                        if (!TextUtils.isEmpty(mDriverInfo.getT_pincode()))
                            hmp.put("tpincode", mDriverInfo.getT_pincode());
                        if (!TextUtils.isEmpty(mDriverInfo.getT_landmark()))
                            hmp.put("tlandmark", mDriverInfo.getT_landmark());
                        if (!TextUtils.isEmpty(mDriverInfo.getP_address_1()))
                            hmp.put("paddress1", mDriverInfo.getP_address_1());
                        if (!TextUtils.isEmpty(mDriverInfo.getP_address_2()))
                            hmp.put("paddress2", mDriverInfo.getP_address_2());
                        if (!TextUtils.isEmpty(mDriverInfo.getP_city_town()))
                            hmp.put("pcitytown", mDriverInfo.getP_city_town());
                        if (!TextUtils.isEmpty(mDriverInfo.getP_state()))
                            hmp.put("pstate", mDriverInfo.getP_state());
                        if (!TextUtils.isEmpty(mDriverInfo.getP_pincode()))
                            hmp.put("ppincode", mDriverInfo.getP_pincode());
                        if (!TextUtils.isEmpty(mDriverInfo.getP_landmark()))
                            hmp.put("plandmark", mDriverInfo.getP_landmark());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_email()))
                            hmp.put("driveremail", mDriverInfo.getDriver_email());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_phoneno()))
                            hmp.put("driverphoneno", mDriverInfo.getDriver_phoneno());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_gender()))
                            hmp.put("drivergender", mDriverInfo.getDriver_gender());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_permit_flag()))
                            hmp.put("driverpermiflag", mDriverInfo.getDriver_permit_flag());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_permit_no()))
                            hmp.put("driverpermitno", mDriverInfo.getDriver_permit_no());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_badge_flag()))
                            hmp.put("driverbadgeflag", mDriverInfo.getDriver_badge_flag());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_badge_no()))
                            hmp.put("driverbadgeno", mDriverInfo.getDriver_badge_no());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_incity_since()))
                            hmp.put("driverincitysince", mDriverInfo.getDriver_incity_since());
                        if (!TextUtils.isEmpty(mDriverInfo.getOther_company_name()))
                            hmp.put("othercompanyname", mDriverInfo.getOther_company_name());
                        if (!TextUtils.isEmpty(mDriverInfo.getCab_needed_flag()))
                            hmp.put("cabneededflag", mDriverInfo.getCab_needed_flag());
                        if (!TextUtils.isEmpty(mDriverInfo.getRemark()))
                            hmp.put("remark", mDriverInfo.getRemark());
                        if (!TextUtils.isEmpty(mDriverInfo.getInternal_comment()))
                            hmp.put("internalcomment", mDriverInfo.getInternal_comment());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_pancard()))
                            hmp.put("driverpancard", mDriverInfo.getDriver_pancard());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_aadhar()))
                            hmp.put("driveraadhar", mDriverInfo.getDriver_aadhar());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_license_no()))
                            hmp.put("driverlicenseno", mDriverInfo.getDriver_license_no());
                        if (!TextUtils.isEmpty(mDriverInfo.getLast_modified_datetime()))
                            hmp.put("lastmodifieddatetime", mDriverInfo.getLast_modified_datetime());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_vehicle_no()))
                            hmp.put("drivervehicleno", mDriverInfo.getDriver_vehicle_no());
                        if (!TextUtils.isEmpty(mDriverInfo.getUniqueid()))
                            hmp.put("uniqueid", mDriverInfo.getUniqueid());
                        if (!TextUtils.isEmpty(mDriverInfo.getPayment_amount()))
                            hmp.put("paymentamount", mDriverInfo.getPayment_amount());
                        if (!TextUtils.isEmpty(mDriverInfo.getPayment_status()))
                            hmp.put("paymentstatus", mDriverInfo.getPayment_status());
                        if (!TextUtils.isEmpty(mDriverInfo.getReceipt_no()))
                            hmp.put("receiptno", mDriverInfo.getReceipt_no());
                        if (!TextUtils.isEmpty(mDriverInfo.getReceipt_date()))
                            hmp.put("receiptdate", mDriverInfo.getReceipt_date());
                        if (!TextUtils.isEmpty(mDriverInfo.getReferral_walk_in()))
                            hmp.put("referralwalkin", mDriverInfo.getReferral_walk_in());
                        if (!TextUtils.isEmpty(mDriverInfo.getReferral_supervisor()))
                            hmp.put("referralsupervisor", mDriverInfo.getReferral_supervisor());
                        if (!TextUtils.isEmpty(mDriverInfo.getReferral_driver()))
                            hmp.put("referraldriver", mDriverInfo.getReferral_driver());
                        if (!TextUtils.isEmpty(mDriverInfo.getReceipt_no_2()))
                            hmp.put("receiptno2", mDriverInfo.getReceipt_no_2());
                        if (!TextUtils.isEmpty(mDriverInfo.getReceipt_no_3()))
                            hmp.put("receiptno3", mDriverInfo.getReceipt_no_3());
                        if (!TextUtils.isEmpty(mDriverInfo.getDriver_backout_reason()))
                            if (!mDriverInfo.getDriver_backout_reason().equals("-1"))
                                hmp.put("driverbackoutreason", mDriverInfo.getDriver_backout_reason());
                        if (!TextUtils.isEmpty(mDriverInfo.getTr_license_exp_date()))
                            hmp.put("trlicenseexpdate", mDriverInfo.getTr_license_exp_date());
                        if (!TextUtils.isEmpty(mDriverInfo.getReceipt_date_2()))
                            hmp.put("receiptdate2", mDriverInfo.getReceipt_date_2());
                        if (!TextUtils.isEmpty(mDriverInfo.getReceipt_date_3()))
                            hmp.put("receiptdate3", mDriverInfo.getReceipt_date_3());
                        if (!TextUtils.isEmpty(mDriverInfo.getPrivate_verification())) {
                            hmp.put("privateverification", mDriverInfo.getPrivate_verification());
                        } else
                            hmp.put("privateverification", "0");
                        if (!TextUtils.isEmpty(mDriverInfo.getPolice_verification())) {
                            hmp.put("policeverification", mDriverInfo.getPolice_verification());
                        } else
                            hmp.put("policeverification", "0");
                        if (!TextUtils.isEmpty(lastSyncDateTime))
                            hmp.put("lastsyncdatetime", lastSyncDateTime);

                        byte[] byteDriver = mDriverInfo.getDriver_image();
                        String strDriverImage;
                        HashMap<String, String> hmpDriverImage = null;
                        if (byteDriver != null) {
                            strDriverImage = Base64.encodeToString(byteDriver, Base64.DEFAULT);
                            hmpDriverImage = new HashMap<>();
                            if (!TextUtils.isEmpty(strDriverImage))
                                hmpDriverImage.put("image", strDriverImage);
                            if (!TextUtils.isEmpty(mDriverInfo.getDriver_image_name()))
                                hmpDriverImage.put("imagefilename", mDriverInfo.getDriver_image_name());
                            if (!TextUtils.isEmpty(mDriverInfo.getDriver_image_datettime()))
                                hmpDriverImage.put("imagedatetime", mDriverInfo.getDriver_image_datettime());
                            if (!TextUtils.isEmpty(mDriverInfo.getDriver_phoneno()))
                                hmpDriverImage.put("driverphoneno", mDriverInfo.getDriver_phoneno());
                            hmpDriverImage.put("imagetype", "Driver");
                        }

                        //
                        byte[] bytePan = mDriverInfo.getPancard_image();
                        String strPanImage;
                        HashMap<String, String> hmpPanImage = null;
                        if (bytePan != null) {
                            strPanImage = Base64.encodeToString(bytePan, Base64.DEFAULT);
                            hmpPanImage = new HashMap<>();
                            if (!TextUtils.isEmpty(strPanImage))
                                hmpPanImage.put("image", strPanImage);
                            if (!TextUtils.isEmpty(mDriverInfo.getPancard_image_name()))
                                hmpPanImage.put("imagefilename", mDriverInfo.getPancard_image_name());
                            if (!TextUtils.isEmpty(mDriverInfo.getPancard_image_datettime()))
                                hmpPanImage.put("imagedatetime", mDriverInfo.getPancard_image_datettime());
                            if (!TextUtils.isEmpty(mDriverInfo.getDriver_phoneno()))
                                hmpPanImage.put("driverphoneno", mDriverInfo.getDriver_phoneno());
                            hmpPanImage.put("imagetype", "PanCard");
                        }

                        byte[] byteAdhar = mDriverInfo.getAadhar_image();
                        String strAdharImage;
                        HashMap<String, String> hmpAdharImage = null;
                        if (byteAdhar != null) {
                            strAdharImage = Base64.encodeToString(byteAdhar, Base64.DEFAULT);
                            hmpAdharImage = new HashMap<>();
                            if (!TextUtils.isEmpty(strAdharImage))
                                hmpAdharImage.put("image", strAdharImage);
                            if (!TextUtils.isEmpty(mDriverInfo.getAadhar_image_name()))
                                hmpAdharImage.put("imagefilename", mDriverInfo.getAadhar_image_name());
                            if (!TextUtils.isEmpty(mDriverInfo.getAadhar_image_datetime()))
                                hmpAdharImage.put("imagedatetime", mDriverInfo.getAadhar_image_datetime());
                            if (!TextUtils.isEmpty(mDriverInfo.getDriver_phoneno()))
                                hmpAdharImage.put("driverphoneno", mDriverInfo.getDriver_phoneno());
                            hmpAdharImage.put("imagetype", "AadharCard");
                        }
                        //
                        byte[] byteLicense = mDriverInfo.getLicense_image();
                        String strLicenseImage;
                        HashMap<String, String> hmpLicenseImage = null;
                        if (byteLicense != null) {
                            strLicenseImage = Base64.encodeToString(byteLicense, Base64.DEFAULT);
                            hmpLicenseImage = new HashMap<>();
                            if (!TextUtils.isEmpty(strLicenseImage))
                                hmpLicenseImage.put("image", strLicenseImage);
                            if (!TextUtils.isEmpty(mDriverInfo.getLicense_image_name()))
                                hmpLicenseImage.put("imagefilename", mDriverInfo.getLicense_image_name());
                            if (!TextUtils.isEmpty(mDriverInfo.getLicense_image_datetime()))
                                hmpLicenseImage.put("imagedatetime", mDriverInfo.getLicense_image_datetime());
                            if (!TextUtils.isEmpty(mDriverInfo.getDriver_phoneno()))
                                hmpLicenseImage.put("driverphoneno", mDriverInfo.getDriver_phoneno());
                            hmpLicenseImage.put("imagetype", "License");
                        }
                        sendDataToServer(hmp, hmpDriverImage, hmpPanImage, hmpAdharImage, hmpLicenseImage, mDriverInfo.getOther_company_code());

                    }
                }
            }
        } else showAlert();
    }

    private void showAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(ActUploadData.this);
        alertDialog.setTitle("No Internet Connection");
        alertDialog.setMessage("Please check your internet connection");
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }

        });

        alertDialog.show();

    }

    private void sendDataToServer(final HashMap<String, String> hmp, final HashMap<String, String> hmpDriverImage,
                                  final HashMap<String, String> hmpPanImage, final HashMap<String, String> hmpAdharImage,
                                  final HashMap<String, String> hmpLicenseImage, final String mOtherLookup) {
        try {
            final ProgressDialog dialog = new ProgressDialog(ActUploadData.this);
            dialog.setCancelable(false);
            dialog.setMessage("Uploading drivers");
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.show();
            final String url = Constants_dp.INSERT_DRIVER_PROFILE_DATA_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1") && !TextUtils.isEmpty(response)) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String driverprofileid = jsonObject.getString("driver_profile_id");
                                    String phoneno = jsonObject.getString("driver_phoneno");
                                    if (hmpDriverImage != null)
                                        if (hmpDriverImage.size() > 0)
                                            sendImagesToServer(hmpDriverImage, phoneno, driverprofileid);
                                    if (hmpPanImage != null)
                                        if (hmpPanImage.size() > 0)
                                            sendImagesToServer(hmpPanImage, phoneno, driverprofileid);
                                    if (hmpAdharImage != null)
                                        if (hmpAdharImage.size() > 0)
                                            sendImagesToServer(hmpAdharImage, phoneno, driverprofileid);
                                    if (hmpLicenseImage != null)
                                        if (hmpLicenseImage.size() > 0)
                                            sendImagesToServer(hmpLicenseImage, phoneno, driverprofileid);

                                    if (!TextUtils.isEmpty(mOtherLookup))
                                        sendOtherCompanyInfo(mOtherLookup, phoneno, driverprofileid);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.d(TAG, "Failed!");
                            }
                            if (dialog != null)
                                dialog.dismiss();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "e" + error);
                    txtStatus.setText(getString(R.string.failedstatus));
                    txtStatus.setVisibility(View.VISIBLE);
                    if (dialog != null)
                        dialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    String column, value;
                    Map<String, String> params = new HashMap<>();
                    Set set = hmp.entrySet();
                    for (Object aSet : set) {
                        Map.Entry me = (Map.Entry) aSet;
                        column = me.getKey().toString();
                        value = me.getValue().toString();
                        params.put(column, value);
                    }
                    params = CGlobals_dp.getInstance(ActUploadData.this).getBasicMobileParams(params,
                            url, ActUploadData.this);

                    return CGlobals_dp.getInstance(ActUploadData.this).checkParams(params);
                }
            };
            CGlobals_dp.getInstance(ActUploadData.this).getRequestQueue(ActUploadData.this).add(postRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendImagesToServer(final HashMap<String, String> hmp, final String phoneno, final String driverprofileid) {
        try {
            final ProgressDialog dialog = new ProgressDialog(ActUploadData.this);
            dialog.setCancelable(false);
            dialog.setMessage("Uploading drivers");
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.show();
            final String url = Constants_dp.DRIVER_IMAGE_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                try {

                                    mApp.mDBHelper.updateDriverProfile(phoneno, "1");
                                } catch (Exception e) {
                                    mApp.mDBHelper.updateDriverProfile(phoneno, "0");
                                    e.printStackTrace();
                                }

                            } else {

                                mApp.mDBHelper.updateDriverProfile(phoneno, "0");
                            }
                            if (dialog != null)
                                dialog.dismiss();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mApp.mDBHelper.updateDriverProfile(phoneno, "0");
                    txtStatus.setText(getString(R.string.failedstatus));
                    txtStatus.setVisibility(View.VISIBLE);
                    if (dialog != null)
                        dialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    String column, value;
                    Map<String, String> params = new HashMap<>();

                    Set set = hmp.entrySet();
                    for (Object aSet : set) {
                        Map.Entry me = (Map.Entry) aSet;
                        column = me.getKey().toString();
                        value = me.getValue().toString();
                        params.put(column, value);
                    }
                    params.put("driverprofileid", driverprofileid);
                    params.put("driverphoneno", phoneno);
                    params = CGlobals_dp.getInstance(ActUploadData.this).
                            getBasicMobileParams(params,
                                    url, ActUploadData.this);

                    return CGlobals_dp.getInstance(ActUploadData.this).
                            checkParams(params);
                }
            };
            CGlobals_dp.getInstance(ActUploadData.this).
                    getRequestQueue(ActUploadData.this).add(postRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendOtherCompanyInfo(final String mOtherLookup, final String phoneno, final String driverprofileid) {
        try {
            final ProgressDialog dialog = new ProgressDialog(ActUploadData.this);
            dialog.setCancelable(false);
            dialog.setMessage("Uploading drivers");
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.show();
            final String url = Constants_dp.INSERT_OTHER_COMPANY_CODE_URL;
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                try {
                                    mApp.mDBHelper.updateDriverProfile(phoneno, "1");

                                } catch (Exception e) {
                                    mApp.mDBHelper.updateDriverProfile(phoneno, "0");
                                    e.printStackTrace();
                                }
                            } else {
                                mApp.mDBHelper.updateDriverProfile(phoneno, "0");

                            }
                            if (dialog != null)
                                dialog.dismiss();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mApp.mDBHelper.updateDriverProfile(phoneno, "0");
                    txtStatus.setText(getString(R.string.failedstatus));
                    txtStatus.setVisibility(View.VISIBLE);
                    if (dialog != null)
                        dialog.dismiss();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    String lookup = null;
                    Type type = new TypeToken<ArrayList<CMember>>() {
                    }.getType();
                    ArrayList<CMember> maMember = new Gson().fromJson(mOtherLookup, type);
                    ArrayList<CMember> curMember = new ArrayList<>();
                    for (CMember cMember : maMember) {
                        cMember.setDriver_profile_id(driverprofileid);
                        curMember.add(cMember);
                    }
                    if (curMember.size() > 0) {
                        lookup = new Gson().toJson(curMember);
                    }
                    if (!TextUtils.isEmpty(lookup))
                        params.put("othercabrows", lookup);
                    params.put("driverprofileid", driverprofileid);
                    params.put("driverphoneno", phoneno);
                    params = CGlobals_dp.getInstance(ActUploadData.this).
                            getBasicMobileParams(params,
                                    url, ActUploadData.this);

                    return CGlobals_dp.getInstance(ActUploadData.this).
                            checkParams(params);
                }
            };
            CGlobals_dp.getInstance(ActUploadData.this).
                    getRequestQueue(ActUploadData.this).add(postRequest);
        } catch (Exception e) {
            mApp.mDBHelper.updateDriverProfile(phoneno, "0");
            e.printStackTrace();
        }
    }

    public void datafromserver() {
        final ProgressDialog pd = new ProgressDialog(getWindow().getContext());
        pd.setMessage("Downloading Drivers"); //
        pd.getWindow().setGravity(Gravity.BOTTOM);
        pd.show();

        try {
            final String url = Constants_dp.GET_DRIVER_PROFILE_DATA;

            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            if (!response.trim().equals("-1")) {
                                try {
                                    txtStatus.setText(getString(R.string.currentstatus));
                                    txtStatus.setVisibility(View.VISIBLE);
                                    dataList(response);
                                    Log.d(TAG, "" + response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (pd != null) {
                                pd.dismiss();

                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    txtStatus.setText(getString(R.string.restorestatus));
                    txtStatus.setVisibility(View.VISIBLE);
                    if (pd != null)
                        pd.dismiss();

                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    params = CGlobals_dp.getInstance(ActUploadData.this).getBasicMobileParams(params,
                            url, ActUploadData.this);

                    Log.d(TAG, "PARAM IS " + params);
                    return CGlobals_dp.getInstance(ActUploadData.this).checkParams(params);
                }
            };
            CGlobals_dp.getInstance(ActUploadData.this).getRequestQueue(ActUploadData.this).add(postRequest);

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "ERROR IS " + e.toString());
        }
    }

    private void dataList(String response) {
        JSONArray resultRow;
        try {

            resultRow = new JSONArray(response);
            String unique_key, clientdatetime, driver_firstname, driver_lastname, driver_dob,
                    driver_email, driver_phoneno, driver_gender, driver_pancard, driver_aadhar,
                    driver_license_no, driver_permit_flag, driver_permit_no, driver_badge_flag,
                    driver_badge_no, driver_incity_since, cab_needed_flag, driver_vehicle_no,
                    remark, internal_comment, google_address, t_address_1, t_address_2,
                    t_city_town, t_landmark, t_state, t_pincode, p_address_1, p_address_2, p_city_town,
                    p_landmark, p_state, p_pincode, other_company_code,
                    driver_image_name, driver_image_path, driver_image_datettime, pancard_image_name,
                    pancard_image_path, pancard_image_datettime, license_image_name,
                    license_image_path, license_image_datetime, aadhar_image_name, aadhar_image_path,
                    aadhar_image_datetime, other_company_name, referral_walk_in, referral_supervisor,
                    referral_driver, receipt_no_2, receipt_no_3, driver_backout_reason, tr_license_exp_date,
                    receipt_date_2, receipt_date_3, payment_amount, receipt_no, receipt_date,
                    private_verification, police_verification;


            JSONObject jObj;
            HashMap<String, String> hmp;
            for (int j = 0; j < resultRow.length(); j++) {
                jObj = resultRow.getJSONObject(j);
                hmp = new HashMap<>();
                unique_key = jObj.isNull("unique_key") ? "" : jObj.getString("unique_key");
                if (!TextUtils.isEmpty(unique_key))
                    hmp.put("unique_key", unique_key);
                clientdatetime = jObj.isNull("clientdatetime") ? "" : jObj.getString("clientdatetime");
                if (!TextUtils.isEmpty(clientdatetime))
                    hmp.put("clientdatetime", clientdatetime);
                driver_firstname = jObj.isNull("driver_firstname") ? "" : jObj.getString("driver_firstname");
                if (!TextUtils.isEmpty(driver_firstname))
                    hmp.put("driver_firstname", driver_firstname);
                driver_lastname = jObj.isNull("driver_lastname") ? "" : jObj.getString("driver_lastname");
                if (!TextUtils.isEmpty(driver_lastname))
                    hmp.put("driver_lastname", driver_lastname);
                driver_dob = jObj.isNull("driver_dob") ? "" : jObj.getString("driver_dob");
                if (!TextUtils.isEmpty(driver_dob))
                    hmp.put("driver_dob", driver_dob);
                driver_email = jObj.isNull("driver_email") ? "" : jObj.getString("driver_email");
                if (!TextUtils.isEmpty(driver_email))
                    hmp.put("driver_email", driver_email);
                driver_phoneno = jObj.isNull("driver_phoneno") ? "" : jObj.getString("driver_phoneno");
                if (!TextUtils.isEmpty(driver_phoneno))
                    hmp.put("driver_phoneno", driver_phoneno);
                driver_gender = jObj.isNull("driver_gender") ? "" : jObj.getString("driver_gender");
                if (!TextUtils.isEmpty(driver_gender))
                    hmp.put("driver_gender", driver_gender);
                driver_pancard = jObj.isNull("driver_pancard") ? "" : jObj.getString("driver_pancard");
                if (!TextUtils.isEmpty(driver_pancard))
                    hmp.put("driver_pancard", driver_pancard);
                driver_aadhar = jObj.isNull("driver_aadhar") ? "" : jObj.getString("driver_aadhar");
                if (!TextUtils.isEmpty(driver_aadhar))
                    hmp.put("driver_aadhar", driver_aadhar);
                driver_license_no = jObj.isNull("driver_license_no") ? "" : jObj.getString("driver_license_no");
                if (!TextUtils.isEmpty(driver_license_no))
                    hmp.put("driver_license_no", driver_license_no);
                driver_permit_flag = jObj.isNull("driver_permit_flag") ? "" : jObj.getString("driver_permit_flag");
                if (!TextUtils.isEmpty(driver_permit_flag))
                    hmp.put("driver_permit_flag", driver_permit_flag);
                driver_permit_no = jObj.isNull("driver_permit_no") ? "" : jObj.getString("driver_permit_no");
                if (!TextUtils.isEmpty(driver_permit_no))
                    hmp.put("driver_permit_no", driver_permit_no);
                driver_badge_flag = jObj.isNull("driver_badge_flag") ? "" : jObj.getString("driver_badge_flag");
                if (!TextUtils.isEmpty(driver_badge_flag))
                    hmp.put("driver_badge_flag", driver_badge_flag);
                driver_badge_no = jObj.isNull("driver_badge_no") ? "" : jObj.getString("driver_badge_no");
                if (!TextUtils.isEmpty(driver_badge_no))
                    hmp.put("driver_badge_no", driver_badge_no);
                driver_incity_since = jObj.isNull("driver_incity_since") ? "" : jObj.getString("driver_incity_since");
                if (!TextUtils.isEmpty(driver_incity_since))
                    hmp.put("driver_incity_since", driver_incity_since);
                cab_needed_flag = jObj.isNull("cab_needed_flag") ? "" : jObj.getString("cab_needed_flag");
                if (!TextUtils.isEmpty(cab_needed_flag))
                    hmp.put("cab_needed_flag", cab_needed_flag);
                driver_vehicle_no = jObj.isNull("driver_vehicle_no") ? "" : jObj.getString("driver_vehicle_no");
                if (!TextUtils.isEmpty(driver_vehicle_no))
                    hmp.put("driver_vehicle_no", driver_vehicle_no);
                remark = jObj.isNull("remark") ? "" : jObj.getString("remark");
                if (!TextUtils.isEmpty(remark))
                    hmp.put("remark", remark);
                internal_comment = jObj.isNull("internal_comment") ? "" : jObj.getString("internal_comment");
                if (!TextUtils.isEmpty(internal_comment))
                    hmp.put("internal_comment", internal_comment);
                google_address = jObj.isNull("google_address") ? "" : jObj.getString("google_address");
                if (!TextUtils.isEmpty(google_address))
                    hmp.put("google_address", google_address);
                t_address_1 = jObj.isNull("t_address_1") ? "" : jObj.getString("t_address_1");
                if (!TextUtils.isEmpty(t_address_1))
                    hmp.put("t_address_1", t_address_1);
                t_address_2 = jObj.isNull("t_address_2") ? "" : jObj.getString("t_address_2");
                if (!TextUtils.isEmpty(t_address_2))
                    hmp.put("t_address_2", t_address_2);
                t_city_town = jObj.isNull("t_city_town") ? "" : jObj.getString("t_city_town");
                if (!TextUtils.isEmpty(t_city_town))
                    hmp.put("t_city_town", t_city_town);
                t_landmark = jObj.isNull("t_landmark") ? "" : jObj.getString("t_landmark");
                if (!TextUtils.isEmpty(t_landmark))
                    hmp.put("t_landmark", t_landmark);
                t_state = jObj.isNull("t_state") ? "" : jObj.getString("t_state");
                if (!TextUtils.isEmpty(t_state))
                    hmp.put("t_state", t_state);
                t_pincode = jObj.isNull("t_pincode") ? "" : jObj.getString("t_pincode");
                if (!TextUtils.isEmpty(t_pincode))
                    hmp.put("t_pincode", t_pincode);
                p_address_1 = jObj.isNull("p_address_1") ? "" : jObj.getString("p_address_1");
                if (!TextUtils.isEmpty(p_address_1))
                    hmp.put("p_address_1", p_address_1);
                p_address_2 = jObj.isNull("p_address_2") ? "" : jObj.getString("p_address_2");
                if (!TextUtils.isEmpty(p_address_2))
                    hmp.put("p_address_2", p_address_2);
                p_city_town = jObj.isNull("p_city_town") ? "" : jObj.getString("p_city_town");
                if (!TextUtils.isEmpty(p_city_town))
                    hmp.put("p_city_town", p_city_town);
                p_landmark = jObj.isNull("p_landmark") ? "" : jObj.getString("p_landmark");
                if (!TextUtils.isEmpty(p_landmark))
                    hmp.put("p_landmark", p_landmark);
                p_state = jObj.isNull("p_state") ? "" : jObj.getString("p_state");
                if (!TextUtils.isEmpty(p_state))
                    hmp.put("p_state", p_state);
                p_pincode = jObj.isNull("p_pincode") ? "" : jObj.getString("p_pincode");
                if (!TextUtils.isEmpty(p_pincode))
                    hmp.put("p_pincode", p_pincode);
                other_company_code = jObj.isNull("other_company_code") ? "" : jObj.getString("other_company_code");
                if (!TextUtils.isEmpty(other_company_code))
                    hmp.put("other_company_code", other_company_code);
                driver_image_name = jObj.isNull("driver_image_name") ? "" : jObj.getString("driver_image_name");
                if (!TextUtils.isEmpty(driver_image_name))
                    hmp.put("driver_image_name", driver_image_name);
                driver_image_path = jObj.isNull("driver_image_path") ? "" : jObj.getString("driver_image_path");
                if (!TextUtils.isEmpty(driver_image_path))
                    hmp.put("driver_image_path", driver_image_path);
                driver_image_datettime = jObj.isNull("driver_image_datettime") ? "" : jObj.getString("driver_image_datettime");
                if (!TextUtils.isEmpty(driver_image_datettime))
                    hmp.put("driver_image_datettime", driver_image_datettime);
                pancard_image_name = jObj.isNull("pancard_image_name") ? "" : jObj.getString("pancard_image_name");
                if (!TextUtils.isEmpty(pancard_image_name))
                    hmp.put("pancard_image_name", pancard_image_name);
                pancard_image_path = jObj.isNull("pancard_image_path") ? "" : jObj.getString("pancard_image_path");
                if (!TextUtils.isEmpty(pancard_image_path))
                    hmp.put("pancard_image_path", pancard_image_path);
                pancard_image_datettime = jObj.isNull("pancard_image_datettime") ? "" : jObj.getString("pancard_image_datettime");
                if (!TextUtils.isEmpty(pancard_image_datettime))
                    hmp.put("pancard_image_datettime", pancard_image_datettime);
                license_image_name = jObj.isNull("license_image_name") ? "" : jObj.getString("license_image_name");
                if (!TextUtils.isEmpty(license_image_name))
                    hmp.put("license_image_name", license_image_name);
                license_image_path = jObj.isNull("license_image_path") ? "" : jObj.getString("license_image_path");
                if (!TextUtils.isEmpty(license_image_path))
                    hmp.put("license_image_path", license_image_path);
                license_image_datetime = jObj.isNull("license_image_datetime") ? "" : jObj.getString("license_image_datetime");
                if (!TextUtils.isEmpty(license_image_datetime))
                    hmp.put("license_image_datetime", license_image_datetime);
                aadhar_image_name = jObj.isNull("aadhar_image_name") ? "" : jObj.getString("aadhar_image_name");
                if (!TextUtils.isEmpty(aadhar_image_name))
                    hmp.put("aadhar_image_name", aadhar_image_name);
                aadhar_image_path = jObj.isNull("aadhar_image_path") ? "" : jObj.getString("aadhar_image_path");
                if (!TextUtils.isEmpty(aadhar_image_path))
                    hmp.put("aadhar_image_path", aadhar_image_path);
                aadhar_image_datetime = jObj.isNull("aadhar_image_datetime") ? "" : jObj.getString("aadhar_image_datetime");
                if (!TextUtils.isEmpty(aadhar_image_datetime))
                    hmp.put("aadhar_image_datetime", aadhar_image_datetime);
                payment_amount = jObj.isNull("payment_amount") ? "" : jObj.getString("payment_amount");
                if (!TextUtils.isEmpty(payment_amount))
                    hmp.put("payment_amount", payment_amount);
                referral_walk_in = jObj.isNull("referral_walk_in") ? "" : jObj.getString("referral_walk_in");
                if (!TextUtils.isEmpty(referral_walk_in))
                    hmp.put("referral_walk_in", referral_walk_in);
                referral_supervisor = jObj.isNull("referral_supervisor") ? "" : jObj.getString("referral_supervisor");
                if (!TextUtils.isEmpty(referral_supervisor))
                    hmp.put("referral_supervisor", referral_supervisor);
                referral_driver = jObj.isNull("referral_driver") ? "" : jObj.getString("referral_driver");
                if (!TextUtils.isEmpty(referral_driver))
                    hmp.put("referral_driver", referral_driver);

                receipt_no = jObj.isNull("receipt_no") ? "" : jObj.getString("receipt_no");
                if (!TextUtils.isEmpty(receipt_no))
                    hmp.put("receipt_no", receipt_no);

                receipt_date = jObj.isNull("receipt_date") ? "" : jObj.getString("receipt_date");
                if (!TextUtils.isEmpty(receipt_date))
                    hmp.put("receipt_date", receipt_date);

                receipt_no_2 = jObj.isNull("receipt_no_2") ? "" : jObj.getString("receipt_no_2");
                if (!TextUtils.isEmpty(receipt_no_2))
                    hmp.put("receipt_no_2", receipt_no_2);
                receipt_no_3 = jObj.isNull("receipt_no_3") ? "" : jObj.getString("receipt_no_3");
                if (!TextUtils.isEmpty(receipt_no_3))
                    hmp.put("receipt_no_3", receipt_no_3);
                driver_backout_reason = jObj.isNull("driver_backout_reason") ? "" : jObj.getString("driver_backout_reason");
                if (!TextUtils.isEmpty(driver_backout_reason))
                    hmp.put("driver_backout_reason", driver_backout_reason);
                tr_license_exp_date = jObj.isNull("tr_license_exp_date") ? "" : jObj.getString("tr_license_exp_date");
                if (!TextUtils.isEmpty(tr_license_exp_date))
                    hmp.put("tr_license_exp_date", tr_license_exp_date);
                receipt_date_2 = jObj.isNull("receipt_date_2") ? "" : jObj.getString("receipt_date_2");
                if (!TextUtils.isEmpty(receipt_date_2))
                    hmp.put("receipt_date_2", receipt_date_2);
                receipt_date_3 = jObj.isNull("receipt_date_3") ? "" : jObj.getString("receipt_date_3");
                if (!TextUtils.isEmpty(receipt_date_3))
                    hmp.put("receipt_date_3", receipt_date_3);

                private_verification = jObj.isNull("private_verification") ? "0" : jObj.getString("private_verification");
                hmp.put("private_verification", private_verification);
                police_verification = jObj.isNull("police_verification") ? "0" : jObj.getString("police_verification");
                hmp.put("police_verification", police_verification);
                hmp.put("sent_to_server_flag", "1");
                other_company_name = jObj.isNull("other_company_name") ? "" : jObj.getString("other_company_name");
                if (!TextUtils.isEmpty(other_company_name))
                    hmp.put("other_company_name", other_company_name);
                if (!TextUtils.isEmpty(driver_firstname) || !TextUtils.isEmpty(driver_lastname) ||
                        !TextUtils.isEmpty(driver_phoneno)) {
                    long status = DriverList.mApp.mDBHelper.insertDriverProfile(hmp);
                    if (status == -1)
                        DriverList.mApp.mDBHelper.updateDriverProfile(driver_phoneno, hmp, null, null, null, null);

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "error is " + e.toString());
        }
    }
}
