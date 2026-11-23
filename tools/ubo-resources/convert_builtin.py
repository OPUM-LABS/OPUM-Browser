#!/usr/bin/env python3
"""Converts the app's own site fixes (app/src/main/assets/filter/builtin.txt, private WebClean syntax) into uBlock
Origin syntax for the filter engine. builtin.txt (formerly
app/src/main/assets/filter/builtin.txt, read by the removed old engine) is kept here as the source of the fixes.

    python3 -I tools/ubo-resources/convert_builtin.py tools/ubo-resources/builtin.txt app/src/main/assets/filter/opum-fixes.txt

builtin.txt sections (as WebClean reads them):
  //sg  per-site request rules   a|<where>|<site>|<cond>|<value>[|<cond>|<value>]   (a = allow, b = block)
  //rg  global request rules     a|<cond>|<value>[|<cond>|<value>]
  //wg  per-site element rules   <where>|<site>|<selectors>
  //wk  per-site element rules (Korean lists)   whs|<words>|<selectors>
  //es  hosts exempt from generic element hiding
  //eg  //ek   element rules already in ##-syntax (//ek: Korean lists)
  where: whe = host ends with, wh= host equals, whc = host contains, whs = host contains one of (comma list),
         wu= page URL equals, wus = page URL starts with; dft_you / dft_ham / dft_izle = the app's YouTube / Hitomi /
         Filmizle checks.
  cond:  rhe = request host ends with, rh= = request host equals, rhc = request host contains,
         rue / ruc / rus / ru= = request URL ends with / contains / starts with / equals.
Approximations (uBO has no "host contains"): a contained word becomes the uBO entity `word.*`.
"""
import sys

SPECIAL_SITES = {'dft_you': ['youtube.com'], 'dft_ham': ['hitomi.la'], 'dft_izle': ['filmizle.*']}


def sites(where, value):
    if value in SPECIAL_SITES:
        return SPECIAL_SITES[value]
    if where in ('whe', 'wh='):
        return [w.strip() for w in value.split(',') if w.strip()]
    if where in ('whc', 'whs'):
        return [w.strip() + '.*' if '.' not in w else w.strip() for w in value.split(',') if w.strip()]
    if where in ('wu=', 'wus'):
        host = value.split('://', 1)[-1].split('/', 1)[0].split('?', 1)[0]
        return [host]
    return []


def pattern(conds):
    """[(cond, value), ...] -> uBO network pattern (best effort)."""
    host = None
    host_mode = None
    url_parts = []
    for c, v in conds:
        if c in ('rhe', 'rh='):
            host, host_mode = v, c
        elif c == 'rhc':
            url_parts.append(('contains', v))
        elif c == 'ruc':
            url_parts.append(('contains', v))
        elif c == 'rue':
            url_parts.append(('ends', v))
        elif c == 'rus':
            url_parts.append(('starts', v))
        elif c == 'ru=':
            url_parts.append(('equals', v))
    if host:
        p = '||' + host + '^'
        for kind, v in url_parts:
            p += '*' + v + ('|' if kind == 'ends' else '')
        return p
    if len(url_parts) == 1:
        kind, v = url_parts[0]
        return {'contains': v, 'ends': v + '|', 'starts': '|' + v, 'equals': '|' + v + '|'}[kind]
    # several URL conditions without a host: "contains a ... ends with b"
    contains = [v for k, v in url_parts if k == 'contains']
    ends = [v for k, v in url_parts if k == 'ends']
    return '*'.join(contains) + ('*' + ends[0] + '|' if ends else '')


def net_rule(kind, conds, site_list):
    p = pattern(conds)
    if not p:
        return None
    if p.startswith('/') and p.endswith('/') and len(p) > 1:
        p += '*'  # never read as a regex
    rule = ('@@' if kind == 'a' else '') + p
    if site_list:
        rule += ('$' if '$' not in rule else ',') + 'domain=' + '|'.join(site_list)
    return rule


def main():
    src, out = sys.argv[1], sys.argv[2]
    lines_out = ['! Title: OPUM Browser site fixes', '! Converted from builtin.txt by tools/ubo-resources/convert_builtin.py',
                 '! Do not edit by hand: edit builtin.txt and run the converter again.']
    section = None
    for raw in open(src, encoding='utf-8'):
        line = raw.rstrip('\n').strip()
        if not line:
            continue
        if line.startswith('//'):
            section = line[2:]
            lines_out.append('! ---- ' + section)
            continue
        if section == 'sg':
            f = line.split('|')
            kind, where, site = f[0], f[1], f[2]
            conds = list(zip(f[3::2], f[4::2]))
            r = net_rule(kind, conds, sites(where, site))
        elif section == 'rg':
            f = line.split('|')
            r = net_rule(f[0], list(zip(f[1::2], f[2::2])), [])
        elif section in ('wg', 'wk'):
            where, site, sels = line.split('|', 2)
            r = ','.join(sites(where, site)) + '##' + sels
        elif section == 'es':
            r = '@@||' + line + '^$generichide'
        else:  # eg, ek: already uBO cosmetic syntax
            r = line
        if r:
            lines_out.append(r)
    open(out, 'w', encoding='utf-8').write('\n'.join(lines_out) + '\n')
    print(f'{len(lines_out)} lines -> {out}')


if __name__ == '__main__':
    main()
