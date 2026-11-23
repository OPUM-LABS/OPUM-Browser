package com.mycompany.app.db.book

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import com.mycompany.app.db.DbSite
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import ch.opumlabs.opumbrowser.R

@Suppress("deprecation")
class DbBookAlbum(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookAlbum_table (_id INTEGER PRIMARY KEY, _path TEXT, _name TEXT, _count INTEGER, _index INTEGER, _page INTEGER, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookAlbum_table")
        db.execSQL("CREATE TABLE DbBookAlbum_table (_id INTEGER PRIMARY KEY, _path TEXT, _name TEXT, _count INTEGER, _index INTEGER, _page INTEGER, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookAlbum? = null

        @JvmStatic
        fun get(context: Context?): DbBookAlbum {
            var dbBookAlbum: DbBookAlbum?
            val existing: DbBookAlbum? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookAlbum::class.java) {
                dbBookAlbum = cached
                if (dbBookAlbum == null) {
                    dbBookAlbum = DbBookAlbum(MainUtil.appContextOf(context), "DbBookAlbum.db", null, 2)
                    cached = dbBookAlbum
                }
            }
            return dbBookAlbum!!
        }

        /** Saves the reading position (count/index/page and thumbnail) for a file.  */
        @JvmStatic
        fun addItem(
            context: Context?,
            path: String?,
            name: String?,
            total: Int,
            totalValue: Int,
            page: Int,
            bitmap: Bitmap?
        ): ChildItem? {
            if (context == null) {
                return null
            }
            return DbSite.addFile(
                get(context).getWritableDatabase(),
                "DbBookAlbum_table",
                path,
                name,
                total,
                totalValue,
                page,
                bitmap,
                R.drawable.outline_local_library_black_24,
                true
            )
        }
    }
}
