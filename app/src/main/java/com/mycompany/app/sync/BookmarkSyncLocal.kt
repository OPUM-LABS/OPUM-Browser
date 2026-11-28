package com.mycompany.app.sync

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import androidx.core.database.sqlite.transaction
import com.mycompany.app.data.sync.LocalItem
import com.mycompany.app.data.sync.MergeResult
import com.mycompany.app.data.sync.SyncNode
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.main.MainUtil

/**
 * The phone's side of the bookmark sync (2026-11-28): reads a workspace's bookmark rows as [LocalItem]s and writes the
 * merged result back.
 *
 * To tell which side changed an item later, the bookmark database notes when each row last changed (`_rsv6`, by
 * triggers, so every place in the app that writes bookmarks is covered) and, per workspace, when a row was last
 * deleted and when anything last changed (`DbBookWeb_sync`). The sync's own writes are not counted: while it writes,
 * the row with `_secret` -1 in `DbBookWeb_sync` tells the triggers to stay quiet.
 */
object BookmarkSyncLocal {
    private const val TABLE = "DbBookWeb_table"
    private const val NOW = "CAST(ROUND((julianday('now') - 2440587.5) * 86400000) AS INTEGER)"
    private const val QUIET = "NOT EXISTS (SELECT 1 FROM DbBookWeb_sync WHERE _secret = -1)"

    /** Creates the change tracking (DbBookWeb.onOpen; tables and triggers that already exist stay). */
    @JvmStatic
    fun ensureTracking(db: SQLiteDatabase) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS DbBookWeb_sync (_secret INTEGER PRIMARY KEY, _deleted INTEGER DEFAULT 0, _changed INTEGER DEFAULT 0)")
            val touch = { secret: String -> "INSERT OR IGNORE INTO DbBookWeb_sync (_secret, _deleted, _changed) VALUES ($secret, 0, 0); " +
                "UPDATE DbBookWeb_sync SET _changed = $NOW WHERE _secret = $secret;" }
            db.execSQL("CREATE TRIGGER IF NOT EXISTS bsync_ins AFTER INSERT ON $TABLE WHEN $QUIET BEGIN " +
                "UPDATE $TABLE SET _rsv6 = $NOW WHERE _id = NEW._id; ${touch("NEW._secret")} END")
            db.execSQL("CREATE TRIGGER IF NOT EXISTS bsync_upd AFTER UPDATE OF _isdir, _dir, _path, _title, _rsv4, _secret ON $TABLE WHEN $QUIET BEGIN " +
                "UPDATE $TABLE SET _rsv6 = $NOW WHERE _id = NEW._id; ${touch("NEW._secret")} END")
            db.execSQL("CREATE TRIGGER IF NOT EXISTS bsync_del AFTER DELETE ON $TABLE WHEN $QUIET BEGIN " +
                "${touch("OLD._secret")} UPDATE DbBookWeb_sync SET _deleted = $NOW WHERE _secret = OLD._secret; END")
        } catch (_: Exception) {
        }
    }

    private fun db(context: Context): SQLiteDatabase = DbBookWeb.get(context).writableDatabase

    /** When workspace [number]'s bookmarks last changed and when one was last deleted (ms; 0 = never). */
    fun changeTimes(context: Context, number: Int): Pair<Long, Long> {
        return try {
            db(context).rawQuery("SELECT _changed, _deleted FROM DbBookWeb_sync WHERE _secret = ?", arrayOf(number.toString())).use { c ->
                if (c.moveToFirst()) c.getLong(0) to c.getLong(1) else 0L to 0L
            }
        } catch (_: Exception) {
            0L to 0L
        }
    }

    private class Row(val id: Long, val isDir: Boolean, val dir: String, val url: String?, val title: String, val order: Long, val time: Long, val mtime: Long)

    private fun norm(dir: String?): String = MainUtil.trimSlashes(dir).let { if (TextUtils.isEmpty(it)) "/" else "/$it" }

    private fun folderPath(dir: String, name: String): String = if (dir == "/") "/$name" else "$dir/$name"

    /**
     * Workspace [number]'s bookmarks, parents before children, each folder's children in the app's order (folders
     * first). Rows the app cannot show (a bookmark in a folder that does not exist, a folder without a name) are left
     * out, so the sync never touches them.
     */
    fun read(context: Context, number: Int): List<LocalItem> {
        val rows = ArrayList<Row>()
        db(context).query(TABLE, arrayOf("_id", "_isdir", "_dir", "_path", "_title", "_rsv4", "_time", "_rsv6"),
            "_secret=?", arrayOf(number.toString()), null, null, null).use { c ->
            while (c.moveToNext()) {
                val isDir = c.getInt(1) == 1
                val title = c.getString(4) ?: ""
                val url = c.getString(3)
                if (isDir && title.isEmpty()) continue
                if (!isDir && url.isNullOrEmpty()) continue
                rows.add(Row(c.getLong(0), isDir, norm(c.getString(2)), if (isDir) null else url, title, c.getLong(5), c.getLong(6), c.getLong(7)))
            }
        }
        val byDir = rows.groupBy { it.dir }
        val out = ArrayList<LocalItem>()
        val seen = HashSet<String>()
        fun walk(dir: String, parentId: Long) {
            if (!seen.add(dir)) return
            val list = byDir[dir] ?: return
            for (r in list.sortedWith(compareBy<Row>({ !it.isDir }, { it.order }, { it.time }, { it.id }))) {
                out.add(LocalItem(r.id, r.isDir, r.title, r.url, parentId, maxOf(r.mtime, 0L)))
                if (r.isDir) walk(folderPath(dir, r.title), r.id)
            }
        }
        walk("/", 0L)
        return out
    }

    /**
     * Writes [result] into workspace [number]'s bookmarks in one transaction, without counting it as a change of the
     * phone. Returns the row ids of the new rows (by their id in the file) and the change time afterwards.
     */
    fun apply(context: Context, number: Int, before: List<LocalItem>, result: MergeResult): Pair<Map<String, Long>, Long> {
        val db = db(context)
        val newIds = HashMap<String, Long>()
        val old = before.associateBy { it.localId }
        // the rows' current place, to write only what changed
        val oldDir = HashMap<Long, String>()
        val oldOrder = HashMap<Long, Long>()
        db.query(TABLE, arrayOf("_id", "_dir", "_rsv4"), "_secret=?", arrayOf(number.toString()), null, null, null).use { c ->
            while (c.moveToNext()) { oldDir[c.getLong(0)] = norm(c.getString(1)); oldOrder[c.getLong(0)] = c.getLong(2) }
        }
        val pathOf = HashMap<String, String>()
        pathOf[SyncNode.ROOT_ID] = "/"
        db.transaction {
            execSQL("INSERT OR REPLACE INTO DbBookWeb_sync (_secret, _deleted, _changed) VALUES (-1, 0, 0)")
            for (id in result.deletes) delete(TABLE, "_id=? AND _secret=?", arrayOf(id.toString(), number.toString()))
            val now = System.currentTimeMillis()
            for (r in result.rows) {
                val dir = pathOf[r.parentServerId] ?: "/"
                if (r.isFolder) pathOf[r.serverId] = folderPath(dir, r.localTitle)
                val order = (r.order + 1).toLong()
                if (r.localId > 0) {
                    val l = old[r.localId]
                    val values = ContentValues()
                    if (oldDir[r.localId] != dir) values.put("_dir", dir)
                    if (l == null || l.title != r.localTitle) values.put("_title", r.localTitle)
                    if (!r.isFolder && l?.url != r.url) values.put("_path", r.url)
                    if (oldOrder[r.localId] != order) values.put("_rsv4", order)
                    if (values.size() > 0) update(TABLE, values, "_id=?", arrayOf(r.localId.toString()))
                } else {
                    val values = ContentValues()
                    values.put("_secret", number)
                    values.put("_isdir", if (r.isFolder) 1 else 0)
                    values.put("_dir", dir)
                    if (!r.isFolder) values.put("_path", r.url)
                    values.put("_title", r.localTitle)
                    values.put("_time", now)
                    values.put("_rsv4", order)
                    values.put("_rsv6", 0L)
                    val id = insert(TABLE, null, values)
                    if (id > 0) newIds[r.serverId] = id
                }
            }
            execSQL("DELETE FROM DbBookWeb_sync WHERE _secret = -1")
        }
        return newIds to changeTimes(context, number).first
    }
}
