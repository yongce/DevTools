package me.ycdev.android.devtools.sampler;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import me.ycdev.android.devtools.R;
import me.ycdev.android.lib.common.apps.AppInfo;
import me.ycdev.android.lib.commonui.base.ListAdapterBase;
import me.ycdev.android.lib.commonui.base.ViewHolderBase;

class AppsSelectedAdapter extends ListAdapterBase<AppInfo, AppsSelectedAdapter.ViewHolder> {
    public AppsSelectedAdapter(Context cxt) {
        super(cxt);
    }

    @Override
    protected @LayoutRes int getItemLayoutResId() {
        return R.layout.apps_selected_list_item;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(@NonNull View itemView, int position) {
        return new ViewHolder(itemView, position);
    }

    @Override
    protected void bindView(@NonNull AppInfo item, @NonNull ViewHolder vh) {
        vh.pkgNameView.setText(item.pkgName);
    }

    static class ViewHolder extends ViewHolderBase {
        public TextView pkgNameView;

        public ViewHolder(View itemView, int position) {
            super(itemView, position);
            pkgNameView = (TextView) itemView;
        }
    }
}
