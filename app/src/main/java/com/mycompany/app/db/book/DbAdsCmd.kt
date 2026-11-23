package com.mycompany.app.db.book

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class DbAdsCmd(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbAdsCmd_table (_id INTEGER PRIMARY KEY, _host TEXT, _ele TEXT, _doc TEXT, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbAdsCmd_table")
        db.execSQL("CREATE TABLE DbAdsCmd_table (_id INTEGER PRIMARY KEY, _host TEXT, _ele TEXT, _doc TEXT, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbAdsCmd? = null

        @JvmStatic
        fun getCmd(context: Context?, host: String?, column: String?): String? {
            var cursorG: Cursor?
            var valueText: String? = null
            if (context != null && !TextUtils.isEmpty(host)) {
                try {
                    cursorG = DbUtil.query(
                        get(context).getWritableDatabase(),
                        "DbAdsCmd_table",
                        arrayOf<String?>(column),
                        "_host=?",
                        arrayOf<String?>(host!!),
                        null
                    )
                    if (cursorG != null) {
                        try {
                            if (cursorG.moveToFirst()) {
                                valueText = cursorG.getString(cursorG.getColumnIndex(column))
                            }
                        } catch (unused: Exception) {
                        }
                    }
                } catch (unused2: Exception) {
                    cursorG = null
                }
                if (cursorG != null) {
                    cursorG.close()
                }
            }
            return valueText
        }

        @JvmStatic
        fun get(context: Context?): DbAdsCmd {
            var dbAdsCmd: DbAdsCmd?
            val existing: DbAdsCmd? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbAdsCmd::class.java) {
                dbAdsCmd = cached
                if (dbAdsCmd == null) {
                    dbAdsCmd = DbAdsCmd(MainUtil.appContextOf(context), "DbAdsCmd.db", null, 1)
                    cached = dbAdsCmd
                }
            }
            return dbAdsCmd!!
        }

    }
}
