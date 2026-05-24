package com.antibrainrot.app.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * In-memory-friendly game state backed by SharedPreferences until Room is added.
 * Farm tab and interceptor callbacks read/write through this class.
 */
public final class GameDataManager {

    private static final String PREFS_NAME = "game_data";
    private static final String KEY_TOTAL_POINTS = "total_points";
    private static final String KEY_PLANTS_ALIVE = "plants_alive";

    private static final int STARTING_POINTS = 0;
    private static final int STARTING_PLANTS = 3;
    private static final int POINTS_PER_LOCK = 15;

    private GameDataManager() {
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static void ensureDefaults(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (!prefs.contains(KEY_PLANTS_ALIVE)) {
            prefs.edit()
                    .putInt(KEY_TOTAL_POINTS, STARTING_POINTS)
                    .putInt(KEY_PLANTS_ALIVE, STARTING_PLANTS)
                    .apply();
        }
    }

    public static int getTotalPoints(Context context) {
        ensureDefaults(context);
        return getPrefs(context).getInt(KEY_TOTAL_POINTS, STARTING_POINTS);
    }

    public static int getPlantsAlive(Context context) {
        ensureDefaults(context);
        return getPrefs(context).getInt(KEY_PLANTS_ALIVE, STARTING_PLANTS);
    }

    /** User chose to lock the distracting app — reward focus points. */
    public static void onUserLockedApp(Context context, int lockMinutes) {
        ensureDefaults(context);
        int bonus = POINTS_PER_LOCK + (lockMinutes / 30);
        addPoints(context, bonus);
    }

    /** User unblocked after passing a task — one plant dies. */
    public static void onUserUnblockedApp(Context context) {
        ensureDefaults(context);
        SharedPreferences prefs = getPrefs(context);
        int plants = Math.max(0, prefs.getInt(KEY_PLANTS_ALIVE, STARTING_PLANTS) - 1);
        prefs.edit().putInt(KEY_PLANTS_ALIVE, plants).apply();
    }

    private static void addPoints(Context context, int delta) {
        SharedPreferences prefs = getPrefs(context);
        int updated = prefs.getInt(KEY_TOTAL_POINTS, STARTING_POINTS) + delta;
        prefs.edit().putInt(KEY_TOTAL_POINTS, updated).apply();
    }
}
