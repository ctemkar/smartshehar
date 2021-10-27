package in.bestbus.app;

import android.graphics.Color;

import org.json.JSONObject;

import java.util.ArrayList;

public class CStop {
    public static int RECENT_STOP_COLOR = Color.BLUE;
    public static int NEAR_STOP_COLOR;
    public static int EASY_WALK_DISTANCE = 800;  // Comfortable walking distance - 800 m
    private static double AVG_WALK_SPEED = .012; //
    public int miStopCode, miStopColor = 0;
    public String msStopNameDetail;
    public String msLandmarkList;
    public String msStopNameDetailLandmarklistId;
    public String msStopName;
    public double lat, lon;
    public float mdDist; // Distance from current location
    public int miStopId;
    public int miStopSerial; // Serial no. of stop in the journey
    String fileLine;
    String busno;
    String area;
    int iStartStopNo;
    int miAreaCode;
    String[] masBuses;
    //		String msStopNameId;
    String msAreaStopLandmark;
    String sStreet;
    double mdLatu, mdLonu, mdLatd, mdLond;
    char mcDirection;
    String sConnectBusNo;
    int iConnectStopNo;
    int iDestStopNo;
    boolean bGPS;    // Got GPS info?
    boolean bSelect;    // to show row selected
    String sLandMark;
    StringBuilder sbBusList;    // the buses in a delimited string
    ArrayList<CBus> businfo;
    // Disp strings to show reader friendly text
    String msDispSearchStr;
    String msDispDist;
    String msBusList;
    String msDispBusList;
    String msStopFull;
    String msBusLable;
    boolean mbStopValid;
    double mdDistance;
    int miRoutecode;

    CStop(String snameid) {
//			msStopNameId = snameid;
        msStopName = snameid;
        area = "N/A";
        mbStopValid = false;
        msStopFull = "";
        sLandMark = "";
        init();
    }

    CStop(int stopid, int stopcode, String snamedetail, double l, double ln) {
        miStopCode = stopcode;
        msStopNameDetail = snamedetail;
        lat = l;
        lon = ln;
        miStopId = stopid;
        mbStopValid = true;
        init();
    }

    CStop(int stopCode, String snamedetail, double l, double ln) {
        msStopNameDetail = snamedetail;
        lat = l;
        lon = ln;
        miStopCode = stopCode;
        mbStopValid = true;
        init();
    }

    CStop(String ar, String sname, String lmark,
          int sno, double l, double ln, int startstopno) {
        area = ar;
        msStopName = sname;
        sLandMark = lmark;
        lat = l;
        lon = ln;
        iStartStopNo = startstopno;
        mbStopValid = true;
        msStopFull = "";
        init();
    }

    CStop(int routecode, int stopcode, double dlat, double dlon) {
        miRoutecode = routecode;
        miStopCode = stopcode;
        lat = dlat;
        lon = dlon;
        init();
    }

    CStop(String ar, String sname, String lmark, String msASL,
          int istopid, double lt, double ln, int ac, String dn) {
        area = ar;
        msStopName = sname;
        sLandMark = lmark;
        msAreaStopLandmark = msASL;
        miAreaCode = ac;
        msStopNameDetail = dn;
        miStopId = istopid;
        lat = lt;
        lon = ln;
        mbStopValid = true;
        msStopFull = "";
        init();
    }

    CStop(String ar, String sname, String lmark,
          String snameid, double lt, double ln, int sc, int ac, String dn) {
        area = ar;
        msStopName = sname;
        sLandMark = lmark;
        miAreaCode = ac;
        msStopNameDetail = dn;
        lat = lt;
        lon = ln;
        mbStopValid = true;
        msStopFull = "";
        init();
    }

    CStop(String ar, String sname, String lmark, String dn,
          String snameid, double lt, double ln, int sc, int ac, String[] sb) {
        area = ar;
        msStopName = sname;
        sLandMark = lmark;
        msStopNameDetail = dn;
        miAreaCode = ac;
        masBuses = sb;
        lat = lt;
        lon = ln;
        int l = masBuses.length;
        for (int i = 0; i < l; i++)
            masBuses[i] = masBuses[i].trim();
        mbStopValid = true;
        msStopFull = "";
        init();
    }

    public CStop() {
        bGPS = false;
        bSelect = false;
        busno = "";
        iConnectStopNo = 0;
        sLandMark = "";
        mcDirection = 'U';    // default direction is Up
        businfo = new ArrayList<CBus>();
        mdDist = -1;
        sbBusList = new StringBuilder("");
        mbStopValid = false;
        msStopFull = "";
        init();
    }

    private void init() {
        NEAR_STOP_COLOR = Color.parseColor("#808000");
        miStopColor = Color.BLACK;
    }

    public void CreateDispStr() {
        msDispSearchStr = "Near - " + msStopNameDetail + "\n";
        msDispSearchStr += "Co-ord. - " + lat + ", " + lon + "\n";
        if (mdDist == -1)
            //msDispDist = "Distance: N/A";
            msDispDist = "";
        else if (mdDist <= 15)
            msDispDist = "You are at the stop.\n";
        else {        // If you are within 5m, you are at the stop
            String sApprox = "";
//			if(!bGPS)
//				sApprox = "(approx. w/o GPS) ";
            msDispDist = "Dist." + sApprox + " : " + (int) mdDist + " m., approx. ";
            msDispDist += (int) (mdDist * AVG_WALK_SPEED + 0.5) + " min. walk\n";
        }
        BusStr();
    }

    // Create a list of the buses as a comma delimited string for display
    public String BusStr() {
        String msDispBusStr = "";
        String sComma = "";
        for (String s : masBuses) {
            msDispBusStr = msDispBusStr + sComma + s;
            sComma = ", ";
        }
        return msDispBusStr;
    }

    public String getDispName() {
        if (msStopNameDetail != null && msStopNameDetail.trim().length() > 0)
            return msStopNameDetail;
        else
            return msStopName;

    }

    public double getDistance() {
        return mdDistance;
    }

    public void setDistance(double dDistance) {
        this.mdDistance = dDistance;
    }

    public String getStopArea() {
        return msStopName + " (" + area + ")";
    }

    public String getAreaStop() {
        return area + ": " + msStopName;
    }

    public String toJSON() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lat", lat);
            jsonObject.put("lon", lon);
            jsonObject.put("stopname", msStopNameDetail);

            return jsonObject.toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }

}    // eoc: CStop.java

