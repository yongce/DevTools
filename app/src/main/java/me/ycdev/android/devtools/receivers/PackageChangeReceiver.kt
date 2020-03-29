package me.ycdev.android.devtools.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.net.URI
import me.ycdev.android.lib.common.wrapper.IntentHelper.getBooleanExtra
import me.ycdev.android.lib.common.wrapper.IntentHelper.getIntExtra
import timber.log.Timber

class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pkgName = URI.create(intent.dataString!!).schemeSpecificPart
        val uid = getIntExtra(intent, Intent.EXTRA_UID, -1)
        val replacing =
            getBooleanExtra(intent, Intent.EXTRA_REPLACING, false)
        Timber.tag(TAG).i(
            "Received: %s, pkgName: %s, uid: %s, replacing: %s",
            intent.action, pkgName, uid, replacing
        )
    }

    companion object {
        private const val TAG = "PackageChangeReceiver"
    }
}
