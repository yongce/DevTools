package me.ycdev.android.devtools.sampler;

import android.os.Environment;

import java.io.File;

import me.ycdev.android.devtools.utils.Constants;

public class SamplerUtils {
    public static File getSamplerFolder() {
        File sdRoot = Environment.getExternalStorageDirectory();
        return new File(sdRoot, Constants.EXTERNAL_STORAGE_PATH_APPS_SAMPLER);
    }

    public static File getFileForSampler(String fileName, boolean mkdir) {
        File appDir = getSamplerFolder();
        if (mkdir) {
            //noinspection ResultOfMethodCallIgnored
            appDir.mkdirs();
        }
        return new File(appDir, fileName);
    }
}
