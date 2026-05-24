package com.focusfarm.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.focusfarm.app.R;
import com.focusfarm.app.data.AppInfo;
import com.focusfarm.app.data.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppViewHolder> {

    private final List<AppInfo> apps = new ArrayList<>();
    private final Context context;

    public AppListAdapter(Context context) {
        this.context = context.getApplicationContext();
    }

    public void setApps(List<AppInfo> items) {
        apps.clear();
        if (items != null) {
            apps.addAll(items);
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

    class AppViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iconView;
        private final TextView nameView;
        private final SwitchCompat blockSwitch;
        private AppInfo boundApp;

        AppViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.img_app_icon);
            nameView = itemView.findViewById(R.id.tv_app_name);
            blockSwitch = itemView.findViewById(R.id.switch_block_app);
        }

        void bind(AppInfo app) {
            boundApp = app;
            iconView.setImageDrawable(app.getIcon());
            nameView.setText(app.getAppName());

            blockSwitch.setOnCheckedChangeListener(null);
            blockSwitch.setChecked(app.isBlocked());
            blockSwitch.setOnCheckedChangeListener(this::onSwitchChanged);
        }

        private void onSwitchChanged(CompoundButton button, boolean isChecked) {
            if (boundApp == null || boundApp.isBlocked() == isChecked) {
                return;
            }
            boundApp.setBlocked(isChecked);
            SharedPrefsManager.setAppBlocked(context, boundApp.getPackageName(), isChecked);
        }
    }
}
