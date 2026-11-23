package com.mycompany.app.quick

import java.util.ArrayList
import android.content.Context
import android.os.Build
import android.os.Handler
import android.text.Html
import android.text.TextUtils
import android.webkit.URLUtil
import com.google.android.gms.common.internal.ImagesContract
import com.mycompany.app.data.DataNews
import com.mycompany.app.data.DataNews.Companion.instance
import com.mycompany.app.data.book.DataBookRecent.Companion.instance
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.datePattern
import com.mycompany.app.main.MainUtil.formatDate
import com.mycompany.app.quick.QuickAdapter.QuickItem

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.Locale

@Suppress("deprecation")
class QuickNewsCustom(rfcDates: Boolean) : QuickNews(rfcDates) {
    @JvmField
    var feedItems: MutableList<QuickAdapter.QuickItem?>? = null

    internal inner class LoadFeedTask : Runnable {

        internal inner class DeliverFeedStep : Runnable {
            override fun run() {
                val quickNewsCustom = this@QuickNewsCustom
                val list = quickNewsCustom.feedItems
                quickNewsCustom.feedItems = null
                val newsListListener = quickNewsCustom.newsListener
                if (newsListListener != null) {
                    newsListListener.onList(list, false)
                    quickNewsCustom.newsListener = null
                }
            }
        }

        /** Loads the custom RSS feed into news items, caches them and reports them.  */
        override fun run() {
            val news = this@QuickNewsCustom
            var items: ArrayList<QuickAdapter.QuickItem?>? = null
            var feed: Document?
            try {
                feed = Jsoup.connect(news.feedUrl!!).parser(Parser.xmlParser()).get()
            } catch (unused: Exception) {
                feed = null
            } catch (unused: LinkageError) {
                feed = null
            } catch (unused: OutOfMemoryError) {
                feed = null
            }
            if (feed != null) {
                try {
                    val cached = DataNews.instance(news.appContext).items
                    for (element in feed.select("item")) {
                        if (news.appContext == null) {
                            items = null
                            break
                        }
                        val item = news.parseEntry(element, (cached))
                        if (item != null) {
                            if (items == null) {
                                items = ArrayList()
                            }
                            items.add(item)
                        }
                    }
                    if (items != null && !items.isEmpty()) {
                        try {
                            Collections.sort<QuickItem?>(items, SortNews())
                        } catch (unused: Exception) {
                        }
                    } else {
                        items = null
                    }
                } catch (unused: Exception) {
                }
            }
            val list: MutableList<QuickAdapter.QuickItem?> = QuickNews.Companion.withHeader(items)
            com.mycompany.app.glide.ThumbLoader.stopAll()
            DataNews.instance(news.appContext).setItems(list)
            news.feedItems = list
            if (news.mainHandler != null) {
                news.mainHandler!!.post(DeliverFeedStep())
            }
        }
    }

    override fun start(
        context: Context?,
        handler: Handler?,
        url: String?,
        newsListListener: NewsListListener?
    ) {
        super.start(context, handler, url, newsListListener)
        runAsync(LoadFeedTask())
    }

    fun parseEntry(element: Element?, list: MutableList<QuickAdapter.QuickItem?>?): QuickItem? {
        var epochMilli: Long
        val dateText: String?
        var descLength: Int = 0
        var titleLength: Int = 0
        var dashIndex: Int = 0
        var newImageUrl: String? = null
        if (element != null) {
            var itemTitle: String? = plainText(element.getElementsByTag("title").html())
            if (!TextUtils.isEmpty(itemTitle)) {
                val itemLink: String? = plainText(element.getElementsByTag("link").html())
                if (URLUtil.isNetworkUrl(itemLink)) {
                    val elementsByTag = element.getElementsByTag("source")
                    var description: String? = plainText(elementsByTag.html())
                    if (TextUtils.isEmpty(description)) {
                        description = MainUtil.siteNameOf(itemLink!!, false)
                    }
                    if (!TextUtils.isEmpty(description)) {
                        val shortTitle = if (description == null || (description.length.also {
                                descLength = it
                            }) == 0 || itemTitle == null || (itemTitle.length.also {
                                titleLength = it
                            }) == 0 || !itemTitle.endsWith(description) || (((titleLength - descLength) + (-3)).also {
                                dashIndex = it
                            }) <= 0 || !itemTitle.startsWith(" - ", dashIndex)) null else itemTitle.substring(0, dashIndex)
                        if (!TextUtils.isEmpty(shortTitle)) {
                            itemTitle = shortTitle
                        }
                    }
                    val imageUrl: String? = plainText(elementsByTag.attr(ImagesContract.URL))
                    val favicon = if (URLUtil.isNetworkUrl(imageUrl)) MainUtil.faviconUrlFor(imageUrl!!) else null
                    var pubDateText: String? = element.getElementsByTag("pubDate").html()
                    val length = if (pubDateText == null) 0 else pubDateText.length
                    if (pubDateText == null || length == 0) {
                        pubDateText = null
                    } else if (length > 4 && pubDateText.endsWith(" GMT")) {
                        pubDateText = pubDateText.substring(0, length - 4)
                    } else if (length > 6 && pubDateText.endsWith(" +0000")) {
                        pubDateText = pubDateText.substring(0, length - 6)
                    }
                    var time: Long = 0
                    if (!TextUtils.isEmpty(pubDateText)) {
                        try {
                            var simpleDateFormat = this.dateFormat
                            if (simpleDateFormat == null) {
                                simpleDateFormat = SimpleDateFormat(this.datePattern, Locale.US)
                                this.dateFormat = simpleDateFormat
                            }
                            epochMilli = simpleDateFormat.parse(pubDateText!!)!!.getTime()
                        } catch (unused: Exception) {
                            try {
                                var dateTimeFormatterOfPattern = this.dateFormatter
                                if (dateTimeFormatterOfPattern == null) {
                                    dateTimeFormatterOfPattern =
                                        DateTimeFormatter.ofPattern(this.datePattern, Locale.US)
                                    this.dateFormatter = dateTimeFormatterOfPattern
                                }
                                epochMilli =
                                    LocalDateTime.parse(pubDateText, dateTimeFormatterOfPattern)
                                        .atOffset(
                                            ZoneOffset.UTC
                                        ).toInstant().toEpochMilli()
                            } catch (unused2: Exception) {
                                epochMilli = 0
                            }
                        }
                        if (epochMilli == 0L) {
                            dateText = null
                        } else {
                            var prevDate = this.prevDate
                            if (prevDate == null) {
                                prevDate = datePattern("yyyy.MM.dd HH:mm:ss")
                                this.prevDate = prevDate
                            }
                            dateText = MainUtil.formatDate(epochMilli, prevDate, "yyyy.MM.dd HH:mm:ss")
                        }
                        if (!TextUtils.isEmpty(dateText)) {
                            pubDateText = dateText
                        }
                        time = epochMilli
                    }
                    if (list != null && !list.isEmpty() && !TextUtils.isEmpty(itemLink)) {
                        val it: MutableIterator<Any?> = list.iterator()
                        while (it.hasNext()) {
                            val quickItem = it.next() as QuickItem?
                            if (quickItem != null && itemLink == quickItem.path) {
                                newImageUrl = quickItem.imageUrl
                                break
                            }
                        }
                    }
                    val quickItem = QuickItem()
                    quickItem.itemKind = 7.toByte()
                    quickItem.label = itemTitle
                    quickItem.summary = description
                    quickItem.path = itemLink
                    quickItem.imageUrl = newImageUrl
                    quickItem.thumbUrl = favicon
                    quickItem.sourceHtml = pubDateText
                    quickItem.time = time
                    return quickItem
                }
            }
        }
        return null
    }

    companion object {
        @JvmStatic
        fun plainText(html: String?): String? {
            var html = html
            if (!TextUtils.isEmpty(html)) {
                val spannedFromHtml = if (TextUtils.isEmpty(html)) null else Html.fromHtml(html, 0)
                if (spannedFromHtml != null) {
                    val plain = spannedFromHtml.toString()
                    if (!TextUtils.isEmpty(plain)) {
                        html = plain
                    }
                }
                val trimmed = html!!.trim { it <= ' ' }
                if (!TextUtils.isEmpty(trimmed)) {
                    return trimmed.replace("amp;", "")
                }
            }
            return null
        }
    }
}
