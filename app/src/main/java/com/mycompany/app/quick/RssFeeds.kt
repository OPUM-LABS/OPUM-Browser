package com.mycompany.app.quick

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.ZtwoSettings
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * The RSS reader (2026-11-19): the user's feeds (`ZtwoSettings.rssFeeds`, one address per line), fetched while the RSS
 * page is open and merged newest first. RSS 2.0 (`item`) and Atom (`entry`) are read with Jsoup's XML parser. Articles
 * are [QuickAdapter.QuickItem]s of kind 7 like the news, so the news page's cards show them:
 * `label` headline, `summary` feed name, `path` article link, `imageUrl` its picture (media / enclosure / first image),
 * `thumbUrl` the site's favicon, `sourceHtml` the date as "yyyy.MM.dd HH:mm:ss" (local time).
 */
object RssFeeds {
    /** Articles kept from all feeds together. */
    const val MAX_ITEMS = 120

    /** Last result (memory only), its time and the feeds it was read from. */
    @Volatile var items: List<QuickAdapter.QuickItem> = emptyList()
        private set
    @Volatile var loadedAt = 0L
        private set
    @Volatile var loadedFrom = ""
        private set
    /** Feeds that could not be read in the last load. */
    @Volatile var failed: List<String> = emptyList()
        private set

    /** Feed names from the last load (address -> channel title), for the News page's tabs. */
    private val titles = java.util.concurrent.ConcurrentHashMap<String, String>()

    /** The feed's name, or its host until it has been read once. */
    fun titleOf(url: String): String = titles[url] ?: (MainUtil.shortHostOf(url) ?: url)

    /**
     * News and RSS became one page (2026-11-20): a custom news address (it replaced Google News and had to be a feed)
     * moves into the feed list once, and Google News is used for "Top stories" again.
     */
    fun migrateCustomNews(context: Context?) {
        val url = ZtwoSettings.newsUrl.value
        if (TextUtils.isEmpty(url) || url == "https://news.google.com/rss") return
        normalize(url)?.let { add(context, it) }
        ZtwoSettings.edit(context) { set(ZtwoSettings.newsUrl, "") }
    }

    /**
     * Blocking: the feed for [input] — the address itself when it is RSS / Atom, otherwise the feed the page announces
     * (`<link rel="alternate" type="application/rss+xml">` or Atom), else null. Call off the main thread.
     */
    fun resolve(context: Context?, input: String): String? {
        val url = normalize(input) ?: return null
        val agent = try { MainUtil.defaultUserAgent(context) } catch (_: Exception) { "Mozilla/5.0" }
        return try {
            val res = Jsoup.connect(url).userAgent(agent).ignoreContentType(true).timeout(15_000)
                .maxBodySize(4 * 1024 * 1024).followRedirects(true).execute()
            val body = res.body()
            val xml = Jsoup.parse(body, url, Parser.xmlParser())
            if (xml.selectFirst("rss, feed, rdf|RDF, RDF") != null) return res.url().toString()
            val html = Jsoup.parse(body, res.url().toString())
            val link = html.select("link[rel~=(?i)alternate][type~=(?i)(rss|atom)\\+xml]").firstOrNull()
            link?.absUrl("href")?.takeIf { it.startsWith("http") }
        } catch (_: Exception) {
            null
        }
    }

    fun feeds(): List<String> =
        (ZtwoSettings.rssFeeds.value ?: "").split('\n').map { it.trim() }.filter { it.isNotEmpty() }

    private fun store(context: Context?, list: List<String>) {
        ZtwoSettings.edit(context) { set(ZtwoSettings.rssFeeds, list.joinToString("\n")) }
    }

    /** "example.com/feed" → "https://example.com/feed"; null if it is no web address. */
    fun normalize(input: String?): String? {
        var url = input?.trim() ?: return null
        if (url.isEmpty() || url.contains(' ')) return null
        if (!url.startsWith("http://", true) && !url.startsWith("https://", true)) url = "https://$url"
        return if (android.webkit.URLUtil.isNetworkUrl(url) && url.substringAfter("://").contains('.')) url else null
    }

    /** Adds a feed (if new); true when the list changed. */
    fun add(context: Context?, url: String): Boolean {
        val list = feeds().toMutableList()
        if (list.any { it.equals(url, true) }) return false
        list.add(url)
        store(context, list)
        return true
    }

    fun remove(context: Context?, url: String) {
        store(context, feeds().filterNot { it == url })
        // drop its articles right away
        items = items.filterNot { it.link == url }
    }

    /** True when the stored articles are older than 30 minutes or from another feed list. */
    fun stale(): Boolean =
        loadedAt == 0L || System.currentTimeMillis() > loadedAt + 1_800_000L || loadedFrom != feeds().joinToString("\n")

    /** Blocking: reads all feeds (at most four at a time) and keeps the merged list. Call off the main thread. */
    fun load(context: Context?) {
        val list = feeds()
        val agent = try { MainUtil.defaultUserAgent(context) } catch (_: Exception) { "Mozilla/5.0" }
        val results = java.util.Collections.synchronizedList(ArrayList<QuickAdapter.QuickItem>())
        val bad = java.util.Collections.synchronizedList(ArrayList<String>())
        val pool = java.util.concurrent.Executors.newFixedThreadPool(4)
        try {
            val jobs = list.map { feed ->
                pool.submit {
                    val got = try { read(feed, agent) } catch (_: Throwable) { null }
                    if (got == null) bad.add(feed) else results.addAll(got)
                }
            }
            for (job in jobs) try { job.get(25, java.util.concurrent.TimeUnit.SECONDS) } catch (_: Exception) { }
        } finally {
            pool.shutdownNow()
        }
        val sorted = synchronized(results) { results.sortedByDescending { it.time } }.take(MAX_ITEMS)
        // not an index into the Google news list (QuickAdapter.loadNewsImage looks pictures up there by it)
        sorted.forEach { it.newsIndex = -1 }
        items = sorted
        failed = synchronized(bad) { bad.toList() }
        loadedFrom = list.joinToString("\n")
        loadedAt = System.currentTimeMillis()
    }

    /** One feed's articles, or null when it could not be read as RSS / Atom. */
    private fun read(feed: String, agent: String): List<QuickAdapter.QuickItem>? {
        val body = Jsoup.connect(feed).userAgent(agent).ignoreContentType(true).timeout(15_000)
            .maxBodySize(4 * 1024 * 1024).followRedirects(true).execute().body()
        val doc: Document = Jsoup.parse(body, feed, Parser.xmlParser())
        val channelTitle = text(doc.selectFirst("channel > title")) ?: text(doc.selectFirst("feed > title"))
        val host = MainUtil.shortHostOf(feed) ?: feed
        val name = if (TextUtils.isEmpty(channelTitle)) host else channelTitle!!
        titles[feed] = name
        val favicon = siteOf(text(doc.selectFirst("channel > link")) ?: doc.selectFirst("feed > link[rel=alternate], feed > link:not([rel])")?.attr("href") ?: feed)
            ?.let { "$it/favicon.ico" }
        val entries = doc.select("item").ifEmpty { doc.select("entry") }
        if (entries.isEmpty() && doc.selectFirst("rss, feed, rdf|RDF, RDF") == null) return null
        val out = ArrayList<QuickAdapter.QuickItem>()
        for (e in entries) {
            val title = clean(text(e.selectFirst("title"))) ?: continue
            val link = linkOf(e) ?: continue
            val time = dateOf(e)
            val item = QuickAdapter.QuickItem()
            item.itemKind = 7
            item.label = title
            item.summary = name
            item.path = link
            item.link = feed
            item.imageUrl = imageOf(e)
            item.thumbUrl = favicon
            item.time = time
            item.sourceHtml = if (time > 0) STAMP.get()!!.format(java.util.Date(time)) else ""
            out.add(item)
        }
        return out
    }

    private fun text(e: Element?): String? = e?.text()?.trim()?.takeIf { it.isNotEmpty() }

    /** Titles sometimes carry escaped HTML. */
    private fun clean(s: String?): String? = s?.let { Jsoup.parse(it).text().trim() }?.takeIf { it.isNotEmpty() }

    private fun linkOf(e: Element): String? {
        text(e.selectFirst("link"))?.let { if (it.startsWith("http")) return it }
        val atom = e.select("link").firstOrNull { it.attr("rel").isEmpty() || it.attr("rel") == "alternate" }
        atom?.attr("href")?.takeIf { it.startsWith("http") }?.let { return it }
        return text(e.selectFirst("guid"))?.takeIf { it.startsWith("http") }
    }

    private fun imageOf(e: Element): String? {
        for (sel in listOf("media|thumbnail", "media|content[medium=image]", "media|content[url]", "enclosure[type^=image]", "itunes|image")) {
            val el = try { e.selectFirst(sel) } catch (_: Exception) { null } ?: continue
            val url = el.attr("url").ifEmpty { el.attr("href") }
            if (url.startsWith("http")) return url
        }
        // the first picture in the description / content
        val html = text(e.selectFirst("content|encoded")) ?: e.selectFirst("description, summary, content")?.text()
        if (!html.isNullOrEmpty() && html.contains("<img", true)) {
            val src = Jsoup.parse(html).selectFirst("img[src]")?.attr("src")
            if (src != null && src.startsWith("http")) return src
        }
        return null
    }

    private fun siteOf(url: String?): String? {
        if (url == null || !url.startsWith("http")) return null
        return try { val u = java.net.URL(url); "${u.protocol}://${u.host}" } catch (_: Exception) { null }
    }

    private val STAMP = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue() = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.US)
    }

    private val PATTERNS = listOf(
        "EEE, dd MMM yyyy HH:mm:ss Z", "EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd MMM yyyy HH:mm Z", "dd MMM yyyy HH:mm:ss Z",
        "yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd",
    )

    /** Publication time in ms (0 = unknown). */
    private fun dateOf(e: Element): Long {
        val raw = text(e.selectFirst("pubDate")) ?: text(e.selectFirst("published")) ?: text(e.selectFirst("updated"))
            ?: text(e.selectFirst("dc|date")) ?: return 0L
        for (p in PATTERNS) {
            try {
                val f = SimpleDateFormat(p, Locale.US)
                if (p.endsWith("'Z'")) f.timeZone = TimeZone.getTimeZone("UTC")
                f.isLenient = false
                return f.parse(raw)?.time ?: continue
            } catch (_: Exception) {
            }
        }
        return 0L
    }
}
