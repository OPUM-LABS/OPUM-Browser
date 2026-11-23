package com.mycompany.app.db.book

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class DbBookOver(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookOver_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookOver_table")
        db.execSQL("CREATE TABLE DbBookOver_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookOver? = null

        @JvmStatic
        fun get(context: Context?): DbBookOver {
            var dbBookOver: DbBookOver?
            val existing: DbBookOver? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookOver::class.java) {
                dbBookOver = cached
                if (dbBookOver == null) {
                    dbBookOver = DbBookOver(MainUtil.appContextOf(context), "DbBookOver.db", null, 1)
                    cached = dbBookOver
                }
            }
            return dbBookOver!!
        }
    }
}
