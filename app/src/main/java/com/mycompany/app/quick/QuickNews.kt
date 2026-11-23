package com.mycompany.app.quick

import java.util.ArrayList
import java.util.Comparator
import android.content.Context
import android.os.Handler
import com.mycompany.app.main.MainApp
import com.mycompany.app.quick.QuickAdapter.QuickItem
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService

@Suppress("deprecation")
open class QuickNews(rfcDates: Boolean) {
    @JvmField
    var appContext: Context? = null
    @JvmField
    var newsListener: NewsListListener? = null
    @JvmField
    var mainHandler: Handler? = null
    @JvmField
    var feedUrl: String? = null
    @JvmField
    var newsExecutor: ExecutorService? = null
    @JvmField
    var datePattern: String?
    @JvmField
    var dateFormat: SimpleDateFormat? = null
    @JvmField
    var dateFormatter: DateTimeFormatter? = null
    @JvmField
    var prevDate: String? = null

    interface NewsListListener {
        fun onTranslated(list: MutableList<QuickAdapter.QuickItem?>?)

        fun onList(list: MutableList<QuickAdapter.QuickItem?>?, isOk: Boolean)

        fun onProgress(progress: Int)
    }

    class SortNews : Comparator<QuickItem?> {
        override fun compare(first: QuickItem?, second: QuickItem?): Int {
            val quickItem = first
            val other = second
            if (quickItem == null) {
                return if (other == null) 0 else 1
            }
            if (other == null) {
                return -1
            }
            val time = quickItem.time
            val timeNow = other.time
            if (time == timeNow) {
                return 0
            }
            return if (time > timeNow) -1 else 1
        }
    }

    class SortPos : Comparator<QuickItem?> {
        override fun compare(first: QuickItem?, second: QuickItem?): Int {
            val quickItem = first
            val other = second
            if (quickItem == null) {
                return if (other == null) 0 else -1
            }
            if (other == null) {
                return 1
            }
            val blockStart = quickItem.blockStart
            val blockStartNow = other.blockStart
            if (blockStart < 0) {
                return if (blockStartNow < 0) 0 else -1
            }
            return if (blockStartNow < 0 || blockStart >= blockStartNow) 1 else -1
        }
    }

    init {
        this.datePattern = if (rfcDates) "EEE, dd MMM yyyy HH:mm:ss" else "yyyy.MM.dd HH:mm:ss"
    }

    open fun start(
        context: Context?,
        handler: Handler?,
        url: String?,
        newsListListener: NewsListListener?
    ) {
        this.appContext = context
        this.newsListener = newsListListener
        this.mainHandler = handler
        this.feedUrl = url
    }

    open fun release() {
        this.appContext = null
        this.newsListener = null
        this.mainHandler = null
        this.feedUrl = null
        this.newsExecutor = null
        this.datePattern = null
        this.dateFormat = null
        this.dateFormatter = null
        this.prevDate = null
    }

    fun runAsync(runnable: Runnable?) {
        var executorServiceH = this.newsExecutor
        if (executorServiceH == null || executorServiceH.isShutdown() || executorServiceH.isTerminated()) {
            executorServiceH = MainApp.executor(this.appContext)
            if (executorServiceH == null) {
                return
            } else {
                this.newsExecutor = executorServiceH
            }
        }
        try {
            executorServiceH.execute(runnable)
        } catch (unused: Exception) {
        }
    }

    companion object {
        @JvmStatic
        fun withHeader(arrayList: ArrayList<QuickItem?>?): MutableList<QuickAdapter.QuickItem?> {
            var newsIndex = 0
            if (arrayList == null || arrayList.isEmpty()) {
                val items: ArrayList<QuickAdapter.QuickItem?> = ArrayList()
                val quickItem = QuickItem()
                quickItem.itemKind = 5.toByte()
                quickItem.newsIndex = 0
                items.add(quickItem)
                val headerItem = QuickItem()
                headerItem.itemKind = 6.toByte()
                headerItem.newsIndex = 1
                items.add(headerItem)
                val gapItem = QuickItem()
                gapItem.itemKind = 8.toByte()
                headerItem.newsIndex = 2
                items.add(gapItem)
                return items
            }
            val moreItem = QuickItem()
            moreItem.itemKind = 5.toByte()
            arrayList.add(0, moreItem)
            val endGap = QuickItem()
            endGap.itemKind = 8.toByte()
            arrayList.add(endGap)
            val size = arrayList.size
            var index = 0
            while (index < size) {
                val rawQuickItem: Any? = arrayList.get(index)
                index++
                val quickItem = rawQuickItem as QuickItem?
                if (quickItem != null) {
                    quickItem.newsIndex = newsIndex
                    newsIndex++
                }
            }
            return arrayList
        }
    }
}
