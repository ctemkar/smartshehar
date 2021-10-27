package com.jumpinjumpout.apk.lib.ui;

/**
 * Created by jijo_soumen on 06/01/2016.
 */
public class MarathiAddress_act extends SearchAddress_act {

    /*protected static final String TAG = "MarathiAddress_act: ";
    // ListView lvMarathilist;
    private ArrayList<CAddress> maogetCITY = new ArrayList<CAddress>();
    private ArrayList<CAddress> maoScheduleTrips = new ArrayList<CAddress>();
    CAddress cAddress;
    protected ProgressDialog mProgressDialog;
    String msCity_Town_Id, msCity_Town, msCity_Town_Mar, msCityorTown;
    double mdCityTown_Mar_lat, mdCityTown_Mar_lng;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.marathiaddress_act);
        // lvMarathilist = (ListView) findViewById(R.id.lvMarathilist);
        if (isMarathiAddress) {
            llHome.setVisibility(View.GONE);
            llWork.setVisibility(View.GONE);
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Getting address...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            getRecentMarathiTrip();

            addressListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent();
                    intent.putExtra("street_address", maogetCITY.get(position).getCity_Town());
                    intent.putExtra("lat", maogetCITY.get(position).getCityTown_Mar_lat());
                    intent.putExtra("lng", maogetCITY.get(position).getCityTown_Mar_lng());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            });
        }
    }

    public void getRecentMarathiTrip() {
        progressMessage("Getting address...");
        StringRequest postRequest = new StringRequest(Request.Method.POST,
                Constants_lib.GET_CITY_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        getMarathiAddress(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressCancel();
                String sErr = CGlobals_lib.getInstance().getVolleyError(error);
                Toast.makeText(MarathiAddress_act.this, sErr, Toast.LENGTH_SHORT).show();
                try {
                    SSLog.d(TAG,
                            "Failed to getRecentMarathiTrip :-  "
                                    + error.getMessage());
                } catch (Exception e) {
                    SSLog.e(TAG, "getRecentMarathiTrip - " + sErr,
                            error.getMessage());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params = CGlobals_lib.getInstance().getBasicMobileParams(params,
                        Constants_lib.GET_CITY_URL, MarathiAddress_act.this);
                return CGlobals_lib.getInstance().checkParams(params);
            }
        };
        CGlobals_lib.getInstance().addVolleyRequest(postRequest, false, MarathiAddress_act.this);
    } // getRecentMarathiTrip

    public void getMarathiAddress(String response) {
        if (TextUtils.isEmpty(response)) {
            progressCancel();
            return;
        }
        if (response.equals("-1")) {
            progressCancel();
            return;
        }
        try {
            JSONArray aJson = new JSONArray(response);
            for (int i = 0; i < aJson.length(); i++) {
                JSONObject person = (JSONObject) aJson.get(i);
                msCity_Town_Id = isNullNotDefined(person, "city_town_id") ? ""
                        : person.getString("city_town_id");

                msCity_Town = isNullNotDefined(person, "city_town") ? ""
                        : person.getString("city_town");

                msCity_Town_Mar = isNullNotDefined(person, "city_town_mar") ? ""
                        : person.getString("city_town_mar");

                msCityorTown = isNullNotDefined(person, "cityortown") ? ""
                        : person.getString("cityortown");

                mdCityTown_Mar_lat = isNullNotDefined(person, "lat") ? Constants_lib.INVALIDLAT : person
                        .getDouble("lat");

                mdCityTown_Mar_lng = isNullNotDefined(person, "lng") ? Constants_lib.INVALIDLAT : person
                        .getDouble("lng");
                cAddress = new CAddress();
                cAddress.setCity_Town_Id(msCity_Town_Id);
                cAddress.setCity_Town(msCity_Town);
                cAddress.setCity_Town_Mar(msCity_Town_Mar);
                cAddress.setCityorTown(msCityorTown);
                cAddress.setCityTown_Mar_lat(mdCityTown_Mar_lat);
                cAddress.setCityTown_Mar_lng(mdCityTown_Mar_lng);
                maogetCITY.add(cAddress);
            }
            maoScheduleTrips = CGlobals_lib.getInstance().readRecentAddresses(MarathiAddress_act.this);
            if (maoScheduleTrips.size() > 0) {
                for (int j = 0; j < maoScheduleTrips.size(); j++) {
                    maogetCITY.add(j, maoScheduleTrips.get(j));
                }
            }
            //addressListView.setAdapter(new MarathiAddress_adapter(MarathiAddress_act.this, maogetCITY));
            addressListAdapter = new AddressListArrayAdapter(
                    MarathiAddress_act.this, maogetCITY,
                    MarathiAddress_act.this, cc,isMarathiAddress);

            addressListView.setAdapter(addressListAdapter);
            InputMethodManager keyboard = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(editsearch.getWindowToken(), 0);
            progressCancel();
        } catch (Exception e) {
            progressCancel();
            SSLog.e(TAG, "getMarathiAddress - ",
                    e.getMessage());
        }
    }// getMarathiAddress

    protected void progressMessage(String msg) {
        if (!isFinishing()) {
            mProgressDialog.setMessage(msg);
            mProgressDialog.show();
        }
    }

    protected void progressCancel() {
        if (mProgressDialog != null) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.cancel();
            }
        }

    }

    public boolean isNullNotDefined(JSONObject jo, String jkey) {

        if (!jo.has(jkey)) {
            return true;
        }
        if (jo.isNull(jkey)) {
            return true;
        }
        return false;

    }*/
}
