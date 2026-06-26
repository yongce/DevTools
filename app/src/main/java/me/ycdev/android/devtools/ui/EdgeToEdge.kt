package me.ycdev.android.devtools.ui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import me.ycdev.android.devtools.R

fun ComponentActivity.enableDevToolsEdgeToEdge() {
    enableEdgeToEdge()
}

fun Activity.applyDevToolsWindowInsets() {
    val root = findViewById<ViewGroup>(android.R.id.content)?.getChildAt(0) ?: return
    val toolbar = root.findViewById<Toolbar?>(R.id.toolbar)
    val insetTargets =
        listOf(R.id.grid, R.id.list)
            .mapNotNull { root.findViewById<View?>(it) }
            .distinct()

    toolbar?.applyToolbarInsets()
    root.findViewById<View?>(R.id.nav_view)?.applyNavigationInsets()

    if (insetTargets.isNotEmpty()) {
        insetTargets.forEach { it.applyContentInsets(includeTop = false) }
    } else {
        root.applyContentInsets(includeTop = toolbar == null && !hasSupportActionBar())
    }
}

private fun Activity.hasSupportActionBar(): Boolean {
    return (this as? AppCompatActivity)?.supportActionBar != null
}

private fun View.applyContentInsets(includeTop: Boolean) {
    val initialPadding = paddingState()
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        view.updatePadding(
            left = initialPadding.left + bars.left,
            top = if (includeTop) initialPadding.top + bars.top else initialPadding.top,
            right = initialPadding.right + bars.right,
            bottom = initialPadding.bottom + bars.bottom,
        )
        insets
    }
    requestInsetsWhenAttached()
}

private fun Toolbar.applyToolbarInsets() {
    val initialPadding = paddingState()
    val initialHeight = layoutParams.height
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        view.updatePadding(
            left = initialPadding.left + bars.left,
            top = initialPadding.top + bars.top,
            right = initialPadding.right + bars.right,
            bottom = initialPadding.bottom,
        )
        if (initialHeight > 0) {
            val params = view.layoutParams
            params.height = initialHeight + bars.top
            view.layoutParams = params
        }
        insets
    }
    requestInsetsWhenAttached()
}

private fun View.applyNavigationInsets() {
    val initialPadding = paddingState()
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        view.updatePadding(
            left = initialPadding.left + bars.left,
            top = initialPadding.top,
            right = initialPadding.right + bars.right,
            bottom = initialPadding.bottom + bars.bottom,
        )
        insets
    }
    requestInsetsWhenAttached()
}

private fun View.requestInsetsWhenAttached() {
    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    } else {
        addOnAttachStateChangeListener(
            object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(view: View) {
                    view.removeOnAttachStateChangeListener(this)
                    ViewCompat.requestApplyInsets(view)
                }

                override fun onViewDetachedFromWindow(view: View) = Unit
            },
        )
    }
}

private fun View.paddingState(): PaddingState {
    return PaddingState(paddingLeft, paddingTop, paddingRight, paddingBottom)
}

private data class PaddingState(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)
