package me.ycdev.android.devtools.sampler.cpu;

public class SysCpuStat {
    public long utime; // user mode
    public long ntime; // user mode with low priority (nice)
    public long stime; // kernel mode
    public long itime; // the idle task

    public long getTotal() {
        return utime + ntime + stime + itime;
    }

    /**
     * Compute system CPU usage
     * @param preStat
     * @return [0, 1000]
     */
    public int computeCpuUsage(SysCpuStat preStat) {
        long preTotal = preStat.getTotal();
        long curTotal = getTotal();
        if (curTotal > preTotal) {
            return 1000 - (int) ((itime - preStat.itime) * 1000 / (curTotal - preTotal));
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SysCpuStat[utime=").append(utime);
        sb.append(", ntime=").append(ntime);
        sb.append(", stime=").append(stime);
        sb.append(", itime=").append(itime);
        sb.append("]");
        return sb.toString();
    }
}
