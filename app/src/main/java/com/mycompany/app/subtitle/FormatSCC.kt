package com.mycompany.app.subtitle

import java.io.InputStream

/** Scenarist SCC: CEA-608 broadcast captions (channel 1) as hex words per timecode. */
internal object FormatSCC {
    @JvmStatic
    fun parse(input: InputStream, charset: String, listener: Subtitle.SubtitleListener): CueLists? {
        val reader = Subtitle.open(input, charset) ?: return null
        val result = Subtitle.newResult()
        val starts = HashSet<Int>()
        try {
            val header = reader.readLine()
            if (header == null || !header.trim().equals("Scenarist_SCC V1.0", ignoreCase = true)) {
                Subtitle.close(reader)
                return null
            }
            var buffered = true // text goes to the off-screen buffer (pop-on captions)
            var channel = false
            var buffer = ""
            var current: Caption? = null
            var started = false // whether `current` has a start time (the original kept it null otherwise)
            while (!listener.isCancelled()) {
                val line = (reader.readLine() ?: break).trim()
                if (line.isEmpty()) continue
                val parts = line.split("\t")
                val time = Time.frames(parts[0] + "/29.97")
                val words = parts[1].split(" ")
                var k = 0
                while (k < words.size) {
                    try {
                        val word = words[k].toInt(16)
                        if (word and 0x6000 != 0) {
                            // Two characters.
                            if (channel) {
                                val chars = char(word and 0x7F00 ushr 8) + char(word and 0x7F)
                                if (buffered) buffer += chars else current?.let { it.text += chars }
                            }
                            continue
                        }
                        if (word and 0x7F7F == 0) {
                            // Padding: one frame passes.
                            time.ms = (time.ms + 33.366700033366705).toInt()
                            continue
                        }
                        // Control codes are sent twice; skip the repeat.
                        if (k + 1 < words.size && words[k] == words[k + 1]) k++
                        when {
                            word and 0x0800 != 0 -> channel = false
                            word and 0x1670 == 0x1420 -> {
                                if (word and 0x0100 != 0) {
                                    channel = false
                                    continue
                                }
                                when (word and 0xF) {
                                    0 -> {
                                        // Resume caption loading.
                                        buffer = ""
                                        channel = true
                                        buffered = true
                                        continue
                                    }
                                    9 -> {
                                        // Resume direct captioning.
                                        current = Caption(start = time)
                                        started = true
                                    }
                                    12 -> current?.let {
                                        // Erase displayed memory: the current caption ends here.
                                        it.end = time
                                        if (started) {
                                            Subtitle.uniqueStart(it, starts)
                                            Subtitle.addCue(it, result)
                                            current = Caption()
                                            started = false
                                        }
                                    }
                                    5, 6, 7 -> {
                                        // Roll-up captions.
                                        current?.let {
                                            it.end = time
                                            if (!started) continue // (the original failed on the missing start here)
                                            starts.add(it.start.ms)
                                            Subtitle.addCue(it, result)
                                        }
                                        current = Caption(start = time)
                                        started = true
                                        buffer = ""
                                    }
                                    14 -> buffer = ""
                                    15 -> {
                                        // End of caption: show the buffer.
                                        current = Caption(start = time, text = buffer)
                                        started = true
                                    }
                                }
                                channel = true
                                buffered = false
                            }
                            channel && word and 0x1040 == 0x1040 -> {
                                // Preamble address code: new row.
                                if (buffered && buffer.isNotEmpty()) buffer += "<br />"
                                if (!buffered) current?.let { if (it.text.isNotEmpty()) it.text += "<br />" }
                            }
                            channel && word and 0x1770 == 0x1130 -> {
                                // Special characters.
                                val special = when (word and 0xF) {
                                    7 -> "♪"
                                    9 -> " "
                                    else -> "�"
                                }
                                if (buffered) buffer += special else current?.let { it.text += special }
                            }
                        }
                    } catch (exception: Exception) {
                    } finally {
                        k++
                    }
                }
            }
            current?.takeIf { started }?.let {
                it.end = Time.frames("99:59:59:29/29.97")
                Subtitle.uniqueStart(it, starts)
                Subtitle.addCue(it, result)
            }
        } catch (exception: Exception) {
        }
        Subtitle.close(reader)
        return result
    }

    /** CEA-608 basic character set (ASCII with a few substitutions). */
    private fun char(code: Int): String = when (code) {
        0 -> ""
        42 -> "�"
        92 -> "é"
        94 -> "í"
        95 -> "ó"
        96 -> "ú"
        123 -> "ç"
        124 -> "�"
        125 -> "Ñ"
        126 -> "ñ"
        127 -> "|"
        else -> code.toChar().toString()
    }
}
