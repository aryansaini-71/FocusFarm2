package com.focusfarm.app.service;

import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.focusfarm.app.R;
import com.focusfarm.app.data.SharedPrefsManager;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.Random;
import java.util.Set;

/**
 * Accessibility interceptor: random mini-game, leave (lock), or continue (temporary use).
 */
public class AntiBrainRotService extends AccessibilityService {

  /** Short durations for hackathon testing (seconds). */
  private static final int[] TEST_LOCK_SECONDS = {10, 30, 60};

  /** Breathing length while testing — raise to 60 for production. */
  private static final long BREATHING_DURATION_SECONDS = 10;
  private static final long BREATHING_PHASE_SECONDS = 4;

  private enum OverlayMode {
    NONE,
    BLOCK_COUNTDOWN,
    INTERVENTION
  }

  private final Random random = new Random();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  private WindowManager windowManager;
  private View overlayView;
  private OverlayMode overlayMode = OverlayMode.NONE;
  private boolean overlayVisible = false;
  private String currentPackageName = "";

  private LinearLayout layoutChooseTask;
  private TextView randomTaskMessageView;
  private LinearLayout layoutBreathing;
  private View layoutQuiz;
  private TextView breathingInstructionView;
  private TextView breathingSecondsView;
  private ProgressBar breathingProgressBar;
  private TextView blockCountdownView;

  private CountDownTimer breathingTimer;
  private CountDownTimer blockCountdownTimer;
  private AlertDialog activeDialog;

  @Override
  public void onServiceConnected() {
    super.onServiceConnected();
    windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
  }

  @Override
  public void onAccessibilityEvent(AccessibilityEvent event) {
    if (event == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
      return;
    }

    CharSequence packageSequence = event.getPackageName();
    if (packageSequence == null) {
      return;
    }

    String currentApp = packageSequence.toString();

    if (SharedPrefsManager.isAppLocked(this, currentApp)) {
      Set<String> blockedApps = SharedPrefsManager.getBlockedApps(this);
      if (!blockedApps.isEmpty() && blockedApps.contains(currentApp)) {
        currentPackageName = currentApp;
        showBlockCountdownOverlay(currentApp);
        return;
      }
    }

    Set<String> blockedApps = SharedPrefsManager.getBlockedApps(this);
    if (blockedApps.isEmpty() || !blockedApps.contains(currentApp)) {
      return;
    }

    if (SharedPrefsManager.isTemporarilyUnblocked(this, currentApp)) {
      return;
    }

    currentPackageName = currentApp;
    showInterventionOverlay(currentApp);
  }

  @Override
  public void onInterrupt() {
    dismissActiveDialog();
    hideOverlay();
  }

  // -------------------------------------------------------------------------
  // Block countdown (active lock — leave only)
  // -------------------------------------------------------------------------

  private void showBlockCountdownOverlay(String packageName) {
    if (overlayVisible) {
      if (overlayMode == OverlayMode.BLOCK_COUNTDOWN && blockCountdownView != null) {
        updateBlockCountdownText(blockCountdownView, packageName);
        startBlockCountdownTimer(blockCountdownView, packageName);
      }
      return;
    }

    WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
    LayoutInflater inflater = createOverlayInflater();
    if (wm == null || inflater == null) {
      return;
    }

    View overlayView = inflater.inflate(R.layout.block_countdown_overlay, null);
    TextView countdownText = overlayView.findViewById(R.id.tv_block_countdown);
    MaterialButton leaveButton = overlayView.findViewById(R.id.btn_leave);

    updateBlockCountdownText(countdownText, packageName);
    leaveButton.setOnClickListener(v -> {
      hideOverlay();
      performGlobalAction(GLOBAL_ACTION_HOME);
    });
    startBlockCountdownTimer(countdownText, packageName);

    try {
      wm.addView(overlayView, buildOverlayLayoutParams());
      this.overlayView = overlayView;
      this.blockCountdownView = countdownText;
      overlayMode = OverlayMode.BLOCK_COUNTDOWN;
      overlayVisible = true;
    } catch (Exception e) {
      cancelBlockCountdownTimer();
      this.overlayView = null;
      overlayVisible = false;
      overlayMode = OverlayMode.NONE;
    }
  }

  // -------------------------------------------------------------------------
  // Intervention overlay
  // -------------------------------------------------------------------------

  private void showInterventionOverlay(String packageName) {
    if (overlayVisible) {
      return;
    }

    WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
    LayoutInflater inflater = createOverlayInflater();
    if (wm == null || inflater == null) {
      return;
    }

    View overlayView = inflater.inflate(R.layout.interceptor_overlay, null);

    layoutChooseTask = overlayView.findViewById(R.id.layout_choose_task);
    randomTaskMessageView = overlayView.findViewById(R.id.tv_random_task_message);
    layoutBreathing = overlayView.findViewById(R.id.layout_breathing);
    layoutQuiz = overlayView.findViewById(R.id.layout_quiz);
    breathingInstructionView = overlayView.findViewById(R.id.tv_breathing_instruction);
    breathingSecondsView = overlayView.findViewById(R.id.tv_breathing_seconds);
    breathingProgressBar = overlayView.findViewById(R.id.progress_breathing);

    MaterialButton continueButton = overlayView.findViewById(R.id.btn_continue_app);
    MaterialButton leaveButton = overlayView.findViewById(R.id.btn_dismiss);
    MaterialButton mercuryButton = overlayView.findViewById(R.id.btn_answer_mercury);
    MaterialButton venusButton = overlayView.findViewById(R.id.btn_answer_venus);
    MaterialButton earthButton = overlayView.findViewById(R.id.btn_answer_earth);

    continueButton.setText(getString(R.string.btn_continue_with_app, getAppLabel(packageName)));
    continueButton.setOnClickListener(v -> showContinueWithAppDialog());
    leaveButton.setOnClickListener(v -> showLeaveBlockDialog());

    mercuryButton.setOnClickListener(v -> onQuizAnswer(true));
    venusButton.setOnClickListener(v -> onQuizAnswer(false));
    earthButton.setOnClickListener(v -> onQuizAnswer(false));

    try {
      wm.addView(overlayView, buildOverlayLayoutParams());
      this.overlayView = overlayView;
      overlayMode = OverlayMode.INTERVENTION;
      overlayVisible = true;
      startRandomTask();
    } catch (Exception e) {
      clearInterventionReferences();
      this.overlayView = null;
      overlayVisible = false;
      overlayMode = OverlayMode.NONE;
    }
  }

  /** Picks breathing or quiz at random — user does not choose. */
  private void startRandomTask() {
    hideAllTaskPanels();
    if (layoutChooseTask != null) {
      layoutChooseTask.setVisibility(View.VISIBLE);
    }
    if (randomTaskMessageView != null) {
      randomTaskMessageView.setText(R.string.random_task_loading);
    }

    boolean useBreathing = random.nextBoolean();
    mainHandler.postDelayed(() -> {
      if (!overlayVisible || overlayMode != OverlayMode.INTERVENTION) {
        return;
      }
      if (useBreathing) {
        if (randomTaskMessageView != null) {
          randomTaskMessageView.setText(R.string.random_task_breathing);
        }
        mainHandler.postDelayed(this::startBreathingExercise, 400);
      } else {
        if (randomTaskMessageView != null) {
          randomTaskMessageView.setText(R.string.random_task_quiz);
        }
        mainHandler.postDelayed(this::startQuiz, 400);
      }
    }, 500);
  }

  // -------------------------------------------------------------------------
  // Leave App → good decision → lock (10s / 30s / 1min for testing)
  // -------------------------------------------------------------------------

  private void showLeaveBlockDialog() {
    showDurationPickerDialog(
            R.string.leave_good_decision_title,
            R.string.leave_good_decision_message,
            this::applyBlockAndLeaveSeconds);
  }

  private void applyBlockAndLeaveSeconds(int seconds) {
    long blockEnd = System.currentTimeMillis() + (seconds * 1000L);
    SharedPrefsManager.setAppBlockEnd(this, currentPackageName, blockEnd);
    SharedPrefsManager.recordResistChoice(this);

    Toast.makeText(this, getString(R.string.toast_app_locked_seconds, seconds), Toast.LENGTH_SHORT).show();
    hideOverlay();
    performGlobalAction(GLOBAL_ACTION_HOME);
  }

  // -------------------------------------------------------------------------
  // Continue with app → health warning → temporary use
  // -------------------------------------------------------------------------

  private void showContinueWithAppDialog() {
    showDurationPickerDialog(
            R.string.continue_bad_title,
            R.string.continue_bad_message,
            this::applyContinueUseSeconds);
  }

  /**
   * Custom dialog with visible duration buttons (setItems + setNegativeButton hides choices on some devices).
   */
  private void showDurationPickerDialog(int titleRes, int messageRes, DurationChoiceListener listener) {
    LayoutInflater inflater = createOverlayInflater();
    View dialogView = inflater.inflate(R.layout.dialog_duration_choice, null);

    TextView titleView = dialogView.findViewById(R.id.tv_dialog_title);
    TextView messageView = dialogView.findViewById(R.id.tv_dialog_message);
    titleView.setText(titleRes);
    messageView.setText(messageRes);

    Context themedContext = new ContextThemeWrapper(this, R.style.Theme_FocusFarm_Dialog);
    AlertDialog dialog = new AlertDialog.Builder(themedContext)
            .setView(dialogView)
            .setCancelable(true)
            .create();

    dialogView.findViewById(R.id.btn_duration_10).setOnClickListener(v -> {
      dialog.dismiss();
      listener.onDurationChosen(TEST_LOCK_SECONDS[0]);
    });
    dialogView.findViewById(R.id.btn_duration_30).setOnClickListener(v -> {
      dialog.dismiss();
      listener.onDurationChosen(TEST_LOCK_SECONDS[1]);
    });
    dialogView.findViewById(R.id.btn_duration_60).setOnClickListener(v -> {
      dialog.dismiss();
      listener.onDurationChosen(TEST_LOCK_SECONDS[2]);
    });
    dialogView.findViewById(R.id.btn_duration_cancel).setOnClickListener(v -> dialog.dismiss());

    attachDialogToOverlay(dialog);
    activeDialog = dialog;
    dialog.show();
  }

  private interface DurationChoiceListener {
    void onDurationChosen(int seconds);
  }

  private void applyContinueUseSeconds(int seconds) {
    SharedPrefsManager.recordGiveInChoice(this);
    Toast.makeText(this, R.string.toast_plant_died, Toast.LENGTH_LONG).show();
    SharedPrefsManager.setTemporaryUnblockMs(this, currentPackageName, seconds * 1000L);
    Toast.makeText(this, getString(R.string.toast_use_app_seconds, seconds), Toast.LENGTH_SHORT).show();
    hideOverlay();
  }

  // -------------------------------------------------------------------------
  // Mini-games (auto-started)
  // -------------------------------------------------------------------------

  private void startBreathingExercise() {
    hideAllTaskPanels();
    if (layoutBreathing == null) {
      return;
    }
    layoutBreathing.setVisibility(View.VISIBLE);

    if (breathingProgressBar != null) {
      breathingProgressBar.setMax((int) BREATHING_DURATION_SECONDS);
      breathingProgressBar.setProgress(0);
    }
    updateBreathingPhase(0);

    cancelBreathingTimer();
    breathingTimer = new CountDownTimer(BREATHING_DURATION_SECONDS * 1000L, 1000L) {
      @Override
      public void onTick(long millisUntilFinished) {
        int secondsElapsed = (int) BREATHING_DURATION_SECONDS
                - (int) Math.ceil(millisUntilFinished / 1000.0);
        if (breathingProgressBar != null) {
          breathingProgressBar.setProgress(secondsElapsed);
        }
        if (breathingSecondsView != null) {
          breathingSecondsView.setText(formatCountdownMs(millisUntilFinished));
        }
        updateBreathingPhase(secondsElapsed);
      }

      @Override
      public void onFinish() {
        showTaskPassedDialog();
      }
    };
    breathingTimer.start();
  }

  private void updateBreathingPhase(int secondsElapsed) {
    if (breathingInstructionView == null) {
      return;
    }
    int phaseIndex = (secondsElapsed / (int) BREATHING_PHASE_SECONDS) % 3;
    switch (phaseIndex) {
      case 0:
        breathingInstructionView.setText(R.string.breathe_in);
        break;
      case 1:
        breathingInstructionView.setText(R.string.breathe_hold);
        break;
      default:
        breathingInstructionView.setText(R.string.breathe_out);
        break;
    }
  }

  private void startQuiz() {
    cancelBreathingTimer();
    hideAllTaskPanels();
    if (layoutQuiz != null) {
      layoutQuiz.setVisibility(View.VISIBLE);
    }
  }

  private void onQuizAnswer(boolean correct) {
    if (!correct) {
      Toast.makeText(this, R.string.quiz_wrong, Toast.LENGTH_SHORT).show();
      return;
    }
    showTaskPassedDialog();
  }

  /** After completing the random challenge — short unlock for testing. */
  private void showTaskPassedDialog() {
    cancelBreathingTimer();
    showDurationPickerDialog(
            R.string.task_passed_title,
            R.string.task_passed_message,
            seconds -> {
              SharedPrefsManager.recordResistChoice(this);
              SharedPrefsManager.setTemporaryUnblockMs(this, currentPackageName, seconds * 1000L);
              Toast.makeText(this, getString(R.string.toast_use_app_seconds, seconds), Toast.LENGTH_SHORT).show();
              hideOverlay();
            });
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private void hideAllTaskPanels() {
    if (layoutChooseTask != null) {
      layoutChooseTask.setVisibility(View.GONE);
    }
    if (layoutBreathing != null) {
      layoutBreathing.setVisibility(View.GONE);
    }
    if (layoutQuiz != null) {
      layoutQuiz.setVisibility(View.GONE);
    }
  }

  private String getAppLabel(String packageName) {
    try {
      PackageManager pm = getPackageManager();
      CharSequence label = pm.getApplicationLabel(
              pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
      return label != null ? label.toString() : packageName;
    } catch (PackageManager.NameNotFoundException e) {
      return packageName;
    }
  }

  private void startBlockCountdownTimer(TextView countdownText, String packageName) {
    cancelBlockCountdownTimer();
    long remainingMs = SharedPrefsManager.getLockEndTime(this, packageName) - System.currentTimeMillis();
    if (remainingMs <= 0L) {
      return;
    }

    blockCountdownTimer = new CountDownTimer(remainingMs, 1000L) {
      @Override
      public void onTick(long millisUntilFinished) {
        countdownText.setText(formatCountdownMs(millisUntilFinished));
      }

      @Override
      public void onFinish() {
        hideOverlay();
      }
    };
    blockCountdownTimer.start();
  }

  private void updateBlockCountdownText(TextView countdownText, String packageName) {
    long remainingMs = SharedPrefsManager.getLockEndTime(this, packageName) - System.currentTimeMillis();
    countdownText.setText(formatCountdownMs(remainingMs));
  }

  private static String formatCountdownMs(long millisUntilFinished) {
    long totalSeconds = Math.max(0L, millisUntilFinished / 1000L);
    long minutes = totalSeconds / 60L;
    long seconds = totalSeconds % 60L;
    return String.format(Locale.US, "%02d:%02d", minutes, seconds);
  }

  private LayoutInflater createOverlayInflater() {
    return LayoutInflater.from(new ContextThemeWrapper(this, R.style.Theme_FocusFarm_Overlay));
  }

  private WindowManager.LayoutParams buildOverlayLayoutParams() {
    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.graphics.PixelFormat.OPAQUE
    );
    params.gravity = Gravity.TOP | Gravity.START;
    return params;
  }

  private void attachDialogToOverlay(AlertDialog dialog) {
    Window window = dialog.getWindow();
    if (window != null) {
      window.setType(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);
    }
    dialog.setOnDismissListener(d -> activeDialog = null);
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

  private void cancelBlockCountdownTimer() {
    if (blockCountdownTimer != null) {
      blockCountdownTimer.cancel();
      blockCountdownTimer = null;
    }
  }

  private void hideOverlay() {
    mainHandler.removeCallbacksAndMessages(null);
    cancelBreathingTimer();
    cancelBlockCountdownTimer();
    dismissActiveDialog();

    if (overlayView != null && windowManager != null && overlayVisible) {
      try {
        windowManager.removeView(overlayView);
      } catch (IllegalArgumentException ignored) {
      }
    }

    overlayView = null;
    blockCountdownView = null;
    clearInterventionReferences();
    overlayVisible = false;
    overlayMode = OverlayMode.NONE;
  }

  private void clearInterventionReferences() {
    layoutChooseTask = null;
    randomTaskMessageView = null;
    layoutBreathing = null;
    layoutQuiz = null;
    breathingInstructionView = null;
    breathingSecondsView = null;
    breathingProgressBar = null;
  }

  @Override
  public void onDestroy() {
    hideOverlay();
    super.onDestroy();
  }
}
