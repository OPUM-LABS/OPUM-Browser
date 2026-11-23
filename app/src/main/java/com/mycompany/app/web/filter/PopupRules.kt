package com.mycompany.app.web.filter

/**
 * `$popup` support for the pop-up blocker.
 *
 * adblock-rust rejects every rule with the `popup` option (Brave blocks pop-ups elsewhere), so the thousands of
 * `$popup` rules in EasyList, AdGuard and uBO's lists were lost. [split] separates a list into
 *  - the main text for the network / cosmetic engine: `popup` removed from rules that also name resource types
 *    (`$popup,subdocument` → `$subdocument`); rules that only target pop-ups are left out (without `popup` they
 *    would block every request to that address), and
 *  - the pop-up text for a small second engine: the `popup` rules with `popup` and the resource-type options
 *    removed (`||ads.example^$popup,third-party` → `||ads.example^$third-party`), checked with the URL a page
 *    opens in a new window and the opener page as source.
 * Exceptions (`@@…$popup`) go the same way. Cosmetic rules and comments stay in the main text.
 */
object PopupRules {
    /** Resource-type options (and their uBO aliases); they make no sense for a pop-up check. */
    private val TYPE_OPTIONS = setOf(
        "script", "image", "stylesheet", "css", "object", "xmlhttprequest", "xhr", "subdocument", "frame", "ping",
        "beacon", "media", "font", "websocket", "other", "document", "doc", "webrtc", "object-subrequest",
    )

    /** Options that only matter for responses / redirects; dropped from pop-up rules. */
    private val DROP_FOR_POPUP = setOf("redirect", "redirect-rule", "rewrite", "csp", "removeparam", "empty", "mp4")

    class Split(val main: String, val popup: String, val popupRules: Int)

    @JvmStatic
    fun split(text: String): Split {
        if (!text.contains("popup")) return Split(text, "", 0)
        val main = StringBuilder(text.length)
        val popup = StringBuilder()
        var count = 0
        for (raw in text.lineSequence()) {
            val parts = splitLine(raw.trim())
            if (parts == null) {
                main.append(raw).append('\n')
                continue
            }
            parts.first?.let { main.append(it).append('\n') }
            parts.second?.let { popup.append(it).append('\n'); count++ }
        }
        return Split(main.toString(), popup.toString(), count)
    }

    /**
     * For a network rule with a `popup` option: (main line or null, pop-up line or null). Null for any other line
     * (it goes to the main text unchanged).
     */
    @JvmStatic
    fun splitLine(line: String): Pair<String?, String?>? {
        if (line.isEmpty() || line[0] == '!' || line.startsWith("[")) return null
        if (isCosmetic(line)) return null
        val dollar = optionsStart(line)
        if (dollar < 0) return null
        val pattern = line.substring(0, dollar)
        val options = line.substring(dollar + 1).split(',').map { it.trim() }.filter { it.isNotEmpty() }
        if (options.none { it == "popup" }) return null
        val rest = options.filter { it != "popup" }
        val types = rest.filter { name(it) in TYPE_OPTIONS }
        val mainLine = if (types.any { !it.startsWith("~") }) pattern + "$" + rest.joinToString(",") else null
        val popupOpts = rest.filter { name(it) !in TYPE_OPTIONS && name(it) !in DROP_FOR_POPUP }
        val popupLine = if (popupOpts.isEmpty()) pattern else pattern + "$" + popupOpts.joinToString(",")
        return mainLine to popupLine.takeIf { it.isNotEmpty() && it != "@@" }
    }

    private fun name(option: String): String = option.removePrefix("~").substringBefore('=').lowercase()

    private fun isCosmetic(line: String): Boolean =
        line.contains("##") || line.contains("#@#") || line.contains("#?#") || line.contains("#$#") ||
            line.contains("#%#") || line.contains("#@?#") || line.contains("#@$#")

    /** Index of the `$` that starts the options, or -1 (a `$` inside a /regex/ pattern does not count). */
    private fun optionsStart(line: String): Int {
        val body = if (line.startsWith("@@")) 2 else 0
        // "/regex/" without options: a `$` in it is part of the regex
        if (line.length > body + 2 && line[body] == '/' && line.endsWith("/")) return -1
        val dollar = line.lastIndexOf('$')
        return if (dollar > body) dollar else -1
    }
}
