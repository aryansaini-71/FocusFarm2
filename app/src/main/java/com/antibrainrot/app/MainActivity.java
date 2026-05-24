package com.antibrainrot.app;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/**
 * Main entry screen for Anti-Brain Rot.
 * Shows a bottom tab bar and swaps Fragments when the user taps a tab.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    /** Prevents stacking multiple accessibility dialogs if onResume fires again. */
    private AlertDialog accessibilityDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int selectedId = item.getItemId();

            if (selectedId == R.id.nav_farm) {
                showFragment(new FarmFragment());
                return true;
            } else if (selectedId == R.id.nav_blocker) {
                showFragment(new BlockerFragment());
                return true;
            } else if (selectedId == R.id.nav_sleep) {
                showFragment(new SleepTreeFragment());
                return true;
            } else if (selectedId == R.id.nav_stats) {
                showFragment(new StatsFragment());
                return true;
            }

            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_farm);
            showFragment(new FarmFragment());
        }
    }

    /**
     * Every time the user returns to the app, make sure accessibility is turned on.
     * The interceptor cannot block Instagram/TikTok without this permission.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityPermissionDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Clean up so the dialog can show again next time if still disabled.
        if (accessibilityDialog != null && accessibilityDialog.isShowing()) {
            accessibilityDialog.dismiss();
        }
        accessibilityDialog = null;
    }

    /**
     * Checks whether AppInterceptorService is enabled in system Accessibility settings.
     */
    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);

        if (accessibilityManager == null) {
            return false;
        }

        List<AccessibilityServiceInfo> enabledServices =
                accessibilityManager.getEnabledAccessibilityServiceList(
                        AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        String ourServiceId = getPackageName() + "/" + AppInterceptorService.class.getName();

        for (AccessibilityServiceInfo serviceInfo : enabledServices) {
            String enabledServiceId = serviceInfo.getId();
            if (ourServiceId.equals(enabledServiceId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Explains why we need accessibility and sends the user to Settings.
     */
    private void showAccessibilityPermissionDialog() {
        if (accessibilityDialog != null && accessibilityDialog.isShowing()) {
            return;
        }

        accessibilityDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.accessibility_dialog_title)
                .setMessage(R.string.accessibility_dialog_message)
                .setCancelable(false)
                .setPositiveButton(R.string.accessibility_dialog_open_settings, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.accessibility_dialog_later, (dialog, which) -> dialog.dismiss())
                .create();

        accessibilityDialog.show();
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
