package me.ycdev.android.devtools.apps.running;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import me.ycdev.android.lib.common.apps.AppInfo;

class RunningAppInfo {
    static class ProcInfo {
        public int pid;
        public String procName;
        public boolean multiplePkgNames;
        public int memPss;  // KB
    }

    public AppInfo appInfo;
    public List<ProcInfo> allProcesses = new ArrayList<>();
    public int totalMemPss; // KB

    RunningAppInfo(@NonNull String pkgName) {
        appInfo = new AppInfo(pkgName);
    }

    public static class AppNameComparator implements Comparator<RunningAppInfo> {
        private Collator mCollator = Collator.getInstance();

        @Override
        public int compare(RunningAppInfo lhs, RunningAppInfo rhs) {
            return mCollator.compare(lhs.appInfo.getAppName(), rhs.appInfo.getAppName());
        }
    }
}
