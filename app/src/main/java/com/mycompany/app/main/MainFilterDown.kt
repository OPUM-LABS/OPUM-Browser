package com.mycompany.app.main

import com.mycompany.app.main.MainUtil.LoopCancelListener
import java.util.ArrayList
import android.content.Context
import android.text.TextUtils
import com.mycompany.app.dialog.DialogSetFilter
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets
import kotlin.math.max

@Suppress("deprecation")
class MainFilterDown(loopCancelListener: LoopCancelListener?) {
    @JvmField
    var outStream: OutputStream? = null
    @JvmField
    var cancelListener: LoopCancelListener?

    fun cancelRequested(): Boolean {
        val loopCancelListener: LoopCancelListener? = this.cancelListener
        if (loopCancelListener == null) {
            return false
        }
        return (loopCancelListener as MainFilterSvc.FilterDownCancel).isCancelled
    }

    /** Optional hooks for a caller that shows progress and can cancel (the update dialog).  */
    interface Hooks {
        val isCancelled: Boolean

        /** A download started; the caller keeps the streams so a cancel can close them.  */
        fun onStreams(
            connection: HttpURLConnection?,
            `in`: InputStream?,
            out: OutputStream?,
            total: Long
        )

        fun onProgress(done: Long, total: Long)

        /** Downloads end; `keepOutput` while included parts are appended to one file.  */
        fun onStreamsDone(keepOutput: Boolean)
    }

    private var hooks: Hooks? = null

    init {
        this.cancelListener = loopCancelListener
    }

    private fun cancelled(): Boolean {
        return if (this.hooks != null) this.hooks!!.isCancelled else cancelRequested()
    }

    /** Downloads filter list `str` and installs it as the local copy of list `str2`.  */
    fun download(context: Context?, url: String?, key: String?): Boolean {
        return downloadWith(context, url, key, null)
    }

    /**
     * Downloads `url` to a temp file and moves it to the local file of list `key`.
     * List-KR lists ([DialogSetFilter.adguardIdOf] gives their base path) are
     * assembled first: "!#if" blocks not meant for Android are dropped and every "!#include" part
     * is downloaded and appended.
     */
    fun downloadWith(context: Context?, url: String?, key: String?, hooks: Hooks?): Boolean {
        this.hooks = hooks
        if (cancelled() || context == null) {
            return false
        }
        val connection = MainUtil.openBrowserConnection(context, url)
        if (connection == null) {
            return false
        }
        val temp = MainUtil.newCacheFilePath(context)
        if (TextUtils.isEmpty(temp)) {
            return false
        }
        MainUtil.deleteUri(context, temp)
        var ok = fetch(connection, temp, false)
        if (hooks != null) {
            hooks.onStreamsDone(false)
        }
        if (ok) {
            val dest = MainUtil.filterFilePath(context, key)
            val base = DialogSetFilter.adguardIdOf(key)
            if (TextUtils.isEmpty(base)) {
                ok = assembleGeneric(context, temp, dest, url) || MainUtil.copyFileOrUri(context, temp, dest)
            } else {
                ok = !cancelled() && assemble(
                    context,
                    temp,
                    dest,
                    base!!,
                    key!!.startsWith("https://cdn.jsdelivr.net/gh/")
                )
            }
        }
        MainUtil.deleteUri(context, temp)
        return ok
    }

    /** Streams `connection` into `path` (or into the shared output [.a] when appending).  */
    private fun fetch(connection: HttpURLConnection?, path: String?, append: Boolean): Boolean {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        var ok: Boolean
        try {
            connection!!.setDoInput(true)
            connection.connect()
            val total = max(0L, connection.getContentLengthLong())
            `in` = connection.getInputStream()
            if (append) {
                if (this.outStream == null) {
                    this.outStream = MainUtil.openFileOutput(path, false)
                }
                out = this.outStream
            } else {
                out = MainUtil.openFileOutput(path, false)
            }
            if (this.hooks != null) {
                this.hooks!!.onStreams(connection, `in`, out, total)
            }
            val buffer = ByteArray(1024)
            var done: Long = 0
            var read: Int = 0
            while (!cancelled() && (`in`.read(buffer, 0, 1024)
                    .also { read = it }) != -1 && !cancelled()
            ) {
                out!!.write(buffer, 0, read)
                done += read.toLong()
                if (this.hooks != null) {
                    this.hooks!!.onProgress(done, total)
                }
            }
            ok = true
        } catch (unused: Exception) {
            ok = false
        }
        if (!append && out != null) {
            try {
                out.close()
            } catch (unused: Exception) {
            }
        }
        if (`in` != null) {
            try {
                `in`.close()
            } catch (unused: Exception) {
            }
        }
        connection!!.disconnect()
        return ok
    }

    /**
     * Any other list with uBlock Origin pre-parsing directives (e.g. uBlock filters: `!#include filters-2025.txt`,
     * `!#if env_mobile`): writes the evaluated list with every allowed include (same folder as the list) downloaded
     * and appended in place ([com.mycompany.app.web.filter.FilterListPreprocessor]). False when the list has no
     * directives or something failed (the caller then copies the list as downloaded).
     */
    private fun assembleGeneric(context: Context?, temp: String?, dest: String?, url: String?): Boolean {
        if (cancelled() || context == null || TextUtils.isEmpty(temp) || TextUtils.isEmpty(dest)) return false
        val text = try {
            MainUtil.openFileInput(temp)?.use { it.readBytes().toString(StandardCharsets.UTF_8) }
        } catch (_: Exception) {
            null
        } ?: return false
        if (!text.contains("!#include ") && !text.contains("!#if")) return false
        val assembled = com.mycompany.app.web.filter.FilterListPreprocessor.assemble(text, url, { partUrl ->
            if (cancelled()) null else readText(MainUtil.openBrowserConnection(context, partUrl))
        })
        if (cancelled()) return false
        return try {
            MainUtil.openFileOutput(dest, false)?.use { it.write(assembled.toByteArray(StandardCharsets.UTF_8)) }
            true
        } catch (_: Exception) {
            false
        }
    }

    /** Body of a small included part (no progress hooks), or null. */
    private fun readText(connection: HttpURLConnection?): String? {
        if (connection == null) return null
        return try {
            connection.setDoInput(true)
            connection.connect()
            if (connection.responseCode !in 200..299) null
            else connection.inputStream.use { it.readBytes().toString(StandardCharsets.UTF_8) }
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    /** Builds a List-KR list: own rules plus all "!#include" parts.  */
    private fun assemble(
        context: Context?,
        temp: String?,
        dest: String?,
        base: String?,
        cdn: Boolean
    ): Boolean {
        val lines = readListKr(temp)
        if (lines == null || lines.isEmpty()) {
            return MainUtil.copyFileOrUri(context, temp, dest)
        }
        val own = temp + "kr1"
        val parts = temp + "kr2"
        MainUtil.deleteUri(context, own)
        MainUtil.deleteUri(context, parts)
        var wroteOwn = writeOwnRules(own, lines)
        var fetched = false
        if (this.hooks != null) {
            this.hooks!!.onStreamsDone(true)
        }
        for (line2 in lines) {
            val line = line2 as String?
            if (cancelled() || TextUtils.isEmpty(line) || !line!!.startsWith("!#include ")) {
                continue
            }
            val part = line.substring(10).trim { it <= ' ' }
            if (part.isEmpty()) {
                continue
            }
            val partUrl =
                (if (cdn) "https://cdn.jsdelivr.net/gh/List-KR/List-KR@" else "https://raw.githubusercontent.com/List-KR/List-KR/") + base + part
            val connection = MainUtil.openBrowserConnection(context, partUrl)
            if (connection != null && fetch(connection, parts, true)) {
                fetched = true
            }
        }
        if (this.outStream != null) {
            try {
                this.outStream!!.close()
            } catch (unused: Exception) {
            }
            this.outStream = null
        }
        if (this.hooks != null) {
            this.hooks!!.onStreamsDone(false)
        }
        val ok: Boolean
        if (fetched) {
            if (wroteOwn) {
                wroteOwn = MainUtil.copyUriToFile(context, own, parts)
            }
            ok = if (wroteOwn) MainUtil.copyFileOrUri(context, own, dest) else MainUtil.copyFileOrUri(context, parts, dest)
        } else {
            ok = MainUtil.copyFileOrUri(context, temp, dest)
        }
        MainUtil.deleteUri(context, own)
        MainUtil.deleteUri(context, parts)
        return ok
    }

    /** Trimmed lines outside non-Android "!#if" blocks, or null when the list has no includes.  */
    private fun readListKr(path: String?): ArrayList<Any?>? {
        if (cancelled() || TextUtils.isEmpty(path)) {
            return null
        }
        var lines: ArrayList<Any?>? = null
        var hasInclude = false
        var `in`: InputStream? = null
        var reader: BufferedReader? = null
        try {
            `in` = MainUtil.openFileInput(path)
            reader = BufferedReader(InputStreamReader(`in`, StandardCharsets.UTF_8))
            var skipping = false
            var line: String?
            while ((reader.readLine().also { line = it }) != null && !cancelled()) {
                val trimmed = line!!.trim { it <= ' ' }
                if (trimmed.isEmpty()) {
                    continue
                }
                if (trimmed.startsWith("!#if")) {
                    skipping = !trimmed.contains("android")
                } else if (trimmed.startsWith("!#endif")) {
                    skipping = false
                } else if (!skipping) {
                    if (trimmed.startsWith("!#include ")) {
                        hasInclude = true
                    }
                    if (lines == null) {
                        lines = ArrayList<Any?>()
                    }
                    lines.add(trimmed)
                }
            }
        } catch (unused: Exception) {
        }
        try {
            if (reader != null) {
                reader.close()
            }
            if (`in` != null) {
                `in`.close()
            }
        } catch (unused: Exception) {
        }
        return if (hasInclude) lines else null
    }

    private fun writeOwnRules(path: String?, lines: ArrayList<Any?>?): Boolean {
        if (cancelled()) {
            return false
        }
        var wrote = false
        var out: OutputStream? = null
        var writer: BufferedWriter? = null
        try {
            out = MainUtil.openFileOutput(path, false)
            writer = BufferedWriter(OutputStreamWriter(out, StandardCharsets.UTF_8))
            for (line2 in lines!!) {
                val line = line2 as String?
                if (cancelled()) {
                    break
                }
                if (!TextUtils.isEmpty(line) && !line!!.startsWith("!#include ")) {
                    writer.write(line)
                    writer.write("\n")
                    wrote = true
                }
            }
        } catch (unused: Exception) {
            wrote = false
        }
        try {
            if (writer != null) {
                writer.close()
            }
            if (out != null) {
                out.close()
            }
        } catch (unused: Exception) {
        }
        return wrote
    }
}
