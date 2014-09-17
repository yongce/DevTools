package me.ycdev.android.devtools.sampler;

import java.util.ArrayList;

import me.ycdev.android.devtools.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class AppsSamplerActivity extends Activity implements View.OnClickListener {
    private Button mStartBtn;
    private Button mStopBtn;
    private Button mClearBtn;
    private EditText mIntervalView;
    private ListView mListView;

    private ArrayList<String> mPkgNames = new ArrayList<String>();
    private int mInterval = 5; // seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_apps_sampler);

        mPkgNames.add("me.ycdev.android.devtools");

        initViews();
    }

    private void initViews() {
        mStartBtn = (Button) findViewById(R.id.start);
        mStartBtn.setOnClickListener(this);
        mStopBtn = (Button) findViewById(R.id.stop);
        mStopBtn.setOnClickListener(this);
        mClearBtn = (Button) findViewById(R.id.clear);
        mClearBtn.setOnClickListener(this);
        mIntervalView = (EditText) findViewById(R.id.interval);
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
                Toast.makeText(this, R.string.apps_sampler_apps_list_select_toast,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            mInterval = Integer.parseInt(intervalStr);
            AppsSamplerService.startSamplerService(this, mPkgNames, mInterval);
            Toast.makeText(this, R.string.apps_sampler_start_sampling_toast,
                    Toast.LENGTH_SHORT).show();
        } else if (v == mStopBtn) {
            AppsSamplerService.stopSamplerService(this);
            Toast.makeText(this, R.string.apps_sampler_stop_sampling_toast,
                    Toast.LENGTH_SHORT).show();
        } else if (v == mClearBtn) {
            AppsSamplerService.clearLogs();
            Toast.makeText(this, R.string.apps_sampler_clear_logs_toast,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
