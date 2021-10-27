package com.smartshehar.cabe.driver;

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

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;
import lib.app.util.ContactInfo;
import lib.app.util.SSLog_SS;


public class CTrip_CED {

    private static String TAG = "CTrip_CED: ";
    public String msHtmlDescription = "", msPhoneno, msVehicleColor, msVehicleCompany,
            msVehicleType, msVehicleNo, msFrom, msFromShort, msTo, msToShort, msToSubLocality,
            msTripStatus, msDrivername, msName, msDriverEmail, msCountryCode,
            msUserName, msFullName, msVehicleCategory, msVehicleModel, msTripCreationTime,
            msTripFare, msCompanyName, msPremium_Seats, msWindow_Seats, msStandard_Seats,
            msPremium_Seat_Fare, msWindow_Seat_Fare, msStandard_Seat_Fare, msFirstName, msLastName,
            msGender, msBooking_Time, msTrip_Cost, msTrip_Distance, msTrip_Time, msTrip_Rating,
            msTrip_Comment, msTotal_For_Hire, msTotal_Trips, msTotal_Shift_Time, msDOB, msDriver_Firstname,
            msDriver_Lastname, msDriver_Email, msDriver_Phoneno, msDriver_Gender, msImage_Name, msImage_Path,
            msDriver_DOB, msCancel_Reason, msCancel_Reason_Code, msCancel_Reason_User, sShareAddress, area_fixed,
            landmark_Fixed, pick_drop_point_Fixed, formatted_address_Fixed, administrative_area_level_1_Fixed,
            administrative_area_level_2_Fixed, locality_Fixed, sublocality_Fixed, postal_code_Fixed, route_Fixed,
            neighborhood_Fixed;
    String msMarkerText = "";
    private String msPlannedStartTime, msPlannedStartDateTime, msPlanned_Start_DateTime;
    private boolean mbHasDriverMoved;
    // for cabs
    private String msCarDesc, msStartLandmark, msTripNote;
    LatLng moDriverLatLng = null;
    boolean mIsFriend = true;
    JSONObject mjTrip;
    private String msJDriver;
    private int miInTrip, fixed_address_id;
    private int miInPathFrom, miInPathTo, miCommercial_Vehicle_Id, msCancel_Reason_Id, miPassenger_Appuser_Id;
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
    private int miImage_Rotation = 0, msDriver_Appuser_Id;
    private String msTripPath;
    private double fromLat, fromLng, toLat, toLng, sShareAddressLat, sShareAddressLng;
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
    private double slastlng, slastlat, latitude_Fixed, longitude_Fixed;
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

    public CTrip_CED() {
    }

    public CTrip_CED(String sFrom, String sTo) {
        msFrom = sFrom;
        msTo = sTo;
    }

    public CTrip_CED(String sFrom, String sTo, double dfromLat, double dfromLng, double dtoLat, double dtoLng) {
        msFrom = sFrom;
        msTo = sTo;
        fromLat = dfromLat;
        fromLng = dfromLng;
        toLat = dtoLat;
        toLng = dtoLng;
    }

    public CTrip_CED(JSONObject oTrip) {

        try {
            msFrom = isNullNotDefined(oTrip, "fromaddress") ? ""
                    : mjTrip.getString("fromaddress");
            msFrom = isNullNotDefined(oTrip, "toaddress") ? ""
                    : mjTrip.getString("toaddress");
            fromLat = isNullNotDefined(oTrip, "fromlat") ? CGlobals_lib_ss.INVALIDLAT
                    : mjTrip.getDouble("fromlat");
            fromLng = isNullNotDefined(oTrip, "fromlng") ? CGlobals_lib_ss.INVALIDLAT
                    : mjTrip.getDouble("fromlng");
            toLat = isNullNotDefined(oTrip, "tolat") ? CGlobals_lib_ss.INVALIDLAT
                    : mjTrip.getDouble("tolat");
            toLng = isNullNotDefined(oTrip, "tolng") ? CGlobals_lib_ss.INVALIDLAT
                    : mjTrip.getDouble("tolng");

        } catch (Exception e) {
            SSLog_SS.e(TAG, "CTrip" + e);
        }
    }

    public CTrip_CED(String sjDriver, Context context) {
        CGlobals_lib_ss.getInstance();
        init(sjDriver, context);

    } // CTrip

    public void init(String sjDriver, Context context) {
        msCar = "";
        msJDriver = sjDriver;
        if (!TextUtils.isEmpty(sjDriver)) {
            try {
                mjTrip = new JSONObject(sjDriver);
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
                msDOB = isNullNotDefined(mjTrip, "dob") ? "" : mjTrip
                        .getString("dob");

                msDriver_Appuser_Id = isNullNotDefined(mjTrip, "driver_appuser_id") ? -1 : mjTrip
                        .getInt("driver_appuser_id");
                msFirstName = isNullNotDefined(mjTrip, "firstname") ? "" : mjTrip
                        .getString("firstname");
                msLastName = isNullNotDefined(mjTrip, "lastname") ? "" : mjTrip
                        .getString("lastname");
                msGender = isNullNotDefined(mjTrip, "gender") ? "" : mjTrip
                        .getString("gender");
                msBooking_Time = isNullNotDefined(mjTrip, "booking_time") ? "" : mjTrip
                        .getString("booking_time");
                msTrip_Cost = isNullNotDefined(mjTrip, "trip_cost") ? "" : mjTrip
                        .getString("trip_cost");
                msTrip_Distance = isNullNotDefined(mjTrip, "trip_distance") ? "" : mjTrip
                        .getString("trip_distance");
                msTrip_Time = isNullNotDefined(mjTrip, "trip_time") ? "" : mjTrip
                        .getString("trip_time");
                msTrip_Rating = isNullNotDefined(mjTrip, "trip_rating") ? "" : mjTrip
                        .getString("trip_rating");
                msTrip_Comment = isNullNotDefined(mjTrip, "trip_comment") ? "" : mjTrip
                        .getString("trip_comment");
                msTotal_For_Hire = isNullNotDefined(mjTrip, "total_for_hire") ? "" : mjTrip
                        .getString("total_for_hire");
                msTotal_Trips = isNullNotDefined(mjTrip, "total_trips") ? "" : mjTrip
                        .getString("total_trips");
                msTotal_Shift_Time = isNullNotDefined(mjTrip, "total_shift_time") ? "" : mjTrip
                        .getString("total_shift_time");

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

                msPlanned_Start_DateTime = isNullNotDefined(mjTrip,
                        "planned_start_datetime") ? "" : mjTrip
                        .getString("planned_start_datetime");

                try {
                    mbHasDriverMoved = !isNullNotDefined(mjTrip,
                            "hasdriver_moved") && (mjTrip
                            .getInt("hasdriver_moved") == 1);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    mbHasDriverMoved = false;
                }

                msTripNote = isNullNotDefined(mjTrip, "trip_note") ? ""
                        : mjTrip.getString("trip_note");
                msTripCreationTime = isNullNotDefined(mjTrip, "tripcreationtime") ? "" : mjTrip
                        .getString("tripcreationtime");

                msFrom = isNullNotDefined(mjTrip, "fromaddress") ? "" : mjTrip
                        .getString("fromaddress");

                fromLat = isNullNotDefined(mjTrip, "fromlat") ? Constants_lib_ss.INVALIDLAT : mjTrip
                        .getDouble("fromlat");
                fromLng = isNullNotDefined(mjTrip, "fromlng") ? Constants_lib_ss.INVALIDLAT : mjTrip
                        .getDouble("fromlng");

                msFromShort = isNullNotDefined(mjTrip, "fromshortaddress") ? ""
                        : mjTrip.getString("fromshortaddress");
                if (TextUtils.isEmpty(msFromShort)) {
                    msFromShort = msFrom;
                }
                msTo = isNullNotDefined(mjTrip, "toaddress") ? "" : mjTrip
                        .getString("toaddress");

                toLat = isNullNotDefined(mjTrip, "tolat") ? Constants_lib_ss.INVALIDLAT : mjTrip
                        .getDouble("tolat");
                toLng = isNullNotDefined(mjTrip, "tolng") ? Constants_lib_ss.INVALIDLAT : mjTrip
                        .getDouble("tolng");

                slastlat = isNullNotDefined(mjTrip, "lastlat") ? Constants_lib_ss.INVALIDLAT : mjTrip
                        .getDouble("lastlat");
                slastlng = isNullNotDefined(mjTrip, "lastlng") ? Constants_lib_ss.INVALIDLAT : mjTrip
                        .getDouble("lastlng");

                mdDriverLat = isNullNotDefined(mjTrip, "driver_lat") ? Constants_lib_ss.INVALIDLAT : mjTrip
                        .getDouble("driver_lat");
                mdDriverLng = isNullNotDefined(mjTrip, "driver_lng") ? Constants_lib_ss.INVALIDLAT : mjTrip
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
                mIsCommercial = !isNullNotDefined(mjTrip, "is_commercial") && (mjTrip.getInt("is_commercial") == 1);
                hasJoined = mjTrip.isNull(Constants_CED.COL_HAS_JOINED) ? 0
                        : Integer.parseInt(mjTrip
                        .getString(Constants_CED.COL_HAS_JOINED));
                hasJumpedIn = mjTrip
                        .isNull(Constants_CED.COL_HAS_JUMPED_IN) ? 0
                        : Integer
                        .parseInt(mjTrip
                                .getString(Constants_CED.COL_HAS_JUMPED_IN));
                hasJumpedOut = mjTrip
                        .isNull(Constants_CED.COL_HAS_JUMPED_OUT) ? 0
                        : Integer
                        .parseInt(mjTrip
                                .getString(Constants_CED.COL_HAS_JUMPED_OUT));

                hasCanceledJoin = mjTrip.isNull(Constants_CED.COL_CANCEL_JOIN) ? 0
                        : Integer.parseInt(mjTrip
                        .getString(Constants_CED.COL_CANCEL_JOIN));

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


                // Share Cab Fixed Address Response
                fixed_address_id = isNullNotDefined(mjTrip, "fixed_address_id") ? -1 : mjTrip.getInt("fixed_address_id");
                area_fixed = isNullNotDefined(mjTrip, "area") ? "" : mjTrip.getString("area");
                landmark_Fixed = isNullNotDefined(mjTrip, "landmark") ? "" : mjTrip.getString("landmark");
                pick_drop_point_Fixed = isNullNotDefined(mjTrip, "pick_drop_point") ? "" : mjTrip.getString("pick_drop_point");
                formatted_address_Fixed = isNullNotDefined(mjTrip, "formatted_address") ? "" : mjTrip.getString("formatted_address");
                locality_Fixed = isNullNotDefined(mjTrip, "locality") ? "" : mjTrip.getString("locality");
                sublocality_Fixed = isNullNotDefined(mjTrip, "sublocality") ? "" : mjTrip.getString("sublocality");
                postal_code_Fixed = isNullNotDefined(mjTrip, "postal_code") ? "" : mjTrip.getString("postal_code");
                route_Fixed = isNullNotDefined(mjTrip, "route") ? "" : mjTrip.getString("route");
                neighborhood_Fixed = isNullNotDefined(mjTrip, "neighborhood") ? "" : mjTrip.getString("neighborhood");
                administrative_area_level_2_Fixed = isNullNotDefined(mjTrip, "administrative_area_level_2") ? ""
                        : mjTrip.getString("administrative_area_level_2");
                administrative_area_level_1_Fixed = isNullNotDefined(mjTrip, "administrative_area_level_1") ? ""
                        : mjTrip.getString("administrative_area_level_1");
                latitude_Fixed = isNullNotDefined(mjTrip, "latitude") ? Constants_lib_ss.INVALIDLAT
                        : mjTrip.getDouble("latitude");
                longitude_Fixed = isNullNotDefined(mjTrip, "longitude") ? Constants_lib_ss.INVALIDLNG
                        : mjTrip.getDouble("longitude");
                //End Share Cab Fixed Address Response


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


                msDriver_Firstname = isNullNotDefined(mjTrip, "driver_firstname") ? ""
                        : mjTrip.getString("driver_firstname");

                msDriver_Lastname = isNullNotDefined(mjTrip, "driver_lastname") ? ""
                        : mjTrip.getString("driver_lastname");

                msDriver_Email = isNullNotDefined(mjTrip, "driver_email") ? ""
                        : mjTrip.getString("driver_email");

                msDriver_Phoneno = isNullNotDefined(mjTrip, "driver_phoneno") ? ""
                        : mjTrip.getString("driver_phoneno");

                msDriver_Gender = isNullNotDefined(mjTrip, "driver_gender") ? ""
                        : mjTrip.getString("driver_gender");

                msImage_Name = isNullNotDefined(mjTrip, "image_name") ? ""
                        : mjTrip.getString("image_name");

                msImage_Path = isNullNotDefined(mjTrip, "image_path") ? ""
                        : mjTrip.getString("image_path");

                msDriver_DOB = isNullNotDefined(mjTrip, "driver_dob") ? ""
                        : mjTrip.getString("driver_dob");
                //Cancel Reason
                msCancel_Reason_Id = isNullNotDefined(mjTrip, "cancel_reason_id") ? -1
                        : mjTrip.getInt("cancel_reason_id");

                msCancel_Reason = isNullNotDefined(mjTrip, "cancel_reason") ? ""
                        : mjTrip.getString("cancel_reason");

                msCancel_Reason_Code = isNullNotDefined(mjTrip, "cancel_reason_code") ? ""
                        : mjTrip.getString("cancel_reason_code");

                msCancel_Reason_User = isNullNotDefined(mjTrip, "cancel_reason_user") ? ""
                        : mjTrip.getString("cancel_reason_user");

                msCancel_Reason_User = isNullNotDefined(mjTrip, "cancel_reason_user") ? ""
                        : mjTrip.getString("cancel_reason_user");

                miPassenger_Appuser_Id = isNullNotDefined(mjTrip, "passenger_appuser_id") ? -1
                        : mjTrip.getInt("passenger_appuser_id");


                SimpleDateFormat df = new SimpleDateFormat(
                        Constants_CED.STANDARD_DATE_FORMAT, Locale.getDefault());
                isTripActive = false;
                if (!TextUtils.isEmpty(sLastAccess)) {
                    Date datetimeLastAccess;
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
                    Date datetimeLastLocation;
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
                        .equals(Constants_CED.TRIP_ACTION_CREATE)) {
                    msTripStatus = "Trip not started. ";
                } else if ((Constants_CED.TRIP_ACTION_BEGIN
                        + Constants_CED.TRIP_ACTION_PAUSE + Constants_CED.TRIP_ACTION_RESUME)
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
                } else if ((Constants_CED.TRIP_ACTION_ABORT + Constants_CED.TRIP_ACTION_END)
                        .contains(msTripAction)) {
                    msTripStatus = "Trip just ended. ";
                    miInTrip = 0;
                    isTripActive = false;
                } /*else if ((Constants_CED.TRIP_ACTION_SCHEDULE)
                        .contains(msTripAction)) {
                    isTripActive = false;
                }*/ else {
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
                mCcontactinfo = CGlobals_lib_ss.getContactInfo(
                        context, msCountryCode, msPhoneno, msDriverEmail);
                if (mCcontactinfo != null) {
                    msName = mCcontactinfo.getContactName();
                    if (CGlobals_lib_ss.getInstance().getPersistentPreference(context).
                            getString(Constants_CED.PREF_PHONENO, "").equals(msPhoneno)) {
                        itsMe = true;
                    }
                    if (itsMe) {
                        msName = "Me";
                    } else if (TextUtils.isEmpty(mCcontactinfo.getContactName())) {
                        mIsFriend = false;
                        msName = msUserName;
                    }
                }

                if (CGlobals_lib_ss.getInstance().getPersistentPreference(context).
                        getString(Constants_CED.PREF_PHONENO, "").equals(msPhoneno.trim())) {
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
                SSLog_SS.e(TAG, "CTrip constructor - ", e, context);
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
            SSLog_SS.e(TAG, "toJSon" + e);
        }
        return obj;

    }

    public boolean sameAs(CTrip_CED oTrip) {

        return oTrip.msFrom.equals(this.msFrom) && oTrip.msTo.equals(this.msTo);

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

    public String getPlanned_Start_DateTime() {
        return msPlanned_Start_DateTime;
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
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date dateObj = sdf.parse(msPlannedStartTime);
            stime = new SimpleDateFormat("HH:mm a", Locale.getDefault()).format(dateObj);
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
        return hasJumpedIn > 0;
    }

    public boolean hasJumpedOut() {
        return hasJumpedOut > 0;
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

        return !jo.has(jkey) || jo.isNull(jkey);

    }


    public boolean hasJoined() {
        return hasJoined > 0;
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        try {
            if (!TextUtils.isEmpty(msTripMovedDriverTime)) {
                cal.setTime(sdf.parse(msTripMovedDriverTime));
                SimpleDateFormat rdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        try {
            if (!TextUtils.isEmpty(msTripActionTime)) {
                cal.setTime(sdf.parse(msTripActionTime));
                SimpleDateFormat rdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
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

        String sToken, sMatchingToken = "";
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

    /*public void setMyAddress(String myAddress) {
        this.myAddress = myAddress;
        this.msFromAddressShort = shortenedAddress(myAddress, msFrom);
        this.msToAddressShort = shortenedAddress(myAddress, msTo);
        createDescription();
    }*/

    private String getToShortAddress() {
        if (!TextUtils.isEmpty(msToAddressShort)) {
            return msToAddressShort;
        } else if (!TextUtils.isEmpty(msToShort)) {
            return msToShort;
        } else {
            return msTo;
        }

    }

    /*private void createDescription() {
        if (msTripType.equals(Constants_CED.TRIP_TYPE_COMMERCIAL)) {
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
    }*/

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
        Date dt;
        String sTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a", Locale.getDefault());
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
        return getsun() && day == 1 || getmon() && day == 2 || gettue() && day == 3 || getwed() && day == 4 || getthu() && day == 5 || getfri() && day == 6 || getsat() && day == 7;
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

    public int getDriver_Appuser_Id() {
        return msDriver_Appuser_Id;
    }

    public String getFirstName() {
        return msFirstName;
    }

    public String getLastName() {
        return msLastName;
    }

    public String getGender() {
        return msGender;
    }

    public String getBooking_Time() {
        return msBooking_Time;
    }

    public String getTrip_Cost() {
        return msTrip_Cost;
    }

    public String getTrip_Distance() {
        return msTrip_Distance;
    }

    public String getTrip_Time() {
        return msTrip_Time;
    }

    public String getTrip_Rating() {
        return msTrip_Rating;
    }

    public String getTrip_Comment() {
        return msTrip_Comment;
    }

    public String getTotal_For_Hire() {
        return msTotal_For_Hire;
    }

    public String getTotal_Trips() {
        return msTotal_Trips;
    }

    public String getTotal_Shift_Time() {
        return msTotal_Shift_Time;
    }

    public String getAge() {
        return msAge;
    }

    public String getDOB() {
        return msDOB;
    }

    public String getDriver_Firstname() {
        return msDriver_Firstname;
    }

    public String getDriver_Lastname() {
        return msDriver_Lastname;
    }

    public String getDriver_Email() {
        return msDriver_Email;
    }

    public String getDriver_Phoneno() {
        return msDriver_Phoneno;
    }

    public String getDriver_Gender() {
        return msDriver_Gender;
    }

    public String getImage_Name() {
        return msImage_Name;
    }

    public String getImage_Path() {
        return msImage_Path;
    }

    public String getDriver_DOB() {
        return msDriver_DOB;
    }

    public int getCancel_Reason_Id() {
        return msCancel_Reason_Id;
    }

    public String getCancel_Reason() {
        return msCancel_Reason;
    }

    public String getCancel_Reason_Code() {
        return msCancel_Reason_Code;
    }

    public String getCancel_Reason_User() {
        return msCancel_Reason_User;
    }

    // Share Cab Fixed Address GET Function

    public String getArea_Fixed() {
        return area_fixed;
    }

    public String getLandmark_Fixed() {
        return landmark_Fixed;
    }

    public String getPick_Drop_Point_Fixed() {
        return pick_drop_point_Fixed;
    }

    public String getFormatted_Address_Fixed() {
        return formatted_address_Fixed;
    }

    public String getAdministrative_Area_Level_1_Fixed() {
        return administrative_area_level_1_Fixed;
    }

    public String getAdministrative_Area_Level_2_Fixed() {
        return administrative_area_level_2_Fixed;
    }

    public String getLocality_Fixed() {
        return locality_Fixed;
    }

    public String getSublocality_Fixed() {
        return sublocality_Fixed;
    }

    public String getPostal_Code_Fixed() {
        return postal_code_Fixed;
    }

    public String getRoute_Fixed() {
        return route_Fixed;
    }

    public String getNeighborhood_Fixed() {
        return neighborhood_Fixed;
    }

    public int getFixed_Address_id() {
        return fixed_address_id;
    }

    public double getLatitude_Fixed() {
        return latitude_Fixed;
    }

    public double getLongitude_Fixed() {
        return longitude_Fixed;
    }

    public int getPassenger_Appuser_Id() {
        return miPassenger_Appuser_Id;
    }
}