package com.jumpinjumpout.apk.driver;

import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;

import com.jumpinjumpout.apk.driver.ui.ForHireSharedTrip_act;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.LocationService;

import com.jumpinjumpout.apk.lib.SSLog;

public class LocationService_cab extends LocationService {

    protected Handler handler = new Handler();
    private static final String TAG = "LocationService";
    double toLat, toLng;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    } // onCreate

    public int getAppLabelId() {
        return R.string.app_label;
    }

    public void runUpdatePosition() {
        try {
            if (!CGlobals_lib.getInstance().isInTrip(LocationService_cab.this)) {
                stopSelf();
                CGlobals_driver.getInstance().sendTripAction(
                        Constants_driver.TRIP_ACTION_END,LocationService_cab.this);
            } else {
                sendMissingLocations();
                sendUpdate(mCurrentLocation);
            }

            if (CGlobals_driver.getInstance().checkConnected(LocationService_cab.this)) {

                if (mCurrentLocation != null) {
                    CGlobals_driver.getInstance().hasTripEnded(mCurrentLocation,
                            toLat, toLng,LocationService_cab.this);
                }
                if (CGlobals_lib.getInstance().isInTrip(LocationService_cab.this)) {
                    isRunningrunnableUpdatePosition = false;
                    handler.postDelayed(runnableUpdatePosition,
                            Constants_driver.DRIVER_UPDATE_INTERVAL);
                }
            } else {
                if (CGlobals_lib.getInstance().isInTrip(LocationService_cab.this)) {
                    // Since net has failed keep on trying
                    isRunningrunnableUpdatePosition = false;
                    handler.postDelayed(runnableUpdatePosition,
                            Constants_driver.DRIVER_UPDATE_INTERVAL);
                }
            }
        } catch (Exception e) {
            SSLog.e(TAG, "runnableUpdatePosition", e);
            isRunningrunnableUpdatePosition = false;
            handler.postDelayed(runnableUpdatePosition,
                    Constants_driver.DRIVER_UPDATE_INTERVAL);
        }
    }

    @Override
    protected void runCheckNotification() {
        if(CGlobals_lib.getInstance().isNewNotification == true){
            getProspects();
        }
    }

    public String sendUpdatePositionUrl() {
        return Constants_driver.UPDATE_POSITON_DRIVER_URL;
    }

    public String passengerProspectsUrl() {
        return Constants_driver.PASSENGER_PROSPECTS_DRIVER_URL;
    }

    public String sendMissingLocationsUrl() {
        return Constants_driver.MISSING_LOCATION_URL;
    }

    public int inTripNotificationImage() {
        return R.mipmap.intrip_notification;
    }

    public void sendUpdatePosition(Location location) {

        CGlobals_driver.getInstance().sendUpdatePosition(location, LocationService_cab.this);
    }

    protected Intent getViewIntentClass() {
        return new Intent(this, ForHireSharedTrip_act.class);
    }

    @Override
    protected String servicetext() {
        return "Jump.in.Jump.out Driver";
    }
}
