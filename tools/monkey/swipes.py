#!/usr/bin/env python3
"""Seeded random swipes that stay away from the screen edges (no system back / home gestures), so the run stays in
the app. Usage: swipes.py <seed> <count>   (prints nothing; runs adb)"""
import random, subprocess, sys, os

adb = os.path.expanduser('~/Library/Android/sdk/platform-tools/adb')
seed, count = int(sys.argv[1]), int(sys.argv[2])
rnd = random.Random(seed)
W, H = 1080, 2400
# safe area: 80 px from the sides (side back gesture), below the status bar, well above the gesture bar
X0, X1, Y0, Y1 = 80, W - 80, 140, H - 260
cmds = []
for _ in range(count):
    kind = rnd.random()
    x, y = rnd.randint(X0, X1), rnd.randint(Y0, Y1)
    if kind < 0.45:      # vertical scroll
        x2, y2 = x + rnd.randint(-40, 40), y + rnd.choice([-1, 1]) * rnd.randint(150, 1100)
    elif kind < 0.8:     # horizontal swipe (tab switching, page gestures)
        x2, y2 = x + rnd.choice([-1, 1]) * rnd.randint(150, 800), y + rnd.randint(-60, 60)
    else:                # diagonal / short drag
        x2, y2 = x + rnd.randint(-300, 300), y + rnd.randint(-300, 300)
    x2 = min(max(x2, X0), X1); y2 = min(max(y2, Y0), Y1)
    dur = rnd.choice([80, 150, 250, 400, 700])
    cmds.append(f'input swipe {x} {y} {x2} {y2} {dur}')
    # no taps here: an adb tap on a link can open another app (monkey does the taps, with its package guard)
# one shell session per 20 commands (fast, and a hang does not lose the whole run)
APP = os.environ.get('OPUM_APP', 'ch.opumlabs.opumbrowser')  # qa_run.py sets the .qa package
def top():
    out = subprocess.run([adb, 'shell', 'dumpsys activity activities | grep -m1 topResumedActivity'], capture_output=True, text=True).stdout
    return APP in out
for i in range(0, len(cmds), 20):
    if not top():
        print('left the app before batch', i // 20, '- bringing it back', flush=True)
        subprocess.run([adb, 'shell', 'input keyevent 4'])
        subprocess.run([adb, 'shell', 'am start -n ' + APP + '/com.mycompany.app.web.WebViewActivity'], capture_output=True)
    subprocess.run([adb, 'shell', '; '.join(cmds[i:i + 20])], timeout=180)
