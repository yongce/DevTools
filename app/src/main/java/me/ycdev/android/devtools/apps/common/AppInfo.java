package me.ycdev.android.devtools.apps.common;

import android.graphics.drawable.Drawable;

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
}
