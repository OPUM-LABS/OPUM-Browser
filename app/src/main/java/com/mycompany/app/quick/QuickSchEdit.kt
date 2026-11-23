package com.mycompany.app.quick

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil.isLandscape
import com.mycompany.app.quick.QuickAdapter.QuickItem
import com.mycompany.app.quick.QuickView.QuickViewListener
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.web.WebViewActivity

@Suppress("deprecation")
class QuickSchEdit(
    webViewActivity: WebViewActivity?,
    context: Context?,
    editModeParam: Int,
    quickViewListener: QuickViewListener?
) : FrameLayout(webViewActivity!!) {
    @JvmField
    var hostActivity: WebViewActivity?
    @JvmField
    var appContext: Context?
    @JvmField
    var mainHandler: Handler?
    @JvmField
    var viewListener: QuickViewListener?
    @JvmField
    var shownView: QuickView? = null
    @JvmField
    var shownControl: QuickControl? = null
    @JvmField
    var edgeStarted: Boolean = false
    @JvmField
    var opening: Boolean
    @JvmField
    var editMode: Int
    @JvmField
    var newQuickView: QuickView? = null
    @JvmField
    var newQuickControl: QuickControl? = null

    internal inner class CreateViewsTask : Runnable {
        override fun run() {
            this@QuickSchEdit.createViews()
        }
    }

    internal inner class ApplyEditModeTask : Runnable {
        override fun run() {
            val quickSchEdit = this@QuickSchEdit
            val editMode = quickSchEdit.editMode
            val quickView = quickSchEdit.newQuickView
            val quickControl = quickSchEdit.newQuickControl
            quickSchEdit.newQuickView = null
            quickSchEdit.newQuickControl = null
            quickSchEdit.opening = false
            if (quickView == null || quickControl == null) {
                return
            }
            quickSchEdit.shownView = quickView
            quickSchEdit.shownControl = quickControl
            val viewActions = quickSchEdit.QuickViewActions()
            quickView.onStartPage = false
            quickView.quickListener = viewActions
            val layoutParams = LayoutParams(-1, -1)
            layoutParams.topMargin = MainApp.dp48
            quickSchEdit.addView(quickSchEdit.shownView, layoutParams)
            quickSchEdit.addView(quickSchEdit.shownControl, -1, -1)
            quickSchEdit.shownControl!!.setQuickView(quickSchEdit.shownView)
            val shownView = quickSchEdit.shownView
            shownView!!.editControl = quickSchEdit.shownControl
            shownView.editIndex = editMode
            val handler = quickSchEdit.mainHandler
            if (handler != null) {
                handler.post(quickSchEdit.RefreshViewTask())
            }
        }
    }

    internal inner class RefreshViewTask : Runnable {

        internal inner class RefreshViewStep : Runnable {

            internal inner class RefreshTask : Runnable {
                override fun run() {
                    val quickView = this@QuickSchEdit.shownView
                    if (quickView == null) {
                        return
                    }
                    quickView.refresh()
                }
            }

            override fun run() {
                val quickSchEdit = this@QuickSchEdit
                val quickView = quickSchEdit.shownView
                if (quickView != null) {
                    quickView.setupView()
                    val handler = quickSchEdit.mainHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(RefreshTask())
                }
            }
        }

        override fun run() {
            val quickSchEdit = this@QuickSchEdit
            val quickView = quickSchEdit.shownView
            if (quickView != null) {
                quickView.createButton()
                val handler = quickSchEdit.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(RefreshViewStep())
            }
        }
    }

    init {
        this.hostActivity = webViewActivity
        this.appContext = context
        this.viewListener = quickViewListener
        this.opening = true
        this.editMode = editModeParam
        val handler = Handler(Looper.getMainLooper())
        this.mainHandler = handler
        handler.post(CreateViewsTask())
    }

    private val uiContext: Context?
        get() {
            val webViewActivity = this.hostActivity
            return if (webViewActivity != null) webViewActivity else this.appContext
        }

    fun createViews() {
        val uiContext = this.uiContext
        if (uiContext != null) {
            this.newQuickView = QuickView(uiContext)
            val quickControl = QuickControl(uiContext)
            quickControl.applyMode(false)
            this.newQuickControl = quickControl
            val handler = this.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ApplyEditModeTask())
        }
    }

    fun isLandscapeNow(): Boolean {
        val quickViewListener = this.viewListener
        return if (quickViewListener == null) isLandscape(this.uiContext) else quickViewListener.isLandscape()
    }

    internal inner class QuickViewActions : QuickViewListener {
        override fun isBarUpdatePending(): Boolean {
            return false
        }

        override fun isLandscape(): Boolean {
            return this@QuickSchEdit.isLandscapeNow()
        }

        override fun showLinkDialog(quickItem: QuickItem?, isFolder: Boolean) {
        }

        override fun hasTransNews(): Boolean {
            return false
        }

        override fun openNewsItem(quickItem: QuickItem?) {
        }

        override fun webBody(): MyWebBody? {
            val quickViewListener = this@QuickSchEdit.viewListener
            if (quickViewListener == null) {
                return null
            }
            return quickViewListener.webBody()
        }

        override fun onPull(x: Float, y: Float, action: Int): Boolean {
            return false
        }

        override fun newsListType(): Int {
            return 0
        }

        override fun setQuickMode(enabled: Boolean) {
            val quickViewListener = this@QuickSchEdit.viewListener
            if (quickViewListener != null) {
                quickViewListener.setQuickMode(enabled)
            }
        }

        override fun closeDeleteDialog() {
            val quickViewListener = this@QuickSchEdit.viewListener
            if (quickViewListener != null) {
                quickViewListener.closeDeleteDialog()
            }
        }

        override fun runQuickAction2() {
        }

        override fun runQuickAction1() {
        }

        override fun showEditDialog(quickItem: QuickItem?) {
            val quickViewListener = this@QuickSchEdit.viewListener
            if (quickViewListener != null) {
                quickViewListener.showEditDialog(quickItem)
            }
        }

        override fun translateNews(list: MutableList<com.mycompany.app.quick.QuickAdapter.QuickItem?>?) {
        }

        override fun showDeleteDialog(quickItem: QuickItem?, position: Int) {
            val quickViewListener = this@QuickSchEdit.viewListener
            if (quickViewListener != null) {
                quickViewListener.showDeleteDialog(quickItem, position)
            }
        }

        override fun isAddingTab(): Boolean {
            return false
        }

        override fun addRssButton() {
        }

        override fun showSettingsMenu(view: View?) {
        }

        override fun openSearchSource(type: Int, url: String?) {
        }

        override fun refreshUpButton() {
        }

        override fun loadSearchItems() {
            val quickViewListener = this@QuickSchEdit.viewListener
            if (quickViewListener != null) {
                quickViewListener.loadSearchItems()
            }
        }

        override fun scrollPage() {
        }

        override fun hasNews(): Boolean {
            return false
        }

        override fun loadNews() {
        }

        override fun openFolder(type: Int, left: Int, top: Int, path: String?, color: Int) {
        }
    }

    companion object {
    }
}
