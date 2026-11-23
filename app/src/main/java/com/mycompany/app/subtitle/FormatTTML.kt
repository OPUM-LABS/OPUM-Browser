package com.mycompany.app.subtitle

import org.w3c.dom.Document
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/** W3C TTML: `<p begin end|dur>` elements with text and `<br/>`. */
internal object FormatTTML {
    @JvmStatic
    fun parse(input: InputStream, listener: Subtitle.SubtitleListener): CueLists? {
        val result = Subtitle.newResult()
        val starts = HashSet<Int>()
        try {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input)
            doc.documentElement.normalize()
            val paragraphs = doc.getElementsByTagName("p")
            var k = 0
            while (k < paragraphs.length && !listener.isCancelled()) {
                val p = paragraphs.item(k++)
                val attrs = p.attributes
                val caption = Caption()
                var valid = true
                attrs.getNamedItem("begin")?.let { caption.start.ms = time(it.nodeValue, doc) }
                val end = attrs.getNamedItem("end")
                val dur = attrs.getNamedItem("dur")
                when {
                    end != null -> caption.end.ms = time(end.nodeValue, doc)
                    dur != null -> caption.end.ms = caption.start.ms + time(dur.nodeValue, doc)
                    else -> valid = false
                }
                val children = p.childNodes
                for (type in 0 until children.length) {
                    val child = children.item(type)
                    when (child.nodeName) {
                        "#text" -> caption.text += child.textContent.trim()
                        "br" -> caption.text += "<br />"
                    }
                }
                if (caption.text.replace("<br />", "").trim().isEmpty()) valid = false
                if (valid) {
                    Subtitle.uniqueStart(caption, starts)
                    Subtitle.addCue(caption, result)
                }
            }
        } catch (exception: Exception) {
        }
        Subtitle.close(input)
        return result
    }

    /**
     * A TTML time expression in ms: `h:m:s(.fff)`, `h:m:s:frames` (at `ttp:frameRate`, default 25) or an
     * offset with a unit (h, m, s, ms, f, t).
     */
    private fun time(value: String, doc: Document): Int {
        if (value.contains(':')) {
            val p = value.split(":")
            if (p.size == 3) return p[0].toInt() * 3600000 + p[1].toInt() * 60000 + (p[2].toFloat() * 1000f).toInt()
            if (p.size != 4) return 0
            val fps = doc.getElementsByTagName("ttp:frameRate").item(0)?.nodeValue?.toIntOrNull() ?: 25
            return p[0].toInt() * 3600000 + p[1].toInt() * 60000 + p[2].toInt() * 1000 + (p[3].toFloat() * 1000f / fps).toInt()
        }
        val unit = value.substring(value.length - 1)
        return try {
            var seconds = value.substring(0, value.length - 1).replace(',', '.').trim().toDouble()
            when {
                unit.equals("h", true) -> seconds *= 3600000.0
                unit.equals("m", true) -> seconds *= 60000.0
                unit.equals("s", true) -> seconds *= 1000.0
                unit.equals("f", true) -> {
                    val rate = doc.getElementsByTagName("ttp:frameRate").item(0)?.nodeValue?.toIntOrNull() ?: return 0
                    seconds = seconds * 1000.0 / rate
                }
                unit.equals("t", true) -> {
                    val rate = doc.getElementsByTagName("ttp:tickRate").item(0)?.nodeValue?.toIntOrNull() ?: return 0
                    seconds = seconds * 1000.0 / rate
                }
                else -> return 0 // unknown unit ("ms" ends in "s", so it is read as seconds, as before)
            }
            seconds.toInt()
        } catch (numberFormatException: NumberFormatException) {
            0
        }
    }
}
