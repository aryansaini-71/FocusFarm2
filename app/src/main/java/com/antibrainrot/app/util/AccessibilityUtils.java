package com.antibrainrot.app.util;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

/**
 * Checks whether an accessibility service is enabled in system settings.
 */
public final class AccessibilityUtils {

    private AccessibilityUtils() {
    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> serviceClass) {
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);

        if (accessibilityManager == null) {
            return false;
        }

        List<AccessibilityServiceInfo> enabledServices =
                accessibilityManager.getEnabledAccessibilityServiceList(
                        AccessibilityServiceInfo.FEEDBACK_GENERIC);

        String expectedId = context.getPackageName() + "/" + serviceClass.getName();

        for (AccessibilityServiceInfo serviceInfo : enabledServices) {
            if (expectedId.equals(serviceInfo.getId())) {
                return true;
            }

            if (serviceInfo.getResolveInfo() != null
                    && serviceInfo.getResolveInfo().serviceInfo != null) {
                String packageName = serviceInfo.getResolveInfo().serviceInfo.packageName;
                String className = serviceInfo.getResolveInfo().serviceInfo.name;
                if (context.getPackageName().equals(packageName)
                        && serviceClass.getName().equals(className)) {
                    return true;
                }
            }
        }

        return false;
    }
}
