#!/usr/bin/env python3
"""Emulator smoke test of the app (2026-11-17). Opens the main screens, takes a screenshot of each, checks the crash log,
and leaves the app's data exactly as it found it.

Usage:  python3 tools/ui-smoke/smoke.py [--install] [--out DIR]
  --install   run `./gradlew installDebug` first (from the project root)
  --out DIR   where the screenshots go (default: a new folder in the system temp folder)

Steps: save the app data (databases, files, shared_prefs) as a tar on this computer and an md5 list of every file on the
device; start the app; open the main menu, the settings hub and a few settings pages; check the crash buffer; stop the
app; delete the files the run created; write the saved data back; compare the md5 list again. Buttons are found by
their text through `uiautomator dump` and, where they have no text, by a position relative to the screen size.

Lessons built in (see the memory notes of 2026-11-17): the md5 list is made on the device, because the Mac's tar hides
`._` files on extraction; every adb call gets stdin from /dev/null, because adb shell otherwise eats the rest of a loop's
input. The run never toggles secret mode or changes a setting.
Exit code: 0 = all screens opened, no crash, data the same; 1 = something failed (printed)."""
import os, re, subprocess, sys, tempfile, time, xml.etree.ElementTree as ET

ADB = os.path.expanduser('~/Library/Android/sdk/platform-tools/adb')
APP = 'ch.opumlabs.opumbrowser'
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DATA = 'databases files shared_prefs'


def adb(*args, binary=False, stdin=None):
    r = subprocess.run([ADB, *args], capture_output=True, stdin=stdin if stdin is not None else subprocess.DEVNULL)
    return r.stdout if binary else r.stdout.decode(errors='replace')


def sh(cmd):
    return adb('shell', cmd)


def file_md5s():
    """{path: md5} of every app data file, worked out on the device."""
    out = sh(f"run-as {APP} sh -c 'find {DATA} -type f -exec md5sum {{}} +'")
    res = {}
    for line in out.splitlines():
        parts = line.split(None, 1)
        if len(parts) == 2:
            res[parts[1].strip()] = parts[0]
    return res


def all_paths():
    return set(p.strip() for p in sh(f'run-as {APP} find {DATA}').splitlines() if p.strip())


def screen_size():
    m = re.search(r'(\d+)x(\d+)', sh('wm size'))
    return (int(m.group(1)), int(m.group(2))) if m else (1080, 2400)


def nodes():
    sh('uiautomator dump /sdcard/smoke_ui.xml')
    xml = adb('exec-out', 'cat', '/sdcard/smoke_ui.xml')
    sh('rm /sdcard/smoke_ui.xml')
    try:
        return ET.fromstring(xml).iter('node')
    except ET.ParseError:
        return iter(())


def tap_text(text, tries=3):
    """Taps the first element whose text or description is [text]; True when found."""
    for _ in range(tries):
        for n in nodes():
            if text in (n.get('text'), n.get('content-desc')):
                x1, y1, x2, y2 = map(int, re.findall(r'\d+', n.get('bounds')))
                sh(f'input tap {(x1 + x2) // 2} {(y1 + y2) // 2}')
                time.sleep(2)
                return True
        time.sleep(1)
    return False


def tap_rel(fx, fy):
    w, h = screen_size()
    sh(f'input tap {int(w * fx)} {int(h * fy)}')
    time.sleep(2)


def shot(out, name):
    path = os.path.join(out, name + '.png')
    with open(path, 'wb') as f:
        f.write(adb('exec-out', 'screencap', '-p', binary=True))
    return path


def main():
    install = '--install' in sys.argv
    out = sys.argv[sys.argv.index('--out') + 1] if '--out' in sys.argv else tempfile.mkdtemp(prefix='ui-smoke-')
    os.makedirs(out, exist_ok=True)
    problems = []

    if install:
        r = subprocess.run(['./gradlew', '-q', 'installDebug'], cwd=ROOT)
        if r.returncode:
            print('install failed'); return 1

    # 1. save the data
    sh(f'am force-stop {APP}')
    saved = os.path.join(out, 'appdata.tar')
    with open(saved, 'wb') as f:
        f.write(adb('exec-out', 'run-as', APP, 'tar', '-cf', '-', *DATA.split(), binary=True))
    paths_before, md5_before = all_paths(), file_md5s()
    print(f'saved {len(md5_before)} files to {saved}')

    # 2. the screens
    sh('logcat -c -b crash')
    sh(f'monkey -p {APP} -c android.intent.category.LAUNCHER 1')
    time.sleep(8)
    shot(out, '01_start')
    tap_rel(0.917, 0.948)                      # main menu (bottom right, no text)
    shot(out, '02_menu')
    # settings start page regrouped 2026-11-19: Advanced and Composition became Privacy / Toolbars & controls
    steps = [('Settings', '03_settings'), ('General', '04_general'), (None, None),
             ('Privacy', '05_privacy'), (None, None), ('Appearance', '06_appearance'), (None, None)]
    if not tap_text('Settings') and not tap_text('Setting'):
        tap_rel(0.932, 0.585)                      # gear in the menu header
    shot(out, '03_settings')
    for text, name in steps[1:]:
        if text is None:
            sh('input keyevent BACK'); time.sleep(1.5); continue
        if tap_text(text):
            shot(out, name)
        else:
            problems.append(f'could not find "{text}"')
    for _ in range(3):
        sh('input keyevent BACK'); time.sleep(1)
    crashes = sh(f'logcat -d -b crash').count(APP)
    if crashes:
        problems.append(f'{crashes} crash log lines for the app')

    # 3. put the data back
    sh(f'am force-stop {APP}')
    time.sleep(2)
    extra = sorted(all_paths() - paths_before, reverse=True)   # deepest first, so folders are empty when removed
    for p in extra:
        if sh(f'run-as {APP} rm "{p}"').strip():
            sh(f'run-as {APP} rmdir "{p}"')
    with open(saved, 'rb') as f:
        adb('exec-in', 'run-as', APP, 'tar', '-xf', '-', stdin=f)
    sh(f'am force-stop {APP}')
    paths_after, md5_after = all_paths(), file_md5s()
    if paths_after != paths_before:
        problems.append(f'file list differs after restore: +{sorted(paths_after - paths_before)} -{sorted(paths_before - paths_after)}')
    changed = [p for p in md5_before if md5_after.get(p) != md5_before[p]]
    if changed:
        problems.append(f'file contents differ after restore: {changed}')
    print(f'removed {len(extra)} files the run created; data restored')

    print(f'screenshots in {out}')
    if problems:
        print('PROBLEMS:'); [print('  -', p) for p in problems]
        return 1
    print('OK: screens opened, no crash, data the same')
    return 0


if __name__ == '__main__':
    sys.exit(main())
