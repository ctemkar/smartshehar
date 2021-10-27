package lib.app.util;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class GeoHelper {
    Context mContext;
    Location location;
    private GeoHelperResult geoHelperResult;

    public boolean getAddress(Context context, Location location, GeoHelperResult geoHelperResult) {
        this.mContext = context;
        this.geoHelperResult = geoHelperResult;
        this.location = location;
        getAddress();
        return true;

    }

    private CAddress getAddress() {
        return fetchCityNameUsingGoogleMap();
    }

    private CAddress fetchCityNameUsingGoogleMap() {

        CAddress addr = null;
        String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + location.getLatitude() + "," + location.getLongitude() + "&sensor=false&language=en";
        StringRequest myReq = new StringRequest(Request.Method.GET, googleMapUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            VolleyLog.v("Response:%n %s", response);
                            parseAddress(response);
//                            parsePlaceDetails(response, addr);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                parseAddress("-1");
            }
        });
        CGlobals_lib_ss.getInstance().addVolleyRequest(myReq, false, mContext);
        return addr;
    }

    private CAddress parseAddress(String response) {
        CAddress addr = null;
        JSONObject jsonObject;
        String cityName = "", postalCode = "", route = "", neighborhood = "", administrative_area_level_2 = "", administrative_area_level_1 = "";
        String address = "", sublocality = "";
        double lat = -999, lng = -999;

        try {
            jsonObject = new JSONObject(response);
            JSONArray results = (JSONArray) jsonObject.get("results");
            addr = new CAddress();

            for (int i = 0; i < results.length(); i++) {
                // loop among all addresses within this result
                JSONObject result = results.getJSONObject(i);
                if (result.has("formatted_address")) {
                    if (TextUtils.isEmpty(address)) {
                        address = result.getString("formatted_address");
                        String TAG = "GeoHelper";
                        Log.d(TAG, "address is --> " + address);
                    }
                }
                if (result.has("geometry")) {
                    JSONObject jObj = result.getJSONObject("geometry");
                    JSONObject locJob = jObj.getJSONObject("location");
                    if (lat == -999 || lng == -999) {
                        lat = locJob.getDouble("lat");
                        lng = locJob.getDouble("lng");
                        addr.setLatitude(lat);
                        addr.setLongitude(lng);
                    }
                }
                if (result.has("address_components")) {
                    JSONArray addressComponents = result.getJSONArray("address_components");
                    // loop among all address component to find a 'locality' or 'sublocality'
                    for (int j = 0; j < addressComponents.length(); j++) {
                        JSONObject addressComponent = addressComponents.getJSONObject(j);
                        if (result.has("types")) {
                            JSONArray types = addressComponent.getJSONArray("types");

                            // search for locality and sublocality
//                        String cityName = null;

                            for (int k = 0; k < types.length(); k++) {
                                if ("locality".equals(types.getString(k)) && TextUtils.isEmpty(cityName)) {
                                    if (addressComponent.has("long_name")) {
                                        cityName = addressComponent.getString("long_name");
                                    } else if (addressComponent.has("short_name")) {
                                        cityName = addressComponent.getString("short_name");
                                    }

                                }
                                if ("sublocality".equals(types.getString(k)) && TextUtils.isEmpty(sublocality)) {
                                    if (addressComponent.has("long_name")) {
                                        sublocality = addressComponent.getString("long_name");
                                    } else if (addressComponent.has("short_name")) {
                                        sublocality = addressComponent.getString("short_name");
                                    }

                                }
                                if ("postal_code".equals(types.getString(k)) && TextUtils.isEmpty(postalCode)) {
                                    if (addressComponent.has("long_name")) {
                                        postalCode = addressComponent.getString("long_name");
                                    } else if (addressComponent.has("short_name")) {
                                        postalCode = addressComponent.getString("short_name");
                                    }


                                }
                                if ("route".equals(types.getString(k)) && TextUtils.isEmpty(route)) {
                                    if (addressComponent.has("long_name")) {
                                        route = addressComponent.getString("long_name");
                                    } else if (addressComponent.has("short_name")) {
                                        route = addressComponent.getString("short_name");
                                    }
                                }
                                if ("neighborhood".equals(types.getString(k)) && TextUtils.isEmpty(neighborhood)) {
                                    if (addressComponent.has("long_name")) {
                                        neighborhood = addressComponent.getString("long_name");
                                    } else if (addressComponent.has("short_name")) {
                                        neighborhood = addressComponent.getString("short_name");
                                    }


                                }
                                if ("administrative_area_level_2".equals(types.getString(k)) && TextUtils.isEmpty(administrative_area_level_2)) {
                                    if (addressComponent.has("long_name")) {
                                        administrative_area_level_2 = addressComponent.getString("long_name");
                                    } else if (addressComponent.has("short_name")) {
                                        administrative_area_level_2 = addressComponent.getString("short_name");
                                    }
                                }
                                if ("administrative_area_level_1".equals(types.getString(k)) && TextUtils.isEmpty(administrative_area_level_1)) {
                                    if (addressComponent.has("long_name")) {
                                        administrative_area_level_1 = addressComponent.getString("long_name");
                                    } else if (addressComponent.has("short_name")) {
                                        administrative_area_level_1 = addressComponent.getString("short_name");
                                    }
                                }

                            }
                            if (!TextUtils.isEmpty(address)) {
                                addr.setAddress(address);
                            }
                            if (!TextUtils.isEmpty(cityName)) {
                                addr.setCity(cityName);
                            }
                            if (!TextUtils.isEmpty(sublocality)) {
                                addr.setSubLocality1(sublocality);
                            }
                            if (!TextUtils.isEmpty(postalCode)) {
                                addr.setPostalCode(postalCode);
                            }
                            if (!TextUtils.isEmpty(route)) {
                                addr.setRoute(route);
                            }
                            if (!TextUtils.isEmpty(neighborhood)) {
                                addr.setMsNeighborhodd(neighborhood);
                            }
                            if (!TextUtils.isEmpty(administrative_area_level_2)) {
                                addr.setAdminstraveArea1(administrative_area_level_2);
                            }
                            if (!TextUtils.isEmpty(administrative_area_level_1)) {
                                addr.setAdminstraveArea1(administrative_area_level_1);
                            }

                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        geoHelperResult.gotAddress(addr);
        return addr;
    }

    public static abstract class GeoHelperResult {
        public abstract void gotAddress(CAddress addr);
    }

}
