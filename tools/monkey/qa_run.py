#!/usr/bin/env python3
"""Stability run on the release-like `qa` build (R8, own package `ch.opumlabs.opumbrowser.qa`, installed next to the
normal app; `./gradlew :app:assembleQa`, then `adb install -r app/build/outputs/apk/qa/*.apk`).

Usage: qa_run.py <seed> [<seed> ...]
For each seed: 600 touch-only monkey taps (stay in the app), then 150 edge-safe swipes (swipes.py), then the crashes
and ANRs of the qa app from logcat are written to tools/monkey/out/qa_<seed>.log (and counted on stdout).
No settings guard: the qa app holds test data only, the normal app is not touched."""
import os, re, subprocess, sys

ADB = os.path.expanduser('~/Library/Android/sdk/platform-tools/adb')
APP = 'ch.opumlabs.opumbrowser.qa'
HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, 'out')
os.makedirs(OUT, exist_ok=True)

def sh(cmd, timeout=None):
    return subprocess.run([ADB, 'shell', cmd], capture_output=True, text=True, timeout=timeout).stdout

def crashes(log):
    """The FATAL EXCEPTION / ANR blocks of the qa process."""
    blocks, cur = [], None
    for line in log.splitlines():
        if 'FATAL EXCEPTION' in line or ('ANR in ' + APP) in line:
            cur = [line]; blocks.append(cur); continue
        if cur is not None:
            if re.search(r'[EIW]/(AndroidRuntime|ActivityManager)\(', line) and len(cur) < 60:
                cur.append(line)
            else:
                cur = None
    return [b for b in blocks if any(APP in l for l in b[:4])]

for seed in [int(s) for s in sys.argv[1:]]:
    subprocess.run([ADB, 'logcat', '-c'])
    sh(f'am start -n {APP}/com.mycompany.app.web.WebViewActivity')
    sh(f'monkey -p {APP} --pct-touch 100 --pct-motion 0 --pct-trackball 0 --pct-nav 0 --pct-majornav 0 '
       f'--pct-syskeys 0 --pct-appswitch 0 --pct-anyevent 0 --throttle 120 -s {seed} 600', timeout=900)
    env = dict(os.environ, OPUM_APP=APP)
    subprocess.run([sys.executable, '-I', os.path.join(HERE, 'swipes.py'), str(seed), '150'], env=env, timeout=1200)
    log = subprocess.run([ADB, 'logcat', '-d', '-v', 'time'], capture_output=True, text=True).stdout
    found = crashes(log)
    with open(os.path.join(OUT, f'qa_{seed}.log'), 'w') as f:
        for b in found:
            f.write('\n'.join(b) + '\n\n')
    print(f'seed {seed}: {len(found)} crash/ANR block(s)', flush=True)
