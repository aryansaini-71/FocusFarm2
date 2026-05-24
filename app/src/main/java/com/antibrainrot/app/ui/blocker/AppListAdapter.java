package com.antibrainrot.app.ui.blocker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.antibrainrot.app.R;
import com.antibrainrot.app.data.BlockedAppsPrefs;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for the Blocker tab app list.
 */
public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {

    private final List<AppInfo> apps = new ArrayList<>();

    public void setApps(List<AppInfo> newApps) {
        apps.clear();
        if (newApps != null) {
            apps.addAll(newApps);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_list, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        holder.bind(apps.get(position));
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgAppIcon;
        private final TextView tvAppName;
        private final SwitchCompat switchBlockApp;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAppIcon = itemView.findViewById(R.id.img_app_icon);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            switchBlockApp = itemView.findViewById(R.id.switch_block_app);
        }

        void bind(AppInfo appInfo) {
            Context context = itemView.getContext();
            tvAppName.setText(appInfo.getAppName());
            imgAppIcon.setImageDrawable(appInfo.getIcon());

            // Prevent listener from firing while we set the initial checked state
            switchBlockApp.setOnCheckedChangeListener(null);
            switchBlockApp.setChecked(appInfo.isBlocked());

            switchBlockApp.setOnCheckedChangeListener((buttonView, isChecked) -> {
                appInfo.setBlocked(isChecked);
                BlockedAppsPrefs.setPackageBlocked(context, appInfo.getPackageName(), isChecked);
            });
        }
    }
}
