package lib.app.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.telephony.TelephonyManager;

public class UserInfo {
    private static final String TAG = "UserInfo: ";
    public String msProduct;
	public String msManufacturer;
	public String msCarrier;
	public String mAppNameCode;

	public static String mIMEI;
	public static String mGmail = null;
	public static String mLine1Number;
	public Location mFakeLocation = null;
	
	public UserInfo(Context activity, String appNameShort) {
	try {

		AccountManager manager = (AccountManager) activity.getSystemService(Context.ACCOUNT_SERVICE);
		Account[] list = manager.getAccounts();

		for(Account account: list)
		{
			if(account.type.equalsIgnoreCase("com.google"))
			{
				mGmail = account.name;
				break;
			}
		}
	 	TelephonyManager telephonyManager = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);
	 	mAppNameCode = appNameShort;
	 	mIMEI = telephonyManager.getDeviceId();
	 	msCarrier = telephonyManager.getNetworkOperator();
	 	mLine1Number = telephonyManager.getLine1Number();
	 	mLine1Number = telephonyManager.getSubscriberId();
	 	msProduct = Build.PRODUCT;
	 	msManufacturer = Build.MANUFACTURER;

		mFakeLocation = new Location("fake");	// Dadar - Tilak Bridge
		double lat = 19.02078;
		double lon = 72.843168;
		mFakeLocation.setLatitude(lat);
		mFakeLocation.setLongitude(lon);
	} catch (Exception e) {
		SSLog_SS.e(TAG, "UserInfo(Context activity, String appNameShort) - ", e, activity.getApplicationContext());
	}

		
	 	
	}


}
