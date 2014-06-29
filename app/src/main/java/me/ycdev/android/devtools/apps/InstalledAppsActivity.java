package me.ycdev.android.devtools.apps;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.utils.AppLogger;
import me.ycdev.androidlib.compat.ViewsCompat;
import me.ycdev.androidlib.utils.PackageUtils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class InstalledAppsActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "InstalledAppsActivity";

    private ListView mListView;
    private InstalledAppsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.installed_apps);
        mListView = (ListView) findViewById(R.id.list);
        mListView.setOnItemClickListener(this);
        mAdapter = new InstalledAppsAdapter(this);
        mListView.setAdapter(mAdapter);

        loadApps();
    }

    private void loadApps() {
        new AppsLoader(this).execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AppInfoItem item = mAdapter.getItem(position);
        AppLogger.i(TAG, "clicked item: " + item.toString());
    }

    private class AppsLoader extends AsyncTask<Void, Void, List<AppInfoItem>> {
        private Context mContext;

        public AppsLoader(Context cxt) {
            mContext = cxt;
        }

        @Override
        protected List<AppInfoItem> doInBackground(Void... params) {
            PackageManager pm = mContext.getPackageManager();
            List<PackageInfo> installedApps = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            List<AppInfoItem> result = new ArrayList<AppInfoItem>(installedApps.size());
            for (PackageInfo pkgInfo : installedApps) {
                AppInfoItem item = new AppInfoItem();
                item.pkgName = pkgInfo.packageName;
                item.appUid = pkgInfo.applicationInfo.uid;
                item.sharedUid = pkgInfo.sharedUserId;
                item.isSysApp = (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                item.appName = pkgInfo.applicationInfo.loadLabel(pm).toString();
                item.versionName = pkgInfo.versionName;
                item.versionCode = pkgInfo.versionCode;
                item.apkPath = pkgInfo.applicationInfo.sourceDir;
                item.icon = pkgInfo.applicationInfo.loadIcon(pm);
                item.disabled = !PackageUtils.isPkgEnabled(mContext, pkgInfo.packageName);
                item.uninstalled = !new File(pkgInfo.applicationInfo.sourceDir).exists();
                result.add(item);
            }
            return result;
        }

        @Override
        protected void onPostExecute(List<AppInfoItem> result) {
            mAdapter.setData(result);
        }
    }

}

class AppInfoItem {
    public String pkgName;
    public int appUid;
    public String sharedUid;
    public boolean isSysApp;
    public String appName;
    public String versionName;
    public int versionCode;
    public String apkPath;
    public Drawable icon;
    public boolean disabled;
    public boolean uninstalled;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("pkgName: ").append(pkgName);
        sb.append(", appUid: ").append(appUid);
        sb.append(", sharedUid: ").append(sharedUid);
        sb.append(", isSysApp: ").append(isSysApp);
        sb.append(", appName: ").append(appName);
        sb.append(", versionName: ").append(versionName);
        sb.append(", versionCode: ").append(versionCode);
        sb.append(", apkPath: ").append(apkPath);
        sb.append(", disabled: ").append(disabled);
        sb.append(", uninstalled: ").append(uninstalled);
        return sb.toString();
    }
}

class InstalledAppsAdapter extends BaseAdapter {
    private static final int ALPHA_ENABLED = 255;
    private static final int ALPHA_DISABLED = 100;

    private Context mContext;
    private LayoutInflater mInflater;
    private List<AppInfoItem> mList;
    private int mSysAppColor;
    private int mNormalAppColor;
    private int mSharedAppColor;
    private int mUnavailableColor;

    InstalledAppsAdapter(Context cxt) {
        mContext = cxt;
        mInflater = LayoutInflater.from(cxt);
        mSysAppColor = cxt.getResources().getColor(R.color.apps_sys_app_color);
        mNormalAppColor = cxt.getResources().getColor(android.R.color.primary_text_dark);
        mSharedAppColor = cxt.getResources().getColor(R.color.apps_shared_uid_color);
        mUnavailableColor = cxt.getResources().getColor(R.color.apps_unavailable_color);
    }

    public void setData(List<AppInfoItem> list) {
        mList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public AppInfoItem getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppInfoItem item = getItem(position);
        ViewHolder holder = ViewHolder.get(convertView, parent, mInflater);
        holder.iconView.setImageDrawable(item.icon);
        holder.appNameView.setText(item.appName);
        holder.appUidView.setText(String.valueOf(item.appUid));
        holder.sharedUidView.setText(item.sharedUid);
        holder.pkgNameView.setText(item.pkgName);
        holder.sharedUidView.setTextColor(mSharedAppColor);
        if (item.isSysApp) {
            holder.pkgNameView.setTextColor(mSysAppColor);
        } else {
            holder.pkgNameView.setTextColor(mNormalAppColor);
        }
        holder.versionNameView.setText(item.versionName);
        holder.versionCodeView.setText(String.valueOf(item.versionCode));
        holder.apkPathView.setText(item.apkPath);

        String stateStr = mContext.getString(R.string.apps_app_state,
                String.valueOf(item.disabled), String.valueOf(item.uninstalled));
        holder.stateView.setText(stateStr);
        if (item.disabled || item.uninstalled) {
            ViewsCompat.setImageViewAlpha(holder.iconView, ALPHA_DISABLED);
            holder.stateView.setTextColor(mUnavailableColor);
        } else {
            ViewsCompat.setImageViewAlpha(holder.iconView, ALPHA_ENABLED);
            holder.stateView.setTextColor(mNormalAppColor);
        }
        return holder.rootView;
    }

    private static class ViewHolder {
        public View rootView;
        public ImageView iconView;
        public TextView appNameView;
        public TextView appUidView;
        public TextView sharedUidView;
        public TextView pkgNameView;
        public TextView versionNameView;
        public TextView versionCodeView;
        public TextView apkPathView;
        public TextView stateView;

        public ViewHolder(View convertView) {
            rootView = convertView;
            iconView = (ImageView) rootView.findViewById(R.id.icon);
            appNameView = (TextView) rootView.findViewById(R.id.app_name);
            appUidView = (TextView) rootView.findViewById(R.id.app_uid);
            sharedUidView = (TextView) rootView.findViewById(R.id.shared_uid);
            pkgNameView = (TextView) rootView.findViewById(R.id.pkg_name);
            versionNameView = (TextView) rootView.findViewById(R.id.version_name);
            versionCodeView = (TextView) rootView.findViewById(R.id.version_code);
            apkPathView = (TextView) rootView.findViewById(R.id.apk_path);
            stateView = (TextView) rootView.findViewById(R.id.state);
        }

        public static ViewHolder get(View convertView, ViewGroup parent, LayoutInflater inflater) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.installed_apps_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            return holder;
        }
    }
}