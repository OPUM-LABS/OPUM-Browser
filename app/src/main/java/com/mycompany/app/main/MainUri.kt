package com.mycompany.app.main

import java.util.ArrayList
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import androidx.documentfile.provider.DocumentFile
import com.mycompany.app.compress.Compress.Companion.isSubtitleExt
import com.mycompany.app.pref.PrefPath
import ch.opumlabs.opumbrowser.R
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.max

@Suppress("deprecation")
object MainUri {
    @JvmStatic
    fun treeDocUri(uri: Uri?): String? {
        try {
            val treeDocumentId = DocumentsContract.getTreeDocumentId(uri)
            val uriBuildDocumentUriUsingTree: Uri? = if (TextUtils.isEmpty(treeDocumentId)) null else DocumentsContract.buildDocumentUriUsingTree(
                    uri,
                    treeDocumentId
                )
            if (TextUtils.isEmpty(treeDocumentId) || uriBuildDocumentUriUsingTree == null) {
                return null
            }
            return uriBuildDocumentUriUsingTree.toString()
        } catch (unused: Exception) {
            return null
        }
    }

    /**
     * Copies `src` to `dest`. Each side is a content uri when its flag is set, a plain
     * file path otherwise.
     */
    @JvmStatic
    fun copyUri(
        context: Context?,
        src: String?,
        dest: String?,
        srcIsUri: Boolean,
        destIsUri: Boolean
    ): Boolean {
        if (TextUtils.isEmpty(src) || TextUtils.isEmpty(dest)) {
            return false
        }
        var `in`: InputStream? = null
        var out: OutputStream? = null
        var ok = false
        try {
            `in` = if (srcIsUri) context!!.getContentResolver()
                .openInputStream(Uri.parse(src)) else MainUtil.openFileInput(src)
            out = if (destIsUri) context!!.getContentResolver()
                .openOutputStream(Uri.parse(dest)) else MainUtil.openFileOutput(dest, false)
            val buf = if (!srcIsUri && destIsUri) bufferFor(File(src!!).length()) else ByteArray(8192)
            var read: Int
            while ((`in`!!.read(buf, 0, buf.size).also { read = it }) != -1) {
                out!!.write(buf, 0, read)
            }
            ok = true
        } catch (unused: Exception) {
        }
        if (out != null) {
            try {
                out.close()
            } catch (unused: Exception) {
            }
        }
        if (`in` != null) {
            try {
                `in`.close()
            } catch (unused: Exception) {
            }
        }
        return ok
    }

    @JvmStatic
    fun createUri(context: Context?, input: String?, dir: String?, fileName: String?): UriItem? {
        var savedMime: String?
        if (!isContentUri(input)) {
            return MainUriVol.createInVolume(context, input, dir, fileName)
        }
        var uri: Uri? = null
        var mime: String? = null
        if (context != null && !TextUtils.isEmpty(input)) {
            try {
                var docUri = Uri.parse(input)
                if (docUri != null) {
                    try {
                        val ext = MainUtil.extOf(fileName)
                        mime = MainUtil.mimeForExt(ext)
                        if (TextUtils.isEmpty(mime) || isSubtitleExt(ext)) {
                            mime = "application/octet-stream"
                        }
                        docUri = DocumentsContract.createDocument(
                            context.getContentResolver(),
                            docUri,
                            mime!!,
                            (fileName as String)
                        )
                        if (docUri != null) {
                            return MainUriDoc.uriItemFor(context, docUri, input, true)
                        }
                    } catch (unused: Exception) {
                        val savedUri = docUri
                        savedMime = mime
                        uri = savedUri
                        val restoredMime = savedMime
                        docUri = uri
                        mime = restoredMime
                    }
                    return MainUriDoc.createIn(context, docUri, input, mime, splitNumber(fileName))
                }
            } catch (unused2: Exception) {
                savedMime = null
            }
        }
        return null
    }

    @JvmStatic
    fun bufferFor(length: Long): ByteArray {
        if (length <= 8192L) {
            return ByteArray(8192)
        }
        val size = if (length < 40960) length.toInt() else 8192
        try {
            return ByteArray(size)
        } catch (unused: Exception) {
            if (size > 8192) {
                try {
                    return ByteArray(8192)
                } catch (unused2: Exception) {
                    return ByteArray(8192)
                } catch (unused2: OutOfMemoryError) {
                    return ByteArray(8192)
                }
            }
            return ByteArray(8192)
        } catch (unused: OutOfMemoryError) {
            if (size > 8192) {
                try {
                    return ByteArray(8192)
                } catch (unused2: Exception) {
                    return ByteArray(8192)
                } catch (unused2: OutOfMemoryError) {
                    return ByteArray(8192)
                }
            }
            return ByteArray(8192)
        }
    }

    @JvmStatic
    fun storageVolume(): String? {
        val picked = PrefPath.picked
        if (!TextUtils.isEmpty(picked)) {
            return picked
        }
        PrefPath.picked = "external_primary"
        return "external_primary"
    }

    @JvmStatic
    fun documentFile(context: Context?, input: String?): DocumentFile? {
        if (context == null || TextUtils.isEmpty(input)) {
            return null
        }
        try {
            return DocumentFile.fromSingleUri(context, Uri.parse(input))
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun splitNumber(name: String?): NumItem? {
        var baseName: String?
        if (TextUtils.isEmpty(name)) {
            return null
        }
        val dotIndex = name!!.lastIndexOf(46.toChar())
        var digits = 0
        var ext = ""
        if (dotIndex != -1) {
            baseName = name.substring(0, dotIndex)
            if (dotIndex < name.length) {
                ext = name.substring(dotIndex)
            }
        } else {
            baseName = name
        }
        val numberStart = if (baseName.endsWith(")")) baseName.lastIndexOf(" (") else 0
        if (baseName.endsWith(")") && numberStart != -1 && numberStart < baseName.length - 3) {
            try {
                val digitStart =
                    baseName.substring(numberStart + 2, baseName.length - 1).toInt()
                try {
                    baseName = name.substring(0, numberStart)
                } catch (unused: Exception) {
                }
                digits = digitStart
            } catch (unused2: Exception) {
            }
        }
        val numItem = NumItem()
        numItem.base = baseName
        numItem.suffix = ext
        numItem.prefix = baseName + " ("
        numItem.ext = ")" + ext
        numItem.counter = (digits + 1).toLong()
        return numItem
    }

    @JvmStatic
    fun displayPath(context: Context?, dir: String?): String? {
        if (TextUtils.isEmpty(dir)) {
            return null
        }
        return if (isContentUri(dir)) treeDisplayPath(context, dir) else volumeLabel(context, dir)
    }

    @JvmStatic
    fun fileNameOf(context: Context?, input: String?): String? {
        var parcelFileDescriptorOpenFileDescriptor: ParcelFileDescriptor?
        var dirPath: String? = null
        try {
            parcelFileDescriptorOpenFileDescriptor =
                context!!.getContentResolver().openFileDescriptor(
                    Uri.parse(input), "r"
                )
            try {
                dirPath = "/proc/self/fd/" + parcelFileDescriptorOpenFileDescriptor!!.detachFd()
            } catch (unused: Exception) {
            }
        } catch (unused2: Exception) {
            parcelFileDescriptorOpenFileDescriptor = null
        }
        if (parcelFileDescriptorOpenFileDescriptor != null) {
            try {
                parcelFileDescriptorOpenFileDescriptor.close()
            } catch (unused3: Exception) {
            }
        }
        return dirPath
    }

    @JvmStatic
    fun createIn(context: Context?, input: String?, name: String?): UriItem? {
        if (TextUtils.isEmpty(input)) {
            return null
        }
        return if (isMediaUri(input)) MainUriVol.createUriItem(
            context,
            Uri.parse(input),
            name,
            null,
            false
        ) else MainUriDoc.uriItemFor(context, Uri.parse(input), name, false)
    }

    @JvmStatic
    fun treeDisplayPath(context: Context?, input: String?): String? {
        var input = input
        val splitAt: Int
        if (TextUtils.isEmpty(input)) {
            return null
        }
        if (input!!.endsWith("%3A")) {
            if (input.endsWith("/tree/primary%3A/document/primary%3A")) {
                return context!!.getString(R.string.storage_device)
            }
            val lastSlash = input.lastIndexOf(47.toChar())
            if (lastSlash > 0 && lastSlash < input.length) {
                val lastPart = input.substring(lastSlash)
                if (input.endsWith("/tree" + lastPart + "/document" + lastPart)) {
                    return context!!.getString(R.string.storage_sdcard)
                }
            }
        }
        val documentFileF = documentFile(context, input)
        if (documentFileF != null && documentFileF.canRead()) {
            return documentFileF.getName()
        }
        val encodedSlash = input.lastIndexOf("%2F")
        val encodedColon = input.lastIndexOf("%3A")
        val plainSlash = input.lastIndexOf(47.toChar())
        val lastSeparator = max(max(encodedSlash, encodedColon), plainSlash)
        val sepLength = if (lastSeparator == plainSlash) 1 else 3
        if (lastSeparator >= 0) {
            val length = input.length
            val index = sepLength + lastSeparator
            if (index < length) {
                input = input.substring(index)
            } else if (index == length) {
                if (lastSeparator == encodedSlash) {
                    splitAt = max(encodedColon, plainSlash)
                } else if (lastSeparator == encodedColon) {
                    splitAt = max(encodedSlash, plainSlash)
                } else {
                    splitAt = if (lastSeparator == plainSlash) max(encodedSlash, encodedColon) else 0
                }
                if (splitAt > 0) {
                    val nameStart = splitAt + (if (splitAt == plainSlash) 1 else 3)
                    if (nameStart < lastSeparator) {
                        input = input.substring(nameStart, lastSeparator)
                    }
                }
            }
        }
        return MainUtil.urlDecode(input)
    }

    @JvmStatic
    fun fileSize(context: Context?, input: String?): Long {
        var statSize: Long
        var parcelFileDescriptorOpenFileDescriptor: ParcelFileDescriptor? = null
        try {
            parcelFileDescriptorOpenFileDescriptor =
                context!!.getContentResolver().openFileDescriptor(
                    Uri.parse(input), "r"
                )
            statSize = parcelFileDescriptorOpenFileDescriptor!!.getStatSize()
        } catch (unused: Exception) {
            statSize = 0
        }
        if (parcelFileDescriptorOpenFileDescriptor != null) {
            try {
                parcelFileDescriptorOpenFileDescriptor.close()
            } catch (unused2: Exception) {
            }
        }
        return statSize
    }

    @JvmStatic
    fun findChild(ctx: Context?, input: String?, nameList: MutableList<Any?>?): String? {
        if (!TextUtils.isEmpty(input)) {
            if (isContentUri(input)) {
                if (ctx != null && !TextUtils.isEmpty(input)) {
                    val isDir: Boolean
                    try {
                        isDir = DocumentFile.fromSingleUri(ctx, Uri.parse(input))!!.isDirectory()
                    } catch (exception: Exception) {
                        return "external_primary"
                    }

                    if (isDir) {
                        return input
                    }
                }
            } else if (nameList != null && nameList.size > 0) {
                val mutableIterator: MutableIterator<Any?> = nameList.iterator()

                while (mutableIterator.hasNext()) {
                    if (input == mutableIterator.next() as String?) {
                        return input
                    }
                }
            }
        }

        return "external_primary"
    }

    @JvmStatic
    fun externalVolumes(context: Context?): ArrayList<Any?>? {
        val externalVolumeNames: MutableSet<String?>? = MediaStore.getExternalVolumeNames(context!!)
        if (externalVolumeNames == null || externalVolumeNames.size == 0) {
            return null
        }
        val arrayList: ArrayList<Any?> = ArrayList<Any?>()
        for (volume in externalVolumeNames) {
            if (!TextUtils.isEmpty(volume)) {
                if ("external_primary" == volume) {
                    arrayList.add(0, volume)
                } else {
                    arrayList.add(volume)
                }
            }
        }
        return arrayList
    }

    @JvmStatic
    fun volumeLabel(context: Context?, path: String?): String {
        val sb = StringBuilder()
        if ("external_primary" == path) {
            sb.append(context!!.getString(R.string.storage_device))
        } else {
            sb.append(context!!.getString(R.string.storage_sdcard))
        }
        sb.append("/")
        sb.append(Environment.DIRECTORY_DOWNLOADS)
        return sb.toString()
    }

    @JvmStatic
    fun isMediaUri(newName: String?): Boolean {
        if (!TextUtils.isEmpty(newName)) {
            return newName!!.startsWith("content://media")
        }
        return false
    }

    @JvmStatic
    fun isContentUri(path: String?): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }
        return path!!.startsWith("content://")
    }

    @JvmStatic
    fun isFile(context: Context?, input: String?): Boolean {
        val uri: Uri? = if (context != null && !TextUtils.isEmpty(input)) Uri.parse(input) else null
        if (context != null && !TextUtils.isEmpty(input) && uri != null) {
            try {
                if (DocumentFile.fromSingleUri(context, uri)!!.isFile()) {
                    return true
                }
            } catch (unused: Exception) {
            }
        }
        return false
    }

    @JvmStatic
    fun moveUri(context: Context?, src: String?, dest: String?, overwrite: Boolean, keepSource: Boolean): Boolean {
        if (isMediaUri(src)) {
            val copiedMedia = copyUri(context, src, dest, overwrite, keepSource)
            if (copiedMedia) {
                MainUtil.deleteUri(context, src)
            }
            return copiedMedia
        }
        val copied = copyUri(context, src, dest, overwrite, keepSource)
        if (copied) {
            MainUtil.deleteUri(context, src)
        }
        return copied
    }

    class NumItem {
        @JvmField
        var base: String? = null
        @JvmField
        var suffix: String? = null
        @JvmField
        var prefix: String? = null
        @JvmField
        var ext: String? = null
        @JvmField
        var counter: Long = 0
        @JvmField
        var digits: Int = 0
    }

    class UriItem {
        @JvmField
        var type: Int = 0
        @JvmField
        var uri: Uri? = null
        @JvmField
        var dirPath: String? = null
        @JvmField
        var dirName: String? = null
        @JvmField
        var uriString: String? = null
        @JvmField
        var name: String? = null
        @JvmField
        var time: Long = 0
        @JvmField
        var size: Long = 0
    }
}
