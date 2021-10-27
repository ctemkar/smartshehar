package com.smartshehar.dashboard.app;


public class CMarker {
    double lat, lng;
    String compalint_type_code, issue_name;

    public CMarker(double lat, double lng, String compalint_type_code, String issue_name) {
        this.lat = lat;
        this.lng = lng;
        this.compalint_type_code = compalint_type_code;
        this.issue_name = issue_name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getCompalint_type_code() {
        return compalint_type_code;
    }

    public void setCompalint_type_code(String compalint_type_code) {
        this.compalint_type_code = compalint_type_code;
    }

    public String getIssue_name() {
        return issue_name;
    }

    public void setIssue_name(String issue_name) {
        this.issue_name = issue_name;
    }
}
