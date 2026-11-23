package com.mycompany.app.main

import com.mycompany.app.web.WebSearchAdapter.SearchHolder
import com.bumptech.glide.request.RequestFutureTarget
import java.util.ArrayList
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.PictureDrawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.util.Executors
import com.mycompany.app.compress.Compress.Companion.isSvgName
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.pref.PrefUtil.setSingleLine
import com.mycompany.app.view.GlideRequest
import com.mycompany.app.view.GlideRequests

import java.util.concurrent.ExecutorService

@Suppress("deprecation")
class MainListLoader(var context: Context?, var withThumb: Boolean, var listener: ListLoadListener?) {
    @JvmField
    var queue: ArrayList<Any?>? = null
    @JvmField
    var glideRequests: GlideRequests? = null
    @JvmField
    var mainHandler: Handler? = Handler(Looper.getMainLooper())
    @JvmField
    var loadExecutor: ExecutorService? = null
    @JvmField
    var running: Boolean = false
    @JvmField
    var doneItem: ChildItem? = null
    @JvmField
    var doneView: View? = null
    @JvmField
    var doneBitmap: Bitmap? = null
    @JvmField
    var packageManager: PackageManager? = null

    internal inner class LoadTask : Runnable {
        /**
         * Takes the next queued row that is still on screen, loads its thumbnail/favicon
         * in the background and hands it to the UI thread.
         * Off the UI thread it only reads the row view's tag (the position check); the view itself is used by
         * [DeliverItemTask] on the UI thread, and the next load starts only after that delivery, so the `done*` fields
         * are never overwritten early.
         */
        @android.annotation.SuppressLint("ThreadConstraint")
        override fun run() {
            val loader = this@MainListLoader
            if (loader.mainHandler == null) {
                return
            }
            var item: ChildItem? = null
            var view: View? = null
            try {
                if (loader.queue == null || loader.queue!!.isEmpty()) {
                    loader.running = false
                    return
                }
                val it = loader.queue!!.iterator()
                while (it.hasNext()) {
                    if (loader.queue == null) {
                        loader.running = false
                        return
                    }
                    val loadItem = it.next() as LoadItem?
                    it.remove()
                    if (loadItem == null || loadItem.childItem == null || loadItem.view == null) {
                        continue
                    }
                    if (isStillBound(loadItem.view!!.getTag(), loadItem.childItem!!)) {
                        item = loadItem.childItem
                        view = loadItem.view
                        break
                    }
                }
            } catch (unused: Exception) {
                item = null
                view = null
            }
            if (item == null || view == null) {
                loader.running = false
                return
            }
            var bitmap: Bitmap? = null
            try {
                bitmap = load(loader, item)
            } catch (unused2: Exception) {
            }
            if (MainUtil.isBitmapOk(bitmap)) {
                cache(item, bitmap)
            }
            loader.doneItem = item
            loader.doneView = view
            loader.doneBitmap = bitmap
            if (loader.mainHandler != null) {
                loader.mainHandler!!.post(loader.DeliverItemTask())
            }
        }

        /** True if the row view still shows `item` (views are recycled while scrolling).  */
        private fun isStillBound(tag: Any?, item: ChildItem?): Boolean {
            if (tag == null) {
                return false
            }
            if (tag is SearchHolder) {
                return (tag).itemPos == item!!.position
            }
            if (tag is Int) {
                return tag == item!!.position
            }
            if (tag is RecyclerView.ViewHolder) {
                return tag.bindingAdapterPosition == item!!.position
            }
            return false
        }

        @Throws(Exception::class)
        private fun load(loader: MainListLoader?, item: ChildItem?): Bitmap? {
            val context = loader!!.context
            when (item!!.itemType.toInt()) {
                1 -> {
                    if (loader.glideRequests == null) {
                        return MainUtil.childBitmap(item, loader.withThumb)
                    }
                    val executor = Executors.directExecutor()
                    val size: Int = MainApp.Companion.dp48
                    if (isSvgName(item.name)) {
                        val request =
                            (loader.glideRequests!!.`as`(PictureDrawable::class.java) as GlideRequest<*>).load(item.path)
                        val builder = request.override(size, size)
                        val target = builder.submit()
                        return MainUtil.drawableToBitmap(size, target.get() as PictureDrawable?)
                    }
                    val request = (loader.glideRequests!!.asBitmap() as GlideRequest<*>).load(item.path)
                    val builder = request.override(size, size)
                    val target = builder.submit()
                    return target.get() as Bitmap?
                }

                2 -> return MainUtil.childThumbCached(context, item)
                3 -> return MainUtil.childThumb2(context, item)
                4 -> return MainUtil.childThumb(context, item)
                6 -> {
                    if (item.resolveInfo != null) {
                        if (loader.packageManager == null) {
                            loader.packageManager = context!!.getPackageManager()
                        }
                        return MainUtil.drawableToBitmap(MainApp.Companion.dp48, item.resolveInfo!!.loadIcon(loader.packageManager))
                    }
                    return MainUtil.appIcon(context, item.path)
                }

                8 -> return if (item.compress != null) item.compress!!.bitmapAt(item.position) else null
                11 -> return MainUtil.loadAlbumThumb(item.listType, item.id, context, item.path)
                else -> return null
            }
        }

        private fun cache(item: ChildItem?, bitmap: Bitmap?) {
            if (item!!.listType == 32) {
                cacheListIcon(item.path, bitmap, item.isSecret)
                return
            }
            if (TextUtils.isEmpty(item.path)) {
                return
            }
            if (item.itemType.toInt() == 11 && (item.listType == 18 || item.listType == 39)) {
                MainUtil.cacheIcon(MainUtil.shortHostOf(item.path), bitmap)
                return
            }
            val key = com.mycompany.app.glide.ImageCache.key(2, item.path)
            com.mycompany.app.glide.ImageCache.put(key, bitmap)
        }
    }

    internal inner class DeliverItemTask : Runnable {
        override fun run() {
            val mainListLoader = this@MainListLoader
            if (mainListLoader.mainHandler == null) {
                return
            }
            val childItem = mainListLoader.doneItem
            val view = mainListLoader.doneView
            val bitmap = mainListLoader.doneBitmap
            mainListLoader.doneItem = null
            mainListLoader.doneView = null
            mainListLoader.doneBitmap = null
            val bitmapOk = MainUtil.isBitmapOk(bitmap)
            val listLoadListener = mainListLoader.listener
            if (bitmapOk) {
                if (listLoadListener != null) {
                    listLoadListener.onLoaded(childItem, view, bitmap)
                }
            } else if (listLoadListener != null) {
                listLoadListener.onFailed(childItem, view)
            }
            mainListLoader.running = false
            mainListLoader.startLoading()
        }
    }

    interface ListLoadListener {
        fun onFailed(childItem: ChildItem?, view: View?)

        fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?)
    }

    private class LoadItem {
        var childItem: ChildItem? = null
        var view: View? = null
    }

    fun startLoading() {
        if (this.mainHandler == null || this.running) {
            return
        }
        this.running = true
        val loadTask: LoadTask = LoadTask()
        var executorServiceH = this.loadExecutor
        if (executorServiceH == null || executorServiceH.isShutdown() || executorServiceH.isTerminated()) {
            executorServiceH = MainApp.Companion.executor(this.context)
            if (executorServiceH == null) {
                return
            } else {
                this.loadExecutor = executorServiceH
            }
        }
        try {
            executorServiceH.execute(loadTask)
        } catch (unused: Exception) {
        }
    }

    fun enqueue(childItem: ChildItem?, view: View?) {
        if (childItem == null || view == null) {
            return
        }
        try {
            var arrayList = this.queue
            if (arrayList == null) {
                arrayList = ArrayList<Any?>()
                this.queue = arrayList
            }
            val loadItem = LoadItem()
            loadItem.childItem = childItem
            loadItem.view = view
            arrayList.add(loadItem)
            startLoading()
        } catch (unused: Exception) {
        }
    }

    fun release() {
        if (this.glideRequests != null) {
            this.glideRequests = null
        }
        this.queue = null
        this.listener = null
        MainUtil.clearHandler(this.mainHandler)
        this.mainHandler = null
        this.loadExecutor = null
        this.doneItem = null
        this.doneView = null
        this.doneBitmap = null
        this.packageManager = null
    }

    companion object {
        @JvmStatic
        fun cachedIconFor(childItem: ChildItem?): Bitmap? {
            val listType: Int
            if (childItem == null || TextUtils.isEmpty(childItem.path)) {
                return null
            }
            return if (childItem.itemType.toInt() == 11 && ((childItem.listType.also {
                    listType = it
                }) == 18 || listType == 39)) MainUtil.cachedIcon(MainUtil.shortHostOf(childItem.path)) else com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(2, childItem.path))
        }

        @JvmStatic
        fun cachedListIcon(path: String?, secret: Boolean): Bitmap? {
            try {
                val key: String? = cacheKey(path, secret)
                if (TextUtils.isEmpty(key)) {
                    return null
                }
                return com.mycompany.app.glide.ImageCache.listGet(key)
            } catch (unused: Exception) {
                return null
            }
        }

        @JvmStatic
        fun cacheKey(path: String?, secret: Boolean): String? {
            if (TextUtils.isEmpty(path)) {
                return null
            }
            val sbR = StringBuilder().append(path)
            sbR.append(if (secret) ".sec" else ".nor")
            return sbR.toString()
        }

        @JvmStatic
        fun cacheListIcon(path: String?, bitmap: Bitmap?, secret: Boolean) {
            try {
                val key: String? = cacheKey(path, secret)
                if (TextUtils.isEmpty(key)) {
                    return
                }
                com.mycompany.app.glide.ImageCache.listPut(key, bitmap)
            } catch (unused: Exception) {
            }
        }
    }
}
