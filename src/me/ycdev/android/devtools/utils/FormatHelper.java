package me.ycdev.android.devtools.utils;

import me.ycdev.android.devtools.R;

import android.content.Context;

public class FormatHelper {
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 60 * 60;

    /**
     * Formats elapsed time for the given seconds.
     */
    public static String formatElapsedTime(Context context, int seconds) {
        int hours = 0, minutes = 0;
        if (seconds > SECONDS_PER_HOUR) {
            hours = seconds / SECONDS_PER_HOUR;
            seconds -= hours * SECONDS_PER_HOUR;
        }
        if (seconds > SECONDS_PER_MINUTE) {
            minutes = seconds / SECONDS_PER_MINUTE;
            seconds -= minutes * SECONDS_PER_MINUTE;
        }

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            if (minutes == 0)
                minutes = 1; // don't show "0 minutes" to user
            sb.append(context.getString(R.string.elapsed_time_hours, hours, minutes));
        } else if (minutes > 0) {
            if (seconds == 0)
                seconds = 1; // don't show "0 seconds" to user
            sb.append(context.getString(R.string.elapsed_time_minutes, minutes, seconds));
        } else {
            sb.append(context.getString(R.string.elapsed_time_seconds, seconds));
        }

        return sb.toString();
    }

    /**
     * Formats data size in KB, MB, from the given bytes.
     */
    public static String formatBytes(Context context, long bytes) {
        if (bytes > 1000 * 1000) {
            return String.format("%.2f MB", bytes / 1000 / 1000.0f);
        } else if (bytes > 1024) {
            return String.format("%.2f KB", bytes / 10 / 100.0f);
        } else {
            return String.format("%d B", (int) bytes);
        }
    }
}
