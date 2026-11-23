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
class DbBookVpos(context: Context?, nameParam: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, nameParam, factory, version) {

    internal class ExistsWhereTask(@JvmField val context: Context?,
                                   @JvmField val position: Int,
                                   @JvmField val path: String?
    ) : Runnable {
        override fun run() {
            val path = this.path
            if (TextUtils.isEmpty(path)) {
                return
            }
            val args = arrayOf<String?>(path)
            val context = this.context
            val db: SQLiteDatabase? = get(context).getWritableDatabase()
            val state = DbUtil.existsWhere(db, "DbBookVpos_table", null, "_path=?", args)
            if (state == 0) {
                return
            }
            val cv = android.content.ContentValues().also { cv -> cv.put("_path", path) }
            cv.put("_pos", this.position)
            cv.put("_time", System.currentTimeMillis())
            if (state == 1) {
                DbUtil.updateWhere(db, "DbBookVpos_table", cv, "_path=?", args)
                return
            }
            if (DbUtil.insert(db, "DbBookVpos_table", cv) == 0L) {
                return
            }
            // Keeps at most 500 positions: drops the oldest one.
            var oldest: Long = 0
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookVpos_table",
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
                DbUtil.deleteById(get(context).getWritableDatabase(), "DbBookVpos_table", oldest)
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookVpos_table (_id INTEGER PRIMARY KEY, _path TEXT, _pos INTEGER, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookVpos_table")
        db.execSQL("CREATE TABLE DbBookVpos_table (_id INTEGER PRIMARY KEY, _path TEXT, _pos INTEGER, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookVpos? = null

        @JvmStatic
        fun get(context: Context?): DbBookVpos {
            var dbBookVpos: DbBookVpos?
            val existing: DbBookVpos? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookVpos::class.java) {
                dbBookVpos = cached
                if (dbBookVpos == null) {
                    dbBookVpos = DbBookVpos(MainUtil.appContextOf(context), "DbBookVpos.db", null, 1)
                    cached = dbBookVpos
                }
            }
            return dbBookVpos!!
        }

        @JvmStatic
        fun savePosition(context: Context?, current: Int, source: String?) {
            val contextQ = MainUtil.appContextOf(context)
            if (contextQ == null) {
                return
            }
            MainApp.runInBackground(contextQ, ExistsWhereTask(contextQ, current, source))
        }
    }
}
