package me.ycdev.android.devtools.sampler

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class AppStatTest {
    @Test
    fun newAppStat_initializesEmptyStats() {
        val appStat = AppStat("com.example.app", 10001)

        assertThat(appStat.cpuStat.total).isEqualTo(0)
        assertThat(appStat.memStat.totalPss).isEqualTo(0)
        assertThat(appStat.memStat.totalPrivate).isEqualTo(0)
        assertThat(appStat.trafficStat.total).isEqualTo(0)
    }
}
