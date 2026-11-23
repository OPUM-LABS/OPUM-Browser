package com.mycompany.app.db.book

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.mycompany.app.main.MainUtil
import ch.opumlabs.opumbrowser.R

@Suppress("deprecation")
class DbBookPms(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookPms_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _allow INTEGER, _block INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookPms_table")
        db.execSQL("CREATE TABLE DbBookPms_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _allow INTEGER, _block INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookPms? = null

        @JvmStatic
        fun get(context: Context?): DbBookPms {
            var dbBookPms: DbBookPms?
            val existing: DbBookPms? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookPms::class.java) {
                dbBookPms = cached
                if (dbBookPms == null) {
                    dbBookPms = DbBookPms(MainUtil.appContextOf(context), "DbBookPms.db", null, 1)
                    cached = dbBookPms
                }
            }
            return dbBookPms!!
        }
    }
}
