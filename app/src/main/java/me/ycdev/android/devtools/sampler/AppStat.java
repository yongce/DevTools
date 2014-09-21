package me.ycdev.android.devtools.sampler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import me.ycdev.android.devtools.sampler.cpu.AppCpuStat;
import me.ycdev.android.devtools.sampler.mem.AppMemStat;
import me.ycdev.android.devtools.sampler.traffic.AppTrafficStat;

class AppStat {
    public String pkgName;
    public int uid;
    public HashSet<Integer> pidsSet = new HashSet<Integer>();
    public AppMemStat memStat;
    public AppCpuStat cpuStat;
    /* may be null */
    public AppTrafficStat trafficStat;

    public AppStat(String pkgName, int uid) {
        this.pkgName = pkgName;
        this.uid = uid;
        this.memStat = new AppMemStat(pkgName);
        this.cpuStat = new AppCpuStat(pkgName);
    }

    public static AppStat computeUsage(AppStat oldStat, AppStat newStat) {
        if (!oldStat.pkgName.equals(newStat.pkgName) || oldStat.uid != newStat.uid) {
            return null;
        }
        AppStat usage = new AppStat(newStat.pkgName, newStat.uid);
        usage.pidsSet.addAll(newStat.pidsSet);
        usage.memStat = newStat.memStat;
        usage.cpuStat = AppCpuStat.computeUsage(oldStat.cpuStat, newStat.cpuStat);
        usage.trafficStat = AppTrafficStat.computeUsage(oldStat.trafficStat, newStat.trafficStat);
        return usage;
    }

    public static void appendHeader(FileWriter writer) throws IOException {
        writer.append("Time Stamp\tTime Usage(ms)\tCPU Time\tPIDs Count\tMem PSS(KB)\tMem RSS(KB)\tTraffic\n");
    }

    public void dumpStat(FileWriter writer, String timeStamp, long timeUsage) throws IOException {
        long cpuTime = cpuStat.getTotal();
        int memPss = memStat.getTotalPss();
        int memRss = memStat.getTotalRss();
        long traffic = trafficStat.getTotal();
        writer.append(timeStamp).append("\t").append(String.valueOf(timeUsage))
                .append("\t").append(String.valueOf(cpuTime))
                .append("\t").append(String.valueOf(pidsSet.size()))
                .append("\t").append(String.valueOf(memPss))
                .append("\t").append(String.valueOf(memRss))
                .append("\t").append(String.valueOf(traffic))
                .append("\n");
    }
}
