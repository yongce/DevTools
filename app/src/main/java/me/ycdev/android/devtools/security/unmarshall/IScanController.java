package me.ycdev.android.devtools.security.unmarshall;

public interface IScanController {
    String getTargetPackageName();
    boolean needKillApp();
    boolean isCanceled();
}
