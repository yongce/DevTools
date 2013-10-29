package me.ycdev.android.devtools.apps;

import java.util.ArrayList;
import java.util.List;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.utils.AppLogger;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class InstalledAppsActivity extends Activity implements AdapterView.OnItemClickListener {
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
            List<PackageInfo> installedApps = pm.getInstalledPackages(0);
            List<AppInfoItem> result = new ArrayList<AppInfoItem>(installedApps.size());
            for (PackageInfo pkgInfo : installedApps) {
                AppInfoItem item = new AppInfoItem();
                item.pkgName = pkgInfo.packageName;
                item.appUid = pkgInfo.applicationInfo.uid;
                item.sharedUid = pkgInfo.sharedUserId;
                item.isSysApp = (pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                item.appName = pkgInfo.applicationInfo.loadLabel(pm).toString();
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("pkgName: " + pkgName);
        sb.append(", appUid: " + appUid);
        sb.append(", sharedUid: " + sharedUid);
        sb.append(", isSysApp: " + isSysApp);
        sb.append(", appName: " + appName);
        return sb.toString();
    }
}

class InstalledAppsAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<AppInfoItem> mList;
    private int mSysAppColor;
    private int mNormalAppColor;
    private int mSharedAppColor;

    InstalledAppsAdapter(Context cxt) {
        mInflater = LayoutInflater.from(cxt);
        mSysAppColor = cxt.getResources().getColor(R.color.sys_app_color);
        mNormalAppColor = cxt.getResources().getColor(android.R.color.primary_text_dark);
        mSharedAppColor = cxt.getResources().getColor(R.color.shared_uid_color);
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
        holder.appNameView.setText(item.appName);
        holder.appUidView.setText(String.valueOf(item.appUid));
        holder.sharedUidView.setText(item.sharedUid);
        holder.pkgNameView.setText(item.pkgName);
        if (item.isSysApp) {
            holder.sharedUidView.setTextColor(mSysAppColor);
            holder.pkgNameView.setTextColor(mSysAppColor);
        } else {
            holder.sharedUidView.setTextColor(mSharedAppColor);
            holder.pkgNameView.setTextColor(mNormalAppColor);
        }
        return holder.rootView;
    }

    private static class ViewHolder {
        public View rootView;
        public TextView appNameView;
        public TextView appUidView;
        public TextView sharedUidView;
        public TextView pkgNameView;

        public ViewHolder(View convertView) {
            rootView = convertView;
            appNameView = (TextView) rootView.findViewById(R.id.app_name);
            appUidView = (TextView) rootView.findViewById(R.id.app_uid);
            sharedUidView = (TextView) rootView.findViewById(R.id.shared_uid);
            pkgNameView = (TextView) rootView.findViewById(R.id.pkg_name);
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