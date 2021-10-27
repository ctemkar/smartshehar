package com.smartshehar.customercalls.apk;

import java.util.Map;

/**
 * Created by asmita on 27/04/2016.
 */
public class COrderHeader {
    private int miCustomerIdLocal;
    private final String TAG = "OrderHeader: ";
    private int miOrederId;
    private int miStorePhoneId;
    private String msPhone;
    private String msCountryCode;
    private int miOrderAmount;
    private long miOrderDate;
    private int miCustomerCallId;



    public COrderHeader() {

    }

    public Map<String, String> paramsPut(Map<String, String> params) {
        params.put("c",getMsCountryCode());
        params.put("ph", getMsPhone());
        params.put("storephoneid", String.valueOf(getMiStorePhoneId()));
        params.put("orderamount",String.valueOf(getMiOrderAmount()));
        params.put("orderdatetime",String.valueOf(getMiOrderDate()));
        params.put("calllogid",String.valueOf(getMiCustomerCallId()));

        return params;
    }

    public int getMiCustomerCallId() {
        return miCustomerCallId;
    }

    public void setMiCustomerCallId(int miCustomerCallId) {
        this.miCustomerCallId = miCustomerCallId;
    }

    public int getMiOrederId() {
        return miOrederId;
    }

    public void setMiOrederId(int miOrederId) {
        this.miOrederId = miOrederId;
    }

    public int getMiStorePhoneId() {
        return miStorePhoneId;
    }

    public void setMiStorePhoneId(int miStorePhoneId) {
        this.miStorePhoneId = miStorePhoneId;
    }



    public int getMiOrderAmount() {
        return miOrderAmount;
    }

    public void setMiOrderAmount(int miOrderAmount) {
        this.miOrderAmount = miOrderAmount;
    }

    public long getMiOrderDate() {
        return miOrderDate;
    }

    public void setMiOrderDate(long miOrderDate) {
        this.miOrderDate = miOrderDate;
    }

    public int getMiCustomerIdLocal() {
        return miCustomerIdLocal;
    }

    public void setMiCustomerIdLocal(int miCustomerIdLocal) {
        this.miCustomerIdLocal = miCustomerIdLocal;
    }

    public String getMsPhone() {
        return msPhone;
    }

    public void setMsPhone(String msPhone) {
        this.msPhone = msPhone;
    }

    public String getMsCountryCode() {
        return msCountryCode;
    }

    public void setMsCountryCode(String msCountryCode) {
        this.msCountryCode = msCountryCode;
    }
}
