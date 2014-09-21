package me.ycdev.android.devtools.sampler;

import java.util.ArrayList;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.apps.common.AppInfo;
import me.ycdev.android.devtools.apps.selector.AppsSelectorActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class AppsSamplerActivity extends Activity implements View.OnClickListener {
    private static final int REQUEST_CODE_APPS_SELECTOR = 1;

    private Button mStartBtn;
    private Button mStopBtn;
    private Button mClearBtn;
    private EditText mIntervalView;
    private ListView mListView;
    private Button mAppsSelectBtn;

    private AppsSelectedAdapter mAdapter;

    private ArrayList<String> mPkgNames = new ArrayList<String>();
    private int mInterval = 5; // seconds

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
            mPkgNames.addAll(taskInfo.pkgNames);
        }

        mStartBtn = (Button) findViewById(R.id.start);
        mStartBtn.setOnClickListener(this);
        mStopBtn = (Button) findViewById(R.id.stop);
        mStopBtn.setOnClickListener(this);
        mClearBtn = (Button) findViewById(R.id.clear);
        mClearBtn.setOnClickListener(this);

        mIntervalView = (EditText) findViewById(R.id.interval);
        mIntervalView.setText(String.valueOf(mInterval));

        mListView = (ListView) findViewById(R.id.list);
        mAdapter = new AppsSelectedAdapter(getLayoutInflater());
        mListView.setAdapter(mAdapter);
        mAppsSelectBtn = (Button) findViewById(R.id.apps_select);
        mAppsSelectBtn.setOnClickListener(this);

        refreshSamplingState(taskInfo != null && taskInfo.isSampling);
        if (mPkgNames.size() > 0) {
            updateSelectedApps(mPkgNames);
        }
    }

    private void refreshSamplingState(boolean isSampling) {
        if (isSampling) {
            mStopBtn.setEnabled(true);
            mStartBtn.setEnabled(false);
            mClearBtn.setEnabled(false);
            mAppsSelectBtn.setEnabled(false);
        } else {
            mStartBtn.setEnabled(true);
            mClearBtn.setEnabled(true);
            mAppsSelectBtn.setEnabled(true);
            mStopBtn.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mStartBtn) {
            String intervalStr = mIntervalView.getText().toString();
            if (intervalStr.length() == 0) {
                Toast.makeText(this, R.string.apps_sampler_sample_interval_input_toast,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (mPkgNames.size() == 0) {
                Toast.makeText(this, R.string.apps_sampler_no_apps_toast,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            mInterval = Integer.parseInt(intervalStr);
            AppsSamplerService.startSamplerService(this, mPkgNames, mInterval);
            Toast.makeText(this, R.string.apps_sampler_start_sampling_toast,
                    Toast.LENGTH_SHORT).show();
            refreshSamplingState(true);
        } else if (v == mStopBtn) {
            AppsSamplerService.stopSamplerService(this);
            Toast.makeText(this, R.string.apps_sampler_stop_sampling_toast,
                    Toast.LENGTH_SHORT).show();
            refreshSamplingState(false);
        } else if (v == mClearBtn) {
            AppsSamplerService.clearLogs();
            Toast.makeText(this, R.string.apps_sampler_clear_logs_toast,
                    Toast.LENGTH_SHORT).show();
        } else if (v == mAppsSelectBtn) {
            Intent intent = new Intent(this, AppsSelectorActivity.class);
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
}
