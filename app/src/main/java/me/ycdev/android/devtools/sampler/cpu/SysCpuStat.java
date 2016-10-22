package me.ycdev.android.devtools.sampler.cpu;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;

import me.ycdev.android.arch.utils.AppLogger;

/**
 * We have to ignore "idle time" for CPU time stats. For devices with multiple CPU cores,
 * the idle time may become shorter. The reason is that some CPU cores may be enabled
 * and disabled dynamically.
 */
public class SysCpuStat {
    private static final String TAG = "SysCpuStat";

    @JSONField(name="utime")
    public long utime; // user mode
    @JSONField(name="ntime")
    public long ntime; // user mode with low priority (nice)
    @JSONField(name="stime")
    public long stime; // kernel mode

    public long getTimeUsed() {
        return utime + ntime + stime;
    }

    public static boolean checkStatSnapshot(@Nullable SysCpuStat previousSnapshot,
            @NonNull SysCpuStat curSnapshot) {
        if (previousSnapshot == null) {
            return curSnapshot.utime >= 0 && curSnapshot.ntime >= 0 && curSnapshot.stime >= 0;
        }

        return curSnapshot.utime >= previousSnapshot.utime
                && curSnapshot.ntime >= previousSnapshot.ntime
                && curSnapshot.stime >= previousSnapshot.stime;
    }

    public void sum(@NonNull SysCpuStat previousSnapshot, @NonNull SysCpuStat curSnapshot) {
        if (!checkStatSnapshot(previousSnapshot, curSnapshot)) {
            AppLogger.w(TAG, "bad CPU stats, previous: %s, current: %s", previousSnapshot, curSnapshot);
            return;
        }

        utime += (curSnapshot.utime - previousSnapshot.utime);
        ntime += (curSnapshot.ntime - previousSnapshot.ntime);
        stime += (curSnapshot.stime - previousSnapshot.stime);
    }

    @Override
    public String toString() {
        SerializeFilter filter = new SimplePropertyPreFilter("utime", "ntime", "stime");
        return JSON.toJSONString(this, filter);
    }

    @Nullable
    public static SysCpuStat parseJsonString(String json) {
        return JSON.parseObject(json, SysCpuStat.class);
    }
}
