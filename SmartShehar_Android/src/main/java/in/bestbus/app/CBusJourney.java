package in.bestbus.app;


public class CBusJourney {
    public String msRouteCode, msBusno, msStopnameDetail, msLandmarkList;
    public double mdStopSerial;
    public double mdLat, mdLon;
    public int miStopId;
    public int mistopCode;

    CBusJourney(String routecode) {
        msRouteCode = routecode;
    }

}
