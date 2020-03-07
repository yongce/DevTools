package me.ycdev.android.devtools.apps.installed

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.apps.installed.InstalledAppsAdapter.ViewHolder
import me.ycdev.android.devtools.databinding.InstalledAppsItemBinding
import me.ycdev.android.lib.common.apps.AppInfo
import me.ycdev.android.lib.commonui.base.ListAdapterBase
import me.ycdev.android.lib.commonui.base.ViewHolderBase

internal class InstalledAppsAdapter(cxt: Context) : ListAdapterBase<AppInfo, ViewHolder>(cxt) {
    private lateinit var defaultTextColor: ColorStateList
    private var sysAppColor: Int = ContextCompat.getColor(cxt, R.color.apps_sys_app_color)
    private val sharedAppColor: Int = ContextCompat.getColor(cxt, R.color.apps_shared_uid_color)
    private val unavailableColor: Int = ContextCompat.getColor(cxt, R.color.apps_unavailable_color)

    @get:LayoutRes
    override val itemLayoutResId: Int = R.layout.installed_apps_item

    override fun createViewHolder(itemView: View, position: Int): ViewHolder {
        return ViewHolder(itemView, position).also {
            defaultTextColor = it.binding.appName.textColors
        }
    }

    override fun bindView(item: AppInfo, holder: ViewHolder) {
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
        val stateStr = context.getString(
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
    }

    class ViewHolder(itemView: View, position: Int) : ViewHolderBase(itemView, position) {
        val binding: InstalledAppsItemBinding = InstalledAppsItemBinding.bind(itemView)
    }

    companion object {
        private const val ALPHA_ENABLED = 255
        private const val ALPHA_DISABLED = 100
    }
}