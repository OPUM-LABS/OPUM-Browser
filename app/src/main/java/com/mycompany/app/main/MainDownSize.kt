package com.mycompany.app.main

import com.mycompany.app.dialog.DialogDownSize.DownSizeListener
import com.mycompany.app.main.MainUtil.LoopCancelListener
import java.util.ArrayList
import android.content.Context
import android.text.TextUtils
import com.mycompany.app.compress.CompressUtil.extractedPaths
import com.mycompany.app.data.DataUtil.addUri
import com.mycompany.app.db.book.DbBookAlbum.Companion.cached; import com.mycompany.app.db.book.DbBookAlbum.Companion.get
import com.mycompany.app.db.book.DbBookCmp.Companion.cached; import com.mycompany.app.db.book.DbBookCmp.Companion.get
import com.mycompany.app.db.book.DbBookLocale.Companion.cached; import com.mycompany.app.db.book.DbBookLocale.Companion.get
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookPdf.Companion.cached; import com.mycompany.app.db.book.DbBookPdf.Companion.get
import com.mycompany.app.down.DownParseDzen
import com.mycompany.app.down.DownParseDzen.segmentUrls
import com.mycompany.app.down.DownParseKakao
import com.mycompany.app.down.DownParseKakao.parseItemText
import com.mycompany.app.down.DownParseKakao.segmentUrls
import com.mycompany.app.down.DownParseM3u8
import com.mycompany.app.down.DownParseM3u8.Companion.parseItemText
import com.mycompany.app.down.DownParseM3u8.Companion.queryOf
import com.mycompany.app.down.DownParseM3u8.DownParseListener
import com.mycompany.app.down.DownParseReddit
import com.mycompany.app.down.DownParseReddit.parseRedditItem
import com.mycompany.app.down.DownParseTsfile.findSegments
import com.mycompany.app.down.DownParseVimeo.vimeoItem
import com.mycompany.app.down.DownParseVimeo.vimeoSegments
import com.mycompany.app.down.DownSaveAudio.saveAudio
import com.mycompany.app.down.DownSaveImage.saveImageAt
import com.mycompany.app.down.DownSavePath.savePathPart
import com.mycompany.app.down.DownSaveSplit.saveSegment
import com.mycompany.app.main.MainDownSvc.DownItem
import com.mycompany.app.main.MainDownSvc.M3u8Item
import com.mycompany.app.pref.PrefUtil.showDetail
import java.net.HttpURLConnection

@Suppress("deprecation")
class MainDownSize {
    @JvmField
    var appContext: Context? = null
    @JvmField
    var running: Boolean = false
    @JvmField
    var connection: HttpURLConnection? = null

    internal inner class SizeForwarder(downSizeListener: DownSizeListener?) : DownSizeListener {
        val downSizeListener: DownSizeListener

        init {
            this.downSizeListener = downSizeListener!!
        }

        override fun onSize(size: Long) {
            this.downSizeListener.onSize(size)
        }
    }

    internal inner class DisconnectTask : Runnable {
        override fun run() {
            val mainDownSize = this@MainDownSize
            val httpURLConnection = mainDownSize.connection
            if (httpURLConnection != null) {
                httpURLConnection.disconnect()
                mainDownSize.connection = null
            }
        }
    }

    internal inner class ReleaseStep(
        mainDownSize: MainDownSize?,
        val downParseM3u8: DownParseM3u8,
        val downItem: DownItem, downSizeListener: DownSizeListener?
    ) : DownParseListener {
        val downSizeListener: DownSizeListener
        val mainDownSize: MainDownSize

        init {
            this.downSizeListener = downSizeListener!!
            this.mainDownSize = mainDownSize!!
        }

        override fun onParsed(list: MutableList<String?>?) {
            val downSizeListener: DownSizeListener = this.downSizeListener
            val mainDownSize = this.mainDownSize
            val downParseM3u = this.downParseM3u8
            val downItem = this.downItem
            if ((list != null && !list.isEmpty()) || !downParseM3u.fakeM3u8) {
                if (downParseM3u.masterFound) {
                    val isApi = MainConst.isApi28
                    downItem.referer = null
                }
                downParseM3u.release()
                mainDownSize.m3u8Size(downItem, null, list, downSizeListener)
                return
            }
            downParseM3u.release()
            val urlText = MainUtil.urlFromText(downItem.url)
            downItem.url = urlText
            downItem.urlType = 0
            (downSizeListener as SizeForwarder).onSize(mainDownSize.headSize(urlText, downItem.referer))
        }
    }

    internal inner class MasterSizeStep(
        mainDownSize: MainDownSize?,
        val downParseM3u8: DownParseM3u8,
        val downItem: DownItem,
        val m3u8Item: M3u8Item?, downSizeListener: DownSizeListener?
    ) : DownParseListener {
        val downSizeListener: DownSizeListener
        val mainDownSize: MainDownSize

        init {
            this.downSizeListener = downSizeListener!!
            this.mainDownSize = mainDownSize!!
        }

        override fun onParsed(list: MutableList<String?>?) {
            val downParseM3u = this.downParseM3u8
            val masterFound = downParseM3u.masterFound
            val downItem = this.downItem
            if (masterFound) {
                val isApi = MainConst.isApi28
                downItem.referer = null
            }
            downParseM3u.release()
            this.mainDownSize.m3u8Size(downItem, this.m3u8Item, list, this.downSizeListener)
        }
    }

    internal inner class StopWhenIdle : LoopCancelListener {
        override val isCancelled: Boolean
            get() = !this@MainDownSize.running
    }

    internal inner class SegmentsSizeStep(
        mainDownSize: MainDownSize?,
        val parser: DownParseM3u8,
        val downItem: DownItem,
        val items: MutableList<String?>?, downSizeListener: DownSizeListener?
    ) : DownParseListener {
        val downSizeListener: DownSizeListener
        val mainDownSize: MainDownSize

        init {
            this.downSizeListener = downSizeListener!!
            this.mainDownSize = mainDownSize!!
        }

        override fun onParsed(list: MutableList<String?>?) {
            this.parser.release()
            this.mainDownSize.segmentsSize(this.downItem, this.items, list, this.downSizeListener)
        }
    }

    fun start(context: Context?, downUrl: String?, referer: String?, downSizeListener: DownSizeListener?) {
        if (TextUtils.isEmpty(downUrl)) {
            downSizeListener!!.onSize(0L)
            return
        }
        this.appContext = context
        this.running = true
        val item = DownItem()
        item.url = downUrl
        item.referer = referer
        val type: Int = MainDownSvc.Companion.urlType(downUrl)
        item.urlType = type
        if (type == 0) {
            downSizeListener!!.onSize(headSize(item.url, item.referer))
            return
        }
        if (type == 15) {
            // Separate video and audio streams: the size is the sum of both.
            var total: Long = 0
            if (!TextUtils.isEmpty(item.url) && this.running) {
                val video = headSize(MainUtil.urlFromText(item.url), item.referer)
                if (this.running) {
                    val sum = headSize(MainUtil.mixUrlOf(item.url), item.referer) + video
                    if (this.running) {
                        total = sum
                    }
                }
            }
            downSizeListener!!.onSize(total)
            return
        }
        if (type == 14 || type == 13) {
            downSizeListener!!.onSize(0L)
            return
        }
        val listener: SizeForwarder = SizeForwarder(downSizeListener)
        if (TextUtils.isEmpty(item.url)) {
            listener.onSize(0L)
            return
        }
        if (type == 1) {
            val url = MainUtil.urlFromText(item.url)
            val base = DownParseM3u8.queryOf(url)
            val parser = DownParseM3u8(this.appContext)
            parser.fakeM3u8 = !TextUtils.isEmpty(item.url) && item.url!!.startsWith("m3fake:")
            parser.parseMaster(url, item.referer, base, ReleaseStep(this, parser, item, listener))
            return
        }
        if (type == 2) {
            val m3u8 = DownParseM3u8.parseItemText(item.url)
            if (m3u8 == null) {
                m3u8Size(item, null, null, listener)
                return
            }
            val base = DownParseM3u8.queryOf(m3u8.url)
            val parser = DownParseM3u8(this.appContext)
            parser.parseSegments(
                null,
                m3u8.url,
                item.referer,
                base,
                m3u8.src,
                MasterSizeStep(this, parser, item, m3u8, listener)
            )
            return
        }
        var m3u8: M3u8Item? = null
        var list: MutableList<String?>? = null
        if (type == 4) {
            m3u8 = DownParseReddit.parseRedditItem(item.url)
            if (m3u8 != null) {
                list = DownParseReddit.redditSegments(m3u8.src)
            }
        } else if (type == 6) {
            m3u8 = DownParseKakao.parseItemText(item.url)
            if (m3u8 != null) {
                list =
                    DownParseKakao.segmentUrls(this.appContext, m3u8.url, item.referer, m3u8.template, m3u8.src, null, StopWhenIdle())
            }
        } else if (type == 8) {
            m3u8 = DownParseDzen.parseItemText(item.url)
            if (m3u8 != null) {
                list = (DownParseDzen.segmentUrls(
                    this.appContext,
                    m3u8.url,
                    item.referer,
                    m3u8.template,
                    m3u8.src,
                    null,
                    StopWhenIdle()
                ))
            }
        } else if (type == 10) {
            m3u8 = vimeoItem(this.appContext, item.url, item.referer)
            if (m3u8 != null) {
                list = vimeoSegments(m3u8.vimeoJson, m3u8.base, m3u8.src, false)
            }
        } else if (type == 11) {
            list = findSegments(this.appContext, item.url, item.referer, null, false, StopWhenIdle())
        } else if (type == 12) {
            list = findSegments(this.appContext, item.url, item.referer, null, true, StopWhenIdle())
        }
        m3u8Size(item, m3u8, list, listener)
    }

    fun headSize(url: String?, referer: String?): Long {
        if (TextUtils.isEmpty(url) || !this.running) {
            return 0L
        }
        val conn = MainUtil.openHeadConnection(this.appContext, url, referer, 0, 0, false, MainUtil.userAgentPref(), true)
        this.connection = conn
        if (conn == null) {
            return 0L
        }
        var size: Long = 0
        try {
            conn.setDoInput(true)
            this.connection!!.connect()
            var type: String? = null
            try {
                type = this.connection!!.getContentType()
            } catch (unused: Exception) {
            }
            // An HTML page is not the file itself.
            if (TextUtils.isEmpty(type) || !type!!.startsWith("text/html")) {
                size = this.connection!!.getContentLengthLong()
            }
        } catch (unused: Exception) {
            size = 0
        }
        if (this.connection != null) {
            this.connection!!.disconnect()
            this.connection = null
        }
        return if (this.running) size else 0L
    }

    fun m3u8Size(
        downItem: DownItem?,
        m3u8Item: M3u8Item?,
        list: MutableList<String?>?,
        downSizeListener: DownSizeListener?
    ) {
        if (TextUtils.isEmpty(downItem!!.url)) {
            (downSizeListener as SizeForwarder).onSize(0L)
            return
        }
        if ((if (list != null) list.size else 0) == 0) {
            (downSizeListener as SizeForwarder).onSize(0L)
            return
        }
        if (!this.running) {
            (downSizeListener as SizeForwarder).onSize(0L)
            return
        }
        val urlType = downItem.urlType
        var arrayListD: ArrayList<String?>? = null
        if (urlType == 2) {
            if (m3u8Item != null && !TextUtils.isEmpty(m3u8Item.audioUrl)) {
                val query = DownParseM3u8.queryOf(m3u8Item.url)
                val downParseM3u = DownParseM3u8(this.appContext)
                downParseM3u.parseSegments(
                    null,
                    m3u8Item.url,
                    downItem.referer,
                    query,
                    m3u8Item.audioUrl,
                    SegmentsSizeStep(
                        this,
                        downParseM3u,
                        downItem,
                        list,
                        downSizeListener
                    )
                )
                return
            }
        } else if (urlType == 4) {
            if (m3u8Item != null) {
                arrayListD = DownParseReddit.redditSegments(m3u8Item.audioUrl)
            }
        } else if (urlType == 6) {
            if (m3u8Item != null) {
                arrayListD = (DownParseKakao.segmentUrls(
                    this.appContext,
                    m3u8Item.url,
                    downItem.referer,
                    m3u8Item.audioTemplate,
                    m3u8Item.audioUrl,
                    downItem,
                    StopWhenIdle()
                ))
            }
        } else if (urlType == 8) {
            if (m3u8Item != null) {
                arrayListD = (DownParseDzen.segmentUrls(
                    this.appContext,
                    m3u8Item.url,
                    downItem.referer,
                    m3u8Item.audioTemplate,
                    m3u8Item.audioUrl,
                    downItem,
                    StopWhenIdle()
                ))
            }
        } else if (urlType == 10 && m3u8Item != null) {
            arrayListD = vimeoSegments(m3u8Item.vimeoJson, m3u8Item.base, null, true)
        }
        segmentsSize(downItem, list, arrayListD, downSizeListener)
    }

    fun segmentsSize(
        downItem: DownItem?,
        list: MutableList<String?>?,
        segments: MutableList<String?>?,
        downSizeListener: DownSizeListener?
    ) {
        if (TextUtils.isEmpty(downItem!!.url)) {
            (downSizeListener as SizeForwarder).onSize(0L)
            return
        }
        val size = if (list != null) list.size else 0
        if (size == 0) {
            (downSizeListener as SizeForwarder).onSize(0L)
            return
        }
        if (!this.running) {
            (downSizeListener as SizeForwarder).onSize(0L)
            return
        }
        val size2 = if (segments != null) segments.size else 0
        if (!this.running) {
            (downSizeListener as SizeForwarder).onSize(0L)
            return
        }
        var estimatedSize = headSize(list!!.get(size / 2), downItem.referer) * (size.toLong())
        if (!this.running) {
            (downSizeListener as SizeForwarder).onSize(0L)
            return
        }
        if (size2 > 0) {
            estimatedSize += headSize(segments!!.get(size2 / 2), downItem.referer) * (size2.toLong())
        }
        if (this.running) {
            (downSizeListener as SizeForwarder).onSize(estimatedSize)
        } else {
            (downSizeListener as SizeForwarder).onSize(0L)
        }
    }

    fun stop() {
        val context = this.appContext
        this.appContext = null
        this.running = false
        if (this.connection == null) {
            return
        }
        MainApp.Companion.runInBackground(context, DisconnectTask())
    }
}
