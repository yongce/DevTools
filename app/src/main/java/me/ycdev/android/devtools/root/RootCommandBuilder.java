package me.ycdev.android.devtools.root;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;

/**
 * Used to create commands to be executed in root shell.
 */
public class RootCommandBuilder {
    private RootCommandBuilder() {
        // nothing to do
    }

    private static String[] getRootJarCommand(Context cxt, String cmd, String cmdParams) {
        String exportCmd = "export CLASSPATH=" + cxt.getPackageCodePath();
        String rootJarCmd = "/system/bin/app_process /system/bin "
                + RootJarExecutor.class.getName() + " " + cmd;
        if (!TextUtils.isEmpty(cmdParams)) {
            rootJarCmd = rootJarCmd + " " + cmdParams;
        }
        return new String[] { exportCmd, rootJarCmd };
    }

    public static String[] forceStopPackage(Context cxt, List<String> pkgNames) {
        StringBuilder sb = new StringBuilder();
        for (String pkg : pkgNames) {
            sb.append(pkg).append("#");
        }
        return getRootJarCommand(cxt, RootJarExecutor.CMD_FORCE_STOP_PACKAGE, sb.toString());
    }
}
