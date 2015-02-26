package me.ycdev.android.devtools.device;

import java.lang.reflect.Method;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.utils.AppLogger;
import me.ycdev.android.devtools.utils.ViewHelper;
import me.ycdev.android.lib.common.internalapi.android.os.EnvironmentIA;
import me.ycdev.android.lib.common.internalapi.android.os.SystemPropertiesIA;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class DeviceInfoActivity extends ActionBarActivity {
    private static final String TAG = "DeviceInfoActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_info_activity);

        initViews();
    }

    @SuppressLint("InlinedApi")
    private void initViews() {
        LinearLayout holder = (LinearLayout) findViewById(R.id.content_holder);

        DisplayMetrics screenMetrics = getScreenMetrics(this);

        ViewHelper.addTextView(holder, "Manufacturer", Build.MANUFACTURER);
        ViewHelper.addTextView(holder, "Product", Build.PRODUCT);
        ViewHelper.addTextView(holder, "Device", Build.DEVICE);
        ViewHelper.addTextView(holder, "Id", Build.ID);
        ViewHelper.addTextView(holder, "Model", Build.MODEL);
        ViewHelper.addTextView(holder, "API level", String.valueOf(Build.VERSION.SDK_INT));
        ViewHelper.addTextView(holder, "SDK Release", Build.VERSION.RELEASE);
        ViewHelper.addTextView(holder, "CPU ABI", Build.CPU_ABI);
        ViewHelper.addTextView(holder, "CPU ABI2", Build.CPU_ABI2);
        ViewHelper.addTextView(holder, "Fingerprint", Build.FINGERPRINT);

        ViewHelper.addLineView(holder, 0xff00ff00); // green
        ViewHelper.addTextView(holder, "LCD densityDpi", screenMetrics.densityDpi);
        ViewHelper.addTextView(holder, "LCD real X&Y densityDPI",
                screenMetrics.xdpi + " x " + screenMetrics.ydpi);
        ViewHelper.addTextView(holder, "LCD density", screenMetrics.density);
        ViewHelper.addTextView(holder, "LCD scaledDensity", screenMetrics.scaledDensity);
        ViewHelper.addTextView(holder, "LCD type", getScreenLayoutSizeType());
        Point screenSizePt = getScreenHardwareSize(this);
        ViewHelper.addTextView(holder, "LCD size in pixels",
                screenSizePt.x + " x " + screenSizePt.y);
        ViewHelper.addTextView(holder, "LCD size in DIP",
                screenMetrics.widthPixels / screenMetrics.density + " x " +
                        screenMetrics.heightPixels / screenMetrics.density);
        ViewHelper.addTextView(holder, "VM heap", SystemPropertiesIA.get("dalvik.vm.heapsize", "N/A"));
        ViewHelper.addTextView(holder, "ANR trace", SystemPropertiesIA.get("dalvik.vm.stack-trace-file", "N/A"));

        ViewHelper.addLineView(holder, 0xff00ff00); // green
        ViewHelper.addTextView(holder, "DNS1", SystemPropertiesIA.get("dhcp.eth0.dns1", "N/A"));
        ViewHelper.addTextView(holder, "DNS2", SystemPropertiesIA.get("dhcp.eth0.dns2", "N/A"));
        ViewHelper.addTextView(holder, "IP", SystemPropertiesIA.get("dhcp.eth0.ipaddress", "N/A"));
        ViewHelper.addTextView(holder, "Gateway", SystemPropertiesIA.get("dhcp.eth0.gateway", "N/A"));
        ViewHelper.addTextView(holder, "Mask", SystemPropertiesIA.get("dhcp.eth0.mask", "N/A"));

        ViewHelper.addLineView(holder, 0xff00ff00); // green
        if (Build.VERSION.SDK_INT >= 9) {
            ViewHelper.addTextView(holder, "Encrypted FS: " +
                    EnvironmentIA.isEncryptedFilesystemEnabled());
        }
        ViewHelper.addTextView(holder, "Data Dir: " +
                Environment.getDataDirectory().getAbsolutePath());
        ViewHelper.addTextView(holder, "Download Cache Dir: " +
                Environment.getDownloadCacheDirectory());

        if (Build.VERSION.SDK_INT >= 9) {
            ViewHelper.addTextView(holder, "Android Secure Data Dir: " +
                    EnvironmentIA.getSecureDataDirectory());
            ViewHelper.addTextView(holder, "Android System Secure Dir: " +
                    EnvironmentIA.getSystemSecureDirectory());
        }

        ViewHelper.addTextView(holder, "External Storage Dir: " +
                Environment.getExternalStorageDirectory());
        ViewHelper.addTextView(holder, "External Storage state: " +
                Environment.getExternalStorageState());
        ViewHelper.addTextView(holder, "External Storage size: " + getExternalStorageSize());
        if (Build.VERSION.SDK_INT >= 9) {
            ViewHelper.addTextView(holder, "External storage removable: " +
                    Environment.isExternalStorageRemovable());
        }
        ViewHelper.addTextView(holder, "External Storage Data Dir: " +
                EnvironmentIA.getExternalStorageAndroidDataDir());

        if (Build.VERSION.SDK_INT >= 8) {
            ViewHelper.addTextView(holder, "Alarms audio: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS));
            ViewHelper.addTextView(holder, "DCIM: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
            ViewHelper.addTextView(holder, "Download: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            ViewHelper.addTextView(holder, "Movies: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES));
            ViewHelper.addTextView(holder, "Music: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
            ViewHelper.addTextView(holder, "Notification audio: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS));
            ViewHelper.addTextView(holder, "Pictures: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
            ViewHelper.addTextView(holder, "Podcasts: " +
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS));
            ViewHelper.addTextView(holder, "Ringtones: " +
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
                getRealSizeMethod.invoke(screen, pt);
            } catch (Exception e) {
                AppLogger.w(TAG, "Unexpected exception: ", e);
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
