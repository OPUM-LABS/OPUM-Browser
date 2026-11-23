# OPUM Browser

A fast, private web browser for Android that you can shape to the way you hold your phone.

No account, no ads, no analytics, no crash reporting: what you do stays on your device.

If you like the app, you can support its development: [Buy me a coffee](https://buymeacoffee.com/opum_labs) ☕

## Features

**Private by design**
- Web Filter: blocks ads, trackers, pop-ups and annoying overlays (EasyList, AdGuard and more), built on
  [adblock-rust](https://github.com/brave/adblock-rust) with uBlock Origin filter syntax and scriptlets
- Incognito mode with its own tabs and an optional lock
- App lock with fingerprint or face unlock
- Encrypted backups (AES-GCM), protected by a password only you know

**Yours to shape**
- Bars at the top or the bottom, your own buttons, an editable menu with a quick bar
- Gestures on the screen edges for back, forward and switching tabs
- Light, dark and dark-for-web-pages, your own main colour, text size and font

**Tabs that stay tidy**
- Tab overview as grid, list, stack or carousel, with tab groups
- Close duplicates and old tabs in one go, or let unused tabs archive themselves
- Undo when you close a tab, and the full back history per tab

**Read, watch, listen**
- Reader mode, page and text translation, read aloud
- Video helpers: full-screen controls, speed, picture-in-picture, background playback

**Downloads and more**
- Download manager with pause, resume and notifications
- Save pages, screenshots, print to PDF
- Start page with your shortcuts and optional news

## Requirements

- Android 12 (API 32) or newer; targets API 37
- To build: Android Studio (its bundled JDK 17+ is enough), Android SDK 37

## Building

```bash
git clone https://github.com/OPUM-LABS/OPUM-Browser.git
cd OPUM-Browser
./gradlew :app:assembleDebug
```

The APK ends up in `app/build/outputs/apk/debug/` (`opum_browser-v<version>-debug.apk`).

### Build types

| Build type | Package | Purpose |
|---|---|---|
| `debug` | `ch.opumlabs.opumbrowser` | everyday development |
| `release` | `ch.opumlabs.opumbrowser` | store builds (R8, shrunk resources); signed when `keystore.properties` exists |
| `qa` | `…opumbrowser.qa` | release code signed with the debug key, installs next to the app; for stability runs and benchmarks |
| `uitest` | `…opumbrowser.uitest` | target of the instrumented tests, so they never touch an installed app's data |

### Signed release

Copy `keystore.properties.example` to `keystore.properties` (git-ignored), fill it in, then:

```bash
./gradlew :app:bundleRelease     # Google Play (AAB)
./gradlew :app:assembleRelease   # APK
```

See [RELEASE.md](RELEASE.md) for the full release checklist.

## Tests

```bash
./gradlew :app:testDebugUnitTest             # JVM unit tests
./gradlew :app:connectedUitestAndroidTest    # instrumented tests on a running emulator or device
```

More tools for the emulator:
- [tools/monkey](tools/monkey/README.md): random touch and swipe runs, also on the release code (`qa`)
- [tools/ui-smoke](tools/ui-smoke/README.md): opens the main screens, takes screenshots and checks the crash log
- [baselineprofile](baselineprofile/README.md): regenerates the baseline profile and runs the benchmarks

## Project layout

| Path | Contents |
|---|---|
| `app/src/main/java/com/mycompany/app/` | the app (Kotlin, Compose for the newer screens) |
| `app/src/main/java/com/mycompany/app/web/` | browser activity, WebView, tabs, page handling |
| `app/src/main/java/com/mycompany/app/web/filter/` | Web Filter (adblock-rust bridge, cosmetic filtering) |
| `app/src/main/java/com/mycompany/app/ui/` | Compose UI: browser chrome, settings, onboarding, theme |
| `app/src/main/java/com/mycompany/app/pref/store/` | settings stores |
| `app/src/main/assets/filter/` | built-in filter lists, site fixes and uBlock Origin resources |
| `native/adblock-jni/` | Rust JNI bridge to adblock-rust ([README](native/adblock-jni/README.md)) |
| `tools/ubo-resources/` | builds the uBlock Origin resources and site fixes ([README](tools/ubo-resources/README.md)) |
| `fastlane/metadata/` | store listing texts |

The native libraries in `app/src/main/jniLibs/` are checked in, so a normal build needs no Rust toolchain. To rebuild
the filter engine after changing `native/adblock-jni`, run `native/adblock-jni/build.sh` (needs Rust and `cargo-ndk`).

## Privacy

The app has no account, no analytics, no ads and no crash reporting. The privacy policy and terms are in
`app/src/main/assets/notice/`.

## Third-party licences

The app uses AndroidX, Jetpack Compose, Media3, Glide, jsoup, Gson, Readability, zip4j, adblock-rust and others. Their
licence texts are shipped in `app/src/main/assets/license/` and shown in the app under Settings > About > Open source
licenses.

## Support

OPUM Browser is free and has no ads. If it is useful to you, you can buy the developer a coffee:
[buymeacoffee.com/opum_labs](https://buymeacoffee.com/opum_labs). The link is also in the app under Settings > About.

## License

OPUM Browser is free software: you can redistribute it and/or modify it under the terms of the
[GNU General Public License](LICENSE) as published by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

It is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

SPDX-License-Identifier: `GPL-3.0-or-later`

---

© OPUM-Labs, Switzerland
