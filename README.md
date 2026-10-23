# WhatSpent (WhatSpent) — Low-Battery WhatsApp Conversation Time Tracker

**WhatSpent** (WhatSpent) is a native Android application built in **Kotlin** with **Jetpack Compose**, **Material 3 (Material You)**, and **Room Database**. It tracks the exact active screen time spent on each individual WhatsApp (`com.whatsapp`) conversation with near-zero battery drain, featuring interactive Material 3 charts, a chronological session timeline, per-chat daily usage limits, biometric app lock, and an optional draggable floating overlay pill.

---

## 1. Core Architecture & Tech Stack

- **Language**: Kotlin 2.0 (Coroutines, `StateFlow`, `Flow`)
- **UI Framework**: Jetpack Compose + Material 3 (Dynamic Color on Android 12+, Dark/Light themes, spring-physics animated charts)
- **Architecture**: Clean MVVM Architecture (`UI -> ViewModel -> Repository -> Room DAO / DataStore / Accessibility Engine`)
- **Local Persistence**:
  - **Room Database** (`WhatSpentDatabase`) with indexed tables (`chat_sessions`, `chat_metadata`)
  - **Jetpack DataStore + SharedPreferences cache** (`AppPreferencesRepository`) for zero-latency service configuration flags
- **Security & Privacy**:
  - **100% Offline**: WhatSpent does **not** request `android.permission.INTERNET`. All analytics remain strictly on-device.
  - **Biometric App Lock**: Integrated `androidx.biometric.BiometricPrompt` supporting Fingerprint, Face Unlock, and Device PIN/Pattern.
- **SDK Support**: `minSdk = 26` (Android 8.0) | `targetSdk = 35` (Android 14/15)

---

## 2. Low-Battery Tracking Engine (`WhatsAppTrackerService`)

### Strict Package & Event Filtering
Configured in [`accessibility_service_config.xml`](app/src/main/res/xml/accessibility_service_config.xml) and enforced in [`WhatsAppTrackerService.kt`](app/src/main/java/com/whatspent/app/service/WhatsAppTrackerService.kt):
- **Package Filter**: Strictly restricted to `com.whatsapp`. Events from other apps are ignored at the OS bridge level, except top-level window switches used to detect leaving WhatsApp.
- **Selective Event Types**: Listens only to `TYPE_WINDOW_STATE_CHANGED` and throttled `TYPE_WINDOW_CONTENT_CHANGED` (`>= 350ms` debounce while inside an already-identified conversation) to prevent CPU wakeups during fast scrolling or typing.

### Multi-Strategy Heuristic Conversation Detection
WhatsApp frequently obfuscates view IDs across releases. `WhatsAppTrackerService` uses a resilient two-tier detection pipeline:
1. **Non-Conversation Activity Guard**: Immediately ends any running session if the window class transitions to `HomeActivity`, `Main`, `Settings`, `ProfileInfoActivity`, `StatusPlaybackActivity`, `VoipActivity`, or if the main tab strip (`Chats`, `Updates`, `Communities`, `Calls`) is visible without a message compose bar.
2. **Primary View ID + Hierarchical Fallback**:
   - First inspects known toolbar title IDs (`com.whatsapp:id/conversation_contact_name`).
   - Falls back to structural verification: confirms the presence of a bottom conversation compose region (`EditText` / message entry layout in the bottom 35% of the screen) and extracts the primary header `TextView` from the top toolbar container while filtering out status subtitles (`online`, `typing...`, `last seen...`, `tap here for contact info`).

### Active Time Only & Battery Preservation
- **Screen State Receiver**: Registers a dynamic `BroadcastReceiver` for `Intent.ACTION_SCREEN_OFF`, `Intent.ACTION_SCREEN_ON`, and `Intent.ACTION_USER_PRESENT`. Turning the screen off immediately flushes and stops the active session timer.
- **In-Memory Session Accumulation & 60s Checkpointing**:
  - [`ActiveSessionManager.kt`](app/src/main/java/com/whatspent/app/service/ActiveSessionManager.kt) tracks elapsed session time in memory.
  - Writes to Room flash storage **only** when a conversation session ends or at periodic **60-second checkpoints** during long sessions.
  - **Adaptive Ticker**: When the Floating Pill is disabled, the coroutine loop sleeps in multi-second/minute intervals rather than ticking every second. When completely outside a WhatsApp conversation, **zero** background timers or polling loops run.
  - **No WakeLocks**: Never holds a `WakeLock`, allowing the CPU to enter deep sleep immediately when the screen turns off.

---

## 3. Database Schema (Room)

### `ChatSessionEntity` (`chat_sessions`)
| Column | Type | Details |
| :--- | :--- | :--- |
| `id` | `Long` | Primary Key (`autoGenerate = true`) |
| `chatName` | `String` | Indexed contact or group title |
| `startTime` | `Long` | Session start timestamp (epoch ms) |
| `endTime` | `Long` | Session end timestamp (epoch ms) |
| `durationMs` | `Long` | Active foreground duration in milliseconds |
| `date` | `String` | Indexed `yyyy-MM-dd` string for fast daily aggregation |

### `ChatMetadataEntity` (`chat_metadata`)
| Column | Type | Details |
| :--- | :--- | :--- |
| `chatName` | `String` | Primary Key |
| `isExcluded` | `Boolean` | When `true`, conversation is ignored by the tracker |
| `dailyLimitMinutes` | `Int?` | Optional daily time budget in minutes |
| `customAlias` | `String?` | Optional custom display name in UI & notifications |
| `colorTag` | `Int?` | Custom ARGB tint for Donut & Bar charts |

---

## 4. Building & Installing the APK

### Option A: Android Studio (Recommended)
1. Open **Android Studio Ladybug (2024.2.1)** or newer.
2. Select **Open** and choose the `nodistractions` project directory.
3. Let Gradle sync complete (uses **JDK 17** and **Android SDK 35**).
4. Connect an Android device (API 26+) or start an emulator and click **Run (`Shift + F10`)**.

### Option B: Command Line (`gradlew`)
1. Ensure `JAVA_HOME` points to **JDK 17** and `ANDROID_HOME` points to your Android SDK installation.
2. Build the debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
   *(On Windows PowerShell: `.\gradlew.bat assembleDebug`)*
3. Install the generated APK onto a connected device via `adb`:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 5. Granting Required Permissions

When you launch **WhatSpent** for the first time, the built-in **Onboarding & Permission Wizard** (and the **Settings -> Service & Permissions Health** panel) provides one-tap buttons for each step:

1. **Accessibility Service (`WhatsAppTrackerService`)** *(Required)*:
   - Tap **Enable Accessibility Service** in WhatSpent (or navigate to **System Settings -> Accessibility -> Installed Services -> WhatSpent**).
   - Toggle **Use WhatSpent** ON and confirm the system dialog.
   - *Note for Android 13+ sideloaded APKs*: If Android displays *"Restricted setting"*, open **System Settings -> Apps -> WhatSpent -> ⋮ (three-dot menu in top right) -> Allow restricted settings**, authenticate with your PIN/biometrics, and then enable the Accessibility Service.
2. **Notification Permission (`POST_NOTIFICATIONS`)** *(Required on Android 13+)*:
   - Grant notification permission when prompted so WhatSpent can display the silent **WhatSpent Active** foreground summary (*"Today: Xh Ym | Top: [Chat Name] (Zh Wm)"*) and heads-up **Daily Limit Alerts**.
3. **Battery Optimization Exemption** *(Recommended)*:
   - Tap **Whitelist Battery Optimization** in WhatSpent and select **Allow** on the system prompt (`ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) so OEM battery savers do not terminate the Accessibility Service.
4. **Display Over Other Apps (`SYSTEM_ALERT_WINDOW`)** *(Optional — for Floating Pill)*:
   - Enable the **Floating Overlay Pill** switch in Settings and grant **Display over other apps** permission to view a compact, draggable real-time timer pill while inside WhatsApp chats.
