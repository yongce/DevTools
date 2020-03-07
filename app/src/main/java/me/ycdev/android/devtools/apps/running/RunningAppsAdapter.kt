package me.ycdev.android.devtools.apps.running

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.apps.running.RunningAppsAdapter.ViewHolder
import me.ycdev.android.devtools.databinding.RunningAppsItemBinding
import me.ycdev.android.lib.commonui.base.ListAdapterBase
import me.ycdev.android.lib.commonui.base.ViewHolderBase

class RunningAppsAdapter(cxt: Context) : ListAdapterBase<RunningAppInfo, ViewHolder>(cxt) {
    override val itemLayoutResId: Int = R.layout.running_apps_item

    override fun createViewHolder(itemView: View, position: Int): ViewHolder {
        return ViewHolder(itemView, position)
    }

    @SuppressLint("SetTextI18n")
    override fun bindView(
        item: RunningAppInfo,
        holder: ViewHolder
    ) {
        holder.binding.appIcon.setImageDrawable(item.appInfo.appIcon)
        holder.binding.appName.text = item.appInfo.appName
        holder.binding.pkgName.text = item.appInfo.pkgName
        holder.binding.memoryUsage.text = item.totalMemPss.toString() + "KB"
        val sb = StringBuilder()
        for (procInfo in item.allProcesses) {
            sb.append("\npid: ").append(procInfo.pid)
            sb.append(", procName: ").append(procInfo.procName)
            sb.append(", memPss: ").append(procInfo.memPss)
            if (procInfo.multiplePkgNames) {
                sb.append("*")
            }
        }
        holder.binding.processesHolder.text = sb.toString()
    }

    class ViewHolder(itemView: View, position: Int) : ViewHolderBase(itemView, position) {
        val binding: RunningAppsItemBinding = RunningAppsItemBinding.bind(itemView)
    }
}