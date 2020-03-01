package me.ycdev.android.devtools.sampler.cpu;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

/**
 * We have to ignore "idle time" for CPU time stats. For devices with multiple CPU cores,
 * the idle time may become shorter. The reason is that some CPU cores may be enabled
 * and disabled dynamically.
 */
public class SysCpuStat {
    private static final String TAG = "SysCpuStat";

    @SerializedName("utime")
    public long utime; // user mode
    @SerializedName("ntime")
    public long ntime; // user mode with low priority (nice)
    @SerializedName("stime")
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
            Timber.tag(TAG).w("bad CPU stats, previous: %s, current: %s", previousSnapshot, curSnapshot);
            return;
        }

        utime += (curSnapshot.utime - previousSnapshot.utime);
        ntime += (curSnapshot.ntime - previousSnapshot.ntime);
        stime += (curSnapshot.stime - previousSnapshot.stime);
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Nullable
    public static SysCpuStat parseJsonString(String json) {
        return new Gson().fromJson(json, SysCpuStat.class);
    }
}
