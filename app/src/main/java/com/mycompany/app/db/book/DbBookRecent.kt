package com.mycompany.app.db.book

import android.content.ContentValues
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtwoSettings
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.mycompany.app.data.book.DataBookRecent.Companion.instance
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.web.WebSearchAdapter.SearchItem

@Suppress("deprecation")
class DbBookRecent(context: Context?, nameParam: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, nameParam, factory, version) {

    internal class ExistsWhereTask(@JvmField val context: Context?,
                                   @JvmField val title: String?
    ) : Runnable {
        override fun run() {
            if (isEnabled()) {
                val title = this.title
                if (TextUtils.isEmpty(title)) {
                    return
                }
                val args = arrayOf<String?>(if (SyncSettings.secretMode.value) "1" else "0", title)
                val context = this.context
                val writableDatabase: SQLiteDatabase? = get(context).getWritableDatabase()
                val exists = DbUtil.existsWhere(
                    writableDatabase,
                    "DbBookRecent_table",
                    null,
                    "_secret=? AND _title=?",
                    args
                )
                if (exists != 0) {
                    val contentValues = ContentValues()
                    contentValues.put("_time", System.currentTimeMillis())
                    if (exists == 1) {
                        DbUtil.updateWhere(
                            writableDatabase,
                            "DbBookRecent_table",
                            contentValues,
                            "_secret=? AND _title=?",
                            args
                        )
                        val dataBookRecentA = instance(context)
                        dataBookRecentA.reloadNeeded = true
                        dataBookRecentA.changed = true
                        return
                    }
                    contentValues.put("_secret", if (SyncSettings.secretMode.value) 1 else 0)
                    contentValues.put("_title", title)
                    DbUtil.insert(writableDatabase, "DbBookRecent_table", contentValues)
                    val dataBookRecentA = instance(context)
                    dataBookRecentA.reloadNeeded = true
                    dataBookRecentA.changed = true
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookRecent_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _title TEXT, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookRecent_table")
        db.execSQL("CREATE TABLE DbBookRecent_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _title TEXT, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbBookRecent? = null

        @JvmStatic
        fun trimOld(context: Context?, secretFlag: Boolean) {
            if (context == null) {
                return
            }
            val limit = ZtwoSettings.recentLimit.value
            if (limit == -1) {
                return
            }
            if (limit == 0) {
                clearSecret(context, secretFlag)
                return
            }
            val secret = if (secretFlag) "1" else "0"
            var cutoff: Long = 0
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookRecent_table",
                    arrayOf<String?>("_time"),
                    "_secret=?",
                    arrayOf<String?>(secret),
                    "_time DESC"
                )
                if (cursor != null && cursor.getCount() > limit && cursor.moveToPosition(limit - 1)) {
                    cutoff = cursor.getLong(cursor.getColumnIndex("_time"))
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
            if (cutoff <= 0) {
                return
            }
            DbUtil.deleteWhere(
                get(context).getWritableDatabase(), "DbBookRecent_table", "_secret=? AND _time<?",
                arrayOf<String?>(secret, cutoff.toString())
            )
            instance(context).changed = true
        }

        @JvmStatic
        fun get(context: Context?): DbBookRecent {
            var dbBookRecent: DbBookRecent?
            val existing: DbBookRecent? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookRecent::class.java) {
                dbBookRecent = cached
                if (dbBookRecent == null) {
                    dbBookRecent = DbBookRecent(MainUtil.appContextOf(context), "DbBookRecent.db", null, 1)
                    cached = dbBookRecent
                }
            }
            return dbBookRecent!!
        }

        @JvmStatic
        fun loadItems(context: Context?): ArrayList<SearchItem?>? {
            var cursorG: Cursor? = null
            if (context == null) {
                return null
            }
            val arrayList: ArrayList<SearchItem?> = ArrayList()
            try {
                cursorG = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookRecent_table",
                    arrayOf<String?>("_id", "_title"),
                    "_secret=?",
                    arrayOf<String?>(if (SyncSettings.secretMode.value) "1" else "0"),
                    "_time DESC"
                )
                if (cursorG != null && cursorG.moveToFirst()) {
                    val columnIndex = cursorG.getColumnIndex("_id")
                    val titleCol = cursorG.getColumnIndex("_title")
                    var limit = ZtwoSettings.recentLimit.value
                    if (limit == -1) {
                        limit = Int.MAX_VALUE
                    }
                    var index = 0
                    do {
                        val searchItem = SearchItem()
                        searchItem.type = 37.toByte()
                        searchItem.id = cursorG.getLong(columnIndex)
                        searchItem.title = cursorG.getString(titleCol)
                        arrayList.add(searchItem)
                        index++
                        if (index >= limit) {
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
        fun deleteById(context: Context?, id: Long) {
            if (context == null || id <= 0) {
                return
            }
            DbUtil.deleteById(get(context).getWritableDatabase(), "DbBookRecent_table", id)
            instance(context).changed = true
        }

        @JvmStatic
        fun clearAll(context: Context?) {
            if (context == null) {
                return
            }
            DbUtil.deleteWhere(get(context).getWritableDatabase(), "DbBookRecent_table", null, null)
            instance(context).changed = true
        }

        @JvmStatic
        fun clearSecret(context: Context?, secret: Boolean) {
            if (context == null) {
                return
            }
            DbUtil.deleteWhere(
                get(context).getWritableDatabase(),
                "DbBookRecent_table",
                "_secret=?",
                arrayOf<String?>(if (secret) "1" else "0")
            )
            instance(context).changed = true
        }

        @JvmStatic
        fun addAsync(context: Context?, title: String?) {
            val contextQ = MainUtil.appContextOf(context)
            if (contextQ == null) {
                return
            }
            MainApp.runInBackground(contextQ, ExistsWhereTask(contextQ, title))
        }

        @JvmStatic
        fun isEnabled(): Boolean {
            if (ZtwoSettings.recentLimit.value == 0) {
                return false
            }
            return !SyncSettings.secretMode.value || ZtwoSettings.recentSecret.value
        }
    }
}
