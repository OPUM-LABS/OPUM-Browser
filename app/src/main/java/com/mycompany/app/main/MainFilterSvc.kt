package com.mycompany.app.main

import com.mycompany.app.main.MainUtil.LoopCancelListener
import com.mycompany.app.dialog.DialogSetDesk.SetDeskListener
import com.mycompany.app.db.book.DbBookFilter
import java.util.ArrayList
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.webkit.URLUtil
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.compress.CompressUtil.extractedPaths
import com.mycompany.app.data.DataUtil.addUri
import com.mycompany.app.data.DataUtil.fileKind
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.DbUtil.queryById
import com.mycompany.app.db.book.DbBookAlbum.Companion.cached; import com.mycompany.app.db.book.DbBookAlbum.Companion.get
import com.mycompany.app.db.book.DbBookCmp.Companion.cached; import com.mycompany.app.db.book.DbBookCmp.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.addPathItem
import com.mycompany.app.db.book.DbBookLocale.Companion.cached; import com.mycompany.app.db.book.DbBookLocale.Companion.get
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookPdf.Companion.cached; import com.mycompany.app.db.book.DbBookPdf.Companion.get
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookSearch.Companion.get
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.down.DownParseKakao.parseItemText
import com.mycompany.app.down.DownParseReddit.parseRedditItem
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.pref.PrefUtil.showDetail
import com.mycompany.app.pref.PrefUtil.foldMode
import com.mycompany.app.pref.PrefUtil.itemMode
import com.mycompany.app.pref.PrefUtil.reversed
import ch.opumlabs.opumbrowser.R
import java.lang.ref.WeakReference

@Suppress("deprecation")
class MainFilterSvc(var context: Context?, setDeskListener: SetDeskListener?) {
    @JvmField
    var deskListener: SetDeskListener?
    @JvmField
    var mainHandler: Handler?
    @JvmField
    var filterTask: DialogTask? = null
    @JvmField
    var stopped: Boolean = false
    @JvmField
    var changed: Boolean = false
    @JvmField
    var notiBuilder: NotificationCompat.Builder? = null
    @JvmField
    var lastNotiTime: Long = 0
    @JvmField
    var notiShown: Boolean = false
    @JvmField
    var stopReceiver: EventReceiver? = null
    @JvmField
    var filterDown: MainFilterDown? = null

    internal inner class ShowListTask : Runnable {

        internal inner class ShowNotificationTask : Runnable {
            override fun run() {
                var cursorG: Cursor?
                val mainFilterSvc = this@MainFilterSvc
                mainFilterSvc.showNotification()
                val context = mainFilterSvc.context
                if (context != null) {
                    var arrayList: ArrayList<Any?>? = null
                    try {
                        cursorG = DbUtil.query(
                            DbBookFilter.get(context).getWritableDatabase(),
                            "DbBookFilter_table",
                            arrayOf<String?>("_path", "_title"),
                            "_use=?",
                            arrayOf<String?>("1"),
                            null
                        )
                        if (cursorG != null) {
                            try {
                                if (cursorG.moveToFirst()) {
                                    val columnIndex = cursorG.getColumnIndex("_path")
                                    val titleCol = cursorG.getColumnIndex("_title")
                                    do {
                                        val path = cursorG.getString(columnIndex)
                                        if (!TextUtils.isEmpty(path)) {
                                            val childItem = ChildItem()
                                            childItem.path = path
                                            childItem.name = cursorG.getString(titleCol)
                                            if (arrayList == null) {
                                                arrayList = ArrayList<Any?>()
                                            }
                                            arrayList.add(childItem)
                                        }
                                        if (mainFilterSvc.stopped) {
                                            break
                                        }
                                    } while (cursorG.moveToNext())
                                }
                            } catch (unused: Exception) {
                            }
                        }
                    } catch (unused2: Exception) {
                        cursorG = null
                    }
                    if (cursorG != null) {
                        cursorG.close()
                    }
                    if (mainFilterSvc.context != null) {
                        mainFilterSvc.filterDown = MainFilterDown(mainFilterSvc.FilterDownCancel())
                        mainFilterSvc.updateNext(0, arrayList)
                    }
                }
            }
        }

        override fun run() {
            val mainFilterSvc = this@MainFilterSvc
            if (mainFilterSvc.context != null && mainFilterSvc.stopReceiver == null) {
                mainFilterSvc.stopReceiver = mainFilterSvc.EventReceiver()
                val intentFilter = IntentFilter()
                intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_FILTER_CANCEL")
                ContextCompat.registerReceiver(mainFilterSvc.context!!, mainFilterSvc.stopReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
            }
            MainApp.Companion.runInBackground(mainFilterSvc.context, ShowNotificationTask())
        }
    }

    internal inner class ChangedTask : Runnable {
        override fun run() {
            val mainFilterSvc = this@MainFilterSvc
            val setDeskListener: SetDeskListener? = mainFilterSvc.deskListener
            mainFilterSvc.deskListener = null
            if (setDeskListener == null) {
                return
            }
            setDeskListener.onChanged(mainFilterSvc.changed && !mainFilterSvc.stopped)
        }
    }

    internal inner class NotificationsTask(val context: Context?) : Runnable {
        override fun run() {
            val notificationManagerP: NotificationManager? = MainApp.Companion.notifications(this.context)
            if (notificationManagerP != null) {
                notificationManagerP.cancel(2147483639)
            }
        }
    }

    internal inner class FilterDownCancel : LoopCancelListener {
        override val isCancelled: Boolean
            get() {
                val dialogTask: DialogTask?
                val mainFilterSvc = this@MainFilterSvc
                return mainFilterSvc.context == null || mainFilterSvc.stopped || ((mainFilterSvc.filterTask.also {
                    dialogTask = it
                }) != null && dialogTask!!.cancelled)
            }
    }

    class DialogTask(mainFilterSvc: MainFilterSvc?, arrayList: ArrayList<Any?>?, index: Int) :
        MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var items: ArrayList<Any?>? = null
        @JvmField
        var index: Int = 0
        @JvmField
        var total: Int = 0
        @JvmField
        var path: String? = null
        @JvmField
        var name: String? = null

        inner class UpdateNextStep(val outer: DialogTask,
                                    val svc: MainFilterSvc
        ) : Runnable {
            override fun run() {
                val dialogTask = this.outer
                val arrayList = dialogTask.items
                this.svc.updateNext(dialogTask.index + 1, arrayList)
            }
        }

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(mainFilterSvc)
            this.dialogRef = weakReference
            if ((weakReference.get() as MainFilterSvc?) != null) {
                this.items = arrayList
                this.index = index
                val childItem: ChildItem? = if (index >= arrayList!!.size) null else (arrayList.get(index) as ChildItem?)
                if (index >= arrayList.size || childItem == null) {
                    return@init
                }
                this.total = arrayList.size
                this.path = childItem.path
                this.name = childItem.name
            }

        }
    }

        public override fun doInBackground() {
            var downloaded: Boolean
            var progressValue: Int = 0
            val mainFilterSvc = this.dialogRef!!.get() as MainFilterSvc?
            if (mainFilterSvc == null || this.cancelled) {
                return
            }
            val builder = mainFilterSvc.notiBuilder
            val indexNow = this.index
            val name = this.name
            val index = if (builder != null) (indexNow + 1) else 0
            if (builder != null && index < (this.total.also { progressValue = it })) {
                try {
                    val now = System.currentTimeMillis()
                    if (now - mainFilterSvc.lastNotiTime >= 1000) {
                        mainFilterSvc.lastNotiTime = now
                        val context = mainFilterSvc.context
                        val notificationManagerP: NotificationManager? = if (context != null) MainApp.Companion.notifications(context) else null
                        if (context != null && notificationManagerP != null) {
                            builder.setContentTitle(name)
                            builder.setContentText(index.toString() + " / " + progressValue)
                            builder.setProgress(progressValue, index, false)
                            if (!mainFilterSvc.notiShown) {
                                mainFilterSvc.notiShown = true
                                val intent =
                                    Intent("ch.opumlabs.opumbrowser.ACTION_FILTER_CANCEL")
                                intent.setPackage(mainFilterSvc.context!!.getPackageName())
                                builder.addAction(
                                    NotificationCompat.Action(
                                        R.drawable.outline_close_black_24,
                                        mainFilterSvc.context!!.getString(
                                            R.string.cancel
                                        ),
                                        PendingIntent.getBroadcast(
                                            mainFilterSvc.context,
                                            11,
                                            intent,
                                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                        )
                                    )
                                )
                            }
                            notificationManagerP.notify(2147483639, builder.build())
                        }
                    }
                } catch (unused: Exception) {
                }
            }
            val path = this.path
            if (URLUtil.isNetworkUrl(path)) {
                val context = mainFilterSvc.context
                val mainFilterDown = mainFilterSvc.filterDown
                if (mainFilterDown == null) {
                    downloaded = false
                } else {
                    val repoUrl = MainUtil.githubRawToRepo(path)
                    downloaded = if (!TextUtils.isEmpty(repoUrl)) mainFilterDown.download(
                        context,
                        repoUrl,
                        path
                    ) else false
                    if (!downloaded) {
                        downloaded = mainFilterDown.download(context, path, path)
                    }
                }
            } else {
                downloaded = true
            }
            if (downloaded) {
                DbBookFilter.addPathItem(mainFilterSvc.context, path, name)
                mainFilterSvc.changed = true
            }
        }

        public override fun onCancelled() {
            val mainFilterSvc = this.dialogRef!!.get() as MainFilterSvc?
            if (mainFilterSvc == null) {
                return
            }
            mainFilterSvc.filterTask = null
            MainUtil.showToast(mainFilterSvc.context, R.string.cancelled)
            mainFilterSvc.finish()
        }

        public override fun onPostExecute() {
            val mainFilterSvc = this.dialogRef!!.get() as MainFilterSvc?
            if (mainFilterSvc != null) {
                mainFilterSvc.filterTask = null
                if (mainFilterSvc.stopped) {
                    MainUtil.showToast(mainFilterSvc.context, R.string.cancelled)
                    mainFilterSvc.finish()
                } else {
                    val handler = mainFilterSvc.mainHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(UpdateNextStep(this, mainFilterSvc))
                }
            }
        }
    }

    inner class EventReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val action = intent.getAction()
                action!!.javaClass
                if (action == "ch.opumlabs.opumbrowser.ACTION_FILTER_CANCEL") {
                    val mainFilterSvc = this@MainFilterSvc
                    if (mainFilterSvc.stopped) {
                        return
                    }
                    mainFilterSvc.stopped = true
                    val dialogTask = mainFilterSvc.filterTask
                    if (dialogTask != null) {
                        dialogTask.cancelled = true
                    }
                    mainFilterSvc.filterTask = null
                    MainUtil.showToast(mainFilterSvc.context, R.string.cancelled)
                    mainFilterSvc.finish()
                }
            }
        }
    }

    init {
        this.deskListener = setDeskListener
        val handler = Handler(Looper.getMainLooper())
        this.mainHandler = handler
        handler.post(ShowListTask())
    }

    fun updateNext(index: Int, arrayList: ArrayList<Any?>?) {
        if (this.context == null) {
            return
        }
        if (this.stopped || arrayList == null || index >= arrayList.size) {
            finish()
            return
        }
        val dialogTask = this.filterTask
        if (dialogTask != null) {
            dialogTask.cancelled = true
        }
        this.filterTask = null
        val filterTask = DialogTask(this, arrayList, index)
        this.filterTask = filterTask
        filterTask.start(this.context)
    }

    fun finish() {
        val handler: Handler? = if (this.deskListener == null) null else this.mainHandler
        if (this.deskListener == null || handler == null) {
            return
        }
        handler.post(ChangedTask())
    }

    fun release() {
        val context = this.context
        if (context == null) {
            return
        }
        val eventReceiver = this.stopReceiver
        if (eventReceiver != null) {
            context.unregisterReceiver(eventReceiver)
            this.stopReceiver = null
        }
        val dialogTask = this.filterTask
        if (dialogTask != null) {
            dialogTask.cancelled = true
        }
        this.filterTask = null
        this.notiBuilder = null
        val contextNow = this.context
        if (contextNow != null) {
            MainApp.Companion.runInBackground(contextNow, NotificationsTask(contextNow))
        }
        this.context = null
        this.deskListener = null
        MainUtil.clearHandler(this.mainHandler)
        this.mainHandler = null
        this.filterDown = null
    }

    fun showNotification() {
        val context: Context? = if (this.notiBuilder != null) null else this.context
        if (this.notiBuilder != null || context == null) {
            return
        }
        try {
            val notificationManagerP: NotificationManager? = MainApp.Companion.notifications(context)
            if (notificationManagerP != null) {
                this.notiShown = false
                val builder = NotificationCompat.Builder(this.context!!, "AdBlockFilter")
                builder.setSmallIcon(R.drawable.outline_verified_user_white_24)
                builder.setContentTitle(
                    this.context!!.getString(R.string.ads_filter) + " " + this.context!!.getString(
                        R.string.update
                    )
                )
                builder.setPriority(1)
                builder.setVisibility(NotificationCompat.VISIBILITY_SECRET)
                builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_FILTER")
                this.notiBuilder = builder
                val notificationB = builder.build()
                notificationB.flags = (notificationB.flags or 32) and (-17)
                notificationManagerP.createNotificationChannel(
                    NotificationChannel(
                        "AdBlockFilter", this.context!!.getString(
                            R.string.ads_block
                        ), NotificationManager.IMPORTANCE_LOW
                    )
                )
                notificationManagerP.notify(2147483639, notificationB)
                this.lastNotiTime = System.currentTimeMillis()
            }
        } catch (unused: Exception) {
        }
    }
}
