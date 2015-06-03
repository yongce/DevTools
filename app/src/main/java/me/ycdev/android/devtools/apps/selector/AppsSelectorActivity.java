package me.ycdev.android.devtools.apps.selector;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.ycdev.android.devtools.R;
import me.ycdev.android.lib.common.apps.AppInfo;
import me.ycdev.android.lib.common.apps.AppsLoadConfig;
import me.ycdev.android.lib.common.apps.AppsLoadFilter;
import me.ycdev.android.lib.common.apps.AppsLoadListener;
import me.ycdev.android.lib.common.apps.AppsLoader;
import me.ycdev.android.lib.commonui.base.LoadingAsyncTaskBase;

public class AppsSelectorActivity extends AppCompatActivity
        implements AppsSelectorAdapter.SelectedAppsChangeListener, View.OnClickListener {
    /** Type: boolean, default value: {@value #DEFAULT_MULTICHOICE} */
    public static final String EXTRA_MULTICHOICE = "extra.multichoice";
    /** Type: boolean, default value: {@value #DEFAULT_EXCLUDE_UNINSTALLED}*/
    public static final String EXTRA_EXCLUDE_UNINSTALLED = "extra.exclude_uninstalled";
    /** Type: boolean, default value: {@value #DEFAULT_EXCLUDE_DISABLED */
    public static final String EXTRA_EXCLUDE_DISABLED = "extra.exclude_disabled";
    /** Type: boolean, default value: {@value #DEFAULT_EXCLUDE_SYSTEM} */
    public static final String EXTRA_EXCLUDE_SYSTEM = "extra.exclude_system";

    /** Type: ArrayList<String> */
    public static final String RESULT_EXTRA_APPS_PKG_NAMES = "extra.pkg_names";

    private static final boolean DEFAULT_MULTICHOICE = false;
    private static final boolean DEFAULT_EXCLUDE_UNINSTALLED = true;
    private static final boolean DEFAULT_EXCLUDE_DISABLED = true;
    private static final boolean DEFAULT_EXCLUDE_SYSTEM = false;

    private boolean mExcludeUninstalled;
    private boolean mExcludeDisabled;
    private boolean mExcludeSystem;

    private TextView mStatusView;
    private AppsSelectorAdapter mAdapter;
    private Button mSelectBtn;

    private AppsLoadingTask mAppsLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_apps_selector);

        Intent intent = getIntent();
        boolean multiChoice = intent.getBooleanExtra(EXTRA_MULTICHOICE, DEFAULT_MULTICHOICE);
        mExcludeUninstalled = intent.getBooleanExtra(EXTRA_EXCLUDE_UNINSTALLED, DEFAULT_EXCLUDE_UNINSTALLED);
        mExcludeDisabled = intent.getBooleanExtra(EXTRA_EXCLUDE_DISABLED, DEFAULT_EXCLUDE_DISABLED);
        mExcludeSystem = intent.getBooleanExtra(EXTRA_EXCLUDE_SYSTEM, DEFAULT_EXCLUDE_SYSTEM);

        mStatusView = (TextView) findViewById(R.id.status);

        ListView listView = (ListView) findViewById(R.id.list);
        mAdapter = new AppsSelectorAdapter(this, this, multiChoice);
        listView.setAdapter(mAdapter);

        mSelectBtn = (Button) findViewById(R.id.select);
        mSelectBtn.setOnClickListener(this);

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
    public void onSelectedAppsChanged(int newCount) {
        if (newCount == 0) {
            mStatusView.setText(R.string.apps_selector_status_no_apps_selected);
        } else if (newCount == 1) {
            String status = getString(R.string.apps_selector_status_one_app_selected,
                    mAdapter.getOneSelectedApp().pkgName);
            mStatusView.setText(status);
        } else {
            String status = getString(R.string.apps_selector_status_multiple_apps_selected, newCount);
            mStatusView.setText(status);
        }
    }

    private void setSelectedApps() {
        List<AppInfo> apps = mAdapter.getSelectedApps();
        ArrayList<String> pkgNames = new ArrayList<>(apps.size());
        for (AppInfo appInfo : apps) {
            pkgNames.add(appInfo.pkgName);
        }

        Intent result = new Intent();
        result.putExtra(RESULT_EXTRA_APPS_PKG_NAMES, pkgNames);
        setResult(RESULT_OK, result);
    }

    @Override
    public void onClick(View v) {
        if (v == mSelectBtn) {
            setSelectedApps();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAppsLoader != null && mAppsLoader.getStatus() != AsyncTask.Status.FINISHED) {
            mAppsLoader.cancel(true);
        }
    }

    private class AppsLoadingTask extends LoadingAsyncTaskBase<Void, List<AppInfo>> {
        public AppsLoadingTask(Activity activity) {
            super(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onSelectedAppsChanged(0);
        }

        @Override
        protected List<AppInfo> doInBackground(Void... params) {
            AppsLoadFilter filter = new AppsLoadFilter();
            filter.onlyMounted = mExcludeUninstalled;
            filter.onlyEnabled = mExcludeDisabled;
            filter.includeSysApp = !mExcludeSystem;

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

            return AppsLoader.getInstance(mActivity).loadInstalledApps(filter, config, listener);
        }

        @Override
        protected void onPostExecute(List<AppInfo> result) {
            super.onPostExecute(result);
            mAdapter.setData(result);
        }
    }
}
