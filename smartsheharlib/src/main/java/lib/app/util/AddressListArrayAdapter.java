package lib.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import smartsheharcom.www.smartsheharlib.R;


public class AddressListArrayAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = "AddrLstArAdptr: ";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyAZosnfKUhXDicFCkTPyMPnYM56PiJUGRw";
    Context mContext;
    LayoutInflater inflater;
    private List<CAddress> filteredData = null;
    private ArrayList<CAddress> originalData = null;
    private List<CAddress> recentAddr = null;
    private Activity mActivity;
    String cc = "in"; // default country
    String msResultHome = null, msResultWork = null;
    CAddress caHomeAddress, caWorkAddress;
    private boolean foundHome = false, foundWork = false;

    public AddressListArrayAdapter(Context context,
                                   List<CAddress> recentAddresses, Activity activity, String ccode) {
        mContext = context;
        mActivity = activity;
        this.filteredData = new ArrayList<CAddress>();
        this.originalData = new ArrayList<CAddress>();
        this.recentAddr = recentAddresses;
        this.filteredData.addAll(recentAddresses);
        if (!TextUtils.isEmpty(ccode)) {
            cc = ccode;
        }
        inflater = LayoutInflater.from(mContext);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        Type type = new TypeToken<CAddress>() {
        }.getType();

        try {
            msResultHome = pref.getString("homeAddress", "");
            caHomeAddress = new Gson().fromJson(msResultHome, type);
            msResultWork = pref.getString("workAddress", "");
            caWorkAddress = new Gson().fromJson(msResultWork, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ViewHolder {
        TextView tvAddressLine1;
        TextView tvAddressLine2;
        ImageView msTvSetAddressLogo;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.address_item, parent, false);
            // Locate the TextViews in listview_item.xml
            holder.tvAddressLine1 = (TextView) view
                    .findViewById(R.id.tvAddressLine1);
            holder.tvAddressLine2 = (TextView) view
                    .findViewById(R.id.tvAddressLine2);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tvAddressLine1.setText(filteredData.get(position)
                .getAddressLine0());
        holder.tvAddressLine2.setText(filteredData.get(position)
                .getAddressLine1());
        // Listen for ListView Item Click
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Intent returnIntent = new Intent();
                CAddress addr = filteredData.get(position);
                if (addr.getLatitude() == Constants_lib_ss.INVALIDLAT
                        || addr.getLongitude() == Constants_lib_ss.INVALIDLNG) {
                    getPlaceDetails(addr);
                } else {
                    returnWithAddress(addr);
                }
            }
        });

        return view;
    }


    private CAddress parsePlaceDetails(String response, CAddress addr) {
        double lat = Constants_lib_ss.INVALIDLAT, lng = Constants_lib_ss.INVALIDLNG;
        if (TextUtils.isEmpty(response)) {
            return null;
        }
        System.out.println("ggggg " + response);
        try {
            JSONObject person = new JSONObject(response);
            JSONObject result = person.getJSONObject("result");
            JSONObject geometry = result.getJSONObject("geometry");
            JSONObject jbLocation = geometry.getJSONObject("location");
            lat = jbLocation.getDouble("lat");
            lng = jbLocation.getDouble("lng");
            addr.setLatitude(lat);
            addr.setLongitude(lng);
        } catch (JSONException e) {
           // SSLog.e(TAG, "parsePlaceDetails", e);
        }
        return addr;
    }


    private void returnWithAddress(CAddress addr) {
        String json = new Gson().toJson(addr);
        Intent returnIntent = new Intent();
        if (addr.hasLatitude() && addr.hasLongitude()) {
            returnIntent.putExtra("add", json);
            CGlobals_lib_ss.getInstance().addRecentAddress(addr);
            CGlobals_lib_ss.getInstance().writeRecentAddresses(mContext);
        }
        mActivity.setResult(Activity.RESULT_OK, returnIntent);
        mActivity.finish();

    }

    // Filter Class
    public AddressListArrayAdapter(Context context,
                                   List<String> listDataHeader,
                                   HashMap<String, List<String>> listChildData) {
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public CAddress getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {

        return myFilter;
    }

    Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            final ArrayList<CAddress> list = originalData;
            int count = list.size();

            final ArrayList<CAddress> nlist = new ArrayList<CAddress>(count);
            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            try {
                StringBuilder sb = new StringBuilder(PLACES_API_BASE
                        + TYPE_AUTOCOMPLETE + OUT_JSON);
                sb.append("?sensor=false&key=" + API_KEY);
                sb.append("&components=country:" + cc);
                sb.append("&input="
                        + URLEncoder.encode(constraint.toString(), "utf8"));
                sb.append("&language=hi" + cc);
                URL url = new URL(sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(
                        conn.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            } catch (MalformedURLException e) {
            //    SSLog.e(TAG, "Error processing Places API URL", e);
            } catch (IOException e) {
               // SSLog.e(TAG, "Error connecting to Places API", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            try {
                // Create a JSON object hierarchy from the results
                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

                // Extract the Place descriptions from the results
                // resultList = new ArrayList<String>(predsJsonArray.length());
                for (int i = 0; i < predsJsonArray.length(); i++) {
                    nlist.add(new CAddress(predsJsonArray.getJSONObject(i)
                            .getString("description"), "",
                            Constants_lib_ss.INVALIDLAT, Constants_lib_ss.INVALIDLNG));

                    nlist.get(nlist.size() - 1).setPlaceId(
                            predsJsonArray.getJSONObject(i).getString(
                                    "place_id"));
                    nlist.get(nlist.size() - 1).setReference(
                            predsJsonArray.getJSONObject(i).getString(
                                    "reference"));
                }
            } catch (JSONException e) {
               // SSLog.e(TAG, "Cannot process JSON results", e);
            }
            filterResults.values = nlist;
            filterResults.count = nlist.size();

            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence contraint,
                                      FilterResults results) {
            filteredData = (ArrayList<CAddress>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };

    private void getPlaceDetails(final CAddress addr) {

        String URL = "https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyAZosnfKUhXDicFCkTPyMPnYM56PiJUGRw&placeid="
                + addr.getPlaceId();
        StringRequest myReq = new StringRequest(Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            VolleyLog.v("Response:%n %s", response.toString());
                            CAddress parsedAddress = parsePlaceDetails(response, addr);
                            if (parsedAddress.getLatitude() != Constants_lib_ss.INVALIDLAT &&
                                    parsedAddress.getLongitude() != Constants_lib_ss.INVALIDLNG) {
                                returnWithAddress(parsedAddress);
                            } else {
                                geoCode(addr.getAddress());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        CGlobals_lib_ss.getInstance().addVolleyRequest(myReq, false, mContext);
    }

    private void geoCode(String address) {

        LatLng latlng;
        String query;


        latlng = getLocationFromAddress(address);

        query = "http://maps.google.com/maps/api/geocode/json?address="
                + address.replaceAll(" ", "%20") + "&sensor=false";


        StringRequest myReq = new StringRequest(Method.GET, query,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            VolleyLog.v("Response:%n %s", response.toString());
                            parseAddress(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        CGlobals_lib_ss.getInstance().addVolleyRequest(myReq, false, mContext);

    }

    private void parseAddress(String response) {
        CAddress addr = null;
        JSONObject jsonObject = null;

        Double lng = Double.valueOf(Constants_lib_ss.INVALIDLAT);
        Double lat = Double.valueOf(Constants_lib_ss.INVALIDLNG);


        try {
            jsonObject = new JSONObject(response);
            addr = new CAddress();
            JSONObject joResult = ((JSONArray) jsonObject
                    .get("results")).getJSONObject(0);
            addr.setAddressLine0(joResult
                    .getString("formatted_address"));
            JSONArray addrComp = ((JSONArray) jsonObject.get("results"))
                    .getJSONObject(0)
                    .getJSONArray("address_components");
            JSONObject joAddrComp;
            String sType;
            int len = addrComp.length();
            for (int i = 0; i < len; i++) {
                joAddrComp = (JSONObject) addrComp.get(i);
                sType = joAddrComp.get("types").toString();

                if (sType.contains("street_number")) {
                    addr.setStreetNumber(joAddrComp
                            .isNull("short_name") ? "" : joAddrComp
                            .getString("short_name"));
                }
                if (sType.contains("route")) {
                    addr.setRoute(joAddrComp.isNull("short_name") ? ""
                            : joAddrComp.getString("short_name"));
                }
                if (sType.contains("postal_code")) {
                    addr.setPostalCode(joAddrComp
                            .getString("short_name"));
                }
                if (sType.contains("sublocality_level_1")) {
                    addr.setSubLocality1(joAddrComp
                            .getString("short_name"));
                }
                if (sType.contains("sublocality_level_2")) {
                    addr.setSubLocality2(joAddrComp
                            .getString("short_name"));
                }
                if (sType.contains("country")) {
                    addr.setCountryCode(joAddrComp
                            .getString("short_name"));
                }

                Log.d(TAG,
                        sType + ": "
                                + joAddrComp.getString("short_name"));

            } // process address components
            addr.setCountrySpecificComponents(joResult.getString("formatted_address"));
            String locality = ((JSONArray) ((JSONObject) addrComp
                    .get(0)).get("types")).getString(0);
            if (locality.compareTo("locality") == 0) {
                locality = ((JSONObject) addrComp.get(0))
                        .getString("long_name");
                addr.setLocality(locality);
            }
            if (locality.compareTo("locality") == 0) {
                locality = ((JSONObject) addrComp.get(0))
                        .getString("long_name");
                addr.setLocality(locality);
            }
            String adminArea = ((JSONArray) ((JSONObject) addrComp
                    .get(2)).get("types")).getString(0);
            if (adminArea.compareTo("administrative_area_level_1") == 0) {
                adminArea = ((JSONObject) addrComp.get(2))
                        .getString("long_name");
                addr.setAdminArea(adminArea);
            }
            String country = ((JSONArray) ((JSONObject) addrComp.get(3))
                    .get("types")).getString(0);
            if (country.compareTo("country") == 0) {
                country = ((JSONObject) addrComp.get(3))
                        .getString("long_name");
                addr.setCountryName(country);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {

            lng = ((JSONArray) jsonObject.get("results"))
                    .getJSONObject(0).getJSONObject("geometry")
                    .getJSONObject("location").getDouble("lng");

            lat = ((JSONArray) jsonObject.get("results"))
                    .getJSONObject(0).getJSONObject("geometry")
                    .getJSONObject("location").getDouble("lat");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        addr.setLatitude(lat);
        addr.setLongitude(lng);
        returnWithAddress(addr);
    }


    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(mContext);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (Exception e) {
          //  SSLog.e(TAG, "getLocationFromAddress", e);

        }
        return p1;
    }
}
