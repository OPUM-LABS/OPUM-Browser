# Random UI test runs on the emulator

Debug build installed, emulator running (1080×2400, gesture navigation).

```bash
python3 -I tools/monkey/guarded_run.py 465 466 467
```

For each seed this runs 600 touch-only monkey taps (`monkey -p ch.opumlabs.opumbrowser --pct-touch 100 …`, which keeps
the taps inside the app) and then 150 random swipes from `swipes.py`.

- **Swipes** stay away from the screen edges, so they never trigger the system Back / Home gestures. Before each batch
  the script checks that the browser is still in front and brings it back if not. The swipe driver does no taps: an adb
  tap on a page link can open another app.
- **Settings guard:** before the run it reads the app's settings (`files/.pref/*`, via `run-as`). After each seed it
  switches secret mode back to its starting state through the app's own menu if the taps changed it. At the end it
  lists every other setting the run changed.
- **Restore secret mode only:** `python3 -I tools/monkey/guarded_run.py --restore true`
- **Check crashes afterwards:** `adb logcat -d | grep -E 'FATAL EXCEPTION|Exception dispatching input|ANR in'`

## Release-like runs (`qa` build, 2026-11-21)

The `qa` build type is the release build (R8, shrunk resources, same keep rules) signed with the debug key under its own
package `ch.opumlabs.opumbrowser.qa`, so it installs next to the normal app and never touches its data:

```bash
./gradlew :app:assembleQa
adb install -r app/build/outputs/apk/qa/opum_browser-v1.0.0-qa-qa.apk
python3 -I tools/monkey/qa_run.py 501 502 503 504
```

Per seed: 600 touch-only taps and 150 edge-safe swipes (`swipes.py` with `OPUM_APP` set to the qa package), then the qa
app's crash / ANR blocks from logcat go to `tools/monkey/out/qa_<seed>.log` (git-ignored). No settings guard is needed.
The qa app is not debuggable, so `run-as` does not work on it.
