package com.antibrainrot.app.service;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

import com.antibrainrot.app.service.overlay.OverlayController;

/**
 * Listens for blocked apps entering the foreground and delegates overlay UI
 * to {@link OverlayController}.
 * Blocked packages are loaded dynamically from SharedPreferences ("BLOCKED_APPS")
 * via {@link InterceptPolicy} — configured in the Blocker tab.
 */
public class AppInterceptorService extends AccessibilityService {

    private OverlayController overlayController;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        overlayController = new OverlayController(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!InterceptPolicy.shouldIntercept(this, event)) {
            return;
        }

        if (overlayController != null && overlayController.isShowing()) {
            return;
        }

        String packageName = InterceptPolicy.getPackageName(event);
        if (packageName.isEmpty()) {
            return;
        }

        overlayController.show(packageName);
    }

    @Override
    public void onInterrupt() {
        if (overlayController != null) {
            overlayController.hide();
        }
    }

    @Override
    public void onDestroy() {
        if (overlayController != null) {
            overlayController.hide();
        }
        super.onDestroy();
    }
}
