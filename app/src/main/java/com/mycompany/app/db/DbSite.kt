package com.mycompany.app.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.text.TextUtils
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import ch.opumlabs.opumbrowser.R
import java.io.ByteArrayOutputStream

/**
 * Insert-or-update helpers shared by the book tables (DbBookAds, DbBookPop, DbBookLink, ... and DbBookAlbum/Pdf/Cmp).
 */
object DbSite {
    /**
     * Adds `path` to a site list table (or refreshes its time if already present) and returns
     * the row as a list item, or null on failure.
     */
    @JvmStatic
    fun addPath(db: SQLiteDatabase?, table: String?, path: String?): ChildItem? {
        if (db == null || TextUtils.isEmpty(path)) {
            return null
        }
        val now = System.currentTimeMillis()
        val args = arrayOf<String?>(path)
        val state = DbUtil.existsWhere(db, table, null, "_path=?", args)
        if (state != 0) {
            val values = ContentValues()
            values.put("_path", path)
            values.put("_time", now)
            if (state == 1) {
                DbUtil.updateWhere(db, table, values, "_path=?", args)
            } else {
                DbUtil.insert(db, table, values)
            }
        }
        val id = findId(db, table, "_path=?", args)
        if (id <= 0) {
            return null
        }
        val item = ChildItem()
        item.id = id
        item.path = path
        item.name = path
        item.time = now
        item.itemType = 0.toByte()
        item.iconColor = -460552
        item.iconRes = R.drawable.outline_public_black_24
        item.dateText = MainUtil.formatDate(now, null, "yyyy.MM.dd")
        item.subText = MainUtil.pathOfUrl(path, false)
        return item
    }

    /** First `_id` matching the selection, or 0.  */
    @JvmStatic
    fun findId(db: SQLiteDatabase?, table: String?, where: String?, args: Array<String?>?): Long {
        var id: Long = 0
        var cursor: Cursor? = null
        try {
            cursor = DbUtil.query(db, table, arrayOf<String?>("_id"), where, args, null)
            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getLong(cursor.getColumnIndex("_id"))
            }
        } catch (unused: Exception) {
        }
        if (cursor != null) {
            cursor.close()
        }
        return id
    }

    /**
     * Saves a reading position for an album / PDF / archive file (one row per file and index).
     * The thumbnail is stored only when the row has none yet.
     * 
     * @param page       page mode; anything other than 3 or 4 is stored as 1
     * @param cleanTitle run the name through [MainUtil.stripExtension] (albums)
     */
    @JvmStatic
    fun addFile(
        db: SQLiteDatabase?, table: String?, path: String?, name: String?, count: Int,
        index: Int, page: Int, thumb: Bitmap?, icon: Int, cleanTitle: Boolean
    ): ChildItem? {
        var page = page
        if (db == null || TextUtils.isEmpty(path)) {
            return null
        }
        if (page != 3 && page != 4) {
            page = 1
        }
        val now = System.currentTimeMillis()
        val values = ContentValues()
        values.put("_path", path)
        values.put("_name", name)
        values.put("_count", count)
        values.put("_index", index)
        values.put("_page", page)
        values.put("_time", now)
        val args = arrayOf<String?>(path, index.toString())
        var state: Int
        var hasIcon = false
        var cursor: Cursor? = null
        try {
            cursor =
                DbUtil.query(db, table, arrayOf<String?>("_icon"), "_path=? AND _index=?", args, null)
            if (cursor != null && cursor.moveToFirst()) {
                state = 1
                try {
                    val blob = cursor.getBlob(cursor.getColumnIndex("_icon"))
                    hasIcon = MainUtil.isBitmapOk(thumb) && blob != null && blob.size > 0
                } catch (unused: Exception) {
                }
            } else {
                state = 2
            }
        } catch (unused: Exception) {
            state = 0
        }
        if (cursor != null) {
            cursor.close()
        }
        if (state != 0) {
            if (!hasIcon && MainUtil.isBitmapOk(thumb)) {
                try {
                    val out = ByteArrayOutputStream()
                    thumb!!.compress(Bitmap.CompressFormat.PNG, 100, out)
                    values.put("_icon", out.toByteArray())
                    out.close()
                } catch (unused: Exception) {
                }
            }
            if (state == 1) {
                DbUtil.updateWhere(db, table, values, "_path=? AND _index=?", args)
            } else {
                DbUtil.insert(db, table, values)
            }
        }
        val id = DbSite.findId(db, table, "_time=?", arrayOf<String?>(now.toString()))
        if (id <= 0) {
            return null
        }
        val item = ChildItem()
        item.id = id
        item.path = path
        item.count = count
        item.index = index
        item.page = page
        item.filePath = MainUtil.backupItemName(count, index, page, path)
        item.time = now
        item.name = if (cleanTitle) MainUtil.stripExtension(name) else name
        item.itemType = 11.toByte()
        item.iconColor = -460552
        item.iconRes = icon
        item.dateText = MainUtil.formatDate(now, null, "yyyy.MM.dd")
        item.subText = MainUtil.positionText(count, index)
        return item
    }
}
