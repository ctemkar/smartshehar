package in.bestbus.app;

import java.text.DecimalFormat;

import lib.app.util.CTime;


public class CStation {
    public int miTrainId, miRouteId, miLineId, miPlatform, miTrainNo, miStationId;
    public String msFirstStation;
    public double mdLat;
    public double mdLon;
    public float dDist;
    String msPlatform, msTrainNo;
    String msLine, msSplCode, msCar, msEmu, msTrainSpeed, msIndicatorSpeedCode,
            msStationName;
    String msTowardsStationName;
    String msLastStation;
    String msDirectionCode;
    String msDirection;
    String msPlatformSide;
    double mdTrainTime;
    int miTrainTimeMin;
    String msTrainTime;
    CTime moTrainTime;

    public CStation(int stationid, String station) {
        miStationId = stationid;
        msStationName = station;
    }

    public CStation(int itrainid, int routeid, int iline, int stationid,
                    String sPlatform, String platformside, String train,
                    String line, String splcode, String car, String emu, String trainspeed, String indicatorspeed,
                    String station, String fs, String ls, String sdir, int mins) {
        miStationId = stationid;
        miTrainId = itrainid;
        miRouteId = routeid;
        msPlatform = sPlatform;
//			miPlatform = sPlatform;
        msTrainNo = train;
        miLineId = iline;
        msLine = line == null ? "-" : line;
        msSplCode = splcode == null ? "" : splcode;
        msCar = car;
        msEmu = emu;
        msTrainSpeed = trainspeed == null ? "-" : trainspeed;
        msIndicatorSpeedCode = indicatorspeed == null ? "-" : indicatorspeed;
        msStationName = station;
        msFirstStation = fs == null ? "-" : fs;
        msLastStation = ls == null ? "-" : ls;
        msDirectionCode = sdir;
        msPlatformSide = platformside;
        msDirection = CGlobals_BA.DIRECTIONDOWN;
        if (msDirectionCode.equals(CGlobals_BA.DIRECTIONCODEUP))
            msDirection = CGlobals_BA.DIRECTIONUP;

        moTrainTime = new CTime(mins);
        DecimalFormat dec = new DecimalFormat("#0.00");
        if (mdTrainTime >= 24) {
            mdTrainTime = mdTrainTime - 24;
        }

        msTrainTime = dec.format(mdTrainTime >= 13 ? mdTrainTime - 12 : mdTrainTime);
//			msTrainTime +=  dTrainTime > 12 ? " p" : " a";

    }


}
