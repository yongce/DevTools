package me.ycdev.android.devtools

import android.app.IntentService
import android.content.Intent
import me.ycdev.android.devtools.security.unmarshall.UnmarshallScannerActivity

class CommonIntentService : IntentService("CommonIntentService") {
    override fun onHandleIntent(intent: Intent?) {
        val action = intent?.action
        if (ACTION_UNMARSHALL_SCANNER == action) {
            UnmarshallScannerActivity.scanUnmarshallIssue(this)
        }
    }

    companion object {
        private const val ACTION_PREFIX = "class.action."

        const val ACTION_UNMARSHALL_SCANNER = ACTION_PREFIX + "UNMARSHALL_SCANNER"
    }
}