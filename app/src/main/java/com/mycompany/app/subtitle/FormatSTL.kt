package com.mycompany.app.subtitle

import java.io.InputStream

/** EBU STL (binary): a 1024-byte GSI header, then 128-byte TTI blocks. */
internal object FormatSTL {
    @JvmStatic
    fun parse(input: InputStream, listener: Subtitle.SubtitleListener): CueLists? {
        val result = Subtitle.newResult()
        val starts = HashSet<Int>()
        val header = ByteArray(1024)
        val block = ByteArray(128)
        try {
            if (input.read(header) < 1024) {
                Subtitle.close(input)
                return null
            }
            val fps = String(byteArrayOf(header[6], header[7])).toInt()
            val blocks = String(header, 238, 5).toInt()
            var caption = Caption()
            var continued = false
            var n = 0
            while (n < blocks && !listener.isCancelled() && input.read(block) >= 128) {
                n++
                if (!continued) caption = Caption()
                // Extension block number 0xFF marks the last block of a subtitle.
                continued = block[3] != (-1).toByte()
                if (block[15].toInt() != 0) continue // comment block
                if (!continued) {
                    caption.start = Time.frames("${block[5]}:${block[6]}:${block[7]}:${block[8]}/$fps")
                    caption.end = Time.frames("${block[9]}:${block[10]}:${block[11]}:${block[12]}/$fps")
                }
                addText(caption, block.copyOfRange(16, 128), starts, result, listener)
            }
        } catch (exception: Exception) {
        }
        Subtitle.close(input)
        return result
    }

    /**
     * Appends one block's text field: printable bytes are text, 0x8A is a line break and 0x8F ends the
     * subtitle (it is added then). Control codes may be doubled.
     */
    private fun addText(caption: Caption, bytes: ByteArray, starts: MutableSet<Int>, result: CueLists, listener: Subtitle.SubtitleListener) {
        var run: StringBuilder? = null
        var index = 0
        while (index < bytes.size && !listener.isCancelled()) {
            val code = bytes[index].toInt()
            when {
                code < 0 -> if (code <= -113) {
                    if (index + 1 < bytes.size && bytes[index] == bytes[index + 1]) index++
                    when (bytes[index].toInt()) {
                        -118 -> caption.text += (run ?: "").toString() + "<br />" // 0x8A line break
                        -113 -> {
                            // 0x8F unused space: the subtitle text ends.
                            caption.text += (run ?: "").toString()
                            Subtitle.uniqueStart(caption, starts)
                            Subtitle.addCue(caption, result)
                            index = bytes.size
                        }
                    }
                    run = null
                }
                code < 32 -> if (index + 1 < bytes.size && bytes[index] == bytes[index + 1]) index++ // teletext control code
                else -> (run ?: StringBuilder().also { run = it }).append(code.toChar())
            }
            index++
        }
    }
}
