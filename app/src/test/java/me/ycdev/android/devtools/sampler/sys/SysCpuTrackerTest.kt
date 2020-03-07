package me.ycdev.android.devtools.sampler.sys

import android.os.SystemClock
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.mockk.every
import io.mockk.mockkStatic
import me.ycdev.android.devtools.sampler.cpu.CpuUtils
import me.ycdev.android.devtools.sampler.cpu.SysCpuStat
import org.junit.Before
import org.junit.Test

class SysCpuTrackerTest {
    @Before
    fun setup() {
    }

    @Test
    fun testTracker() {
        val jsonData = "[{\"ntime\":38606,\"stime\":1314050,\"utime\":2164513}," +
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
                "{\"ntime\":38611,\"stime\":1315426,\"utime\":2165377}]"
        val snapshots = Gson().fromJson<List<SysCpuStat>>(
            jsonData,
            object : TypeToken<List<SysCpuStat?>?>() {}.type
        )
        mockkStatic(SystemClock::class)
        mockkStatic(CpuUtils::class)
        every { SystemClock.elapsedRealtime() } returns System.currentTimeMillis()
        val tracker = SysCpuTracker()
        tracker.startTracker(true)
        for (sysCpuStat in snapshots) {
            every { CpuUtils.cpuStat } returns sysCpuStat
            tracker.sample()
        }
        tracker.stopTracker()
        assertThat(tracker.sampleCount).isEqualTo(snapshots.size - 1)
        assertThat(tracker.snapshotList).isEqualTo(snapshots)
        val cpuUsage = tracker.cpuUsage
        assertThat(cpuUsage).isNotNull()
        assertThat(cpuUsage!!.utime).isEqualTo(864L)
        assertThat(cpuUsage.ntime).isEqualTo(5L)
        assertThat(cpuUsage.stime).isEqualTo(1376L)
    }
}