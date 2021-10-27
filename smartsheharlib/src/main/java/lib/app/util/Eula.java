package lib.app.util;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;


public class Eula {
    private Activity mActivity;
	private String appTitle;
	private String eula;
	private String updates;
	
	public Eula(Activity context, String appttl, String e, String upd) {
		mActivity = context; 
		appTitle = appttl;
		eula = e;
		updates = upd;
	}
	
	private PackageInfo getPackageInfo() {
        PackageInfo pi = null;
        try {
             pi = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi; 
    }

     public void show() {
        PackageInfo versionInfo = getPackageInfo();

        // the eulaKey changes every time you increment the version number in the AndroidManifest.xml
         String EULA_PREFIX = "eula_";
         final String eulaKey = EULA_PREFIX + versionInfo.versionCode;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        boolean hasBeenShown = prefs.getBoolean(eulaKey, false);
        if(!hasBeenShown){

        	// Show the Eula
            final String title = appTitle + " v" + versionInfo.versionName;
            
            //Includes the updates as well so users know what changed. 
            String message =  eula;

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Mark this version as read.
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(eulaKey, true);
                            editor.apply();
                           dialogInterface.dismiss();
                            String message2 = updates;
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(mActivity)
                            .setTitle(title + ": updates")
                            .setMessage(message2)
                            .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    
                                }
                            });
                            builder2.create().show();
                                                       
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Close the activity as they have declined the EULA
							mActivity.finish(); 
						}
                    	
                    });
            builder.create().show();
            
        }
    }

}

