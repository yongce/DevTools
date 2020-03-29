package me.ycdev.android.devtools.root.cmd

import android.content.Context
import me.ycdev.android.devtools.root.RootCommandBuilder
import timber.log.Timber

class AppsKillerCmd(cxt: Context, private val pkgList: List<String>) : RootCmdBase(cxt) {
    override fun run() {
        if (pkgList.isEmpty()) {
            Timber.tag(TAG).w("no apps to kill")
            return
        }
        val cmds = RootCommandBuilder.forceStopPackage(context, pkgList)
        RootCmdBase.Companion.runSuCommand(cmds, "u:r:system_app:s0")
    }

    companion object {
        private const val TAG = "AppsKillerCmd"
    }
}
