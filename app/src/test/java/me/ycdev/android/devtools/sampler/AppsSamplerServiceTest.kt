package me.ycdev.android.devtools.sampler

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.justRun
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class AppsSamplerServiceTest {
    @After
    fun tearDown() {
        unmockkStatic(ContextCompat::class)
    }

    @Test
    fun startSampler_usesForegroundServiceStart() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intentSlot = slot<Intent>()
        mockkStatic(ContextCompat::class)
        justRun { ContextCompat.startForegroundService(context, capture(intentSlot)) }

        AppsSamplerService.startSampler(
            context,
            arrayListOf("me.ycdev.android.devtools"),
            intervalSeconds = 5,
            periodMinutes = 1,
        )

        verify(exactly = 1) { ContextCompat.startForegroundService(context, any()) }
        assertThat(intentSlot.captured.component?.className)
            .isEqualTo(AppsSamplerService::class.java.name)
    }

    @Test
    fun notificationPendingIntentFlags_isImmutable() {
        val flags = AppsSamplerService.notificationPendingIntentFlags()

        assertThat(flags and PendingIntent.FLAG_UPDATE_CURRENT).isNotEqualTo(0)
        assertThat(flags and PendingIntent.FLAG_IMMUTABLE).isNotEqualTo(0)
    }

    @Test
    fun statsFilePackageName_removesTextFileExtension() {
        val pkgName = AppsSamplerService.statsFilePackageName("2026-06-18-stats-com.example.app.txt")

        assertThat(pkgName).isEqualTo("com.example.app")
    }
}
