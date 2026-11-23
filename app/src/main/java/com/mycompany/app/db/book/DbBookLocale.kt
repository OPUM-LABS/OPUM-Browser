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
class DbBookLocale(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookLocale_table (_id INTEGER PRIMARY KEY, _code TEXT, _index INTEGER, _name TEXT, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookLocale_table")
        db.execSQL("CREATE TABLE DbBookLocale_table (_id INTEGER PRIMARY KEY, _code TEXT, _index INTEGER, _name TEXT, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookLocale? = null

        @JvmStatic
        fun get(context: Context?): DbBookLocale {
            var dbBookLocale: DbBookLocale?
            val existing: DbBookLocale? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookLocale::class.java) {
                dbBookLocale = cached
                if (dbBookLocale == null) {
                    dbBookLocale = DbBookLocale(MainUtil.appContextOf(context), "DbBookLocale.db", null, 1)
                    cached = dbBookLocale
                }
            }
            return dbBookLocale!!
        }

        @JvmStatic
        fun tagsOf(context: Context?, localeTag: String?): ArrayList<String?>? {
            var cursorG: Cursor? = null
            if (context == null || TextUtils.isEmpty(localeTag)) {
                return null
            }
            val columns = arrayOf<String?>("_name")
            val args = arrayOf<String?>(localeTag)
            val arrayList: ArrayList<String?> = ArrayList()
            try {
                cursorG = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookLocale_table",
                    columns,
                    "_code=?",
                    args,
                    "_index ASC"
                )
                if (cursorG != null && cursorG.moveToFirst()) {
                    val columnIndex = cursorG.getColumnIndex("_name")
                    do {
                        val tag = cursorG.getString(columnIndex)
                        if (!TextUtils.isEmpty(tag)) {
                            arrayList.add(tag)
                        }
                    } while (cursorG.moveToNext())
                }
            } catch (unused: Exception) {
            }
            if (cursorG != null) {
                cursorG.close()
            }
            return arrayList
        }
    }
}
