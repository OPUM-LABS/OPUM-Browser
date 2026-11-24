package com.mycompany.app.quick

import com.mycompany.app.web.TabGridListener
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings

import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.ViewItemListener
import com.mycompany.app.main.JarBridge
import com.mycompany.app.main.MainItem
import java.util.ArrayList
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Outline
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.app.data.DataNews
import com.mycompany.app.data.book.DataBookRecent
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.book.DbBookQuick
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookQuick.Companion.orderOf
import com.mycompany.app.db.book.DbBookQuick.Companion.deletePath
import com.mycompany.app.db.book.DbBookQuick.Companion.insertItem
import com.mycompany.app.db.book.DbBookRecent.Companion.deleteById
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.ViewItem
import com.mycompany.app.main.MainListLoader
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.isLandscape
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.main.MainUtil.isDarkMode
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.main.MainUtil.defaultUserAgent
import com.mycompany.app.main.MainUtil.copyText
import com.mycompany.app.main.MainUtil.stripPunctuation
import com.mycompany.app.main.MainUtil.isDarkBars
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.pref.PrefZtwo
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.browser.IconButton
import com.mycompany.app.view.MyProgressBar
import com.mycompany.app.view.MyRoundImage
import com.mycompany.app.web.WebReadTask
import com.mycompany.app.web.WebSearchAdapter.SearchItem

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.concurrent.ExecutorService
import java.util.regex.Pattern
import kotlin.math.min

@Suppress("deprecation")
class QuickAdapter(
    @JvmField var appContext: Context?,
    listModeParam: Int,
    @JvmField var darkState: Boolean,
    @JvmField var itemListener: QuickListener?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    @JvmField
    var recentItems: MutableList<com.mycompany.app.web.WebSearchAdapter.SearchItem?>? = null
    @JvmField
    var recentListener: QuickRcntListener? = null
    @JvmField
    var recentBusy: Boolean = false
    @JvmField
    var frozenCount: Int = 0
    @JvmField
    var hasHeader: Boolean = false
    @JvmField
    var footerCount: Byte = 0
    @JvmField
    var listMode: Byte
    @JvmField
    var reversed: Boolean = false
    @JvmField
    var headerHeight: Int = 0
    @JvmField
    var tabListener: TabGridListener? = null
    @JvmField
    var items: ArrayList<QuickItem?>? = null
    @JvmField
    var newsItems: MutableList<QuickItem?>? = null
    @JvmField
    var newsLoading: Boolean = false
    @JvmField
    var editMode: Boolean = false
    @JvmField
    var listLoader: MainListLoader?
    @JvmField
    var mainHandler: Handler? = null
    @JvmField
    var orderBusy: Boolean = false
    @JvmField
    var userAgent: String? = null
    @JvmField
    var newsPending: Boolean = false
    @JvmField
    var executor: ExecutorService? = null
    @JvmField
    var newsHeadOnly: Boolean = false
    @JvmField
    var newsDragPos: Int = -1
    @JvmField
    var recentMode: Boolean = false

    internal inner class TileLoadListener : ListLoadListener {
        override fun onFailed(childItem: ChildItem?, view: View?) {
            val holder = (if (childItem == null) null else holderOf(view)) ?: return
            val tile = holder.tile ?: return
            val pos = holder.pos
            if (pos != childItem!!.position) return
            val item = itemAt(pos) ?: return
            tileFallback(tile, item, if (holder.tileMode != 1) backColor() else 0)
        }

        override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
            val holder = (if (childItem == null) null else holderOf(view)) ?: return
            val tile = holder.tile ?: return
            val pos = holder.pos
            if (pos != childItem!!.position) return
            val back = if (holder.tileMode != 1) backColor() else 0
            if (MainUtil.isBitmapOk(bitmap)) {
                tile.iconBitmap(bitmap, back)
                return
            }
            val item = itemAt(pos) ?: return
            tileFallback(tile, item, back)
        }
    }

    internal inner class ItemClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val quickHolderI: QuickHolder? = holderOf(view)
            val quickListener: QuickListener? = if (quickHolderI == null) null else this@QuickAdapter.itemListener
            if (quickHolderI == null || quickListener == null) {
                return
            }
            quickListener.onItemClick(quickHolderI, quickHolderI.pos)
        }
    }

    internal inner class ItemLongClick : OnLongClickListener {
        override fun onLongClick(view: View?): Boolean {
            val quickHolderI: QuickHolder? = holderOf(view)
            val quickListener: QuickListener? = if (quickHolderI != null) this@QuickAdapter.itemListener else null
            if (quickHolderI != null && quickListener != null) {
                quickListener.onItemLongClick(quickHolderI, quickHolderI.pos)
            }
            return true
        }
    }

    internal inner class RecentAtClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val adapter = this@QuickAdapter
            val item: SearchItem? = if (adapter.recentListener == null) null else adapter.recentAt(positionOf(view))
            if (adapter.recentListener == null || item == null) {
                return
            }
            val listener = adapter.recentListener!!.owner()!!.searchListener
            if (listener != null) {
                listener.onItemClick(item)
            }
        }
    }

    internal inner class RecentAtLongClick : OnLongClickListener {

        internal inner class CopyTitleTask(
                                             @JvmField val clipText: String?
        ) : Runnable {
            /** Copies a recent search to the clipboard and offers it in the search field.  */
            override fun run() {
                val adapter = this@QuickAdapter
                copyText(adapter.appContext, R.string.copied_clipboard, "Copied text", this.clipText)
                if (adapter.recentListener != null) {
                    adapter.recentListener!!.owner()!!.setLoadClip(this.clipText)
                }
            }
        }

        override fun onLongClick(view: View?): Boolean {
            val quickAdapter = this@QuickAdapter
            val searchItemG: SearchItem? = if (quickAdapter.recentListener == null) null else quickAdapter.recentAt(positionOf(view))
            if (quickAdapter.recentListener == null || searchItemG == null) {
                return true
            }
            val title = searchItemG.title
            if (TextUtils.isEmpty(title)) {
                showToast(quickAdapter.appContext, R.string.empty)
                return true
            }
            quickAdapter.runAsync(CopyTitleTask(title))
            return true
        }
    }

    internal inner class DeleteRecentClick : View.OnClickListener {

        internal inner class DeleteRecentTask(@JvmField val outer: DeleteRecentClick?,
                                             @JvmField val index: Int
        ) : Runnable {

            internal inner class RefreshRecentStep : Runnable {
                override fun run() {
                    val recentClick = this@DeleteRecentTask.outer
                    this@QuickAdapter.notifyDataSetChanged()
                    this@QuickAdapter.recentBusy = false
                }
            }

            override fun run() {
                val quickAdapter = this@QuickAdapter
                val searchItemG = quickAdapter.recentAt(this.index)
                if (searchItemG == null) {
                    return
                }
                val list = quickAdapter.recentItems
                if (list != null && !list.isEmpty()) {
                    try {
                        list.remove(searchItemG)
                        deleteById(quickAdapter.appContext, searchItemG.id)
                        DataBookRecent.instance(quickAdapter.appContext).changed = false
                        if (quickAdapter.mainHandler == null) {
                            quickAdapter.mainHandler = Handler(Looper.getMainLooper())
                        }
                        quickAdapter.mainHandler!!.post(RefreshRecentStep())
                        return
                    } catch (unused: Exception) {
                    }
                }
                quickAdapter.recentBusy = false
            }
        }

        override fun onClick(view: View?) {
            val quickAdapter = this@QuickAdapter
            if (quickAdapter.recentBusy) {
                return
            }
            quickAdapter.recentBusy = true
            quickAdapter.runAsync(DeleteRecentTask(this, positionOf(view)))
        }
    }

    internal inner class RecentPathClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val quickAdapter = this@QuickAdapter
            val searchItemG: SearchItem? = if (quickAdapter.recentListener == null) null else quickAdapter.recentAt(positionOf(view))
            if (quickAdapter.recentListener == null || searchItemG == null) {
                return
            }
            if (!TextUtils.isEmpty(searchItemG.path)) {
                val quickRcntListener = quickAdapter.recentListener
                val path: String = searchItemG.path!!
                quickRcntListener!!.onRecentPick(path.length, path)
            } else {
                if (TextUtils.isEmpty(searchItemG.title)) {
                    return
                }
                val fillText = (StringBuilder()).append(searchItemG.title).append(" ").toString()
                (quickAdapter.recentListener as QuickSearch.SetQueryStep).onRecentPick(fillText.length, fillText)
            }
        }
    }

    internal inner class NotifyDataSetChangedTask : Runnable {
        override fun run() {
            this@QuickAdapter.notifyDataSetChanged()
        }
    }

    internal inner class ThumbListener : ViewItemListener {
        override fun onFailed(viewItem: MainItem.ViewItem?, view: View?, failReason: Exception?) {
        }

        override fun onComplete(viewItem: MainItem.ViewItem?, view: View?, bitmap: Bitmap?) {
            val quickHolderI: QuickHolder? = if (viewItem == null || this@QuickAdapter.appContext == null || !MainUtil.isBitmapOk(bitmap)) null else holderOf(view)
            if (viewItem == null || this@QuickAdapter.appContext == null || !MainUtil.isBitmapOk(bitmap) || quickHolderI == null || quickHolderI.sourceIcon == null || quickHolderI.pos != viewItem.position) {
                return
            }
            quickHolderI.sourceIcon!!.setImageBitmap(bitmap)
        }
    }

    internal inner class ChangedClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val tabGridListener = this@QuickAdapter.tabListener
            if (tabGridListener != null) {
                (tabGridListener as QuickView.ScrollPageStep).onChanged()
            }
        }
    }

    internal inner class FindByPathStep : ViewItemListener {
        override fun onFailed(viewItem: MainItem.ViewItem?, view: View?, failReason: Exception?) {
            if (viewItem != null) {
                val quickAdapter = this@QuickAdapter
                val quickHolderI: QuickHolder? = if (quickAdapter.appContext == null) null else holderOf(view)
                val quickItemF: QuickItem? = if (quickAdapter.appContext == null || quickHolderI == null || quickHolderI.thumb == null || quickHolderI.pos != viewItem.position) null else quickAdapter.findByPath(
                        viewItem.iconUrl
                    )
                if (quickAdapter.appContext == null || quickHolderI == null || quickHolderI.thumb == null || quickHolderI.pos != viewItem.position || quickItemF == null) {
                    return
                }
                val myRoundImage = quickHolderI.thumb
                val position = viewItem.position
                if (myRoundImage == null || TextUtils.isEmpty(quickItemF.thumbUrl)) {
                    return
                }
                val bitmapA = com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(2, quickItemF.thumbUrl))
                if (MainUtil.isBitmapOk(bitmapA)) {
                    quickAdapter.applyThumb(myRoundImage, bitmapA)
                    return
                }
                val failItem = ViewItem()
                failItem.itemType = 7.toByte()
                failItem.url = quickItemF.thumbUrl
                failItem.position = position
                failItem.cacheType = 2
                quickAdapter.loadThumb(failItem, myRoundImage)
            }
        }

        override fun onComplete(viewItem: MainItem.ViewItem?, view: View?, bitmap: Bitmap?) {
            if (viewItem != null) {
                val quickAdapter = this@QuickAdapter
                val quickHolderI: QuickHolder? = if (quickAdapter.appContext == null || !MainUtil.isBitmapOk(bitmap)) null else holderOf(view)
                if (quickAdapter.appContext == null || !MainUtil.isBitmapOk(bitmap) || quickHolderI == null || quickHolderI.thumb == null || quickHolderI.pos != viewItem.position) {
                    return
                }
                quickAdapter.applyThumb(quickHolderI.thumb, bitmap)
            }
        }
    }

    internal inner class FetchDocumentTask(@JvmField val outer: QuickAdapter,
                                          @JvmField val url: String?,
                                          @JvmField val generation: Int
    ) : Runnable {

        /** Finds the lead image of news article `onRecentPick` (following a script redirect once) and caches it.  */
        override fun run() {
            val adapter = this.outer
            val url = this.url
            val page = adapter.fetchDocument(url)
            if (page == null || adapter.mainHandler == null) {
                return
            }
            var image = WebReadTask.mainImage(page, true)
            if (TextUtils.isEmpty(image)) {
                var redirect: String? = null
                try {
                    val script = page.selectFirst("script")
                    if (script != null) {
                        val code = script.html()
                        val at = if (!TextUtils.isEmpty(code)) code.indexOf("location.href='http") else 0
                        val end = if (!TextUtils.isEmpty(code) && at != -1) code.indexOf('\'', at + 20) else 0
                        if (!TextUtils.isEmpty(code) && at != -1 && end != -1) {
                            redirect = code.substring(at + 15, end)
                            if (MainUtil.sameObject(redirect, page.location())) {
                                redirect = null
                            }
                        }
                    }
                } catch (unused: Exception) {
                }
                val target = adapter.fetchDocument(redirect)
                if (target == null || adapter.mainHandler == null) {
                    return
                }
                image = WebReadTask.mainImage(target, true)
                if (TextUtils.isEmpty(image)) {
                    return
                }
            }
            val item = adapter.findByPath(url)
            if (item == null) {
                return
            }
            val news = DataNews.instance(adapter.appContext)
            if (item.newsIndex >= 0 && news.items != null && item.newsIndex < news.items!!.size) {
                val cached = news.items!!.get(item.newsIndex)
                if (cached != null) {
                    cached.imageUrl = image
                    news.loadedAt = System.currentTimeMillis()
                }
            }
            item.imageUrl = image
            if (adapter.mainHandler != null) {
                adapter.mainHandler!!.post(FindByPathTask(adapter, url, this.generation))
            }
        }
    }

    internal inner class FindByPathTask(@JvmField val outer: QuickAdapter,
                                          @JvmField val iconUrl: String?,
                                          @JvmField val generation: Int
    ) : Runnable {
        override fun run() {
            val iconUrl = this.iconUrl
            val quickAdapter = this.outer
            val quickItemF = quickAdapter.findByPath(iconUrl)
            val quickHolder: QuickHolder? = if (quickItemF == null) null else quickItemF.quickHolder
            if (quickItemF == null || quickHolder == null) {
                return
            }
            val position = quickHolder.pos
            val generation = this.generation
            if (position != generation || TextUtils.isEmpty(quickItemF.imageUrl)) {
                return
            }
            quickAdapter.loadNewsImage(quickItemF, quickHolder.thumb, generation)
        }
    }

    internal inner class CardOpenClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val quickAdapter = this@QuickAdapter
            val quickItemD: QuickItem? = if (quickAdapter.itemListener == null) null else quickAdapter.itemAt(positionOf(view))
            if (quickAdapter.itemListener == null || quickItemD == null) {
                return
            }
            quickAdapter.itemListener!!.openNewsItem(quickItemD)
        }
    }

    internal inner class CardLongClick : OnLongClickListener {
        override fun onLongClick(view: View?): Boolean {
            val quickAdapter = this@QuickAdapter
            val quickItemD: QuickItem? = if (quickAdapter.itemListener == null) null else quickAdapter.itemAt(positionOf(view))
            if (quickAdapter.itemListener == null || quickItemD == null) {
                return false
            }
            quickAdapter.itemListener!!.showLinkDialog(quickItemD, true)
            return true
        }
    }

    internal inner class CardMoreClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val quickAdapter = this@QuickAdapter
            val quickItemD: QuickItem? = if (quickAdapter.itemListener == null) null else quickAdapter.itemAt(positionOf(view))
            if (quickAdapter.itemListener == null || quickItemD == null) {
                return
            }
            quickAdapter.itemListener!!.showLinkDialog(quickItemD, false)
        }
    }

    class QuickHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        @JvmField
        var sourceIcon: MyRoundImage? = null
        /** The start page's spacer row (item kind 9). */
        @JvmField
        var homeHeader: QuickHomeHeader? = null
        @JvmField
        var tileMode: Int = 0
        @JvmField
        var thumb: MyRoundImage? = null
        @JvmField
        var bar: MyProgressBar? = null
        /** The row's position in the Compose grid (−1 when it sits in a RecyclerView). */
        @JvmField
        var gridPos: Int = -1

        /** The row's adapter position, in the grid or in a RecyclerView. */
        val pos: Int get() = if (gridPos >= 0) gridPos else bindingAdapterPosition

        /** Shortcut tiles (types 0 and 1): the Compose tile, which is also the item view. */
        @JvmField
        var tile: QuickTileView? = null
        /** Search mode: the Compose recent-search row, which is also the item view. */
        @JvmField
        var searchRow: QuickSearchRow? = null
        /** Types 3–5: the Compose notice row, which is also the item view. */
        @JvmField
        var notice: QuickNoticeCard? = null
        /** Type 7: the Compose news card (its thumbnail and source icon are [transState] and [tileMode]). */
        @JvmField
        var news: QuickNewsCard? = null
    }

    class QuickItem {
        @JvmField
        var itemKind: Byte = 0
        @JvmField
        var isFolder: Boolean = false
        @JvmField
        var path: String? = null
        @JvmField
        var link: String? = null
        @JvmField
        var label: String? = null
        @JvmField
        var title: String? = null
        @JvmField
        var color: Int = 0
        /** The user chose the letter in the editor (1-byte `_icon`): no favicon. */
        @JvmField
        var letterOnly: Boolean = false
        @JvmField
        var order: Int = 0
        @JvmField
        var isChecked: Boolean = false
        @JvmField
        var changed: Boolean = false
        @JvmField
        var subItems: ArrayList<QuickSubItem?>? = null
        @JvmField
        var blockStart: Int = 0
        @JvmField
        var keyEnd: Int = 0
        @JvmField
        var newsIndex: Int = 0
        @JvmField
        var imageUrl: String? = null
        @JvmField
        var thumbUrl: String? = null
        @JvmField
        var summary: String? = null
        @JvmField
        var time: Long = 0
        @JvmField
        var sourceHtml: String? = null
        @JvmField
        var quickHolder: QuickHolder? = null
        @JvmField
        var transTitle: String? = null
        @JvmField
        var sourceName: String? = null
        @JvmField
        var transState: Byte = 0
    }

    interface QuickListener {
        fun isLandscape(): Boolean

        fun showLinkDialog(quickItem: QuickItem?, isFolder: Boolean)

        fun hasTransNews(): Boolean

        fun openNewsItem(quickItem: QuickItem?)

        fun newsListType(): Int

        fun openNewsSettings()

        fun openNewsSources()

        fun translateNews(list: MutableList<com.mycompany.app.quick.QuickAdapter.QuickItem?>?)

        fun onItemClick(quickHolder: QuickHolder?, position: Int)

        fun onEditMode(enabled: Boolean)

        fun onItemLongClick(quickHolder: QuickHolder?, position: Int)
    }

    /** Implemented by QuickSearch.CardOpenClick (recent searches in the quick search panel).  */
    interface QuickRcntListener {
        fun onRecentPick(cursor: Int, query: String?)

        fun owner(): QuickSearch?
    }

    class QuickSubItem {
        @JvmField
        var iconUrl: String? = null
        @JvmField
        var letterText: String? = null
        @JvmField
        var color: Int = 0
        /** The user chose the letter in the editor (1-byte `_icon`): no favicon. */
        @JvmField
        var letterOnly: Boolean = false
    }

    /**
     * The news page (2026-11-19): the list shows the news only (header, cards, notices). Off, the start page shows no
     * news; its spacer row carries the home strip ([QuickHomeHeader]) with the "News" chip that turns this on.
     */
    @JvmField
    var newsPage: Boolean = false

    /** The RSS reader (2026-11-19): with [newsPage], the list shows [rssRows] instead of the news. */
    @JvmField
    var rssPage: Boolean = false
    /** Masthead, then a notice (loading / no feeds / nothing read) or the articles. */
    private var rssRows: List<QuickItem> = emptyList()
    /** The RSS notice's text (0 = none). */
    private var rssNotice = 0

    /** The News page's tab labels and the selected one (set by QuickView). */
    @JvmField
    var tabLabels: List<String> = emptyList()
    @JvmField
    var tabIndex: Int = 0
    /** On a feeds tab: only this feed's articles (null = all feeds). */
    @JvmField
    var rssFilter: String? = null
    private val topHeader = QuickItem().also { it.itemKind = 5 }

    /** Shows [articles] on a feeds tab (filtered to [rssFilter]); [loading] true while the feeds are being read. */
    fun applyRss(all: List<QuickItem>, loading: Boolean) {
        val filter = rssFilter
        val articles = if (filter == null) all else all.filter { it.link == filter }
        val rows = ArrayList<QuickItem>()
        rows.add(QuickItem().also { it.itemKind = 5 })
        rssNotice = when {
            articles.isNotEmpty() -> 0
            loading -> R.string.rss_loading
            RssFeeds.feeds().isEmpty() -> R.string.rss_empty
            else -> R.string.rss_nothing
        }
        if (rssNotice != 0) rows.add(QuickItem().also { it.itemKind = 6 })
        rows.addAll(articles)
        rssRows = rows
        if (this.rssPage) notifyDataSetChanged()
    }

    /** The rows of the open page: the RSS rows or the news. */
    private fun pageRows(): List<Any?>? = if (this.rssPage) rssRows else (this.newsItems ?: listOf(topHeader))

    /** Taps of the home strip. */
    interface HomeListener {
        fun openNews()
        fun closeNews()
        fun selectTab(index: Int)
        fun addFeed()
        fun openBook(path: String)
        fun openAllBooks()
    }

    @JvmField
    var homeListener: HomeListener? = null
    private var homeBooks: List<QuickHomeHeader.Book> = emptyList()
    private var homeBooksAt = 0L
    private var homeBooksJob: kotlinx.coroutines.Job? = null

    /** True while the start page's spacer row shows the home strip (news on, or bookmarks). */
    fun homeStripWanted(): Boolean = this.darkState && this.listMode.toInt() == 0 && (ZtwoSettings.newsButton.value || homeBooks.isNotEmpty())

    /** Forget the loaded bookmarks so the next bind reads them again (the start page was shown again). */
    fun refreshHomeBooks() {
        homeBooksAt = 0L
    }

    /** The most recent bookmarks (current secret mode) with their favicons; the strip updates when done. */
    private fun loadHomeBooks() {
        val context = this.appContext ?: return
        if (homeBooksJob?.isActive == true) return
        homeBooksAt = System.currentTimeMillis()
        val secret = SyncSettings.secretMode.value
        homeBooksJob = iconScope.launch {
            val list = kotlinx.coroutines.withContext(QuickIcons.loads) { readHomeBooks(context, secret) }
            if (this@QuickAdapter.appContext == null) return@launch
            val hadStrip = homeStripWanted()
            val same = list.size == homeBooks.size && list.indices.all { list[it].id == homeBooks[it].id && list[it].title == homeBooks[it].title && (list[it].icon == null) == (homeBooks[it].icon == null) }
            homeBooks = list
            if (same) return@launch
            if (hadStrip != homeStripWanted() && !ZtwoSettings.quickDown.value) {
                // shortcuts at the top: the spacer is the strip's height or nothing
                this@QuickAdapter.headerHeight = if (homeStripWanted()) Math.round(QuickHomeHeader.STRIP_DP * context.resources.displayMetrics.density) else 1
                notifyDataSetChanged()
                return@launch
            }
            val header = rowAt(0)?.homeHeader
            if (header != null) bindHomeHeader(header) else notifyItemChanged(0)
        }
    }

    private fun readHomeBooks(context: Context, secret: Boolean): List<QuickHomeHeader.Book> {
        val out = ArrayList<QuickHomeHeader.Book>()
        var cursor: android.database.Cursor? = null
        try {
            cursor = com.mycompany.app.db.DbUtil.query(
                com.mycompany.app.db.book.DbBookWeb.get(context).writableDatabase, "DbBookWeb_table",
                arrayOf<String?>("_id", "_path", "_title"),
                "_secret=? AND (_isdir IS NULL OR _isdir=0) AND _path IS NOT NULL AND _path<>''",
                arrayOf<String?>(if (secret) "1" else "0"), "_time DESC LIMIT " + QuickHomeHeader.BOOK_COUNT,
            )
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(0)
                    val path = cursor.getString(1) ?: continue
                    var title = cursor.getString(2)
                    if (TextUtils.isEmpty(title)) title = MainUtil.shortHostOf(path) ?: path
                    var icon: android.graphics.Bitmap? = try { MainUtil.loadAlbumThumb(17, id, context, path) } catch (_: Exception) { null }
                    if (!MainUtil.isBitmapOk(icon)) {
                        val host = MainUtil.shortHostOf(path)
                        icon = if (TextUtils.isEmpty(host)) null else try { com.mycompany.app.db.book.DbBookIcon.iconOf(context, host) } catch (_: Exception) { null }
                    }
                    out.add(QuickHomeHeader.Book(id, path, title!!, icon?.takeIf { MainUtil.isBitmapOk(it) }))
                } while (cursor.moveToNext())
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
        return out
    }

    private fun webStyle0(): Boolean = MainUtil.isDarkBars(this.darkState)

    private val newsDateFormat = java.text.SimpleDateFormat("yyyy.MM.dd HH:mm:ss", java.util.Locale.US)

    /** "2 hours ago" for a news date ("yyyy.MM.dd HH:mm:ss"); other texts as they are. */
    private fun newsAge(date: String?): String {
        if (TextUtils.isEmpty(date)) return ""
        val time = try { synchronized(newsDateFormat) { newsDateFormat.parse(date!!)?.time } } catch (_: Exception) { null } ?: return date!!
        val now = System.currentTimeMillis()
        if (time > now + 60_000L || now - time > 7L * 86_400_000L) return date!!
        return android.text.format.DateUtils.getRelativeTimeSpanString(time, now, android.text.format.DateUtils.MINUTE_IN_MILLIS).toString()
    }

    /** Fills the spacer row's home strip. */
    private fun bindHomeHeader(header: QuickHomeHeader) {
        header.heightPx = this.headerHeight
        header.atBottom = ZtwoSettings.quickDown.value
        header.dark = if (!MainUtil.isDarkBars(this.darkState)) AppState.darkUi else (AppState.darkUi || BrowserSettings.darkHome.value)
        header.showNews = ZtwoSettings.newsButton.value
        header.books = homeBooks
        header.hasBooks = homeBooks.isNotEmpty()
        // inert while the shortcuts are being edited
        header.onNews = { if (!this.editMode) homeListener?.openNews() }
        header.onBook = { if (!this.editMode) homeListener?.openBook(it.path) }
        header.onAllBooks = { if (!this.editMode) homeListener?.openAllBooks() }
        if (System.currentTimeMillis() - homeBooksAt > 3000L) loadHomeBooks()
    }

    /** What shows the rows: the Compose grid ([QuickGridView]). */
    @JvmField
    var host: RowHost? = null

    /** The holder now showing [pos], or null when it is not on screen. */
    fun rowAt(pos: Int): QuickHolder? = host?.rowAt(pos)

    /** Finds rows by position for whatever displays the adapter. */
    interface RowHost {
        fun rowAt(pos: Int): QuickHolder?
        fun firstVisible(): Int
        fun lastVisible(): Int
    }

    init {
        this.listMode = (listModeParam).toByte()
        updateHeaderFooter()
        this.listLoader = MainListLoader(this.appContext!!, false, TileLoadListener())
    }

    fun firstChecked(): QuickItem? {
        val arrayList = this.items
        if (arrayList == null || arrayList.isEmpty()) {
            return null
        }
        val size = arrayList.size
        var index = 0
        while (index < size) {
            val rawQuickItem: Any? = arrayList.get(index)
            index++
            val quickItem = rawQuickItem as QuickItem?
            if (quickItem != null && quickItem.itemKind.toInt() == 0 && quickItem.isChecked) {
                return quickItem
            }
        }
        return null
    }

    fun bookCount(): Int {
        val arrayList = this.items
        val size = if (arrayList == null || arrayList.size == 0) 0 else ((this.items!!.size - (if (this.hasHeader) 1 else 0)) - this.footerCount)
        if (arrayList == null || arrayList.size == 0 || size < 0) {
            return 0
        }
        return size
    }

    fun itemAt(pos: Int): QuickItem? {
        var pos = pos
        if (this.newsPage) return pageRows()?.getOrNull(pos) as QuickItem?
        val offset: Int
        val listMode: Byte
        val arrayList = this.items
        if (arrayList != null) {
            val size = arrayList.size
            if (pos >= 0 && pos < size) {
                return arrayList.get(pos)
            }
            if (SyncSettings.secretMode.value && showsQuick() && ((this.listMode.also { listMode = it }).toInt() == 0 || listMode.toInt() == 3)) {
                if (size != (if (this.hasHeader) 1 else 0) + this.footerCount) {
                    offset = 0
                } else {
                    if (pos == size) {
                        val quickItem = QuickItem()
                        quickItem.itemKind = 3.toByte()
                        return quickItem
                    }
                    offset = 1
                }
            } else {
                offset = 0
            }
            pos = (pos - size) - offset
        }
        // the start page shows no news (they have their own page)
        if (this.darkState && this.listMode.toInt() == 0) return null
        val list: MutableList<QuickItem?>? = if (!this.darkState || !newsEnabled()) null else this.newsItems
        if (!this.darkState || !newsEnabled() || list == null || pos < 0 || pos >= list.size) {
            return null
        }
        return list.get(pos)
    }

    fun fetchDocument(url: String?): Document? {
        if (!URLUtil.isNetworkUrl(url) || this.mainHandler == null) {
            return null
        }
        if (TextUtils.isEmpty(this.userAgent)) {
            this.userAgent = MainUtil.defaultUserAgent(this.appContext)
        }
        try {
            return Jsoup.connect(url!!).referrer(url).userAgent(this.userAgent!!).get()
        } catch (unused: Exception) {
            return null
        } catch (unused: LinkageError) {
            return null
        } catch (unused: OutOfMemoryError) {
            return null
        }
    }

    fun findByPath(iconUrl: String?): QuickItem? {
        val list: MutableList<QuickItem?>? = if (!TextUtils.isEmpty(iconUrl)) this.newsItems else null
        if (!TextUtils.isEmpty(iconUrl) && list != null && !list.isEmpty()) {
            for (quickItem in list) {
                if (quickItem != null && iconUrl == quickItem.path) {
                    return quickItem
                }
            }
        }
        return null
    }

    fun recentAt(index: Int): SearchItem? {
        var index = index
        val list = this.recentItems
        if (list == null || index < 0 || index >= list.size) {
            return null
        }
        if (this.reversed) {
            index = (list.size - 1) - index
        }
        return list.get(index)
    }

    /**
     * Height of the shortcut rows beyond the first (2026-11-20). The spacer above the shortcuts ([updateHeaderHeight])
     * used to leave room for one row only; further rows went under the bottom bars (cut off with the single bar, hidden
     * with two bars) and were to be scrolled into view on opening, which did not happen. Now the spacer leaves room for
     * every row, counting the "+" tile.
     */
    private fun extraRowsHeight(): Int {
        if (!ZtriSettings.quickUse.value) return 0
        val tiles = bookCount() + (if (ZtriSettings.quickPlus.value) 1 else 0)
        if (tiles == 0) return 0
        val landscape = if (this.itemListener == null) isLandscape(this.appContext) else this.itemListener!!.isLandscape()
        var columns = if (landscape) ZtriSettings.quickLand.value else ZtriSettings.quickPort.value
        if (columns <= 0) columns = 5
        val rows = (tiles + columns - 1) / columns
        return (rows - 1).coerceAtLeast(0) * MainApp.dp90
    }

    /**
     * Extra height of the shortcut grid beyond its first row, scrolled away on opening. 0 since 2026-11-20: the spacer
     * already leaves room for all rows ([extraRowsHeight]), so nothing has to be scrolled.
     */
    fun headerSize(): Int {
        return 0
    }

    fun allChecked(): Boolean {
        val arrayList = this.items
        if (arrayList != null && !arrayList.isEmpty()) {
            val size = arrayList.size
            var checked = 0
            var index = 0
            while (index < size) {
                val rawQuickItem: Any? = arrayList.get(index)
                index++
                val quickItem = rawQuickItem as QuickItem?
                if (quickItem != null && quickItem.itemKind.toInt() == 0 && quickItem.isChecked) {
                    checked++
                }
            }
            val itemCount = (arrayList.size - (if (this.hasHeader) 1 else 0)) - this.footerCount
            if (itemCount > 0 && checked >= itemCount) {
                return true
            }
        }
        return false
    }

    fun showsQuick(): Boolean {
        val listMode = this.listMode
        if (listMode.toInt() == 2 || listMode.toInt() == 3) {
            return ZtriSettings.qckSchType.value == 1
        }
        return ZtriSettings.quickUse.value
    }

    fun release() {
        this.frozenCount = getItemCount()
        iconJobs.clear()
        iconScope.coroutineContext[kotlinx.coroutines.Job]?.cancelChildren()
        val mainListLoader = this.listLoader
        if (mainListLoader != null) {
            mainListLoader.release()
            this.listLoader = null
        }
        this.appContext = null
        this.itemListener = null
        this.tabListener = null
        this.items = null
        this.newsItems = null
        clearHandler(this.mainHandler)
        this.mainHandler = null
        this.userAgent = null
        this.executor = null
        this.recentItems = null
        this.recentListener = null
    }

    fun moveItem(fromOrder: Int, toOrder: Int): Boolean {
        var footerStart: Int = 0
        var size: Int = 0
        val arrayList = this.items
        if (arrayList == null || fromOrder < ((if (this.hasHeader) 1 else 0).also {
                footerStart = it
            }) || toOrder < footerStart || fromOrder >= ((arrayList.size - this.footerCount).also {
                size = it
            }) || toOrder >= size || this.orderBusy) {
            return false
        }
        this.orderBusy = true
        val items = this.items
        if (items != null) {
            val quickItemD = itemAt(fromOrder)
            if (quickItemD != null) {
                quickItemD.order = toOrder
                quickItemD.changed = true
            }
            val target = itemAt(toOrder)
            if (target != null) {
                target.order = fromOrder
                target.changed = true
            }
            val quickItem = items.removeAt(fromOrder)
            if (quickItem != null) {
                items.add(toOrder, quickItem)
            }
        }
        notifyItemMoved(fromOrder, toOrder)
        this.orderBusy = false
        return true
    }

    fun runAsync(runnable: Runnable?) {
        var executorServiceH = this.executor
        if (executorServiceH == null || executorServiceH.isShutdown() || executorServiceH.isTerminated()) {
            executorServiceH = MainApp.executor(this.appContext)
            if (executorServiceH == null) {
                return
            } else {
                this.executor = executorServiceH
            }
        }
        try {
            executorServiceH.execute(runnable)
        } catch (unused: Exception) {
        }
    }

    fun saveOrder(view: View?): Boolean {
        val arrayList: ArrayList<QuickAdapter.QuickItem?>? = if (this.appContext == null) null else this.items
        if (this.appContext == null || arrayList == null || arrayList.isEmpty() || this.orderBusy) {
            return false
        }
        this.orderBusy = true
        val size = arrayList.size
        var found = false
        var order = 0
        var index = 0
        while (index < size) {
            val rawQuickItem: Any? = arrayList.get(index)
            index++
            val quickItem = rawQuickItem as QuickItem?
            if (quickItem != null && quickItem.itemKind.toInt() == 0) {
                if ((quickItem.changed || quickItem.order != order) && !TextUtils.isEmpty(quickItem.path)) {
                    quickItem.order = order
                    val contentValues = ContentValues()
                    contentValues.put("_order", quickItem.order)
                    updateWhere(
                        get(this.appContext).getWritableDatabase(),
                        "DbBookQuick_table",
                        contentValues,
                        "_secret=? AND _path=?",
                        arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey(), quickItem.path)
                    )
                    found = true
                }
                quickItem.changed = false
                order++
            }
        }
        if (found && view != null) {
            view.post(NotifyDataSetChangedTask())
        }
        this.orderBusy = false
        return found
    }

    fun setAllChecked(isChecked: Boolean, editMode: Boolean) {
        val arrayList = this.items
        if (arrayList == null || arrayList.isEmpty()) {
            return
        }
        val size = arrayList.size
        var index = 0
        while (index < size) {
            val rawQuickItem: Any? = arrayList.get(index)
            index++
            val quickItem = rawQuickItem as QuickItem?
            if (quickItem != null && quickItem.itemKind.toInt() == 0) {
                quickItem.isChecked = isChecked
            }
        }
        val rows = this.host
        if (rows != null && rows.firstVisible() >= 0) {
            for (iT in rows.firstVisible()..rows.lastVisible()) {
                applyEditAlpha(iT, true)
            }
        }
    }

    fun updateHeaderFooter() {
        val listMode = this.listMode
        if (listMode.toInt() == 1) {
            this.hasHeader = false
            this.footerCount = 0.toByte()
            return
        }
        if (listMode.toInt() == 2) {
            if (this.recentMode) {
                this.hasHeader = false
                this.footerCount = 0.toByte()
                return
            } else {
                this.hasHeader = false
                this.footerCount = 1.toByte()
                return
            }
        }
        if (!showsQuick()) {
            this.hasHeader = true
            this.footerCount = 0.toByte()
        } else if (this.darkState && newsEnabled()) {
            this.hasHeader = true
            this.footerCount = 1.toByte()
        } else {
            this.hasHeader = true
            this.footerCount = 2.toByte()
        }
    }

    fun applyEditMode(pos: Int, editMode: Boolean) {
        if (this.editMode != editMode) {
            this.editMode = editMode
            if (editMode) {
                val quickItemD = itemAt(pos)
                if (quickItemD != null && quickItemD.itemKind.toInt() == 0) {
                    quickItemD.isChecked = true
                }
                val rows = this.host
                if (rows != null && rows.firstVisible() >= 0) {
                    for (iT in rows.firstVisible()..rows.lastVisible()) {
                        applyEditAlpha(iT, false)
                    }
                }
                return
            }
            val arrayList = this.items
            if (arrayList != null && !arrayList.isEmpty()) {
                val size = arrayList.size
                var index = 0
                while (index < size) {
                    val rawQuickItem: Any? = arrayList.get(index)
                    index++
                    val quickItem = rawQuickItem as QuickItem?
                    if (quickItem != null && quickItem.itemKind.toInt() == 0) {
                        quickItem.isChecked = false
                    }
                }
            }
            notifyDataSetChanged()
        }
    }

    fun checkNewsHead(list: MutableList<QuickItem?>?) {
        if (list == null || list.size != 3) {
            this.newsHeadOnly = false
            this.newsDragPos = -1
            return
        }
        val it: MutableIterator<Any?> = list.iterator()
        while (it.hasNext()) {
            val quickItem = it.next() as QuickItem?
            if (quickItem != null && quickItem.itemKind.toInt() == 6) {
                this.newsHeadOnly = true
                return
            }
        }
        this.newsHeadOnly = false
        this.newsDragPos = -1
    }

    /** Input of the last [updateHeaderHeight] and the rows height it reserved, so [syncRows] can redo it. */
    private var lastAvail = -1
    private var lastDown = false
    private var reservedRows = -1

    /**
     * The spacer was sized before the shortcuts were loaded (no rows to reserve yet); redo it when the number of rows has
     * changed since (2026-11-20). Called when the spacer's view type is asked for, i.e. on every list refresh.
     */
    private fun syncRows() {
        if (lastAvail < 0 || !lastDown) return
        if (extraRowsHeight() != reservedRows) updateHeaderHeight(lastAvail, lastDown)
    }

    fun updateHeaderHeight(headerHeight: Int, quickDown: Boolean) {
        lastAvail = headerHeight
        lastDown = quickDown
        reservedRows = extraRowsHeight()
        val listMode = this.listMode
        if (listMode.toInt() == 1) {
            this.headerHeight = 1
            return
        }
        if (listMode.toInt() == 2) {
            this.headerHeight = 1
            return
        }
        if (!this.darkState) {
            if (!quickDown) {
                this.headerHeight = MainApp.dp16
                return
            }
            val newHeaderHeight = (headerHeight - MainApp.dp90) - MainApp.dp48 - extraRowsHeight()
            this.headerHeight = newHeaderHeight
            if (newHeaderHeight < 1) {
                this.headerHeight = 1
                return
            }
            return
        }
        if (!quickDown) {
            // shortcuts at the top: the spacer holds just the home strip, above them
            val ctx = this.appContext
            this.headerHeight = if (ctx != null && homeStripWanted()) Math.round(QuickHomeHeader.STRIP_DP * ctx.resources.displayMetrics.density) else 1
            return
        }
        if (showsQuick()) {
            val newHeaderHeight = headerHeight - MainApp.dp90 - extraRowsHeight()
            this.headerHeight = newHeaderHeight
            if (newHeaderHeight < 1) {
                this.headerHeight = 1
                return
            }
            return
        }
        if (!newsEnabled()) {
            this.headerHeight = 1
            return
        }
        this.headerHeight = headerHeight
        if (headerHeight < 1) {
            this.headerHeight = 1
        }
    }

    fun updatePlusItem() {
        val arrayList: ArrayList<QuickAdapter.QuickItem?>? = if (!showsQuick() || this.listMode.toInt() == 1) null else this.items
        val quickItemD: QuickItem? = if (!showsQuick() || this.listMode.toInt() == 1 || arrayList == null || arrayList.size == 0) null else itemAt(this.items!!.size - this.footerCount)
        if (!showsQuick() || this.listMode.toInt() == 1 || arrayList == null || arrayList.size == 0 || quickItemD == null) {
            return
        }
        val quickPlus = ZtriSettings.quickPlus.value
        val itemKind = quickItemD.itemKind
        if (quickPlus) {
            if (itemKind.toInt() == 2) {
                quickItemD.itemKind = 1.toByte()
            }
        } else if (itemKind.toInt() == 1) {
            quickItemD.itemKind = 2.toByte()
        }
    }

    fun loadThumb(viewItem: ViewItem?, myRoundImage: MyRoundImage?) {
        if (this.appContext == null) {
            return
        }
        com.mycompany.app.glide.ThumbLoader.loadFor(viewItem, myRoundImage, FindByPathStep())
    }

    fun applyThumb(myRoundImage: MyRoundImage?, bitmap: Bitmap?) {
        val res: Int
        if (myRoundImage == null) {
            return
        }
        if (MainUtil.isBitmapOk(bitmap)) {
            if (bitmap!!.getWidth() > (bitmap.getHeight() shl 1)) {
                myRoundImage.setScaleType(ScaleType.FIT_CENTER)
            } else {
                myRoundImage.setScaleType(ScaleType.CENTER_CROP)
            }
            myRoundImage.setImageBitmap(bitmap)
            return
        }
        if (QuickView.Companion.isCustomNewsUrl()) {
            res =
                if (MainUtil.isDarkPage(this.darkState)) R.drawable.outline_newspaper_dark_24 else R.drawable.outline_newspaper_black_24
        } else {
            res = R.drawable.google_news
        }
        myRoundImage.setScaleType(ScaleType.CENTER_INSIDE)
        myRoundImage.setImageResource(res)
    }

    fun applyList(list: ArrayList<QuickItem?>?, flat: Boolean) {
        val newsItems: MutableList<QuickItem?>?
        this.recentMode = false
        val mainListLoader = this.listLoader
        if (mainListLoader != null) {
            mainListLoader.queue = null
        }
        updateHeaderFooter()
        this.recentItems = null
        this.items = list
        if (flat && this.darkState && newsEnabled()) {
            if (ZtwoSettings.newsUse.value) {
                newsItems = DataNews.instance(this.appContext).items
            } else {
                val items: ArrayList<QuickItem?> = ArrayList()
                val quickItem = QuickItem()
                quickItem.itemKind = 4.toByte()
                quickItem.newsIndex = 0
                items.add(quickItem)
                newsItems = items
            }
            applyNewsList(newsItems, false)
        }
        notifyDataSetChanged()
    }

    /** Shows a news item's image, looking up (and the first time fetching) the article's lead image.  */
    fun loadNewsImage(item: QuickItem?, view: MyRoundImage?, position: Int) {
        if (view == null || this.appContext == null) {
            return
        }
        if (TextUtils.isEmpty(item!!.imageUrl)) {
            val news = DataNews.instance(this.appContext)
            var cached: QuickItem? = null
            if (item.newsIndex >= 0 && news.items != null && item.newsIndex < news.items!!.size) {
                cached = news.items!!.get(item.newsIndex)
            }
            item.imageUrl = if (cached != null) cached.imageUrl else null
            if (TextUtils.isEmpty(item.imageUrl)) {
                applyThumb(view, null)
                if (TextUtils.isEmpty(item.path) || item.path!!.startsWith("https://news.google.com/rss/articles/") || item.isChecked) {
                    return
                }
                item.isChecked = true
                runAsync(FetchDocumentTask(this, item.path, position))
                return
            }
        }
        val bitmap = com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(2, item.imageUrl))
        if (MainUtil.isBitmapOk(bitmap)) {
            applyThumb(view, bitmap)
            return
        }
        applyThumb(view, null)
        val request = ViewItem()
        request.itemType = 7
        request.url = item.imageUrl
        request.position = position
        request.cacheType = 2
        request.iconUrl = item.path
        loadThumb(request, view)
    }

    fun applyNewsList(list: MutableList<QuickItem?>?, refresh: Boolean) {
        var refresh = refresh
        if (this.appContext == null) {
            return
        }
        this.newsItems = list
        checkNewsHead(list)
        if (list != null && !list.isEmpty() && this.mainHandler == null) {
            this.mainHandler = Handler(Looper.getMainLooper())
        }
        val arrayList = this.items
        if (arrayList != null && !arrayList.isEmpty()) {
            val items = this.items
            val quickItem = items!!.get(items.size - 1)
            if (quickItem != null) {
                val newsOn: Boolean = newsEnabled()
                val itemKind = quickItem.itemKind
                if (newsOn) {
                    if (itemKind.toInt() == 8) {
                        val itemsNow = this.items
                        itemsNow!!.removeAt(itemsNow.size - 1)
                        refresh = true
                    }
                } else if (itemKind.toInt() != 8) {
                    val moreItem = QuickItem()
                    moreItem.itemKind = 8.toByte()
                    this.items!!.add(moreItem)
                    refresh = true
                }
            }
        }
        if (refresh) {
            refreshNewsRange()
        }
        checkNewsChange(this.newsItems)
    }

    /** Fills the loading notice (type 6): message, colours, and the bar's progress / visibility. */
    fun styleNotice(holder: QuickHolder?) {
        val card = holder?.notice ?: return
        val bar = card.bar ?: return
        card.message = card.context.getString(if (this.newsLoading) R.string.news_loading else R.string.no_news)
        if (this.rssPage) {
            card.message = if (rssNotice != 0) card.context.getString(rssNotice) else ""
            card.cardColor = 0
            card.textColor = if (AppState.darkUi || (MainUtil.isDarkBars(this.darkState) && BrowserSettings.darkHome.value)) -328966 else -16777216
            card.barShown = false
            return
        }
        val dark: Boolean
        if (MainUtil.isDarkBars(this.darkState)) {
            dark = AppState.darkUi || BrowserSettings.darkHome.value
            card.cardColor = if (dark) -16777216 else -1
        } else {
            dark = AppState.darkUi
            card.cardColor = 0
        }
        card.textColor = if (dark) -328966 else -16777216
        if (dark) bar.applyColors(-922746881, -14606047) else bar.applyColors(-13022805, -460552)
        if (QuickView.Companion.isCustomNewsUrl() || !this.newsLoading) {
            card.barShown = false
            return
        }
        bar.progress = (this.itemListener?.newsListType() ?: 0).toFloat()
        card.barShown = true
    }

    fun checkNewsChange(list: MutableList<QuickItem?>?) {
        val quickListener = this.itemListener
        if (quickListener != null) {
            if (quickListener.hasTransNews()) {
                this.newsPending = true
                return
            }
            this.newsPending = false
            if (!ZtwoSettings.newsTitle.value || this.newsHeadOnly) {
                return
            }
            if ((!ZtwoSettings.newsUse.value && ZtwoSettings.newsAllow.value) || list == null || list.isEmpty()) {
                return
            }
            if (MainUtil.sameObject(DataNews.instance(this.appContext).newsPick, ZtwoSettings.newsPick.value)) {
                val it: MutableIterator<Any?> = list.iterator()
                while (it.hasNext()) {
                    val quickItem = it.next() as QuickItem?
                    if (quickItem != null && quickItem.itemKind.toInt() == 7) {
                        if (quickItem.transState.toInt() != 0) {
                            return
                        }
                    }
                }
                return
            }
            val iterator: MutableIterator<Any?> = list.iterator()
            while (iterator.hasNext()) {
                val quickItem = iterator.next() as QuickItem?
                if (quickItem != null) {
                    quickItem.transTitle = null
                    quickItem.sourceName = null
                    quickItem.transState = 0.toByte()
                }
            }
            val itemListener = this.itemListener
            if (itemListener != null) {
                itemListener.translateNews(list)
            }
        }
    }

    override fun getItemCount(): Int {
        var list: MutableList<QuickItem?>? = null
        val listMode: Byte
        val frozenCount = this.frozenCount
        if (frozenCount != 0) {
            return frozenCount
        }
        if (this.recentMode) {
            val recentItems = this.recentItems
            if (recentItems == null) {
                return 0
            }
            return recentItems.size
        }
        if (this.newsPage) return pageRows()?.size ?: 0
        val arrayList = this.items
        var size = if (arrayList != null) arrayList.size else 0
        if (SyncSettings.secretMode.value && showsQuick() && ((this.listMode.also { listMode = it }).toInt() == 0 || listMode.toInt() == 3)) {
            if (size == (if (this.hasHeader) 1 else 0) + this.footerCount) {
                size++
            }
        }
        if (this.darkState && this.listMode.toInt() == 0) return size
        return if (this.darkState && newsEnabled() && (this.newsItems.also {
                list = it
            }) != null) list!!.size + size else size
    }

    fun updateItem(oldPath: String?, path: String?, label: String?, color: Int, arrayList: ArrayList<QuickSubItem?>?) {
        val items: ArrayList<QuickAdapter.QuickItem?>? = if (TextUtils.isEmpty(oldPath) || TextUtils.isEmpty(path)) null else this.items
        if (TextUtils.isEmpty(oldPath) || TextUtils.isEmpty(path) || items == null || items.isEmpty()) {
            return
        }
        val size = items.size
        var index = 0
        while (index < size) {
            val rawQuickItem: Any? = items.get(index)
            index++
            val quickItem = rawQuickItem as QuickItem
            if (oldPath == quickItem.path) {
                val patternCompile = Pattern.compile("\\p{Punct}")
                quickItem.path = path
                quickItem.label = label
                quickItem.title = MainUtil.stripPunctuation(label, patternCompile)
                quickItem.color = color
                quickItem.subItems = arrayList
                break
            }
        }
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        if (this.recentMode) {
            return 0
        }
        val quickItemD = itemAt(position)
        if (quickItemD == null) {
            return if (PdfSettings.quickSmall.value) 1 else 0
        }
        val itemKind = quickItemD.itemKind
        if (itemKind.toInt() == 9) {
            syncRows()
            return this.headerHeight + 9
        }
        if (itemKind.toInt() == 2) {
            return 2
        }
        if (itemKind.toInt() == 3) {
            return 3
        }
        if (itemKind.toInt() == 4) {
            return 4
        }
        if (itemKind.toInt() == 5) {
            return 5
        }
        if (itemKind.toInt() == 6) {
            return 6
        }
        if (itemKind.toInt() == 7) {
            return 7
        }
        if (itemKind.toInt() == 8) {
            return 8
        }
        return if (PdfSettings.quickSmall.value) 1 else 0
    }

    fun refreshNewsRange() {
        try {
            val count = itemCount
            val list = this.newsItems
            var added = 0
            val size = if (list == null) 0 else list.size
            val overflow = count - size
            var index = overflow - 1
            if (index <= 0) {
                notifyItemRangeChanged(0, count)
                return
            }
            while (index >= 0) {
                val quickItemD = itemAt(index)
                if (quickItemD != null) {
                    if (quickItemD.itemKind.toInt() == 7) {
                        break
                    } else {
                        added++
                    }
                }
                index--
            }
            notifyItemRangeChanged(overflow - added, size + added)
        } catch (unused: Exception) {
            notifyDataSetChanged()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var manyItems = false
        val res: Int
        val quickHolder = holder as QuickHolder
        val view = quickHolder.itemView
        if (this.recentMode) {
            val searchItemG = recentAt(position)
            val row = quickHolder.searchRow
            if (searchItemG != null && row != null) {
                view.setTag(quickHolder)
                view.setOnClickListener(RecentAtClick())
                view.setOnLongClickListener(RecentAtLongClick())
                val dark = MainUtil.isDarkPage(this.darkState)
                view.setBackgroundResource(if (dark) R.drawable.selector_normal_dark else R.drawable.selector_normal)
                row.text = searchItemG.title ?: ""
                row.textColor = if (dark) -4079167 else -16777216
                row.pressColor = if (dark) -12632257 else -2039584
                row.historyIcon = if (dark) R.drawable.outline_history_dark_24 else R.drawable.outline_history_black_24
                row.deleteIcon = if (dark) R.drawable.outline_close_dark_18 else R.drawable.outline_close_black_18
                // the arrow points down-left when the panel sits at the bottom
                row.fillIcon = when {
                    this.reversed -> if (dark) R.drawable.outline_south_west_dark_20 else R.drawable.outline_south_west_black_20
                    else -> if (dark) R.drawable.outline_north_west_dark_20 else R.drawable.outline_north_west_black_20
                }
                row.onDelete = { DeleteRecentClick().onClick(it) }
                row.onFill = { RecentPathClick().onClick(it) }
            }
            return
        }
        val quickItemD = itemAt(position)
        if (quickItemD != null) {
            quickItemD.quickHolder = quickHolder
            val tileState = quickItemD.itemKind
            if (tileState.toInt() == 1) {
                view.setAlpha(if (this.editMode) 0.4f else 1.0f)
            } else {
                view.setAlpha(1.0f)
            }
            val itemKindNow = quickItemD.itemKind
            if (itemKindNow.toInt() == 2) {
                view.setTag(null)
                return
            }
            if (itemKindNow.toInt() == 8) {
                if (this.darkState && newsEnabled()) {
                    val list = this.newsItems
                    manyItems = (if (list == null) 0 else list.size) > 5 && (view is ImageView)
                }
                view.setTag(null)
                if (!manyItems) {
                    if (view is ImageView) {
                        view.setImageDrawable(null)
                    }
                    view.setBackground(null)
                    return
                }
                view.setOnClickListener(ChangedClick())
                if (MainUtil.isDarkBars(this.darkState)) {
                    if (view is ImageView) {
                        view.setImageResource(R.drawable.outline_list_footer_dark_24)
                    }
                    res = R.drawable.selector_round_gray
                } else if (AppState.darkUi) {
                    if (view is ImageView) {
                        view.setImageResource(R.drawable.outline_list_footer_dark_24)
                    }
                    res = R.drawable.selector_round_dark
                } else {
                    if (view is ImageView) {
                        view.setImageResource(R.drawable.outline_list_footer_black_24)
                    }
                    res = R.drawable.selector_round
                }
                view.setBackgroundResource(res)
                return
            }
            val listMode = this.listMode
            if (itemKindNow.toInt() == 9) {
                view.setTag(null)
                if (view is QuickHomeHeader) {
                    bindHomeHeader(view)
                    return
                }
                if (listMode.toInt() == 0 && (view is ImageView)) {
                    if (!ZtwoSettings.quickDown.value || isDarkMode() || this.headerHeight <= (MainApp.dp90 shl 1)) {
                        view.setImageDrawable(null)
                        return
                    } else {
                        view.setImageResource(R.drawable.ic_logo_gray_24)
                        return
                    }
                }
                return
            }
            view.setTag(quickHolder)
            val itemKind = quickItemD.itemKind
            if (itemKind.toInt() == 4 || itemKind.toInt() == 5) {
                bindNotice(quickHolder, itemKind.toInt())
                return
            }
            if (itemKind.toInt() == 6) {
                this.newsDragPos = position
                styleNotice(quickHolder)
                return
            }
            if (itemKind.toInt() == 7) {
                bindNews(quickHolder, quickItemD, position)
                return
            }
            if (itemKind.toInt() == 3) {
                bindNotice(quickHolder, 3)
                return
            }
            view.setOnClickListener(ItemClick())
            view.setOnLongClickListener(ItemLongClick())
            bindTile(quickHolder, quickItemD, position)
        }
    }

    /** Binds a shortcut tile (types 0 / 1) at [pos]: background, disc, label, selection check and icon. */
    private fun bindTile(holder: QuickHolder, item: QuickItem, pos: Int) {
        val tile = holder.tile ?: return
        val view = holder.itemView
        val res = when {
            this.listMode.toInt() == 0 && MainUtil.isDarkBars(this.darkState) -> R.drawable.selector_round_gray
            MainUtil.isDarkPage(this.darkState) -> R.drawable.selector_round_dark
            else -> R.drawable.selector_round
        }
        view.setBackgroundResource(res)
        if (holder.tileMode == 1) tile.discColor = backColor()
        val back = if (holder.tileMode != 1) backColor() else 0
        if (item.itemKind.toInt() == 1) {
            // the "add" tile
            tile.iconCircle(back, if (MainUtil.isDarkPage(this.darkState)) R.drawable.outline_add_dark_24 else R.drawable.outline_add_black_24)
            tile.labelShown = false
            tile.checkShown = false
            return
        }
        val darkPage = AppState.darkUi || BrowserSettings.darkHome.value
        val (labelColor, darkCheck) = when {
            !MainUtil.isDarkBars(this.darkState) -> if (AppState.darkUi) Pair(-328966, true) else Pair(-16777216, false)
            this.listMode.toInt() == 0 -> if (darkPage) Pair(-1, true) else Pair(-16777216, false)
            else -> if (darkPage) Pair(-328966, true) else Pair(-16777216, false)
        }
        tile.labelColor = labelColor
        if (darkCheck) {
            tile.setCheckColors(-16777216, R.drawable.baseline_check_circle_dark_24, R.drawable.outline_radio_button_unchecked_dark_24)
        } else {
            tile.setCheckColors(-1, R.drawable.baseline_check_circle_black_24, R.drawable.outline_radio_button_unchecked_black_24)
        }
        tile.label = if (TextUtils.isEmpty(item.label)) view.context.getString(R.string.group_title) else item.label!!
        tile.labelShown = true
        tile.checkShown = this.editMode
        if (this.editMode) tile.setChecked(item.isChecked, false)
        if (this.appContext == null) return
        val folder: ArrayList<QuickAdapter.QuickSubItem?>? = if (item.isFolder) item.subItems else null
        if (folder != null && folder.isNotEmpty()) {
            tile.iconFolder(pos, folder, SyncSettings.secretMode.value, back)
            return
        }
        val color = item.color
        val colored = color != 0 && color != -460552
        if (item.isFolder) {
            tile.iconImage(DbBookQuick.iconRes(if (colored) color else -65536))
            return
        }
        // the letter on the shortcut's colour, unless a favicon is known or found (QuickIcons)
        if (TextUtils.isEmpty(item.path) || item.letterOnly) {
            letterTile(tile, item, back)
            return
        }
        val secret = SyncSettings.secretMode.value
        val cached = QuickIcons.cached(item.path, secret)
        if (cached != null) {
            tile.iconBitmap(cached, back)
            return
        }
        letterTile(tile, item, back)
        val path = item.path
        val context = this.appContext
        val job = iconJobs.remove(holder)
        job?.cancel()
        iconJobs[holder] = iconScope.launch {
            val bitmap = kotlinx.coroutines.withContext(QuickIcons.loads) { QuickIcons.load(context, path, secret) }
            iconJobs.remove(holder)
            if (bitmap == null || this@QuickAdapter.appContext == null) return@launch
            // the row may show another shortcut by now
            if (holder.pos != pos || itemAt(pos)?.path != path) return@launch
            holder.tile?.iconBitmap(bitmap, if (holder.tileMode != 1) backColor() else 0)
        }
    }

    /** Favicon loads of the shortcut tiles now bound (one per holder; a re-bind cancels the old one). */
    private val iconScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main.immediate + kotlinx.coroutines.SupervisorJob())
    private val iconJobs = HashMap<QuickHolder, kotlinx.coroutines.Job>()

    /** The shortcut's letter on its own colour (or on [back] when it has none). */
    private fun letterTile(tile: QuickTileView, item: QuickItem, back: Int) {
        val color = item.color
        tile.iconLetter(item.title, this.darkState, if (color != 0 && color != -460552) color else back)
    }

    /** Binds a news article (type 7): texts, thumbnail and source icon (loaded as before), colours. */
    private fun bindNews(holder: QuickHolder, item: QuickItem, pos: Int) {
        val card = holder.news ?: return
        card.onOpen = { CardOpenClick().onClick(it) }
        card.onLongPress = { CardLongClick().onLongClick(it) }
        card.onMore = { CardMoreClick().onClick(it) }
        val darkPage = MainUtil.isDarkPage(this.darkState)
        card.sourceIcon.setTag(holder)
        card.thumb.setTag(holder)
        // the translated title / source when the user reads news translated
        var title = item.label
        var source = item.summary
        val translated = ZtwoSettings.newsTitle.value && item.transState.toInt() == 2 && !TextUtils.isEmpty(item.transTitle)
        if (translated) {
            title = item.transTitle
            if (!TextUtils.isEmpty(item.sourceName)) source = item.sourceName
        }
        card.title = title ?: ""
        card.source = "$source · ${newsAge(item.sourceHtml)}"
        card.paper = this.newsPage
        card.sourceName = source ?: ""
        card.age = newsAge(item.sourceHtml)
        card.badge = if (!translated) 0 else if (darkPage) R.drawable.trans_logo_short_trans_white else R.drawable.trans_logo_short_trans_color
        val icon = card.sourceIcon
        if (this.appContext != null) {
            val fallback = if (darkPage) R.drawable.outline_public_dark_web_24 else R.drawable.outline_public_black_24
            if (TextUtils.isEmpty(item.thumbUrl)) {
                icon.setImageResource(fallback)
            } else {
                val cached = com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(2, item.thumbUrl))
                if (MainUtil.isBitmapOk(cached)) {
                    icon.setImageBitmap(cached)
                } else {
                    icon.setImageResource(fallback)
                    val request = ViewItem()
                    request.itemType = 7.toByte()
                    request.url = item.thumbUrl
                    request.position = pos
                    request.cacheType = 2
                    com.mycompany.app.glide.ThumbLoader.loadFor(request, icon, ThumbListener())
                }
            }
        }
        loadNewsImage(item, card.thumb, pos)
        val webStyle = MainUtil.isDarkBars(this.darkState)
        val dark = if (webStyle) AppState.darkUi || BrowserSettings.darkHome.value else AppState.darkUi
        card.cardColor = if (!webStyle) 0 else if (dark) -16777216 else -1
        card.joinsAbove = webStyle && item.newsIndex == 1
        card.textColor = if (dark) -328966 else -16777216
        card.moreIcon = if (dark) R.drawable.outline_more_vert_dark_20 else R.drawable.outline_more_vert_black_20
        card.pressColor = if (dark) -12632257 else -2039584
        card.dark = dark
    }

    /** Binds a notice row: 3 import button, 4 news-start card, 5 news header (colours follow the page style). */
    private fun bindNotice(holder: QuickHolder, type: Int) {
        val card = holder.notice ?: return
        // "web style" pages use the page's own black / white; otherwise the app theme
        val webStyle = MainUtil.isDarkBars(this.darkState)
        val dark = if (webStyle) AppState.darkUi || BrowserSettings.darkHome.value else AppState.darkUi
        card.topPad = if (type == 3) 0 else bottomPad()
        if (type == 5) {
            // on the news page: the masthead (the grid already starts below the ⋮ button's row)
            card.topPad = if (this.newsPage) 0 else bottomPad()
            card.pageMode = this.newsPage
            card.dark = if (webStyle0()) AppState.darkUi || BrowserSettings.darkHome.value else AppState.darkUi
            card.dateText = android.text.format.DateUtils.formatDateTime(
                card.context, System.currentTimeMillis(),
                android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY or android.text.format.DateUtils.FORMAT_SHOW_DATE or android.text.format.DateUtils.FORMAT_SHOW_YEAR,
            )
            card.onBack = { homeListener?.closeNews() }
            card.mastTitle = card.context.getString(R.string.news_title)
            card.tabs = this.tabLabels
            card.selectedTab = this.tabIndex
            card.onTab = { homeListener?.selectTab(it) }
            card.onAddFeed = { homeListener?.addFeed() }
            val height = if (this.newsPage) dpToPx(card.context, 164.0f).toInt() else dpToPx(card.context, 144.0f).toInt()
            val lp = card.layoutParams
            if (lp != null && lp.height != height) { lp.height = height; card.layoutParams = lp }
        }
        card.pressColor = if (dark) -12632257 else -2039584
        when (type) {
            3 -> {
                card.textColor = if (dark) -328966 else -16777216
                card.buttonColor = backColor()
                card.pressColor = if (dark) -12632257 else 553648128
                card.onMain = { this.itemListener?.onItemClick(null, positionOf(holder.itemView)) }
            }
            4 -> {
                card.cardColor = when {
                    dark -> -14606047
                    webStyle -> -1
                    else -> QuickNoticeCard.OUTLINE_GREY
                }
                card.textColor = if (dark) -328966 else -16777216
                card.closeIcon = if (dark) R.drawable.outline_close_dark_24 else R.drawable.outline_close_black_24
                card.onClose = { this.itemListener?.onEditMode(false) }
                card.onMain = { this.itemListener?.onEditMode(true) }
            }
            else -> {
                card.cardColor = when {
                    !webStyle -> 0
                    dark -> -16777216
                    else -> -1
                }
                card.textColor = if (dark) -5197648 else -16777216
                // the old "sources" button did nothing (its action was lost): not shown
                card.sourcesIcon = 0
                card.settingsIcon = if (this.newsPage) (if (dark) R.drawable.outline_settings_dark_24 else R.drawable.outline_settings_black_24)
                    else (if (dark) R.drawable.outline_settings_dark_20 else R.drawable.outline_settings_black_20)
                card.onSources = { this.itemListener?.openNewsSources() }
                card.onSettings = { this.itemListener?.openNewsSettings() }
            }
        }
    }

    /** No favicon: a red folder image for folders, otherwise the shortcut's letter on [back]. */
    private fun tileFallback(tile: QuickTileView, item: QuickItem, back: Int) {
        if (item.isFolder) tile.iconImage(DbBookQuick.iconRes(-65536)) else tile.iconLetter(item.title, this.darkState, back)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.getContext()
        if (context == null) {
            throw IllegalStateException("no view holder for view type")
        }
        if (this.recentMode) {
            // a recent search (search panel)
            val row = QuickSearchRow(context)
            row.setMinimumHeight(MainApp.dp56)
            row.setLayoutParams(ViewGroup.LayoutParams(-1, -2))
            val holder = QuickHolder(row)
            holder.tileMode = viewType
            holder.searchRow = row
            return holder
        }
        if (viewType >= 9 && this.darkState && this.listMode.toInt() == 0) {
            // the start page's spacer with the home strip
            val header = QuickHomeHeader(context)
            header.setLayoutParams(ViewGroup.LayoutParams(-1, this.headerHeight))
            val holder = QuickHolder(header)
            holder.tileMode = viewType
            holder.homeHeader = header
            return holder
        }
        if (viewType >= 9) {
            val imageView = ImageView(context)
            imageView.setScaleType(ScaleType.CENTER_INSIDE)
            imageView.setLayoutParams(ViewGroup.LayoutParams(-1, this.headerHeight))
            val quickHolder = QuickHolder(imageView)
            quickHolder.tileMode = viewType
            return quickHolder
        }
        if (viewType == 2) {
            val view = View(context)
            view.setLayoutParams(ViewGroup.LayoutParams(-1, 1))
            val quickHolder = QuickHolder(view)
            quickHolder.tileMode = viewType
            return quickHolder
        }
        if (viewType == 3 || viewType == 4 || viewType == 5) {
            // notice rows: import button, news-start card, news header
            val card = QuickNoticeCard(context, viewType)
            val height = when (viewType) {
                3 -> MainApp.dp90
                4 -> dpToPx(context, 280.0f).toInt()
                else -> dpToPx(context, 144.0f).toInt()
            }
            card.setLayoutParams(ViewGroup.LayoutParams(-1, height))
            val holder = QuickHolder(card)
            holder.tileMode = viewType
            holder.notice = card
            return holder
        }
        if (viewType == 6) {
            // the news loading notice; its bar is driven from QuickView through holder.z
            val card = QuickNoticeCard(context, 6)
            card.setLayoutParams(ViewGroup.LayoutParams(-1, dpToPx(context, 168.0f).toInt()))
            val holder = QuickHolder(card)
            holder.tileMode = viewType
            holder.notice = card
            holder.bar = card.bar
            return holder
        }
        if (viewType != 7) {
            if (viewType == 8) {
                val imageView = ImageView(context)
                imageView.setScaleType(ScaleType.CENTER_INSIDE)
                imageView.setLayoutParams(ViewGroup.LayoutParams(-1, MainApp.dp90))
                val quickHolder = QuickHolder(imageView)
                quickHolder.tileMode = viewType
                return quickHolder
            }
            // shortcut tiles: type 0 normal, type 1 compact (small icon on a disc)
            val tile = QuickTileView(context, viewType == 1)
            tile.setLayoutParams(ViewGroup.LayoutParams(-1, MainApp.dp90))
            val tileHolder = QuickHolder(tile)
            tileHolder.tileMode = viewType
            tileHolder.tile = tile
            return tileHolder
        }
        // a news article
        val card = QuickNewsCard(context)
        card.setLayoutParams(ViewGroup.LayoutParams(-1, -2))
        val holder = QuickHolder(card)
        holder.tileMode = viewType
        holder.news = card
        holder.thumb = card.thumb
        holder.sourceIcon = card.sourceIcon
        return holder
    }

    fun insertItem(path: String?, color: Int, order: Int, label: String?) {
        val arrayList: ArrayList<QuickAdapter.QuickItem?>? = if (TextUtils.isEmpty(path) || TextUtils.isEmpty(label)) null else this.items
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(label) || arrayList == null || arrayList.size == 0 || order < 0 || order > (this.items!!.size - (if (this.hasHeader) 1 else 0)) - this.footerCount) {
            return
        }
        val quickItem = QuickItem()
        quickItem.path = path
        quickItem.label = label
        quickItem.title = MainUtil.stripPunctuation(label, null)
        quickItem.color = color
        quickItem.order = order
        val index = order + (if (this.hasHeader) 1 else 0)
        if (index <= this.items!!.size - this.footerCount) {
            this.items!!.add(index, quickItem)
        } else if (this.listMode.toInt() != 1) {
            return
        } else {
            this.items!!.add(quickItem)
        }
        notifyDataSetChanged()
    }

    fun deleteChecked(folderPath: String?): Boolean {
        val arrayList: ArrayList<QuickAdapter.QuickItem?>? = if (this.appContext == null) null else this.items
        if (this.appContext == null || arrayList == null || arrayList.isEmpty()) {
            return false
        }
        var items: ArrayList<QuickItem?>? = ArrayList()
        val size = arrayList.size
        var found = false
        var index = 0
        while (true) {
            if (index >= size) {
                break
            }
            val rawQuickItem: Any? = arrayList.get(index)
            index++
            val quickItem = rawQuickItem as QuickItem?
            if (quickItem != null) {
                if (quickItem.itemKind.toInt() != 0) {
                    items!!.add(quickItem)
                } else if (!quickItem.isChecked || TextUtils.isEmpty(quickItem.path)) {
                    items!!.add(quickItem)
                } else if (!quickItem.isFolder) {
                    if (deleteWhere(
                            get(this.appContext).getWritableDatabase(),
                            "DbBookQuick_table",
                            "_secret=? AND _path=?",
                            arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey(), quickItem.path)
                        ) > 0
                    ) {
                        found = true
                    }
                } else if (deletePath(this.appContext, quickItem.path, true)) {
                    found = true
                }
            }
        }
        if (!TextUtils.isEmpty(folderPath) && items!!.size < 2) {
            if (items.size == 1) {
                val quickItem = items.get(0)
                val order = if (quickItem != null && !TextUtils.isEmpty(quickItem.path)) orderOf(
                        this.appContext,
                        folderPath
                    ) else 0
                if (quickItem != null && !TextUtils.isEmpty(quickItem.path) && order != -1) {
                    val args = arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey(), quickItem.path)
                    val contentValuesG = android.content.ContentValues().also { cv -> cv.put("_rsv1", "") }
                    contentValuesG.put("_order", order)
                    updateWhere(
                        get(this.appContext).getWritableDatabase(),
                        "DbBookQuick_table",
                        contentValuesG,
                        "_secret=? AND _path=?",
                        args
                    )
                    deletePath(this.appContext, folderPath, false)
                }
            } else {
                deletePath(this.appContext, folderPath, false)
            }
            items = null
        }
        this.items = items
        return found
    }

    fun applyEditAlpha(pos: Int, animate: Boolean) {
        val quickHolderI: QuickHolder = rowAt(pos) ?: return
        val view: View = quickHolderI.itemView
        val quickItemD: QuickItem = itemAt(quickHolderI.pos) ?: return
        if (quickItemD.itemKind.toInt() == 1) {
            view.setAlpha(if (this.editMode) 0.4f else 1.0f)
        } else {
            view.setAlpha(1.0f)
        }
        if (quickItemD.itemKind.toInt() == 0) {
            val tile = quickHolderI.tile ?: return
            tile.checkShown = this.editMode
            if (this.editMode) tile.setChecked(quickItemD.isChecked, animate)
        }
    }

    fun backColor(): Int {
        val darkBars = MainUtil.isDarkBars(this.darkState)
        val listMode = this.listMode
        if (darkBars) {
            if (listMode.toInt() == 0) {
                return if (AppState.darkUi || BrowserSettings.darkHome.value) -16777216 else -1
            }
            return if (AppState.darkUi || BrowserSettings.darkHome.value) -16777216 else -592138
        }
        if (AppState.darkUi) {
            return if (listMode.toInt() == 0) -14606047 else -16777216
        }
        return -592138
    }

    fun checkedCount(): Int {
        val arrayList = this.items
        var checked = 0
        if (arrayList != null && !arrayList.isEmpty()) {
            val size = arrayList.size
            var index = 0
            while (index < size) {
                val rawQuickItem: Any? = arrayList.get(index)
                index++
                val quickItem = rawQuickItem as QuickItem?
                if (quickItem != null && quickItem.itemKind.toInt() == 0 && quickItem.isChecked) {
                    checked++
                }
            }
        }
        return checked
    }

    companion object {
        @JvmStatic
        fun bottomPad(): Int {
            if (ZtriSettings.quickUse.value || ZtwoSettings.quickDown.value) {
                return MainApp.dp48 shl 1
            }
            return 0
        }

        @JvmStatic
        fun holderOf(view: View?): QuickHolder? {
            val tag: Any? = if (view == null) null else view.getTag()
            if (view == null || tag == null || (tag !is QuickHolder)) {
                return null
            }
            return tag
        }

        @JvmStatic
        fun positionOf(view: View?): Int {
            val quickHolderI: QuickHolder? = holderOf(view)
            if (quickHolderI == null) {
                return -1
            }
            return quickHolderI.pos
        }

        @JvmStatic
        fun newsEnabled(): Boolean {
            return ZtwoSettings.newsUse.value || ZtwoSettings.newsAllow.value
        }
    }
}
