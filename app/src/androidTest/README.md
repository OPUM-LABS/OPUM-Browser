# UI tests on a device

```bash
./gradlew :app:connectedUitestAndroidTest
```

They run against the `uitest` build type (the debug build under the package `ch.opumlabs.opumbrowser.uitest`), never
the installed app: connected tests uninstall the app under test when they finish, and the Android Test Orchestrator
clears its data before every test, so each test starts like a fresh install (onboarding included).

- `flows/Browser.kt`: start the browser (skips the onboarding), open the menu / tab overview, find things by their
  translated labels (`R.string...`), so the tests work in any device language.
- `flows/TabFlowTest.kt`: New tab shows in the overview count; close a card and undo; a card opens its tab and the
  overview closes.
- `flows/SettingsSearchTest.kt`: search "archive" in the settings, the result opens its page with the setting shown.
- `ui/components/ComponentsUiTest.kt`: shared Compose components on their own.

The JVM unit tests stay on debug: `./gradlew :app:testDebugUnitTest` (includes `BackupCryptoTest` for the password
backup format).

The tests rely on accessibility labels (toolbar buttons, overview buttons, cards). A button without one is invisible to
TalkBack and to these tests alike: give new buttons a `contentDescription`.
