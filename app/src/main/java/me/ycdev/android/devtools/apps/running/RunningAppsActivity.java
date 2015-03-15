package me.ycdev.android.devtools.apps.running;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.utils.AppLogger;
import me.ycdev.android.lib.common.apps.AppInfo;
import me.ycdev.android.lib.common.utils.MiscUtils;
import me.ycdev.android.lib.commonui.base.LoadingAsyncTaskBase;

public class RunningAppsActivity extends ActionBarActivity {
    private static final String TAG = "RunningAppsActivity";

    private RunningAppsAdapter mAdapter;
    private AppsLoader mAppsLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.running_apps);
        ListView listView = (ListView) findViewById(R.id.list);
        mAdapter = new RunningAppsAdapter(this);
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
        inflater.inflate(R.menu.running_apps_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            loadApps();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAppsLoader != null && mAppsLoader.getStatus() != AsyncTask.Status.FINISHED) {
            mAppsLoader.cancel(true);
        }
    }

    private class AppsLoader extends LoadingAsyncTaskBase<Void, List<RunningAppInfo>> {
        public AppsLoader(Activity cxt) {
            super(cxt);
        }

        @Override
        protected List<RunningAppInfo> doInBackground(Void... params) {
            PackageManager pm = mActivity.getPackageManager();
            ActivityManager am = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            HashMap<String, RunningAppInfo> runningApps = new HashMap<>();

            // Get all running processes' info
            final int N = runningProcesses.size();
            int[] pidsList = new int[N];
            RunningAppInfo.ProcInfo[] procInfoList = new RunningAppInfo.ProcInfo[N];

            for (int i = 0; i < N; i++) {
                if (isCancelled()) {
                    return null; // cancelled
                }
                ActivityManager.RunningAppProcessInfo procInfo = runningProcesses.get(i);

                RunningAppInfo.ProcInfo procItem = new RunningAppInfo.ProcInfo();
                procItem.pid = procInfo.pid;
                procItem.procName = procInfo.processName;
                procItem.multiplePkgNames = procInfo.pkgList.length > 1;

                pidsList[i] = procInfo.pid;
                procInfoList[i] = procItem;

                String pkgName = procInfo.pkgList[0];
                RunningAppInfo appItem = runningApps.get(pkgName);
                if (appItem == null) {
                    appItem = new RunningAppInfo();
                    appItem.pkgName = pkgName;
                    try {
                        PackageInfo pkgInfo = pm.getPackageInfo(pkgName, 0);
                        appItem.appName = pkgInfo.applicationInfo.loadLabel(pm).toString();
                        appItem.appIcon = pkgInfo.applicationInfo.loadIcon(pm);
                    } catch (PackageManager.NameNotFoundException e) {
                        AppLogger.w(TAG, "unexpected exception", e);
                    }
                    runningApps.put(pkgName, appItem);
                }
                appItem.allProcesses.add(procItem);

                int percent = MiscUtils.calcProgressPercent(1, 40, i + 1, N);
                publishProgress(percent);
            }

            // Get memory usage of all the running processes
            Debug.MemoryInfo[] pidsMemInfo = am.getProcessMemoryInfo(pidsList);
            for (int i = 0; i < N; i++) {
                if (isCancelled()) {
                    return null; // cancelled
                }
                Debug.MemoryInfo memInfo = pidsMemInfo[i];
                procInfoList[i].memPss = memInfo.getTotalPss();

                int percent = MiscUtils.calcProgressPercent(41, 80, i + 1, N);
                publishProgress(percent);
            }

            // Convert the map to list
            List<RunningAppInfo> result = new ArrayList<>(runningApps.size());
            int i = 0;
            final int RUNNING_APPS_N = runningApps.size();
            for (RunningAppInfo appInfo : runningApps.values()) {
                if (isCancelled()) {
                    return null; // cancelled
                }
                result.add(appInfo);
                appInfo.totalMemPss = 0;
                for (RunningAppInfo.ProcInfo procInfo : appInfo.allProcesses) {
                    appInfo.totalMemPss += procInfo.memPss;
                }

                i++;
                int percent = MiscUtils.calcProgressPercent(81, 95, i, RUNNING_APPS_N);
                publishProgress(percent);
            }

            Collections.sort(result, new AppInfo.AppNameComparator());
            publishProgress(100);

            return result;
        }

        @Override
        protected void onPostExecute(List<RunningAppInfo> result) {
            super.onPostExecute(result);
            mAdapter.setData(result);
        }
    }
}
