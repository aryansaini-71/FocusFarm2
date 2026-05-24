package com.focusfarm.app.data;

/**
 * One row in the Statistics "Recent Activity" list.
 */
public class StatActivityItem {

    private final String title;
    private final String timeAgo;
    private final String pointsLabel;
    private final int iconResId;

    public StatActivityItem(String title, String timeAgo, String pointsLabel, int iconResId) {
        this.title = title;
        this.timeAgo = timeAgo;
        this.pointsLabel = pointsLabel;
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public String getPointsLabel() {
        return pointsLabel;
    }

    public int getIconResId() {
        return iconResId;
    }
}
