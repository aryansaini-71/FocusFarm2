package com.antibrainrot.app.ui.farm;

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
 * Farm tab — displays points and plant count from {@link GameDataManager}.
 */
public class FarmFragment extends Fragment {

    private TextView tvTotalPoints;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_farm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvTotalPoints = view.findViewById(R.id.tv_total_points);
        refreshStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStats();
    }

    private void refreshStats() {
        if (tvTotalPoints == null || getContext() == null) {
            return;
        }
        int points = GameDataManager.getTotalPoints(requireContext());
        int plants = GameDataManager.getPlantsAlive(requireContext());
        tvTotalPoints.setText(getString(R.string.farm_stats_format, points, plants));
    }
}
