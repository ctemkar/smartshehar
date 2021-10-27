package com.smartshehar.customercalls.apk;

import android.text.TextUtils;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;

/**
 * Created by ctemkar on 11/03/2016.
 * Incoming call data
 */
public class CCallInfo {
    private final String TAG = "CallInfo: ";
    private int getMiCustomerLocalId;
    private long miCustomerId;
    private int miCustomerCallId;
    private String msFirstName = "";
    private String msLastName = "";
    private String msCompany = "";
    private String msCountryCode = "";
    private String msNationalNumber = "";
    public String msPhoneNo = "";
    private String msFlatNo = "";
    private String msWing = "";
    private String msFloorNo = "";
    private String msBulding = "";
    private String msArea = "";
    private String msRoad = "";
    private String msLandmark1 = "";
    private String msLandmark2 = "";
    private String msComplex;
    private String msCity;
    private String msState;
    private String msCountry;
    private String msPostalCode;
    private Date msClientDateTime;
    private String msCustomerInfoAll;
    private boolean mIsIgnore = false;
    private boolean mIsDeleted = false;
    private boolean mIsNew = true;
    private long dateCreated = -1;

    public long getDateUpdated() {
        return mDateUpdated;
    }

    public void setDateUpdated(long dateUpdated) {
        this.mDateUpdated = dateUpdated;
    }

    public long getDateLastCall() {
        return mDateLastCall;
    }

    public void setDateLastCall(long dateLastCall) {
        this.mDateLastCall = dateLastCall;
    }

    public int getMiCustomerCallId() {
        return miCustomerCallId;
    }

    public void setMiCustomerCallId(int miCustomerCallId) {
        this.miCustomerCallId = miCustomerCallId;
    }

    private long mDateUpdated = -1;
    private long mDateLastCall = -1;

    public CCallInfo(JSONObject jResponse) {

        try {
            miCustomerId = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "customer_id") ? -1
                    : Integer.valueOf(jResponse.getString("customer_id"));
            miCustomerCallId = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "call_log_id") ? -1
                    : Integer.valueOf(jResponse.getString("call_log_id"));
            msFirstName = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "first_name") ? ""
                    : jResponse.getString("first_name");
            msLastName = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "last_name") ? ""
                    : jResponse.getString("last_name");
            msCompany = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "company") ? ""
                    : jResponse.getString("company");
            msCountryCode = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "country_code") ? ""
                    : jResponse.getString("country_code");
            msNationalNumber = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "phone") ? ""
                    : jResponse.getString("phone");
            msFlatNo = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "flat") ? ""
                    : jResponse.getString("flat");
            msWing = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "wing") ? ""
                    : jResponse.getString("wing");
            msFloorNo = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "floor") ? ""
                    : jResponse.getString("floor");
            msComplex = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "complex") ? ""
                    : jResponse.getString("complex");
            msBulding = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "building") ? ""
                    : jResponse.getString("building");
            msArea = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "area") ? ""
                    : jResponse.getString("area");
            msRoad = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "road") ? ""
                    : jResponse.getString("road");
            msLandmark1 = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "landmark_1") ? ""
                    : jResponse.getString("landmark_1");
            msLandmark2 = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "landmark_2") ? ""
                    : jResponse.getString("landmark_2");
            msCity = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "city") ? ""
                    : jResponse.getString("city");
            msState = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "state") ? ""
                    : jResponse.getString("state");
            msPostalCode = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "postal_code") ? ""
                    : jResponse.getString("postal_code");

            try {
                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM, yyyy", java.util.Locale.getDefault());
                msClientDateTime = CGlobals_lib_ss.getInstance().isNullNotDefined(jResponse, "client_datetime") ? null : formatter.parse(jResponse.getString("postal_code"));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public CCallInfo() {

    }

    public CCallInfo(boolean b) { // new Customer
        mIsNew = b;
        dateCreated = System.currentTimeMillis();
        mDateUpdated = System.currentTimeMillis();
    }

    public void setIsNew(boolean isNew) {
        this.mIsNew = isNew;
    }

    public boolean isNew() {
        return mIsNew;
    }

    public long getCustomerId() {
        return miCustomerId;
    }

    public int getGetMiCustomerLocalId() {
        return getMiCustomerLocalId;
    }

    public void setGetMiCustomerLocalId(int getMiCustomerLocalId) {
        this.getMiCustomerLocalId = getMiCustomerLocalId;
    }

    public void setCustomerId(long miCustomerId) {
        this.miCustomerId = miCustomerId;
    }

    public String getFirstName() {
        return msFirstName;
    }

    public void setFirstName(String msFirstName) {
        this.msFirstName = msFirstName;
    }

    public String getLastName() {
        return msLastName;
    }

    public void setLastName(String msLastName) {
        this.msLastName = msLastName;
    }

    public String getCompany() {
        return msCompany;
    }

    public String getFlatFloorBuildingWing() {
        String sFlatFloorWing;
        sFlatFloorWing = TextUtils.isEmpty(msFlatNo) ? "" : msFlatNo;
        sFlatFloorWing = TextUtils.isEmpty(sFlatFloorWing) ? "" +
                (TextUtils.isEmpty(msBulding) ? "" : msBulding)
                : (TextUtils.isEmpty(msBulding) ? sFlatFloorWing + "" : sFlatFloorWing + ", " + msBulding);
        sFlatFloorWing = TextUtils.isEmpty(sFlatFloorWing) ? "" + getFloorString()
                : sFlatFloorWing + getFloorString();
        sFlatFloorWing = TextUtils.isEmpty(sFlatFloorWing) ? "" +
                (TextUtils.isEmpty(msWing) ? "" : "Wing " + msWing)
                : (TextUtils.isEmpty(msWing) ? sFlatFloorWing + "" : sFlatFloorWing + ", Wing " + msWing);

        return sFlatFloorWing;
    }

    public void setCompany(String msCompanyName) {
        this.msCompany = msCompanyName;
    }

    public String getCountryCode() {
        return msCountryCode;
    }

    public void setCountryCode(String msCountryCode) {
        this.msCountryCode = msCountryCode;
    }

    public String getNationalNumber() {
        return msNationalNumber;
    }

    public void setNationalNumber(String msNationalNumber) {
        this.msNationalNumber = msNationalNumber;
    }

    public String getPhoneNo() {
        String phoneno = "+" + msCountryCode + msNationalNumber;
        return phoneno;
    }

    public void setPhoneNo(String sPhoneNo) {
        this.msPhoneNo = sPhoneNo;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(sPhoneNo, Locale.getDefault().getCountry());
            msPhoneNo = sPhoneNo;
            msCountryCode = String.valueOf(numberProto.getCountryCode());
            msNationalNumber = String.valueOf(numberProto.getNationalNumber());

            if (phoneUtil.isValidNumber(numberProto)) {
                Log.d("TAG", "Country Code: " + numberProto.getCountryCode());
            } else {
                Log.d("TAG", "Invalid number format: " + sPhoneNo);
            }
        } catch (NumberParseException e) {
            Log.d(TAG, "Unable to parse phoneNumber " + e.toString());
        }

    }

    public String getFlatNo() {
        return msFlatNo;
    }

    public void setFlatNo(String msFlatNo) {
        this.msFlatNo = msFlatNo;
    }

    public String getWing() {
        return isNull(msWing);
    }

    public void setWing(String msWing) {
        this.msWing = msWing;
    }

    public String getFloor() {
        return msFloorNo;
    }

    public String getFloorString() {
        String sFloor = msFloorNo;
        if (!TextUtils.isEmpty(msFloorNo)) {
            if (!msFloorNo.contains("[a-zA-Z]+")) {
                try {
                    sFloor = " " + ordinal_suffix_of(Integer.parseInt(msFloorNo)) + " floor";
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return isNull(sFloor);
    }

    public void setFloor(String sFloor) {
        this.msFloorNo = sFloor;
    }

    public String getBulding() {
        return isNull(msBulding);
    }

    public void setBulding(String msBuldingName) {
        this.msBulding = msBuldingName;
    }

    public String getArea() {
        return isNull(msArea);
    }

    public void setArea(String msArea) {
        this.msArea = msArea;
    }

    public String getRoad() {
        return isNull(msRoad);
    }

    public void setRoad(String msRoadName) {
        this.msRoad = msRoadName;
    }

    public String getLandmark1Disp() {
        String[] matches = new String[]{"near", "opp", "next"};
        if (TextUtils.isEmpty(msLandmark1))
            return "";
        boolean found = false;
        for (String s : matches) {
            if (msLandmark1.contains(s)) {
                found = true;
                break;
            }
        }
        if (found)
            return msLandmark1;
        else
            return "near " + msLandmark1;
    }

    public String getLandmark1() {
        return msLandmark1;
    }

    public String getLandmark2() {
        String[] matches = new String[]{"near", "opp", "next"};
        if (TextUtils.isEmpty(msLandmark2))
            return "";
        boolean found = false;
        for (String s : matches) {
            if (msLandmark2.contains(s)) {
                found = true;
                break;
            }
        }
        if (found)
            return msLandmark2;
        else
            return "near " + msLandmark2;
    }

    public void setLandmark1(String msLandmark1) {
        this.msLandmark1 = msLandmark1;
    }


    public void setLandmark2(String sLandmark2) {
        this.msLandmark2 = sLandmark2;
    }

    public String getCity() {
        return isNull(msCity);
    }

    public void setCity(String msCity) {
        this.msCity = msCity;
    }

    public String getCountry() {
        return msCountry;
    }

    public void setCountry(String sCountry) {
        this.msCountry = sCountry;
    }

    public void setState(String msState) {
        this.msState = msState;
    }

    public String getState() {
        return msState;
    }

    public String getPostalCode() {
        return msPostalCode;
    }

    public void setPostalCode(String msPostalCode) {
        this.msPostalCode = msPostalCode;
    }

    public Date getClientDateTime() {
        return msClientDateTime;
    }

    public void setClientDateTime(Date msClientDateTime) {
        this.msClientDateTime = msClientDateTime;
    }

    public void setDateCreated(long ms) { // Date craeted in ms
        dateCreated = ms;
    }

    public long getDateCreated() { // Date craeted in ms
        return dateCreated == -1 ? System.currentTimeMillis() : dateCreated;
    }


    public boolean hasNoData() {
        return !(!TextUtils.isEmpty(msFirstName) || !TextUtils.isEmpty((msLastName))
                || !TextUtils.isEmpty((msCompany)) || !TextUtils.isEmpty((msCountryCode))
                || !TextUtils.isEmpty((msNationalNumber)) || !TextUtils.isEmpty((msPhoneNo))
                || !TextUtils.isEmpty((msFlatNo)) || !TextUtils.isEmpty((msWing))
                || !TextUtils.isEmpty((msFloorNo)) || !TextUtils.isEmpty((msFirstName))
                || !TextUtils.isEmpty((msFloorNo)) || !TextUtils.isEmpty((msBulding))
                || !TextUtils.isEmpty((msArea)) || !TextUtils.isEmpty((msRoad))
                || !TextUtils.isEmpty((msLandmark1)) || !TextUtils.isEmpty((msLandmark2)));
    }

    public boolean IsIgnore() {
        return mIsIgnore;
    }

    public void setIgnore(boolean mIsIgnore) {
        this.mIsIgnore = mIsIgnore;
    }

    public boolean IsDeleted() {
        return mIsDeleted;
    }

    public void setDeleted(boolean isDelete) {
        this.mIsDeleted = isDelete;
    }


    public String getCustomerInfoAll() {
        return msCustomerInfoAll;
    }

    public void setCustomerInfoAll(String sCustomerInfoAll) {
        this.msCustomerInfoAll = sCustomerInfoAll;
    }


    public String getComplex() {
        return msComplex;
    }

    public void setComplexOrColony(String sComplex) {
        this.msComplex = sComplex;
    }


    public String getName() {
        String sName;
        sName = TextUtils.isEmpty(getFirstName()) ? "" : getFirstName().trim() + " ";
        sName += getLastName().trim();
        return sName;
    }

    public String getAddress() {
        String sAddress;
        sAddress = TextUtils.isEmpty(getFlatNo()) ? "" : getFlatNo();
        sAddress = TextUtils.isEmpty(sAddress) ? "" : sAddress + ", " + getBulding();
        sAddress += " " + getFloorString();
        return sAddress;
    }

    private String ordinal_suffix_of(int i) {
        int j = i % 10,
                k = i % 100;
        if (j == 1 && k != 11) {
            return i + "st";
        }
        if (j == 2 && k != 12) {
            return i + "nd";
        }
        if (j == 3 && k != 13) {
            return i + "rd";
        }
        return i + "th";
    }

    private String isNull(String sVal) {
        return sVal == null ? "" : sVal;
    }

    public Map<String, String> paramsPut(Map<String, String> params) {
        if (!TextUtils.isEmpty(getFirstName()))
            params.put("firstname", getFirstName());
        if (!TextUtils.isEmpty(getLastName()))
            params.put("lastname", getLastName());
        if (!TextUtils.isEmpty(getCompany()))
            params.put("company", getCompany());
        if (!TextUtils.isEmpty(getCountryCode()))
            params.put("c", getCountryCode());
        if (!TextUtils.isEmpty(getNationalNumber()))
            params.put("ph", getNationalNumber());
        if (!TextUtils.isEmpty(getFlatNo()))
            params.put("flat", getFlatNo());
        if (!TextUtils.isEmpty(getWing()))
            params.put("wing", getWing());
        if (!TextUtils.isEmpty(getFloor()))
            params.put("floor", getFloor());
        if (!TextUtils.isEmpty(getComplex()))
            params.put("complex", getComplex());
        if (!TextUtils.isEmpty(getBulding()))
            params.put("building", getBulding());
        if (!TextUtils.isEmpty(getArea()))
            params.put("area", getArea());
        if (!TextUtils.isEmpty(getRoad()))
            params.put("road", getRoad());
        if (!TextUtils.isEmpty(getLandmark1()))
            params.put("landmark1", getLandmark1());
        if (!TextUtils.isEmpty(getLandmark2()))
            params.put("landmark2", getLandmark2());
        if (!TextUtils.isEmpty(getCity()))
            params.put("city", getCity());
        if (!TextUtils.isEmpty(getState()))
            params.put("state", getState());
        if (!TextUtils.isEmpty(getPostalCode()))
            params.put("postalcode", getPostalCode());
        long currentTime = System.currentTimeMillis();
        if (dateCreated == -1) {
            dateCreated = currentTime;
        }
        params.put("created", String.valueOf(dateCreated));
        params.put("last_updated", String.valueOf(System.currentTimeMillis()));


        return params;
    }

}
/*
{"customer_id":"1","first_name":"Chetan ","last_name":"Temkar",
"company_name":null,"country_code":null,"phoneno":"9892452492",
"flat_no":"103","wing":null,"floor_no":"1","building_name":"Smruti",
"area":"Vile-Parle(E)","roadname":"M.V. Pandloskar Marg",
"landmark_1":"Shivaji High School","landmark_2":null,"city":"Mumbai",
"state":"Maharashtra","postal_code":"400057","client_datetime":null}
 */