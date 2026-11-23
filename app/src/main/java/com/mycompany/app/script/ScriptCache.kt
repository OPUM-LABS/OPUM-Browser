package com.mycompany.app.script

import android.content.Context
import android.database.Cursor
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookScript

@Suppress("deprecation")
class ScriptCache {
    @JvmField
    val scriptsByUrl: LinkedHashMap<String?, Array<Script?>?> = ScriptLruMap(64, 1.0f, true)
    @JvmField
    var criteriaList: Array<ScriptCriteria?>? = null

    internal class ScriptLruMap(initialCapacity: Int, loadFactor: Float, accessOrder: Boolean) :
        LinkedHashMap<String?, Array<Script?>?>(initialCapacity, loadFactor, accessOrder) {
        public override fun removeEldestEntry(entry: MutableMap.MutableEntry<String?, Array<Script?>?>?): Boolean {
            return size > 62
        }
    }

    /** Loads the url criteria (exclude/include/match per script id) of every enabled script into [criteriaList].  */
    fun loadCriteria(context: Context?) {
        var criteria: Array<ScriptCriteria?>? = null
        var cursor: Cursor? = null
        try {
            val args = ArrayList<String?>()
            args.add("1")
            val selectionArgs = args.toTypedArray<String?>()
            val excludes = DbBookScript.patterns(context, "DbBookScript_table_exclude", null, null)
            val includes = DbBookScript.patterns(context, "DbBookScript_table_include", null, null)
            val matches = DbBookScript.patterns(context, "DbBookScript_table_match", null, null)
            cursor = DbUtil.query(
                DbBookScript.get(context).getWritableDatabase(), "DbBookScript_table",
                arrayOf<String?>("_name", "_namespace"), "_enabled = ?", selectionArgs, null
            )!!
            criteria = arrayOfNulls<ScriptCriteria>(cursor.getCount())
            if (cursor.moveToFirst()) {
                val nameColumn = cursor.getColumnIndex("_name")
                val namespaceColumn = cursor.getColumnIndex("_namespace")
                var index = 0
                try {
                    do {
                        val name = cursor.getString(nameColumn)
                        val namespace = cursor.getString(namespaceColumn)
                        val id = ScriptId(name, namespace)
                        criteria[index] = ScriptCriteria(
                            name, namespace, toArray(excludes, id), toArray(includes, id),
                            toArray(matches, id)
                        )
                        index++
                    } while (cursor.moveToNext())
                } catch (ignored: Exception) {
                }
            }
        } catch (exception: Exception) {
            criteria = null
        }
        if (cursor != null) {
            try {
                cursor.close()
            } catch (ignored: Exception) {
            }
        }
        this.criteriaList = criteria
    }

    companion object {
        private fun toArray(map: MutableMap<*, *>?, id: ScriptId?): Array<String?>? {
            val list = if (map == null) null else map.get(id) as MutableList<*>?
            return list?.map { it as String? }?.toTypedArray()
        }
    }
}
