package com.mycompany.app.subtitle

import java.io.InputStream

/** PowerDivX PSB: one cue per line, `{h:m:s}{h:m:s}text` with `|` as line break. */
internal object FormatPSB {
    @JvmStatic
    fun parse(input: InputStream, charset: String, listener: Subtitle.SubtitleListener): CueLists? =
        LineFormat.parse(input, charset, listener, ::caption)

    @JvmStatic
    fun caption(line: String): Caption? {
        if (line.isEmpty() || line[0] != '{') return null
        // Fixed width form: {hh:mm:ss}{hh:mm:ss}text
        if (line.length >= 18 && line[8] == '}' && line[9] == '{' && line[17] == '}') {
            return Caption(Time.hms(line.substring(1, 8)), Time.hms(line.substring(10, 17)),
                if (line.length > 18) line.substring(18).replace("|", "<br />") else "")
        }
        return LineFormat.braces(line, Time::hms)
    }
}

/** MicroDVD SUB: one cue per line, `{startFrame}{endFrame}text` with `|` as line break. */
internal object FormatSUB {
    @JvmStatic
    fun parse(input: InputStream, charset: String, listener: Subtitle.SubtitleListener): CueLists? =
        LineFormat.parse(input, charset, listener) { line ->
            if (line.isEmpty() || line[0] != '{') null else LineFormat.braces(line, Time::subFrame)
        }
}

/** Shared parsing for the one-cue-per-line formats (PSB, SUB). */
internal object LineFormat {
    @JvmStatic
    fun parse(
        input: InputStream,
        charset: String,
        listener: Subtitle.SubtitleListener,
        caption: (String) -> Caption?,
    ): CueLists? {
        val reader = Subtitle.open(input, charset) ?: return null
        val result = Subtitle.newResult()
        val starts = HashSet<Int>()
        try {
            var line = reader.readLine()?.replace("﻿", "")
            while (line != null && !listener.isCancelled()) {
                val values = line.trim()
                if (values.isNotEmpty()) {
                    try {
                        caption(values)?.let {
                            Subtitle.uniqueStart(it, starts)
                            Subtitle.addCue(it, result)
                        }
                    } catch (exception: Exception) {
                    }
                }
                line = reader.readLine()
            }
        } catch (exception: Exception) {
        }
        Subtitle.close(reader)
        return result
    }

    /** `{start}{end}text`, both times read with [time]. */
    @JvmStatic
    fun braces(line: String, time: (String) -> Time): Caption? {
        val close = line.indexOf('}')
        if (close == -1 || line.length <= close + 2 || line[close + 1] != '{') return null
        val rest = line.substring(close + 1)
        val closeBrace = rest.indexOf('}')
        if (closeBrace == -1) return null
        return Caption(time(line.substring(1, close)), time(rest.substring(1, closeBrace)),
            if (rest.length > closeBrace + 1) rest.substring(closeBrace + 1).replace("|", "<br />") else "")
    }
}
