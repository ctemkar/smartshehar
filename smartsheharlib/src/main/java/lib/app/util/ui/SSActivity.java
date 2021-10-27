package lib.app.util.ui;


import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;


public abstract class SSActivity extends AppCompatActivity {

	LocationManager locationManagerNetwork;
	LocationManager locationManagerGPS;
	LocationManager locationManagerWiFi;

	LocationListener locationListener;
	LocationListener locationListenerGPS;
	LocationListener locationListenerWiFi;

	protected static boolean mbAutoLocation;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
	public abstract void updateMyLocation(Location location);
        

	// Setup location listeners
	public void setupLocationListeners() {
		locationManagerNetwork = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManagerGPS = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//		locationManagerWiFi = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				updateMyLocation(location);
			}
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
			public void onProviderEnabled(String provider) {
			}
			public void onProviderDisabled(String provider) {
			}
		};
		locationListenerGPS = new LocationListener() {
			public void onLocationChanged(Location location) {
				updateMyLocation(location);
			}
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
			public void onProviderEnabled(String provider) {
			}
			public void onProviderDisabled(String provider) {
			}
		};
		locationListenerWiFi = new LocationListener() {
			public void onLocationChanged(Location location) {
				updateMyLocation(location);
			}
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
			public void onProviderEnabled(String provider) {
			}
			public void onProviderDisabled(String provider) {
			}
		};
	}


	public void startLocationListeners() {
		stopLocationListeners();

			try {
				if (ActivityCompat.checkSelfPermission(SSActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
						!= PackageManager.PERMISSION_GRANTED &&
						ActivityCompat.checkSelfPermission(SSActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
								!= PackageManager.PERMISSION_GRANTED) {
					return;
				}
            locationManagerNetwork.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 3500, 10,
                    locationListener);
            } catch (Exception e) {
                //SSLog.e("SSActivity: ","startLocationListeners", e.getMessage());
            }
			try {
            locationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    3500, 10, locationListenerGPS);
        } catch (Exception e) {
//            SSLog.e("SSActivity: ","startLocationListeners", e.getMessage());
        }

		// locationManagerWiFi.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
		// 3500, 10, locationListenerWiFi);
	}

	public void stopLocationListeners() {
        if (ActivityCompat.checkSelfPermission(SSActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(SSActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
			if (locationManagerNetwork != null && locationListener != null)
                locationManagerNetwork.removeUpdates(locationListener);
			if (locationManagerGPS != null && locationListenerGPS != null)
                locationManagerGPS.removeUpdates(locationListener);
			if (locationManagerWiFi != null && locationListenerWiFi != null)
                locationManagerWiFi.removeUpdates(locationListener);

	}
	@Override
	protected void onPause() {
		// Unregister the LocationListener to stop updating the
		// GUI when the Activity isn't visible.
		// locationManager.removeUpdates(locationListener);

        if (ActivityCompat.checkSelfPermission(SSActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(SSActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
			if (locationManagerNetwork != null && locationListener != null)
                locationManagerNetwork.removeUpdates(locationListener);
			if (locationManagerGPS != null && locationListenerGPS != null)
                locationManagerGPS.removeUpdates(locationListenerGPS);
			if (locationManagerWiFi != null && locationListenerWiFi != null)
                locationManagerWiFi.removeUpdates(locationListenerWiFi);


		super.onPause();
	} // onPause

	@Override
	protected void onResume() {
		startLocationListeners();
		super.onResume();
	}	
	public Location getLocation() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    // Define the criteria how to select the locatioin provider -> use
	    // default
	    Criteria criteria = new Criteria();
	    String provider = locationManager.getBestProvider(criteria, false);

		Location location = null;
        if (ActivityCompat.checkSelfPermission(SSActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(SSActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
			location = locationManager.getLastKnownLocation(provider);


		// Initialize the location fields
	    if (location != null) {
	      System.out.println("Provider " + provider + " has been selected.");
	    } 		
	    return location;
	}


} // SSActivity





