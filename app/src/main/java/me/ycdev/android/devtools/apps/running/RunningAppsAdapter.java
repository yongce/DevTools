package me.ycdev.android.devtools.apps.running;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import me.ycdev.android.devtools.R;
import me.ycdev.android.lib.commonui.base.ListAdapterBase;
import me.ycdev.android.lib.commonui.base.ViewHolderBase;

public class RunningAppsAdapter extends ListAdapterBase<RunningAppInfo, RunningAppsAdapter.ViewHolder> {
    public RunningAppsAdapter(@NonNull Context cxt) {
        super(cxt);
    }

    @Override
    protected int getItemLayoutResId() {
        return R.layout.running_apps_item;
    }

    @NonNull
    @Override
    protected ViewHolder createViewHolder(@NonNull View itemView, int position) {
        return new ViewHolder(itemView, position);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void bindView(@NonNull RunningAppInfo item, @NonNull ViewHolder vh) {
        vh.appIconView.setImageDrawable(item.appInfo.getAppIcon());
        vh.appNameView.setText(item.appInfo.getAppName());
        vh.pkgNameView.setText(item.appInfo.getPkgName());
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

    static class ViewHolder extends ViewHolderBase {
        public ImageView appIconView;
        public TextView appNameView;
        public TextView pkgNameView;
        public TextView memoryUsageView;
        public TextView processesHolderView;

        public ViewHolder(@NonNull View itemView, int position) {
            super(itemView, position);
            appIconView = (ImageView) itemView.findViewById(R.id.app_icon);
            appNameView = (TextView) itemView.findViewById(R.id.app_name);
            pkgNameView = (TextView) itemView.findViewById(R.id.pkg_name);
            memoryUsageView = (TextView) itemView.findViewById(R.id.memory_usage);
            processesHolderView = (TextView) itemView.findViewById(R.id.processes_holder);
        }
    }
}
