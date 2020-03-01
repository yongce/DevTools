package me.ycdev.android.devtools.receivers;

import java.net.URI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.ycdev.android.lib.common.wrapper.IntentHelper;
import timber.log.Timber;

public class PackageChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "PackageChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String pkgName = URI.create(intent.getDataString()).getSchemeSpecificPart();
        int uid = IntentHelper.INSTANCE.getIntExtra(intent, Intent.EXTRA_UID, -1);
        boolean replacing = IntentHelper.INSTANCE.getBooleanExtra(intent, Intent.EXTRA_REPLACING, false);
        Timber.tag(TAG).i("Received: " + intent.getAction()
                + ", pkgName: " + pkgName + ", uid: " + uid + ", replacing: " + replacing);
    }

}
