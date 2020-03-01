package me.ycdev.android.devtools;

import android.app.Application;
import android.os.Process;

import timber.log.Timber;

public class PhoneApp extends Application {
    private static final String TAG = "DevToolsApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        Timber.tag(TAG).i("app start...");
        checkAppReplacingState();
    }

    private void checkAppReplacingState() {
        if (getResources() == null) {
            Timber.tag(TAG).w("app is replacing...kill");
            Process.killProcess(Process.myPid());
        }
    }
}
