# 🏪 Play Store par app kaise daalein

## 1. Signing secrets (ek baar)
`signing/GITHUB_SECRETS.txt` me diye 4 secrets GitHub repo me add karein:
Repo → **Settings → Secrets and variables → Actions → New repository secret**.
Phir Actions tab me build chalega aur Release me **LiveCallTranslator-playstore.aab** aa jayega.

## 2. Google Play Console account
https://play.google.com/console → $25 (ek baar) → identity verification (Aadhaar/PAN, 1–3 din).

## 3. App banayein
Create app → Name: *Live Call Translator – Indian* → App, Free.

## 4. Store listing & policies
`playstore/LISTING.md` se text copy karein. Graphics:
- App icon: `playstore/icon-512.png`
- Feature graphic: `playstore/feature-graphic-1024x500.png`
- Phone screenshots (2–8): apne phone par app ke 3–4 screenshot lein.
Privacy policy: https://livecalltranslator-relay.onrender.com/privacy

## 5. Testing → Closed testing (naye personal account ke liye ZAROORI)
- Release banayein → `LiveCallTranslator-playstore.aab` upload.
- "Play App Signing" accept karein.
- Testers: **kam se kam 12 log** (Gmail IDs) jo **14 din lagaataar** opt-in rahein.
- 14 din baad: Dashboard → **Apply for production** → Production me release.

## 6. Har update par
Code push → naya AAB Release me → Play Console → new release → upload. (versionCode apne aap badhta hai.)
