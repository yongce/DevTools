package me.ycdev.android.devtools.security

import android.content.Intent
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.arch.activity.EdgeToEdgeGridEntriesActivity
import me.ycdev.android.devtools.security.unmarshall.UnmarshallScannerActivity
import java.util.ArrayList

class SecurityScannerActivity : EdgeToEdgeGridEntriesActivity() {
    override fun loadIntents(): List<IntentEntry> {
        val activities: MutableList<IntentEntry> = ArrayList()
        activities.add(
            IntentEntry(
                Intent(this, UnmarshallScannerActivity::class.java),
                getString(R.string.security_scanner_unmarshall_module_title),
                getString(R.string.security_scanner_unmarshall_module_desc),
            ),
        )
        return activities
    }
}
