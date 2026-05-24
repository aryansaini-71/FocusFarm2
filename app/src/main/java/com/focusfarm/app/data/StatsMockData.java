package com.focusfarm.app.data;

/**
 * Mock analytics for the Stats screen (hackathon demo).
 * Replace with real persistence when backend is ready.
 */
public final class StatsMockData {

    public static final int RESISTANCE_PERCENT = 78;
    public static final int TREE_GROWTH_PERCENT = 75;
    public static final int BREATHING_SUCCESS_PERCENT = 92;

    /** Hours slept per day for the past 7 days (oldest → newest). */
    public static final float[] SLEEP_HOURS_LAST_7_DAYS = {
            6.2f, 7.1f, 5.8f, 7.5f, 6.9f, 8.0f, 7.3f
    };

    public static final String[] SLEEP_DAY_LABELS = {
            "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
    };

    public static final float SLEEP_GOAL_HOURS = 8f;

    private StatsMockData() {
    }

    public static String companionSpeciesForTreeLevel(int treeLevel) {
        switch (treeLevel) {
            case 3:
                return "GOLDENTREE";
            case 2:
                return "YELLOWTREE";
            default:
                return "SEEDLING";
        }
    }

    public static float averageSleepHours() {
        float sum = 0f;
        for (float hours : SLEEP_HOURS_LAST_7_DAYS) {
            sum += hours;
        }
        return sum / SLEEP_HOURS_LAST_7_DAYS.length;
    }

    public static int nightsAtGoal() {
        int count = 0;
        for (float hours : SLEEP_HOURS_LAST_7_DAYS) {
            if (hours >= SLEEP_GOAL_HOURS * 0.85f) {
                count++;
            }
        }
        return count;
    }
}
