package com.mycompany.app.dialog

import android.text.TextUtils
import com.mycompany.app.data.DataUrl
import com.mycompany.app.data.DataUrl.ImgCntItem
import com.mycompany.app.dialog.DialogLoadImg.LoadImgListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUtil.hostLabelUpper
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.web.WebHmgTask
import com.mycompany.app.web.WebHmgTask.HmgTaskListener
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebViewActivity
import java.util.ArrayList

/**
 * Loader for the gallery site handled by [WebHmgTask] (opened by `WebViewActivity` when "Save all images" runs on that
 * site): runs the task on a hidden page, then opens the image viewer with the found pictures
 * (`EXTRA_LOAD_TYPE` 2). The sheet and its state machine are in [SiteLoadDialog]; this class owns the task and
 * keeps the old task listener. Compose now; same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogLoadHmg(
    webViewActivity: WebViewActivity?,
    webNestView: WebNestView?,
    url: String?,
    forceLoad: Boolean,
    loadImgListener: LoadImgListener?,
) : SiteLoadDialog(webViewActivity, webNestView, url, forceLoad, loadImgListener) {
    @JvmField
    var hmgTask: WebHmgTask? = null

    override fun createTask() {
        hmgTask = WebHmgTask(appContext, webNest, ShowResultStep())
    }

    override fun taskProgress(): Int? = hmgTask?.loadProgress()

    override fun taskStep() {
        hmgTask?.readPage()
    }

    override fun taskStop() {
        hmgTask?.release()
        hmgTask = null
    }

    internal inner class ShowResultStep : HmgTaskListener {
        override fun onFailed() {
            val dialogLoadHmg = this@DialogLoadHmg
            dialogLoadHmg.loadState = 2.toByte()
            dialogLoadHmg.showResult(false, false, false)
        }
        override fun onLoading() {
            val dialogLoadHmg = this@DialogLoadHmg
            if (dialogLoadHmg.loadState.toInt() == 2) {
                return
            }
            dialogLoadHmg.loadState = 1.toByte()
        }
        override fun onResult(urls: ArrayList<String?>?, thumbs: ArrayList<String?>?, position: Int) {
            var siteName: String?
            val dialogLoadHmg = this@DialogLoadHmg
            if (dialogLoadHmg.hmgTask == null || dialogLoadHmg.loadState.toInt() == 2) {
                return
            }
            val isDone = false
            dialogLoadHmg.loadState = 0.toByte()
            if (urls == null || urls.isEmpty()) {
                dialogLoadHmg.loadFailed = false
                val progress = dialogLoadHmg.hmgTask!!.loadProgress()
                if (progress < 100) {
                    dialogLoadHmg.onProgress(progress)
                    return
                } else if (dialogLoadHmg.progressMode) {
                    dialogLoadHmg.progressMode = false
                    dialogLoadHmg.onProgress(progress)
                    return
                }
            }
            if (dialogLoadHmg.hmgTask != null) {
                dialogLoadHmg.loadState = 2.toByte()
                if (urls == null || urls.isEmpty()) {
                    var loadFailed = dialogLoadHmg.loadFailed
                    if (!loadFailed) {
                        dialogLoadHmg.loadFailed = false
                        loadFailed = false
                    }
                    if (dialogLoadHmg.hmgTask!!.netError) {
                        dialogLoadHmg.showResult(false, true, false)
                        return
                    } else if (loadFailed) {
                        dialogLoadHmg.showResult(false, false, true)
                        return
                    } else {
                        dialogLoadHmg.showResult(false, false, false)
                        return
                    }
                }
                if (dialogLoadHmg.opening || dialogLoadHmg.hostActivity == null) {
                    return
                }
                dialogLoadHmg.opening = true
                val imgCntItem = ImgCntItem()
                imgCntItem.webpCount = urls.size
                val dataUrlB = DataUrl.instance(dialogLoadHmg.appContext)
                dataUrlB.urls = urls
                dataUrlB.thumbUrls = thumbs
                val intentQ = imageViewerIntent(dialogLoadHmg.appContext)
                intentQ.putExtra("EXTRA_TYPE", 12)
                val pageUrl = dialogLoadHmg.pageUrl
                val webNestView = dialogLoadHmg.webNest
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
                intentQ.putExtra("EXTRA_REFERER", dialogLoadHmg.pageUrl)
                intentQ.putExtra("EXTRA_PRELOAD", true)
                intentQ.putExtra("EXTRA_LOAD_TYPE", 2)
                dialogLoadHmg.hostActivity!!.startActivity(intentQ)
                val loadImgListener = dialogLoadHmg.loadListener
                if (loadImgListener != null) {
                    loadImgListener.onClose(isDone)
                }
            }
        }
    }
    companion object {
    }
}
