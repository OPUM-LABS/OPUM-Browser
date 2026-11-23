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
class DbBookPdf(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookPdf_table (_id INTEGER PRIMARY KEY, _path TEXT, _name TEXT, _count INTEGER, _index INTEGER, _page INTEGER, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookPdf_table")
        db.execSQL("CREATE TABLE DbBookPdf_table (_id INTEGER PRIMARY KEY, _path TEXT, _name TEXT, _count INTEGER, _index INTEGER, _page INTEGER, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookPdf? = null

        @JvmStatic
        fun get(context: Context?): DbBookPdf {
            var dbBookPdf: DbBookPdf?
            val existing: DbBookPdf? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookPdf::class.java) {
                dbBookPdf = cached
                if (dbBookPdf == null) {
                    dbBookPdf = DbBookPdf(MainUtil.appContextOf(context), "DbBookPdf.db", null, 2)
                    cached = dbBookPdf
                }
            }
            return dbBookPdf!!
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
                "DbBookPdf_table",
                path,
                name,
                total,
                totalValue,
                page,
                bitmap,
                R.drawable.outline_picture_as_pdf_black_24,
                false
            )
        }
    }
}
