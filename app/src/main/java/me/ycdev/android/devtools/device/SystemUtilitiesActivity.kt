package me.ycdev.android.devtools.device

import android.content.Intent
import java.util.ArrayList
import me.ycdev.android.devtools.R
import me.ycdev.android.lib.commonui.activity.GridEntriesActivity

class SystemUtilitiesActivity : GridEntriesActivity() {
    override val intents: List<IntentEntry>
        get() {
            val activities: MutableList<IntentEntry> =
                ArrayList()
            activities.add(
                IntentEntry(
                    Intent("android.settings.APP_OPS_SETTINGS"),
                    getString(R.string.module_apps_ops_title),
                    getString(R.string.module_apps_ops_desc)
                )
            )
            val appsMgrIntent = Intent("com.android.settings.applications")
            appsMgrIntent.setClassName(
                "com.android.settings",
                "com.android.settings.applications.ManageApplications"
            )
            activities.add(
                IntentEntry(
                    appsMgrIntent,
                    getString(R.string.module_apps_mgr_title),
                    getString(R.string.module_apps_mgr_desc)
                )
            )
            val testSettingsIntent = Intent().setClassName(
                "com.android.settings",
                "com.android.settings.TestingSettings"
            )
            activities.add(
                IntentEntry(
                    testSettingsIntent,
                    getString(R.string.module_testing_settings_title),
                    getString(R.string.module_testing_settings_desc)
                )
            )
            val bluetoothViewerIntent = Intent(this, BluetoothViewerActivity::class.java)
            activities.add(
                IntentEntry(
                    bluetoothViewerIntent,
                    getString(R.string.module_bluetooth_viewer_title),
                    getString(R.string.module_bluetooth_viewer_desc)
                )
            )
            return activities
        }
}
