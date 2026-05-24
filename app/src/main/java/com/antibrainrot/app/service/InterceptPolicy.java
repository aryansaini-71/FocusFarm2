package com.antibrainrot.app.service;

import android.content.Context;
import android.view.accessibility.AccessibilityEvent;

import com.antibrainrot.app.data.BlockedAppsPrefs;
import com.antibrainrot.app.data.InterceptPrefs;

import java.util.Set;

/**
 * Decides whether the interceptor should show the overlay for a given event.
 */
public final class InterceptPolicy {

    private InterceptPolicy() {
    }

    public static boolean shouldIntercept(Context context, AccessibilityEvent event) {
        if (event == null) {
            return false;
        }

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return false;
        }

        CharSequence packageSequence = event.getPackageName();
        if (packageSequence == null) {
            return false;
        }

        String packageName = packageSequence.toString();

        Set<String> blockedPackages = BlockedAppsPrefs.getBlockedPackages(context);
        if (!blockedPackages.contains(packageName)) {
            return false;
        }

        if (InterceptPrefs.isTemporarilyUnblocked(context, packageName)) {
            return false;
        }

        if (InterceptPrefs.isOverlayCooldownActive(context, packageName)) {
            return false;
        }

        return true;
    }

    public static String getPackageName(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return "";
        }
        return event.getPackageName().toString();
    }
}
