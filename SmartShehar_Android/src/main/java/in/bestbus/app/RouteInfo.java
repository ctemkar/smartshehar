package in.bestbus.app;

import java.util.ArrayList;


public class RouteInfo {

    private static double AVG_WALK_SPEED = .012; //
    public int miStopSerial; // Serial no. of station in the journey
    protected int miStationId;
    int id;
    String fileLine;
    String busno;
    String area;
    int iStartStopNo;
    int miAreaCode;
    String[] masLines;
    String msDisplayName;
    String stationname;
    String msAreaStopLandmark;
    String sStreet;
    double lat;
    double lon;
    double latu;
    double lonu;
    double latd;
    double lond;
    char mcDirection;
    String sConnectTrainNo;
    int iConnectStopNo;
    int iDestStopNo;
    float dist; // Distance from current location
    boolean bGPS;    // Got GPS info?
    boolean bSelect;    // to show row selected
    String sLandMark;
    String msSearchStr;
    StringBuilder sbTrainList;    // the Lines in a delimited string
    ArrayList<RouteInfo> traininfo;
    // Disp strings to show reader friendly text
    String msDispSearchStr;
    String msDispDist;
    String msTrainList;
    String msDispTrainList;
    String msStopFull;
    boolean mbStopValid;


    RouteInfo(String ar, String sname, String lmark,
              int sno, double l, double ln, int startstationno) {
        area = ar;
        stationname = sname;
        sLandMark = lmark;
        lat = l;
        lon = ln;
        iStartStopNo = startstationno;
        mbStopValid = true;
        msStopFull = "";
    }

    RouteInfo(String ar, String sname, String lmark, String msASL,
              int istationid, double lt, double ln, int ac, String dn) {
        area = ar;
        stationname = sname;
        sLandMark = lmark;
        msAreaStopLandmark = msASL;
        miAreaCode = ac;
        msDisplayName = dn;
        miStationId = istationid;
        lat = lt;
        lon = ln;
        mbStopValid = true;
        msStopFull = "";
    }

    RouteInfo(String ar, String sname, String lmark,
              String snameid, double lt, double ln, int sc, int ac, String dn) {
        area = ar;
        stationname = sname;
        sLandMark = lmark;
        miAreaCode = ac;
        msDisplayName = dn;
        lat = lt;
        lon = ln;
        mbStopValid = true;
        msStopFull = "";
    }

    RouteInfo(String ar, String sname, String lmark, String dn,
              String snameid, double lt, double ln, int sc, int ac, String[] sb) {
        area = ar;
        stationname = sname;
        sLandMark = lmark;
        msDisplayName = dn;
        miAreaCode = ac;
        masLines = sb;
        lat = lt;
        lon = ln;
        int l = masLines.length;
        for (int i = 0; i < l; i++)
            masLines[i] = masLines[i].trim();
        mbStopValid = true;
        msStopFull = "";
    }

    RouteInfo() {
        bGPS = false;
        bSelect = false;
        busno = "";
        iConnectStopNo = 0;
        msSearchStr = "";
        sLandMark = "";
        mcDirection = 'U';    // default direction is Up
        traininfo = new ArrayList<RouteInfo>();
        dist = -1;
        sbTrainList = new StringBuilder("");
        mbStopValid = false;
        msStopFull = "";
    }

    public void CreateDispStr() {

        msDispSearchStr = "Near - " + msSearchStr + "\n";
        msDispSearchStr += "Co-ord. - " + lat + ", " + lon + "\n";
        if (dist == -1)
            //msDispDist = "Distance: N/A";
            msDispDist = "";
        else if (dist <= 15)
            msDispDist = "You are at the station.\n";
        else {        // If you are within 5m, you are at the station
            String sApprox = "";
//			if(!bGPS)
//				sApprox = "(approx. w/o GPS) ";
            msDispDist = "Dist." + sApprox + " : " + (int) dist + " m., approx. ";
            msDispDist += (int) (dist * AVG_WALK_SPEED + 0.5) + " min. walk\n";
        }
        TrainStr();
    }

    // Create a list of the Lines as a comma delimited string for display
    public String TrainStr() {
        String msDispTrainStr = "";
        String sComma = "";
        for (String s : masLines) {
            msDispTrainStr = msDispTrainStr + sComma + s;
            sComma = ", ";
        }
        return msDispTrainStr;
    }

    public String getDispName() {
        if (msDisplayName != null && msDisplayName.trim().length() > 0)
            return msDisplayName;
        else
            return stationname;

    }

    public String getStopArea() {
        return stationname + " (" + area + ")";
    }

    public String getAreaStop() {
        return area + ": " + stationname;
    }

    // stationname, landmark and are in human readable form
    public String toStopFull() {
        if (msStopFull.trim().length() > 0)
            return msStopFull;
//		if(area == null)
//			parseFileLine();
        String sLmark = sLandMark.trim().length() > 0 ? " / " + sLandMark.trim() : "";
        msStopFull = area.trim() + ": " + stationname.trim() + sLmark;
        return msStopFull;
    }

    public String stationExplore() {
        if (stationname.trim().length() == 0)
            return "N/A";
        String sLmark = sLandMark.trim().length() > 0 ? " / " + sLandMark.trim() : "";
        return stationname + sLmark + ": " + area;

    }


}    // eoc: RouteInfo.java

