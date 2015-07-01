package me.ycdev.android.devtools.sampler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.util.ArrayList;

import me.ycdev.android.arch.utils.AppLogger;

class SampleTaskInfo {
    private static final String TAG = "SampleTaskInfo";

    private static final String KEY_PKG_COUNT = "pkg_count";
    private static final String KEY_PKG_NAME_PREFIX = "pkg_";
    private static final String KEY_SAMPLE_INTERVAL = "interval";
    private static final String KEY_SAMPLE_PERIOD = "period";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_SAMPLE_CLOCK_TIME = "sample_clock_time";
    private static final String KEY_SAMPLE_COUNT = "sample_count";

    public ArrayList<String> pkgNames;
    public int sampleInterval; // in seconds
    public int samplePeriod; // in minutes
    public long startTime;
    public long sampleClockTime;
    public int sampleCount;

    public boolean isSampling;
    public ArrayList<FileWriter> fileWriters;
    public AppsSetStat preAppsSetStat;

    public String backupTaskInfo() {
        try {
            JSONObject json = new JSONObject();
            final int N = pkgNames.size();
            json.put(KEY_PKG_COUNT, N);
            for (int i = 0; i < N; i++) {
                json.put(KEY_PKG_NAME_PREFIX + i, pkgNames.get(i));
            }
            json.put(KEY_SAMPLE_INTERVAL, sampleInterval);
            json.put(KEY_SAMPLE_PERIOD, samplePeriod);
            json.put(KEY_START_TIME, startTime);
            json.put(KEY_SAMPLE_CLOCK_TIME, sampleClockTime);
            json.put(KEY_SAMPLE_COUNT, sampleCount);
            return json.toString();
        } catch (JSONException e) {
            AppLogger.w(TAG, "failed to create sample task info backup");
        }
        return null;
    }

    public static SampleTaskInfo restoreTaskInfo(String backup) {
        try {
            JSONObject json = new JSONObject(backup);
            SampleTaskInfo taskInfo = new SampleTaskInfo();
            taskInfo.pkgNames = new ArrayList<>();
            final int N = json.getInt(KEY_PKG_COUNT);
            for (int i = 0; i < N; i++) {
                taskInfo.pkgNames.add(json.getString(KEY_PKG_NAME_PREFIX + i));
            }
            taskInfo.sampleInterval = json.getInt(KEY_SAMPLE_INTERVAL);
            taskInfo.samplePeriod = json.getInt(KEY_SAMPLE_PERIOD);
            taskInfo.startTime = json.getLong(KEY_START_TIME);
            taskInfo.sampleClockTime = json.getLong(KEY_SAMPLE_CLOCK_TIME);
            taskInfo.sampleCount = json.getInt(KEY_SAMPLE_COUNT);
            return taskInfo;
        } catch (JSONException e) {
            AppLogger.w(TAG, "failed to restore sample task info from backup");
        }
        return null;
    }
}
