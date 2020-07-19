package me.ycdev.android.devtools.security

import android.content.Intent
import java.util.ArrayList
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.security.unmarshall.UnmarshallScannerActivity
import me.ycdev.android.lib.commonui.activity.GridEntriesActivity

class SecurityScannerActivity : GridEntriesActivity() {
    override fun loadIntents(): List<IntentEntry> {
        val activities: MutableList<IntentEntry> = ArrayList()
        activities.add(
            IntentEntry(
                Intent(this, UnmarshallScannerActivity::class.java),
                getString(R.string.security_scanner_unmarshall_module_title),
                getString(R.string.security_scanner_unmarshall_module_desc)
            )
        )
        return activities
    }
}
