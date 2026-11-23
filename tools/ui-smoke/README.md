# ui-smoke

`smoke.py` opens the app's main screens on the running emulator (start page, main menu, settings hub, General,
Advanced, Composition), saves a screenshot of each, checks the crash log, and puts the app's data back exactly as it
was (checked with md5 sums made on the device). Exit code 0 = fine.

    python3 tools/ui-smoke/smoke.py [--install] [--out DIR]

Compose UI tests of shared components are in `app/src/androidTest`. Run them without uninstalling the app (Gradle's
`connectedDebugAndroidTest` uninstalls it afterwards, which wipes the emulator's app data):

    ./gradlew installDebug installDebugAndroidTest
    adb shell am instrument -w ch.opumlabs.opumbrowser.test/androidx.test.runner.AndroidJUnitRunner

JVM unit tests (settings store, app state): `./gradlew testDebugUnitTest`.
