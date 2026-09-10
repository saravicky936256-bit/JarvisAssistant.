package com.shiva.jarvis;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import androidx.core.content.ContextCompat;

public class ContactHelper {

    /** Finds a phone number for a contact whose display name contains the spoken name. */
    public static String findNumberByName(Context context, String spokenName) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        String cleaned = spokenName.trim();
        if (cleaned.isEmpty()) return null;

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " LIKE ?",
                new String[]{"%" + cleaned + "%"},
                null);

        String number = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                number = cursor.getString(numIdx);
            }
            cursor.close();
        }
        return number;
    }
}
