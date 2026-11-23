package com.mycompany.app.db.book

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class DbBookMemo(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookMemo_table (_id INTEGER PRIMARY KEY, _text TEXT, _time INTEGER, _order INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookMemo_table")
        db.execSQL("CREATE TABLE DbBookMemo_table (_id INTEGER PRIMARY KEY, _text TEXT, _time INTEGER, _order INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookMemo? = null

        @JvmStatic
        fun get(context: Context?): DbBookMemo {
            var dbBookMemo: DbBookMemo?
            val existing: DbBookMemo? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookMemo::class.java) {
                dbBookMemo = cached
                if (dbBookMemo == null) {
                    dbBookMemo = DbBookMemo(MainUtil.appContextOf(context), "DbBookMemo.db", null, 1)
                    cached = dbBookMemo
                }
            }
            return dbBookMemo!!
        }

        @JvmStatic
        fun setOrder(context: Context?, id: Long, index: Int) {
            if (context == null || id <= 0) {
                return
            }
            try {
                val contentValues = ContentValues()
                contentValues.put("_order", index)
                DbUtil.updateById(get(context).getWritableDatabase(), "DbBookMemo_table", contentValues, id)
            } catch (unused: Exception) {
            }
        }
    }
}
