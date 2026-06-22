package me.ycdev.android.devtools.device

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import com.google.common.truth.Truth.assertThat
import me.ycdev.android.devtools.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBluetoothDevice

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class BluetoothViewerActivityTest {
    @Test
    fun requiresBluetoothConnectPermission_onlyOnAndroid12AndNewer() {
        assertThat(BluetoothViewerActivity.requiresBluetoothConnectPermission(Build.VERSION_CODES.R))
            .isFalse()
        assertThat(BluetoothViewerActivity.requiresBluetoothConnectPermission(Build.VERSION_CODES.S))
            .isTrue()
    }

    @Test
    fun disableActionTextResId_usesSettingsTextOnAndroid13AndNewer() {
        assertThat(BluetoothViewerActivity.disableActionTextResId(Build.VERSION_CODES.S_V2))
            .isEqualTo(R.string.action_disable)
        assertThat(BluetoothViewerActivity.disableActionTextResId(Build.VERSION_CODES.TIRAMISU))
            .isEqualTo(R.string.action_open_bluetooth_settings)
    }

    @Test
    fun remoteDeviceLogName_readsBluetoothDeviceParcelableExtra() {
        @Suppress("DEPRECATION")
        val device = ShadowBluetoothDevice.newInstance("00:11:22:33:44:55")
        val intent = Intent().putExtra(BluetoothDevice.EXTRA_DEVICE, device)

        assertThat(BluetoothViewerActivity.remoteDeviceLogName(intent)).isEqualTo("00:11:22:33:44:55")
    }
}
