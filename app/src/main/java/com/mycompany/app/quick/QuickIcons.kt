package com.mycompany.app.quick

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import com.mycompany.app.db.book.DbBookIcon
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainUtil

/**
 * Favicons of the start page's shortcuts (tiles and folder cells).
 *
 * Order: the picture stored with the shortcut (`DbBookQuick._icon`), then the favicon the browser saved for the site
 * (`DbBookIcon`, by host without www. / m.), else none (the caller keeps the letter). A shortcut's stored colour is
 * only the colour of its letter; a 1-byte `_icon` means the user chose the letter in the editor ([letterOnly]).
 *
 * Before 2026-11-19 a shortcut with a colour never asked for an icon, the loader only read `_icon` (which nothing fills
 * after a visit), and the Compose grid's requests were all dropped by `MainListLoader.isStillBound`.
 */
object QuickIcons {
    /** Only [MainUtil.loadAlbumThumb] / database reads; at most two at a time. */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val loads = kotlinx.coroutines.Dispatchers.IO.limitedParallelism(2)

    /** True if a stored `_icon` blob marks "letter chosen by the user". */
    fun letterOnly(blob: ByteArray?): Boolean = blob != null && blob.size == 1

    fun cached(path: String?, secret: Boolean): Bitmap? =
        MainListLoader.cachedListIcon(path, secret)?.takeIf { MainUtil.isBitmapOk(it) }

    /** Blocking: call off the main thread. Caches what it finds under the list-icon key. */
    fun load(context: Context?, path: String?, secret: Boolean): Bitmap? {
        if (context == null || TextUtils.isEmpty(path)) return null
        cached(path, secret)?.let { return it }
        var bitmap: Bitmap? = try { MainUtil.loadAlbumThumb(32, 0L, context, path) } catch (_: Exception) { null }
        if (!MainUtil.isBitmapOk(bitmap)) {
            val host = MainUtil.shortHostOf(path)
            bitmap = if (TextUtils.isEmpty(host)) null else try { DbBookIcon.iconOf(context, host) } catch (_: Exception) { null }
        }
        if (!sharp(bitmap)) {
            // only the page's small favicon (or none, as for YouTube): ask the site itself for a large one
            fetchSharp(context, path!!, secret)?.let { bitmap = it }
        }
        if (!MainUtil.isBitmapOk(bitmap)) return null
        MainListLoader.cacheListIcon(path, bitmap, secret)
        return bitmap
    }

    /** Smallest side (px) an icon needs to look sharp on a tile; WebView favicons are mostly 16 – 32 px. */
    private const val SHARP_MIN = 64
    /** Large icons are stored at most this big (px). */
    private const val STORE_MAX = 192

    private fun sharp(bitmap: Bitmap?): Boolean = MainUtil.isBitmapOk(bitmap) && minOf(bitmap!!.width, bitmap.height) >= SHARP_MIN

    /** Sites asked for a large icon since the app started (once per site, found or not). */
    private val asked = java.util.Collections.synchronizedSet(HashSet<String>())

    /**
     * A large icon from the shortcut's own site (2026-11-20): the icons its start page declares (`apple-touch-icon`,
     * `icon` with `sizes`), largest first, then `/apple-touch-icon.png`. Stored in the shortcut (`_icon`), so the site
     * is asked once; no other server is contacted. Blocking: call off the main thread.
     */
    private fun fetchSharp(context: Context, path: String, secret: Boolean): Bitmap? {
        val start = try { java.net.URL(path) } catch (_: Exception) { return null }
        if (start.protocol != "https" && start.protocol != "http") return null
        val origin = "${start.protocol}://${start.host}"
        if (!asked.add(origin)) return null
        val found = siteIcon(context, origin, sharpOnly = true) ?: return null
        store(context, path, found, secret)
        return found
    }

    /**
     * The "refresh" button of the shortcut editors (2026-11-20): asks the site of [address] for its icon now (a missing
     * "https://" is added), the largest it declares; when it has no large one, its small favicon. Nothing is stored (the
     * editor saves it with the shortcut). Blocking: call off the main thread. Null when the site has none or is offline.
     */
    fun fromSite(context: Context?, address: String?): Bitmap? {
        if (context == null || address.isNullOrBlank()) return null
        val text = address.trim().let { if (it.contains("://")) it else "https://$it" }
        val start = try { java.net.URL(text) } catch (_: Exception) { return null }
        if ((start.protocol != "https" && start.protocol != "http") || start.host.isNullOrEmpty()) return null
        return siteIcon(context, "${start.protocol}://${start.host}", sharpOnly = false)
    }

    /**
     * The icon of the site at [origin]: the icons its start page declares, largest first, then `/apple-touch-icon.png`;
     * with [sharpOnly] false also small ones and `/favicon.ico` (the largest found). Scaled to at most [STORE_MAX].
     */
    private fun siteIcon(context: Context, origin: String, sharpOnly: Boolean): Bitmap? {
        val mobile = try { android.webkit.WebSettings.getDefaultUserAgent(context) } catch (_: Exception) { null }
        // some mobile pages (m.youtube.com) declare only a 16 px favicon; their desktop page has the large ones
        val desktop = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36"
        val tried = HashSet<String>()
        var found: Bitmap? = null
        // a small icon is kept until the desktop page was asked too (m.youtube.com only declares a small one)
        var small: Bitmap? = null
        for (agent in listOf(mobile, desktop)) {
            val candidates = ArrayList<Pair<Int, String>>()
            var base = "$origin/"
            try {
                val conn = open("$origin/", agent)
                try {
                    if (conn.responseCode == 200) {
                        base = conn.url.toString()
                        val html = conn.inputStream.use { readLimited(it, 512 * 1024, cut = true) }?.toString(Charsets.UTF_8) ?: ""
                        candidates.addAll(declaredIcons(html, base, if (sharpOnly) SHARP_MIN else 1))
                    }
                } finally { conn.disconnect() }
            } catch (_: Exception) {
            }
            val baseOrigin = try { java.net.URL(base).let { "${it.protocol}://${it.host}" } } catch (_: Exception) { origin }
            candidates.add(180 to "$baseOrigin/apple-touch-icon.png")
            if (!sharpOnly) candidates.add(16 to "$baseOrigin/favicon.ico")
            for ((_, url) in candidates.sortedByDescending { it.first }.distinctBy { it.second }.take(if (sharpOnly) 5 else 7)) {
                if (!tried.add(url)) continue
                val bitmap = try { download(url, agent) } catch (_: Exception) { null }
                if (sharp(bitmap)) { found = bitmap; break }
                if (!sharpOnly && MainUtil.isBitmapOk(bitmap) && (small == null || bitmap!!.width > small.width)) small = bitmap
            }
            if (found != null) break
        }
        var icon = found ?: small ?: return null
        val big = maxOf(icon.width, icon.height)
        if (big > STORE_MAX) {
            icon = Bitmap.createScaledBitmap(icon, icon.width * STORE_MAX / big, icon.height * STORE_MAX / big, true)
        }
        return icon
    }

    private val linkTag = Regex("<link\\b[^>]*>", RegexOption.IGNORE_CASE)

    private fun attr(tag: String, name: String): String? =
        Regex("\\b$name\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))", RegexOption.IGNORE_CASE).find(tag)
            ?.groupValues?.drop(1)?.firstOrNull { it.isNotEmpty() }

    /** `<link rel="…icon…" href="…" sizes="…">` of [html], as (size, absolute url); SVG and mask icons are skipped. */
    private fun declaredIcons(html: String, base: String, minSize: Int): List<Pair<Int, String>> {
        val out = ArrayList<Pair<Int, String>>()
        for (m in linkTag.findAll(html)) {
            val tag = m.value
            val rel = attr(tag, "rel")?.lowercase() ?: continue
            if (!rel.contains("icon") || rel.contains("mask")) continue
            val href = attr(tag, "href")?.replace("&amp;", "&") ?: continue
            if (href.startsWith("data:") || href.lowercase().substringBefore('?').endsWith(".svg")) continue
            val size = attr(tag, "sizes")?.lowercase()?.split(' ')?.mapNotNull { it.substringBefore('x').toIntOrNull() }?.maxOrNull()
                ?: if (rel.contains("apple")) 180 else 32
            if (size < minSize) continue
            val url = try { java.net.URL(java.net.URL(base), href).toString() } catch (_: Exception) { continue }
            out.add(size to url)
        }
        return out
    }

    private fun open(url: String, agent: String?): java.net.HttpURLConnection {
        val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        conn.instanceFollowRedirects = true
        if (agent != null) conn.setRequestProperty("User-Agent", agent)
        return conn
    }

    private fun download(url: String, agent: String?): Bitmap? {
        val conn = open(url, agent)
        try {
            if (conn.responseCode != 200) return null
            val bytes = conn.inputStream.use { readLimited(it, 1024 * 1024) } ?: return null
            return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } finally { conn.disconnect() }
    }

    /** At most [max] bytes of [input]; when it is longer, the first [max] bytes if [cut] (a page's head), else null. */
    private fun readLimited(input: java.io.InputStream, max: Int, cut: Boolean = false): ByteArray? {
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(16 * 1024)
        while (true) {
            val n = input.read(buf)
            if (n < 0) break
            out.write(buf, 0, n)
            if (out.size() > max) return if (cut) out.toByteArray() else null
        }
        return out.toByteArray()
    }

    /** Saves [bitmap] as the picture of the shortcuts with [path], unless the user chose the letter for one. */
    private fun store(context: Context, path: String, bitmap: Bitmap, secret: Boolean) {
        val bytes = try {
            java.io.ByteArrayOutputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); out.toByteArray() }
        } catch (_: Exception) { return }
        try {
            val db = com.mycompany.app.db.book.DbBookQuick.get(context).writableDatabase
            val values = android.content.ContentValues().apply { put("_icon", bytes) }
            com.mycompany.app.db.DbUtil.updateWhere(
                db, "DbBookQuick_table", values,
                "_secret=? AND _path=? AND (_icon IS NULL OR length(_icon)<>1)",
                arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), path),
            )
        } catch (_: Exception) {
        }
    }

    /**
     * A page's favicon arrived: shortcuts of the same site (host without www. / m.) that have no picture yet get it,
     * so they show it even where the shared favicon store has none (YouTube is kept out of that store). Shortcuts
     * whose letter the user chose are left alone. Blocking: call off the main thread.
     */
    fun rememberVisit(context: Context?, url: String?, bitmap: Bitmap?) {
        if (context == null || !MainUtil.isBitmapOk(bitmap)) return
        val host = MainUtil.shortHostOf(url)
        if (TextUtils.isEmpty(host)) return
        val secret = com.mycompany.app.pref.store.SyncSettings.secretMode.value
        val db = try { com.mycompany.app.db.book.DbBookQuick.get(context).writableDatabase } catch (_: Exception) { return }
        val hits = ArrayList<Pair<Long, String>>()
        var cursor: android.database.Cursor? = null
        try {
            cursor = com.mycompany.app.db.DbUtil.query(
                db, "DbBookQuick_table", arrayOf<String?>("_id", "_path"),
                "_secret=? AND (_rsv5 IS NULL OR _rsv5<>1) AND (_icon IS NULL OR length(_icon)=0)",
                arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey()), null,
            )
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val path = cursor.getString(1)
                    if (!TextUtils.isEmpty(path) && host == MainUtil.shortHostOf(path)) hits.add(cursor.getLong(0) to path!!)
                } while (cursor.moveToNext())
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
        if (hits.isEmpty()) return
        val bytes = try {
            java.io.ByteArrayOutputStream().use { out -> bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, out); out.toByteArray() }
        } catch (_: Exception) { return }
        for ((id, path) in hits) {
            val values = android.content.ContentValues().apply { put("_icon", bytes) }
            com.mycompany.app.db.DbUtil.updateWhere(db, "DbBookQuick_table", values, "_id=?", arrayOf<String?>(id.toString()))
            MainListLoader.cacheListIcon(path, bitmap, secret)
        }
    }
}
