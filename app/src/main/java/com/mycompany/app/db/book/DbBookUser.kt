package com.mycompany.app.db.book

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.mycompany.app.db.DbExport
import com.mycompany.app.db.DbExport.RowWriter
import com.mycompany.app.db.DbUtil
import com.mycompany.app.list.book.ListBookUser
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.web.WebClean
import java.io.BufferedWriter
import java.io.File

@Suppress("deprecation")
class DbBookUser(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookUser_table (_id INTEGER PRIMARY KEY, _path TEXT, _time INTEGER, _use INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookUser_table")
        db.execSQL("CREATE TABLE DbBookUser_table (_id INTEGER PRIMARY KEY, _path TEXT, _time INTEGER, _use INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookUser? = null

        @JvmStatic
        fun exportFile(context: Context?, path: String?): File? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookUser_table",
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
                        && MainUtil.writeStringEntry(bufferedWriter, "_rsv1", cursor2!!.getString(cursor2.getColumnIndex("_rsv1")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
                        && MainUtil.writeIntEntry(bufferedWriter, "_use", cursor2.getInt(cursor2.getColumnIndex("_use")))
            })
        }

        @JvmStatic
        fun get(context: Context?): DbBookUser {
            var dbBookUser: DbBookUser?
            val existing: DbBookUser? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookUser::class.java) {
                dbBookUser = cached
                if (dbBookUser == null) {
                    dbBookUser = DbBookUser(MainUtil.appContextOf(context), "DbBookUser.db", null, 1)
                    cached = dbBookUser
                }
            }
            return dbBookUser!!
        }

        @JvmStatic
        fun hostOfRule(path: String?): String? {
            if (path == null || path.length == 0) {
                return null
            }
            val length = path.length
            val hashIndex = path.indexOf(35.toChar())
            return if (hashIndex <= 0 || hashIndex >= length) "noname" else path.substring(0, hashIndex)
        }

        /**
         * Saves user rule `str2` for host `str`: updates row `ruleId` if it exists, otherwise
         * inserts an enabled rule. Returns the saved row as a list item.
         */
        @JvmStatic
        fun saveRule(context: Context?, ruleId: Long, host: String?, ruleText: String?): ChildItem? {
            var ruleId = ruleId
            if (context == null || TextUtils.isEmpty(ruleText)) {
                return null
            }
            val db: SQLiteDatabase? = get(context).getWritableDatabase()
            val state = DbUtil.existsById(db, "DbBookUser_table", ruleId)
            if (state != 0) {
                val values = ContentValues()
                values.put("_rsv1", host)
                values.put("_path", ruleText)
                if (state == 1) {
                    DbUtil.updateById(db, "DbBookUser_table", values, ruleId)
                } else {
                    values.put("_time", System.currentTimeMillis())
                    values.put("_use", 1)
                    ruleId = DbUtil.insert(db, "DbBookUser_table", values)
                }
            }
            if (ruleId == 0L) {
                return null
            }
            var item: ChildItem? = null
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.queryById(db, "DbBookUser_table", null, ruleId)
                if (cursor != null && cursor.moveToFirst()) {
                    val path = cursor.getString(cursor.getColumnIndex("_path"))
                    if (!TextUtils.isEmpty(path)) {
                        item = ChildItem()
                        item.id = cursor.getLong(cursor.getColumnIndex("_id"))
                        item.path = path
                        item.name = path
                        item.time = cursor.getLong(cursor.getColumnIndex("_time"))
                        item.isEnabled = cursor.getInt(cursor.getColumnIndex("_use")) == 1
                        item.itemType = 0.toByte()
                        item.iconColor = -460552
                        item.iconRes = R.drawable.outline_verified_user_black_24
                        item.subText = hostOfRule(path)
                        ListBookUser.applyFold(item)
                    }
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
            return item
        }

        @JvmStatic
        fun setUse(context: Context?, id: Long, on: Boolean) {
            if (context == null) {
                return
            }
            val contentValues = ContentValues()
            contentValues.put("_use", if (on) 1 else 0)
            DbUtil.updateWhere(
                get(context).getWritableDatabase(),
                "DbBookUser_table",
                contentValues,
                "_id=?",
                arrayOf<String?>(id.toString())
            )
        }
    }
}
