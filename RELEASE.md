# Release checklist

State of 2026-11-21: version 1.0.0 (versionCode 1), minSdk 32 (Android 12), targetSdk 37.

Release candidate checked 2026-11-23 (everything except signing): `assembleRelease` builds (unsigned without
`keystore.properties`, 29.6 MB APK), baseline profile regenerated, 114 unit + 6 device tests green, release code (qa)
monkey seeds 601–610 without crash or ANR, cold start on the emulator about 0.83–1.08 s (landscape; portrait not timed).
Glide 5.0.9 → 5.0.10. What is left before uploading: the signing key, the privacy policy URL and the store forms below. Planned channels: Google
Play, own website (APK), F-Droid. No crash reporting, no analytics, no ads: the app collects no personal data.

## Before every release

- [ ] `versionCode` +1 (every upload needs a higher one) and `versionName` in `app/build.gradle`;
      `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` with the changes.
- [ ] Baseline profile regenerated after bigger changes: `./gradlew :app:generateBaselineProfile` (see
      `baselineprofile/README.md`).
- [ ] Tests: `./gradlew :app:testDebugUnitTest`, `./gradlew :app:connectedUitestAndroidTest` (emulator), stability run
      on the release code: `./gradlew :app:assembleQa` + `tools/monkey/qa_run.py`.
- [ ] Signed build: copy `keystore.properties.example` to `keystore.properties` (git-ignored) and fill it in, then
      `./gradlew :app:bundleRelease` (Google Play, AAB) and / or `./gradlew :app:assembleRelease` (APK).

## Once, before the first release

- [x] Legal texts `app/src/main/assets/notice/privacy_en.txt` and `tos_en.txt` rewritten 2026-11-21 (OPUM-Labs,
      Switzerland, Swiss law; shown in every app language). Still advisable: a check by someone with legal knowledge.
- [ ] Put the privacy policy online (Google Play needs a public URL), e.g. on opum.ch.
- [ ] Signing key: create one keystore and keep it (and its passwords) safe and backed up. An app can only be updated
      with the same key, and users can switch between channels only if the APKs are signed with the same key. On
      Google Play, Play App Signing holds the key that signs what Play delivers: to keep one signature for Play and the
      website, upload your own app signing key to Play instead of letting Google generate one.
- [ ] Translations (added 2026-11-21 by Claude for 17 languages): a native-speaker look, at least at the longer hints.

## Google Play

- [ ] App content, Data safety: data collected: none; data shared: none (the app sends nothing to us; connections to
      the sites you open, search, filter lists, Google News / Translate on request are your browsing, not collection
      by the app).
- [ ] Permission declarations:
  - `QUERY_ALL_PACKAGES`: core function web browser (open links in the app that handles them, list font apps).
  - `REQUEST_INSTALL_PACKAGES`: web browser that downloads files, including APKs the user chooses to install.
  - Foreground service type `dataSync` (download manager; on Android 14+ a user-initiated transfer job instead):
    short video of a download running.
- [ ] Content rating questionnaire (a browser: unrestricted internet access).
- [ ] Store listing from `fastlane/metadata/android/en-US/` (title, short and full description); screenshots and
      feature graphic still to make.

## Own website

- [ ] Publish the signed APK together with its SHA-256 certificate fingerprint
      (`apksigner verify --print-certs app-release.apk`).
- [ ] The app has no update check of its own: tell users where to find new versions (or add a check later).

## F-Droid (blockers)

F-Droid builds the app itself from public source and accepts only free software. Open points:

1. **License**: the repository needs a free / open-source license and public source code.
2. **`libmyutil.so`** (in `app/src/main/jniLibs`) is a prebuilt library with no source in the
   repository (page scripts, the old backup key). F-Droid does not accept it: its functions have to move to code that
   is built from source.
3. **Google Play services** (`play-services-basement`, `-tasks`; used to update the security provider): proprietary.
   An `fdroid` build flavor without them is needed.
4. `libopumadblock.so` is built from `native/adblock-jni` (Rust): F-Droid can build that; the build recipe has to be
   written for it.

The store texts in `fastlane/metadata/` are already in the format F-Droid reads.
