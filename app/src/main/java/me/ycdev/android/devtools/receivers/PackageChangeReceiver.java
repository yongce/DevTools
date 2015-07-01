package me.ycdev.android.devtools.receivers;

import java.net.URI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.ycdev.android.arch.utils.AppLogger;

public class PackageChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "PackageChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String pkgName = URI.create(intent.getDataString()).getSchemeSpecificPart();
        int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
        AppLogger.i(TAG, "Received: " + intent.getAction()
                + ", pkgName: " + pkgName + ", uid: " + uid + ", replacing: " + replacing);
    }

}
