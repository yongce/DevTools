package me.ycdev.android.devtools.security.unmarshall;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.apps.selector.AppsSelectorActivity;
import me.ycdev.android.devtools.utils.AppLogger;
import me.ycdev.android.lib.common.utils.WeakHandler;

public class UnmarshallScannerActivity extends AppCompatActivity
        implements View.OnClickListener, WeakHandler.MessageHandler {
    private static final String TAG = "UnmarshallScannerActivity";

    private static final int MSG_CHECK_DONE = 1;

    private static final int REQUEST_CODE_APP_SELECTOR = 1;

    private TextView mAppSelectedStateView;
    private Button mAppSelectBtn;
    private Button mScanAllBtn;

    private String mTargetPkgName;
    private boolean mScanRunning;
    private Handler mHandler = new WeakHandler(this);
    private MyScanController mScanController = new MyScanController();

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_CHECK_DONE: {
                mScanRunning = false;
                mScanAllBtn.setText(R.string.security_scanner_unmarshall_scan_all);
                break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_unmarshall_scanner);
        AppLogger.i(TAG, "onCreate()");

        mAppSelectedStateView = (TextView) findViewById(R.id.app_selected_state);
        mAppSelectBtn = (Button) findViewById(R.id.app_select);
        mAppSelectBtn.setOnClickListener(this);
        mScanAllBtn = (Button) findViewById(R.id.test_all);
        mScanAllBtn.setOnClickListener(this);

        updateSelectedApp(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppLogger.i(TAG, "onDestroy()");
        mScanController.cancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_APP_SELECTOR) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> pkgNames = data.getStringArrayListExtra(
                        AppsSelectorActivity.RESULT_EXTRA_APPS_PKG_NAMES);
                if (pkgNames.size() > 0) {
                    updateSelectedApp(pkgNames);
                }
            }
        }
    }

    private void updateSelectedApp(ArrayList<String> pkgNames) {
        if (pkgNames != null && pkgNames.size() > 0) {
            mTargetPkgName = pkgNames.get(0);
            mScanAllBtn.setEnabled(true);
        } else {
            mScanAllBtn.setEnabled(false);
        }

        String appSelected = getString(R.string.security_scanner_unmarshall_app_selected_state,
                (mTargetPkgName != null ? mTargetPkgName : ""));
        mAppSelectedStateView.setText(appSelected);
    }

    @Override
    public void onClick(View v) {
        if (v == mAppSelectBtn) {
            Intent intent = new Intent(this, AppsSelectorActivity.class);
            startActivityForResult(intent, REQUEST_CODE_APP_SELECTOR);
        } else if (v == mScanAllBtn) {
            if (mScanRunning) {
                mScanController.cancel();
            } else {
                mScanRunning = true;
                mScanController.reset();
                mScanController.setTargetPackageName(mTargetPkgName);
                mScanAllBtn.setText(R.string.security_scanner_unmarshall_stop_scan);
                testAll();
            }
        }
    }

    private void testAll() {
        final Context appContext = getApplicationContext();
        new Thread() {
            @Override
            public void run() {
                mScanController.setNeedKillApp(true);
                IntentUnmarshallScanner.scanAllReceivers(appContext, mScanController);
                IntentUnmarshallScanner.scanAllServices(appContext, mScanController);
                mScanController.setNeedKillApp(false);
                IntentUnmarshallScanner.scanAllReceivers(appContext, mScanController);
                IntentUnmarshallScanner.scanAllServices(appContext, mScanController);
                mScanController.setNeedKillApp(true);
                IntentUnmarshallScanner.scanAllActivities(appContext, mScanController);
                mScanController.setNeedKillApp(false);
                IntentUnmarshallScanner.scanAllActivities(appContext, mScanController);
                mHandler.sendEmptyMessage(MSG_CHECK_DONE);
            }
        }.start();
    }

    private static class MyScanController implements IScanController {
        private String mTargetAppPkgName;
        private boolean mNeedKillApp;
        private boolean mIsCanceled = false;

        public void setTargetPackageName(String pkgName) {
            mTargetAppPkgName = pkgName;
        }

        public void setNeedKillApp(boolean needKillApp) {
            mNeedKillApp = needKillApp;
        }

        public void reset() {
            mIsCanceled = false;
        }

        public void cancel() {
            mIsCanceled = true;
        }

        @Override
        public String getTargetPackageName() {
            return mTargetAppPkgName;
        }

        @Override
        public boolean needKillApp() {
            return mNeedKillApp;
        }

        @Override
        public boolean isCanceled() {
            return mIsCanceled;
        }
    }
}
