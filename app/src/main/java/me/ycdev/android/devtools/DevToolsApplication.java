package me.ycdev.android.devtools;

import android.app.Application;
import android.os.Process;

import me.ycdev.android.arch.utils.AppLogger;

public class DevToolsApplication extends Application {
    private static final String TAG = "DevToolsApplication";

    @Override
    public void onCreate() {
        super.onCreate();
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
