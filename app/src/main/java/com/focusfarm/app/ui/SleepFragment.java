package com.focusfarm.app.ui;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.focusfarm.app.R;
import com.focusfarm.app.components.FallingLeavesOverlayView;
import com.focusfarm.app.data.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SleepFragment extends Fragment {

    private TextView consecutiveDaysView;
    private View dotDay1;
    private View dotDay2;
    private View lineDay1Day2;
    private View lineDay2Reward;
    private FrameLayout dotRewardContainer;
    private ImageView rewardGiftIcon;
    private MaterialButton logSleepButton;
    private TextView resetStreakLink;
    private TextView detailLastLog;
    private TextView detailMilestone;
    private TextView detailPlants;
    private TextView detailAvgSleep;
    private TextView detailGoal;
    private TextView detailStatus;
    private FallingLeavesOverlayView fallingLeavesOverlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sleep, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        consecutiveDaysView = view.findViewById(R.id.tv_consecutive_days);
        dotDay1 = view.findViewById(R.id.dot_day1);
        dotDay2 = view.findViewById(R.id.dot_day2);
        lineDay1Day2 = view.findViewById(R.id.line_day1_day2);
        lineDay2Reward = view.findViewById(R.id.line_day2_reward);
        dotRewardContainer = view.findViewById(R.id.dot_reward_container);
        rewardGiftIcon = view.findViewById(R.id.img_reward_gift);
        logSleepButton = view.findViewById(R.id.btn_log_sleep);
        resetStreakLink = view.findViewById(R.id.tv_reset_streak);
        detailLastLog = view.findViewById(R.id.tv_detail_last_log);
        detailMilestone = view.findViewById(R.id.tv_detail_milestone);
        detailPlants = view.findViewById(R.id.tv_detail_plants);
        detailAvgSleep = view.findViewById(R.id.tv_detail_avg_sleep);
        detailGoal = view.findViewById(R.id.tv_detail_goal);
        detailStatus = view.findViewById(R.id.tv_detail_status);
        fallingLeavesOverlay = view.findViewById(R.id.falling_leaves_overlay);
        setupFallingLeavesOverlay();

        logSleepButton.setOnClickListener(v -> onLogSleepClicked());
        resetStreakLink.setOnClickListener(v -> onResetStreakClicked());
    }

    @Override
    public void onResume() {
        super.onResume();
        bindSleepUi();
        if (fallingLeavesOverlay != null) {
            fallingLeavesOverlay.resumeAnimation();
        }
    }

    @Override
    public void onPause() {
        if (fallingLeavesOverlay != null) {
            fallingLeavesOverlay.pauseAnimation();
        }
        super.onPause();
    }

    private void setupFallingLeavesOverlay() {
        if (fallingLeavesOverlay == null) {
            return;
        }
        // Slower multiplier = calmer drift; raise toward 1.2f for a busier shower
        fallingLeavesOverlay.setFallSpeed(0.75f);
    }

    private void onLogSleepClicked() {
        if (getContext() == null) {
            return;
        }

        if (SharedPrefsManager.hasLoggedSleepToday(getContext())) {
            Toast.makeText(getContext(), R.string.sleep_already_logged, Toast.LENGTH_SHORT).show();
            return;
        }

        int streakBefore = SharedPrefsManager.getSleepStreak(getContext());
        boolean logged = SharedPrefsManager.logRestfulSleepNight(getContext());
        if (!logged) {
            return;
        }

        int streakAfter = SharedPrefsManager.getSleepStreak(getContext());
        boolean earnedPlant = streakAfter > streakBefore
                && streakAfter % SharedPrefsManager.SLEEP_MILESTONE_DAYS == 0;

        if (earnedPlant) {
            Toast.makeText(getContext(), R.string.sleep_plant_earned, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), R.string.sleep_logged_success, Toast.LENGTH_SHORT).show();
        }

        bindSleepUi();
    }

    private void onResetStreakClicked() {
        if (getContext() == null) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.sleep_reset_confirm_title)
                .setMessage(R.string.sleep_reset_confirm_message)
                .setPositiveButton(R.string.btn_confirm, (dialog, which) -> {
                    SharedPrefsManager.resetSleepStreak(getContext());
                    Toast.makeText(getContext(), R.string.sleep_streak_reset, Toast.LENGTH_SHORT).show();
                    bindSleepUi();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void bindSleepUi() {
        if (getContext() == null) {
            return;
        }

        int streak = SharedPrefsManager.getSleepStreak(getContext());
        int milestoneProgress = SharedPrefsManager.getSleepMilestoneProgress(getContext());
        boolean loggedToday = SharedPrefsManager.hasLoggedSleepToday(getContext());

        consecutiveDaysView.setText(String.valueOf(streak));
        updateMilestoneDots(milestoneProgress);
        updateLogButton(loggedToday);
        bindDetailPanel(streak, milestoneProgress, loggedToday);
    }

    private void updateMilestoneDots(int milestoneProgress) {
        if (getContext() == null) {
            return;
        }

        dotDay1.setBackgroundResource(milestoneProgress >= 1
                ? R.drawable.bg_milestone_dot_filled
                : R.drawable.bg_milestone_dot_empty);

        dotDay2.setBackgroundResource(milestoneProgress >= 2
                ? R.drawable.bg_milestone_dot_filled
                : R.drawable.bg_milestone_dot_empty);

        int lineColorActive = ContextCompat.getColor(getContext(), R.color.moss);
        int lineColorInactive = ContextCompat.getColor(getContext(), R.color.sleep_milestone_line);

        lineDay1Day2.setBackgroundColor(milestoneProgress >= 2 ? lineColorActive : lineColorInactive);
        lineDay2Reward.setBackgroundColor(milestoneProgress >= 3 ? lineColorActive : lineColorInactive);

        boolean rewardReady = milestoneProgress >= 3;
        dotRewardContainer.setBackgroundResource(rewardReady
                ? R.drawable.bg_milestone_dot_reward
                : R.drawable.bg_milestone_dot_empty);
        rewardGiftIcon.setAlpha(rewardReady ? 1f : 0.35f);
    }

    private void updateLogButton(boolean loggedToday) {
        logSleepButton.setEnabled(!loggedToday);
        logSleepButton.setText(loggedToday
                ? R.string.sleep_log_button_done
                : R.string.sleep_log_button);
        logSleepButton.setAlpha(loggedToday ? 0.65f : 1f);
    }

    private void bindDetailPanel(int streak, int milestoneProgress, boolean loggedToday) {
        String lastLogDay = SharedPrefsManager.getLastSleepLogDay(getContext());
        detailLastLog.setText(getString(
                R.string.sleep_detail_last_log,
                formatLogDay(lastLogDay)));

        detailMilestone.setText(getString(
                R.string.sleep_detail_milestone,
                milestoneProgress,
                SharedPrefsManager.SLEEP_MILESTONE_DAYS));

        detailPlants.setText(getString(
                R.string.sleep_detail_plants,
                SharedPrefsManager.getSleepPlantsEarned(getContext())));

        float avgSleep = SharedPrefsManager.getAvgSleepHours(getContext());
        float goal = SharedPrefsManager.getSleepGoalHours(getContext());
        detailAvgSleep.setText(getString(R.string.sleep_detail_avg, avgSleep));
        detailGoal.setText(getString(R.string.sleep_detail_goal, goal));

        if (loggedToday) {
            detailStatus.setText(R.string.sleep_detail_status_logged);
        } else if (streak > 0) {
            detailStatus.setText(R.string.sleep_detail_status_pending);
        } else {
            detailStatus.setText(R.string.sleep_detail_status_start);
        }
    }

    private String formatLogDay(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.isEmpty()) {
            return getString(R.string.sleep_detail_never);
        }
        try {
            SimpleDateFormat stored = new SimpleDateFormat("yyyyMMdd", Locale.US);
            Date date = stored.parse(yyyyMmDd);
            if (date == null) {
                return yyyyMmDd;
            }
            return DateFormat.getMediumDateFormat(requireContext()).format(date);
        } catch (ParseException e) {
            return yyyyMmDd;
        }
    }
}
