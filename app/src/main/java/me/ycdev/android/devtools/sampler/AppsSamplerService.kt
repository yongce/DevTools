package me.ycdev.android.devtools.sampler

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Handler.Callback
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.utils.AppConstants
import me.ycdev.android.devtools.utils.AppConstants.NOTIFICATION_CHANNEL_SAMPLER
import me.ycdev.android.lib.common.utils.DateTimeUtils
import me.ycdev.android.lib.common.utils.IoUtils
import me.ycdev.android.lib.common.wrapper.IntentHelper
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.text.ParseException
import java.util.ArrayList
import java.util.HashMap
import kotlin.collections.set

class AppsSamplerService : Service(), Callback {
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var sampleLogger: SampleLogger

    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).i("Apps sampler service is creating...")

        handlerThread = HandlerThread("AppsSampler")
        handlerThread.start()
        handler = Handler(handlerThread.looper, this)
        sampleLogger = SampleLogger()
    }

    override fun onDestroy() {
        Timber.tag(TAG).i("Apps sampler service is destroying...")
        handlerThread.looper.quit()
        IoUtils.closeQuietly(sampleLogger)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sampleLogger.logInfo(TAG, "requested to start sampler service: $intent")
        if (intent == null) {
            sampleLogger.logInfo(TAG, "sampling service restart and info lost")
            val taskInfo = restoreSampleTaskInfo()
            if (taskInfo == null) {
                sampleLogger.logError(TAG, "cannot restore the sample task")
                stopSelf()
            } else {
                sampleLogger.logInfo(TAG, "restore sample: " + taskInfo.backupTaskInfo())
                handler.obtainMessage(MSG_START_SAMPLER, startId, 0, taskInfo).sendToTarget()
            }
            return START_STICKY
        }

        val action = intent.action
        if (action == ACTION_START_SAMPLER) {
            var taskInfo = restoreSampleTaskInfo()
            if (taskInfo == null) {
                taskInfo = SampleTaskInfo()
                IntentHelper.getStringArrayListExtra(intent, EXTRA_PKG_NAMES)?.let {
                    taskInfo.pkgNames.addAll(it)
                }
                taskInfo.sampleInterval = IntentHelper.getIntExtra(intent, EXTRA_INTERVAL, 0)
                taskInfo.samplePeriod = IntentHelper.getIntExtra(intent, EXTRA_PERIOD, 0)
                taskInfo.startTime = System.currentTimeMillis()
            } else {
                sampleLogger.logInfo(TAG, "start sampler, use backup: " + taskInfo.backupTaskInfo())
            }
            handler.obtainMessage(MSG_START_SAMPLER, startId, 0, taskInfo).sendToTarget()
        } else if (action == ACTION_STOP_SAMPLER) {
            handler.obtainMessage(MSG_STOP_SAMPLER, startId, 0).sendToTarget()
        } else if (action == ACTION_CREATE_REPORT) {
            handler.obtainMessage(MSG_CREATE_REPORT, startId, 0).sendToTarget()
        } else if (action == ACTION_CLEAR_LOGS) {
            handler.obtainMessage(MSG_CLEAR_LOGS, startId, 0).sendToTarget()
        } else {
            sampleLogger.logWarning(TAG, "unknown request: $intent")
            handler.obtainMessage(MSG_UNKNOWN_ACTION, startId, 0).sendToTarget()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val notifyMgr = getSystemService(NotificationManager::class.java) ?: return
        var channel = notifyMgr.getNotificationChannel(NOTIFICATION_CHANNEL_SAMPLER)
        if (channel == null) {
            channel = NotificationChannel(
                NOTIFICATION_CHANNEL_SAMPLER,
                getString(R.string.apps_sampler_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            notifyMgr.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val samplerIntent = Intent(this, AppsSamplerActivity::class.java)
        val pi = PendingIntent.getActivity(
            this, 0, samplerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val status = getString(R.string.apps_sampler_notification_ticker)
        val title = getString(R.string.apps_sampler_module_title)
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_SAMPLER)
            .setTicker(status)
            .setContentTitle(title)
            .setContentText(status)
            .setContentIntent(pi)
            .setOngoing(true)
            .setAutoCancel(false)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    // Handler.Callback.handleMessage
    override fun handleMessage(msg: Message): Boolean {
        var done = true
        when (msg.what) {
            MSG_START_SAMPLER -> {
                doStartSampler(msg.arg1, msg.obj as SampleTaskInfo)
            }
            MSG_SAMPLE_STATS -> {
                doSampleSnapshot(lastSampleTask)
                if (lastSampleTask!!.samplePeriod == 0 ||
                    System.currentTimeMillis() - lastSampleTask!!.startTime < lastSampleTask!!.samplePeriod * (60 * 1000)
                ) {
                    handler.sendEmptyMessageDelayed(
                        MSG_SAMPLE_STATS,
                        lastSampleTask!!.sampleInterval * 1000.toLong()
                    )
                } else {
                    createSampleReport(this)
                    stopSampler(this)
                }
            }
            MSG_STOP_SAMPLER -> {
                doStopSampler(lastSampleTask)
                stopSelf(msg.arg1)
            }
            MSG_CREATE_REPORT -> {
                if (lastSampleTask != null && lastSampleTask!!.isSampling) {
                    createSampleReport(this, lastSampleTask!!)
                } else {
                    createAllReports(this)
                    stopSelf(msg.arg1)
                }
            }
            MSG_CLEAR_LOGS -> {
                if (lastSampleTask != null && lastSampleTask!!.isSampling) {
                    sampleLogger.logWarning(TAG, "sampler is running, cannot clear logs")
                } else {
                    clearLogs()
                    stopSelf(msg.arg1)
                }
            }
            else -> {
                done = false
            }
        }
        return done
    }

    private fun doStartSampler(startId: Int, taskInfo: SampleTaskInfo) {
        if (lastSampleTask != null && lastSampleTask!!.isSampling) {
            Timber.tag(TAG).i("the sampler is running, igonre the new request")
            return
        }
        sampleLogger.logInfo(
            TAG,
            "try to start sampler, interval: " + taskInfo.sampleInterval +
                    ", period: " + taskInfo.samplePeriod
        )
        if (taskInfo.pkgNames.size == 0 || taskInfo.sampleInterval <= 0) {
            sampleLogger.logWarning(TAG, "cannot start sampler because of wrong parameters")
            stopSelf(startId)
            return
        }
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            sampleLogger.logWarning(TAG, "cannot start sampler because no SD card mounted")
            stopSelf(startId)
            return
        }
        sampleLogger.logDebug(TAG, "do start sampler...")
        taskInfo.isSampling = true
        taskInfo.fileWriters = ArrayList(taskInfo.pkgNames.size)
        for (pkgName in taskInfo.pkgNames) {
            try {
                val dataFile = SamplerUtils.getFileForSampler(
                    generateStatsFileName(pkgName, taskInfo.startTime),
                    true
                )
                val writer = FileWriter(dataFile, true)
                AppStat.appendHeader(writer)
                writer.flush()
                taskInfo.fileWriters!!.add(writer)
            } catch (e: IOException) {
                Timber.tag(TAG).w(e, "failed to write header: %s", pkgName)
                stopSelf(startId)
                return
            }
        }
        lastSampleTask = taskInfo
        createChannelIfNeeded()
        val notification = buildNotification()
        startForeground(
            AppConstants.NOTIFICATION_ID_PROC_MEM_SAMPLER,
            notification
        )
        handler.obtainMessage(MSG_SAMPLE_STATS).sendToTarget()
    }

    private fun doSampleSnapshot(taskInfo: SampleTaskInfo?) {
        Timber.tag(TAG).i("sample stats snapshot begin...")
        val appsSetStat: AppsSetStat = AppsSetStat.createSnapshot(this, taskInfo!!.pkgNames)
        if (taskInfo.preAppsSetStat != null) {
            val appsUsage = AppsSetStat.computeUsage(taskInfo.preAppsSetStat!!, appsSetStat)
            val timeStamp = DateTimeUtils.getReadableTimeStamp(appsUsage.sampleTime)
            val N = taskInfo.pkgNames.size
            for (i in 0 until N) {
                val pkgName = taskInfo.pkgNames[i]
                val appStat = appsUsage.appsStat[pkgName] ?: continue
                val writer = taskInfo.fileWriters!![i]
                try {
                    appStat.dumpStat(writer!!, timeStamp, appsUsage.clockTime)
                    writer.flush()
                } catch (e: IOException) {
                    Timber.tag(TAG).w(e, "ignored IO exception")
                }
            }
            taskInfo.sampleClockTime += appsUsage.clockTime
            taskInfo.sampleCount++
        }
        taskInfo.preAppsSetStat = appsSetStat
        // backup the current task info state
        backupSampleTaskInfo(taskInfo)
        Timber.tag(TAG).i("sample stats snapshot done")
    }

    private fun doStopSampler(taskInfo: SampleTaskInfo?) {
        sampleLogger.logInfo(TAG, "requested to stop sample...")
        if (!taskInfo!!.isSampling) {
            sampleLogger.logWarning(TAG, "sampler not running")
            return
        }
        handler.removeMessages(MSG_SAMPLE_STATS)
        val N = taskInfo.fileWriters!!.size
        for (i in 0 until N) {
            val writer = taskInfo.fileWriters!![i]
            try {
                writer!!.flush()
                writer.close()
            } catch (e: IOException) {
                Timber.tag(TAG)
                    .w(e, "failed to flush data: %s", taskInfo.pkgNames[i])
            }
        }
        val backupFile = SamplerUtils.getFileForSampler(
            FILENAME_SAMPLE_TASK_BACKUP,
            false
        )
        backupFile.delete()
        taskInfo.isSampling = false
        stopForeground(true)
    }

    companion object {
        private const val TAG = "AppsSamplerService"

        private const val ACTION_START_SAMPLER = "action.start_sampler"
        private const val ACTION_STOP_SAMPLER = "action.stop_sampler"
        private const val ACTION_CREATE_REPORT = "action.create_report"
        private const val ACTION_CLEAR_LOGS = "action.clear_logs"

        private const val EXTRA_PKG_NAMES = "extra.pkgs" // ArrayList
        private const val EXTRA_INTERVAL = "extra.interval" // seconds
        private const val EXTRA_PERIOD = "extra.period" // minutes

        private const val FILENAME_SAMPLE_TASK_BACKUP = "task-backup.txt"
        private const val FILENAME_TAG_STATS = "-stats-"
        private const val FILENAME_TAG_REPORT = "-report"

        private const val MSG_UNKNOWN_ACTION = 100
        private const val MSG_START_SAMPLER = 101
        private const val MSG_SAMPLE_STATS = 102
        private const val MSG_STOP_SAMPLER = 103
        private const val MSG_CLEAR_LOGS = 104
        private const val MSG_CREATE_REPORT = 105

        var lastSampleTask: SampleTaskInfo? = null
            private set

        fun startSampler(
            cxt: Context,
            pkgNames: ArrayList<String>?,
            intervalSeconds: Int,
            periodMinutes: Int
        ) {
            // delete task info backup if exist
            val backupFile = SamplerUtils.getFileForSampler(
                FILENAME_SAMPLE_TASK_BACKUP,
                false
            )
            backupFile.delete()
            val intent = Intent(cxt, AppsSamplerService::class.java)
            intent.action = ACTION_START_SAMPLER
            intent.putExtra(EXTRA_PKG_NAMES, pkgNames)
            intent.putExtra(EXTRA_INTERVAL, intervalSeconds)
            intent.putExtra(EXTRA_PERIOD, periodMinutes)
            cxt.startService(intent)
        }

        fun stopSampler(cxt: Context) {
            val intent = Intent(cxt, AppsSamplerService::class.java)
            intent.action = ACTION_STOP_SAMPLER
            cxt.startService(intent)
        }

        fun clearLogs(cxt: Context) {
            val intent = Intent(cxt, AppsSamplerService::class.java)
            intent.action = ACTION_CLEAR_LOGS
            cxt.startService(intent)
        }

        fun createSampleReport(cxt: Context) {
            val intent = Intent(cxt, AppsSamplerService::class.java)
            intent.action = ACTION_CREATE_REPORT
            cxt.startService(intent)
        }

        private fun backupSampleTaskInfo(taskInfo: SampleTaskInfo?) {
            if (taskInfo == null) {
                Timber.tag(TAG).w("cannot backup sample task info, no task info yet")
                return
            }
            val taskInfoBackup = taskInfo.backupTaskInfo()
            if (taskInfoBackup == null) {
                Timber.tag(TAG).w("failed to create sample task info backup")
                return
            }
            try {
                val backupFile = SamplerUtils.getFileForSampler(
                    FILENAME_SAMPLE_TASK_BACKUP,
                    true
                )
                IoUtils.saveAsFile(taskInfoBackup, backupFile.absolutePath)
            } catch (e: IOException) {
                Timber.tag(TAG).w(e, "failed to save sample task info into backup file")
            }
        }

        private fun restoreSampleTaskInfo(): SampleTaskInfo? {
            val backupFile = SamplerUtils.getFileForSampler(
                FILENAME_SAMPLE_TASK_BACKUP,
                false
            )
            if (!backupFile.exists()) {
                return null
            }
            val taskInfoBackup: String
            taskInfoBackup = try {
                IoUtils.readAllLines(backupFile.absolutePath)
            } catch (e: IOException) {
                Timber.tag(TAG).w(e, "failed to create sampler log file")
                return null
            }
            if (TextUtils.isEmpty(taskInfoBackup)) {
                Timber.tag(TAG).w("no task info backup")
                return null
            }
            return SampleTaskInfo.restoreTaskInfo(taskInfoBackup)
        }

        private fun generateStatsFileName(pkgName: String?, startTime: Long): String {
            return DateTimeUtils.generateFileName(startTime) + FILENAME_TAG_STATS + pkgName + ".txt"
        }

        private fun generateReportFileName(startTime: Long): String {
            return DateTimeUtils.generateFileName(startTime) + FILENAME_TAG_REPORT + ".txt"
        }

        private fun createSampleReport(cxt: Context, taskInfo: SampleTaskInfo) {
            val appDir = SamplerUtils.samplerFolder
            val reportFile = File(
                appDir,
                generateReportFileName(taskInfo.startTime)
            )
            var writer: FileWriter? = null
            try {
                writer = FileWriter(reportFile, false)
                for (pkgName in taskInfo.pkgNames) {
                    val statFile = File(
                        appDir, generateStatsFileName(pkgName, taskInfo.startTime)
                    )
                    val reader = BufferedReader(FileReader(statFile))
                    try {
                        val appReport = AppStatReport(pkgName)
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            val entry: StatFileLine = AppStat.readStat(line!!) ?: continue // skip header line
                            if (appReport.sysTimeStampStart == null) {
                                appReport.sysTimeStampStart = entry.sysTimeStamp
                            }
                            appReport.sysTimeStampEnd = entry.sysTimeStamp
                            appReport.sampleCount++
                            appReport.totalTimeUsage += entry.timeUsage
                            appReport.totalCpuTime += entry.cpuTime
                            if (entry.processCount > appReport.maxProcessCount) {
                                appReport.maxProcessCount = entry.processCount
                            }
                            if (appReport.minMemPss == 0 || entry.memPss < appReport.minMemPss) {
                                appReport.minMemPss = entry.memPss
                            } else if (entry.memPss > appReport.maxMemPss) {
                                appReport.maxMemPss = entry.memPss
                            }
                            appReport.totalMemPss += entry.memPss.toLong()
                            if (appReport.minMemPrivate == 0 || entry.memPrivate < appReport.minMemPrivate) {
                                appReport.minMemPrivate = entry.memPrivate
                            } else if (entry.memPrivate > appReport.maxMemPrivate) {
                                appReport.maxMemPrivate = entry.memPrivate
                            }
                            appReport.totalMemPrivate += entry.memPrivate.toLong()
                            appReport.totalTrafficRecv += entry.trafficRecv
                            appReport.totalTrafficSend += entry.trafficSend
                        }
                        if (appReport.sampleCount > 0) {
                            appReport.dumpStat(cxt, writer)
                        } else {
                            Timber.tag(TAG).w("no stats for %s", appReport.pkgName)
                        }
                    } finally {
                        IoUtils.closeQuietly(reader)
                    }
                }
                writer.flush()
            } catch (e: IOException) {
                Timber.tag(TAG).w(e, "failed to dump stats report")
            } finally {
                IoUtils.closeQuietly(writer)
            }
        }

        private fun createAllReports(cxt: Context) {
            val appDir = SamplerUtils.samplerFolder
            val allFiles = appDir.listFiles()
            if (allFiles == null || allFiles.isEmpty()) {
                return
            }

            // get all stats files
            val allTasks = HashMap<String, SampleTaskInfo>()
            for (file in allFiles) {
                val fileName = file.name
                val statsTagIndex = fileName.indexOf(FILENAME_TAG_STATS)
                if (statsTagIndex == -1) {
                    Timber.tag(TAG).i("not stats file, skip: %s", fileName)
                    continue
                }
                val timeStr = fileName.substring(0, statsTagIndex)
                var taskInfo = allTasks[timeStr]
                if (taskInfo == null) {
                    taskInfo = SampleTaskInfo()
                    try {
                        taskInfo.startTime = DateTimeUtils.parseFileName(timeStr)
                    } catch (e: ParseException) {
                        Timber.tag(TAG).w(e, "bad file name when parsing time: %s", fileName)
                        continue
                    }
                    allTasks[timeStr] = taskInfo
                }
                val pkgName = fileName.substring(statsTagIndex + FILENAME_TAG_STATS.length)
                taskInfo.pkgNames.add(pkgName)
            }
            // create all reports
            for (taskInfo in allTasks.values) {
                createSampleReport(cxt, taskInfo)
            }
        }

        private fun clearLogs() {
            val appDir = SamplerUtils.samplerFolder
            if (appDir.exists()) {
                appDir.listFiles()?.forEach {
                    it.delete()
                }
            }
        }
    }
}
