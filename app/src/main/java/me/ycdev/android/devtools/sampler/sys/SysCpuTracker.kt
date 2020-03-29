package me.ycdev.android.devtools.sampler.sys

import android.os.SystemClock
import java.util.LinkedList
import me.ycdev.android.devtools.sampler.cpu.CpuUtils
import me.ycdev.android.devtools.sampler.cpu.SysCpuStat
import timber.log.Timber

class SysCpuTracker {
    var startSysTime: Long = 0
        private set
    private var startClockTime: Long = 0
    var endSysTime: Long = 0
        private set
    private var endClockTime: Long = 0
    private var baseCpuStat: SysCpuStat? = null
    var cpuUsage: SysCpuStat? = null
        private set
    var sampleCount = 0
        private set
    private var _snapshotList: MutableList<SysCpuStat>? = null
    fun startTracker(keepSnapshot: Boolean) {
        startSysTime = 0
        startClockTime = 0
        endSysTime = 0
        endClockTime = 0
        baseCpuStat = null
        cpuUsage = SysCpuStat()
        sampleCount = 0
        _snapshotList = if (keepSnapshot) {
            LinkedList()
        } else {
            null
        }
    }

    fun sample() {
        if (endClockTime > 0) {
            Timber.tag(TAG).w("tracker already stopped")
            return
        }
        val sysCpu = CpuUtils.cpuStat
        if (sysCpu == null) {
            Timber.tag(TAG).w("failed to get CPU stats")
            return
        }
        if (_snapshotList != null) {
            _snapshotList!!.add(sysCpu)
            Timber.tag(TAG).i("snapshot: %s", sysCpu)
        }
        if (baseCpuStat != null) {
            cpuUsage!!.sum(baseCpuStat!!, sysCpu)
            sampleCount++
        } else {
            startSysTime = System.currentTimeMillis()
            startClockTime = SystemClock.elapsedRealtime()
        }
        baseCpuStat = sysCpu
    }

    fun stopTracker() {
        endSysTime = System.currentTimeMillis()
        endClockTime = SystemClock.elapsedRealtime()
    }

    val naturalTimeUsed: Long
        get() = if (endClockTime > 0) {
            endClockTime - startClockTime
        } else {
            SystemClock.elapsedRealtime() - startClockTime
        }

    val snapshotList: List<SysCpuStat>?
        get() = _snapshotList

    companion object {
        private const val TAG = "SysCpuTracker"
    }
}
