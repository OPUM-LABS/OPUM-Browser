/*
 * OPUM Browser: cosmetic filtering of the uBlock Origin-compatible engine.
 * Registered with WebViewCompat.addDocumentStartJavaScript, so it runs in every frame before the page's own scripts.
 * Asks the Java bridge `opumUbo` (web/filter/UboCosmetic.kt) for the frame's rules and applies them:
 *   - scriptlets (main world, before page scripts),
 *   - hide selectors as one <style> (one rule per selector, so a bad selector cannot void the others),
 *   - generic class / id rules for classes and ids seen in the DOM (unless $generichide),
 *   - procedural filters (:has-text, :upward, :xpath, :matches-css, ... and :remove() / :style() actions).
 * The bridge answers null when the engine is off, the page is on the allow list, or for YouTube.
 */
(function () {
  'use strict';
  if (window.__opumUboDone) return;
  window.__opumUboDone = true;
  var bridge = window.opumUbo;
  if (!bridge) return;

  var data;
  try {
    var raw = bridge.cosmetic(String(location.href), window === window.top);
    if (!raw) return;
    data = JSON.parse(raw);
  } catch (e) {
    return;
  }

  // 1. Scriptlets, as early as possible.
  if (data.script) {
    // uBO's scriptlets (safe-self.fn) read `scriptletGlobals`, which uBO's own injector defines around them.
    var code = '(function(){var scriptletGlobals = {};\n' + data.script + '\nwindow.__opumUboScriptletsRan = true;})();';
    var ran = false;
    try { (new Function(code))(); ran = true; } catch (e) {}
    if (!ran) {
      try {
        var s = document.createElement('script');
        s.textContent = code;
        (document.head || document.documentElement).appendChild(s);
        s.remove();
        ran = window.__opumUboScriptletsRan === true;
      } catch (e) {}
    }
    // Both refused by the page's Content-Security-Policy: let the app run them (top frame only).
    if (!ran && window === window.top) {
      try { bridge.runBlockedScriptlets(String(location.href)); } catch (e) {}
    }
  }

  // 2. Hide selectors.
  var style = document.createElement('style');
  style.setAttribute('data-opum', 'ubo');
  var seenSel = Object.create(null);
  var canCheck = !!(window.CSS && CSS.supports);
  // Brackets and quotes must close: CSS.supports() closes an open "(" or "[" on its own, but in the style sheet the
  // open bracket swallows every rule after it (a list rule ending in ":is([...]" hid nothing after it on reddit).
  function balanced(sel) {
    var stack = [], quote = '';
    for (var i = 0; i < sel.length; i++) {
      var c = sel.charAt(i);
      if (c === '\\') { i++; continue; }
      if (quote) { if (c === quote) quote = ''; continue; }
      if (c === '"' || c === "'") quote = c;
      else if (c === '(' || c === '[') stack.push(c === '(' ? ')' : ']');
      else if (c === ')' || c === ']') { if (stack.pop() !== c) return false; }
      else if (c === '{' || c === '}') return false;
    }
    return !quote && stack.length === 0;
  }
  function validSelector(sel) {
    // An invalid selector (e.g. an unclosed bracket) would swallow the rules after it in the same style sheet.
    if (!balanced(sel)) return false;
    if (canCheck) { try { return CSS.supports('selector(' + sel + ')'); } catch (e) { return false; } }
    try { document.createDocumentFragment().querySelector(sel); return true; } catch (e) { return false; }
  }
  function addSelectors(list) {
    if (!list || !list.length) return;
    var css = '';
    for (var i = 0; i < list.length; i++) {
      var sel = list[i];
      if (!sel || seenSel[sel]) continue;
      seenSel[sel] = true;
      if (!validSelector(sel)) continue;
      css += sel + '{display:none!important}\n';
    }
    if (css) style.appendChild(document.createTextNode(css));
  }
  function attachStyle() {
    if (style.isConnected) return;
    var parent = document.head || document.documentElement;
    if (parent) parent.appendChild(style);
  }
  addSelectors(data.hide);
  attachStyle();

  // 2b. Pages that lock scrolling while a dialog is open (cookie consent, sign-in, "open in app") stay locked when
  // that dialog is hidden above. The app asks __opumScrollCheck() after swipes that moved nothing; __opumUnlock()
  // lifts the lock (the user's "Unlock scrolling", and on every visit of a site it was chosen for: data.unlock).
  if (window === window.top) {
    var unlocked = false;
    var unlock = function () {
      if (unlocked) return true;
      unlocked = true;
      var css = 'html,body{overflow-y:auto!important}\n';
      var b = document.body;
      if (b && getComputedStyle(b).position === 'fixed') {
        // the other usual lock: a fixed body, moved up by the scroll position it had
        var top = parseInt(b.style.top, 10) || 0;
        css += 'body{position:static!important;top:auto!important}\n';
        if (top < 0) setTimeout(function () { window.scrollTo(0, -top); }, 0);
      }
      // a style of its own, so nothing in the hide rules can affect it; kept attached like the hide style
      var own = document.createElement('style');
      own.setAttribute('data-opum', 'unlock');
      own.textContent = css;
      var put = function () { if (!own.isConnected) (document.head || document.documentElement).appendChild(own); };
      put();
      new MutationObserver(put).observe(document.documentElement, { childList: true, subtree: true });
      return true;
    };
    var modalHint = /consent|cookie|gdpr|privacy|cmp|modal|dialog|overlay|popup|banner|notice/i;
    var looksModal = function (el) {
      if (el.tagName === 'DIALOG') return true;
      var role = el.getAttribute('role');
      if (role === 'dialog' || role === 'alertdialog' || el.getAttribute('aria-modal') === 'true') return true;
      var cls = typeof el.className === 'string' ? el.className : '';
      return modalHint.test((el.id || '') + ' ' + cls);
    };
    window.__opumScrollCheck = function () {
      var de = document.documentElement, b = document.body;
      if (!de || !b) return { stuck: false };
      var tall = Math.max(de.scrollHeight, b.scrollHeight) > window.innerHeight + 40;
      var hs = getComputedStyle(de), bs = getComputedStyle(b);
      var hidden = function (v) { return v === 'hidden' || v === 'clip'; };
      var locked = hidden(hs.overflowY) || (hs.overflowY === 'visible' && hidden(bs.overflowY)) || bs.position === 'fixed';
      var dialog = null;
      if (tall && locked && !unlocked) {
        for (var sel in seenSel) {
          var list;
          try { list = document.querySelectorAll(sel); } catch (e) { continue; }
          for (var i = 0; i < list.length && i < 20; i++) {
            if (looksModal(list[i])) { dialog = sel; break; }
          }
          if (dialog) break;
        }
      }
      return { stuck: !!(tall && locked && dialog), dialog: dialog };
    };
    window.__opumUnlock = unlock;
    if (data.unlock) unlock();
  }

  // 3. Generic class / id rules.
  var generic = !data.generichide;
  var exceptions = JSON.stringify(data.exceptions || []);
  var seenClass = Object.create(null), seenId = Object.create(null);
  var newClasses = [], newIds = [];
  function collect(el) {
    if (!generic || !el || el.nodeType !== 1) return;
    var id = el.id;
    if (id && typeof id === 'string' && !seenId[id]) { seenId[id] = true; newIds.push(id); }
    var cl = el.classList;
    if (cl) for (var i = 0; i < cl.length; i++) {
      var c = cl[i];
      if (!seenClass[c]) { seenClass[c] = true; newClasses.push(c); }
    }
  }
  function collectTree(root) {
    if (!generic || !root || !root.querySelectorAll) return;
    collect(root);
    var all = root.querySelectorAll('[id],[class]');
    for (var i = 0; i < all.length; i++) collect(all[i]);
  }
  function flushGeneric() {
    if (!newClasses.length && !newIds.length) return;
    var c = JSON.stringify(newClasses), d = JSON.stringify(newIds);
    newClasses = []; newIds = [];
    try {
      var sel = bridge.classId(c, d, exceptions);
      if (sel) addSelectors(JSON.parse(sel));
    } catch (e) {}
  }

  // 4. Procedural filters.
  var procedural = [];
  (data.procedural || []).forEach(function (p) {
    try { procedural.push(JSON.parse(p)); } catch (e) {}
  });

  function toRegex(arg) {
    var m = /^\/(.*)\/([imsu]*)$/.exec(arg);
    if (m) { try { return new RegExp(m[1], m[2]); } catch (e) { return null; } }
    return null;
  }
  function textTest(arg) {
    var re = toRegex(arg);
    return re ? function (t) { return re.test(t); } : function (t) { return t.indexOf(arg) !== -1; };
  }
  function unquote(s) {
    s = s.trim();
    if (s.length >= 2 && (s[0] === '"' || s[0] === "'") && s[s.length - 1] === s[0]) return s.slice(1, -1);
    return s;
  }
  function splitPair(arg, sep) {
    var i = arg.indexOf(sep);
    return i < 0 ? [arg, null] : [arg.slice(0, i), arg.slice(i + 1)];
  }
  function relative(nodes, arg) {
    var out = [];
    var a = arg.trim();
    var comb = a[0];
    if (comb === '>' || comb === '+' || comb === '~') {
      for (var i = 0; i < nodes.length; i++) {
        var n = nodes[i];
        try {
          if (comb === '>') {
            out.push.apply(out, n.querySelectorAll(':scope ' + a));
          } else {
            var rest = a.slice(1).trim();
            var sib = n.nextElementSibling;
            while (sib) {
              if (sib.matches(rest)) { out.push(sib); if (comb === '+') break; }
              if (comb === '+') break;
              sib = sib.nextElementSibling;
            }
          }
        } catch (e) {}
      }
      return out;
    }
    if (arg[0] === ' ') {
      for (var j = 0; j < nodes.length; j++) {
        try { out.push.apply(out, nodes[j].querySelectorAll(':scope ' + a)); } catch (e) {}
      }
      return out;
    }
    return nodes.filter(function (n) { try { return n.matches(a); } catch (e) { return false; } });
  }
  function run(ops) {
    var nodes = null;
    for (var i = 0; i < ops.length; i++) {
      var op = ops[i], t = op.type, arg = op.arg == null ? '' : String(op.arg);
      if (nodes === null) {
        if (t === 'css-selector') {
          try { nodes = Array.prototype.slice.call(document.querySelectorAll(arg)); } catch (e) { return []; }
          continue;
        }
        if (t === 'matches-path') {
          var p = location.pathname + location.search, re0 = toRegex(arg);
          if (!(re0 ? re0.test(p) : p.indexOf(arg) !== -1)) return [];
          continue;
        }
        nodes = document.documentElement ? [document.documentElement] : [];
      }
      if (!nodes.length) return nodes;
      switch (t) {
        case 'css-selector':
          nodes = relative(nodes, arg);
          break;
        case 'has-text':
          var tt = textTest(arg);
          nodes = nodes.filter(function (n) { return tt(n.textContent || ''); });
          break;
        case 'min-text-length':
          var min = parseInt(arg, 10) || 0;
          nodes = nodes.filter(function (n) { return (n.textContent || '').length >= min; });
          break;
        case 'matches-attr':
          var kv = splitPair(arg, '='), kTest = textTest(unquote(kv[0])), vRaw = kv[1] == null ? null : unquote(kv[1]);
          var vTest = vRaw == null ? null : textTest(vRaw);
          nodes = nodes.filter(function (n) {
            for (var a = 0; a < n.attributes.length; a++) {
              var at = n.attributes[a];
              if (kTest(at.name) && (vTest === null || vTest(at.value))) return true;
            }
            return false;
          });
          break;
        case 'matches-css':
        case 'matches-css-before':
        case 'matches-css-after':
          var pseudo = t === 'matches-css-before' ? '::before' : t === 'matches-css-after' ? '::after' : null;
          var pv = splitPair(arg, ':'), prop = pv[0].trim(), valTest = textTest((pv[1] || '').trim());
          nodes = nodes.filter(function (n) {
            try { return valTest(getComputedStyle(n, pseudo).getPropertyValue(prop)); } catch (e) { return false; }
          });
          break;
        case 'matches-path':
          var path = location.pathname + location.search, re1 = toRegex(arg);
          if (!(re1 ? re1.test(path) : path.indexOf(arg) !== -1)) return [];
          break;
        case 'upward':
          var num = /^\d+$/.test(arg.trim()) ? parseInt(arg, 10) : 0;
          nodes = nodes.map(function (n) {
            if (num > 0) {
              var u = n;
              for (var k = 0; k < num && u; k++) u = u.parentElement;
              return u;
            }
            try { return n.parentElement ? n.parentElement.closest(arg) : null; } catch (e) { return null; }
          }).filter(Boolean);
          break;
        case 'xpath':
          var res = [];
          nodes.forEach(function (n) {
            try {
              var r = document.evaluate(arg, n, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
              for (var x = 0; x < r.snapshotLength; x++) if (r.snapshotItem(x).nodeType === 1) res.push(r.snapshotItem(x));
            } catch (e) {}
          });
          nodes = res;
          break;
        default:
          return []; // unknown operator: do nothing rather than hide the wrong thing
      }
    }
    return nodes || [];
  }
  // elements hidden or removed by procedural filters, each once (read by the Web Filter dialog via the style element)
  var procSeen = typeof WeakSet === 'function' ? new WeakSet() : null;
  var procCount = 0;
  try { Object.defineProperty(style, '_opumProc', { get: function () { return procCount; } }); } catch (e) {}
  function countProc(n) {
    if (procSeen && !procSeen.has(n)) { procSeen.add(n); procCount++; }
  }
  function act(nodes, action) {
    for (var i = 0; i < nodes.length; i++) {
      var n = nodes[i];
      if (n === document.documentElement || n === document.body) continue;
      try {
        if (!action) { countProc(n); n.style.setProperty('display', 'none', 'important'); continue; }
        if (action.type === 'remove') countProc(n);
        switch (action.type) {
          case 'remove': n.remove(); break;
          case 'style': n.setAttribute('style', (n.getAttribute('style') || '') + ';' + action.arg); break;
          case 'remove-attr': n.removeAttribute(action.arg); break;
          case 'remove-class': n.classList.remove(action.arg); break;
        }
      } catch (e) {}
    }
  }
  function runProcedural() {
    for (var i = 0; i < procedural.length; i++) {
      try { act(run(procedural[i].selector || []), procedural[i].action); } catch (e) {}
    }
  }

  // 4b. uBO's :others() (OthersRules): hide every element except the targets, their ancestors and descendants.
  // Nothing is hidden while no target exists yet (an empty page would otherwise go blank).
  var others = (data.others || []).filter(function (s) { return typeof s === 'string' && s; });
  var SKIP_TAGS = { SCRIPT: 1, STYLE: 1, LINK: 1, META: 1, NOSCRIPT: 1, TEMPLATE: 1, BASE: 1, TITLE: 1 };
  function hideOthers(node, targets, ancestors) {
    var kids = node.children;
    for (var i = 0; i < kids.length; i++) {
      var child = kids[i];
      if (targets.has(child)) continue; // a target keeps its whole subtree
      if (ancestors.has(child)) { hideOthers(child, targets, ancestors); continue; }
      if (SKIP_TAGS[child.tagName]) continue;
      if (child.style && child.style.getPropertyValue('display') !== 'none') {
        countProc(child);
        child.style.setProperty('display', 'none', 'important');
      }
    }
  }
  function runOthers() {
    var body = document.body;
    if (!body || typeof Set !== 'function') return;
    for (var i = 0; i < others.length; i++) {
      var list;
      try { list = document.querySelectorAll(others[i]); } catch (e) { continue; }
      if (!list.length) continue;
      var targets = new Set(), ancestors = new Set();
      for (var j = 0; j < list.length; j++) {
        targets.add(list[j]);
        for (var p = list[j].parentElement; p && !ancestors.has(p); p = p.parentElement) ancestors.add(p);
      }
      if (!ancestors.has(body) && !targets.has(body)) continue; // targets outside the body: leave the page alone
      if (targets.has(body)) continue;
      hideOthers(body, targets, ancestors);
    }
  }

  // 5. Now and on changes (throttled).
  var pending = false;
  function onChange() {
    pending = false;
    attachStyle();
    flushGeneric();
    if (procedural.length) runProcedural();
    if (others.length) runOthers();
  }
  function schedule() {
    if (pending) return;
    pending = true;
    setTimeout(onChange, 50);
  }
  function start() {
    collectTree(document.documentElement);
    onChange();
    if (!generic && !procedural.length && !others.length) {
      new MutationObserver(attachStyle).observe(document.documentElement, { childList: true });
      return;
    }
    new MutationObserver(function (records) {
      for (var i = 0; i < records.length; i++) {
        var r = records[i];
        if (r.type === 'attributes') { collect(r.target); continue; }
        for (var j = 0; j < r.addedNodes.length; j++) collectTree(r.addedNodes[j]);
      }
      schedule();
    }).observe(document.documentElement, generic
      ? { childList: true, subtree: true, attributes: true, attributeFilter: ['class', 'id'] }
      : { childList: true, subtree: true });
  }
  if (document.documentElement) start();
  else document.addEventListener('readystatechange', start, { once: true });
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', schedule, { once: true });
})();
