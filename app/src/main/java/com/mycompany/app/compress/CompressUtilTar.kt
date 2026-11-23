package com.mycompany.app.compress

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import com.mycompany.app.compress.CompressUtil.CompressSort
import com.mycompany.app.main.BitmapUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.InputStream
import java.util.Collections

@Suppress("deprecation")
class CompressUtilTar(context: Context?, path: String?, referer: String?) : Compress(context, path, referer) {
    override fun load() {
        var tarArchiveInputStreamV: TarArchiveInputStream?
        var index = 0
        this.entryTotal = 0
        if (TextUtils.isEmpty(this.path)) {
            return
        }
        this.sortItems = ArrayList()
        this.entryTotal = 0
        var nonUtf = true
        try {
            tarArchiveInputStreamV = openTarStream(this.path)
            if (tarArchiveInputStreamV != null) {
                while (true) {
                    try {
                        val tarArchiveEntryD = tarArchiveInputStreamV.getNextTarEntry()
                        if (tarArchiveEntryD == null) {
                            nonUtf = false
                            break
                        }
                        if (!tarArchiveEntryD.isDirectory()) {
                            val name = tarArchiveEntryD.getName()
                            val sortItemB: SortItem? = if (Compress.Companion.isImageName(name, true, true)) CompressUtil.sortItemFor(name) else null
                            if (Compress.Companion.isImageName(name, true, true) && sortItemB != null) {
                                val sortItems = this.sortItems
                                if (sortItems == null) {
                                    break
                                } else {
                                    sortItems.add(sortItemB)
                                }
                            }
                        }
                    } catch (unused: Exception) {
                    }
                }
                try {
                    tarArchiveInputStreamV.close()
                } catch (unused2: Exception) {
                }
                if (nonUtf) {
                    this.sortItems = null
                    return
                }
                val sortItems = this.sortItems
                if (sortItems == null || sortItems.isEmpty()) {
                    return
                }
                val size = this.sortItems!!.size
                this.entryTotal = size
                val arrayList: ArrayList<SortItem?>? = if (size <= 0) null else this.sortItems
                if (size <= 0 || arrayList == null || this.pathNames == null) {
                    return
                }
                try {
                    Collections.sort<SortItem?>(arrayList, CompressSort())
                } catch (unused3: Exception) {
                }
                val sortItemsNow = this.sortItems
                val itemCount = sortItemsNow!!.size
                while (index < itemCount) {
                    val rawSortItem: Any? = sortItemsNow.get(index)
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
        } catch (unused4: Exception) {
            tarArchiveInputStreamV = null
        }
    }

    override fun open(): Boolean {
        var entryTotal: Int = 0
        var found: Boolean = false
        this.entryTotal = 0
        if (TextUtils.isEmpty(this.path)) {
            return false
        }
        var tarArchiveInputStreamV: TarArchiveInputStream? = null
        try {
            tarArchiveInputStreamV = openTarStream(this.path)
            if (tarArchiveInputStreamV == null) {
                return false
            }
            entryTotal = 0
            while (tarArchiveInputStreamV.getNextTarEntry() != null) {
                try {
                    entryTotal++
                } catch (unused: Exception) {
                    found = true
                }
            }
            found = false
            try {
                tarArchiveInputStreamV.close()
            } catch (unused2: Exception) {
            }
            if (found) {
                return false
            }
            this.entryTotal = entryTotal
            return entryTotal > 0
        } catch (unused3: Exception) {
            return false
        }
    }

    fun openTarStream(path: String?): TarArchiveInputStream? {
        var nonUtf: Boolean = false
        var tarArchiveInputStream: TarArchiveInputStream? = null
        if (TextUtils.isEmpty(path)) {
            return null
        }
        try {
            if (TextUtils.isEmpty(this.charsetName)) {
                this.charsetName = MainConst.defaultCharset
            }
            nonUtf = false
            tarArchiveInputStream = TarArchiveInputStream(MainUtil.openUriInput(this.context, path), this.charsetName)
        } catch (unused: Exception) {
            nonUtf = "UTF-8" != this.charsetName
        }
        if (!nonUtf) {
            return tarArchiveInputStream
        }
        try {
            this.charsetName = "UTF-8"
            return TarArchiveInputStream(MainUtil.openUriInput(this.context, path), this.charsetName)
        } catch (unused2: Exception) {
            return tarArchiveInputStream
        }
    }

    override fun streamFor(name: String?): InputStream? {
        val tarArchiveInputStreamV: TarArchiveInputStream?
        var tarArchiveEntryD: TarArchiveEntry?
        if (TextUtils.isEmpty(name)) {
            return null
        }
        val dirPrefix = (StringBuilder()).append(this.path).append("/").toString()
        val relName: String =
            (if (name!!.startsWith(dirPrefix)) name.substring(dirPrefix.length) else null)!!
        if (TextUtils.isEmpty(relName)) {
            return null
        }
        try {
            tarArchiveInputStreamV = openTarStream(this.path)
            if (tarArchiveInputStreamV == null) {
                return null
            }
            while (true) {
                try {
                    tarArchiveEntryD = tarArchiveInputStreamV.getNextTarEntry()
                    if (tarArchiveEntryD != null) {
                        if (relName == tarArchiveEntryD.getName()) {
                            return tarArchiveInputStreamV
                        }
                        continue
                    }
                } catch (unused: Exception) {
                }
                try {
                    tarArchiveInputStreamV.close()
                } catch (unused2: Exception) {
                }
                return null
            }
        } catch (unused3: Exception) {
            return null
        }
    }

    override fun bitmapAt(index: Int): Bitmap? {
        var tarArchiveInputStreamV: TarArchiveInputStream?
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
                tarArchiveInputStreamV = null
                try {
                    tarArchiveInputStreamV = openTarStream(this.path)
                    if (tarArchiveInputStreamV != null) {
                        while (true) {
                            val tarArchiveEntryD = tarArchiveInputStreamV.getNextTarEntry()
                            if (tarArchiveEntryD == null) {
                                break
                            }
                            if (path == tarArchiveEntryD.getName()) {
                                bArrD = CompressUtil.readBytes(
                                    tarArchiveInputStreamV,
                                    tarArchiveEntryD.getSize().toInt()
                                )
                                break
                            }
                        }
                    }
                } catch (unused: Exception) {
                    bArrD = null
                } finally {
                    if (tarArchiveInputStreamV != null) {
                        try {
                            tarArchiveInputStreamV.close()
                        } catch (unused2: Exception) {
                        }
                    }
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
    }

    override fun archiveType(): Int {
        return 2
    }
}
