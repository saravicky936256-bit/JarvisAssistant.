package com.shiva.jarvis;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/**
 * We don't need to process any accessibility events — this service exists
 * purely because Android exempts enabled AccessibilityServices from the
 * "background activity launch" restrictions that otherwise silently block
 * apps from opening other apps (Instagram, Maps, WhatsApp, etc.) while
 * running in the background. This is the same mechanism automation apps
 * like Tasker use to reliably open apps on command.
 */
public class JarvisAccessibilityService extends AccessibilityService {

    private static JarvisAccessibilityService instance;

    public static JarvisAccessibilityService getInstance() {
        return instance;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // not needed for our use case
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
