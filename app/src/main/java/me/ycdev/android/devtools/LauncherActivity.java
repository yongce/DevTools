package me.ycdev.android.devtools;

import java.util.ArrayList;
import java.util.List;

import me.ycdev.android.arch.utils.AppLogger;
import me.ycdev.android.devtools.apps.installed.InstalledAppsActivity;
import me.ycdev.android.devtools.apps.running.RunningAppsActivity;
import me.ycdev.android.devtools.sampler.AppsSamplerActivity;
import me.ycdev.android.devtools.base.GridEntriesActivity;
import me.ycdev.android.devtools.device.BroadcastTesterActivity;
import me.ycdev.android.devtools.device.DeviceInfoActivity;
import me.ycdev.android.devtools.device.SystemUtilitiesActivity;
import me.ycdev.android.devtools.security.SecurityScannerActivity;

import android.content.Intent;
import android.os.Bundle;

public class LauncherActivity extends GridEntriesActivity {
    private static final String TAG = "LauncherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLogger.d(TAG, "#onCreate()");
    }

    @Override
    protected boolean needLoadIntentsAsync() {
        return true;
    }

    @Override
    protected List<IntentEntry> getIntents() {
        List<IntentEntry> activities = new ArrayList<IntentEntry>();
        activities.add(new IntentEntry(new Intent(this, DeviceInfoActivity.class),
                getString(R.string.module_device_info_title),
                getString(R.string.module_device_info_desc)));
        activities.add(new IntentEntry(new Intent(this, SystemUtilitiesActivity.class),
                getString(R.string.module_system_utilities_title),
                getString(R.string.module_system_utilities_desc)));
        activities.add(new IntentEntry(new Intent(this, InstalledAppsActivity.class),
                getString(R.string.module_installed_apps_title),
                getString(R.string.module_installed_apps_desc)));
        activities.add(new IntentEntry(new Intent(this, BroadcastTesterActivity.class),
                getString(R.string.module_broadcast_tester_title),
                getString(R.string.module_broadcast_tester_desc)));
        activities.add(new IntentEntry(new Intent(this, AppsSamplerActivity.class),
                getString(R.string.apps_sampler_module_title),
                getString(R.string.apps_sampler_module_desc)));
        activities.add(new IntentEntry(new Intent(this, RunningAppsActivity.class),
                getString(R.string.running_apps_module_title),
                getString(R.string.running_apps_module_desc)));
        activities.add(new IntentEntry(new Intent(this, SecurityScannerActivity.class),
                getString(R.string.security_scanner_module_title),
                getString(R.string.security_scanner_module_desc)));
        return activities;
    }
}
