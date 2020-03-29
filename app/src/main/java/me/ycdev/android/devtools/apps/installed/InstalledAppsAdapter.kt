package me.ycdev.android.devtools.apps.installed

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections
import java.util.Comparator
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.apps.installed.InstalledAppsAdapter.ViewHolder
import me.ycdev.android.devtools.databinding.InstalledAppsItemBinding
import me.ycdev.android.lib.common.apps.AppInfo
import timber.log.Timber

internal class InstalledAppsAdapter(cxt: Context) : RecyclerView.Adapter<ViewHolder>() {
    private lateinit var defaultTextColor: ColorStateList
    private var sysAppColor: Int = ContextCompat.getColor(cxt, R.color.apps_sys_app_color)
    private val sharedAppColor: Int = ContextCompat.getColor(cxt, R.color.apps_shared_uid_color)
    private val unavailableColor: Int = ContextCompat.getColor(cxt, R.color.apps_unavailable_color)

    var data: List<AppInfo>? = null

    private fun getItem(position: Int): AppInfo {
        return data!![position]
    }

    fun sort(comparator: Comparator<AppInfo>) {
        data?.let {
            Collections.sort(it, comparator)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.installed_apps_item, parent, false)
        return ViewHolder(itemView).also {
            defaultTextColor = it.binding.appName.textColors
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.appIcon.setImageDrawable(item.appIcon)
        holder.binding.appName.text = item.appName
        holder.binding.appUid.text = item.appUid.toString()
        holder.binding.sharedUid.text = item.sharedUid
        holder.binding.sharedUid.setTextColor(sharedAppColor)
        holder.binding.pkgName.text = item.pkgName
        if (item.isSysApp) {
            holder.binding.pkgName.setTextColor(sysAppColor)
        } else {
            holder.binding.pkgName.setTextColor(defaultTextColor)
        }
        holder.binding.versionName.text = item.versionName
        holder.binding.versionCode.text = item.versionCode.toString()
        holder.binding.apkPath.text = item.apkPath
        val stateStr = holder.itemView.context.getString(
            R.string.apps_app_state,
            item.isDisabled.toString(),
            item.isUnmounted.toString()
        )
        holder.binding.state.text = stateStr
        if (item.isDisabled || item.isUnmounted) {
            holder.binding.appIcon.imageAlpha = ALPHA_DISABLED
            holder.binding.state.setTextColor(unavailableColor)
        } else {
            holder.binding.appIcon.imageAlpha = ALPHA_ENABLED
            holder.binding.state.setTextColor(defaultTextColor)
        }

        holder.binding.targetMinSdk.text = holder.itemView.context.getString(
            R.string.apps_app_target_min_sdk,
            item.targetSdkVersion,
            item.minSdkVersion
        )

        holder.itemView.setOnClickListener {
            Timber.tag(TAG).i("clicked item: %s", item)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: InstalledAppsItemBinding = InstalledAppsItemBinding.bind(itemView)
    }

    companion object {
        private const val TAG = "InstalledAppsAdapter"

        private const val ALPHA_ENABLED = 255
        private const val ALPHA_DISABLED = 100
    }
}
