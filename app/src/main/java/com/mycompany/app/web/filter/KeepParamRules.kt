package com.mycompany.app.web.filter

/**
 * `$removeparam` exceptions for the tracking-parameter stripping.
 *
 * adblock-rust removes named parameters (`$removeparam=fbclid`) but ignores every exception
 * (for example `@@||urldefense.com^$removeparam` and the web.archive.org ones), so links the lists deliberately
 * protect would be changed anyway. [extract] collects those exceptions from a list as plain rules (without `@@` and
 * without the `removeparam` / resource-type options, other options such as `domain=` kept) for a small separate
 * engine; `UboFilter` only uses a cleaned URL when none of them matches the address (checked as type `other`:
 * adblock-rust matches rules with a path against page loads only when they say `$document`). A per-parameter exception
 * (`$removeparam=x`) therefore keeps every parameter of a matching address — less stripping, never a broken link.
 */
object KeepParamRules {
    private val DROP = setOf(
        "removeparam", "script", "image", "stylesheet", "css", "object", "xmlhttprequest", "xhr", "subdocument",
        "frame", "ping", "beacon", "media", "font", "websocket", "other", "document", "doc", "popup", "important",
    )

    /** The `@@…$removeparam` exceptions of [text] as plain rules, one per line, and how many there were. */
    @JvmStatic
    fun extract(text: String): Pair<String, Int> {
        if (!text.contains("removeparam")) return "" to 0
        val out = StringBuilder()
        var count = 0
        for (raw in text.lineSequence()) {
            val rule = toKeepRule(raw.trim()) ?: continue
            out.append(rule).append('\n')
            count++
        }
        return out.toString() to count
    }

    /** `@@<pattern>$…removeparam…` → `<pattern>[$<other options>]`, else null. */
    @JvmStatic
    fun toKeepRule(line: String): String? {
        if (!line.startsWith("@@")) return null
        val body = line.substring(2)
        if (body.length > 2 && body[0] == '/' && body.endsWith("/")) return null // regex without options
        val dollar = body.lastIndexOf('$')
        if (dollar < 0) return null
        val options = body.substring(dollar + 1).split(',').map { it.trim() }.filter { it.isNotEmpty() }
        if (options.none { it == "removeparam" || it.startsWith("removeparam=") }) return null
        val keep = options.filter { it.removePrefix("~").substringBefore('=').lowercase() !in DROP }
        val pattern = body.substring(0, dollar)
        if (pattern.isEmpty() && keep.isEmpty()) return null // "@@$removeparam" alone would match everything
        return if (keep.isEmpty()) pattern else pattern + "$" + keep.joinToString(",")
    }
}
