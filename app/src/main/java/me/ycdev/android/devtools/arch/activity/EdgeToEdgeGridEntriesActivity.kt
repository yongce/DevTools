package me.ycdev.android.devtools.arch.activity

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.ui.applyDevToolsWindowInsets
import me.ycdev.android.devtools.ui.enableDevToolsEdgeToEdge
import me.ycdev.android.lib.commonui.activity.GridEntriesActivity

abstract class EdgeToEdgeGridEntriesActivity : GridEntriesActivity() {
    override val contentViewLayout: Int = R.layout.app_bar_main

    override fun onCreate(savedInstanceState: Bundle?) {
        enableDevToolsEdgeToEdge()
        super.onCreate(savedInstanceState)
        updateGridSpanCount()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        applyDevToolsWindowInsets()
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        applyDevToolsWindowInsets()
    }

    override fun setContentView(
        view: View?,
        params: ViewGroup.LayoutParams?,
    ) {
        super.setContentView(view, params)
        applyDevToolsWindowInsets()
    }

    private fun updateGridSpanCount() {
        val widthDp = resources.configuration.screenWidthDp
        val spanCount =
            when {
                widthDp >= EXPANDED_WIDTH_DP -> 5
                widthDp >= MEDIUM_WIDTH_DP -> 4
                else -> 3
            }
        (gridView.layoutManager as? GridLayoutManager)?.spanCount = spanCount
    }

    companion object {
        private const val MEDIUM_WIDTH_DP = 600
        private const val EXPANDED_WIDTH_DP = 840
    }
}
