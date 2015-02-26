package me.ycdev.android.devtools;

import java.util.ArrayList;
import java.util.List;

import me.ycdev.android.devtools.apps.installed.InstalledAppsActivity;
import me.ycdev.android.devtools.apps.running.RunningAppsActivity;
import me.ycdev.android.devtools.sampler.AppsSamplerActivity;
import me.ycdev.android.devtools.base.GridEntriesActivity;
import me.ycdev.android.devtools.device.BroadcastTester;
import me.ycdev.android.devtools.device.DeviceInfoActivity;
import me.ycdev.android.devtools.device.SystemUtilitiesActivity;

import android.content.Intent;

public class LauncherActivity extends GridEntriesActivity {
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
        activities.add(new IntentEntry(new Intent(this, BroadcastTester.class),
                getString(R.string.module_broadcast_tester_title),
                getString(R.string.module_broadcast_tester_desc)));
        activities.add(new IntentEntry(new Intent(this, AppsSamplerActivity.class),
                getString(R.string.apps_sampler_module_title),
                getString(R.string.apps_sampler_module_desc)));
        activities.add(new IntentEntry(new Intent(this, RunningAppsActivity.class),
                getString(R.string.running_apps_module_title),
                getString(R.string.running_apps_module_desc)));
        return activities;
    }
}
