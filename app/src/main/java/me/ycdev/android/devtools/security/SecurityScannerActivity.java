package me.ycdev.android.devtools.security;

import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.base.GridEntriesActivity;
import me.ycdev.android.devtools.security.unmarshall.UnmarshallScannerActivity;

public class SecurityScannerActivity extends GridEntriesActivity {
    @Override
    protected List<IntentEntry> getIntents() {
        List<IntentEntry> activities = new ArrayList<IntentEntry>();
        activities.add(new IntentEntry(new Intent(this, UnmarshallScannerActivity.class),
                getString(R.string.security_scanner_unmarshall_module_title),
                getString(R.string.security_scanner_unmarshall_module_desc)));
        return activities;
    }
}
