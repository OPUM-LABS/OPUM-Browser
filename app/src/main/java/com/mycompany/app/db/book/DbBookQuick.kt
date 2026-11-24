package com.mycompany.app.db.book

import android.content.ContentValues
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtriSettings
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
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.quick.QuickIcons
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.quick.QuickAdapter.QuickItem
import com.mycompany.app.quick.QuickAdapter.QuickSubItem
import ch.opumlabs.opumbrowser.R
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.regex.Pattern

@Suppress("deprecation")
class DbBookQuick(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookQuick_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _path TEXT, _title TEXT, _icon BLOB, _order INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookQuick_table")
        db.execSQL("CREATE TABLE DbBookQuick_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _path TEXT, _title TEXT, _icon BLOB, _order INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookQuick? = null
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
                    "DbBookQuick_table",
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
                        && MainUtil.writeIntEntry(bufferedWriter, "_rsv5", cursor2.getInt(cursor2.getColumnIndex("_rsv5")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_rsv1", cursor2.getString(cursor2.getColumnIndex("_rsv1")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeBytesEntry(bufferedWriter, "_icon", cursor2.getBlob(cursor2.getColumnIndex("_icon")))
                        && MainUtil.writeIntEntry(bufferedWriter, "_rsv4", cursor2.getInt(cursor2.getColumnIndex("_rsv4")))
                        && MainUtil.writeIntEntry(bufferedWriter, "_order", cursor2.getInt(cursor2.getColumnIndex("_order")))
            })
        }

        @JvmStatic
        fun iconFromCursor(cursor: Cursor, colIcon: Int, path: String?, secret: Boolean): Bitmap? {
            var bitmapC: Bitmap? = null
            if (TextUtils.isEmpty(path)) {
                return null
            }
            try {
                bitmapC = MainListLoader.cachedListIcon(path, secret)
                val blob: ByteArray? = if (MainUtil.isBitmapOk(bitmapC)) null else cursor.getBlob(colIcon)
                if (blob != null && blob.size > 1) {
                    bitmapC = BitmapUtil.decodeBytes(blob, blob.size)
                    if (!MainUtil.isBitmapOk(bitmapC)) {
                        return bitmapC
                    }
                    MainListLoader.cacheListIcon(path, bitmapC, secret)
                }
            } catch (unused: Exception) {
            }
            return bitmapC
        }

        @JvmStatic
        fun countInFolder(context: Context?, path: String?): Int {
            var count = 0
            if (context != null && !TextUtils.isEmpty(path)) {
                val args = arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), path)
                var cursorG: Cursor? = null
                try {
                    cursorG = DbUtil.query(
                        get(context).getWritableDatabase(),
                        "DbBookQuick_table",
                        null,
                        "_secret=? AND _rsv1=?",
                        args,
                        null
                    )
                    if (cursorG != null) {
                        count = cursorG.getCount()
                    }
                } catch (unused: Exception) {
                }
                if (cursorG != null) {
                    cursorG.close()
                }
            }
            return count
        }

        @JvmStatic
        fun itemsInFolder(context: Context?, path: String?): ArrayList<QuickItem?>? {
            var cursorG: Cursor? = null
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            val arrayList: ArrayList<QuickItem?> = ArrayList()
            try {
                cursorG = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookQuick_table",
                    null,
                    "_secret=? AND _rsv1=?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), path!!),
                    "_order ASC"
                )
                if (cursorG != null && cursorG.moveToFirst()) {
                    val columnIndex = cursorG.getColumnIndex("_id")
                    val pathCol = cursorG.getColumnIndex("_path")
                    val titleCol = cursorG.getColumnIndex("_title")
                    val rsv4Col = cursorG.getColumnIndex("_rsv4")
                    val orderCol = cursorG.getColumnIndex("_order")
                    val patternCompile = Pattern.compile("\\p{Punct}")
                    do {
                        val quickItem = QuickItem()
                        quickItem.path = cursorG.getString(pathCol)
                        val label = cursorG.getString(titleCol)
                        quickItem.label = label
                        quickItem.title = MainUtil.stripPunctuation(label, patternCompile)
                        quickItem.color = cursorG.getInt(rsv4Col)
                        quickItem.order = cursorG.getInt(orderCol)
                        arrayList.add(quickItem)
                    } while (cursorG.moveToNext())
                }
            } catch (unused: Exception) {
            }
            if (cursorG != null) {
                cursorG.close()
            }
            return arrayList
        }

        @JvmStatic
        fun iconRes(color: Int): Int {
            val length = MainConst.iconColors.size
            for (index in 0..<length) {
                if (color == MainConst.iconColors[index]) {
                    return if (color == -16777216 && AppState.darkUi) R.drawable.baseline_folder_2_black_24 else MainConst.folderIcons[index]
                }
            }
            return MainConst.folderIcons[0]
        }

        @JvmStatic
        fun get(context: Context?): DbBookQuick {
            var dbBookQuick: DbBookQuick?
            val existing: DbBookQuick? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookQuick::class.java) {
                dbBookQuick = cached
                if (dbBookQuick == null) {
                    dbBookQuick = DbBookQuick(MainUtil.appContextOf(context), "DbBookQuick.db", null, 1)
                    if (colorIndex == 0) {
                        colorIndex = MainUtil.randomBetween(0, MainConst.iconColors.size)
                    }
                    cached = dbBookQuick
                }
            }
            return dbBookQuick!!
        }

        @JvmStatic
        fun folderCount(context: Context?): Int {
            var index = 0
            if (context == null) {
                return 0
            }
            var cursorG: Cursor? = null
            try {
                cursorG = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookQuick_table",
                    arrayOf<String?>("_rsv1"),
                    "_secret=?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey()),
                    null
                )
                if (cursorG != null && cursorG.moveToFirst()) {
                    val columnIndex = cursorG.getColumnIndex("_rsv1")
                    do {
                        if (TextUtils.isEmpty(cursorG.getString(columnIndex))) {
                            index++
                        }
                    } while (cursorG.moveToNext())
                }
            } catch (unused: Exception) {
            }
            if (cursorG != null) {
                cursorG.close()
            }
            return index
        }

        @JvmStatic
        fun loadItems(context: Context?, withNews: Boolean, withFolders: Boolean, firstLoad: Boolean): ArrayList<QuickItem?>? {
            var cursorG: Cursor?
            var iconColumn: Int
            var order: Int
            var cursor: Cursor? = null
            var patternCompile: Pattern? = null
            if (context == null) {
                return null
            }
            val arrayList: ArrayList<QuickItem?> = ArrayList()
            try {
                cursorG = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookQuick_table",
                    null,
                    "_secret=?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey()),
                    "_order ASC"
                )
                if (cursorG != null) {
                    try {
                        if (cursorG.moveToFirst()) {
                            val columnIndex = cursorG.getColumnIndex("_id")
                            val rsv5Col = cursorG.getColumnIndex("_rsv5")
                            val rsv1Col = cursorG.getColumnIndex("_rsv1")
                            val pathCol = cursorG.getColumnIndex("_path")
                            val titleCol = cursorG.getColumnIndex("_title")
                            var iconCol = cursorG.getColumnIndex("_icon")
                            val rsv4Col = cursorG.getColumnIndex("_rsv4")
                            val orderCol = cursorG.getColumnIndex("_order")
                            val rsv6Col = cursorG.getColumnIndex("_rsv6")
                            while (true) {
                                if (TextUtils.isEmpty(cursorG.getString(rsv1Col))) {
                                    if (patternCompile == null) {
                                        patternCompile = Pattern.compile("\\p{Punct}")
                                    }
                                    val quickItem = QuickItem()
                                    val iconIndex = iconCol
                                    quickItem.isFolder = cursorG.getInt(rsv5Col) == 1
                                    quickItem.path = cursorG.getString(pathCol)
                                    val label = cursorG.getString(titleCol)
                                    quickItem.label = label
                                    quickItem.title = MainUtil.stripPunctuation(label, patternCompile)
                                    quickItem.color = cursorG.getInt(rsv4Col)
                                    quickItem.letterOnly = QuickIcons.letterOnly(cursorG.getBlob(iconIndex))
                                    quickItem.order = cursorG.getInt(orderCol)
                                    if (quickItem.isFolder && cursorG.getInt(rsv6Col) != 1) {
                                        quickItem.subItems = subItems(context, quickItem.path)
                                    }
                                    arrayList.add(quickItem)
                                    if (firstLoad && ((quickItem.color.also {
                                            order = it
                                        }) == 0 || order == -460552)) {
                                        iconColumn = iconIndex
                                        iconFromCursor(cursorG, iconColumn, quickItem.path, SyncSettings.secretMode.value)
                                    } else {
                                        iconColumn = iconIndex
                                    }
                                } else {
                                    iconColumn = iconCol
                                }
                                if (!cursorG.moveToNext()) {
                                    break
                                }
                                iconCol = iconColumn
                            }
                        }
                    } catch (unused: Exception) {
                        cursor = cursorG
                        cursorG = cursor
                    }
                }
            } catch (unused2: Exception) {
                cursorG = cursor
            }
            if (cursorG != null) {
                cursorG.close()
            }
            if (withNews) {
                val quickItem = QuickItem()
                quickItem.itemKind = 9.toByte()
                arrayList.add(0, quickItem)
            }
            val folderItem = QuickItem()
            if (ZtriSettings.quickPlus.value) {
                folderItem.itemKind = 1.toByte()
            } else {
                folderItem.itemKind = 2.toByte()
            }
            arrayList.add(folderItem)
            if (withFolders) {
                val quickItem = QuickItem()
                quickItem.itemKind = 8.toByte()
                arrayList.add(quickItem)
            }
            return arrayList
        }

        @JvmStatic
        fun orderOf(context: Context?, folderPath: String?): Int {
            var order = -1
            if (context != null && !TextUtils.isEmpty(folderPath)) {
                val columns = arrayOf<String?>("_order")
                val args = arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), folderPath)
                var cursorG: Cursor? = null
                try {
                    cursorG = DbUtil.query(
                        get(context).getWritableDatabase(),
                        "DbBookQuick_table",
                        columns,
                        "_secret=? AND _path=?",
                        args,
                        null
                    )
                    if (cursorG != null && cursorG.moveToFirst()) {
                        order = cursorG.getInt(cursorG.getColumnIndex("_order"))
                    }
                } catch (unused: Exception) {
                }
                if (cursorG != null) {
                    cursorG.close()
                }
            }
            return order
        }

        @JvmStatic
        fun nextColor(): Int {
            val colors = MainConst.iconColors
            val length = colors.size
            var index: Int = colorIndex % length
            if (index < 0) {
                index = 0
            }
            val color = colors[index]
            colorIndex = (index + 3) % length
            return color
        }

        @JvmStatic
        fun subItems(context: Context?, path: String?): ArrayList<QuickSubItem?>? {
            var cursorG: Cursor? = null
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            val arrayList: ArrayList<QuickSubItem?> = ArrayList()
            try {
                cursorG = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookQuick_table",
                    null,
                    "_secret=? AND _rsv1=?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), path!!),
                    "_order ASC"
                )
                if (cursorG != null && cursorG.moveToFirst()) {
                    val columnIndex = cursorG.getColumnIndex("_id")
                    val pathCol = cursorG.getColumnIndex("_path")
                    val titleCol = cursorG.getColumnIndex("_title")
                    val rsv4Col = cursorG.getColumnIndex("_rsv4")
                    val patternCompile = Pattern.compile("\\p{Punct}")
                    var index = 0
                    do {
                        val quickSubItem = QuickSubItem()
                        quickSubItem.iconUrl = cursorG.getString(pathCol)
                        quickSubItem.letterText =
                            MainUtil.stripPunctuation(cursorG.getString(titleCol), patternCompile)
                        quickSubItem.color = cursorG.getInt(rsv4Col)
                        quickSubItem.letterOnly = QuickIcons.letterOnly(cursorG.getBlob(cursorG.getColumnIndex("_icon")))
                        arrayList.add(quickSubItem)
                        index++
                        if (index >= 4) {
                            break
                        }
                    } while (cursorG.moveToNext())
                }
            } catch (unused: Exception) {
            }
            if (cursorG != null) {
                cursorG.close()
            }
            return arrayList
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
                        "DbBookQuick_table",
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
        fun addToFolder(context: Context?, path: String?, quickItem: QuickItem, label: String?, color: Int) {
            var label = label
            var color = color
            if (context == null || TextUtils.isEmpty(path)) {
                return
            }
            val currentTimeMillis = System.currentTimeMillis().toString()
            if (label == null) {
                label = ""
            }
            if (color == 0) {
                color = nextColor()
            }
            val contentValues = ContentValues()
            contentValues.put("_rsv5", 1 as Int?)
            contentValues.put("_path", currentTimeMillis)
            contentValues.put("_title", label)
            contentValues.put("_order", quickItem.order)
            contentValues.put("_secret", com.mycompany.app.pref.store.Workspaces.sharedNumber())
            contentValues.put("_rsv4", color)
            val rowId = DbUtil.insert(get(context).getWritableDatabase(), "DbBookQuick_table", contentValues)
            if (rowId != 0L) {
                moveToFolder(context, 0, currentTimeMillis, quickItem.path)
                moveToFolder(context, 1, currentTimeMillis, path)
                quickItem.isFolder = true
                quickItem.path = currentTimeMillis
                quickItem.label = label
                quickItem.title = MainUtil.stripPunctuation(label, null)
                quickItem.color = color
            }
        }

        @JvmStatic
        fun deletePath(context: Context?, path: String?, notify: Boolean): Boolean {
            var deleted = false
            if (context != null && !TextUtils.isEmpty(path)) {
                deleted = DbUtil.deleteWhere(
                    get(context).getWritableDatabase(),
                    "DbBookQuick_table",
                    "_secret=? AND _path=?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), path!!)
                ) > 0
                if (notify && deleted) {
                    DbUtil.deleteWhere(
                        get(context).getWritableDatabase(),
                        "DbBookQuick_table",
                        "_secret=? AND _rsv1=?",
                        arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), path)
                    )
                }
            }
            return deleted
        }

        @JvmStatic
        fun insertItem(
            context: Context?,
            type: Int,
            extra: String?,
            path: String?,
            title: String?,
            bitmap: Bitmap?,
            color: Int,
            order: Int
        ) {
            var color = color
            if (context == null || TextUtils.isEmpty(path)) {
                return
            }
            val contentValues = ContentValues()
            contentValues.put("_rsv5", type)
            contentValues.put("_rsv1", extra)
            contentValues.put("_path", path)
            contentValues.put("_title", title)
            contentValues.put("_order", order)
            contentValues.put("_secret", com.mycompany.app.pref.store.Workspaces.sharedNumber())
            if (MainUtil.isBitmapOk(bitmap)) {
                try {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    contentValues.put("_icon", byteArrayOutputStream.toByteArray())
                    byteArrayOutputStream.close()
                } catch (unused: Exception) {
                }
            } else {
                if (color == 0) {
                    color = nextColor()
                }
                contentValues.put("_rsv4", color)
            }
            DbUtil.insert(get(context).getWritableDatabase(), "DbBookQuick_table", contentValues)
        }

        @JvmStatic
        fun moveToFolder(context: Context?, order: Int, path: String?, folderPath: String?) {
            if (context == null || TextUtils.isEmpty(path) || TextUtils.isEmpty(folderPath)) {
                return
            }
            val secret = com.mycompany.app.pref.store.Workspaces.sharedKey()
            val contentValuesG = android.content.ContentValues().also { cv -> cv.put("_rsv1", path) }
            contentValuesG.put("_order", order)
            DbUtil.updateWhere(
                get(context).getWritableDatabase(),
                "DbBookQuick_table",
                contentValuesG,
                "_secret=? AND _path=?",
                arrayOf<String?>(secret, folderPath)
            )
        }
    }
}
