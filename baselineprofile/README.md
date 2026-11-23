# Baseline profile and benchmarks

The app ships a baseline profile: the code of a typical session, compiled when the app is installed instead of running
un-optimised until Android compiles the app in the background (overnight while charging). Without it, a fresh
sideloaded release APK starts slower and scrolls heavier for a while.

All runs use the app's `qa` build (release code, package `ch.opumlabs.opumbrowser.qa`), never the installed app.
The release variants of this module are switched off on purpose (`androidComponents` block in `build.gradle`).

## Regenerate the profile

Emulator or phone connected (Android 13+; an emulator is fine for the profile):

```bash
./gradlew :app:generateBaselineProfile
```

The journey (`Journey.kt`, `BaselineProfileGenerator.kt`): start (skips the onboarding on a fresh install), open a
Wikipedia page, fling it down and up, open the tab overview and scroll it, open the main menu. The result is written to
`app/src/main/generated/baselineProfiles/baseline-prof.txt` (and `startup-prof.txt`) and goes into every build. Commit
the regenerated files. Regenerate after larger changes to start-up, the page, the tab overview or the menu.

`app/src/main/baseline-prof.txt` holds hand-written rules for the page path (2026-11-21); both files are merged.

## Measure

```bash
./gradlew :baselineprofile:connectedBenchmarkQaAndroidTest
```

`Benchmarks.kt`: cold start and page scrolling, each without compilation (a fresh install) and with the profile.
Results are printed and written to `baselineprofile/build/outputs/connected_android_test_additional_output/`.
Emulator numbers are only good for comparing the two modes; real numbers need a phone.

The journey finds the bottom toolbar buttons by position inside `web_bot_view` (the default bar: back, forward, home,
bookmarks, tabs, menu), because the buttons have no accessibility labels yet.
