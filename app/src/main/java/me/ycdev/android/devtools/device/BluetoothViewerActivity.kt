package me.ycdev.android.devtools.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.View.OnClickListener
import me.ycdev.android.arch.activity.AppCompatBaseActivity
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.databinding.ActBluetoothViewerBinding
import me.ycdev.android.lib.common.perms.PermissionCallback
import me.ycdev.android.lib.common.perms.PermissionRequestParams
import me.ycdev.android.lib.common.perms.PermissionUtils
import me.ycdev.android.lib.common.wrapper.BroadcastHelper.registerForExternal
import me.ycdev.android.lib.common.wrapper.IntentHelper.getIntExtra
import me.ycdev.android.lib.common.wrapper.IntentHelper.getStringExtra
import timber.log.Timber

class BluetoothViewerActivity :
    AppCompatBaseActivity(),
    OnClickListener,
    PermissionCallback {
    private lateinit var binding: ActBluetoothViewerBinding

    private var logContents = ""
    private var bluetoothReceiverRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActBluetoothViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        startBluetoothViewer()
    }

    private fun initViews() {
        binding.enable.setOnClickListener(this)
        binding.disable.setOnClickListener(this)
        binding.disable.setText(disableActionTextResId(Build.VERSION.SDK_INT))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBluetoothReceiverIfNeeded()
    }

    private val receiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent,
            ) {
                val action = intent.action
                if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
                    val preState =
                        getIntExtra(
                            intent,
                            BluetoothAdapter.EXTRA_PREVIOUS_STATE,
                            STATE_UNKNOWN,
                        )
                    val newState =
                        getIntExtra(
                            intent,
                            BluetoothAdapter.EXTRA_STATE,
                            STATE_UNKNOWN,
                        )
                    addStateChangeLog(preState, newState)
                } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED == action) {
                    val preState =
                        getIntExtra(
                            intent,
                            BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE,
                            STATE_UNKNOWN,
                        )
                    val newState =
                        getIntExtra(
                            intent,
                            BluetoothAdapter.EXTRA_CONNECTION_STATE,
                            STATE_UNKNOWN,
                        )
                    val remoteDevice =
                        getStringExtra(
                            intent,
                            BluetoothDevice.EXTRA_DEVICE,
                        )
                    addConnectionChangeLog(preState, newState, remoteDevice)
                }
            }
        }

    private fun registerBluetoothReceiver() {
        if (bluetoothReceiverRegistered) {
            return
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        registerForExternal(this, receiver, intentFilter)
        bluetoothReceiverRegistered = true
    }

    private fun unregisterBluetoothReceiverIfNeeded() {
        if (bluetoothReceiverRegistered) {
            unregisterReceiver(receiver)
            bluetoothReceiverRegistered = false
        }
    }

    private fun addStateChangeLog(
        preState: Int,
        newState: Int,
    ) {
        val log =
            "State changed: " + getStateString(preState) + " -> " + getStateString(newState)
        appendNewLog(log)
    }

    private fun addConnectionChangeLog(
        preState: Int,
        newState: Int,
        remoteDevice: String?,
    ) {
        val log =
            (
                "Connection changed: " + getStateString(preState) + " -> " + getStateString(newState) +
                    ", remoteDevice: " + remoteDevice
            )
        appendNewLog(log)
    }

    private fun appendNewLog(log: String) {
        Timber.tag(TAG).i(log)
        logContents = log + "\n" + logContents
        binding.logViewer.text = logContents
    }

    private fun getStateString(state: Int): String {
        if (state == BluetoothAdapter.STATE_ON) {
            return "on"
        } else if (state == BluetoothAdapter.STATE_OFF) {
            return "off"
        } else if (state == BluetoothAdapter.STATE_CONNECTED) {
            return "connected"
        } else if (state == BluetoothAdapter.STATE_CONNECTING) {
            return "connecting"
        } else if (state == BluetoothAdapter.STATE_DISCONNECTED) {
            return "disconnected"
        } else if (state == BluetoothAdapter.STATE_DISCONNECTING) {
            return "disconnecting"
        } else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
            return "turning_off"
        } else if (state == BluetoothAdapter.STATE_TURNING_ON) {
            return "turning_on"
        }
        return "unknown-($state)"
    }

    private val bluetoothAdapter: BluetoothAdapter?
        get() {
            val mgr = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            return mgr.adapter
        }

    private val bluetoothState: Int
        get() {
            val adapter = bluetoothAdapter
            return adapter?.state ?: STATE_UNKNOWN
        }

    private fun startBluetoothViewer() {
        if (!ensureBluetoothConnectPermission()) {
            return
        }
        appendNewLog("Cur state: " + getStateString(bluetoothState))
        registerBluetoothReceiver()
    }

    private fun ensureBluetoothConnectPermission(): Boolean {
        if (!requiresBluetoothConnectPermission(Build.VERSION.SDK_INT)) {
            return true
        }
        if (PermissionUtils.hasPermissions(this, Manifest.permission.BLUETOOTH_CONNECT)) {
            return true
        }
        PermissionUtils.requestPermissions(this, createPermissionRequestParams())
        return false
    }

    @SuppressLint("InlinedApi")
    private fun createPermissionRequestParams(): PermissionRequestParams {
        val params = PermissionRequestParams()
        params.requestCode = PERMISSION_RC_BLUETOOTH_CONNECT
        params.permissions = arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
        params.rationaleTitle = getString(R.string.title_permission_request)
        params.rationaleContent = getString(R.string.module_bluetooth_viewer_desc)
        params.callback = this
        return params
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_RC_BLUETOOTH_CONNECT &&
            PermissionUtils.verifyPermissions(grantResults)
        ) {
            startBluetoothViewer()
        }
    }

    override fun onRationaleDenied(requestCode: Int) { // ignore
    }

    @SuppressLint("MissingPermission")
    private fun enableBluetooth() {
        if (!ensureBluetoothConnectPermission()) {
            return
        }
        val adapter = bluetoothAdapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } else {
            @Suppress("DEPRECATION")
            adapter?.enable()
        }
    }

    @SuppressLint("MissingPermission")
    private fun disableBluetooth() {
        if (!ensureBluetoothConnectPermission()) {
            return
        }
        val adapter = bluetoothAdapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        } else {
            @Suppress("DEPRECATION")
            adapter?.disable()
        }
    }

    override fun onClick(v: View) {
        if (v === binding.enable) {
            enableBluetooth()
        } else if (v === binding.disable) {
            disableBluetooth()
        }
    }

    companion object {
        private const val TAG = "BluetoothViewerActivity"

        private const val PERMISSION_RC_BLUETOOTH_CONNECT = 1

        private const val STATE_UNKNOWN = -1

        internal fun requiresBluetoothConnectPermission(sdkInt: Int): Boolean = sdkInt >= Build.VERSION_CODES.S

        internal fun disableActionTextResId(sdkInt: Int): Int =
            if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
                R.string.action_open_bluetooth_settings
            } else {
                R.string.action_disable
            }
    }
}
