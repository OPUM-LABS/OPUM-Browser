# adblock-jni

JNI bridge from the OPUM Browser web filter to [Brave's adblock-rust](https://github.com/brave/adblock-rust)
(`adblock` crate, pinned to 0.13.6), which reads uBlock Origin / Adblock Plus filter syntax.
Plan and background: `UBLOCK_MIGRATION_PLAN.md` in the repo root.

* Kotlin side: `app/src/main/java/com/mycompany/app/web/filter/UboEngine.kt` (static `native` methods,
  engine passed as a `long` handle; thread-safe; every native entry point catches panics).
* Built libraries are **checked in** at `app/src/main/jniLibs/<abi>/libopumadblock.so` (arm64-v8a, armeabi-v7a,
  x86_64, x86), so a normal Gradle build needs no Rust.
* Rebuild after changing this crate: `native/adblock-jni/build.sh` (runs the Rust tests, builds the four Android
  libraries with `cargo-ndk`, API 32, and a host library in `target/release` used by the JVM test
  `UboEngineTest`; that test is skipped when the host library is missing).
* The crate's `single-thread` feature is off on purpose: the WebView checks requests from several threads.

## Licences of what is linked into libopumadblock.so
* `adblock` 0.13.6: MPL-2.0 (text: `app/src/main/assets/license/MPL2.txt`). If its files are ever modified,
  the modified source must be published.
* ICU4X crates (`icu_*`, `zerovec`, `yoke`, `tinystr`, ... via `idna`): Unicode-3.0 (`assets/license/Unicode.txt`).
* All others (jni, regex, serde, serde_json, idna, psl, flatbuffers, addr, memchr, aho-corasick, ...): MIT and/or
  Apache-2.0 (some also Unlicense). No GPL code. Regenerate the list with `cargo metadata` when dependencies change.
