package me.ycdev.android.devtools

import android.app.Service
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CommonIntentServiceTest {
    @Test
    fun serviceDoesNotExtendDeprecatedIntentService() {
        assertThat(CommonIntentService::class.java.superclass)
            .isEqualTo(Service::class.java)
    }
}
