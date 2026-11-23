package com.mycompany.app.db.book

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.text.TextUtils
import com.mycompany.app.data.book.DataBookScript.Companion.instance
import com.mycompany.app.db.DbUtil
import com.mycompany.app.list.book.ListBookScript
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.script.Script
import com.mycompany.app.script.ScriptId
import com.mycompany.app.script.ScriptRequire
import com.mycompany.app.script.ScriptResource
import ch.opumlabs.opumbrowser.R
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

@Suppress("deprecation")
class DbBookScript(context: Context?, name: String?, factory: CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE DbBookScript_table (_name TEXT NOT NULL, _namespace TEXT NOT NULL, _descript TEXT, _downurl TEXT, _updateurl TEXT, _insturl TEXT, _icon TEXT, _runat TEXT, _flags INTEGER, _version TEXT, _content TEXT NOT NULL, _enabled INTEGER NOT NULL DEFAULT 1, _id INTEGER, _time INTEGER, _loccode TEXT, _locname TEXT, _locdesc TEXT, _author TEXT, _rsv01 TEXT, _rsv02 TEXT, _rsv03 TEXT, _rsv04 TEXT, _rsv05 TEXT, _rsv06 TEXT, _rsv07 TEXT, _rsv08 TEXT, _rsv09 TEXT, _rsv10 TEXT, _rsv11 INTEGER, _rsv12 INTEGER, _rsv13 INTEGER, _rsv14 INTEGER, _rsv15 INTEGER, _rsv16 INTEGER, _rsv17 INTEGER, _rsv18 INTEGER, _rsv19 INTEGER, _rsv20 INTEGER, PRIMARY KEY (_name, _namespace));")
        db.execSQL("CREATE TABLE DbBookScript_table_exclude (_name TEXT NOT NULL, _namespace TEXT NOT NULL, _pattern TEXT NOT NULL, PRIMARY KEY (_name, _namespace, _pattern), FOREIGN KEY (_name, _namespace) REFERENCES DbBookScript_table (_name, _namespace) ON UPDATE CASCADE ON DELETE CASCADE);")
        db.execSQL("CREATE TABLE DbBookScript_table_include (_name TEXT NOT NULL, _namespace TEXT NOT NULL, _pattern TEXT NOT NULL, PRIMARY KEY (_name, _namespace, _pattern), FOREIGN KEY (_name, _namespace) REFERENCES DbBookScript_table (_name, _namespace) ON UPDATE CASCADE ON DELETE CASCADE);")
        db.execSQL("CREATE TABLE DbBookScript_table_match (_name TEXT NOT NULL, _namespace TEXT NOT NULL, _pattern TEXT NOT NULL, PRIMARY KEY (_name, _namespace, _pattern), FOREIGN KEY (_name, _namespace) REFERENCES DbBookScript_table (_name, _namespace) ON UPDATE CASCADE ON DELETE CASCADE);")
        db.execSQL("CREATE TABLE DbBookScript_table_value (_name TEXT NOT NULL, _namespace TEXT NOT NULL, _key TEXT NOT NULL, _value TEXT NOT NULL, PRIMARY KEY (_name, _namespace, _key), FOREIGN KEY (_name, _namespace) REFERENCES DbBookScript_table (_name, _namespace) ON UPDATE CASCADE ON DELETE CASCADE);")
        db.execSQL("CREATE TABLE DbBookScript_table_require (_name TEXT NOT NULL, _namespace TEXT NOT NULL, _downurl TEXT NOT NULL, _content TEXT NOT NULL, PRIMARY KEY (_name, _namespace, _downurl), FOREIGN KEY (_name, _namespace) REFERENCES DbBookScript_table (_name, _namespace) ON UPDATE CASCADE ON DELETE CASCADE);")
        db.execSQL("CREATE TABLE DbBookScript_table_resource (_name TEXT NOT NULL, _namespace TEXT NOT NULL, _resname TEXT NOT NULL, _downurl TEXT NOT NULL, _data BLOB NOT NULL, PRIMARY KEY (_name, _namespace, _resname), FOREIGN KEY (_name, _namespace) REFERENCES DbBookScript_table (_name, _namespace) ON UPDATE CASCADE ON DELETE CASCADE);")
    }

    override fun onOpen(sQLiteDatabase: SQLiteDatabase) {
        super.onOpen(sQLiteDatabase)
        sQLiteDatabase.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS DbBookScript_table")
        db.execSQL("DROP TABLE IF EXISTS DbBookScript_table_exclude")
        db.execSQL("DROP TABLE IF EXISTS DbBookScript_table_include")
        db.execSQL("DROP TABLE IF EXISTS DbBookScript_table_match")
        db.execSQL("DROP TABLE IF EXISTS DbBookScript_table_value")
        db.execSQL("DROP TABLE IF EXISTS DbBookScript_table_require")
        db.execSQL("DROP TABLE IF EXISTS DbBookScript_table_resource")
        onCreate(db)
    }

    companion object {
        @JvmField
        var cached: DbBookScript? = null

        @JvmStatic
        fun deleteScript(context: Context?, name: String?, namespace: String?) {
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(namespace)) {
                return
            }
            DbUtil.deleteWhere(
                get(context).getWritableDatabase(),
                "DbBookScript_table",
                "_name=? AND _namespace=?",
                arrayOf<String?>(name!!, namespace!!)
            )
        }

        @JvmStatic
        fun exportFile(context: Context?, fileName: String?): File? {
            if (context == null || TextUtils.isEmpty(fileName)) {
                return null
            }
            var cursor: Cursor? = null
            var writer: BufferedWriter? = null
            var stream: OutputStream? = null
            var wroteLast = false
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookScript_table",
                    null,
                    null,
                    null,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val colUrl = cursor.getColumnIndex("_downurl")
                    val colName = cursor.getColumnIndex("_name")
                    val colContent = cursor.getColumnIndex("_content")
                    stream = MainUtil.openFileOutput(fileName, false)
                    writer = BufferedWriter(OutputStreamWriter(stream, StandardCharsets.UTF_8))
                    do {
                        val url = cursor.getString(colUrl)
                        val name = cursor.getString(colName)
                        val content = cursor.getString(colContent)
                        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(name) || TextUtils.isEmpty(
                                content
                            )
                        ) {
                            wroteLast = false
                            continue
                        }
                        if (!MainUtil.writeLineMark(writer)) {
                            wroteLast = false
                            break
                        }
                        writer.write(url)
                        writer.write("\n")
                        writer.write(name)
                        writer.write("\n")
                        writer.write(content)
                        writer.write("\n")
                        wroteLast = true
                    } while (cursor.moveToNext())
                }
            } catch (unused: Exception) {
                wroteLast = false
            }
            if (cursor != null) {
                cursor.close()
            }
            if (writer != null) {
                try {
                    writer.close()
                } catch (unused2: Exception) {
                }
            }
            if (stream != null) {
                try {
                    stream.close()
                } catch (unused3: Exception) {
                }
            }
            if (wroteLast) {
                return File(fileName!!)
            }
            val file = File(fileName!!)
            if (file.exists()) {
                file.delete()
            }
            return null
        }

        @JvmStatic
        fun get(context: Context?): DbBookScript {
            var dbBookScript: DbBookScript?
            val existing: DbBookScript? = cached
            if (existing != null) {
                return existing
            }
            synchronized(DbBookScript::class.java) {
                dbBookScript = cached
                if (dbBookScript == null) {
                    dbBookScript = DbBookScript(MainUtil.appContextOf(context), "DbBookScript.db", null, 1)
                    cached = dbBookScript
                }
            }
            return dbBookScript!!
        }

        @JvmStatic
        fun valueKeys(context: Context?, name: String?, namespace: String?): Array<String?>? {
            var cursorG: Cursor?
            var keys: Array<String?>? = null
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(namespace)) {
                try {
                    cursorG = DbUtil.query(
                        get(context).getWritableDatabase(),
                        "DbBookScript_table_value",
                        arrayOf<String?>("_key"),
                        "_name=? AND _namespace=?",
                        arrayOf<String?>(name!!, namespace!!),
                        null
                    )
                    try {
                        val arrayList: ArrayList<String?> = ArrayList()
                        if (cursorG != null && cursorG.moveToFirst()) {
                            val columnIndex = cursorG.getColumnIndex("_key")
                            do {
                                arrayList.add(cursorG.getString(columnIndex))
                            } while (cursorG.moveToNext())
                        }
                        keys = arrayList.toTypedArray()
                    } catch (unused: Exception) {
                    }
                } catch (unused2: Exception) {
                    cursorG = null
                }
                if (cursorG != null) {
                    try {
                        cursorG.close()
                    } catch (unused3: Exception) {
                    }
                }
            }
            return keys
        }

        /** Rows of a pattern table (exclude/include/match) grouped by script id: ScriptId to list of "_pattern".  */
        @JvmStatic
        fun patterns(
            context: Context?,
            table: String?,
            selection: String?,
            args: Array<String?>?
        ): HashMap<*, *>? {
            return Companion.grouped(
                context,
                table,
                arrayOf<String?>("_name", "_namespace", "_pattern"),
                selection,
                args,
                0
            )
        }

        /** @require rows grouped by script id: ScriptId to list of ScriptRequire(url, content).
         */
        @JvmStatic
        fun requires(context: Context?, selection: String?, args: Array<String?>?): HashMap<*, *>? {
            return Companion.grouped(
                context,
                "DbBookScript_table_require",
                arrayOf<String?>("_name", "_namespace", "_downurl", "_content"),
                selection,
                args,
                1
            )
        }

        /** @resource rows grouped by script id: ScriptId to list of ScriptResource(data, name, url).
         */
        @JvmStatic
        fun resources(context: Context?, selection: String?, args: Array<String?>?): HashMap<*, *>? {
            return Companion.grouped(
                context,
                "DbBookScript_table_resource",
                arrayOf<String?>("_name", "_namespace", "_downurl", "_resname", "_data"),
                selection,
                args,
                2
            )
        }

        /** kind 0: column 2 as a string; 1: ScriptRequire; 2: ScriptResource. A failure mid-way keeps what was read.  */
        private fun grouped(
            context: Context?,
            table: String?,
            columns: Array<String?>,
            selection: String?,
            args: Array<String?>?,
            mode: Int
        ): HashMap<ScriptId, MutableList<Any?>>? {
            var cursor: Cursor? = null
            var map: HashMap<ScriptId, MutableList<Any?>>? = null
            try {
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(),
                    table,
                    columns,
                    selection,
                    args,
                    null
                )
                map = HashMap()
                if (cursor != null && cursor.moveToFirst()) {
                    val index = IntArray(columns.size)
                    for (index2 in columns.indices) {
                        index[index2] = cursor.getColumnIndex(columns[index2])
                    }
                    try {
                        do {
                            val id = ScriptId(
                                cursor.getString(index[0]), cursor.getString(index[1])
                            )
                            var list = map.get(id)
                            if (list == null) {
                                list = ArrayList()
                                map.put(id, list)
                            }
                            if (mode == 0) {
                                list.add(cursor.getString(2))
                            } else if (mode == 1) {
                                list.add(
                                    ScriptRequire(
                                        cursor.getString(index[2]),
                                        cursor.getString(index[3])
                                    )
                                )
                            } else {
                                list.add(
                                    ScriptResource(
                                        cursor.getBlob(index[4]),
                                        cursor.getString(index[3]), cursor.getString(index[2])
                                    )
                                )
                            }
                        } while (cursor.moveToNext())
                    } catch (ignored: Exception) {
                    }
                }
            } catch (exception: Exception) {
                map = null
            }
            if (cursor != null) {
                try {
                    cursor.close()
                } catch (ignored: Exception) {
                }
            }
            return map
        }

        @JvmStatic
        fun findScript(context: Context?, name: String?, namespace: String?): Script? {
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(namespace)) {
                try {
                    val scriptArrJ: Array<Script?>? =
                        loadScripts(context, arrayOf<ScriptId>(ScriptId(name, namespace)), null)
                    if (scriptArrJ != null && scriptArrJ.size != 0) {
                        return scriptArrJ[0]
                    }
                } catch (unused: Exception) {
                }
            }
            return null
        }

        /**
         * Loads the scripts with the given ids (optionally only enabled / disabled ones), with their url patterns,
         * @require and @resource data. Rows that fail to load leave null slots.
         */
        @JvmStatic
        fun loadScripts(context: Context?, ids: Array<ScriptId>, enabled: Boolean?): Array<Script?>? {
            val where = StringBuilder()
            val args = ArrayList<String?>()
            if (ids.size == 0) {
                return arrayOfNulls<Script>(0)
            }
            addIdWhere(ids, where, args)
            val idWhere = where.toString()
            val idArgs = args.toTypedArray<String?>()
            if (enabled != null) {
                where.insert(0, "(").append(")").append(" AND ")
                where.append("_enabled").append(" = ?")
                args.add(if (enabled) "1" else "0")
            }
            var scripts: Array<Script?>? = null
            var cursor: Cursor? = null
            try {
                val excludes: HashMap<*, *>? =
                    patterns(context, "DbBookScript_table_exclude", idWhere, idArgs)
                val includes: HashMap<*, *>? =
                    patterns(context, "DbBookScript_table_include", idWhere, idArgs)
                val matches: HashMap<*, *>? =
                    patterns(context, "DbBookScript_table_match", idWhere, idArgs)
                val requires: HashMap<*, *>? = requires(context, idWhere, idArgs)
                val resources: HashMap<*, *>? = resources(context, idWhere, idArgs)
                cursor = DbUtil.query(
                    get(context).getWritableDatabase(), "DbBookScript_table",
                    arrayOf<String?>(
                        "_name",
                        "_namespace",
                        "_descript",
                        "_downurl",
                        "_updateurl",
                        "_insturl",
                        "_icon",
                        "_runat",
                        "_flags",
                        "_version",
                        "_content",
                        "_enabled",
                        "_id",
                        "_time",
                        "_loccode",
                        "_locname",
                        "_locdesc",
                        "_author"
                    ),
                    where.toString(), args.toTypedArray<String?>(), null
                )!!
                scripts = arrayOfNulls<Script>(cursor.getCount())
                if (cursor.moveToFirst()) {
                    val nameColumn = cursor.getColumnIndex("_name")
                    val namespaceColumn = cursor.getColumnIndex("_namespace")
                    val descColumn = cursor.getColumnIndex("_descript")
                    val downColumn = cursor.getColumnIndex("_downurl")
                    val updateColumn = cursor.getColumnIndex("_updateurl")
                    val installColumn = cursor.getColumnIndex("_insturl")
                    val iconColumn = cursor.getColumnIndex("_icon")
                    val runAtColumn = cursor.getColumnIndex("_runat")
                    val flagsColumn = cursor.getColumnIndex("_flags")
                    val versionColumn = cursor.getColumnIndex("_version")
                    val contentColumn = cursor.getColumnIndex("_content")
                    val enabledColumn = cursor.getColumnIndex("_enabled")
                    val idColumn = cursor.getColumnIndex("_id")
                    val timeColumn = cursor.getColumnIndex("_time")
                    val locCodeColumn = cursor.getColumnIndex("_loccode")
                    val locNameColumn = cursor.getColumnIndex("_locname")
                    val locDescColumn = cursor.getColumnIndex("_locdesc")
                    val authorColumn = cursor.getColumnIndex("_author")
                    var index = 0
                    try {
                        do {
                            val name = cursor.getString(nameColumn)
                            val namespace = cursor.getString(namespaceColumn)
                            val id = ScriptId(name, namespace)
                            val req =
                                if (requires == null) null else requires.get(id) as MutableList<*>?
                            val res =
                                if (resources == null) null else resources.get(id) as MutableList<*>?
                            val script = Script(
                                name,
                                namespace,
                                strings(excludes, id),
                                strings(includes, id),
                                strings(matches, id),
                                cursor.getString(descColumn),
                                cursor.getString(downColumn),
                                cursor.getString(updateColumn),
                                cursor.getString(installColumn),
                                cursor.getString(iconColumn),
                                cursor.getString(runAtColumn),
                                cursor.getInt(flagsColumn),
                                cursor.getString(versionColumn),
                                req?.filterIsInstance<ScriptRequire>()?.toTypedArray<ScriptRequire?>(),
                                res?.filterIsInstance<ScriptResource>()?.toTypedArray<ScriptResource?>(),
                                cursor.getString(contentColumn),
                                cursor.getString(locCodeColumn),
                                cursor.getString(locNameColumn),
                                cursor.getString(locDescColumn),
                                cursor.getString(authorColumn)
                            )
                            script.rowId = cursor.getLong(idColumn)
                            script.updatedAt = cursor.getLong(timeColumn)
                            script.enabled = cursor.getInt(enabledColumn) == 1
                            scripts[index++] = script
                        } while (cursor.moveToNext())
                    } catch (ignored: Exception) {
                    }
                }
            } catch (ignored: Exception) {
            }
            if (cursor != null) {
                try {
                    cursor.close()
                } catch (ignored: Exception) {
                }
            }
            return scripts
        }

        private fun strings(map: MutableMap<*, *>?, id: ScriptId?): Array<String?>? {
            val list = if (map == null) null else map.get(id) as MutableList<*>?
            return list?.map { it as String? }?.toTypedArray()
        }

        @JvmStatic
        fun getValue(context: Context?, name: String?, namespace: String?, key: String?): String? {
            var cursorG: Cursor?
            var valueText: String? = null
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(namespace) && !TextUtils.isEmpty(key)) {
                try {
                    cursorG = DbUtil.query(
                        get(context).getWritableDatabase(),
                        "DbBookScript_table_value",
                        arrayOf<String?>("_value"),
                        "_name=? AND _namespace=? AND _key=?",
                        arrayOf<String?>(name!!, namespace!!, key!!),
                        null
                    )
                    if (cursorG != null) {
                        try {
                            if (cursorG.moveToFirst()) {
                                valueText = cursorG.getString(cursorG.getColumnIndex("_value"))
                            }
                        } catch (unused: Exception) {
                        }
                    }
                } catch (unused2: Exception) {
                    cursorG = null
                }
                if (cursorG != null) {
                    try {
                        cursorG.close()
                    } catch (unused3: Exception) {
                    }
                }
            }
            return valueText
        }

        @JvmStatic
        fun addIdWhere(scriptIdArr: Array<ScriptId>, sb: StringBuilder, arrayList: ArrayList<String?>) {
            try {
                for (scriptId in scriptIdArr) {
                    sb.append(" OR (")
                    sb.append("_name")
                    sb.append("=? AND ")
                    sb.append("_namespace")
                    sb.append("=?)")
                    arrayList.add(scriptId.name)
                    arrayList.add(scriptId.namespace)
                }
                sb.delete(0, 4)
            } catch (unused: Exception) {
            }
        }

        /**
         * Saves a script (replacing an edited one, old, when its id changed), keeping the stored _id/_enabled of an
         * existing row with the same name/namespace, plus its pattern, @require and @resource rows. Returns the list
         * item for it (localized name/description when its locale is lang or subLang), or null without a download url.
         */
        @JvmStatic
        fun saveScript(
            context: Context?,
            script: Script,
            old: ChildItem?,
            lang: String?,
            subLang: String?
        ): ChildItem? {
            var old = old
            val author = script.authorName
            val locDesc = script.locDesc
            val locName = script.locName
            val locCode = script.locCode
            val version = script.version
            val icon = script.iconText
            val downUrl = script.downloadUrl
            val description = script.description
            val namespace = script.namespace
            val name = script.name
            if (old != null && !(MainUtil.sameObject(name, old.name) && MainUtil.sameObject(namespace, old.namespace))) {
                deleteScript(context, old.name, old.namespace)
                instance(context).removeId(old.id)
                old = null
            }
            var existing = old
            if (old == null && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(namespace)) {
                var cursor: Cursor? = null
                try {
                    cursor = DbUtil.query(
                        get(context).getWritableDatabase(),
                        "DbBookScript_table",
                        arrayOf<String?>("_id", "_enabled"),
                        "_name=? AND _namespace=?",
                        arrayOf<String?>(name!!, namespace!!),
                        null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val idColumn = cursor.getColumnIndex("_id")
                        val enabledColumn = cursor.getColumnIndex("_enabled")
                        existing = ChildItem()
                        existing.name = name
                        existing.namespace = namespace
                        try {
                            existing.id = cursor.getLong(idColumn)
                            existing.isEnabled = cursor.getInt(enabledColumn) == 1
                        } catch (ignored: Exception) {
                        }
                    }
                } catch (exception: Exception) {
                    existing = null
                }
                if (cursor != null) {
                    try {
                        cursor.close()
                    } catch (ignored: Exception) {
                    }
                }
            }
            val now = System.currentTimeMillis()
            if (existing != null && existing.id > 0) {
                script.rowId = existing.id
                script.enabled = existing.isEnabled
            } else {
                script.rowId = now
                script.enabled = true
            }
            script.updatedAt = now
            deleteScript(context, name, namespace)
            val base = ContentValues()
            base.put("_name", name)
            base.put("_namespace", namespace)
            val excludes: ArrayList<ContentValues?> = patterns(base, script.excludes)
            val includes: ArrayList<ContentValues?> = patterns(base, script.includes)
            val matches: ArrayList<ContentValues?> = patterns(base, script.matches)
            val requires = ArrayList<ContentValues?>()
            if (script.requires != null) {
                for (require in script.requires) {
                    val values = ContentValues(base)
                    values.put("_downurl", require!!.url)
                    values.put("_content", require.content)
                    requires.add(values)
                }
            }
            val resources = ArrayList<ContentValues?>()
            if (script.resourceList != null) {
                for (resource in script.resourceList) {
                    val values = ContentValues(base)
                    values.put("_resname", resource!!.name)
                    values.put("_downurl", resource.url)
                    values.put("_data", resource.data)
                    resources.add(values)
                }
            }
            var id = script.rowId
            var time = script.updatedAt
            var enabled = script.enabled
            if (id == 0L || time == 0L) {
                time = System.currentTimeMillis()
                id = time
                enabled = true
            }
            val values = ContentValues(base)
            values.put("_descript", description)
            values.put("_downurl", downUrl)
            values.put("_updateurl", script.updateUrl)
            values.put("_insturl", script.installUrl)
            values.put("_icon", icon)
            values.put("_runat", script.runAt)
            values.put("_flags", script.flags)
            values.put("_version", version)
            values.put("_content", script.iconUrl)
            values.put("_enabled", if (enabled) 1 else 0)
            values.put("_id", id)
            values.put("_time", time)
            values.put("_loccode", locCode)
            values.put("_locname", locName)
            values.put("_locdesc", locDesc)
            values.put("_author", author)
            try {
                val db: SQLiteDatabase? = get(context).getWritableDatabase()
                if (DbUtil.insert(db, "DbBookScript_table", values) != 0L && insertAll(
                        db,
                        "DbBookScript_table_exclude",
                        excludes
                    )
                    && insertAll(db, "DbBookScript_table_include", includes)
                    && insertAll(db, "DbBookScript_table_match", matches)
                    && insertAll(db, "DbBookScript_table_require", requires)
                ) {
                    insertAll(db, "DbBookScript_table_resource", resources)
                }
            } catch (ignored: Exception) {
            }
            if (script.rowId == 0L || TextUtils.isEmpty(downUrl) || TextUtils.isEmpty(name) || TextUtils.isEmpty(
                    namespace
                )
            ) {
                return null
            }
            val item = ChildItem()
            item.id = script.rowId
            item.path = downUrl
            item.name = name
            item.namespace = namespace
            item.time = script.updatedAt
            item.isEnabled = script.enabled
            item.itemType = 0
            item.iconColor = -460552
            item.iconRes = R.drawable.outline_extension_black_24
            item.subText = if (TextUtils.isEmpty(version)) null else "v" + version
            item.dir = icon
            item.dirName = description
            item.author = author
            if (!TextUtils.isEmpty(locCode) && (locCode == subLang || locCode == lang)) {
                item.badge = locName
                item.mimeType = locDesc
            }
            ListBookScript.applyFold(item)
            return item
        }

        private fun patterns(
            base: ContentValues?,
            patterns: Array<String?>?
        ): ArrayList<ContentValues?> {
            val list = ArrayList<ContentValues?>()
            if (patterns != null) {
                for (pattern in patterns) {
                    val values = ContentValues(base)
                    values.put("_pattern", pattern)
                    list.add(values)
                }
            }
            return list
        }

        /** Inserts the rows in order; false at the first failed insert.  */
        private fun insertAll(
            db: SQLiteDatabase?,
            table: String?,
            rows: ArrayList<ContentValues?>
        ): Boolean {
            for (row in rows) {
                if (DbUtil.insert(db, table, row) == 0L) {
                    return false
                }
            }
            return true
        }

        @JvmStatic
        fun putValue(context: Context?, name: String?, namespace: String?, key: String?, valueText: String?) {
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(namespace) || TextUtils.isEmpty(key)) {
                return
            }
            val writableDatabase: SQLiteDatabase? = get(context).getWritableDatabase()
            val args: Array<String?>? = arrayOf<String?>(name!!, namespace!!, key!!)
            val exists = DbUtil.existsWhere(writableDatabase, "DbBookScript_table_value", arrayOf<String?>("_value"), "_name=? AND _namespace=? AND _key=?", args)
            if (exists == 0) {
                return
            }
            val contentValuesG = android.content.ContentValues().also { cv -> cv.put("_value", valueText) }
            if (exists == 1) {
                DbUtil.updateWhere(
                    writableDatabase,
                    "DbBookScript_table_value",
                    contentValuesG,
                    "_name=? AND _namespace=? AND _key=?",
                    args
                )
                return
            }
            contentValuesG.put("_name", name)
            contentValuesG.put("_namespace", namespace)
            contentValuesG.put("_key", key)
            DbUtil.insert(writableDatabase, "DbBookScript_table_value", contentValuesG)
        }
    }
}
