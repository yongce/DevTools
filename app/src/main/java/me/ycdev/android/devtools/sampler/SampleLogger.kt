package me.ycdev.android.devtools.sampler

import android.content.Context
import me.ycdev.android.lib.common.utils.DateTimeUtils.getReadableTimeStamp
import me.ycdev.android.lib.common.utils.IoUtils.closeQuietly
import timber.log.Timber
import java.io.Closeable
import java.io.FileWriter
import java.io.IOException

class SampleLogger(
    context: Context,
) : Closeable {
    private var logWriter: FileWriter? = null

    init {
        try {
            val logFile = SamplerUtils.getFileForSampler(context, SAMPLER_LOG_FILENAME, true)
            logWriter = FileWriter(logFile, true)
        } catch (e: IOException) {
            Timber.tag(TAG).w(e, "failed to create sampler log file")
        }
    }

    private fun addLog(
        tag: String,
        msg: String,
    ) {
        val writer = logWriter ?: return
        val timeStamp = getReadableTimeStamp(System.currentTimeMillis())
        try {
            writer
                .append(timeStamp)
                .append(STAT_FILE_COLUMNS_SEP)
                .append(tag)
                .append(STAT_FILE_COLUMNS_SEP)
                .append(msg)
                .append("\n")
            writer.flush()
        } catch (e: IOException) {
            Timber.tag(TAG).w(e, "ignored IO exception")
        }
    }

    fun logDebug(
        tag: String,
        msg: String,
    ) {
        Timber.tag(tag).d(msg)
        addLog(tag, msg)
    }

    fun logInfo(
        tag: String,
        msg: String,
    ) {
        Timber.tag(tag).i(msg)
        addLog(tag, msg)
    }

    fun logWarning(
        tag: String,
        msg: String,
    ) {
        Timber.tag(tag).w(msg)
        addLog(tag, msg)
    }

    fun logError(
        tag: String,
        msg: String,
    ) {
        Timber.tag(tag).e(msg)
        addLog(tag, msg)
    }

    @Throws(IOException::class)
    override fun close() {
        logWriter?.let { closeQuietly(it) }
        logWriter = null
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
