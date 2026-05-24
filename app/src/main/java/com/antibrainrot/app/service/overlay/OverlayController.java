package com.antibrainrot.app.service.overlay;

import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.antibrainrot.app.R;
import com.antibrainrot.app.data.GameDataManager;
import com.antibrainrot.app.data.InterceptPrefs;

/**
 * Owns the WindowManager overlay, mini-games, and lock/unblock dialogs.
 * AppInterceptorService delegates all UI work here.
 */
public class OverlayController {

    private static final long BREATHING_DURATION_MS = 60_000L;
    private static final int PHASE_DURATION_SECONDS = 4;
    private static final long OVERLAY_COOLDOWN_MS = 3_000L;

    private final AccessibilityService service;
    private final WindowManager windowManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private View overlayView;
    private AlertDialog activeDialog;
    private CountDownTimer breathingTimer;

    private LinearLayout layoutChooseTask;
    private LinearLayout layoutBreathing;
    private LinearLayout layoutQuiz;
    private TextView textBreatheInstruction;
    private TextView textBreatheTimer;
    private ProgressBar progressBreathing;

    private boolean overlayVisible = false;
    private String currentPackageName = "";

    public OverlayController(AccessibilityService service) {
        this.service = service;
        this.windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
    }

    public boolean isShowing() {
        return overlayVisible;
    }

    public void show(String packageName) {
        if (overlayVisible || windowManager == null) {
            return;
        }

        currentPackageName = packageName;

        try {
            LayoutInflater inflater = LayoutInflater.from(service);
            overlayView = inflater.inflate(R.layout.interceptor_overlay, null);

            bindOverlayViews(overlayView);
            showChooseTaskScreen();

            Button dismissButton = overlayView.findViewById(R.id.btn_dismiss);
            dismissButton.setOnClickListener(v -> onDismissRequested());

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    android.graphics.PixelFormat.OPAQUE
            );
            params.gravity = Gravity.TOP | Gravity.START;

            windowManager.addView(overlayView, params);
            overlayVisible = true;
        } catch (Exception e) {
            overlayView = null;
            overlayVisible = false;
        }
    }

    public void hide() {
        cancelBreathingTimer();
        mainHandler.removeCallbacksAndMessages(null);
        dismissActiveDialog();

        if (overlayView != null && windowManager != null && overlayVisible) {
            try {
                windowManager.removeView(overlayView);
            } catch (IllegalArgumentException ignored) {
                // Already removed.
            }
        }

        clearViewReferences();
        overlayVisible = false;
    }

    private void clearViewReferences() {
        overlayView = null;
        layoutChooseTask = null;
        layoutBreathing = null;
        layoutQuiz = null;
        textBreatheInstruction = null;
        textBreatheTimer = null;
        progressBreathing = null;
    }

    private void bindOverlayViews(View root) {
        layoutChooseTask = root.findViewById(R.id.layout_choose_task);
        layoutBreathing = root.findViewById(R.id.layout_breathing);
        layoutQuiz = root.findViewById(R.id.layout_quiz);
        textBreatheInstruction = root.findViewById(R.id.text_breathe_instruction);
        textBreatheTimer = root.findViewById(R.id.text_breathe_timer);
        progressBreathing = root.findViewById(R.id.progress_breathing);

        root.findViewById(R.id.btn_breathing).setOnClickListener(v -> startBreathingExercise());
        root.findViewById(R.id.btn_quiz).setOnClickListener(v -> startQuiz());
        root.findViewById(R.id.btn_answer_mercury).setOnClickListener(v -> onQuizAnswer(true));
        root.findViewById(R.id.btn_answer_venus).setOnClickListener(v -> onQuizAnswer(false));
        root.findViewById(R.id.btn_answer_earth).setOnClickListener(v -> onQuizAnswer(false));
    }

    private void showChooseTaskScreen() {
        cancelBreathingTimer();
        setScreenVisible(layoutChooseTask);
    }

    private void startBreathingExercise() {
        setScreenVisible(layoutBreathing);
        applyBreathingTick(0);
        if (progressBreathing != null) {
            progressBreathing.setMax(60);
            progressBreathing.setProgress(0);
        }
        startBreathingTimer();
    }

    private void startQuiz() {
        cancelBreathingTimer();
        setScreenVisible(layoutQuiz);
    }

    private void setScreenVisible(LinearLayout visibleLayout) {
        if (layoutChooseTask == null || layoutBreathing == null || layoutQuiz == null) {
            return;
        }

        layoutChooseTask.setVisibility(
                visibleLayout == layoutChooseTask ? View.VISIBLE : View.GONE);
        layoutBreathing.setVisibility(
                visibleLayout == layoutBreathing ? View.VISIBLE : View.GONE);
        layoutQuiz.setVisibility(
                visibleLayout == layoutQuiz ? View.VISIBLE : View.GONE);
    }

    private void startBreathingTimer() {
        cancelBreathingTimer();

        breathingTimer = new CountDownTimer(BREATHING_DURATION_MS, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (!overlayVisible) {
                    return;
                }
                int secondsElapsed = 60 - (int) Math.ceil(millisUntilFinished / 1000.0);
                runOnOverlay(() -> applyBreathingTick(secondsElapsed));
            }

            @Override
            public void onFinish() {
                if (!overlayVisible) {
                    return;
                }
                runOnOverlay(() -> {
                    cancelBreathingTimer();
                    onTaskPassed();
                });
            }
        };
        breathingTimer.start();
    }

    private void applyBreathingTick(int secondsElapsed) {
        if (progressBreathing != null) {
            progressBreathing.setProgress(secondsElapsed);
        }
        updateBreathingUi(secondsElapsed);
    }

    private void updateBreathingUi(int secondsElapsed) {
        if (textBreatheInstruction == null || textBreatheTimer == null) {
            return;
        }

        int secondInCycle = secondsElapsed % 12;
        int phaseIndex = secondInCycle / PHASE_DURATION_SECONDS;
        int secondInPhase = secondInCycle % PHASE_DURATION_SECONDS;
        int phaseSecondsRemaining = PHASE_DURATION_SECONDS - secondInPhase;

        if (phaseIndex == 0) {
            textBreatheInstruction.setText(R.string.breathe_in);
        } else if (phaseIndex == 1) {
            textBreatheInstruction.setText(R.string.breathe_hold);
        } else {
            textBreatheInstruction.setText(R.string.breathe_out);
        }

        textBreatheTimer.setText(String.valueOf(phaseSecondsRemaining));
    }

    private void onQuizAnswer(boolean correct) {
        if (!overlayVisible) {
            return;
        }

        if (correct) {
            onTaskPassed();
        } else {
            Toast.makeText(service, R.string.quiz_wrong_answer, Toast.LENGTH_SHORT).show();
        }
    }

    private void onTaskPassed() {
        cancelBreathingTimer();
        showUnblockDurationDialog();
    }

    private void onDismissRequested() {
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        InterceptPrefs.setOverlayCooldown(service, currentPackageName, OVERLAY_COOLDOWN_MS);
        showLockDurationDialog();
    }

    private void showLockDurationDialog() {
        showDurationPickerDialog(
                R.layout.dialog_lock_duration,
                new int[]{R.id.btn_lock_30, R.id.btn_lock_60, R.id.btn_lock_90, R.id.btn_lock_120},
                new int[]{30, 60, 90, 120},
                this::applyAppLock
        );
    }

    private void showUnblockDurationDialog() {
        showDurationPickerDialog(
                R.layout.dialog_unblock_duration,
                new int[]{R.id.btn_unblock_15, R.id.btn_unblock_30},
                new int[]{15, 30},
                this::applyTemporaryUnblock
        );
    }

    private interface DurationChoiceListener {
        void onChosen(int minutes);
    }

    private void showDurationPickerDialog(int layoutRes, int[] buttonIds, int[] minutes,
                                          DurationChoiceListener listener) {
        View dialogView = LayoutInflater.from(service).inflate(layoutRes, null);
        AlertDialog dialog = buildOverlayDialog(dialogView);

        for (int i = 0; i < buttonIds.length; i++) {
            int minuteValue = minutes[i];
            Button button = dialogView.findViewById(buttonIds[i]);
            button.setOnClickListener(v -> {
                dialog.dismiss();
                activeDialog = null;
                listener.onChosen(minuteValue);
            });
        }

        activeDialog = dialog;
        dialog.show();
    }

    private void applyAppLock(int minutes) {
        InterceptPrefs.setAppLock(service, currentPackageName, minutes);
        InterceptPrefs.setOverlayCooldown(service, currentPackageName, OVERLAY_COOLDOWN_MS);
        GameDataManager.onUserLockedApp(service, minutes);

        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        hide();

        Toast.makeText(service,
                service.getString(R.string.toast_app_locked, minutes),
                Toast.LENGTH_SHORT).show();
    }

    private void applyTemporaryUnblock(int minutes) {
        GameDataManager.onUserUnblockedApp(service);
        Toast.makeText(service, R.string.toast_plant_died, Toast.LENGTH_LONG).show();

        InterceptPrefs.setTemporaryUnblock(service, currentPackageName, minutes);
        hide();
    }

    private AlertDialog buildOverlayDialog(View contentView) {
        Context themedContext = new ContextThemeWrapper(service, R.style.Theme_AntiBrainRot_Dialog);

        AlertDialog dialog = new AlertDialog.Builder(themedContext)
                .setView(contentView)
                .setCancelable(true)
                .create();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);
        }

        dialog.setOnDismissListener(d -> activeDialog = null);
        return dialog;
    }

    private void dismissActiveDialog() {
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
        }
        activeDialog = null;
    }

    private void cancelBreathingTimer() {
        if (breathingTimer != null) {
            breathingTimer.cancel();
            breathingTimer = null;
        }
    }

    private void runOnOverlay(Runnable action) {
        mainHandler.post(() -> {
            if (overlayVisible && overlayView != null) {
                try {
                    action.run();
                } catch (Exception ignored) {
                    // Prevent view errors from crashing the accessibility service.
                }
            }
        });
    }
}
