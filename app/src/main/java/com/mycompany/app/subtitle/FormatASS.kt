package com.mycompany.app.subtitle

import com.mycompany.app.subtitle.Subtitle.nextTrimmed
import java.io.InputStream

/** Advanced SubStation Alpha (ASS) and SSA: the `[Events]` section's `Dialogue:` lines. */
internal object FormatASS {
    @JvmStatic
    fun parse(input: InputStream, charset: String, listener: Subtitle.SubtitleListener): CueLists? {
        val reader = Subtitle.open(input, charset) ?: return null
        val result = Subtitle.newResult()
        val starts = HashSet<Int>()
        try {
            var line: String? = reader.readLine()
            var timer = 100f
            while (line != null && !listener.isCancelled()) {
                val section = line.trim()
                when {
                    !section.startsWith("[") -> line = reader.readLine()
                    section.equals("[Script info]", ignoreCase = true) -> {
                        line = reader.nextTrimmed()
                        while (!line!!.startsWith("[")) {
                            if (!line.startsWith("Script Type:") && line.startsWith("Timer:")) {
                                timer = line.split(":")[1].trim().replace(',', '.').toFloat()
                            }
                            line = reader.nextTrimmed()
                        }
                    }
                    section.equals("[v4 Styles]", true) || section.equals("[v4 Styles+]", true) ||
                        section.equals("[v4+ Styles]", true) -> {
                        var format = reader.nextTrimmed()
                        while (!format.startsWith("Format:")) format = reader.nextTrimmed()
                        line = reader.nextTrimmed()
                        while (!line!!.startsWith("[")) line = reader.nextTrimmed()
                    }
                    section.equals("[Events]", true) -> {
                        var format = reader.nextTrimmed()
                        while (!format.startsWith("Format:")) format = reader.nextTrimmed()
                        val fields = format.split(":")[1].trim().split(",").toTypedArray()
                        var event = reader.nextTrimmed()
                        while (!event.startsWith("[")) {
                            if (event.startsWith("Dialogue:")) {
                                val values = event.split(":", limit = 2)[1].trim().split(",", limit = fields.size).toTypedArray()
                                caption(values, fields, timer)?.let {
                                    Subtitle.uniqueStart(it, starts)
                                    Subtitle.addCue(it, result)
                                }
                            }
                            event = reader.nextTrimmed()
                        }
                        line = event
                    }
                    else -> line = reader.nextTrimmed() // [Fonts], [Graphics] and unknown sections
                }
            }
        } catch (exception: Exception) {
        }
        Subtitle.close(reader)
        return result
    }

    /** One `Dialogue:` line; [fields] are the names from the section's `Format:` line. */
    private fun caption(values: Array<String>, fields: Array<String>, timer: Float): Caption? {
        val caption = Caption()
        for (index in fields.indices) {
            try {
                val field = fields[index].trim()
                when {
                    field.equals("Start", true) -> caption.start = Time.centis(values[index].trim())
                    field.equals("End", true) -> caption.end = Time.centis(values[index].trim())
                    field.equals("Text", true) -> caption.text = values[index].replace(Regex("\\{.*?\\}"), "")
                        .replace("\n", "<br />").replace("\\N", "<br />")
                }
            } catch (exception: Exception) {
                return null
            }
        }
        if (timer != 100f) {
            // "Timer:" is a speed percentage.
            val factor = timer / 100f
            caption.start.ms = (caption.start.ms / factor).toInt()
            caption.end.ms = (caption.end.ms / factor).toInt()
        }
        return caption
    }
}
