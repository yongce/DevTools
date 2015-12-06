package me.ycdev.android.devtools.sampler.cpu;

import android.util.SparseArray;

public class AppCpuStat {
    public String pkgName;
    public SparseArray<ProcCpuStat> procSetStats = new SparseArray<>();

    public AppCpuStat(String pkgName) {
        this.pkgName = pkgName;
    }

    public long getTotal() {
        long total = 0;
        final int N = procSetStats.size();
        for (int i = 0; i < N; i++) {
            ProcCpuStat pidStat = procSetStats.valueAt(i);
            total += pidStat.getTotal();
        }
        return total;
    }

    public static AppCpuStat computeUsage(AppCpuStat oldStat, AppCpuStat newStat) {
        if (!oldStat.pkgName.equals(newStat.pkgName)) {
            return null;
        }
        AppCpuStat usage = new AppCpuStat(newStat.pkgName);
        final int N = newStat.procSetStats.size();
        for (int i = 0; i < N; i++) {
            int pid = newStat.procSetStats.keyAt(i);
            ProcCpuStat newPidStat = newStat.procSetStats.valueAt(i);
            ProcCpuStat oldPidStat = oldStat.procSetStats.get(pid);
            if (oldPidStat != null) {
                ProcCpuStat pidUsage = ProcCpuStat.computeUsage(oldPidStat, newPidStat);
                if (pidUsage != null) {
                    usage.procSetStats.put(pidUsage.pid, pidUsage);
                }
            }
        }
        return usage;
    }
}
