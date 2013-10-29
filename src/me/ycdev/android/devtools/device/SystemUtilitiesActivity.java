package me.ycdev.android.devtools.device;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.base.GridEntriesActivity;

public class SystemUtilitiesActivity extends GridEntriesActivity {
    protected List<ActivityEntry> getIntents() {
        List<ActivityEntry> activities = new ArrayList<ActivityEntry>();
        activities.add(new ActivityEntry(new Intent("android.settings.APP_OPS_SETTINGS"),
                getString(R.string.module_apps_ops_title),
                getString(R.string.module_apps_ops_desc)));

        Intent appsMgrIntent = new Intent("com.android.settings.applications");
        appsMgrIntent.setClassName("com.android.settings",
                "com.android.settings.applications.ManageApplications");
        activities.add(new ActivityEntry(appsMgrIntent,
                getString(R.string.module_apps_mgr_title),
                getString(R.string.module_apps_mgr_desc)));

        Intent testSettingsIntent = new Intent().setClassName("com.android.settings",
                "com.android.settings.TestingSettings");
        activities.add(new ActivityEntry(testSettingsIntent,
                getString(R.string.module_testing_settings_title),
                getString(R.string.module_testing_settings_desc)));
        return activities;
    }
}
