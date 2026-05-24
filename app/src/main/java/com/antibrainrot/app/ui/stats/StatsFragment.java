package com.antibrainrot.app.ui.stats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.antibrainrot.app.R;
import com.antibrainrot.app.data.GameDataManager;

/**
 * Stats tab — shows live totals from {@link GameDataManager}.
 */
public class StatsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        View root = getView();
        if (root == null || getContext() == null) {
            return;
        }
        TextView stats = root.findViewById(R.id.tv_weekly_stats);
        if (stats != null) {
            int points = GameDataManager.getTotalPoints(requireContext());
            stats.setText(getString(R.string.stats_points_format, points));
        }
    }
}
