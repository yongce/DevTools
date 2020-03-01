package me.ycdev.android.devtools.sampler.cpu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import androidx.annotation.Nullable;
import me.ycdev.android.lib.common.utils.IoUtils;
import me.ycdev.android.lib.common.utils.StringUtils;
import timber.log.Timber;

public class CpuUtils {
    private static final String TAG = "CpuUtils";

    // Please "man proc" for the detail info of the proc files.
    private static final String FILE_SYS_CPU_STAT = "/proc/stat";
    private static final String FILE_PROC_CPU_STAT = "/proc/%d/stat";

    @Nullable
    public static SysCpuStat getCpuStat() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(FILE_SYS_CPU_STAT));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\\s+");
                if (fields.length >= 5 && fields[0].equals("cpu")) {
                    SysCpuStat stat = new SysCpuStat();
                    stat.utime = StringUtils.INSTANCE.parseLong(fields[1], 0);
                    stat.ntime = StringUtils.INSTANCE.parseLong(fields[2], 0);
                    stat.stime = StringUtils.INSTANCE.parseLong(fields[3], 0);
//                    stat.itime = StringUtils.parseLong(fields[4], 0);
                    return stat;
                }
            }
        } catch (IOException e) {
            Timber.tag(TAG).w(e, "failed to read sys cpu stat");
        } finally {
            IoUtils.INSTANCE.closeQuietly(br);
        }
        return null;
    }

    /**
     * Get CPU stat of specified process.
     * @param pid Process ID
     * @return null may be returned if some unexpected things happens
     */
    public static ProcCpuStat getProcCpuStat(int pid) {
        String file = String.format(Locale.US, FILE_PROC_CPU_STAT, pid);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split("\\s+");
                if (fields.length >= 15) {
                    ProcCpuStat stat = new ProcCpuStat(pid);
                    stat.utime = StringUtils.INSTANCE.parseLong(fields[13], 0);
                    stat.stime = StringUtils.INSTANCE.parseLong(fields[14], 0);
                    return stat;
                }
            }
        } catch (IOException e) {
            Timber.tag(TAG).w(e, "failed to read proc cpu stat: %s", pid);
        } finally {
            IoUtils.INSTANCE.closeQuietly(br);
        }
        return null;
    }

    /**
     * Get CPU stats of specified processes.
     * @param pids
     * @return The array element may be null if some unexpected things happens
     */
    public static ProcCpuStat[] getProcCpuStats(int[] pids) {
        final int N = pids.length;
        ProcCpuStat[] procCpuStats = new ProcCpuStat[N];
        for (int i = 0; i < N; i++) {
            procCpuStats[i] = getProcCpuStat(pids[i]);
        }
        return procCpuStats;
    }
}
