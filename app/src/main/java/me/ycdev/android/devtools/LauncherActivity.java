package me.ycdev.android.devtools;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import me.ycdev.android.arch.utils.AppLogger;
import me.ycdev.android.devtools.apps.installed.InstalledAppsActivity;
import me.ycdev.android.devtools.apps.running.RunningAppsActivity;
import me.ycdev.android.devtools.contacts.ContactsActivity;
import me.ycdev.android.devtools.device.BroadcastTesterActivity;
import me.ycdev.android.devtools.device.DeviceInfoActivity;
import me.ycdev.android.devtools.device.SystemUtilitiesActivity;
import me.ycdev.android.devtools.sampler.AppsSamplerActivity;
import me.ycdev.android.devtools.security.SecurityScannerActivity;
import me.ycdev.android.lib.commonui.activity.GridEntriesActivity;

public class LauncherActivity extends GridEntriesActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "LauncherActivity";

    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppLogger.d(TAG, "#onCreate()");
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected int getContentViewLayout() {
        return R.layout.act_main;
    }

    @Override
    protected boolean needLoadIntentsAsync() {
        return true;
    }

    @Override
    protected List<IntentEntry> getIntents() {
        List<IntentEntry> activities = new ArrayList<>();
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
        activities.add(new IntentEntry(new Intent(this, ContactsActivity.class),
                getString(R.string.contacts_module_title),
                getString(R.string.contacts_module_desc)));
        return activities;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Snackbar.make(mGridView, "Not implemented yet!", Snackbar.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_visit_homepage) {
            openHomePage();
        } else if (id == R.id.nav_send_email) {
            sendEmail();
        } else if (id == R.id.nav_share) {
            share();
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void openHomePage() {
        String url = getString(R.string.developer_home_page_url);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (intent.resolveActivity(getPackageManager()) != null) {
            String title = getString(R.string.nav_visit_homepage);
            startActivity(Intent.createChooser(intent, title));
        } else {
            Snackbar.make(mGridView, R.string.nav_tip_no_web_apps, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL,
                new String[] {getString(R.string.nav_send_email_address)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.nav_send_subject));
        if (intent.resolveActivity(getPackageManager()) != null) {
            String title = getString(R.string.nav_send_email);
            startActivity(Intent.createChooser(intent, title));
        } else {
            Snackbar.make(mGridView, R.string.nav_tip_no_email_apps, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.nav_send_subject));
        if (intent.resolveActivity(getPackageManager()) != null) {
            String title = getString(R.string.nav_share);
            startActivity(Intent.createChooser(intent, title));
        } else {
            Snackbar.make(mGridView, R.string.nav_tip_no_available_apps, Snackbar.LENGTH_SHORT).show();
        }
    }
}
