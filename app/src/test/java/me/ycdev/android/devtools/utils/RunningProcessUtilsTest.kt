package me.ycdev.android.devtools.utils

import android.app.ActivityManager
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RunningProcessUtilsTest {
    @Test
    fun validProcesses_returnsEmptyListForNullInput() {
        assertThat(RunningProcessUtils.validProcesses(null)).isEmpty()
    }

    @Test
    fun validProcesses_skipsProcessesWithoutPackages() {
        val process = ActivityManager.RunningAppProcessInfo()
        process.pkgList = emptyArray()

        assertThat(RunningProcessUtils.validProcesses(listOf(process))).isEmpty()
    }

    @Test
    fun primaryPackageName_returnsFirstPackageName() {
        val process = ActivityManager.RunningAppProcessInfo()
        process.pkgList = arrayOf("com.example.first", "com.example.second")

        assertThat(RunningProcessUtils.primaryPackageName(process)).isEqualTo("com.example.first")
    }
}
