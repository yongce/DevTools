package me.ycdev.android.devtools.sampler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import me.ycdev.android.devtools.sampler.cpu.AppCpuStat;
import me.ycdev.android.devtools.sampler.mem.AppMemStat;
import me.ycdev.android.devtools.sampler.traffic.AppTrafficStat;

class AppStat {
    private static final String STAT_FILE_COLUMNS_SEP = "\t";

    private static final String COLUMN_NAME_TIME_STAMP = "Time Stamp(ms)";
    private static final String COLUMN_NAME_CPU_TIME = "CPU Time(ms)";
    private static final String COLUMN_NAME_PROCESS_COUNT = "Process Count";
    private static final String COLUMN_NAME_MEM_PSS = "Mem PSS(KB)";
    private static final String COLUMN_NAME_MEM_PRIVATE = "Mem Private(KB)";
    private static final String COLUMN_NAME_TRAFFIC_RECV = "Traffic Recv(B)";
    private static final String COLUMN_NAME_TRAFFIC_SEND = "Traffic Send(B)";

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
        writer.append(COLUMN_NAME_TIME_STAMP + STAT_FILE_COLUMNS_SEP + COLUMN_NAME_CPU_TIME
                + STAT_FILE_COLUMNS_SEP + COLUMN_NAME_PROCESS_COUNT
                + STAT_FILE_COLUMNS_SEP + COLUMN_NAME_MEM_PSS
                + STAT_FILE_COLUMNS_SEP + COLUMN_NAME_MEM_PRIVATE
                + STAT_FILE_COLUMNS_SEP + COLUMN_NAME_TRAFFIC_RECV
                + STAT_FILE_COLUMNS_SEP + COLUMN_NAME_TRAFFIC_SEND);
        writer.append("\n");
    }

    public void dumpStat(FileWriter writer, String timeStamp, long timeUsage) throws IOException {
        long cpuTime = cpuStat.getTotal();
        int memPss = memStat.getTotalPss();
        int memPrivate = memStat.getTotalPrivate();
        writer.append(timeStamp).append(STAT_FILE_COLUMNS_SEP).append(String.valueOf(timeUsage))
                .append(STAT_FILE_COLUMNS_SEP).append(String.valueOf(cpuTime))
                .append(STAT_FILE_COLUMNS_SEP).append(String.valueOf(pidsSet.size()))
                .append(STAT_FILE_COLUMNS_SEP).append(String.valueOf(memPss))
                .append(STAT_FILE_COLUMNS_SEP).append(String.valueOf(memPrivate))
                .append(STAT_FILE_COLUMNS_SEP).append(String.valueOf(trafficStat.recvBytes))
                .append(STAT_FILE_COLUMNS_SEP).append(String.valueOf(trafficStat.sendBytes))
                .append("\n");
    }

    public static StatFileLine readStat(String statLine) throws IOException {
        if (statLine.startsWith(COLUMN_NAME_TIME_STAMP)) {
            return null;
        }
        String[] columns = statLine.split(STAT_FILE_COLUMNS_SEP);
        StatFileLine entry = new StatFileLine();
        entry.sysTimeStamp = columns[0];
        entry.timeUsage = Long.parseLong(columns[1]);
        entry.cpuTime = Long.parseLong(columns[2]);
        entry.processCount = Integer.parseInt(columns[3]);
        entry.memPss = Integer.parseInt(columns[4]);
        entry.memPrivate = Integer.parseInt(columns[5]);
        entry.trafficRecv = Long.parseLong(columns[6]);
        entry.trafficSend = Long.parseLong(columns[7]);
        return entry;
    }
}
