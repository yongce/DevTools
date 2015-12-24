package me.ycdev.android.devtools.apps.selector;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import me.ycdev.android.devtools.R;
import me.ycdev.android.lib.common.apps.AppInfo;
import me.ycdev.android.lib.commonui.base.ListAdapterBase;
import me.ycdev.android.lib.commonui.base.ViewHolderBase;

class AppsSelectorAdapter extends ListAdapterBase<AppInfo, AppsSelectorAdapter.ViewHolder> {
    public interface SelectedAppsChangeListener {
        void onSelectedAppsChanged(int newCount);
    }

    private SelectedAppsChangeListener mChangeListener;
    private boolean mMultiChoice;
    private HashSet<AppInfo> mSelectedApps = new HashSet<>();

    private View.OnClickListener mCheckedChangeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AppInfo item = (AppInfo) v.getTag();
            item.isSelected = !item.isSelected;
            if (item.isSelected) {
                if (!mMultiChoice && mSelectedApps.size() > 0) {
                    getOneSelectedApp().isSelected = false;
                    mSelectedApps.clear();
                }
                mSelectedApps.add(item);
            } else {
                mSelectedApps.remove(item);
            }
            notifyDataSetChanged();
            mChangeListener.onSelectedAppsChanged(mSelectedApps.size());
        }
    };

    public AppsSelectorAdapter(Context cxt, SelectedAppsChangeListener listener,
                               boolean multiChoice) {
        super(cxt);
        mChangeListener = listener;
        mMultiChoice = multiChoice;
    }

    public int getSelectedAppsCount() {
        return mSelectedApps.size();
    }

    public List<AppInfo> getSelectedApps() {
        return new ArrayList<>(mSelectedApps);
    }

    public AppInfo getOneSelectedApp() {
        Iterator<AppInfo> it = mSelectedApps.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override
    public void setData(List<AppInfo> data) {
        super.setData(data);
        Collections.sort(mList, new AppInfo.AppNameComparator());
    }

    @Override
    protected @LayoutRes int getItemLayoutResId() {
        return R.layout.apps_selector_list_item;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(@NonNull View itemView, int position) {
        return new ViewHolder(itemView, position);
    }

    @Override
    protected void bindView(@NonNull AppInfo item, @NonNull ViewHolder vh) {
        vh.iconView.setImageDrawable(item.appIcon);
        vh.appNameView.setText(item.appName);
        vh.pkgNameView.setText(item.pkgName);
        vh.checkBox.setChecked(item.isSelected);
        vh.checkBox.setTag(item);
        vh.checkBox.setOnClickListener(mCheckedChangeListener);
    }

    static class ViewHolder extends ViewHolderBase {
        public ImageView iconView;
        public TextView appNameView;
        public TextView pkgNameView;
        public CheckBox checkBox;

        public ViewHolder(View itemView, int position) {
            super(itemView, position);
            iconView = (ImageView) itemView.findViewById(R.id.app_icon);
            appNameView = (TextView) itemView.findViewById(R.id.app_name);
            pkgNameView = (TextView) itemView.findViewById(R.id.pkg_name);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }
}
