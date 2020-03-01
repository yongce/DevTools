package me.ycdev.android.devtools.sampler;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.core.app.NotificationCompat;
import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.utils.Constants;
import me.ycdev.android.lib.common.utils.DateTimeUtils;
import me.ycdev.android.lib.common.utils.IoUtils;
import me.ycdev.android.lib.common.wrapper.IntentHelper;
import timber.log.Timber;

public class AppsSamplerService extends Service implements Handler.Callback {
    private static final String TAG = "AppsSamplerService";

    private static final String ACTION_START_SAMPLER = "action.start_sampler";
    private static final String ACTION_STOP_SAMPLER = "action.stop_sampler";
    private static final String ACTION_CREATE_REPORT = "action.create_report";
    private static final String ACTION_CLEAR_LOGS = "action.clear_logs";

    private static final String EXTRA_PKG_NAMES = "extra.pkgs"; // ArrayList
    private static final String EXTRA_INTERVAL = "extra.interval"; // seconds
    private static final String EXTRA_PERIOD = "extra.period"; // minutes

    private static final String FILENAME_SAMPLE_TASK_BACKUP = "task-backup.txt";
    private static final String FILENAME_TAG_STATS = "-stats-";
    private static final String FILENAME_TAG_REPORT = "-report";

    private static final int MSG_UNKNOWN_ACTION = 100;
    private static final int MSG_START_SAMPLER = 101;
    private static final int MSG_SAMPLE_STATS = 102;
    private static final int MSG_STOP_SAMPLER = 103;
    private static final int MSG_CLEAR_LOGS = 104;
    private static final int MSG_CREATE_REPORT = 105;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private SampleLogger mSampleLogger;

    private static SampleTaskInfo sTaskInfo;

    public static void startSampler(Context cxt, ArrayList<String> pkgNames,
            int intervalSeconds, int periodMinutes) {
        // delete task info backup if exist
        File backupFile = SamplerUtils.getFileForSampler(FILENAME_SAMPLE_TASK_BACKUP, false);
        //noinspection ResultOfMethodCallIgnored
        backupFile.delete();

        Intent intent = new Intent(cxt, AppsSamplerService.class);
        intent.setAction(ACTION_START_SAMPLER);
        intent.putExtra(EXTRA_PKG_NAMES, pkgNames);
        intent.putExtra(EXTRA_INTERVAL, intervalSeconds);
        intent.putExtra(EXTRA_PERIOD, periodMinutes);
        cxt.startService(intent);
    }

    public static void stopSampler(Context cxt) {
        Intent intent = new Intent(cxt, AppsSamplerService.class);
        intent.setAction(ACTION_STOP_SAMPLER);
        cxt.startService(intent);
    }

    public static void clearLogs(Context cxt) {
        Intent intent = new Intent(cxt, AppsSamplerService.class);
        intent.setAction(ACTION_CLEAR_LOGS);
        cxt.startService(intent);
    }

    public static void createSampleReport(Context cxt) {
        Intent intent = new Intent(cxt, AppsSamplerService.class);
        intent.setAction(ACTION_CREATE_REPORT);
        cxt.startService(intent);
    }

    public static SampleTaskInfo getLastSampleTask() {
        return sTaskInfo;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.tag(TAG).i("Apps sampler service is creating...");
        mHandlerThread = new HandlerThread("AppsSampler");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), this);
        mSampleLogger = new SampleLogger();
    }

    @Override
    public void onDestroy() {
        Timber.tag(TAG).i("Apps sampler service is destroying...");
        mHandlerThread.getLooper().quit();
        IoUtils.INSTANCE.closeQuietly(mSampleLogger);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSampleLogger.logInfo(TAG, "requested to start sampler service: " + intent);
        if (intent == null) {
            mSampleLogger.logInfo(TAG, "sampling service restart and info lost");
            SampleTaskInfo taskInfo = restoreSampleTaskInfo();
            if (taskInfo == null) {
                mSampleLogger.logError(TAG, "cannot restore the sample task");
                stopSelf();
            } else {
                mSampleLogger.logInfo(TAG, "restore sample: " + taskInfo.backupTaskInfo());
                mHandler.obtainMessage(MSG_START_SAMPLER, startId, 0, taskInfo).sendToTarget();
            }
            return START_STICKY;
        }

        String action = intent.getAction();
        if (action.equals(ACTION_START_SAMPLER)) {
            SampleTaskInfo taskInfo = restoreSampleTaskInfo();
            if (taskInfo == null) {
                taskInfo = new SampleTaskInfo();
                taskInfo.pkgNames = IntentHelper.INSTANCE.getStringArrayListExtra(intent, EXTRA_PKG_NAMES);
                taskInfo.sampleInterval = IntentHelper.INSTANCE.getIntExtra(intent, EXTRA_INTERVAL, 0);
                taskInfo.samplePeriod = IntentHelper.INSTANCE.getIntExtra(intent, EXTRA_PERIOD, 0);
                taskInfo.startTime = System.currentTimeMillis();
            } else {
                mSampleLogger.logInfo(TAG, "start sampler, use backup: " + taskInfo.backupTaskInfo());
            }
            mHandler.obtainMessage(MSG_START_SAMPLER, startId, 0, taskInfo).sendToTarget();
        } else if (action.equals(ACTION_STOP_SAMPLER)) {
            mHandler.obtainMessage(MSG_STOP_SAMPLER, startId, 0).sendToTarget();
        } else if (action.equals(ACTION_CREATE_REPORT)) {
            mHandler.obtainMessage(MSG_CREATE_REPORT, startId, 0).sendToTarget();
        } else if (action.equals(ACTION_CLEAR_LOGS)) {
            mHandler.obtainMessage(MSG_CLEAR_LOGS, startId, 0).sendToTarget();
        } else {
            mSampleLogger.logWarning(TAG, "unknown request: " + intent);
            mHandler.obtainMessage(MSG_UNKNOWN_ACTION, startId, 0).sendToTarget();
        }
        return START_STICKY;
    }

    private static void backupSampleTaskInfo(SampleTaskInfo taskInfo) {
        if (taskInfo == null) {
            Timber.tag(TAG).w("cannot backup sample task info, no task info yet");
            return;
        }

        String taskInfoBackup = taskInfo.backupTaskInfo();
        if (taskInfoBackup == null) {
            Timber.tag(TAG).w("failed to create sample task info backup");
            return;
        }

        try {
            File backupFile = SamplerUtils.getFileForSampler(FILENAME_SAMPLE_TASK_BACKUP, true);
            IoUtils.INSTANCE.saveAsFile(taskInfoBackup, backupFile.getAbsolutePath());
        } catch (IOException e) {
            Timber.tag(TAG).w(e, "failed to save sample task info into backup file");
        }
    }

    private static SampleTaskInfo restoreSampleTaskInfo() {
        File backupFile = SamplerUtils.getFileForSampler(FILENAME_SAMPLE_TASK_BACKUP, false);
        if (!backupFile.exists()) {
            return null;
        }

        String taskInfoBackup;
        try {
            taskInfoBackup = IoUtils.INSTANCE.readAllLines(backupFile.getAbsolutePath());
        } catch (IOException e) {
            Timber.tag(TAG).w(e, "failed to create sampler log file");
            return null;
        }
        if (TextUtils.isEmpty(taskInfoBackup)) {
            Timber.tag(TAG).w("no task info backup");
            return null;
        }

        return SampleTaskInfo.restoreTaskInfo(taskInfoBackup);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static String generateStatsFileName(String pkgName, long startTime) {
        return DateTimeUtils.INSTANCE.generateFileName(startTime) + FILENAME_TAG_STATS + pkgName + ".txt";
    }

    private static String generateReportFileName(long startTime) {
        return DateTimeUtils.INSTANCE.generateFileName(startTime) + FILENAME_TAG_REPORT + ".txt";
    }

    private Notification buildNotification() {
        Intent samplerIntent = new Intent(this, AppsSamplerActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, samplerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String status = getString(R.string.apps_sampler_notification_ticker);
        String title = getString(R.string.apps_sampler_module_title);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setTicker(status);
        builder.setContentTitle(title);
        builder.setContentText(status);
        builder.setContentIntent(pi);
        builder.setOngoing(true);
        builder.setAutoCancel(false);
        builder.setWhen(System.currentTimeMillis());
        //builder.setLargeIcon(appIcon)
        builder.setSmallIcon(R.mipmap.ic_launcher);
        return builder.build();
    }

    // Handler.Callback.handleMessage
    @Override
    public boolean handleMessage(Message msg) {
        boolean done = true;
        switch (msg.what) {
            case MSG_START_SAMPLER: {
                doStartSampler(msg.arg1, (SampleTaskInfo)msg.obj);
                break;
            }

            case MSG_SAMPLE_STATS: {
                doSampleSnapshot(sTaskInfo);
                if (sTaskInfo.samplePeriod == 0 ||
                        System.currentTimeMillis() - sTaskInfo.startTime < sTaskInfo.samplePeriod * (60 * 1000)) {
                    mHandler.sendEmptyMessageDelayed(MSG_SAMPLE_STATS, sTaskInfo.sampleInterval * 1000);
                } else {
                    createSampleReport(this);
                    stopSampler(this);
                }
                break;
            }

            case MSG_STOP_SAMPLER: {
                doStopSampler(sTaskInfo);
                stopSelf(msg.arg1);
                break;
            }

            case MSG_CREATE_REPORT: {
                if (sTaskInfo != null && sTaskInfo.isSampling) {
                    createSampleReport(this, sTaskInfo);
                } else {
                    createAllReports(this);
                    stopSelf(msg.arg1);
                }
                break;
            }

            case MSG_CLEAR_LOGS: {
                if (sTaskInfo != null && sTaskInfo.isSampling) {
                    mSampleLogger.logWarning(TAG, "sampler is running, cannot clear logs");
                } else {
                    clearLogs();
                    stopSelf(msg.arg1);
                }
                break;
            }

            default: {
                done = false;
                break;
            }
        }
        return done;
    }

    private void doStartSampler(int startId, SampleTaskInfo taskInfo) {
        if (sTaskInfo != null && sTaskInfo.isSampling) {
            Timber.tag(TAG).i("the sampler is running, igonre the new request");
            return;
        }

        mSampleLogger.logInfo(TAG, "try to start sampler, interval: " + taskInfo.sampleInterval
                + ", period: " + taskInfo.samplePeriod);
        if (taskInfo.pkgNames == null || taskInfo.pkgNames.size() == 0 || taskInfo.sampleInterval <= 0) {
            mSampleLogger.logWarning(TAG, "cannot start sampler because of wrong parameters");
            stopSelf(startId);
            return;
        }

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mSampleLogger.logWarning(TAG, "cannot start sampler because no SD card mounted");
            stopSelf(startId);
            return;
        }

        mSampleLogger.logDebug(TAG, "do start sampler...");

        taskInfo.isSampling = true;
        taskInfo.fileWriters = new ArrayList<>(taskInfo.pkgNames.size());
        for (String pkgName : taskInfo.pkgNames) {
            try {
                File dataFile = SamplerUtils.getFileForSampler(
                        generateStatsFileName(pkgName, taskInfo.startTime), true);
                FileWriter writer = new FileWriter(dataFile, true);
                AppStat.appendHeader(writer);
                writer.flush();
                taskInfo.fileWriters.add(writer);
            } catch (IOException e) {
                Timber.tag(TAG).w(e, "failed to write header: %s", pkgName);
                stopSelf(startId);
                return;
            }
        }

        sTaskInfo = taskInfo;
        Notification notification = buildNotification();
        startForeground(Constants.NOTIFICATION_ID_PROC_MEM_SAMPLER, notification);

        mHandler.obtainMessage(MSG_SAMPLE_STATS).sendToTarget();
    }

    private void doSampleSnapshot(SampleTaskInfo taskInfo) {
        Timber.tag(TAG).i("sample stats snapshot begin...");

        AppsSetStat appsSetStat = AppsSetStat.createSnapshot(this, taskInfo.pkgNames);
        if (taskInfo.preAppsSetStat != null) {
            AppsSetStat appsUsage = AppsSetStat.computeUsage(taskInfo.preAppsSetStat, appsSetStat);
            String timeStamp = DateTimeUtils.INSTANCE.getReadableTimeStamp(appsUsage.sampleTime);

            final int N = taskInfo.pkgNames.size();
            for (int i = 0; i < N; i++) {
                String pkgName = taskInfo.pkgNames.get(i);
                AppStat appStat = appsUsage.appsStat.get(pkgName);
                if (appStat == null) {
                    continue;
                }
                FileWriter writer = taskInfo.fileWriters.get(i);
                try {
                    appStat.dumpStat(writer, timeStamp, appsUsage.clockTime);
                    writer.flush();
                } catch (IOException e) {
                    Timber.tag(TAG).w(e, "ignored IO exception");
                }
            }

            taskInfo.sampleClockTime += appsUsage.clockTime;
            taskInfo.sampleCount++;
        }

        taskInfo.preAppsSetStat = appsSetStat;

        // backup the current task info state
        backupSampleTaskInfo(taskInfo);

        Timber.tag(TAG).i("sample stats snapshot done");
    }

    private void doStopSampler(SampleTaskInfo taskInfo) {
        mSampleLogger.logInfo(TAG, "requested to stop sample...");
        if (!taskInfo.isSampling) {
            mSampleLogger.logWarning(TAG, "sampler not running");
            return;
        }
        mHandler.removeMessages(MSG_SAMPLE_STATS);

        final int N = taskInfo.fileWriters.size();
        for (int i = 0; i < N; i++) {
            FileWriter writer = taskInfo.fileWriters.get(i);
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                Timber.tag(TAG).w(e, "failed to flush data: %s", taskInfo.pkgNames.get(i));
            }
        }

        File backupFile = SamplerUtils.getFileForSampler(FILENAME_SAMPLE_TASK_BACKUP, false);
        //noinspection ResultOfMethodCallIgnored
        backupFile.delete();

        taskInfo.isSampling = false;
        stopForeground(true);
    }

    private static void createSampleReport(Context cxt, SampleTaskInfo taskInfo) {
        File appDir = SamplerUtils.getSamplerFolder();
        File reportFile = new File(appDir, generateReportFileName(taskInfo.startTime));
        FileWriter writer = null;
        try {
            writer = new FileWriter(reportFile, false);
            for (String pkgName : taskInfo.pkgNames) {
                File statFile = new File(appDir, AppsSamplerService.generateStatsFileName(
                        pkgName, taskInfo.startTime));
                BufferedReader reader = new BufferedReader(new FileReader(statFile));
                try {
                    AppStatReport appReport = new AppStatReport(pkgName);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        StatFileLine entry = AppStat.readStat(line);
                        if (entry == null) {
                            continue; // skip header line
                        }

                        if (appReport.sysTimeStampStart == null) {
                            appReport.sysTimeStampStart = entry.sysTimeStamp;
                        }
                        appReport.sysTimeStampEnd = entry.sysTimeStamp;

                        appReport.sampleCount++;
                        appReport.totalTimeUsage += entry.timeUsage;
                        appReport.totalCpuTime += entry.cpuTime;
                        if (entry.processCount > appReport.maxProcessCount) {
                            appReport.maxProcessCount = entry.processCount;
                        }

                        if (appReport.minMemPss == 0 || entry.memPss < appReport.minMemPss) {
                            appReport.minMemPss = entry.memPss;
                        } else if (entry.memPss > appReport.maxMemPss) {
                            appReport.maxMemPss = entry.memPss;
                        }
                        appReport.totalMemPss += entry.memPss;

                        if (appReport.minMemPrivate == 0 || entry.memPrivate < appReport.minMemPrivate) {
                            appReport.minMemPrivate = entry.memPrivate;
                        } else if (entry.memPrivate > appReport.maxMemPrivate) {
                            appReport.maxMemPrivate = entry.memPrivate;
                        }
                        appReport.totalMemPrivate += entry.memPrivate;

                        appReport.totalTrafficRecv += entry.trafficRecv;
                        appReport.totalTrafficSend += entry.trafficSend;
                    }
                    if (appReport.sampleCount > 0) {
                        appReport.dumpStat(cxt, writer);
                    } else {
                        Timber.tag(TAG).w("no stats for %s", appReport.pkgName);
                    }
                } finally {
                    IoUtils.INSTANCE.closeQuietly(reader);
                }
            }
            writer.flush();
        } catch (IOException e) {
            Timber.tag(TAG).w(e, "failed to dump stats report");
        } finally {
            IoUtils.INSTANCE.closeQuietly(writer);
        }
    }

    private static void createAllReports(Context cxt) {
        File appDir = SamplerUtils.getSamplerFolder();
        File[] allFiles = appDir.listFiles();
        if (allFiles == null || allFiles.length == 0) {
            return;
        }

        // get all stats files
        HashMap<String, SampleTaskInfo> allTasks = new HashMap<>();
        for (File file : allFiles) {
            String fileName = file.getName();
            int statsTagIndex = fileName.indexOf(FILENAME_TAG_STATS);
            if (statsTagIndex == -1) {
                Timber.tag(TAG).i("not stats file, skip: %s", fileName);
                continue;
            }

            String timeStr = fileName.substring(0, statsTagIndex);
            SampleTaskInfo taskInfo = allTasks.get(timeStr);
            if (taskInfo == null) {
                taskInfo = new SampleTaskInfo();
                try {
                    taskInfo.startTime = DateTimeUtils.INSTANCE.parseFileName(timeStr);
                } catch (ParseException e) {
                    Timber.tag(TAG).w(e, "bad file name when parsing time: %s", fileName);
                    continue;
                }
                taskInfo.pkgNames = new ArrayList<>();
                allTasks.put(timeStr, taskInfo);
            }
            String pkgName = fileName.substring(statsTagIndex + FILENAME_TAG_STATS.length());
            taskInfo.pkgNames.add(pkgName);
        }

        // create all reports
        for (SampleTaskInfo taskInfo : allTasks.values()) {
            createSampleReport(cxt, taskInfo);
        }
    }

    private static void clearLogs() {
        File appDir = SamplerUtils.getSamplerFolder();
        if (appDir.exists()) {
            File[] files = appDir.listFiles();
            for (File file : files) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
    }

}


