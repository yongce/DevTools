package me.ycdev.android.devtools.root;

import java.util.Arrays;

import me.ycdev.android.devtools.utils.AppConfigs;
import me.ycdev.android.lib.common.internalapi.android.app.ActivityManagerIA;
import timber.log.Timber;

public class RootJarExecutor {
    private static final String TAG = "RootJarExecutor";
    private static final boolean DEBUG = AppConfigs.DEBUG_LOG;

    public static final String CMD_FORCE_STOP_PACKAGE = "amForceStop";

    private String[] mArgs;

    public static void main(String[] args) {
        if (DEBUG) Timber.tag(TAG).d("Received params: " + Arrays.toString(args));
        if (args.length < 1) {
            if (DEBUG) Timber.tag(TAG).e("Usage: RootJarExecutor <command> [command parameters]");
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
                Timber.tag(TAG).e("Usage: RootJarExecutor amForceStop <file path>");
            }
        }
    }

    public static void forceStopPackage(String params) {
        String[] pkgNames = params.split("#");
        Object service = ActivityManagerIA.INSTANCE.getIActivityManager();
        if (service != null) {
            if (DEBUG) Timber.tag(TAG).d("to kill apps: %s", Arrays.toString(pkgNames));
            for (String pkg : pkgNames) {
                ActivityManagerIA.INSTANCE.forceStopPackage(service, pkg);
            }
        }
    }
}
