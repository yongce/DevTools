package me.ycdev.android.devtools.sampler.cpu;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SysCpuStatTest {
    @Test
    public void testNewObject() {
        SysCpuStat sysCpuStat = new SysCpuStat();
        assertThat(sysCpuStat.utime, equalTo(0L));
        assertThat(sysCpuStat.ntime, equalTo(0L));
        assertThat(sysCpuStat.stime, equalTo(0L));
    }

    @Test
    public void test_checkStatSnapshot() {
        assertThat(SysCpuStat.checkStatSnapshot(null, new SysCpuStat()), equalTo(true));

        final SysCpuStat stat1 = new SysCpuStat();
        stat1.utime = 10;
        stat1.ntime = 5;
        stat1.stime = 20;

        final SysCpuStat stat2 = new SysCpuStat();
        stat2.utime = 15;
        stat2.ntime = 5;
        stat2.stime = 27;

        assertThat(SysCpuStat.checkStatSnapshot(null, stat1), equalTo(true));
        assertThat(SysCpuStat.checkStatSnapshot(stat1, stat2), equalTo(true));
        assertThat(SysCpuStat.checkStatSnapshot(stat1, stat1), equalTo(true));
        assertThat(SysCpuStat.checkStatSnapshot(stat2, stat1), equalTo(false));
    }

    @Test
    public void test_sum() {
        final SysCpuStat totalStat = new SysCpuStat();

        final SysCpuStat stat1 = new SysCpuStat();
        stat1.utime = 10;
        stat1.ntime = 5;
        stat1.stime = 20;

        final SysCpuStat stat2 = new SysCpuStat();
        stat2.utime = 15;
        stat2.ntime = 5;
        stat2.stime = 27;

        final SysCpuStat stat3 = new SysCpuStat();
        stat3.utime = 23;
        stat3.ntime = 12;
        stat3.stime = 30;

        totalStat.sum(stat1, stat2);
        assertThat(totalStat.utime, equalTo(5L));
        assertThat(totalStat.ntime, equalTo(0L));
        assertThat(totalStat.stime, equalTo(7L));

        totalStat.sum(stat2, stat3);
        assertThat(totalStat.utime, equalTo(13L));
        assertThat(totalStat.ntime, equalTo(7L));
        assertThat(totalStat.stime, equalTo(10L));
    }

    @Test
    public void test_getTotal() {
        final SysCpuStat stat1 = new SysCpuStat();
        stat1.utime = 10;
        stat1.ntime = 5;
        stat1.stime = 20;

        assertThat(stat1.getTimeUsed(), equalTo(35L));
    }
}
