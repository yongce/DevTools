package me.ycdev.android.devtools;

import android.app.Application;
import android.os.Process;

import me.ycdev.android.arch.utils.AppLogger;
import timber.log.Timber;

public class PhoneApp extends Application {
    private static final String TAG = "DevToolsApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        AppLogger.i(TAG, "app start...");
        checkAppReplacingState();
    }

    private void checkAppReplacingState() {
        if (getResources() == null) {
            AppLogger.w(TAG, "app is replacing...kill");
            Process.killProcess(Process.myPid());
        }
    }
}
