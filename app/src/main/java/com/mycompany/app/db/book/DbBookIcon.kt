package com.mycompany.app.db.book

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.text.TextUtils
import com.mycompany.app.db.DbExport
import com.mycompany.app.db.DbExport.RowWriter
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.BitmapUtil
import com.mycompany.app.main.MainUtil
import ch.opumlabs.opumbrowser.R
import java.io.BufferedWriter
import java.io.File

@Suppress("deprecation")
class DbBookIcon(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookIcon_table (_id INTEGER PRIMARY KEY, _host TEXT, _icon BLOB, _time INTEGER, _count INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookIcon_table")
        db.execSQL("CREATE TABLE DbBookIcon_table (_id INTEGER PRIMARY KEY, _host TEXT, _icon BLOB, _time INTEGER, _count INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookIcon? = null

        @JvmStatic
        fun exportFile(context: Context?, path: String?): File? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookIcon_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return DbExport.exportCsv(cursor, path, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter)
                        && MainUtil.writeStringEntry(bufferedWriter, "_host", cursor2!!.getString(cursor2.getColumnIndex("_host")))
                        && MainUtil.writeBytesEntry(bufferedWriter, "_icon", cursor2.getBlob(cursor2.getColumnIndex("_icon")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
                        && MainUtil.writeIntEntry(bufferedWriter, "_count", cursor2.getInt(cursor2.getColumnIndex("_count")))
            })
        }

        /** Cached favicon for a host (YouTube search uses a built-in icon).  */
        @JvmStatic
        fun iconOf(context: Context?, path: String?): Bitmap? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            if (MainUtil.isYouTubeHost(path)) {
                return BitmapUtil.decodeResource(context.getResources(), R.drawable.baseline_search_youtube)
            }
            var cursor: Cursor? = null
            var blob: ByteArray? = null
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookIcon_table",
                    arrayOf<String?>("_icon"),
                    "_host=?",
                    arrayOf<String?>(path!!),
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    blob = cursor.getBlob(cursor.getColumnIndex("_icon"))
                }
            } catch (unused: Exception) {
                blob = null
            }
            if (cursor != null) {
                cursor.close()
            }
            if (blob == null || blob.size == 0) {
                return null
            }
            return BitmapUtil.decodeBytes(blob, blob.size)
        }

        @JvmStatic
        fun get(context: Context?): DbBookIcon {
            var dbBookIcon: DbBookIcon?
            val existing: DbBookIcon? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookIcon::class.java) {
                dbBookIcon = cached
                if (dbBookIcon == null) {
                    dbBookIcon = DbBookIcon(MainUtil.appContextOf(context), "DbBookIcon.db", null, 1)
                    cached = dbBookIcon
                }
            }
            return dbBookIcon!!
        }
    }
}
