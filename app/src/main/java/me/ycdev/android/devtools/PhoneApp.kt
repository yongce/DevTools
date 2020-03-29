package me.ycdev.android.devtools

import android.app.Application
import android.os.Process
import timber.log.Timber
import timber.log.Timber.DebugTree

class PhoneApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        Timber.tag(TAG).i("app start...")
        checkAppReplacingState()
    }

    private fun checkAppReplacingState() {
        if (resources == null) {
            Timber.tag(TAG).w("app is replacing...kill")
            Process.killProcess(Process.myPid())
        }
    }

    companion object {
        private const val TAG = "DevToolsApp"
    }
}
