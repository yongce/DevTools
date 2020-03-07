package me.ycdev.android.devtools.apps.installed

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import me.ycdev.android.arch.activity.AppCompatBaseActivity
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.databinding.InstalledAppsBinding
import me.ycdev.android.lib.common.apps.AppInfo
import me.ycdev.android.lib.common.apps.AppInfo.AppNameComparator
import me.ycdev.android.lib.common.apps.AppInfo.InstallTimeComparator
import me.ycdev.android.lib.common.apps.AppInfo.PkgNameComparator
import me.ycdev.android.lib.common.apps.AppInfo.UidComparator
import me.ycdev.android.lib.common.apps.AppInfo.UpdateTimeComparator
import me.ycdev.android.lib.common.apps.AppsLoadConfig
import me.ycdev.android.lib.common.apps.AppsLoadFilter
import me.ycdev.android.lib.common.apps.AppsLoadListener
import me.ycdev.android.lib.common.apps.AppsLoader
import me.ycdev.android.lib.commonui.base.LoadingAsyncTaskBase
import timber.log.Timber

class InstalledAppsActivity : AppCompatBaseActivity(), OnItemClickListener {
    private lateinit var binding: InstalledAppsBinding
    private lateinit var adapter: InstalledAppsAdapter
    private var appsLoader: AppsLoadingTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = InstalledAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = InstalledAppsAdapter(this)
        binding.list.adapter = adapter
        binding.list.onItemClickListener = this

        loadApps()
    }

    private fun loadApps() {
        if (appsLoader != null && appsLoader?.status != AsyncTask.Status.FINISHED) {
            appsLoader?.cancel(true)
        }
        appsLoader = AppsLoadingTask(this)
        appsLoader?.execute()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.installed_apps_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sort_by_app_name -> {
                adapter.sort(AppNameComparator())
                item.isChecked = true
                return true
            }
            R.id.action_sort_by_pkg_name -> {
                adapter.sort(PkgNameComparator())
                item.isChecked = true
                return true
            }
            R.id.action_sort_by_uid -> {
                adapter.sort(UidComparator())
                item.isChecked = true
                return true
            }
            R.id.action_sort_by_install_time -> {
                adapter.sort(InstallTimeComparator())
                item.isChecked = true
                return true
            }
            R.id.action_sort_by_update_time -> {
                adapter.sort(UpdateTimeComparator())
                item.isChecked = true
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val item = adapter.getItem(position)
        Timber.tag(TAG).i("clicked item: %s", item.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (appsLoader != null && appsLoader?.status != AsyncTask.Status.FINISHED) {
            appsLoader?.cancel(true)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class AppsLoadingTask(cxt: Activity) :
        LoadingAsyncTaskBase<Void, List<AppInfo>>(cxt) {

        override fun doInBackground(vararg params: Void): List<AppInfo> {
            val filter = AppsLoadFilter()
            filter.onlyMounted = false
            filter.onlyEnabled = false
            val config = AppsLoadConfig()
            val listener: AppsLoadListener = object : AppsLoadListener {
                override fun isCancelled(): Boolean {
                    return this@AppsLoadingTask.isCancelled
                }

                override fun onProgressUpdated(percent: Int, appInfo: AppInfo) {
                    publishProgress(percent)
                }
            }
            return AppsLoader.getInstance(activity).loadInstalledApps(filter, config, listener)
        }

        override fun onPostExecute(result: List<AppInfo>) {
            super.onPostExecute(result)
            adapter.setData(result)
            adapter.sort(AppNameComparator())
        }
    }

    companion object {
        private const val TAG = "InstalledAppsActivity"
    }
}