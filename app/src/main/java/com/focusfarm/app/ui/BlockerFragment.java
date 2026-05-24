package com.focusfarm.app.ui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.focusfarm.app.R;
import com.focusfarm.app.data.AppInfo;
import com.focusfarm.app.data.SharedPrefsManager;
import com.focusfarm.app.data.SocialMediaAppFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * App selector: lists launchable installed apps and persists block toggles.
 */
public class BlockerFragment extends Fragment {

    private RecyclerView recyclerViewApps;
    private ProgressBar progressBar;
    private AppListAdapter adapter;
    private ExecutorService loaderExecutor;

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

        adapter = new AppListAdapter(requireContext());
        recyclerViewApps.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewApps.setAdapter(adapter);

        loadInstalledApps();
    }

    @Override
    public void onDestroyView() {
        if (loaderExecutor != null) {
            loaderExecutor.shutdownNow();
            loaderExecutor = null;
        }
        super.onDestroyView();
    }

    private void loadInstalledApps() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewApps.setVisibility(View.INVISIBLE);

        loaderExecutor = Executors.newSingleThreadExecutor();
        final android.content.Context appContext = requireContext().getApplicationContext();
        loaderExecutor.execute(() -> {
            List<AppInfo> apps = queryLaunchableApps(appContext);
            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                recyclerViewApps.setVisibility(View.VISIBLE);
                adapter.setApps(apps);
            });
        });
    }

    private List<AppInfo> queryLaunchableApps(android.content.Context context) {
        PackageManager pm = context.getPackageManager();
        Set<String> blockedApps = SharedPrefsManager.getBlockedApps(context);
        String ownPackage = context.getPackageName();

        List<ApplicationInfo> installed = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<AppInfo> launchable = new ArrayList<>();

        for (ApplicationInfo info : installed) {
            if (ownPackage.equals(info.packageName)) {
                continue;
            }

            Intent launchIntent = pm.getLaunchIntentForPackage(info.packageName);
            if (launchIntent == null) {
                continue;
            }

            // Shortlist: only show known social / entertainment apps
            if (!SocialMediaAppFilter.isSocialMediaApp(info.packageName)) {
                continue;
            }

            CharSequence label = pm.getApplicationLabel(info);
            String appName = label != null ? label.toString() : info.packageName;

            launchable.add(new AppInfo(
                    appName,
                    info.packageName,
                    info.loadIcon(pm),
                    blockedApps.contains(info.packageName)
            ));
        }

        Collections.sort(launchable, (a, b) ->
                a.getAppName().compareToIgnoreCase(b.getAppName()));
        return launchable;
    }
}
