package me.ycdev.android.devtools.apps.selector

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import me.ycdev.android.arch.activity.AppCompatBaseActivity
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.apps.selector.AppsSelectorAdapter.SelectedAppsChangeListener
import me.ycdev.android.devtools.databinding.ActAppsSelectorBinding
import me.ycdev.android.lib.common.apps.AppInfo.AppNameComparator
import me.ycdev.android.lib.common.wrapper.IntentHelper
import timber.log.Timber
import java.util.ArrayList

class AppsSelectorActivity : AppCompatBaseActivity(), SelectedAppsChangeListener, OnClickListener {
    private lateinit var binding: ActAppsSelectorBinding

    private var excludeUninstalled = false
    private var excludeDisabled = false
    private var excludeSystem = false

    private lateinit var listAdapter: AppsSelectorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActAppsSelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val multiChoice = IntentHelper.getBooleanExtra(
            intent,
            EXTRA_MULTIPLE_CHOICE,
            DEFAULT_MULTIPLE_CHOICE
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

        listAdapter = AppsSelectorAdapter(this, multiChoice)
        binding.list.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(this@AppsSelectorActivity)
            addItemDecoration(
                DividerItemDecoration(this@AppsSelectorActivity, DividerItemDecoration.VERTICAL)
            )
        }
        binding.select.setOnClickListener(this)
        onSelectedAppsChanged(0)

        val viewModelFactory = AppsSelectorViewModel.Factory(
            application,
            excludeUninstalled,
            excludeDisabled,
            excludeSystem
        )
        val viewModel = ViewModelProvider(this, viewModelFactory)
            .get(AppsSelectorViewModel::class.java)
        viewModel.apps.observe(this, Observer {
            Timber.tag(TAG).d("apps loaded: ${it.size}")
            listAdapter.data = it
            listAdapter.sort(AppNameComparator())
            binding.progress.visibility = View.GONE
        })
    }

    override fun onSelectedAppsChanged(newCount: Int) {
        if (newCount == 0) {
            binding.status.setText(R.string.apps_selector_status_no_apps_selected)
        } else if (newCount == 1) {
            val status = getString(
                R.string.apps_selector_status_one_app_selected,
                listAdapter.oneSelectedApp?.pkgName
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
        val apps = listAdapter.selectedApps
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        private const val TAG = "AppsSelectorActivity"

        /** Type: boolean, default value: {@value #DEFAULT_MULTICHOICE}  */
        const val EXTRA_MULTIPLE_CHOICE = "extra.multiple_choice"
        /** Type: boolean, default value: {@value #DEFAULT_EXCLUDE_UNINSTALLED} */
        const val EXTRA_EXCLUDE_UNINSTALLED = "extra.exclude_uninstalled"
        /** Type: boolean, default value: {@value #DEFAULT_EXCLUDE_DISABLED  */
        const val EXTRA_EXCLUDE_DISABLED = "extra.exclude_disabled"
        /** Type: boolean, default value: {@value #DEFAULT_EXCLUDE_SYSTEM}  */
        const val EXTRA_EXCLUDE_SYSTEM = "extra.exclude_system"
        /** Type: ArrayList<String> </String> */
        const val RESULT_EXTRA_APPS_PKG_NAMES = "extra.pkg_names"

        private const val DEFAULT_MULTIPLE_CHOICE = false
        private const val DEFAULT_EXCLUDE_UNINSTALLED = true
        private const val DEFAULT_EXCLUDE_DISABLED = true
        private const val DEFAULT_EXCLUDE_SYSTEM = false
    }
}
