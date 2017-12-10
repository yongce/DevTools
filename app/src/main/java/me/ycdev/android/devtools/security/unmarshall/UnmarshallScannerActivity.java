package me.ycdev.android.devtools.security.unmarshall;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import me.ycdev.android.arch.activity.AppCompatBaseActivity;
import me.ycdev.android.arch.utils.AppLogger;
import me.ycdev.android.devtools.CommonIntentService;
import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.apps.selector.AppsSelectorActivity;
import me.ycdev.android.lib.common.utils.WeakHandler;
import me.ycdev.android.lib.common.wrapper.IntentHelper;

public class UnmarshallScannerActivity extends AppCompatBaseActivity
        implements View.OnClickListener, WeakHandler.Callback {
    private static final String TAG = "UnmarshallScannerActivity";

    private static final int MSG_CHECK_DONE = 1;

    private static final int REQUEST_CODE_APP_SELECTOR = 1;

    private TextView mAppSelectedStateView;
    private Button mAppSelectBtn;
    private Button mScanAllBtn;
    private CheckBox mReceiverCheckBox;
    private CheckBox mServiceCheckBox;
    private CheckBox mActivityCheckBox;
    private CheckBox mNeedKillCheckBox;

    private String mTargetPkgName;
    private Handler mHandler = new WeakHandler(this);


    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_CHECK_DONE: {
                if (sScanTask == null || !sScanTask.taskRunning) {
                    mScanAllBtn.setText(R.string.security_scanner_unmarshall_scan_all);
                    sScanTask = null;
                } else {
                    mHandler.sendEmptyMessageDelayed(MSG_CHECK_DONE, 3000);
                }
                return true;
            }
        }
        return false;
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
        mReceiverCheckBox = (CheckBox) findViewById(R.id.option_receiver);
        mServiceCheckBox = (CheckBox) findViewById(R.id.option_service);
        mActivityCheckBox = (CheckBox) findViewById(R.id.option_activity);
        mNeedKillCheckBox = (CheckBox) findViewById(R.id.option_needkill);

        if (sScanTask != null) {
            updateSelectedApp(sScanTask.targetAppPkgName);
            mScanAllBtn.setText(R.string.security_scanner_unmarshall_stop_scan);
            mReceiverCheckBox.setChecked(sScanTask.scanReceiver);
            mServiceCheckBox.setChecked(sScanTask.scanService);
            mActivityCheckBox.setChecked(sScanTask.scanActivity);
            mNeedKillCheckBox.setChecked(sScanTask.needKill);
            mHandler.sendEmptyMessageDelayed(MSG_CHECK_DONE, 3000);
        } else {
            updateSelectedApp(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppLogger.i(TAG, "onDestroy()");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_APP_SELECTOR) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> pkgNames = IntentHelper.getStringArrayListExtra(data,
                        AppsSelectorActivity.RESULT_EXTRA_APPS_PKG_NAMES);
                if (pkgNames != null && pkgNames.size() > 0) {
                    updateSelectedApp(pkgNames.get(0));
                }
            }
        }
    }

    private void updateSelectedApp(String pkgName) {
        if (pkgName != null) {
            mTargetPkgName = pkgName;
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
            if (sScanTask != null) {
                sScanTask.scanController.cancel();
            } else {
                mScanAllBtn.setText(R.string.security_scanner_unmarshall_stop_scan);
                scanAll();
            }
        }
    }

    private void scanAll() {
        ScanTask task = new ScanTask();
        task.taskRunning = true;
        task.targetAppPkgName = mTargetPkgName;
        task.needKill = mNeedKillCheckBox.isChecked();
        task.scanReceiver = mReceiverCheckBox.isChecked();
        task.scanService = mServiceCheckBox.isChecked();
        task.scanActivity = mActivityCheckBox.isChecked();

        task.scanController = new MyScanController();
        task.scanController.setTargetPackageName(mTargetPkgName);

        sScanTask = task;
        Intent intent = new Intent(this, CommonIntentService.class);
        intent.setAction(CommonIntentService.ACTION_UNMARSHALL_SCANNER);
        startService(intent);

        mHandler.sendEmptyMessageDelayed(MSG_CHECK_DONE, 3000);
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

    private static class ScanTask {
        public boolean taskRunning;
        public String targetAppPkgName;
        public boolean needKill;
        public boolean scanReceiver;
        public boolean scanService;
        public boolean scanActivity;

        public MyScanController scanController;
    }

    private static ScanTask sScanTask;

    public static void scanUnmarshallIssue(Context cxt) {
        ScanTask task = sScanTask;
        if (task == null) {
            AppLogger.w(TAG, "Cannot scan unmarshall issues, no task");
            return;
        }

        if (task.needKill) {
            task.scanController.setNeedKillApp(true);
            if (task.scanReceiver) {
                IntentUnmarshallScanner.scanAllReceivers(cxt, task.scanController);
            }
            if (task.scanService) {
                IntentUnmarshallScanner.scanAllServices(cxt, task.scanController);
            }
        }
        task.scanController.setNeedKillApp(false);
        if (task.scanReceiver) {
            IntentUnmarshallScanner.scanAllReceivers(cxt, task.scanController);
        }
        if (task.scanService) {
            IntentUnmarshallScanner.scanAllServices(cxt, task.scanController);
        }

        if (task.needKill) {
            task.scanController.setNeedKillApp(true);
            if (task.scanActivity) {
                IntentUnmarshallScanner.scanAllActivities(cxt, task.scanController);
            }
        }
        task.scanController.setNeedKillApp(false);
        if (task.scanActivity) {
            IntentUnmarshallScanner.scanAllActivities(cxt, task.scanController);
        }

        task.taskRunning = false;
    }
}
