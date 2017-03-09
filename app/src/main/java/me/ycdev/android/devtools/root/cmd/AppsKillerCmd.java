package me.ycdev.android.devtools.root.cmd;

import android.content.Context;

import java.util.List;

import me.ycdev.android.arch.utils.AppLogger;
import me.ycdev.android.devtools.root.RootCommandBuilder;

public class AppsKillerCmd extends RootCmdBase {
    private static final String TAG = "AppsKillerCmd";

    private List<String> mPkgList;

    public AppsKillerCmd(Context cxt) {
        super(cxt);
    }

    public void setPackagesToKill(List<String> pkgList) {
        mPkgList = pkgList;
    }

    @Override
    public void run() {
        if (mPkgList == null || mPkgList.size() == 0) {
            AppLogger.w(TAG, "no apps to kill");
            return;
        }

        String[] cmds = RootCommandBuilder.forceStopPackage(mContext, mPkgList);
        runSuCommand(cmds, "u:r:system_app:s0");
    }
}
