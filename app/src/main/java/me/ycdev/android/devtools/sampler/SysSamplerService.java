package me.ycdev.android.devtools.sampler;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;

import com.google.gson.Gson;

import java.util.List;

import androidx.annotation.Nullable;
import me.ycdev.android.devtools.sampler.cpu.SysCpuStat;
import me.ycdev.android.devtools.sampler.sys.SysCpuTracker;
import me.ycdev.android.lib.common.utils.DateTimeUtils;
import me.ycdev.android.lib.common.wrapper.IntentHelper;
import timber.log.Timber;

public class SysSamplerService extends Service implements Handler.Callback {
    private static final String TAG = "SysSamplerService";

    private static final String ACTION_SAMPLE_SYSTEM = "action.SAMPLE_SYSTEM";

    private static final String EXTRA_SAMPLE_INTERVAL = "extra.sample_interval"; // seconds
    private static final String EXTRA_SAMPLE_TIME = "extra.sample_time"; // seconds
    private static final String EXTRA_KEEP_SNAPSHOTS = "extra.keep_snapshots";
    private static final String EXTRA_KEEP_WAKELOCK = "extra.keep_wakelock";

    private static final int MSG_SAMPLE = 1;

    private static final int DEFAULT_SAMPLE_INTERVAL = 5; // seconds
    private static final int DEFAULT_SAMPLE_TIME = 60; // seconds
    private static final boolean DEFAULT_KEEP_SNAPSHOTS = false;
    private static final boolean DEFAULT_KEEP_WAKELOCK = false;

    private int mSampleInterval; // milliseconds
    private int mSampleTime; // milliseconds
    private boolean mKeepSnapshots;
    private boolean mKeepWakelock;

    private Handler mHandler;
    private SysCpuTracker mSysCpuTracker;
    private PowerManager.WakeLock mWakeLock;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        String action = intent.getAction();
        Timber.tag(TAG).i("onStartCommand: %s", action);
        if (mSysCpuTracker != null) {
            Timber.tag(TAG).i("already started, ignore request");
        } else if (ACTION_SAMPLE_SYSTEM.equals(action)) {
            mSampleInterval = IntentHelper.INSTANCE.getIntExtra(intent, EXTRA_SAMPLE_INTERVAL, DEFAULT_SAMPLE_INTERVAL) * 1000;
            mSampleTime = IntentHelper.INSTANCE.getIntExtra(intent, EXTRA_SAMPLE_TIME, DEFAULT_SAMPLE_TIME) * 1000;
            mKeepSnapshots = IntentHelper.INSTANCE.getBooleanExtra(intent, EXTRA_KEEP_SNAPSHOTS, DEFAULT_KEEP_SNAPSHOTS);
            mKeepWakelock = IntentHelper.INSTANCE.getBooleanExtra(intent, EXTRA_KEEP_WAKELOCK, DEFAULT_KEEP_WAKELOCK);
            startTracker();
        }

        return START_STICKY;
    }

    private void acquireWakelock() {
        if (mKeepWakelock) {
            PowerManager powerMgr = (PowerManager) getSystemService(POWER_SERVICE);
            mWakeLock = powerMgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DevTools:SysSampler");
            mWakeLock.acquire(mSampleTime);
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private void startTracker() {
        Timber.tag(TAG).i("startTracker...");
        acquireWakelock();

        mSysCpuTracker = new SysCpuTracker();
        mSysCpuTracker.startTracker(mKeepSnapshots);

        HandlerThread thread = new HandlerThread("SysSampler");
        thread.start();
        mHandler = new Handler(thread.getLooper(), this);
        mHandler.sendEmptyMessage(MSG_SAMPLE);
        mHandler.sendEmptyMessageDelayed(MSG_SAMPLE, mSampleTime);
    }

    private void stopTracker() {
        Timber.tag(TAG).i("stopTracker...");
        mSysCpuTracker.stopTracker();
        SysCpuStat sysCpuUsage = mSysCpuTracker.getCpuUsage();
        Timber.tag(TAG).i("stats report (%s ~ %s, %d samples)",
                DateTimeUtils.INSTANCE.getReadableTimeStamp(mSysCpuTracker.getStartSysTime()),
                DateTimeUtils.INSTANCE.getReadableTimeStamp(mSysCpuTracker.getEndSysTime()),
                mSysCpuTracker.getSampleCount());
        Timber.tag(TAG).i("utime: %d, ntime: %d, stime: %d",
                sysCpuUsage.utime, sysCpuUsage.ntime, sysCpuUsage.stime);
        Timber.tag(TAG).i("cpu time used: %d, natural time used: %d",  sysCpuUsage.getTimeUsed(),
                mSysCpuTracker.getNaturalTimeUsed());

        List<SysCpuStat> snapshotList = mSysCpuTracker.getSnapshotList();
        if (snapshotList != null) {
            Timber.tag(TAG).i("snapshot list: %s", new Gson().toJson(snapshotList));
        }

        stopSelf();
        releaseWakeLock();
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == MSG_SAMPLE) {
            mSysCpuTracker.sample();
            if (mSysCpuTracker.getNaturalTimeUsed() >= mSampleTime) {
                mHandler.removeMessages(MSG_SAMPLE);
                stopTracker();
            } else {
                mHandler.sendEmptyMessageDelayed(MSG_SAMPLE, mSampleInterval);
            }
        }
        return false;
    }
}
