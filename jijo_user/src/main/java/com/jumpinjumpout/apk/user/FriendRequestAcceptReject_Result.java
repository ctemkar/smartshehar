package com.jumpinjumpout.apk.user;

/**
 * Created by user pc on 05-10-2015.
 */
public class FriendRequestAcceptReject_Result {

    int iFriendid = 0;
    int iAppuserIdMe = 0;
    int iAppuserIdFriend = 0;
    String sUserName = "";
    String sFullName = "";

    public void setiFriendid(int iFriendid) {
        this.iFriendid = iFriendid;
    }

    public int getiFriendid() {
        return iFriendid;
    }

    public void setiAppuserIdMe(int iAppuserIdMe) {
        this.iAppuserIdMe = iAppuserIdMe;
    }

    public void setiAppuserIdFriend(int iAppuserIdFriend) {
        this.iAppuserIdFriend = iAppuserIdFriend;
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
}
