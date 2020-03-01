package me.ycdev.android.devtools.apps.installed;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import me.ycdev.android.devtools.R;
import me.ycdev.android.lib.common.apps.AppInfo;
import me.ycdev.android.lib.commonui.base.ListAdapterBase;
import me.ycdev.android.lib.commonui.base.ViewHolderBase;

class InstalledAppsAdapter extends ListAdapterBase<AppInfo, InstalledAppsAdapter.ViewHolder> {
    private static final int ALPHA_ENABLED = 255;
    private static final int ALPHA_DISABLED = 100;

    private int mSysAppColor;
    private int mNormalAppColor;
    private int mSharedAppColor;
    private int mUnavailableColor;

    InstalledAppsAdapter(@NonNull Context cxt) {
        super(cxt);
        mSysAppColor = ContextCompat.getColor(cxt, R.color.apps_sys_app_color);
        mSysAppColor = ContextCompat.getColor(cxt, R.color.apps_sys_app_color);
        mNormalAppColor = ContextCompat.getColor(cxt, android.R.color.secondary_text_dark);
        mSharedAppColor = ContextCompat.getColor(cxt, R.color.apps_shared_uid_color);
        mUnavailableColor = ContextCompat.getColor(cxt, R.color.apps_unavailable_color);
    }

    @Override
    protected @LayoutRes int getItemLayoutResId() {
        return R.layout.installed_apps_item;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(@NonNull View itemView, int position) {
        return new ViewHolder(itemView, position);
    }

    @Override
    protected void bindView(@NonNull AppInfo item, @NonNull ViewHolder vh) {
        vh.iconView.setImageDrawable(item.getAppIcon());
        vh.appNameView.setText(item.getAppName());
        vh.appUidView.setText(String.valueOf(item.getAppUid()));
        vh.sharedUidView.setText(item.getSharedUid());
        vh.pkgNameView.setText(item.getPkgName());
        vh.sharedUidView.setTextColor(mSharedAppColor);
        if (item.isSysApp()) {
            vh.pkgNameView.setTextColor(mSysAppColor);
        } else {
            vh.pkgNameView.setTextColor(mNormalAppColor);
        }
        vh.versionNameView.setText(item.getVersionName());
        vh.versionCodeView.setText(String.valueOf(item.getVersionCode()));
        vh.apkPathView.setText(item.getApkPath());

        String stateStr = getContext().getString(R.string.apps_app_state,
                String.valueOf(item.isDisabled()), String.valueOf(item.isUnmounted()));
        vh.stateView.setText(stateStr);
        if (item.isDisabled() || item.isUnmounted()) {
            vh.iconView.setImageAlpha(ALPHA_DISABLED);
            vh.stateView.setTextColor(mUnavailableColor);
        } else {
            vh.iconView.setImageAlpha(ALPHA_ENABLED);
            vh.stateView.setTextColor(mNormalAppColor);
        }
    }

    static class ViewHolder extends ViewHolderBase {
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
