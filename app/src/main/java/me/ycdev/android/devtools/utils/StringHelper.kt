package me.ycdev.android.devtools.utils

import android.content.Context

object StringHelper {
    fun addTimeEntry(
        strBuilder: StringBuilder,
        prefix: String?,
        context: Context,
        macroSeconds: Long
    ) {
        val timeUnit = 1000 * 1000.toLong()
        strBuilder.append(prefix)
        val seconds = (macroSeconds / timeUnit).toInt()
        strBuilder.append(FormatHelper.formatElapsedTime(context, seconds))
        strBuilder.append('\n')
    }

    fun addCountEntry(
        strBuilder: StringBuilder,
        prefix: String?,
        count: Int
    ) {
        strBuilder.append(prefix)
        strBuilder.append(count)
        strBuilder.append('\n')
    }

    fun addBooleanEntry(
        strBuilder: StringBuilder,
        prefix: String?,
        yesNo: Boolean
    ) {
        strBuilder.append(prefix)
        strBuilder.append(yesNo)
        strBuilder.append('\n')
    }

    fun addBytesEntry(
        strBuilder: StringBuilder,
        prefix: String?,
        context: Context?,
        bytes: Long
    ) {
        strBuilder.append(prefix)
        strBuilder.append(FormatHelper.formatBytes(context, bytes))
        strBuilder.append('\n')
    }

    fun addDoubleEntry(
        strBuilder: StringBuilder,
        prefix: String?,
        value: Double
    ) {
        strBuilder.append(prefix)
        strBuilder.append(value)
        strBuilder.append('\n')
    }

    fun addStringEntry(
        strBuilder: StringBuilder,
        prefix: String?,
        str: String?
    ) {
        strBuilder.append(prefix)
        strBuilder.append(str)
        strBuilder.append('\n')
    }
}
