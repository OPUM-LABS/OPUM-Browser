package com.mycompany.app.web.filter

/**
 * uBlock Origin's `:others()` cosmetic operator (2026-11-20): `site##a, b, c:others()` hides every element of the
 * page except the targets (a, b, c), their ancestors and their descendants. adblock-rust does not know the operator
 * and would split the selector list into plain hide rules (the opposite of the rule), so [FilterListPreprocessor]
 * keeps these rules away from the engine; they are collected here ([extract]), stored next to the engine cache
 * ([UboFilter], `others.txt`) and the ones for a page ([selectorsFor]) are applied by `assets/filter/ubo-cosmetic.js`.
 *
 * Domains: `a.com,~b.a.com` (subdomains included, `~` excludes), entity names `example.*`, and uBO's host+path form
 * (`igorslab.de/community/`). `#@#` exceptions with the same selector cancel a rule for their domains.
 */
object OthersRules {
    private const val SUFFIX = ":others()"

    class Rule(
        val include: List<String>,
        val exclude: List<String>,
        /** The selector list without `:others()`. */
        val selector: String,
        val exception: Boolean,
    )

    /** Whether [line] (trimmed) is a `:others()` cosmetic rule. */
    @JvmStatic
    fun isOthersRule(line: String): Boolean {
        if (line.startsWith("!") || !line.endsWith(SUFFIX)) return false
        return line.contains("##") || line.contains("#@#")
    }

    /** The `:others()` rule lines of [text], unchanged (for the cache file). */
    @JvmStatic
    fun extractLines(text: String): List<String> {
        if (!text.contains(SUFFIX)) return emptyList()
        return text.lineSequence().map { it.trim() }.filter { isOthersRule(it) }.toList()
    }

    /** [lines] parsed; lines that are no `:others()` rules or have an empty selector are skipped. */
    @JvmStatic
    fun parse(lines: List<String>): List<Rule> = lines.mapNotNull { parseLine(it) }

    @JvmStatic
    fun parseLine(line: String): Rule? {
        if (!isOthersRule(line)) return null
        val exception = line.contains("#@#") && line.indexOf("#@#") < (line.indexOf("##").takeIf { it >= 0 } ?: Int.MAX_VALUE)
        val sep = if (exception) "#@#" else "##"
        val at = line.indexOf(sep)
        val domains = line.substring(0, at)
        val selector = line.substring(at + sep.length, line.length - SUFFIX.length).trim()
        if (selector.isEmpty()) return null
        val include = ArrayList<String>()
        val exclude = ArrayList<String>()
        for (d in domains.split(',').map { it.trim().lowercase() }.filter { it.isNotEmpty() }) {
            if (d.startsWith("~")) exclude.add(d.substring(1)) else include.add(d)
        }
        return Rule(include, exclude, selector, exception)
    }

    /** Whether domain entry [d] covers [host] / [path] (path with leading "/"). */
    @JvmStatic
    fun matches(d: String, host: String, path: String): Boolean {
        val slash = d.indexOf('/')
        val dHost = if (slash >= 0) d.substring(0, slash) else d
        val dPath = if (slash >= 0) d.substring(slash) else ""
        val hostOk = if (dHost.endsWith(".*")) {
            // entity: example.* matches example.com, www.example.co.uk, ... (any suffix after the name)
            val base = dHost.dropLast(2)
            host.startsWith("$base.") || host.contains(".$base.")
        } else {
            host == dHost || host.endsWith(".$dHost")
        }
        return hostOk && (dPath.isEmpty() || path.startsWith(dPath))
    }

    /** The selector lists of the `:others()` rules for [url] (exceptions applied), or an empty list. */
    @JvmStatic
    fun selectorsFor(rules: List<Rule>, url: String): List<String> {
        if (rules.isEmpty()) return emptyList()
        val noScheme = url.substringAfter("://", url)
        val hostPort = noScheme.substringBefore('/').substringBefore('?').substringBefore('#')
        val host = hostPort.substringAfter('@').substringBefore(':').lowercase()
        if (host.isEmpty()) return emptyList()
        val path = "/" + noScheme.substringAfter('/', "").substringBefore('#')
        fun applies(r: Rule): Boolean {
            if (r.exclude.any { matches(it, host, path) }) return false
            return r.include.isEmpty() && r.exception || r.include.any { matches(it, host, path) }
        }
        val cancelled = rules.filter { it.exception && applies(it) }.map { it.selector }.toHashSet()
        return rules.filter { !it.exception && it.include.isNotEmpty() && applies(it) && it.selector !in cancelled }
            .map { it.selector }.distinct()
    }
}
