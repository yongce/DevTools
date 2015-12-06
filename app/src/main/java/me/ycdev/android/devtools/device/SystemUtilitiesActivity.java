package me.ycdev.android.devtools.device;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;

import me.ycdev.android.devtools.R;
import me.ycdev.android.lib.commonui.activity.GridEntriesActivity;

public class SystemUtilitiesActivity extends GridEntriesActivity {
    protected List<IntentEntry> getIntents() {
        List<IntentEntry> activities = new ArrayList<IntentEntry>();
        activities.add(new IntentEntry(new Intent("android.settings.APP_OPS_SETTINGS"),
                getString(R.string.module_apps_ops_title),
                getString(R.string.module_apps_ops_desc)));

        Intent appsMgrIntent = new Intent("com.android.settings.applications");
        appsMgrIntent.setClassName("com.android.settings",
                "com.android.settings.applications.ManageApplications");
        activities.add(new IntentEntry(appsMgrIntent,
                getString(R.string.module_apps_mgr_title),
                getString(R.string.module_apps_mgr_desc)));

        Intent testSettingsIntent = new Intent().setClassName("com.android.settings",
                "com.android.settings.TestingSettings");
        activities.add(new IntentEntry(testSettingsIntent,
                getString(R.string.module_testing_settings_title),
                getString(R.string.module_testing_settings_desc)));
        return activities;
    }
}
