package com.jumpinjumpout.apk.user;

/**
 * Created by user pc on 01-10-2015.
 */
public class FindWorkAddress_Result {

    int iAppUserId = 0;
    String sUserName = "";
    String sFullName = "";
    String sEmail = "";
    String sPhoneNumber = "";
    public boolean box;

    public FindWorkAddress_Result() {
    }

    public void setiAppUserId(int iAppUserId) {
        this.iAppUserId = iAppUserId;
    }

    public int getiAppUserId() {
        return iAppUserId;
    }

    public void setsUserName(String sUserName) {
        this.sUserName = sUserName;
    }

    public void setsFullName(String sFullName) {
        this.sFullName = sFullName;
    }

    public String getsFullName() {
        return sFullName;
    }

    public void setsEmail(String sEmail) {
        this.sEmail = sEmail;
    }

    public void setsPhoneNumber(String sPhoneNumber) {
        this.sPhoneNumber = sPhoneNumber;
    }
}
