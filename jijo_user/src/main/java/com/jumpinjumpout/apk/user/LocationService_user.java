package com.jumpinjumpout.apk.user;

import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import com.jumpinjumpout.apk.R;
import com.jumpinjumpout.apk.lib.CGlobals_lib;
import com.jumpinjumpout.apk.lib.Constants_lib;
import com.jumpinjumpout.apk.lib.LocationService;
import com.jumpinjumpout.apk.user.ui.FriendPool_act;

import com.jumpinjumpout.apk.lib.SSLog;


public class LocationService_user extends LocationService {

    private static final String TAG = "LocationService";

    @Override
    protected String servicetext() {
        return "Jump.in.Jump.out";
    }

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

    protected void runCheckNotification(){
        if(CGlobals_lib.getInstance().isNewNotification == true){
            getProspects();
        }
    }


    public void runUpdatePosition() {
        try {
            if (!CGlobals_lib.getInstance().isInTrip(LocationService_user.this)) {
                stopSelf();
                CGlobals_user.getInstance().sendTripAction(
                        Constants_user.TRIP_ACTION_END,this);
            } else {
                sendMissingLocations();
                sendUpdate(mCurrentLocation);
            }
            if (mCurrentLocation == null){
                mCurrentLocation = CGlobals_lib.getInstance().getMyLocation(this);
            }
            if (mCurrentLocation != null) {
                CGlobals_user.getInstance().hasTripEnded(mCurrentLocation,
                        toLat, toLng,this);
            }
            if (MyApplication.getInstance().getConnectivity().checkConnected(LocationService_user.this)) {


                if (CGlobals_lib.getInstance().isInTrip(LocationService_user.this)) {
                    isRunningrunnableUpdatePosition = false;
                    if(CGlobals_lib.getInstance().hasVolleyConnection()){
                        handler.postDelayed(runnableUpdatePosition,
                                Constants_user.DRIVER_UPDATE_INTERVAL);
                        handler.postDelayed(runnableCheckNotification, Constants_lib.DRIVER_CHECK_NOTIFICATION_USER);
                    }else{
                        handler.postDelayed(runnableUpdatePosition,
                                Constants_lib.ONE_SEC_INTERVAL);
                        handler.postDelayed(runnableCheckNotification, Constants_lib.DRIVER_CHECK_NOTIFICATION_USER);
                    }
                }
            } else {
                if (CGlobals_lib.getInstance().isInTrip(LocationService_user.this)) {
                    // Since net has failed keep on trying
                    isRunningrunnableUpdatePosition = false;
                    handler.postDelayed(runnableUpdatePosition,
                            Constants_user.DRIVER_UPDATE_INTERVAL);
                    handler.postDelayed(runnableCheckNotification, Constants_lib.DRIVER_CHECK_NOTIFICATION_USER);
                }
            }
        } catch (Exception e) {
            SSLog.e(TAG, "runnableUpdatePosition", e);
            isRunningrunnableUpdatePosition = false;
            handler.postDelayed(runnableUpdatePosition,
                    Constants_user.DRIVER_UPDATE_INTERVAL);
            handler.postDelayed(runnableCheckNotification, Constants_lib.DRIVER_CHECK_NOTIFICATION_USER);
        }
    }

    public String sendUpdatePositionUrl() {
        return Constants_user.UPDATE_POSITION_URL;
    }

    public String passengerProspectsUrl() {
        return Constants_user.PASSENGER_PROSPECTS_URL;
    }

    public String sendMissingLocationsUrl() {
        return Constants_user.MISSING_LOCATION_URL;
    }

    public int inTripNotificationImage() {
        return R.mipmap.intrip_notification;
    }

    public void sendUpdatePosition(Location location) {

        CGlobals_user.getInstance().sendUpdatePosition(location, LocationService_user.this);
    }

    protected Intent getViewIntentClass() {
        return new Intent(this, FriendPool_act.class);
    }


}
