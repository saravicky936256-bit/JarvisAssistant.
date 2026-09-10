package com.shiva.jarvis;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_PERMISSIONS = 101;
    private static final int REQ_DEVICE_ADMIN = 102;
    private static final int REQ_OVERLAY = 103;

    private final String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.POST_NOTIFICATIONS
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 120, 40, 40);

        TextView title = new TextView(this);
        title.setText("Jarvis Assistant");
        title.setTextSize(26);
        root.addView(title);

        TextView status = new TextView(this);
        status.setText("Say \"Jarvis\" anytime to give a voice command. Grant all permissions below, then keep this app running (or let it run in background) for Jarvis to listen.");
        status.setTextSize(15);
        status.setPadding(0, 30, 0, 40);
        root.addView(status);

        Button grantBtn = new Button(this);
        grantBtn.setText("1. Grant Permissions");
        grantBtn.setOnClickListener(v -> requestAllPermissions());
        root.addView(grantBtn);

        Button adminBtn = new Button(this);
        adminBtn.setText("2. Enable Screen Lock Control");
        adminBtn.setOnClickListener(v -> requestDeviceAdmin());
        root.addView(adminBtn);

        Button overlayBtn = new Button(this);
        overlayBtn.setText("3. Allow Opening Apps (important!)");
        overlayBtn.setOnClickListener(v -> requestOverlayPermission());
        root.addView(overlayBtn);

        Button accessBtn = new Button(this);
        accessBtn.setText("4. Enable Background App Control (must do!)");
        accessBtn.setOnClickListener(v -> requestAccessibilityPermission());
        root.addView(accessBtn);

        Button startBtn = new Button(this);
        startBtn.setText("5. Start Jarvis");
        startBtn.setOnClickListener(v -> startJarvisService());
        root.addView(startBtn);

        setContentView(root);

        requestAllPermissions();
    }

    private void requestAllPermissions() {
        java.util.List<String> toRequest = new java.util.ArrayList<>();
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                toRequest.add(p);
            }
        }
        if (!toRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, toRequest.toArray(new String[0]), REQ_PERMISSIONS);
        }
    }

    private void requestDeviceAdmin() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName admin = new ComponentName(this, JarvisDeviceAdminReceiver.class);
        if (dpm != null && !dpm.isAdminActive(admin)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Jarvis needs this permission to lock your screen when you say \"lock the phone\".");
            startActivityForResult(intent, REQ_DEVICE_ADMIN);
        }
    }

    /** Without this, Android silently blocks Jarvis from opening apps (Instagram, Maps,
     * WhatsApp, calls) while it's running in the background — the command will say it
     * worked but nothing actually opens. This permission fixes that. */
    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQ_OVERLAY);
        }
    }

    /** THE fix for "app opens command says it worked but nothing actually opens" —
     * Android exempts an enabled Accessibility Service from background-activity-launch
     * restrictions. Opens the system Accessibility settings screen; the user must find
     * "Jarvis" in the list and turn it on manually (Android requires this to be a
     * deliberate user action, it can't be auto-granted). */
    private void requestAccessibilityPermission() {
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    private void startJarvisService() {
        Intent svc = new Intent(this, JarvisService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(svc);
        } else {
            startService(svc);
        }
    }
}
