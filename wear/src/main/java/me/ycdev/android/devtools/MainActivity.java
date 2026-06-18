package me.ycdev.android.devtools;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.core.app.ComponentActivity;
import androidx.wear.ambient.AmbientLifecycleObserver;
import androidx.wear.ambient.AmbientLifecycleObserverKt;

@SuppressLint("MyBaseActivity")
public class MainActivity extends ComponentActivity {

    private final AmbientLifecycleObserver.AmbientLifecycleCallback mAmbientCallback =
            new AmbientLifecycleObserver.AmbientLifecycleCallback() {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enables Always-on
        getLifecycle().addObserver(
                AmbientLifecycleObserverKt.AmbientLifecycleObserver(this, mAmbientCallback));
    }
}
