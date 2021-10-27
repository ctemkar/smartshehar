package com.smartshehar.cabe.driver;

import android.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.smartshehar.cabe.driver.ui.CabEDriverForHire_act;
import com.smartshehar.cabe.driver.ui.CabEMainDriver_act;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lib.app.util.CGlobals_lib_ss;
import lib.app.util.Constants_lib_ss;
import lib.app.util.SSLog_SS;


public class PassengerAdapter extends BaseAdapter {
    private static String TAG = "PassengerAdapter";
    private ArrayList<CTrip_CED> aPassengerList;
    Context context;
    private int iJumpIn, iJumpOut;

    public PassengerAdapter(Context c, ArrayList<CTrip_CED> d) {
        this.context = c;
        aPassengerList = d;
    }

    public int getCount() {
        return aPassengerList.size();
    }

    public Object getItem(int position) {
        return aPassengerList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {

        TextView passengerName, passengerNumber, passengerGender;
        Button btnCallPassenger, btnJumpIn;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.passenger_item_list, null);
            holder = new ViewHolder();
            holder.passengerName = (TextView) convertView.findViewById(R.id.passengerName);
            holder.passengerNumber = (TextView) convertView.findViewById(R.id.passengerNumber);
            holder.passengerGender = (TextView) convertView.findViewById(R.id.passengerGender);
            holder.btnCallPassenger = (Button) convertView.findViewById(R.id.btnCallPassenger);
            holder.btnJumpIn = (Button) convertView.findViewById(R.id.btnJumpIn);

            convertView.setTag(holder);
            holder.btnCallPassenger.setTag(position);
            holder.btnJumpIn.setTag(position);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.passengerName.setText(aPassengerList.get(position).getMsFullName());
        holder.passengerNumber.setText(aPassengerList.get(position).getPhoneNo());
        holder.passengerGender.setText(aPassengerList.get(position).getGender());
        holder.btnCallPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(aPassengerList.get(position).getPhoneNo())) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + aPassengerList.get(position).getPhoneNo()));
                    if (ActivityCompat.checkSelfPermission(context,
                            android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    context.startActivity(intent);
                }
            }
        });

        if (aPassengerList.get(position).hasJumpedIn()) {
            holder.btnJumpIn.setText("Jump Out!");
            holder.btnJumpIn.setVisibility(View.VISIBLE);
        } else if (!aPassengerList.get(position).hasJumpedIn()) {
            holder.btnJumpIn.setText("Jump In!");
            holder.btnJumpIn.setVisibility(View.VISIBLE);
        } else {
            holder.btnJumpIn.setVisibility(View.GONE);
        }

        holder.btnJumpIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!aPassengerList.get(position).hasJumpedIn() && !aPassengerList.get(position).hasJumpedOut()) {
                    SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                            Locale.getDefault());
                    Calendar cal1 = Calendar.getInstance();
                    String sDateTime1 = df1.format(cal1.getTime());
                    int appuserid1 = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                            .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                    int tripid1 = CGlobals_lib_ss.getInstance().getPersistentPreference(context)
                            .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
                    Location location1 = CGlobals_lib_ss.getInstance().getMyLocation(context);
                    DatabaseHandler_CabE db = new DatabaseHandler_CabE(context);
                    db.addJumpInOutTrip(location1, appuserid1, tripid1, aPassengerList.get(position).getPassenger_Appuser_Id(),
                            CGlobals_lib_ss.msGmail, sDateTime1, "jumpin");
                    holder.btnJumpIn.setText("Jump Out!");
                    holder.btnJumpIn.setVisibility(View.VISIBLE);
                }
            }
        });
        return convertView;
    }

    /*public void callJumpInJumpOutUrl(final String sJumpIn, final int passenger_appuser_id, final Context mcontext) {
        final int tripid = CGlobals_lib_ss.getInstance().getPersistentPreference(mcontext)
                .getInt(Constants_CED.PREF_TRIP_ID_INT, -1);
        final String url = Constants_CED.JUMP_IN_URL;
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                String sDateTime = df.format(cal.getTime());
                int appuserid = CGlobals_lib_ss.getInstance().getPersistentPreference(mcontext)
                        .getInt(Constants_lib_ss.PREF_APPUSERID, -1);
                Location location = CGlobals_lib_ss.getInstance().getMyLocation(mcontext);
                DatabaseHandler_CabE db = new DatabaseHandler_CabE(mcontext);
                db.addJumpInOutTrip(location, appuserid, tripid, passenger_appuser_id,
                        CGlobals_lib_ss.msGmail, sDateTime, sJumpIn);
                SSLog_SS.e(TAG, "sendUserAccess :-   ", error, context);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tripid", String.valueOf(tripid));
                params.put("passengerappuserid", String.valueOf(passenger_appuser_id));
                if (sJumpIn.equals("jumpin")) {
                    params.put("jumpin", "1");
                }
                params = CGlobals_lib_ss.getInstance().getBasicMobileParamsShort(params,
                        url, context);
                String delim = "";
                StringBuilder getParams = new StringBuilder();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    getParams.append(delim).append(entry.getKey()).append("=").append(entry.getValue());
                    delim = "&";
                }
                String debugUrl;
                try {
                    debugUrl = url + "?" + getParams.toString() + "&verbose=Y";
                    Log.e(TAG, debugUrl);
                } catch (Exception e) {
                    SSLog_SS.e(TAG, "getPassengers map - ", e, context);
                }
                return CGlobals_lib_ss.getInstance().checkParams(params);
            }
        };
        CGlobals_lib_ss.getInstance().addVolleyRequest(postRequest, true, context);
    }*/
}