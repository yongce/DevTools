package me.ycdev.android.devtools.apps.running;

import java.util.ArrayList;
import java.util.List;

import me.ycdev.android.devtools.apps.common.AppInfo;

class RunningAppInfo extends AppInfo {
    static class ProcInfo {
        public int pid;
        public String procName;
        public boolean multiplePkgNames;
        public int memPss;  // KB
    }

    public List<ProcInfo> allProcesses = new ArrayList<>();
    public int totalMemPss; // KB
}
