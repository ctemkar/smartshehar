package com.jumpinjumpout;

public class DriverInfo {

    String driver_profile_id;
    String unique_key;
    String clientdatetime;
    String driver_firstname;
    String driver_lastname;
    String driver_dob;
    String driver_email;
    String driver_phoneno;
    String driver_gender;
    String driver_pancard;
    String driver_aadhar;
    String driver_license_no;
    String driver_permit_flag;
    String driver_permit_no;
    String driver_badge_flag;
    String driver_badge_no;
    String driver_incity_since;
    String cab_needed_flag;
    String driver_vehicle_no;
    String remark;
    String internal_comment;
    String address_id;
    String google_address;
    String t_address_1;
    String t_address_2;
    String t_city_town;
    String t_landmark;
    String t_state;
    String t_pincode;
    String p_address_1;
    String p_address_2;
    String p_city_town;
    String p_landmark;
    String p_state;
    String p_pincode;
    String other_company_code;
    String driver_other_company_id;
    String driver_image_name;
    String driver_image_path;
    String driver_image_datettime;
    String pancard_image_name;
    String pancard_image_path;
    String pancard_image_datettime;
    String license_image_name;
    String license_image_path;
    String license_image_datetime;
    String aadhar_image_name;
    String aadhar_image_path;
    String aadhar_image_datetime;
    String last_modified_datetime;
    String sent_to_server_datetime;
    String sent_to_server_flag;
    String other_company_name;
    String uniqueid;
    String payment_amount;
    String payment_status;
    String receipt_no;
    String receipt_date;
    String referral_walk_in;
    String referral_supervisor;
    String referral_driver;
    String receipt_no_2;
    String receipt_no_3;
    String driver_backout_reason;
    String private_verification;
    String police_verification;
    String tr_license_exp_date;
    String receipt_date_2;
    String receipt_date_3;




    byte[] driver_image, aadhar_image, pancard_image, license_image;

    public DriverInfo(String driver_profile_id, String unique_key,
                      String clientdatetime, String driver_firstname,
                      String driver_lastname, String driver_dob, String driver_email,
                      String driver_phoneno, String driver_gender,
                      String driver_pancard, String driver_aadhar,
                      String driver_license_no, String driver_permit_flag,
                      String driver_permit_no, String driver_badge_flag,
                      String driver_badge_no, String driver_incity_since,
                      String cab_needed_flag, String driver_vehicle_no,
                      String remark, String internal_comment, String address_id,
                      String google_address, String t_address_1, String t_address_2,
                      String t_city_town, String t_landmark, String t_state,
                      String t_pincode, String p_address_1, String p_address_2,
                      String p_city_town, String p_landmark, String p_state,
                      String p_pincode, String other_company_code,
                      String driver_other_company_id, String driver_image_name,
                      String driver_image_path, byte[] driver_image, String driver_image_datettime,
                      String pancard_image_name, String pancard_image_path, byte[] pancard_image,
                      String pancard_image_datettime, String license_image_name,
                      String license_image_path, byte[] license_image, String license_image_datetime,
                      String aadhar_image_name, String aadhar_image_path, byte[] aadhar_image,
                      String aadhar_image_datetime, String last_modified_datetime, String sent_to_server_datetime,
                      String sent_to_server_flag, String other_company_name, String uniqueid, String payment_amount,
                      String payment_status, String receipt_no, String receipt_date,
                      String referral_walk_in,String referral_supervisor,String referral_driver,String receipt_no_2,
                      String receipt_no_3,String driver_backout_reason,String private_verification,
                      String police_verification, String tr_license_exp_date,
                      String receipt_date_2,String receipt_date_3) {
        this.driver_profile_id = driver_profile_id;
        this.unique_key = unique_key;
        this.clientdatetime = clientdatetime;
        this.driver_firstname = driver_firstname;
        this.driver_lastname = driver_lastname;
        this.driver_dob = driver_dob;
        this.driver_email = driver_email;
        this.driver_phoneno = driver_phoneno;
        this.driver_gender = driver_gender;
        this.driver_pancard = driver_pancard;
        this.driver_aadhar = driver_aadhar;
        this.driver_license_no = driver_license_no;
        this.driver_permit_flag = driver_permit_flag;
        this.driver_permit_no = driver_permit_no;
        this.driver_badge_flag = driver_badge_flag;
        this.driver_badge_no = driver_badge_no;
        this.driver_incity_since = driver_incity_since;
        this.cab_needed_flag = cab_needed_flag;
        this.driver_vehicle_no = driver_vehicle_no;
        this.remark = remark;
        this.internal_comment = internal_comment;
        this.address_id = address_id;
        this.google_address = google_address;
        this.t_address_1 = t_address_1;
        this.t_address_2 = t_address_2;
        this.t_city_town = t_city_town;
        this.t_landmark = t_landmark;
        this.t_state = t_state;
        this.t_pincode = t_pincode;
        this.p_address_1 = p_address_1;
        this.p_address_2 = p_address_2;
        this.p_city_town = p_city_town;
        this.p_landmark = p_landmark;
        this.p_state = p_state;
        this.p_pincode = p_pincode;
        this.other_company_code = other_company_code;
        this.driver_other_company_id = driver_other_company_id;
        this.driver_image_name = driver_image_name;
        this.driver_image_path = driver_image_path;
        this.driver_image_datettime = driver_image_datettime;
        this.pancard_image_name = pancard_image_name;
        this.pancard_image_path = pancard_image_path;
        this.pancard_image_datettime = pancard_image_datettime;
        this.license_image_name = license_image_name;
        this.license_image_path = license_image_path;
        this.license_image_datetime = license_image_datetime;
        this.aadhar_image_name = aadhar_image_name;
        this.aadhar_image_path = aadhar_image_path;
        this.aadhar_image_datetime = aadhar_image_datetime;
        this.last_modified_datetime = last_modified_datetime;
        this.sent_to_server_datetime = sent_to_server_datetime;
        this.sent_to_server_flag = sent_to_server_flag;
        this.other_company_name = other_company_name;
        this.driver_image = driver_image;
        this.pancard_image = pancard_image;
        this.license_image = license_image;
        this.aadhar_image = aadhar_image;
        this.uniqueid = uniqueid;
        this.payment_amount = payment_amount;
        this.payment_status = payment_status;
        this.receipt_no = receipt_no;
        this.receipt_date = receipt_date;
        this.referral_walk_in=referral_walk_in;
        this.referral_supervisor = referral_supervisor;
        this.referral_driver = referral_driver;
        this.receipt_no_2 = receipt_no_2;
        this.receipt_no_3 = receipt_no_3;
        this.driver_backout_reason = driver_backout_reason;
        this.private_verification = private_verification;
        this.police_verification = police_verification;
        this.tr_license_exp_date = tr_license_exp_date;
        this.receipt_date_2 = receipt_date_2;
        this.receipt_date_3 = receipt_date_3;

    }

    public String getDriver_profile_id() {
        return driver_profile_id;
    }

    public void setDriver_profile_id(String driver_profile_id) {
        this.driver_profile_id = driver_profile_id;
    }

    public String getUnique_key() {
        return unique_key;
    }

    public void setUnique_key(String unique_key) {
        this.unique_key = unique_key;
    }

    public String getClientdatetime() {
        return clientdatetime;
    }

    public void setClientdatetime(String clientdatetime) {
        this.clientdatetime = clientdatetime;
    }

    public String getDriver_firstname() {
        return driver_firstname;
    }

    public void setDriver_firstname(String driver_firstname) {
        this.driver_firstname = driver_firstname;
    }

    public String getDriver_lastname() {
        return driver_lastname;
    }

    public void setDriver_lastname(String driver_lastname) {
        this.driver_lastname = driver_lastname;
    }

    public String getDriver_dob() {
        return driver_dob;
    }

    public void setDriver_dob(String driver_dob) {
        this.driver_dob = driver_dob;
    }

    public String getDriver_email() {
        return driver_email;
    }

    public void setDriver_email(String driver_email) {
        this.driver_email = driver_email;
    }

    public String getDriver_phoneno() {
        return driver_phoneno;
    }

    public void setDriver_phoneno(String driver_phoneno) {
        this.driver_phoneno = driver_phoneno;
    }

    public String getDriver_gender() {
        return driver_gender;
    }

    public void setDriver_gender(String driver_gender) {
        this.driver_gender = driver_gender;
    }

    public String getDriver_pancard() {
        return driver_pancard;
    }

    public void setDriver_pancard(String driver_pancard) {
        this.driver_pancard = driver_pancard;
    }

    public String getDriver_aadhar() {
        return driver_aadhar;
    }

    public void setDriver_aadhar(String driver_aadhar) {
        this.driver_aadhar = driver_aadhar;
    }

    public String getDriver_license_no() {
        return driver_license_no;
    }

    public void setDriver_license_no(String driver_license_no) {
        this.driver_license_no = driver_license_no;
    }

    public String getDriver_permit_flag() {
        return driver_permit_flag;
    }

    public void setDriver_permit_flag(String driver_permit_flag) {
        this.driver_permit_flag = driver_permit_flag;
    }

    public String getDriver_permit_no() {
        return driver_permit_no;
    }

    public void setDriver_permit_no(String driver_permit_no) {
        this.driver_permit_no = driver_permit_no;
    }

    public String getDriver_badge_flag() {
        return driver_badge_flag;
    }

    public void setDriver_badge_flag(String driver_badge_flag) {
        this.driver_badge_flag = driver_badge_flag;
    }

    public String getDriver_badge_no() {
        return driver_badge_no;
    }

    public void setDriver_badge_no(String driver_badge_no) {
        this.driver_badge_no = driver_badge_no;
    }

    public String getDriver_incity_since() {
        return driver_incity_since;
    }

    public void setDriver_incity_since(String driver_incity_since) {
        this.driver_incity_since = driver_incity_since;
    }

    public String getCab_needed_flag() {
        return cab_needed_flag;
    }

    public void setCab_needed_flag(String cab_needed_flag) {
        this.cab_needed_flag = cab_needed_flag;
    }

    public String getDriver_vehicle_no() {
        return driver_vehicle_no;
    }

    public void setDriver_vehicle_no(String driver_vehicle_no) {
        this.driver_vehicle_no = driver_vehicle_no;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getInternal_comment() {
        return internal_comment;
    }

    public void setInternal_comment(String internal_comment) {
        this.internal_comment = internal_comment;
    }

    public String getAddress_id() {
        return address_id;
    }

    public void setAddress_id(String address_id) {
        this.address_id = address_id;
    }

    public String getGoogle_address() {
        return google_address;
    }

    public void setGoogle_address(String google_address) {
        this.google_address = google_address;
    }

    public String getT_address_1() {
        return t_address_1;
    }

    public void setT_address_1(String t_address_1) {
        this.t_address_1 = t_address_1;
    }

    public String getT_address_2() {
        return t_address_2;
    }

    public void setT_address_2(String t_address_2) {
        this.t_address_2 = t_address_2;
    }

    public String getT_city_town() {
        return t_city_town;
    }

    public void setT_city_town(String t_city_town) {
        this.t_city_town = t_city_town;
    }

    public String getT_landmark() {
        return t_landmark;
    }

    public void setT_landmark(String t_landmark) {
        this.t_landmark = t_landmark;
    }

    public String getT_state() {
        return t_state;
    }

    public void setT_state(String t_state) {
        this.t_state = t_state;
    }

    public String getT_pincode() {
        return t_pincode;
    }

    public void setT_pincode(String t_pincode) {
        this.t_pincode = t_pincode;
    }

    public String getP_address_1() {
        return p_address_1;
    }

    public void setP_address_1(String p_address_1) {
        this.p_address_1 = p_address_1;
    }

    public String getP_address_2() {
        return p_address_2;
    }

    public void setP_address_2(String p_address_2) {
        this.p_address_2 = p_address_2;
    }

    public String getP_city_town() {
        return p_city_town;
    }

    public void setP_city_town(String p_city_town) {
        this.p_city_town = p_city_town;
    }

    public String getP_landmark() {
        return p_landmark;
    }

    public void setP_landmark(String p_landmark) {
        this.p_landmark = p_landmark;
    }

    public String getP_state() {
        return p_state;
    }

    public void setP_state(String p_state) {
        this.p_state = p_state;
    }

    public String getP_pincode() {
        return p_pincode;
    }

    public void setP_pincode(String p_pincode) {
        this.p_pincode = p_pincode;
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

    public String getDriver_image_name() {
        return driver_image_name;
    }

    public void setDriver_image_name(String driver_image_name) {
        this.driver_image_name = driver_image_name;
    }

    public String getDriver_image_path() {
        return driver_image_path;
    }

    public void setDriver_image_path(String driver_image_path) {
        this.driver_image_path = driver_image_path;
    }

    public String getDriver_image_datettime() {
        return driver_image_datettime;
    }

    public void setDriver_image_datettime(String driver_image_datettime) {
        this.driver_image_datettime = driver_image_datettime;
    }

    public String getPancard_image_name() {
        return pancard_image_name;
    }

    public void setPancard_image_name(String pancard_image_name) {
        this.pancard_image_name = pancard_image_name;
    }

    public String getPancard_image_path() {
        return pancard_image_path;
    }

    public void setPancard_image_path(String pancard_image_path) {
        this.pancard_image_path = pancard_image_path;
    }

    public String getPancard_image_datettime() {
        return pancard_image_datettime;
    }

    public void setPancard_image_datettime(String pancard_image_datettime) {
        this.pancard_image_datettime = pancard_image_datettime;
    }

    public String getLicense_image_name() {
        return license_image_name;
    }

    public void setLicense_image_name(String license_image_name) {
        this.license_image_name = license_image_name;
    }

    public String getLicense_image_path() {
        return license_image_path;
    }

    public void setLicense_image_path(String license_image_path) {
        this.license_image_path = license_image_path;
    }

    public String getLicense_image_datetime() {
        return license_image_datetime;
    }

    public void setLicense_image_datetime(String license_image_datetime) {
        this.license_image_datetime = license_image_datetime;
    }

    public String getAadhar_image_name() {
        return aadhar_image_name;
    }

    public void setAadhar_image_name(String aadhar_image_name) {
        this.aadhar_image_name = aadhar_image_name;
    }

    public String getAadhar_image_path() {
        return aadhar_image_path;
    }

    public void setAadhar_image_path(String aadhar_image_path) {
        this.aadhar_image_path = aadhar_image_path;
    }

    public String getAadhar_image_datetime() {
        return aadhar_image_datetime;
    }

    public void setAadhar_image_datetime(String aadhar_image_datetime) {
        this.aadhar_image_datetime = aadhar_image_datetime;
    }

    public String getLast_modified_datetime() {
        return last_modified_datetime;
    }

    public void setLast_modified_datetime(String last_modified_datetime) {
        this.last_modified_datetime = last_modified_datetime;
    }

    public String getSent_to_server_datetime() {
        return sent_to_server_datetime;
    }

    public void setSent_to_server_datetime(String sent_to_server_datetime) {
        this.sent_to_server_datetime = sent_to_server_datetime;
    }

    public String getSent_to_server_flag() {
        return sent_to_server_flag;
    }

    public void setSent_to_server_flag(String sent_to_server_flag) {
        this.sent_to_server_flag = sent_to_server_flag;
    }

    public String getOther_company_name() {
        return other_company_name;
    }

    public void setOther_company_name(String other_company_name) {
        this.other_company_name = other_company_name;
    }

    public byte[] getDriver_image() {
        return driver_image;
    }

    public void setDriver_image(byte[] driver_image) {
        this.driver_image = driver_image;
    }

    public byte[] getAadhar_image() {
        return aadhar_image;
    }

    public void setAadhar_image(byte[] aadhar_image) {
        this.aadhar_image = aadhar_image;
    }

    public byte[] getPancard_image() {
        return pancard_image;
    }

    public void setPancard_image(byte[] pancard_image) {
        this.pancard_image = pancard_image;
    }

    public byte[] getLicense_image() {
        return license_image;
    }

    public void setLicense_image(byte[] license_image) {
        this.license_image = license_image;
    }

    public String getUniqueid() {
        return uniqueid;
    }

    public void setUniqueid(String uniqueid) {
        this.uniqueid = uniqueid;
    }

    public String getPayment_amount() {
        return payment_amount;
    }

    public void setPayment_amount(String payment_amount) {
        this.payment_amount = payment_amount;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public String getReceipt_no() {
        return receipt_no;
    }

    public void setReceipt_no(String receipt_no) {
        this.receipt_no = receipt_no;
    }

    public String getReceipt_date() {
        return receipt_date;
    }

    public void setReceipt_date(String receipt_date) {
        this.receipt_date = receipt_date;
    }

    public String getReferral_walk_in() {
        return referral_walk_in;
    }

    public void setReferral_walk_in(String referral_walk_in) {
        this.referral_walk_in = referral_walk_in;
    }

    public String getReferral_supervisor() {
        return referral_supervisor;
    }

    public void setReferral_supervisor(String referral_supervisor) {
        this.referral_supervisor = referral_supervisor;
    }

    public String getReferral_driver() {
        return referral_driver;
    }

    public void setReferral_driver(String referral_driver) {
        this.referral_driver = referral_driver;
    }

    public String getReceipt_no_2() {
        return receipt_no_2;
    }

    public void setReceipt_no_2(String receipt_no_2) {
        this.receipt_no_2 = receipt_no_2;
    }

    public String getReceipt_no_3() {
        return receipt_no_3;
    }

    public void setReceipt_no_3(String receipt_no_3) {
        this.receipt_no_3 = receipt_no_3;
    }

    public String getDriver_backout_reason() {
        return driver_backout_reason;
    }

    public void setDriver_backout_reason(String driver_backout_reason) {
        this.driver_backout_reason = driver_backout_reason;
    }

    public String getPrivate_verification() {
        return private_verification;
    }

    public void setPrivate_verification(String private_verification) {
        this.private_verification = private_verification;
    }

    public String getPolice_verification() {
        return police_verification;
    }

    public void setPolice_verification(String police_verification) {
        this.police_verification = police_verification;
    }

    public String getTr_license_exp_date() {
        return tr_license_exp_date;
    }

    public void setTr_license_exp_date(String tr_license_exp_date) {
        this.tr_license_exp_date = tr_license_exp_date;
    }

    public String getReceipt_date_2() {
        return receipt_date_2;
    }

    public void setReceipt_date_2(String receipt_date_2) {
        this.receipt_date_2 = receipt_date_2;
    }

    public String getReceipt_date_3() {
        return receipt_date_3;
    }

    public void setReceipt_date_3(String receipt_date_3) {
        this.receipt_date_3 = receipt_date_3;
    }
}
