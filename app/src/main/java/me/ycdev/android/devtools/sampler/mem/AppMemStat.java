package me.ycdev.android.devtools.sampler.mem;

import android.util.SparseArray;

public class AppMemStat {
    public String pkgName;
    public SparseArray<ProcMemStat> procSetStats = new SparseArray<ProcMemStat>();

    public AppMemStat(String pkgName) {
        this.pkgName = pkgName;
    }

    public int getTotalPss() {
        int totalPss = 0;
        final int N = procSetStats.size();
        for (int i = 0; i < N; i++) {
            ProcMemStat pidStat = procSetStats.valueAt(i);
            totalPss += pidStat.memPss;
        }
        return totalPss;
    }

    public int getTotalPrivate() {
        int totalRss = 0;
        final int N = procSetStats.size();
        for (int i = 0; i < N; i++) {
            ProcMemStat pidStat = procSetStats.valueAt(i);
            totalRss += pidStat.memPrivate;
        }
        return totalRss;
    }
}
