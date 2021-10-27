package com.smartshehar.busdriver_apk;

import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;

import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.LocationService;
import com.jumpinjumpout.apk.lib.SSLog;
import com.smartshehar.busdriver_apk.ui.BusDriverMap_act;


public class LocationService_bus extends LocationService {

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
            if (!CGlobals_lib.getInstance().isInTrip(LocationService_bus.this)) {
                stopSelf();
                CGlobal_bd.getInstance().sendTripStatus(
                        Constants_bd.TRIP_END, LocationService_bus.this);
            } else {
                sendMissingLocations();
                sendUpdate(mCurrentLocation);
            }

            if (CGlobal_bd.getInstance().checkConnected(LocationService_bus.this)) {

                if (mCurrentLocation != null) {
                    CGlobal_bd.getInstance().hasTripEnded(mCurrentLocation,
                            toLat, toLng,LocationService_bus.this);
                }
                if (CGlobals_lib.getInstance().isInTrip(LocationService_bus.this)) {
                    isRunningrunnableUpdatePosition = false;
                    handler.postDelayed(runnableUpdatePosition,
                            Constants_bd.DRIVER_UPDATE_INTERVAL);
                }
            } else {
                if (CGlobals_lib.getInstance().isInTrip(LocationService_bus.this)) {
                    // Since net has failed keep on trying
                    isRunningrunnableUpdatePosition = false;
                    handler.postDelayed(runnableUpdatePosition,
                            Constants_bd.DRIVER_UPDATE_INTERVAL);
                }
            }
        } catch (Exception e) {
            SSLog.e(TAG, "runnableUpdatePosition", e);
            isRunningrunnableUpdatePosition = false;
            handler.postDelayed(runnableUpdatePosition,
                    Constants_bd.DRIVER_UPDATE_INTERVAL);
        }
    }

    @Override
    protected void runCheckNotification() {
    }

    public String sendUpdatePositionUrl() {
        return null;
    }

    public String passengerProspectsUrl() {
        return null;
    }

    public String sendMissingLocationsUrl() {
        return null;
    }

    public int inTripNotificationImage() {
        return R.mipmap.ic_launcher;
    }

    public void sendUpdatePosition(Location location) {

        CGlobal_bd.getInstance().sendUpdatePosition(LocationService_bus.this);

    }

    protected Intent getViewIntentClass() {
        return new Intent(this, BusDriverMap_act.class);
    }

    @Override
    protected String servicetext() {
        return "SmartShehar Driver";
    }
}
