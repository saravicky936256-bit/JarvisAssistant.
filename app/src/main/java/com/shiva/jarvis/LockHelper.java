package com.shiva.jarvis;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

public class LockHelper {
    public static boolean lockScreen(Context context) {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName admin = new ComponentName(context, JarvisDeviceAdminReceiver.class);
            if (dpm != null && dpm.isAdminActive(admin)) {
                dpm.lockNow();
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
