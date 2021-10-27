package com.smartshehar.dashboard.app;

public class CIssue {

    // private variables
    int _id;
    int _foreingkey;
    long _unique_key, issueuniquekey;
    String _address;
    String _issue_time;
    String _issue_type;
    String _vehicle_no;
    String _video_uri;
    int _submitReport;
    int _sentToServer;
    int _approved;
    int _lettersubmitted;
    int _resolved_unresolved;
    int rejected;
    int _closed;
    int _open;
    byte[] _imageOne;
    String _imageName;
    String _creationDateTime;
    String _ba, _imagepath;
    String issue_lookup_item_code;
    String issue_itme_description;
    String issue_lookup_subtype_code;
    String complaint_subtype_description;
    String typecode;
    String subtypecode, itemcode;
    double lat;
    double lng;
    String wardno;
    String mla_id;
    String mp_id;
    String group_id;
    String group_name;
    String locality;
    String sublocality;
    String postal_code;
    String route;
    String neighborhood;
    String administrative_area_level_2;
    String administrative_area_level_1;
    int likedcount,unlikedcount;
    int liked;



    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getSublocality() {
        return sublocality;
    }

    public void setSublocality(String sublocality) {
        this.sublocality = sublocality;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getAdministrative_area_level_2() {
        return administrative_area_level_2;
    }

    public void setAdministrative_area_level_2(String administrative_area_level_2) {
        this.administrative_area_level_2 = administrative_area_level_2;
    }

    public String getAdministrative_area_level_1() {
        return administrative_area_level_1;
    }

    public void setAdministrative_area_level_1(String administrative_area_level_1) {
        this.administrative_area_level_1 = administrative_area_level_1;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getWardno() {
        return wardno;
    }

    public void setWardno(String wardno) {
        this.wardno = wardno;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getSubtypecode() {
        return subtypecode;
    }

    public void setSubtypecode(String subtypecode) {
        this.subtypecode = subtypecode;
    }

    public String getTypecode() {
        return typecode;
    }

    public void setTypecode(String typecode) {
        this.typecode = typecode;
    }

    public String getIssue_lookup_subtype_code() {
        return issue_lookup_subtype_code;
    }

    public void setIssue_lookup_subtype_code(String issue_lookup_subtype_code) {
        this.issue_lookup_subtype_code = issue_lookup_subtype_code;
    }

    public String getComplaint_subtype_description() {
        return complaint_subtype_description;
    }

    public void setComplaint_subtype_description(String complaint_subtype_description) {
        this.complaint_subtype_description = complaint_subtype_description;
    }

    public String getIssue_lookup_item_code() {
        return issue_lookup_item_code;
    }

    public void setIssue_lookup_item_code(String issue_lookup_item_code) {
        this.issue_lookup_item_code = issue_lookup_item_code;
    }

    public String getIssue_itme_description() {
        return issue_itme_description;
    }

    public void setIssue_itme_description(String issue_itme_description) {
        this.issue_itme_description = issue_itme_description;
    }

    public long getUniqueKey() {
        return _unique_key;
    }

    public void setUniqueKey(long unique_key) {
        this._unique_key = unique_key;
    }

    public String getAddress() {
        return _address;
    }

    public void setAddress(String _address) {
        this._address = _address;
    }

    public String getTimeOffence() {
        return _issue_time;
    }

    public void setTimeOffence(String _time_offence) {
        this._issue_time = _time_offence;
    }

    public String getTypeOffence() {
        return _issue_type;
    }

    public void setTypeOffence(String _type_offence) {
        this._issue_type = _type_offence;
    }

    public String getVehicle() {
        return _vehicle_no;
    }

    public void setVehicle(String _vehicle_no) {
        this._vehicle_no = _vehicle_no;
    }

    public String get_imagepath() {
        return _imagepath;
    }

    public void set_imagepath(String _imagepath) {
        this._imagepath = _imagepath;
    }

    public int get_lettersubmitted() {
        return _lettersubmitted;
    }

    public void set_lettersubmitted(int _lettersubmitted) {
        this._lettersubmitted = _lettersubmitted;
    }

    public int get_approved() {
        return _approved;
    }

    public void set_approved(int _approved) {
        this._approved = _approved;
    }

    public int get_resolved_unresolved() {
        return _resolved_unresolved;
    }

    public void set_resolved_unresolved(int _resolved_unresolved) {
        this._resolved_unresolved = _resolved_unresolved;
    }

    public int get_closed() {
        return _closed;
    }

    public void set_closed(int _closed) {
        this._closed = _closed;
    }

    // Empty constructor
    public CIssue() {

    }



    public CIssue(long unique_key, String address,
                  String timeoffence, String typeoffence, String vehicleno, int _submitReport,
                  String itemcode, double lat, double lng, String ward_no, String mla_id, String mp_id,
                  String group_id, String locality, String sublocality, String postal_code,
                  String route, String neighborhood, String administrative_area_level_2, String administrative_area_level_1) {

        this._unique_key = unique_key;
        this._address = address;
        this._issue_time = timeoffence;
        this._issue_type = typeoffence;
        this._vehicle_no = vehicleno;
        this._submitReport = _submitReport;
        this.lat = lat;
        this.lng = lng;
        this.wardno = ward_no;
        this.mla_id = mla_id;
        this.mp_id = mp_id;
        this.group_id = group_id;
        this.locality = locality;
        this.sublocality = sublocality;
        this.postal_code = postal_code;
        this.route = route;
        this.neighborhood = neighborhood;
        this.administrative_area_level_2 = administrative_area_level_2;
        this.administrative_area_level_1 = administrative_area_level_1;
        this.itemcode = itemcode;
    }

    public CIssue(long issueuniquekey, byte[] image_one, String image_name, String creation_date_time, String ba, String imagePath) {

        this._imageOne = image_one;
        this._imageName = image_name;
        this._creationDateTime = creation_date_time;
        this._ba = ba;
        this._imagepath = imagePath;
        this.issueuniquekey = issueuniquekey;
    }


    public long getIssueuniquekey() {
        return issueuniquekey;
    }

    public void setIssueuniquekey(long issueuniquekey) {
        this.issueuniquekey = issueuniquekey;
    }

    public String getItemcode() {
        return itemcode;
    }

    public void setItemcode(String itemcode) {
        this.itemcode = itemcode;
    }

    //getting ID
    public int getID() {
        return this._id;
    }

    //setting id
    public void setID(int keyId) {
        this._id = keyId;
    }


    // getting phone number
    public byte[] getImage() {
        return this._imageOne;
    }

    // setting phone number
    public void setImage(byte[] image) {
        this._imageOne = image;
    }

    public String getImageName() {
        return _imageName;
    }

    public void setImageName(String _imageName) {
        this._imageName = _imageName;
    }

    public int get_foreingkey() {
        return _foreingkey;
    }

    public String get_video_uri() {
        return _video_uri;
    }

    public void set_video_uri(String _video_uri) {
        this._video_uri = _video_uri;
    }

    public String get_creationDateTime() {
        return _creationDateTime;
    }

    public void set_creationDateTime(String _creationDateTime) {
        this._creationDateTime = _creationDateTime;
    }

    public int get_submitReport() {
        return _submitReport;
    }

    public void set_submitReport(int _submitReport) {
        this._submitReport = _submitReport;
    }

    public String get_ba() {
        return _ba;
    }

    public void set_ba(String _ba) {
        this._ba = _ba;
    }

    public int get_sentToServer() {
        return _sentToServer;
    }

    public void set_sentToServer(int _sentToServer) {
        this._sentToServer = _sentToServer;
    }

    public String getMla_id() {
        return mla_id;
    }

    public void setMla_id(String mla_id) {
        this.mla_id = mla_id;
    }

    public String getMp_id() {
        return mp_id;
    }

    public void setMp_id(String mp_id) {
        this.mp_id = mp_id;
    }

    public int get_open() {
        return _open;
    }

    public void set_open(int _open) {
        this._open = _open;
    }
    public int getRejected() {
        return rejected;
    }

    public void setRejected(int rejected) {
        this.rejected = rejected;
    }

    public int getLikedcount() {
        return likedcount;
    }

    public void setLikedcount(int likedcount) {
        this.likedcount = likedcount;
    }

    public int getUnlikedcount() {
        return unlikedcount;
    }

    public void setUnlikedcount(int unlikedcount) {
        this.unlikedcount = unlikedcount;
    }

    public int getLiked() {
        return liked;
    }

    public void setLiked(int liked) {
        this.liked = liked;
    }
}