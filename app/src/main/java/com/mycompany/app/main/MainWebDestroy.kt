package com.mycompany.app.main

import android.view.View

import java.util.ArrayList
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import com.mycompany.app.web.WebViewActivity

@Suppress("deprecation")
class MainWebDestroy(var outer: WebViewActivity?, var handler: Handler?, var webBusyListener: WebBusyListener?) {
    @JvmField
    val lock: Any = Any()
    @JvmField
    var running: Boolean = false
    @JvmField
    var jsBlocked: Boolean = false
    @JvmField
    var busyChecked: Boolean = false
    @JvmField
    var paused: Boolean = false
    @JvmField
    var stopRequested: Boolean = false
    @JvmField
    var webIndex: Int = 0
    @JvmField
    var delItems: ArrayList<DelItem?>? = null
    @JvmField
    var nextItem: DelItem? = null
    @JvmField
    var currentItem: DelItem? = null

    internal inner class TakeNextTask : Runnable {

        internal inner class TakeNextStep : Runnable {
            override fun run() {
                val mainWebDestroy = this@MainWebDestroy
                mainWebDestroy.stopRequested = false
                var delItemE = mainWebDestroy.nextItem
                mainWebDestroy.nextItem = null
                if (delItemE == null) {
                    synchronized(mainWebDestroy.lock) {
                        delItemE = mainWebDestroy.takeNext()
                    }
                }
                if (mainWebDestroy.stopRequested) {
                    mainWebDestroy.stopRequested = false
                    mainWebDestroy.running = false
                    mainWebDestroy.jsBlocked = false
                    mainWebDestroy.start(false)
                    return
                }
                if (delItemE == null) {
                    mainWebDestroy.running = false
                    return
                }
                mainWebDestroy.currentItem = delItemE
                val handler = mainWebDestroy.handler
                if (handler == null) {
                    return
                }
                handler.post(mainWebDestroy.DestroyNextTask())
            }
        }

        override fun run() {
            val webViewActivity = this@MainWebDestroy.outer
            if (webViewActivity == null) {
                return
            }
            webViewActivity.runInBackground(TakeNextStep())
        }
    }

    internal inner class DestroyNextTask : Runnable {
        override fun run() {
            val mainWebDestroy = this@MainWebDestroy
            val delItem = mainWebDestroy.currentItem
            mainWebDestroy.currentItem = null
            if (delItem == null) {
                mainWebDestroy.running = false
                return
            }
            val webView = delItem.webView
            if (webView != null) {
                if (mainWebDestroy.paused) {
                    val nextItem = DelItem()
                    nextItem.webView = webView
                    mainWebDestroy.nextItem = nextItem
                    return
                }
                if (mainWebDestroy.checkBusy()) {
                    val nextItem = DelItem()
                    nextItem.webView = webView
                    mainWebDestroy.nextItem = nextItem
                    mainWebDestroy.running = false
                    mainWebDestroy.jsBlocked = false
                    mainWebDestroy.start(false)
                    return
                }
                mainWebDestroy.jsBlocked = true
                MainUtil.detachWebView(webView)
                try {
                    webView.destroy()
                } catch (unused: Exception) {
                }
                val handler = mainWebDestroy.handler
                if (handler != null) {
                    handler.postDelayed(mainWebDestroy.RestartTask(), 200L)
                    return
                }
                return
            }
            // Only web views are queued now (the ad views are gone).
            mainWebDestroy.running = false
        }
    }

    internal inner class RestartTask : Runnable {
        override fun run() {
            val mainWebDestroy = this@MainWebDestroy
            mainWebDestroy.running = false
            mainWebDestroy.jsBlocked = false
            mainWebDestroy.start(false)
        }
    }

    class DelItem {
        @JvmField
        var list: MutableList<WebView?>? = null
        @JvmField
        var webView: WebView? = null
    }

    interface WebBusyListener {
        fun isBusy(): Boolean
    }

    init {
        if (handler == null) {
            this.handler = Handler(Looper.getMainLooper())
        }
    }

    fun addAll(list: MutableList<WebView?>?) {
        if (list!!.isEmpty()) {
            return
        }
        synchronized(this.lock) {
            var arrayList = this.delItems
            if (arrayList == null) {
                arrayList = ArrayList()
                this.delItems = arrayList
            }
            val delItem = DelItem()
            delItem.list = list
            arrayList.add(delItem)
        }
        if (this.paused) {
            return
        }
        start(false)
    }

    fun add(webView: WebView?) {
        if (webView != null) {
            webView.onPause()
            webView.setVisibility(View.GONE)
            synchronized(this.lock) {
                var arrayList = this.delItems
                if (arrayList == null) {
                    arrayList = ArrayList()
                    this.delItems = arrayList
                }
                val delItem = DelItem()
                delItem.webView = webView
                arrayList.add(delItem)
            }
            if (this.paused) {
                return
            }
            start(false)
        }
    }

    fun start(quick: Boolean) {
        val delayMs: Long
        if (this.running) {
            return
        }
        this.running = true
        if (quick) {
            delayMs = 1200
        } else {
            delayMs = if (checkBusy()) 800L else 500L
        }
        val handler = this.handler
        if (handler == null) {
            return
        }
        handler.postDelayed(TakeNextTask(), delayMs)
    }

    fun currentWeb(): DelItem? {
        try {
            val delItem = this.delItems!!.get(0)
            if (delItem != null) {
                val list = delItem.list
                if (list != null && this.webIndex < list.size) {
                    val webView = list.get(this.webIndex)
                    this.webIndex++
                    val pending = DelItem()
                    pending.webView = webView
                    return pending
                }
                val webView = delItem.webView
                if (webView != null) {
                    delItem.webView = null
                    val queued = DelItem()
                    queued.webView = webView
                    return queued
                }
            }
        } catch (unused: Exception) {
        }
        return null
    }

    fun takeNext(): DelItem? {
        try {
            val arrayList = this.delItems
            if (arrayList == null || arrayList.isEmpty() || this.webIndex < 0) {
                reset()
                return null
            }
            if (this.paused) {
                return null
            }
            if (checkBusy()) {
                this.stopRequested = true
                return null
            }
            val delItemD = currentWeb()
            if (delItemD != null) {
                return delItemD
            }
            this.delItems!!.removeAt(0)
            this.webIndex = 0
            if (!this.delItems!!.isEmpty()) {
                return currentWeb()
            }
            reset()
            return null
        } catch (unused: Exception) {
            reset()
            return null
        }
    }

    fun checkBusy(): Boolean {
        if (this.busyChecked) {
            this.busyChecked = false
            return true
        }
        val webBusyListener = this.webBusyListener
        if (webBusyListener == null) {
            return false
        }
        return webBusyListener.isBusy()
    }

    fun reset() {
        this.running = false
        this.jsBlocked = false
        this.busyChecked = false
        this.paused = false
        this.stopRequested = false
        this.webIndex = 0
        this.delItems = null
    }
}
