package me.ycdev.android.devtools.security.unmarshall

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import me.ycdev.android.devtools.root.cmd.AppsKillerCmd
import me.ycdev.android.devtools.security.foo.ParcelableTest
import me.ycdev.android.lib.common.utils.PackageUtils
import me.ycdev.android.lib.common.wrapper.BroadcastHelper
import timber.log.Timber

object IntentUnmarshallScanner {
    private const val TAG = "IntentUnmarshallScanner"

    private fun buildScanIntent(target: ComponentName): Intent {
        val intent = Intent()
        intent.component = target
        intent.putExtra("extra.oom_attack", ParcelableTest(1014 * 1024 * 1024))
        return intent
    }

    private fun scanReceiverTarget(cxt: Context, target: ComponentName, perm: String?): Boolean {
        Timber.tag(TAG).i("scan receiver: $target, with perm: $perm")
        try {
            val intent = buildScanIntent(target)
            BroadcastHelper.sendToExternal(cxt, intent, perm)
            return true
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "failed to send broadcast")
        }
        return false
    }

    private fun scanServiceTarget(cxt: Context, target: ComponentName): Boolean {
        Timber.tag(TAG).i("scan service: %s", target)
        try {
            val intent = buildScanIntent(target)
            cxt.startService(intent)
            return true
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "failed to start service")
        }
        return false
    }

    private fun scanActivityTarget(cxt: Context, target: ComponentName): Boolean {
        Timber.tag(TAG).i("scan activity: %s", target)
        try {
            val intent = buildScanIntent(target)
            intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            cxt.startActivity(intent)
            return true
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "failed to start activity")
        }
        return false
    }

    fun scanAllReceivers(cxt: Context, controller: IScanController) {
        val pkgName = controller.targetPackageName
        val allReceivers = PackageUtils.getAllReceivers(cxt, pkgName, true)
        if (allReceivers.isEmpty()) {
            return
        }

        Timber.tag(TAG).i("%s receivers to scan...", allReceivers.size)
        var appsKillerCmd: AppsKillerCmd? = null
        if (controller.needKillApp) {
            appsKillerCmd = AppsKillerCmd(cxt, listOf(pkgName))
        }
        for (receiverInfo in allReceivers) {
            if (controller.isCanceled) {
                Timber.tag(TAG).i("scan canceled")
                return
            }
            appsKillerCmd?.run()
            val cn = ComponentName(receiverInfo.packageName, receiverInfo.name)
            scanReceiverTarget(cxt, cn, receiverInfo.permission)
            if (appsKillerCmd != null) {
                SystemClock.sleep(5000)
            } else {
                SystemClock.sleep(500)
            }
        }
        Timber.tag(TAG).i("receivers scan done")
    }

    fun scanAllServices(cxt: Context, controller: IScanController) {
        val pkgName = controller.targetPackageName
        val allServices = PackageUtils.getAllServices(cxt, pkgName, true)
        if (allServices.isEmpty()) {
            return
        }
        Timber.tag(TAG).i("%s services to check...", allServices.size)
        var appsKillerCmd: AppsKillerCmd? = null
        if (controller.needKillApp) {
            appsKillerCmd = AppsKillerCmd(cxt, listOf(pkgName))
        }
        for (serviceInfo in allServices) {
            if (controller.isCanceled) {
                Timber.tag(TAG).i("scan canceled")
                return
            }
            appsKillerCmd?.run()
            val cn = ComponentName(serviceInfo.packageName, serviceInfo.name)
            scanServiceTarget(cxt, cn)
            if (appsKillerCmd != null) {
                SystemClock.sleep(5000)
            } else {
                SystemClock.sleep(500)
            }
        }
        Timber.tag(TAG).i("services scan done")
    }

    fun scanAllActivities(cxt: Context, controller: IScanController) {
        val pkgName = controller.targetPackageName
        val allActivities = PackageUtils.getAllActivities(cxt, pkgName, true)
        if (allActivities.isEmpty()) {
            return
        }
        Timber.tag(TAG).i("%s activities to check...", allActivities.size)
        var appsKillerCmd: AppsKillerCmd? = null
        if (controller.needKillApp) {
            appsKillerCmd = AppsKillerCmd(cxt, listOf(pkgName))
        }
        for (activityInfo in allActivities) {
            if (controller.isCanceled) {
                Timber.tag(TAG).i("scan canceled")
                return
            }
            appsKillerCmd?.run()
            val cn = ComponentName(activityInfo.packageName, activityInfo.name)
            scanActivityTarget(cxt, cn)
            SystemClock.sleep(5000)
        }
        Timber.tag(TAG).i("activities scan done")
    }
}
