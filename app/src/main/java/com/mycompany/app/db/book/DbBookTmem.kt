package com.mycompany.app.db.book

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.mycompany.app.db.DbExport
import com.mycompany.app.db.DbExport.RowWriter
import com.mycompany.app.db.DbSite
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import java.io.BufferedWriter
import java.io.File

@Suppress("deprecation")
class DbBookTmem(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookTmem_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookTmem_table")
        db.execSQL("CREATE TABLE DbBookTmem_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookTmem? = null

        @JvmStatic
        fun exportFile(context: Context?, path: String?): File? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookTmem_table",
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
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2!!.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
            })
        }

        @JvmStatic
        fun get(context: Context?): DbBookTmem {
            var dbBookTmem: DbBookTmem?
            val existing: DbBookTmem? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookTmem::class.java) {
                dbBookTmem = cached
                if (dbBookTmem == null) {
                    dbBookTmem = DbBookTmem(MainUtil.appContextOf(context), "DbBookTmem.db", null, 1)
                    cached = dbBookTmem
                }
            }
            return dbBookTmem!!
        }

        /** Adds a site to this list (or refreshes its time) and returns it as a list item.  */
        @JvmStatic
        fun addPath(context: Context?, path: String?): ChildItem? {
            if (context == null) {
                return null
            }
            return DbSite.addPath(get(context).getWritableDatabase(), "DbBookTmem_table", path)
        }
    }
}
