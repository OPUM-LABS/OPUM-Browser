package com.mycompany.app.db.book

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ZtwoSettings
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
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefZtwo
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.web.WebSearch
import com.mycompany.app.web.WebSearch.WebSchItem

import java.io.BufferedWriter
import java.io.File

@Suppress("deprecation")
class DbBookSearch(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookSearch_table (_id INTEGER PRIMARY KEY, _title TEXT, _text TEXT, _icon BLOB, _color INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookSearch_table")
        db.execSQL("CREATE TABLE DbBookSearch_table (_id INTEGER PRIMARY KEY, _title TEXT, _text TEXT, _icon BLOB, _color INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookSearch? = null
        @JvmField
        var colorIndex: Int = 0

        @JvmStatic
        fun exportFile(context: Context?, path: String?): File? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookSearch_table",
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
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2!!.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_text", cursor2.getString(cursor2.getColumnIndex("_text")))
                        && MainUtil.writeBytesEntry(bufferedWriter, "_icon", cursor2.getBlob(cursor2.getColumnIndex("_icon")))
                        && MainUtil.writeIntEntry(bufferedWriter, "_color", cursor2.getInt(cursor2.getColumnIndex("_color")))
            })
        }

        @JvmStatic
        fun cachedIcon(url: String?): Bitmap? {
            if (TextUtils.isEmpty(url)) {
                return null
            }
            return com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(2, url))
        }

        @JvmStatic
        fun iconById(context: Context?, id: Long): Bitmap? {
            if (context == null || id <= 0) {
                return null
            }
            var blob: ByteArray? = null
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.queryById(
                    get(context).getWritableDatabase(),
                    "DbBookSearch_table",
                    arrayOf<String?>("_icon"),
                    id
                )
                if (cursor != null && cursor.moveToFirst()) {
                    blob = cursor.getBlob(cursor.getColumnIndex("_icon"))
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
            if (blob == null || blob.size <= 1) {
                return null
            }
            return BitmapUtil.decodeBytes(blob, blob.size)
        }

        @JvmStatic
        fun iconRes(color: Int): Int {
            val length = MainConst.iconColors.size
            for (index in 0..<length) {
                if (color == MainConst.iconColors[index]) {
                    return if (color == -16777216 && AppState.darkUi) R.drawable.baseline_search_15_dark_24 else MainConst.searchIcons[index]
                }
            }
            return MainConst.searchIcons[0]
        }

        @JvmStatic
        fun get(context: Context?): DbBookSearch {
            var dbBookSearch: DbBookSearch?
            val existing: DbBookSearch? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookSearch::class.java) {
                dbBookSearch = cached
                if (dbBookSearch == null) {
                    dbBookSearch = DbBookSearch(MainUtil.appContextOf(context), "DbBookSearch.db", null, 1)
                    if (colorIndex == 0) {
                        colorIndex = MainUtil.randomBetween(0, MainConst.iconColors.size)
                    }
                    cached = dbBookSearch
                }
            }
            return dbBookSearch!!
        }

        @JvmStatic
        fun engines(context: Context?): MutableList<WebSchItem?>? {
            if (context == null) {
                return null
            }
            val cached = WebSearch.instance().engines
            if (cached != null && cached.size > 0) {
                return cached
            }
            // Custom engines; also re-checks the selected custom engine (index >= 10 is item id + 100).
            var index = 4
            var url: String? = null
            var color = 0
            var list: ArrayList<WebSchItem?>? = null
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookSearch_table",
                    null,
                    null,
                    null,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val idCol = cursor.getColumnIndex("_id")
                    val titleCol = cursor.getColumnIndex("_title")
                    val textCol = cursor.getColumnIndex("_text")
                    val colorCol = cursor.getColumnIndex("_color")
                    list = ArrayList()
                    do {
                        val engineText = cursor.getString(textCol)
                        if (!TextUtils.isEmpty(engineText)) {
                            val item = WebSchItem()
                            item.id = cursor.getLong(idCol)
                            item.name = cursor.getString(titleCol)
                            item.engineUrl = engineText
                            item.color = cursor.getInt(colorCol)
                            list.add(item)
                            if (ZtwoSettings.searchType.value.toLong() == item.id + 100) {
                                url = item.engineUrl
                                color = item.color
                                index = ZtwoSettings.searchType.value
                            }
                        }
                    } while (cursor.moveToNext())
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
            if (ZtwoSettings.searchType.value >= 10 && (ZtwoSettings.searchType.value != index || ZtwoSettings.searchColor.value != color || !MainUtil.sameObject(
                    ZtwoSettings.searchUser.value,
                    url
                ))
            ) {
                ZtwoSettings.editLocal { set(ZtwoSettings.searchType, index) }
                ZtwoSettings.editLocal { set(ZtwoSettings.searchUser, url) }
                ZtwoSettings.editLocal { set(ZtwoSettings.searchColor, color) }
                PrefZtwo.saveSearch(context)
            }
            if (list != null) {
                WebSearch.instance().engines = list
            }
            return list
        }

        @JvmStatic
        fun cacheEngineIcon(url: String?, bitmap: Bitmap?) {
            if (TextUtils.isEmpty(url) || !MainUtil.isBitmapOk(bitmap)) {
                return
            }
            com.mycompany.app.glide.ImageCache.put(com.mycompany.app.glide.ImageCache.key(2, url), bitmap)
        }
    }
}
