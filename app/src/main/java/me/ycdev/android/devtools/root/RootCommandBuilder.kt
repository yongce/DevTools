package me.ycdev.android.devtools.root

import android.content.Context
import android.text.TextUtils

/**
 * Used to create commands to be executed in root shell.
 */
object RootCommandBuilder {
    private fun getRootJarCommand(
        cxt: Context,
        cmd: String,
        cmdParams: String
    ): Array<String> {
        val exportCmd = "export CLASSPATH=" + cxt.packageCodePath
        var rootJarCmd = ("/system/bin/app_process /system/bin " +
                RootJarExecutor::class.java.name + " " + cmd)
        if (!TextUtils.isEmpty(cmdParams)) {
            rootJarCmd = "$rootJarCmd $cmdParams"
        }
        return arrayOf(exportCmd, rootJarCmd)
    }

    fun forceStopPackage(
        cxt: Context,
        pkgNames: List<String?>
    ): Array<String> {
        val sb = StringBuilder()
        for (pkg in pkgNames) {
            sb.append(pkg).append("#")
        }
        return getRootJarCommand(
            cxt,
            RootJarExecutor.CMD_FORCE_STOP_PACKAGE,
            sb.toString()
        )
    }
}
