package me.ycdev.android.devtools.sampler

import android.os.Environment
import java.io.File
import me.ycdev.android.devtools.utils.AppConstants

object SamplerUtils {
    val samplerFolder: File
        get() {
            val sdRoot = Environment.getExternalStorageDirectory()
            return File(
                sdRoot,
                AppConstants.EXTERNAL_STORAGE_PATH_APPS_SAMPLER
            )
        }

    fun getFileForSampler(fileName: String, mkdir: Boolean): File {
        val appDir = samplerFolder
        if (mkdir) {
            appDir.mkdirs()
        }
        return File(appDir, fileName)
    }
}
