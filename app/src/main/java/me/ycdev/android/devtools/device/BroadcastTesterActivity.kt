package me.ycdev.android.devtools.device

import android.annotation.TargetApi
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build.VERSION_CODES
import android.provider.Telephony.Sms.Intents
import android.widget.Toast
import me.ycdev.android.arch.ArchConstants
import me.ycdev.android.arch.ArchConstants.INTENT_TYPE_BROADCAST
import me.ycdev.android.arch.ArchConstants.IntentType
import me.ycdev.android.arch.wrapper.ToastHelper
import me.ycdev.android.devtools.utils.AppConstants
import me.ycdev.android.lib.common.wrapper.BroadcastHelper.getInternalBroadcastPerm
import me.ycdev.android.lib.common.wrapper.BroadcastHelper.registerForExternal
import me.ycdev.android.lib.common.wrapper.BroadcastHelper.registerForInternal
import me.ycdev.android.lib.commonui.activity.GridEntriesActivity
import java.util.ArrayList

class BroadcastTesterActivity : GridEntriesActivity() {
    private inner class MyIntentEntry(
        @IntentType type: Int = ArchConstants.INTENT_TYPE_ACTIVITY,
        intent: Intent,
        title: String,
        desc: String,
        perm: String? = null
    ) : IntentEntry(type, intent, title, desc, perm) {
        override fun onItemClicked(context: Context) {
            try {
                super.onItemClicked(context)
            } catch (e: Exception) {
                showCrashInfo(e)
                e.printStackTrace()
            }
        }
    }

    private var targetPkgName: String? = null
    private val receiver1: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            ToastHelper.show(
                applicationContext, "Received1: " + intent.action,
                Toast.LENGTH_LONG
            )
        }
    }
    private val receiver2: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            ToastHelper.show(
                applicationContext, "Received2: " + intent.action,
                Toast.LENGTH_LONG
            )
        }
    }

    override fun onResume() {
        super.onResume()
        @Suppress("DEPRECATION")
        registerForExternal(
            this, receiver1,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        registerForInternal(
            this, receiver2,
            IntentFilter(AppConstants.ACTION_DYNAMIC_BROADCAST_TEST)
        )
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver1)
        unregisterReceiver(receiver2)
    }

    override val needLoadIntentsAsync: Boolean = true

    override val intents: List<IntentEntry>
        get() {
            val broadcasts: MutableList<MyIntentEntry> = ArrayList()
            targetPkgName = packageName
            testBroadcastWithPerm(broadcasts)
            testBroadcastWithoutPerm(broadcasts)
            testPackageAdd(broadcasts)
            testPackageReplaced(broadcasts)
            testPackageRemoved(broadcasts)
            testPackageChanged(broadcasts)
            testNewOutgoingCall(broadcasts)
            testSmsReceived(broadcasts)
            testSmsDeliver(broadcasts)
            testConnectivityChange(broadcasts)
            testWifiStateChange(broadcasts)
            testBootComplete(broadcasts)
            testShutdown(broadcasts)
            testTimeSet(broadcasts)
            testAppWidgetUpdate(broadcasts)
            testBluetoothStateChange(broadcasts)
            testAirplaceModeChange(broadcasts)
            testLocationChanged(broadcasts)
            return broadcasts
        }

    private fun showCrashInfo(e: Exception) {
        AlertDialog.Builder(this)
            .setTitle("Crashe Info")
            .setMessage(e.toString())
            .show()
    }

    private fun testBroadcastWithPerm(itemsList: MutableList<MyIntentEntry>) {
        val intent = Intent(AppConstants.ACTION_DYNAMIC_BROADCAST_TEST)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "Broadcast#1",
            "Send Broadcast with perm: " + intent.action,
            getInternalBroadcastPerm(this)
        )
        itemsList.add(item)
    }

    private fun testBroadcastWithoutPerm(itemsList: MutableList<MyIntentEntry>) {
        val intent = Intent(AppConstants.ACTION_DYNAMIC_BROADCAST_TEST)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "Broadcast#2",
            "Send Broadcast without perm: " + intent.action
        )
        itemsList.add(item)
    }

    private fun testPackageAdd(itemsList: MutableList<MyIntentEntry>) {
        // SecurityException: protected-broadcast
        val intent = Intent(Intent.ACTION_PACKAGE_ADDED)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "PackageAdded",
            "Send package added broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    private fun testPackageReplaced(itemsList: MutableList<MyIntentEntry>) {
        // SecurityException: protected-broadcast
        val intent = Intent(Intent.ACTION_PACKAGE_REPLACED)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "PackageReplaced",
            "Send package replaced broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    private fun testPackageRemoved(itemsList: MutableList<MyIntentEntry>) {
        // SecurityException: protected-broadcast
        val intent = Intent(Intent.ACTION_PACKAGE_REMOVED)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "PackageRemoved",
            "Send package removed broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    private fun testPackageChanged(itemsList: MutableList<MyIntentEntry>) {
        // SecurityException: protected-broadcast
        val intent = Intent(Intent.ACTION_PACKAGE_CHANGED)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "PackageChanged",
            "Send package changed broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    @Suppress("DEPRECATION")
    private fun testNewOutgoingCall(itemsList: MutableList<MyIntentEntry>) {
        // SecurityException: protected-broadcast
        val intent = Intent(Intent.ACTION_NEW_OUTGOING_CALL)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "NewOutgoingCall",
            "Send new outgoing call broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    @TargetApi(VERSION_CODES.KITKAT)
    private fun testSmsReceived(itemsList: MutableList<MyIntentEntry>) {
        // OK: the broadcast has no protection (last checked version: android-4.4_r1.1)
        val intent = Intent(Intents.SMS_RECEIVED_ACTION)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "SmsReceived",
            "Send SMS received broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    @TargetApi(VERSION_CODES.KITKAT)
    private fun testSmsDeliver(itemsList: MutableList<MyIntentEntry>) {
        // Permission Denial: android.permission.BROADCAST_SMS (signature protection) (Supported by android-4.4)
        val intent = Intent(Intents.SMS_DELIVER_ACTION)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "SmsDeliver",
            "Send SMS deliver broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    private fun testConnectivityChange(itemsList: MutableList<MyIntentEntry>) {
        // a) OK: no protection (older versions than android-4.0.1_r1)
        // b) SecurityException: protected-broadcast (android-4.0.1_r1+)
        // Can be received even though the receiver is not exported!
        @Suppress("DEPRECATION")
        val intent = Intent(ConnectivityManager.CONNECTIVITY_ACTION)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "ConnectChange",
            "Send network connectivity change broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    private fun testWifiStateChange(itemsList: MutableList<MyIntentEntry>) {
        // a) OK: no protection (older versions than android-4.2.2_r1)
        // b) SecurityException: protected-broadcast (android-4.2.2_r1+)
        val intent = Intent(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "WifiChange",
            "Send wifi state change broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    private fun testBootComplete(itemsList: MutableList<MyIntentEntry>) {
        // SecurityException: protected-broadcast
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "BootCompleted",
            "Send boot completed broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    private fun testShutdown(itemsList: MutableList<MyIntentEntry>) {
        // SecurityException: protected-broadcast
        val intent = Intent(Intent.ACTION_SHUTDOWN)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "Shutdown",
            "Send shutdown broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    private fun testTimeSet(itemsList: MutableList<MyIntentEntry>) {
        // a) OK: no protection (older versions than android-4.4_r0.7)
        // b) SecurityException: protected-broadcast (android-4.4_r0.7+)
        val intent = Intent(Intent.ACTION_TIME_CHANGED)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "TimeSet",
            "Send time set broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    private fun testAppWidgetUpdate(itemsList: MutableList<MyIntentEntry>) {
        // OK: no protection (last checked version: android-4.4_r1.1)
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "AppWidgetUpdate",
            "Send app widget update broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    private fun testBluetoothStateChange(itemsList: MutableList<MyIntentEntry>) {
        // SecurityException: protected-broadcast
        val intent = Intent(BluetoothAdapter.ACTION_STATE_CHANGED)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "BluetoothChange",
            "Send bluetooth state change broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    private fun testAirplaceModeChange(itemsList: MutableList<MyIntentEntry>) {
        // a) OK: no protection (older versions than android-4.3_r0.9)
        // b) SecurityException: protected-broadcast (android-4.3_r0.9+)
        val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "AirplaceChange",
            "Send airplane mode change broadcast: " + intent.action
        )
        itemsList.add(item)
    }

    private fun testLocationChanged(itemsList: MutableList<MyIntentEntry>) {
        // a) OK: no protection (older versions than android-4.4_r0.7)
        // b) SecurityException: protected-broadcast (android-4.4_r0.7+)
        val intent = Intent(LocationManager.PROVIDERS_CHANGED_ACTION)
        intent.setPackage(targetPkgName)
        val item = MyIntentEntry(
            INTENT_TYPE_BROADCAST, intent, "LocationChange",
            "Send location change broadcast: " + intent.action
        )
        itemsList.add(item)
    }
}