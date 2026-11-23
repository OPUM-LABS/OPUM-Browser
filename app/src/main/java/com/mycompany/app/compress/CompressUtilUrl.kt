package com.mycompany.app.compress

import android.content.Context
import com.mycompany.app.pref.store.AlbumSettings
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import com.mycompany.app.data.DataUrl.Companion.instance
import com.mycompany.app.main.BitmapUtil
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefAlbum

import java.io.InputStream

@Suppress("deprecation")
class CompressUtilUrl(context: Context?, path: String?, referer: String?) : Compress(context, path, referer) {
    @JvmField
    var entryNames: ArrayList<String?>? = null

    override fun load() {
        var index = 0
        this.entryTotal = 0
        this.entryNames = null
        val imageType = AlbumSettings.imageType.value
        if (imageType != 0) {
            val context = this.context
            if (imageType == 126) {
                val arrayList: ArrayList<String?>? = instance(context).urls
                this.entryNames = arrayList
                this.entryTotal = if (arrayList != null) arrayList.size else 0
                return
            }
            val urls: ArrayList<String?>? = instance(context).urls
            if (urls == null || urls.isEmpty()) {
                return
            }
            val typeFlags = AlbumSettings.imageType.value
            val entryNames: ArrayList<String?> = ArrayList()
            val sortedUrls: ArrayList<String?> = ArrayList(urls)
            val loadType = this.loadType
            if (loadType == 1) {
                val size = sortedUrls.size
                while (index < size) {
                    val rawUrl: Any? = sortedUrls.get(index)
                    index++
                    val url = rawUrl as String?
                    if ((typeFlags and 2) == 2) {
                        entryNames.add(url)
                    }
                }
            } else if (loadType == 2) {
                val size = sortedUrls.size
                while (index < size) {
                    val rawUrl: Any? = sortedUrls.get(index)
                    index++
                    val url = rawUrl as String?
                    if ((typeFlags and 16) == 16) {
                        entryNames.add(url)
                    }
                }
            } else {
                val size = sortedUrls.size
                while (index < size) {
                    val entry: Any? = sortedUrls.get(index)
                    index++
                    val urlText = entry as String?
                    val ext = MainUtil.fileExtFromName(urlText, true)
                    if (TextUtils.isEmpty(ext)) {
                        if ((typeFlags and 64) == 64) {
                            entryNames.add(urlText)
                        }
                    } else if (ext == "jpg") {
                        if ((typeFlags and 2) == 2) {
                            entryNames.add(urlText)
                        }
                    } else if (ext == "png") {
                        if ((typeFlags and 4) == 4) {
                            entryNames.add(urlText)
                        }
                    } else if (ext == "gif") {
                        if ((typeFlags and 8) == 8) {
                            entryNames.add(urlText)
                        }
                    } else if (ext == "webp") {
                        if ((typeFlags and 16) == 16) {
                            entryNames.add(urlText)
                        }
                    } else if ((typeFlags and 32) == 32) {
                        entryNames.add(urlText)
                    }
                }
            }
            if (entryNames.isEmpty()) {
                return
            }
            this.entryNames = entryNames as ArrayList<String?>?
            this.entryTotal = entryNames.size
        }
    }

    override fun entryCount(): Int {
        return this.entryTotal
    }

    override fun syncNames() {
        val arrayList: ArrayList<String?>? = instance(this.context).urls
        val size = if (arrayList == null || this.entryNames == null) 0 else arrayList.size
        if (arrayList == null || this.entryNames == null || size != this.entryNames!!.size) {
            return
        }
        for (index in 0..<size) {
            this.entryNames!!.set(index, arrayList.get(index))
        }
    }

    override fun setNameAt(index: Int, name: String?) {
        val arrayList = this.entryNames
        if (arrayList == null || index < 0 || index >= arrayList.size) {
            return
        }
        this.entryNames!!.set(index, name)
    }

    override fun replaceName(index: Int, url: String?, name: String?) {
        val arrayList = this.entryNames
        if (arrayList == null || index < 0 || index >= arrayList.size) {
            return
        }
        this.entryNames!!.set(index, name)
        instance(this.context).addUrl(url, name)
    }

    override fun release() {
        super.release()
        this.entryNames = null
    }

    override fun albumCount(): Int {
        return this.entryTotal
    }

    override fun albumIndexOf(name: String?): Int {
        val arrayList: ArrayList<String?>? = if (TextUtils.isEmpty(name)) null else this.entryNames
        if (TextUtils.isEmpty(name) || arrayList == null || arrayList.isEmpty()) {
            return -1
        }
        return this.entryNames!!.indexOf(name)
    }

    override fun albumItemAt(index: Int): ChildItem? {
        return null
    }

    override fun albumItems(): MutableList<ChildItem?>? {
        return null
    }

    override fun indexOfName(name: String?): Int {
        val arrayList: ArrayList<String?>? = if (TextUtils.isEmpty(name)) null else this.entryNames
        if (TextUtils.isEmpty(name) || arrayList == null || arrayList.isEmpty()) {
            return -1
        }
        return this.entryNames!!.indexOf(name)
    }

    override fun nameAt(index: Int): String? {
        val arrayList = this.entryNames
        val name =
            if (arrayList == null || index < 0 || index >= arrayList.size) null else this.entryNames!!.get(index)
        return if (name != null) name else this.path
    }

    override fun bitmapAt(index: Int): Bitmap? {
        var inputStreamB: InputStream?
        var bitmapZ: Bitmap? = null
        val arrayList: ArrayList<String?>? = if (this.context != null) this.entryNames else null
        if (this.context != null && arrayList != null && index >= 0 && index < arrayList.size) {
            val address = this.entryNames!!.get(index)
            if (!TextUtils.isEmpty(address)) {
                val options = BitmapFactory.Options()
                options.inSampleSize = 4
                val context = this.context
                val referer = this.referer
                if (context != null) {
                    try {
                        inputStreamB = com.mycompany.app.glide.ViewItemLoader.openStream(context, address, referer)
                    } catch (unused: Exception) {
                        inputStreamB = null
                    }
                    if (inputStreamB != null) {
                        bitmapZ = if (Compress.Companion.isSvgName(
                                MainUtil.guessDownloadName(
                                    address,
                                    null,
                                    null,
                                    true
                                )
                            )
                        ) MainUtil.svgToBitmap(inputStreamB) else BitmapUtil.decodeStream(inputStreamB, options)
                        try { inputStreamB.close() } catch (_: Exception) {}
                    }
                }
            }
        }
        return bitmapZ
    }
}
