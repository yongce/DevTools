package me.ycdev.android.devtools.sampler;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.IOException;
import java.io.Writer;

import me.ycdev.android.lib.common.utils.DateTimeUtils;

public class AppStatReport {
    public String sysTimeStampStart;
    public String sysTimeStampEnd;
    public String pkgName;
    public int sampleCount;
    public long totalTimeUsage;
    public long totalCpuTime;
    public int maxProcessCount;
    public int minMemPss;
    public int maxMemPss;
    public long totalMemPss;
    public int minMemPrivate;
    public int maxMemPrivate;
    public long totalMemPrivate;
    public long totalTrafficRecv;
    public long totalTrafficSend;

    public AppStatReport(String pkgName) {
        this.pkgName = pkgName;
    }

    public void dumpStat(Context cxt, Writer writer) throws IOException {
        String versionInfo = null;
        try {
            PackageInfo pkgInfo = cxt.getPackageManager().getPackageInfo(pkgName, 0);
            versionInfo = pkgInfo.versionName + " & " + pkgInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        writer.append("System time: ").append(sysTimeStampStart).append(" ~ ").append(sysTimeStampEnd).append("\n");
        writer.append("Package name: " + pkgName).append("\n");
        writer.append("Version: " + versionInfo).append("\n");
        writer.append("Phone model: " + Build.MODEL).append("\n");
        writer.append("Android OS: " + Build.VERSION.RELEASE).append("\n");
        writer.append("Sample count: " + sampleCount).append("\n");
        String timeUsageStr = DateTimeUtils.getReadableTimeUsage(totalTimeUsage);
        writer.append("Time usage (ms): " + totalTimeUsage).append(", str: ").append(timeUsageStr).append("\n");
        writer.append("Total CPU time (ms): " + totalCpuTime).append("\n");
        double averageCpu = totalCpuTime * 60 * 1000;
        averageCpu /= totalTimeUsage;
        writer.append("Average CPU (ms per minute): " + averageCpu).append("\n");
        writer.append("Max process count: " + maxProcessCount).append("\n");
        writer.append("Min Mem PSS (KB): " + minMemPss).append("\n");
        writer.append("Max Mem PSS (KB): " + maxMemPss).append("\n");
        writer.append("Average Mem PSS (KB): " + (totalMemPss / sampleCount)).append("\n");
        writer.append("Min Mem Private (KB): " + minMemPrivate).append("\n");
        writer.append("Max Mem Private (KB): " + maxMemPrivate).append("\n");
        writer.append("Average Mem Private (KB): " + (totalMemPrivate / sampleCount)).append("\n");
        writer.append("Traffic Recv (B): " + totalTrafficRecv).append("\n");
        writer.append("Traffic Send (B): " + totalTrafficSend).append("\n");
        double averageTraffic = totalTrafficRecv + totalTrafficSend;
        averageTraffic = (averageTraffic * 60 * 1000) / totalTimeUsage;
        writer.append("Averate traffic (bytes per minute): " + averageTraffic).append("\n");
        writer.append("\n");
    }
}
