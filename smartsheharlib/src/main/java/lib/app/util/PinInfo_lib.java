package lib.app.util;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;


public class PinInfo_lib {
	String sTitle, sSnippet;
	JSONObject jPersonInfo;
	int iResource;
	LatLng oLatLng;
	String sOverlayText;
	public PinInfo_lib(LatLng latlng, String title, String snippet, int resource, String overlaytext,
					   JSONObject jpersoninfo) {
		oLatLng = latlng; sTitle = title; sSnippet = snippet;
		iResource = resource; sOverlayText = overlaytext;
		jPersonInfo = jpersoninfo;
	}
	public LatLng getLatLng() { return oLatLng; }
	public String getTitle() { return sTitle; }
	public String getSnippet() { return sSnippet; }
	public int getResource() { return iResource; }
}
