# My Fridge

A clean, modern **Android** app for tracking what's in your fridge, getting notified before things expire, and keeping your shopping organised — all in one place. Built with Kotlin and Jetpack Compose on an MVVM architecture, and published on the Google Play Store.

<p align="center">
  <video src="https://github.com/user-attachments/assets/7f06bf0e-1fc3-4014-a90a-da466ed11d84" width="320" controls autoplay loop muted></video>
</p>

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

## How It Works

The app follows an **MVVM architecture**. A `MainViewModel` holds the UI state and business logic and exposes it as `StateFlow`, which Jetpack Compose observes to recompose screens automatically. Data is persisted locally with a **Room** database (entity, DAO, and an `ItemRepository` interface backed by an `OfflineItemRepository`), using **KSP** for annotation processing. Screens are wired together with **Navigation Compose**, and user settings persist via Jetpack **DataStore** / SharedPreferences.

Expiry reminders are powered by **WorkManager**: a daily `CoroutineWorker` recalculates each item's days-until-expiry and fires a local notification at the user's chosen time, with a configurable lead window — scheduled so it survives app restarts and device reboots. Shared UI is factored into reusable composables (`ItemCard`, `EditCard`, `TopBar`), and the whole interface is themed with Material 3.

## Skills Demonstrated

- MVVM architecture — `ViewModel` + repository + Room, with UI state exposed as `StateFlow`
- Declarative UI — Jetpack Compose with Material 3 (Material You) theming
- Reactive state management — Compose state and `StateFlow` driving automatic recomposition
- Asynchronous concurrency — Kotlin coroutines and Flow for non-blocking data access
- Local persistence — Room database (entity, DAO, repository) with KSP annotation processing
- Repository design pattern — `ItemRepository` interface with an offline implementation
- Background processing — WorkManager `CoroutineWorker` for a daily expiry check
- Local notifications — scheduled alerts with a user-configurable lead time
- Navigation — multi-screen routing with Navigation Compose
- Preferences storage — Jetpack DataStore / SharedPreferences for settings
- Component reuse — shared composables (`ItemCard`, `EditCard`, `TopBar`)
- Material Design 3 — light/dark theming, colour scheme, and typography
- UX engineering — swipe-to-delete with undo snackbar, instant search, urgency sorting, live badges
- Lifecycle awareness — boot receiver and lifecycle-aware scheduling
- Gradle build system — Android Gradle Plugin, Compose BOM, and KSP
- Shipping to production — published on the Google Play Store

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

## Demo & Links

Get it on Google Play: **[play.google.com/store/apps/details?id=com.iamtherealgeorge.myfridge](https://play.google.com/store/apps/details?id=com.iamtherealgeorge.myfridge)**

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.iamtherealgeorge.myfridge">
    <img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="80"/>
  </a>
</p>

## Getting Started

> **Requires Android 11 (API 30) or higher.**

Download from [**Google Play**](https://play.google.com/store/apps/details?id=com.iamtherealgeorge.myfridge), or build from source:

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

Licensed under the [PolyForm Noncommercial License 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0/) — see [LICENSE](LICENSE). You may use, modify, and share this work for any non-commercial purpose with attribution, but not for commercial purposes (including selling it).
