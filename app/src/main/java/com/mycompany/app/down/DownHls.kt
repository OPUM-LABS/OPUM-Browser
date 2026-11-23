package com.mycompany.app.down

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer

/**
 * HLS helpers (2026-11-20).
 *
 * Byte ranges: playlists with `#EXT-X-BYTERANGE` cut many segments out of one file. The segment list is plain URL
 * strings everywhere (parser, service, saver, resume), so a segment's range travels as a suffix on its URL
 * ([withRange]); the saver strips it ([split]) and asks the server for exactly those bytes. Before, such segments were
 * fetched whole (the same file many times) or dropped as duplicates.
 *
 * Remux: HLS video is MPEG-TS; the joined segments used to be saved as a ".mp4" that was really a TS stream (many
 * players and galleries cannot seek in it or refuse it). [remuxTsToMp4] rewrites it as a real MP4 with Android's
 * MediaExtractor / MediaMuxer (no re-encoding); when that fails the TS is kept as before.
 */
object DownHls {
    private const val MARK = "#opumrange="

    /** [url] with the byte range [start] … [start] + [length] − 1. */
    @JvmStatic
    fun withRange(url: String, start: Long, length: Long): String = "$url$MARK$start-$length"

    /** (url without the range, start, length); start / length are -1 when the segment has no range. */
    @JvmStatic
    fun split(url: String?): Triple<String?, Long, Long> {
        if (url == null) return Triple(null, -1, -1)
        val at = url.lastIndexOf(MARK)
        if (at < 0) return Triple(url, -1, -1)
        val parts = url.substring(at + MARK.length).split('-')
        val start = parts.getOrNull(0)?.toLongOrNull() ?: -1
        val length = parts.getOrNull(1)?.toLongOrNull() ?: -1
        return Triple(url.substring(0, at), start, length)
    }

    /** The segment's byte length when it has a range, else -1 (the caller asks the server). */
    @JvmStatic
    fun rangeLength(url: String?): Long = split(url).third

    /**
     * `#EXT-X-BYTERANGE:<n>[@<o>]` (or a MAP's BYTERANGE="n@o"): (length, offset); offset -1 = right after the
     * previous range of the same file.
     */
    @JvmStatic
    fun parseByteRange(value: String?): Pair<Long, Long>? {
        val v = value?.trim()?.trim('"') ?: return null
        val n = v.substringBefore('@').trim().toLongOrNull() ?: return null
        val o = if (v.contains('@')) v.substringAfter('@').trim().toLongOrNull() ?: -1 else -1
        return n to o
    }

    /** True when [path] starts like an MPEG-TS stream (sync byte 0x47 every 188 bytes). */
    @JvmStatic
    fun isMpegTs(path: String?): Boolean {
        if (path == null) return false
        return try {
            FileInputStream(path).use { input ->
                val buf = ByteArray(188 * 3 + 1)
                val n = input.read(buf)
                n >= 188 * 2 + 1 && buf[0] == 0x47.toByte() && buf[188] == 0x47.toByte() && buf[376] == 0x47.toByte()
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Copies every video / audio track of the TS file [src] into a new MP4 [out] (no re-encoding). [onBytes] gets the
     * source bytes passed (for the progress bar). False (and [out] deleted) when Android cannot read a track or write
     * it to MP4: the caller keeps the TS.
     */
    @JvmStatic
    fun remuxTsToMp4(src: String, out: String, onBytes: (Long) -> Unit = {}): Boolean {
        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null
        var started = false
        try {
            extractor = MediaExtractor()
            extractor.setDataSource(src)
            muxer = MediaMuxer(out, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val map = HashMap<Int, Int>()
            var maxInput = 1024 * 1024
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (!mime.startsWith("video/") && !mime.startsWith("audio/")) continue
                map[i] = muxer.addTrack(format)
                extractor.selectTrack(i)
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                    maxInput = maxOf(maxInput, format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE))
                }
            }
            if (map.isEmpty() || map.size != map.values.toSet().size) return fail(out, muxer, started, extractor)
            muxer.start()
            started = true
            val buffer = ByteBuffer.allocate(maxInput)
            val info = MediaCodec.BufferInfo()
            val lastPts = HashMap<Int, Long>()
            val total = File(src).length()
            var reported = 0L
            var samples = 0L
            while (true) {
                val track = extractor.sampleTrackIndex
                if (track < 0) break
                val size = extractor.readSampleData(buffer, 0)
                if (size < 0) break
                val target = map[track]
                if (target != null) {
                    var pts = extractor.sampleTime
                    // timestamps must not go back within a track (discontinuities): keep them increasing
                    val last = lastPts[track]
                    if (last != null && pts <= last) pts = last + 1
                    lastPts[track] = pts
                    info.set(0, size, pts, if ((extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0)
                    muxer.writeSampleData(target, buffer, info)
                    samples++
                }
                extractor.advance()
                // progress: the extractor's file position is not exposed; spread the source size over the samples
                if (samples % 64 == 0L && total > 0) {
                    val now = minOf(total, reported + total / 200)
                    onBytes(now - reported)
                    reported = now
                }
            }
            if (samples == 0L) return fail(out, muxer, started, extractor)
            muxer.stop()
            muxer.release()
            muxer = null
            extractor.release()
            onBytes(total - reported)
            return File(out).length() > 0
        } catch (_: Throwable) {
            return fail(out, muxer, started, extractor)
        }
    }

    private fun fail(out: String, muxer: MediaMuxer?, started: Boolean, extractor: MediaExtractor?): Boolean {
        try { if (started) muxer?.stop() } catch (_: Throwable) { }
        try { muxer?.release() } catch (_: Throwable) { }
        try { extractor?.release() } catch (_: Throwable) { }
        File(out).delete()
        return false
    }
}
