package me.ycdev.android.devtools;

import android.app.IntentService;
import android.content.Intent;

import me.ycdev.android.devtools.security.unmarshall.UnmarshallScannerActivity;

public class CommonIntentService extends IntentService {
    private static final String ACTION_PREFIX = "class.action.";
    public static final String ACTION_UNMARSHALL_SCANNER = ACTION_PREFIX + "UNMARSHALL_SCANNER";

    public CommonIntentService() {
        super("CommonIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (ACTION_UNMARSHALL_SCANNER.equals(action)) {
            UnmarshallScannerActivity.scanUnmarshallIssue(this);
        }
    }
}
