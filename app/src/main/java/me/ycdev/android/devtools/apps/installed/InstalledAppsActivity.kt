package me.ycdev.android.devtools.apps.installed

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import me.ycdev.android.arch.activity.AppCompatBaseActivity
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.databinding.ActCommonListBinding
import me.ycdev.android.lib.common.apps.AppInfo.AppNameComparator
import me.ycdev.android.lib.common.apps.AppInfo.InstallTimeComparator
import me.ycdev.android.lib.common.apps.AppInfo.MinSdkComparator
import me.ycdev.android.lib.common.apps.AppInfo.PkgNameComparator
import me.ycdev.android.lib.common.apps.AppInfo.TargetSdkComparator
import me.ycdev.android.lib.common.apps.AppInfo.UidComparator
import me.ycdev.android.lib.common.apps.AppInfo.UpdateTimeComparator
import timber.log.Timber

class InstalledAppsActivity : AppCompatBaseActivity() {
    private lateinit var binding: ActCommonListBinding

    private lateinit var listAdapter: InstalledAppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActCommonListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listAdapter = InstalledAppsAdapter(this)
        binding.list.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(this@InstalledAppsActivity)
            addItemDecoration(
                DividerItemDecoration(this@InstalledAppsActivity, DividerItemDecoration.VERTICAL)
            )
        }

        val viewModel = ViewModelProvider(this).get(InstalledAppsViewModel::class.java)
        viewModel.apps.observe(this, Observer {
            Timber.tag(TAG).d("apps loaded: ${it.size}")
            listAdapter.data = it
            listAdapter.sort(AppNameComparator())
            binding.progress.visibility = View.GONE
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.installed_apps_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sort_by_app_name -> {
                listAdapter.sort(AppNameComparator())
                item.isChecked = true
                return true
            }
            R.id.action_sort_by_pkg_name -> {
                listAdapter.sort(PkgNameComparator())
                item.isChecked = true
                return true
            }
            R.id.action_sort_by_uid -> {
                listAdapter.sort(UidComparator())
                item.isChecked = true
                return true
            }
            R.id.action_sort_by_install_time -> {
                listAdapter.sort(InstallTimeComparator())
                item.isChecked = true
                return true
            }
            R.id.action_sort_by_update_time -> {
                listAdapter.sort(UpdateTimeComparator())
                item.isChecked = true
                return true
            }
            R.id.action_sort_by_target_sdk -> {
                listAdapter.sort(TargetSdkComparator())
                item.isChecked = true
                return true
            }
            R.id.action_sort_byt_min_sdk -> {
                listAdapter.sort(MinSdkComparator())
                item.isChecked = true
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val TAG = "InstalledAppsActivity"
    }
}
