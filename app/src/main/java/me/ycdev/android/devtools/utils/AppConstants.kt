package me.ycdev.android.devtools.utils

object AppConstants {
    // Notification channels
    const val NOTIFICATION_CHANNEL_SAMPLER = "channel_sampler"

    // Notification IDs
    const val NOTIFICATION_ID_PROC_MEM_SAMPLER = 1

    const val EXTERNAL_STORAGE_PATH_APP_ROOT = "yc/devtools"
    const val EXTERNAL_STORAGE_PATH_APPS_SAMPLER = "$EXTERNAL_STORAGE_PATH_APP_ROOT/sampler"

    private const val ACTION_PREFIX = "me.ycdev.android.devtools.action."
    const val ACTION_DYNAMIC_BROADCAST_TEST = ACTION_PREFIX + "DYNAMIC_BROADCAST_TEST"
}