package com.focusfarm.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.focusfarm.app.service.AntiBrainRotService;
import com.focusfarm.app.ui.BlockAppFragment;
import com.focusfarm.app.ui.FarmFragment;
import com.focusfarm.app.ui.SleepFragment;
import com.focusfarm.app.ui.StatsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private AlertDialog accessibilityDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        findViewById(R.id.btn_profile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

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
        refreshVisibleFragment();

        if (isAccessibilityServiceEnabled(this, AntiBrainRotService.class)) {
            hideAccessibilityPermissionDialog();
        } else {
            showAccessibilityPermissionDialog();
        }
    }

    /** Re-binds Farm metrics when returning from overlay / profile. */
    public void refreshVisibleFragment() {
        Fragment current = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (current instanceof FarmFragment) {
            ((FarmFragment) current).refreshUi();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideAccessibilityPermissionDialog();
    }

    /**
     * Checks Settings.Secure for our exact accessibility component id.
     * This avoids false positives from AccessibilityManager on some devices.
     */
    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> serviceClass) {
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        if (enabledServices == null || enabledServices.isEmpty()) {
            return false;
        }

        String ourServiceId = context.getPackageName() + "/" + serviceClass.getName();
        String[] serviceIds = enabledServices.split(":");

        for (String serviceId : serviceIds) {
            if (ourServiceId.equals(serviceId.trim())) {
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

    private void hideAccessibilityPermissionDialog() {
        if (accessibilityDialog != null && accessibilityDialog.isShowing()) {
            accessibilityDialog.dismiss();
        }
        accessibilityDialog = null;
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /** Opens the Sleep tab from Farm metric rings or other entry points. */
    public void navigateToSleepTab() {
        bottomNavigation.setSelectedItemId(R.id.nav_sleep);
        showFragment(new SleepFragment());
    }
}
