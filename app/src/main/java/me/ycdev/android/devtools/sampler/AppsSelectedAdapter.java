package me.ycdev.android.devtools.sampler;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import me.ycdev.android.devtools.R;
import me.ycdev.android.devtools.apps.common.AppInfo;
import me.ycdev.android.lib.common.ui.base.ListAdapterBase;

class AppsSelectedAdapter extends ListAdapterBase<AppInfo> {
    public AppsSelectedAdapter(LayoutInflater inflater) {
        super(inflater);
    }

    @Override
    protected int getItemResId() {
        return R.layout.apps_selected_list_item;
    }

    @Override
    protected ViewHolderBase createViewHolder(View itemView, int position) {
        return new ViewHolder(itemView, position);
    }

    @Override
    protected void bindView(AppInfo item, ViewHolderBase holder) {
        ViewHolder vh = (ViewHolder) holder;
        vh.pkgNameView.setText(item.pkgName);
    }

    private static class ViewHolder extends ViewHolderBase {
        public TextView pkgNameView;

        public ViewHolder(View itemView, int position) {
            super(itemView, position);
        }

        @Override
        protected void findViews() {
            pkgNameView = (TextView) itemView;
        }
    }
}
