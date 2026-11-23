package com.mycompany.app.data.transfer

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.mycompany.app.db.book.DbBookPass
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.SyncSettings
import java.nio.charset.StandardCharsets

/** How an import went: entries added, entries already there (skipped) and entries that could not be used. */
data class TransferSummary(val imported: Int, val skipped: Int, val failed: Int)

/**
 * Moving bookmarks and saved passwords to and from other browsers (Settings > Backup & restore > Other browsers).
 *
 * Bookmarks use the Netscape bookmark HTML file ([BookmarkHtml]); passwords use the browsers' CSV files
 * ([PasswordCsv]): Chrome and Firefox are read, Chrome's format is written. Both work on the current mode's data
 * (normal or secret mode), as the rest of the app does. Imports keep folders, skip what is already saved (a bookmark
 * with the same address; a login with the same site and user name, whose stored password is left alone) and add the
 * rest at the end of the lists. Nothing here logs entries or passwords.
 */
object BrowserTransfer {

    /** Bigger files are not bookmark or password exports; reading them would only risk running out of memory. */
    private const val MAX_FILE_BYTES = 32L * 1024 * 1024

    private val BOOKMARK_SCHEMES = listOf("http://", "https://", "ftp://", "file://", "javascript:")

    private fun secretArg() = if (SyncSettings.secretMode.value) "1" else "0"

    /** UTF-8 text of [uri], or null when it cannot be read or is too big. */
    fun readText(context: Context, uri: Uri): String? = try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val bytes = input.readBytes(MAX_FILE_BYTES)
            bytes?.toString(StandardCharsets.UTF_8)
        }
    } catch (_: Exception) {
        null
    }

    fun writeText(context: Context, uri: Uri, text: String): Boolean = try {
        context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
            out.write(text.toByteArray(StandardCharsets.UTF_8))
            out.flush()
            true
        } ?: false
    } catch (_: Exception) {
        false
    }

    private fun java.io.InputStream.readBytes(limit: Long): ByteArray? {
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(64 * 1024)
        var total = 0L
        while (true) {
            val n = read(buf)
            if (n < 0) break
            total += n
            if (total > limit) return null
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }

    // ---- bookmarks ----

    /** True when [text] looks like a Netscape bookmark file (Chrome, Firefox, Edge, Brave, Safari export). */
    fun isBookmarkFile(text: String): Boolean {
        val head = text.take(4096).lowercase()
        return head.contains("netscape-bookmark-file") || (head.contains("<dl") && head.contains("<dt"))
    }

    fun importBookmarks(context: Context, html: String): TransferSummary {
        val nodes = BookmarkHtml.parse(html)
        val db = DbBookWeb.get(context).writableDatabase
        val secret = secretArg()
        val savedUrls = HashSet<String>()
        val savedFolders = HashSet<String>()
        db.query("DbBookWeb_table", arrayOf("_isdir", "_dir", "_path", "_title"), "_secret=?", arrayOf(secret), null, null, null).use { c ->
            while (c.moveToNext()) {
                if (c.getInt(0) == 1) savedFolders.add(folderPath(c.getString(1), c.getString(3) ?: continue))
                else c.getString(2)?.let { savedUrls.add(it) }
            }
        }
        var imported = 0
        var skipped = 0
        var failed = 0
        var clock = System.currentTimeMillis()
        fun insert(values: ContentValues) {
            val t = clock++
            values.put("_time", t)
            values.put("_rsv4", t)
            values.put("_secret", secret.toInt())
            db.insert("DbBookWeb_table", null, values)
        }
        fun addAll(dir: String, list: List<BookmarkNode>) {
            for (node in list) {
                if (node.isFolder) {
                    val name = node.title.replace('/', '-').trim()
                    if (name.isEmpty()) {
                        addAll(dir, node.children)
                        continue
                    }
                    val path = folderPath(dir, name)
                    if (savedFolders.add(path)) {
                        insert(ContentValues().apply {
                            put("_isdir", 1)
                            put("_dir", dir)
                            put("_title", name)
                        })
                    }
                    addAll(path, node.children)
                } else {
                    val url = node.url!!.trim()
                    if (BOOKMARK_SCHEMES.none { url.startsWith(it, ignoreCase = true) } || url.length <= 8) {
                        failed++
                    } else if (!savedUrls.add(url)) {
                        skipped++
                    } else {
                        insert(ContentValues().apply {
                            put("_isdir", 0)
                            put("_dir", dir)
                            put("_path", url)
                            put("_title", node.title.ifBlank { url })
                        })
                        imported++
                    }
                }
            }
        }
        db.beginTransaction()
        try {
            addAll("/", nodes)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return TransferSummary(imported, skipped, failed)
    }

    /** Path of folder [name] inside [dir] as stored in `_dir` of its entries ("/" root, "/A", "/A/B"). */
    private fun folderPath(dir: String?, name: String): String {
        val parent = MainUtil.trimSlashes(dir)
        return if (TextUtils.isEmpty(parent)) "/$name" else "/$parent/$name"
    }

    /** The current mode's bookmarks as a tree (folders first, then bookmarks, each in the app's order). */
    fun bookmarkTree(context: Context): List<BookmarkNode> {
        class Row(val isDir: Boolean, val dir: String, val url: String?, val title: String, val time: Long, val order: Long)
        val rows = ArrayList<Row>()
        DbBookWeb.get(context).writableDatabase.query(
            "DbBookWeb_table", arrayOf("_isdir", "_dir", "_path", "_title", "_time", "_rsv4"),
            "_secret=?", arrayOf(secretArg()), null, null, null,
        ).use { c ->
            while (c.moveToNext()) {
                val isDir = c.getInt(0) == 1
                val dir = MainUtil.trimSlashes(c.getString(1)).let { if (TextUtils.isEmpty(it)) "/" else "/$it" }
                val url = c.getString(2)
                val title = c.getString(3) ?: ""
                if (isDir && title.isEmpty()) continue
                if (!isDir && url.isNullOrEmpty()) continue
                rows.add(Row(isDir, dir, url, title, c.getLong(4), c.getLong(5)))
            }
        }
        val byDir = rows.groupBy { it.dir }
        val visited = HashSet<String>()
        fun build(dir: String): List<BookmarkNode> {
            if (!visited.add(dir)) return emptyList()
            val list = (byDir[dir] ?: return emptyList())
                .sortedWith(compareBy<Row>({ !it.isDir }, { it.order }, { it.time }))
            return list.map { r ->
                if (r.isDir) BookmarkNode(r.title, null, r.time / 1000, build(folderPath(dir, r.title)))
                else BookmarkNode(r.title.ifBlank { r.url!! }, r.url, r.time / 1000)
            }
        }
        return build("/")
    }

    fun bookmarkCount(context: Context): Int = try {
        android.database.DatabaseUtils.queryNumEntries(
            DbBookWeb.get(context).writableDatabase, "DbBookWeb_table", "_secret=? AND _isdir=0", arrayOf(secretArg()),
        ).toInt()
    } catch (_: Exception) {
        0
    }

    fun exportBookmarks(context: Context): String = BookmarkHtml.write(bookmarkTree(context))

    // ---- passwords ----

    /** Imports [csv]; null when it is not a password CSV (no url/password columns). */
    fun importPasswords(context: Context, csv: String): TransferSummary? {
        val parsed = PasswordCsv.parse(csv)
        if (!parsed.headerFound) return null
        val db = DbBookPass.get(context).writableDatabase
        val secret = secretArg()
        val saved = HashSet<String>()
        db.query("DbBookPass_table", arrayOf("_path", "_user_val"), "_secret=?", arrayOf(secret), null, null, null).use { c ->
            while (c.moveToNext()) saved.add((c.getString(0) ?: "") + "\u0000" + (c.getString(1) ?: ""))
        }
        var imported = 0
        var skipped = 0
        var failed = parsed.failed
        db.beginTransaction()
        try {
            for (e in parsed.entries) {
                val url = if (e.url.contains("://")) e.url else "https://" + e.url
                val host = if (url.startsWith("http://", true) || url.startsWith("https://", true)) MainUtil.hostOfUrl(url, true)?.lowercase() else null
                // the browser fills logins by user name; entries without one cannot be used here
                if (host.isNullOrEmpty() || e.username.isEmpty()) {
                    failed++
                    continue
                }
                if (!saved.add(host + "\u0000" + e.username)) {
                    skipped++
                    continue
                }
                val values = ContentValues()
                values.put("_secret", secret.toInt())
                values.put("_path", host)
                values.put("_user_val", e.username)
                values.put("_pass_val", e.password)
                values.put("_rsv1", url)
                if (db.insert("DbBookPass_table", null, values) > 0) imported++ else failed++
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return TransferSummary(imported, skipped, failed)
    }

    fun passwordEntries(context: Context): List<PasswordEntry> {
        val list = ArrayList<PasswordEntry>()
        DbBookPass.get(context).writableDatabase.query(
            "DbBookPass_table", arrayOf("_path", "_user_val", "_pass_val", "_rsv1"),
            "_secret=?", arrayOf(secretArg()), null, null, "_path ASC",
        ).use { c ->
            while (c.moveToNext()) {
                val host = c.getString(0) ?: continue
                val user = c.getString(1) ?: ""
                val pass = c.getString(2) ?: continue
                val url = c.getString(3).let { if (it.isNullOrEmpty()) "https://$host/" else it }
                list.add(PasswordEntry(url, user, pass, host, ""))
            }
        }
        return list
    }

    fun passwordCount(context: Context): Int = try {
        android.database.DatabaseUtils.queryNumEntries(
            DbBookPass.get(context).writableDatabase, "DbBookPass_table", "_secret=?", arrayOf(secretArg()),
        ).toInt()
    } catch (_: Exception) {
        0
    }

    fun exportPasswords(context: Context): String = PasswordCsv.write(passwordEntries(context))

    // ---- whole steps, for the screens (call off the main thread) ----

    /** Imports the bookmark HTML or password CSV at [uri]; null when the file cannot be read or is not that kind. */
    fun importFromUri(context: Context, uri: Uri, passwords: Boolean): TransferSummary? {
        val text = readText(context, uri) ?: return null
        return try {
            when {
                passwords -> importPasswords(context, text)
                isBookmarkFile(text) -> importBookmarks(context, text)
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    /** Writes the bookmark HTML or password CSV to [uri]; the number of entries written, or -1 when it failed. */
    fun exportToUri(context: Context, uri: Uri, passwords: Boolean): Int = try {
        val text: String
        val count: Int
        if (passwords) {
            val entries = passwordEntries(context)
            text = PasswordCsv.write(entries)
            count = entries.size
        } else {
            text = exportBookmarks(context)
            count = bookmarkCount(context)
        }
        if (writeText(context, uri, text)) count else -1
    } catch (_: Exception) {
        -1
    }

    /** What a shared file holds: bookmarks or passwords, and how many usable entries. */
    data class FileKind(val passwords: Boolean, val entries: Int)

    /** Bookmark HTML or password CSV? null when [text] is neither (or holds nothing usable). */
    fun detect(text: String): FileKind? {
        if (isBookmarkFile(text)) {
            fun count(list: List<BookmarkNode>): Int = list.sumOf { if (it.isFolder) count(it.children) else 1 }
            return FileKind(false, count(BookmarkHtml.parse(text)))
        }
        val csv = PasswordCsv.parse(text)
        return if (csv.headerFound) FileKind(true, csv.entries.size) else null
    }

    /** File-picker MIME types offered when picking a file to import. */
    val BOOKMARK_MIME_TYPES = arrayOf("text/html", "application/xhtml+xml", "application/octet-stream", "*/*")
    val PASSWORD_MIME_TYPES = arrayOf("text/csv", "text/comma-separated-values", "text/plain", "application/octet-stream", "*/*")

    /** Suggested name of an export file, e.g. `opum_bookmarks_20261120_1807.html`. */
    fun exportFileName(passwords: Boolean): String {
        val stamp = java.text.SimpleDateFormat("yyyyMMdd_HHmm", java.util.Locale.US).format(java.util.Date())
        return if (passwords) "opum_passwords_$stamp.csv" else "opum_bookmarks_$stamp.html"
    }
}
