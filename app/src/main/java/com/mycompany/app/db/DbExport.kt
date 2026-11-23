package com.mycompany.app.db

import android.database.Cursor
import com.mycompany.app.main.MainUtil
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * Shared table-export loop used by the backup code.
 * 
 * Every row is written with `rowWriter`; the file is kept only if all rows were written, otherwise it is deleted
 * and null is returned.
 */
object DbExport {
    @JvmStatic
    fun exportCsv(cursor: Cursor?, path: String?, append: Boolean, rowWriter: RowWriter): File? {
        var writer: BufferedWriter? = null
        var stream: OutputStream? = null
        var success = false
        try {
            if (cursor != null && cursor.moveToFirst()) {
                stream = MainUtil.openFileOutput(path, append)
                writer = BufferedWriter(OutputStreamWriter(stream, StandardCharsets.UTF_8))
                while (rowWriter.write(writer, cursor)) {
                    if (!cursor.moveToNext()) {
                        success = true
                        break
                    }
                }
            }
        } catch (ignored: Exception) {
        }
        if (cursor != null) {
            cursor.close()
        }
        if (writer != null) {
            try {
                writer.close()
            } catch (ignored: Exception) {
            }
        }
        if (stream != null) {
            try {
                stream.close()
            } catch (ignored: Exception) {
            }
        }
        if (success) {
            return File(path!!)
        }
        val file = File(path!!)
        if (file.exists()) {
            file.delete()
        }
        return null
    }

    @JvmStatic
    fun exportCsv(cursor: Cursor?, path: String?, rowWriter: RowWriter): File? {
        return exportCsv(cursor, path, false, rowWriter)
    }

    fun interface RowWriter {
        fun write(writer: BufferedWriter?, cursor: Cursor?): Boolean
    }
}
