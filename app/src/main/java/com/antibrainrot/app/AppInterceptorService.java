package com.antibrainrot.app;

import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Accessibility service that detects when a blocked app (Instagram, TikTok)
 * comes to the foreground and shows a full-screen cognitive friction overlay.
 *
 * Enable in: Settings → Accessibility → Anti-Brain Rot
 */
public class AppInterceptorService extends AccessibilityService {

    /** Total friction time shown on the overlay (seconds). */
    private static final long COUNTDOWN_SECONDS = 60;

    /** Packages we intercept — must match accessibility_service_config.xml. */
    private static final Set<String> BLOCKED_PACKAGES = new HashSet<>(Arrays.asList(
            "com.instagram.android",
            "com.zhiliaoapp.musically"  // TikTok (international package name)
    ));

    private WindowManager windowManager;
    private View overlayView;
    private TextView countdownTextView;
    private CountDownTimer countDownTimer;
    private AlertDialog activeDialog;

    /** Remaining seconds on the 60s timer (updated every tick). */
    private int remainingSeconds = (int) COUNTDOWN_SECONDS;

    /** True while the overlay is attached to the screen. */
    private boolean overlayVisible = false;

    /** Which app triggered the overlay (e.g. com.instagram.android). */
    private String currentPackageName = "";

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }

        // Only react when the foreground window changes (app opened / switched).
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        CharSequence packageSequence = event.getPackageName();
        if (packageSequence == null) {
            return;
        }

        String packageName = packageSequence.toString();
        if (!BLOCKED_PACKAGES.contains(packageName)) {
            return;
        }

        // User passed the task and picked a temporary unblock — skip the overlay.
        if (InterceptPrefs.isTemporarilyUnblocked(this, packageName)) {
            return;
        }

        // TODO: Check lock_until SharedPreferences / Room here for stricter lock rules.

        currentPackageName = packageName;
        showOverlay();
    }

    @Override
    public void onInterrupt() {
        dismissActiveDialog();
        hideOverlay();
    }

    /**
     * Inflates and displays the full-screen overlay on top of the blocked app.
     */
    private void showOverlay() {
        if (overlayVisible || windowManager == null) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        overlayView = inflater.inflate(R.layout.interceptor_overlay, null);

        countdownTextView = overlayView.findViewById(R.id.countdown_timer);
        Button dismissButton = overlayView.findViewById(R.id.btn_dismiss);
        Button passTestButton = overlayView.findViewById(R.id.btn_pass_test);

        updateCountdownDisplay(remainingSeconds);
        startCountdown();

        dismissButton.setOnClickListener(v -> onDismissRequested());
        passTestButton.setOnClickListener(v -> onPassTestRequested());

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
    }

    /**
     * Starts (or restarts) the 60-second countdown and updates the on-screen timer.
     */
    private void startCountdown() {
        cancelCountdown();

        remainingSeconds = (int) COUNTDOWN_SECONDS;
        updateCountdownDisplay(remainingSeconds);

        countDownTimer = new CountDownTimer(COUNTDOWN_SECONDS * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingSeconds = (int) Math.ceil(millisUntilFinished / 1000.0);
                updateCountdownDisplay(remainingSeconds);
            }

            @Override
            public void onFinish() {
                remainingSeconds = 0;
                updateCountdownDisplay(0);
                // TODO: Decide what happens when the timer hits zero
                //       (e.g. require mini-game, auto-lock, or enable "Pass Test").
                Toast.makeText(AppInterceptorService.this,
                        "Time's up — complete the challenge or close the app.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        countDownTimer.start();
    }

    private void updateCountdownDisplay(int seconds) {
        if (countdownTextView != null) {
            countdownTextView.setText(String.valueOf(seconds));
        }
    }

    private void cancelCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    /**
     * User tapped "Don't want to use the app" — ask how long to lock the app.
     */
    private void onDismissRequested() {
        showLockDurationDialog();
    }

    /**
     * User tapped "Simulate Task Passed" — ask how long to temporarily unblock.
     */
    private void onPassTestRequested() {
        // TODO: Replace this with real mini-game validation from R.id.minigame_container.
        showUnblockDurationDialog();
    }

    /**
     * Dialog: lock the app for 30 / 60 / 90 / 120 minutes.
     */
    private void showLockDurationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_lock_duration, null);

        AlertDialog dialog = buildOverlayDialog(dialogView);
        wireLockButton(dialogView.findViewById(R.id.btn_lock_30), dialog, 30);
        wireLockButton(dialogView.findViewById(R.id.btn_lock_60), dialog, 60);
        wireLockButton(dialogView.findViewById(R.id.btn_lock_90), dialog, 90);
        wireLockButton(dialogView.findViewById(R.id.btn_lock_120), dialog, 120);

        activeDialog = dialog;
        dialog.show();
    }

    private void wireLockButton(Button button, AlertDialog dialog, int minutes) {
        button.setOnClickListener(v -> {
            dialog.dismiss();
            activeDialog = null;
            applyAppLock(minutes);
        });
    }

    private void applyAppLock(int minutes) {
        // TODO: Add points here — reward the user for choosing to stay off the app.
        InterceptPrefs.setAppLock(this, currentPackageName, minutes);

        hideOverlay();
        performGlobalAction(GLOBAL_ACTION_HOME);

        Toast.makeText(this,
                getString(R.string.toast_app_locked, minutes),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Dialog: temporarily unblock the app for 15 or 30 minutes after passing the task.
     */
    private void showUnblockDurationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_unblock_duration, null);

        AlertDialog dialog = buildOverlayDialog(dialogView);
        wireUnblockButton(dialogView.findViewById(R.id.btn_unblock_15), dialog, 15);
        wireUnblockButton(dialogView.findViewById(R.id.btn_unblock_30), dialog, 30);

        activeDialog = dialog;
        dialog.show();
    }

    private void wireUnblockButton(Button button, AlertDialog dialog, int minutes) {
        button.setOnClickListener(v -> {
            dialog.dismiss();
            activeDialog = null;
            applyTemporaryUnblock(minutes);
        });
    }

    private void applyTemporaryUnblock(int minutes) {
        // TODO: Add points here — deduct points / kill a plant in the farm when unblocking.
        Toast.makeText(this, R.string.toast_plant_died, Toast.LENGTH_LONG).show();

        InterceptPrefs.setTemporaryUnblock(this, currentPackageName, minutes);

        hideOverlay();
        // User stays in the blocked app — overlay is gone for the chosen duration.
    }

    /**
     * Builds an AlertDialog that can appear on top of other apps from this service.
     * TYPE_ACCESSIBILITY_OVERLAY is required when showing UI from an AccessibilityService.
     */
    private AlertDialog buildOverlayDialog(View contentView) {
        Context themedContext = new ContextThemeWrapper(this, R.style.Theme_AntiBrainRot_Dialog);

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

    /**
     * Removes the overlay from the screen and resets countdown state for next show.
     */
    private void hideOverlay() {
        cancelCountdown();
        dismissActiveDialog();

        if (overlayView != null && windowManager != null && overlayVisible) {
            try {
                windowManager.removeView(overlayView);
            } catch (IllegalArgumentException ignored) {
                // View was already detached.
            }
        }

        overlayView = null;
        countdownTextView = null;
        overlayVisible = false;
        remainingSeconds = (int) COUNTDOWN_SECONDS;
    }

    @Override
    public void onDestroy() {
        hideOverlay();
        super.onDestroy();
    }
}
