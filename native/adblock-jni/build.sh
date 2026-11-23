#!/bin/sh
# Builds libopumadblock.so for the four ABIs libmyutil.so ships for into app/src/main/jniLibs (checked in, so a normal Gradle build
# needs no Rust) and a host library into target/release for the JVM unit tests (UboEngineTest).
# Needs: rustup with the Android targets, cargo-ndk, the NDK from local.properties' sdk.dir.
#   rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android i686-linux-android
#   cargo install cargo-ndk
set -eu
cd "$(dirname "$0")"
ROOT="$(cd ../.. && pwd)"
[ -f "$HOME/.cargo/env" ] && . "$HOME/.cargo/env"

if [ -z "${ANDROID_NDK_HOME:-}" ]; then
    SDK="$(sed -n 's/^sdk\.dir=//p' "$ROOT/local.properties")"
    ANDROID_NDK_HOME="$(ls -d "$SDK"/ndk/* | sort -V | tail -1)"
    export ANDROID_NDK_HOME
fi

cargo test --release
cargo ndk -t arm64-v8a -t armeabi-v7a -t x86_64 -t x86 -P 32 -o "$ROOT/app/src/main/jniLibs" build --release
cargo build --release   # host library for the JVM unit tests
ls -l "$ROOT"/app/src/main/jniLibs/*/libopumadblock.so
