package com.antibrainrot.app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.antibrainrot.app.R;
import com.antibrainrot.app.service.AppInterceptorService;
import com.antibrainrot.app.ui.blocker.BlockerFragment;
import com.antibrainrot.app.ui.farm.FarmFragment;
import com.antibrainrot.app.ui.sleep.SleepTreeFragment;
import com.antibrainrot.app.ui.stats.StatsFragment;
import com.antibrainrot.app.util.AccessibilityUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

/**
 * Host activity with bottom navigation. Retains one instance per tab.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private AlertDialog accessibilityDialog;

    private final Map<Integer, Fragment> tabFragments = new HashMap<>();
    private int activeTabId = R.id.nav_farm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            bottomNavigation.setSelectedItemId(R.id.nav_farm);
            showTab(R.id.nav_farm);
        } else {
            activeTabId = savedInstanceState.getInt("active_tab_id", R.id.nav_farm);
            bottomNavigation.setSelectedItemId(activeTabId);
            showTab(activeTabId);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("active_tab_id", activeTabId);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (AccessibilityUtils.isAccessibilityServiceEnabled(this, AppInterceptorService.class)) {
            dismissAccessibilityDialog();
        } else {
            showAccessibilityPermissionDialog();
        }
    }

    private void showTab(@IdRes int tabId) {
        activeTabId = tabId;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, getOrCreateFragment(tabId))
                .commit();
    }

    private Fragment getOrCreateFragment(@IdRes int tabId) {
        Fragment cached = tabFragments.get(tabId);
        if (cached != null) {
            return cached;
        }

        Fragment fragment = createFragmentForTab(tabId);
        tabFragments.put(tabId, fragment);
        return fragment;
    }

    @Nullable
    private Fragment createFragmentForTab(@IdRes int tabId) {
        if (tabId == R.id.nav_farm) {
            return new FarmFragment();
        } else if (tabId == R.id.nav_blocker) {
            return new BlockerFragment();
        } else if (tabId == R.id.nav_sleep) {
            return new SleepTreeFragment();
        } else if (tabId == R.id.nav_stats) {
            return new StatsFragment();
        }
        return new FarmFragment();
    }

    private void dismissAccessibilityDialog() {
        if (accessibilityDialog != null && accessibilityDialog.isShowing()) {
            accessibilityDialog.dismiss();
        }
        accessibilityDialog = null;
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
}
