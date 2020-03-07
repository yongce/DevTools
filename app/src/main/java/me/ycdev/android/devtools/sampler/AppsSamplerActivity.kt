package me.ycdev.android.devtools.sampler

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Handler.Callback
import android.os.Message
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import me.ycdev.android.arch.activity.AppCompatBaseActivity
import me.ycdev.android.arch.wrapper.ToastHelper
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.apps.selector.AppsSelectorActivity
import me.ycdev.android.devtools.databinding.ActAppsSamplerBinding
import me.ycdev.android.lib.common.apps.AppInfo
import me.ycdev.android.lib.common.perms.PermissionCallback
import me.ycdev.android.lib.common.perms.PermissionRequestParams
import me.ycdev.android.lib.common.perms.PermissionUtils
import me.ycdev.android.lib.common.utils.DateTimeUtils
import me.ycdev.android.lib.common.utils.StorageUtils
import me.ycdev.android.lib.common.utils.WeakHandler
import me.ycdev.android.lib.common.wrapper.IntentHelper
import timber.log.Timber
import java.util.ArrayList

class AppsSamplerActivity : AppCompatBaseActivity(),
    OnClickListener, Callback, PermissionCallback {

    private lateinit var binding: ActAppsSamplerBinding

    private lateinit var adapter: AppsSelectedAdapter
    private var pkgNames: ArrayList<String> = ArrayList()
    private var interval = 5 // seconds
    private var period = 0 // minutes, forever by default
    private val handler: Handler = WeakHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActAppsSamplerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        val taskInfo: SampleTaskInfo? = AppsSamplerService.lastSampleTask
        if (taskInfo != null) {
            interval = taskInfo.sampleInterval
            period = taskInfo.samplePeriod
            pkgNames.addAll(taskInfo.pkgNames)
        }
        binding.start.setOnClickListener(this)
        binding.stop.setOnClickListener(this)
        binding.createReport.setOnClickListener(this)
        binding.interval.setText(interval.toString())
        binding.period.setText(period.toString())

        adapter = AppsSelectedAdapter(this)
        binding.list.adapter = adapter
        binding.appsSelect.setOnClickListener(this)
        if (pkgNames.size > 0) {
            updateSelectedApps(pkgNames)
        }

        refreshButtonsState(taskInfo != null && taskInfo.isSampling)
        refreshSamplingInfo()

        // refresh the UI later for killed
        handler.sendEmptyMessageDelayed(
            MSG_REFRESH_SAMPLE_STATUS,
            interval * 1000.toLong()
        )
    }

    private fun refreshButtonsState(isSampling: Boolean) {
        if (isSampling) {
            binding.stop.isEnabled = true
            binding.start.isEnabled = false
            binding.appsSelect.isEnabled = false
        } else {
            binding.start.isEnabled = true
            binding.appsSelect.isEnabled = true
            binding.stop.isEnabled = false
        }
    }

    private fun refreshSamplingInfo() {
        val taskInfo: SampleTaskInfo? = AppsSamplerService.lastSampleTask
        if (taskInfo != null && taskInfo.isSampling) {
            val status = getString(
                R.string.apps_sampler_sample_status,
                DateTimeUtils.getReadableTimeStamp(taskInfo.startTime),
                taskInfo.sampleClockTime / 1000,
                taskInfo.sampleCount
            )
            binding.sampleStatus.text = status
            handler.sendEmptyMessageDelayed(
                MSG_REFRESH_SAMPLE_STATUS,
                interval * 1000.toLong()
            )
        } else {
            refreshButtonsState(false)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.apps_sampler_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.clear) {
            AppsSamplerService.clearLogs(this)
            ToastHelper.show(
                this, R.string.apps_sampler_clear_logs_toast,
                Toast.LENGTH_SHORT
            )
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        when {
            v === binding.start -> {
                startSample()
            }
            v === binding.stop -> {
                AppsSamplerService.createSampleReport(this)
                AppsSamplerService.stopSampler(this)
                ToastHelper.show(
                    this, R.string.apps_sampler_stop_sampling_toast,
                    Toast.LENGTH_SHORT
                )
                refreshButtonsState(false)
            }
            v === binding.createReport -> {
                AppsSamplerService.createSampleReport(this)
                ToastHelper.show(
                    this, R.string.apps_sampler_create_report_toast,
                    Toast.LENGTH_SHORT
                )
            }
            v === binding.appsSelect -> {
                val intent = Intent(this, AppsSelectorActivity::class.java)
                intent.putExtra(AppsSelectorActivity.EXTRA_MULTICHOICE, true)
                startActivityForResult(intent, REQUEST_CODE_APPS_SELECTOR)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_APPS_SELECTOR) {
            if (resultCode == Activity.RESULT_OK) {
                val pkgNames = IntentHelper.getStringArrayListExtra(
                    data,
                    AppsSelectorActivity.RESULT_EXTRA_APPS_PKG_NAMES
                )
                updateSelectedApps(pkgNames!!)
            }
        }
    }

    private fun updateSelectedApps(pkgNames: ArrayList<String>) {
        this.pkgNames = pkgNames
        val appsList = ArrayList<AppInfo>(pkgNames.size)
        for (pkgName in pkgNames) {
            val item = AppInfo(pkgName)
            appsList.add(item)
        }
        adapter.setData(appsList)
    }

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == MSG_REFRESH_SAMPLE_STATUS) {
            refreshSamplingInfo()
            return true
        }
        return false
    }

    override fun onDestroy() {
        handler.removeMessages(MSG_REFRESH_SAMPLE_STATUS)
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionsGranted = PermissionUtils.verifyPermissions(grantResults)
        Timber.tag(TAG).d(
            "onRequestPermissionsResult: %s",
            permissionsGranted
        )
        if (permissionsGranted) {
            startSample()
        }
    }

    override fun onRationaleDenied(requestCode: Int) { // ignore
    }

    private fun createPermissionRequestParams(): PermissionRequestParams {
        val params = PermissionRequestParams()
        params.requestCode = PERMISSION_RC_SAMPLER
        params.permissions = REQUESTED_PERMISSIONS
        params.rationaleTitle = getString(R.string.title_permission_request)
        params.rationaleContent = getString(R.string.apps_sampler_permissions_rationale)
        params.callback = this
        return params
    }

    private fun startSample() {
        if (!StorageUtils.isExternalStorageAvailable()) {
            ToastHelper.show(this, R.string.tip_no_sdcard, Toast.LENGTH_SHORT)
            return
        }
        val intervalStr = binding.interval.text.toString()
        if (intervalStr.isEmpty()) {
            ToastHelper.show(
                this, R.string.apps_sampler_sample_interval_input_toast,
                Toast.LENGTH_SHORT
            )
            return
        }
        if (pkgNames.size == 0) {
            ToastHelper.show(
                this, R.string.apps_sampler_no_apps_toast,
                Toast.LENGTH_SHORT
            )
            return
        }
        if (!PermissionUtils.hasPermissions(this, *REQUESTED_PERMISSIONS)) {
            Timber.tag(TAG).d(
                "Need to request the permission"
            )
            PermissionUtils.requestPermissions(this, createPermissionRequestParams())
            return
        }

        val periodStr = binding.period.text.toString()
        if (periodStr.isNotEmpty()) {
            period = periodStr.toInt()
        }
        interval = intervalStr.toInt()
        AppsSamplerService.startSampler(this, pkgNames, interval, period)
        ToastHelper.show(
            this, R.string.apps_sampler_start_sampling_toast,
            Toast.LENGTH_SHORT
        )
        refreshButtonsState(true)
        handler.sendEmptyMessageDelayed(
            MSG_REFRESH_SAMPLE_STATUS,
            interval * 1000.toLong()
        )
    }

    companion object {
        private const val TAG = "AppsSamplerActivity"

        private const val REQUEST_CODE_APPS_SELECTOR = 1

        private const val PERMISSION_RC_SAMPLER = 1

        private const val MSG_REFRESH_SAMPLE_STATUS = 100

        private val REQUESTED_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
}