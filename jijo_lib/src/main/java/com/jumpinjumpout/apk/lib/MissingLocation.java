package com.jumpinjumpout.apk.lib;

/**
 * Created by user pc on 07-10-2015.
 */
public class MissingLocation {

    double lat;
    double lng;
    int sent_to_server;
    float accuracy;
    double altitude;
    float bearing;
    float speed;
    String locationdatetime = "";
    String provider = "";

    public MissingLocation() {
    }


    public MissingLocation(double dlat, double dlng, float faccuracy, double daltitude, float fbearing, float fspeed, String slocationdatetime, String sprovider, int isent_to_server) {
        this.lat = dlat;
        this.lng = dlng;
        this.sent_to_server = isent_to_server;
        this.accuracy = faccuracy;
        this.altitude = daltitude;
        this.bearing = fbearing;
        this.speed = fspeed;
        this.locationdatetime = slocationdatetime;
        this.provider = sprovider;
    }

    public void setTrip_lat(double dTrip_lat) {
        this.lat = dTrip_lat;
    }

    public double getTrip_lat() {
        return lat;
    }

    public void setTrip_lng(double dTrip_lng) {
        this.lng = dTrip_lng;
    }

    public double getTrip_lng() {
        return lng;
    }

    public int getSent_to_server() {
        return sent_to_server;
    }

    public void setAccuracy(float iaccuracy) {
        this.accuracy = iaccuracy;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAltitude(double ialtitude) {
        this.altitude = ialtitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setBearing(float ibearing) {
        this.bearing = ibearing;
    }

    public float getBearing() {
        return bearing;
    }

    public void setSpeed(float ispeed) {
        this.speed = ispeed;
    }

    public float getSpeed() {
        return speed;
    }

    public void setLoctime(String iloctime) {
        this.locationdatetime = iloctime;
    }

    public String getLoctime() {
        return locationdatetime;
    }

    public void setSProvider(String iprovider) {
        this.provider = iprovider;
    }

    public String getSProvider() {
        return provider;
    }

}
