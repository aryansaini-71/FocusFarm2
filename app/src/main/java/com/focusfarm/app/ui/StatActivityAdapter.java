package com.focusfarm.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.focusfarm.app.R;
import com.focusfarm.app.data.StatActivityItem;

import java.util.ArrayList;
import java.util.List;

/** Adapter for the Statistics recent-activity list. */
public class StatActivityAdapter extends RecyclerView.Adapter<StatActivityAdapter.ViewHolder> {

    private final List<StatActivityItem> items = new ArrayList<>();

    public void setItems(List<StatActivityItem> activities) {
        items.clear();
        if (activities != null) {
            items.addAll(activities);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stat_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iconView;
        private final TextView titleView;
        private final TextView metaView;
        private final TextView pointsView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.img_activity_icon);
            titleView = itemView.findViewById(R.id.tv_activity_title);
            metaView = itemView.findViewById(R.id.tv_activity_meta);
            pointsView = itemView.findViewById(R.id.tv_activity_points);
        }

        void bind(StatActivityItem item) {
            iconView.setImageResource(item.getIconResId());
            titleView.setText(item.getTitle());
            metaView.setText(item.getTimeAgo());
            pointsView.setText(item.getPointsLabel());
        }
    }
}
