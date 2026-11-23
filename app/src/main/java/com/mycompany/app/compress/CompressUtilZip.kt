package com.mycompany.app.compress

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import com.mycompany.app.compress.CompressUtil.CompressListener
import com.mycompany.app.compress.CompressUtil.CompressSort
import com.mycompany.app.main.BitmapUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import net.lingala.zip4j.model.FileHeader
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Collections
import java.util.Enumeration
import java.util.LinkedList
import java.util.Locale

@Suppress("deprecation")
open class CompressUtilZip(context: Context?, path: String?, referer: String?) : Compress(context, path, referer) {
    @JvmField
    var openZip: ZipFile? = null
    @JvmField
    var zip2: CompressUtilZip2? = null

    override fun load() {
        try {
            var sortItemB: SortItem?
            var arrayList: ArrayList<SortItem?>?
            val sortItemsNow: ArrayList<SortItem?>?
            var arrayListD: List<FileHeader?>?
            var pathItem: SortItem?
            var sortItemsList: ArrayList<SortItem?>?
            var index = 0
            this.entryTotal = 0
            if (open()) {
                val isZip = archiveType() == 3
                this.sortItems = ArrayList()
                this.entryTotal = 0
                val compressUtilZip = this.zip2
                if (compressUtilZip == null) {
                    val zipFile = this.openZip
                    if (zipFile != null) {
                        val enumeration: Enumeration<*> = zipFile.entries
                        var retry = isZip
                        while (enumeration.hasMoreElements()) {
                            val zipArchiveEntry = enumeration.nextElement() as ZipArchiveEntry?
                            if (zipArchiveEntry != null && !zipArchiveEntry.isDirectory()) {
                                val name = zipArchiveEntry.getName()
                                if (isZip) {
                                    if (retry) {
                                        if ("icon_album" == name) {
                                            retry = false
                                        } else {
                                            retry = false
                                        }
                                    }
                                    sortItemB = CompressUtil.sortItemFor(name)
                                    if (sortItemB == null) {
                                        continue
                                    } else {
                                        arrayList = this.sortItems
                                        if (arrayList == null) {
                                            this.sortItems = null
                                            return
                                        }
                                        arrayList.add(sortItemB)
                                    }
                                } else if (Compress.Companion.isImageName(name, true, true)) {
                                    sortItemB = CompressUtil.sortItemFor(name)
                                    if (sortItemB == null) {
                                        continue
                                    } else {
                                        arrayList = this.sortItems
                                        if (arrayList == null) {
                                            this.sortItems = null
                                            return
                                        }
                                        arrayList.add(sortItemB)
                                    }
                                } else {
                                    continue
                                }
                            }
                        }
                    }
                    sortItemsNow = this.sortItems
                    if (sortItemsNow != null) {
                        return
                    } else {
                        return
                    }
                }
                val zipFile = compressUtilZip.openZip
                if (zipFile != null) {
                    try {
                        arrayListD = zipFile.headerList()
                    } catch (unused: Exception) {
                        arrayListD = null
                    }
                } else {
                    arrayListD = null
                }
                if (arrayListD == null || arrayListD.isEmpty()) {
                    return
                }
                val size = arrayListD.size
                var headerIndex = 0
                var retry = isZip
                while (headerIndex < size) {
                    val rawHeader = arrayListD.get(headerIndex)
                    headerIndex++
                    val fileHeader = rawHeader
                    if (fileHeader != null && !fileHeader.isDirectory) {
                        val fileName = fileHeader.fileName
                        if (isZip) {
                            if (retry) {
                                if ("icon_album" == fileName) {
                                    retry = false
                                } else {
                                    retry = false
                                }
                            }
                            pathItem = CompressUtil.sortItemFor(fileName)
                            if (pathItem == null) {
                                continue
                            } else {
                                sortItemsList = this.sortItems
                                if (sortItemsList != null) {
                                    this.sortItems = null
                                    return
                                }
                                sortItemsList!!.add(pathItem)
                            }
                        } else if (Compress.Companion.isImageName(fileName, true, true)) {
                            pathItem = CompressUtil.sortItemFor(fileName)
                            if (pathItem == null) {
                                continue
                            } else {
                                sortItemsList = this.sortItems
                                if (sortItemsList != null) {
                                    this.sortItems = null
                                    return
                                }
                                sortItemsList!!.add(pathItem)
                            }
                        } else {
                            continue
                        }
                    }
                }
                sortItemsNow = this.sortItems
                if (sortItemsNow != null || sortItemsNow!!.isEmpty()) {
                    return
                }
                val entryTotal = this.sortItems!!.size
                this.entryTotal = entryTotal
                if (entryTotal <= 0 || this.sortItems == null || this.pathNames == null) {
                    return
                }
                if (archiveType() != 3) {
                    try {
                        Collections.sort<SortItem?>(this.sortItems!!, CompressSort())
                    } catch (unused2: Exception) {
                    }
                }
                val sortItems = this.sortItems
                val sortItemsSize = sortItems!!.size
                while (index < sortItemsSize) {
                    val rawSortItem: Any? = sortItems.get(index)
                    index++
                    val sortItem = rawSortItem as SortItem
                    val pathNames: ArrayList<String?>? = if (this.sortItems == null) null else this.pathNames
                    if (this.sortItems == null || pathNames == null) {
                        return
                    } else {
                        pathNames.add(sortItem.name)
                    }
                }
                this.sortItems = null
            }
        } catch (_ex: Throwable) {
            throw RuntimeException(_ex)
        }
    }

    override fun open(): Boolean {
        try {
            var nonUtf: Boolean = false
            this.entryTotal = 0
            if (!TextUtils.isEmpty(this.path)) {
                if (TextUtils.isEmpty(this.charsetName)) {
                    this.charsetName = MainConst.defaultCharset
                }
                var arrayListD: List<FileHeader?>? = null
                this.zip2 = null
                if (TextUtils.isEmpty(this.password) && MainUtil.fileSizeOf(this.context, this.path) < 2146435072) {
                    if (TextUtils.isEmpty(this.realFilePath)) {
                        this.realFilePath = CompressUtil.realPath(this.context, this.path)
                    }
                    try {
                        val zipFile = ZipFile(this.realFilePath, this.charsetName)
                        this.openZip = zipFile
                        val linkedList = java.util.Collections.list(zipFile.entries)
                        this.entryTotal = if (linkedList == null) 0 else linkedList.size
                        nonUtf = false
                    } catch (unused: Exception) {
                        nonUtf = "UTF-8" != this.charsetName
                    }
                    if (nonUtf) {
                        try {
                            this.charsetName = "UTF-8"
                            val zipFile = ZipFile(this.realFilePath, this.charsetName)
                            this.openZip = zipFile
                            val linkedList = java.util.Collections.list(zipFile.entries)
                            this.entryTotal = if (linkedList == null) 0 else linkedList.size
                        } catch (unused2: Exception) {
                        }
                    }
                    return this.entryTotal > 0
                }
                this.zip2 = CompressUtilZip2()
                if (TextUtils.isEmpty(this.realFilePath)) {
                    this.realFilePath = CompressUtil.realPath(this.context, this.path)
                }
                if (this.zip2!!.extract(this.realFilePath, this.charsetName, this.password)) {
                    val zipFile = this.zip2!!.openZip
                    if (zipFile != null) {
                        try {
                            arrayListD = zipFile.headerList()
                        } catch (unused3: Exception) {
                        }
                    }
                    this.entryTotal = if (arrayListD == null) 0 else arrayListD.size
                } else if ("UTF-8" != this.charsetName) {
                    this.charsetName = "UTF-8"
                    if (this.zip2!!.extract(this.realFilePath, "UTF-8", this.password)) {
                        val zipFile = this.zip2!!.openZip
                        if (zipFile != null) {
                            try {
                                arrayListD = zipFile.headerList()
                            } catch (unused4: Exception) {
                            }
                        }
                        this.entryTotal = if (arrayListD == null) 0 else arrayListD.size
                    }
                }
                if (this.entryTotal > 0) {
                    return true
                }
            }
            return false
        } catch (_ex: Throwable) {
            throw RuntimeException(_ex)
        }
    }

    override fun release() {
        super.release()
        val compressUtilZip = this.zip2
        if (compressUtilZip != null) {
            compressUtilZip.openZip = null
            this.zip2 = null
        }
        val zipFile = this.openZip
        if (zipFile != null) {
            try {
                zipFile.close()
            } catch (unused: Exception) {
            }
            this.openZip = null
        }
    }

    override fun extractTo(target: String?, compressListener: CompressListener?): Boolean {
        try {
            var arrayListD: List<FileHeader?>?
            var listener: CompressListener?
            var path: String?
            var read = 0
            var extracted = false
            val compressUtilZip: CompressUtilZip2? = if (TextUtils.isEmpty(this.path)) null else this.zip2
            if (TextUtils.isEmpty(this.path) || compressUtilZip == null) {
                return false
            }
            val context = this.context
            val zipFile = compressUtilZip.openZip
            if (zipFile == null) {
                return false
            }
            try {
                arrayListD = zipFile.headerList()
            } catch (unused: Exception) {
                arrayListD = null
            }
            if (arrayListD == null || arrayListD.isEmpty()) {
                return false
            }
            val padded = MainUtil.zeroPadFormat(MainUtil.signOf(arrayListD.size))
            val size = arrayListD.size
            var entryCount = 0
            var index = 0
            while (index < size) {
                index++
                val fileHeader = arrayListD.get(index)
                if (compressListener != null && compressListener.isCancelled) {
                    return false
                }
                if (fileHeader != null) {
                    if (entryCount == 0) {
                        entryCount++
                    } else {
                        try {
                            if (fileHeader.isDirectory) {
                                listener = compressListener
                                read = entryCount
                                extracted = false
                                path = null
                            } else {
                                val sb = StringBuilder()
                                if (TextUtils.isEmpty(padded)) {
                                    sb.append(entryCount)
                                } else {
                                    sb.append(String.format(Locale.US, padded!!, entryCount))
                                }
                                sb.append(".jpg")
                                path = sb.toString()
                                read = entryCount + 1
                                try {
                                    listener = compressListener
                                    try {
                                        extracted = CompressUtil.unzipEntry(
                                            context,
                                            compressUtilZip.openZip!!.getInputStream(fileHeader),
                                            target,
                                            path,
                                            fileHeader.uncompressedSize,
                                            listener
                                        )
                                        path = path
                                    } catch (unused2: Exception) {
                                        path = path
                                        entryCount = read
                                        read = entryCount
                                        extracted = false
                                    }
                                } catch (unused3: Exception) {
                                    listener = compressListener
                                }
                            }
                        } catch (unused4: Exception) {
                            listener = compressListener
                            path = null
                        }
                        if (listener != null) {
                            listener.onDone(target + "/" + path, extracted)
                        }
                        entryCount = read
                    }
                }
            }
            return true
        } catch (_ex: Throwable) {
            throw RuntimeException(_ex)
        }
    }

    override fun coverBitmap(): Bitmap? {
        try {
            var arrayListD: List<FileHeader?>?
            val arrayList: ArrayList<SortItem?>?
            if (!TextUtils.isEmpty(this.path)) {
                val pathNames = this.pathNames
                if (pathNames == null || pathNames.isEmpty()) {
                    this.zip2 = CompressUtilZip2()
                    if (TextUtils.isEmpty(this.realFilePath)) {
                        this.realFilePath = CompressUtil.realPath(this.context, this.path)
                    }
                    if (this.zip2!!.extract(this.realFilePath, null, "debug_logger_tag")) {
                        val zipFile = this.zip2!!.openZip
                        if (zipFile != null) {
                            try {
                                arrayListD = zipFile.headerList()
                            } catch (unused: Exception) {
                                arrayListD = null
                            }
                        } else {
                            arrayListD = null
                        }
                        if (arrayListD != null && !arrayListD.isEmpty()) {
                            val newPathNames: ArrayList<String?> = ArrayList()
                            this.pathNames = newPathNames
                            newPathNames.add((arrayListD.get(0) as FileHeader).fileName)
                            val names = this.pathNames
                            if (names != null) {
                                return bitmapAt(0)
                            }
                        }
                    } else {
                        val names = this.pathNames
                        if (names != null && !names.isEmpty()) {
                            return bitmapAt(0)
                        }
                    }
                } else {
                    val names = this.pathNames
                    if (names != null) {
                        return bitmapAt(0)
                    }
                }
            }
            return null
        } catch (_ex: Throwable) {
            throw RuntimeException(_ex)
        }
    }

    override fun coverStream(): InputStream? {
        try {
            var arrayListD: List<FileHeader?>?
            val arrayList: ArrayList<SortItem?>?
            if (!TextUtils.isEmpty(this.path)) {
                val pathNames = this.pathNames
                if (pathNames == null || pathNames.isEmpty()) {
                    this.zip2 = CompressUtilZip2()
                    if (TextUtils.isEmpty(this.realFilePath)) {
                        this.realFilePath = CompressUtil.realPath(this.context, this.path)
                    }
                    if (this.zip2!!.extract(this.realFilePath, null, "debug_logger_tag")) {
                        val zipFile = this.zip2!!.openZip
                        if (zipFile != null) {
                            try {
                                arrayListD = zipFile.headerList()
                            } catch (unused: Exception) {
                                arrayListD = null
                            }
                        } else {
                            arrayListD = null
                        }
                        if (arrayListD != null && !arrayListD.isEmpty()) {
                            val newPathNames: ArrayList<String?> = ArrayList()
                            this.pathNames = newPathNames
                            newPathNames.add((arrayListD.get(0) as FileHeader).fileName)
                            val names = this.pathNames
                            if (names != null) {
                                return streamFor((this.pathNames!!.get(0))!!)
                            }
                        }
                    } else {
                        val names = this.pathNames
                        if (names != null && !names.isEmpty()) {
                            return streamFor((this.pathNames!!.get(0))!!)
                        }
                    }
                } else {
                    val names = this.pathNames
                    if (names != null) {
                        return streamFor((this.pathNames!!.get(0))!!)
                    }
                }
            }
            return null
        } catch (_ex: Throwable) {
            throw RuntimeException(_ex)
        }
    }

    override fun streamFor(name: String?): InputStream? {
        if (!TextUtils.isEmpty(name)) {
            val prefix = (StringBuilder()).append(this.path).append("/").toString()
            val relativeName = if (name!!.startsWith(prefix)) name.substring(prefix.length) else null
            if (!TextUtils.isEmpty(relativeName)) {
                val compressUtilZip = this.zip2
                try {
                    if (compressUtilZip != null) {
                        val zipFile = compressUtilZip.openZip
                        if (zipFile != null) {
                            return zipFile.getInputStream(zipFile.getFileHeader(relativeName))
                        }
                    } else {
                        val zipFile = this.openZip
                        if (zipFile != null) {
                            val linkedList: ZipArchiveEntry? = zipFile.getEntry(relativeName)
                            val zipArchiveEntry =
                                linkedList
                            if (zipArchiveEntry != null) {
                                return this.openZip!!.getInputStream(zipArchiveEntry)
                            }
                        }
                    }
                } catch (unused: Exception) {
                }
            }
        }
        return null
    }

    override fun bitmapAt(index: Int): Bitmap? {
        try {
            var bArrD: ByteArray? = null
            val arrayList: ArrayList<String?>? = if (!TextUtils.isEmpty(this.path)) this.pathNames else null
            if (!TextUtils.isEmpty(this.path) && arrayList != null && index >= 0 && index <= arrayList.size - 1) {
                val bitmapR = MainUtil.cachedBitmapAny(nameAt(index))
                if (MainUtil.isBitmapOk(bitmapR)) {
                    return bitmapR
                }
                val path = this.pathNames!!.get(index)
                if (TextUtils.isEmpty(path)) {
                    bArrD = null
                } else {
                    val compressUtilZip = this.zip2
                    try {
                        if (compressUtilZip != null) {
                            val zipFile = compressUtilZip.openZip
                            if (zipFile != null) {
                                val zipInputStreamE = zipFile.getInputStream(zipFile.getFileHeader(path))
                                val extractedPaths = CompressUtil.extractedPaths
                                try {
                                    val byteArrayOutputStream = ByteArrayOutputStream()
                                    val bArr = ByteArray(8192)
                                    while (true) {
                                        val read = zipInputStreamE.read(bArr, 0, 8192)
                                        if (read == -1) {
                                            break
                                        }
                                        byteArrayOutputStream.write(bArr, 0, read)
                                    }
                                    byteArrayOutputStream.flush()
                                    byteArrayOutputStream.close()
                                    bArrD = byteArrayOutputStream.toByteArray()
                                } catch (unused: Exception) {
                                    bArrD = null
                                } catch (unused: OutOfMemoryError) {
                                    bArrD = null
                                }
                                try {
                                    zipInputStreamE.close()
                                } catch (unused2: Exception) {
                                }
                            } else {
                                bArrD = null
                            }
                        } else {
                            val zipFile = this.openZip
                            if (zipFile != null) {
                                val linkedList: ZipArchiveEntry? = zipFile.getEntry(path)
                                val zipArchiveEntry =
                                    linkedList
                                if (zipArchiveEntry != null) {
                                    bArrD = CompressUtil.readBytes(
                                        this.openZip!!.getInputStream(zipArchiveEntry),
                                        zipArchiveEntry.size.toInt()
                                    )
                                }
                            }
                            bArrD = null
                        }
                    } catch (unused3: Exception) {
                    }
                }
                if (bArrD != null) {
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapUtil.decodeBytesWith(bArrD, bArrD.size, options)
                    val outWidth = options.outWidth
                    val dp48Px = MainApp.dp48
                    if (outWidth > dp48Px || options.outHeight > dp48Px) {
                        options.inSampleSize = MainUtil.sampleSizeFor(outWidth, options.outHeight, dp48Px, dp48Px)
                    }
                    options.inJustDecodeBounds = false
                    return BitmapUtil.decodeBytesWith(bArrD, bArrD.size, options)
                }
            }
            return null
        } catch (_ex: Throwable) {
            throw RuntimeException(_ex)
        }
    }

    override fun archiveType(): Int {
        return 2
    }

    companion object {
        @JvmStatic
        fun canOpen(context: Context?, path: String?): Boolean {
            var inputStreamX: InputStream?
            var found = false
            if (context == null) {
                return false
            }
            try {
                inputStreamX = MainUtil.openUriInput(context, path)
                try {
                    val bArr = ByteArray(2)
                    if (inputStreamX!!.read(
                            bArr,
                            0,
                            2
                        ) == 2 && bArr[0].toInt() == 80 && bArr[1].toInt() == 75
                    ) {
                        found = true
                    }
                } catch (unused: Exception) {
                }
            } catch (unused2: Exception) {
                inputStreamX = null
            }
            if (inputStreamX != null) {
                try {
                    inputStreamX.close()
                } catch (unused3: Exception) {
                }
            }
            return found
        }
    }
}

/** zip4j's header list is a raw Java `List`: typed here, each entry checked. */
private fun net.lingala.zip4j.core.ZipFile.headerList(): List<FileHeader?>? = getFileHeaders()?.map { it as FileHeader? }
