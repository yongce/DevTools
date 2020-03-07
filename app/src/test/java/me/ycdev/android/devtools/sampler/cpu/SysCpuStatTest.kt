package me.ycdev.android.devtools.sampler.cpu

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SysCpuStatTest {
    @Test
    fun testNewObject() {
        val (utime, ntime, stime) = SysCpuStat()
        assertThat(utime).isEqualTo(0L)
        assertThat(ntime).isEqualTo(0L)
        assertThat(stime).isEqualTo(0L)
    }

    @Test
    fun test_checkStatSnapshot() {
        assertThat(SysCpuStat.checkStatSnapshot(null, SysCpuStat())).isTrue()
        val stat1 = SysCpuStat(10, 5, 20)
        val stat2 = SysCpuStat(15, 5, 27)

        assertThat(SysCpuStat.checkStatSnapshot(null, stat1)).isTrue()
        assertThat(SysCpuStat.checkStatSnapshot(stat1, stat2)).isTrue()
        assertThat(SysCpuStat.checkStatSnapshot(stat1, stat1)).isTrue()
        assertThat(SysCpuStat.checkStatSnapshot(stat2, stat1)).isFalse()
    }

    @Test
    fun test_sum() {
        val totalStat = SysCpuStat()
        val stat1 = SysCpuStat(10, 5, 20)
        val stat2 = SysCpuStat(15, 5, 27)
        val stat3 = SysCpuStat(23, 12, 30)

        totalStat.sum(stat1, stat2)
        assertThat(totalStat.utime).isEqualTo(5L)
        assertThat(totalStat.ntime).isEqualTo(0L)
        assertThat(totalStat.stime).isEqualTo(7L)

        totalStat.sum(stat2, stat3)
        assertThat(totalStat.utime).isEqualTo(13L)
        assertThat(totalStat.ntime).isEqualTo(7L)
        assertThat(totalStat.stime).isEqualTo(10L)
    }

    @Test
    fun test_getTotal() {
        val stat1 = SysCpuStat(10, 5, 20)
        assertThat(stat1.timeUsed).isEqualTo(35L)
    }
}