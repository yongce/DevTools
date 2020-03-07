package me.ycdev.android.devtools.security.unmarshall

interface IScanController {
    val targetPackageName: String
    val needKillApp: Boolean
    val isCanceled: Boolean
}