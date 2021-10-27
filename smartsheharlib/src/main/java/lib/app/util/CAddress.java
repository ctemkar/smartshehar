package lib.app.util;

import android.location.Address;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class CAddress extends Address {

	protected static final String TAG = "CAddress: ";
	public static final Parcelable.Creator<CAddress> CREATOR = null;
	String msArea ="", msAdminArea ="", msCountryCode ="", msCountryName ="", msExtras ="",
			msFeatureName ="", msLocality ="", msPhone ="", msPostalCode ="", msSubAdminArea ="",
			msSubLocality1 ="", msSubLocality2 ="", msAdminstraveArea1 ="",
			msAdminstrativeArea2 ="", msNeighborhodd ="";

	public String getMsNeighborhodd() {
		return msNeighborhodd;
	}

	public void setMsNeighborhodd(String msNeighborhodd) {
		this.msNeighborhodd = msNeighborhodd;
	}

	// private LatLng latLng = null;
	public String msCity, msCountry;
	private String msStreetNumber;
	private String msRoute;
	private String msShortAddress="";

	private String msPlaceId;
    private String msReference;

	public CAddress() {
		super(Locale.getDefault());
	}

	public String getPlaceId() {
		return msPlaceId;
	}

	public void setPlaceId(String msPlaceId) {
		this.msPlaceId = msPlaceId;
	}

    public void setReference(String msReference) {
        this.msReference = msReference;
    }

    public String getReference() {
        return msReference;
    }

	public CAddress(String a1, String a2, double lat, double lng) {
		super(Locale.getDefault());
		setAddressLine(0, a1);
		setAddressLine(1, a2);
		setLatitude(lat);
		setLongitude(lng);
	}

    public CAddress(String a1, double lat, double lng) {
        super(Locale.getDefault());
        setAddress(a1);
        setLatitude(lat);
        setLongitude(lng);
    }

	public CAddress(Address addr) {
		super(Locale.getDefault());
		int i = 0;
		while (i < 5) {
			String addrLine = addr.getAddressLine(i);
			if (TextUtils.isEmpty(addrLine)) {
				break;
			}
			setAddressLine(i, addrLine);
			i++;
		}
		msAdminArea = addr.getAdminArea();
		msCountryCode = addr.getCountryCode();
		msCountryName = addr.getCountryName();
		msExtras = String.valueOf(addr.getExtras());
		msFeatureName = addr.getFeatureName();
		msLocality = addr.getLocality();
		msPhone = addr.getPhone();
		msPostalCode = addr.getPostalCode();
		msSubAdminArea = addr.getSubAdminArea();
		msSubLocality1 = addr.getSubLocality();
		msSubLocality2 = addr.getSubLocality();
		msAdminstraveArea1 = addr.getSubAdminArea();
		msStreetNumber = addr.getSubThoroughfare();
		setLongitude(addr.getLongitude());
		setLatitude(addr.getLatitude());
	}

	public CAddress(String address) {
		super(Locale.getDefault());
		setAddressLine(0, address);
	}

	public CAddress(JSONObject oAddress) {
		super(Locale.getDefault());
		try {
			setAddress(oAddress.getString("address"));
			setLatitude(oAddress.isNull("lat") ? Constants_lib_ss.INVALIDLAT
					: oAddress.getDouble("lat"));
			setLongitude(oAddress.isNull("lng") ? Constants_lib_ss.INVALIDLAT
					: oAddress.getDouble("lng"));
		} catch (JSONException e) {

			// SSLog.e(TAG, "CTrip", e);
		}
	}

	public String getAddressLine0() {
		return getAddressLine(0);
	}

	public void setAddressLine0(String sAddressLine1) {
		setAddressLine(0, sAddressLine1);
		;
	}

	public String getAddressLine1() {
		return getAddressLine(1);
	}

	public void setAddressLine1(String sAddressLine) {
		setAddressLine(1, sAddressLine);
	}

	public String getShortAddress() {
		return msShortAddress;
	}

	public void setShortAddress(String sShortAddress) {
		msShortAddress = sShortAddress;
	}

	public String getArea() {
		return msArea;
	}

	public String getAdminArea() {
		return msAdminArea;
	}

	public void setAdminArea(String msAdminArea) {
		this.msAdminArea = msAdminArea;
	}

	public String getCountryCode() {
		return msCountryCode;
	}

	public void setCountryCode(String msCountryCode) {
		this.msCountryCode = msCountryCode;
	}

	public String getCountryName() {
		return msCountryName;
	}

	public void setCountryName(String msCountryName) {
		this.msCountryName = msCountryName;
	}


	public String getFeatureName() {
		return msFeatureName;
	}

	public void setFeatureName(String msFeatureName) {
		this.msFeatureName = msFeatureName;
	}

	public String getLocality() {
		return msLocality;
	}

	public void setLocality(String msLocality) {
		this.msLocality = msLocality;
	}

	public String getPhone() {
		return msPhone;
	}

	public void setPhone(String msPhone) {
		this.msPhone = msPhone;
	}

	public String getStreetNumber() {
		return msStreetNumber;
	}

	public void setStreetNumber(String sStreetNumber) {
		this.msStreetNumber = sStreetNumber;
	}

	public String getRoute() {
		return msRoute;
	}

	public void setRoute(String sRoute) {
		this.msRoute = sRoute;
	}

	public String getPostalCode() {
		return msPostalCode;
	}

	public void setPostalCode(String msPostalCode) {
		this.msPostalCode = msPostalCode;
	}

	public String getSubAdminArea() {
		return msSubAdminArea;
	}

	public void setSubAdminArea(String msSubAdminArea) {
		this.msSubAdminArea = msSubAdminArea;
	}

	public String getSubLocality1() {
		return msSubLocality1;
	}

	public void setSubLocality1(String msSubLocality1) {
		this.msSubLocality1 = msSubLocality1;
	}

	public String getSubLocality2() {
		return msSubLocality2;
	}

	public void setSubLocality2(String msSubLocality2) {
		this.msSubLocality2 = msSubLocality2;
	}

	public String getAdminstraveArea1() {
		return msAdminstraveArea1;
	}

	public void setAdminstraveArea1(String msAdminstraveArea1) {
		this.msAdminstraveArea1 = msAdminstraveArea1;
	}

	public String getAdminstrativeArea2() {
		return msAdminstrativeArea2;
	}

	public void setAdminstrativeArea2(String msAdminstrativeArea2) {
		this.msAdminstrativeArea2 = msAdminstrativeArea2;
	}


	// public LatLng getLatLng() {
	// return latLng;
	// }
	//
	// public void setLatLng(LatLng latlng) {
	// this.latLng = latlng;
	// }

	public String getCity() {
		return msCity;
	}

	public void setCity(String msCity) {
		this.msCity = msCity;
	}

	public String getCountry() {
		return msCountry;
	}

	public void setCountry(String msCountry) {
		this.msCountry = msCountry;
	}

	public void setCountrySpecificComponents() {
		this.msArea = (!TextUtils.isEmpty(msSubLocality2) ? msSubLocality2
				+ ", " : "")
				+ (!TextUtils.isEmpty(msSubLocality1) ? msSubLocality1 : "");
		this.msShortAddress = (!TextUtils.isEmpty(msStreetNumber) ? msStreetNumber
				+ " "
				: "")
				+ (!TextUtils.isEmpty(msRoute) ? msRoute + " " : "");

		if (TextUtils.isEmpty(msShortAddress)) {
			msShortAddress = getAddressLine(1);
		} else {
			msShortAddress += this.msArea;
		}

	}

	public void setCountrySpecificComponents(String address) {
		this.msArea = (!TextUtils.isEmpty(msSubLocality2) ? msSubLocality2
				+ ", " : "")
				+ (!TextUtils.isEmpty(msSubLocality1) ? msSubLocality1 : "");
		this.msShortAddress = (!TextUtils.isEmpty(msStreetNumber) ? msStreetNumber
				+ " "
				: "")
				+ (!TextUtils.isEmpty(msRoute) ? msRoute + " " : "");

		if (TextUtils.isEmpty(msShortAddress)) {
			msShortAddress = getAddressLine(1);
		} else {
			msShortAddress += this.msArea;
		}
		if (TextUtils.isEmpty(msRoute) || TextUtils.isEmpty(msSubLocality1)
				|| TextUtils.isEmpty(msSubLocality2)
				|| TextUtils.isEmpty(msStreetNumber)) {
			msShortAddress = address;
		}
	}

	public void setAddress(String address) {
		setAddressLine(0, address.trim());
	}

	public String getAddress() {
		String sAddress = "";
		// if (TextUtils.isEmpty(msAddress))
		sAddress = (getAddressLine(0) == null ? "" : getAddressLine(0) + " ");
        sAddress += (getAddressLine(1) == null ? "" : getAddressLine(1) + " ");
//		if (getAddressLine(2) != null){
//			sAddress = getAddressLine(2);
//		}
//				+ (getAddressLine(2) == null ? "" : getAddressLine(2));
		// }
		return sAddress;

	}

	public JSONObject toJSon() {
		JSONObject obj = new JSONObject();
		try {
			if (TextUtils.isEmpty(getAddress()) && (!hasLongitude() || !hasLatitude())) {
				return null;
			} else {
				obj.put("address", getAddress());
				obj.put("lat", getLatitude());
				obj.put("lng", getLongitude());
			}

		} catch (JSONException e) {
			//SSLog.e(TAG, " toJson - ", e);
		}
		return obj;

	}

}
