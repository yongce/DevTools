package me.ycdev.android.devtools.sampler

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.LayoutRes
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.sampler.AppsSelectedAdapter.ViewHolder
import me.ycdev.android.lib.common.apps.AppInfo
import me.ycdev.android.lib.commonui.base.ListAdapterBase
import me.ycdev.android.lib.commonui.base.ViewHolderBase

internal class AppsSelectedAdapter(cxt: Context) : ListAdapterBase<AppInfo, ViewHolder>(cxt) {
    @LayoutRes
    override val itemLayoutResId: Int = R.layout.apps_selected_list_item

    override fun createViewHolder(itemView: View, position: Int): ViewHolder {
        return ViewHolder(itemView, position)
    }

    override fun bindView(item: AppInfo, holder: ViewHolder) {
        holder.pkgNameView.text = item.pkgName
    }

    class ViewHolder(itemView: View, position: Int) : ViewHolderBase(itemView, position) {
        var pkgNameView: TextView = itemView as TextView
    }
}