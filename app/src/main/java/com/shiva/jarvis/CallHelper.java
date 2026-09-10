package com.shiva.jarvis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.telecom.TelecomManager;
import androidx.core.content.ContextCompat;

public class CallHelper {

    public static boolean callNumber(Context context, String number) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ContextProvider.best(context).startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean answerCall(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        try {
            TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (tm != null) {
                tm.acceptRingingCall();
                return true;
            }
        } catch (SecurityException ignored) {}
        return false;
    }

    /** Ending a call programmatically requires the app to be the default dialer on modern
     * Android — not something we can silently obtain. We attempt it, and if it fails we tell
     * the user to end the call manually. */
    public static boolean endCall(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        try {
            TelecomManager tm = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (tm != null) {
                return tm.endCall();
            }
        } catch (SecurityException ignored) {}
        return false;
    }
}
