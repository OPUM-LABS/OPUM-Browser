package com.mycompany.app.subtitle

import java.io.BufferedReader
import java.io.Closeable
import java.io.EOFException
import java.io.InputStream
import java.io.InputStreamReader

/** One cue list per language (most formats have exactly one). */
internal typealias CueLists = ArrayList<ArrayList<SubtitleItem>>

/** Subtitle file parsing: the supported formats and the shared cue handling. */
object Subtitle {

    /** Supported file extensions; the index is the `format` argument of [parse]. */
    @JvmField
    val EXTENSIONS = arrayOf("ass", "psb", "scc", "smi", "srt", "ssa", "stl", "sub", "ttml", "vtt")

    /** [EXTENSIONS] with a leading dot. */
    @JvmField
    val DOT_EXTENSIONS = EXTENSIONS.map { ".$it" }.toTypedArray()

    /** Lets the caller cancel a running parse. */
    fun interface SubtitleListener {
        fun isCancelled(): Boolean
    }

    /**
     * Parses a subtitle file. [format] indexes [EXTENSIONS]. Returns the cue lists (one per language)
     * of [SubtitleItem]s, or null if the stream cannot be read.
     */
    @JvmStatic
    fun parse(format: Int, input: InputStream?, charset: String, listener: SubtitleListener): ArrayList<ArrayList<SubtitleItem>>? {
        if (input == null) return null
        return when (format) {
            1 -> FormatPSB.parse(input, charset, listener)
            2 -> FormatSCC.parse(input, charset, listener)
            3 -> FormatSMI.parse(input, charset, listener)
            4 -> FormatSRT.parse(input, charset, listener)
            6 -> FormatSTL.parse(input, listener)
            7 -> FormatSUB.parse(input, charset, listener)
            8 -> FormatTTML.parse(input, listener)
            9 -> FormatVTT.parse(input, charset, listener)
            else -> FormatASS.parse(input, charset, listener) // ASS / SSA
        }
    }

    internal fun newResult(): CueLists = arrayListOf(ArrayList())

    internal fun open(input: InputStream, charset: String): BufferedReader? =
        try {
            BufferedReader(InputStreamReader(input, charset))
        } catch (e: Exception) {
            null
        }

    internal fun close(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (exception: Exception) {
        }
    }

    /** Next line, trimmed; ends parsing (via the parsers' catch) at the end of the file. */
    internal fun BufferedReader.nextTrimmed(): String = readLine()?.trim() ?: throw EOFException()

    /** Makes the caption's start unique among [starts] (a later cue moves by 1 ms). */
    internal fun uniqueStart(caption: Caption, starts: MutableSet<Int>) {
        var start = caption.start.ms
        while (start in starts) start++
        caption.start.ms = start
        starts.add(start)
    }

    /** Adds the caption to the first cue list: its text at the start, and a clearing entry at the end. */
    internal fun addCue(caption: Caption, result: CueLists) {
        val cues = result.firstOrNull() ?: return
        if (caption.start.ms < caption.end.ms) {
            val last = cues.lastOrNull()
            if (last != null && last.start == caption.start.ms.toLong()) {
                last.text = caption.text
            } else {
                cues.add(SubtitleItem(caption.start.ms.toLong(), caption.text))
            }
        }
        cues.add(SubtitleItem(caption.end.ms.toLong(), null))
    }
}
