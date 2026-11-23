package com.mycompany.app.ui.lists

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.data.book.DataBookAds
import com.mycompany.app.data.book.DataBookJava
import com.mycompany.app.data.book.DataBookLink
import com.mycompany.app.data.book.DataBookOver
import com.mycompany.app.data.book.DataBookPop
import com.mycompany.app.data.book.DataBookTmem
import com.mycompany.app.data.book.DataBookTrans
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookAds
import com.mycompany.app.db.book.DbBookJava
import com.mycompany.app.db.book.DbBookLink
import com.mycompany.app.db.book.DbBookOver
import com.mycompany.app.db.book.DbBookPop
import com.mycompany.app.db.book.DbBookTmem
import com.mycompany.app.db.book.DbBookTrans
import com.mycompany.app.db.DbSite
import com.mycompany.app.main.MainUtil

/** One stored site path. */
data class SiteEntry(val id: Long, val path: String)

/**
 * Plain site-path lists kept by the browser (ads allow list, overlay allow list, popup allow list, blocked links,
 * JS blacklist, memory block list, translation exceptions). Each has a SQLite table and an in-memory host index
 * that the browser consults while loading pages, so every change updates both.
 * The codes are the list type codes of the site lists.
 */
class SiteListStore(private val context: Context, val code: Int) {

    companion object {
        val supported = intArrayOf(19, 20, 21, 22, 27, 28, 29)

        fun titleRes(code: Int): Int = when (code) {
            19 -> R.string.ads_white
            20 -> R.string.over_white
            21 -> R.string.pop_white
            22 -> R.string.blocked_link
            27 -> R.string.js_black
            28 -> R.string.mem_block_list
            else -> R.string.trans_except
        }
    }

    private val table: String = when (code) {
        19 -> "DbBookAds_table"
        20 -> "DbBookOver_table"
        21 -> "DbBookPop_table"
        22 -> "DbBookLink_table"
        27 -> "DbBookJava_table"
        28 -> "DbBookTmem_table"
        else -> "DbBookTrans_table"
    }

    private fun db(): SQLiteDatabase? = when (code) {
        19 -> DbBookAds.get(context).writableDatabase
        20 -> DbBookOver.get(context).writableDatabase
        21 -> DbBookPop.get(context).writableDatabase
        22 -> DbBookLink.get(context).writableDatabase
        27 -> DbBookJava.get(context).writableDatabase
        28 -> DbBookTmem.get(context).writableDatabase
        else -> DbBookTrans.get(context).writableDatabase
    }

    private fun cache(): ArrayList<String?>? = when (code) {
        19 -> DataBookAds.instance(context).sites
        20 -> DataBookOver.instance(context).paths
        21 -> DataBookPop.instance(context).sites
        22 -> DataBookLink.instance(context).links
        27 -> DataBookJava.instance(context).sites
        28 -> DataBookTmem.instance(context).sites
        else -> DataBookTrans.instance(context).sites
    }

    private fun dropItemCache() {
        when (code) {
            19 -> DataBookAds.instance(context).clear()
            20 -> DataBookOver.instance(context).clear()
            21 -> DataBookPop.instance(context).clear()
            22 -> DataBookLink.instance(context).clear()
            27 -> DataBookJava.instance(context).clear()
            28 -> DataBookTmem.instance(context).clear()
            else -> DataBookTrans.instance(context).clear()
        }
    }

    fun load(): List<SiteEntry> {
        val result = ArrayList<SiteEntry>()
        val cursor = DbUtil.query(db(), table, arrayOf("_id", "_path"), null, null, "_time DESC") ?: return result
        try {
            while (cursor.moveToNext()) {
                val path = cursor.getString(1) ?: continue
                result.add(SiteEntry(cursor.getLong(0), path))
            }
        } catch (_: Exception) {
        } finally {
            cursor.close()
        }
        return result
    }

    enum class AddResult { OK, EMPTY, EXISTS, UNSUPPORTED }

    fun add(input: String, existing: List<SiteEntry>): AddResult {
        val path = MainUtil.trimTrailingSlash(input.trim())
        if (path.isNullOrEmpty()) return AddResult.EMPTY
        if (code == 22 && (path == "file:///android_asset/shortcut.html" || path == "about:blank")) {
            return AddResult.UNSUPPORTED
        }
        if (existing.any { it.path.equals(path, ignoreCase = true) }) return AddResult.EXISTS
        when (code) {
            19 -> { DataBookAds.instance(context).addSite(path); DbBookAds.addPath(context, path) }
            20 -> {
                val book = DataBookOver.instance(context)
                val list = book.paths ?: ArrayList<String?>().also { book.paths = it }
                if (!list.contains(path)) list.add(path)
                DbSite.addPath(db(), table, path)
            }
            21 -> { DataBookPop.instance(context).addSite(path); DbBookPop.addPath(context, path) }
            22 -> { DataBookLink.instance(context).addLink(path); DbBookLink.addPath(context, path) }
            27 -> { DataBookJava.instance(context).addSite(path); DbBookJava.addPath(context, path) }
            28 -> { DataBookTmem.instance(context).addSite(path); DbBookTmem.addPath(context, path) }
            else -> { DataBookTrans.instance(context).addSite(path); DbBookTrans.addPath(context, path) }
        }
        dropItemCache()
        return AddResult.OK
    }

    /** Renames [entry]; returns false when [newPath] is empty or already used by another entry. */
    fun rename(entry: SiteEntry, newPath: String, existing: List<SiteEntry>): AddResult {
        val path = MainUtil.trimTrailingSlash(newPath.trim())
        if (path.isNullOrEmpty()) return AddResult.EMPTY
        if (path == entry.path) return AddResult.OK
        if (code == 22 && (path == "file:///android_asset/shortcut.html" || path == "about:blank")) {
            return AddResult.UNSUPPORTED
        }
        if (existing.any { it.id != entry.id && it.path.equals(path, ignoreCase = true) }) return AddResult.EXISTS
        val values = ContentValues()
        values.put("_path", path)
        DbUtil.updateById(db(), table, values, entry.id)
        cache()?.let { list ->
            val index = list.indexOf(entry.path)
            if (index >= 0) list[index] = path else list.add(path)
        }
        dropItemCache()
        return AddResult.OK
    }

    fun delete(entries: Collection<SiteEntry>) {
        val database = db()
        for (entry in entries) {
            cache()?.remove(entry.path)
            DbUtil.deleteById(database, table, entry.id)
        }
        dropItemCache()
    }
}
