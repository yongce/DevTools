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
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashSet

open class AppsSelectorAdapter(
    private val changeListener: SelectedAppsChangeListener,
    private val multiChoice: Boolean
) : RecyclerView.Adapter<ViewHolder>() {

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

    var data: List<AppInfo>? = null

    fun sort(comparator: Comparator<AppInfo>) {
        data?.let {
            Collections.sort(it, comparator)
            notifyDataSetChanged()
        }
    }

    private fun getItem(position: Int): AppInfo {
        return data!![position]
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.apps_selector_list_item, parent, false)
        return ViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.appIcon.setImageDrawable(item.appIcon)
        holder.binding.appName.text = item.appName
        holder.binding.pkgName.text = item.pkgName
        holder.binding.checkbox.isChecked = item.isSelected
        holder.binding.checkbox.tag = item
        holder.binding.checkbox.setOnClickListener(checkedChangeListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: AppsSelectorListItemBinding = AppsSelectorListItemBinding.bind(itemView)
    }
}