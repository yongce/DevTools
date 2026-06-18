package me.ycdev.android.devtools.sampler

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Test
import java.io.IOException

class SampleLoggerTest {
    @After
    fun tearDown() {
        unmockkObject(SamplerUtils)
    }

    @Test
    fun logInfo_doesNotThrowWhenLogFileCannotBeCreated() {
        val context = mockk<Context>(relaxed = true)
        mockkObject(SamplerUtils)
        every { SamplerUtils.getFileForSampler(context, "sampler.log", true) } throws IOException("disk unavailable")

        val result =
            runCatching {
                SampleLogger(context).use {
                    it.logInfo("tag", "message")
                }
            }

        assertThat(result.exceptionOrNull()).isNull()
    }
}
