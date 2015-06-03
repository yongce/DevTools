package me.ycdev.android.devtools.apps.selector;

import android.content.Context;
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

class AppsSelectorAdapter extends ListAdapterBase<AppInfo> {
    public interface SelectedAppsChangeListener {
        public void onSelectedAppsChanged(int newCount);
    }

    private SelectedAppsChangeListener mChangeListener;
    private boolean mMultiChoice;
    private HashSet<AppInfo> mSelectedApps = new HashSet<AppInfo>();

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
        return new ArrayList<AppInfo>(mSelectedApps);
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
    protected int getItemResId() {
        return R.layout.apps_selector_list_item;
    }

    @Override
    protected ViewHolderBase createViewHolder(View itemView, int position) {
        return new ViewHolder(itemView, position);
    }

    @Override
    protected void bindView(AppInfo item, ViewHolderBase holder) {
        ViewHolder vh = (ViewHolder) holder;
        vh.iconView.setImageDrawable(item.appIcon);
        vh.appNameView.setText(item.appName);
        vh.pkgNameView.setText(item.pkgName);
        vh.checkBox.setChecked(item.isSelected);
        vh.checkBox.setTag(item);
        vh.checkBox.setOnClickListener(mCheckedChangeListener);
    }

    private static class ViewHolder extends ViewHolderBase {
        public ImageView iconView;
        public TextView appNameView;
        public TextView pkgNameView;
        public CheckBox checkBox;

        public ViewHolder(View itemView, int position) {
            super(itemView, position);
        }

        @Override
        protected void findViews() {
            iconView = (ImageView) itemView.findViewById(R.id.app_icon);
            appNameView = (TextView) itemView.findViewById(R.id.app_name);
            pkgNameView = (TextView) itemView.findViewById(R.id.pkg_name);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }
}
