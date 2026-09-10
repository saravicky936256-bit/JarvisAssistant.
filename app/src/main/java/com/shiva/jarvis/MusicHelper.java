package com.shiva.jarvis;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.provider.MediaStore;
import android.view.KeyEvent;

/** Sends media button key events (play/pause/next/previous) to whichever music
 * app currently holds the active media session — works with Spotify, YouTube
 * Music, Gaana, JioSaavn, the default Music app, etc. without needing to know
 * which one the user has installed. */
public class MusicHelper {

    /** Asks whatever music app is installed to search for and play a song —
     * uses Android's standard voice-search-and-play intent, which Spotify,
     * YouTube Music, Gaana, JioSaavn etc. all support and usually auto-play
     * the first/best match for. */
    public static boolean searchAndPlay(Context context, String query) {
        try {
            Intent intent = new Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
            intent.putExtra(android.app.SearchManager.QUERY, query);
            intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ContextProvider.best(context).startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean play(Context context) {
        return sendKey(context, KeyEvent.KEYCODE_MEDIA_PLAY);
    }

    public static boolean pause(Context context) {
        return sendKey(context, KeyEvent.KEYCODE_MEDIA_PAUSE);
    }

    public static boolean playPauseToggle(Context context) {
        return sendKey(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
    }

    public static boolean next(Context context) {
        return sendKey(context, KeyEvent.KEYCODE_MEDIA_NEXT);
    }

    public static boolean previous(Context context) {
        return sendKey(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    }

    private static boolean sendKey(Context context, int keyCode) {
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am == null) return false;
            long eventTime = System.currentTimeMillis();
            am.dispatchMediaKeyEvent(new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0));
            am.dispatchMediaKeyEvent(new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
