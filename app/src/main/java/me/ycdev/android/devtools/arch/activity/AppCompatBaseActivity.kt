package me.ycdev.android.arch.activity

import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.MaterialColors
import com.google.android.material.R as MaterialR
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.ui.applyDevToolsWindowInsets
import me.ycdev.android.devtools.ui.enableDevToolsEdgeToEdge

/**
 * Base class for Activity which wants to inherit
 * [androidx.appcompat.app.AppCompatActivity].
 */
abstract class AppCompatBaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableDevToolsEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {
        setContentView(layoutInflater.inflate(layoutResID, null))
    }

    override fun setContentView(view: View?) {
        val content = view?.withGeneratedToolbarIfNeeded()
        super.setContentView(content)
        content?.configureSupportActionBar()
        applyDevToolsWindowInsets()
    }

    override fun setContentView(
        view: View?,
        params: ViewGroup.LayoutParams?,
    ) {
        val content = view?.withGeneratedToolbarIfNeeded()
        super.setContentView(content, params)
        content?.configureSupportActionBar()
        applyDevToolsWindowInsets()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    protected open fun shouldSetDisplayHomeAsUpEnabled(): Boolean = true

    private fun View.withGeneratedToolbarIfNeeded(): View {
        if (findViewById<Toolbar?>(R.id.toolbar) != null) {
            return this
        }
        val toolbar = MaterialToolbar(this@AppCompatBaseActivity).apply {
            id = R.id.toolbar
            title = this@AppCompatBaseActivity.title
            setBackgroundColor(MaterialColors.getColor(this, MaterialR.attr.colorPrimaryContainer))
            setTitleTextColor(MaterialColors.getColor(this, MaterialR.attr.colorOnPrimaryContainer))
            setNavigationIconTint(MaterialColors.getColor(this, MaterialR.attr.colorOnPrimaryContainer))
            layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    actionBarSize(),
                )
        }
        return LinearLayout(this@AppCompatBaseActivity).apply {
            orientation = LinearLayout.VERTICAL
            addView(toolbar)
            addView(
                this@withGeneratedToolbarIfNeeded,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f,
                ),
            )
        }
    }

    private fun View.configureSupportActionBar() {
        val toolbar = findViewById<Toolbar?>(R.id.toolbar) ?: return
        setSupportActionBar(toolbar)
        supportActionBar?.title = title
        if (shouldSetDisplayHomeAsUpEnabled()) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun actionBarSize(): Int {
        val value = TypedValue()
        theme.resolveAttribute(android.R.attr.actionBarSize, value, true)
        return TypedValue.complexToDimensionPixelSize(value.data, resources.displayMetrics)
    }
}
