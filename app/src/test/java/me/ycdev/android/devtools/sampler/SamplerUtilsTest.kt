package me.ycdev.android.devtools.sampler

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import me.ycdev.android.devtools.utils.AppConstants
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SamplerUtilsTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun getSamplerFolder_usesAppSpecificExternalStorage() {
        val externalFilesDir = temporaryFolder.newFolder("external")
        val internalFilesDir = temporaryFolder.newFolder("internal")
        val context =
            mockk<Context> {
                every { getExternalFilesDir(null) } returns externalFilesDir
                every { filesDir } returns internalFilesDir
            }

        val samplerFolder = SamplerUtils.getSamplerFolder(context)

        assertThat(samplerFolder)
            .isEqualTo(File(externalFilesDir, AppConstants.EXTERNAL_STORAGE_PATH_APPS_SAMPLER))
    }

    @Test
    fun getSamplerFolder_fallsBackToInternalStorage() {
        val internalFilesDir = temporaryFolder.newFolder("internal")
        val context =
            mockk<Context> {
                every { getExternalFilesDir(null) } returns null
                every { filesDir } returns internalFilesDir
            }

        val samplerFolder = SamplerUtils.getSamplerFolder(context)

        assertThat(samplerFolder)
            .isEqualTo(File(internalFilesDir, AppConstants.EXTERNAL_STORAGE_PATH_APPS_SAMPLER))
    }
}
