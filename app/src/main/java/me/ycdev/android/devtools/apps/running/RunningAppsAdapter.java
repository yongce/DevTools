package me.ycdev.android.devtools.apps.running;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.ycdev.android.devtools.R;
import me.ycdev.android.lib.commonui.base.ListAdapterBase;

public class RunningAppsAdapter extends ListAdapterBase<RunningAppInfo> {
    public RunningAppsAdapter(@NonNull Context cxt) {
        super(cxt);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.running_apps_item;
    }

    @NonNull
    @Override
    protected ViewHolderBase createViewHolder(@NonNull View itemView, int position) {
        return new ViewHolder(itemView, position);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void bindView(@NonNull RunningAppInfo item, @NonNull ViewHolderBase holder) {
        ViewHolder vh = (ViewHolder) holder;
        vh.appIconView.setImageDrawable(item.appIcon);
        vh.appNameView.setText(item.appName);
        vh.pkgNameView.setText(item.pkgName);
        vh.memoryUsageView.setText(String.valueOf(item.totalMemPss) + "KB");

        StringBuilder sb = new StringBuilder();
        for (RunningAppInfo.ProcInfo procInfo : item.allProcesses) {
            sb.append("\npid: ").append(procInfo.pid);
            sb.append(", procName: ").append(procInfo.procName);
            sb.append(", memPss: ").append(procInfo.memPss);
            if (procInfo.multiplePkgNames) {
                sb.append("*");
            }
        }
        vh.processesHolderView.setText(sb.toString());
    }

    class ViewHolder extends ViewHolderBase {
        public ImageView appIconView;
        public TextView appNameView;
        public TextView pkgNameView;
        public TextView memoryUsageView;
        public TextView processesHolderView;

        public ViewHolder(@NonNull View itemView, int position) {
            super(itemView, position);
        }

        @Override
        protected void findViews() {
            appIconView = (ImageView) itemView.findViewById(R.id.app_icon);
            appNameView = (TextView) itemView.findViewById(R.id.app_name);
            pkgNameView = (TextView) itemView.findViewById(R.id.pkg_name);
            memoryUsageView = (TextView) itemView.findViewById(R.id.memory_usage);
            processesHolderView = (TextView) itemView.findViewById(R.id.processes_holder);
        }
    }
}
