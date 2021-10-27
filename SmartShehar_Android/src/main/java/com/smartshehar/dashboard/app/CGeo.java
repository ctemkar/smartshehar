package com.smartshehar.dashboard.app;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.util.List;
import java.util.Locale;

public class CGeo {
	public class CAddr {
		public List<Address> addresses;
		public String firstAddressString;
	}
	Activity mActivity;
	public CAddr mAddr;
	public CGeo(Activity activity) {
		mActivity = activity;
		mAddr = new CAddr();
	}
	public boolean getAddress(Location location) {
		if(location == null)
			return false;
		try {
			Geocoder geo = new Geocoder(mActivity.getApplicationContext(), Locale.getDefault());
		    mAddr.addresses = geo.getFromLocation(location.getLatitude(), 
		    		location.getLongitude(), 1);
	        if (mAddr.addresses.isEmpty()) {
	        	return false;
	        } else if (mAddr.addresses.size() > 0) {
	            	mAddr.firstAddressString = mAddr.addresses.get(0).getAddressLine(0);
	            	mAddr.firstAddressString += ", " + mAddr.addresses.get(0).getLocality() +", " + 
	            	mAddr.addresses.get(0).getAdminArea() + ", " + mAddr.addresses.get(0).getCountryName();
					return true;
	        }
	    } catch (Exception e) {
	    	e.printStackTrace(); // getFromLocation() may sometimes fail
	    }
		return false;
	}

}
