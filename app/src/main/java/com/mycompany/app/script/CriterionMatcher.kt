package com.mycompany.app.script

import android.text.TextUtils
import java.util.Locale

@Suppress("deprecation")
object CriterionMatcher {
    @JvmStatic
    fun matches(criterion: String?, text: String?): Boolean {
        if (TextUtils.isEmpty(criterion) || TextUtils.isEmpty(text)) {
            return true
        }
        val locale = Locale.US
        val lowerCase = criterion!!.lowercase(locale)
        val lowerText = text!!.lowercase(locale)
        if (lowerCase.length < 2 || !lowerCase.startsWith("/") || !lowerCase.endsWith("/")) {
            return globMatch(lowerCase, 0, 0, lowerText)
        }
        var pattern: String? = null
        if (lowerCase.length >= 3) {
            val regexBody = lowerCase.let { s -> s.substring(1, s.length - 1) }
            if (!TextUtils.isEmpty(regexBody)) {
                val sb = StringBuilder()
                if (!regexBody.startsWith("^")) {
                    sb.append("^.*")
                }
                sb.append(regexBody)
                if (!regexBody.endsWith("$")) {
                    sb.append(".*$")
                }
                pattern = sb.toString()
            }
        }
        if (TextUtils.isEmpty(pattern)) {
            return false
        }
        return lowerText.matches(pattern!!.toRegex())
    }

    /**
     * Glob match of pattern from index p against text from index t: '*' matches any run (a trailing '*' matches
     * the rest), '\\' escapes the next character; a successful '*' match returns true at once.
     */
    @JvmStatic
    fun globMatch(pattern: String, patternIndex: Int, textIndex: Int, lowerCase: String): Boolean {
        var patternIndex = patternIndex
        var textIndex = textIndex
        val plen = pattern.length
        val tlen = lowerCase.length
        while (patternIndex != plen) {
            var c = pattern.get(patternIndex)
            if (c == '*') {
                val next = patternIndex + 1
                if (next >= plen) {
                    return true
                }
                while (!globMatch(pattern, next, textIndex, lowerCase)) {
                    if (textIndex == tlen) {
                        return false
                    }
                    textIndex++
                }
                return true
            }
            if (textIndex == tlen) {
                return false
            }
            if (c == '\\') {
                patternIndex++
                if (patternIndex >= plen) {
                    return false
                }
                c = pattern.get(patternIndex)
            }
            if (c != lowerCase.get(textIndex)) {
                return false
            }
            patternIndex++
            textIndex++
        }
        return textIndex == tlen
    }
}
