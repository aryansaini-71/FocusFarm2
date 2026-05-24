package com.focusfarm.app.data;

import android.graphics.drawable.Drawable;

/** Installed app row for the block-app picker. */
public class AppInfo {

    private final String appName;
    private final String packageName;
    private final Drawable icon;
    private boolean isBlocked;

    public AppInfo(String appName, String packageName, Drawable icon, boolean isBlocked) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
        this.isBlocked = isBlocked;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
