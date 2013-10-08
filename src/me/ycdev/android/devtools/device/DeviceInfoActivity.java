package me.ycdev.android.devtools.device;

import java.lang.reflect.Method;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.utils.Logger;
import me.ycdev.android.devtools.utils.ViewHelper;
import me.ycdev.androidlib.internalapi.android.os.EnvironmentIA;
import me.ycdev.androidlib.internalapi.android.os.SystemPropertiesIA;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class DeviceInfoActivity extends Activity {
    private static final String TAG = "DeviceInfoActivity";

    private LinearLayout mHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_info_activity);

        initViews();
    }

    @SuppressLint("InlinedApi")
    private void initViews() {
        mHolder = (LinearLayout) findViewById(R.id.content_holder);

        DisplayMetrics screenMetrics = getScreenMetrics(this);

        ViewHelper.addTextView(mHolder, "Manufacturer", Build.MANUFACTURER);
        ViewHelper.addTextView(mHolder, "Product", Build.PRODUCT);
        ViewHelper.addTextView(mHolder, "Device", Build.DEVICE);
        ViewHelper.addTextView(mHolder, "Id", Build.ID);
        ViewHelper.addTextView(mHolder, "Model", Build.MODEL);
        ViewHelper.addTextView(mHolder, "API level", String.valueOf(Build.VERSION.SDK_INT));
        ViewHelper.addTextView(mHolder, "SDK Release", Build.VERSION.RELEASE);
        ViewHelper.addTextView(mHolder, "CPU ABI", Build.CPU_ABI);
        ViewHelper.addTextView(mHolder, "CPU ABI2", Build.CPU_ABI2);
        ViewHelper.addTextView(mHolder, "Fingerprint", Build.FINGERPRINT);

        ViewHelper.addLineView(mHolder, 0xff00ff00); // green
        ViewHelper.addTextView(mHolder, "LCD densityDpi", screenMetrics.densityDpi);
        ViewHelper.addTextView(mHolder, "LCD real X&Y densityDPI",
                screenMetrics.xdpi + " x " + screenMetrics.ydpi);
        ViewHelper.addTextView(mHolder, "LCD density", screenMetrics.density);
        ViewHelper.addTextView(mHolder, "LCD scaledDensity", screenMetrics.scaledDensity);
        ViewHelper.addTextView(mHolder, "LCD type", getScreenLayoutSizeType());
        Point screenSizePt = getScreenHardwareSize(this);
        ViewHelper.addTextView(mHolder, "LCD size in pixels",
                screenSizePt.x + " x " + screenSizePt.y);
        ViewHelper.addTextView(mHolder, "LCD size in DIP",
                screenMetrics.widthPixels / screenMetrics.density + " x " +
                        screenMetrics.heightPixels / screenMetrics.density);
        ViewHelper.addTextView(mHolder, "VM heap", SystemPropertiesIA.get("dalvik.vm.heapsize", "N/A"));
        ViewHelper.addTextView(mHolder, "ANR trace", SystemPropertiesIA.get("dalvik.vm.stack-trace-file", "N/A"));

        ViewHelper.addLineView(mHolder, 0xff00ff00); // green
        ViewHelper.addTextView(mHolder, "DNS1", SystemPropertiesIA.get("dhcp.eth0.dns1", "N/A"));
        ViewHelper.addTextView(mHolder, "DNS2", SystemPropertiesIA.get("dhcp.eth0.dns2", "N/A"));
        ViewHelper.addTextView(mHolder, "IP", SystemPropertiesIA.get("dhcp.eth0.ipaddress", "N/A"));
        ViewHelper.addTextView(mHolder, "Gateway", SystemPropertiesIA.get("dhcp.eth0.gateway", "N/A"));
        ViewHelper.addTextView(mHolder, "Mask", SystemPropertiesIA.get("dhcp.eth0.mask", "N/A"));

        ViewHelper.addLineView(mHolder, 0xff00ff00); // green
        if (Build.VERSION.SDK_INT >= 9) {
            ViewHelper.addTextView(mHolder, "Encrypted FS: " +
                    EnvironmentIA.isEncryptedFilesystemEnabled());
        }
        ViewHelper.addTextView(mHolder, "Data Dir: " +
                Environment.getDataDirectory().getAbsolutePath());
        ViewHelper.addTextView(mHolder, "Download Cache Dir: " +
                Environment.getDownloadCacheDirectory());

        if (Build.VERSION.SDK_INT >= 9) {
            ViewHelper.addTextView(mHolder, "Android Secure Data Dir: " +
                    EnvironmentIA.getSecureDataDirectory());
            ViewHelper.addTextView(mHolder, "Android System Secure Dir: " +
                    EnvironmentIA.getSystemSecureDirectory());
        }

        ViewHelper.addTextView(mHolder, "External Storage Dir: " +
                Environment.getExternalStorageDirectory());
        ViewHelper.addTextView(mHolder, "External Storage state: " +
                Environment.getExternalStorageState());
        ViewHelper.addTextView(mHolder, "External Storage size: " + getExternalStorageSize());
        if (Build.VERSION.SDK_INT >= 9) {
            ViewHelper.addTextView(mHolder, "External storage removable: " +
                    Environment.isExternalStorageRemovable());
        }
        ViewHelper.addTextView(mHolder, "External Storage Data Dir: " +
                EnvironmentIA.getExternalStorageAndroidDataDir());

        if (Build.VERSION.SDK_INT >= 8) {
            ViewHelper.addTextView(mHolder, "Alarms audio: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS));
            ViewHelper.addTextView(mHolder, "DCIM: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
            ViewHelper.addTextView(mHolder, "Download: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            ViewHelper.addTextView(mHolder, "Movies: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES));
            ViewHelper.addTextView(mHolder, "Music: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
            ViewHelper.addTextView(mHolder, "Notification audio: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS));
            ViewHelper.addTextView(mHolder, "Pictures: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
            ViewHelper.addTextView(mHolder, "Podcasts: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS));
            ViewHelper.addTextView(mHolder, "Ringtones: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES));
        }
    }

    private String getScreenLayoutSizeType() {
        int type = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        switch (type) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return "small-320x426dp";
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "normal-320x470dp";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return "large-480x640";
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return "xlarge-720x960";
            case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:
                return "undefined";
        }
        return "unknown: " + type;
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static Point getScreenHardwareSize(Context cxt) {
        WindowManager wm = (WindowManager) cxt.getSystemService(Context.WINDOW_SERVICE);
        Display screen = wm.getDefaultDisplay();
        Point pt = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            try {
                Method getRealSizeMethod = Display.class.getMethod("getRealSize",
                        new Class[] { Point.class });
                getRealSizeMethod.invoke(screen, new Object[] { pt });
            } catch (Exception e) {
                Logger.w(TAG, "Unexpected exception: ", e);
                screen.getSize(pt); // exclude window decor size (eg, statusbar)
            }
        } else {
            pt.x = screen.getWidth();
            pt.y = screen.getHeight();
        }
        return pt;
    }

    // in MB
    @SuppressWarnings("deprecation")
    private static String getExternalStorageSize() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
        long avail = stat.getBlockSize() * stat.getAvailableBlocks() / (1024 * 1024);
        long total = stat.getBlockSize() * stat.getBlockCount() / (1024 * 1024);
        return avail + "/" + total + " MB";
    }

    private static DisplayMetrics getScreenMetrics(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

}
