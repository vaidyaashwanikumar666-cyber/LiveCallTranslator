# 🌐 Live Call Translator (Android)

Aap **Hindi** boliye → saamne wala **Marathi** me sunega. Woh Marathi bole → aap Hindi me sunenge.
23 Indian bhashayein: Hindi, Marathi, Bengali, Gujarati, Tamil, Telugu, Kannada, Malayalam, Punjabi, Odia,
Urdu, Assamese, English, Nepali, Konkani, Maithili, Sanskrit, Sindhi, Kashmiri, Dogri, Manipuri, Bodo, Santali.

## ⚠️ Zaroori baat
Android kisi bhi normal app ko **SIM call ki audio** padhne/badalne nahi deta (Android 10+ me sirf system apps ko).
Isliye app 2 mode me kaam karta hai:

| Mode | Kaise | Kya chahiye |
|---|---|---|
| 📞 **Interpreter Mode** | Normal call **speaker** par. Aap bolein → neela button; woh bolein → hara button. App translation zor se bolta hai jo call me dusri taraf jaata hai. | Sirf aapke phone me app |
| 🔁 **App-to-App Call** | Dono phone me app, same **room code**. Bilkul hands-free: aap bolte raho, dusre phone par turant unki bhasha me bola jata hai. | Dono phone me app + relay server |

## 🧠 Agent kaise kaam karta hai
1. **Sunna** – Android SpeechRecognizer (Google) aapki bhasha me speech → text
2. **Translate** – Google ML Kit (offline, free) ya **Sarvam AI** (online, sabhi 22 bhasha)
3. **Bolna** – Android Text-to-Speech, sunne wale ki bhasha me
4. Jab app bol raha ho to mic pause hota hai, taaki apni hi awaaz translate na ho.

Offline (bina API key): Hindi, Marathi, Bengali, Gujarati, Tamil, Telugu, Kannada, Urdu, English.
Baaki bhashaon ke liye: https://www.sarvam.ai se free API key lekar app Settings me daalein.

## 📦 APK kaise banayein
**Option A – GitHub (PC ki zaroorat nahi):**
1. GitHub par naya repo banayein aur yeh folder upload/push karein.
2. **Actions** tab → "Build APK" apne aap chalega (~5 min).
3. Run kholein → neeche **LiveCallTranslator-apk** download → phone me install karein.

**Option B – Android Studio:** folder open karein → ▶ Run.

## ☁️ Relay server (sirf App-to-App mode ke liye) – 1 click setup
[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/vaidyaashwanikumar666-cyber/LiveCallTranslator)

1. Upar wala button dabayein → **GitHub se Sign in** karein (free, card nahi chahiye).
2. **Apply / Deploy Blueprint** dabayein. 2-3 minute me server live.
3. Agar URL `https://livecalltranslator-relay.onrender.com` hai to app me kuch nahi badalna – woh default hai.
   Alag URL mile to app Settings me "Call server URL" me woh daal dein.

Note: free server 15 min khaali rehne par so jaata hai; pehla connect ~1 minute le sakta hai, uske baad turant.

Local test: `cd server && npm install && npm start` → phone me `ws://<PC-ka-IP>:8080`.

## 📱 Use kaise karein
1. App kholein → Meri bhasha: Hindi, Saamne wale ki: Marathi.
2. "Offline language pack download" dabayein (ek baar, Wi-Fi par).
3. App-to-App: **🎲 Naya code** → **📤 Invite bhejo** (WhatsApp par code jayega) → Join. Dost bhi wahi code daal kar Join kare.
4. Earphones lagayein to echo kam hoga.

Tips: Awaaz na aaye to Phone Settings → Text-to-speech → Google → bhasha ka voice data install karein.
Speech pakad me na aaye to Google app update karein aur Settings → Google → Voice → Offline speech me bhasha download karein.
