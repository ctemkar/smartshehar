package com.smartshehar.dashboard.app;

import android.content.ContentValues;
import android.text.TextUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CTrip {
	public int _id;
	public String msVehicleNo;
	public double mdStartLat, mdStartLon, mdDestLat, mdDestLon;
	public long mlStartMs, mlDestMs;
	public String msStartTime, msDestTime;
	public int miPhoneDist, miEstimatedDist, miMeterDistance;
	public double mdVehicleMeter, mdFareCharged, mdPhoneMeter, mdPhoneFare;
	public ContentValues mRowValues;
	public String msStartLat, msStartLon, msDestLat, msDestLon;
	public String msStartAddr, msDestAddr;
	public CFareParams moFareParams;
	public String msStartDateTime, msDestDateTime;
	public String msMeterType;

	public CTrip() {

	}

	public CTrip(CFareParams cfp, String vehicletype, double startlat, double startlon,
			double destlat, double destlon, long startms, long destms,
			double phonemeter, double phonefare, int phoneDistance) {
		moFareParams = cfp;
		moFareParams.msVehicleType = vehicletype;
		mdStartLat = startlat; mdStartLon = startlon; mdDestLat = destlat; mdDestLon = destlon;
		mlStartMs = startms; mlDestMs = destms;
		mdPhoneMeter = phonemeter; mdPhoneFare = phonefare; miPhoneDist = phoneDistance;

		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm");
		msStartTime = formatter.format(new Date(mlStartMs));
		msDestTime = formatter.format(new Date(mlDestMs));
		msStartDateTime = android.text.format.DateFormat.format("dd-MM-yyyy - hh:mm", new Date(mlStartMs)).toString();
		msDestDateTime = android.text.format.DateFormat.format("dd-MM-yyyy - hh:mm", new Date(mlDestMs)).toString();
		msStartLat = new DecimalFormat("##.###").format(mdStartLat);
		msStartLon = new DecimalFormat("##.###").format(mdStartLon);
		msDestLat = new DecimalFormat("##.###").format(mdDestLat);
		msDestLon = new DecimalFormat("##.###").format(mdDestLon);
		insertContent();

	}
	public void insertContent() {
		mRowValues = new ContentValues();

		mRowValues.put("vehicletype", moFareParams.msVehicleType);
		mRowValues.put("vehicleno", msVehicleNo);
		mRowValues.put("startlat", Double.toString(mdStartLat));
		mRowValues.put("startlon", Double.toString(mdStartLon));
		mRowValues.put("destlat", Double.toString(mdDestLat));
		mRowValues.put("destlon", Double.toString(mdDestLon));
		mRowValues.put("startms", Long.toString(mlStartMs));
		mRowValues.put("destms", Long.toString(mlDestMs));

		mRowValues.put("vehiclemeter", Double.toString(mdVehicleMeter));
		mRowValues.put("actualdistance", Integer.toString(miMeterDistance));
		mRowValues.put("phonedistance", Integer.toString(miPhoneDist));
		mRowValues.put("estimateddistance", Integer.toString(miEstimatedDist));
		mRowValues.put("startaddr", msStartAddr);
		mRowValues.put("destaddr", msDestAddr);

	}

	public void updateContent() {
		mRowValues = new ContentValues();

		mRowValues.put("vehicleno", msVehicleNo);
		mRowValues.put("vehiclemeter", Double.toString(mdVehicleMeter));
		mRowValues.put("farecharged", Double.toString(mdFareCharged));
		mRowValues.put("actualdistance", Integer.toString(miMeterDistance));
		if(!TextUtils.isEmpty(msStartAddr))
			mRowValues.put("startaddr", msStartAddr);
		if(!TextUtils.isEmpty(msDestAddr))
			mRowValues.put("destaddr", msDestAddr);
		if(miEstimatedDist >= 0)
			mRowValues.put("estimateddistance", Integer.toString(miEstimatedDist));
	}

}
