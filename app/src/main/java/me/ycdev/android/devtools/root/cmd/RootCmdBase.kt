package me.ycdev.android.devtools.root.cmd

import android.content.Context

// import eu.chainfire.libsuperuser.Shell;
abstract class RootCmdBase(cxt: Context) : ContextCmdBase(cxt) {
    companion object {
        fun runSuCommand(
            cmds: Array<String>,
            selinuxContext: String?
        ) { //        if (!TextUtils.isEmpty(selinuxContext) && isSELinuxEnforced()) {
//            String shell = Shell.SU.shell(0, selinuxContext);
//            Shell.run(shell, cmds, null, false, true);
//        } else {
//            Shell.SU.run(cmds);
//        }
        } //    private static boolean isSELinuxEnforced() {
//        return Shell.SU.isSELinuxEnforcing();
//    }
    }
}
