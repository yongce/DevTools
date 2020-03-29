package me.ycdev.android.devtools.device

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import me.ycdev.android.arch.activity.AppCompatBaseActivity
import me.ycdev.android.devtools.databinding.ActBluetoothViewerBinding
import me.ycdev.android.lib.common.wrapper.BroadcastHelper.registerForExternal
import me.ycdev.android.lib.common.wrapper.IntentHelper.getIntExtra
import me.ycdev.android.lib.common.wrapper.IntentHelper.getStringExtra
import timber.log.Timber

class BluetoothViewerActivity : AppCompatBaseActivity(), OnClickListener {
    private lateinit var binding: ActBluetoothViewerBinding

    private var logContents = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActBluetoothViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        registerBluetoothReceiver()
    }

    private fun initViews() {
        appendNewLog("Cur state: " + getStateString(bluetoothState))
        binding.enable.setOnClickListener(this)
        binding.disable.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterBluetoothReceiver()
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
                val preState = getIntExtra(
                    intent,
                    BluetoothAdapter.EXTRA_PREVIOUS_STATE,
                    STATE_UNKNOWN
                )
                val newState = getIntExtra(
                    intent,
                    BluetoothAdapter.EXTRA_STATE, STATE_UNKNOWN
                )
                addStateChangeLog(preState, newState)
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED == action) {
                val preState = getIntExtra(
                    intent,
                    BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE,
                    STATE_UNKNOWN
                )
                val newState = getIntExtra(
                    intent,
                    BluetoothAdapter.EXTRA_CONNECTION_STATE,
                    STATE_UNKNOWN
                )
                val remoteDevice = getStringExtra(
                    intent,
                    BluetoothDevice.EXTRA_DEVICE
                )
                addConnectionChangeLog(preState, newState, remoteDevice)
            }
        }
    }

    private fun registerBluetoothReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        registerForExternal(this, receiver, intentFilter)
    }

    private fun unregisterBluetoothReceiver() {
        unregisterReceiver(receiver)
    }

    private fun addStateChangeLog(preState: Int, newState: Int) {
        val log =
            "State changed: " + getStateString(preState) + " -> " + getStateString(newState)
        appendNewLog(log)
    }

    private fun addConnectionChangeLog(
        preState: Int,
        newState: Int,
        remoteDevice: String?
    ) {
        val log =
            ("Connection changed: " + getStateString(preState) + " -> " + getStateString(newState) +
                    ", remoteDevice: " + remoteDevice)
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

    private fun enableBluetooth() {
        val adapter = bluetoothAdapter
        adapter?.enable()
    }

    private fun disableBluetooth() {
        val adapter = bluetoothAdapter
        adapter?.disable()
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

        private const val STATE_UNKNOWN = -1
    }
}
