package me.ycdev.android.devtools.security.unmarshall;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.IntentCompat;

import java.util.Collections;

import me.ycdev.android.devtools.root.cmd.AppsKillerCmd;
import me.ycdev.android.devtools.security.foo.ParcelableTest;
import me.ycdev.android.devtools.security.foo.SerializableTest;
import me.ycdev.android.devtools.utils.AppLogger;
import me.ycdev.android.lib.common.utils.PackageUtils;

public class IntentUnmarshallScanner {
    private static final String TAG = "IntentUnmarshallScanner";

    private static Intent buildScanIntent(ComponentName target) {
        Intent intent = new Intent();
        intent.setComponent(target);
        intent.putExtra("test_serializable", new SerializableTest());
        intent.putExtra("test_parcelable", new ParcelableTest());
        return intent;
    }

    public static boolean scanReceiverTarget(Context cxt, ComponentName target, String perm) {
        AppLogger.i(TAG, "scan receiver: " + target + ", with perm: " + perm);
        try {
            Intent intent = buildScanIntent(target);
            cxt.sendBroadcast(intent, perm);
            return true;
        } catch (Exception e) {
            AppLogger.w(TAG, "failed to send broadcast", e);
        }
        return false;
    }

    public static boolean scanServiceTarget(Context cxt, ComponentName target) {
        AppLogger.i(TAG, "scan service: " + target);
        try {
            Intent intent = buildScanIntent(target);
            cxt.startService(intent);
            return true;
        } catch (Exception e) {
            AppLogger.w(TAG, "failed to send broadcast", e);
        }
        return false;
    }

    public static boolean scanActivityTarget(Context cxt, ComponentName target) {
        AppLogger.i(TAG, "scan activity: " + target);
        try {
            Intent intent = buildScanIntent(target);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
            cxt.startActivity(intent);
            return true;
        } catch (Exception e) {
            AppLogger.w(TAG, "failed to send broadcast", e);
        }
        return false;
    }

    public static void scanAllReceivers(@NonNull Context cxt, @NonNull IScanController controller) {
        String pkgName = controller.getTargetPackageName();
        ActivityInfo[] allReceivers = PackageUtils.getAllReceivers(cxt, pkgName, true);
        if (allReceivers == null || allReceivers.length == 0) {
            return;
        }

        AppLogger.i(TAG, allReceivers.length + " receivers to scan...");
        AppsKillerCmd appsKillerCmd = null;
        if (controller.needKillApp()) {
            appsKillerCmd = new AppsKillerCmd(cxt);
            appsKillerCmd.setPackagesToKill(Collections.singletonList(pkgName));
        }
        for (ActivityInfo receiverInfo : allReceivers) {
            if (controller.isCanceled()) {
                AppLogger.i(TAG, "scan canceled");
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
        AppLogger.i(TAG, "receivers scan done");
    }

    public static void scanAllServices(@NonNull Context cxt, @NonNull IScanController controller) {
        String pkgName = controller.getTargetPackageName();
        ServiceInfo[] allServices = PackageUtils.getAllServices(cxt, pkgName, true);
        if (allServices == null || allServices.length == 0) {
            return;
        }

        AppLogger.i(TAG, allServices.length + " services to check...");
        AppsKillerCmd appsKillerCmd = null;
        if (controller.needKillApp()) {
            appsKillerCmd = new AppsKillerCmd(cxt);
            appsKillerCmd.setPackagesToKill(Collections.singletonList(pkgName));
        }
        for (ServiceInfo serviceInfo : allServices) {
            if (controller.isCanceled()) {
                AppLogger.i(TAG, "scan canceled");
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
        AppLogger.i(TAG, "services scan done");
    }

    public static void scanAllActivities(@NonNull Context cxt, @NonNull IScanController controller) {
        String pkgName = controller.getTargetPackageName();
        ActivityInfo[] allActivities = PackageUtils.getAllActivities(cxt, pkgName, true);
        if (allActivities == null || allActivities.length == 0) {
            return;
        }

        AppLogger.i(TAG, allActivities.length + " activities to check...");
        AppsKillerCmd appsKillerCmd = null;
        if (controller.needKillApp()) {
            appsKillerCmd = new AppsKillerCmd(cxt);
            appsKillerCmd.setPackagesToKill(Collections.singletonList(pkgName));
        }
        for (ActivityInfo activityInfo : allActivities) {
            if (controller.isCanceled()) {
                AppLogger.i(TAG, "scan canceled");
                return;
            }
            if (appsKillerCmd != null) {
                appsKillerCmd.run();
            }
            ComponentName cn = new ComponentName(activityInfo.packageName, activityInfo.name);
            scanActivityTarget(cxt, cn);
            SystemClock.sleep(5000);
        }
        AppLogger.i(TAG, "activities scan done");
    }
}
