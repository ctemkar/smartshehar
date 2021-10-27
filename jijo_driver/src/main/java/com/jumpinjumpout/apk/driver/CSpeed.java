package com.jumpinjumpout.apk.driver;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by user pc on 14-08-2015.
 */
public class CSpeed {
    double dSpeed = 0;
    String sLastTime = "";


    public CSpeed(double speed, String lastTime) {
        dSpeed = speed;
        sLastTime = lastTime;
    }

    public double getSpeed() {
        return dSpeed;
    }

    public Date getTime() {
        Date date = null;
        try {
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            date = sdf.parse(sLastTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

}