package me.ycdev.android.devtools.sampler

import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.FileWriter
import java.util.ArrayList

class SampleTaskInfo {
    val pkgNames: ArrayList<String> = arrayListOf()
    var sampleInterval = 0 // in seconds
    var samplePeriod = 0 // in minutes
    var startTime: Long = 0
    var sampleClockTime: Long = 0
    var sampleCount = 0
    var isSampling = false
    var fileWriters: ArrayList<FileWriter?>? = null
    var preAppsSetStat: AppsSetStat? = null

    fun backupTaskInfo(): String? {
        try {
            val json = JSONObject()
            json.put(KEY_PKG_COUNT, pkgNames.size)
            for (i in 0 until pkgNames.size) {
                json.put(KEY_PKG_NAME_PREFIX + i, pkgNames[i])
            }
            json.put(KEY_SAMPLE_INTERVAL, sampleInterval)
            json.put(KEY_SAMPLE_PERIOD, samplePeriod)
            json.put(KEY_START_TIME, startTime)
            json.put(KEY_SAMPLE_CLOCK_TIME, sampleClockTime)
            json.put(KEY_SAMPLE_COUNT, sampleCount)
            return json.toString()
        } catch (e: JSONException) {
            Timber.tag(TAG).w(e, "failed to create sample task info backup")
        }
        return null
    }

    companion object {
        private const val TAG = "SampleTaskInfo"

        private const val KEY_PKG_COUNT = "pkg_count"
        private const val KEY_PKG_NAME_PREFIX = "pkg_"
        private const val KEY_SAMPLE_INTERVAL = "interval"
        private const val KEY_SAMPLE_PERIOD = "period"
        private const val KEY_START_TIME = "start_time"
        private const val KEY_SAMPLE_CLOCK_TIME = "sample_clock_time"
        private const val KEY_SAMPLE_COUNT = "sample_count"

        fun restoreTaskInfo(backup: String): SampleTaskInfo? {
            try {
                val json = JSONObject(backup)
                val taskInfo = SampleTaskInfo()
                val count = json.getInt(KEY_PKG_COUNT)
                for (i in 0 until count) {
                    taskInfo.pkgNames.add(json.getString(KEY_PKG_NAME_PREFIX + i))
                }
                taskInfo.sampleInterval = json.getInt(KEY_SAMPLE_INTERVAL)
                taskInfo.samplePeriod = json.getInt(KEY_SAMPLE_PERIOD)
                taskInfo.startTime = json.getLong(KEY_START_TIME)
                taskInfo.sampleClockTime = json.getLong(KEY_SAMPLE_CLOCK_TIME)
                taskInfo.sampleCount = json.getInt(KEY_SAMPLE_COUNT)
                return taskInfo
            } catch (e: JSONException) {
                Timber.tag(TAG).w(e, "failed to restore sample task info from backup")
            }
            return null
        }
    }
}