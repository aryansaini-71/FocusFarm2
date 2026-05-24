package com.antibrainrot.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Simple storage for lock / unblock timestamps per app package.
 * Uses SharedPreferences until the team wires up Room later.
 */
public final class InterceptPrefs {

    private static final String PREFS_NAME = "intercept_state";

    /** unblock_until_com.instagram.android → epoch millis when unblock expires */
    private static final String KEY_UNBLOCK_PREFIX = "unblock_until_";

    /** lock_until_com.instagram.android → epoch millis when lock expires (for future use) */
    private static final String KEY_LOCK_PREFIX = "lock_until_";

    private InterceptPrefs() {
        // Utility class — no instances.
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Returns true if this app is in a temporary "pass task" unblock window.
     */
    public static boolean isTemporarilyUnblocked(Context context, String packageName) {
        long unblockUntil = getPrefs(context).getLong(KEY_UNBLOCK_PREFIX + packageName, 0L);
        return System.currentTimeMillis() < unblockUntil;
    }

    /**
     * Saves a temporary unblock so the interceptor ignores this app for {@code minutes}.
     */
    public static void setTemporaryUnblock(Context context, String packageName, int minutes) {
        long unblockUntil = System.currentTimeMillis() + (minutes * 60_000L);
        getPrefs(context).edit()
                .putLong(KEY_UNBLOCK_PREFIX + packageName, unblockUntil)
                .apply();
    }

    /**
     * Records that the user locked themselves out of this app for {@code minutes}.
     * TODO: Add points here when the points system is built.
     */
    public static void setAppLock(Context context, String packageName, int minutes) {
        long lockUntil = System.currentTimeMillis() + (minutes * 60_000L);
        getPrefs(context).edit()
                .putLong(KEY_LOCK_PREFIX + packageName, lockUntil)
                .apply();
    }
}
