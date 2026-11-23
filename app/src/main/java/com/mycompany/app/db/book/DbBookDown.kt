package com.mycompany.app.db.book

import android.content.ContentValues
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.SecretSettings
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.mycompany.app.data.DataUtil.addUri
import com.mycompany.app.data.DataUtil.fileKind
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.MainDownSvc
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync

import java.io.File
import kotlin.math.max

@Suppress("deprecation")
class DbBookDown(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookDown_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _status INTEGER, _type INTEGER, _url TEXT, _dir TEXT, _dname TEXT, _path TEXT, _name TEXT, _size INTEGER, _read INTEGER, _time INTEGER, _fast INTEGER, _vcnt1 INTEGER, _vcnt2 INTEGER, _vcnt3 INTEGER, _acnt1 INTEGER, _acnt2 INTEGER, _acnt3 INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookDown_table")
        db.execSQL("CREATE TABLE DbBookDown_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _status INTEGER, _type INTEGER, _url TEXT, _dir TEXT, _dname TEXT, _path TEXT, _name TEXT, _size INTEGER, _read INTEGER, _time INTEGER, _fast INTEGER, _vcnt1 INTEGER, _vcnt2 INTEGER, _vcnt3 INTEGER, _acnt1 INTEGER, _acnt2 INTEGER, _acnt3 INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookDown? = null

        /**
         * Deletes leftover temp files in the ".down" folder that no running (1) or paused (2)
         * download still uses. Nothing is deleted if the table cannot be read.
         */
        @JvmStatic
        fun clearDone(context: Context?) {
            if (context == null) {
                return
            }
            val dir = MainUtil.filesSubdir(context, ".down")
            if (TextUtils.isEmpty(dir)) {
                return
            }
            val files = File(dir!!).listFiles()
            if (files == null || files.size == 0) {
                return
            }
            val inUse = ArrayList<String?>()
            var readOk = true
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(), "DbBookDown_table",
                    arrayOf<String?>("_status", "_url", "_path"), null, null, null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val colStatus = cursor.getColumnIndex("_status")
                    val colUrl = cursor.getColumnIndex("_url")
                    val colPath = cursor.getColumnIndex("_path")
                    do {
                        val status = cursor.getInt(colStatus)
                        if (status != 1 && status != 2) {
                            continue
                        }
                        val temp = MainUtil.downFilePath(context, cursor.getString(colPath))
                        if (TextUtils.isEmpty(temp)) {
                            continue
                        }
                        inUse.add(temp)
                        if (MainDownSvc.urlType(cursor.getString(colUrl)) == 14) {
                            inUse.add(temp + "b")
                        }
                    } while (cursor.moveToNext())
                }
            } catch (unused: Exception) {
                readOk = false
            }
            if (cursor != null) {
                cursor.close()
            }
            if (!readOk) {
                return
            }
            try {
                for (file in files) {
                    if (!inUse.contains(file.getPath())) {
                        MainUtil.deleteRecursive(file)
                    }
                }
            } catch (unused: Exception) {
            }
        }

        @JvmStatic
        fun get(context: Context?): DbBookDown {
            var dbBookDown: DbBookDown?
            val existing: DbBookDown? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookDown::class.java) {
                dbBookDown = cached
                if (dbBookDown == null) {
                    dbBookDown = DbBookDown(MainUtil.appContextOf(context), "DbBookDown.db", null, 6)
                    cached = dbBookDown
                }
            }
            return dbBookDown!!
        }

        @JvmStatic
        fun pathById(context: Context?, longExtra: Long): String? {
            var cursorF: Cursor?
            var path: String? = null
            if (context != null && longExtra > 0) {
                try {
                    cursorF = DbUtil.queryById(
                        get(context).getWritableDatabase(),
                        "DbBookDown_table",
                        arrayOf<String?>("_path"),
                        longExtra
                    )
                    if (cursorF != null) {
                        try {
                            if (cursorF.moveToFirst()) {
                                path = cursorF.getString(cursorF.getColumnIndex("_path"))
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
            return path
        }

        @JvmStatic
        fun urlById(context: Context?, longExtra: Long): String? {
            var cursorF: Cursor?
            var url: String? = null
            if (context != null && longExtra > 0) {
                try {
                    cursorF = DbUtil.queryById(
                        get(context).getWritableDatabase(),
                        "DbBookDown_table",
                        arrayOf<String?>("_url"),
                        longExtra
                    )
                    if (cursorF != null) {
                        try {
                            if (cursorF.moveToFirst()) {
                                url = cursorF.getString(cursorF.getColumnIndex("_url"))
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
            return url
        }

        @JvmStatic
        fun hasPath(context: Context?, uriString: String?, secret: Boolean): Boolean {
            var found = false
            if (context != null && !TextUtils.isEmpty(uriString)) {
                val args = arrayOf<String?>(if (secret) "1" else "0", uriString)
                var cursorG: Cursor? = null
                try {
                    cursorG = DbUtil.query(
                        get(context).getWritableDatabase(),
                        "DbBookDown_table",
                        null,
                        "_secret=? AND _path=?",
                        args,
                        null
                    )
                    if (cursorG != null && cursorG.moveToFirst()) {
                        found = true
                    }
                } catch (unused: Exception) {
                }
                if (cursorG != null) {
                    cursorG.close()
                }
            }
            return found
        }

        @JvmStatic
        fun addFile(context: Context?, path: String?, name: String?, uriItem: UriItem): Long {
            return addFileItem(context, path, name, uriItem, SyncSettings.secretMode.value, SecretSettings.secretDown.value)
        }

        @JvmStatic
        fun addFileItem(
            context: Context?,
            url: String?,
            referer: String?,
            uriItem: UriItem,
            paused: Boolean,
            sessionId: Long
        ): Long {
            if (context == null || TextUtils.isEmpty(uriItem.uriString)) {
                return 0L
            }
            val archiveIcon = MainUtil.archiveIconFor(uriItem.name)
            val count = uriItem.size
            val itemId: Long = insertItem(context, 3, archiveIcon, url, referer, uriItem, count, count, false, paused, sessionId, true)
            val kind: Int = if (uriItem.size == 0L || archiveIcon == 31) -1 else fileKind(uriItem.name)
            if (kind != 1 && kind != 2 && kind != 3) {
                return itemId
            }
            addUri(context, kind, uriItem)
            return itemId
        }

        @JvmStatic
        fun deleteById(context: Context?, id: Long) {
            if (context == null) {
                return
            }
            val contentValues = ContentValues()
            contentValues.put("_status", 3 as Int?)
            contentValues.put("_rsv4", 0 as Int?)
            DbUtil.updateWhere(
                get(context).getWritableDatabase(),
                "DbBookDown_table",
                contentValues,
                "_id=?",
                arrayOf<String?>(id.toString())
            )
        }

        @JvmStatic
        fun deleteFile(id: Long, context: Context?, path: String?, url: String?, withData: Boolean) {
            if (context == null || id <= 0) {
                return
            }
            if (withData && !TextUtils.isEmpty(path)) {
                MainUtil.deleteUri(context, path)
            }
            if (!TextUtils.isEmpty(url)) {
                com.mycompany.app.glide.ImageCache.removeUrl(url)
            }
            DbUtil.deleteById(get(context).getWritableDatabase(), "DbBookDown_table", id)
        }

        @JvmStatic
        fun clearSecretDone(context: Context?) {
            if (context == null) {
                return
            }
            DbUtil.deleteWhere(
                get(context).getWritableDatabase(),
                "DbBookDown_table",
                "_secret=? AND _status>?",
                arrayOf<String?>("1", "2")
            )
        }

        @JvmStatic
        fun setStatus(context: Context?, id: Long, readSize: Long, totalSize: Long, pause: Boolean) {
            if (context == null) {
                return
            }
            val contentValues = ContentValues()
            if (pause) {
                contentValues.put("_status", 2 as Int?)
            } else {
                contentValues.put("_status", 1 as Int?)
            }
            contentValues.put("_rsv4", 0 as Int?)
            if (readSize > 0) {
                contentValues.put("_size", readSize)
            }
            if (totalSize > 0) {
                contentValues.put("_read", totalSize)
            }
            DbUtil.updateWhere(
                get(context).getWritableDatabase(),
                "DbBookDown_table",
                contentValues,
                "_id=?",
                arrayOf<String?>(id.toString())
            )
        }

        @JvmStatic
        fun setSize(context: Context?, id: Long, readSize: Long, oldParts: Int) {
            if (context != null) {
                if (readSize == 0L && oldParts == 0) {
                    return
                }
                val contentValues = ContentValues()
                if (readSize > 0) {
                    contentValues.put("_size", readSize)
                }
                if (oldParts > 0) {
                    contentValues.put("_fast", oldParts)
                }
                DbUtil.updateWhere(
                    get(context).getWritableDatabase(),
                    "DbBookDown_table",
                    contentValues,
                    "_id=?",
                    arrayOf<String?>(id.toString())
                )
            }
        }

        /**
         * Inserts or updates the download row for `uriItem.e` (one row per path and mode).
         *
         * @param status   download status (1 running, 2 paused, 3+ finished/failed)
         * @param type     file type
         * @param url      source url
         * @param extra    stored in _rsv1
         * @param size     total size, `read` bytes done
         * @param flag     stored in _rsv4 for paused downloads only
         * @param secret   secret-mode row
         * @param session  secret session time; finished secret downloads without one are not kept
         * @param wantId   return the row id (otherwise 0)
         */
        @JvmStatic
        fun insertItem(
            context: Context?, status: Int, type: Int, url: String?, extra: String?,
            uriItem: UriItem?, size: Long, read: Long, isReserved: Boolean, secret: Boolean,
            session: Long, wantId: Boolean
        ): Long {
            if (context == null || TextUtils.isEmpty(url) || uriItem == null || TextUtils.isEmpty(
                    uriItem.uriString
                )
            ) {
                return 0
            }
            val db: SQLiteDatabase? = get(context).getWritableDatabase()
            if (secret && session == 0L && status > 2) {
                DbUtil.deleteWhere(
                    db,
                    "DbBookDown_table",
                    "_secret=? AND _path=?",
                    arrayOf<String?>("1", uriItem.uriString)
                )
                return 0
            }
            val now = System.currentTimeMillis()
            val mode = if (secret) "1" else "0"
            val args = arrayOf<String?>(mode, uriItem.uriString)
            var id: Long = 0
            val state = DbUtil.existsWhere(db, "DbBookDown_table", null, "_secret=? AND _path=?", args)
            if (state != 0) {
                val values = ContentValues()
                values.put("_status", status)
                values.put("_type", type)
                values.put("_url", url)
                values.put("_rsv1", extra)
                values.put("_dir", uriItem.dirPath)
                values.put("_dname", uriItem.dirName)
                values.put("_path", uriItem.uriString)
                values.put("_name", uriItem.name)
                values.put("_size", size)
                values.put("_read", read)
                values.put("_time", now)
                values.put("_rsv4", if (status == 2 && isReserved) 1 else 0)
                if (state == 1) {
                    DbUtil.updateWhere(db, "DbBookDown_table", values, "_secret=? AND _path=?", args)
                } else {
                    values.put("_secret", if (secret) 1 else 0)
                    id = DbUtil.insert(db, "DbBookDown_table", values)
                }
            }
            if (!wantId) {
                return 0
            }
            if (id > 0) {
                return id
            }
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    db, "DbBookDown_table", arrayOf<String?>("_id"), "_secret=? AND _time=?",
                    arrayOf<String?>(mode, now.toString()), null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    id = cursor.getLong(cursor.getColumnIndex("_id"))
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
            return max(id, 0)
        }
    }
}
