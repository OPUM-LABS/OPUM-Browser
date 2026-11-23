package com.mycompany.app.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

@Suppress("deprecation")
object DbUtil {
    @JvmStatic
    fun deleteWhere(
        sQLiteDatabase: SQLiteDatabase?,
        table: String?,
        all: String?,
        args: Array<String?>?
    ): Int {
        var deleted = 0
        try {
            sQLiteDatabase!!.beginTransaction()
            deleted = sQLiteDatabase.delete(table!!, all, args)
            sQLiteDatabase.setTransactionSuccessful()
        } catch (unused: Exception) {
        }
        try {
            sQLiteDatabase!!.endTransaction()
        } catch (unused2: Exception) {
        }
        return deleted
    }

    @JvmStatic
    fun deleteById(sQLiteDatabase: SQLiteDatabase?, table: String?, id: Long) {
        try {
            sQLiteDatabase!!.beginTransaction()
            sQLiteDatabase.delete(table!!, "_id=?", arrayOf<String?>(id.toString()))
            sQLiteDatabase.setTransactionSuccessful()
        } catch (unused: Exception) {
        }
        try {
            sQLiteDatabase!!.endTransaction()
        } catch (unused2: Exception) {
        }
    }

    @JvmStatic
    fun existsById(sQLiteDatabase: SQLiteDatabase?, table: String?, id: Long): Int {
        var state = 2
        if (id <= 0) {
            return 2
        }
        var cursorQuery: Cursor? = null
        try {
            cursorQuery = sQLiteDatabase!!.query(
                table!!,
                null,
                "_id=?",
                arrayOf<String?>(id.toString()),
                null,
                null,
                null
            )
            if (cursorQuery.moveToFirst()) {
                state = 1
            }
        } catch (unused: Exception) {
            state = 0
        }
        if (cursorQuery != null) {
            try {
                cursorQuery.close()
            } catch (unused2: Exception) {
            }
        }
        return state
    }

    /** Row-existence check used before update-or-insert: 1 = a matching row exists, 2 = none, 0 = query failed.  */
    @JvmStatic
    fun existsWhere(
        sQLiteDatabase: SQLiteDatabase?,
        table: String?,
        columns: Array<String?>?,
        selection: String?,
        args: Array<String?>?
    ): Int {
        var cursor: Cursor? = null
        var result: Int
        try {
            cursor = sQLiteDatabase!!.query(table!!, columns, selection, args, null, null, null)
            result = if (cursor.moveToFirst()) 1 else 2
        } catch (unused: Exception) {
            result = 0
        }
        if (cursor != null) {
            try {
                cursor.close()
            } catch (unused2: Exception) {
            }
        }
        return result
    }

    @JvmStatic
    fun insert(sQLiteDatabase: SQLiteDatabase?, table: String?, contentValues: ContentValues?): Long {
        var rowId: Long = 0
        try {
            sQLiteDatabase!!.beginTransaction()
            rowId = sQLiteDatabase.insert(table!!, null, contentValues)
            sQLiteDatabase.setTransactionSuccessful()
        } catch (unused: Exception) {
        }
        try {
            sQLiteDatabase!!.endTransaction()
        } catch (unused2: Exception) {
        }
        return rowId
    }

    @JvmStatic
    fun queryById(sQLiteDatabase: SQLiteDatabase?, table: String?, columns: Array<String?>?, id: Long): Cursor? {
        try {
            return sQLiteDatabase!!.query(
                table!!,
                columns,
                "_id=?",
                arrayOf<String?>(id.toString()),
                null,
                null,
                null
            )
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun query(
        sQLiteDatabase: SQLiteDatabase?,
        table: String?,
        columns: Array<String?>?,
        selection: String?,
        args: Array<String?>?,
        order: String?
    ): Cursor? {
        try {
            return sQLiteDatabase!!.query(table!!, columns, selection, args, null, null, order)
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun updateWhere(
        sQLiteDatabase: SQLiteDatabase?,
        table: String?,
        contentValues: ContentValues?,
        selection: String?,
        args: Array<String?>?
    ): Int {
        var updated = 0
        try {
            sQLiteDatabase!!.beginTransaction()
            updated = sQLiteDatabase.update(table!!, contentValues, selection, args)
            sQLiteDatabase.setTransactionSuccessful()
        } catch (unused: Exception) {
        }
        try {
            sQLiteDatabase!!.endTransaction()
        } catch (unused2: Exception) {
        }
        return updated
    }

    @JvmStatic
    fun updateById(sQLiteDatabase: SQLiteDatabase?, table: String?, contentValues: ContentValues?, id: Long) {
        try {
            sQLiteDatabase!!.beginTransaction()
            sQLiteDatabase.update(table!!, contentValues, "_id=?", arrayOf<String?>(id.toString()))
            sQLiteDatabase.setTransactionSuccessful()
        } catch (unused: Exception) {
        }
        try {
            sQLiteDatabase!!.endTransaction()
        } catch (unused2: Exception) {
        }
    }
}
