package com.smartshehar.cabe.driver;

/**
 * Created by jijo_soumen on 14/05/2016.
 * Weekly History Result
 */
public class Weekly_History_Result {

    Double iCost;
    String dCurrentDate;
    Double dDistance;
    int iTotalTrip;

    public void setCost(Double isCost) {
        this.iCost = isCost;
    }

    public Double getCost() {
        return iCost;
    }

    public void setCurrentDate(String ddCurrentDate) {
        this.dCurrentDate = ddCurrentDate;
    }

    public String getCurrentDate() {
        return dCurrentDate;
    }

    public void setDistance(Double ddDistance) {
        this.dDistance = ddDistance;
    }

    public Double getDistance() {
        return dDistance;
    }

    public void setTotalTrip(int iiTotalTrip) {
        this.iTotalTrip = iiTotalTrip;
    }

    public int getTotalTrip() {
        return iTotalTrip;
    }
}
