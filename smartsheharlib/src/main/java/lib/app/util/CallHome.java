package lib.app.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpResponse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class CallHome {
	private static final String TAG = "Callhome: ";
    private Location location;
	private Double lastLat = -99.0, lastLon = -99.0;
	private String lastModule = "", msLastContent = "";
    private Context activity;
    private String msModule = "", msContent = "";
    private DBHelperLocal dbHelper;
    private String appCode;
    private String lastClientMs = "";
	public CallHome(Context act, Location loc, PackageInfo packageinfo, String appnameshort,
			String appcode, UserInfo uinfo, DBHelperLocal localdb) {
        activity = act;
        location = loc;
        appCode = appcode;
        dbHelper = localdb;

	}
    public CallHome(Context act, Location loc, PackageInfo packageinfo, String appnameshort,
                    String appcode, UserInfo uinfo) {
        activity = act;
        location = loc;
        appCode = appcode;


    }

	
	// Log first use
    public void userPing(final String module, final String sContent) {
        try {
            msModule = TextUtils.isEmpty(module) ? "" : module;
            msContent = TextUtils.isEmpty(sContent) ? "" : sContent;

            final boolean bIsConnected = Connectivity.isConnected(activity);
//		final String version = mApp.mVersionInfo.versionName;
            Calendar cal =  Calendar.getInstance();
            if(location == null) {
                if(lastModule.equals(module) && msLastContent.equals(msContent))
                    return;
            } else {
                if (msLastContent.equals(msContent)) {
                    if((Double.parseDouble(new DecimalFormat("###.#####").format(location.getLatitude()))
                            == Double.parseDouble(new DecimalFormat("###.#####").format(lastLat))
                            && Double.parseDouble(new DecimalFormat("###.#####").format(location.getLongitude()))
                            == Double.parseDouble(new DecimalFormat("###.#####").format(lastLon))
                            && lastModule.equals(module)) || Long.toString(cal.getTimeInMillis()).equals(lastClientMs)
                            )
                        return;
                }
            }
            msLastContent = msContent;
            lastModule = module;
            if(location != null) {
                lastLat =  location.getLatitude();
                lastLon = location.getLongitude();
            }

            Thread t = new Thread() {
                public void run() {
                    try	{
                        // First write to local db in case we lose connection
                        String clientdatetime, locationdatetime;
                        Date date;
                        Format format = new SimpleDateFormat(Constants_lib_ss.STANDARD_DATE_FORMAT);
                        double dLat = -1, dLon = -1;
                        float fAccuracy = -1;
                        long lLocationTime = -1;
                        String sProvider = "-";
                        if(location != null) {
                            dLat = location.getLatitude();
                            dLon = location.getLongitude();
                            fAccuracy = location.getAccuracy();
                            lLocationTime = location.getTime();
                            sProvider = location.getProvider();
                        }
                        int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(activity).getInt(
                                Constants_lib_ss.PREF_APPUSERID, -1);
                        int appusageid = CGlobals_lib_ss.getInstance().getPersistentPreference(activity).getInt(
                                Constants_lib_ss.PREF_APPUSAGEID, -1);
                        dbHelper.writeDBCallHome(msModule, msContent, Double.toString(dLat),
                                Double.toString(dLon), Float.toString(fAccuracy),
                                Long.toString(lLocationTime), sProvider);
                        if(bIsConnected) {
                            try {
                                Cursor curs = dbHelper.getDBCallHome();
                                String dt, tm, clientms, email, imei, module, lat, lon, acc, locationtime,
                                        provider, version, carrier, product, manufacturer, content;

                                if(curs != null) {
                                    curs.moveToFirst();
                                    for(curs.moveToFirst(); !curs.isAfterLast(); curs.moveToNext()) {

                                        clientms = curs.getString(curs.getColumnIndexOrThrow("clientms"));
                                        date = new Date(Long.valueOf(clientms));
                                        clientdatetime=  format.format(date);
                                        lastClientMs = clientms;
                                        email = curs.getString(curs.getColumnIndexOrThrow("email"));
                                        imei = curs.getString(curs.getColumnIndexOrThrow("imei"));
                                        dt = curs.getString(curs.getColumnIndexOrThrow("dt"));
                                        tm = curs.getString(curs.getColumnIndexOrThrow("tm"));
                                        module = curs.getString(curs.getColumnIndexOrThrow("module"));
                                        lat = curs.getString(curs.getColumnIndexOrThrow("lat"));
                                        lon = curs.getString(curs.getColumnIndexOrThrow("lon"));
                                        acc = curs.getString(curs.getColumnIndexOrThrow("accuracy"));
                                        locationtime = curs.getString(curs.getColumnIndexOrThrow("locationtime"));
                                        if (!locationtime.equalsIgnoreCase("0")) {
                                            date = new Date(Long.valueOf(locationtime));
                                            locationdatetime=  format.format(date);
                                        } else {
                                            locationdatetime=null;
                                        }
                                        provider = curs.getString(curs.getColumnIndexOrThrow("provider"));
                                        locationtime = curs.getString(curs.getColumnIndexOrThrow("locationtime"));
                                        version = curs.getString(curs.getColumnIndexOrThrow("version"));
                                        carrier = curs.getString(curs.getColumnIndexOrThrow("carrier"));
                                        product = curs.getString(curs.getColumnIndexOrThrow("product"));
                                        manufacturer = curs.getString(curs.getColumnIndexOrThrow("manufacturer"));
                                        content = curs.getString(curs.getColumnIndexOrThrow("content"));
                                        if(TextUtils.isEmpty(module)||TextUtils.isEmpty(content))
                                            content = module+content;

//                                        locationdatetime;

                                        Uri uri = new Uri.Builder()
                                                .scheme("http")
                                                .authority(CGlobals_lib_ss.AUTHORITY)
                                                .path(Constants_lib_ss.ADD_CALL_HOME_URL)
                                                .appendQueryParameter("date", dt)
                                                .appendQueryParameter("tm", tm)
                                                .appendQueryParameter("clientdatetime", clientdatetime)
                                                .appendQueryParameter("email", email)
                                                .appendQueryParameter("imei", imei)
                                                .appendQueryParameter("module", module)
                                                .appendQueryParameter("lat", lat)
                                                .appendQueryParameter("lng", lon)
                                                .appendQueryParameter("accuracy", acc)
                                                .appendQueryParameter("locationdatetime", locationdatetime)
                                                .appendQueryParameter("provider", provider)
                                                .appendQueryParameter("version", version)
                                                .appendQueryParameter("app", appCode)
                                                .appendQueryParameter("carrier", carrier)
                                                .appendQueryParameter("product", product)
                                                .appendQueryParameter("manufacturer", manufacturer)
                                                .appendQueryParameter("content", content)
                                                .appendQueryParameter("appuserid", String.valueOf(appuserid))
                                                .appendQueryParameter("appusageid", String.valueOf(appusageid))
                                                .build();
                                        URL url = new URL(uri.toString());
                                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                                        try {
                                            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                                            @SuppressWarnings("unused")
                                            String result= convertStreamToString(in);
                                            Log.d(TAG,"RESULT");
//								dbHelper.deleteCallHomeLocal(callHomeId);
                                            //	                      readStream(in);
                                        } catch (Exception e) {
                                            Log.e("CallHome", "io error", e);
                                        } finally {
                                            urlConnection.disconnect();
                                            dbHelper.DBClearCallHome();
                                        }
                                    }
                                    try {
                                        curs.close();
                                        curs = null;
                                    } catch (Exception e) {
                                      //  SSLog.e(TAG , " userping - ", e);
                                    }
                                }
                                @SuppressWarnings("unused")
                                HttpResponse response;
                                // Making HTTP Request
                                try {
                                } catch (Exception e) {
                                    Log.e("CallHome", "io error", e);
                                }
                            } catch (Exception e) {
                                Log.e("CallHome", "io error", e);
                            } finally  { // Wrote local db to server
                                dbHelper.DBClearCallHome();
                            }
                        } else { 	// if bConnected > 0
                            // Not connected - write to local db

                        }
                    } catch (Exception e)	{
                       // SSLog.e(TAG ,"userping - ", e);
                    }
                }
            };
            t.start();
        } catch (Exception e) {
            //SSLog.e(TAG,"userPing",e);
        }

    } // saveStopLatLon



    private static String convertStreamToString(InputStream is) {
/*
 * To convert the InputStream to String we use the BufferedReader.readLine()
 * method. We iterate until the BufferedReader return null which means
 * there's no more data to read. Each line will appended to a StringBuilder
 * and returned as String.
 */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }




} // CallHome

