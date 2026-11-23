#!/usr/bin/env python3
"""Converts a uBlock Origin source tree into one resources.json for adblock-rust.

    python3 -I tools/ubo-resources/build.py <uBlock source dir> <out resources.json>

Reads uBO's files as TEXT ONLY (nothing of uBO is executed on the build machine):
  * Scriptlets: every `registerScriptlet(IDENT, { name, aliases, dependencies, requiresTrust, ... })` call and every
    `builtinScriptlets.push({ name, aliases, fn: IDENT, dependencies, ... })` in src/js/resources/*.js, with the source of `function IDENT(...) {...}` cut out of the same tree by a small
    JavaScript-aware scanner (strings, template literals, comments, regex literals). The result is the same text
    uBO's `fn.toString()` gives. Helper functions (name ends with ".fn") -> kind {"mime": "fn/javascript"},
    scriptlets -> {"mime": "application/javascript"} (adblock-rust calls these "function-style" resources and invokes
    them with the filter's arguments). Dependencies are mapped to the registered names, `requiresTrust` ->
    `permission: 1` (only lists parsed with that permission may use them, i.e. uBO's own lists).
  * Redirect resources: the entries of `export default new Map([...])` in src/js/redirect-resources.js, content from
    src/web_accessible_resources/<name> (base64), mime type from the extension.
Writes <out> and <out without .json>.meta.json (uBO version, counts). `--check` additionally runs `node --check` (a
syntax check, nothing is executed) on every extracted function.

The output is uBO code and data under GPLv3 (uBO's LICENSE.txt).
"""
import base64, json, os, re, subprocess, sys, tempfile

REGEX_PREV = set('(,=:[!&|?{};+-*%<>~^')
REGEX_KEYWORDS = ('return', 'typeof', 'case', 'do', 'else', 'in', 'of', 'new', 'delete', 'void', 'throw', 'yield', 'await')


def skip_string(src, i, quote):
    i += 1
    while i < len(src):
        c = src[i]
        if c == '\\':
            i += 2
            continue
        if c == quote:
            return i + 1
        i += 1
    raise ValueError('unterminated string')


def skip_template(src, i):
    i += 1
    while i < len(src):
        c = src[i]
        if c == '\\':
            i += 2
            continue
        if c == '`':
            return i + 1
        if c == '$' and i + 1 < len(src) and src[i + 1] == '{':
            i = skip_block(src, i + 1)
            continue
        i += 1
    raise ValueError('unterminated template')


def skip_regex(src, i):
    i += 1
    in_class = False
    while i < len(src):
        c = src[i]
        if c == '\\':
            i += 2
            continue
        if c == '\n':
            raise ValueError('newline in regex')
        if in_class:
            if c == ']':
                in_class = False
        elif c == '[':
            in_class = True
        elif c == '/':
            i += 1
            while i < len(src) and (src[i].isalpha()):
                i += 1
            return i
        i += 1
    raise ValueError('unterminated regex')


def prev_significant(src, i):
    j = i - 1
    while j >= 0 and src[j] in ' \t\r\n':
        j -= 1
    if j < 0:
        return '', ''
    k = j
    while k >= 0 and (src[k].isalnum() or src[k] in '_$'):
        k -= 1
    return src[j], src[k + 1:j + 1]


def skip_block(src, i):
    """src[i] == '{'; returns the index after the matching '}'."""
    assert src[i] == '{'
    depth = 0
    while i < len(src):
        c = src[i]
        if c in '"\'':
            i = skip_string(src, i, c)
            continue
        if c == '`':
            i = skip_template(src, i)
            continue
        if c == '/' and i + 1 < len(src):
            n = src[i + 1]
            if n == '/':
                i = src.index('\n', i)
                continue
            if n == '*':
                i = src.index('*/', i + 2) + 2
                continue
            ch, word = prev_significant(src, i)
            if ch in REGEX_PREV or word in REGEX_KEYWORDS or ch == '':
                i = skip_regex(src, i)
                continue
        if c == '{':
            depth += 1
        elif c == '}':
            depth -= 1
            if depth == 0:
                return i + 1
        i += 1
    raise ValueError('unterminated block')


FUNC_RE = re.compile(r'(?m)^(?:export\s+)?(async\s+)?function\s*(\*?)\s*([A-Za-z_$][\w$]*)\s*\(')
CLASS_RE = re.compile(r'(?m)^(?:export\s+)?class\s+([A-Za-z_$][\w$]*)[^{]*\{')
REEXPORT_RE = re.compile(r'(?m)^export\s+const\s+([A-Za-z_$][\w$]*)\s*=\s*__([A-Za-z_$][\w$]*)__\s*;')
REG_RE = re.compile(r'registerScriptlet\(\s*([A-Za-z_$][\w$]*)\s*,\s*\{')
PUSH_RE = re.compile(r'builtinScriptlets\.push\(\s*\{')


def functions(src):
    out = {}
    for m in FUNC_RE.finditer(src):
        start = m.start() + (len(m.group(0)) - len(m.group(0).lstrip()))
        if src.startswith('export', start):
            start = src.index('function', start) if not m.group(1) else src.index('async', start)
        # find the ')' that closes the parameter list (defaults can contain parentheses)
        depth, j = 0, m.end() - 1
        while j < len(src):
            if src[j] == '(':
                depth += 1
            elif src[j] == ')':
                depth -= 1
                if depth == 0:
                    break
            elif src[j] in '"\'':
                j = skip_string(src, j, src[j]) - 1
            j += 1
        brace = src.index('{', j)
        end = skip_block(src, brace)
        out[m.group(3)] = src[start:end]
    for m in CLASS_RE.finditer(src):
        start = src.index('class', m.start())
        out[m.group(1)] = src[start:skip_block(src, m.end() - 1)]
    return out


def js_value(text):
    """Small parser for the literal objects uBO passes to registerScriptlet / the redirect Map."""
    t = text.strip()
    if t.startswith("'") or t.startswith('"'):
        return ('str', t[1:-1])
    if t in ('true', 'false'):
        return ('bool', t == 'true')
    if re.fullmatch(r'-?\d+', t):
        return ('num', int(t))
    return ('ident', t)


def split_top(text, sep=','):
    parts, depth, cur, i = [], 0, '', 0
    while i < len(text):
        c = text[i]
        if c in '"\'':
            j = skip_string(text, i, c)
            cur += text[i:j]
            i = j
            continue
        if c in '[{(':
            depth += 1
        elif c in ']})':
            depth -= 1
        if c == sep and depth == 0:
            parts.append(cur)
            cur = ''
        else:
            cur += c
        i += 1
    if cur.strip():
        parts.append(cur)
    return parts


def parse_object(body):
    """body: text between '{' and '}' -> {key: value | [values]}."""
    obj = {}
    for part in split_top(body):
        part = re.sub(r'//[^\n]*', '', part).strip()
        if not part or ':' not in part:
            continue
        key, val = part.split(':', 1)
        key = key.strip().strip('\'"')
        val = val.strip()
        if val.startswith('['):
            obj[key] = [js_value(v) for v in split_top(val[1:val.rindex(']')]) if v.strip()]
        elif val.startswith('{'):
            obj[key] = parse_object(val[1:val.rindex('}')])
        else:
            obj[key] = js_value(val)
    return obj


def b64(data):
    if isinstance(data, str):
        data = data.encode('utf-8')
    return base64.b64encode(data).decode('ascii')


MIMES = {'.gif': 'image/gif', '.png': 'image/png', '.js': 'application/javascript', '.html': 'text/html',
         '.txt': 'text/plain', '.json': 'application/json', '.mp3': 'audio/mp3', '.mp4': 'video/mp4',
         '.xml': 'text/xml', '.css': 'text/css'}
TEXTUAL = {'application/javascript', 'text/html', 'text/plain', 'application/json', 'text/xml', 'text/css'}


def main():
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    check = '--check' in sys.argv
    if len(args) != 2:
        print(__doc__)
        return 2
    src_dir, out = os.path.abspath(args[0]), os.path.abspath(args[1])
    res_dir = os.path.join(src_dir, 'src', 'js', 'resources')

    funcs, regs, reexports = {}, [], {}
    # helpers uBO imports from its main code base (urlskip.js, jsonpath.js, arglist-parser.js, ...)
    js_dir = os.path.dirname(res_dir)
    for name in sorted(os.listdir(js_dir)):
        if name.endswith('.js'):
            try:
                for k, v in functions(open(os.path.join(js_dir, name), encoding='utf-8').read()).items():
                    funcs.setdefault(k, v)
            except ValueError:
                pass  # a file the scanner cannot read is only a problem if a scriptlet needs it (reported below)
    for name in sorted(os.listdir(res_dir)):
        if not name.endswith('.js'):
            continue
        text = open(os.path.join(res_dir, name), encoding='utf-8').read()
        funcs.update(functions(text))
        for m in REEXPORT_RE.finditer(text):
            reexports[m.group(1)] = m.group(2)
        for m in REG_RE.finditer(text):
            brace = m.end() - 1
            end = skip_block(text, brace)
            regs.append((m.group(1), parse_object(text[brace + 1:end - 1]), name))
        # older style in scriptlets.js: builtinScriptlets.push({ name, aliases, fn: ident, dependencies: [...] })
        for m in PUSH_RE.finditer(text):
            brace = m.end() - 1
            end = skip_block(text, brace)
            obj = parse_object(text[brace + 1:end - 1])
            fn = obj.get('fn')
            if fn and fn[0] == 'ident':
                regs.append((fn[1], obj, name))

    ident_to_name = {ident: obj['name'][1] for ident, obj, _ in regs if 'name' in obj}
    resources, problems, seen = [], [], set()
    n_fn = n_scriptlet = n_trusted = 0
    for ident, obj, file in regs:
        name = obj.get('name', (None, None))[1]
        if not name or name in seen:
            continue
        code = funcs.get(ident) or funcs.get(reexports.get(ident, ''))
        if code is None:
            problems.append(f'{name}: function {ident} not found ({file})')
            continue
        deps = []
        for kind, value in obj.get('dependencies', []):
            dep = value if kind == 'str' else ident_to_name.get(value)
            if dep is None:
                problems.append(f'{name}: dependency {value} unknown')
                continue
            deps.append(dep)
        is_fn = name.endswith('.fn')
        r = {'name': name, 'aliases': [v for k, v in obj.get('aliases', []) if k == 'str'],
             'kind': {'mime': 'fn/javascript' if is_fn else 'application/javascript'},
             'content': b64(code), 'dependencies': deps}
        if obj.get('requiresTrust', ('bool', False))[1] is True:
            r['permission'] = 1
            n_trusted += 1
        if is_fn:
            n_fn += 1
        else:
            n_scriptlet += 1
        seen.add(name)
        resources.append(r)

    # redirect resources
    red_text = open(os.path.join(src_dir, 'src', 'js', 'redirect-resources.js'), encoding='utf-8').read()
    start = red_text.index('new Map([') + len('new Map(')
    arr_start = red_text.index('[', start)
    # the Map literal: [ [ 'name', {...} ], ... ]
    depth, i = 0, arr_start
    while True:
        c = red_text[i]
        if c in '"\'':
            i = skip_string(red_text, i, c)
            continue
        if c == '[':
            depth += 1
        elif c == ']':
            depth -= 1
            if depth == 0:
                break
        i += 1
    entries = split_top(re.sub(r'//[^\n]*', '', red_text[arr_start + 1:i]))
    war = os.path.join(src_dir, 'src', 'web_accessible_resources')
    n_red, skipped = 0, []
    for e in entries:
        e = e.strip()
        if not e.startswith('['):
            continue
        parts = split_top(e[1:e.rindex(']')])
        name = js_value(parts[0])[1]
        details = parse_object(parts[1].strip()[1:-1]) if len(parts) > 1 and parts[1].strip().startswith('{') else {}
        mime = MIMES.get(os.path.splitext(name)[1], 'text/plain' if '.' not in name else None)
        path = os.path.join(war, name)
        if not mime or not os.path.isfile(path) or name in seen:
            skipped.append(name)
            continue
        raw = open(path, 'rb').read()
        content = b64(raw.decode('utf-8').replace('\r', '')) if mime in TEXTUAL else b64(raw)
        alias = details.get('alias')
        aliases = [v for k, v in alias if k == 'str'] if isinstance(alias, list) else ([alias[1]] if alias else [])
        resources.append({'name': name, 'aliases': aliases, 'kind': {'mime': mime}, 'content': content,
                          'dependencies': []})
        seen.add(name)
        n_red += 1

    if check:
        with tempfile.TemporaryDirectory() as tmp:
            for r in resources:
                if r['kind']['mime'] not in ('application/javascript', 'fn/javascript') or 'dependencies' not in r:
                    continue
                code = base64.b64decode(r['content']).decode('utf-8')
                f = os.path.join(tmp, 'f.js')
                open(f, 'w', encoding='utf-8').write('(' + code + ');\n' if code.lstrip().startswith(('function', 'async')) else code)
                p = subprocess.run(['node', '--check', f], capture_output=True, text=True)
                if p.returncode:
                    problems.append(f"{r['name']}: syntax: {p.stderr.strip().splitlines()[-1] if p.stderr.strip() else '?'}")

    vf = os.path.join(src_dir, 'dist', 'version')
    version = open(vf, encoding='utf-8').read().strip() if os.path.isfile(vf) else None
    if not version:
        mf = os.path.join(src_dir, 'platform', 'chromium', 'manifest.json')
        version = json.load(open(mf, encoding='utf-8')).get('version') if os.path.isfile(mf) else None
    json.dump(resources, open(out, 'w', encoding='utf-8'), separators=(',', ':'))
    meta = {'source': 'uBlock Origin', 'version': version, 'license': 'GPL-3.0', 'scriptlets': n_scriptlet,
            'functions': n_fn, 'trusted': n_trusted, 'redirects': n_red, 'skipped': skipped, 'problems': problems}
    json.dump(meta, open(re.sub(r'\.json$', '', out) + '.meta.json', 'w', encoding='utf-8'), indent=1)
    print(f'uBO {version}: {n_scriptlet} scriptlets, {n_fn} helper functions ({n_trusted} need trust), '
          f'{n_red} redirect resources, skipped {len(skipped)}, problems {len(problems)}')
    for p in problems:
        print('  problem:', p)
    return 1 if problems else 0


if __name__ == '__main__':
    sys.exit(main())
