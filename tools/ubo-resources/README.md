# ubo-resources

Builds `app/src/main/assets/filter/ubo-resources.json` (+ `.meta.json`): uBlock Origin's scriptlets and redirect
resources in the format adblock-rust's `use_resources()` reads.

1. Download a uBlock Origin release source (e.g. `https://codeload.github.com/gorhill/uBlock/tar.gz/refs/tags/<version>`)
   into an empty folder outside the repo and unpack it.
2. `python3 -I tools/ubo-resources/build.py <unpacked uBlock dir> app/src/main/assets/filter/ubo-resources.json --check`
   (reads uBO's files as text only; `--check` runs `node --check`, a syntax check, on every extracted function).
3. Copy uBO's `LICENSE.txt` to `app/src/main/assets/license/GPL3.txt` if it changed; run the unit tests
   (`UboEngineTest.builtInUboResources`) and, for the native side,
   `UBO_RESOURCES=<json> cargo test --release -- --ignored real_ubo_resources` in `native/adblock-jni`.

The output is uBO code and data: **GPLv3**. Scriptlets marked `requiresTrust` get `permission: 1`; the app gives that
permission only to uBO's own lists (`UboFilter.isTrustedListUrl`).
