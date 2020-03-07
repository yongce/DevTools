package me.ycdev.android.devtools.apps.selector

import android.content.Context
import android.view.View
import android.view.View.OnClickListener
import androidx.annotation.LayoutRes
import me.ycdev.android.devtools.R.layout
import me.ycdev.android.devtools.apps.selector.AppsSelectorAdapter.ViewHolder
import me.ycdev.android.devtools.databinding.AppsSelectorListItemBinding
import me.ycdev.android.lib.common.apps.AppInfo
import me.ycdev.android.lib.common.apps.AppInfo.AppNameComparator
import me.ycdev.android.lib.commonui.base.ListAdapterBase
import me.ycdev.android.lib.commonui.base.ViewHolderBase
import java.util.ArrayList
import java.util.Collections
import java.util.HashSet

open class AppsSelectorAdapter(
    cxt: Context,
    private val changeListener: SelectedAppsChangeListener,
    private val multiChoice: Boolean
) : ListAdapterBase<AppInfo, ViewHolder>(cxt) {

    interface SelectedAppsChangeListener {
        fun onSelectedAppsChanged(newCount: Int)
    }

    private val checkedChangeListener = OnClickListener { v ->
        val item = v.tag as AppInfo
        item.isSelected = !item.isSelected
        if (item.isSelected) {
            if (!multiChoice && selectedAppsCount > 0) {
                oneSelectedApp?.isSelected = false
                _selectedApps.clear()
            }
            _selectedApps.add(item)
        } else {
            _selectedApps.remove(item)
        }
        notifyDataSetChanged()
        changeListener.onSelectedAppsChanged(_selectedApps.size)
    }

    private val _selectedApps = HashSet<AppInfo>()
    val selectedAppsCount: Int
        get() = _selectedApps.size
    val selectedApps: List<AppInfo>
        get() = ArrayList(_selectedApps)

    val oneSelectedApp: AppInfo?
        get() {
            val it = _selectedApps.iterator()
            return if (it.hasNext()) it.next() else null
        }

    override fun setData(data: List<AppInfo>?) {
        if (data != null) {
            Collections.sort(data, AppNameComparator())
        }
        super.setData(data)
    }

    @LayoutRes
    override val itemLayoutResId: Int = layout.apps_selector_list_item

    override fun createViewHolder(itemView: View, position: Int): ViewHolder {
        return ViewHolder(itemView, position)
    }

    override fun bindView(item: AppInfo, holder: ViewHolder) {
        holder.binding.appIcon.setImageDrawable(item.appIcon)
        holder.binding.appName.text = item.appName
        holder.binding.pkgName.text = item.pkgName
        holder.binding.checkbox.isChecked = item.isSelected
        holder.binding.checkbox.tag = item
        holder.binding.checkbox.setOnClickListener(checkedChangeListener)
    }

    class ViewHolder(itemView: View, position: Int) : ViewHolderBase(itemView, position) {
        val binding: AppsSelectorListItemBinding = AppsSelectorListItemBinding.bind(itemView)
    }
}