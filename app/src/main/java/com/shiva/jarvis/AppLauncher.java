package com.shiva.jarvis;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.List;
import java.util.Locale;

public class AppLauncher {

    public static boolean openApp(Context context, String packageName) {
        try {
            Context ctx = ContextProvider.best(context);
            Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent == null) return false;
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Opens any installed app whose visible name contains the spoken name
     * (e.g. spoken "whatsapp" or "youtube" or "phonepe"). */
    public static boolean openAppByName(Context context, String spokenName) {
        try {
            Context ctx = ContextProvider.best(context);
            PackageManager pm = ctx.getPackageManager();
            String needle = spokenName.trim().toLowerCase(Locale.getDefault());
            if (needle.isEmpty()) return false;

            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo app : apps) {
                String label = pm.getApplicationLabel(app).toString().toLowerCase(Locale.getDefault());
                if (label.contains(needle) || needle.contains(label)) {
                    Intent launch = pm.getLaunchIntentForPackage(app.packageName);
                    if (launch != null) {
                        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(launch);
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean openMapsNavigation(Context context, String destination) {
        Context ctx = ContextProvider.best(context);
        try {
            Uri uri = Uri.parse("google.navigation:q=" + Uri.encode(destination));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
            return true;
        } catch (Exception e) {
            try {
                Uri uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode(destination));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
                return true;
            } catch (Exception e2) {
                return false;
            }
        }
    }

    /** Opens WhatsApp with the message pre-filled for a contact — user still has to tap Send,
     * since WhatsApp does not allow apps to send messages fully automatically without its
     * official (paid, business-only) Cloud API. */
    public static boolean sendWhatsAppText(Context context, String contactName, String message) {
        String number = ContactHelper.findNumberByName(context, contactName);
        if (number == null) return false;
        String digits = number.replaceAll("[^0-9+]", "");
        try {
            Context ctx = ContextProvider.best(context);
            Uri uri = Uri.parse("https://wa.me/" + digits + "?text=" + Uri.encode(message));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
