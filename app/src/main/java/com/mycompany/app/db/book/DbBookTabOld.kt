package com.mycompany.app.db.book

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class DbBookTabOld(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    private class TabOldItem {
        var uid: Long = 0
        var parentId: Long = 0
        var groupId: Long = 0
        var groupName: String? = null
        var color: Int = 0
        var path: String? = null
        var title: String? = null
        var deskMode: Int = 0
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookTab2_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _gid INTEGER, _gname TEXT, _color INTEGER, _index INTEGER, _path TEXT, _title TEXT, _state TEXT, _desk INTEGER, _ikey TEXT, _icon BLOB, _tkey TEXT, _thumb BLOB, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookTab2_table")
        db.execSQL("CREATE TABLE DbBookTab2_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _gid INTEGER, _gname TEXT, _color INTEGER, _index INTEGER, _path TEXT, _title TEXT, _state TEXT, _desk INTEGER, _ikey TEXT, _icon BLOB, _tkey TEXT, _thumb BLOB, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookTabOld? = null

        @JvmStatic
        fun get(context: Context?): DbBookTabOld {
            var dbBookTabOld: DbBookTabOld?
            val existing: DbBookTabOld? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookTabOld::class.java) {
                dbBookTabOld = cached
                if (dbBookTabOld == null) {
                    dbBookTabOld = DbBookTabOld(MainUtil.appContextOf(context), "DbBookTab2.db", null, 1)
                    cached = dbBookTabOld
                }
            }
            return dbBookTabOld!!
        }

        /** Copies the tabs of the old DbBookTab2 table into DbBookTab3, linking each to the previous one.  */
        @JvmStatic
        fun readTabs(context: Context?, secret: Boolean) {
            if (context == null) {
                return
            }
            val tabs: ArrayList<TabOldItem?> = ArrayList()
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(), "DbBookTab2_table", null, "_secret=?",
                    arrayOf<String?>(if (secret) "1" else "0"), "_index ASC"
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val colUid = cursor.getColumnIndex("_rsv4")
                    val colGid = cursor.getColumnIndex("_gid")
                    val colGname = cursor.getColumnIndex("_gname")
                    val colColor = cursor.getColumnIndex("_color")
                    val colPath = cursor.getColumnIndex("_path")
                    val colTitle = cursor.getColumnIndex("_title")
                    val colDesk = cursor.getColumnIndex("_desk")
                    do {
                        try {
                            val tab = TabOldItem()
                            tab.uid = cursor.getLong(colUid)
                            tab.groupId = cursor.getLong(colGid)
                            tab.groupName = cursor.getString(colGname)
                            tab.color = cursor.getInt(colColor)
                            tab.path = cursor.getString(colPath)
                            tab.title = cursor.getString(colTitle)
                            tab.deskMode = cursor.getInt(colDesk)
                            tabs.add(tab)
                        } catch (unused: Exception) {
                        }
                    } while (cursor.moveToNext())
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
            var prev: Long = -1
            for (obj in tabs) {
                val tab: TabOldItem = obj as TabOldItem
                tab.parentId = prev
                val values = ContentValues()
                values.put("_secret", if (secret) 1 else 0)
                values.put("_uid", tab.uid)
                values.put("_pid", tab.parentId)
                values.put("_gid", tab.groupId)
                values.put("_gname", tab.groupName)
                values.put("_color", tab.color)
                values.put("_path", tab.path)
                values.put("_title", tab.title)
                values.put("_desk", tab.deskMode)
                if (DbUtil.insert(
                        DbBookTab.Companion.get(context).getWritableDatabase(),
                        "DbBookTab3_table",
                        values
                    ) != 0L
                ) {
                    prev = tab.uid
                }
            }
        }
    }
}
