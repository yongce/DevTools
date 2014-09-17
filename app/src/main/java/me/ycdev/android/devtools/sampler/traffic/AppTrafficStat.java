package me.ycdev.android.devtools.sampler.traffic;

public class AppTrafficStat {
    public int uid;
    public long recvBytes;
    public long sendBytes;

    public AppTrafficStat(int uid) {
        this.uid = uid;
    }

    public long getTotal() {
        return recvBytes + sendBytes;
    }

    public static AppTrafficStat computeUsage(AppTrafficStat oldStat, AppTrafficStat newStat) {
        if (oldStat.uid != newStat.uid) {
            return null;
        }
        AppTrafficStat usage = new AppTrafficStat(newStat.uid);
        usage.recvBytes = newStat.recvBytes - oldStat.recvBytes;
        usage.sendBytes = newStat.sendBytes - oldStat.sendBytes;
        return usage;
    }
}
