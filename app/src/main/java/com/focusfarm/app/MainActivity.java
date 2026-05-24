package com.focusfarm.app;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.focusfarm.app.service.FocusAccessibilityService;
import com.focusfarm.app.ui.BlockAppFragment;
import com.focusfarm.app.ui.FarmFragment;
import com.focusfarm.app.ui.SleepFragment;
import com.focusfarm.app.ui.StatsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

/**
 * Main entry: bottom tab bar hosting Farm, Block Apps, Sleep, and Stats.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
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
            } else if (selectedId == R.id.nav_block_app) {
                showFragment(new BlockAppFragment());
                return true;
            } else if (selectedId == R.id.nav_sleep) {
                showFragment(new SleepFragment());
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
        if (accessibilityDialog != null && accessibilityDialog.isShowing()) {
            accessibilityDialog.dismiss();
        }
        accessibilityDialog = null;
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);

        if (accessibilityManager == null) {
            return false;
        }

        List<AccessibilityServiceInfo> enabledServices =
                accessibilityManager.getEnabledAccessibilityServiceList(
                        AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        String ourServiceId = getPackageName() + "/" + FocusAccessibilityService.class.getName();

        for (AccessibilityServiceInfo serviceInfo : enabledServices) {
            if (ourServiceId.equals(serviceInfo.getId())) {
                return true;
            }
        }

        return false;
    }

    private void showAccessibilityPermissionDialog() {
        if (accessibilityDialog != null && accessibilityDialog.isShowing()) {
            return;
        }

        accessibilityDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.accessibility_dialog_title)
                .setMessage(R.string.accessibility_dialog_message)
                .setCancelable(false)
                .setPositiveButton(R.string.accessibility_dialog_open_settings, (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
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
