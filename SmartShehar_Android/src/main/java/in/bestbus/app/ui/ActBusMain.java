package in.bestbus.app.ui;

import com.smartshehar.dashboard.app.R;

import in.bestbus.app.CGlobals_BA;
import lib.app.util.AppRater;
import lib.app.util.Eula;
import lib.app.util.UpgradeApp;


public class ActBusMain extends AbstractActivity {
    /*
     * (non-Javadoc)
     * @see org.example.actionbarsherlock.list.navigation.activity.AbstractActivity#getContentLayoutId()
     */
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onResume() {
//		SSApp mApp = (SSApp) this.getApplication();
        CGlobals_BA mApp = CGlobals_BA.getInstance();

        AppRater.app_launched(this, getString(R.string.appTitle), mApp.mPackageInfo.packageName);
        UpgradeApp.app_launched(this, mApp.mPackageInfo, mApp.mUserInfo,
                getString(R.string.appTitle),
                getString(R.string.appCode), mApp.mPackageInfo.versionCode,
                CGlobals_BA.PHP_PATH);
        new Eula(this, getString(R.string.appNameShort),
                getString(R.string.eula), getString(R.string.updates)).show();

        super.onResume();
    }
}
