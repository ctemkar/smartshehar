package com.smartshehar.customercalls.apk;

import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;
import java.util.Map;

/**
 * Created by asmita on 30/04/2016.
 */
public class CCallHandle {

    private final String TAG = "CCallHandle: ";
    private int miCallLogId;
    private long miCallLogDate;
    private String msPhoneNo;
    private String msCountryCode;
    private String msNationalNumber;
    private int miCallHandled;
    private int miStorePhoneId;
    private int miCallLogIdServer;


    public CCallHandle() {
    }
    public Map<String, String> paramsPut(Map<String, String> params) {
        params.put("calllogid", String.valueOf(getMiCallLogId()));
        params.put("calldatetime", String.valueOf(getMiCallLogDate()));
        params.put("phone", getMsNationalNumber());
        params.put("c",getMsCountryCode());
        params.put("callhandle",String.valueOf(getMiCallHandled()));
        params.put("storephoneid",String.valueOf(getMiStorePhoneId()));

        return params;
    }
    public int getMiCallLogId() {
        return miCallLogId;
    }

    public void setMiCallLogId(int miCallLogId) {
        this.miCallLogId = miCallLogId;
    }

    public long getMiCallLogDate() {
        return miCallLogDate;
    }

    public void setMiCallLogDate(long miCallLogDate) {
        this.miCallLogDate = miCallLogDate;
    }

    public String getMsPhoneNo() {
        return msPhoneNo;
    }

    public void setMsPhoneNo(String msPhoneNo) {

        this.msPhoneNo = msPhoneNo;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(msPhoneNo, Locale.getDefault().getCountry());
            msCountryCode = String.valueOf(numberProto.getCountryCode());
            msNationalNumber = String.valueOf(numberProto.getNationalNumber());

            if (phoneUtil.isValidNumber(numberProto)) {
                Log.d(TAG, "Country Code: " + numberProto.getCountryCode());
            } else {
                Log.d(TAG, "Invalid number format: " + msPhoneNo);
            }
        } catch (NumberParseException e) {
            Log.d(TAG, "Unable to parse phoneNumber " + e.toString());
        }
    }

    public int getMiCallHandled() {
        return miCallHandled;
    }

    public void setMiCallHandled(int miCallHandled) {
        this.miCallHandled = miCallHandled;
    }

    public int getMiStorePhoneId() {
        return miStorePhoneId;
    }

    public void setMiStorePhoneId(int miStorePhoneId) {
        this.miStorePhoneId = miStorePhoneId;
    }

    public String getMsCountryCode() {
        return msCountryCode;
    }

    public void setMsCountryCode(String msCountryCode) {
        this.msCountryCode = msCountryCode;
    }

    public String getMsNationalNumber() {
        return msNationalNumber;
    }

    public void setMsNationalNumber(String msNationalNumber) {
        this.msNationalNumber = msNationalNumber;
    }

    public int getMiCallLogIdServer() {
        return miCallLogIdServer;
    }

    public void setMiCallLogIdServer(int miCallLogIdServer) {
        this.miCallLogIdServer = miCallLogIdServer;
    }
}
