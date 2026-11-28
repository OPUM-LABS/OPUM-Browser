package com.mycompany.app.db.book

import android.content.ContentValues
import com.mycompany.app.pref.store.SyncSettings
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
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSync
import ch.opumlabs.opumbrowser.R
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("deprecation")
class DbBookWeb(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookWeb_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _isdir INTEGER, _dir TEXT, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    /** When each row last changed, for the bookmark sync (2026-11-28, [com.mycompany.app.sync.BookmarkSyncLocal]). */
    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) com.mycompany.app.sync.BookmarkSyncLocal.ensureTracking(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookWeb_table")
        db.execSQL("CREATE TABLE DbBookWeb_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _isdir INTEGER, _dir TEXT, _path TEXT, _title TEXT, _icon BLOB, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookWeb? = null

        @JvmStatic
        fun exportFile(context: Context?, path: String?): File? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookWeb_table",
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
                        && MainUtil.writeIntEntry(bufferedWriter, "_isdir", cursor2.getInt(cursor2.getColumnIndex("_isdir")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_dir", cursor2.getString(cursor2.getColumnIndex("_dir")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeBytesEntry(bufferedWriter, "_icon", cursor2.getBlob(cursor2.getColumnIndex("_icon")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_rsv4", cursor2.getLong(cursor2.getColumnIndex("_rsv4")))
            })
        }

        @JvmStatic
        fun insertItem(
            context: Context?,
            isDir: Boolean,
            type: String?,
            path: String?,
            name: String?,
            time: Long,
            order: Long
        ): ChildItem? {
            var cursorG: Cursor?
            var id: Long
            var newOrder: Long
            val dir: String?
            val childItem: ChildItem
            val host: String?
            if (context != null) {
                try {
                    cursorG = DbUtil.query(
                        get(context).getWritableDatabase(),
                        "DbBookWeb_table",
                        arrayOf<String?>("_id", "_dir", "_title", "_rsv4"),
                        "_secret=? AND _time=?",
                        arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), time.toString()),
                        null
                    )
                    if (cursorG != null) {
                        try {
                            if (cursorG.moveToFirst()) {
                                id = cursorG.getLong(cursorG.getColumnIndex("_id"))
                                try {
                                    newOrder = cursorG.getLong(cursorG.getColumnIndex("_rsv4"))
                                } catch (unused: Exception) {
                                    newOrder = order
                                }
                            } else {
                                newOrder = order
                                id = 0
                            }
                        } catch (unused2: Exception) {
                            id = 0
                            newOrder = order
                        }
                    } else {
                        newOrder = order
                        id = 0
                    }
                } catch (unused3: Exception) {
                    cursorG = null
                    newOrder = order
                    id = 0
                }
                if (cursorG != null) {
                    cursorG.close()
                }
                if (id > 0) {
                    if (TextUtils.isEmpty(type)) {
                        dir = "/"
                    } else {
                        dir = type
                    }
                    childItem = ChildItem()
                    childItem.isDir = isDir
                    childItem.dir = dir
                    childItem.name = name
                    if (isDir) {
                        if (dir == "/") {
                            childItem.path = "/" + childItem.name
                        } else {
                            val sbS = StringBuilder(dir.toString()).append("/")
                            sbS.append(childItem.name)
                            childItem.path = sbS.toString()
                        }
                        childItem.subText = " "
                        childItem.itemType = 0.toByte()
                        childItem.iconColor = 0
                        childItem.iconRes = R.drawable.outline_folder_black_24
                    } else {
                        childItem.path = path
                        host = MainUtil.hostOfUrl(path, false)
                        childItem.subText = host
                        if (TextUtils.isEmpty(host)) {
                            childItem.subText = childItem.path
                        }
                        childItem.filePath = MainUtil.pageUrlOrNull(childItem.path)
                        childItem.itemType = 11.toByte()
                        childItem.iconColor = -460552
                        childItem.iconRes = R.drawable.outline_public_black_24
                    }
                    childItem.id = id
                    childItem.time = time
                    childItem.dateText = MainUtil.formatDate(time, null, "yyyy.MM.dd")
                    childItem.order = newOrder
                    childItem.position = newOrder.toInt()
                    return childItem
                }
            }
            return null
        }

        @JvmStatic
        fun idByPath(context: Context?, menuItemText: String?): Long {
            var id: Long = 0
            if (context != null && !TextUtils.isEmpty(menuItemText)) {
                val columns = arrayOf<String?>("_id")
                val args = arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), menuItemText)
                var cursorG: Cursor? = null
                try {
                    cursorG = DbUtil.query(
                        get(context).getWritableDatabase(),
                        "DbBookWeb_table",
                        columns,
                        "_secret=? AND _path=?",
                        args,
                        null
                    )
                    if (cursorG != null && cursorG.moveToFirst()) {
                        id = cursorG.getLong(cursorG.getColumnIndex("_id"))
                    }
                } catch (unused: Exception) {
                }
                if (cursorG != null) {
                    cursorG.close()
                }
            }
            return id
        }

        @JvmStatic
        fun iconById(context: Context?, id: Long): ByteArray? {
            var cursorF: Cursor?
            var blob: ByteArray? = null
            if (id <= 0) {
                return null
            }
            try {
                cursorF = DbUtil.queryById(
                    get(context).getWritableDatabase(),
                    "DbBookWeb_table",
                    arrayOf<String?>("_icon"),
                    id
                )
                if (cursorF != null) {
                    try {
                        if (cursorF.moveToFirst()) {
                            blob = cursorF.getBlob(cursorF.getColumnIndex("_icon"))
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
            return blob
        }

        @JvmStatic
        fun get(context: Context?): DbBookWeb {
            var dbBookWeb: DbBookWeb?
            val existing: DbBookWeb? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookWeb::class.java) {
                dbBookWeb = cached
                if (dbBookWeb == null) {
                    dbBookWeb = DbBookWeb(MainUtil.appContextOf(context), "DbBookWeb.db", null, 2)
                    cached = dbBookWeb
                }
            }
            return dbBookWeb!!
        }

        @JvmStatic
        fun hasFolder(context: Context?, dir: String?, name: String?): Boolean {
            var found = false
            if (context != null && !TextUtils.isEmpty(name)) {
                val trimmed = MainUtil.trimSlashes(dir)
                val folderPath = if (TextUtils.isEmpty(trimmed)) "/" else "/$trimmed"
                var cursorG: Cursor? = null
                try {
                    cursorG = DbUtil.query(
                        get(context).getWritableDatabase(),
                        "DbBookWeb_table",
                        null,
                        "_secret=? AND _isdir=? AND _dir=? AND _title=?",
                        arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), "1", folderPath, name!!),
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
        fun hasPath(context: Context?, url: String?): Boolean {
            var found = false
            if (context != null && !TextUtils.isEmpty(url)) {
                val args = arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), url)
                var cursorG: Cursor? = null
                try {
                    cursorG = DbUtil.query(
                        get(context).getWritableDatabase(),
                        "DbBookWeb_table",
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
        fun deleteById(context: Context?, id: Long) {
            if (context == null || id <= 0) {
                return
            }
            DbUtil.deleteById(get(context).getWritableDatabase(), "DbBookWeb_table", id)
        }

        @JvmStatic
        fun addFolder(parentId: Long, context: Context?, dir: String?, title: String?, secret: Boolean): ChildItem? {
            if (context == null || TextUtils.isEmpty(title)) {
                return null
            }
            val trimmed = MainUtil.trimSlashes(dir)
            val folderPath = if (TextUtils.isEmpty(trimmed)) "/" else "/$trimmed"
            val now = System.currentTimeMillis()
            val time = if (parentId < 0) now else parentId
            val contentValues = ContentValues()
            contentValues.put("_isdir", 1 as Int?)
            contentValues.put("_dir", folderPath)
            contentValues.put("_title", title)
            contentValues.put("_time", now)
            contentValues.put("_secret", com.mycompany.app.pref.store.Workspaces.sharedNumber())
            contentValues.put("_rsv4", time)
            DbUtil.insert(get(context).getWritableDatabase(), "DbBookWeb_table", contentValues)
            if (secret) {
                return insertItem(context, true, folderPath, null, title, now, time)
            }
            return null
        }

        @JvmStatic
        fun addBookmark(context: Context?, folder: String?, path: String?, title: String?, bitmap: Bitmap?) {
            var folder = folder
            var bitmap = bitmap
            if (context == null || TextUtils.isEmpty(path)) {
                return
            }
            if (TextUtils.isEmpty(folder)) {
                folder = "/"
            }
            val now = System.currentTimeMillis()
            val contentValues = ContentValues()
            contentValues.put("_isdir", 0 as Int?)
            contentValues.put("_dir", folder)
            contentValues.put("_path", path)
            contentValues.put("_title", title)
            contentValues.put("_time", now)
            contentValues.put("_rsv4", now)
            if (MainUtil.isYouTubeUrl(path)) {
                bitmap = BitmapUtil.decodeResource(context.getResources(), R.drawable.baseline_search_youtube)
            }
            if (MainUtil.isBitmapOk(bitmap)) {
                try {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    contentValues.put("_icon", byteArrayOutputStream.toByteArray())
                    byteArrayOutputStream.close()
                } catch (unused: Exception) {
                }
            }
            contentValues.put("_secret", com.mycompany.app.pref.store.Workspaces.sharedNumber())
            DbUtil.insert(get(context).getWritableDatabase(), "DbBookWeb_table", contentValues)
        }

        @JvmStatic
        fun updateById(context: Context?, id: Long, contentValues: ContentValues?) {
            if (context == null || id <= 0) {
                return
            }
            val writableDatabase: SQLiteDatabase? = get(context).getWritableDatabase()
            if (DbUtil.existsById(writableDatabase, "DbBookWeb_table", id) == 1) {
                DbUtil.updateById(writableDatabase, "DbBookWeb_table", contentValues, id)
            }
        }

        @JvmStatic
        fun setIcon(context: Context?, bitmap: Bitmap?, url: String?) {
            if (context == null || TextUtils.isEmpty(url) || !MainUtil.isBitmapOk(bitmap)) {
                return
            }
            val columns = arrayOf<String?>("_icon")
            val args = arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), url)
            var found = false
            var cursorG: Cursor? = null
            try {
                cursorG = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookWeb_table",
                    columns,
                    "_secret=? AND _path=?",
                    args,
                    null
                )
                if (cursorG != null && cursorG.moveToFirst() && cursorG.getBlob(
                        cursorG.getColumnIndex(
                            "_icon"
                        )
                    )?.let { it.size > 0 } == true
                ) {
                    found = true
                }
            } catch (unused: Exception) {
            }
            if (cursorG != null) {
                cursorG.close()
            }
            if (found || !MainUtil.isBitmapOk(bitmap)) {
                return
            }
            try {
                val contentValues = ContentValues()
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                contentValues.put("_icon", byteArrayOutputStream.toByteArray())
                DbUtil.updateWhere(
                    get(context).getWritableDatabase(),
                    "DbBookWeb_table",
                    contentValues,
                    "_secret=? AND _path=?",
                    args
                )
                byteArrayOutputStream.close()
            } catch (unused2: Exception) {
            }
        }

        @JvmStatic
        fun addItem(
            context: Context?,
            dir: String,
            url: String?,
            title: String?,
            bitmap: Bitmap?
        ): ChildItem? {
            var dir = dir
            if (context == null || TextUtils.isEmpty(url)) {
                return null
            }
            if (TextUtils.isEmpty(dir)) {
                dir = "/"
            }
            val type = dir
            val now = System.currentTimeMillis()
            val contentValues = ContentValues()
            contentValues.put("_isdir", 0 as Int?)
            contentValues.put("_path", url)
            contentValues.put("_time", now)
            Companion.editItem(
                context,
                arrayOf<String?>("_icon"),
                arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), url!!),
                contentValues,
                type,
                url,
                title,
                bitmap
            )
            return insertItem(context, false, type, url, title, now, now)
        }

        @JvmStatic
        fun editItem(
            context: Context?,
            columns: Array<String?>?,
            args: Array<String?>?,
            contentValues: ContentValues,
            type: String?,
            url: String?,
            title: String?,
            bitmap: Bitmap?
        ): Long {
            // returns the row id of the saved bookmark (0 when unknown), so the list can scroll to it
            var rowId = 0L
            var change: Char
            if (context != null) {
                var found = false
                var cursorG: Cursor? = null
                try {
                    cursorG = DbUtil.query(
                        get(context).getWritableDatabase(),
                        "DbBookWeb_table",
                        columns,
                        "_secret=? AND _path=?",
                        args,
                        null
                    )
                    if (cursorG == null || !cursorG.moveToFirst()) {
                        change = 2.toChar()
                    } else {
                        val idCol = cursorG.getColumnIndex("_id")
                        if (idCol >= 0) rowId = cursorG.getLong(idCol)
                        try {
                            if (MainUtil.isBitmapOk(bitmap) && cursorG.getBlob(cursorG.getColumnIndex("_icon"))?.let { it.size > 0 } == true
                            ) {
                                found = true
                            }
                        } catch (unused: Exception) {
                        }
                        change = 1.toChar()
                    }
                } catch (unused2: Exception) {
                    change = 0.toChar()
                }
                if (cursorG != null) {
                    cursorG.close()
                }
                if (change.code != 0) {
                    val bitmapD = if (found || !MainUtil.isYouTubeUrl(url)) bitmap else BitmapUtil.decodeResource(
                        context.getResources(),
                        R.drawable.baseline_search_youtube
                    )
                    if (!found && MainUtil.isBitmapOk(bitmapD)) {
                        try {
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmapD!!.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                byteArrayOutputStream
                            )
                            contentValues.put("_icon", byteArrayOutputStream.toByteArray())
                            byteArrayOutputStream.close()
                        } catch (unused3: Exception) {
                        }
                    }
                    if (change.code == 1) {
                        contentValues.put("_dir", type)
                        contentValues.put("_title", title)
                        contentValues.put("_rsv4", System.currentTimeMillis())
                        DbUtil.updateWhere(
                            get(context).getWritableDatabase(),
                            "DbBookWeb_table",
                            contentValues,
                            "_secret=? AND _path=?",
                            args
                        )
                        return rowId
                    }
                    contentValues.put("_dir", type)
                    contentValues.put("_title", title)
                    contentValues.put("_secret", com.mycompany.app.pref.store.Workspaces.sharedNumber())
                    contentValues.put("_rsv4", System.currentTimeMillis())
                    rowId = DbUtil.insert(get(context).getWritableDatabase(), "DbBookWeb_table", contentValues)
                }
            }
            return rowId
        }

        @JvmStatic
        fun setOrder(context: Context?, childItem: ChildItem, index: Int) {
            if (context == null || TextUtils.isEmpty(childItem.path)) {
                return
            }
            val contentValues = ContentValues()
            contentValues.put("_rsv4", index)
            val isDir = childItem.isDir
            val secret = SyncSettings.secretMode.value
            if (isDir) {
                DbUtil.updateWhere(
                    get(context).getWritableDatabase(),
                    "DbBookWeb_table",
                    contentValues,
                    "_secret=? AND _isdir=? AND _dir=? AND _title=?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), "1", childItem.dir, childItem.name)
                )
            } else {
                DbUtil.updateWhere(
                    get(context).getWritableDatabase(),
                    "DbBookWeb_table",
                    contentValues,
                    "_secret=? AND _path=?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), childItem.path)
                )
            }
        }
    }
}
