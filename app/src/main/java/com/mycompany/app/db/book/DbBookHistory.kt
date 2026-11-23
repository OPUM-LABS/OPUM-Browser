package com.mycompany.app.db.book

import android.content.Context
import com.mycompany.app.pref.store.BrowserSettings
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.mycompany.app.db.DbExport
import com.mycompany.app.db.DbExport.RowWriter
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefWeb
import java.io.BufferedWriter
import java.io.File

@Suppress("deprecation")
class DbBookHistory(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookHistory_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _count INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookHistory_table")
        db.execSQL("CREATE TABLE DbBookHistory_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _count INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookHistory? = null

        @JvmStatic
        fun exportFile(context: Context?, path: String?): File? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookHistory_table",
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
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
            })
        }

        @JvmStatic
        fun get(context: Context?): DbBookHistory {
            var dbBookHistory: DbBookHistory?
            val existing: DbBookHistory? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookHistory::class.java) {
                dbBookHistory = cached
                if (dbBookHistory == null) {
                    dbBookHistory = DbBookHistory(MainUtil.appContextOf(context), "DbBookHistory.db", null, 2)
                    cached = dbBookHistory
                }
            }
            return dbBookHistory!!
        }

        @JvmStatic
        fun clearAll(context: Context?) {
            if (context == null) {
                return
            }
            DbUtil.deleteWhere(get(context).getWritableDatabase(), "DbBookHistory_table", null, null)
        }

        @JvmStatic
        fun clearSecret(context: Context?, secret: Boolean) {
            if (context == null) {
                return
            }
            DbUtil.deleteWhere(
                get(context).getWritableDatabase(),
                "DbBookHistory_table",
                "_secret=?",
                arrayOf<String?>(if (secret) "1" else "0")
            )
        }

        @JvmStatic
        fun clearOld(context: Context?) {
            if (BrowserSettings.historyTime.value == -1 || context == null) {
                return
            }
            DbUtil.deleteWhere(
                get(context).getWritableDatabase(),
                "DbBookHistory_table",
                "_time<?",
                arrayOf<String?>((System.currentTimeMillis() - ((BrowserSettings.historyTime.value.toLong()) * 86400000)).toString())
            )
        }
    }
}
