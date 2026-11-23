package com.mycompany.app.db.book

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class DbBookAgent(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookAgent_table (_id INTEGER PRIMARY KEY, _title TEXT, _text TEXT, _icon BLOB, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookAgent_table")
        db.execSQL("CREATE TABLE DbBookAgent_table (_id INTEGER PRIMARY KEY, _title TEXT, _text TEXT, _icon BLOB, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookAgent? = null

        @JvmStatic
        fun get(context: Context?): DbBookAgent {
            var dbBookAgent: DbBookAgent?
            val existing: DbBookAgent? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookAgent::class.java) {
                dbBookAgent = cached
                if (dbBookAgent == null) {
                    dbBookAgent = DbBookAgent(MainUtil.appContextOf(context), "DbBookAgent.db", null, 1)
                    cached = dbBookAgent
                }
            }
            return dbBookAgent!!
        }

        @JvmStatic
        fun titleById(context: Context?, id: Long): String? {
            var cursorF: Cursor?
            var title: String? = null
            if (context != null && id > 0) {
                try {
                    cursorF = DbUtil.queryById(
                        get(context).getWritableDatabase(),
                        "DbBookAgent_table",
                        arrayOf<String?>("_title"),
                        id
                    )
                    if (cursorF != null) {
                        try {
                            if (cursorF.moveToFirst()) {
                                title = cursorF.getString(cursorF.getColumnIndex("_title"))
                            }
                        } catch (unused: Exception) {
                        }
                    }
                } catch (unused2: Exception) {
                    cursorF = null
                }
                if (cursorF != null) {
                    cursorF.close()
                }
            }
            return title
        }
    }
}
