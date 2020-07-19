package me.ycdev.android.devtools

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.android.material.snackbar.Snackbar
import java.util.ArrayList
import me.ycdev.android.devtools.apps.installed.InstalledAppsActivity
import me.ycdev.android.devtools.apps.running.RunningAppsActivity
import me.ycdev.android.devtools.contacts.ContactsActivity
import me.ycdev.android.devtools.device.BroadcastTesterActivity
import me.ycdev.android.devtools.device.DeviceInfoActivity
import me.ycdev.android.devtools.device.SystemUtilitiesActivity
import me.ycdev.android.devtools.sampler.AppsSamplerActivity
import me.ycdev.android.devtools.security.SecurityScannerActivity
import me.ycdev.android.lib.commonui.activity.GridEntriesActivity
import timber.log.Timber

open class LauncherActivity : GridEntriesActivity(), OnNavigationItemSelectedListener {
    private lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG).d("#onCreate()")
        super.onCreate(savedInstanceState)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
    }

    override val contentViewLayout: Int = R.layout.act_main

    override val needLoadIntentsAsync: Boolean = true

    override fun loadIntents(): List<Entry> {
        val activities: MutableList<Entry> = ArrayList()
        activities.add(
            IntentEntry(
                Intent(this, DeviceInfoActivity::class.java),
                getString(R.string.module_device_info_title),
                getString(R.string.module_device_info_desc)
            )
        )
        activities.add(
            IntentEntry(
                Intent(this, SystemUtilitiesActivity::class.java),
                getString(R.string.module_system_utilities_title),
                getString(R.string.module_system_utilities_desc)
            )
        )
        activities.add(
            IntentEntry(
                Intent(this, InstalledAppsActivity::class.java),
                getString(R.string.module_installed_apps_title),
                getString(R.string.module_installed_apps_desc)
            )
        )
        activities.add(
            IntentEntry(
                Intent(this, BroadcastTesterActivity::class.java),
                getString(R.string.module_broadcast_tester_title),
                getString(R.string.module_broadcast_tester_desc)
            )
        )
        activities.add(
            IntentEntry(
                Intent(this, AppsSamplerActivity::class.java),
                getString(R.string.apps_sampler_module_title),
                getString(R.string.apps_sampler_module_desc)
            )
        )
        activities.add(
            IntentEntry(
                Intent(this, RunningAppsActivity::class.java),
                getString(R.string.running_apps_module_title),
                getString(R.string.running_apps_module_desc)
            )
        )
        activities.add(
            IntentEntry(
                Intent(this, SecurityScannerActivity::class.java),
                getString(R.string.security_scanner_module_title),
                getString(R.string.security_scanner_module_desc)
            )
        )
        activities.add(
            IntentEntry(
                Intent(this, ContactsActivity::class.java),
                getString(R.string.contacts_module_title),
                getString(R.string.contacts_module_desc)
            )
        )
        return activities
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            Snackbar.make(gridView, "Not implemented yet!", Snackbar.LENGTH_SHORT).show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean { // Handle navigation view item clicks here.
        val id = item.itemId
        if (id == R.id.nav_visit_homepage) {
            openHomePage()
        } else if (id == R.id.nav_send_email) {
            sendEmail()
        } else if (id == R.id.nav_share) {
            share()
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun openHomePage() {
        val url = getString(R.string.developer_home_page_url)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(packageManager) != null) {
            val title = getString(R.string.nav_visit_homepage)
            startActivity(Intent.createChooser(intent, title))
        } else {
            Snackbar.make(gridView, R.string.nav_tip_no_web_apps, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(
            Intent.EXTRA_EMAIL,
            arrayOf(getString(R.string.nav_send_email_address))
        )
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.nav_send_subject))
        if (intent.resolveActivity(packageManager) != null) {
            val title = getString(R.string.nav_send_email)
            startActivity(Intent.createChooser(intent, title))
        } else {
            Snackbar.make(gridView, R.string.nav_tip_no_email_apps, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun share() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.nav_send_subject))
        if (intent.resolveActivity(packageManager) != null) {
            val title = getString(R.string.nav_share)
            startActivity(Intent.createChooser(intent, title))
        } else {
            Snackbar.make(gridView, R.string.nav_tip_no_available_apps, Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    companion object {
        private const val TAG = "LauncherActivity"
    }
}
