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
class DbBookTrans(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookTrans_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookTrans_table")
        db.execSQL("CREATE TABLE DbBookTrans_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookTrans? = null

        @JvmStatic
        fun exportFile(context: Context?, path: String?): File? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookTrans_table",
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
        fun get(context: Context?): DbBookTrans {
            var dbBookTrans: DbBookTrans?
            val existing: DbBookTrans? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookTrans::class.java) {
                dbBookTrans = cached
                if (dbBookTrans == null) {
                    dbBookTrans = DbBookTrans(MainUtil.appContextOf(context), "DbBookTrans.db", null, 1)
                    cached = dbBookTrans
                }
            }
            return dbBookTrans!!
        }

        /** Adds a site to this list (or refreshes its time) and returns it as a list item.  */
        @JvmStatic
        fun addPath(context: Context?, path: String?): ChildItem? {
            if (context == null) {
                return null
            }
            return DbSite.addPath(get(context).getWritableDatabase(), "DbBookTrans_table", path)
        }
    }
}
