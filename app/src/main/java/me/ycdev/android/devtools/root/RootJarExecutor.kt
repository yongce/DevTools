package me.ycdev.android.devtools.root

import me.ycdev.android.lib.common.internalapi.android.app.ActivityManagerIA
import timber.log.Timber

class RootJarExecutor private constructor(private val args: Array<String>) {
    private fun execute() {
        val cmd = args[0]
        if (cmd == CMD_FORCE_STOP_PACKAGE) {
            if (args.size > 1) {
                val filePath = args[1]
                forceStopPackage(filePath)
            } else {
                Timber.tag(TAG).e("Usage: RootJarExecutor amForceStop <file path>")
            }
        }
    }

    companion object {
        private const val TAG = "RootJarExecutor"

        const val CMD_FORCE_STOP_PACKAGE = "amForceStop"

        @JvmStatic
        fun main(args: Array<String>) {
            Timber.tag(TAG).d(
                "Received params: %s", args.contentToString()
            )
            if (args.isEmpty()) {
                Timber.tag(TAG).e("Usage: RootJarExecutor <command> [command parameters]")
                return
            }
            RootJarExecutor(args).execute()
        }

        fun forceStopPackage(params: String) {
            val pkgNames = params.split("#").toTypedArray()
            val service = ActivityManagerIA.getIActivityManager()
            if (service != null) {
                Timber.tag(TAG).d(
                    "to kill apps: %s",
                    pkgNames.contentToString()
                )
                for (pkg in pkgNames) {
                    ActivityManagerIA.forceStopPackage(service, pkg)
                }
            }
        }
    }
}