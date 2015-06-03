package me.ycdev.android.devtools.root.cmd;

import android.content.Context;
import android.text.TextUtils;

import eu.chainfire.libsuperuser.Shell;

public abstract class RootCmdBase extends ContextCmdBase {
    public RootCmdBase(Context cxt) {
        super(cxt);
    }

    public static void runSuCommand(String[] cmds, String selinuxContext) {
        if (!TextUtils.isEmpty(selinuxContext) && isSELinuxEnforced()) {
            String shell = Shell.SU.shell(0, selinuxContext);
            Shell.run(shell, cmds, null, false);
        } else {
            Shell.SU.run(cmds);
        }
    }

    private static boolean isSELinuxEnforced() {
        return Shell.SU.isSELinuxEnforcing();
    }
}
