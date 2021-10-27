package in.bestbus.app;

import android.text.TextUtils;

import java.text.DecimalFormat;

public class CTrainJourney {
    int miTrainId, miRouteId, miLineId, miPlatform, miTrainNo, miTowardsStationId;
    String msPlatform, msTrainNo;
    String msLine, msSpl, msIndictorSpeedCode, msTrainSpeedCode, msFirstStation,
            msLastStation, msDirectionCode, msDirection, msPlatformSide;
    String msTrainTime;

    public CTrainJourney(int itrainid, String line, String traintime, int station_id,
                         String stationname, String directioncode, int routeid, String trainspeedcode,
                         String indicatorspeedcode, int platformno, String platformside) {
        miTrainId = itrainid;
        miRouteId = routeid;
        msPlatform = platformno == 0 ? "-" : Integer.toString(platformno);
        miPlatform = platformno;
//			msTrainNo = train== 0 ? "-" : Integer.toString(train);
//			miTrainNo = train; 
//			miLineId = iline;
        msLine = line == null ? "-" : line;
        msTrainSpeedCode = trainspeedcode == null ? "-" : trainspeedcode;
        msIndictorSpeedCode = indicatorspeedcode == null ? "-" : indicatorspeedcode;
        msDirectionCode = directioncode;
        double dTrainTime;
        DecimalFormat dec = new DecimalFormat("#.00");
        msPlatformSide = "";
        if (!TextUtils.isEmpty(msPlatformSide))
            msPlatformSide = "";

        if (traintime.equals("-"))
            msTrainTime = "-";
        else {
            dTrainTime = new Double(traintime);
            msTrainTime = dTrainTime > 13 ? dec.format(dTrainTime - 13) : dec.format(dTrainTime);

        }
    }


}
