package com.mycompany.app.widget

import android.app.PendingIntent
import com.mycompany.app.pref.store.ListSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.TtsSettings
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.SparseArray
import android.widget.RemoteViews
import android.widget.RemoteViews.RemoteCollectionItems
import com.mycompany.app.data.book.DataBookRecent.Companion.instance
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.db.book.DbBookWeb.Companion.hasFolder
import com.mycompany.app.list.book.ListBookWeb
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.SortChildUser
import com.mycompany.app.pref.PrefList
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefTts
import ch.opumlabs.opumbrowser.R

import java.util.Collections
import com.mycompany.app.db.book.DbBookWeb

@Suppress("deprecation")
open class WidgetBookProviderBody : AppWidgetProvider() {
    @JvmField
    var darkTheme: Boolean = false
    @JvmField
    var updateContext: Context? = null
    @JvmField
    var widgetManager: AppWidgetManager? = null
    @JvmField
    var widgetIds: IntArray? = null
    @JvmField
    var widgetIndex: Int = 0
    @JvmField
    var receiveContext: Context? = null
    @JvmField
    var receiveId: Int = 0
    @JvmField
    var openContext: Context? = null
    @JvmField
    var openPath: String? = null

    internal inner class UpdateAllTask : Runnable {
        override fun run() {
            val widgetBookProviderBody = this@WidgetBookProviderBody
            val context = widgetBookProviderBody.receiveContext
            widgetBookProviderBody.receiveContext = null
            val appWidgetManager: AppWidgetManager? = if (context == null) null else AppWidgetManager.getInstance(context)
            if (context == null || appWidgetManager == null) {
                return
            }
            widgetBookProviderBody.updateAll(
                context,
                appWidgetManager,
                appWidgetManager.getAppWidgetIds(
                    ComponentName(
                        context,
                        (if (widgetBookProviderBody.darkTheme) WidgetBookProviderDark::class.java else WidgetBookProviderLight::class.java) as Class<*>
                    )
                )
            )
        }
    }

    internal inner class UpdateWidgetTask : Runnable {
        override fun run() {
            val widgetBookProviderBody = this@WidgetBookProviderBody
            val receiveId = widgetBookProviderBody.receiveId
            val context = widgetBookProviderBody.openContext
            val openPath = widgetBookProviderBody.openPath
            widgetBookProviderBody.openContext = null
            widgetBookProviderBody.openPath = null
            if (context == null || receiveId == 0) {
                return
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            widgetBookProviderBody.updateWidget(context, appWidgetManager, receiveId, openPath)
        }
    }

    internal inner class BrowserIntentTask : Runnable {
        override fun run() {
            val widgetBookProviderBody = this@WidgetBookProviderBody
            val context = widgetBookProviderBody.openContext
            val openPath = widgetBookProviderBody.openPath
            widgetBookProviderBody.openContext = null
            widgetBookProviderBody.openPath = null
            if (context == null) {
                return
            }
            if (!MainUtil.isLockEnabled()) {
                val intentL = MainUtil.browserIntent(context)
                intentL.putExtra("EXTRA_PATH", openPath)
                if (TtsSettings.appKeypad.value) {
                    intentL.putExtra("EXTRA_KEYPAD", true)
                }
                intentL.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intentL)
                return
            }
            val mainAppO = MainApp.from(context)
            if (mainAppO != null) {
                mainAppO.skipLockOnce = true
            }
            val intentH = MainUtil.lockIntent(context, SecretSettings.lockType.value)
            intentH.putExtra("EXTRA_TYPE", 0)
            intentH.putExtra("EXTRA_PATH", openPath)
            intentH.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intentH)
        }
    }

    internal inner class UpdateNextTask : Runnable {
        override fun run() {
            this@WidgetBookProviderBody.updateNext()
        }
    }

    internal inner class UpdateNextStep : Runnable {
        override fun run() {
            this@WidgetBookProviderBody.updateNext()
        }
    }

    fun updateNext() {
        try {
            val widgetIndex = this.widgetIndex
            val widgetId = if (widgetIndex < 0 || this.widgetIds == null || widgetIndex >= this.widgetIds!!.size) -1234 else this.widgetIds!![widgetIndex]
            if (widgetId == -1234) {
                this.updateContext = null
                this.widgetManager = null
                this.widgetIds = null
                return
            }
            updateWidget(this.updateContext, this.widgetManager, widgetId, WidgetBookData.Companion.instance().pathFor(widgetId))
            val newWidgetIndex = this.widgetIndex + 1
            this.widgetIndex = newWidgetIndex
            if ((if (newWidgetIndex < 0 || this.widgetIds == null || newWidgetIndex >= this.widgetIds!!.size) -1234 else this.widgetIds!![newWidgetIndex]) != -1234) {
                MainApp.runInBackground(this.updateContext, UpdateNextStep())
                return
            }
            this.updateContext = null
            this.widgetManager = null
            this.widgetIds = null
        } catch (unused: Exception) {
            this.updateContext = null
            this.widgetManager = null
            this.widgetIds = null
        }
    }

    fun updateAll(context: Context?, appWidgetManager: AppWidgetManager?, widgetIds: IntArray?) {
        if (context == null || widgetIds == null || widgetIds.size == 0) {
            this.updateContext = null
            this.widgetManager = null
            this.widgetIds = null
        } else {
            this.updateContext = context
            this.widgetManager = appWidgetManager
            this.widgetIds = widgetIds
            this.widgetIndex = 0
            MainApp.runInBackground(context, UpdateNextTask())
        }
    }

    fun updateWidget(context: Context?, appWidgetManager: AppWidgetManager?, id: Int, path: String?) {
        if (context == null || id == 0 || appWidgetManager == null) {
            return
        }
        val intent = Intent(
            context,
            (if (this.darkTheme) WidgetBookProviderDark::class.java else WidgetBookProviderLight::class.java) as Class<*>
        )
        intent.putExtra("appWidgetId", id)
        intent.setAction("ch.opumlabs.opumbrowser.ACTION_BOOKGET_SELECT")
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)))
        val sdkInt = Build.VERSION.SDK_INT
        val broadcast =
            PendingIntent.getBroadcast(context, 10, intent, if (sdkInt >= 31) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT)
        val remoteViews = RemoteViews(
            context.getPackageName(),
            if (this.darkTheme) R.layout.widget_book_layout_dark else R.layout.widget_book_layout_light
        )
        remoteViews.setEmptyView(R.id.list_view, R.id.empty_view)
        remoteViews.setPendingIntentTemplate(R.id.list_view, broadcast)
        val arrayListC: ArrayList<*>? = loadItems(context, id, path)
        if (sdkInt >= 31) {
            val builder = RemoteCollectionItems.Builder()
            var position = 0
            if ((if (arrayListC != null) arrayListC.size else 0) != 0) {
                val size = arrayListC!!.size
                var index = 0
                while (index < size) {
                    val obj: Any? = arrayListC.get(index)
                    index++
                    builder.addItem(position.toLong(), itemViews(context, obj as ChildItem?, this.darkTheme)!!)
                    position++
                }
            }
            remoteViews.setRemoteAdapter(R.id.list_view, builder.build())
        } else {
            val openIntent = Intent(
                context,
                (if (this.darkTheme) WidgetBookServiceDark::class.java else WidgetBookServiceLight::class.java) as Class<*>
            )
            openIntent.putExtra("appWidgetId", id)
            openIntent.setData(Uri.parse(openIntent.toUri(Intent.URI_INTENT_SCHEME)))
            remoteViews.setRemoteAdapter(R.id.list_view, openIntent)
            appWidgetManager.notifyAppWidgetViewDataChanged(id, R.id.list_view)
        }
        appWidgetManager.updateAppWidget(id, remoteViews)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        if (context == null || appWidgetIds == null || appWidgetIds.size == 0) {
            return
        }
        for (index in appWidgetIds) {
            val widgetBookDataB: WidgetBookData = WidgetBookData.Companion.instance()
            if (index == 0) {
                widgetBookDataB.javaClass
            } else {
                val sparseArray = widgetBookDataB.widgetPaths
                if (sparseArray != null) {
                    try {
                        sparseArray.remove(index)
                        val widgetItems = widgetBookDataB.widgetItems
                        if (widgetItems != null) {
                            widgetItems.remove(index)
                        }
                    } catch (unused: Exception) {
                    }
                    widgetBookDataB.savePaths(context)
                }
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.getAction()
        action!!.javaClass
        if (action != "ch.opumlabs.opumbrowser.ACTION_BOOKGET_SELECT") {
            if (action == "ch.opumlabs.opumbrowser.ACTION_BOOKGET_UPDATE") {
                this.receiveContext = context
                MainApp.runInBackground(context, UpdateAllTask())
                return
            }
            return
        }
        val intExtra = intent.getIntExtra("appWidgetId", 0)
        val booleanExtra = intent.getBooleanExtra("EXTRA_TYPE", false)
        val stringExtra = intent.getStringExtra("EXTRA_PATH")
        this.receiveId = intExtra
        this.openContext = context
        this.openPath = stringExtra
        if (booleanExtra) {
            MainApp.runInBackground(context, UpdateWidgetTask())
        } else {
            Handler(Looper.getMainLooper()).post(BrowserIntentTask())
        }
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        updateAll(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        @JvmStatic
        fun iconFor(url: String?): Bitmap? {
            if (TextUtils.isEmpty(url)) {
                return null
            }
            val bitmapA = com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(2, url))
            return if (MainUtil.isBitmapOk(bitmapA)) bitmapA else MainUtil.cachedIcon(MainUtil.shortHostOf(url))
        }

        @JvmStatic
        fun itemViews(context: Context?, childItem: ChildItem?, darkTheme: Boolean): RemoteViews? {
            if (context == null || childItem == null) {
                return null
            }
            val remoteViews = RemoteViews(context.getPackageName(), R.layout.widget_book_item)
            val iconView = R.id.icon_view
            if (childItem.isDir) {
                if (childItem.rowType.toInt() == 1) {
                    if (darkTheme) {
                        remoteViews.setImageViewResource(iconView, R.drawable.outline_shift_2_dark_24)
                    } else {
                        remoteViews.setImageViewResource(iconView, R.drawable.outline_shift_2_black_24)
                    }
                } else if (darkTheme) {
                    remoteViews.setImageViewResource(iconView, R.drawable.outline_folder_dark_24)
                } else {
                    remoteViews.setImageViewResource(iconView, R.drawable.outline_folder_black_24)
                }
            } else if (!TextUtils.isEmpty(childItem.path)) {
                val isLocal = childItem.path!!.startsWith("file:///")
                val path = childItem.path
                if (!isLocal) {
                    val bitmapA: Bitmap? = iconFor(path)
                    if (MainUtil.isBitmapOk(bitmapA)) {
                        remoteViews.setImageViewBitmap(iconView, bitmapA)
                    } else if (darkTheme) {
                        remoteViews.setImageViewResource(iconView, R.drawable.outline_public_dark_web_24)
                    } else {
                        remoteViews.setImageViewResource(iconView, R.drawable.outline_public_black_24)
                    }
                } else if ("file:///android_asset/shortcut.html" == path) {
                    if (darkTheme) {
                        remoteViews.setImageViewResource(iconView, R.drawable.outline_home_dark_web_24)
                    } else {
                        remoteViews.setImageViewResource(iconView, R.drawable.outline_home_black_24)
                    }
                } else if (darkTheme) {
                    remoteViews.setImageViewResource(iconView, R.drawable.outline_offline_pin_dark_web_24)
                } else {
                    remoteViews.setImageViewResource(iconView, R.drawable.outline_offline_pin_black_24)
                }
            } else if (darkTheme) {
                remoteViews.setImageViewResource(iconView, R.drawable.outline_public_dark_web_24)
            } else {
                remoteViews.setImageViewResource(iconView, R.drawable.outline_public_black_24)
            }
            remoteViews.setTextViewText(R.id.name_view, childItem.name)
            remoteViews.setTextColor(R.id.name_view, if (darkTheme) -5197648 else -16777216)
            val intent = Intent()
            intent.putExtra("EXTRA_TYPE", childItem.isDir)
            intent.putExtra("EXTRA_PATH", childItem.path)
            remoteViews.setOnClickFillInIntent(R.id.item_view, intent)
            return remoteViews
        }

        /**
         * Bookmarks of folder path (falls back to "/" when the folder no longer exists) for widget id: folders and
         * links, sorted by the bookmark list setting, with a ".." item outside the root. Remembers the widget's folder
         * (and on Android < 12 its items) in WidgetBookData.
         */
        @JvmStatic
        fun loadItems(context: Context?, id: Int, path: String?): ArrayList<*>? {
            if (context == null || id == 0) {
                return null
            }
            var dir = MainUtil.trimSlashes(path)
            if (!TextUtils.isEmpty(dir)) {
                dir = "/" + dir
                if (!hasFolder(context, MainUtil.uriDisplayName(null, dir), MainUtil.parentPathOf(null, dir))) {
                    dir = "/"
                }
            } else {
                dir = "/"
            }
            val list: ArrayList<ChildItem?> = ArrayList()
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    DbBookWeb.get(context).getWritableDatabase(),
                    "DbBookWeb_table",
                    arrayOf<String?>("_id", "_isdir", "_path", "_title", "_time", "_rsv4"),
                    "_secret=? AND _dir=?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey(), dir),
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndex("_id")
                    val dirColumn = cursor.getColumnIndex("_isdir")
                    val pathColumn = cursor.getColumnIndex("_path")
                    val titleColumn = cursor.getColumnIndex("_title")
                    val orderColumn = cursor.getColumnIndex("_rsv4")
                    do {
                        val item = ChildItem()
                        item.listType = 17
                        item.id = cursor.getLong(idColumn)
                        item.isDir = cursor.getInt(dirColumn) == 1
                        item.dir = dir
                        item.name = cursor.getString(titleColumn)
                        if (item.isDir) {
                            item.path = if (dir == "/") "/" + item.name else dir + "/" + item.name
                        } else {
                            item.path = cursor.getString(pathColumn)
                            if (TextUtils.isEmpty(item.path)) {
                                continue
                            }
                        }
                        if (ListSettings.bookWebUser.value) {
                            item.order = cursor.getLong(orderColumn)
                        } else {
                            ListBookWeb.lowerTitle(item)
                        }
                        cacheThumb(context, item)
                        list.add(item)
                    } while (cursor.moveToNext())
                }
            } catch (ignored: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
            if (!list.isEmpty()) {
                try {
                    Collections.sort<ChildItem?>(
                        list,
                        if (ListSettings.bookWebUser.value) SortChildUser() else (MainUtil.childComparator(0, ListSettings.bookWebItem.value, ListSettings.bookWebRvse.value))
                    )
                } catch (ignored: Exception) {
                }
            }
            if ("/" != dir) {
                val up = ChildItem()
                up.rowType = 1
                up.isDir = true
                var name = ".."
                val slash = dir.lastIndexOf('/')
                if (slash >= 0 && slash + 1 < dir.length) {
                    name = dir.substring(slash + 1)
                }
                up.name = name
                up.path = MainUtil.uriDisplayName(null, dir)
                list.add(0, up)
            }
            val data: WidgetBookData = WidgetBookData.Companion.instance()
            try {
                if (data.widgetPaths == null) {
                    data.widgetPaths = SparseArray()
                }
                data.widgetPaths!!.put(id, dir)
            } catch (ignored: Exception) {
            }
            data.savePaths(context)
            return list
        }

        @JvmStatic
        fun cacheThumb(context: Context?, childItem: ChildItem) {
            if (childItem.isDir || TextUtils.isEmpty(childItem.path) || MainUtil.isBitmapOk(iconFor(childItem.path))) {
                return
            }
            val bitmapF = MainUtil.loadAlbumThumb(childItem.listType, childItem.id, context, childItem.path)
            if (MainUtil.isBitmapOk(bitmapF)) {
                com.mycompany.app.glide.ImageCache.put(com.mycompany.app.glide.ImageCache.key(2, childItem.path), bitmapF)
            }
        }
    }
}
