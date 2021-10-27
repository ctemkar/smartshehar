package com.jumpinjumpout;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lib.app.util.Connectivity;

public class DriverProfileListService extends Service {
    Connectivity mConnectivity;

    public static final String TAG = "ListService";
    CGlobals_dp mApp;
    Handler handler = new Handler();
    int interval = 1000 * 5; //180;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mConnectivity = new Connectivity();
        mApp = CGlobals_dp.getInstance(DriverProfileListService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // starts service
        // handler is used to execute piece of code after perticular time.
        handler.postDelayed(runnableSendDataToServer, interval);

        return START_STICKY;
    }

    protected Runnable runnableSendDataToServer = new Runnable() {
        @Override
        public void run() {
            /// write a code ///
            checkConnection();
            handler.postDelayed(this, interval);
        }
    };

    public void checkConnection() {
        if (Connectivity.checkConnected(DriverProfileListService.this)) {
            if (!mConnectivity.connError(DriverProfileListService.this)) {
                if (mApp != null) {
                    if (mApp.mDBHelper != null) {
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
                            //
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
                                    hmpDriverImage.put("clientdatetime", mDriverInfo.getDriver_image_datettime());
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
                                    hmpPanImage.put("clientdatetime", mDriverInfo.getPancard_image_datettime());
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
                                    hmpAdharImage.put("clientdatetime", mDriverInfo.getAadhar_image_datetime());
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
                                    hmpLicenseImage.put("clientdatetime", mDriverInfo.getLicense_image_datetime());
                                if (!TextUtils.isEmpty(mDriverInfo.getDriver_phoneno()))
                                    hmpLicenseImage.put("driverphoneno", mDriverInfo.getDriver_phoneno());
                                hmpLicenseImage.put("imagetype", "License");
                            }
                            sendDataToServer(hmp, hmpDriverImage, hmpPanImage, hmpAdharImage, hmpLicenseImage, mDriverInfo.getOther_company_code());

                        }

                    }
                }
            }
        }
    }

    private void sendDataToServer(final HashMap<String, String> hmp, final HashMap<String, String> hmpDriverImage,
                                  final HashMap<String, String> hmpPanImage, final HashMap<String, String> hmpAdharImage,
                                  final HashMap<String, String> hmpLicenseImage, final String mOtherLookup) {
        try {
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
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "e" + error);
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
                    params = CGlobals_dp.getInstance(DriverProfileListService.this).getBasicMobileParams(params,
                            url, DriverProfileListService.this);

                    return CGlobals_dp.getInstance(DriverProfileListService.this).checkParams(params);
                }
            };
            CGlobals_dp.getInstance(DriverProfileListService.this).getRequestQueue(DriverProfileListService.this).add(postRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendImagesToServer(final HashMap<String, String> hmp, final String phoneno, final String driverprofileid) {
        try {
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
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mApp.mDBHelper.updateDriverProfile(phoneno, "0");
                    Log.d(TAG, "e" + error);
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
                    params = CGlobals_dp.getInstance(DriverProfileListService.this).
                            getBasicMobileParams(params,
                                    url, DriverProfileListService.this);

                    return CGlobals_dp.getInstance(DriverProfileListService.this).
                            checkParams(params);
                }
            };
            CGlobals_dp.getInstance(DriverProfileListService.this).
                    getRequestQueue(DriverProfileListService.this).add(postRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendOtherCompanyInfo(final String mOtherLookup, final String phoneno, final String driverprofileid) {
        try {
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
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    mApp.mDBHelper.updateDriverProfile(phoneno, "0");
                    Log.d(TAG, "e" + error);
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
                        cMember.driver_profile_id = driverprofileid;
                        curMember.add(cMember);
                    }
                    if (curMember.size() > 0) {
                        lookup = new Gson().toJson(curMember);
                    }
                    if (!TextUtils.isEmpty(lookup))
                        params.put("othercabrows", lookup);
                    params.put("driverprofileid", driverprofileid);
                    params.put("driverphoneno", phoneno);
                    params = CGlobals_dp.getInstance(DriverProfileListService.this).
                            getBasicMobileParams(params,
                                    url, DriverProfileListService.this);

                    return CGlobals_dp.getInstance(DriverProfileListService.this).
                            checkParams(params);
                }
            };
            CGlobals_dp.getInstance(DriverProfileListService.this).
                    getRequestQueue(DriverProfileListService.this).add(postRequest);
        } catch (Exception e) {
            mApp.mDBHelper.updateDriverProfile(phoneno, "0");
            e.printStackTrace();
        }
    }

}
