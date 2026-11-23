package com.mycompany.app.db.book

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.mycompany.app.db.DbSite
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import ch.opumlabs.opumbrowser.R

@Suppress("deprecation")
class DbBookBlock(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookBlock_table (_id INTEGER PRIMARY KEY, _type INTEGER, _path TEXT, _image TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookBlock_table")
        db.execSQL("CREATE TABLE DbBookBlock_table (_id INTEGER PRIMARY KEY, _type INTEGER, _path TEXT, _image TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookBlock? = null

        @JvmStatic
        fun get(context: Context?): DbBookBlock {
            var dbBookBlock: DbBookBlock?
            val existing: DbBookBlock? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookBlock::class.java) {
                dbBookBlock = cached
                if (dbBookBlock == null) {
                    dbBookBlock = DbBookBlock(MainUtil.appContextOf(context), "DbBookBlock.db", null, 2)
                    cached = dbBookBlock
                }
            }
            return dbBookBlock!!
        }

        /** Blocks image `str2` on page `str` (or refreshes the entry) and returns it as a list item.  */
        @JvmStatic
        fun addPath(context: Context?, path: String?, name: String?): ChildItem? {
            if (context == null || TextUtils.isEmpty(path) || TextUtils.isEmpty(name)) {
                return null
            }
            val now = System.currentTimeMillis()
            val args = arrayOf<String?>(path, name)
            val db: SQLiteDatabase? = get(context).getWritableDatabase()
            val state = DbUtil.existsWhere(db, "DbBookBlock_table", null, "_path=? AND _image=?", args)
            if (state != 0) {
                val values = ContentValues()
                values.put("_type", 0)
                values.put("_path", path)
                values.put("_image", name)
                values.put("_time", now)
                if (state == 1) {
                    DbUtil.updateWhere(db, "DbBookBlock_table", values, "_path=? AND _image=?", args)
                } else {
                    DbUtil.insert(db, "DbBookBlock_table", values)
                }
            }
            val id = DbSite.findId(db, "DbBookBlock_table", "_path=? AND _image=?", args)
            if (id <= 0) {
                return null
            }
            val item = ChildItem()
            item.id = id
            item.path = path
            item.name = name
            item.time = now
            item.itemType = 0.toByte()
            item.iconColor = -460552
            item.iconRes = R.drawable.outline_public_black_24
            item.dateText = MainUtil.formatDate(now, null, "yyyy.MM.dd")
            item.subText = MainUtil.pathOfUrl(path, false)
            return item
        }
    }
}
