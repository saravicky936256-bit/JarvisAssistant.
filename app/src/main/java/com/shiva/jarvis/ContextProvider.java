package com.shiva.jarvis;

import android.content.Context;

public class ContextProvider {
    /** If the user has enabled Jarvis's Accessibility Service, starting activities from
     * its context is exempt from Android's background-activity-launch restrictions —
     * this is what makes "open Instagram" etc. actually work reliably while Jarvis is
     * running as a background service. Falls back to the given context if not enabled. */
    public static Context best(Context fallback) {
        JarvisAccessibilityService svc = JarvisAccessibilityService.getInstance();
        return svc != null ? svc : fallback;
    }
}
