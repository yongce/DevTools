package me.ycdev.android.devtools.sampler

import java.io.Closeable
import java.io.FileWriter
import java.io.IOException
import me.ycdev.android.lib.common.utils.DateTimeUtils.getReadableTimeStamp
import me.ycdev.android.lib.common.utils.IoUtils.closeQuietly
import timber.log.Timber

class SampleLogger : Closeable {
    private lateinit var logWriter: FileWriter

    init {
        try {
            val logFile = SamplerUtils.getFileForSampler(SAMPLER_LOG_FILENAME, true)
            logWriter = FileWriter(logFile, true)
        } catch (e: IOException) {
            Timber.tag(TAG).w(e, "failed to create sampler log file")
        }
    }

    private fun addLog(tag: String, msg: String) {
        val timeStamp = getReadableTimeStamp(System.currentTimeMillis())
        try {
            logWriter.append(timeStamp).append(STAT_FILE_COLUMNS_SEP)
                .append(tag).append(STAT_FILE_COLUMNS_SEP)
                .append(msg).append("\n")
            logWriter.flush()
        } catch (e: IOException) {
            Timber.tag(TAG).w(e, "ignored IO exception")
        }
    }

    fun logDebug(tag: String, msg: String) {
        Timber.tag(tag).d(msg)
        addLog(tag, msg)
    }

    fun logInfo(tag: String, msg: String) {
        Timber.tag(tag).i(msg)
        addLog(tag, msg)
    }

    fun logWarning(tag: String, msg: String) {
        Timber.tag(tag).w(msg)
        addLog(tag, msg)
    }

    fun logError(tag: String, msg: String) {
        Timber.tag(tag).e(msg)
        addLog(tag, msg)
    }

    @Throws(IOException::class)
    override fun close() {
        closeQuietly(logWriter)
    }

    protected fun finalize() {
        close()
    }

    companion object {
        private const val TAG = "SampleLogger"
        private const val SAMPLER_LOG_FILENAME = "sampler.log"
        private const val STAT_FILE_COLUMNS_SEP = "\t"
    }
}
