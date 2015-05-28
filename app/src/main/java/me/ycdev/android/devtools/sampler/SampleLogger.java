package me.ycdev.android.devtools.sampler;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import me.ycdev.android.devtools.utils.AppLogger;
import me.ycdev.android.lib.common.utils.DateTimeUtils;
import me.ycdev.android.lib.common.utils.IoUtils;

public class SampleLogger implements Closeable {
    private static final String TAG = "SampleLogger";

    private static final String SAMPLER_LOG_FILENAME = "sampler.log";
    private static final String STAT_FILE_COLUMNS_SEP = "\t";

    private FileWriter mLogWriter;

    public SampleLogger() {
        try {
            File logFile = SamplerUtils.getFileForSampler(SAMPLER_LOG_FILENAME, true);
            mLogWriter = new FileWriter(logFile, true);
        } catch (IOException e) {
            AppLogger.w(TAG, "failed to create sampler log file", e);
        }
    }

    private void addLog(String tag, String msg) {
        String timeStamp = DateTimeUtils.getReadableTimeStamp(System.currentTimeMillis());
        try {
            mLogWriter.append(timeStamp).append(STAT_FILE_COLUMNS_SEP)
                    .append(tag).append(STAT_FILE_COLUMNS_SEP)
                    .append(msg).append("\n");
            mLogWriter.flush();
        } catch (IOException e) {
            AppLogger.w(TAG, "ignored IO exception", e);
        }
    }

    public void logDebug(String tag, String msg) {
        AppLogger.d(tag, msg);
        addLog(tag, msg);
    }

    public void logInfo(String tag, String msg) {
        AppLogger.i(tag, msg);
        addLog(tag, msg);
    }

    public void logWarning(String tag, String msg) {
        AppLogger.w(tag, msg);
        addLog(tag, msg);
    }

    public void logError(String tag, String msg) {
        AppLogger.e(tag, msg);
        addLog(tag, msg);
    }

    @Override
    public void close() throws IOException {
        IoUtils.closeQuietly(mLogWriter);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}
