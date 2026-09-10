package com.shiva.jarvis;

import android.app.admin.DeviceAdminReceiver;

public class JarvisDeviceAdminReceiver extends DeviceAdminReceiver {
    // no overrides needed — just needs to exist and be registered so
    // DevicePolicyManager.lockNow() works once the user enables it
}
