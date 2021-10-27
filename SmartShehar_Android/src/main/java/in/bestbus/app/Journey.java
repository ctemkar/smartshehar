package in.bestbus.app;

public class Journey {
    public String stopSerial;
    public String stopnameDetail;
    public String msLandmarkList;
    public int color;
    public double mdLat, mdLon;
    public int miStopid;
    public int miStopCode;

    public Journey(String string, String msStopnameDetail) {
        super();
    }

    public Journey(String serial, String stopname, String landmarklist, int color,
                   double lat, double lon, int iStopCode) {
        super();
        this.stopSerial = serial;
        this.stopnameDetail = stopname;
        this.msLandmarkList = landmarklist;
        this.color = color;
        this.mdLat = lat;
        this.mdLon = lon;
        this.miStopCode = iStopCode;
    }
}
