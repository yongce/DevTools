package me.ycdev.android.devtools.sampler.mem;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

public class MemoryUtils {
    /**
     * Get memory stats of specified processes.
     */
    public static ProcMemStat[] getProcessMemoryStat(Context cxt, int[] pids) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        final int N = pids.length;
        Debug.MemoryInfo[] pidsMemInfo = am.getProcessMemoryInfo(pids);
        ProcMemStat[] pidsMemStat = new ProcMemStat[N];
        for (int i = 0; i < N; i++) {
            int pid = pids[i];
            Debug.MemoryInfo memInfo = pidsMemInfo[i];
            pidsMemStat[i] = new ProcMemStat(pid);
            pidsMemStat[i].memPss = memInfo.getTotalPss();
            pidsMemStat[i].memPrivate = memInfo.getTotalPrivateDirty();
        }
        return pidsMemStat;
    }
}
