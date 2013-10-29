package me.ycdev.android.devtools;

import java.util.ArrayList;
import java.util.List;

import me.ycdev.android.devtools.apps.InstalledAppsActivity;
import me.ycdev.android.devtools.base.GridEntriesActivity;
import me.ycdev.android.devtools.device.DeviceInfoActivity;
import me.ycdev.android.devtools.device.SystemUtilitiesActivity;

import android.content.Intent;

public class LauncherActivity extends GridEntriesActivity {
    @Override
    protected boolean needLoadIntentsAsync() {
        return true;
    }

    @Override
    protected List<ActivityEntry> getIntents() {
        List<ActivityEntry> activities = new ArrayList<ActivityEntry>();
        activities.add(new ActivityEntry(new Intent(this, DeviceInfoActivity.class),
                getString(R.string.module_device_info_title),
                getString(R.string.module_device_info_desc)));
        activities.add(new ActivityEntry(new Intent(this, SystemUtilitiesActivity.class),
                getString(R.string.module_system_utilities_title),
                getString(R.string.module_system_utilities_desc)));
        activities.add(new ActivityEntry(new Intent(this, InstalledAppsActivity.class),
                getString(R.string.module_installed_apps_title),
                getString(R.string.module_installed_apps_desc)));
        return activities;
    }
}
