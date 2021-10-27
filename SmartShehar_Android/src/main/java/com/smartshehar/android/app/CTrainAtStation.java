package com.smartshehar.android.app;

import lib.app.util.CTime;


public class CTrainAtStation {
	public int miMins;
		public int miTrainId;
		int miRouteId;
		int miLineId;
		int miPlatform;
		public int miCar, miDiff;
		public boolean mbSundayOnly, mbNotOnSunday, mbHoliday;
		public String msPlatform, msTrainNo, msLine;
		public String msSpl, msSpeedAbbr, msSpeed, msFirstStation, msTowardsStationName, 
			msLastStation, msDirectionCode, msDirection, msTrainTime, msColour, msSearchStr;
		public CTime moTrainTime;
		public double mdLat, mdLon;
		public float dDist;
		public String msTrainName;
		
		public CTrainAtStation(int itrainid, int routeid, int iline, String platform, String trainno, 
				String line, String colour, String spl, int sundayonly, int notonsunday, int holiday, 
				String spcode, String speed, String fs, String ls, 
				String sdir, int iCar, String towards, int iMins) {
			msColour = colour;
			miTrainId = itrainid;
			miRouteId = routeid;
			mbSundayOnly = sundayonly > 0 ? true : false;
			mbNotOnSunday = notonsunday > 0 ? true : false; 
			mbHoliday = holiday > 0 ? true : false;
//			msPlatform = platform == 0 ? "-" : Integer.toString(platform);
			msPlatform = platform;
			msTrainNo = trainno;
			miLineId = iline;
			msLine = line == null ? "-" : line;
			msSpl = spl == null ? "" : spl;
			msSpeedAbbr = spcode == null ? "-" : spcode;
			msSpeed = speed == null ? "-" : speed;
			msFirstStation = fs == null ? "-" : fs;
			msLastStation = ls == null ? "-" : ls;
			msDirectionCode = sdir;
			msDirection = CGlobals_trains.DIRECTIONDOWN;
			if(msDirectionCode.equals(CGlobals_trains.DIRECTIONCODEUP))
				msDirection = CGlobals_trains.DIRECTIONUP;

			msTowardsStationName = towards;
			
//			msTrainTime +=  dTrainTime > 12 ? " p" : " a";
			miCar = iCar;
			this.miMins = iMins;

	}


}
