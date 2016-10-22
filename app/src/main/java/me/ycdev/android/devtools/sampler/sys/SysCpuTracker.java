package me.ycdev.android.devtools.sampler.sys;

import android.os.SystemClock;

import java.util.LinkedList;
import java.util.List;

import me.ycdev.android.arch.utils.AppLogger;
import me.ycdev.android.devtools.sampler.cpu.CpuUtils;
import me.ycdev.android.devtools.sampler.cpu.SysCpuStat;

public class SysCpuTracker {
    private static final String TAG = "SysCpuTracker";

    private long mStartSysTime;
    private long mStartClockTime;
    private long mEndSysTime;
    private long mEndClockTime;

    private SysCpuStat mBaseCpuStat;
    private SysCpuStat mCpuUsage;
    private int mSampleCount;
    private List<SysCpuStat> mSnapshotList;

    public void startTracker(boolean keepSnapshot) {
        mStartSysTime = 0;
        mStartClockTime = 0;
        mEndSysTime = 0;
        mEndClockTime = 0;

        mBaseCpuStat = null;
        mCpuUsage = new SysCpuStat();
        mSampleCount = 0;
        if (keepSnapshot) {
            mSnapshotList = new LinkedList<>();
        } else {
            mSnapshotList = null;
        }
    }

    public void sample() {
        if (mEndClockTime > 0) {
            AppLogger.w(TAG, "tracker already stopped");
            return;
        }

        SysCpuStat sysCpu = CpuUtils.getCpuStat();
        if (sysCpu == null) {
            AppLogger.w(TAG, "failed to get CPU stats");
            return;
        }
        if (mSnapshotList != null) {
            mSnapshotList.add(sysCpu);
            AppLogger.i(TAG, "snapshot: " + sysCpu);
        }

        if (mBaseCpuStat != null) {
            mCpuUsage.sum(mBaseCpuStat, sysCpu);
            mSampleCount++;
        } else {
            mStartSysTime = System.currentTimeMillis();
            mStartClockTime = SystemClock.elapsedRealtime();
        }
        mBaseCpuStat = sysCpu;
    }

    public void stopTracker() {
        mEndSysTime = System.currentTimeMillis();
        mEndClockTime = SystemClock.elapsedRealtime();
    }

    public SysCpuStat getCpuUsage() {
        return mCpuUsage;
    }

    public long getStartSysTime() {
        return mStartSysTime;
    }

    public long getEndSysTime() {
        return mEndSysTime;
    }

    public long getNaturalTimeUsed() {
        if (mEndClockTime > 0) {
            return mEndClockTime - mStartClockTime;
        } else {
            return SystemClock.elapsedRealtime() - mStartClockTime;
        }
    }

    public int getSampleCount() {
        return mSampleCount;
    }

    public List<SysCpuStat> getSnapshotList() {
        return mSnapshotList;
    }
}
