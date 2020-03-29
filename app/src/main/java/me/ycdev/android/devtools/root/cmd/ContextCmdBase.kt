package me.ycdev.android.devtools.root.cmd

import android.content.Context

abstract class ContextCmdBase(protected var context: Context) : Runnable
