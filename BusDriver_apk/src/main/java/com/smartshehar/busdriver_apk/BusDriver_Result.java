package com.smartshehar.busdriver_apk;

/**
 * Created by user pc on 25-07-2015.
 */
public class BusDriver_Result {
    private String sRoutecode = "";
    private String sBuslabel = "";
    private String sStopcode = "";
    private String sStopserial = "";
    private String sStopname = "";
    private double dLat;
    private double dLon;
    private String sDistance = "";

    public void setRoutecode(String ssRoutecode) {
        this.sRoutecode = ssRoutecode;
    }

    public String getRoutecode() {
        return sRoutecode;
    }

    public void setBuslabel(String ssBuslabel) {
        this.sBuslabel = ssBuslabel;
    }

    public String getBuslabel() {
        return sBuslabel;
    }

    public void setStopcode(String ssStopcode) {
        this.sStopcode = ssStopcode;
    }

    public String getStopcode() {
        return sStopcode;
    }

    public void setStopserial(String ssStopserial) {
        this.sStopserial = ssStopserial;
    }

    public String getStopserial() {
        return sStopserial;
    }

    public void setStopname(String ssStopname) {
        this.sStopname = ssStopname;
    }

    public String getStopname() {
        return sStopname;
    }

    public void setDistance(String ssDistance) {
        this.sDistance = ssDistance;
    }

    public String getDistance() {
        return sDistance;
    }

    public void setLat(double ssLat) {
        this.dLat = ssLat;
    }

    public double getLat() {
        return dLat;
    }

    public void setLon(double ssLon) {
        this.dLon = ssLon;
    }

    public double getLon() {
        return dLon;
    }
}
