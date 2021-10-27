package com.smartshehar.android.app;

// Lines - WR, CR, etc.
public class CDirection {
	public String msStation, msDirectionCode, msMergeId;
	public int iLineId, miTowardsStationId;
	
	public CDirection(String station, String dir, int lineid, int towardstationid, String mrg) {
		msStation = station;
		msDirectionCode = dir;
		iLineId = lineid;
		miTowardsStationId = towardstationid;
		msMergeId = mrg;
	}
}
