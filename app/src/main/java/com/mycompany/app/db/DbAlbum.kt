package com.mycompany.app.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.mycompany.app.data.DataAlbum
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUtil

import java.io.File

@Suppress("deprecation")
class DbAlbum(context: Context?, nameParam: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, nameParam, factory, version) {

    internal class ItemForTask(@JvmField val context: Context?,
                                   @JvmField val path: String?,
                                   @JvmField val size: Long,
                                   @JvmField val time: Long,
                                   @JvmField val count: Int
    ) : Runnable {
        override fun run() {
            val dbAlbum: DbAlbum? = Companion.cached
            val path = this.path
            if (TextUtils.isEmpty(path)) {
                return
            }
            val context = this.context
            val childItemG = DataAlbum.instance(context).itemFor(path)
            if (childItemG != null) {
                val sizeNow = this.size
                childItemG.count = sizeNow.toInt()
                val time = this.time
                childItemG.index = time.toInt()
                val count = this.count
                childItemG.page = count
                val args = arrayOf<String?>(path)
                val writableDatabase: SQLiteDatabase? = get(context).getWritableDatabase()
                val exists = DbUtil.existsWhere(writableDatabase, "DbAlbum_table", null, "_path=?", args)
                if (exists != 0) {
                    if (exists == 1) {
                        val contentValues = ContentValues()
                        contentValues.put("_count", childItemG.count)
                        contentValues.put("_index", childItemG.index)
                        contentValues.put("_page", childItemG.page)
                        DbUtil.updateWhere(
                            writableDatabase,
                            "DbAlbum_table",
                            contentValues,
                            "_path=?",
                            args
                        )
                        return
                    }
                    val uriItemJ = MainUri.createIn(context, path, null)
                    if (uriItemJ != null) {
                        val contentValues = ContentValues()
                        contentValues.put("_dir", uriItemJ.dirPath)
                        contentValues.put("_dname", uriItemJ.dirName)
                        contentValues.put("_path", path)
                        contentValues.put("_name", uriItemJ.name)
                        contentValues.put("_time", uriItemJ.time)
                        contentValues.put("_size", uriItemJ.size)
                        contentValues.put("_icon", MainUtil.cacheFilePathFor(context, path))
                        contentValues.put("_count", sizeNow)
                        contentValues.put("_index", time)
                        contentValues.put("_page", count)
                        DbUtil.insert(writableDatabase, "DbAlbum_table", contentValues)
                    }
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbAlbum_table (_id INTEGER PRIMARY KEY, _dir TEXT, _dname TEXT, _path TEXT, _name TEXT, _time INTEGER, _size INTEGER, _icon TEXT, _count INTEGER, _index INTEGER, _page INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbAlbum_table")
        db.execSQL("CREATE TABLE DbAlbum_table (_id INTEGER PRIMARY KEY, _dir TEXT, _dname TEXT, _path TEXT, _name TEXT, _time INTEGER, _size INTEGER, _icon TEXT, _count INTEGER, _index INTEGER, _page INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbAlbum? = null

        @JvmStatic
        fun get(context: Context?): DbAlbum {
            var dbAlbum: DbAlbum?
            val existing: DbAlbum? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbAlbum::class.java) {
                dbAlbum = cached
                if (dbAlbum == null) {
                    dbAlbum = DbAlbum(MainUtil.appContextOf(context), "DbAlbum.db", null, 2)
                    cached = dbAlbum
                }
            }
            return dbAlbum!!
        }

        @JvmStatic
        fun addItem(context: Context?, uriItem: UriItem?) {
            if (context == null || uriItem == null || TextUtils.isEmpty(uriItem.uriString)) {
                return
            }
            val args = arrayOf<String?>(uriItem.uriString)
            var cursorG: Cursor? = null
            try {
                val writableDatabase: SQLiteDatabase? = get(context).getWritableDatabase()
                cursorG = DbUtil.query(writableDatabase, "DbAlbum_table", null, "_path=?", args, null)
                if (cursorG == null || !cursorG.moveToFirst()) {
                    if (uriItem.time == 0L) {
                        uriItem.time = System.currentTimeMillis()
                    }
                    if (uriItem.size == 0L) {
                        uriItem.size = MainUtil.fileSizeOf(context, uriItem.uriString)
                    }
                    val contentValues = ContentValues()
                    contentValues.put("_dir", uriItem.dirPath)
                    contentValues.put("_dname", uriItem.dirName)
                    contentValues.put("_path", uriItem.uriString)
                    contentValues.put("_name", uriItem.name)
                    contentValues.put("_time", uriItem.time)
                    contentValues.put("_size", uriItem.size)
                    contentValues.put("_icon", MainUtil.cacheFilePathFor(context, uriItem.uriString))
                    contentValues.put("_count", 0 as Int?)
                    contentValues.put("_index", 0 as Int?)
                    contentValues.put("_page", 0 as Int?)
                    DbUtil.insert(writableDatabase, "DbAlbum_table", contentValues)
                }
            } catch (unused: Exception) {
            }
            if (cursorG != null) {
                cursorG.close()
            }
        }

        @JvmStatic
        fun removePath(context: Context?, url: String?) {
            if (context == null || TextUtils.isEmpty(url)) {
                return
            }
            com.mycompany.app.glide.ImageCache.removeUrl(url)
            val cacheFilePath = MainUtil.cacheFilePathFor(context, url)
            if (!TextUtils.isEmpty(cacheFilePath)) {
                File(cacheFilePath!!).delete()
            }
            DbUtil.deleteWhere(
                get(context).getWritableDatabase(),
                "DbAlbum_table",
                "_path=?",
                arrayOf<String?>(url)
            )
        }

        @JvmStatic
        fun updateAsync(context: Context?, path: String?, size: Long, time: Long, count: Int) {
            if (TextUtils.isEmpty(path)) {
                return
            }
            val contextQ: Context? = MainUtil.appContextOf(context)
            if (contextQ == null) {
                return
            }
            MainApp.runInBackground(contextQ, ItemForTask(contextQ, path, size, time, count))
        }
    }
}
