package me.ycdev.android.devtools.apps.selector

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.ycdev.android.devtools.R
import me.ycdev.android.devtools.apps.selector.AppsSelectorAdapter.ViewHolder
import me.ycdev.android.devtools.databinding.AppsSelectorListItemBinding
import me.ycdev.android.lib.common.apps.AppInfo
import java.util.Collections
import java.util.Comparator

open class AppsSelectorAdapter(
    private val changeListener: SelectedAppsChangeListener,
    multiChoice: Boolean,
) : RecyclerView.Adapter<ViewHolder>() {
    interface SelectedAppsChangeListener {
        fun onSelectedAppsChanged(newCount: Int)
    }

    private val checkedChangeListener =
        OnClickListener { v ->
            val item = v.tag as AppInfo
            val changedPackageNames = selectionState.toggle(item.pkgName)
            changedPackageNames.forEach { pkgName ->
                val position = data?.indexOfFirst { it.pkgName == pkgName } ?: -1
                if (position >= 0) {
                    notifyItemChanged(position)
                }
            }
            changeListener.onSelectedAppsChanged(selectionState.selectedCount)
        }

    private val selectionState = AppSelectionState(multiChoice)
    val selectedAppsCount: Int
        get() = selectionState.selectedCount
    val selectedApps: List<AppInfo>
        get() =
            selectionState.selectedPackageNames.mapNotNull { pkgName ->
                data?.firstOrNull { it.pkgName == pkgName }
            }

    val oneSelectedApp: AppInfo?
        get() = selectionState.oneSelectedPackageName?.let { pkgName ->
            data?.firstOrNull { it.pkgName == pkgName }
        }

    var data: List<AppInfo>? = null

    fun sort(comparator: Comparator<AppInfo>) {
        data?.let {
            Collections.sort(it, comparator)
            notifyItemRangeChanged(0, it.size)
        }
    }

    private fun getItem(position: Int): AppInfo = data!![position]

    override fun getItemCount(): Int = data?.size ?: 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val itemView =
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.apps_selector_list_item, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val item = getItem(position)
        holder.binding.appIcon.setImageDrawable(item.appIcon)
        holder.binding.appName.text = item.appName
        holder.binding.pkgName.text = item.pkgName
        holder.binding.checkbox.isChecked = selectionState.isSelected(item.pkgName)
        holder.binding.checkbox.tag = item
        holder.binding.checkbox.setOnClickListener(checkedChangeListener)
    }

    class ViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {
        val binding: AppsSelectorListItemBinding = AppsSelectorListItemBinding.bind(itemView)
    }
}
