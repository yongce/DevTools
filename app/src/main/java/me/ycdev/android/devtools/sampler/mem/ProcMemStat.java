package me.ycdev.android.devtools.sampler.mem;

public class ProcMemStat {
    public int pid;
    public int memPss;  // KB
    public int memRss;  // private dirty memory usage, KB

    public ProcMemStat(int pid) {
        this.pid = pid;
    }
}
