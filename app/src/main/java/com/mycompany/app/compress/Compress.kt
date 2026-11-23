package com.mycompany.app.compress

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.collection.LruCache
import com.mycompany.app.compress.CompressUtil.CompressListener
import com.mycompany.app.data.DataCmp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.subtitle.Subtitle

import java.io.File
import java.io.InputStream

@Suppress("deprecation")
open class Compress(@JvmField var context: Context?, @JvmField var path: String?, @JvmField var referer: String?) {
    @JvmField
    var realFilePath: String? = null
    @JvmField
    var loadType: Int = 0
    @JvmField
    var charsetName: String? = null
    @JvmField
    var password: String? = null
    @JvmField
    var pathNames: ArrayList<String?>? = ArrayList()
    @JvmField
    var sortItems: ArrayList<SortItem?>? = null
    @JvmField
    var entryTotal: Int = 0

    class SortItem {
        @JvmField
        var name: String? = null
        @JvmField
        var path: String? = null
        @JvmField
        var sortKey: String? = null
    }

    open fun open(): Boolean {
        return false
    }

    open fun entryCount(): Int {
        return this.entryTotal
    }

    open fun syncNames() {
    }

    open fun setNameAt(index: Int, name: String?) {
    }

    open fun replaceName(index: Int, url: String?, name: String?) {
    }

    open fun release() {
        this.entryTotal = 0
        this.context = null
        this.path = null
        this.referer = null
        this.realFilePath = null
        this.loadType = 0
        this.charsetName = null
        this.password = null
        this.pathNames = null
        this.sortItems = null
    }

    open fun extractTo(target: String?, compressListener: CompressListener?): Boolean {
        return false
    }

    open fun coverBitmap(): Bitmap? {
        return null
    }

    open fun coverStream(): InputStream? {
        return null
    }

    open fun albumCount(): Int {
        val context = this.context
        if (context == null) {
            return 0
        }
        return DataCmp.instance(context).pathCount()
    }

    open fun albumIndexOf(name: String?): Int {
        val context = this.context
        if (context == null) {
            return 0
        }
        return DataCmp.instance(context).indexOfPath(name)
    }

    open fun albumItemAt(index: Int): ChildItem? {
        val context = this.context
        if (context == null) {
            return null
        }
        return DataCmp.instance(context).itemAt(index)
    }

    open fun albumItems(): MutableList<ChildItem?>? {
        val context = this.context
        if (context == null) {
            return null
        }
        return DataCmp.instance(context).items
    }

    open fun indexOfName(name: String?): Int {
        val arrayList: ArrayList<String?>? = if (TextUtils.isEmpty(name)) null else this.pathNames
        if (TextUtils.isEmpty(name) || arrayList == null || arrayList.isEmpty()) {
            return -1
        }
        if (TextUtils.isEmpty(this.path)) {
            return arrayList.indexOf(name)
        }
        val prefix = (StringBuilder()).append(this.path).append("/").toString()
        val relativeName = if (name!!.startsWith(prefix)) name.substring(prefix.length) else null
        if (TextUtils.isEmpty(relativeName)) {
            return -1
        }
        return arrayList.indexOf(relativeName)
    }

    open fun nameAt(index: Int): String? {
        val arrayList = this.pathNames
        if (arrayList == null || index < 0 || index > arrayList.size - 1) {
            return null
        }
        if (TextUtils.isEmpty(this.path)) {
            return arrayList.get(index)
        }
        return this.path + "/" + (arrayList.get(index))
    }

    open fun streamFor(name: String?): InputStream? {
        return null
    }

    open fun bitmapAt(index: Int): Bitmap? {
        return null
    }

    open fun archiveType(): Int {
        return 0
    }

    fun realPath(): String? {
        if (TextUtils.isEmpty(this.realFilePath)) {
            this.realFilePath = CompressUtil.realPath(this.context, this.path)
        }
        return this.realFilePath
    }

    open fun load() {
    }

    companion object {
        @JvmStatic
        fun isDocExt(ext: String?): Boolean {
            if (TextUtils.isEmpty(ext)) {
                return false
            }
            return isTextExt(ext) || ext == "asv" || ext == "csh" || ext == "doc" || ext == "docm" || ext == "docx" || ext == "dotm" || ext == "dotx" || ext == "h" || ext == "hwp" || ext == "mdb" || ext == "pdf" || ext == "potm" || ext == "potx" || ext == "ppam" || ext == "ppsm" || ext == "ppsx" || ext == "ppt" || ext == "pptm" || ext == "pptx" || ext == "rtf" || ext == "sldm" || ext == "sldx" || ext == "thmx" || ext == "xlam" || ext == "xls" || ext == "xlsb" || ext == "xlsm" || ext == "xlsx" || ext == "xltm" || ext == "xltx"
        }

        @JvmStatic
        fun isImageName(name: String, allowEmpty: Boolean, tarOnly: Boolean): Boolean {
            val dotIndex = if ((allowEmpty && TextUtils.isEmpty(name))) 0 else name.lastIndexOf(46.toChar())
            if ((allowEmpty && TextUtils.isEmpty(name)) || dotIndex == -1) {
                return false
            }
            var ext = name.substring(dotIndex + 1)
            if (tarOnly) {
                ext = ext.lowercase()
            }
            return isImageExt(ext)
        }

        @JvmStatic
        fun isImageExt(ext: String?): Boolean {
            if (TextUtils.isEmpty(ext)) {
                return false
            }
            val mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            if (TextUtils.isEmpty(mimeTypeFromExtension)) {
                return false
            }
            return mimeTypeFromExtension!!.lowercase().startsWith("image")
        }

        @JvmStatic
        fun isAudioExt(ext: String?): Boolean {
            if (TextUtils.isEmpty(ext)) {
                return false
            }
            if (ext == "aac") {
                return true
            }
            val mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            if (TextUtils.isEmpty(mimeTypeFromExtension)) {
                return false
            }
            val lowerCase = mimeTypeFromExtension!!.lowercase()
            return lowerCase.startsWith("audio") || lowerCase == "application/ogg" || lowerCase == "application/x-ogg"
        }

        @JvmStatic
        fun isPdfName(name: String?): Boolean {
            val dotIndex = if (TextUtils.isEmpty(name)) 0 else name!!.lastIndexOf(46.toChar())
            if (TextUtils.isEmpty(name) || dotIndex == -1) {
                return false
            }
            return isPdfExt(name!!.substring(dotIndex + 1).lowercase())
        }

        @JvmStatic
        fun isPdfExt(ext: String?): Boolean {
            return !TextUtils.isEmpty(ext) && ext == "pdf"
        }

        @JvmStatic
        fun isSubtitleExt(ext: String?): Boolean {
            var index: Int = 0
            if (TextUtils.isEmpty(ext)) {
                index = -1
            } else {
                index = 0
                while (index < 10 && ext != Subtitle.EXTENSIONS[index]) {
                    index++
                }
                if (index >= 10) {
                    index = -1
                }
            }
            return index != -1
        }

        @JvmStatic
        fun isSvgName(name: String?): Boolean {
            val dotIndex = if (TextUtils.isEmpty(name)) 0 else name!!.lastIndexOf(46.toChar())
            if (TextUtils.isEmpty(name) || dotIndex == -1) {
                return false
            }
            val lowerCase = name!!.substring(dotIndex + 1).lowercase()
            if (TextUtils.isEmpty(lowerCase)) {
                return false
            }
            return lowerCase == "svg"
        }

        @JvmStatic
        fun isTextExt(ext: String?): Boolean {
            if (TextUtils.isEmpty(ext)) {
                return false
            }
            if (isSubtitleExt(ext)) {
                return true
            }
            val mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            if (TextUtils.isEmpty(mimeTypeFromExtension)) {
                return false
            }
            return mimeTypeFromExtension!!.lowercase().startsWith("text")
        }

        @JvmStatic
        fun isVideoExt(ext: String?): Boolean {
            if (!TextUtils.isEmpty(ext) && (ext == "mp4" || ext == "ts" || ext == "webm" || ext == "avi" || ext == "flv" || ext == "mkv" || ext == "mov" || ext == "wmv" || ext == "3gp" || ext == "3gpp" || ext == "3g2" || ext == "3gpp2" || ext == "asf" || ext == "asx" || ext == "avs" || ext == "f4v" || ext == "hlv" || ext == "m1v" || ext == "m4v" || ext == "mp2" || ext == "mpe" || ext == "mpeg" || ext == "mpg" || ext == "mts" || ext == "ogv" || ext == "qt" || ext == "rm" || ext == "rmvb" || ext == "swf" || ext == "tp" || ext == "viv" || ext == "vivo" || ext == "vob" || ext == "wtv" || ext == "yuv")) {
                return true
            }
            if (TextUtils.isEmpty(ext) || ext.equals("mkv", ignoreCase = true)) {
                return false
            }
            val mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            if (TextUtils.isEmpty(mimeTypeFromExtension)) {
                return false
            }
            return mimeTypeFromExtension!!.lowercase().startsWith("video")
        }

        @JvmStatic
        fun isMhtExt(ext: String?): Boolean {
            if (TextUtils.isEmpty(ext)) {
                return false
            }
            return ext == "mht" || ext == "mhtml"
        }

        @JvmStatic
        fun create(context: Context?, type: Int, path: String?, referer: String?): Compress {
            var inputStreamX: InputStream?
            var isTar = false
            if (type == 12) {
                return CompressUtilUrl(context, path, referer)
            }
            if (type == 2) {
                return CompressUtilPdf(context, path, null)
            }
            if (!MainUri.isFile(context, path)) {
                return Compress(context, path, referer)
            }
            if (type == 1) {
                if (!CompressUtilZip.Companion.canOpen(context, path)) {
                    return Compress(context, path, referer)
                }
                val compressUtilAlbum = CompressUtilAlbum(context, path, null)
                compressUtilAlbum.password = "debug_logger_tag"
                return compressUtilAlbum
            }
            if (type != 3) {
                return Compress(context, path, referer)
            }
            if (CompressUtilZip.Companion.canOpen(context, path)) {
                return CompressUtilZip(context, path, null)
            }
            var tarFound = false
            if (context != null) {
                try {
                    inputStreamX = MainUtil.openUriInput(context, path)
                    try {
                        if (inputStreamX!!.available() % 512 == 0) {
                            if (inputStreamX.skip(99L) == 99L) {
                                val bArr = ByteArray(1)
                                isTar = inputStreamX.read(bArr, 0, 1) == 1 && bArr[0].toInt() == 0
                            }
                            tarFound = isTar
                            try {
                                inputStreamX.close()
                            } catch (unused: Exception) {
                            }
                        }
                    } catch (unused2: Exception) {
                    }
                } catch (unused3: Exception) {
                    inputStreamX = null
                }
            }
            return if (tarFound) CompressUtilTar(context, path, null) else Compress(context, path, referer)
        }

        @JvmStatic
        fun isAlbumName(name: String?): Boolean {
            val dotIndex = if (TextUtils.isEmpty(name)) 0 else name!!.lastIndexOf(46.toChar())
            if (TextUtils.isEmpty(name) || dotIndex == -1) {
                return false
            }
            return isAlbumExt(name!!.substring(dotIndex + 1).lowercase())
        }

        @JvmStatic
        fun isAlbumExt(ext: String?): Boolean {
            return !TextUtils.isEmpty(ext) && ext == "album"
        }

        @JvmStatic
        fun isApkExt(ext: String?): Boolean {
            if (TextUtils.isEmpty(ext)) {
                return false
            }
            if (ext == "apk") {
                return true
            }
            val mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            if (TextUtils.isEmpty(mimeTypeFromExtension)) {
                return false
            }
            return mimeTypeFromExtension!!.lowercase()
                .startsWith("application/vnd.android.package-archive")
        }

        @JvmStatic
        fun isArchiveName(name: String?): Boolean {
            val dotIndex = if (TextUtils.isEmpty(name)) 0 else name!!.lastIndexOf(46.toChar())
            if (TextUtils.isEmpty(name) || dotIndex == -1) {
                return false
            }
            return isArchiveExt(name!!.substring(dotIndex + 1).lowercase())
        }

        @JvmStatic
        fun isArchiveExt(ext: String?): Boolean {
            if (TextUtils.isEmpty(ext)) {
                return false
            }
            return ext == "zip" || ext == "jar" || ext == "cbz" || ext == "cbr" || ext == "cbt" || ext == "tar"
        }
    }
}
