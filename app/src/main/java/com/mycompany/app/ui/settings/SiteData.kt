package com.mycompany.app.ui.settings

import android.content.Context
import android.database.Cursor
import android.webkit.WebStorage
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookPms
import com.mycompany.app.dialog.DialogPermission
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.lists.SiteEntry
import com.mycompany.app.ui.lists.SiteListStore

/**
 * What the browser keeps per site, for Site settings (2026-11-20): permission decisions (`DbBookPms`, keyed by the
 * page host as `MainUtil.hostOfUrl` gives it, "www." included), the ad / pop-up allow lists and the JavaScript blacklist ([SiteListStore] 19 / 21 / 27,
 * entries are hosts or page addresses) and the web storage WebView reports per origin. Database calls block: use them
 * off the main thread.
 */
object SiteData {
    /** Permission kinds in display order: `DbBookPms` bit to label. */
    val permissionKinds: List<Pair<Int, Int>> = listOf(
        DialogPermission.LOCATION to ch.opumlabs.opumbrowser.R.string.location,
        DialogPermission.CAMERA to ch.opumlabs.opumbrowser.R.string.camera,
        DialogPermission.MIC to ch.opumlabs.opumbrowser.R.string.perm_mic,
        DialogPermission.PROTECTED to ch.opumlabs.opumbrowser.R.string.perm_protected,
    )

    const val ADS = 19
    const val POPS = 21
    const val JS = 27

    /** A site's permission bits. */
    data class Perms(val allow: Int, val block: Int)

    /** All saved permission decisions, by host. */
    fun permissions(context: Context): Map<String, Perms> {
        val result = HashMap<String, Perms>()
        var cursor: Cursor? = null
        try {
            cursor = DbUtil.query(
                DbBookPms.get(context).writableDatabase, "DbBookPms_table", arrayOf("_path", "_allow", "_block"), null, null, null,
            )
            while (cursor != null && cursor.moveToNext()) {
                val host = cursor.getString(0) ?: continue
                val p = Perms(cursor.getInt(1), cursor.getInt(2))
                if ((p.allow or p.block) != 0) result[host] = p
            }
        } catch (_: Exception) {
        }
        cursor?.close()
        return result
    }

    /** Sets one permission of [host]: 0 ask (forget), 1 allowed, 2 blocked. Removes the row when nothing is left. */
    fun setPermission(context: Context, host: String, bit: Int, state: Int) {
        val db = DbBookPms.get(context).writableDatabase
        val old = permissions(context)[host] ?: Perms(0, 0)
        val allow = if (state == 1) old.allow or bit else old.allow and bit.inv()
        val block = if (state == 2) old.block or bit else old.block and bit.inv()
        if ((allow or block) == 0) {
            DbUtil.deleteWhere(db, "DbBookPms_table", "_path=?", arrayOf(host))
            return
        }
        val values = android.content.ContentValues()
        values.put("_path", host)
        values.put("_allow", allow)
        values.put("_block", block)
        values.put("_time", System.currentTimeMillis())
        if (DbUtil.existsWhere(db, "DbBookPms_table", null, "_path=?", arrayOf(host)) == 1) {
            DbUtil.updateWhere(db, "DbBookPms_table", values, "_path=?", arrayOf(host))
        } else {
            DbUtil.insert(db, "DbBookPms_table", values)
        }
    }

    /** The host an allow-list entry stands for (entries are hosts or page addresses). */
    fun entryHost(path: String): String =
        (if (path.contains("://")) MainUtil.hostOfUrl(path, true) else path)?.lowercase() ?: path

    /** Entries of list [code] (19 ads, 21 pop-ups, 27 JavaScript) by host. */
    fun listHosts(context: Context, code: Int): Map<String, List<SiteEntry>> =
        SiteListStore(context, code).load().groupBy { entryHost(it.path) }

    /** Puts [host] on list [code] ([on]) or removes every entry of it. */
    fun setListed(context: Context, code: Int, host: String, on: Boolean) {
        val store = SiteListStore(context, code)
        val existing = store.load()
        if (on) {
            if (existing.none { entryHost(it.path) == host }) store.add(host, existing)
        } else {
            store.delete(existing.filter { entryHost(it.path) == host })
        }
    }

    /** Storage WebView keeps per site (bytes, by host); [done] runs on the main thread. */
    fun storage(done: (Map<String, Long>) -> Unit) {
        try {
            com.mycompany.app.web.WebProfiles.webStorage()!!.getOrigins { origins ->
                val result = HashMap<String, Long>()
                for ((key, value) in (origins ?: emptyMap<Any?, Any?>())) {
                    val origin = (value as? WebStorage.Origin)?.origin ?: key?.toString() ?: continue
                    val host = MainUtil.hostOfUrl("$origin/", true)?.lowercase() ?: continue
                    result[host] = (result[host] ?: 0L) + ((value as? WebStorage.Origin)?.usage ?: 0L)
                }
                done(result)
            }
        } catch (_: Exception) {
            done(emptyMap())
        }
    }

    /** Deletes the cookies and web storage of [host] (both schemes, also the host with / without "www."). */
    fun clearData(host: String) {
        val manager = com.mycompany.app.web.WebProfiles.cookieManager() ?: return
        for (h in listOf(host, if (host.startsWith("www.")) host.removePrefix("www.") else "www.$host")) {
            for (scheme in listOf("https", "http")) {
                val url = "$scheme://$h/"
                val cookies = try { manager.getCookie(url) } catch (_: Exception) { null } ?: continue
                val domains = ArrayList<String>()
                var d = h
                while (d.count { it == '.' } >= 1) { domains.add(d); d = d.substringAfter('.') }
                for (part in cookies.split(';')) {
                    val name = part.substringBefore('=').trim()
                    if (name.isEmpty()) continue
                    manager.setCookie(url, "$name=; Max-Age=0; Path=/")
                    for (dom in domains) manager.setCookie(url, "$name=; Max-Age=0; Path=/; Domain=$dom")
                }
            }
            for (scheme in listOf("https", "http")) {
                try { com.mycompany.app.web.WebProfiles.webStorage()!!.deleteOrigin("$scheme://$h") } catch (_: Exception) {}
            }
        }
        manager.flush()
    }

    /** "Clear and reset" (call [clearData] on the main thread first): permissions and the site's entries on the ad / pop-up / JavaScript lists. */
    fun clearAndReset(context: Context, host: String) {
        DbUtil.deleteWhere(DbBookPms.get(context).writableDatabase, "DbBookPms_table", "_path=?", arrayOf(host))
        for (code in intArrayOf(ADS, POPS, JS)) setListed(context, code, host, false)
    }
}
