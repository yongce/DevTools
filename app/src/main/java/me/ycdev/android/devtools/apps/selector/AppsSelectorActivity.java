package me.ycdev.android.devtools.apps.selector;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.apps.common.AppInfo;
import me.ycdev.androidlib.utils.PackageUtils;

public class AppsSelectorActivity extends Activity {
    /** Type: boolean, default value: {@value #DEFAULT_MULTICHOICE} */
    public static final String EXTRA_MULTICHOICE = "extra.multichoice";
    /** Type: boolean, default value: {@value #DEFAULT_EXCLUDE_UNINSTALLED}*/
    public static final String EXTRA_EXCLUDE_UNINSTALLED = "extra.exclude_uninstalled";
    /** Type: boolean, default value: {@value #DEFAULT_EXCLUDE_DISABLED */
    public static final String EXTRA_EXCLUDE_DISABLED = "extra.exclude_disabled";
    /** Type: boolean, default value: {@value #DEFAULT_EXCLUDE_SYSTEM} */
    public static final String EXTRA_EXCLUDE_SYSTEM = "extra.exclude_system";

    private static final boolean DEFAULT_MULTICHOICE = false;
    private static final boolean DEFAULT_EXCLUDE_UNINSTALLED = true;
    private static final boolean DEFAULT_EXCLUDE_DISABLED = true;
    private static final boolean DEFAULT_EXCLUDE_SYSTEM = false;

    private boolean mMultiChoice;
    private boolean mExcludeUninstalled;
    private boolean mExcludeDisabled;
    private boolean mExcludeSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_apps_selector);

        Intent intent = getIntent();
        mMultiChoice = intent.getBooleanExtra(EXTRA_MULTICHOICE, DEFAULT_MULTICHOICE);
        mExcludeUninstalled = intent.getBooleanExtra(EXTRA_EXCLUDE_UNINSTALLED, DEFAULT_EXCLUDE_UNINSTALLED);
        mExcludeDisabled = intent.getBooleanExtra(EXTRA_EXCLUDE_DISABLED, DEFAULT_EXCLUDE_DISABLED);
        mExcludeSystem = intent.getBooleanExtra(EXTRA_EXCLUDE_SYSTEM, DEFAULT_EXCLUDE_SYSTEM);
    }

    private List<AppInfo> loadApps() {
        PackageManager pm = getPackageManager();
        List<PackageInfo> installedApps = pm.getInstalledPackages(0);
        List<AppInfo> result = new ArrayList<AppInfo>(installedApps.size());
        for (PackageInfo pkgInfo : installedApps) {
            AppInfo item = new AppInfo();
            item.pkgName = pkgInfo.packageName;
            item.isSysApp = PackageUtils.isPkgSystem(pkgInfo.applicationInfo);
            item.isUninstalled = !new File(pkgInfo.applicationInfo.sourceDir).exists();
            item.isDisabled = PackageUtils.isPkgEnabled(pkgInfo.applicationInfo);
            if (mExcludeSystem && item.isSysApp || mExcludeUninstalled && item.isUninstalled
                    || mExcludeDisabled && item.isDisabled) {
                continue;
            }
            item.appName = pkgInfo.applicationInfo.loadLabel(pm).toString();
            item.appIcon = pkgInfo.applicationInfo.loadIcon(pm);
            result.add(item);
        }
        return result;
    }
}
