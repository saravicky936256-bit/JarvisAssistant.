package com.shiva.jarvis;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;

import java.util.Locale;

/**
 * Understands a spoken command (English / Tamil / Thanglish mixed) and
 * performs the matching action. Uses simple keyword matching first
 * (fast, works offline) and falls back to the Claude API for anything
 * it doesn't recognise, it simply says it didn't understand — fully offline,
 * no AI, no internet needed, no cost.
 */
public class CommandProcessor {

    public interface Speaker {
        void speak(String text);
    }

    private final Context context;
    private final Speaker speaker;

    public CommandProcessor(Context context, Speaker speaker) {
        this.context = context;
        this.speaker = speaker;
    }

    public void process(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) return;
        String text = rawText.toLowerCase(Locale.getDefault()).trim();

        try {
            // ---- Exit assistant ----
            if (containsAny(text, "veliya vaanum", "veliya po", "exit", "stop jarvis", "close jarvis")) {
                speaker.speak("Sari, naan exit aagiren.");
                if (context instanceof JarvisService) {
                    ((JarvisService) context).stopSelf();
                }
                return;
            }

            // ---- Lock phone ----
            if (containsAny(text, "lock pannu", "phone lock", "screen lock", "lock the phone")) {
                boolean ok = LockHelper.lockScreen(context);
                speaker.speak(ok ? "Phone lock pannitten." : "Lock permission illa, settings la device admin on pannunga.");
                return;
            }

            // ---- Answer call ----
            if (containsAny(text, "call attend", "call pick", "answer call", "call edu")) {
                boolean ok = CallHelper.answerCall(context);
                speaker.speak(ok ? "Call attend pannitten." : "Call attend panna mudiyala.");
                return;
            }

            // ---- End / cut call ----
            if (containsAny(text, "call cut", "call end", "hang up", "call disconnect")) {
                boolean ok = CallHelper.endCall(context);
                speaker.speak(ok ? "Call cut pannitten." : "Call cut panna mudiyala.");
                return;
            }

            // ---- Open Instagram ----
            if (containsAny(text, "insta open", "instagram open", "insta podu",
                    "open insta", "open instagram", "instagram podu")) {
                boolean ok = AppLauncher.openApp(context, "com.instagram.android");
                speaker.speak(ok ? "Instagram open pannitten." : "Instagram install pannala nu nenaikren.");
                return;
            }

            // ---- Play a specific song (by name, movie, director — say it all together) ----
            // e.g. "Vaathi Coming Vijay song play pannu"
            String songQuery = extractSongQuery(text);
            if (songQuery != null && !songQuery.trim().isEmpty()) {
                boolean ok = MusicHelper.searchAndPlay(context, songQuery.trim());
                speaker.speak(ok ? (songQuery + " podren.") : "Music app kandupudika mudiyala, ஒரு music app install pannirukkanum.");
                return;
            }

            // ---- Music controls ----
            if (containsAny(text, "music play pannu", "play music", "song play pannu")) {
                boolean ok = MusicHelper.play(context);
                speaker.speak(ok ? "Music play pannitten." : "Music player kandupudika mudiyala.");
                return;
            }
            if (containsAny(text, "music pause pannu", "pause music", "music stop pannu", "stop music")) {
                boolean ok = MusicHelper.pause(context);
                speaker.speak(ok ? "Music pause pannitten." : "Music player kandupudika mudiyala.");
                return;
            }
            if (containsAny(text, "next song", "song next pannu", "next track")) {
                boolean ok = MusicHelper.next(context);
                speaker.speak(ok ? "Next song podren." : "Music player kandupudika mudiyala.");
                return;
            }
            if (containsAny(text, "previous song", "song previous pannu", "back song")) {
                boolean ok = MusicHelper.previous(context);
                speaker.speak(ok ? "Previous song podren." : "Music player kandupudika mudiyala.");
                return;
            }

            // ---- Open any other installed app by spoken name ----
            // e.g. "whatsapp open pannu", "open youtube", "youtube podu"
            String appName = extractAppNameToOpen(text);
            if (appName != null) {
                boolean ok = AppLauncher.openAppByName(context, appName);
                speaker.speak(ok ? (appName + " open pannitten.") : (appName + " nu app kandupudika mudiyala."));
                return;
            }

            // ---- Open Maps navigation ----
            String mapDest = extractAfterAny(text, "map podu", "map open pannu", "navigate to", "directions to");
            if (mapDest != null) {
                boolean ok = AppLauncher.openMapsNavigation(context, mapDest.trim());
                speaker.speak(ok ? ("Sari, " + mapDest + " ku map podren.") : "Location purila, innoru murai sollunga.");
                return;
            }

            // ---- Send WhatsApp text ----
            // pattern: "<name> ku whatsapp la <message> nu message anuppu"
            String[] waParts = extractWhatsApp(text);
            if (waParts != null) {
                boolean ok = AppLauncher.sendWhatsAppText(context, waParts[0], waParts[1]);
                speaker.speak(ok ? "WhatsApp message ready pannitten, Send button mattum tap pannunga." : "Andha contact kandupidikka mudiyala.");
                return;
            }

            // ---- Call by contact name ----
            String callName = extractAfterAny(text, "ku call pannu", "ku call pannunga", "call pannu", "call podu");
            if (callName != null && !callName.trim().isEmpty()) {
                String number = ContactHelper.findNumberByName(context, callName.trim());
                if (number != null) {
                    boolean ok = CallHelper.callNumber(context, number);
                    speaker.speak(ok ? (callName + " ku call pannitten.") : "Call panna mudiyala.");
                } else {
                    speaker.speak(callName + " nu contact kandupudika mudiyala.");
                }
                return;
            }

            // ---- Greeting / acknowledgement ----
            if (containsAny(text, "jarvis")) {
                speaker.speak("Yes boss.");
                return;
            }

            // ---- Didn't understand — no AI, no internet needed, fully offline ----
            speaker.speak("Purila boss, innoru murai clear ah sollunga.");

        } catch (Exception e) {
            speaker.speak("Problem irukku: " + e.getMessage());
        }
    }

    private boolean containsAny(String text, String... needles) {
        for (String n : needles) if (text.contains(n)) return true;
        return false;
    }

    private String extractAfterAny(String text, String... markers) {
        for (String m : markers) {
            int idx = text.indexOf(m);
            if (idx >= 0) {
                // marker could be before ("ku call pannu" -> name is BEFORE marker)
                if (m.startsWith("ku ")) {
                    return text.substring(0, idx).trim();
                }
                return text.substring(idx + m.length()).trim();
            }
        }
        return null;
    }

    /** "<app> open pannu" / "open <app>" / "<app> podu" — generic app launcher */
    private String extractAppNameToOpen(String text) {
        if (text.contains(" open pannu")) {
            return text.substring(0, text.indexOf(" open pannu")).trim();
        }
        if (text.startsWith("open ")) {
            return text.substring(5).trim();
        }
        if (text.endsWith(" podu") && text.contains(" ")) {
            return text.substring(0, text.lastIndexOf(" podu")).trim();
        }
        return null;
    }

    /** Very simple pattern: "<name> ku whatsapp la <message> nu anuppu" */
    /** "<song name> <movie> <director/singer, if said> song play pannu" — the whole
     * phrase before the trigger words is used as the search query. */
    private String extractSongQuery(String text) {
        String[] markers = {"song play pannu", "song podu", "play song"};
        for (String m : markers) {
            int idx = text.indexOf(m);
            if (idx >= 0) {
                if (m.equals("play song")) {
                    return text.substring(idx + m.length()).trim();
                }
                return text.substring(0, idx).trim();
            }
        }
        return null;
    }

    private String[] extractWhatsApp(String text) {
        if (!text.contains("whatsapp")) return null;
        int kuIdx = text.indexOf(" ku whatsapp");
        if (kuIdx < 0) return null;
        String name = text.substring(0, kuIdx).trim();
        int laIdx = text.indexOf("la ", kuIdx);
        if (laIdx < 0) return null;
        String rest = text.substring(laIdx + 3).trim();
        // strip trailing "nu message anuppu" / "nu anuppu" if present
        rest = rest.replaceAll("\\s*nu\\s+(message\\s+)?anuppu.*$", "").trim();
        if (name.isEmpty() || rest.isEmpty()) return null;
        return new String[]{name, rest};
    }
}
