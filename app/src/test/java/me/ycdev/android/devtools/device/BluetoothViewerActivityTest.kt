package me.ycdev.android.devtools.device

import android.os.Build
import com.google.common.truth.Truth.assertThat
import me.ycdev.android.devtools.R
import org.junit.Test

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
}
