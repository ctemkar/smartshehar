package com.jumpinjumpout;

/**
 * Created by asmita on 07/03/2016.
 */
public class CMember {
    String driver_profile_id,other_company_code,driver_other_company_id;

    public CMember(String driver_profile_id, String other_company_code, String driver_other_company_id) {
        this.driver_profile_id = driver_profile_id;
        this.other_company_code = other_company_code;
        this.driver_other_company_id = driver_other_company_id;
    }

    public String getDriver_profile_id() {
        return driver_profile_id;
    }

    public void setDriver_profile_id(String driver_profile_id) {
        this.driver_profile_id = driver_profile_id;
    }

    public String getOther_company_code() {
        return other_company_code;
    }

    public void setOther_company_code(String other_company_code) {
        this.other_company_code = other_company_code;
    }

    public String getDriver_other_company_id() {
        return driver_other_company_id;
    }

    public void setDriver_other_company_id(String driver_other_company_id) {
        this.driver_other_company_id = driver_other_company_id;
    }
}
