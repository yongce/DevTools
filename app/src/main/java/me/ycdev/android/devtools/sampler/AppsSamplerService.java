package me.ycdev.android.devtools.sampler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.utils.AppLogger;
import me.ycdev.android.devtools.utils.Constants;
import me.ycdev.android.devtools.utils.StringHelper;

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

public class AppsSamplerService extends Service implements Handler.Callback {
    private static final String TAG = "AppsSamplerService";

    private static final String EXTRA_PKG_NAMES = "extra.pkgs"; // ArrayList
    private static final String EXTRA_INTERVAL = "extra.interval"; // seconds

    private static final int MSG_SAMPLE = 100;

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private boolean mIsSampling;

    private ArrayList<String> mPkgNames;
    private int mIntervalSeconds;
    private ArrayList<FileWriter> mFileWriters;

    private AppsSetStat mPreAppsSetStat;

    private static SampleTaskInfo sTaskInfo;

    public static void startSamplerService(Context cxt, ArrayList<String> pkgNames,
            int intervalSeconds) {
        Intent intent = new Intent(cxt, AppsSamplerService.class);
        intent.putExtra(EXTRA_PKG_NAMES, pkgNames);
        intent.putExtra(EXTRA_INTERVAL, intervalSeconds);
        cxt.startService(intent);
    }

    public static void stopSamplerService(Context cxt) {
        Intent intent = new Intent(cxt, AppsSamplerService.class);
        cxt.stopService(intent);
    }

    public static void clearLogs() {
        File sdRoot = Environment.getExternalStorageDirectory();
        File appDir = new File(sdRoot, Constants.EXTERNAL_STORAGE_PATH_APP_ROOT);
        if (appDir.exists()) {
            File[] files = appDir.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static SampleTaskInfo getLastSampleTask() {
        return sTaskInfo;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandlerThread = new HandlerThread("AppsSampler");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), this);
    }

    @Override
    public void onDestroy() {
        stopSampler();
        mHandlerThread.getLooper().quit();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppLogger.i(TAG, "requested to start sampler service...");
        if (intent == null) {
            AppLogger.w(TAG, "sampling service cannot restart because of info lost");
            return super.onStartCommand(intent, flags, startId);
        }

        if (mIsSampling) {
            AppLogger.w(TAG, "sampling already started, ignore the request");
        } else {
            mPkgNames = intent.getStringArrayListExtra(EXTRA_PKG_NAMES);
            mIntervalSeconds = intent.getIntExtra(EXTRA_INTERVAL, 0);
            startSampler();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startSampler() {
        if (mPkgNames == null || mPkgNames.size() == 0 || mIntervalSeconds <= 0) {
            AppLogger.w(TAG, "cannot start sampler because of wrong parameters");
            return;
        }

        AppLogger.d(TAG, "start sampler...");

        sTaskInfo = new SampleTaskInfo();
        sTaskInfo.pkgNames = mPkgNames;
        sTaskInfo.sampleInterval = mIntervalSeconds;
        sTaskInfo.startTime = System.currentTimeMillis();
        sTaskInfo.isSampling = true;

        mFileWriters = new ArrayList<FileWriter>(mPkgNames.size());
        for (String pkgName : mPkgNames) {
            try {
                File sdRoot = Environment.getExternalStorageDirectory();
                File appDir = new File(sdRoot, Constants.EXTERNAL_STORAGE_PATH_APP_ROOT);
                appDir.mkdirs();
                File dataFile = new File(appDir, pkgName);
                FileWriter writer = new FileWriter(dataFile, true);
                AppStat.appendHeader(writer);
                writer.flush();
                mFileWriters.add(writer);
            } catch (IOException e) {
                AppLogger.w(TAG, "failed to write header: " + pkgName, e);
                return;
            }
        }

        mIsSampling = true;
        Notification notification = buildNotification();
        startForeground(Constants.NOTIFICATION_ID_PROC_MEM_SAMPLER, notification);

        mHandler.obtainMessage(MSG_SAMPLE).sendToTarget();
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
        if (msg.what == MSG_SAMPLE) {
            sampleSnapshot();
            mHandler.sendEmptyMessageDelayed(MSG_SAMPLE, mIntervalSeconds * 1000);
        } else {
            done = false;
        }
        return done;
    }

    private void sampleSnapshot() {
        AppLogger.i(TAG, "sample mem info snapshot begin...");

        AppsSetStat appsSetStat = AppsSetStat.createSnapshot(this, mPkgNames);
        if (mPreAppsSetStat != null) {
            AppsSetStat appsUsage = AppsSetStat.computeUsage(mPreAppsSetStat, appsSetStat);
            String timeStamp = StringHelper.formatDateTime(appsUsage.sampleTime);

            final int N = mPkgNames.size();
            for (int i = 0; i < N; i++) {
                String pkgName = mPkgNames.get(i);
                AppStat appStat = appsUsage.appsStat.get(pkgName);
                if (appStat == null) {
                    continue;
                }
                FileWriter writer = mFileWriters.get(i);
                try {
                    appStat.dumpStat(writer, timeStamp, appsUsage.clockTime);
                    writer.flush();
                } catch (IOException e) {
                    AppLogger.w(TAG, "ignored IO exception", e);
                }
            }

            sTaskInfo.sampleClockTime += appsUsage.clockTime;
            sTaskInfo.sampleCount++;
        }

        mPreAppsSetStat = appsSetStat;

        AppLogger.i(TAG, "sample mem info snapshot done");
    }

    private void stopSampler() {
        AppLogger.i(TAG, "requested to stop sample...");
        if (!mIsSampling) {
            return;
        }
        mHandler.removeMessages(MSG_SAMPLE);
        final int N = mFileWriters.size();
        for (int i = 0; i < N; i++) {
            FileWriter writer = mFileWriters.get(i);
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                AppLogger.w(TAG, "failed to flush data: " + mPkgNames.get(i), e);
            }
        }
        mPkgNames = null;
        mFileWriters.clear();
        sTaskInfo.isSampling = false;
        stopForeground(true);
    }
}


