package me.ycdev.android.devtools.sampler

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Handler.Callback
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import com.google.gson.Gson
import me.ycdev.android.devtools.sampler.sys.SysCpuTracker
import me.ycdev.android.lib.common.utils.DateTimeUtils
import me.ycdev.android.lib.common.wrapper.IntentHelper
import timber.log.Timber

class SysSamplerService : Service(), Callback {
    private var sampleInterval = 0 // milliseconds
    private var sampleTime = 0 // milliseconds
    private var keepSnapshots = false
    private var keepWakelock = false
    private lateinit var handler: Handler
    private var sysCpuTracker: SysCpuTracker? = null
    private var wakeLock: WakeLock? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_STICKY
        }
        val action = intent.action
        Timber.tag(TAG).i("onStartCommand: %s", action)
        if (sysCpuTracker != null) {
            Timber.tag(TAG).i("already started, ignore request")
        } else if (ACTION_SAMPLE_SYSTEM == action) {
            sampleInterval = IntentHelper.getIntExtra(
                intent,
                EXTRA_SAMPLE_INTERVAL,
                DEFAULT_SAMPLE_INTERVAL
            ) * 1000
            sampleTime = IntentHelper.getIntExtra(
                intent,
                EXTRA_SAMPLE_TIME,
                DEFAULT_SAMPLE_TIME
            ) * 1000
            keepSnapshots = IntentHelper.getBooleanExtra(
                intent,
                EXTRA_KEEP_SNAPSHOTS,
                DEFAULT_KEEP_SNAPSHOTS
            )
            keepWakelock = IntentHelper.getBooleanExtra(
                intent,
                EXTRA_KEEP_WAKELOCK,
                DEFAULT_KEEP_WAKELOCK
            )
            startTracker()
        }
        return START_STICKY
    }

    private fun acquireWakelock() {
        if (keepWakelock) {
            val powerMgr =
                getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DevTools:SysSampler")
            wakeLock?.acquire(sampleTime.toLong())
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock != null && wakeLock!!.isHeld) {
            wakeLock!!.release()
            wakeLock = null
        }
    }

    private fun startTracker() {
        Timber.tag(TAG).i("startTracker...")
        acquireWakelock()
        sysCpuTracker = SysCpuTracker()
        sysCpuTracker!!.startTracker(keepSnapshots)
        val thread = HandlerThread("SysSampler")
        thread.start()
        handler = Handler(thread.looper, this)
        handler.sendEmptyMessage(MSG_SAMPLE)
        handler.sendEmptyMessageDelayed(
            MSG_SAMPLE,
            sampleTime.toLong()
        )
    }

    private fun stopTracker() {
        Timber.tag(TAG).i("stopTracker...")
        sysCpuTracker!!.stopTracker()
        val sysCpuUsage = sysCpuTracker!!.cpuUsage
        Timber.tag(TAG).i(
            "stats report (%s ~ %s, %d samples)",
            DateTimeUtils.getReadableTimeStamp(sysCpuTracker!!.startSysTime),
            DateTimeUtils.getReadableTimeStamp(sysCpuTracker!!.endSysTime),
            sysCpuTracker!!.sampleCount
        )
        Timber.tag(TAG).i(
            "utime: %d, ntime: %d, stime: %d",
            sysCpuUsage!!.utime, sysCpuUsage.ntime, sysCpuUsage.stime
        )
        Timber.tag(TAG).i(
            "cpu time used: %d, natural time used: %d", sysCpuUsage.timeUsed,
            sysCpuTracker!!.naturalTimeUsed
        )
        val snapshotList = sysCpuTracker!!.snapshotList
        if (snapshotList != null) {
            Timber.tag(TAG)
                .i("snapshot list: %s", Gson().toJson(snapshotList))
        }
        stopSelf()
        releaseWakeLock()
    }

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == MSG_SAMPLE) {
            sysCpuTracker!!.sample()
            if (sysCpuTracker!!.naturalTimeUsed >= sampleTime) {
                handler.removeMessages(MSG_SAMPLE)
                stopTracker()
            } else {
                handler.sendEmptyMessageDelayed(
                    MSG_SAMPLE,
                    sampleInterval.toLong()
                )
            }
        }
        return false
    }

    companion object {
        private const val TAG = "SysSamplerService"

        private const val ACTION_SAMPLE_SYSTEM = "action.SAMPLE_SYSTEM"
        private const val EXTRA_SAMPLE_INTERVAL = "extra.sample_interval" // seconds
        private const val EXTRA_SAMPLE_TIME = "extra.sample_time" // seconds
        private const val EXTRA_KEEP_SNAPSHOTS = "extra.keep_snapshots"
        private const val EXTRA_KEEP_WAKELOCK = "extra.keep_wakelock"

        private const val MSG_SAMPLE = 1

        private const val DEFAULT_SAMPLE_INTERVAL = 5 // seconds
        private const val DEFAULT_SAMPLE_TIME = 60 // seconds
        private const val DEFAULT_KEEP_SNAPSHOTS = false
        private const val DEFAULT_KEEP_WAKELOCK = false
    }
}
