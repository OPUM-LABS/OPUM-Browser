package com.mycompany.app.subtitle

import java.io.InputStream
import java.util.Locale

/** Microsoft SAMI (SMI): `<SYNC Start=ms>` blocks, one cue list per `<P Class=...>` language. */
internal object FormatSMI {
    @JvmStatic
    fun parse(input: InputStream, charset: String, listener: Subtitle.SubtitleListener): CueLists? {
        val reader = Subtitle.open(input, charset) ?: return null
        val result = Subtitle.newResult()
        val languages = ArrayList<String>()
        val stringBuilder = StringBuilder()
        var start = -1L
        var lang = 0
        try {
            while (!listener.isCancelled()) {
                val line = reader.readLine() ?: break
                val lower = line.lowercase(Locale.US)
                if (lower.contains("<sync")) {
                    if (start != -1L) addCue(result, lang, start, stringBuilder.toString())
                    val sync = sync(line, lower)
                    if (sync != null) {
                        val (index, rest) = language(sync.text, sync.lower, languages)
                        lang = index
                        stringBuilder.setLength(0)
                        stringBuilder.append(rest)
                        start = sync.start
                    }
                } else if (start != -1L) {
                    stringBuilder.append(line)
                }
            }
            if (start != -1L) addCue(result, lang, start, stringBuilder.toString())
        } catch (exception: Exception) {
        }
        Subtitle.close(reader)
        return result
    }

    /** A `<SYNC Start=...>` tag: its time and the line without the tag (as is and lower case). */
    private class Sync(val start: Long, val text: String, val lower: String)

    private fun sync(line: String, lower: String): Sync? {
        val at = lower.indexOf("<sync start=")
        if (at == -1) return null
        val from = at + 12
        val close = line.substring(from).indexOf('>')
        if (close == -1) return null
        val start = line.substring(from, from + close).toLongOrNull() ?: 0L
        val after = from + close + 1
        val rest = line.substring(0, at) + (if (after < line.length) line.substring(after) else "")
        val lowerText = lower.substring(0, at) + (if (after < line.length) lower.substring(after) else "")
        return Sync(start, rest, lowerText)
    }

    /**
     * Removes a `<P Class=name>` tag and returns the language index of `name` (new names are added to
     * [languages]) with the remaining text.
     */
    private fun language(tag: String, lower: String, languages: MutableList<String>): Pair<Int, String> {
        val at = lower.indexOf("<p class=")
        if (at == -1) return 0 to tag
        val from = at + 9
        val close = tag.substring(from).indexOf('>')
        if (close == -1) return 0 to tag
        val name = tag.substring(from, from + close)
        val after = from + close + 1
        val rest = tag.substring(0, at) + (if (after < tag.length) tag.substring(after) else "")
        if (name.isEmpty()) return 0 to rest
        val key = name.lowercase(Locale.US)
        if (languages.isEmpty()) {
            languages.add(key)
            return 0 to rest
        }
        val index = languages.indexOf(key)
        if (index != -1) return index to rest
        languages.add(key)
        return languages.size - 1 to rest
    }

    /** Adds a cue to language list [lang], keeping the list ordered by start time. */
    private fun addCue(result: CueLists, lang: Int, start: Long, bodyText: String) {
        val body = if (bodyText == "&nbsp;") "" else bodyText
        while (result.size <= lang) result.add(ArrayList())
        val cues = result[lang]
        val cue = SubtitleItem(start, fontColors(body))
        val previous = cues.lastOrNull()
        when {
            previous == null -> cues.add(cue)
            start < previous.start -> {
                cues[cues.size - 1] = cue
                cues.add(previous)
            }
            else -> cues.add(cue)
        }
    }

    /** Turns `<font color=name>` / `color="rrggbb"` into `color="#rrggbb"` so the text view can show it. */
    private fun fontColors(body: String): String {
        if (body.isEmpty() || !body.contains("<font color")) return body
        val parts = body.split("<font color")
        val out = StringBuilder()
        for (part in parts) {
            val eq = part.indexOf('=')
            val close = part.indexOf('>')
            if (eq == -1 || close == -1 || eq > close) {
                out.append(part)
                continue
            }
            val value = part.substring(eq + 1, close)
            out.append("<font color")
            if (value.startsWith("\"") && value.endsWith("\"")) {
                out.append(if (value.startsWith("\"#")) part else part.replace(value, "\"#" + value.substring(1)))
            } else {
                val name = value.replace(Regex("[^a-zA-Z]"), "")
                val hex = SubtitleColor.hex(name.lowercase(Locale.US))
                out.append(if (hex != null) part.replace(name, hex) else part)
            }
        }
        return out.toString()
    }
}
