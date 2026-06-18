package me.ycdev.android.devtools.sampler

import android.content.Context
import me.ycdev.android.devtools.utils.AppConstants
import java.io.File

object SamplerUtils {
    fun getSamplerFolder(context: Context): File {
        val appRoot = context.getExternalFilesDir(null) ?: context.filesDir
        return File(
            appRoot,
            AppConstants.EXTERNAL_STORAGE_PATH_APPS_SAMPLER,
        )
    }

    fun getFileForSampler(
        context: Context,
        fileName: String,
        mkdir: Boolean,
    ): File {
        val appDir = getSamplerFolder(context)
        if (mkdir) {
            appDir.mkdirs()
        }
        return File(appDir, fileName)
    }
}
