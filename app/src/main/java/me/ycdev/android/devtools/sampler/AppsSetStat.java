package me.ycdev.android.devtools.sampler;

import android.app.ActivityManager;
import android.content.Context;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import me.ycdev.android.devtools.sampler.cpu.CpuUtils;
import me.ycdev.android.devtools.sampler.cpu.ProcCpuStat;
import me.ycdev.android.devtools.sampler.mem.MemoryUtils;
import me.ycdev.android.devtools.sampler.mem.ProcMemStat;
import me.ycdev.android.devtools.sampler.traffic.TrafficUtils;

public class AppsSetStat {
    public long sampleTime; // in System.currentTimeMillis()
    public long clockTime; //
    public HashSet<String> targetApps = new HashSet<String>();
    public HashMap<String, AppStat> appsStat = new HashMap<String, AppStat>();

    private AppsSetStat() {
        // private
    }

    public static AppsSetStat computeUsage(AppsSetStat oldStat, AppsSetStat newStat) {
        AppsSetStat usage = new AppsSetStat();
        usage.sampleTime = newStat.sampleTime;
        usage.clockTime = newStat.clockTime - oldStat.clockTime;
        usage.targetApps.addAll(newStat.targetApps);
        for (AppStat newAppStat : newStat.appsStat.values()) {
            AppStat oldAppStat = oldStat.appsStat.get(newAppStat.pkgName);
            if (oldAppStat != null) {
                AppStat appUsage = AppStat.computeUsage(oldAppStat, newAppStat);
                usage.appsStat.put(appUsage.pkgName, appUsage);
            }
        }
        return usage;
    }

    public static AppsSetStat createSnapshot(Context cxt, ArrayList<String> pkgNames) {
        AppsSetStat appsSetStat = new AppsSetStat();
        appsSetStat.sampleTime = System.currentTimeMillis();
        appsSetStat.clockTime = SystemClock.uptimeMillis();
        appsSetStat.targetApps.addAll(pkgNames);

        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();

        HashSet<Integer> allPidsSet = new HashSet<Integer>();

        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            for (String pkgName : procInfo.pkgList) {
                if (appsSetStat.targetApps.contains(pkgName)) {
                    allPidsSet.add(procInfo.pid);
                    AppStat appStat = appsSetStat.appsStat.get(pkgName);
                    if (appStat == null) {
                        appStat = new AppStat(pkgName, procInfo.uid);
                        appsSetStat.appsStat.put(pkgName, appStat);
                    }
                    appStat.pidsSet.add(procInfo.pid);
                }
            }
        }

        final int PIDS_COUNT = allPidsSet.size();
        int[] pids = new int[PIDS_COUNT];
        {
            int i = 0;
            for (Integer pid : allPidsSet) {
                pids[i] = pid;
                i++;
            }
        }

        // cpu stats
        ProcCpuStat[] pidsCpuStats = CpuUtils.getProcCpuStats(pids);
        for (int i = 0; i < PIDS_COUNT; i++) {
            int pid = pids[i];
            ProcCpuStat procCpu = pidsCpuStats[i];
            if (procCpu == null) {
                continue;
            }
            for (AppStat appStat : appsSetStat.appsStat.values()) {
                if (appStat.pidsSet.contains(pid)) {
                    appStat.cpuStat.procSetStats.put(pid, procCpu);
                    break;
                }
            }
        }

        // memory stats
        ProcMemStat[] pidsMemStats = MemoryUtils.getProcessMemoryStat(cxt, pids);
        for (int i = 0; i < PIDS_COUNT; i++) {
            int pid = pids[i];
            ProcMemStat procMem = pidsMemStats[i];
            for (AppStat appStat : appsSetStat.appsStat.values()) {
                if (appStat.pidsSet.contains(pid)) {
                    appStat.memStat.procSetStats.put(pid, procMem);
                    break;
                }
            }
        }

        // traffic stats
        for (AppStat appStat : appsSetStat.appsStat.values()) {
            appStat.trafficStat = TrafficUtils.getTrafficStat(appStat.uid);
        }

        return appsSetStat;
    }

}
