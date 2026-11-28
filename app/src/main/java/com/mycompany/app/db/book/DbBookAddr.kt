package com.mycompany.app.db.book

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.Workspaces
import org.json.JSONObject
import java.io.File

/**
 * Saved addresses (2026-11-28). One row per address; all its fields are one JSON text in `_data`, sealed with the
 * same keystore key as the saved passwords ([PassCrypt]), so the database file alone shows nothing but how many there
 * are. Rows belong to a workspace (`_secret`, like the saved passwords: [Workspaces.sharedNumber]).
 *
 * Backups carry the addresses opened ([openAllIn]) and a restore seals them again ([sealPlain]), so they move to a
 * new phone; Android's own backup does not take this database (backup_rules), it would arrive unreadable.
 */
class DbBookAddr private constructor(context: Context) : SQLiteOpenHelper(context, NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE (_id INTEGER PRIMARY KEY, _secret INTEGER, _data TEXT, _time INTEGER);")
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        sealPlain(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        const val NAME = "DbBookAddr.db"
        private const val TABLE = "DbBookAddr_table"

        @Volatile private var cached: DbBookAddr? = null

        @JvmStatic
        fun get(context: Context?): DbBookAddr {
            cached?.let { return it }
            synchronized(DbBookAddr::class.java) {
                cached?.let { return it }
                return DbBookAddr(MainUtil.appContextOf(context)!!).also { cached = it }
            }
        }

        /** The addresses of the workspace in use, the one used last first. Rows that cannot be opened are left out. */
        @JvmStatic
        fun all(context: Context?): List<SavedAddress> {
            if (context == null) return emptyList()
            val out = ArrayList<SavedAddress>()
            try {
                get(context).writableDatabase.query(
                    TABLE, arrayOf("_id", "_data"), "_secret=?", arrayOf(Workspaces.sharedKey()), null, null, "_time DESC",
                ).use { c ->
                    while (c.moveToNext()) {
                        val json = PassCrypt.open(c.getString(1)) ?: continue
                        fromJson(c.getLong(0), json)?.let { out.add(it) }
                    }
                }
            } catch (_: Exception) {
            }
            return out
        }

        /** Saves [address]: a new row when its id is 0, else over the row with its id. Returns the id (0 on failure). */
        @JvmStatic
        fun save(context: Context?, address: SavedAddress): Long {
            if (context == null || address.isEmpty) return 0L
            return try {
                val values = ContentValues().apply {
                    put("_data", PassCrypt.seal(toJson(address)))
                    put("_time", System.currentTimeMillis())
                }
                val db = get(context).writableDatabase
                if (address.id > 0 && db.update(TABLE, values, "_id=?", arrayOf(address.id.toString())) > 0) {
                    address.id
                } else {
                    values.put("_secret", Workspaces.sharedNumber())
                    db.insert(TABLE, null, values).coerceAtLeast(0L)
                }
            } catch (_: Exception) {
                0L
            }
        }

        /** Marks [id] as just used, so it is offered first. */
        @JvmStatic
        fun touch(context: Context?, id: Long) {
            if (context == null || id <= 0) return
            try {
                get(context).writableDatabase.update(
                    TABLE, ContentValues().apply { put("_time", System.currentTimeMillis()) }, "_id=?", arrayOf(id.toString()),
                )
            } catch (_: Exception) {
            }
        }

        @JvmStatic
        fun delete(context: Context?, id: Long) {
            if (context == null) return
            try { get(context).writableDatabase.delete(TABLE, "_id=?", arrayOf(id.toString())) } catch (_: Exception) {}
        }

        /** Seals rows still in plain text (restored from a backup). */
        @JvmStatic
        fun sealPlain(context: Context?) {
            if (context == null || !context.getDatabasePath(NAME).exists()) return
            try { get(context).writableDatabase } catch (_: Exception) {} // onOpen seals
        }

        private fun sealPlain(db: SQLiteDatabase) {
            try {
                db.query(TABLE, arrayOf("_id", "_data"), "_data IS NOT NULL AND _data NOT LIKE 'enc1:%'", null, null, null, null).use { c ->
                    while (c.moveToNext()) {
                        val sealed = PassCrypt.seal(c.getString(1)) ?: continue
                        db.update(TABLE, ContentValues().apply { put("_data", sealed) }, "_id=?", arrayOf(c.getLong(0).toString()))
                    }
                }
            } catch (_: Exception) {
            }
        }

        /** Opens every sealed row in [copy], a copy of the database made for a backup (see [PassCrypt]). */
        @JvmStatic
        fun openAllIn(copy: File) {
            val db = SQLiteDatabase.openDatabase(copy.path, null, SQLiteDatabase.OPEN_READWRITE)
            try {
                db.query(TABLE, arrayOf("_id", "_data"), "_data LIKE 'enc1:%'", null, null, null, null).use { c ->
                    while (c.moveToNext()) {
                        val plain = PassCrypt.open(c.getString(1)) ?: continue
                        db.update(TABLE, ContentValues().apply { put("_data", plain) }, "_id=?", arrayOf(c.getLong(0).toString()))
                    }
                }
            } finally {
                db.close()
            }
        }

        private fun toJson(a: SavedAddress): String = JSONObject().apply {
            put("label", a.label); put("name", a.name); put("org", a.org); put("street", a.street); put("line2", a.line2)
            put("postcode", a.postcode); put("city", a.city); put("region", a.region); put("country", a.country)
            put("cc", a.countryCode); put("tel", a.tel); put("email", a.email)
        }.toString()

        /** An address from [json] (a stored row, or what a page reported: keys as in [SavedAddress.fields]). */
        @JvmStatic
        fun fromJson(id: Long, json: String?): SavedAddress? {
            val o = try { JSONObject(json ?: return null) } catch (_: Exception) { return null }
            fun f(key: String) = o.optString(key, "").trim().take(SavedAddress.MAX_FIELD)
            return SavedAddress(
                id = id, label = f("label"), name = f("name"), org = f("org"), street = f("street"), line2 = f("line2"),
                postcode = f("postcode"), city = f("city"), region = f("region"), country = f("country"),
                countryCode = f("cc").uppercase().takeIf { it.length == 2 } ?: "", tel = f("tel"), email = f("email"),
            )
        }
    }
}
