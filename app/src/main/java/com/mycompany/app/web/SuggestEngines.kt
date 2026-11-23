package com.mycompany.app.web

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Search suggestions come from the chosen search engine (2026-11-23; before, only Google or DuckDuckGo could be picked):
 * the suggestion service of each built-in engine ([com.mycompany.app.main.MainConst.engineNames] index). Custom engines
 * have none. All answer in the OpenSearch form `["query", ["suggestion", ...], ["description", ...]]` except Naver.
 */
object SuggestEngines {
    /** True when the built-in engine [index] has a suggestion service. */
    fun hasService(index: Int): Boolean = index in 0..9

    /** The request for [query] to the suggestion service of engine [index] ([lang] e.g. "de-CH"), null when it has none. */
    fun url(index: Int, query: String, lang: String?): String? {
        val q = try { URLEncoder.encode(query, "UTF-8") } catch (_: Exception) { query }
        val hl = try { URLEncoder.encode(lang ?: "", "UTF-8") } catch (_: Exception) { "" }
        return when (index) {
            0 -> "https://suggestion.baidu.com/su?action=opensearch&ie=UTF-8&wd=$q"
            1 -> "https://api.bing.com/osjson.aspx?query=$q"
            2 -> "https://suggest.search.daum.net/sushi/opensearch/pc?q=$q"
            3 -> "https://duckduckgo.com/ac/?type=list&q=$q"
            // client=chrome also suggests web sites (shown with their address)
            4 -> "https://suggestqueries.google.com/complete/search?client=chrome&hl=$hl&q=$q"
            5 -> "https://ac.search.naver.com/nx/ac?q_enc=UTF-8&st=100&frm=nv&r_format=json&r_enc=UTF-8&r_unicode=0&t_koreng=1&q=$q"
            6 -> "https://www.startpage.com/osuggestions?q=$q"
            7 -> "https://search.yahoo.com/sugg/gossip/gossip-us-fastbreak?output=fxjson&command=$q"
            8 -> "https://suggest.yandex.com/suggest-ff.cgi?part=$q"
            9 -> "https://suggestqueries.google.com/complete/search?client=firefox&ds=yt&hl=$hl&q=$q"
            else -> null
        }
    }

    /** One suggestion: a search term, or a web site ([url] set, [title] its description or host). */
    data class Suggestion(val title: String, val url: String?)

    /** The suggestions in a service's answer (at most [MAX]); the typed [query] itself and repeats are left out. */
    fun parse(response: String?, query: String?): List<Suggestion> {
        val text = response?.trim() ?: return emptyList()
        val raw = ArrayList<Pair<String, String?>>()
        try {
            if (text.startsWith("[")) {
                val root = JSONArray(text)
                val terms = root.optJSONArray(1) ?: return emptyList()
                val notes = root.optJSONArray(2)
                for (i in 0 until terms.length()) raw.add(terms.optString(i) to notes?.optString(i))
            } else if (text.startsWith("{")) {
                // Naver: {"items": [[["term"], ["term"], ...]]}
                val items = JSONObject(text).optJSONArray("items")?.optJSONArray(0) ?: return emptyList()
                for (i in 0 until items.length()) raw.add((items.optJSONArray(i)?.optString(0) ?: "") to null)
            }
        } catch (_: Exception) {
            return emptyList()
        }
        val typed = query?.trim()?.lowercase()
        val seen = HashSet<String>()
        val out = ArrayList<Suggestion>()
        for ((term, note) in raw) {
            val value = term.trim()
            if (value.isEmpty() || value.lowercase() == typed || !seen.add(value.lowercase())) continue
            if (value.startsWith("https://") || value.startsWith("http://")) {
                val host = try { java.net.URI(value).host } catch (_: Exception) { null }
                out.add(Suggestion(note?.trim()?.takeIf { it.isNotEmpty() } ?: host ?: value, value))
            } else {
                out.add(Suggestion(value, null))
            }
            if (out.size >= MAX) break
        }
        return out
    }

    const val MAX = 15
}
