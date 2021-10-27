package com.smartshehar.dashboard.app;

public class CFareParams {
	public static final String DEFAULTCITY = "MUMBAI";
	public static String AUTO = "A";
	public static String TAXI = "T";
	public static String TAXIDISPLAY = "Taxi";
	public static final String DEFAULT_VEHICLE = AUTO;
	public static final String METERTYPEDIGITAL = "D";
	
	public String sCity, msMeterType, msVehicleType,
		msCity, msTransportDescription, msVehicleTypeDescription;
	public int iMinimumDistance;
	public double fMinimumFare, fMinimumNightFare, fFarePerKm, fMeterMovesPerKm,
		fMeterRounding, fNightExtra, fNightStart, fNightEnd;
	public int iMinimumWaitingMinutes, iWaitChargePerHour, iLuggageChargePerPiece;
	public String msDistanceUnit;
	// copy constructor
	public void objClone(CFareParams cfp) {
		this.sCity = cfp.sCity;
		this.msVehicleTypeDescription = cfp.msVehicleTypeDescription;
		this.msMeterType = cfp.msMeterType;
		this.msVehicleType = cfp.msVehicleType;
		this.msCity = cfp.msCity;
		this.iMinimumDistance = cfp.iMinimumDistance;
		this.fMinimumFare = cfp.fMinimumFare;
		this.fMinimumNightFare = cfp.fMinimumNightFare;
		this.fFarePerKm = cfp.fFarePerKm;
		this.fMeterMovesPerKm = cfp.fMeterMovesPerKm;
		this.fMeterRounding = cfp.fMeterRounding;
		this.fNightExtra = cfp.fNightExtra;
		this.fNightStart = cfp.fNightStart;
		this.fNightEnd = cfp.fNightEnd;
		this.iMinimumWaitingMinutes = cfp.iMinimumWaitingMinutes;
		this.iWaitChargePerHour = cfp.iWaitChargePerHour;
		this.iLuggageChargePerPiece = cfp.iLuggageChargePerPiece;
		this.msTransportDescription = cfp.msTransportDescription;
		this.msDistanceUnit = cfp.msDistanceUnit;
	}
	public CFareParams() {
		
	}
	public CFareParams(String sCity, String sVehicleType) {
		msCity = sCity;
	} ;
}
