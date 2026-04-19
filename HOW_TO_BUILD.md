# How to Build the Remindly APK

Follow these steps on your PC to turn this project into an installable APK for your Android phone.

---

## Step 1 – Install Android Studio

1. Go to: https://developer.android.com/studio
2. Download and install **Android Studio Hedgehog (2023.1)** or newer.
3. During setup, let it install the **Android SDK** (choose default settings).

---

## Step 2 – Open the Project

1. Extract the `Remindly.zip` you downloaded.
2. Open **Android Studio**.
3. Click **File → Open** and select the extracted `Remindly` folder.
4. Wait for Gradle to sync (bottom progress bar). This may take 2–5 minutes on first open.

---

## Step 3 – Fix local.properties (if needed)

If Android Studio shows an SDK path error:

1. Open `local.properties` (in the root of the project).
2. Replace the `sdk.dir` line with your actual Android SDK path.

   **Windows example:**
   ```
   sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
   ```
   **Mac example:**
   ```
   sdk.dir=/Users/YourName/Library/Android/sdk
   ```

   > Tip: In Android Studio go to **File → Project Structure → SDK Location** to find your SDK path.

---

## Step 4 – Sync Gradle

1. Click **File → Sync Project with Gradle Files**.
2. Wait for "BUILD SUCCESSFUL" in the Build output panel.
3. If you see dependency errors, click **Try Again** or **Sync Now**.

---

## Step 5 – Build the APK

1. In the top menu click: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for the build to finish (usually 1–3 minutes).
3. A popup will appear saying **"APK(s) generated successfully"** → click **locate** to find it.

   The APK is at:
   ```
   Remindly/app/build/outputs/apk/debug/app-debug.apk
   ```

---

## Step 6 – Install on Your Android Phone

### Option A – USB Cable (recommended)
1. On your phone: **Settings → Developer Options → USB Debugging** → Enable.
   (To enable Developer Options: **Settings → About Phone** → tap **Build Number** 7 times.)
2. Connect your phone via USB cable.
3. In Android Studio click the **▶ Run** button (green triangle) — choose your phone.
4. The app will install and launch automatically.

### Option B – Transfer APK manually
1. Copy `app-debug.apk` to your phone (via USB cable, WhatsApp, Google Drive, etc.).
2. On your phone open the APK file.
3. If prompted, enable **"Install from unknown sources"** for your file manager app.
4. Tap **Install**.

---

## Step 7 – Grant Permissions on First Launch

When you first open Remindly:

1. **Allow notifications** – tap "Allow" on the notification permission popup.
2. **Allow exact alarms** – tap "Grant access" and enable **"Alarms & reminders"** for Remindly.
   - This is required for reminders to fire at the exact time you set.
   - Without it, reminders may be delayed by a few minutes.

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Gradle sync fails | Check internet connection; click **Sync Now** again |
| "SDK not found" error | Fix `sdk.dir` in `local.properties` (Step 3) |
| Build fails with KSP error | Go to **File → Invalidate Caches → Invalidate and Restart** |
| App installs but no sound | Open phone Settings → Apps → Remindly → Notifications → enable sound |
| Reminder didn't fire on time | Grant exact alarm permission (Settings → Apps → Remindly → Alarms & Reminders) |
| App crashes on Android 14+ | Grant exact alarm permission from the in-app prompt |

---

## Minimum Requirements
- Android phone running **Android 8.0 (API 26)** or higher
- PC with 8 GB RAM and ~10 GB free disk space for Android Studio + SDK
