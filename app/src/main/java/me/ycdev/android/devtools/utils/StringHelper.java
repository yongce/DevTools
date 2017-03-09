package me.ycdev.android.devtools.utils;

import android.content.Context;

public class StringHelper {
    public static void addTimeEntry(StringBuilder strBuilder,
            String prefix, Context context, long macroSeconds) {
        final long TIME_UNIT = 1000 * 1000;
        strBuilder.append(prefix);
        int seconds = (int) (macroSeconds / TIME_UNIT);
        strBuilder.append(FormatHelper.formatElapsedTime(context, seconds));
        strBuilder.append('\n');
    }

    public static void addCountEntry(StringBuilder strBuilder,
            String prefix, int count) {
        strBuilder.append(prefix);
        strBuilder.append(count);
        strBuilder.append('\n');
    }

    public static void addBooleanEntry(StringBuilder strBuilder,
            String prefix, boolean yesNo) {
        strBuilder.append(prefix);
        strBuilder.append(yesNo);
        strBuilder.append('\n');
    }

    public static void addBytesEntry(StringBuilder strBuilder,
            String prefix, Context context, long bytes) {
        strBuilder.append(prefix);
        strBuilder.append(FormatHelper.formatBytes(context, bytes));
        strBuilder.append('\n');
    }

    public static void addDoubleEntry(StringBuilder strBuilder,
            String prefix, double value) {
        strBuilder.append(prefix);
        strBuilder.append(value);
        strBuilder.append('\n');
    }

    public static void addStringEntry(StringBuilder strBuilder,
            String prefix, String str) {
        strBuilder.append(prefix);
        strBuilder.append(str);
        strBuilder.append('\n');
    }
}
