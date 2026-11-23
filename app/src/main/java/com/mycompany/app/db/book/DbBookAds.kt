package com.mycompany.app.db.book

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.mycompany.app.db.DbSite
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class DbBookAds(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookAds_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookAds_table")
        db.execSQL("CREATE TABLE DbBookAds_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookAds? = null

        @JvmStatic
        fun get(context: Context?): DbBookAds {
            var dbBookAds: DbBookAds?
            val existing: DbBookAds? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookAds::class.java) {
                dbBookAds = cached
                if (dbBookAds == null) {
                    dbBookAds = DbBookAds(MainUtil.appContextOf(context), "DbBookAds.db", null, 2)
                    cached = dbBookAds
                }
            }
            return dbBookAds!!
        }

        /** Adds a site to this list (or refreshes its time) and returns it as a list item.  */
        @JvmStatic
        fun addPath(context: Context?, path: String?): ChildItem? {
            if (context == null) {
                return null
            }
            return DbSite.addPath(get(context).getWritableDatabase(), "DbBookAds_table", path)
        }
    }
}
