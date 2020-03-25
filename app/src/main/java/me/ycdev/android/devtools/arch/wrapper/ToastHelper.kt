package me.ycdev.android.devtools.arch.wrapper

import android.content.Context
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes

/**
 * A wrapper class for Toast so that we can customize and unify the UI in future.
 */
object ToastHelper {
    /*
     * Durations for toast
     */
    @IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ToastDuration

    fun show(cxt: Context, @StringRes msgResId: Int, @ToastDuration duration: Int) {
        Toast.makeText(cxt, msgResId, duration).show()
    }

    fun show(cxt: Context, msg: CharSequence, @ToastDuration duration: Int) {
        Toast.makeText(cxt, msg, duration).show()
    }
}
