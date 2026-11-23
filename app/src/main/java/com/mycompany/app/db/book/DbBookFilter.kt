package com.mycompany.app.db.book

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import android.webkit.URLUtil
import com.mycompany.app.db.DbUtil
import com.mycompany.app.dialog.DialogSetFilter
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import ch.opumlabs.opumbrowser.R
import java.io.File

@Suppress("deprecation")
class DbBookFilter(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookFilter_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _time INTEGER, _use INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookFilter_table")
        db.execSQL("CREATE TABLE DbBookFilter_table (_id INTEGER PRIMARY KEY, _path TEXT, _title TEXT, _time INTEGER, _use INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookFilter? = null

        @JvmStatic
        fun addFilters(index: Int, context: Context?, sb: StringBuilder?): StringBuilder? {
            var sb = sb
            if (index >= 0) {
                val filters = DialogSetFilter.filters
                if (index < 44) {
                    val filter: Array<String?> = filters[index]!!
                    addFilter(context, filter[1], filter[0])
                    if (sb == null) {
                        sb = StringBuilder()
                    } else {
                        sb.append("/")
                    }
                    sb.append(index)
                }
            }
            return sb
        }

        @JvmStatic
        fun addFilter(context: Context?, base: String?, title: String?) {
            val args = arrayOf<String?>(base)
            var cursorG: Cursor? = null
            try {
                val writableDatabase: SQLiteDatabase? = get(context).getWritableDatabase()
                cursorG =
                    DbUtil.query(writableDatabase, "DbBookFilter_table", null, "_path=?", args, null)
                if (cursorG == null || !cursorG.moveToFirst()) {
                    val contentValues = ContentValues()
                    contentValues.put("_path", base)
                    contentValues.put("_title", title)
                    contentValues.put("_time", System.currentTimeMillis())
                    contentValues.put("_use", 1 as Int?)
                    DbUtil.insert(writableDatabase, "DbBookFilter_table", contentValues)
                }
            } catch (unused: Exception) {
            }
            if (cursorG != null) {
                cursorG.close()
            }
        }

        /** Sum of the last-modified times of all enabled filter lists; changes whenever any list changes.  */
        @JvmStatic
        fun totalCount(context: Context?): Long {
            if (context == null) {
                return 0L
            }
            var sum: Long = 0
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookFilter_table",
                    arrayOf<String?>("_path", "_time"),
                    "_use=?",
                    arrayOf<String?>("1"),
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val colPath = cursor.getColumnIndex("_path")
                    do {
                        try {
                            val path = cursor.getString(colPath)
                            var modified: Long = 0
                            if ("sb_user_filter_path" == path) {
                                modified = cursor.getLong(cursor.getColumnIndex("_time"))
                            } else if (MainUri.isContentUri(path)) {
                                val file = MainUri.documentFile(context, path)
                                modified = if (file == null) 0L else file.lastModified()
                            } else {
                                val local = MainUtil.filterFilePath(context, path)
                                if (!TextUtils.isEmpty(local)) {
                                    modified = File(local!!).lastModified()
                                }
                            }
                            sum += modified
                        } catch (unused: Exception) {
                        }
                    } while (cursor.moveToNext())
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
            return sum
        }

        @JvmStatic
        fun iconOf(path: String): Int {
            var path = path
            if ("sb_user_filter_path" == path) {
                return R.drawable.outline_verified_user_red_24
            }
            if (!URLUtil.isNetworkUrl(path)) {
                return R.drawable.outline_draft_black_24
            }
            val githubPath = MainUtil.jsDelivrToGithub(path)
            if (!TextUtils.isEmpty(githubPath)) {
                path = githubPath!!
            }
            val filters = DialogSetFilter.filters
            var index = 0
            if (if (TextUtils.isEmpty(path)) false else path.startsWith("https://raw.githubusercontent.com/AdguardTeam/")) {
                while (index < 18) {
                    if (path == DialogSetFilter.adguardUrl(index)) {
                        return R.drawable.ic_adguard
                    }
                    index++
                }
            } else {
                while (index < 44) {
                    if (path == DialogSetFilter.filters[index]!![1]) {
                        return R.drawable.ic_adblock
                    }
                    index++
                }
            }
            return R.drawable.outline_public_black_24
        }

        @JvmStatic
        fun get(context: Context?): DbBookFilter {
            var dbBookFilter: DbBookFilter?
            val existing: DbBookFilter? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookFilter::class.java) {
                dbBookFilter = cached
                if (dbBookFilter == null) {
                    dbBookFilter = DbBookFilter(MainUtil.appContextOf(context), "DbBookFilter.db", null, 2)
                    cached = dbBookFilter
                }
            }
            return dbBookFilter!!
        }

        /**
         * Adds or updates the filter list at `str` (title `str2`; the user-filter entry uses its
         * path as title). New lists start enabled. Returns the row as a list item.
         */
        @JvmStatic
        fun addPathItem(context: Context?, path: String?, extra: String?): ChildItem? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            val title = if ("sb_user_filter_path" == path) path else extra
            var time = System.currentTimeMillis()
            val args = arrayOf<String?>(path)
            val db: SQLiteDatabase? = get(context).getWritableDatabase()
            val state = DbUtil.existsWhere(db, "DbBookFilter_table", null, "_path=?", args)
            if (state != 0) {
                val values = ContentValues()
                values.put("_path", path)
                values.put("_title", title)
                values.put("_time", time)
                if (state == 1) {
                    DbUtil.updateWhere(db, "DbBookFilter_table", values, "_path=?", args)
                } else {
                    values.put("_use", 1)
                    DbUtil.insert(db, "DbBookFilter_table", values)
                }
            }
            var id: Long = 0
            var enabled = false
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    db,
                    "DbBookFilter_table",
                    arrayOf<String?>("_id", "_time", "_use"),
                    "_path=?",
                    args,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    id = cursor.getLong(cursor.getColumnIndex("_id"))
                    time = cursor.getLong(cursor.getColumnIndex("_time"))
                    enabled = cursor.getInt(cursor.getColumnIndex("_use")) == 1
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
            if (id <= 0) {
                return null
            }
            val item = ChildItem()
            item.id = id
            item.path = path
            item.time = time
            item.isEnabled = enabled
            item.itemType = 0.toByte()
            val icon: Int = Companion.iconOf(path!!)
            item.iconRes = icon
            val red = R.drawable.outline_verified_user_red_24
            val adblock = R.drawable.ic_adblock
            val adguard = R.drawable.ic_adguard
            item.iconColor = if (icon == red || icon == adblock || icon == adguard) 0 else -460552
            if (icon == red) {
                item.listType = 0
            } else if (icon == adblock) {
                item.listType = 1
            } else if (icon == adguard) {
                item.listType = 2
            } else {
                item.listType = if (icon == R.drawable.outline_public_black_24) 3 else 4
            }
            item.dateText = MainUtil.formatDate(item.time, null, "yyyy.MM.dd")
            if ("sb_user_filter_path" == path) {
                item.name = context.getString(R.string.user_filter)
            } else if (MainUri.isContentUri(path)) {
                item.name = title
                item.size = MainUtil.uriFileSize(context, path)
                if (item.size == -1234L) {
                    item.dateText = context.getString(R.string.permission_removed)
                } else {
                    item.subText = MainUtil.sizeLabel(item.size)
                }
            } else {
                item.name = title
                item.filePath = MainUtil.filterFilePath(context, path)
                if (!TextUtils.isEmpty(item.filePath)) {
                    try {
                        item.size = File(item.filePath!!).length()
                        item.subText = MainUtil.sizeLabel(item.size)
                    } catch (unused: Exception) {
                    }
                }
            }
            return item
        }
    }
}
