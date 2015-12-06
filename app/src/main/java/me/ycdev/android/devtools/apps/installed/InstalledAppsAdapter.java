package me.ycdev.android.devtools.apps.installed;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.ycdev.android.devtools.R;
import me.ycdev.android.lib.common.apps.AppInfo;
import me.ycdev.android.lib.common.compat.ViewsCompat;
import me.ycdev.android.lib.commonui.base.ListAdapterBase;

class InstalledAppsAdapter extends ListAdapterBase<AppInfo> {
    private static final int ALPHA_ENABLED = 255;
    private static final int ALPHA_DISABLED = 100;

    private int mSysAppColor;
    private int mNormalAppColor;
    private int mSharedAppColor;
    private int mUnavailableColor;

    InstalledAppsAdapter(@NonNull Context cxt) {
        super(cxt);
        mSysAppColor = cxt.getResources().getColor(R.color.apps_sys_app_color);
        mNormalAppColor = cxt.getResources().getColor(android.R.color.secondary_text_dark);
        mSharedAppColor = cxt.getResources().getColor(R.color.apps_shared_uid_color);
        mUnavailableColor = cxt.getResources().getColor(R.color.apps_unavailable_color);
    }

    @Override
    protected @LayoutRes int getItemLayoutResId() {
        return R.layout.installed_apps_item;
    }

    @NonNull
    @Override
    protected ViewHolderBase createViewHolder(@NonNull View itemView, int position) {
        return new ViewHolder(itemView, position);
    }

    @Override
    protected void bindView(@NonNull AppInfo item, @NonNull ViewHolderBase holder) {
        ViewHolder vh = (ViewHolder) holder;
        vh.iconView.setImageDrawable(item.appIcon);
        vh.appNameView.setText(item.appName);
        vh.appUidView.setText(String.valueOf(item.appUid));
        vh.sharedUidView.setText(item.sharedUid);
        vh.pkgNameView.setText(item.pkgName);
        vh.sharedUidView.setTextColor(mSharedAppColor);
        if (item.isSysApp) {
            vh.pkgNameView.setTextColor(mSysAppColor);
        } else {
            vh.pkgNameView.setTextColor(mNormalAppColor);
        }
        vh.versionNameView.setText(item.versionName);
        vh.versionCodeView.setText(String.valueOf(item.versionCode));
        vh.apkPathView.setText(item.apkPath);

        String stateStr = mContext.getString(R.string.apps_app_state,
                String.valueOf(item.isDisabled), String.valueOf(item.isUnmounted));
        vh.stateView.setText(stateStr);
        if (item.isDisabled || item.isUnmounted) {
            ViewsCompat.setImageViewAlpha(vh.iconView, ALPHA_DISABLED);
            vh.stateView.setTextColor(mUnavailableColor);
        } else {
            ViewsCompat.setImageViewAlpha(vh.iconView, ALPHA_ENABLED);
            vh.stateView.setTextColor(mNormalAppColor);
        }
    }

    private static class ViewHolder extends ViewHolderBase {
        public ImageView iconView;
        public TextView appNameView;
        public TextView appUidView;
        public TextView sharedUidView;
        public TextView pkgNameView;
        public TextView versionNameView;
        public TextView versionCodeView;
        public TextView apkPathView;
        public TextView stateView;

        public ViewHolder(@NonNull View itemView, int position) {
            super(itemView, position);
        }

        @Override
        protected void findViews() {
            iconView = (ImageView) itemView.findViewById(R.id.app_icon);
            appNameView = (TextView) itemView.findViewById(R.id.app_name);
            appUidView = (TextView) itemView.findViewById(R.id.app_uid);
            sharedUidView = (TextView) itemView.findViewById(R.id.shared_uid);
            pkgNameView = (TextView) itemView.findViewById(R.id.pkg_name);
            versionNameView = (TextView) itemView.findViewById(R.id.version_name);
            versionCodeView = (TextView) itemView.findViewById(R.id.version_code);
            apkPathView = (TextView) itemView.findViewById(R.id.apk_path);
            stateView = (TextView) itemView.findViewById(R.id.state);
        }
    }
}
