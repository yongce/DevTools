package me.ycdev.android.devtools;

import me.ycdev.android.devtools.device.DeviceInfoActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LauncherActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initViews();
    }

    private void initViews() {
        View deviceInfoBtn = findViewById(R.id.device_info);
        deviceInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(DeviceInfoActivity.class);
            }
        });
    }

    private void startActivity(Class<?> activityClass) {
        Intent i = new Intent(this, activityClass);
        startActivity(i);
    }
}
