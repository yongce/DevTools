package me.ycdev.android.devtools.root;

import java.util.Arrays;

import me.ycdev.android.arch.utils.AppConfigs;
import me.ycdev.android.arch.utils.AppLogger;
import me.ycdev.android.lib.common.internalapi.android.app.ActivityManagerIA;

public class RootJarExecutor {
    private static final String TAG = "RootJarExecutor";
    private static final boolean DEBUG = AppConfigs.DEBUG_LOG;

    public static final String CMD_FORCE_STOP_PACKAGE = "amForceStop";

    private String[] mArgs;

    public static void main(String[] args) {
        if (DEBUG) AppLogger.d(TAG, "Received params: " + Arrays.toString(args));
        if (args.length < 1) {
            if (DEBUG) AppLogger.e(TAG, "Usage: RootJarExecutor <command> [command parameters]");
            return;
        }

        new RootJarExecutor(args).execute();
    }

    private RootJarExecutor(String[] args) {
        mArgs = args;
    }

    private void execute() {
        String cmd = mArgs[0];
        if (cmd.equals(CMD_FORCE_STOP_PACKAGE)) {
            if (mArgs.length > 1) {
                String filePath = mArgs[1];
                forceStopPackage(filePath);
            } else {
                AppLogger.e(TAG, "Usage: RootJarExecutor amForceStop <file path>");
            }
        }
    }

    public static void forceStopPackage(String params) {
        String[] pkgNames = params.split("#");
        Object service = ActivityManagerIA.getIActivityManager();
        if (service != null) {
            if (DEBUG) AppLogger.d(TAG, "to kill apps: " + Arrays.toString(pkgNames));
            for (String pkg : pkgNames) {
                ActivityManagerIA.forceStopPackage(service, pkg);
            }
        }
    }
}
