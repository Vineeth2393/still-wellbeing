# Still — a digital wellbeing app

A small Android app that tracks phone pickups, phone-free breaks, and time
spent in each app — entirely on-device, no account, no cloud, no ads.

This is a real, from-scratch Android Studio project (Kotlin + Jetpack
Compose). **It has been built and run for real** — once Android Studio was
installed, I compiled it from the command line with its bundled JDK, then
installed it on a test emulator, granted usage access, locked/unlocked the
device, and confirmed the pickup counter, the break calculation, and the
partial-tracking-day notice all behave correctly against real Android system
events (one bug turned up in that pass — a false "same as yesterday" on day
one — and is already fixed). Opening it in Android Studio should be routine.

## 1. Install Android Studio

Download and install **Android Studio** (the free, official IDE for Android)
from [developer.android.com/studio](https://developer.android.com/studio).
It bundles everything else you need — a JDK, the Android SDK, and Gradle —
so there's nothing else to install separately.

## 2. Open the project

Open Android Studio → **File → Open** → select this folder
(`StillWellbeing`). Let it "Sync" (a progress bar at the bottom). The
dependency versions (AGP 9.4.0, Gradle 9.7.1, Room 2.8.5, Compose BOM
2026.09.00, compileSdk/targetSdk 37) are ones I confirmed actually resolve
and compile together against what's currently on this machine — so sync
should go smoothly, but if Android Studio still offers a version upgrade,
accepting it is fine.

If it reports the Gradle wrapper is missing a jar file, that's expected (I
couldn't generate that binary file by hand) — Android Studio will offer to
regenerate it automatically; accept that.

## 3. Run it

You'll need somewhere to run the app:

- **Easiest: a virtual device.** In Android Studio, **Tools → Device
  Manager → Create Device**, pick any recent phone profile, and pick a
  system image for **Android 14 or 15**. Then press the green ▶ Run button
  in the toolbar with that device selected.
- **Or: your own Android phone.** Enable Developer Options (Settings → About
  phone → tap "Build number" 7 times), then enable USB debugging inside the
  new Developer Options menu, and plug the phone in. It'll appear in the
  device dropdown next to the Run button.

The very first launch will show a permission explanation screen and a button
to Android's own Settings — Still's core feature (pickups, per-app time)
depends on the "Usage access" special permission, which Android only lets
you grant from system Settings, not from a normal in-app popup. Find "Still"
in that list and switch it on, then come back.

## What's built (v1 scope)

- **Today**: pickups today, longest completed phone-free break + your most
  recent completed break, time in your top apps, and optional daily goals
  with quiet progress bars (never red warnings).
- **Insights**: a 7-day pickup trend and personal records (longest break,
  fewest pickups in a full day) — computed only from fully-elapsed tracked
  days, never estimated.
- **Settings**: optional goals, an all-day vs. waking-hours toggle for break
  calculations (kept as separate histories, per the design notes), a
  System/Light/Dark theme picker, and a delete-history control.
- Honest empty/partial states throughout — a day that started tracking
  partway through is labeled as partial, never silently treated as a full
  day with fewer pickups.

## How pickups are detected

A "pickup" is defined as the system's own `KEYGUARD_HIDDEN` usage event —
the moment your lock screen goes away — read through
`UsageStatsManager`, the same on-device API (and the same "Usage access"
permission) that also supplies per-app time. This needs **Android 10 (API
29)+** and was chosen deliberately over a background service: it costs no
battery, needs no persistent notification, and is exactly what Android's own
Digital Wellbeing uses.

## Project structure

```
app/src/main/java/com/stillwellbeing/app/
  data/usage/       UsageStatsManager access (pickups, per-app time)
  data/settings/     DataStore-backed user preferences (goals, theme, ...)
  data/local/        Room database — one row per finalized day
  domain/            Pure logic: break calculation, core models
  repository/        TrackingRepository — the one place that combines it all
  worker/            WorkManager job that finalizes each day into Room
  ui/theme/          Colors/type ported from the design mockup, light + dark
  ui/today, insights, settings, permission, navigation, components
```

`StillApplication.kt` is a small hand-rolled service locator (no
Hilt/Koin) — there's exactly one of everything, so a DI framework would add
ceremony without adding safety. Each screen builds its own ViewModel from
`(application as StillApplication)`.

## Deliberately deferred (per the agreed roadmap)

These weren't cut by accident — they're genuinely more Android-specific and
better tackled once the basics are running on your phone:

- **App-detail screens** (tap an app for its own 7-day chart) — needs a new
  Room table for per-app daily history, not just per-day totals.
- **Home-screen widget** — a different Android surface entirely
  (`AppWidgetProvider` + `RemoteViews`), showing "best break today" as a
  static, timestamped snapshot rather than a live timer.
- **Hourly pickup pattern & weekly summary notification** — genuinely
  optional polish on top of a working core.
- **App blocking, instant alerts, cloud sync** — out of scope for a
  wellbeing app in the way we discussed (no enforcement, no accounts).

## A couple of honest caveats

- The app icon and in-app illustrations are simple hand-drawn vector shapes,
  not the fuller illustration set from the design mockup — kept deliberately
  minimal so the first build is about the app working, not asset production.
- One environment quirk from testing, worth knowing: a bare-bones test
  emulator with **no lock screen configured at all** never fires the
  `KEYGUARD_HIDDEN` event Still counts pickups from — because there's no
  keyguard to hide. Any real phone with a PIN, pattern, or even plain "swipe"
  security is unaffected (confirmed directly — pickups counted correctly the
  moment a lock screen was set). Only a phone with security set to "None"
  would undercount, which is rare and would equally affect Android's own
  Digital Wellbeing, since it uses the same system event.
- Kotlin note if you look at the build files: AGP 9's compiler now has Kotlin
  support built in, so there's no separate `org.jetbrains.kotlin.android`
  plugin — that's not an oversight, it's current (2026) practice.
