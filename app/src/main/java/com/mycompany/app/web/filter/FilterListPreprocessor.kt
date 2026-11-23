package com.mycompany.app.web.filter

/**
 * uBlock Origin's filter list pre-parsing directives:
 * `!#if <condition>` / `!#else` / `!#endif` (nestable; conditions with `!`, `&&`, `||`, parentheses) and
 * `!#include <file>`. adblock-rust treats them as comments, so without this both branches of every `!#if` block
 * would be loaded (e.g. Firefox-only rules).
 *
 * Tokens are evaluated for this app: an Android WebView is `env_mobile` and `env_chromium`; everything else uBO
 * knows (`env_firefox`, `env_safari`, `env_edge`, `ext_ubol`, `ext_devbuild`, `cap_html_filtering`,
 * `cap_ipaddress`, `cap_user_stylesheet`, `adguard*`, `false`, unknown tokens) is false, as uBO does for unknown ones.
 */
object FilterListPreprocessor {
    private val TRUE_TOKENS = setOf("env_mobile", "env_chromium", "true")

    /** The value of one `!#if` condition for this app. */
    @JvmStatic
    fun condition(expr: String): Boolean = try {
        Parser(expr).parse()
    } catch (_: Exception) {
        false
    }

    /**
     * [text] without inactive `!#if` blocks and without the directive lines themselves (includes are kept), and
     * without the cosmetic rules [dropUnsupported] removes.
     */
    @JvmStatic
    fun evaluate(input: String): String = directives(dropUnsupported(input))

    /** [text] without inactive `!#if` blocks and without the directive lines themselves (includes are kept). */
    @JvmStatic
    fun directives(text: String): String {
        if (!text.contains("!#if")) return text
        val out = StringBuilder(text.length)
        // stack of (this block is active, the enclosing block is active)
        val stack = ArrayList<BooleanArray>()
        var active = true
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            when {
                line.startsWith("!#if ") || line == "!#if" -> {
                    val cond = condition(line.removePrefix("!#if").trim())
                    stack.add(booleanArrayOf(cond, active))
                    active = active && cond
                }
                line.startsWith("!#else") -> {
                    val top = stack.lastOrNull() ?: continue
                    top[0] = !top[0]
                    active = top[1] && top[0]
                }
                line.startsWith("!#endif") -> {
                    val top = stack.removeLastOrNull() ?: continue
                    active = top[1]
                }
                active -> out.append(raw).append('\n')
            }
        }
        return out.toString()
    }

    /**
     * Removes cosmetic rules adblock-rust would turn into their opposite. uBO's `:others()` operator
     * (`site##a, b, c:others()`: hide everything on the page except a, b, c) is unknown to adblock-rust, which splits
     * the selector list at the commas and keeps the plain parts as ordinary hide rules, so exactly the elements the
     * rule wants to keep get hidden (2026-11-20: igorslab.de's main content disappeared). Such rules are left out of
     * the engine's input; [OthersRules] collects them and the cosmetic script applies them.
     */
    @JvmStatic
    fun dropUnsupported(text: String): String {
        if (!text.contains(":others()")) return text
        val out = StringBuilder(text.length)
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            val cosmetic = line.contains("##") || line.contains("#@#") || line.contains("#?#")
            if (cosmetic && !line.startsWith("!") && line.contains(":others()")) continue
            out.append(raw).append('\n')
        }
        return out.toString()
    }

    /** File names of the `!#include` lines of an already [evaluate]d text. */
    @JvmStatic
    fun includes(text: String): List<String> =
        text.lineSequence().map { it.trim() }.filter { it.startsWith("!#include ") }
            .map { it.removePrefix("!#include ").trim() }.filter { it.isNotEmpty() }.toList()

    /**
     * URL of include [name] of the list at [listUrl]: like uBO, only files in the list's own folder (or below) on
     * the same host are allowed — no absolute URLs, no `..`. Null when not allowed.
     */
    @JvmStatic
    fun includeUrl(listUrl: String, name: String): String? {
        if (name.contains("://") || name.startsWith("/") || name.split('/').any { it == ".." || it == "." }) return null
        if (name.any { it.isWhitespace() || it == '?' || it == '#' }) return null
        val base = listUrl.substringBefore('?').substringBefore('#')
        val slash = base.lastIndexOf('/')
        if (slash < base.indexOf("://") + 3) return null
        return base.substring(0, slash + 1) + name
    }

    /**
     * The whole list: [text] evaluated, every allowed `!#include` replaced by the evaluated part [fetch] returns
     * (recursively, up to [depth] levels; a part that cannot be fetched is left out). [listUrl] resolves the includes.
     */
    @JvmStatic
    @JvmOverloads
    fun assemble(text: String, listUrl: String?, fetch: (String) -> String?, depth: Int = 3): String {
        // downloaded files keep the :others() rules (OthersRules reads them when the engine is built)
        val evaluated = directives(text)
        if (listUrl == null || depth <= 0 || !evaluated.contains("!#include ")) return evaluated
        val out = StringBuilder(evaluated.length)
        for (raw in evaluated.lineSequence()) {
            val line = raw.trim()
            if (!line.startsWith("!#include ")) {
                out.append(raw).append('\n')
                continue
            }
            val url = includeUrl(listUrl, line.removePrefix("!#include ").trim()) ?: continue
            val part = fetch(url) ?: continue
            out.append("! ---- included: ").append(url).append('\n')
            out.append(assemble(part, url, fetch, depth - 1))
            if (out.isNotEmpty() && out[out.length - 1] != '\n') out.append('\n')
        }
        return out.toString()
    }

    /** `expr := or; or := and ('||' and)*; and := unary ('&&' unary)*; unary := '!' unary | '(' or ')' | token`. */
    private class Parser(private val s: String) {
        private var i = 0

        fun parse(): Boolean {
            val v = or()
            skip()
            require(i == s.length) { "trailing input" }
            return v
        }

        private fun skip() {
            while (i < s.length && s[i].isWhitespace()) i++
        }

        private fun or(): Boolean {
            var v = and()
            while (true) {
                skip()
                if (s.startsWith("||", i)) { i += 2; v = and() or v } else return v
            }
        }

        private fun and(): Boolean {
            var v = unary()
            while (true) {
                skip()
                if (s.startsWith("&&", i)) { i += 2; v = unary() and v } else return v
            }
        }

        private fun unary(): Boolean {
            skip()
            require(i < s.length) { "missing operand" }
            if (s[i] == '!') { i++; return !unary() }
            if (s[i] == '(') {
                i++
                val v = or()
                skip()
                require(i < s.length && s[i] == ')') { "missing )" }
                i++
                return v
            }
            val start = i
            while (i < s.length && (s[i].isLetterOrDigit() || s[i] == '_')) i++
            require(i > start) { "token expected" }
            return s.substring(start, i) in TRUE_TOKENS
        }
    }
}
