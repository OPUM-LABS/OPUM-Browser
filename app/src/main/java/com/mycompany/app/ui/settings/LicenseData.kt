package com.mycompany.app.ui.settings

/** One bundled open source library shown in the license list. [licenseId] is the file name in `assets/license/`. */
data class LicenseEntry(val name: String, val url: String, val author: String, val licenseId: String)

/** Libraries shipped with the app. Keep in sync with the dependencies in `app/build.gradle`. */
object LicenseData {
    val entries: List<LicenseEntry> = listOf(
        LicenseEntry("Android SDK", "https://developer.android.com", "Google", "Apache2"),
        LicenseEntry("Activity Compose", "https://developer.android.com/jetpack/androidx/releases/activity", "Google", "Apache2"),
        LicenseEntry("adblock-rust", "https://github.com/brave/adblock-rust", "Brave Software", "MPL2"),
        LicenseEntry("adblock-resources (Brave scriptlets)", "https://github.com/brave/adblock-resources", "Brave Software", "MPL2"),
        LicenseEntry("AndroidSVG", "https://github.com/BigBadaboom/androidsvg", "Paul LeBeau", "Apache2"),
        LicenseEntry("Apache Commons Compress", "https://commons.apache.org/proper/commons-compress", "Apache Software Foundation", "Apache2"),
        LicenseEntry("Apache Commons Text", "https://commons.apache.org/proper/commons-text", "Apache Software Foundation", "Apache2"),
        LicenseEntry("AppCompat", "https://developer.android.com/jetpack/androidx/releases/appcompat", "Google", "Apache2"),
        LicenseEntry("Biometric", "https://developer.android.com/jetpack/androidx/releases/biometric", "Google", "Apache2"),
        LicenseEntry("Browser", "https://developer.android.com/jetpack/androidx/releases/browser", "Google", "Apache2"),
        LicenseEntry("Jetpack Compose (UI, Foundation, Material 3, Material Icons)", "https://developer.android.com/jetpack/compose", "Google", "Apache2"),
        LicenseEntry("Exifinterface", "https://developer.android.com/jetpack/androidx/releases/exifinterface", "Google", "Apache2"),
        LicenseEntry("Glide", "https://github.com/bumptech/glide", "Bump Technologies", "Glide"),
        LicenseEntry("GlideWebpDecoder", "https://github.com/zjupure/GlideWebpDecoder", "Gray Liu", "Apache2"),
        LicenseEntry("Gson", "https://github.com/google/gson", "Google", "Apache2"),
        LicenseEntry("Guava: Google Core Libraries for Java", "https://github.com/google/guava", "Google", "Apache2"),
        LicenseEntry("jsoup", "https://jsoup.org", "Jonathan Hedley", "MIT"),
        LicenseEntry("juniversalchardet", "https://github.com/albfernandez/juniversalchardet", "Alberto Fernández", "MPL"),
        LicenseEntry("Lifecycle (Runtime Compose, ViewModel Compose)", "https://developer.android.com/jetpack/androidx/releases/lifecycle", "Google", "Apache2"),
        LicenseEntry("Material Components for Android", "https://github.com/material-components/material-components-android", "Google", "Apache2"),
        LicenseEntry("Media", "https://developer.android.com/jetpack/androidx/releases/media", "Google", "Apache2"),
        LicenseEntry("NanoHttpd", "https://github.com/NanoHttpd/nanohttpd", "NanoHttpd", "NanoHttpd"),
        LicenseEntry("Play Services AdMob", "https://developers.google.com/android", "Google", "Android"),
        LicenseEntry("Play Services ML Kit", "https://developers.google.com/android", "Google", "Android"),
        LicenseEntry("Readability4J", "https://github.com/dankito/Readability4J", "dankito", "Apache2"),
        LicenseEntry("Readability.js", "https://github.com/mozilla/readability", "Mozilla", "Apache2"),
        LicenseEntry("Rust libraries used by adblock-rust (jni, regex, serde, idna, psl, flatbuffers, ...)", "https://crates.io", "The Rust project developers and others", "Apache2"),
        LicenseEntry("ICU4X (used by idna)", "https://github.com/unicode-org/icu4x", "Unicode, Inc.", "Unicode"),
        LicenseEntry("uBlock Origin (scriptlets and redirect resources)", "https://github.com/gorhill/uBlock", "Raymond Hill and contributors", "GPL3"),
        LicenseEntry("Webkit", "https://developer.android.com/jetpack/androidx/releases/webkit", "Google", "Apache2"),
        LicenseEntry("XZ for Java", "https://tukaani.org/xz/java.html", "Tukaani Project", "XZ"),
        LicenseEntry("Zip4j", "https://github.com/srikanth-lingala/zip4j", "Srikanth Reddy Lingala", "Apache2"),
    )
}
