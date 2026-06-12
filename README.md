# My Fridge

A clean, modern Android app for tracking what's in your fridge, getting notified before things expire, and keeping your shopping organised — all in one place.

<p align="center">
  <video src="https://github.com/user-attachments/assets/7f06bf0e-1fc3-4014-a90a-da466ed11d84" width="320" controls autoplay loop muted></video>
</p>

---

## Features

- **Fridge tracker** — add items with an expiry countdown; items sort automatically by urgency (expiring first)
- **Expiry labels** — colour-coded: grey for future, orange for today, red for expired
- **Smart notifications** — a background worker runs daily at your chosen time and alerts you when something is about to expire; configure how many days in advance you want the warning
- **Shopping list** — keep a running list with per-item quantities; swipe to remove, or tap *Move all to My Fridge* after a shop
- **Saved items** — store your regular items as templates and add them to the fridge or shopping list in one tap
- **Search** — instant filter on every list screen
- **Undo delete** — swipe to dismiss or tap the delete icon; a brief snackbar lets you undo before it's gone
- **Expiry badge** — the My Fridge tab shows a live count of items needing attention
- **Dark mode** — full Material You colour scheme, works in light and dark system themes
- **Settings** — choose your daily notification time (clock picker) and expiry warning window (0–30 days)

## Download & Install

> **Requires Android 11 (API 30) or higher.**

Download on [**Google Play**](https://play.google.com/store/apps/details?id=com.iamtherealgeorge.myfridge).

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.iamtherealgeorge.myfridge">
    <img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="80"/>
  </a>
</p>

## Tech Stack

| Layer | Technology |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Database | Room (via KSP) |
| Background work | WorkManager CoroutineWorker |
| State management | ViewModel + StateFlow |
| Persistence | DataStore / SharedPreferences |
| Build | Gradle 8.9, AGP 8.7.3 |

## Build from Source

**Prerequisites:** Android Studio Hedgehog or newer (bundles JDK 21).

```bash
git clone https://github.com/TheYellowDuck/MyFridge.git
cd MyFridge

# Open in Android Studio and press Run, or from terminal:
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
  ./gradlew installDebug
```

The app targets API 35 and requires a device or emulator running Android 11+.

## Project Structure

```text
app/src/main/java/com/example/myfridge/  (package: com.iamtherealgeorge.myfridge)
├── data/                  # Room entities, DAO, repository
├── receivers/             # (superseded by WorkManager)
├── reusables/             # Shared composables (ItemCard, EditCard, TopBar)
├── services/              # (superseded by WorkManager)
├── ui/theme/              # Material 3 colour scheme & typography
├── AlarmScheduler.kt      # WorkManager scheduling + SharedPrefs helpers
├── MainActivity.kt        # Nav host, all screen composables
├── MainViewModel.kt       # State + business logic
└── UpdateDaysWorker.kt    # Daily expiry check & notifications
```

## License

MIT
