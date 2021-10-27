package in.bestbus.app;


public class Station {
    public static final double INVALIDLAT = -999;
    public static final double INVALIDLON = -999;

    public String msLin;
    public String msStationName, msStationAbbr;
    public double mdLat, mdLon, dDist;
    public int miStationId, miStationNo, miTimeToNxt;
    public String msSearchStr;
    boolean mbValid;
    String msWR, msCR, msHR, msTH, msNS;

    public Station(int stnno, String lin, String stn,
                   double lat, double lon, int timenxt, String stnab) {
        miStationNo = stnno;
        msLin = lin;
        msStationName = stn;
        msStationAbbr = stnab;
        mdLat = lat;
        mdLon = lon;
        miTimeToNxt = timenxt;
        mbValid = true;
    }

    public Station() {
        miStationNo = 0;
        msLin = "";
        msStationName = "";
        msStationAbbr = "";
        mdLat = INVALIDLAT;
        mdLon = INVALIDLON;
        miTimeToNxt = -1;
        mbValid = false;
    }

    public Station(int iId, String sStationName, String sSearchStr, Double dLat, Double dLon) {
        miStationId = iId;
        msStationName = sStationName;
        mdLat = dLat;
        mdLon = dLon;
        msSearchStr = sSearchStr;

        mbValid = true;
    }

    public String getStationArea() {
        // TODO Auto-generated method stub
        return null;
    }
}
