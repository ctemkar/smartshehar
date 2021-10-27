package com.jumpinjumpout.apk.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CTrip {

    private static String TAG = "CTrip: ";
    public String msHtmlDescription = "", msPhoneno, msVehicleColor, msVehicleCompany,
            msVehicleType, msVehicleNo, msFrom, msFromShort, msTo, msToShort, msToSubLocality,
            msTripStatus, msDrivername, msName, msDriverEmail, msCountryCode,
            msUserName, msFullName, msVehicleCategory, msVehicleModel, msTripCreationTime,
            msTripFare, msCompanyName, msPremium_Seats, msWindow_Seats, msStandard_Seats,
            msPremium_Seat_Fare, msWindow_Seat_Fare, msStandard_Seat_Fare;
    private String msMarkerText = "";
    private String msPlannedStartTime, msPlannedStartDateTime;
    private boolean mbHasDriverMoved;
    // for cabs
    private String msCarDesc, msStartLandmark, msTripNote;
    LatLng moDriverLatLng = null;
    boolean mIsFriend = true;
    JSONObject mjTrip;
    private String msJDriver;
    private int miInTrip;
    private int miInPathFrom, miInPathTo, miCommercial_Vehicle_Id;
    private int miIsTracking, miIsTrackingNotify;
    private String msTripAction, msTripType;
    private int miResourceIcon, miSeating;
    private int hasJoined, hasJumpedIn, hasJumpedOut, hasCanceledJoin;
    private String sLastActive = "";
    private Bitmap mbmContactThnumbnail;
    private int miRate, miEmptySeats;
    private boolean mIsCommercial;
    private String msHtmlName, msName1;
    private int miAppUserId, miDriverId;
    private int miTripId;
    private ContactInfo mCcontactinfo;
    long lTimeDiff, lTimeDifflocation;
    private String miUserProfileImageFileName, miUserProfileImagePath;
    private String msAge, msSex;
    private int miImage_Rotation = 0;
    private String msTripPath;
    private double fromLat, fromLng, toLat, toLng;
    private String msTripStatusTrack;
    private int miTripDistance, miPassed;
    private String msTripActionTime;
    boolean itsMe = false;
    private String myAddress;
    private String msFromAddressShort;
    private String msToAddressShort;
    private boolean isTripActive;
    private String msCar;
    private String msTripMovedDriverTime;
    private int miCabsinPlay;
    private double slastlng, slastlat;
    private double mdDriverLng, mdDriverLat;
    private boolean isFavourite;

    int key_id;
    String start_address = "";
    String startlat = "";
    String startlng = "";
    String destination_address = "";
    String destlat = "";
    String destlng = "";
    String time = "";
    Date lastRunDate;
    int sun = 0;
    int mon = 0;
    int twe = 0;
    int wes = 0;
    int thu = 0;
    int fri = 0;
    int sat = 0;
    int one_Flag = 0;

    public CTrip() {
    }

    public CTrip(String sFrom, String sTo) {
        msFrom = sFrom;
        msTo = sTo;
    }

    public CTrip(String sFrom, String sTo, double dfromLat, double dfromLng, double dtoLat, double dtoLng) {
        msFrom = sFrom;
        msTo = sTo;
        fromLat = dfromLat;
        fromLng = dfromLng;
        toLat = dtoLat;
        toLng = dtoLng;
    }

    public CTrip(JSONObject oTrip) {

        try {
            msFrom = oTrip.getString("fromAddress");
            msTo = oTrip.getString("toAddress");
            fromLat = oTrip.getDouble("fromLAT");
            fromLng = oTrip.getDouble("fromLNG");
            toLat = oTrip.getDouble("toLAT");
            toLng = oTrip.getDouble("toLNG");
        } catch (Exception e) {

            SSLog.e(TAG, "CTrip", e);
        }
    }

    public CTrip(String sjDriver, Context context) {
        CGlobals_lib.getInstance();
        init(sjDriver, context);

    } // CTrip

    public void init(String sjDriver, Context context) {
        msCar = "";
        msJDriver = sjDriver;
        if (!TextUtils.isEmpty(sjDriver)) {
            try {
                mjTrip = new JSONObject(sjDriver);
                if (mjTrip == null) {
                    return;
                }
                msCountryCode = isNullNotDefined(mjTrip, "country_code") ? ""
                        : mjTrip.getString("country_code");
                msPhoneno = isNullNotDefined(mjTrip, "phoneno") ? "" : mjTrip
                        .getString("phoneno") + " ";

                msVehicleType = isNullNotDefined(mjTrip, "vehicle_type") ? ""
                        : mjTrip.getString("vehicle_type");

                msTripFare = isNullNotDefined(mjTrip, "trip_fare") ? ""
                        : mjTrip.getString("trip_fare");

                msVehicleNo = isNullNotDefined(mjTrip, "vehicleno") ? ""
                        : mjTrip.getString("vehicleno");
                msVehicleColor = isNullNotDefined(mjTrip, "vehicle_color") ? ""
                        : mjTrip.getString("vehicle_color");
                msVehicleCompany = isNullNotDefined(mjTrip, "vehicle_company") ? ""
                        : mjTrip.getString("vehicle_company");

                msCompanyName = isNullNotDefined(mjTrip, "company_name") ? ""
                        : mjTrip.getString("company_name");

                msVehicleModel = isNullNotDefined(mjTrip, "vehicle_model") ? ""
                        : mjTrip.getString("vehicle_model");
                msDrivername = isNullNotDefined(mjTrip, "drivername") ? ""
                        : mjTrip.getString("drivername");

                msUserName = isNullNotDefined(mjTrip, "username") ? "" : mjTrip
                        .getString("username");

                miAppUserId = isNullNotDefined(mjTrip, "appuser_id") ? -1 : mjTrip
                        .getInt("appuser_id");

                miDriverId = isNullNotDefined(mjTrip, "driver_id") ? -1 : mjTrip
                        .getInt("driver_id");

                miCommercial_Vehicle_Id = isNullNotDefined(mjTrip, "commercial_vehicle_id") ? -1 : mjTrip
                        .getInt("commercial_vehicle_id");

                miPassed = isNullNotDefined(mjTrip, "passed") ? -1 : mjTrip
                        .getInt("passed");

                miTripId = isNullNotDefined(mjTrip, "trip_id") ? -1 : mjTrip
                        .getInt("trip_id");
                msFullName = isNullNotDefined(mjTrip, "fullname") ? "" : mjTrip
                        .getString("fullname");
                msAge = isNullNotDefined(mjTrip, "age") ? "" : mjTrip
                        .getString("age");
                msSex = isNullNotDefined(mjTrip, "sex") ? "" : mjTrip
                        .getString("sex");

                miImage_Rotation = isNullNotDefined(mjTrip, "image_rotation") ? 0 : mjTrip
                        .getInt("image_rotation");

                miCabsinPlay = isNullNotDefined(mjTrip, "cabsinplay") ? 0 : mjTrip
                        .getInt("cabsinplay");

                msTripPath = isNullNotDefined(mjTrip, "trip_directions_polyline") ? ""
                        : mjTrip.getString("trip_directions_polyline");

                msVehicleCategory = isNullNotDefined(mjTrip, "vehicle_category") ? ""
                        : mjTrip.getString("vehicle_category");

                msDriverEmail = isNullNotDefined(mjTrip, "email") ? "" : mjTrip
                        .getString("email");
                if (!isNullNotDefined(mjTrip, "lat")
                        && !isNullNotDefined(mjTrip, "lng")) {
                    moDriverLatLng = new LatLng(mjTrip.getDouble("lat"),
                            mjTrip.getDouble("lng"));
                }
                msCarDesc = isNullNotDefined(mjTrip, "car_desc") ? "" : mjTrip
                        .getString("car_desc");
                msStartLandmark = isNullNotDefined(mjTrip, "start_landmark") ? ""
                        : mjTrip.getString("start_landmark");
                msPlannedStartTime = isNullNotDefined(mjTrip,
                        "planned_start_time") ? "" : mjTrip
                        .getString("planned_start_time");

                msPlannedStartDateTime = isNullNotDefined(mjTrip,
                        "plannedstartdatetime") ? "" : mjTrip
                        .getString("plannedstartdatetime");

                try {
                    mbHasDriverMoved = isNullNotDefined(mjTrip,
                            "hasdriver_moved") ? false : (mjTrip
                            .getInt("hasdriver_moved") == 1 ? true : false);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    mbHasDriverMoved = false;
                }

                /*try {
                    isFavourite = isNullNotDefined(mjTrip,
                            "hasdriver_moved") ? false : (mjTrip
                            .getInt("hasdriver_moved") == 1 ? true : false);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    isFavourite = false;
                }*/

                msTripNote = isNullNotDefined(mjTrip, "trip_note") ? ""
                        : mjTrip.getString("trip_note");
                msTripCreationTime = isNullNotDefined(mjTrip, "tripcreationtime") ? "" : mjTrip
                        .getString("tripcreationtime");

                msFrom = isNullNotDefined(mjTrip, "fromaddress") ? "" : mjTrip
                        .getString("fromaddress");

                fromLat = isNullNotDefined(mjTrip, "fromlat") ? Constants_lib.INVALIDLAT : mjTrip
                        .getDouble("fromlat");
                fromLng = isNullNotDefined(mjTrip, "fromlng") ? Constants_lib.INVALIDLAT : mjTrip
                        .getDouble("fromlng");

                msFromShort = isNullNotDefined(mjTrip, "fromshortaddress") ? ""
                        : mjTrip.getString("fromshortaddress");
                if (TextUtils.isEmpty(msFromShort)) {
                    msFromShort = msFrom;
                }
                msTo = isNullNotDefined(mjTrip, "toaddress") ? "" : mjTrip
                        .getString("toaddress");

                toLat = isNullNotDefined(mjTrip, "tolat") ? Constants_lib.INVALIDLAT : mjTrip
                        .getDouble("tolat");
                toLng = isNullNotDefined(mjTrip, "tolng") ? Constants_lib.INVALIDLAT : mjTrip
                        .getDouble("tolng");

                slastlat = isNullNotDefined(mjTrip, "lastlat") ? Constants_lib.INVALIDLAT : mjTrip
                        .getDouble("lastlat");
                slastlng = isNullNotDefined(mjTrip, "lastlng") ? Constants_lib.INVALIDLAT : mjTrip
                        .getDouble("lastlng");

                mdDriverLat = isNullNotDefined(mjTrip, "driver_lat") ? Constants_lib.INVALIDLAT : mjTrip
                        .getDouble("driver_lat");
                mdDriverLng = isNullNotDefined(mjTrip, "driver_lng") ? Constants_lib.INVALIDLAT : mjTrip
                        .getDouble("driver_lng");

                msToShort = isNullNotDefined(mjTrip, "toshortaddress") ? ""
                        : mjTrip.getString("toshortaddress");

                msToSubLocality = isNullNotDefined(mjTrip, "tosublocality") ? ""
                        : mjTrip.getString("tosublocality");

                msTripType = isNullNotDefined(mjTrip, "triptype") ? "" : mjTrip
                        .getString("triptype");
                msTripAction = isNullNotDefined(mjTrip, "trip_action") ? ""
                        : mjTrip.getString("trip_action");

                miUserProfileImageFileName = isNullNotDefined(mjTrip, "userprofileimagefilename") ? "" : mjTrip
                        .getString("userprofileimagefilename");

                miUserProfileImagePath = isNullNotDefined(mjTrip, "userprofileimagepath") ? "" : mjTrip
                        .getString("userprofileimagepath");

                msTripStatusTrack = isNullNotDefined(mjTrip, "trip_action") ? "N" : mjTrip
                        .getString("trip_action");

                miTripDistance = isNullNotDefined(mjTrip, "tripdistance") ? -1 : mjTrip
                        .getInt("tripdistance");

                msTripActionTime = isNullNotDefined(mjTrip, "trip_action_time") ? "" : mjTrip
                        .getString("trip_action_time");

                msTripMovedDriverTime = isNullNotDefined(mjTrip, "hasdriver_moved_time") ? "" : mjTrip
                        .getString("hasdriver_moved_time");

                msTripStatus = "";
                miInTrip = isNullNotDefined(mjTrip, "intrip") ? 0 : mjTrip
                        .getInt("intrip");
                miInPathFrom = isNullNotDefined(mjTrip, "inpathfrom") ? 0
                        : mjTrip.getInt("inpathfrom");
                miInPathTo = isNullNotDefined(mjTrip, "inpathto") ? 0 : mjTrip
                        .getInt("inpathto");
                mIsCommercial = isNullNotDefined(mjTrip, "is_commercial") ? false
                        : (mjTrip.getInt("is_commercial") == 1 ? true : false);
                hasJoined = mjTrip.isNull(Constants_lib.COL_HAS_JOINED) ? 0
                        : Integer.parseInt(mjTrip
                        .getString(Constants_lib.COL_HAS_JOINED));
                hasJumpedIn = mjTrip
                        .isNull(Constants_lib.COL_HAS_JUMPED_IN) ? 0
                        : Integer
                        .parseInt(mjTrip
                                .getString(Constants_lib.COL_HAS_JUMPED_IN));
                hasJumpedOut = mjTrip
                        .isNull(Constants_lib.COL_HAS_JUMPED_OUT) ? 0
                        : Integer
                        .parseInt(mjTrip
                                .getString(Constants_lib.COL_HAS_JUMPED_OUT));

                hasCanceledJoin = mjTrip.isNull(Constants_lib.COL_CANCEL_JOIN) ? 0
                        : Integer.parseInt(mjTrip
                        .getString(Constants_lib.COL_CANCEL_JOIN));

                miRate = isNullNotDefined(mjTrip, "rate") ? -1 : mjTrip
                        .getInt("rate");
                miEmptySeats = isNullNotDefined(mjTrip, "empty_seats") ? -1
                        : mjTrip.getInt("empty_seats");

                String sLastAccess = isNullNotDefined(mjTrip, "lastaccess") ? ""
                        : mjTrip.getString("lastaccess");
                String sLocTime = isNullNotDefined(mjTrip, "loctime") ? ""
                        : mjTrip.getString("loctime");

               /* msCity_Town_Id = isNullNotDefined(mjTrip, "city_town_id") ? ""
                        : mjTrip.getString("city_town_id");

                msCity_Town = isNullNotDefined(mjTrip, "city_town") ? ""
                        : mjTrip.getString("city_town");

                msCity_Town_Mar = isNullNotDefined(mjTrip, "city_town_mar") ? ""
                        : mjTrip.getString("city_town_mar");

                msCityorTown = isNullNotDefined(mjTrip, "cityortown") ? ""
                        : mjTrip.getString("cityortown");

                mdCityTown_Mar_lat = isNullNotDefined(mjTrip, "lat") ? Constants_lib.INVALIDLAT : mjTrip
                        .getDouble("lat");

                mdCityTown_Mar_lng = isNullNotDefined(mjTrip, "lng") ? Constants_lib.INVALIDLAT : mjTrip
                        .getDouble("lng");*/

                msPremium_Seats = isNullNotDefined(mjTrip, "premium_seats") ? ""
                        : mjTrip.getString("premium_seats");
                msWindow_Seats = isNullNotDefined(mjTrip, "window_seats") ? ""
                        : mjTrip.getString("window_seats");
                msStandard_Seats = isNullNotDefined(mjTrip, "standard_seats") ? ""
                        : mjTrip.getString("standard_seats");
                msPremium_Seat_Fare = isNullNotDefined(mjTrip, "premium_seat_fare") ? ""
                        : mjTrip.getString("premium_seat_fare");
                msWindow_Seat_Fare = isNullNotDefined(mjTrip, "window_seat_fare") ? ""
                        : mjTrip.getString("window_seat_fare");
                msStandard_Seat_Fare = isNullNotDefined(mjTrip, "standard_seat_fare") ? ""
                        : mjTrip.getString("standard_seat_fare");

                miSeating = isNullNotDefined(mjTrip, "seating") ? -1 : mjTrip
                        .getInt("seating");

                SimpleDateFormat df = new SimpleDateFormat(
                        Constants_lib.STANDARD_DATE_FORMAT, Locale.getDefault());
                isTripActive = false;
                if (!TextUtils.isEmpty(sLastAccess)) {
                    Date datetimeLastAccess = new Date();
                    Calendar cal = Calendar.getInstance();
                    try {
                        datetimeLastAccess = df.parse(sLastAccess);
                        lTimeDiff = (cal.getTimeInMillis() - datetimeLastAccess
                                .getTime()) / 1000 / 60;
                        if (lTimeDiff < 1) {
                            sLastActive = "Online now";
                        } else {
                            sLastActive = "Last online " + lTimeDiff
                                    + " min. back";
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
                if (!TextUtils.isEmpty(sLocTime)) {
                    Date datetimeLastLocation = new Date();
                    Calendar cal = Calendar.getInstance();
                    try {
                        datetimeLastLocation = df
                                .parse(sLocTime);
                        lTimeDifflocation = (cal
                                .getTimeInMillis() - datetimeLastLocation
                                .getTime()) / 1000;
                        if (lTimeDifflocation < 30) {
                            sLastActive = "Location available";
                        } else {
                            sLastActive = "Last location recd. "
                                    + lTimeDifflocation
                                    / 60
                                    + " min. ago";
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
                if (TextUtils.isEmpty(msTripAction)) {
                    msTripStatus = "No trip created as yet. ";
                } else if (msTripAction
                        .equals(Constants_lib.TRIP_ACTION_CREATE)) {
                    msTripStatus = "Trip not started. ";
                } else if ((Constants_lib.TRIP_ACTION_BEGIN
                        + Constants_lib.TRIP_ACTION_PAUSE + Constants_lib.TRIP_ACTION_RESUME)
                        .contains(msTripAction)) {
                    if (mbHasDriverMoved) {
                        msTripStatus = "Trip in progress. ";
                    } else {
                        msTripStatus = "Starts at: " + msPlannedStartTime;
                        if (!TextUtils.isEmpty(msStartLandmark)) {
                            msTripStatus += " from " + msStartLandmark;
                        }
                    }
                    miInTrip = 1;
                    isTripActive = true;
                } else if ((Constants_lib.TRIP_ACTION_ABORT + Constants_lib.TRIP_ACTION_END)
                        .contains(msTripAction)) {
                    msTripStatus = "Trip just ended. ";
                    miInTrip = 0;
                    isTripActive = false;
                } else if ((Constants_lib.TRIP_ACTION_SCHEDULE)
                        .contains(msTripAction)) {
                    isTripActive = false;
                } else {
                    msTripStatus = "Unknown status. (" + msTripStatus + ")";
                }

                miIsTracking = isNullNotDefined(mjTrip, "istracking") ? 0
                        : mjTrip.getInt("istracking");
                setMiIsTrackingNotify(isNullNotDefined(mjTrip,
                        "istrackingnotify") ? 0 : mjTrip
                        .getInt("istrackingnotify"));
                if (!TextUtils.isEmpty(msCarDesc)) {
                    msCar = msCarDesc;
                } else {
                    msCar = msVehicleColor + msVehicleCompany + msVehicleType
                            + msVehicleNo;
                }
                mCcontactinfo = CGlobals_lib.getContactInfo(
                        context, msCountryCode, msPhoneno, msDriverEmail);
                if (mCcontactinfo != null) {
                    msName = mCcontactinfo.getContactName();
                    if (CGlobals_lib.getInstance().getSharedPreferences(context).getString(Constants_lib.PREF_PHONENO, "").equals(msPhoneno)) {
                        itsMe = true;
                    }
                    if (itsMe) {
                        msName = "Me";
                    } else if (TextUtils.isEmpty(mCcontactinfo.getContactName())) {
                        mIsFriend = false;
                        msName = msUserName;
                    }
                }

                if (CGlobals_lib.getInstance().getSharedPreferences(context).getString(Constants_lib.PREF_PHONENO, "").equals(msPhoneno.trim())) {
                    itsMe = true;
                }
                if (itsMe) {
                    msName = "Me";
                }
                if (TextUtils.isEmpty(msName)) {
                    msName = msDrivername;
                }

                if (TextUtils.isEmpty(msName)) {
                    msName = msPhoneno;
                }

                msName1 = msName;
                msHtmlName = "<b>" + msName + "</b>";
                if (!TextUtils.isEmpty(msFromShort)) {
                    msFromShort = "From " + msFromShort;
                } else {
                    msFromShort = "From " + msFrom;
                }
                miResourceIcon = -1;

            } catch (Exception e) {
                SSLog.e(TAG, "CTrip constructor - ", e);
            }

        }
    }

    public JSONObject toJSon() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("fromAddress", msFrom);
            obj.put("toAddress", msTo);
            obj.put("fromLAT", fromLat);
            obj.put("fromLNG", fromLng);
            obj.put("toLAT", toLat);
            obj.put("toLNG", toLng);
        } catch (Exception e) {
            SSLog.e(TAG, "toJSon", e);
        }
        return obj;

    }

    public boolean sameAs(CTrip oTrip) {

        if (oTrip.msFrom.equals(this.msFrom) && oTrip.msTo.equals(this.msTo)) {
            return true;
        }

        return false;

    }

    public String getDescription() {
        return msHtmlDescription;
    }

    public String getFrom() {
        return msFrom;
    }

    public String getVehicleColor() {
        return msVehicleColor;
    }

    public String getTripCreationTime() {
        return msTripCreationTime;
    }

    public String getCompanyName() {
        return msCompanyName;
    }

    public String getVehicleCompany() {
        return msVehicleCompany;
    }

    public String getVehicleModel() {
        return msVehicleModel;
    }

    public String getVehicleNo() {
        return msVehicleNo;
    }

    public int getCommercial_Vehicle_Id() {
        return miCommercial_Vehicle_Id;
    }

    public String getVehicleType() {
        return msVehicleType;
    }

    public String getTripFare() {
        return msTripFare;
    }

    public String getPlannedStartDateTime() {
        return msPlannedStartDateTime;
    }

    public String getPlannedStartTime() {
        /*Date dt = null;
        String stime = "";
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
        try {
            dt = sdf.parse(msPlannedStartTime);
            stime = sdfs.format(dt);

        } catch (Exception e) {
            e.printStackTrace();
        }*/
        String stime = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date dateObj = sdf.parse(msPlannedStartTime);
            stime = new SimpleDateFormat("HH:mm a").format(dateObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "<b>" + stime + "</b>";
    }

    public String getPhoneNo() {
        return msPhoneno;
    }

    public String getDriverEmail() {
        return msDriverEmail;
    }

    public String getCountryCode() {
        return msCountryCode;
    }

    public String getTo() {
        return msTo;
    }

    public int getAppUserId() {
        return miAppUserId;
    }

    public int getTripId() {
        return miTripId;
    }

    public String getInfo() {
        return msJDriver;
    }

    public int getInPath() {
        return miInPathFrom;
    }

    public int getTripStatusResource() {
        return miResourceIcon;
    }

    public String getLastActive() {
        return sLastActive;
    }

    public boolean getIsFriend() {
        return mIsFriend;
    }

    public LatLng getLatLng() {
        return moDriverLatLng;
    }

    public String getCarDesc() {
        return msCarDesc;
    }

    public String getStartLandmark() {
        return "";
    }

    public void setStartLandmark(String msStartLandmark) {
        this.msStartLandmark = msStartLandmark;
    }

    public String getTripNotes() {
        return msTripNote;
    }

    public boolean isCommercial() {
        return this.mIsCommercial;
    }

    public String getHtmlName() {
        return msHtmlName;
    }

    public String getName() {
        return msName1;
    }

    public String getMarkerText() {
        return msMarkerText;
    }

    public int getRate() {
        return miRate;
    }

    public long getlTimeDiff() {
        return lTimeDiff;
    }

    public long getlTimeDifflocation() {
        return lTimeDifflocation;
    }

    public String isTripCommercial() {
        return msTripType;
    }

    public boolean hasJumpedIn() {
        return hasJumpedIn > 0 ? true : false;
    }

    public boolean hasJumpedOut() {
        return hasJumpedOut > 0 ? true : false;
    }

    public void setMiIsTrackingNotify(int miIsTrackingNotify) {
        this.miIsTrackingNotify = miIsTrackingNotify;
    }

    public String getUserProfileImageFileName() {
        return miUserProfileImageFileName.trim();
    }

    public String getUserProfileImagePath() {
        return miUserProfileImagePath.trim();
    }

    public boolean isNullNotDefined(JSONObject jo, String jkey) {

        if (!jo.has(jkey)) {
            return true;
        }
        if (jo.isNull(jkey)) {
            return true;
        }
        return false;

    }


    public boolean hasJoined() {
        if (hasJoined > 0) {
            return true;
        }
        return false;
    } // CTrip

    public Bitmap getContactThnumbnail() {
        if (mCcontactinfo == null) {
            return null;
        }
        return mCcontactinfo.getContactThumbnail();
    }

    public String getMsUserUserName() {
        return msUserName;
    }

    public String getMsFullName() {
        return msFullName;
    }

    public String getVehicle_Category() {
        return msVehicleCategory;
    }

    public int getMiImageRotation() {
        return miImage_Rotation;
    }

    public boolean getHasDriveMoved() {
        return mbHasDriverMoved;
    }

    public String getMsTripPath() {
        return msTripPath;
    }

    public String getMsTripMovedDriverTime() {
        String sTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        try {
            if (!TextUtils.isEmpty(msTripMovedDriverTime)) {
                cal.setTime(sdf.parse(msTripMovedDriverTime));
                SimpleDateFormat rdf = new SimpleDateFormat("HH:mm");
                sTime = rdf.format(cal.getTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return sTime;
    }

    public double getFromLat() {
        return fromLat;
    }

    public double getFromLng() {
        return fromLng;
    }

    public double getToLat() {
        return toLat;
    }

    public double getToLng() {
        return toLng;
    }

    public int getCabInPlay() {
        return miCabsinPlay;
    }

    public String getMsTripStatusTrack() {
        return msTripStatusTrack;
    }

    public String getToSubLocality() {
        return msToSubLocality;
    }

    public int getMiTripDistance() {
        return miTripDistance;
    }

    public int getMiPassed() {
        return miPassed;
    }

    public String getMsTripActionTime1() {
        return msTripActionTime;
    }

    public String getMsTripActionTime() {
        String sTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        try {
            if (!TextUtils.isEmpty(msTripActionTime)) {
                cal.setTime(sdf.parse(msTripActionTime));
                SimpleDateFormat rdf = new SimpleDateFormat("HH:mm");
                sTime = rdf.format(cal.getTime());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return sTime;
    }

    public void setDistance(int distance) {
        this.miTripDistance = distance;
    }

    public void setDuration(String duration) {
        this.msTripActionTime = duration;
    }

    public String shortenedAddress(String myAddress, String sAddress) {

        String toTokens[] = new StringBuilder(myAddress.trim()).reverse().toString().split(",");
        String fromTokens[] = new StringBuilder(sAddress.trim()).reverse().toString().split(",");

        String sToken = "", sMatchingToken = "";
        int nSameToken = 0;
        StringBuilder sbLastToken = new StringBuilder("");

        for (int i = 0; i < toTokens.length; i++) {
            sToken = toTokens[i];
            if (i < fromTokens.length) {
                if (sToken.equals(fromTokens[i])) {
                    nSameToken++;
                    sMatchingToken = sToken;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        String sShortAddress = sAddress.trim();
        try {
            if (nSameToken > 0) {
                Log.i(TAG, "Commont Tokens: " + nSameToken);
                String sLastMatchingToken = new StringBuilder(sMatchingToken).reverse().toString();
                int iPos = sShortAddress.indexOf(sLastMatchingToken);
                sShortAddress = sShortAddress.substring(0, iPos);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sShortAddress.length() < 25) {
            int len = sAddress.length() > 25 ? 25 : sAddress.length();
            sShortAddress = sAddress.substring(0, len) + " ...";
        }
        return sShortAddress;
    }

    public void setMyAddress(String myAddress) {
        this.myAddress = myAddress;
        this.msFromAddressShort = shortenedAddress(myAddress, msFrom);
        this.msToAddressShort = shortenedAddress(myAddress, msTo);
        createDescription();
    }

    private String getToShortAddress() {
        if (!TextUtils.isEmpty(msToAddressShort)) {
            return msToAddressShort;
        } else if (!TextUtils.isEmpty(msToShort)) {
            return msToShort;
        } else {
            return msTo;
        }

    }

    private void createDescription() {
        if (msTripType.equals(Constants_lib.TRIP_TYPE_COMMERCIAL)) {
            msHtmlDescription = msCar;
            msHtmlDescription += " " + msDrivername;
            if (!isTripActive) {
                msHtmlDescription += " is For Hire";
            }
            if (miRate != -1) {
                msHtmlDescription += " Rs. " + miRate + " per km.";
            }
            msHtmlDescription = getToShortAddress();
        } else {
            msHtmlDescription = msHtmlName + " going to<br>" + getToShortAddress();
        }
    }

    public void setId(int id) {
        this.key_id = id;
    }

    public int getId() {
        return key_id;
    }

    public void setLastRunDate(Date d) {
        lastRunDate = d;
    }

    public Date getLastRunDate() {
        return lastRunDate;
    }

    public void setstart_address(String sstart_address) {
        this.start_address = sstart_address;
    }

    public String getstart_address() {
        return start_address;
    }

    public void setstart_Lat(String sstartlat) {
        this.startlat = sstartlat;
    }

    public String getstart_Lat() {
        return startlat;
    }

    public void setstart_Lng(String sstartlng) {
        this.startlng = sstartlng;
    }

    public String getstart_Lng() {
        return startlng;
    }

    public void setdestination_address(String sdestination_address) {
        this.destination_address = sdestination_address;
    }

    public String getdestination_address() {
        return destination_address;
    }

    public void setdestination_Lat(String sdestlat) {
        this.destlat = sdestlat;
    }

    public String getdestination_Lat() {
        return destlat;
    }


    public void setdestination_Lng(String sdestlng) {
        this.destlng = sdestlng;
    }

    public String getdestination_Lng() {
        return destlng;
    }

    public void setStime(String stime) {
        this.time = stime;
    }

    public String getSTime1() {
        return time;
    }

    public String getStime() {
        Date dt = null;
        String sTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
        try {
            dt = sdf.parse(time);
            sTime = sdfs.format(dt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sTime;
    }

    public void setsun(int isun) {
        this.sun = isun;
    }

    public boolean getsun() {
        return sun == 1;
    }

    public void setmon(int imon) {
        this.mon = imon;
    }

    public boolean getmon() {
        return mon == 1;
    }

    public void settwe(int itwe) {
        this.twe = itwe;
    }

    public boolean gettue() {
        return twe == 1;
    }

    public void setwes(int iwes) {
        this.wes = iwes;
    }

    public boolean getwed() {
        return wes == 1;
    }

    public void setthu(int ithu) {
        this.thu = ithu;
    }

    public boolean getthu() {
        return thu == 1;
    }

    public void setfri(int ifri) {
        this.fri = ifri;
    }

    public boolean getfri() {
        return fri == 1;
    }

    public void setsat(int isat) {
        this.sat = isat;
    }

    public boolean getsat() {
        return sat == 1;
    }

    public void setOneFlag(int ione_Flag) {
        this.one_Flag = ione_Flag;
    }

    public int getOneFlag() {
        return one_Flag;
    }

    public boolean isScheduleActiveToday(int day) {
        if (getsun() && day == 1)
            return true;
        if (getmon() && day == 2)
            return true;
        if (gettue() && day == 3)
            return true;
        if (getwed() && day == 4)
            return true;
        if (getthu() && day == 5)
            return true;
        if (getfri() && day == 6)
            return true;
        if (getsat() && day == 7)
            return true;
        return false;
    }

    public double getLAT() {
        return mdDriverLat;
    }

    public double getLNG() {
        return mdDriverLng;
    }

    public void setFavourite(boolean bFavourite) {
        this.isFavourite = bFavourite;
    }

    public boolean getFavourite() {
        return isFavourite;
    }

    public int getSeating() {
        return miSeating;
    }

    public String getPremium_Seats() {
        return msPremium_Seats;
    }

    public String getWindow_Seats() {
        return msWindow_Seats;
    }

    public String getStandard_Seats() {
        return msStandard_Seats;
    }

    public String getPremium_Seat_Fare() {
        return msPremium_Seat_Fare;
    }

    public String getWindow_Seat_Fare() {
        return msWindow_Seat_Fare;
    }

    public String getStandard_Seat_Fare() {
        return msStandard_Seat_Fare;
    }

}