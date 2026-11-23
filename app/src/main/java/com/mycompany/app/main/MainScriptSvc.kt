package com.mycompany.app.main

import com.mycompany.app.dialog.DialogSetDesk.SetDeskListener
import com.mycompany.app.db.book.DbBookScript
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
import com.mycompany.app.db.book.DbBookAlbum.Companion.cached; import com.mycompany.app.db.book.DbBookAlbum.Companion.get
import com.mycompany.app.db.book.DbBookCmp.Companion.cached; import com.mycompany.app.db.book.DbBookCmp.Companion.get
import com.mycompany.app.db.book.DbBookLocale.Companion.cached; import com.mycompany.app.db.book.DbBookLocale.Companion.get
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookPdf.Companion.cached; import com.mycompany.app.db.book.DbBookPdf.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.saveScript
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.db.book.DbRecentLang.Companion.get
import com.mycompany.app.down.DownParseDzen.segmentUrls
import com.mycompany.app.down.DownParseKakao.parseItemText
import com.mycompany.app.down.DownParseReddit.parseRedditItem
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefUtil.showDetail
import com.mycompany.app.pref.PrefUtil.singleLine
import com.mycompany.app.pref.PrefUtil.foldMode
import com.mycompany.app.script.Script.Companion.parse
import com.mycompany.app.script.UnicodeReader
import ch.opumlabs.opumbrowser.R
import java.io.InputStream
import java.lang.ref.WeakReference
import java.util.Locale

@Suppress("deprecation")
class MainScriptSvc(var context: Context?, setDeskListener: SetDeskListener?) {
    @JvmField
    var deskListener: SetDeskListener?
    @JvmField
    var mainHandler: Handler?
    @JvmField
    var scriptTask: DialogTask? = null
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
    var scriptLang: String? = null
    @JvmField
    var scriptLocale: String? = null

    internal inner class ShowListTask : Runnable {

        internal inner class ShowNotificationTask : Runnable {
            override fun run() {
                var cursorG: Cursor?
                val mainScriptSvc = this@MainScriptSvc
                mainScriptSvc.showNotification()
                val context = mainScriptSvc.context
                if (context != null) {
                    var arrayList: ArrayList<Any?>? = null
                    try {
                        cursorG = DbUtil.query(
                            DbBookScript.get(context).getWritableDatabase(),
                            "DbBookScript_table",
                            arrayOf<String?>("_downurl", "_name"),
                            "_enabled=?",
                            arrayOf<String?>("1"),
                            null
                        )
                        if (cursorG != null) {
                            try {
                                if (cursorG.moveToFirst()) {
                                    val columnIndex = cursorG.getColumnIndex("_downurl")
                                    val nameCol = cursorG.getColumnIndex("_name")
                                    do {
                                        val path = cursorG.getString(columnIndex)
                                        if (!TextUtils.isEmpty(path)) {
                                            val childItem = ChildItem()
                                            childItem.path = path
                                            childItem.name = cursorG.getString(nameCol)
                                            if (arrayList == null) {
                                                arrayList = ArrayList<Any?>()
                                            }
                                            arrayList.add(childItem)
                                        }
                                        if (mainScriptSvc.stopped) {
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
                    if (mainScriptSvc.context != null) {
                        mainScriptSvc.updateNext(0, arrayList)
                    }
                }
            }
        }

        override fun run() {
            val mainScriptSvc = this@MainScriptSvc
            if (mainScriptSvc.context != null && mainScriptSvc.stopReceiver == null) {
                mainScriptSvc.stopReceiver = mainScriptSvc.EventReceiver()
                val intentFilter = IntentFilter()
                intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_SCRIPT_CANCEL")
                ContextCompat.registerReceiver(mainScriptSvc.context!!, mainScriptSvc.stopReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
            }
            MainApp.Companion.runInBackground(mainScriptSvc.context, ShowNotificationTask())
        }
    }

    internal inner class ChangedTask : Runnable {
        override fun run() {
            val mainScriptSvc = this@MainScriptSvc
            val setDeskListener: SetDeskListener? = mainScriptSvc.deskListener
            mainScriptSvc.deskListener = null
            if (setDeskListener == null) {
                return
            }
            setDeskListener.onChanged(mainScriptSvc.changed && !mainScriptSvc.stopped)
        }
    }

    internal inner class NotificationsTask(val context: Context?) : Runnable {
        override fun run() {
            val notificationManagerP: NotificationManager? = MainApp.Companion.notifications(this.context)
            if (notificationManagerP != null) {
                notificationManagerP.cancel(2147483637)
            }
        }
    }

    class DialogTask(mainScriptSvc: MainScriptSvc?, arrayList: ArrayList<Any?>?, index: Int) :
        MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var items: ArrayList<Any?>? = null
        @JvmField
        var childItem: ChildItem? = null
        @JvmField
        var index: Int = 0
        @JvmField
        var total: Int = 0
        @JvmField
        var source: String? = null
        @JvmField
        var namespace: String? = null

        inner class UpdateNextStep(val outer: DialogTask,
                                    val svc: MainScriptSvc
        ) : Runnable {
            override fun run() {
                val dialogTask = this.outer
                val arrayList = dialogTask.items
                this.svc.updateNext(dialogTask.index + 1, arrayList)
            }
        }

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(mainScriptSvc)
            this.dialogRef = weakReference
            if ((weakReference.get() as MainScriptSvc?) != null) {
                this.items = arrayList
                this.index = index
                if (index < arrayList!!.size) {
                    val childItem = arrayList.get(index) as ChildItem?
                    this.childItem = childItem
                    if (childItem == null) {
                        return@init
                    }
                    this.total = arrayList.size
                    this.source = childItem.path
                    this.namespace = childItem.name
                }
            }

        }
    }

        /**
         * Updates one script (by source url and name) for the background update service: refreshes the progress
         * notification at most once a second (adding a Cancel action once), downloads or reads the source, parses
         * and saves it, reporting success; the service notes that something changed.
         */
        public override fun doInBackground() {
            val svc = this.dialogRef!!.get() as MainScriptSvc?
            if (svc == null || this.cancelled || this.childItem == null) {
                return
            }
            val builder = svc.notiBuilder
            if (builder != null) {
                val done = this.index + 1
                val total = this.total
                if (done < total) {
                    val now = System.currentTimeMillis()
                    if (now - svc.lastNotiTime >= 1000) {
                        svc.lastNotiTime = now
                        try {
                            val manager: NotificationManager? =
                                if (svc.context == null) null else MainApp.Companion.notifications(svc.context)
                            if (manager != null) {
                                builder.setContentTitle(this.namespace)
                                builder.setContentText(
                                    done.toString() + " / " + total)
                                builder.setProgress(total, done, false)
                                if (!svc.notiShown) {
                                    svc.notiShown = true
                                    val intent =
                                        Intent("ch.opumlabs.opumbrowser.ACTION_SCRIPT_CANCEL")
                                    intent.setPackage(svc.context!!.getPackageName())
                                    val cancel =
                                        PendingIntent.getBroadcast(svc.context, 12, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                                    builder.addAction(
                                        NotificationCompat.Action(
                                            R.drawable.outline_close_black_24,
                                            svc.context!!.getString(R.string.cancel), cancel
                                        )
                                    )
                                }
                                manager.notify(2147483637, builder.build())
                            }
                        } catch (ignored: Exception) {
                        }
                    }
                }
            }
            val source = this.source!!
            if (source.startsWith("userscript://")) {
                svc.changed = true
                return
            }
            val context = svc.context
            var sourceText: String? = null
            if (URLUtil.isNetworkUrl(source)) {
                val conn = if (context == null) null else MainUtil.openBrowserConnection(context, source)
                if (conn != null) {
                    var `in`: InputStream? = null
                    var reader: UnicodeReader? = null
                    try {
                        conn.setDoInput(true)
                        conn.connect()
                        `in` = conn.getInputStream()
                        reader = UnicodeReader(`in`, conn.getContentEncoding())
                        val chars = CharArray(4096)
                        var sb: StringBuilder? = null
                        var read: Int
                        while ((reader.reader!!.read(chars, 0, 4096).also { read = it }) != -1) {
                            if (read > 0) {
                                if (sb == null) {
                                    sb = StringBuilder()
                                }
                                sb.append(chars, 0, read)
                            }
                        }
                        sourceText = if (sb == null) null else sb.toString()
                    } catch (exception: Exception) {
                        sourceText = null
                    }
                    try {
                        if (reader != null) {
                            reader.close()
                        }
                    } catch (ignored: Exception) {
                    }
                    try {
                        if (`in` != null) {
                            `in`.close()
                        }
                    } catch (ignored: Exception) {
                    }
                    conn.disconnect()
                }
            } else {
                try {
                    sourceText = if (TextUtils.isEmpty(source)) null else MainUtil.readTextFile(
                        context,
                        MainUtil.openUriInput(context, source),
                        false
                    )
                } catch (exception: Exception) {
                    sourceText = null
                }
            }
            if (TextUtils.isEmpty(sourceText)) {
                return
            }
            if (TextUtils.isEmpty(svc.scriptLang)) {
                val locale: Locale = MainApp.Companion.currentLocale()
                var language = locale.getLanguage()
                if (!TextUtils.isEmpty(language)) {
                    language = language.lowercase()
                    val country = locale.getCountry()
                    svc.scriptLang = language
                    svc.scriptLocale =
                        if (TextUtils.isEmpty(country)) null else language + "-" + country.lowercase()
                }
            }
            val script = parse(context, this.source, this.namespace, sourceText, svc.scriptLang, svc.scriptLocale, false)
            if (script == null) {
                return
            }
            if (DbBookScript.saveScript(context, script, this.childItem, svc.scriptLang, svc.scriptLocale) == null) {
                return
            }
            svc.changed = true
        }

        public override fun onCancelled() {
            val mainScriptSvc = this.dialogRef!!.get() as MainScriptSvc?
            if (mainScriptSvc == null) {
                return
            }
            mainScriptSvc.scriptTask = null
            MainUtil.showToast(mainScriptSvc.context, R.string.cancelled)
            mainScriptSvc.finish()
        }

        public override fun onPostExecute() {
            val mainScriptSvc = this.dialogRef!!.get() as MainScriptSvc?
            if (mainScriptSvc != null) {
                mainScriptSvc.scriptTask = null
                if (mainScriptSvc.stopped) {
                    MainUtil.showToast(mainScriptSvc.context, R.string.cancelled)
                    mainScriptSvc.finish()
                } else {
                    val handler = mainScriptSvc.mainHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(UpdateNextStep(this, mainScriptSvc))
                }
            }
        }
    }

    inner class EventReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val action = intent.getAction()
                action!!.javaClass
                if (action == "ch.opumlabs.opumbrowser.ACTION_SCRIPT_CANCEL") {
                    val mainScriptSvc = this@MainScriptSvc
                    if (mainScriptSvc.stopped) {
                        return
                    }
                    mainScriptSvc.stopped = true
                    val dialogTask = mainScriptSvc.scriptTask
                    if (dialogTask != null) {
                        dialogTask.cancelled = true
                    }
                    mainScriptSvc.scriptTask = null
                    MainUtil.showToast(mainScriptSvc.context, R.string.cancelled)
                    mainScriptSvc.finish()
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
        val dialogTask = this.scriptTask
        if (dialogTask != null) {
            dialogTask.cancelled = true
        }
        this.scriptTask = null
        val scriptTask = DialogTask(this, arrayList, index)
        this.scriptTask = scriptTask
        scriptTask.start(this.context)
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
        val dialogTask = this.scriptTask
        if (dialogTask != null) {
            dialogTask.cancelled = true
        }
        this.scriptTask = null
        this.notiBuilder = null
        val contextNow = this.context
        if (contextNow != null) {
            MainApp.Companion.runInBackground(contextNow, NotificationsTask(contextNow))
        }
        this.context = null
        this.deskListener = null
        MainUtil.clearHandler(this.mainHandler)
        this.mainHandler = null
        this.scriptLang = null
        this.scriptLocale = null
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
                val builder = NotificationCompat.Builder(this.context!!, "UserScript")
                builder.setSmallIcon(R.drawable.outline_extension_dark_24)
                builder.setContentTitle(
                    this.context!!.getString(R.string.user_script_title) + " " + this.context!!.getString(
                        R.string.update
                    )
                )
                builder.setPriority(1)
                builder.setVisibility(NotificationCompat.VISIBILITY_SECRET)
                builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_SCRIPT")
                this.notiBuilder = builder
                val notificationB = builder.build()
                notificationB.flags = (notificationB.flags or 32) and (-17)
                notificationManagerP.createNotificationChannel(
                    NotificationChannel(
                        "UserScript", this.context!!.getString(
                            R.string.ads_block
                        ), NotificationManager.IMPORTANCE_LOW
                    )
                )
                notificationManagerP.notify(2147483637, notificationB)
                this.lastNotiTime = System.currentTimeMillis()
            }
        } catch (unused: Exception) {
        }
    }
}
