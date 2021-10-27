package com.smartshehar.busdriver_apk;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jijo_soumen on 27/01/2016.
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
