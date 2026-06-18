package me.ycdev.android.devtools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.wear.ambient.AmbientMode;

@SuppressLint("MyBaseActivity")
@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements AmbientMode.AmbientCallbackProvider {

    private TextView mTextView;
    private final AmbientMode.AmbientCallback mAmbientCallback = new AmbientMode.AmbientCallback() {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        AmbientMode.attachAmbientSupport(this);
    }

    @Override
    public AmbientMode.AmbientCallback getAmbientCallback() {
        return mAmbientCallback;
    }
}
