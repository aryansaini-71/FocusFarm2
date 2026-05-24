package com.focusfarm.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Persisted farm state: lock/unblock, health, streak, tree level, sleep.
 */
public final class SharedPrefsManager {

    private static final String PREFS_NAME = "focus_farm_prefs";

    private static final String KEY_UNBLOCK_PREFIX = "unblock_until_";
    /** Per-package block end timestamp, e.g. {@code com.instagram.android_block_end}. */
    private static final String KEY_BLOCK_END_SUFFIX = "_block_end";
    private static final String KEY_TREE_LEVEL = "tree_level";
    private static final String KEY_STREAK = "streak";
    private static final String KEY_LAST_STREAK_DAY = "last_streak_day";
    private static final String KEY_FARM_HEALTH = "farm_health";
    private static final String KEY_RESIST_TOTAL = "resist_total";
    private static final String KEY_AVG_SLEEP_HOURS = "avg_sleep_hours";
    private static final String KEY_SLEEP_GOAL_HOURS = "sleep_goal_hours";
    private static final String KEY_SLEEP_STREAK = "sleep_streak";
    private static final String KEY_LAST_SLEEP_LOG_DAY = "last_sleep_log_day";
    private static final String KEY_SLEEP_PLANTS_EARNED = "sleep_plants_earned";
    private static final String KEY_FARM_POINTS = "farm_points";
    public static final String KEY_BLOCKED_APPS = "BLOCKED_APPS";

    public static final int SLEEP_MILESTONE_DAYS = 3;

    public static final float DEFAULT_SLEEP_GOAL_HOURS = 8f;

    private SharedPrefsManager() {
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String todayKey() {
        return new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
    }

    // --- Block / unblock ---

    public static boolean isTemporarilyUnblocked(Context context, String packageName) {
        long unblockUntil = getPrefs(context).getLong(KEY_UNBLOCK_PREFIX + packageName, 0L);
        return System.currentTimeMillis() < unblockUntil;
    }

    public static void setTemporaryUnblock(Context context, String packageName, int minutes) {
        setTemporaryUnblockMs(context, packageName, minutes * 60_000L);
    }

    public static void setTemporaryUnblockMs(Context context, String packageName, long durationMs) {
        long unblockUntil = System.currentTimeMillis() + durationMs;
        getPrefs(context).edit()
                .putLong(KEY_UNBLOCK_PREFIX + packageName, unblockUntil)
                .apply();
    }

    /** Builds the SharedPreferences key for a package block end time. */
    public static String blockEndKey(String packageName) {
        return packageName + KEY_BLOCK_END_SUFFIX;
    }

    /** Saves an exact block end timestamp for the given package. */
    public static void setAppBlockEnd(Context context, String packageName, long blockEndMillis) {
        getPrefs(context).edit()
                .putLong(blockEndKey(packageName), blockEndMillis)
                .apply();
    }

    public static void setAppLock(Context context, String packageName, int minutes) {
        long blockEnd = System.currentTimeMillis() + (minutes * 60_000L);
        setAppBlockEnd(context, packageName, blockEnd);
    }

    public static long getLockEndTime(Context context, String packageName) {
        return getPrefs(context).getLong(blockEndKey(packageName), 0L);
    }

    public static boolean isAppLocked(Context context, String packageName) {
        return System.currentTimeMillis() < getLockEndTime(context, packageName);
    }

    public static int getLockRemainingMinutes(Context context, String packageName) {
        long remainingMs = getLockEndTime(context, packageName) - System.currentTimeMillis();
        if (remainingMs <= 0L) {
            return 0;
        }
        return (int) Math.ceil(remainingMs / 60_000.0);
    }

    // --- Blocked app list (app selector) ---

    public static Set<String> getBlockedApps(Context context) {
        Set<String> stored = getPrefs(context).getStringSet(KEY_BLOCKED_APPS, null);
        if (stored == null || stored.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(stored);
    }

    public static void setBlockedApps(Context context, Set<String> packageNames) {
        getPrefs(context).edit()
                .putStringSet(KEY_BLOCKED_APPS, new HashSet<>(packageNames))
                .apply();
    }

    public static void setAppBlocked(Context context, String packageName, boolean blocked) {
        Set<String> blockedApps = new HashSet<>(getBlockedApps(context));
        if (blocked) {
            blockedApps.add(packageName);
        } else {
            blockedApps.remove(packageName);
        }
        setBlockedApps(context, blockedApps);
    }

    public static boolean isAppBlocked(Context context, String packageName) {
        return getBlockedApps(context).contains(packageName);
    }

    // --- Farm progression ---

    /** User chose not to open the blocked app — grows streak & farm health. */
    public static void recordResistChoice(Context context) {
        SharedPreferences prefs = getPrefs(context);
        int streak = prefs.getInt(KEY_STREAK, 0);
        String today = todayKey();
        String lastDay = prefs.getString(KEY_LAST_STREAK_DAY, "");

        if (!today.equals(lastDay)) {
            streak += 1;
        }

        int health = Math.min(100, prefs.getInt(KEY_FARM_HEALTH, 40) + 12);
        int resistTotal = prefs.getInt(KEY_RESIST_TOTAL, 0) + 1;
        int treeLevel = computeTreeLevel(streak, health, getAvgSleepHours(context));

        prefs.edit()
                .putInt(KEY_STREAK, streak)
                .putString(KEY_LAST_STREAK_DAY, today)
                .putInt(KEY_FARM_HEALTH, health)
                .putInt(KEY_RESIST_TOTAL, resistTotal)
                .putInt(KEY_TREE_LEVEL, treeLevel)
                .apply();
    }

    /** User gave in and opened the app — farm takes a hit. */
    public static void recordGiveInChoice(Context context) {
        SharedPreferences prefs = getPrefs(context);
        int health = Math.max(0, prefs.getInt(KEY_FARM_HEALTH, 40) - 18);
        int streak = prefs.getInt(KEY_STREAK, 0);
        int treeLevel = computeTreeLevel(Math.max(0, streak - 1), health, getAvgSleepHours(context));

        prefs.edit()
                .putInt(KEY_FARM_HEALTH, health)
                .putInt(KEY_STREAK, Math.max(0, streak - 1))
                .putInt(KEY_TREE_LEVEL, treeLevel)
                .apply();
    }

    public static int getTreeLevel(Context context) {
        SharedPreferences prefs = getPrefs(context);
        int stored = prefs.getInt(KEY_TREE_LEVEL, 0);
        if (stored > 0) {
            return stored;
        }
        int level = computeTreeLevel(
                prefs.getInt(KEY_STREAK, 0),
                prefs.getInt(KEY_FARM_HEALTH, 40),
                getAvgSleepHours(context));
        prefs.edit().putInt(KEY_TREE_LEVEL, level).apply();
        return level;
    }

    public static int getFarmHealth(Context context) {
        return getPrefs(context).getInt(KEY_FARM_HEALTH, 40);
    }

    public static int getStreak(Context context) {
        return getPrefs(context).getInt(KEY_STREAK, 0);
    }

    public static int getResistTotal(Context context) {
        return getPrefs(context).getInt(KEY_RESIST_TOTAL, 0);
    }

    public static float getAvgSleepHours(Context context) {
        return getPrefs(context).getFloat(KEY_AVG_SLEEP_HOURS, 6.5f);
    }

    public static float getSleepGoalHours(Context context) {
        return getPrefs(context).getFloat(KEY_SLEEP_GOAL_HOURS, DEFAULT_SLEEP_GOAL_HOURS);
    }

    public static void setAvgSleepHours(Context context, float hours) {
        SharedPreferences prefs = getPrefs(context);
        prefs.edit().putFloat(KEY_AVG_SLEEP_HOURS, hours).apply();
        int treeLevel = computeTreeLevel(
                prefs.getInt(KEY_STREAK, 0),
                prefs.getInt(KEY_FARM_HEALTH, 40),
                hours);
        prefs.edit().putInt(KEY_TREE_LEVEL, treeLevel).apply();
    }

    /** 0.0–1.0 sleep progress toward nightly goal. */
    public static float getSleepProgress(Context context) {
        float goal = getSleepGoalHours(context);
        if (goal <= 0f) {
            return 0f;
        }
        return Math.min(1f, getAvgSleepHours(context) / goal);
    }

    /** 0.0–1.0 streak progress (7-day target). */
    public static float getStreakProgress(Context context) {
        return Math.min(1f, getStreak(context) / 7f);
    }

    /** 0.0–1.0 farm health. */
    public static float getFarmHealthProgress(Context context) {
        return getFarmHealth(context) / 100f;
    }

    // --- Sleep streak (not wired to tree yet) ---

    public static int getFarmPoints(Context context) {
        return getPrefs(context).getInt(KEY_FARM_POINTS, 100);
    }

    public static int getSleepStreak(Context context) {
        return getPrefs(context).getInt(KEY_SLEEP_STREAK, 0);
    }

    public static int getSleepPlantsEarned(Context context) {
        return getPrefs(context).getInt(KEY_SLEEP_PLANTS_EARNED, 0);
    }

    public static String getLastSleepLogDay(Context context) {
        return getPrefs(context).getString(KEY_LAST_SLEEP_LOG_DAY, "");
    }

    /** Progress within the current 3-day milestone cycle (0–3). */
    public static int getSleepMilestoneProgress(Context context) {
        int streak = getSleepStreak(context);
        if (streak <= 0) {
            return 0;
        }
        int remainder = streak % SLEEP_MILESTONE_DAYS;
        return remainder == 0 ? SLEEP_MILESTONE_DAYS : remainder;
    }

    public static boolean hasLoggedSleepToday(Context context) {
        return todayKey().equals(getLastSleepLogDay(context));
    }

    /**
     * @return true if a new night was logged; false if already logged today.
     */
    public static boolean logRestfulSleepNight(Context context) {
        if (hasLoggedSleepToday(context)) {
            return false;
        }

        SharedPreferences prefs = getPrefs(context);
        String today = todayKey();
        String lastDay = prefs.getString(KEY_LAST_SLEEP_LOG_DAY, "");
        int streak = prefs.getInt(KEY_SLEEP_STREAK, 0);

        if (lastDay.isEmpty()) {
            streak = 1;
        } else if (isYesterday(lastDay, today)) {
            streak += 1;
        } else if (!today.equals(lastDay)) {
            streak = 1;
        }

        int plantsEarned = prefs.getInt(KEY_SLEEP_PLANTS_EARNED, 0);
        boolean earnedPlant = streak > 0 && streak % SLEEP_MILESTONE_DAYS == 0;
        if (earnedPlant) {
            plantsEarned += 1;
            // TODO: Connect sleep plant reward to farm plot / tree when ready.
        }

        prefs.edit()
                .putInt(KEY_SLEEP_STREAK, streak)
                .putString(KEY_LAST_SLEEP_LOG_DAY, today)
                .putInt(KEY_SLEEP_PLANTS_EARNED, plantsEarned)
                .apply();

        return true;
    }

    public static void resetSleepStreak(Context context) {
        getPrefs(context).edit()
                .putInt(KEY_SLEEP_STREAK, 0)
                .remove(KEY_LAST_SLEEP_LOG_DAY)
                .apply();
    }

    private static boolean isYesterday(String lastDay, String today) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);
            Date lastDate = format.parse(lastDay);
            Date todayDate = format.parse(today);
            if (lastDate == null || todayDate == null) {
                return false;
            }
            long diffMs = todayDate.getTime() - lastDate.getTime();
            return diffMs == 86_400_000L;
        } catch (Exception e) {
            return false;
        }
    }

    public static int computeTreeLevel(int streak, int farmHealth, float sleepHours) {
        float sleepGoal = DEFAULT_SLEEP_GOAL_HOURS;
        boolean goodSleep = sleepHours >= sleepGoal * 0.85f;

        if (streak >= 7 && farmHealth >= 70 && goodSleep) {
            return 3;
        }
        if (streak >= 3 || farmHealth >= 55) {
            return 2;
        }
        return 1;
    }

    public static int treeDrawableForLevel(int level) {
        switch (level) {
            case 3:
                return com.focusfarm.app.R.drawable.treelvl3;
            case 2:
                return com.focusfarm.app.R.drawable.treelvl2;
            default:
                return com.focusfarm.app.R.drawable.treelvl1;
        }
    }

    public static int treeSizeDimForLevel(int level) {
        switch (level) {
            case 3:
                return com.focusfarm.app.R.dimen.tree_lvl3;
            case 2:
                return com.focusfarm.app.R.dimen.tree_lvl2;
            default:
                return com.focusfarm.app.R.dimen.tree_lvl1;
        }
    }
}
