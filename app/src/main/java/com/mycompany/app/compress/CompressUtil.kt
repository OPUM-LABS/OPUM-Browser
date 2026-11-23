package com.mycompany.app.compress

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.Md5FileNameGenerator
import net.lingala.zip4j.io.ZipInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URLDecoder

@Suppress("deprecation")
object CompressUtil {
    @JvmField
    val extractedPaths: ArrayList<String?> = ArrayList()

    @JvmStatic
    fun sortItemFor(name: String?): Compress.SortItem? {
        var name = name
        if (TextUtils.isEmpty(name)) {
            return null
        }
        val sortItem = Compress.SortItem()
        while (name!!.endsWith("/")) {
            if (name.length <= 1) {
                sortItem.name = name
                sortItem.path = null
                sortItem.sortKey = name
                return sortItem
            }
            name = name.let { s -> s.substring(0, s.length - 1) }
        }
        val lastSlash = name.lastIndexOf(47.toChar())
        val nameStart = if (lastSlash == -1) 0 else (lastSlash + 1)
        if (lastSlash == -1 || nameStart >= name.length) {
            sortItem.name = name
            sortItem.path = null
            sortItem.sortKey = name
            return sortItem
        }
        sortItem.name = name
        sortItem.path = name.substring(0, lastSlash)
        sortItem.sortKey = name.substring(nameStart)
        return sortItem
    }

    @JvmStatic
    fun realPath(context: Context?, source: String?): String? {
        if (!MainUri.isContentUri(source)) {
            return source
        }
        val fileName = MainUri.fileNameOf(context, source)
        try {
            if (File(fileName!!).canRead()) {
                return fileName
            }
        } catch (unused: Exception) {
        }
        val viewerDir = MainUtil.cacheFilePath(context, ".viewer")
        try {
            val file = File(viewerDir!!)
            if (!file.exists()) {
                file.mkdirs()
            }
        } catch (unused2: Exception) {
        }
        val hashName = Md5FileNameGenerator().generate(source)
        val cachedPath = "$viewerDir/$hashName"
        try {
            val arrayList = extractedPaths
            if (arrayList.contains(hashName)) {
                if (File(cachedPath).exists()) {
                    return cachedPath
                }
            } else {
                arrayList.add(hashName)
                if (arrayList.size > 10) {
                    File(viewerDir + "/" + (arrayList.get(0))).delete()
                    arrayList.removeAt(0)
                }
            }
        } catch (unused3: Exception) {
        }
        if (!MainUri.copyUri(context, source, cachedPath, true, false)) {
            var filePart: String? = null
            if (!TextUtils.isEmpty(source) && source!!.startsWith("content://")) {
                try {
                    val decoded = URLDecoder.decode(source, "UTF-8")
                    val fileIndex = decoded.indexOf("file://", 10)
                    if (fileIndex != -1) {
                        filePart = decoded.substring(fileIndex)
                    }
                } catch (unused4: Exception) {
                }
            }
            if (!MainUtil.copyFile(filePart, cachedPath)) {
                return source
            }
        }
        return cachedPath
    }

    @JvmStatic
    fun readBytes(inputStream: InputStream?, length: Int): ByteArray? {
        var length = length
        var bArr: ByteArray? = null
        if (inputStream == null) {
            return null
        }
        try {
            val byteArray = ByteArray(length)
            var index = 0
            while (length > 0) {
                val read = inputStream.read(byteArray, index, length)
                if (read == -1) {
                    break
                }
                index += read
                length -= read
            }
            if (length != 0) {
                Log.e("CompressUtil", "size = " + length)
            }
            bArr = byteArray
        } catch (unused: Exception) {
        } catch (unused: OutOfMemoryError) {
        }
        try {
            inputStream.close()
        } catch (unused2: Exception) {
        }
        return bArr
    }

    @JvmStatic
    fun unzipEntry(
        context: Context?,
        zipInputStream: ZipInputStream,
        input: String?,
        entryName: String?,
        total: Long,
        compressListener: CompressListener?
    ): Boolean {
        val bArr: ByteArray?
        var read: Int = 0
        var found = false
        if (!TextUtils.isEmpty(input) && !TextUtils.isEmpty(entryName)) {
            var done: Long = 0
            var outputStreamOpenOutputStream: OutputStream? = null
            if (total != 0L) {
                val uriItemC = MainUri.createUri(context, input, null, entryName)
                if (uriItemC != null) {
                    try {
                        outputStreamOpenOutputStream =
                            context!!.getContentResolver().openOutputStream(uriItemC.uri!!)
                    } catch (unused: Exception) {
                    }
                    val outputStream = outputStreamOpenOutputStream
                    if (compressListener != null) {
                        compressListener.onEntry(entryName)
                        if (compressListener.isCancelled) {
                            found = true
                        } else if (outputStream != null) {
                            try {
                                bArr = ByteArray(8192)
                                while (true) {
                                    read = zipInputStream.read(bArr, 0, 8192)
                                    if (read == -1 || (compressListener.isCancelled)) {
                                        break
                                    }
                                    outputStream.write(bArr, 0, read)
                                    val doneNow = done + (read.toLong())
                                    compressListener.onProgress(entryName, total, doneNow)
                                    done = doneNow
                                }
                                found = true
                            } catch (unused2: Exception) {
                            }
                        }
                    } else if (outputStream != null) {
                        found = true
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close()
                        } catch (unused3: Exception) {
                        }
                    }
                }
            } else if (MainUri.createUri(context, input, null, entryName) != null) {
                return true
            }
        }
        return found
    }

    interface CompressListener {
        fun onDone(path: String?, isOk: Boolean)

        fun onEntry(entryName: String?)

        fun onProgress(entryName: String?, total: Long, done: Long)

        val isCancelled: Boolean
    }

    class CompressSort : Comparator<Compress.SortItem?> {
        override fun compare(first: Compress.SortItem?, second: Compress.SortItem?): Int {
            val sortItem = first
            val other = second
            if (sortItem == null) {
                return if (other == null) 0 else 1
            }
            if (other == null) {
                return -1
            }
            val order = MainUtil.compareStringsIgnoreCase(sortItem.path, other.path, false)
            return if (order != 0) order else MainUtil.compareStrings(sortItem.sortKey, other.sortKey, false)
        }
    }
}
