package me.ycdev.android.devtools.root.cmd;

import android.content.Context;

public abstract class ContextCmdBase implements Runnable {
    protected Context mContext;

    public ContextCmdBase(Context cxt) {
        mContext = cxt;
    }
}
