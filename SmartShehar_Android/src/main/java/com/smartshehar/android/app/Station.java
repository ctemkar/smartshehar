package com.smartshehar.android.app;

import java.text.DecimalFormat;

import lib.app.util.CTime;


public class Station {
	public static final double INVALIDLAT = -999;
	public static final double INVALIDLON = -999;

	public int miTrainId, miRouteId, miLineId, miPlatform, miTrainNo, miStationId;
	public String msPlatform;
	String msTrainNo;
	public String msLine;
	public String msSplCode;
	public String msCar;
	public String msEmu;
	public String msTrainSpeed;
	public String msIndicatorSpeedCode;
	public String msStationName;
	public String msFirstStation;
	String msTowardsStationName;
	public String msLastStation;
	String msDirectionCode;
	String msDirection;
	public String msPlatformSide;
	double mdTrainTime;
	public int miTrainTimeMin;
	public String msTrainTime;
	public double mdLat;
	public double mdLon;
	public float dDist;
	CTime moTrainTime;
	
	String msLin;
	public String msStationAbbr;
	public int miStationNo, miTimeToNxt;
	boolean mbValid;
	String msWR, msCR, msHR, msTH, msNS;
	public String msSearchStr;
		
		public Station(int stnno, String lin, String stn, 
							double lat, double lon, int timenxt, String stnab) {
			miStationNo = stnno; msLin = lin; msStationName = stn; msStationAbbr = stnab;
			mdLat = lat; mdLon = lon; miTimeToNxt = timenxt;
			mbValid = true;
		}
		public Station(int stationid, String station) {
			miStationId = stationid;
			msStationName = station;
		}

		public Station() {
			miStationNo = 0; msLin = ""; msStationName = ""; msStationAbbr = ""; 
			mdLat = INVALIDLAT; mdLon = INVALIDLON; miTimeToNxt = -1;
			mbValid = false;
		}
		public Station(int iId, String sStationName, String sSearchStr, Double dLat, Double dLon) {
			miStationId = iId; msStationName = sStationName; 
/*			msWR = sWR; msCR = sCR; msHR = sHR; msTH = sTH; msNS = sNS; */
			mdLat = dLat; mdLon = dLon; 
			msSearchStr = sSearchStr;
/*			
			if(sWR.equals("1"))
				msSearchStr +=  "WR ";
			if(sCR.equals("1"))
				msSearchStr +=  "CR ";
			if(sHR.equals("1"))
				msSearchStr +=  "HR ";
			if(sTH.equals("1"))
				msSearchStr +=  "TH ";
			if(sNS.equals("1"))
				msSearchStr +=  "PN ";
*/				
				
/*			" CASE WHEN wr = 1 THEN 'WR ' ELSE '' END || " +
			" CASE WHEN cr = 1 THEN 'CR ' ELSE '' END || " +
			" CASE WHEN hr = 1 THEN 'HR ' ELSE '' END  || " +
			" CASE WHEN th = 1 THEN 'TH ' ELSE '' END  || " +
			" CASE WHEN pn = 1 THEN 'PN ' ELSE '' END  || " +
			"')' AS searchstr ";
*/			
			mbValid = true;
		}
		public Station(int itrainid, int routeid, int iline, int stationid, 
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
			msDirection = CGlobals_trains.DIRECTIONDOWN;
			if(msDirectionCode.equals(CGlobals_trains.DIRECTIONCODEUP))
				msDirection = CGlobals_trains.DIRECTIONUP;

			moTrainTime = new CTime(mins);
			DecimalFormat dec = new DecimalFormat("#0.00");
			if(mdTrainTime >= 24) {
				mdTrainTime = mdTrainTime - 24;
			} 

			msTrainTime = dec.format(mdTrainTime >= 13 ? mdTrainTime - 12 : mdTrainTime);
//			msTrainTime +=  dTrainTime > 12 ? " p" : " a";
			
	}
		
		public String getStationArea() {
			// TODO Auto-generated method stub
			return null;
		}
}
