package me.ycdev.android.devtools.sampler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.sampler.AppsSelectedAdapter.ViewHolder
import me.ycdev.android.lib.common.apps.AppInfo

internal class AppsSelectedAdapter : RecyclerView.Adapter<ViewHolder>() {
    var data: List<AppInfo>? = null

    private fun getItem(position: Int): AppInfo {
        return data!![position]
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.apps_selected_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.pkgNameView.text = item.pkgName
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var pkgNameView: TextView = itemView as TextView
    }
}