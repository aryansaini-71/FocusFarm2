package com.antibrainrot.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores which app package names the user chose to block (Blocker tab switches).
 */
public final class BlockedAppsPrefs {

    public static final String PREFS_NAME = "blocked_apps_prefs";
    public static final String KEY_BLOCKED_APPS = "BLOCKED_APPS";

    private BlockedAppsPrefs() {
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Returns a copy — never mutate the set returned by SharedPreferences directly. */
    public static Set<String> getBlockedPackages(Context context) {
        Set<String> stored = getPrefs(context).getStringSet(KEY_BLOCKED_APPS, null);
        if (stored == null) {
            return new HashSet<>();
        }
        return new HashSet<>(stored);
    }

    public static boolean isPackageBlocked(Context context, String packageName) {
        return getBlockedPackages(context).contains(packageName);
    }

    public static void setPackageBlocked(Context context, String packageName, boolean blocked) {
        Set<String> updated = getBlockedPackages(context);
        if (blocked) {
            updated.add(packageName);
        } else {
            updated.remove(packageName);
        }
        getPrefs(context).edit()
                .putStringSet(KEY_BLOCKED_APPS, updated)
                .apply();
    }
}
