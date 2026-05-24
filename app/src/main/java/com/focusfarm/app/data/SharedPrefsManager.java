package com.focusfarm.app.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Single entry for persisted app state: lock/unblock windows, points, streak, blocked apps, sleep log.
 */
public final class SharedPrefsManager {

    private static final String PREFS_NAME = "focus_farm_prefs";

    private static final String KEY_UNBLOCK_PREFIX = "unblock_until_";
    private static final String KEY_LOCK_PREFIX = "lock_until_";

    private SharedPrefsManager() {
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isTemporarilyUnblocked(Context context, String packageName) {
        long unblockUntil = getPrefs(context).getLong(KEY_UNBLOCK_PREFIX + packageName, 0L);
        return System.currentTimeMillis() < unblockUntil;
    }

    public static void setTemporaryUnblock(Context context, String packageName, int minutes) {
        long unblockUntil = System.currentTimeMillis() + (minutes * 60_000L);
        getPrefs(context).edit()
                .putLong(KEY_UNBLOCK_PREFIX + packageName, unblockUntil)
                .apply();
    }

    public static void setAppLock(Context context, String packageName, int minutes) {
        long lockUntil = System.currentTimeMillis() + (minutes * 60_000L);
        getPrefs(context).edit()
                .putLong(KEY_LOCK_PREFIX + packageName, lockUntil)
                .apply();
    }

    // TODO: get/set points
    // TODO: get/set streak
    // TODO: get/set blocked apps list
    // TODO: get/set tree level
    // TODO: get/set sleep log
}
