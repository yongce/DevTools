package me.ycdev.android.devtools.utils;

public class Constants {
    // Notification IDs
    public static final int NOTIFICATION_ID_PROC_MEM_SAMPLER = 1;

    public static final String EXTERNAL_STORAGE_PATH_APP_ROOT = "yc/devtools";
    public static final String EXTERNAL_STORAGE_PATH_APPS_SAMPLER = EXTERNAL_STORAGE_PATH_APP_ROOT + "/sampler";

    private static final String PERM_PREFIX = "me.ycdev.android.devtools.permission.";
    public static final String PERM_DYNAMIC_BROADCAST = PERM_PREFIX + "DYNAMIC_BROADCAST";

    private static final String ACTION_PREFIX = "me.ycdev.android.devtools.action.";
    public static final String ACTION_DYNAMIC_BROADCAST_TEST = ACTION_PREFIX + "DYNAMIC_BROADCAST_TEST";
}
