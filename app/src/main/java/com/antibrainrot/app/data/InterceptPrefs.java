package com.antibrainrot.app.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Persistence for per-app lock, unblock, and overlay cooldown timestamps.
 */
public final class InterceptPrefs {

    private static final String PREFS_NAME = "intercept_state";
    private static final String KEY_UNBLOCK_PREFIX = "unblock_until_";
    private static final String KEY_LOCK_PREFIX = "lock_until_";
    private static final String KEY_COOLDOWN_PREFIX = "overlay_cooldown_until_";

    private InterceptPrefs() {
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

    public static boolean isAppLocked(Context context, String packageName) {
        long lockUntil = getPrefs(context).getLong(KEY_LOCK_PREFIX + packageName, 0L);
        return System.currentTimeMillis() < lockUntil;
    }

    public static void setAppLock(Context context, String packageName, int minutes) {
        long lockUntil = System.currentTimeMillis() + (minutes * 60_000L);
        getPrefs(context).edit()
                .putLong(KEY_LOCK_PREFIX + packageName, lockUntil)
                .apply();
    }

    public static void setOverlayCooldown(Context context, String packageName, long cooldownMs) {
        long cooldownUntil = System.currentTimeMillis() + cooldownMs;
        getPrefs(context).edit()
                .putLong(KEY_COOLDOWN_PREFIX + packageName, cooldownUntil)
                .apply();
    }

    public static boolean isOverlayCooldownActive(Context context, String packageName) {
        long cooldownUntil = getPrefs(context).getLong(KEY_COOLDOWN_PREFIX + packageName, 0L);
        return System.currentTimeMillis() < cooldownUntil;
    }
}
