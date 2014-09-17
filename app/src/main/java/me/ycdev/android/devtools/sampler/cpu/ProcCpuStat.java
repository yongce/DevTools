package me.ycdev.android.devtools.sampler.cpu;

public class ProcCpuStat {
    public int pid;
    public long utime; // user mode
    public long stime; // kernel mode

    public ProcCpuStat(int pid) {
        this.pid = pid;
    }

    public long getTotal() {
        return utime + stime;
    }

    public static ProcCpuStat computeUsage(ProcCpuStat oldStat, ProcCpuStat newStat) {
        if (oldStat.pid != newStat.pid) {
            return null;
        }
        ProcCpuStat usage = new ProcCpuStat(newStat.pid);
        usage.utime = newStat.utime - oldStat.utime;
        usage.stime = newStat.stime - oldStat.stime;
        return usage;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ProcCpuStat[pid=").append(pid);
        sb.append(", utime=").append(utime);
        sb.append(", stime=").append(stime);
        sb.append("]");
        return sb.toString();
    }
}
