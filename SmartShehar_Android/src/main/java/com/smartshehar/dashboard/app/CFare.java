package com.smartshehar.dashboard.app;

import android.util.Log;

import java.text.DecimalFormat;

public class CFare {
    public int miDist;
    public int miMeterRs, miMeterPs;
    public double mdFare, mdNightFare, mdFareTotal, mdNightFareTotal;
    public int miRs, miPs;
    public String msNightFare;
    public String msFare, msRatePerDist;
    public String msFareTotal, msNightFareTotal;
    public String TAG = "CFare";

   /* public CFare(double dDistTraveled, String sVehicleType) {
        miDist = 0;
        miMeterRs = 1;
        miMeterPs = 0;
    }*/

    public CFare(CFareParams cfp, String sMeterType, int dDistTraveled,
                 int iWaitSecs) {
        calcFareByDist(cfp, sMeterType, dDistTraveled, iWaitSecs);
    }

    void calcFareByDist(CFareParams cfp, String sMeterType, int dDist,
                        int iWaitSecs) {
        try {
            //		if (cfp.msCity.toUpperCase(Locale.ENGLISH) == "MUMBAI") {
            getFareMinWaiting(cfp, sMeterType, dDist, iWaitSecs);
//		}


            if (cfp.fMeterRounding == 1) {
                msFare = new DecimalFormat("##").format(mdFare);
                msFareTotal = new DecimalFormat("##").format(mdFareTotal);
                msNightFare = new DecimalFormat("##").format(mdNightFare);
                msNightFareTotal = new DecimalFormat("##").format(mdNightFareTotal);
            } else {
                msFare = new DecimalFormat("##").format(mdFare);
                msFareTotal = new DecimalFormat("##").format(mdFareTotal);
                msNightFare = new DecimalFormat("##").format(mdNightFare);
                msNightFareTotal = new DecimalFormat("##").format(mdNightFareTotal);
//                msNightFareTotal = mdNightFareTotal.toString();
//                msNightFareTotal = new DecimalFormat("##").format(mdNightFareTotal).toString();
            }
//		calcFareByMeter(cfp, sMeterType, miMeterRs, miMeterPs);
            miRs = (int) (mdFare);
            miPs = (int) ((mdFare - miRs) * 100);

            CGlobals_db.mdNightFare = mdNightFare;
            miDist = dDist;
        } catch (Exception e) {
            Log.d(TAG, "CFare in calcFareByDist " + e.toString());
            e.printStackTrace();
        }

    }

    private void getFareMinWaiting(CFareParams cfp, String sMeterType,
                                   int dDist, int iWaitSecs) {
        try {
            Log.d(TAG,"sMeterType "+sMeterType);
            int totDist = dDist + iWaitSecs * cfp.iWaitChargePerHour / 36;
            if (dDist < cfp.iMinimumDistance) {
                miMeterRs = CGlobals_db.METERSTARTRS;
                miMeterPs = CGlobals_db.METERSTARTPAISE;
                mdFare = cfp.fMinimumFare;
                mdFareTotal = cfp.fMinimumFare;
                mdNightFare = cfp.fMinimumNightFare;
                mdNightFareTotal = cfp.fMinimumNightFare;
            } else {
                miDist = dDist;
                if (CGlobals_db.msCity.equalsIgnoreCase(CGlobals_db.CITY_MUMBAI)) {
                    mdFare = (dDist / 1000.0) * cfp.fFarePerKm;
                    mdFareTotal = (totDist / 1000.0) * cfp.fFarePerKm;
                } else {
                    mdFare = cfp.fMinimumFare +
                            ((dDist - cfp.iMinimumDistance) / 1000.0) * cfp.fFarePerKm;
                    mdFareTotal = cfp.fMinimumFare +
                            ((totDist - cfp.iMinimumDistance) / 1000.0) * cfp.fFarePerKm;
/*
            mdNightFare = cfp.fMinimumNightFare +
					((dDist - cfp.iMinimumDistance)/ 1000.0) *  cfp.fFarePerKm * cfp.fNightExtra;
			mdNightFareTotal = cfp.fMinimumNightFare +
					((totDist - cfp.iMinimumDistance)/ 1000.0) *  cfp.fFarePerKm * cfp.fNightExtra;
*/
                }
                mdNightFare = ((int) Math.round(mdFare)) * cfp.fNightExtra;
                mdNightFareTotal = ((int) Math.round(mdFareTotal)) * cfp.fNightExtra;

                double dIncr = (dDist - cfp.iMinimumDistance) / cfp.fMeterMovesPerKm / 10 + .1;

                int iRs = CGlobals_db.METERSTARTRS + (int) (dIncr);
                int iPs = (int) ((dIncr - (int) (dIncr)) * 10) * 10;
                miMeterRs = iRs;
                miMeterPs = iPs;
                msRatePerDist = Double.toString(cfp.fFarePerKm) + " per" + cfp.msDistanceUnit;
            }
            CGlobals_db.mEstimatedTotalDayFare = mdFareTotal;
            CGlobals_db.mEstimatedToatalNightFare = mdNightFareTotal;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getFareMinWaiting " + e.toString());
        }
    }
}
