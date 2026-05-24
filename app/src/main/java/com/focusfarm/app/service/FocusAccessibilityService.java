package com.focusfarm.app.service;

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

import com.focusfarm.app.R;
import com.focusfarm.app.data.SharedPrefsManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Detects blocked apps in the foreground and shows the intervention overlay.
 */
public class FocusAccessibilityService extends AccessibilityService {

    private static final long COUNTDOWN_SECONDS = 60;

    private static final Set<String> BLOCKED_PACKAGES = new HashSet<>(Arrays.asList(
            "com.instagram.android",
            "com.zhiliaoapp.musically"
    ));

    private WindowManager windowManager;
    private View overlayView;
    private TextView countdownTextView;
    private CountDownTimer countDownTimer;
    private AlertDialog activeDialog;

    private int remainingSeconds = (int) COUNTDOWN_SECONDS;
    private boolean overlayVisible = false;
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

        if (SharedPrefsManager.isTemporarilyUnblocked(this, packageName)) {
            return;
        }

        currentPackageName = packageName;
        showOverlay();
    }

    @Override
    public void onInterrupt() {
        dismissActiveDialog();
        hideOverlay();
    }

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
                Toast.makeText(FocusAccessibilityService.this,
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

    private void onDismissRequested() {
        showLockDurationDialog();
    }

    private void onPassTestRequested() {
        showUnblockDurationDialog();
    }

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
        SharedPrefsManager.setAppLock(this, currentPackageName, minutes);
        hideOverlay();
        performGlobalAction(GLOBAL_ACTION_HOME);
        Toast.makeText(this,
                getString(R.string.toast_app_locked, minutes),
                Toast.LENGTH_SHORT).show();
    }

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
        Toast.makeText(this, R.string.toast_plant_died, Toast.LENGTH_LONG).show();
        SharedPrefsManager.setTemporaryUnblock(this, currentPackageName, minutes);
        hideOverlay();
    }

    private AlertDialog buildOverlayDialog(View contentView) {
        Context themedContext = new ContextThemeWrapper(this, R.style.Theme_FocusFarm_Dialog);

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

    private void hideOverlay() {
        cancelCountdown();
        dismissActiveDialog();

        if (overlayView != null && windowManager != null && overlayVisible) {
            try {
                windowManager.removeView(overlayView);
            } catch (IllegalArgumentException ignored) {
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
