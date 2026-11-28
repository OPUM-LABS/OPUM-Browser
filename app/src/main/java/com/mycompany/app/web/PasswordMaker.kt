package com.mycompany.app.web

import java.security.SecureRandom

/**
 * Strong passwords for sign-up and change-password forms (2026-11-28): 16 characters from a cryptographic random
 * source, at least one each of lower case, upper case, digit and symbol, without look-alikes (0 O o, 1 l I) so it can
 * be read and typed off the screen.
 */
object PasswordMaker {
    private const val LOWER = "abcdefghijkmnpqrstuvwxyz"
    private const val UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    private const val DIGITS = "23456789"
    private const val SYMBOLS = "!#%+-=?@_"
    private const val LENGTH = 16
    private val random = SecureRandom()

    fun make(): String {
        val all = LOWER + UPPER + DIGITS + SYMBOLS
        val chars = ArrayList<Char>()
        for (set in listOf(LOWER, UPPER, DIGITS, SYMBOLS)) chars.add(set[random.nextInt(set.length)])
        while (chars.size < LENGTH) chars.add(all[random.nextInt(all.length)])
        // the guaranteed ones end up anywhere
        for (i in chars.indices.reversed()) {
            val j = random.nextInt(i + 1)
            val t = chars[i]; chars[i] = chars[j]; chars[j] = t
        }
        return chars.joinToString("")
    }
}
