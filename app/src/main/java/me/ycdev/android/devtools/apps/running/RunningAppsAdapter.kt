package me.ycdev.android.devtools.apps.running

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.apps.running.RunningAppsAdapter.ViewHolder
import me.ycdev.android.devtools.databinding.RunningAppsItemBinding

class RunningAppsAdapter : RecyclerView.Adapter<ViewHolder>() {
    var data: List<RunningAppInfo>? = null

    private fun getItem(position: Int): RunningAppInfo {
        return data!![position]
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.running_apps_item, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RunningAppsItemBinding = RunningAppsItemBinding.bind(itemView)
    }
}