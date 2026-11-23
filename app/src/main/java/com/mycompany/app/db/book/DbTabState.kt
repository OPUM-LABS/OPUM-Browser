package com.mycompany.app.db.book

import android.content.ContentValues
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtwoSettings
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import com.mycompany.app.db.DbExport
import com.mycompany.app.db.DbExport.RowWriter
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.web.WebNestFrame.PageItem
import java.io.BufferedWriter
import java.io.File

@Suppress("deprecation")
class DbTabState(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    class StateItem {
        @JvmField
        var bundle: Bundle? = null
        @JvmField
        var pages: ArrayList<PageItem?>? = null
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbTabState_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _uid INTEGER, _path TEXT, _state TEXT, _pages TEXT, _ads TEXT, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbTabState_table")
        db.execSQL("CREATE TABLE DbTabState_table (_id INTEGER PRIMARY KEY, _secret INTEGER, _uid INTEGER, _path TEXT, _state TEXT, _pages TEXT, _ads TEXT, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbTabState? = null

        @JvmStatic
        fun exportFile(context: Context?, path: String?): File? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbTabState_table",
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
                        && MainUtil.writeLongEntry(bufferedWriter, "_uid", cursor2.getLong(cursor2.getColumnIndex("_uid")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_state", cursor2.getString(cursor2.getColumnIndex("_state")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_pages", cursor2.getString(cursor2.getColumnIndex("_pages")))
            })
        }

        @JvmStatic
        fun get(context: Context?): DbTabState {
            var dbTabState: DbTabState?
            val existing: DbTabState? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbTabState::class.java) {
                dbTabState = cached
                if (dbTabState == null) {
                    dbTabState = DbTabState(MainUtil.appContextOf(context), "DbTabState.db", null, 1)
                    cached = dbTabState
                }
            }
            return dbTabState!!
        }

        /**
         * Saved state of tab `uid`: the WebView back/forward state and, with page stacking on
         * (ZtwoSettings.addPage.value), the extra pages. Null if nothing is stored.
         */
        @JvmStatic
        fun stateById(context: Context?, uid: Long): StateItem? {
            if (context == null || uid <= 0) {
                return null
            }
            var state: String? = null
            var pages: String? = null
            var found = false
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbTabState_table",
                    arrayOf<String?>("_state", "_pages"),
                    "_uid=?",
                    arrayOf<String?>(uid.toString()),
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    state = cursor.getString(cursor.getColumnIndex("_state"))
                    pages =
                        if (ZtwoSettings.addPage.value) cursor.getString(cursor.getColumnIndex("_pages")) else null
                    found = true
                }
            } catch (unused: Exception) {
                found = false
            }
            if (cursor != null) {
                cursor.close()
            }
            if (!found) {
                return null
            }
            val item = StateItem()
            if (!TextUtils.isEmpty(state)) {
                try {
                    val bytes = Base64.decode(state, Base64.NO_WRAP)
                    if (bytes != null && bytes.size > 0) {
                        val bundle = Bundle()
                        bundle.putByteArray("WEBVIEW_CHROMIUM_STATE", bytes)
                        item.bundle = bundle
                    }
                } catch (unused: Exception) {
                } catch (unused: OutOfMemoryError) {
                }
            }
            if (ZtwoSettings.addPage.value && !TextUtils.isEmpty(pages)) {
                for (page in pages!!.split("!@!".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()) {
                    val url = MainUtil.offlinePathFor(context, page)
                    if (TextUtils.isEmpty(url)) {
                        continue
                    }
                    val pageItem = PageItem()
                    pageItem.url = url
                    if (item.pages == null) {
                        item.pages = ArrayList()
                    }
                    item.pages!!.add(pageItem)
                }
            }
            return item
        }

        @JvmStatic
        fun deleteWhere(context: Context?, whereText: String?, args: Array<String?>?) {
            if (context != null) {
                DbUtil.deleteWhere(get(context).getWritableDatabase(), "DbTabState_table", whereText, args)
            }
        }

        @JvmStatic
        fun saveState(context: Context?, tabUid: Long, validPageUrl: String?, bundle: Bundle?, listToStr: String?) {
            if (context == null || tabUid <= 0) {
                return
            }
            val args = arrayOf<String?>(tabUid.toString())
            val writableDatabase: SQLiteDatabase? = get(context).getWritableDatabase()
            var stateText: String? = null
            val exists = DbUtil.existsWhere(writableDatabase, "DbTabState_table", null, "_uid=?", args)
            if (exists != 0) {
                if (bundle != null) {
                    try {
                        val byteArray = bundle.getByteArray("WEBVIEW_CHROMIUM_STATE")
                        if (byteArray != null && byteArray.size != 0) {
                            stateText = Base64.encodeToString(byteArray, 2)
                        }
                    } catch (unused: Exception) {
                    } catch (unused: OutOfMemoryError) {
                    }
                }
                val contentValues = ContentValues()
                contentValues.put("_secret", if (SyncSettings.secretMode.value) 1 else 0)
                contentValues.put("_uid", tabUid)
                contentValues.put("_path", validPageUrl)
                contentValues.put("_state", stateText)
                contentValues.put("_pages", listToStr)
                if (exists == 1) {
                    DbUtil.updateWhere(writableDatabase, "DbTabState_table", contentValues, "_uid=?", args)
                } else {
                    DbUtil.insert(writableDatabase, "DbTabState_table", contentValues)
                }
            }
        }
    }
}
