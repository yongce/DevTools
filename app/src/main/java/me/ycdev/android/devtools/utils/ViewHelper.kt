package me.ycdev.android.devtools.utils

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

object ViewHelper {
    fun addTextView(holder: ViewGroup, itemValue: String?) {
        val itemView = TextView(holder.context)
        itemView.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        itemView.text = itemValue
        holder.addView(itemView)
    }

    fun addLineView(holder: ViewGroup, color: Int) {
        val context = holder.context
        val lineView = ImageView(context)
        lineView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            1
        )
        lineView.setBackgroundColor(color)
        val marginView = LinearLayout(context)
        marginView.setPadding(0, 6, 0, 6)
        marginView.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        marginView.addView(lineView)
        holder.addView(marginView)
    }

    fun addTextView(
        holder: ViewGroup,
        itemName: String,
        itemValue: String?
    ) {
        addTextView(holder, "$itemName: $itemValue")
    }

    fun addTextView(
        holder: ViewGroup,
        itemName: String,
        itemValue: Boolean
    ) {
        addTextView(holder, itemName, itemValue.toString())
    }

    fun addTextView(
        holder: ViewGroup,
        itemName: String,
        itemValue: Int
    ) {
        addTextView(holder, itemName, itemValue.toString())
    }

    fun addTextView(
        holder: ViewGroup,
        itemName: String,
        itemValue: Double
    ) {
        addTextView(holder, itemName, itemValue.toString())
    }

    fun addTimeTextView(
        holder: ViewGroup,
        itemName: String,
        macroSeconds: Long
    ) {
        val seconds = (macroSeconds / (1000 * 1000)).toInt()
        val timeStr = FormatHelper.formatElapsedTime(holder.context, seconds)
        addTextView(holder, itemName, timeStr)
    }

    fun addBytesTextView(
        holder: ViewGroup,
        itemName: String,
        bytes: Long
    ) {
        val bytesStr = FormatHelper.formatBytes(holder.context, bytes)
        addTextView(holder, itemName, bytesStr)
    }
}
