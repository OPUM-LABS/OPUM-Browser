package com.mycompany.app.db.book

import android.content.ContentValues
import com.mycompany.app.pref.store.SecretSettings
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.mycompany.app.data.DataUtil.addUri
import com.mycompany.app.data.DataUtil.fileKind
import com.mycompany.app.db.DbExport
import com.mycompany.app.db.DbExport.RowWriter
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.web.WebTabItem
import java.io.BufferedWriter
import java.io.File
import kotlin.math.max

@Suppress("deprecation")
class DbBookTab(context: Context?, nameParam: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, nameParam, factory, version) {

    internal class UpdateWhereTask(@JvmField val context: Context?,
                                   @JvmField val tabs: ArrayList<WebTabItem?>
    ) : Runnable {
        override fun run() {
            val dbBookTab: DbBookTab? = Companion.cached
            val arrayList = this.tabs
            if (!arrayList.isEmpty()) {
                val size = arrayList.size
                var index = 0
                while (index < size) {
                    val entry: Any? = arrayList.get(index)
                    index++
                    val webTabItem = entry as WebTabItem?
                    if (webTabItem != null) {
                        val uid = webTabItem.uid
                        if (uid > 0 && webTabItem.parentId != 0L) {
                            val args = arrayOf<String?>(uid.toString())
                            val contentValues = ContentValues()
                            contentValues.put("_pid", webTabItem.parentId)
                            contentValues.put("_gid", webTabItem.groupId)
                            contentValues.put("_gname", webTabItem.groupName)
                            contentValues.put("_color", webTabItem.groupColor)
                            DbUtil.updateWhere(
                                get(this.context).getWritableDatabase(),
                                "DbBookTab3_table",
                                contentValues,
                                "_uid=?",
                                args
                            )
                        }
                    }
                }
            }
            Companion.isSaving = false
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookTab3_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _uid INTEGER, _pid INTEGER, _gid INTEGER, _gname TEXT, _color INTEGER, _path TEXT, _title TEXT, _state TEXT, _desk INTEGER, _ikey TEXT, _icon BLOB, _tkey TEXT, _thumb BLOB, _ads TEXT, _pages TEXT, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookTab3_table")
        db.execSQL("CREATE TABLE DbBookTab3_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _uid INTEGER, _pid INTEGER, _gid INTEGER, _gname TEXT, _color INTEGER, _path TEXT, _title TEXT, _state TEXT, _desk INTEGER, _ikey TEXT, _icon BLOB, _tkey TEXT, _thumb BLOB, _ads TEXT, _pages TEXT, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    /** One archived tab: the tab as `loadTabs` would build it, and when it was archived (ms). */
    class Archived(@JvmField val tab: WebTabItem, @JvmField val archivedAt: Long)

    companion object {
        @JvmField
        var cached: DbBookTab? = null
        @JvmField
        var isSaving: Boolean = false

        @JvmStatic
        fun exportFile(context: Context?, path: String?): File? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookTab3_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return DbExport.exportCsv(cursor, path, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter)
                        && MainUtil.writeIntEntry(bufferedWriter, "_secret", cursor2!!.getInt(cursor2.getColumnIndex("_secret")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_uid", cursor2.getLong(cursor2.getColumnIndex("_uid")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_pid", cursor2.getLong(cursor2.getColumnIndex("_pid")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_gid", cursor2.getLong(cursor2.getColumnIndex("_gid")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_gname", cursor2.getString(cursor2.getColumnIndex("_gname")))
                        && MainUtil.writeIntEntry(bufferedWriter, "_color", cursor2.getInt(cursor2.getColumnIndex("_color")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeIntEntry(bufferedWriter, "_desk", cursor2.getInt(cursor2.getColumnIndex("_desk")))
            })
        }

        @JvmStatic
        fun get(context: Context?): DbBookTab {
            var dbBookTab: DbBookTab?
            val existing: DbBookTab? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookTab::class.java) {
                dbBookTab = cached
                if (dbBookTab == null) {
                    dbBookTab = DbBookTab(MainUtil.appContextOf(context), "DbBookTab3.db", null, 1)
                    cached = dbBookTab
                }
            }
            return dbBookTab!!
        }

        @JvmStatic
        fun loadTabs(context: Context?, secret: Boolean): ArrayList<WebTabItem?>? {
            var arrayList: ArrayList<WebTabItem?>? = null
            var result: ArrayList<WebTabItem?>? = null
            var cursorG: Cursor?
            var position: Int
            var countCursor: Cursor?
            var count = 0
            var oldCursor: Cursor?
            var oldCount = 0
            var oldTabs: ArrayList<WebTabItem?>? = null
            if (context == null) {
                return null
            }
            if (SecretSettings.checkTab.value) {
                SecretSettings.edit(context) { set(SecretSettings.checkTab, false) }
                try {
                    countCursor = DbUtil.query(
                        DbBookTabOld.Companion.get(context).getWritableDatabase(),
                        "DbBookTab2_table",
                        null,
                        null,
                        null,
                        null
                    )
                    if (countCursor != null) {
                        try {
                            count = countCursor.getCount()
                        } catch (unused: Exception) {
                            count = 0
                        }
                    } else {
                        count = 0
                    }
                } catch (unused2: Exception) {
                    countCursor = null
                }
                if (countCursor != null) {
                    countCursor.close()
                }
                if (count > 0) {
                    try {
                        oldCursor = DbUtil.query(
                            get(context).getWritableDatabase(),
                            "DbBookTab3_table",
                            null,
                            null,
                            null,
                            null
                        )
                        if (oldCursor != null) {
                            try {
                                oldCount = oldCursor.getCount()
                            } catch (unused3: Exception) {
                                oldCount = 0
                            }
                        } else {
                            oldCount = 0
                        }
                    } catch (unused4: Exception) {
                        oldCursor = null
                    }
                    if (oldCursor != null) {
                        oldCursor.close()
                    }
                    if (oldCount <= 0) {
                        DbBookTabOld.Companion.readTabs(context, false)
                        DbBookTabOld.Companion.readTabs(context, true)
                        DbUtil.deleteWhere(
                            DbBookTabOld.Companion.get(context).getWritableDatabase(),
                            "DbBookTab2_table",
                            null,
                            null
                        )
                    }
                }
            }
            var loadedTabs: ArrayList<WebTabItem?>? = ArrayList<WebTabItem?>()
            try {
                cursorG = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookTab3_table",
                    null,
                    // archived tabs (`_rsv5` = when, see [archiveTabs]) are not in the tab list
                    "_secret=? AND (_rsv5 IS NULL OR _rsv5=0)",
                    arrayOf<String?>(if (secret) "1" else "0"),
                    "_pid ASC"
                )
                if (cursorG != null) {
                    try {
                        if (cursorG.moveToFirst()) {
                            val columnIndex = cursorG.getColumnIndex("_id")
                            val uidCol = cursorG.getColumnIndex("_uid")
                            val pidCol = cursorG.getColumnIndex("_pid")
                            val gidCol = cursorG.getColumnIndex("_gid")
                            val gnameCol = cursorG.getColumnIndex("_gname")
                            val colorCol = cursorG.getColumnIndex("_color")
                            val pathCol = cursorG.getColumnIndex("_path")
                            val titleCol = cursorG.getColumnIndex("_title")
                            val deskCol = cursorG.getColumnIndex("_desk")
                            val columnUsed = cursorG.getColumnIndex("_rsv4") // last used (ms), see [setLastUsed]
                            while (true) {
                                try {
                                    val webTabItem = WebTabItem()
                                    cursorG.getLong(columnIndex)
                                    arrayList = oldTabs
                                    val collected = loadedTabs
                                    try {
                                        webTabItem.uid = cursorG.getLong(uidCol)
                                        webTabItem.parentId = cursorG.getLong(pidCol)
                                        val groupId = cursorG.getLong(gidCol)
                                        webTabItem.groupId = groupId
                                        if (groupId != 0L) {
                                            webTabItem.groupName = cursorG.getString(gnameCol)
                                            webTabItem.groupColor = cursorG.getInt(colorCol)
                                        }
                                        val tabUrl = cursorG.getString(pathCol)
                                        webTabItem.tabUrl = tabUrl
                                        webTabItem.tabUrl = MainUtil.offlinePathFor(context, tabUrl)
                                        webTabItem.tabTitle = cursorG.getString(titleCol)
                                        webTabItem.deskMode = cursorG.getInt(deskCol) == 1
                                        if (columnUsed >= 0) webTabItem.lastUsed = cursorG.getLong(columnUsed)
                                        result = collected
                                        try {
                                            result!!.add(webTabItem)
                                        } catch (unused5: Exception) {
                                        }
                                    } catch (unused6: Exception) {
                                        result = collected
                                    }
                                } catch (unused7: Exception) {
                                    arrayList = oldTabs
                                    result = loadedTabs
                                }
                                try {
                                    if (!cursorG.moveToNext()) {
                                        break
                                    }
                                    loadedTabs = result
                                    oldTabs = arrayList
                                } catch (unused8: Exception) {
                                }
                            }
                        } else {
                            arrayList = null
                            result = loadedTabs
                        }
                    } catch (unused9: Exception) {
                    }
                } else {
                    arrayList = null
                    result = loadedTabs
                }
            } catch (unused10: Exception) {
                arrayList = null
                result = loadedTabs
                cursorG = null
            }
            if (cursorG != null) {
                cursorG.close()
            }
            if (result!!.isEmpty()) {
                return arrayList
            }
            var webTabItemF: WebTabItem? = Companion.firstChild(result, -1L)
            if (webTabItemF == null) {
                webTabItemF = Companion.firstRoot(result)
                if (webTabItemF == null) {
                    return arrayList
                }
                webTabItemF.parentId = -1L
                val copies: ArrayList<WebTabItem?> = ArrayList<WebTabItem?>()
                val copy = WebTabItem()
                copy.uid = webTabItemF.uid
                copy.parentId = webTabItemF.parentId
                copies.add(copy)
                arrayList = copies
            }
            val singleTabs: ArrayList<WebTabItem?> = ArrayList<WebTabItem?>()
            try {
                result.remove(webTabItemF)
                singleTabs.add(webTabItemF)
                webTabItemF.position = 0
                position = 1
                while (true) {
                    try {
                        webTabItemF = Companion.firstChild(result, webTabItemF!!.uid)
                        if (webTabItemF == null && (Companion.firstRoot(result)
                                .also { webTabItemF = it }) == null
                        ) {
                            break
                        }
                        result.remove(webTabItemF)
                        singleTabs.add(webTabItemF)
                        webTabItemF!!.position = position
                        position++
                    } catch (unused11: Exception) {
                        // the state would not change, so retrying would loop forever; the remaining tabs are
                        // appended below
                        break
                    }
                }
            } catch (unused12: Exception) {
                position = 0
            }
            if (!result.isEmpty()) {
                try {
                    var previousUid: Long = prevUid(position, singleTabs)
                    val size = result.size
                    var index = 0
                    while (index < size) {
                        val entry: Any? = result.get(index)
                        index++
                        val tab = entry as WebTabItem?
                        if (tab != null) {
                            singleTabs.add(tab)
                            val newParent = tab.parentId != previousUid
                            tab.parentId = previousUid
                            tab.position = position
                            previousUid = tab.uid
                            position++
                            if (newParent) {
                                val withOthers =
                                    if (arrayList == null) ArrayList<WebTabItem?>() else arrayList
                                try {
                                    val otherCopy = WebTabItem()
                                    otherCopy.uid = tab.uid
                                    otherCopy.parentId = tab.parentId
                                    withOthers.add(otherCopy)
                                    arrayList = withOthers
                                } catch (unused13: Exception) {
                                }
                            }
                        }
                    }
                } catch (unused14: Exception) {
                }
            }
            val tabs = arrayList
            if (tabs != null && !tabs.isEmpty()) {
                updateParents(context, tabs)
            }
            return singleTabs
        }

        @JvmStatic
        fun firstRoot(arrayList: ArrayList<WebTabItem?>): WebTabItem? {
            var webTabItem: WebTabItem?
            if (arrayList.isEmpty()) {
                return null
            }
            val size = arrayList.size
            var index = 0
            while (index < size) {
                val entry: Any? = arrayList.get(index)
                index++
                val tab = entry as WebTabItem?
                if (tab != null) {
                    val parentId = tab.parentId
                    if (!arrayList.isEmpty()) {
                        val tabCount = arrayList.size
                        var tabIndex = 0
                        while (true) {
                            if (tabIndex >= tabCount) {
                                webTabItem = null
                                break
                            }
                            val otherEntry: Any? = arrayList.get(tabIndex)
                            tabIndex++
                            webTabItem = otherEntry as WebTabItem?
                            if (webTabItem != null && webTabItem.uid == parentId) {
                                break
                            }
                        }
                    } else {
                        webTabItem = null
                        break
                    }
                    if (webTabItem == null) {
                        return tab
                    }
                }
            }
            return arrayList.get(0)
        }

        @JvmStatic
        fun newUid(arrayList: ArrayList<WebTabItem?>?): Long {
            val now = System.currentTimeMillis()
            if (arrayList != null && !arrayList.isEmpty()) {
                val size = arrayList.size
                var found = false
                var maxUid: Long = 0
                var index = 0
                while (index < size) {
                    val entry: Any? = arrayList.get(index)
                    index++
                    val webTabItem = entry as WebTabItem?
                    if (webTabItem != null) {
                        val uid = webTabItem.uid
                        if (uid == now) {
                            found = true
                        }
                        maxUid = max(maxUid, uid)
                    }
                }
                if (found) {
                    return maxUid + 1
                }
            }
            return now
        }

        @JvmStatic
        fun firstChild(arrayList: ArrayList<WebTabItem?>, uid: Long): WebTabItem? {
            if (arrayList.isEmpty()) {
                return null
            }
            val size = arrayList.size
            var index = 0
            while (index < size) {
                val entry: Any? = arrayList.get(index)
                index++
                val webTabItem = entry as WebTabItem?
                if (webTabItem != null && webTabItem.parentId == uid) {
                    return webTabItem
                }
            }
            return null
        }

        @JvmStatic
        fun prevUid(size: Int, arrayList: ArrayList<WebTabItem?>?): Long {
            val index = size - 1
            val webTabItem =
                if (arrayList == null || index < 0 || index >= arrayList.size) null else arrayList.get(index)
            if (webTabItem == null) {
                return -1L
            }
            return webTabItem.uid
        }

        @JvmStatic
        fun deleteTabs(context: Context?, arrayList: ArrayList<WebTabItem?>?) {
            if (context == null || arrayList == null || arrayList.isEmpty()) {
                return
            }
            val size = arrayList.size
            var index = 0
            while (index < size) {
                val entry: Any? = arrayList.get(index)
                index++
                val webTabItem = entry as WebTabItem?
                if (webTabItem != null) {
                    val uid = webTabItem.uid
                    if (uid > 0) {
                        val args = arrayOf<String?>(uid.toString())
                        DbUtil.deleteWhere(
                            get(context).getWritableDatabase(),
                            "DbBookTab3_table",
                            "_uid=?",
                            args
                        )
                        DbTabState.Companion.deleteWhere(context, "_uid=?", args)
                        DbTabThumb.Companion.deleteWhere(context, "_uid=?", args)
                    }
                }
            }
        }

        @JvmStatic
        fun deleteById(context: Context?, id: Long) {
            if (context == null || id <= 0) {
                return
            }
            val args = arrayOf<String?>(id.toString())
            DbUtil.deleteWhere(get(context).getWritableDatabase(), "DbBookTab3_table", "_uid=?", args)
            DbTabState.Companion.deleteWhere(context, "_uid=?", args)
            DbTabThumb.Companion.deleteWhere(context, "_uid=?", args)
        }

        @JvmStatic
        fun clearAll(context: Context?) {
            if (context == null) {
                return
            }
            DbUtil.deleteWhere(get(context).getWritableDatabase(), "DbBookTab3_table", null, null)
            DbUtil.deleteWhere(
                DbTabState.Companion.get(context).getWritableDatabase(),
                "DbTabState_table",
                null,
                null
            )
            DbUtil.deleteWhere(
                DbTabThumb.Companion.get(context).getWritableDatabase(),
                "DbTabThumb_table",
                null,
                null
            )
        }

        @JvmStatic
        fun clearSecret(context: Context?, secret: Boolean) {
            if (context == null) {
                return
            }
            val args = arrayOf<String?>(if (secret) "1" else "0")
            DbUtil.deleteWhere(get(context).getWritableDatabase(), "DbBookTab3_table", "_secret=?", args)
            DbTabState.Companion.deleteWhere(context, "_secret=?", args)
            DbTabThumb.Companion.deleteWhere(context, "_secret=?", args)
        }

        @JvmStatic
        fun insertTab(context: Context?, webTabItem: WebTabItem?, secret: Boolean) {
            if (context == null || webTabItem == null) {
                return
            }
            val fileTitle = MainUtil.pageTitleForFile(context, webTabItem.tabUrl)
            val contentValues = ContentValues()
            contentValues.put("_secret", if (secret) 1 else 0)
            contentValues.put("_uid", webTabItem.uid)
            contentValues.put("_pid", webTabItem.parentId)
            contentValues.put("_gid", webTabItem.groupId)
            contentValues.put("_gname", webTabItem.groupName)
            contentValues.put("_color", webTabItem.groupColor)
            contentValues.put("_path", fileTitle)
            contentValues.put("_title", webTabItem.tabTitle)
            contentValues.put("_desk", if (webTabItem.deskMode) 1 else 0)
            DbUtil.insert(get(context).getWritableDatabase(), "DbBookTab3_table", contentValues)
        }

        @JvmStatic
        fun updateParents(context: Context?, arrayList: ArrayList<WebTabItem?>?) {
            if (context == null || arrayList == null || arrayList.isEmpty()) {
                return
            }
            val size = arrayList.size
            var index = 0
            while (index < size) {
                val entry: Any? = arrayList.get(index)
                index++
                val webTabItem = entry as WebTabItem?
                if (webTabItem != null) {
                    val uid = webTabItem.uid
                    if (uid > 0 && webTabItem.parentId != 0L) {
                        val args = arrayOf<String?>(uid.toString())
                        val contentValues = ContentValues()
                        contentValues.put("_pid", webTabItem.parentId)
                        DbUtil.updateWhere(
                            get(context).getWritableDatabase(),
                            "DbBookTab3_table",
                            contentValues,
                            "_uid=?",
                            args
                        )
                    }
                }
            }
        }

        @JvmStatic
        fun setGroup(context: Context?, uid: Long, groupId: Long) {
            if (context == null || uid <= 0 || groupId <= 0) {
                return
            }
            val args = arrayOf<String?>(uid.toString())
            val contentValues = ContentValues()
            contentValues.put("_gid", groupId)
            DbUtil.updateWhere(
                get(context).getWritableDatabase(),
                "DbBookTab3_table",
                contentValues,
                "_uid=?",
                args
            )
        }

        /**
         * When tab [uid] was last made the current tab (ms), kept in the spare column `_rsv4` (2026-11-19) so the tab overview can
         * offer "close tabs not used for N days" after a restart. Written in the background.
         */
        @JvmStatic
        fun setLastUsed(context: Context?, uid: Long, time: Long) {
            val app = MainUtil.appContextOf(context) ?: return
            if (uid <= 0 || time <= 0) return
            MainApp.runInBackground(app, Runnable {
                try {
                    val values = ContentValues()
                    values.put("_rsv4", time)
                    DbUtil.updateWhere(get(app).getWritableDatabase(), "DbBookTab3_table", values, "_uid=?", arrayOf<String?>(uid.toString()))
                } catch (_: Exception) {
                }
            })
        }

        // ---- archive (2026-11-21) ---------------------------------------------------------------------------------
        // An archived tab keeps its row (and its state and thumbnail rows) with the archive time in the spare column
        // `_rsv5`; [loadTabs] leaves such rows out. Restoring clears the mark. A version bump would drop the table
        // (see onUpgrade), hence the spare column.

        private fun uidList(uids: Collection<Long>): String = uids.filter { it > 0 }.joinToString(",")

        /** Marks tabs [uids] archived at [time]. Main thread is fine (one statement). */
        @JvmStatic
        fun archiveTabs(context: Context?, uids: Collection<Long>, time: Long) {
            val list = uidList(uids)
            if (context == null || list.isEmpty()) return
            try {
                get(context).getWritableDatabase().execSQL("UPDATE DbBookTab3_table SET _rsv5=$time WHERE _uid IN ($list)")
            } catch (_: Exception) {
            }
        }

        /** The archived (normal, not incognito) tabs, the most recently archived first. Background thread. */
        @JvmStatic
        fun archivedTabs(context: Context?): ArrayList<Archived> {
            val out = ArrayList<Archived>()
            if (context == null) return out
            var cursor: android.database.Cursor? = null
            try {
                cursor = get(context).getReadableDatabase().rawQuery(
                    "SELECT _uid, _gid, _gname, _color, _path, _title, _desk, _rsv4, _rsv5 FROM DbBookTab3_table " +
                        "WHERE _secret=0 AND _rsv5>0 ORDER BY _rsv5 DESC, _pid ASC", null,
                )
                while (cursor.moveToNext()) {
                    val tab = WebTabItem()
                    tab.uid = cursor.getLong(0)
                    tab.groupId = cursor.getLong(1)
                    if (tab.groupId != 0L) {
                        tab.groupName = cursor.getString(2)
                        tab.groupColor = cursor.getInt(3)
                    }
                    tab.tabUrl = MainUtil.offlinePathFor(context, cursor.getString(4))
                    tab.tabTitle = cursor.getString(5)
                    tab.deskMode = cursor.getInt(6) == 1
                    tab.lastUsed = cursor.getLong(7)
                    out.add(Archived(tab, cursor.getLong(8)))
                }
            } catch (_: Exception) {
            } finally {
                cursor?.close()
            }
            return out
        }

        /** How many tabs are archived (normal tabs only). */
        @JvmStatic
        fun archivedCount(context: Context?): Int {
            if (context == null) return 0
            return try {
                android.database.DatabaseUtils.longForQuery(
                    get(context).getReadableDatabase(), "SELECT COUNT(*) FROM DbBookTab3_table WHERE _secret=0 AND _rsv5>0", null,
                ).toInt()
            } catch (_: Exception) {
                0
            }
        }

        /** Takes tabs [uids] out of the archive; they count as used at [now] (so they are not archived again at once). */
        @JvmStatic
        fun restoreArchived(context: Context?, uids: Collection<Long>, now: Long) {
            val list = uidList(uids)
            if (context == null || list.isEmpty()) return
            try {
                get(context).getWritableDatabase().execSQL("UPDATE DbBookTab3_table SET _rsv5=0, _rsv4=$now WHERE _uid IN ($list)")
            } catch (_: Exception) {
            }
        }

        /** Deletes the archived tabs archived before [cutoff] (ms), with their state and thumbnail rows; returns how many. */
        @JvmStatic
        fun deleteArchivedBefore(context: Context?, cutoff: Long): Int {
            if (context == null) return 0
            val old = archivedTabs(context).filter { it.archivedAt < cutoff }
            for (item in old) deleteById(context, item.tab.uid)
            return old.size
        }

        /**
         * "Close all tabs": deletes the open tabs of a mode but keeps the archived ones ([clearSecret] deletes all, for
         * the privacy clean-ups).
         */
        @JvmStatic
        fun clearOpen(context: Context?, secret: Boolean) {
            if (context == null) return
            if (secret) { clearSecret(context, true); return }
            val uids = ArrayList<Long>()
            var cursor: android.database.Cursor? = null
            try {
                cursor = get(context).getReadableDatabase().rawQuery(
                    "SELECT _uid FROM DbBookTab3_table WHERE _secret=0 AND (_rsv5 IS NULL OR _rsv5=0)", null,
                )
                while (cursor.moveToNext()) uids.add(cursor.getLong(0))
            } catch (_: Exception) {
            } finally {
                cursor?.close()
            }
            for (uid in uids) deleteById(context, uid)
        }

        @JvmStatic
        fun setParent(context: Context?, uid: Long, parentUid: Long) {
            if (context == null || uid <= 0 || parentUid == 0L) {
                return
            }
            val args = arrayOf<String?>(uid.toString())
            val contentValues = ContentValues()
            contentValues.put("_pid", parentUid)
            DbUtil.updateWhere(
                get(context).getWritableDatabase(),
                "DbBookTab3_table",
                contentValues,
                "_uid=?",
                args
            )
        }

        @JvmStatic
        fun updateTabs(context: Context?, arrayList: ArrayList<WebTabItem?>) {
            val contextQ = MainUtil.appContextOf(context)
            if (contextQ == null) {
                return
            }
            isSaving = true
            MainApp.runInBackground(contextQ, UpdateWhereTask(contextQ, arrayList))
        }
    }
}
