package com.mycompany.app.db.book

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.mycompany.app.db.DbSite
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class DbBookLink(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookLink_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookLink_table")
        db.execSQL("CREATE TABLE DbBookLink_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookLink? = null

        @JvmStatic
        fun get(context: Context?): DbBookLink {
            var dbBookLink: DbBookLink?
            val existing: DbBookLink? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookLink::class.java) {
                dbBookLink = cached
                if (dbBookLink == null) {
                    dbBookLink = DbBookLink(MainUtil.appContextOf(context), "DbBookLink.db", null, 1)
                    cached = dbBookLink
                }
            }
            return dbBookLink!!
        }

        /** Adds a site to this list (or refreshes its time) and returns it as a list item.  */
        @JvmStatic
        fun addPath(context: Context?, path: String?): ChildItem? {
            if (context == null) {
                return null
            }
            return DbSite.addPath(get(context).getWritableDatabase(), "DbBookLink_table", path)
        }
    }
}
