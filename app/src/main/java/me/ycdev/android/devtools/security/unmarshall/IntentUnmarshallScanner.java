package me.ycdev.android.devtools.security.unmarshall;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.os.SystemClock;

import java.util.Collections;

import androidx.annotation.NonNull;
import me.ycdev.android.devtools.root.cmd.AppsKillerCmd;
import me.ycdev.android.devtools.security.foo.ParcelableTest;
import me.ycdev.android.lib.common.utils.PackageUtils;
import me.ycdev.android.lib.common.wrapper.BroadcastHelper;
import timber.log.Timber;

public class IntentUnmarshallScanner {
    private static final String TAG = "IntentUnmarshallScanner";

    private static Intent buildScanIntent(ComponentName target) {
        Intent intent = new Intent();
        intent.setComponent(target);
        intent.putExtra("extra.oom_attack", new ParcelableTest(1014 * 1024 * 1024));
        return intent;
    }

    public static boolean scanReceiverTarget(Context cxt, ComponentName target, String perm) {
        Timber.tag(TAG).i("scan receiver: " + target + ", with perm: " + perm);
        try {
            Intent intent = buildScanIntent(target);
            BroadcastHelper.INSTANCE.sendToExternal(cxt, intent, perm);
            return true;
        } catch (Exception e) {
            Timber.tag(TAG).w(e, "failed to send broadcast");
        }
        return false;
    }

    public static boolean scanServiceTarget(Context cxt, ComponentName target) {
        Timber.tag(TAG).i("scan service: %s", target);
        try {
            Intent intent = buildScanIntent(target);
            cxt.startService(intent);
            return true;
        } catch (Exception e) {
            Timber.tag(TAG).w(e, "failed to send broadcast");
        }
        return false;
    }

    public static boolean scanActivityTarget(Context cxt, ComponentName target) {
        Timber.tag(TAG).i("scan activity: %s", target);
        try {
            Intent intent = buildScanIntent(target);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            cxt.startActivity(intent);
            return true;
        } catch (Exception e) {
            Timber.tag(TAG).w(e, "failed to send broadcast");
        }
        return false;
    }

    public static void scanAllReceivers(@NonNull Context cxt, @NonNull IScanController controller) {
        String pkgName = controller.getTargetPackageName();
        ActivityInfo[] allReceivers = PackageUtils.INSTANCE.getAllReceivers(cxt, pkgName, true);
        if (allReceivers == null || allReceivers.length == 0) {
            return;
        }

        Timber.tag(TAG).i("%s receivers to scan...", allReceivers.length);
        AppsKillerCmd appsKillerCmd = null;
        if (controller.needKillApp()) {
            appsKillerCmd = new AppsKillerCmd(cxt);
            appsKillerCmd.setPackagesToKill(Collections.singletonList(pkgName));
        }
        for (ActivityInfo receiverInfo : allReceivers) {
            if (controller.isCanceled()) {
                Timber.tag(TAG).i("scan canceled");
                return;
            }
            if (appsKillerCmd != null) {
                appsKillerCmd.run();
            }
            ComponentName cn = new ComponentName(receiverInfo.packageName, receiverInfo.name);
            scanReceiverTarget(cxt, cn, receiverInfo.permission);
            if (appsKillerCmd != null) {
                SystemClock.sleep(5000);
            } else {
                SystemClock.sleep(500);
            }
        }
        Timber.tag(TAG).i("receivers scan done");
    }

    public static void scanAllServices(@NonNull Context cxt, @NonNull IScanController controller) {
        String pkgName = controller.getTargetPackageName();
        ServiceInfo[] allServices = PackageUtils.INSTANCE.getAllServices(cxt, pkgName, true);
        if (allServices == null || allServices.length == 0) {
            return;
        }

        Timber.tag(TAG).i("%s services to check...", allServices.length);
        AppsKillerCmd appsKillerCmd = null;
        if (controller.needKillApp()) {
            appsKillerCmd = new AppsKillerCmd(cxt);
            appsKillerCmd.setPackagesToKill(Collections.singletonList(pkgName));
        }
        for (ServiceInfo serviceInfo : allServices) {
            if (controller.isCanceled()) {
                Timber.tag(TAG).i("scan canceled");
                return;
            }
            if (appsKillerCmd != null) {
                appsKillerCmd.run();
            }
            ComponentName cn = new ComponentName(serviceInfo.packageName, serviceInfo.name);
            scanServiceTarget(cxt, cn);
            if (appsKillerCmd != null) {
                SystemClock.sleep(5000);
            } else {
                SystemClock.sleep(500);
            }
        }
        Timber.tag(TAG).i("services scan done");
    }

    public static void scanAllActivities(@NonNull Context cxt, @NonNull IScanController controller) {
        String pkgName = controller.getTargetPackageName();
        ActivityInfo[] allActivities = PackageUtils.INSTANCE.getAllActivities(cxt, pkgName, true);
        if (allActivities == null || allActivities.length == 0) {
            return;
        }

        Timber.tag(TAG).i("%s activities to check...", allActivities.length);
        AppsKillerCmd appsKillerCmd = null;
        if (controller.needKillApp()) {
            appsKillerCmd = new AppsKillerCmd(cxt);
            appsKillerCmd.setPackagesToKill(Collections.singletonList(pkgName));
        }
        for (ActivityInfo activityInfo : allActivities) {
            if (controller.isCanceled()) {
                Timber.tag(TAG).i("scan canceled");
                return;
            }
            if (appsKillerCmd != null) {
                appsKillerCmd.run();
            }
            ComponentName cn = new ComponentName(activityInfo.packageName, activityInfo.name);
            scanActivityTarget(cxt, cn);
            SystemClock.sleep(5000);
        }
        Timber.tag(TAG).i("activities scan done");
    }
}
