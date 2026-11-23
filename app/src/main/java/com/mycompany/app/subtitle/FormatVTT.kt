package com.mycompany.app.subtitle

import com.mycompany.app.subtitle.Subtitle.nextTrimmed
import java.io.InputStream

/** WebVTT: a `WEBVTT` header, then blocks of an optional cue number, `start --> end` and text lines. */
internal object FormatVTT {
    @JvmStatic
    fun parse(input: InputStream, charset: String, listener: Subtitle.SubtitleListener): CueLists? {
        val reader = Subtitle.open(input, charset) ?: return null
        val result = Subtitle.newResult()
        val starts = HashSet<Int>()
        try {
            var header = true
            var line = reader.readLine()?.replace("﻿", "")
            while (line != null && !listener.isCancelled()) {
                var lineText = line.trim()
                if (lineText.isNotEmpty()) {
                    if (header) {
                        if (lineText.contains("WEBVTT")) header = false
                        // The original skips the line after the one checked here as well.
                        line = reader.readLine()
                    } else {
                        if (lineText.toIntOrNull() != null) lineText = reader.nextTrimmed()
                        val caption = try {
                            Caption(Time.millis(lineText.substring(0, 12), '.'), Time.millis(lineText.substring(lineText.length - 12), '.'))
                        } catch (exception: Exception) {
                            null
                        }
                        if (caption != null) {
                            lineText = reader.nextTrimmed()
                            val body = StringBuilder()
                            while (lineText.isNotEmpty()) {
                                if (body.isNotEmpty()) body.append("<br />")
                                body.append(lineText)
                                lineText = reader.nextTrimmed()
                            }
                            caption.text = body.toString()
                            Subtitle.uniqueStart(caption, starts)
                            Subtitle.addCue(caption, result)
                        }
                        while (lineText.isNotEmpty()) lineText = reader.nextTrimmed()
                    }
                }
                line = reader.readLine()
            }
        } catch (exception: Exception) {
        }
        Subtitle.close(reader)
        return result
    }
}
