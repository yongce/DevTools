package me.ycdev.android.devtools.apps.installed;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

import me.ycdev.android.arch.activity.AppCompatBaseActivity;
import me.ycdev.android.devtools.R;
import me.ycdev.android.lib.common.apps.AppInfo;
import me.ycdev.android.lib.common.apps.AppsLoadConfig;
import me.ycdev.android.lib.common.apps.AppsLoadFilter;
import me.ycdev.android.lib.common.apps.AppsLoadListener;
import me.ycdev.android.lib.common.apps.AppsLoader;
import me.ycdev.android.lib.commonui.base.LoadingAsyncTaskBase;
import timber.log.Timber;

public class InstalledAppsActivity extends AppCompatBaseActivity
        implements AdapterView.OnItemClickListener {
    private static final String TAG = "InstalledAppsActivity";

    private InstalledAppsAdapter mAdapter;
    private AppsLoadingTask mAppsLoader;

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
        mAppsLoader = new AppsLoadingTask(this);
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
        Timber.tag(TAG).i("clicked item: %s", item.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAppsLoader != null && mAppsLoader.getStatus() != AsyncTask.Status.FINISHED) {
            mAppsLoader.cancel(true);
        }
    }

    private class AppsLoadingTask extends LoadingAsyncTaskBase<Void, List<AppInfo>> {
        public AppsLoadingTask(Activity cxt) {
            super(cxt);
        }

        @Override
        protected List<AppInfo> doInBackground(Void... params) {
            AppsLoadFilter filter = new AppsLoadFilter();
            filter.setOnlyMounted(false);
            filter.setOnlyEnabled(false);

            AppsLoadConfig config = new AppsLoadConfig();

            AppsLoadListener listener = new AppsLoadListener() {
                @Override
                public boolean isCancelled() {
                    return AppsLoadingTask.this.isCancelled();
                }

                @Override
                public void onProgressUpdated(int percent, AppInfo appInfo) {
                    publishProgress(percent);
                }
            };

            return AppsLoader.Companion.getInstance(getActivity()).loadInstalledApps(filter, config, listener);
        }

        @Override
        protected void onPostExecute(List<AppInfo> result) {
            super.onPostExecute(result);
            mAdapter.setData(result);
            mAdapter.sort(new AppInfo.AppNameComparator());
        }
    }
}
