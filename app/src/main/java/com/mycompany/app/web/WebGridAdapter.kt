package com.mycompany.app.web

import android.content.Context
import android.webkit.URLUtil
import com.mycompany.app.data.DataUrl
import com.mycompany.app.main.MainUtil.fillAllFlags

/**
 * List and selection model of the picture list (WebGridDialog / WebSiteDialog, drawn by [PictureListScreen]):
 * [imageUrls] the image addresses, [selectMode] select mode, [index] the selected flags and [checkedCount] their count, [loadedFlags] / [failedFlags] / [noRefererFlags] / [imageSizes] per-item
 * load state. The RecyclerView adapter part (rows with `MyThumbView` / `MyFadeView` / `MyButtonCheck`) was never
 * attached to a list after the Compose port and was removed on 2026-11-17.
 */
class WebGridAdapter(@JvmField var context: Context?, @JvmField var pageUrl: String?, imageTypeParam: Int) {
    @JvmField
    var imageUrls: ArrayList<String?>? = null
    @JvmField
    var selectMode: Boolean = false
    @JvmField
    var checked: BooleanArray? = null
    @JvmField
    var checkedCount: Int = 0
    @JvmField
    var loadedFlags: BooleanArray? = null
    @JvmField
    var failedFlags: BooleanArray? = null
    @JvmField
    var noRefererFlags: BooleanArray? = null
    @JvmField
    var imageSizes: IntArray? = null
    @JvmField
    var imageType: Byte

    init {
        this.imageType = (imageTypeParam).toByte()
    }

    fun allChecked(): Boolean {
        val count = itemCount()
        return count != 0 && this.checkedCount == count
    }

    fun validIndex(index: Int): Boolean {
        val arrayList = this.imageUrls
        return arrayList != null && index >= 0 && index < arrayList.size
    }

    fun clear() {
        this.imageUrls = null
        this.pageUrl = null
        this.checked = null
        this.checkedCount = 0
        this.loadedFlags = null
        this.failedFlags = null
        this.noRefererFlags = null
        this.imageSizes = null
    }

    fun setAllChecked(checked: Boolean, refresh: Boolean) {
        var count = itemCount()
        if (count != 0) {
            if (this.selectMode) {
                val checks = this.checked
                if (checks == null) {
                    return
                }
                fillAllFlags(checks, checked)
                if (!checked) {
                    count = 0
                }
                if (count == this.checkedCount) {
                    return
                } else {
                    this.checkedCount = count
                }
            }
        }
    }

    fun setSelectMode(index: Int, on: Boolean) {
        if (this.selectMode != on) {
            this.selectMode = on
            val count = itemCount()
            if (count == 0) {
                return
            }
            if (this.selectMode) {
                val booleanArray = BooleanArray(count)
                this.checked = booleanArray
                fillAllFlags(booleanArray, false)
                if (index >= 0) {
                    val checks = this.checked
                    if (index < checks!!.size) {
                        checks[index] = true
                        this.checkedCount = 1
                    } else {
                        this.checkedCount = 0
                    }
                } else {
                    this.checkedCount = 0
                }
            } else {
                this.checked = null
                this.checkedCount = 0
            }
        }
    }

    fun setImages(list: ArrayList<String?>?) {
        if (this.selectMode) {
            return
        }
        this.imageUrls = list
        val count = itemCount()
        if (count > 0) {
            if (this.imageType.toInt() == 0) {
                this.loadedFlags = null
                this.failedFlags = BooleanArray(count)
                this.noRefererFlags = BooleanArray(count)
            } else {
                this.loadedFlags = BooleanArray(count)
                this.failedFlags = null
                this.noRefererFlags = null
            }
            this.imageSizes = IntArray(count)
        } else {
            this.loadedFlags = null
            this.failedFlags = null
            this.noRefererFlags = null
            this.imageSizes = null
        }
    }

    fun updateUrl(index: Int, alt: String?) {
        val url = if (!validIndex(index)) null else this.imageUrls!!.get(index)
        if (url != null) {
            val isNetwork = URLUtil.isNetworkUrl(alt)
            val loaded: BooleanArray? = if (isNetwork) this.loadedFlags else null
            if (isNetwork && loaded != null && index >= 0 && index < loaded.size) {
                if (loaded[index] && url == alt) {
                    return
                } else {
                    this.loadedFlags!![index] = true
                }
            }
            this.imageUrls!!.set(index, alt)
            DataUrl.instance(this.context).addUrl(url, alt)
        }
    }

    fun toggleChecked(index: Int) {
        val checks = this.checked
        if (checks == null || index < 0 || index >= checks.size) {
            return
        }
        val wasChecked = checks[index]
        val nowChecked = !wasChecked
        val count = itemCount()
        val checksIfAny: BooleanArray? = if (count == 0) null else this.checked
        if (count == 0 || checksIfAny == null || index < 0 || index >= checksIfAny.size || checksIfAny[index] == nowChecked) {
            return
        }
        checksIfAny[index] = nowChecked
        val checkedCount = this.checkedCount
        if (wasChecked) {
            if (checkedCount > 0) {
                this.checkedCount = checkedCount - 1
            }
        } else if (checkedCount < count) {
            this.checkedCount = checkedCount + 1
        }
    }

    fun mergeUrls(arrayList: ArrayList<String?>?) {
        val size = if (this.imageUrls == null) 0 else arrayList!!.size
        if (this.imageUrls == null || size != this.imageUrls!!.size) {
            return
        }
        val length = this.loadedFlags!!.size
        for (index in 0..<size) {
            val url = arrayList!!.get(index)
            val loaded: BooleanArray? = if (URLUtil.isNetworkUrl(url)) this.loadedFlags else null
            if (URLUtil.isNetworkUrl(url) && loaded != null && index < length) {
                loaded[index] = true
            }
            this.imageUrls!!.set(index, url)
        }
    }

    fun checkedUrls(): ArrayList<String?>? {
        val count = if (this.checkedCount <= 0) 0 else itemCount()
        if (this.checkedCount <= 0 || count == 0) {
            return null
        }
        if (allChecked()) {
            return ArrayList<String?>(this.imageUrls!!)
        }
        val arrayList: ArrayList<String?> = ArrayList<String?>()
        var index = 0
        while (index < count) {
            val url = this.imageUrls!!.get(index)
            if (url != null) {
                val checks = this.checked
                if (if (checks == null || index < 0 || index >= checks.size) false else checks[index]) {
                    arrayList.add(url)
                }
            }
            index++
        }
        return arrayList
    }

    fun itemCount(): Int {
        val arrayList = this.imageUrls
        if (arrayList == null) {
            return 0
        }
        return arrayList.size
    }
}
