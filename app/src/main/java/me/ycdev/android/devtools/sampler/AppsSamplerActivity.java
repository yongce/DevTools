package me.ycdev.android.devtools.sampler;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import me.ycdev.android.arch.wrapper.ToastHelper;
import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.apps.selector.AppsSelectorActivity;
import me.ycdev.android.lib.common.apps.AppInfo;
import me.ycdev.android.lib.common.utils.DateTimeUtils;
import me.ycdev.android.lib.common.utils.WeakHandler;

public class AppsSamplerActivity extends AppCompatActivity
        implements View.OnClickListener, WeakHandler.MessageHandler {
    private static final int REQUEST_CODE_APPS_SELECTOR = 1;

    private static final int MSG_REFRESH_SAMPLE_STATUS = 100;

    private Button mStartBtn;
    private Button mStopBtn;
    private Button mCreateReportBtn;
    private EditText mIntervalView;
    private EditText mPeriodView;
    private TextView mSampleStatusView;
    private ListView mListView;
    private Button mAppsSelectBtn;

    private AppsSelectedAdapter mAdapter;

    private ArrayList<String> mPkgNames = new ArrayList<String>();
    private int mInterval = 5; // seconds
    private int mPeriod = 0; // minutes, forever by default

    private Handler mHandler = new WeakHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_apps_sampler);

        initViews();
    }

    private void initViews() {
        SampleTaskInfo taskInfo = AppsSamplerService.getLastSampleTask();
        if (taskInfo != null) {
            mInterval = taskInfo.sampleInterval;
            mPeriod = taskInfo.samplePeriod;
            mPkgNames.addAll(taskInfo.pkgNames);
        }

        mStartBtn = (Button) findViewById(R.id.start);
        mStartBtn.setOnClickListener(this);
        mStopBtn = (Button) findViewById(R.id.stop);
        mStopBtn.setOnClickListener(this);
        mCreateReportBtn = (Button) findViewById(R.id.create_report);
        mCreateReportBtn.setOnClickListener(this);

        mIntervalView = (EditText) findViewById(R.id.interval);
        mIntervalView.setText(String.valueOf(mInterval));
        mPeriodView = (EditText) findViewById(R.id.period);
        mPeriodView.setText(String.valueOf(mPeriod));
        mSampleStatusView = (TextView) findViewById(R.id.sample_status);

        mListView = (ListView) findViewById(R.id.list);
        mAdapter = new AppsSelectedAdapter(this);
        mListView.setAdapter(mAdapter);
        mAppsSelectBtn = (Button) findViewById(R.id.apps_select);
        mAppsSelectBtn.setOnClickListener(this);

        if (mPkgNames.size() > 0) {
            updateSelectedApps(mPkgNames);
        }
        refreshButtonsState(taskInfo != null && taskInfo.isSampling);
        refreshSamplingInfo();
        // refresh the UI later for killed
        mHandler.sendEmptyMessageDelayed(MSG_REFRESH_SAMPLE_STATUS, mInterval * 1000);
    }

    private void refreshButtonsState(boolean isSampling) {
        if (isSampling) {
            mStopBtn.setEnabled(true);
            mStartBtn.setEnabled(false);
            mAppsSelectBtn.setEnabled(false);
        } else {
            mStartBtn.setEnabled(true);
            mAppsSelectBtn.setEnabled(true);
            mStopBtn.setEnabled(false);
        }
    }

    private void refreshSamplingInfo() {
        SampleTaskInfo taskInfo = AppsSamplerService.getLastSampleTask();
        if (taskInfo != null && taskInfo.isSampling) {
            String status = getString(R.string.apps_sampler_sample_status,
                    DateTimeUtils.getReadableTimeStamp(taskInfo.startTime),
                    taskInfo.sampleClockTime / 1000,
                    taskInfo.sampleCount);
            mSampleStatusView.setText(status);
            mHandler.sendEmptyMessageDelayed(MSG_REFRESH_SAMPLE_STATUS, mInterval * 1000);
        } else {
            refreshButtonsState(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.apps_sampler_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear) {
            AppsSamplerService.clearLogs(this);
            ToastHelper.show(this, R.string.apps_sampler_clear_logs_toast,
                    Toast.LENGTH_SHORT);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v == mStartBtn) {
            String intervalStr = mIntervalView.getText().toString();
            if (intervalStr.length() == 0) {
                ToastHelper.show(this, R.string.apps_sampler_sample_interval_input_toast,
                        Toast.LENGTH_SHORT);
                return;
            }
            if (mPkgNames.size() == 0) {
                ToastHelper.show(this, R.string.apps_sampler_no_apps_toast,
                        Toast.LENGTH_SHORT);
                return;
            }

            String periodStr = mPeriodView.getText().toString();
            if (periodStr.length() > 0) {
                mPeriod = Integer.parseInt(periodStr);
            }
            mInterval = Integer.parseInt(intervalStr);
            AppsSamplerService.startSampler(this, mPkgNames, mInterval, mPeriod);
            ToastHelper.show(this, R.string.apps_sampler_start_sampling_toast,
                    Toast.LENGTH_SHORT);
            refreshButtonsState(true);
            mHandler.sendEmptyMessageDelayed(MSG_REFRESH_SAMPLE_STATUS, mInterval * 1000);
        } else if (v == mStopBtn) {
            AppsSamplerService.createSampleReport(this);
            AppsSamplerService.stopSampler(this);
            ToastHelper.show(this, R.string.apps_sampler_stop_sampling_toast,
                    Toast.LENGTH_SHORT);
            refreshButtonsState(false);
        } else if (v == mCreateReportBtn) {
            AppsSamplerService.createSampleReport(this);
            ToastHelper.show(this, R.string.apps_sampler_create_report_toast,
                    Toast.LENGTH_SHORT);
        } else if (v == mAppsSelectBtn) {
            Intent intent = new Intent(this, AppsSelectorActivity.class);
            intent.putExtra(AppsSelectorActivity.EXTRA_MULTICHOICE, true);
            startActivityForResult(intent, REQUEST_CODE_APPS_SELECTOR);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_APPS_SELECTOR) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> pkgNames = data.getStringArrayListExtra(
                        AppsSelectorActivity.RESULT_EXTRA_APPS_PKG_NAMES);
                updateSelectedApps(pkgNames);
            }
        }
    }

    private void updateSelectedApps(ArrayList<String> pkgNames) {
        mPkgNames = pkgNames;
        ArrayList<AppInfo> appsList = new ArrayList<AppInfo>(pkgNames.size());
        for (String pkgName : pkgNames) {
            AppInfo item = new AppInfo();
            item.pkgName = pkgName;
            appsList.add(item);
        }
        mAdapter.setData(appsList);
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == MSG_REFRESH_SAMPLE_STATUS) {
            refreshSamplingInfo();
        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeMessages(MSG_REFRESH_SAMPLE_STATUS);
        super.onDestroy();
    }
}
