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
import android.support.v4.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.utils.AppLogger;
import me.ycdev.android.devtools.utils.Constants;
import me.ycdev.android.devtools.utils.StringHelper;
import me.ycdev.androidlib.utils.DateTimeUtils;
import me.ycdev.androidlib.utils.IoUtils;
import me.ycdev.androidlib.utils.StorageUtils;

public class AppsSamplerService extends Service implements Handler.Callback {
    private static final String TAG = "AppsSamplerService";

    private static final String ACTION_START_SAMPLER = "action.start_sampler";
    private static final String ACTION_STOP_SAMPLER = "action.stop_sampler";
    private static final String ACTION_CREATE_REPORT = "action.create_report";
    private static final String ACTION_CLEAR_LOGS = "action.clear_logs";

    private static final String EXTRA_PKG_NAMES = "extra.pkgs"; // ArrayList
    private static final String EXTRA_INTERVAL = "extra.interval"; // seconds

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

    private static SampleTaskInfo sTaskInfo;

    public static void startSampler(Context cxt, ArrayList<String> pkgNames,
            int intervalSeconds) {
        Intent intent = new Intent(cxt, AppsSamplerService.class);
        intent.setAction(ACTION_START_SAMPLER);
        intent.putExtra(EXTRA_PKG_NAMES, pkgNames);
        intent.putExtra(EXTRA_INTERVAL, intervalSeconds);
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
        AppLogger.i(TAG, "Apps sampler service is creating...");
        mHandlerThread = new HandlerThread("AppsSampler");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), this);
    }

    @Override
    public void onDestroy() {
        AppLogger.i(TAG, "Apps sampler service is destroying...");
        mHandlerThread.getLooper().quit();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppLogger.i(TAG, "requested to start sampler service: " + intent);
        if (intent == null) {
            AppLogger.e(TAG, "sampling service cannot restart because of info lost");
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if (action.equals(ACTION_START_SAMPLER)) {
            ArrayList<String> pkgNames = intent.getStringArrayListExtra(EXTRA_PKG_NAMES);
            int sampleInterval = intent.getIntExtra(EXTRA_INTERVAL, 0);
            mHandler.obtainMessage(MSG_START_SAMPLER, startId, sampleInterval, pkgNames).sendToTarget();
        } else if (action.equals(ACTION_STOP_SAMPLER)) {
            mHandler.obtainMessage(MSG_STOP_SAMPLER, startId, 0).sendToTarget();
        } else if (action.equals(ACTION_CREATE_REPORT)) {
            mHandler.obtainMessage(MSG_CREATE_REPORT, startId, 0).sendToTarget();
        } else if (action.equals(ACTION_CLEAR_LOGS)) {
            mHandler.obtainMessage(MSG_CLEAR_LOGS, startId, 0).sendToTarget();
        } else {
            AppLogger.w(TAG, "unknown request: " + intent);
            mHandler.obtainMessage(MSG_UNKNOWN_ACTION, startId, 0).sendToTarget();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static String generateStatsFileName(String pkgName, long startTime) {
        return DateTimeUtils.generateFileName(startTime) + FILENAME_TAG_STATS + pkgName;
    }

    private static String generateReportFileName(long startTime) {
        return DateTimeUtils.generateFileName(startTime) + FILENAME_TAG_REPORT;
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
        builder.setSmallIcon(R.drawable.ic_launcher);
        return builder.build();
    }

    // Handler.Callback.handleMessage
    @Override
    public boolean handleMessage(Message msg) {
        boolean done = true;
        switch (msg.what) {
            case MSG_START_SAMPLER: {
                doStartSampler(msg.arg1, msg.arg2, (ArrayList<String>)msg.obj);
                break;
            }

            case MSG_SAMPLE_STATS: {
                doSampleSnapshot(sTaskInfo);
                mHandler.sendEmptyMessageDelayed(MSG_SAMPLE_STATS, sTaskInfo.sampleInterval * 1000);
                break;
            }

            case MSG_STOP_SAMPLER: {
                doStopSampler(sTaskInfo);
                stopSelf(msg.arg1);
                break;
            }

            case MSG_CREATE_REPORT: {
                if (sTaskInfo != null && sTaskInfo.isSampling) {
                    createSampleReport(sTaskInfo);
                } else {
                    createAllReports();
                    stopSelf(msg.arg1);
                }
                break;
            }

            case MSG_CLEAR_LOGS: {
                if (sTaskInfo != null && sTaskInfo.isSampling) {
                    AppLogger.w(TAG, "sampler is running, cannot clear logs");
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

    private void doStartSampler(int startId, int sampleInterval, ArrayList<String> pkgNames) {
        if (sTaskInfo != null && sTaskInfo.isSampling) {
            AppLogger.i(TAG, "the sampler is running, igonre the new request");
            return;
        }

        if (pkgNames == null || pkgNames.size() == 0 || sampleInterval <= 0) {
            AppLogger.w(TAG, "cannot start sampler because of wrong parameters");
            stopSelf(startId);
            return;
        }

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            AppLogger.w(TAG, "cannot start sampler because no SD card mounted");
            stopSelf(startId);
            return;
        }

        AppLogger.d(TAG, "start sampler...");

        SampleTaskInfo taskInfo = new SampleTaskInfo();
        taskInfo.pkgNames = pkgNames;
        taskInfo.sampleInterval = sampleInterval;
        taskInfo.startTime = System.currentTimeMillis();

        taskInfo.isSampling = true;
        taskInfo.fileWriters = new ArrayList<FileWriter>(pkgNames.size());
        for (String pkgName : pkgNames) {
            try {
                File sdRoot = Environment.getExternalStorageDirectory();
                File appDir = new File(sdRoot, Constants.EXTERNAL_STORAGE_PATH_APPS_SAMPLER);
                appDir.mkdirs();
                File dataFile = new File(appDir, generateStatsFileName(pkgName, taskInfo.startTime));
                FileWriter writer = new FileWriter(dataFile, true);
                AppStat.appendHeader(writer);
                writer.flush();
                taskInfo.fileWriters.add(writer);
            } catch (IOException e) {
                AppLogger.w(TAG, "failed to write header: " + pkgName, e);
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
        AppLogger.i(TAG, "sample stats snapshot begin...");

        AppsSetStat appsSetStat = AppsSetStat.createSnapshot(this, taskInfo.pkgNames);
        if (taskInfo.preAppsSetStat != null) {
            AppsSetStat appsUsage = AppsSetStat.computeUsage(taskInfo.preAppsSetStat, appsSetStat);
            String timeStamp = StringHelper.formatDateTime(appsUsage.sampleTime);

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
                    AppLogger.w(TAG, "ignored IO exception", e);
                }
            }

            taskInfo.sampleClockTime += appsUsage.clockTime;
            taskInfo.sampleCount++;
        }

        taskInfo.preAppsSetStat = appsSetStat;

        AppLogger.i(TAG, "sample stats snapshot done");
    }

    private void doStopSampler(SampleTaskInfo taskInfo) {
        AppLogger.i(TAG, "requested to stop sample...");
        if (!taskInfo.isSampling) {
            AppLogger.w(TAG, "sampler not running");
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
                AppLogger.w(TAG, "failed to flush data: " + taskInfo.pkgNames.get(i), e);
            }
        }

        taskInfo.isSampling = false;
        stopForeground(true);
    }

    private static void createSampleReport(SampleTaskInfo taskInfo) {
        File sdRoot = Environment.getExternalStorageDirectory();
        File appDir = new File(sdRoot, Constants.EXTERNAL_STORAGE_PATH_APPS_SAMPLER);
        appDir.mkdirs();
        File reportFile = new File(appDir, generateReportFileName(taskInfo.startTime));
        FileWriter writer = null;
        try {
            writer = new FileWriter(reportFile, false);
            for (String pkgName : taskInfo.pkgNames) {
                File statFile = new File(appDir, AppsSamplerService.generateStatsFileName(
                        pkgName, taskInfo.startTime));
                BufferedReader reader = new BufferedReader(new FileReader(statFile));
                AppStatReport appReport = new AppStatReport(pkgName);
                String line = null;
                while ((line = reader.readLine()) != null) {
                    StatFileLine entry = AppStat.readStat(line);
                    if (entry == null) {
                        continue; // skip header line
                    }

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
                    appReport.dumpStat(writer);
                } else {
                    AppLogger.w(TAG, "no stats for " + appReport.pkgName);
                }
            }
            writer.flush();
        } catch (IOException e) {
            AppLogger.w(TAG, "failed to dump stats report", e);
        } finally {
            IoUtils.closeQuietly(writer);
        }
    }

    private static void createAllReports() {
        File sdRoot = Environment.getExternalStorageDirectory();
        File appDir = new File(sdRoot, Constants.EXTERNAL_STORAGE_PATH_APPS_SAMPLER);
        File[] allFiles = appDir.listFiles();
        if (allFiles == null || allFiles.length == 0) {
            return;
        }

        // get all stats files
        HashMap<String, SampleTaskInfo> allTasks = new HashMap<String, SampleTaskInfo>();
        for (File file : allFiles) {
            String fileName = file.getName();
            int statsTagIndex = fileName.indexOf(FILENAME_TAG_STATS);
            if (statsTagIndex == -1) {
                AppLogger.i(TAG, "not stats file, skip: " + fileName);
                continue;
            }

            String timeStr = fileName.substring(0, statsTagIndex);
            SampleTaskInfo taskInfo = allTasks.get(timeStr);
            if (taskInfo == null) {
                taskInfo = new SampleTaskInfo();
                try {
                    taskInfo.startTime = DateTimeUtils.parseFileName(timeStr);
                } catch (ParseException e) {
                    AppLogger.w(TAG, "bad file name when parsing time: " + fileName, e);
                    continue;
                }
                taskInfo.pkgNames = new ArrayList<String>();
                allTasks.put(timeStr, taskInfo);
            }
            String pkgName = fileName.substring(statsTagIndex + FILENAME_TAG_STATS.length());
            taskInfo.pkgNames.add(pkgName);
        }

        // create all reports
        for (SampleTaskInfo taskInfo : allTasks.values()) {
            createSampleReport(taskInfo);
        }
    }

    private static void clearLogs() {
        File sdRoot = Environment.getExternalStorageDirectory();
        File appDir = new File(sdRoot, Constants.EXTERNAL_STORAGE_PATH_APPS_SAMPLER);
        if (appDir.exists()) {
            File[] files = appDir.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
    }

}


