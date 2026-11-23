package com.mycompany.app.web.filter

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Kotlin side of the native web filter engine (`libopumadblock.so`, source in `native/adblock-jni/`): Brave's
 * adblock-rust (MPL-2.0), which reads uBlock Origin / Adblock Plus filter syntax.
 *
 * One instance owns one native engine. All methods are thread-safe (the WebView calls `shouldInterceptRequest` from
 * several threads); [close] waits for running calls and frees the native memory, later calls answer "allow".
 * Not used by the browser yet (migration step 2 wires it in behind the `WebClean` seam).
 */
class UboEngine private constructor(handle: Long) : AutoCloseable {
    private val lock = ReentrantReadWriteLock()
    private var handle: Long = handle

    /** Network verdict for one request. */
    sealed class Verdict {
        data object Allow : Verdict()
        data object Block : Verdict()
        /** Blocked; serve this `data:` URL's content instead (`$redirect`). */
        data class Redirect(val dataUrl: String) : Verdict()
        /** Allowed, but load this URL instead (`$removeparam`). */
        data class Rewrite(val url: String) : Verdict()

        val blocks: Boolean get() = this is Block || this is Redirect
    }

    /** Cosmetic filtering for one page. */
    data class Cosmetic(
        /** CSS selectors to hide (`display:none !important`). */
        val hideSelectors: List<String>,
        /** Procedural filters / filters with an action, one JSON object each (`:has-text()`, `:remove()`, ...). */
        val proceduralActions: List<String>,
        /** Class / id selectors that generic rules must not hide; pass to [classIdSelectors]. */
        val exceptions: List<String>,
        /** Scriptlet JavaScript to run at document start in the page's main world ("" when none). */
        val script: String,
        /** `$generichide`: do not ask [classIdSelectors] for this page. */
        val genericHide: Boolean,
    )

    /** Rules of one list the engine can use, the ones it rejects, and some rejected lines with the reason. */
    data class ListStats(val rules: Int, val rejected: Int, val rejectedSamples: List<String>)

    /** List header comments. [expiresHours] is null when the list has no `! Expires:`. */
    data class ListMetadata(val title: String?, val homepage: String?, val expiresHours: Int?)

    val isClosed: Boolean get() = lock.read { handle == 0L }

    /**
     * Checks one request. [type] is uBO's request type name (`document`, `subdocument`, `script`, `stylesheet`,
     * `image`, `media`, `font`, `xmlhttprequest`, `websocket`, `ping`, `other`, ...). [sourceUrl] is the page URL.
     */
    fun check(url: String, sourceUrl: String, type: String, method: String = "GET"): Verdict = lock.read {
        if (handle == 0L) return Verdict.Allow
        val reply = try { nativeCheck(handle, url, sourceUrl, type, method) } catch (_: Throwable) { null }
        when {
            reply == null -> Verdict.Allow
            reply.isEmpty() -> Verdict.Block
            reply.startsWith("r:") -> Verdict.Redirect(reply.substring(2))
            reply.startsWith("u:") -> Verdict.Rewrite(reply.substring(2))
            else -> Verdict.Block
        }
    }

    /** Cosmetic resources for the page [url], or null when the engine is closed. */
    fun cosmetic(url: String): Cosmetic? = lock.read {
        if (handle == 0L) return null
        val parts = try { nativeCosmetic(handle, url) } catch (_: Throwable) { null }
        if (parts == null || parts.size < 5) return null
        Cosmetic(
            hideSelectors = split(parts[0], ','),
            proceduralActions = split(parts[1], '\n'),
            exceptions = split(parts[2], ','),
            script = parts[3] ?: "",
            genericHide = parts[4] == "1",
        )
    }

    /** Generic hiding selectors for classes / ids seen on a page (without `.` / `#`). */
    fun classIdSelectors(classes: Collection<String>, ids: Collection<String>, exceptions: Collection<String>): List<String> =
        lock.read {
            if (handle == 0L || (classes.isEmpty() && ids.isEmpty())) return emptyList()
            val selectors = try {
                nativeClassId(handle, classes.toTypedArray(), ids.toTypedArray(), exceptions.toTypedArray())
            } catch (_: Throwable) { null }
            selectors?.filterNotNull() ?: emptyList()
        }

    /**
     * Loads scriptlet / redirect resources: a JSON array of `{"name", "aliases", "kind": {"mime": ...}, "content"
     * (base64)}`. Returns how many were loaded, or -1 when the JSON is invalid.
     */
    fun useResources(json: String): Int = lock.write {
        if (handle == 0L) return -1
        try { nativeUseResources(handle, json).toInt() } catch (_: Throwable) { -1 }
    }

    /** The engine in a compact form for [deserialize] (fast start without parsing the lists again). */
    fun serialize(): ByteArray? = lock.read {
        if (handle == 0L) return null
        try { nativeSerialize(handle) } catch (_: Throwable) { null }
    }

    override fun close() {
        lock.write {
            val oldHandle = handle
            handle = 0L
            if (oldHandle != 0L) try { nativeFree(oldHandle) } catch (_: Throwable) {}
        }
    }

    companion object {
        @Volatile
        private var libState = 0 // 0 = not tried, 1 = loaded, -1 = failed

        /** Loads `libopumadblock`; false when it is missing (the caller keeps the old engine). */
        @JvmStatic
        fun loadLibrary(): Boolean {
            if (libState == 0) {
                synchronized(this) {
                    if (libState == 0) {
                        libState = try { System.loadLibrary("opumadblock"); 1 } catch (_: Throwable) { -1 }
                    }
                }
            }
            return libState == 1
        }

        /**
         * Builds an engine from whole filter list texts (EasyList, uBlock filters, the user's rules, ...).
         * `trusted[i]` lets list `i` use scriptlets uBO marks "requires trust" (only uBO's own lists).
         */
        @JvmStatic
        @JvmOverloads
        fun create(lists: List<String>, trusted: List<Boolean> = emptyList()): UboEngine? {
            if (!loadLibrary()) return null
            val flags = BooleanArray(lists.size) { trusted.getOrElse(it) { false } }
            val newHandle = try { nativeCreate(lists.toTypedArray(), flags) } catch (_: Throwable) { 0L }
            return if (newHandle == 0L) null else UboEngine(newHandle)
        }

        /** Restores an engine written by [serialize]; null when the data is from another engine version. */
        @JvmStatic
        fun deserialize(data: ByteArray): UboEngine? {
            if (!loadLibrary()) return null
            val newHandle = try { nativeDeserialize(data) } catch (_: Throwable) { 0L }
            return if (newHandle == 0L) null else UboEngine(newHandle)
        }

        /** null when [rule] is a valid filter (or a comment), else the parser's message. */
        @JvmStatic
        fun validate(rule: String): String? {
            if (!loadLibrary()) return null
            return try { nativeValidate(rule) } catch (_: Throwable) { null }
        }

        @JvmStatic
        fun listMetadata(text: String): ListMetadata? {
            if (!loadLibrary()) return null
            val fields = try { nativeListMetadata(text) } catch (_: Throwable) { null } ?: return null
            return ListMetadata(
                title = fields.getOrNull(0)?.takeIf { it.isNotEmpty() },
                homepage = fields.getOrNull(1)?.takeIf { it.isNotEmpty() },
                expiresHours = fields.getOrNull(2)?.toIntOrNull(),
            )
        }

        /** Counts the rules of a list text ([samples] rejected lines are kept); null when the library is missing. */
        @JvmStatic
        @JvmOverloads
        fun listStats(text: String, samples: Int = 20): ListStats? {
            if (!loadLibrary()) return null
            val fields = try { nativeListStats(text, samples) } catch (_: Throwable) { null } ?: return null
            if (fields.size < 2) return null
            return ListStats(fields[0]?.toIntOrNull() ?: 0, fields[1]?.toIntOrNull() ?: 0, fields.drop(2).filterNotNull())
        }

        /** Library build, e.g. "opumadblock 0.1.0 / adblock 0.13.6"; null when it is not loaded. */
        @JvmStatic
        fun version(): String? = if (loadLibrary()) try { nativeVersion() } catch (_: Throwable) { null } else null

        private fun split(s: String?, sep: Char): List<String> =
            if (s.isNullOrEmpty()) emptyList() else s.split(sep).filter { it.isNotEmpty() }

        @JvmStatic private external fun nativeCreate(lists: Array<String>, trusted: BooleanArray): Long
        @JvmStatic private external fun nativeDeserialize(data: ByteArray): Long
        @JvmStatic private external fun nativeSerialize(handle: Long): ByteArray?
        @JvmStatic private external fun nativeCheck(handle: Long, url: String, source: String, type: String, method: String): String?
        @JvmStatic private external fun nativeCosmetic(handle: Long, url: String): Array<String?>?
        @JvmStatic private external fun nativeClassId(handle: Long, classes: Array<String>, ids: Array<String>, exceptions: Array<String>): Array<String?>?
        @JvmStatic private external fun nativeUseResources(handle: Long, json: String): Long
        @JvmStatic private external fun nativeValidate(rule: String): String?
        @JvmStatic private external fun nativeListMetadata(text: String): Array<String?>?
        @JvmStatic private external fun nativeListStats(text: String, samples: Int): Array<String?>?
        @JvmStatic private external fun nativeFree(handle: Long)
        @JvmStatic private external fun nativeVersion(): String?
    }
}
