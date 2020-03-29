package me.ycdev.android.devtools.security.unmarshall

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Handler.Callback
import android.os.Message
import android.view.View
import android.view.View.OnClickListener
import me.ycdev.android.arch.activity.AppCompatBaseActivity
import me.ycdev.android.devtools.CommonIntentService
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.apps.selector.AppsSelectorActivity
import me.ycdev.android.devtools.databinding.ActUnmarshallScannerBinding
import me.ycdev.android.lib.common.utils.WeakHandler
import me.ycdev.android.lib.common.wrapper.IntentHelper
import timber.log.Timber

class UnmarshallScannerActivity : AppCompatBaseActivity(), OnClickListener, Callback {
    private lateinit var binding: ActUnmarshallScannerBinding

    private var mTargetPkgName: String? = null
    private val mHandler: Handler = WeakHandler(this)

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_CHECK_DONE -> {
                if (scanTask == null || !scanTask!!.taskRunning) {
                    binding.testAll.setText(R.string.security_scanner_unmarshall_scan_all)
                    scanTask = null
                } else {
                    mHandler.sendEmptyMessageDelayed(MSG_CHECK_DONE, 3000)
                }
                return true
            }
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActUnmarshallScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Timber.tag(TAG).i("onCreate()")

        binding.appSelect.setOnClickListener(this)
        binding.testAll.setOnClickListener(this)
        if (scanTask != null) {
            updateSelectedApp(scanTask!!.targetAppPkgName)
            binding.testAll.setText(R.string.security_scanner_unmarshall_stop_scan)
            binding.optionReceiver.isChecked = scanTask!!.scanReceiver
            binding.optionService.isChecked = scanTask!!.scanService
            binding.optionActivity.isChecked = scanTask!!.scanActivity
            binding.optionNeedkill.isChecked = scanTask!!.needKill
            mHandler.sendEmptyMessageDelayed(MSG_CHECK_DONE, 3000)
        } else {
            updateSelectedApp(null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.tag(TAG).i("onDestroy()")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.tag(TAG).i("onActivityResult, requestCode: $requestCode, resultCode: $resultCode")
        if (requestCode == REQUEST_CODE_APP_SELECTOR) {
            if (resultCode == Activity.RESULT_OK) {
                val pkgNames = IntentHelper.getStringArrayListExtra(
                    data,
                    AppsSelectorActivity.RESULT_EXTRA_APPS_PKG_NAMES
                )
                Timber.tag(TAG).i("selected app: $pkgNames")
                if (pkgNames != null && pkgNames.size > 0) {
                    updateSelectedApp(pkgNames[0])
                }
            }
        }
    }

    private fun updateSelectedApp(pkgName: String?) {
        if (pkgName != null) {
            mTargetPkgName = pkgName
            binding.testAll.isEnabled = true
        } else {
            binding.testAll.isEnabled = false
        }
        val appSelected = getString(
            R.string.security_scanner_unmarshall_app_selected_state,
            if (mTargetPkgName != null) mTargetPkgName else ""
        )
        binding.appSelectedState.text = appSelected
    }

    override fun onClick(v: View) {
        if (v === binding.appSelect) {
            val intent = Intent(this, AppsSelectorActivity::class.java)
            startActivityForResult(
                intent,
                REQUEST_CODE_APP_SELECTOR
            )
        } else if (v === binding.testAll) {
            if (scanTask != null) {
                scanTask!!.scanController!!.cancel()
            } else {
                binding.testAll.setText(R.string.security_scanner_unmarshall_stop_scan)
                scanAll()
            }
        }
    }

    private fun scanAll() {
        val task = ScanTask()
        task.taskRunning = true
        task.targetAppPkgName = mTargetPkgName
        task.needKill = binding.optionNeedkill.isChecked
        task.scanReceiver = binding.optionReceiver.isChecked
        task.scanService = binding.optionService.isChecked
        task.scanActivity = binding.optionActivity.isChecked
        task.scanController = MyScanController(mTargetPkgName!!)
        scanTask = task
        val intent = Intent(this, CommonIntentService::class.java)
        intent.action = CommonIntentService.ACTION_UNMARSHALL_SCANNER
        startService(intent)
        mHandler.sendEmptyMessageDelayed(MSG_CHECK_DONE, 3000)
    }

    private class MyScanController(targetPkgName: String) : IScanController {
        private var _needKillApp: Boolean = false
        private var _isCanceled: Boolean = false

        override val targetPackageName: String = targetPkgName
        override val needKillApp: Boolean get() = _needKillApp
        override val isCanceled: Boolean get() = _isCanceled

        fun setNeedKillApp(needKillApp: Boolean) {
            _needKillApp = needKillApp
        }

        fun cancel() {
            _isCanceled = true
        }
    }

    private class ScanTask {
        var taskRunning = false
        var targetAppPkgName: String? = null
        var needKill = false
        var scanReceiver = false
        var scanService = false
        var scanActivity = false
        var scanController: MyScanController? = null
    }

    companion object {
        private const val TAG = "UnmarshallScanner"

        private const val MSG_CHECK_DONE = 1

        private const val REQUEST_CODE_APP_SELECTOR = 1

        private var scanTask: ScanTask? = null

        fun scanUnmarshallIssue(cxt: Context) {
            val task = scanTask
            if (task == null) {
                Timber.tag(TAG).w("Cannot scan unmarshall issues, no task")
                return
            }

            if (task.needKill) {
                task.scanController!!.setNeedKillApp(true)
                if (task.scanReceiver) {
                    IntentUnmarshallScanner.scanAllReceivers(cxt, task.scanController!!)
                }
                if (task.scanService) {
                    IntentUnmarshallScanner.scanAllServices(cxt, task.scanController!!)
                }
            }
            task.scanController!!.setNeedKillApp(false)
            if (task.scanReceiver) {
                IntentUnmarshallScanner.scanAllReceivers(cxt, task.scanController!!)
            }
            if (task.scanService) {
                IntentUnmarshallScanner.scanAllServices(cxt, task.scanController!!)
            }
            if (task.needKill) {
                task.scanController!!.setNeedKillApp(true)
                if (task.scanActivity) {
                    IntentUnmarshallScanner.scanAllActivities(cxt, task.scanController!!)
                }
            }
            task.scanController!!.setNeedKillApp(false)
            if (task.scanActivity) {
                IntentUnmarshallScanner.scanAllActivities(cxt, task.scanController!!)
            }
            task.taskRunning = false
        }
    }
}
