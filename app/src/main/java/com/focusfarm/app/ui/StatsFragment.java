package com.focusfarm.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.focusfarm.app.R;
import com.focusfarm.app.components.DonutChartView;
import com.focusfarm.app.components.SleepBarChartView;
import com.focusfarm.app.data.SharedPrefsManager;
import com.focusfarm.app.data.StatsMockData;

/**
 * Statistics tab — sanctuary metrics, 7-day sleep detail, and weekly snapshot.
 * Uses {@link StatsMockData} where live analytics are not wired yet.
 */
public class StatsFragment extends Fragment {

    private DonutChartView resistanceDonut;
    private TextView resistancePercent;
    private TextView resistanceSummary;
    private TextView treeGrowthPct;
    private ProgressBar treeGrowthBar;
    private TextView breathingPct;
    private ProgressBar breathingBar;
    private TextView companionSpecies;
    private SleepBarChartView sleepBarChart;
    private TextView sleepSummary;
    private LinearLayout sleepDayRows;
    private TextView snapshotResists;
    private TextView snapshotSleepStreak;
    private TextView snapshotFarmHealth;
    private TextView snapshotPoints;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        resistanceDonut = view.findViewById(R.id.donut_resistance);
        resistancePercent = view.findViewById(R.id.tv_resistance_percent);
        resistanceSummary = view.findViewById(R.id.tv_resistance_summary);
        treeGrowthPct = view.findViewById(R.id.tv_tree_growth_pct);
        treeGrowthBar = view.findViewById(R.id.progress_tree_growth);
        breathingPct = view.findViewById(R.id.tv_breathing_pct);
        breathingBar = view.findViewById(R.id.progress_breathing);
        companionSpecies = view.findViewById(R.id.tv_companion_species);
        sleepBarChart = view.findViewById(R.id.sleep_bar_chart);
        sleepSummary = view.findViewById(R.id.tv_sleep_7day_summary);
        sleepDayRows = view.findViewById(R.id.layout_sleep_day_rows);
        snapshotResists = view.findViewById(R.id.tv_snapshot_resists);
        snapshotSleepStreak = view.findViewById(R.id.tv_snapshot_sleep_streak);
        snapshotFarmHealth = view.findViewById(R.id.tv_snapshot_farm_health);
        snapshotPoints = view.findViewById(R.id.tv_snapshot_points);
    }

    @Override
    public void onResume() {
        super.onResume();
        bindStatsUi();
    }

    private void bindStatsUi() {
        if (getContext() == null || resistanceDonut == null) {
            return;
        }

        bindResistanceIndex();
        bindSanctuaryProgress();
        bindSleepLastSevenDays();
        bindWeeklySnapshot();
    }

    /** Global Resistance Index — % of distractions resisted this week. */
    private void bindResistanceIndex() {
        int resisted = StatsMockData.RESISTANCE_PERCENT;
        int remaining = 100 - resisted;

        resistancePercent.setText(getString(R.string.stats_percent_format, resisted));
        resistanceSummary.setText(getString(R.string.stats_resistance_summary_format, resisted));

        resistanceDonut.setSegments(
                new float[]{resisted / 100f, remaining / 100f},
                new int[]{
                        getContext().getColor(R.color.moss),
                        getContext().getColor(R.color.stats_ring_track)
                });
    }

    /** Tree growth, breathing success, and active companion species. */
    private void bindSanctuaryProgress() {
        int treePct = StatsMockData.TREE_GROWTH_PERCENT;
        int breathingSuccess = StatsMockData.BREATHING_SUCCESS_PERCENT;

        // Blend mock data with real farm health for a connected feel
        int farmHealth = SharedPrefsManager.getFarmHealth(getContext());
        int treeDisplay = Math.round((treePct + farmHealth) / 2f);

        treeGrowthPct.setText(getString(R.string.stats_percent_format, treeDisplay));
        treeGrowthBar.setProgress(treeDisplay);
        breathingPct.setText(getString(R.string.stats_percent_format, breathingSuccess));
        breathingBar.setProgress(breathingSuccess);

        int treeLevel = SharedPrefsManager.getTreeLevel(getContext());
        String species = StatsMockData.companionSpeciesForTreeLevel(treeLevel);
        companionSpecies.setText(getString(R.string.stats_companion_species, species));
    }

    /** 7-day sleep bar chart + per-day detail rows (mock hours). */
    private void bindSleepLastSevenDays() {
        float[] hours = StatsMockData.SLEEP_HOURS_LAST_7_DAYS;
        String[] labels = StatsMockData.SLEEP_DAY_LABELS;
        float goal = StatsMockData.SLEEP_GOAL_HOURS;
        float avg = StatsMockData.averageSleepHours();
        int nightsAtGoal = StatsMockData.nightsAtGoal();

        sleepSummary.setText(getString(
                R.string.stats_sleep_7day_summary,
                avg,
                goal,
                nightsAtGoal));

        sleepBarChart.setSleepData(hours, labels, goal);
        populateSleepDayRows(hours, labels, goal);
    }

    private void populateSleepDayRows(float[] hours, String[] labels, float goal) {
        sleepDayRows.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (int i = 0; i < hours.length; i++) {
            View row = inflater.inflate(R.layout.item_sleep_day_stat, sleepDayRows, false);

            TextView dayView = row.findViewById(R.id.tv_sleep_day);
            ProgressBar bar = row.findViewById(R.id.progress_sleep_day);
            TextView hoursView = row.findViewById(R.id.tv_sleep_hours);
            TextView qualityView = row.findViewById(R.id.tv_sleep_quality);

            dayView.setText(labels[i]);
            int progress = Math.min(100, Math.round((hours[i] / goal) * 100f));
            bar.setProgress(progress);
            hoursView.setText(getString(R.string.stats_sleep_hours_short, hours[i]));

            boolean metGoal = hours[i] >= goal * 0.85f;
            qualityView.setText(metGoal
                    ? R.string.stats_sleep_quality_good
                    : R.string.stats_sleep_quality_low);

            sleepDayRows.addView(row);
        }
    }

    /** Compact weekly stats pulled from SharedPreferences + mock data. */
    private void bindWeeklySnapshot() {
        int resists = SharedPrefsManager.getResistTotal(getContext());
        int sleepStreak = SharedPrefsManager.getSleepStreak(getContext());
        int health = SharedPrefsManager.getFarmHealth(getContext());
        int points = SharedPrefsManager.getFarmPoints(getContext());
        int focusStreak = SharedPrefsManager.getStreak(getContext());

        snapshotResists.setText(getString(R.string.stats_snapshot_resists, resists));
        snapshotSleepStreak.setText(getString(R.string.stats_snapshot_sleep_streak, sleepStreak));
        snapshotFarmHealth.setText(getString(
                R.string.stats_snapshot_farm_health,
                health,
                focusStreak));
        snapshotPoints.setText(getString(R.string.stats_snapshot_points, points));
    }
}
