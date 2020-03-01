package me.ycdev.android.devtools.common;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import me.ycdev.android.lib.common.net.NetworkUtils;
import timber.log.Timber;

public class DebugService extends Service {
    private static final String TAG = "DebugService";

    private static final String ACTION_CMD_DUMP_NETWORK_INFO = "action.CMD_DUMP_NETWORK_INFO";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Timber.tag(TAG).d("onStartCommand: %s", intent);
        if (intent != null) {
            processRequest(intent);
        }
        return START_NOT_STICKY;
    }

    private void processRequest(Intent intent) {
        String action = intent.getAction();
        if (ACTION_CMD_DUMP_NETWORK_INFO.equals(action)) {
            Timber.tag(TAG).d("Dump of active network info:\n%s",
                    NetworkUtils.INSTANCE.dumpActiveNetworkInfo(this));
        } else {
            Timber.tag(TAG).w("unknown cmd: %s", action);
        }
    }
}
