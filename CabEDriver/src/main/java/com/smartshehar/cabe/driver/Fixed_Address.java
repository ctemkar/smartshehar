package com.smartshehar.cabe.driver;

/**
 * Created by soumen on 16-11-2016.
 * fixed address result
 */

public class Fixed_Address {

    private int fixed_id;
    private String fixed_address_id = "";
    private String area = "";
    private String landmark = "";
    private String pick_drop_point = "";
    private String formatted_address = "";
    private String locality = "";
    private String sublocality = "";
    private String postal_code = "";
    private String route = "";
    private String neighborhood = "";
    private String administrative_area_level_2 = "";
    private String administrative_area_level_1 = "";
    private double latitude, longitude;

    public void setFixed_id(int sfixed_id) {
        this.fixed_id = sfixed_id;
    }

    public int getFixed_id() {
        return fixed_id;
    }

    public void setFixed_address_id(String sfixed_address_id) {
        this.fixed_address_id = sfixed_address_id;
    }

    public String getFixed_address_id() {
        return fixed_address_id;
    }

    public void setArea(String sarea) {
        this.area = sarea;
    }

    public String getArea() {
        return area;
    }

    public void setLandmark(String slandmark) {
        this.landmark = slandmark;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setPick_Drop_Point(String spick_drop_point) {
        this.pick_drop_point = spick_drop_point;
    }

    public String getPick_Drop_Point() {
        return pick_drop_point;
    }

    public void setFormatted_Address(String sformatted_address) {
        this.formatted_address = sformatted_address;
    }

    public String getFormatted_Address() {
        return formatted_address;
    }

    public void setLocality(String slocality) {
        this.locality = slocality;
    }

    public String getLocality() {
        return locality;
    }

    public void setSublocality(String ssublocality) {
        this.sublocality = ssublocality;
    }

    public String getSublocality() {
        return sublocality;
    }

    public void setPostal_code(String spostal_code) {
        this.postal_code = spostal_code;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setRoute(String sroute) {
        this.route = sroute;
    }

    public String getRoute() {
        return route;
    }

    public void setNeighborhood(String sneighborhood) {
        this.neighborhood = sneighborhood;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setAdministrative_area_level_2(String sadministrative_area_level_2) {
        this.administrative_area_level_2 = sadministrative_area_level_2;
    }

    public String getAdministrative_area_level_2() {
        return administrative_area_level_2;
    }

    public void setAdministrative_area_level_1(String sadministrative_area_level_1) {
        this.administrative_area_level_1 = sadministrative_area_level_1;
    }

    public String getAdministrative_area_level_1() {
        return administrative_area_level_1;
    }

    public void setLatitude(double slatitude) {
        this.latitude = slatitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double slongitude) {
        this.longitude = slongitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
