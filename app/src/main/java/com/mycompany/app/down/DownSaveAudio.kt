package com.mycompany.app.down

import android.content.Context
import com.mycompany.app.pref.store.ZoneSettings
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainDownSvc.DownItem
import com.mycompany.app.main.MainDownSvc.DownSaveListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefZone

@Suppress("deprecation")
object DownSaveAudio {
    /** Downloads the separate audio stream of a type-15 item into `<temp>/<0>a`.  */
    @JvmStatic
    fun saveAudio(
        context: Context?, item: DownItem?, url: String?, wait: Int, retries: Int, resumed: Boolean,
        listener: DownSaveListener
    ) {
        if (item == null) {
            return
        }
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(item.path)) {
            if (item.status == 1) {
                listener.onStarted(item)
            }
            return
        }
        val path = StringBuilder().append(item.segmentDir).append("/").append(MainUtil.zeroPadded(0, 1)).append("a").toString()
        val attempt = DownSavePath.transfer(
            context,
            item,
            url,
            path,
            0,
            item.audioLength,
            true,
            wait,
            resumed,
            listener
        )
        if (attempt == null) {
            return
        }
        if (attempt.noConnection) {
            if (item.status == 1) {
                fetchAudio(context, item, url, 0, retries, null, false, listener)
            }
            return
        }
        if (item.status != 1) {
            return
        }
        if (attempt.permanent) {
            listener.onStarted(item)
            return
        }
        if (attempt.retry) {
            fetchAudio(
                context,
                item,
                url,
                attempt.wait,
                if (attempt.progressed) 0 else retries,
                attempt.location,
                attempt.progressed,
                listener,
                attempt.retryAfterMs,
            )
        } else {
            listener.onFailed(item)
        }
    }

    /** Retry scheduling for the audio stream; same rules as [DownSavePath.retryDelay].  */
    @JvmStatic
    fun fetchAudio(
        context: Context?,
        item: DownItem?,
        url: String?,
        wait: Int,
        retries: Int,
        location: String?,
        progressed: Boolean,
        listener: DownSaveListener,
        retryAfterMs: Long = 0,
    ) {
        var retries = retries
        if (item == null) {
            return
        }
        val delay: Long
        var next = url
        if (wait > 0) {
            if (wait >= DownSavePath.MAX_BUSY_TRIES) {
                listener.onPaused(item)
                return
            }
            delay = DownSavePath.busyDelay(wait, retryAfterMs)
        } else if (!TextUtils.isEmpty(location)) {
            delay = 0
            next = location
        } else if (!progressed && !TextUtils.isEmpty(item.referer)) {
            item.referer = null
            delay = 0
        } else if (retries >= ZoneSettings.retryDown.value) {
            listener.onStarted(item)
            return
        } else {
            retries++
            delay = DownSavePath.retryDelay(retries)
        }
        try {
            if (delay == 0L) {
                DownPool.execute(SaveStep(wait, retries, context, item, listener, next))
                return
            }
            val handler = listener.uiHandler()
            if (handler == null) {
                listener.onStarted(item)
                return
            }
            handler.postDelayed(
                StartedTask(wait, retries, context, item, listener, next),
                delay
            )
        } catch (unused: OutOfMemoryError) {
            item.outOfMemory = true
            listener.onStarted(item)
        }
    }

    internal class StartedTask(@JvmField val wait: Int,
                                   @JvmField val retries: Int,
                                   @JvmField val context: Context?,
                                   @JvmField val downItem: DownItem?,
                                   @JvmField val downSaveListener: DownSaveListener,
                                   @JvmField val sub: String?
    ) : Runnable {
        override fun run() {
            val context = this.context
            val sub = this.sub
            val wait = this.wait
            val retries = this.retries
            val downSaveListener = this.downSaveListener
            val downItem = this.downItem
            if (downItem != null) {
                try {
                    DownPool.execute(SaveStep(wait, retries, context, downItem, downSaveListener, sub))
                } catch (unused: OutOfMemoryError) {
                    downItem.outOfMemory = true
                    downSaveListener.onStarted(downItem)
                }
            }
        }
    }

    internal class SaveStep(@JvmField val wait: Int,
                                   @JvmField val retries: Int,
                                   @JvmField val context: Context?,
                                   @JvmField val downItem: DownItem,
                                   @JvmField val downSaveListener: DownSaveListener,
                                   @JvmField val sub: String?
    ) : Runnable {
        override fun run() {
            val downSaveListener = this.downSaveListener
            val downItem = this.downItem
            if (downItem.status != 1) {
                return
            }
            try {
                saveAudio(this.context, downItem, this.sub, this.wait, this.retries, true, downSaveListener)
            } catch (unused: OutOfMemoryError) {
                downItem.outOfMemory = true
                downSaveListener.onStarted(downItem)
            }
        }
    }
}
