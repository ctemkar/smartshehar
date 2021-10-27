package com.smartshehar.cabe.driver;

/**
 * Created by jijo_soumen on 16/03/2016.
 * Vehicle Result
 */
public class VehicleResult {
    String msFirstName = "", msLastName = "", sPhoneNo = "", sDate_of_Birth = "", sSex = "", sShift = "";
    String commercial_vehicle_id = "";
    String company_Id = "";
    String vehicle_No = "";
    String vehicle_company = "";
    String vehicle_model = "";
    String vehicle_type = "";
    String vehicle_color = "";
    String vehicle_seating = "";
    String vehicle_distinctive_marks = "";
    String vehicle_date_registered = "";
    String vehicle_is_booked = "";
    String sCompany_Name = "";
    String sCompany_Address = "";
    String sDriver_Appuser_Id = "";
    String sRate_Per_Km = "";
    String sMinimum_Fare = "";
    String sVehicle_Category = "";
    String msServiceCode = "";
    String msFull_Vehicleno = "";

    public void setVehicleID(String commercialvehicleid) {
        this.commercial_vehicle_id = commercialvehicleid;
    }

    public String getVehicleID() {
        return commercial_vehicle_id;
    }

    public void setCompanyId(String companyId) {
        this.company_Id = companyId;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicle_No = vehicleNo;
    }

    public String getVehicleNo() {
        return vehicle_No;
    }

    public void setVehicleCompany(String vehiclecompany) {
        this.vehicle_company = vehiclecompany;
    }

    public String getVehicleCompany() {
        return vehicle_company;
    }

    public void setVehicleModel(String vehiclemodel) {
        this.vehicle_model = vehiclemodel;
    }

    public String getVehicleModel() {
        return vehicle_model;
    }

    public void setVehicleType(String vehicletype) {
        this.vehicle_type = vehicletype;
    }

    public void setVehicleColor(String vehiclecolor) {
        this.vehicle_color = vehiclecolor;
    }

    public String getVehicleColor() {
        return vehicle_color;
    }

    public void setVehicleSeating(String vehicleseating) {
        this.vehicle_seating = vehicleseating;
    }

    public String getVehicleSeating() {
        return vehicle_seating;
    }

    public void setVehicleDistinctiveMarks(String vehicledistinctivemarks) {
        this.vehicle_distinctive_marks = vehicledistinctivemarks;
    }

    public void setVehicleDateRegistered(String vehicledateregistered) {
        this.vehicle_date_registered = vehicledateregistered;
    }

    public void setVehicleIsBooked(String vehicleisbooked) {
        this.vehicle_is_booked = vehicleisbooked;
    }

    public void setFirstName(String sFirstName) {
        this.msFirstName = sFirstName;
    }

    public void setLastName(String sLastName) {
        this.msLastName = sLastName;
    }

    public void setPhoneNo(String phoneNo) {
        this.sPhoneNo = phoneNo;
    }

    public void setDateofBirth(String ssDateofBirth) {
        this.sDate_of_Birth = ssDateofBirth;
    }

    public void setSex(String sex) {
        this.sSex = sex;
    }

    public void setShift(String shift) {
        this.sShift = shift;
    }

    public void setCompany_Name(String ssCompany_Name) {
        this.sCompany_Name = ssCompany_Name;
    }

    public void setCompany_Address(String ssCompany_Address) {
        this.sCompany_Address = ssCompany_Address;
    }

    public void setDriver_Appuser_Id(String ssDriver_Appuser_Id) {
        this.sDriver_Appuser_Id = ssDriver_Appuser_Id;
    }

    public void setRate_Per_Km(String ssRate_Per_Km) {
        this.sRate_Per_Km = ssRate_Per_Km;
    }

    public void setMinimum_Fare(String ssMinimum_Fare) {
        this.sMinimum_Fare = ssMinimum_Fare;
    }

    public void setVehicle_Category(String ssVehicle_Category) {
        this.sVehicle_Category = ssVehicle_Category;
    }

    public void setServiceCode(String sServiceCode) {
        this.msServiceCode = sServiceCode;
    }

    public String getServiceCode() {
        return msServiceCode;
    }

    public void setFull_Vehicleno(String sFull_Vehicleno){
        this.msFull_Vehicleno = sFull_Vehicleno;
    }

    public String getFull_Vehicleno() {
        return msFull_Vehicleno;
    }

}
