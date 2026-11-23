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
import java.io.BufferedWriter
import java.io.File

@Suppress("deprecation")
class DbTabThumb(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbTabThumb_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _uid INTEGER, _tkey TEXT, _thumb BLOB, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbTabThumb_table")
        db.execSQL("CREATE TABLE DbTabThumb_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _uid INTEGER, _tkey TEXT, _thumb BLOB, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbTabThumb? = null

        @JvmStatic
        fun exportFile(context: Context?, path: String?): File? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbTabThumb_table",
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
                        && MainUtil.writeIntEntry(bufferedWriter, "_secret", cursor2!!.getInt(cursor2.getColumnIndex("_secret")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_uid", cursor2.getLong(cursor2.getColumnIndex("_uid")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_tkey", cursor2.getString(cursor2.getColumnIndex("_tkey")))
                        && MainUtil.writeBytesEntry(bufferedWriter, "_thumb", cursor2.getBlob(cursor2.getColumnIndex("_thumb")))
            })
        }

        @JvmStatic
        fun get(context: Context?): DbTabThumb {
            var dbTabThumb: DbTabThumb?
            val existing: DbTabThumb? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbTabThumb::class.java) {
                dbTabThumb = cached
                if (dbTabThumb == null) {
                    dbTabThumb = DbTabThumb(MainUtil.appContextOf(context), "DbTabThumb.db", null, 1)
                    cached = dbTabThumb
                }
            }
            return dbTabThumb!!
        }

        /** Stored thumbnail of the tab with id `id`.  */
        @JvmStatic
        fun thumbById(context: Context?, id: Long): Bitmap? {
            if (context == null || id <= 0) {
                return null
            }
            var cursor: Cursor? = null
            var blob: ByteArray? = null
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbTabThumb_table",
                    arrayOf<String?>("_thumb"),
                    "_uid=?",
                    arrayOf<String?>(id.toString()),
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    blob = cursor.getBlob(cursor.getColumnIndex("_thumb"))
                }
            } catch (unused: Exception) {
                blob = null
            }
            if (cursor != null) {
                cursor.close()
            }
            return if (blob != null && blob.size > 0) BitmapUtil.decodeBytes(blob, blob.size) else null
        }

        /**
         * The stored snapshot of tab [id] as it is in the database (lossy WebP, or PNG for older rows) with the cache
         * key it was taken under (`url + deskMode + secret + "_2"`); null if none.
         */
        @JvmStatic
        fun thumbBytes(context: Context?, id: Long): Pair<ByteArray, String?>? {
            if (context == null || id <= 0) return null
            return try {
                DbUtil.query(get(context).getWritableDatabase(), "DbTabThumb_table", arrayOf<String?>("_thumb", "_tkey"), "_uid=?", arrayOf<String?>(id.toString()), null)?.use { cursor ->
                    if (!cursor.moveToFirst()) return@use null
                    val blob = cursor.getBlob(0)
                    if (blob == null || blob.isEmpty()) null else blob to cursor.getString(1)
                }
            } catch (_: Exception) {
                null
            }
        }

        @JvmStatic
        fun deleteWhere(context: Context?, selection: String?, args: Array<String?>?) {
            if (context != null) {
                DbUtil.deleteWhere(get(context).getWritableDatabase(), "DbTabThumb_table", selection, args)
            }
        }
    }
}
