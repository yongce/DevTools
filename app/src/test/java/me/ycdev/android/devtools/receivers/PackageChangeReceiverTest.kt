package me.ycdev.android.devtools.receivers

import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class PackageChangeReceiverTest {
    @Test
    fun packageChangeReceiver_isExportedForSystemPackageBroadcasts() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        @Suppress("DEPRECATION")
        val receiverInfo =
            context.packageManager.getReceiverInfo(
                ComponentName(context, PackageChangeReceiver::class.java),
                0,
            )

        assertThat(receiverInfo.exported).isTrue()
    }
}
