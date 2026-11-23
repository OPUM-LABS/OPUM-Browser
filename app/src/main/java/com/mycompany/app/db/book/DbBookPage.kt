package com.mycompany.app.db.book

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class DbBookPage(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookPage_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _dir TEXT, _dname TEXT, _path TEXT, _name TEXT, _url TEXT, _icon BLOB, _time INTEGER, _size INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookPage_table")
        db.execSQL("CREATE TABLE DbBookPage_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _dir TEXT, _dname TEXT, _path TEXT, _name TEXT, _url TEXT, _icon BLOB, _time INTEGER, _size INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookPage? = null

        @JvmStatic
        fun get(context: Context?): DbBookPage {
            var dbBookPage: DbBookPage?
            val existing: DbBookPage? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookPage::class.java) {
                dbBookPage = cached
                if (dbBookPage == null) {
                    dbBookPage = DbBookPage(MainUtil.appContextOf(context), "DbBookPage.db", null, 3)
                    cached = dbBookPage
                }
            }
            return dbBookPage!!
        }
    }
}
