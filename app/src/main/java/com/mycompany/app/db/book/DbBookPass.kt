package com.mycompany.app.db.book

import android.content.Context
import com.mycompany.app.pref.store.SyncSettings
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
class DbBookPass(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookPass_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _path TEXT, _icon BLOB, _user_id TEXT, _user_name TEXT, _user_val TEXT, _pass_id TEXT, _pass_name TEXT, _pass_val TEXT, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookPass_table")
        db.execSQL("CREATE TABLE DbBookPass_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _path TEXT, _icon BLOB, _user_id TEXT, _user_name TEXT, _user_val TEXT, _pass_id TEXT, _pass_name TEXT, _pass_val TEXT, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookPass? = null

        @JvmStatic
        fun exportFile(context: Context?, path: String?): File? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookPass_table",
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
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeBytesEntry(bufferedWriter, "_icon", cursor2.getBlob(cursor2.getColumnIndex("_icon")))
                        && MainUtil.writeStringEntry(
                    bufferedWriter,
                    "_user_val",
                    cursor2.getString(cursor2.getColumnIndex("_user_val"))
                )
                        && MainUtil.writeStringEntry(
                    bufferedWriter,
                    "_pass_val",
                    cursor2.getString(cursor2.getColumnIndex("_pass_val"))
                )
            })
        }

        /** Stored site icon for a saved password entry.  */
        @JvmStatic
        fun iconOf(context: Context?, path: String?): Bitmap? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            var cursor: Cursor? = null
            var blob: ByteArray? = null
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookPass_table",
                    arrayOf<String?>("_icon"),
                    "_secret=? AND _path=?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey(), path!!),
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
            return if (blob != null && blob.size > 0) BitmapUtil.decodeBytes(blob, blob.size) else null
        }

        @JvmStatic
        fun get(context: Context?): DbBookPass {
            var dbBookPass: DbBookPass?
            val existing: DbBookPass? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookPass::class.java) {
                dbBookPass = cached
                if (dbBookPass == null) {
                    dbBookPass = DbBookPass(MainUtil.appContextOf(context), "DbBookPass.db", null, 2)
                    cached = dbBookPass
                }
            }
            return dbBookPass!!
        }

        @JvmStatic
        fun itemsForHost(context: Context?, pageHost: String?): ArrayList<ChildItem?>? {
            var cursorG: Cursor?
            var arrayList: ArrayList<ChildItem?>? = null
            if (context != null && !TextUtils.isEmpty(pageHost)) {
                try {
                    cursorG = DbUtil.query(
                        get(context).getWritableDatabase(),
                        "DbBookPass_table",
                        null,
                        "_secret=? AND _path=?",
                        arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey(), pageHost!!),
                        null
                    )
                    if (cursorG != null) {
                        try {
                            if (cursorG.moveToFirst()) {
                                val columnIndex = cursorG.getColumnIndex("_user_val")
                                val passValCol = cursorG.getColumnIndex("_pass_val")
                                val items: ArrayList<ChildItem?> = ArrayList()
                                do {
                                    try {
                                        val childItem = ChildItem()
                                        childItem.url = cursorG.getString(columnIndex)
                                        childItem.subText = cursorG.getString(passValCol)
                                        items.add(childItem)
                                    } catch (unused: Exception) {
                                    }
                                } while (cursorG.moveToNext())
                                arrayList = items
                            }
                        } catch (unused2: Exception) {
                        }
                    }
                } catch (unused3: Exception) {
                    cursorG = null
                }
                if (cursorG != null) {
                    cursorG.close()
                }
            }
            return arrayList
        }

        @JvmStatic
        fun insertItem(
            context: Context?,
            host: String?,
            bitmap: Bitmap,
            userName: String?,
            pass: String?,
            url: String?
        ): Long {
            var url = url
            if (context == null || TextUtils.isEmpty(host) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(
                    pass
                )
            ) {
                return 0L
            }
            if (TextUtils.isEmpty(url)) {
                url = "https://$host"
            }
            val contentValuesE = android.content.ContentValues().also { cv -> cv.put("_path", host); cv.put("_user_val", userName) }
            contentValuesE.put("_pass_val", pass)
            contentValuesE.put("_rsv1", url)
            if (MainUtil.isBitmapOk(bitmap)) {
                try {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    contentValuesE.put("_icon", byteArrayOutputStream.toByteArray())
                    byteArrayOutputStream.close()
                } catch (unused: Exception) {
                }
            }
            contentValuesE.put("_secret", com.mycompany.app.pref.store.Workspaces.dataNumber())
            return DbUtil.insert(get(context).getWritableDatabase(), "DbBookPass_table", contentValuesE)
        }

        @JvmStatic
        fun saveItem(
            context: Context?,
            siteUrl: String?,
            bitmap: Bitmap,
            saveName: String?,
            saveWord: String?,
            siteKey: String?
        ) {
            var bitmap = bitmap
            var siteKey = siteKey
            var change: Char
            if (context == null || TextUtils.isEmpty(siteUrl) || TextUtils.isEmpty(saveName) || TextUtils.isEmpty(
                    saveWord
                )
            ) {
                return
            }
            if (TextUtils.isEmpty(siteKey)) {
                siteKey = "https://$siteUrl"
            }
            val columns = arrayOf<String?>("_icon")
            val args = arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey(), siteUrl, saveName)
            if (MainUtil.isYouTubeUrl(siteUrl)) {
                bitmap = BitmapUtil.decodeResource(context.getResources(), R.drawable.baseline_search_youtube)!!
            } else if (!MainUtil.isBitmapOk(bitmap)) {
                bitmap = MainUtil.cachedIcon(MainUtil.shortHostOf(siteUrl))!!
            }
            var found = false
            var cursorG: Cursor? = null
            try {
                cursorG = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookPass_table",
                    columns,
                    "_secret=? AND _path=? AND _user_val=?",
                    args,
                    null
                )
                if (cursorG == null || !cursorG.moveToFirst()) {
                    change = 2.toChar()
                } else {
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
                val contentValuesE = android.content.ContentValues().also { cv -> cv.put("_path", siteUrl); cv.put("_user_val", saveName) }
                contentValuesE.put("_pass_val", saveWord)
                contentValuesE.put("_rsv1", siteKey)
                if (!found && MainUtil.isBitmapOk(bitmap)) {
                    try {
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                        contentValuesE.put("_icon", byteArrayOutputStream.toByteArray())
                        byteArrayOutputStream.close()
                    } catch (unused3: Exception) {
                    }
                }
                if (change.code == 1) {
                    DbUtil.updateWhere(
                        get(context).getWritableDatabase(),
                        "DbBookPass_table",
                        contentValuesE,
                        "_secret=? AND _path=? AND _user_val=?",
                        args
                    )
                } else {
                    contentValuesE.put("_secret", com.mycompany.app.pref.store.Workspaces.dataNumber())
                    DbUtil.insert(get(context).getWritableDatabase(), "DbBookPass_table", contentValuesE)
                }
            }
        }
    }
}
