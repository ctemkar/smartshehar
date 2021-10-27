package com.smartshehar.dashboard.app;


public class CNotification {

    String mType,mSubType, mTime, mMessage, mAddress;
    int mIssueId;
    long mUniqueKey;

    public CNotification(String mType,String mSubType, String mTime, String mMessage,
                         String mAddress, long mUniqueKey, int mIssueId) {
        this.mType = mType;
        this.mSubType = mSubType;
        this.mTime = mTime;
        this.mMessage = mMessage;
        this.mAddress = mAddress;
        this.mUniqueKey = mUniqueKey;
        this.mIssueId = mIssueId;

    }

    public String getmType() {
        return mType;
    }

    public void setmType(String mType) {
        this.mType = mType;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public String getmMessage() {
        return mMessage;
    }

    public void setmMessage(String mNotificationType) {
        this.mMessage = mNotificationType;
    }

    public long getmUniqueKey() {
        return mUniqueKey;
    }

    public void setmUniqueKey(long mUniqueKey) {
        this.mUniqueKey = mUniqueKey;
    }

    public String getmAddress() {
        return mAddress;
    }

    public void setmAddress(String mAddress) {
        this.mAddress = mAddress;
    }



    public String getmSubType() {
        return mSubType;
    }

    public void setmSubType(String mSubType) {
        this.mSubType = mSubType;
    }

    public int getmIssueId() {
        return mIssueId;
    }

    public void setmIssueId(int mIssueId) {
        this.mIssueId = mIssueId;
    }
}
