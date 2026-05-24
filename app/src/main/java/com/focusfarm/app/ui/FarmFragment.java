package com.focusfarm.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.focusfarm.app.MainActivity;
import com.focusfarm.app.R;
import com.focusfarm.app.components.MetricRingView;
import com.focusfarm.app.components.TreeView;
import com.focusfarm.app.data.SharedPrefsManager;

import java.util.Locale;

public class FarmFragment extends Fragment {

    private ImageView imgFarmBase;
    private TreeView farmTree;
    private TextView treeStage;
    private MetricRingView ringHealth;
    private MetricRingView ringStreak;
    private MetricRingView ringSleep;

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
        imgFarmBase = view.findViewById(R.id.img_farm_base);
        farmTree = view.findViewById(R.id.farm_tree);
        treeStage = view.findViewById(R.id.tv_tree_stage);
        ringHealth = view.findViewById(R.id.ring_health);
        ringStreak = view.findViewById(R.id.ring_streak);
        ringSleep = view.findViewById(R.id.ring_sleep);

        ringSleep.setClickable(true);
        ringSleep.setFocusable(true);
        ringSleep.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToSleepTab();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        bindFarmUi();
    }

    public void refreshUi() {
        bindFarmUi();
    }

    private void bindFarmUi() {
        if (getContext() == null || imgFarmBase == null || farmTree == null || treeStage == null) {
            return;
        }

        int level = SharedPrefsManager.getTreeLevel(getContext());

        imgFarmBase.setVisibility(View.VISIBLE);
        imgFarmBase.setImageResource(R.drawable.farm_base);
        farmTree.setVisibility(View.VISIBLE);
        farmTree.bindFromPrefs(getContext());
        treeStage.setVisibility(View.VISIBLE);
        treeStage.setText(getString(R.string.tree_stage_format, level));

        int health = SharedPrefsManager.getFarmHealth(getContext());
        int streak = SharedPrefsManager.getStreak(getContext());
        float sleep = SharedPrefsManager.getAvgSleepHours(getContext());
        float sleepGoal = SharedPrefsManager.getSleepGoalHours(getContext());

        ringHealth.setMetric(
                getString(R.string.metric_farm_health),
                health + "%",
                "",
                SharedPrefsManager.getFarmHealthProgress(getContext()),
                ContextCompat.getColor(getContext(), R.color.metric_ring_health));

        ringStreak.setMetric(
                getString(R.string.metric_streak),
                streak + "d",
                "",
                SharedPrefsManager.getStreakProgress(getContext()),
                ContextCompat.getColor(getContext(), R.color.metric_ring_streak));

        ringSleep.setMetric(
                getString(R.string.metric_avg_sleep),
                String.format(Locale.US, "%.1f", sleep),
                String.format(Locale.US, "/%.0fh", sleepGoal),
                SharedPrefsManager.getSleepProgress(getContext()),
                ContextCompat.getColor(getContext(), R.color.metric_ring_sleep));
    }
}
