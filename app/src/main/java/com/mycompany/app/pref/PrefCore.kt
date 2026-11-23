package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService

@Suppress("deprecation")
open class PrefCore(context: Context?, prefName: String?) {
    @JvmField
    val dataLock: Any = Any()
    @JvmField
    val saveLock: Any = Any()
    @JvmField
    var oldPath: String? = null
    @JvmField
    var filePath: String? = null
    @JvmField
    var json: JsonObject? = null
    @JvmField
    var saving: Boolean = false
    @JvmField
    var saveAgain: Boolean = false
    @JvmField
    var prefExecutor: ExecutorService? = null

    internal inner class SaveTask : Runnable {
        override fun run() {
            this@PrefCore.save()
        }
    }

    init {
        if (context != null) {
            init(context, prefName)
        }
    }

    fun saveAsync() {
        val saveTask: SaveTask = SaveTask()
        val executorService = this.prefExecutor
        if (executorService == null) {
            return
        }
        try {
            executorService.execute(saveTask)
        } catch (unused: Exception) {
        }
    }

    fun writeFile() {
        var bufferedWriter: BufferedWriter?
        val save: String?
        if (this.json == null || TextUtils.isEmpty(this.filePath)) {
            return
        }
        var outputStream: OutputStream? = null
        try {
            synchronized(this.dataLock) {
                save = this.json.toString()
            }
            if (TextUtils.isEmpty(save)) {
                return
            }
            if (!TextUtils.isEmpty(this.oldPath)) {
                val file = File(this.oldPath!!)
                if (!file.isDirectory()) {
                    file.mkdir()
                }
            }
            var outputStreamB = MainUtil.openFileOutput(this.filePath, false)
            try {
                bufferedWriter =
                    BufferedWriter(OutputStreamWriter(outputStreamB, StandardCharsets.UTF_8))
                try {
                    bufferedWriter.write(save)
                } catch (unused: Exception) {
                    outputStream = outputStreamB
                    outputStreamB = outputStream
                }
            } catch (unused2: Exception) {
                bufferedWriter = null
            }
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close()
                } catch (unused3: Exception) {
                }
            }
            if (outputStreamB != null) {
                try {
                    outputStreamB.close()
                } catch (unused4: Exception) {
                }
            }
        } catch (unused5: Exception) {
            bufferedWriter = null
        }
    }

    fun save() {
        synchronized(this.saveLock) {
            if (this.saving) {
                this.saveAgain = true
                return
            }
            this.saving = true
            writeFile()
            val saveAgain = this.saveAgain
            this.saveAgain = false
            if (saveAgain) {
                writeFile()
            }
            synchronized(this.saveLock) {
                this.saving = false
            }
        }
    }

    fun readBool(key: String?, fallback: Boolean): Boolean {
        val jsonObject = this.json
        val jsonElementD: JsonElement? = if (jsonObject != null) jsonObject.get(key) else null
        if (jsonObject != null && jsonElementD != null) {
            try {
                if (jsonElementD !is JsonNull) {
                    return jsonElementD.getAsBoolean()
                }
            } catch (unused: Exception) {
            }
        }
        return fallback
    }

    fun readFloat(key: String?, fallback: Float): Float {
        val jsonObject = this.json
        val jsonElementD: JsonElement? = if (jsonObject != null) jsonObject.get(key) else null
        if (jsonObject != null && jsonElementD != null) {
            try {
                if (jsonElementD !is JsonNull) {
                    return jsonElementD.getAsFloat()
                }
            } catch (unused: Exception) {
            }
        }
        return fallback
    }

    fun readInt(fallback: Int, key: String?): Int {
        val jsonObject = this.json
        val jsonElementD: JsonElement? = if (jsonObject != null) jsonObject.get(key) else null
        if (jsonObject != null && jsonElementD != null) {
            try {
                if (jsonElementD !is JsonNull) {
                    return jsonElementD.getAsInt()
                }
            } catch (unused: Exception) {
            }
        }
        return fallback
    }

    fun readLong(fallback: Long, key: String?): Long {
        val jsonObject = this.json
        val jsonElementD: JsonElement? = if (jsonObject != null) jsonObject.get(key) else null
        if (jsonObject != null && jsonElementD != null) {
            try {
                if (jsonElementD !is JsonNull) {
                    return jsonElementD.getAsLong()
                }
            } catch (unused: Exception) {
            }
        }
        return fallback
    }

    fun readString(key: String?, fallback: String?): String? {
        val jsonObject = this.json
        val jsonElementD: JsonElement? = if (jsonObject != null) jsonObject.get(key) else null
        if (jsonObject != null && jsonElementD != null) {
            try {
                val text: String? = if (jsonElementD !is JsonNull) jsonElementD.getAsString() else null
                if (jsonElementD !is JsonNull && text != null) {
                    return text
                }
            } catch (unused: Exception) {
            }
        }
        return fallback
    }

    fun init(context: Context?, fileName: String?) {
        if (context != null) {
            this.prefExecutor = MainApp.executor(context)
            try {
                val path = File(context.getFilesDir(), ".pref").getPath()
                if (TextUtils.isEmpty(path)) {
                    return
                }
                this.oldPath = path
                this.filePath = path + "/" + fileName
                load()
            } catch (unused: Exception) {
            }
        }
    }

    fun notLoaded(): Boolean {
        return this.json == null
    }

    /** Loads the stored preference JSON from [.d] into [.e] (kept as is if unreadable).  */
    fun load() {
        if (TextUtils.isEmpty(this.filePath) || !File(this.filePath!!).exists()) {
            return
        }
        var json: String? = null
        var `in`: InputStream? = null
        var reader: BufferedReader? = null
        try {
            `in` = MainUtil.openFileInput(this.filePath)
            reader = BufferedReader(InputStreamReader(`in`, StandardCharsets.UTF_8))
            val sb = StringBuilder()
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                sb.append(line)
            }
            json = sb.toString()
        } catch (unused: Exception) {
        }
        if (reader != null) {
            try {
                reader.close()
            } catch (unused: Exception) {
            }
        }
        if (`in` != null) {
            try {
                `in`.close()
            } catch (unused: Exception) {
            }
        }
        if (TextUtils.isEmpty(json)) {
            return
        }
        var loaded: JsonObject? = null
        try {
            loaded = JsonParser.parseString(json).getAsJsonObject()
        } catch (unused: Exception) {
        }
        if (loaded != null) {
            synchronized(this.dataLock) {
                this.json = loaded
            }
        }
    }

    fun putBool(key: String?, user: Boolean) {
        synchronized(this.dataLock) {
            var jsonObject = this.json
            if (jsonObject == null) {
                jsonObject = JsonObject()
                this.json = jsonObject
            }
            jsonObject.addProperty(key, user)
        }
    }

    fun putFloat(key: String?, rate: Float) {
        synchronized(this.dataLock) {
            var jsonObject = this.json
            if (jsonObject == null) {
                jsonObject = JsonObject()
                this.json = jsonObject
            }
            jsonObject.addProperty(key, rate)
        }
    }

    fun putInt(days: Int, key: String?) {
        synchronized(this.dataLock) {
            var jsonObject = this.json
            if (jsonObject == null) {
                jsonObject = JsonObject()
                this.json = jsonObject
            }
            jsonObject.addProperty(key, days)
        }
    }

    fun putLong(now: Long, key: String?) {
        synchronized(this.dataLock) {
            var jsonObject = this.json
            if (jsonObject == null) {
                jsonObject = JsonObject()
                this.json = jsonObject
            }
            jsonObject.addProperty(key, now)
        }
    }

    fun putString(key: String?, lang: String?) {
        var lang = lang
        if (lang == null) {
            lang = ""
        }
        synchronized(this.dataLock) {
            var jsonObject = this.json
            if (jsonObject == null) {
                jsonObject = JsonObject()
                this.json = jsonObject
            }
            jsonObject.addProperty(key, lang)
        }
    }

    /** The file holds a value for [key] (stored once; absent = never changed). */
    fun has(key: String?): Boolean = synchronized(this.dataLock) { this.json?.has(key) == true }

    fun removeKey(key: String?) {
        synchronized(this.dataLock) {
            val jsonObject = this.json
            if (jsonObject == null) {
                return
            }
            jsonObject.remove(key)
        }
    }
}
