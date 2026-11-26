package com.mycompany.app.ui.settings

/**
 * One bundled open source library shown in the license list. [licenseId] is the file name in `assets/license/`;
 * [adBlock] puts it under "Ad blocker" instead of "App libraries".
 */
data class LicenseEntry(
    val name: String,
    val url: String,
    val author: String,
    val licenseId: String,
    val adBlock: Boolean = false,
)

/**
 * Libraries shipped with the app. Keep in sync with what the release build ships: the dependencies in
 * `app/build.gradle` and what they bring (`./gradlew :app:dependencies --configuration releaseRuntimeClasspath`),
 * the crates of `native/adblock-jni` and the bundled assets. LicenseDataTest checks the license files.
 */
object LicenseData {
    /** Shown name of each license file. */
    val licenseNames: Map<String, String> = mapOf(
        "Apache2" to "Apache License 2.0",
        "MIT" to "MIT License",
        "MPL" to "Mozilla Public License 1.1",
        "MPL2" to "Mozilla Public License 2.0",
        "GPL3" to "GNU General Public License 3.0",
        "GPL2CE" to "GNU GPL 2.0 with Classpath Exception",
        "Unicode" to "Unicode License v3",
        "Glide" to "Glide license",
    )

    fun licenseName(id: String): String = licenseNames[id] ?: id

    private val all: List<LicenseEntry> = listOf(
        // App libraries
        LicenseEntry("AndroidX libraries", "https://developer.android.com/jetpack/androidx", "Google", "Apache2"),
        LicenseEntry("AndroidSVG", "https://github.com/BigBadaboom/androidsvg", "Paul LeBeau", "Apache2"),
        LicenseEntry("Apache Commons (Codec, Compress, IO, Lang, Text)", "https://commons.apache.org", "Apache Software Foundation", "Apache2"),
        LicenseEntry("Glide", "https://github.com/bumptech/glide", "Bump Technologies", "Glide"),
        LicenseEntry("GlideWebpDecoder", "https://github.com/zjupure/GlideWebpDecoder", "Gray Liu", "Apache2"),
        LicenseEntry("Gson", "https://github.com/google/gson", "Google", "Apache2"),
        LicenseEntry("Guava: Google Core Libraries for Java", "https://github.com/google/guava", "Google", "Apache2"),
        LicenseEntry("Java library backport (desugar_jdk_libs)", "https://github.com/google/desugar_jdk_libs", "Google, Oracle", "GPL2CE"),
        LicenseEntry("JetBrains Java Annotations", "https://github.com/JetBrains/java-annotations", "JetBrains", "Apache2"),
        LicenseEntry("Jetpack Compose (UI, Foundation, Material 3, Material Icons)", "https://developer.android.com/jetpack/compose", "Google", "Apache2"),
        LicenseEntry("JSpecify", "https://jspecify.dev", "The JSpecify Authors", "Apache2"),
        LicenseEntry("jsoup", "https://jsoup.org", "Jonathan Hedley", "MIT"),
        LicenseEntry("juniversalchardet", "https://github.com/albfernandez/juniversalchardet", "Alberto Fernández", "MPL"),
        LicenseEntry("Kotlin standard library, coroutines and serialization", "https://kotlinlang.org", "JetBrains", "Apache2"),
        LicenseEntry("Material Color Utilities", "https://github.com/material-foundation/material-color-utilities", "Google", "Apache2"),
        LicenseEntry("Material Symbols", "https://fonts.google.com/icons", "Google", "Apache2"),
        LicenseEntry("Media3 (ExoPlayer)", "https://developer.android.com/jetpack/androidx/releases/media3", "Google", "Apache2"),
        LicenseEntry("OkHttp", "https://square.github.io/okhttp", "Square", "Apache2"),
        LicenseEntry("Okio", "https://square.github.io/okio", "Square", "Apache2"),
        LicenseEntry("Public Suffix List", "https://publicsuffix.org", "Mozilla Foundation", "MPL2"),
        LicenseEntry("Readability.js", "https://github.com/mozilla/readability", "Mozilla", "Apache2"),
        LicenseEntry("Readability4J", "https://github.com/dankito/Readability4J", "dankito", "Apache2"),
        LicenseEntry("SLF4J API", "https://www.slf4j.org", "QOS.ch", "MIT"),
        LicenseEntry("Zip4j", "https://github.com/srikanth-lingala/zip4j", "Srikanth Reddy Lingala", "Apache2"),
        // Ad blocker
        LicenseEntry("adblock-rust", "https://github.com/brave/adblock-rust", "Brave Software", "MPL2", adBlock = true),
        LicenseEntry("adblock-resources (Brave scriptlets)", "https://github.com/brave/adblock-resources", "Brave Software", "MPL2", adBlock = true),
        LicenseEntry("ICU4X (used by idna)", "https://github.com/unicode-org/icu4x", "Unicode, Inc.", "Unicode", adBlock = true),
        LicenseEntry("Rust libraries (jni, regex, serde, idna and others)", "https://crates.io", "The Rust project developers and others", "Apache2", adBlock = true),
        LicenseEntry("uBlock Origin (scriptlets and redirect resources)", "https://github.com/gorhill/uBlock", "Raymond Hill and contributors", "GPL3", adBlock = true),
    )

    /** Every library, the app's own first, each group sorted by name. */
    val entries: List<LicenseEntry> = all.sortedWith(compareBy({ it.adBlock }, { it.name.lowercase() }))
}
