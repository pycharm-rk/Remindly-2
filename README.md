# Remindly – Offline Reminder App for Android

A fully offline Android reminder app built with Kotlin + Jetpack Compose.
Features a Microsoft Teams–inspired glassy light UI theme.

## Features
- ✅ Create, edit, and delete reminders
- ✅ Date & time picker for any future date (hours to 1 year ahead)
- ✅ Exact local notification with sound at the scheduled time
- ✅ 6 built-in reminder sounds + phone default sound option
- ✅ Fully offline — all data stored locally with Room database
- ✅ Auto-reschedules reminders after phone reboot
- ✅ Upcoming / Completed tabs with overdue indicator
- ✅ Teams-inspired glassy light theme (blue-lilac accents, rounded cards)

## Sound Options
| Key | Sound |
|-----|-------|
| Phone Default | System notification sound |
| Soft Bell | Gentle 880 Hz bell with decay |
| Clear Ping | Crisp 1200 Hz short ping |
| Gentle Chime | Two-tone ascending chime |
| Digital Alert | Pulsed triple beep |
| Warm Tone | Low mellow 440 Hz tone |
| Urgent Beep | Rapid four-beep alert |

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Database**: Room (local device storage)
- **Scheduling**: AlarmManager + BroadcastReceiver
- **Notifications**: NotificationManager + NotificationChannel
- **Boot recovery**: RECEIVE_BOOT_COMPLETED BroadcastReceiver

---

## How to Build the APK → See HOW_TO_BUILD.md
