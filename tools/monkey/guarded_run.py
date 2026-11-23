#!/usr/bin/env python3
"""Random test run (monkey taps + edge-safe swipes) with a settings guard.
Usage: guarded_run.py <seed> [<seed> ...]   |   guarded_run.py --restore true|false
- snapshots the app's settings (files/.pref/*, JSON) before the run
- after each seed: restores secret mode (PrefSync.mSecretMode) through the app's own menu if it changed
- at the end: prints every other setting key that changed, and every user-data database table whose content changed
  (shortcuts, bookmarks, memos, … added, edited or deleted by random taps) and writes those databases back from the
  snapshot taken before the run (app stopped meanwhile); `--keep-db` only reports. Changed settings files are
  written back the same way, and settings files the run created are removed (`--keep-prefs` only reports). Tab / session / cache databases
  (SESSION_DBS) are neither reported nor restored."""
import json, os, subprocess, sys, time

ADB = os.path.expanduser('~/Library/Android/sdk/platform-tools/adb')
APP = 'ch.opumlabs.opumbrowser'
ACT = APP + '/com.mycompany.app.web.WebViewActivity'
HERE = os.path.dirname(os.path.abspath(__file__))

def sh(cmd, **kw):
    return subprocess.run([ADB, 'shell', cmd], capture_output=True, text=True, **kw).stdout

def pref_files():
    """{name: raw bytes} of the settings files (files/.pref/*)."""
    return {n: subprocess.run([ADB, 'exec-out', 'run-as', APP, 'cat', f'files/.pref/{n}'], capture_output=True).stdout
            for n in sh(f'run-as {APP} ls files/.pref').split()}

def prefs():
    out = {}
    for name in sh(f'run-as {APP} ls files/.pref').split():
        try:
            out[name] = json.loads(sh(f'run-as {APP} cat files/.pref/{name}') or '{}')
        except ValueError:
            out[name] = {'<unreadable>': True}
    return out

# Databases the browser itself rewrites while browsing (open tabs, session state, page thumbnails, favicon cache).
# They are not restored or reported; every other database holds user data (bookmarks, shortcuts, memos, downloads, …).
SESSION_DBS = ('DbBookTab', 'DbTabState', 'DbTabThumb', 'DbBookIcon')

def is_user_db(name):
    return not name.startswith(SESSION_DBS)

def db_snapshot():
    """{name: bytes} of every app database (read through run-as), plus a per-table content digest of each."""
    import hashlib, sqlite3, tempfile
    raw, digest = {}, {}
    tmp = tempfile.mkdtemp(prefix='opum-db-')
    for name in sh(f'run-as {APP} ls databases').split():
        if not name.endswith('.db'):
            continue
        data = subprocess.run([ADB, 'exec-out', 'run-as', APP, 'cat', f'databases/{name}'], capture_output=True).stdout
        raw[name] = data
        path = os.path.join(tmp, name)
        with open(path, 'wb') as f:
            f.write(data)
        try:
            con = sqlite3.connect(f'file:{path}?mode=ro', uri=True)
            for (t,) in con.execute("select name from sqlite_master where type='table' and name not like 'sqlite_%' and name != 'android_metadata'"):
                rows = con.execute(f'select * from "{t}"').fetchall()
                digest[f'{name}/{t}'] = (len(rows), hashlib.sha1(repr(sorted(map(repr, rows))).encode()).hexdigest())
            con.close()
        except sqlite3.DatabaseError:
            digest[f'{name}/<unreadable>'] = (-1, '')
        os.remove(path)
    os.rmdir(tmp)
    return raw, digest

def restore_dbs(raw, names):
    """Writes the snapshot of each database in [names] back, with the app stopped (its rollback journal removed)."""
    sh(f'am force-stop {APP}')
    for name in names:
        subprocess.run([ADB, 'exec-in', 'run-as', APP, 'sh', '-c', f'cat > databases/{name}'], input=raw[name])
        sh(f'run-as {APP} rm -f databases/{name}-journal')
    sh(f'am start -n {ACT}'); time.sleep(3)

def secret():
    return prefs().get('PrefSync', {}).get('mSecretMode')

def to_front():
    if APP not in sh('dumpsys activity activities | grep -m1 topResumedActivity'):
        sh('input keyevent 4'); sh(f'am start -n {ACT}'); time.sleep(3)

def toggle_secret():
    to_front()
    sh('input swipe 540 900 540 1500 300')   # bars back in view
    time.sleep(1)
    sh('input tap 990 2273'); time.sleep(2.5)  # menu
    sh('input tap 108 2274'); time.sleep(5)    # secret-mode button (bottom left of the menu)

def monkey(seed):
    return subprocess.run([ADB, 'shell', 'monkey', '-p', APP, '--pct-touch', '100', '--pct-motion', '0',
                           '--pct-trackball', '0', '--pct-nav', '0', '--pct-majornav', '0', '--pct-syskeys', '0',
                           '--pct-appswitch', '0', '--pct-anyevent', '0', '--throttle', '120', '-s', str(seed), '600'],
                          capture_output=True, text=True).stdout.strip().split('\n')[-1]

if sys.argv[1:2] == ['--restore']:
    # restore-only: guarded_run.py --restore true|false
    want = sys.argv[2] == 'true'
    for attempt in range(3):
        now = secret()
        print('secret mode is', now, flush=True)
        if now == want: break
        toggle_secret()
    print('secret mode now:', secret()); sys.exit(0)
before = prefs()
before_raw = pref_files()
db_raw, rows_before = db_snapshot()
want = before.get('PrefSync', {}).get('mSecretMode')
print('secret mode before:', want, flush=True)
for seed in [int(s) for s in sys.argv[1:] if not s.startswith('--')]:
    to_front()
    print('seed', seed, monkey(seed), flush=True)
    subprocess.run([sys.executable, '-I', os.path.join(HERE, 'swipes.py'), str(seed), '150'])
    for attempt in range(3):
        now = secret()
        if now == want: break
        print(f'  secret mode is {now}, restoring to {want} (attempt {attempt + 1})', flush=True)
        toggle_secret()
    else:
        print('  WARNING: could not restore secret mode', flush=True)
to_front()
after = prefs()
changed = []
for f in sorted(set(before) | set(after)):
    a, b = before.get(f, {}), after.get(f, {})
    for k in sorted(set(a) | set(b)):
        if a.get(k) != b.get(k) and not (f == 'PrefSync' and k in ('mSecretIndex', 'mNormalIndex')):
            changed.append(f'{f}.{k}: {a.get(k)!r} -> {b.get(k)!r}')
print('secret mode after:', after.get('PrefSync', {}).get('mSecretMode'))
print('other settings changed by the run:', len(changed))
for c in changed: print('  ' + c)
pref_names = sorted({c.split('.', 1)[0] for c in changed if c.split('.', 1)[0] in before_raw})
# settings files the run created (absent before): removed, so the app falls back to its defaults as before
new_prefs = sorted(n for n in after if n not in before_raw)
if (pref_names or new_prefs) and '--keep-prefs' not in sys.argv:
    # written back with the app stopped, so it cannot overwrite them from memory; secret mode is in PrefSync and was
    # already back at its old value, the index keys skipped above are session bookkeeping
    sh(f'am force-stop {APP}')
    for n in pref_names:
        subprocess.run([ADB, 'exec-in', 'run-as', APP, 'sh', '-c', f'cat > files/.pref/{n}'], input=before_raw[n])
    for n in new_prefs:
        sh(f'run-as {APP} rm -f files/.pref/{n}')
    sh(f'am start -n {ACT}'); time.sleep(3)
    print('settings files restored from the snapshot:', ', '.join(pref_names) or '-')
    print('settings files created by the run, removed:', ', '.join(new_prefs) or '-')
rows_after = db_snapshot()[1]
changed_tables = [k for k in sorted(set(rows_before) | set(rows_after)) if rows_before.get(k) != rows_after.get(k)
                  and is_user_db(k.split('/')[0])]
print('user-data tables changed by the run:', len(changed_tables))
for k in changed_tables:
    print(f'  {k}: rows {rows_before.get(k, (None,))[0]} -> {rows_after.get(k, (None,))[0]}')
dbs = sorted({k.split('/')[0] for k in changed_tables if k.split('/')[0] in db_raw})
if dbs and '--keep-db' not in sys.argv:
    restore_dbs(db_raw, dbs)
    again = db_snapshot()[1]
    still = [k for k in changed_tables if again.get(k) != rows_before.get(k)]
    print('restored from the snapshot:', ', '.join(dbs), '| still different:', len(still))
