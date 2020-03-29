package me.ycdev.android.devtools.utils

import android.content.Context
import java.util.Locale
import me.ycdev.android.devtools.R.string

object FormatHelper {
    private const val SECONDS_PER_MINUTE = 60
    private const val SECONDS_PER_HOUR = 60 * 60
    /**
     * Formats elapsed time for the given seconds.
     */
    fun formatElapsedTime(context: Context, seconds: Int): String {
        var secondsRemaining = seconds
        var hours = 0
        var minutes = 0
        if (secondsRemaining > SECONDS_PER_HOUR) {
            hours = secondsRemaining / SECONDS_PER_HOUR
            secondsRemaining -= hours * SECONDS_PER_HOUR
        }
        if (secondsRemaining > SECONDS_PER_MINUTE) {
            minutes = secondsRemaining / SECONDS_PER_MINUTE
            secondsRemaining -= minutes * SECONDS_PER_MINUTE
        }
        val sb = StringBuilder()
        if (hours > 0) {
            if (minutes == 0) minutes = 1 // don't show "0 minutes" to user
            sb.append(context.getString(string.elapsed_time_hours, hours, minutes))
        } else if (minutes > 0) {
            if (secondsRemaining == 0) secondsRemaining = 1 // don't show "0 seconds" to user
            sb.append(context.getString(string.elapsed_time_minutes, minutes, secondsRemaining))
        } else {
            sb.append(context.getString(string.elapsed_time_seconds, secondsRemaining))
        }
        return sb.toString()
    }

    /**
     * Formats data size in KB, MB, from the given bytes.
     */
    fun formatBytes(context: Context?, bytes: Long): String {
        return if (bytes > 1000 * 1000) {
            String.format(Locale.US, "%.2f MB", bytes / 1000 / 1000.0f)
        } else if (bytes > 1024) {
            String.format(Locale.US, "%.2f KB", bytes / 10 / 100.0f)
        } else {
            String.format(Locale.US, "%d B", bytes.toInt())
        }
    }
}
