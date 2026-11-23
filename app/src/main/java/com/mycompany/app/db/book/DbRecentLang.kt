package com.mycompany.app.db.book

import android.content.ContentValues
import com.mycompany.app.pref.store.ZtwoSettings
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.mycompany.app.db.DbExport
import com.mycompany.app.db.DbExport.RowWriter
import com.mycompany.app.db.DbUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefZtwo
import java.io.BufferedWriter
import java.io.File

@Suppress("deprecation")
class DbRecentLang(context: Context?, nameParam: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, nameParam, factory, version) {

    internal class AddLangTask(@JvmField val context: Context?, typeParam: Int,
                                   @JvmField val lang: String?,
                                   @JvmField val langText: String?
    ) : Runnable {
        val type: Byte

        init {
            this.type = (typeParam).toByte()
        }

        override fun run() {
            val lang = this.lang
            val langEmpty = TextUtils.isEmpty(lang)
            val type = this.type
            val context = this.context
            if (!langEmpty) {
                addLang(context, type.toInt(), lang)
            }
            addLang(context, type.toInt(), this.langText)
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbRecentLang_table (_id INTEGER PRIMARY KEY, _type INTEGER, _lang TEXT, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbRecentLang_table")
        db.execSQL("CREATE TABLE DbRecentLang_table (_id INTEGER PRIMARY KEY, _type INTEGER, _lang TEXT, _time INTEGER, _rsv1 TEXT, _rsv2 TEXT, _rsv3 TEXT, _rsv4 INTEGER, _rsv5 INTEGER, _rsv6 INTEGER);")
    }

    companion object {
        @JvmField
        var cached: DbRecentLang? = null

        @JvmStatic
        fun trimOld(context: Context?, type: Int) {
            if (context == null) {
                return
            }
            val limit: Int = limitOf(type)
            if (limit == -1) {
                return
            }
            if (limit == 0) {
                trimType(context, type)
                return
            }
            var cutoff: Long = 0
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbRecentLang_table",
                    arrayOf<String?>("_time"),
                    "_type=?",
                    arrayOf<String?>(type.toString()),
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
                get(context).getWritableDatabase(), "DbRecentLang_table", "_type=? AND _time<?",
                arrayOf<String?>(type.toString(), cutoff.toString())
            )
        }

        @JvmStatic
        fun exportFile(context: Context?, path: String?): File? {
            if (context == null || TextUtils.isEmpty(path)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbRecentLang_table",
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
                        && MainUtil.writeIntEntry(bufferedWriter, "_type", cursor2!!.getInt(cursor2.getColumnIndex("_type")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_lang", cursor2.getString(cursor2.getColumnIndex("_lang")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
            })
        }

        @JvmStatic
        fun get(context: Context?): DbRecentLang {
            var dbRecentLang: DbRecentLang?
            val existing: DbRecentLang? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbRecentLang::class.java) {
                dbRecentLang = cached
                if (dbRecentLang == null) {
                    dbRecentLang = DbRecentLang(MainUtil.appContextOf(context), "DbRecentLang.db", null, 1)
                    cached = dbRecentLang
                }
            }
            return dbRecentLang!!
        }

        @JvmStatic
        fun limitOf(type: Int): Int {
            if (type == 0) {
                return ZtwoSettings.recentLocal.value
            }
            return if (type == 1) ZtwoSettings.recentTitle.value else ZtwoSettings.recentTrans.value
        }

        @JvmStatic
        fun trimType(context: Context?, type: Int) {
            if (context == null) {
                return
            }
            DbUtil.deleteWhere(
                get(context).getWritableDatabase(),
                "DbRecentLang_table",
                "_type=?",
                arrayOf<String?>(type.toString())
            )
        }

        @JvmStatic
        fun addLang(context: Context?, type: Int, lang: String?) {
            if (limitOf(type) == 0 || TextUtils.isEmpty(lang)) {
                return
            }
            val writableDatabase: SQLiteDatabase? = get(context).getWritableDatabase()
            val args: Array<String?>? = arrayOf<String?>(type.toString(), lang!!)
            val exists = DbUtil.existsWhere(writableDatabase, "DbRecentLang_table", null, "_type=? AND _lang=?", args)
            if (exists == 0) {
                return
            }
            val contentValues = ContentValues()
            contentValues.put("_time", System.currentTimeMillis())
            if (exists == 1) {
                DbUtil.updateWhere(
                    writableDatabase,
                    "DbRecentLang_table",
                    contentValues,
                    "_type=? AND _lang=?",
                    args
                )
                return
            }
            contentValues.put("_type", type)
            contentValues.put("_lang", lang)
            if (DbUtil.insert(writableDatabase, "DbRecentLang_table", contentValues) != 0L) {
                trimOld(context, type)
            }
        }

        @JvmStatic
        fun addAsync(context: Context?, type: Int, langText: String?, lang: String?) {
            val contextQ = MainUtil.appContextOf(context)
            if (contextQ == null) {
                return
            }
            MainApp.runInBackground(contextQ, AddLangTask(contextQ, type, langText, lang))
        }
    }
}
