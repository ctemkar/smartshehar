package in.bestbus.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.smartshehar.dashboard.app.R;
import com.smartshehar.dashboard.app.SSLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import in.bestbus.app.ui.ActBusRoute;
import lib.app.util.CGlobals_lib_ss;
import lib.app.util.SSLog_SS;

/**
 * Created by jijo_soumen on 20/01/2016.
 */
public class ActFindBus_adapter extends BaseAdapter {

    private static ArrayList<CBus> cBusArrayList;
    private static String TAG = ActFindBus_adapter.class.getSimpleName();
    Context context;
    String buseta = "";
    CBus cBus;
    int iCurrentStopCode;
    double busCurrentLat, busCurrentLng, speed;
    String busCLable, tripstatus, sDirection;
    int fromStopSerial, toStopSerial;
    double iMyDistanceUp, iMyDistanceDown;
    boolean isClickButton = false;
    private LayoutInflater mInflater;
    ArrayList<CSpeed> aDriverSpeed = new ArrayList<CSpeed>();
    String sLastAccess = "";

    public ActFindBus_adapter(Context context, ArrayList<CBus> results, int iCStopCode) {
        cBusArrayList = results;
        mInflater = LayoutInflater.from(context);
        this.context = context;
        iCurrentStopCode = iCStopCode;

    }

    public int getCount() {
        return cBusArrayList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.busrowupdn, parent, false);
            holder.tvBusNo = (TextView) convertView.findViewById(R.id.tvBusNo);
            holder.llBusNo = (LinearLayout) convertView.findViewById(R.id.llBusNo);
            holder.rlUp = (LinearLayout) convertView.findViewById(R.id.rlUp);
            holder.rlDn = (LinearLayout) convertView.findViewById(R.id.rlDn);
            holder.llEtaBusDown = (LinearLayout) convertView.findViewById(R.id.llEtaBusDown);
            holder.llEtaBusUp = (LinearLayout) convertView.findViewById(R.id.llEtaBusUp);
            holder.tvFrequencyUp = (TextView) convertView.findViewById(R.id.tvFreqUp);
            holder.tvMin = (TextView) convertView.findViewById(R.id.tvMin);
            holder.tvRouteUp = (TextView) convertView.findViewById(R.id.tvRouteUp);
            holder.tvRouteDn = (TextView) convertView.findViewById(R.id.tvRouteDn);
            holder.tvEtaUp = (TextView) convertView.findViewById(R.id.tvEtaUp);
            holder.tvEtaDn = (TextView) convertView.findViewById(R.id.tvEtaDn);
            holder.tvAcTimeUp = (TextView) convertView.findViewById(R.id.tvAcTimeUp);
            holder.tvAcTimeDn = (TextView) convertView.findViewById(R.id.tvAcTimeDn);
            holder.tvEtaBusNo = (ImageView) convertView.findViewById(R.id.tvEtaBusNo);
            holder.tvEtaBusNoUpDown = (ImageView) convertView.findViewById(R.id.tvEtaBusNoUpDown);
            holder.tvMap = (ImageView) convertView.findViewById(R.id.tvMap);
            holder.tvMap1 = (ImageView) convertView.findViewById(R.id.tvMap1);
            holder.llHidelistText = (LinearLayout) convertView.findViewById(R.id.llHidelistText);
            holder.llMAinlistText = (LinearLayout) convertView.findViewById(R.id.llMAinlistText);
            holder.tvEtaBusNo.setTag(position);
            holder.tvEtaBusNoUpDown.setTag(position);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
        if (cBusArrayList.size() <= 0) {
            holder.llHidelistText.setVisibility(View.VISIBLE);
            holder.llMAinlistText.setVisibility(View.GONE);
        } else {
            holder.llHidelistText.setVisibility(View.GONE);
            holder.llMAinlistText.setVisibility(View.VISIBLE);
            cBus = null;
            try {
                cBus = (CBus) cBusArrayList.get(position);
            } catch (Exception e) {
                holder.llHidelistText.setVisibility(View.VISIBLE);
                holder.llMAinlistText.setVisibility(View.GONE);
                SSLog.e(TAG, "GetView:-", e);
                return null;
            }
            holder.tvBusNo.setText(cBus.msBusLabel);
            holder.tvBusNo.setTextColor(cBus.busColor);
            holder.tvBusNo.setTypeface(CGlobals_BA.getInstance().typefaceHindi, Typeface.NORMAL);
            holder.tvBusNo.setText(cBus.msBusLabel + "\n" + cBus.msBusNoDevanagari);
            holder.tvBusNo.setContentDescription(cBus.msBusLabel);
            holder.tvRouteUp.setText(cBus.msLastStop);
            holder.tvRouteDn.setText(cBus.msFirstStop);
            holder.tvRouteUp.setContentDescription(cBus.msLastStop);
            holder.tvRouteDn.setContentDescription(cBus.msFirstStop);
            holder.tvFrequencyUp.setText("");
            if (!TextUtils.isEmpty(cBus.msFrequencyUp)) {
                holder.tvFrequencyUp.setText(cBus.msFrequencyUp);
                holder.tvFrequencyUp.setContentDescription(cBus.msFrequencyUp);
                holder.tvFrequencyUp.setVisibility(View.VISIBLE);
                holder.tvMin.setVisibility(View.VISIBLE);
            }else{
                holder.tvFrequencyUp.setVisibility(View.GONE);
                holder.tvMin.setVisibility(View.GONE);
            }
            holder.tvEtaUp.setText("");
            if (!TextUtils.isEmpty(cBus.msEtaUp)) {
                holder.tvEtaUp.setText(cBus.msEtaUp);
                holder.tvEtaUp.setContentDescription(cBus.msEtaUp);
                holder.tvEtaUp.setVisibility(View.VISIBLE);
                holder.llEtaBusUp.setVisibility(View.VISIBLE);
            }
            holder.tvEtaDn.setText("");
            if (!TextUtils.isEmpty(cBus.msEtaDn)) {
                holder.tvEtaDn.setText(cBus.msEtaDn);
                holder.tvEtaDn.setContentDescription(cBus.msEtaDn);
                holder.tvEtaDn.setVisibility(View.VISIBLE);
                holder.llEtaBusDown.setVisibility(View.VISIBLE);
            }
            holder.tvAcTimeUp.setText("");
            if (!TextUtils.isEmpty(cBus.msAcTimeUp)) {
                holder.tvAcTimeUp.setVisibility(View.VISIBLE);
                holder.tvAcTimeUp.setText(cBus.msAcTimeUp);
                holder.tvAcTimeUp.setContentDescription(cBus.msAcTimeUp);
            }
            holder.tvAcTimeDn.setText("");
            if (!TextUtils.isEmpty(cBus.msAcTimeDn)) {
                holder.tvAcTimeDn.setVisibility(View.VISIBLE);
                holder.tvAcTimeDn.setText(cBus.msAcTimeDn);
                holder.tvAcTimeDn.setContentDescription(cBus.msAcTimeDn);
            }
            holder.rlUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    cBus = null;
                    cBus = (CBus) cBusArrayList.get(position);
                    String selectedBus = "Getting route for: " + cBus.msBusLabel;
                    Toast.makeText(context,
                            selectedBus, Toast.LENGTH_SHORT).show();
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        public void run() {
                            CGlobals_BA.getInstance().mCallHome.userPing(context.getString(R.string.atTITrip),
                                    cBus.msBusLabel);
                        }
                    });
                    CGlobals_BA.getInstance().msRouteCode = cBus.msRouteCode;
                    CGlobals_BA.getInstance().msBusNo = cBus.msBusLabel;
                    CGlobals_BA.getInstance().mdStopLat = cBus.mdLatu;
                    CGlobals_BA.getInstance().mdStopLon = cBus.mdLonu;
                    CGlobals_BA.getInstance().msBusDirection = "U";
                    Toast.makeText(
                            context.getApplicationContext(),
                            "Showing bus route - Up direction", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, ActBusRoute.class);
                    if (intent != null) {
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    }
                }
            });
            holder.llBusNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    cBus = null;
                    cBus = (CBus) cBusArrayList.get(position);
                    String selectedBus = "Getting route for: " + cBus.msBusLabel;
                    Toast.makeText(context,
                            selectedBus, Toast.LENGTH_SHORT).show();
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        public void run() {
                            CGlobals_BA.getInstance().mCallHome.userPing(context.getString(R.string.atTITrip),
                                    cBus.msBusLabel);
                        }
                    });
                    CGlobals_BA.getInstance().msRouteCode = cBus.msRouteCode;
                    CGlobals_BA.getInstance().msBusNo = cBus.msBusLabel;
                    CGlobals_BA.getInstance().mdStopLat = cBus.mdLatu;
                    CGlobals_BA.getInstance().mdStopLon = cBus.mdLonu;
                    CGlobals_BA.getInstance().msBusDirection = "U";
                    Toast.makeText(
                            context.getApplicationContext(),
                            "Showing bus route - Up direction", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, ActBusRoute.class);
                    if (intent != null) {
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    }
                }
            });

            /*busUpEta(cBus.msBusLabel, holder.tvEtaUp, iCurrentStopCode, context);
            busDownEta(cBus.msBusLabel, holder.tvEtaDn, iCurrentStopCode, context);*/
            if (cBus.getMyDistanceBusDOWN() != 0.0) {
                holder.tvEtaDn.setText(addSpeed(cBus.getSpeed(), cBus.getLastAccess(), cBus.getMyDistanceBusDOWN()) + "\n" +
                        CGlobals_BA.getInstance().getDistanceText((float) cBus.getMyDistanceBusDOWN() * 1000));
                holder.tvEtaBusNoUpDown.setVisibility(View.VISIBLE);
                holder.llEtaBusDown.setVisibility(View.VISIBLE);
            }
            if (cBus.getMyDistanceBusUP() != 0.0) {
                holder.tvEtaUp.setText(addSpeed(cBus.getSpeed(), cBus.getLastAccess(), cBus.getMyDistanceBusUP()) + "\n" +
                        CGlobals_BA.getInstance().getDistanceText((float) cBus.getMyDistanceBusUP() * 1000));
                holder.tvEtaBusNo.setVisibility(View.VISIBLE);
                holder.llEtaBusUp.setVisibility(View.VISIBLE);
            }
            holder.tvEtaBusNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cBus = null;
                    cBus = (CBus) cBusArrayList.get(position);
                    isClickButton = true;
                    buseta = cBus.msBusLabel;
                    busUpEta(buseta, holder.tvEtaUp, iCurrentStopCode, context);
                }
            });

            holder.tvEtaBusNoUpDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cBus = null;
                    cBus = (CBus) cBusArrayList.get(position);
                    isClickButton = true;
                    buseta = cBus.msBusLabel;
                    busDownEta(buseta, holder.tvEtaDn, iCurrentStopCode, context);
                }
            });

            holder.rlDn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    cBus = null;
                    cBus = (CBus) cBusArrayList.get(position);
                    String selectedBus = "Getting route for: " + cBus.msBusLabel;
                    Toast.makeText(context,
                            selectedBus, Toast.LENGTH_SHORT).show();
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        public void run() {
                            CGlobals_BA.getInstance().mCallHome.userPing(context.getString(R.string.atTITrip),
                                    cBus.msBusLabel);
                        }
                    });
                    CGlobals_BA.getInstance().msRouteCode = cBus.msRouteCode;
                    CGlobals_BA.getInstance().msBusNo = cBus.msBusLabel;
                    CGlobals_BA.getInstance().mdStopLat = cBus.mdLatd;
                    CGlobals_BA.getInstance().mdStopLon = cBus.mdLond;
                    CGlobals_BA.getInstance().msBusDirection = "D";
                    Toast.makeText(
                            context.getApplicationContext(),
                            "Showing bus route - Down direction", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, ActBusRoute.class);
                    if (intent != null) {
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                    }
                }
            });
        }
        return convertView;
    }

    static class ViewHolder {
        TextView tvFrequencyUp, tvRouteUp, tvRouteDn, tvBusNo,
                tvEtaUp, tvEtaDn, tvAcTimeUp, tvAcTimeDn, tvMin;
        LinearLayout llBusNo, llHidelistText, llMAinlistText;
        LinearLayout rlUp, rlDn, llEtaBusDown, llEtaBusUp;
        ImageView tvEtaBusNo, tvEtaBusNoUpDown;
        ImageView tvMap, tvMap1;
    }

    private String addSpeed(double speed, String lastAccess, double dDistanceFromMe) {
        Calendar now = Calendar.getInstance();
        double dTotalSpeed = 0.0, dAvgSpeed = 0.0;
        int i, j = 0, eTA = 0, eTA1 = 0, eTA2 = 0;
        CSpeed cSpeed = new CSpeed(speed, lastAccess);
        aDriverSpeed.add(cSpeed);
        Date lastDriverLocationTime = aDriverSpeed.get(aDriverSpeed.size() - 1).getTime();
        Date firstDriverLocationTime = aDriverSpeed.get(0).getTime();
        long dif = (lastDriverLocationTime.getTime() - firstDriverLocationTime.getTime()) / 1000;
        // if (dif >= 20) {
        for (i = aDriverSpeed.size() - 1; i > 0; i--) {
            if (aDriverSpeed.get(i).getSpeed() > 0.83) {
                dTotalSpeed += aDriverSpeed.get(i).getSpeed();
                j++;
            }
        }
        dAvgSpeed = (dTotalSpeed / j) * 3.6;
        Log.d("AVGSpeed: ", String.valueOf(dAvgSpeed));
        if (dDistanceFromMe > 0) {
            eTA1 = (int) ((dDistanceFromMe / dAvgSpeed) * 60);
            if (eTA1 < 1) {
                eTA2 = (int) (((dDistanceFromMe / dAvgSpeed) * 60) * 60);
            } else {
                eTA = eTA1;
            }
        }
        // }
        now.add(Calendar.MINUTE, eTA);
        now.add(Calendar.SECOND, eTA2);
        String newTimeString = String.format("%1$tH:%1$tM", now);
        return newTimeString; // calculate time
    }

    private void busUpEta(final String sBuseta, final TextView tVEtaBusUp, final int iCurrentStopCodeA,
                          final Context mContext) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_bus.GET_BUS_LOCATION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        if (TextUtils.isEmpty(response.trim())) {
                            Toast.makeText(mContext, "No ETA", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (response.trim().equals("-1")) {
                            Toast.makeText(mContext, "No ETA", Toast.LENGTH_LONG).show();
                            return;
                        }

                        try {
                            JSONArray aJson = new JSONArray(response.trim());
                            for (int i = 0; i < aJson.length(); i++) {
                                JSONObject person = (JSONObject) aJson.get(i);
                                busCurrentLat = person.isNull("lat") ? -999 : person
                                        .getDouble("lat");
                                busCurrentLng = person.isNull("lng") ? -999 : person
                                        .getDouble("lng");
                                busCLable = person.isNull("buslabel") ? "" : person
                                        .getString("buslabel");
                                fromStopSerial = person.isNull("fromstopserial") ? 0 : person
                                        .getInt("fromstopserial");
                                toStopSerial = person.isNull("tostopserial") ? 0 : person
                                        .getInt("tostopserial");
                                tripstatus = person.isNull("tripstatus") ? "" : person
                                        .getString("tripstatus");

                                speed = person.isNull("speed") ? -999 : person
                                        .getDouble("speed");
                                sLastAccess = person
                                        .isNull("clientaccessdatetime") ? ""
                                        : person
                                        .getString("clientaccessdatetime");
                                sDirection = person.isNull("direction") ? "" : person.getString("direction");
                                try {
                                    if (tripstatus.equals("B")) {
                                        if (sDirection.equals("U")) {
                                            iMyDistanceUp = CGlobals_BA.getInstance().mDBHelperBus.
                                                    getNearCurrentBusStopsUp(busCurrentLat,
                                                            busCurrentLng, busCLable,
                                                            fromStopSerial, toStopSerial, iCurrentStopCodeA, "U");
                                            if (iMyDistanceUp == 0.0) {
                                                Toast.makeText(mContext, "No ETA", Toast.LENGTH_LONG).show();
                                            } else {
                                                tVEtaBusUp.setText(addSpeed(speed, sLastAccess, iMyDistanceUp) + "\n" +
                                                        CGlobals_BA.getInstance().getDistanceText((float) iMyDistanceUp * 1000));
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(mContext, "No ETA", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(mContext, "No ETA", Toast.LENGTH_LONG).show();
                            SSLog_SS.e(TAG + " busLocation", e.getMessage());
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(mContext, "Connection failed. Please try again", Toast.LENGTH_LONG).show();
                try {
                    SSLog_SS.e(TAG + " callBusTrip", error.toString());
                } catch (Exception e) {
                    SSLog_SS.e(TAG + " callBusTrip", e.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("buslabel", sBuseta);
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        Constants_bus.GET_BUS_LOCATION_URL, mContext);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_bus.GET_BUS_LOCATION_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog_SS.e(TAG + " callBusTrip", e.getMessage());
                }
                return CGlobals_BA.getInstance().checkParams(params);
            }
        };
        CGlobals_BA.getInstance().getRequestQueue(mContext).add(postRequest);
    }

    private void busDownEta(final String sBuseta, final TextView tVEtaBusDown, final int iCurrentStopCodeA,
                            final Context mContext) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_bus.GET_BUS_LOCATION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("succ" + response.toString());
                        if (TextUtils.isEmpty(response.trim())) {
                            Toast.makeText(mContext, "No ETA", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (response.trim().equals("-1")) {
                            Toast.makeText(mContext, "No ETA", Toast.LENGTH_LONG).show();
                            return;
                        }

                        try {
                            JSONArray aJson = new JSONArray(response.trim());
                            for (int i = 0; i < aJson.length(); i++) {
                                JSONObject person = (JSONObject) aJson.get(i);
                                busCurrentLat = person.isNull("lat") ? -999 : person
                                        .getDouble("lat");
                                busCurrentLng = person.isNull("lng") ? -999 : person
                                        .getDouble("lng");
                                busCLable = person.isNull("buslabel") ? "" : person
                                        .getString("buslabel");
                                fromStopSerial = person.isNull("fromstopserial") ? 0 : person
                                        .getInt("fromstopserial");
                                toStopSerial = person.isNull("tostopserial") ? 0 : person
                                        .getInt("tostopserial");
                                tripstatus = person.isNull("tripstatus") ? "" : person
                                        .getString("tripstatus");
                                speed = person.isNull("speed") ? -999 : person
                                        .getDouble("speed");
                                sLastAccess = person
                                        .isNull("clientaccessdatetime") ? ""
                                        : person
                                        .getString("clientaccessdatetime");
                                try {
                                    if (tripstatus.equals("B")) {
                                        if (sDirection.equals("D")) {
                                            iMyDistanceDown = CGlobals_BA.getInstance().mDBHelperBus.
                                                    getNearCurrentBusStopsUp(busCurrentLat,
                                                            busCurrentLng, busCLable,
                                                            fromStopSerial, toStopSerial, iCurrentStopCodeA, "D");
                                            if (iMyDistanceDown == 0.0) {
                                                Toast.makeText(mContext, "No ETA", Toast.LENGTH_LONG).show();
                                            } else {
                                                tVEtaBusDown.setText(addSpeed(speed, sLastAccess, iMyDistanceDown) + "\n" +
                                                        CGlobals_BA.getInstance().getDistanceText((float) iMyDistanceDown * 1000));
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(mContext, "No ETA", Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            Toast.makeText(mContext, "No ETA", Toast.LENGTH_LONG).show();
                            SSLog_SS.e(TAG + " busLocation", e.getMessage());
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(mContext, "Connection failed. Please try again", Toast.LENGTH_LONG).show();
                try {
                    SSLog_SS.e(TAG + " callBusTrip", error.toString());
                } catch (Exception e) {
                    SSLog_SS.e(TAG + " callBusTrip", e.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Put Value Using HashMap
                Map<String, String> params = new HashMap<String, String>();
                params.put("buslabel", sBuseta);
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        Constants_bus.GET_BUS_LOCATION_URL, mContext);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim + entry.getKey() + "="
                            + entry.getValue());
                    delim = "&";
                }
                String url1 = Constants_bus.GET_BUS_LOCATION_URL;
                try {
                    String url = url1 + "?" + getParams.toString()
                            + "&verbose=Y";
                    System.out.println("url  " + url);

                } catch (Exception e) {
                    SSLog_SS.e(TAG + " callBusTrip", e.getMessage());
                }
                return CGlobals_BA.getInstance().checkParams(params);
            }
        };
        CGlobals_BA.getInstance().getRequestQueue(mContext).add(postRequest);
    }
}