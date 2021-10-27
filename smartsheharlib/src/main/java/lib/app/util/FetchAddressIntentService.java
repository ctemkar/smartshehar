package lib.app.util;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import smartsheharcom.www.smartsheharlib.R;


public class FetchAddressIntentService extends IntentService {

    private static final String TAG = "FetchAddrISvc";

    /**
     * The receiver where results are forwarded from this service.
     */
    protected ResultReceiver mReceiver;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public FetchAddressIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    /**
     * Tries to get the location address using a Geocoder. If successful, sends
     * an address to a result receiver. If unsuccessful, sends an error message
     * instead. Note: We define a {@link android.os.ResultReceiver} in *
     * MainActivity to process content sent from this service.
     * <p/>
     * This service calls this method from the default worker thread with the
     * intent that started the service. When this method returns, the service
     * automatically stops.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        mReceiver = intent.getParcelableExtra(Constants_lib_ss.RECEIVER);

        // Check if receiver was properly registered.
        if (mReceiver == null) {
            Log.wtf(TAG,
                    "No receiver received. There is nowhere to send the results.");
            return;
        }

        // Get the location passed to this service through an extra.
        Location location = intent
                .getParcelableExtra(Constants_lib_ss.LOCATION_DATA_EXTRA);

        // Make sure that the location data was really sent over through an
        // extra. If it wasn't,
        // send an error error message and return.
        if (location == null) {
            errorMessage = getString(R.string.no_location_data_provided);
            Log.wtf(TAG, errorMessage);
            deliverResultToReceiver(Constants_lib_ss.FAILURE_RESULT, errorMessage);
            return;
        }

        // Errors could still arise from using the Geocoder (for example, if
        // there is no
        // connectivity, or if the Geocoder is given illegal location data). Or,
        // the Geocoder may
        // simply not have an address for a location. In all these cases, we
        // communicate with the
        // receiver using a resultCode indicating failure. If an address is
        // found, we use a
        // resultCode indicating success.

        // The Geocoder used in this sample. The Geocoder's responses are
        // localized for the given
        // Locale, which represents a specific geographical or linguistic
        // region. Locales are used
        // to alter the presentation of information such as numbers or dates to
        // suit the conventions
        // in the region they describe.
//        Locale locale = new Locale("hi");
//        Locale.setDefault(locale);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> addresses = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the
            // area immediately
            // surrounding the given latitude and longitude. The results are a
            // best guess and are
            // not guaranteed to be accurate.
            addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);

        } catch (IOException ioException) { // Catch network or other I/O

            geoCode(location.getLatitude(), location.getLongitude());

            errorMessage = getString(R.string.service_not_available);
          //  SSLog.e(TAG, "onHandleIntent - " + errorMessage, ioException);

        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = getString(R.string.invalid_lat_long_used);
            Log.e(TAG,
                    errorMessage + ". " + "Latitude = "
                            + location.getLatitude() + ", Longitude = "
                            + location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants_lib_ss.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using {@code getAddressLine},
            // join them, and send them to the thread. The {@link
            // android.location.address}
            // class provides other options for fetching address details that
            // you may prefer
            // to use. Here are some examples:
            // getLocality() ("Mountain View", for example)
            // getAdminArea() ("CA", for example)
            // getPostalCode() ("94043", for example)
            // getCountryCode() ("US", for example)
            // getCountryName() ("United States", for example)
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            if (address.getCountryCode().equals("IN")) {
                addressFragments.clear();
                String sSubThoroughFare = TextUtils.isEmpty(address.getSubThoroughfare()) ? "" : address.getSubThoroughfare() + ", ";
                String sThoroughFare = TextUtils.isEmpty(address.getThoroughfare()) ? "" : address.getThoroughfare() + ", ";
                String sLocality = TextUtils.isEmpty(address.getLocality()) ? "" : address.getLocality() + ", ";
                String sAdminArea = TextUtils.isEmpty(address.getAdminArea()) ? "" : address.getAdminArea() + ", ";
                String sCountry = TextUtils.isEmpty(address.getCountryName()) ? "" : address.getCountryName();
                if (TextUtils.isEmpty(sLocality)) {
                    geoCode(location.getLatitude(), location.getLongitude());
                } else {
                    addressFragments.add(sSubThoroughFare + sThoroughFare + sLocality + sAdminArea + sCountry);
                }


            }

            Log.i(TAG, getString(R.string.address_found));
            deliverResultToReceiver(Constants_lib_ss.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }
    }

    /**
     * Sends a resultCode and message to the receiver.
     */
    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants_lib_ss.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

    private void geoCode(double lat, double lng) {

        String query ;

        query = "http://maps.google.com/maps/api/geocode/json?latlng=" + lat
                + "," + lng + "&sensor=false";

        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(query);

        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }

                try {
                    JSONObject result = new JSONObject(stringBuilder.toString());
                    System.out.println("GeoCodeResult:-   " + result);

                    JSONArray array = (JSONArray) result.get("results");

                    String address = array.getJSONObject(0).getString(
                            "formatted_address");

                    deliverResultToReceiver(Constants_lib_ss.SUCCESS_RESULT,
                            address);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}