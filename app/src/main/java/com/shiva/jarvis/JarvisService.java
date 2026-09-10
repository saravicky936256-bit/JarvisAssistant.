package com.shiva.jarvis;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Locale;

public class JarvisService extends Service implements CommandProcessor.Speaker {

    private static final String CHANNEL_ID = "jarvis_channel";
    private static final int NOTIF_ID = 1;
    private static final String WAKE_WORD = "jarvis";

    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private CommandProcessor commandProcessor;
    private boolean listeningForCommand = false;
    private boolean ttsReady = false;
    private boolean shuttingDown = false;
    private boolean justSpoke = false; // true while Jarvis is talking — mic restart waits for it

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIF_ID, buildNotification("Jarvis is listening..."));

        commandProcessor = new CommandProcessor(this, this);

        tts = new TextToSpeech(this, status -> {
            ttsReady = (status == TextToSpeech.SUCCESS);
            if (ttsReady) {
                tts.setLanguage(Locale.US); // Tamil TTS voice availability varies by device;
                                             // falls back to English if Tamil pack isn't installed
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String utteranceId) {}
                    @Override public void onDone(String utteranceId) {
                        // Jarvis finished talking — safe to listen again now, so we don't
                        // pick up Jarvis's own voice as the next "command"
                        new Handler(getMainLooper()).postDelayed(() -> {
                            justSpoke = false;
                            startListening();
                        }, 250);
                    }
                    @Override public void onError(String utteranceId) {
                        justSpoke = false;
                        startListening();
                    }
                });
            }
        });

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(listener);
            startListening();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        shuttingDown = true;
        if (recognizer != null) recognizer.destroy();
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }

    private void startListening() {
        if (shuttingDown || recognizer == null) return;
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Prefer Tamil+English mixed recognition where the device supports it;
        // falls back to the device default if not.
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        try {
            recognizer.startListening(intent);
        } catch (Exception ignored) {}
    }

    private final RecognitionListener listener = new RecognitionListener() {
        @Override public void onReadyForSpeech(Bundle params) {}
        @Override public void onBeginningOfSpeech() {}
        @Override public void onRmsChanged(float rmsdB) {}
        @Override public void onBufferReceived(byte[] buffer) {}
        @Override public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            // restart listening loop regardless of error (timeout, no match, etc.)
            // — but only if Jarvis isn't currently talking (TTS onDone will restart it)
            if (!justSpoke) restartSoon();
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                handleHeardText(matches.get(0));
            }
            // if handleHeardText triggered a spoken reply, TTS's onDone will restart
            // listening once Jarvis finishes talking — avoids the mic picking up
            // Jarvis's own voice as the next command
            if (!justSpoke) restartSoon();
        }

        @Override public void onPartialResults(Bundle partialResults) {}
        @Override public void onEvent(int eventType, Bundle params) {}
    };

    private void restartSoon() {
        if (shuttingDown) return;
        // small delay avoids hammering the recognizer, keeps response snappy
        new Handler(getMainLooper()).postDelayed(this::startListening, 300);
    }

    private void handleHeardText(String heard) {
        String lower = heard.toLowerCase(Locale.getDefault());

        if (!listeningForCommand) {
            if (lower.contains(WAKE_WORD)) {
                listeningForCommand = true;
                // if the wake word was said together with the command in one breath,
                // process whatever came after it immediately
                int idx = lower.indexOf(WAKE_WORD);
                String rest = heard.substring(idx + WAKE_WORD.length()).trim();
                if (!rest.isEmpty()) {
                    listeningForCommand = false;
                    commandProcessor.process(rest);
                } else {
                    speak("Yes boss.");
                }
            }
            // not the wake word — ignore, keep listening
            return;
        }

        // we already heard "Jarvis" on a previous turn — this is the command
        listeningForCommand = false;
        commandProcessor.process(heard);
    }

    @Override
    public void speak(String text) {
        updateNotification(text);
        if (ttsReady && tts != null) {
            justSpoke = true;
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_utt");
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Jarvis", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private android.app.Notification buildNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Jarvis")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String text) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIF_ID, buildNotification(text));
    }
}
