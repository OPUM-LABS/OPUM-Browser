package com.mycompany.app.main

@Suppress("deprecation")
object InitialSearch {
    @JvmField
    val initials: CharArray = charArrayOf(
        12593.toChar(),
        12594.toChar(),
        12596.toChar(),
        12599.toChar(),
        12600.toChar(),
        12601.toChar(),
        12609.toChar(),
        12610.toChar(),
        12611.toChar(),
        12613.toChar(),
        12614.toChar(),
        12615.toChar(),
        12616.toChar(),
        12617.toChar(),
        12618.toChar(),
        12619.toChar(),
        12620.toChar(),
        12621.toChar(),
        12622.toChar()
    )

    /**
     * Finds `str3` in `str2`, where a Hangul initial consonant in `str3` also matches
     * any syllable starting with it. Returns the matched part of `str` (same indices), or null.
     */
    @JvmStatic
    fun initialMatch(title: String?, lowerTitle: String?, query: String?): String? {
        if (title == null || lowerTitle == null || query == null) {
            return null
        }
        val length = query.length
        val spare = lowerTitle.length - length
        if (spare < 0) {
            return null
        }
        var sb: StringBuilder? = null
        for (index3 in 0..spare) {
            var index = 0
            while (index < length) {
                val titlePos = index + index3
                val cCharAt = lowerTitle.get(titlePos)
                val queryChar = query.get(index)
                val match: Boolean
                var isInitial = false
                if (44032 <= cCharAt.code && cCharAt.code <= 55203) {
                    for (index4 in 0..18) {
                        if (initials[index4] == queryChar) {
                            isInitial = true
                            break
                        }
                    }
                }
                if (isInitial) {
                    val initialIndex = (cCharAt.code - 44032) / 588
                    match = (if (initialIndex < 0 || initialIndex >= 19) 0.toChar() else initials[initialIndex]) == queryChar
                } else {
                    match = cCharAt == queryChar
                }
                if (!match) {
                    sb = null
                    break
                }
                if (sb == null) {
                    sb = StringBuilder()
                }
                sb.append(title.get(titlePos))
                index++
            }
            if (index == length) {
                return if (sb != null) sb.toString() else null
            }
        }
        return null
    }
}
