package com.antibrainrot.app.ui.blocker;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.antibrainrot.app.R;
import com.antibrainrot.app.data.BlockedAppsPrefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Blocker tab — pick which installed apps trigger the interceptor overlay.
 */
public class BlockerFragment extends Fragment {

    private RecyclerView recyclerViewApps;
    private ProgressBar progressBar;
    private AppListAdapter adapter;

    private ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blocker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewApps = view.findViewById(R.id.recycler_view_apps);
        progressBar = view.findViewById(R.id.progress_bar);

        adapter = new AppListAdapter();
        recyclerViewApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewApps.setAdapter(adapter);

        Button openSettings = view.findViewById(R.id.btn_blocker_open_settings);
        openSettings.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        loadInstalledApps();
    }

    private void loadInstalledApps() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewApps.setVisibility(View.GONE);

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        final Context appContext = requireContext().getApplicationContext();

        executor.execute(() -> {
            List<AppInfo> apps = queryLaunchableApps(appContext);
            mainHandler.post(() -> {
                if (!isAdded()) {
                    return;
                }
                progressBar.setVisibility(View.GONE);
                recyclerViewApps.setVisibility(View.VISIBLE);
                adapter.setApps(apps);

                if (apps.isEmpty()) {
                    Toast.makeText(requireContext(),
                            R.string.blocker_no_apps_found,
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private List<AppInfo> queryLaunchableApps(Context context) {
        List<AppInfo> result = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        String ourPackage = context.getPackageName();
        Set<String> blockedPackages = BlockedAppsPrefs.getBlockedPackages(context);

        List<ApplicationInfo> installedApps =
                packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : installedApps) {
            if (ourPackage.equals(appInfo.packageName)) {
                continue;
            }

            // Only apps the user can open from the launcher
            if (packageManager.getLaunchIntentForPackage(appInfo.packageName) == null) {
                continue;
            }

            CharSequence label = packageManager.getApplicationLabel(appInfo);
            String appName = label != null ? label.toString() : appInfo.packageName;
            boolean isBlocked = blockedPackages.contains(appInfo.packageName);

            result.add(new AppInfo(
                    appName,
                    appInfo.packageName,
                    packageManager.getApplicationIcon(appInfo),
                    isBlocked
            ));
        }

        Collections.sort(result, (a, b) ->
                a.getAppName().compareToIgnoreCase(b.getAppName()));

        return result;
    }

    @Override
    public void onDestroyView() {
        mainHandler.removeCallbacksAndMessages(null);
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        super.onDestroyView();
    }
}
