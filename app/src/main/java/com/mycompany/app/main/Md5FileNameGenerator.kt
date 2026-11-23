package com.mycompany.app.main

import java.math.BigInteger
import java.security.MessageDigest

/**
 * File names from strings: MD5 of the string's bytes (platform charset, UTF-8 on Android) as a base-36 number.
 * Taken over unchanged from Universal Image Loader's `Md5FileNameGenerator`, because the app names cached pages,
 * downloads, filters and backup entries with it: the output must stay byte-identical so existing files are found.
 */
class Md5FileNameGenerator {
    fun generate(pageUrl: String?): String {
        val digest = MessageDigest.getInstance("MD5").digest(pageUrl!!.toByteArray())
        return BigInteger(digest).abs().toString(36)
    }
}
