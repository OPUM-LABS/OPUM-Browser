package com.mycompany.app.dialog

import android.text.TextUtils
import com.mycompany.app.data.DataUrl
import com.mycompany.app.data.DataUrl.ImgCntItem
import com.mycompany.app.dialog.DialogLoadImg.LoadImgListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.parseIntSafe
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUtil.hostLabelUpper
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.web.WebEmgTask
import com.mycompany.app.web.WebEmgTask.EmgTaskListener
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebViewActivity
import java.util.ArrayList

/**
 * Loader for the gallery site handled by [WebEmgTask] (opened by `WebViewActivity` when "Save all images" runs on that
 * site): runs the task on a hidden page, then opens the image viewer with the found pictures
 * (`EXTRA_LOAD_TYPE` 1). The sheet and its state machine are in [SiteLoadDialog]; this class owns the task and
 * keeps the old task listener. Compose now; same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogLoadEmg(
    webViewActivity: WebViewActivity?,
    webNestView: WebNestView?,
    url: String?,
    forceLoad: Boolean,
    loadImgListener: LoadImgListener?,
) : SiteLoadDialog(webViewActivity, webNestView, url, forceLoad, loadImgListener) {
    @JvmField
    var emgTask: WebEmgTask? = null

    override fun createTask() {
        emgTask = WebEmgTask(appContext, webNest, ShowResultStep())
    }

    override fun taskProgress(): Int? = emgTask?.loadProgress()

    override fun taskStep() {
        emgTask?.readPage()
    }

    override fun taskStop() {
        emgTask?.release()
        emgTask = null
    }

    internal inner class ShowResultStep : EmgTaskListener {
        override fun onFailed() {
            val dialogLoadEmg = this@DialogLoadEmg
            dialogLoadEmg.loadState = 2.toByte()
            dialogLoadEmg.showResult(false, false, false)
        }
        override fun onLoading() {
            val dialogLoadEmg = this@DialogLoadEmg
            if (dialogLoadEmg.loadState.toInt() == 2) {
                return
            }
            dialogLoadEmg.loadState = 1.toByte()
        }
        override fun onResult(arrayList: ArrayList<String?>?, position: Int, count: Int, title: String?) {
            var siteName: String?
            val dialogLoadEmg = this@DialogLoadEmg
            if (dialogLoadEmg.emgTask == null || dialogLoadEmg.loadState.toInt() == 2) {
                return
            }
            val isDone = false
            dialogLoadEmg.loadState = 0.toByte()
            if (arrayList == null || arrayList.isEmpty() || count == 0 || TextUtils.isEmpty(title)) {
                dialogLoadEmg.loadFailed = false
                val progress = dialogLoadEmg.emgTask!!.loadProgress()
                if (progress < 100) {
                    dialogLoadEmg.onProgress(progress)
                    return
                } else if (dialogLoadEmg.progressMode) {
                    dialogLoadEmg.progressMode = false
                    dialogLoadEmg.onProgress(progress)
                    return
                }
            }
            if (dialogLoadEmg.emgTask != null) {
                dialogLoadEmg.loadState = 2.toByte()
                if (arrayList == null || arrayList.isEmpty() || count == 0 || TextUtils.isEmpty(title)) {
                    var loadFailed = dialogLoadEmg.loadFailed
                    if (!loadFailed) {
                        dialogLoadEmg.loadFailed = false
                        loadFailed = false
                    }
                    if (dialogLoadEmg.emgTask!!.netError) {
                        dialogLoadEmg.showResult(false, true, false)
                        return
                    } else if (loadFailed) {
                        dialogLoadEmg.showResult(false, false, true)
                        return
                    } else {
                        dialogLoadEmg.showResult(false, false, false)
                        return
                    }
                }
                if (dialogLoadEmg.opening || dialogLoadEmg.hostActivity == null) {
                    return
                }
                dialogLoadEmg.opening = true
                val urls: ArrayList<String?> = ArrayList()
                val thumbUrls: ArrayList<String?> = ArrayList()
                for (index in 0..<count) {
                    val label = index.toString() + ".jpg"
                    if (index == position) {
                        urls.add(title)
                    } else {
                        urls.add(label)
                    }
                    thumbUrls.add(label)
                }
                val size = arrayList.size
                var index = 0
                while (index < size) {
                    val item: Any? = arrayList.get(index)
                    index++
                    val url = item as String?
                    val dashIndex = if (!TextUtils.isEmpty(url)) url!!.lastIndexOf(45.toChar()) else 0
                    val pageStart = if (!TextUtils.isEmpty(url) && dashIndex != -1) (dashIndex + 1) else 0
                    val pageNumber = if (!TextUtils.isEmpty(url) && dashIndex != -1 && pageStart < url!!.length) (parseIntSafe(url.substring(pageStart)) - 1) else 0
                    if (!TextUtils.isEmpty(url) && dashIndex != -1 && pageStart < url!!.length && pageNumber != position && pageNumber >= 0 && pageNumber < urls.size) {
                        thumbUrls.set(pageNumber, url)
                    }
                }
                val imgCntItem = ImgCntItem()
                imgCntItem.jpgCount = count
                val dataUrlB = DataUrl.instance(dialogLoadEmg.appContext)
                dataUrlB.urls = urls
                dataUrlB.thumbUrls = thumbUrls
                val intentQ = imageViewerIntent(dialogLoadEmg.appContext)
                intentQ.putExtra("EXTRA_TYPE", 12)
                val pageUrl = dialogLoadEmg.pageUrl
                val webNestView = dialogLoadEmg.webNest
                if (webNestView != null) {
                    siteName = MainUtil.siteNameOf(webNestView.getTitle(), true)
                    if (TextUtils.isEmpty(siteName)) {
                        siteName = MainUtil.hostLabelUpper(MainUtil.hostNameOf(pageUrl!!, true))
                    }
                } else {
                    siteName = MainUtil.hostLabelUpper(MainUtil.hostNameOf(pageUrl!!, true))
                }
                intentQ.putExtra("EXTRA_NAME", siteName)
                intentQ.putExtra("EXTRA_INDEX", position)
                intentQ.putExtra("EXTRA_PAGE", 1)
                intentQ.putExtra("EXTRA_REFERER", dialogLoadEmg.pageUrl)
                intentQ.putExtra("EXTRA_PRELOAD", true)
                intentQ.putExtra("EXTRA_LOAD_TYPE", 1)
                dialogLoadEmg.hostActivity!!.startActivity(intentQ)
                val loadImgListener = dialogLoadEmg.loadListener
                if (loadImgListener != null) {
                    loadImgListener.onClose(isDone)
                }
            }
        }
    }
    companion object {
    }
}
