package com.shiva.jarvis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Registered so the app is notified of call state changes (ringing / offhook / idle).
 * Currently a no-op placeholder — Jarvis answers/ends calls only when the user gives
 * the voice command (see CommandProcessor + CallHelper), not automatically on every ring.
 */
public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // intentionally left as a no-op for now
    }
}
