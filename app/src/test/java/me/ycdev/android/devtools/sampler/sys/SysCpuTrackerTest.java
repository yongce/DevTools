package me.ycdev.android.devtools.sampler.sys;

import android.os.SystemClock;

import com.alibaba.fastjson.JSON;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import me.ycdev.android.arch.utils.AppLogger;
import me.ycdev.android.devtools.sampler.cpu.CpuUtils;
import me.ycdev.android.devtools.sampler.cpu.SysCpuStat;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SystemClock.class, CpuUtils.class})
public class SysCpuTrackerTest {
    @Before
    public void setup() {
        AppLogger.enableJvmLogger();
    }

    @Test
    public void testTracker() {
        final String jsonData = "[{\"ntime\":38606,\"stime\":1314050,\"utime\":2164513}," +
                "{\"ntime\":38606,\"stime\":1314167,\"utime\":2164612}," +
                "{\"ntime\":38610,\"stime\":1314305,\"utime\":2164690}," +
                "{\"ntime\":38611,\"stime\":1314411,\"utime\":2164779}," +
                "{\"ntime\":38611,\"stime\":1314525,\"utime\":2164862}," +
                "{\"ntime\":38611,\"stime\":1314651,\"utime\":2164938}," +
                "{\"ntime\":38611,\"stime\":1314783,\"utime\":2165013}," +
                "{\"ntime\":38611,\"stime\":1314931,\"utime\":2165080}," +
                "{\"ntime\":38611,\"stime\":1315067,\"utime\":2165145}," +
                "{\"ntime\":38611,\"stime\":1315167,\"utime\":2165213}," +
                "{\"ntime\":38611,\"stime\":1315255,\"utime\":2165277}," +
                "{\"ntime\":38611,\"stime\":1315353,\"utime\":2165330}," +
                "{\"ntime\":38611,\"stime\":1315426,\"utime\":2165377}]";
        final List<SysCpuStat> snapshots = JSON.parseArray(jsonData, SysCpuStat.class);

        mockStatic(SystemClock.class);
        mockStatic(CpuUtils.class);

        PowerMockito.when(SystemClock.elapsedRealtime()).thenReturn(System.currentTimeMillis());

        SysCpuTracker tracker = new SysCpuTracker();
        tracker.startTracker(true);

        for (SysCpuStat sysCpuStat : snapshots) {
            PowerMockito.when(CpuUtils.getCpuStat()).thenReturn(sysCpuStat);
            tracker.sample();
        }

        tracker.stopTracker();

        assertThat(tracker.getSampleCount(), equalTo(snapshots.size() - 1));
        assertThat(tracker.getSnapshotList(), equalTo(snapshots));

        SysCpuStat cpuUsage = tracker.getCpuUsage();
        assertThat(cpuUsage, notNullValue());
        assertThat(cpuUsage.utime, equalTo(864L));
        assertThat(cpuUsage.ntime, equalTo(5L));
        assertThat(cpuUsage.stime, equalTo(1376L));
    }
}
