Jarvis Assistant — Android app
================================

Fully free — no AI subscription, no API key, no internet cost, no signup.
Only uses your phone's built-in speech recognition (Google's on-device/free
service) and does everything else locally.

Note on battery: Jarvis listens continuously for the word "Jarvis" so it
reacts instantly — this does use noticeably more battery and can make the
phone warm during long use, since there's no low-power dedicated wake-word
chip involved (that would need a paid/signup service). Disable battery
optimisation for the app so Android doesn't kill it, and expect it to use
more battery than a normal app if left running all day.

What it does
------------
Runs quietly in the background. Say "Jarvis" and then your command, in
English, Tamil, or mixed (Thanglish):

  - "Jarvis, Suresh ku call pannu"        -> calls the contact "Suresh"
  - "Jarvis, call attend pannu"           -> answers a ringing call
  - "Jarvis, call cut pannu"              -> ends the current call
  - "Jarvis, Suresh ku whatsapp la naan     -> opens WhatsApp with the message
     late varen nu message anuppu"           ready — you tap Send
  - "Jarvis, phone lock pannu"            -> locks the screen
  - "Jarvis, map podu Coimbatore"         -> opens Google Maps navigation
  - "Jarvis, insta open pannu" /            -> opens Instagram, or any other
     "Jarvis, whatsapp open pannu"           installed app by its name
  - "Jarvis, veliya vaanum"               -> stops Jarvis
  - anything else                         -> says "Purila boss" and waits
                                              for a clearer command

Building the APK
-----------------
Push this project to GitHub — the included .github/workflows/build.yml
builds a debug APK automatically (Actions tab -> latest run -> Artifacts).
No setup needed before building — no API key to fill in.

First run on the phone
-----------------------
1. Install the APK, open the app once.
2. Tap "1. Grant Permissions" and allow everything asked.
3. Tap "2. Enable Screen Lock Control" and confirm (needed only for the
   "lock the phone" command).
4. Tap "3. Start Jarvis" — a persistent notification shows Jarvis is
   listening. Keep the app open in the background (don't force-stop it,
   and disable battery optimisation for it in phone Settings so Android
   doesn't kill the listening service).
