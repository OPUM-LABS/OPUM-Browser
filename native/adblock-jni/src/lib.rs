//! JNI bridge from the OPUM Browser web filter to Brave's adblock-rust engine (MPL-2.0).
//!
//! Kotlin side: `com.mycompany.app.web.filter.UboEngine` (all functions are static `native` methods of that class).
//! An engine is passed around as an opaque `long` handle (a leaked `Box<Engine>`), freed by `nativeFree`.
//! Every entry point catches panics so a bad list or request can never take the browser down.

use std::collections::HashSet;
use std::panic::{catch_unwind, AssertUnwindSafe};

use adblock::lists::{ExpiresInterval, FilterSet, ParseOptions};
use adblock::request::Request;
use adblock::resources::Resource;
use adblock::Engine;
use jni::objects::{JByteArray, JClass, JObject, JObjectArray, JString};
use jni::sys::{jbyteArray, jlong, jobjectArray, jstring};
use jni::JNIEnv;

// The engine is shared between WebView threads.
const _: fn() = || {
    fn assert_sync<T: Send + Sync>() {}
    assert_sync::<Engine>();
};

// ---------------------------------------------------------------------------------------------
// Plain Rust API (also used by the unit tests below)
// ---------------------------------------------------------------------------------------------

/// Permission bit for scriptlets uBO marks `requiresTrust` (`tools/ubo-resources/build.py` writes `permission: 1`).
pub const TRUSTED: u8 = 1;

/// Builds an engine from whole filter list texts (all untrusted).
pub fn build_engine(lists: &[String]) -> Engine {
    build_engine_trust(lists, &[])
}

/// Builds an engine; `trusted[i]` lets list `i` use trusted scriptlets (uBO's own lists only).
pub fn build_engine_trust(lists: &[String], trusted: &[bool]) -> Engine {
    let mut set = FilterSet::new(false);
    for (i, text) in lists.iter().enumerate() {
        let mut opts = ParseOptions::default();
        if trusted.get(i).copied().unwrap_or(false) {
            opts.permissions = adblock::resources::PermissionMask::from_bits(TRUSTED);
        }
        set.add_filter_list(text.clone(), opts);
    }
    Engine::new_with_filter_set(set)
}

/// Result of a network check.
#[derive(Debug, PartialEq, Eq)]
pub enum Verdict {
    Allow,
    Block,
    /// Blocked, serve this body instead (adblock-rust gives a `data:` URL).
    Redirect(String),
    /// Allowed, but load this URL instead (`$removeparam`).
    Rewrite(String),
}

pub fn check(engine: &Engine, url: &str, source_url: &str, request_type: &str, method: &str) -> Verdict {
    let Ok(req) = Request::new(url, source_url, request_type, method) else {
        return Verdict::Allow;
    };
    let r = engine.check_network_request(&req);
    if r.should_block() {
        return match r.redirect {
            Some(body) => Verdict::Redirect(body),
            None => Verdict::Block,
        };
    }
    match r.rewritten_url {
        Some(u) => Verdict::Rewrite(u),
        None => Verdict::Allow,
    }
}

/// Cosmetic resources of a page as 5 strings: hide selectors (joined with ","), procedural actions
/// (JSON objects, one per line), class/id exceptions (joined with ","), scriptlet JS, generichide ("1"/"0").
pub fn cosmetic(engine: &Engine, url: &str) -> [String; 5] {
    let r = engine.url_cosmetic_resources(url);
    let mut hide: Vec<String> = r.hide_selectors.into_iter().collect();
    hide.sort();
    let mut procedural: Vec<String> = r.procedural_actions.into_iter().collect();
    procedural.sort();
    let mut exceptions: Vec<String> = r.exceptions.into_iter().collect();
    exceptions.sort();
    [
        hide.join(","),
        procedural.join("\n"),
        exceptions.join(","),
        r.injected_script,
        if r.generichide { "1".into() } else { "0".into() },
    ]
}

/// `None` when the line is a usable filter (or a comment), else the parser's error text.
pub fn validate(rule: &str) -> Option<String> {
    let t = rule.trim();
    if t.is_empty() || t.starts_with('!') || t.starts_with("[Adblock") {
        return None;
    }
    match adblock::lists::parse_filter(t, false, ParseOptions::default()) {
        Ok(_) => None,
        Err(e) => Some(e.to_string()),
    }
}

/// Rule counts of one list: usable rules, rejected rules, and up to `samples` rejected lines with the reason.
/// Comments (`!`, `[Adblock`, `#` lines of hosts files) and empty lines are not counted.
pub fn list_stats(text: &str, samples: usize) -> (usize, usize, Vec<String>) {
    let (mut ok, mut bad) = (0usize, 0usize);
    let mut examples = Vec::new();
    for line in text.lines() {
        let t = line.trim();
        if t.is_empty() || t.starts_with('!') || t.starts_with("[Adblock") || t.starts_with("# ") || t == "#" {
            continue;
        }
        match adblock::lists::parse_filter(t, false, ParseOptions::default()) {
            Ok(_) => ok += 1,
            Err(adblock::lists::FilterParseError::Empty) => {}
            Err(e) => {
                bad += 1;
                if examples.len() < samples {
                    examples.push(format!("{t}  ({e})"));
                }
            }
        }
    }
    (ok, bad, examples)
}

/// `! Title:`, `! Homepage:` and `! Expires:` (in hours, "" when missing) of a list.
pub fn list_metadata(text: &str) -> [String; 3] {
    let m = adblock::lists::read_list_metadata(text);
    let hours = match m.expires {
        Some(ExpiresInterval::Hours(h)) => h.to_string(),
        Some(ExpiresInterval::Days(d)) => (d as u32 * 24).to_string(),
        None => String::new(),
    };
    [m.title.unwrap_or_default(), m.homepage.unwrap_or_default(), hours]
}

/// Loads scriptlet / redirect resources (JSON array in adblock-rust's `Resource` format).
pub fn use_resources(engine: &mut Engine, json: &str) -> Result<usize, String> {
    let list: Vec<Resource> = serde_json::from_str(json).map_err(|e| e.to_string())?;
    let n = list.len();
    engine.use_resources(list);
    Ok(n)
}

// ---------------------------------------------------------------------------------------------
// JNI glue
// ---------------------------------------------------------------------------------------------

fn guard<T>(fallback: T, f: impl FnOnce() -> T) -> T {
    catch_unwind(AssertUnwindSafe(f)).unwrap_or(fallback)
}

fn engine_ref<'a>(handle: jlong) -> Option<&'a Engine> {
    if handle == 0 { None } else { Some(unsafe { &*(handle as *const Engine) }) }
}

fn engine_mut<'a>(handle: jlong) -> Option<&'a mut Engine> {
    if handle == 0 { None } else { Some(unsafe { &mut *(handle as *mut Engine) }) }
}

fn jstr(env: &mut JNIEnv, s: &JString) -> String {
    if s.is_null() {
        return String::new();
    }
    env.get_string(s).map(|v| v.into()).unwrap_or_default()
}

fn jstrings(env: &mut JNIEnv, arr: &JObjectArray) -> Vec<String> {
    if arr.is_null() {
        return Vec::new();
    }
    let n = env.get_array_length(arr).unwrap_or(0);
    let mut out = Vec::with_capacity(n.max(0) as usize);
    for i in 0..n {
        if let Ok(o) = env.get_object_array_element(arr, i) {
            if o.is_null() {
                continue;
            }
            let s = JString::from(o);
            out.push(jstr(env, &s));
        }
    }
    out
}

fn new_jstring(env: &mut JNIEnv, s: &str) -> jstring {
    env.new_string(s).map(|v| v.into_raw()).unwrap_or(std::ptr::null_mut())
}

fn new_jstrings(env: &mut JNIEnv, items: &[String]) -> jobjectArray {
    let Ok(arr) = env.new_object_array(items.len() as i32, "java/lang/String", JObject::null()) else {
        return std::ptr::null_mut();
    };
    for (i, s) in items.iter().enumerate() {
        if let Ok(js) = env.new_string(s) {
            let _ = env.set_object_array_element(&arr, i as i32, js);
        }
    }
    arr.into_raw()
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_mycompany_app_web_filter_UboEngine_nativeCreate(
    mut env: JNIEnv, _c: JClass, lists: JObjectArray, trusted: jni::objects::JBooleanArray,
) -> jlong {
    let lists = jstrings(&mut env, &lists);
    let mut flags = Vec::new();
    if !trusted.is_null() {
        let n = env.get_array_length(&trusted).unwrap_or(0).max(0) as usize;
        let mut buf = vec![0u8; n];
        if env.get_boolean_array_region(&trusted, 0, &mut buf).is_ok() {
            flags = buf.into_iter().map(|b| b != 0).collect();
        }
    }
    guard(0, || Box::into_raw(Box::new(build_engine_trust(&lists, &flags))) as jlong)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_mycompany_app_web_filter_UboEngine_nativeDeserialize(
    env: JNIEnv, _c: JClass, data: JByteArray,
) -> jlong {
    let Ok(bytes) = env.convert_byte_array(&data) else { return 0 };
    guard(0, || {
        let mut e = Engine::default();
        match e.deserialize(&bytes) {
            Ok(()) => Box::into_raw(Box::new(e)) as jlong,
            Err(_) => 0,
        }
    })
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_mycompany_app_web_filter_UboEngine_nativeSerialize(
    env: JNIEnv, _c: JClass, handle: jlong,
) -> jbyteArray {
    let Some(e) = engine_ref(handle) else { return std::ptr::null_mut() };
    let data = guard(Vec::new(), || e.serialize());
    if data.is_empty() {
        return std::ptr::null_mut();
    }
    env.byte_array_from_slice(&data).map(|a| a.into_raw()).unwrap_or(std::ptr::null_mut())
}

/// null = allow, "" = block, "r:<data url>" = redirect, "u:<url>" = rewritten URL.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_mycompany_app_web_filter_UboEngine_nativeCheck(
    mut env: JNIEnv, _c: JClass, handle: jlong, url: JString, source: JString, kind: JString, method: JString,
) -> jstring {
    let Some(e) = engine_ref(handle) else { return std::ptr::null_mut() };
    let (url, source, kind, method) =
        (jstr(&mut env, &url), jstr(&mut env, &source), jstr(&mut env, &kind), jstr(&mut env, &method));
    let verdict = guard(Verdict::Allow, || check(e, &url, &source, &kind, &method));
    match verdict {
        Verdict::Allow => std::ptr::null_mut(),
        Verdict::Block => new_jstring(&mut env, ""),
        Verdict::Redirect(body) => new_jstring(&mut env, &format!("r:{body}")),
        Verdict::Rewrite(u) => new_jstring(&mut env, &format!("u:{u}")),
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_mycompany_app_web_filter_UboEngine_nativeCosmetic(
    mut env: JNIEnv, _c: JClass, handle: jlong, url: JString,
) -> jobjectArray {
    let Some(e) = engine_ref(handle) else { return std::ptr::null_mut() };
    let url = jstr(&mut env, &url);
    let Some(parts) = guard(None, || Some(cosmetic(e, &url))) else { return std::ptr::null_mut() };
    new_jstrings(&mut env, &parts)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_mycompany_app_web_filter_UboEngine_nativeClassId(
    mut env: JNIEnv, _c: JClass, handle: jlong, classes: JObjectArray, ids: JObjectArray, exceptions: JObjectArray,
) -> jobjectArray {
    let Some(e) = engine_ref(handle) else { return std::ptr::null_mut() };
    let (c, i, x) = (jstrings(&mut env, &classes), jstrings(&mut env, &ids), jstrings(&mut env, &exceptions));
    let x: HashSet<String> = x.into_iter().collect();
    let sel = guard(Vec::new(), || e.hidden_class_id_selectors(c.iter(), i.iter(), &x));
    new_jstrings(&mut env, &sel)
}

/// Returns the number of resources loaded, or -1 when the JSON is not valid.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_mycompany_app_web_filter_UboEngine_nativeUseResources(
    mut env: JNIEnv, _c: JClass, handle: jlong, json: JString,
) -> jlong {
    let Some(e) = engine_mut(handle) else { return -1 };
    let json = jstr(&mut env, &json);
    guard(-1, || use_resources(e, &json).map(|n| n as jlong).unwrap_or(-1))
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_mycompany_app_web_filter_UboEngine_nativeValidate(
    mut env: JNIEnv, _c: JClass, rule: JString,
) -> jstring {
    let rule = jstr(&mut env, &rule);
    match guard(Some("internal error".into()), || validate(&rule)) {
        None => std::ptr::null_mut(),
        Some(err) => new_jstring(&mut env, &err),
    }
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_mycompany_app_web_filter_UboEngine_nativeListMetadata(
    mut env: JNIEnv, _c: JClass, text: JString,
) -> jobjectArray {
    let text = jstr(&mut env, &text);
    let parts = guard([String::new(), String::new(), String::new()], || list_metadata(&text));
    new_jstrings(&mut env, &parts)
}

/// [rules, rejected, sample rejected lines...]
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_mycompany_app_web_filter_UboEngine_nativeListStats(
    mut env: JNIEnv, _c: JClass, text: JString, samples: jni::sys::jint,
) -> jobjectArray {
    let text = jstr(&mut env, &text);
    let (ok, bad, examples) = guard((0, 0, Vec::new()), || list_stats(&text, samples.max(0) as usize));
    let mut out = vec![ok.to_string(), bad.to_string()];
    out.extend(examples);
    new_jstrings(&mut env, &out)
}

#[unsafe(no_mangle)]
pub extern "system" fn Java_com_mycompany_app_web_filter_UboEngine_nativeFree(
    _env: JNIEnv, _c: JClass, handle: jlong,
) {
    if handle != 0 {
        let _ = catch_unwind(|| unsafe { drop(Box::from_raw(handle as *mut Engine)) });
    }
}

/// Lets Kotlin check that the library is the expected build.
#[unsafe(no_mangle)]
pub extern "system" fn Java_com_mycompany_app_web_filter_UboEngine_nativeVersion(
    mut env: JNIEnv, _c: JClass,
) -> jstring {
    new_jstring(&mut env, concat!("opumadblock ", env!("CARGO_PKG_VERSION"), " / adblock 0.13.6"))
}

// ---------------------------------------------------------------------------------------------

#[cfg(test)]
mod tests {
    use super::*;

    const RULES: &str = "\
! Title: Test list
! Expires: 4 days
||ads.example.com^
||tracker.example.net^$third-party
@@||ads.example.com/allowed/*
/banner/*$image
||cdn.example.org/ad.js$script,redirect=noopjs
example.com##.sponsored
example.com#@#.keep-me
##.ad-banner
###ad_top
||example.com^$removeparam=utm_source
";

    fn engine() -> Engine {
        build_engine(&[RULES.to_string()])
    }

    #[test]
    fn network_block_allow_and_exception() {
        let e = engine();
        assert_eq!(check(&e, "https://ads.example.com/x.js", "https://news.test/", "script", "GET"), Verdict::Block);
        assert_eq!(check(&e, "https://ads.example.com/allowed/x.js", "https://news.test/", "script", "GET"), Verdict::Allow);
        assert_eq!(check(&e, "https://www.example.com/app.js", "https://www.example.com/", "script", "GET"), Verdict::Allow);
    }

    #[test]
    fn third_party_and_type_options() {
        let e = engine();
        assert_eq!(check(&e, "https://tracker.example.net/p", "https://news.test/", "xmlhttprequest", "GET"), Verdict::Block);
        assert_eq!(check(&e, "https://tracker.example.net/p", "https://www.example.net/", "xmlhttprequest", "GET"), Verdict::Allow);
        assert_eq!(check(&e, "https://img.test/banner/1.png", "https://news.test/", "image", "GET"), Verdict::Block);
        assert_eq!(check(&e, "https://img.test/banner/1.png", "https://news.test/", "script", "GET"), Verdict::Allow);
    }

    #[test]
    fn redirect_without_resources_still_blocks() {
        let e = engine();
        // No resources loaded: the rule blocks; with "noopjs" loaded it would redirect.
        let v = check(&e, "https://cdn.example.org/ad.js", "https://news.test/", "script", "GET");
        assert!(matches!(v, Verdict::Block | Verdict::Redirect(_)), "{v:?}");
    }

    #[test]
    fn redirect_with_resource() {
        let mut e = engine();
        let json = r#"[{"name":"noopjs","aliases":["noop.js"],"kind":{"mime":"application/javascript"},"content":"KGZ1bmN0aW9uKCl7fSkoKTs="}]"#;
        assert_eq!(use_resources(&mut e, json), Ok(1));
        let v = check(&e, "https://cdn.example.org/ad.js", "https://news.test/", "script", "GET");
        assert!(matches!(v, Verdict::Redirect(ref b) if b.starts_with("data:application/javascript")), "{v:?}");
    }

    #[test]
    fn removeparam_rewrites() {
        let e = engine();
        let v = check(&e, "https://www.example.com/page?utm_source=x&id=1", "https://www.example.com/", "xmlhttprequest", "GET");
        assert_eq!(v, Verdict::Rewrite("https://www.example.com/page?id=1".into()));
    }

    #[test]
    fn cosmetic_site_generic_and_exception() {
        let e = engine();
        let [hide, _proc, _exc, _script, generichide] = cosmetic(&e, "https://www.example.com/a");
        assert!(hide.split(',').any(|s| s == ".sponsored"), "{hide}");
        assert_eq!(generichide, "0");
        let sel = e.hidden_class_id_selectors(["ad-banner", "content"], ["ad_top"], &HashSet::new());
        assert!(sel.contains(&".ad-banner".to_string()) && sel.contains(&"#ad_top".to_string()), "{sel:?}");
    }

    #[test]
    fn serialize_round_trip() {
        let e = engine();
        let data = e.serialize();
        let mut e2 = Engine::default();
        e2.deserialize(&data).unwrap();
        assert_eq!(check(&e2, "https://ads.example.com/x.js", "https://news.test/", "script", "GET"), Verdict::Block);
    }

    #[test]
    fn validate_and_metadata() {
        assert_eq!(validate("||ads.example.com^"), None);
        assert_eq!(validate("example.com##.ad"), None);
        assert_eq!(validate("! comment"), None);
        assert!(validate("||x.com^$unknownoption123").is_some());
        let [title, _home, hours] = list_metadata(RULES);
        assert_eq!(title, "Test list");
        assert_eq!(hours, "96");
    }

    fn b64(s: &str) -> String {
        use std::fmt::Write;
        // tiny base64 encoder for the tests (no extra dependency)
        const T: &[u8; 64] = b"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        let b = s.as_bytes();
        let mut out = String::new();
        for c in b.chunks(3) {
            let n = (c[0] as u32) << 16 | (*c.get(1).unwrap_or(&0) as u32) << 8 | *c.get(2).unwrap_or(&0) as u32;
            for i in 0..4 {
                if i <= c.len() { out.write_char(T[(n >> (18 - 6 * i) & 63) as usize] as char).unwrap(); } else { out.push('='); }
            }
        }
        out
    }

    fn scriptlet_resources() -> String {
        format!(
            r#"[{{"name":"set-constant.js","aliases":["set.js"],"kind":{{"mime":"application/javascript"}},"content":"{}","dependencies":["helper.fn"]}},
               {{"name":"helper.fn","aliases":[],"kind":{{"mime":"fn/javascript"}},"content":"{}","dependencies":[]}},
               {{"name":"trusted-set-constant.js","aliases":["trusted-set.js"],"kind":{{"mime":"application/javascript"}},"content":"{}","dependencies":[],"permission":1}}]"#,
            b64("function setConstant(a, b) { helper(); window[a] = b; }"),
            b64("function helper() {}"),
            b64("function trustedSetConstant(a, b) { window[a] = b; }"),
        )
    }

    #[test]
    fn function_style_scriptlets_and_trust() {
        let lists = vec![
            "example.com##+js(set, foo, true)\nexample.com##+js(trusted-set, bar, 1)".to_string(),
            "trusted.example##+js(trusted-set, baz, 2)".to_string(),
        ];
        let mut e = build_engine_trust(&lists, &[false, true]);
        assert_eq!(use_resources(&mut e, &scriptlet_resources()), Ok(3));
        let [_, _, _, script, _] = cosmetic(&e, "https://example.com/");
        assert!(script.contains("function setConstant") && script.contains("function helper"), "{script}");
        assert!(script.contains("foo"), "{script}");
        assert!(!script.contains("trustedSetConstant"), "untrusted list must not run a trusted scriptlet: {script}");
        let [_, _, _, script2, _] = cosmetic(&e, "https://trusted.example/");
        assert!(script2.contains("trustedSetConstant") && script2.contains("baz"), "{script2}");
    }

    /// Checks the real uBO resources built by tools/ubo-resources/build.py:
    /// `UBO_RESOURCES=path cargo test --release -- --ignored real_ubo_resources`
    #[test]
    #[ignore]
    fn real_ubo_resources() {
        let path = std::env::var("UBO_RESOURCES").expect("UBO_RESOURCES");
        let json = std::fs::read_to_string(path).unwrap();
        let lists = vec![
            "example.com##+js(set, foo, true)\nexample.com##+js(json-prune, ads)\nexample.com##+js(aopr, adsbygoogle)\n||ads.example.com/a.js$script,redirect=noopjs\n||ads.example.com/p.gif$image,redirect=1x1.gif".to_string(),
            "example.com##+js(trusted-set, bar, 1)".to_string(),
        ];
        let mut e = build_engine_trust(&lists, &[true, false]);
        let n = use_resources(&mut e, &json).expect("resources load");
        assert!(n > 100, "{n}");
        let [_, _, _, script, _] = cosmetic(&e, "https://example.com/");
        assert!(script.contains("setConstant") && script.contains("jsonPrune") && script.contains("abortOnPropertyRead"), "len {}", script.len());
        assert!(!script.contains("trustedSetConstant("), "untrusted list ran a trusted scriptlet");
        assert!(matches!(check(&e, "https://ads.example.com/a.js", "https://example.com/", "script", "GET"), Verdict::Redirect(ref b) if b.starts_with("data:application/javascript")));
        assert!(matches!(check(&e, "https://ads.example.com/p.gif", "https://example.com/", "image", "GET"), Verdict::Redirect(ref b) if b.starts_with("data:image/gif")));
        println!("resources {n}, scriptlet JS for example.com: {} bytes", script.len());
    }

    #[test]
    fn stats_count_rules_and_rejects() {
        let text = "! comment\n[Adblock Plus 2.0]\n\n||ads.example.com^\nexample.com##.ad\n||x.com^$unknownoption123\n# hosts comment\n";
        let (ok, bad, ex) = list_stats(text, 5);
        assert_eq!((ok, bad), (2, 1), "{ex:?}");
        assert!(ex[0].starts_with("||x.com^$unknownoption123"), "{ex:?}");
    }

    #[test]
    fn bad_input_does_not_panic() {
        let e = engine();
        assert_eq!(check(&e, "not a url", "", "", ""), Verdict::Allow);
        let _ = cosmetic(&e, "");
        let mut e = engine();
        assert!(use_resources(&mut e, "{nope").is_err());
    }
}
