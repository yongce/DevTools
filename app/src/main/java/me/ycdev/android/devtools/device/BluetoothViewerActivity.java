package me.ycdev.android.devtools.device;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import me.ycdev.android.arch.activity.AppCompatBaseActivity;
import me.ycdev.android.arch.utils.AppLogger;
import me.ycdev.android.devtools.R;

public class BluetoothViewerActivity extends AppCompatBaseActivity implements View.OnClickListener {
    private static final String TAG = "BluetoothViewerActivity";

    private static final int STATE_UNKNOWN = -1;

    private TextView mLoggerView;
    private Button mEnableBtn;
    private Button mDisableBtn;

    private String mLogContents = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_bluetooth_viewer);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initViews();

        registerBluetoothReceiver();
    }

    private void initViews() {
        mLoggerView = (TextView) findViewById(R.id.log_viewer);
        appendNewLog("Cur state: " + getStateString(getBluetoothState()));

        mEnableBtn = (Button) findViewById(R.id.enable);
        mEnableBtn.setOnClickListener(this);
        mDisableBtn = (Button) findViewById(R.id.disable);
        mDisableBtn.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBluetoothReceiver();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int preState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, STATE_UNKNOWN);
                int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, STATE_UNKNOWN);
                addStateChangeLog(preState, newState);
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                @SuppressLint("InlinedApi")
                int preState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE, STATE_UNKNOWN);
                @SuppressLint("InlinedApi")
                int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, STATE_UNKNOWN);
                String remoteDevice = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                addConnectionChangeLog(preState, newState, remoteDevice);
            }
        }
    };

    @SuppressLint("InlinedApi")
    private void registerBluetoothReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mReceiver, intentFilter);
    }

    private void unregisterBluetoothReceiver() {
        unregisterReceiver(mReceiver);
    }

    private void addStateChangeLog(int preState, int newState) {
        String log = "State changed: " + getStateString(preState) + " -> " + getStateString(newState);
        appendNewLog(log);
    }

    private void addConnectionChangeLog(int preState, int newState, String remoteDevice) {
        String log = "Connection changed: " + getStateString(preState) + " -> " + getStateString(newState)
                + ", remoteDevice: " + remoteDevice;
        appendNewLog(log);
    }

    private void appendNewLog(String log) {
        AppLogger.i(TAG, log);
        mLogContents = log + "\n" + mLogContents;
        mLoggerView.setText(mLogContents);
    }

    private String getStateString(int state) {
        if (state == BluetoothAdapter.STATE_ON) {
            return "on";
        } else if (state == BluetoothAdapter.STATE_OFF) {
            return "off";
        } else if (state == BluetoothAdapter.STATE_CONNECTED) {
            return "connected";
        } else if (state == BluetoothAdapter.STATE_CONNECTING) {
            return "connecting";
        } else if (state == BluetoothAdapter.STATE_DISCONNECTED) {
            return "disconnected";
        } else if (state == BluetoothAdapter.STATE_DISCONNECTING) {
            return "disconnecting";
        } else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
            return "turning_off";
        } else if (state == BluetoothAdapter.STATE_TURNING_ON) {
            return "turning_on";
        }
        return "unknonw(" + state + ")";
    }

    @Nullable
    private BluetoothAdapter getBluetoothAdapter() {
        if (Build.VERSION.SDK_INT >= 18) {
            BluetoothManager mgr = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            return mgr.getAdapter();
        }
        return BluetoothAdapter.getDefaultAdapter();
    }

    private int getBluetoothState() {
        BluetoothAdapter adapter = getBluetoothAdapter();
        if (adapter != null) {
            return adapter.getState();
        }
        return STATE_UNKNOWN;
    }

    private void enableBluetooth() {
        BluetoothAdapter adapter = getBluetoothAdapter();
        if (adapter != null) {
            adapter.enable();
        }
    }

    private void disableBluetooth() {
        BluetoothAdapter adapter = getBluetoothAdapter();
        if (adapter != null) {
            adapter.disable();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mEnableBtn) {
            enableBluetooth();
        } else if (v == mDisableBtn) {
            disableBluetooth();
        }
    }
}
