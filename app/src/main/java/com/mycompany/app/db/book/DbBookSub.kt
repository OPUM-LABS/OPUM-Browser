package com.mycompany.app.db.book

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class DbBookSub(context: Context?, nameParam: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, nameParam, factory, version) {

    internal class ExistsWhereTask(@JvmField val context: Context?,
                                   @JvmField val path: String?,
                                   @JvmField val sub: String?,
                                   @JvmField val sync: Int,
                                   @JvmField val count: Int
    ) : Runnable {
        override fun run() {
            val dbBookSub: DbBookSub? = Companion.cached
            val path = this.path
            if (TextUtils.isEmpty(path)) {
                return
            }
            val args = arrayOf<String?>(path)
            val context = this.context
            val writableDatabase: SQLiteDatabase? = get(context).getWritableDatabase()
            val exists = DbUtil.existsWhere(writableDatabase, "DbBookSub_table", null, "_path=?", args)
            if (exists != 0) {
                val contentValuesE = android.content.ContentValues().also { cv -> cv.put("_path", path); cv.put("_sub", this.sub) }
                contentValuesE.put("_sync", this.sync)
                contentValuesE.put("_lang", this.count)
                contentValuesE.put("_time", System.currentTimeMillis())
                if (exists == 1) {
                    DbUtil.updateWhere(writableDatabase, "DbBookSub_table", contentValuesE, "_path=?", args)
                } else if (DbUtil.insert(writableDatabase, "DbBookSub_table", contentValuesE) != 0L) {
                    trimOld(context)
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookSub_table (_id INTEGER PRIMARY KEY, _path TEXT, _sub TEXT, _sync INTEGER, _lang INTEGER, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookSub_table")
        db.execSQL("CREATE TABLE DbBookSub_table (_id INTEGER PRIMARY KEY, _path TEXT, _sub TEXT, _sync INTEGER, _lang INTEGER, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookSub? = null

        @JvmStatic
        fun trimOld(context: Context?) {
            if (context == null) {
                return
            }
            // Keeps at most 500 rows: drops the oldest one.
            var oldest: Long = 0
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookSub_table",
                    arrayOf<String?>("_id"),
                    null,
                    null,
                    "_time ASC"
                )
                if (cursor != null && cursor.getCount() > 500 && cursor.moveToFirst()) {
                    oldest = cursor.getLong(cursor.getColumnIndex("_id"))
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
            if (oldest > 0) {
                DbUtil.deleteById(get(context).getWritableDatabase(), "DbBookSub_table", oldest)
            }
        }

        @JvmStatic
        fun get(context: Context?): DbBookSub {
            var dbBookSub: DbBookSub?
            val existing: DbBookSub? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookSub::class.java) {
                dbBookSub = cached
                if (dbBookSub == null) {
                    dbBookSub = DbBookSub(MainUtil.appContextOf(context), "DbBookSub.db", null, 1)
                    cached = dbBookSub
                }
            }
            return dbBookSub!!
        }

        @JvmStatic
        fun putSub(context: Context?, type: Int, path: String?, uriString: String?) {
            if (context == null || TextUtils.isEmpty(path)) {
                return
            }
            val writableDatabase: SQLiteDatabase? = get(context).getWritableDatabase()
            val args: Array<String?>? = arrayOf<String?>(path!!)
            val exists = DbUtil.existsWhere(writableDatabase, "DbBookSub_table", null, "_path=?", args)
            if (exists == 0) {
                return
            }
            val contentValuesE = android.content.ContentValues().also { cv -> cv.put("_path", path); cv.put("_rsv1", uriString) }
            contentValuesE.put("_rsv4", type)
            contentValuesE.put("_time", System.currentTimeMillis())
            if (exists == 1) {
                DbUtil.updateWhere(writableDatabase, "DbBookSub_table", contentValuesE, "_path=?", args)
            } else if (DbUtil.insert(writableDatabase, "DbBookSub_table", contentValuesE) != 0L) {
                trimOld(context)
            }
        }

        @JvmStatic
        fun addAsync(context: Context?, path: String?, sub: String?, type: Int, sync: Int) {
            val contextQ = MainUtil.appContextOf(context)
            if (contextQ == null) {
                return
            }
            MainApp.runInBackground(contextQ, ExistsWhereTask(contextQ, path, sub, type, sync))
        }

    }
}
