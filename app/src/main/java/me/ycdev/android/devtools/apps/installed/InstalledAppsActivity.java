package me.ycdev.android.devtools.apps.installed;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.apps.common.AppInfo;
import me.ycdev.android.devtools.utils.AppLogger;
import me.ycdev.android.lib.common.utils.PackageUtils;
import me.ycdev.android.lib.commonui.base.WaitingAsyncTaskBase;

public class InstalledAppsActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "InstalledAppsActivity";

    private InstalledAppsAdapter mAdapter;
    private AppsLoader mAppsLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.installed_apps);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(this);
        mAdapter = new InstalledAppsAdapter(this);
        listView.setAdapter(mAdapter);

        loadApps();
    }

    private void loadApps() {
        if (mAppsLoader != null && mAppsLoader.getStatus() != AsyncTask.Status.FINISHED) {
            mAppsLoader.cancel(true);
        }
        mAppsLoader = new AppsLoader(this);
        mAppsLoader.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.installed_apps_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort_by_app_name) {
            mAdapter.sort(new AppInfo.AppNameComparator());
            item.setChecked(true);
            return true;
        } else if (id == R.id.action_sort_by_pkg_name) {
            mAdapter.sort(new AppInfo.PkgNameComparator());
            item.setChecked(true);
            return true;
        } else if (id == R.id.action_sort_by_uid) {
            mAdapter.sort(new AppInfo.UidComparator());
            item.setChecked(true);
            return true;
        } else if (id == R.id.action_sort_by_install_time) {
            mAdapter.sort(new AppInfo.InstallTimeComparator());
            item.setChecked(true);
            return true;
        } else if (id == R.id.action_sort_by_update_time) {
            mAdapter.sort(new AppInfo.UpdateTimeComparator());
            item.setChecked(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppInfo item = mAdapter.getItem(position);
        AppLogger.i(TAG, "clicked item: " + item.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAppsLoader != null && mAppsLoader.getStatus() != AsyncTask.Status.FINISHED) {
            mAppsLoader.cancel(true);
        }
    }

    private class AppsLoader extends WaitingAsyncTaskBase<Void, Void, List<AppInfo>> {
        private Context mContext;

        public AppsLoader(Activity cxt) {
            super(cxt, cxt.getString(R.string.tips_loading));
            mContext = cxt;
        }

        @Override
        protected List<AppInfo> doInBackground(Void... params) {
            PackageManager pm = mContext.getPackageManager();
            List<PackageInfo> installedApps = pm.getInstalledPackages(0);
            List<AppInfo> result = new ArrayList<>(installedApps.size());
            for (PackageInfo pkgInfo : installedApps) {
                if (isCancelled()) {
                    return result; // cancelled
                }
                AppInfo item = new AppInfo();
                item.pkgName = pkgInfo.packageName;
                item.appUid = pkgInfo.applicationInfo.uid;
                item.sharedUid = pkgInfo.sharedUserId;
                item.isSysApp = (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                item.appName = pkgInfo.applicationInfo.loadLabel(pm).toString();
                item.versionName = pkgInfo.versionName;
                item.versionCode = pkgInfo.versionCode;
                item.apkPath = pkgInfo.applicationInfo.sourceDir;
                item.appIcon = pkgInfo.applicationInfo.loadIcon(pm);
                item.isDisabled = !PackageUtils.isPkgEnabled(mContext, pkgInfo.packageName);
                item.isUninstalled = !new File(pkgInfo.applicationInfo.sourceDir).exists();
                item.installTime = pkgInfo.firstInstallTime;
                item.updateTime = pkgInfo.lastUpdateTime;
                result.add(item);
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<AppInfo> result) {
            super.onPostExecute(result);
            mAdapter.setData(result);
            mAdapter.sort(new AppInfo.AppNameComparator());
        }
    }
}
