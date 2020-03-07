package me.ycdev.android.devtools.apps.selector

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.AsyncTask.Status.FINISHED
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import me.ycdev.android.arch.activity.AppCompatBaseActivity
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.apps.selector.AppsSelectorAdapter.SelectedAppsChangeListener
import me.ycdev.android.devtools.databinding.ActAppsSelectorBinding
import me.ycdev.android.lib.common.apps.AppInfo
import me.ycdev.android.lib.common.apps.AppsLoadConfig
import me.ycdev.android.lib.common.apps.AppsLoadFilter
import me.ycdev.android.lib.common.apps.AppsLoadListener
import me.ycdev.android.lib.common.apps.AppsLoader
import me.ycdev.android.lib.common.wrapper.IntentHelper
import me.ycdev.android.lib.commonui.base.LoadingAsyncTaskBase
import java.util.ArrayList

class AppsSelectorActivity : AppCompatBaseActivity(), SelectedAppsChangeListener, OnClickListener {
    private lateinit var binding: ActAppsSelectorBinding

    private var excludeUninstalled = false
    private var excludeDisabled = false
    private var excludeSystem = false

    private lateinit var adapter: AppsSelectorAdapter
    private var appsLoader: AppsLoadingTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActAppsSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val multiChoice = IntentHelper.getBooleanExtra(
            intent,
            EXTRA_MULTICHOICE,
            DEFAULT_MULTICHOICE
        )
        excludeUninstalled = IntentHelper.getBooleanExtra(
            intent,
            EXTRA_EXCLUDE_UNINSTALLED,
            DEFAULT_EXCLUDE_UNINSTALLED
        )
        excludeDisabled = IntentHelper.getBooleanExtra(
            intent,
            EXTRA_EXCLUDE_DISABLED,
            DEFAULT_EXCLUDE_DISABLED
        )
        excludeSystem = IntentHelper.getBooleanExtra(
            intent,
            EXTRA_EXCLUDE_SYSTEM,
            DEFAULT_EXCLUDE_SYSTEM
        )

        adapter = AppsSelectorAdapter(this, this, multiChoice)
        binding.list.adapter = adapter
        binding.select.setOnClickListener(this)

        loadApps()
    }

    private fun loadApps() {
        if (appsLoader != null && appsLoader!!.status != FINISHED) {
            appsLoader?.cancel(true)
        }
        appsLoader = AppsLoadingTask(this)
        appsLoader?.execute()
    }

    override fun onSelectedAppsChanged(newCount: Int) {
        if (newCount == 0) {
            binding.status.setText(R.string.apps_selector_status_no_apps_selected)
        } else if (newCount == 1) {
            val status = getString(
                R.string.apps_selector_status_one_app_selected,
                adapter.oneSelectedApp?.pkgName
            )
            binding.status.text = status
        } else {
            val status = resources.getQuantityString(
                R.plurals.apps_selector_status_multiple_apps_selected, newCount, newCount
            )
            binding.status.text = status
        }
    }

    private fun setSelectedApps() {
        val apps = adapter.selectedApps
        val pkgNames = ArrayList<String>(apps.size)
        for (appInfo in apps) {
            pkgNames.add(appInfo.pkgName)
        }

        val result = Intent()
        result.putExtra(RESULT_EXTRA_APPS_PKG_NAMES, pkgNames)
        setResult(Activity.RESULT_OK, result)
    }

    override fun onClick(v: View) {
        if (v === binding.select) {
            setSelectedApps()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (appsLoader?.status != FINISHED) {
            appsLoader?.cancel(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    @SuppressLint("StaticFieldLeak")
    private inner class AppsLoadingTask(activity: Activity) :
        LoadingAsyncTaskBase<Void, List<AppInfo>>(activity) {
        override fun onPreExecute() {
            super.onPreExecute()
            onSelectedAppsChanged(0)
        }

        override fun doInBackground(vararg params: Void): List<AppInfo> {
            val filter = AppsLoadFilter()
            filter.onlyMounted = excludeUninstalled
            filter.onlyEnabled = excludeDisabled
            filter.includeSysApp = !excludeSystem
            val config = AppsLoadConfig()
            val listener: AppsLoadListener = object : AppsLoadListener {
                override fun isCancelled(): Boolean {
                    return this@AppsLoadingTask.isCancelled
                }

                override fun onProgressUpdated(
                    percent: Int,
                    appInfo: AppInfo
                ) {
                    publishProgress(percent)
                }
            }
            return AppsLoader.getInstance(activity).loadInstalledApps(filter, config, listener)
        }

        override fun onPostExecute(result: List<AppInfo>) {
            super.onPostExecute(result)
            adapter.setData(result)
        }
    }

    companion object {
        /** Type: boolean, default value: {@value #DEFAULT_MULTICHOICE}  */
        const val EXTRA_MULTICHOICE = "extra.multichoice"
        /** Type: boolean, default value: {@value #DEFAULT_EXCLUDE_UNINSTALLED} */
        const val EXTRA_EXCLUDE_UNINSTALLED = "extra.exclude_uninstalled"
        /** Type: boolean, default value: {@value #DEFAULT_EXCLUDE_DISABLED  */
        const val EXTRA_EXCLUDE_DISABLED = "extra.exclude_disabled"
        /** Type: boolean, default value: {@value #DEFAULT_EXCLUDE_SYSTEM}  */
        const val EXTRA_EXCLUDE_SYSTEM = "extra.exclude_system"
        /** Type: ArrayList<String> </String> */
        const val RESULT_EXTRA_APPS_PKG_NAMES = "extra.pkg_names"

        private const val DEFAULT_MULTICHOICE = false
        private const val DEFAULT_EXCLUDE_UNINSTALLED = true
        private const val DEFAULT_EXCLUDE_DISABLED = true
        private const val DEFAULT_EXCLUDE_SYSTEM = false
    }
}