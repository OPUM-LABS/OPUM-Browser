package com.mycompany.app.subtitle

/** A subtitle timestamp in milliseconds. */
internal class Time(var ms: Int = 0) {

    /** `hh:mm:ss.cc` (centiseconds), the form ASS/SSA files use. */
    override fun toString(): String {
        fun two(value: Int) = value.toString().padStart(2, '0')
        return "${two(ms / 3600000)}:${two(ms / 60000 % 60)}:${two(ms / 1000 % 60)}.${two(ms / 10 % 100)}"
    }

    companion object {
        /** PSB: `h:m:s`. */
        @JvmStatic
        fun hms(valueText: String): Time {
            val p = valueText.split(":")
            return Time(if (p.size == 3) p[0].toInt() * 3600000 + p[1].toInt() * 60000 + p[2].toInt() * 1000 else 0)
        }

        /** ASS/SSA: `h:mm:ss.cc` (centiseconds). */
        @JvmStatic
        fun centis(valueText: String): Time {
            val dot = valueText.lastIndexOf('.')
            if (dot == -1 || dot + 1 >= valueText.length) return Time()
            val p = valueText.substring(0, dot).split(":")
            if (p.size != 3) return Time()
            return Time(p[0].toInt() * 3600000 + p[1].toInt() * 60000 + p[2].toInt() * 1000 + valueText.substring(dot + 1).toInt() * 10)
        }

        /** SRT (`hh:mm:ss,mmm`) and VTT (`hh:mm:ss.mmm`); [separator] is the millisecond separator. */
        @JvmStatic
        fun millis(valueText: String, separator: Char): Time {
            val sep = valueText.lastIndexOf(separator)
            if (sep == -1 || sep + 1 >= valueText.length) return Time()
            val p = valueText.substring(0, sep).split(":")
            if (p.size != 3) return Time()
            return Time(p[0].toInt() * 3600000 + p[1].toInt() * 60000 + p[2].toInt() * 1000 + valueText.substring(sep + 1).toInt())
        }

        /** SCC/STL: `h:m:s:frame/fps`. */
        @JvmStatic
        fun frames(current: String): Time {
            val slash = current.lastIndexOf('/')
            if (slash == -1 || slash + 1 >= current.length) return Time()
            val p = current.substring(0, slash).split(":")
            if (p.size != 4) return Time()
            val frame = (p[3].toInt() * 1000 / current.substring(slash + 1).toFloat()).toInt()
            return Time(p[0].toInt() * 3600000 + p[1].toInt() * 60000 + p[2].toInt() * 1000 + frame)
        }

        /** MicroDVD SUB: a frame number at 25 fps. */
        @JvmStatic
        fun subFrame(text: String) = Time(text.toInt() * 40)
    }
}
