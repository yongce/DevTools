package me.ycdev.android.devtools.apps.common;

import android.graphics.drawable.Drawable;

import java.text.Collator;
import java.util.Comparator;

import me.ycdev.android.devtools.utils.StringHelper;

public class AppInfo {
    public String pkgName;
    public int appUid;
    public String sharedUid;
    public String appName;
    public Drawable appIcon;
    public String versionName;
    public int versionCode;
    public String apkPath;
    public long installTime;
    public long updateTime;
    public boolean isSysApp;
    public boolean isDisabled;
    public boolean isUninstalled;
    public boolean isSelected;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AppInfo[");
        sb.append("pkgName: ").append(pkgName);
        sb.append(", appUid: ").append(appUid);
        sb.append(", sharedUid: ").append(sharedUid);
        sb.append(", appName: ").append(appName);
        sb.append(", versionName: ").append(versionName);
        sb.append(", versionCode: ").append(versionCode);
        sb.append(", apkPath: ").append(apkPath);
        sb.append(", installTime: ").append(StringHelper.formatDateTime(installTime));
        sb.append(", updateTime: ").append(StringHelper.formatDateTime(updateTime));
        sb.append(", isSysApp: ").append(isSysApp);
        sb.append(", isDisabled: ").append(isDisabled);
        sb.append(", isUninstalled: ").append(isUninstalled);
        sb.append(", isSelected: ").append(isSelected);
        sb.append("]");
        return sb.toString();
    }

    public static class AppNameComparator implements Comparator<AppInfo> {
        private Collator mCollator = Collator.getInstance();

        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
            return mCollator.compare(lhs.appName, rhs.appName);
        }
    }

    public static class PkgNameComparator implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
            return lhs.pkgName.compareTo(rhs.pkgName);
        }
    }

    public static class UidComparator implements Comparator<AppInfo> {
        private PkgNameComparator mPkgNameComparator = new PkgNameComparator();

        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
            if (lhs.appUid < rhs.appUid) {
                return -1;
            } else if (lhs.appUid > rhs.appUid) {
                return 1;
            } else {
                return mPkgNameComparator.compare(lhs,rhs);
            }
        }
    }

    public static class InstallTimeComparator implements Comparator<AppInfo> {
        private PkgNameComparator mPkgNameComparator = new PkgNameComparator();

        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
            if (lhs.installTime < rhs.installTime) {
                return 1;
            } else if (lhs.installTime > rhs.installTime) {
                return -1;
            } else {
                return mPkgNameComparator.compare(lhs,rhs);
            }
        }
    }

    public static class UpdateTimeComparator implements Comparator<AppInfo> {
        private PkgNameComparator mPkgNameComparator = new PkgNameComparator();

        @Override
        public int compare(AppInfo lhs, AppInfo rhs) {
            if (lhs.updateTime < rhs.updateTime) {
                return 1;
            } else if (lhs.updateTime > rhs.updateTime) {
                return -1;
            } else {
                return mPkgNameComparator.compare(lhs,rhs);
            }
        }
    }

}
