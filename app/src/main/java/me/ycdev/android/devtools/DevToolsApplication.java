package me.ycdev.android.devtools;
import me.ycdev.android.devtools.utils.AppLogger;

import android.app.Application;

public class DevToolsApplication extends Application {
    private static final String TAG = "DevToolsApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        AppLogger.i(TAG, "app start...");
    }
}
