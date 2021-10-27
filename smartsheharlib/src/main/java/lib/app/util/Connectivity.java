package lib.app.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Connectivity {
    /*
     * HACKISH: These constants aren't yet available in my API level (7), but I need to handle these cases if they come up, on newer versions
     */
    public static final String TAG = "Connectivity";
    public static final int NETWORK_TYPE_EHRPD = 14; // Level 11
    public static final int NETWORK_TYPE_EVDO_B = 12; // Level 9
    public static final int NETWORK_TYPE_HSPAP = 15; // Level 13
    public static final int NETWORK_TYPE_IDEN = 11; // Level 8
    public static final int NETWORK_TYPE_LTE = 13; // Level 11

    public static String sConnectionErrorMessage = "Driver App requires Internet Connection to function." +
            "\n\nPlease switch Mobile Data or Wi-Fi ON using option below.";

    public static String sGPSConnectionErrorMessage = "Make sure you location is ON and Location mode is set to \'High Accuracy\' while using this app. The app automatically detects location of the offense you are trying to report." +
            "\nTurn Location ON using option below.";

    public static String sGPSConnectionBatterySaving = "Make sure you location mode is set to \'High Accuracy\' before sharing your trip." +
            "\nPlease Change your Location mode by using option below";

    public static void setConnectionErrorMessage(String sconnectionErrorMessage) {

        sConnectionErrorMessage = sconnectionErrorMessage;

    }

    public static void setGPSConnectionErrorMessage(String sgPSConnectionErrorMessage) {

        sGPSConnectionErrorMessage = sgPSConnectionErrorMessage;

    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && info.isConnectedOrConnecting());
    }

    /**
     * Check if there is fast connectivity
     *
     * @param context
     * @return
     */
    public static boolean isConnectedFast(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return (info != null && info.isConnected() && Connectivity.isConnectionFast(info.getType(), info.getSubtype()));
    }


    public static boolean checkConnected(Context context) {
        for (int i = 0; i < 3; i++) {
//			Log.i(TAG,
//					"Working... " + (i + 1) + "/5 @ "
//							+ SystemClock.elapsedRealtime());
            try {
                if (isConnected(context)) {
                    return true;
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
        return false;
    }

    public static boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            System.out.println("CONNECTED VIA WIFI");
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                // NOT AVAILABLE YET IN API LEVEL 7
                case Connectivity.NETWORK_TYPE_EHRPD:
                    return true; // ~ 1-2 Mbps
                case Connectivity.NETWORK_TYPE_EVDO_B:
                    return true; // ~ 5 Mbps
                case Connectivity.NETWORK_TYPE_HSPAP:
                    return true; // ~ 10-20 Mbps
                case Connectivity.NETWORK_TYPE_IDEN:
                    return false; // ~25 kbps
                case Connectivity.NETWORK_TYPE_LTE:
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public boolean connectionError(final Context context) {
        String settingOk = "Setting", settingCancel = "Skip";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            sConnectionErrorMessage = "Smartshehar App requires Internet Connection to function." +
                    "\n\nTurn mobile data on?";
            settingOk = "Yes";
            settingCancel = "No";

        }

        if (checkConnected(context)) {
            return false;
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("No Internet Connection");

            alertDialog.setMessage(sConnectionErrorMessage);

            alertDialog.setPositiveButton(settingOk, new DialogInterface.OnClickListener() {


                public void onClick(DialogInterface dialog, int which) {


                    try {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            setMobileDataEnabled(context, true);

                        } else {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            context.startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Please start your internet manually", Toast.LENGTH_SHORT).show();
                    }

                }

            });
            alertDialog.setNegativeButton(settingCancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {

                    dialog.cancel();
                }

            });
            alertDialog.show();

            return true;
        }
    }
    public boolean connError(Context context) {
        String settingOk = "Setting", settingCancel = "Skip";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            sConnectionErrorMessage = "Smartshehar App requires Internet Connection to function." +
                    "\n\nTurn mobile data on?";
            settingOk = "Yes";
            settingCancel = "No";

        }

        if (checkConnected(context)) {
            return false;
        } else {
            Log.d(TAG, "No internet Connection");

            return true;
        }
    }

    public boolean connectionError(final Context context, String msg) {
        String settingOk = "Setting", settingCancel = "Skip";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            settingOk = "Yes";
            settingCancel = "No";

        }

        if (checkConnected(context)) {
            return false;
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("No Internet Connection");
            alertDialog.setMessage(msg);
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton(settingOk, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {


                    try {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            setMobileDataEnabled(context, true);

                        } else {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            context.startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Please start your internet manually", Toast.LENGTH_SHORT).show();
                    }

                }

            });
            alertDialog.setNegativeButton(settingCancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    ((Activity) context).finish();
                    dialog.cancel();
                }

            });
            alertDialog.show();

            return true;
        }
    }

    public boolean connectionErrorSplashScreen(final Context context, String msg) {
        String settingOk = "Ok";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

            settingOk = "Ok";

        }

        if (checkConnected(context)) {
            return false;
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("No Internet Connection");
            alertDialog.setMessage(msg);
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton(settingOk, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {


                    try {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            setMobileDataEnabled(context, true);

                        } else {
                            Intent intent = new Intent(Settings.ACTION_SETTINGS);
                            context.startActivity(intent);
                        }
                        ((Activity) context).finish();
                        dialog.cancel();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Please start your internet manually", Toast.LENGTH_SHORT).show();
                    }

                }

            });
            alertDialog.show();

            return true;
        }
    }

    public boolean isGPSEnable(final Context context) {
        LocationManager service = (LocationManager) context.getSystemService(Activity.LOCATION_SERVICE);
        boolean enabled = service
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("Switch GPS location ON");

            alertDialog.setMessage(sGPSConnectionErrorMessage);

            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {


                public void onClick(DialogInterface dialog, int which) {

                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);
                    Toast.makeText(context, sGPSConnectionBatterySaving, Toast.LENGTH_SHORT).show();

                }

            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {

                    dialog.cancel();
                }

            });
            alertDialog.show();


            return false;
        } else {
            return true;
        }
    }

    /*private void setMobileDataEnabled(Context mContext, boolean enabled) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final ConnectivityManager conman = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        final Class conmanClass = Class.forName(conman.getClass().getName());
        final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
        connectivityManagerField.setAccessible(true);
        final Object connectivityManager = connectivityManagerField.get(conman);
        final Class connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        setMobileDataEnabledMethod.setAccessible(true);
        setMobileDataEnabledMethod.invoke(connectivityManager, enabled);
    }*/

    public static void setMobileDataEnabled(Context context, boolean enabled) {
        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
//            Toast.makeText(mContext, "Please start your internet manually.", Toast.LENGTH_SHORT).show();

        } catch (InvocationTargetException e) {
            e.printStackTrace();
//            Toast.makeText(mContext, "Please start your internet manually", Toast.LENGTH_SHORT).show();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
//            Toast.makeText(mContext, "Please start your internet manually", Toast.LENGTH_SHORT).show();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
//            Toast.makeText(mContext, "Please start your internet manually", Toast.LENGTH_SHORT).show();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
//            Toast.makeText(mContext, "Please start your internet manually", Toast.LENGTH_SHORT).show();
        }
        finally {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            context.startActivity(intent);
        }
    }


} // Connectivity
