package com.smartshehar.dashboard.app;

/**
 * Created by asmita on 15/02/2016.
 */
public class CRecentCategory {
    String issueDescription;
    String issueitemcode;
    boolean imageVerification;
    boolean vehicleNumber;
    String issueType;

    public CRecentCategory(String issueDescription, String issueitemcode, boolean imageVerification, boolean vehicleNumber, String issueType) {
        this.issueDescription = issueDescription;
        this.issueitemcode = issueitemcode;
        this.imageVerification = imageVerification;
        this.vehicleNumber = vehicleNumber;
        this.issueType = issueType;

    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public String getIssueitemcode() {
        return issueitemcode;
    }

    public void setIssueitemcode(String issueitemcode) {
        this.issueitemcode = issueitemcode;
    }

    public boolean getImageVerification() {
        return imageVerification;
    }

    public void setImageVerification(boolean imageVerification) {
        this.imageVerification = imageVerification;
    }

    public boolean getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(boolean vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
}
