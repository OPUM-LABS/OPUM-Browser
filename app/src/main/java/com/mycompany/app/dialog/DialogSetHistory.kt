package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.pref.store.BrowserSettings
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookHistory
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.main.MainActivity
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.browser.SheetButtons
import com.mycompany.app.ui.browser.SheetChoiceRow
import com.mycompany.app.ui.browser.SheetNote
import java.lang.ref.WeakReference

/**
 * History keeping time (history list > settings): "History time" with a pick menu ([timeValues] days: do not keep, 1 day,
 * 1 week, 1 month, 6 months, 1 year, forever) and its explanation; Apply stores it (`mHistoryTime`) and, unless it is
 * "forever", removes the history that is now too old in the background ([DialogTask]; the button turns into Cancel /
 * Canceling meanwhile), then calls [SetHistoryListener.onChanged]. Compose now (on [ComposeBottomDialog]); same constructor,
 * shows itself.
 */
@Suppress("deprecation")
class DialogSetHistory(mainActivity: MainActivity?, setHistoryListener: SetHistoryListener?) : ComposeBottomDialog(mainActivity) {
    @JvmField val mainHandler: Handler? = Handler(Looper.getMainLooper())
    @JvmField var appContext: Context? = context
    @JvmField var historyListener: SetHistoryListener? = setHistoryListener
    @JvmField var deleteTask: DialogTask? = null
    @JvmField var mainBusy: Boolean = false
    @JvmField var historyTime: Int = BrowserSettings.historyTime.value

    private var timeIndex by mutableIntStateOf(timeValues.indexOf(BrowserSettings.historyTime.value).coerceAtLeast(0))
    /** 0 = Apply, 1 = Cancel (running), 2 = Canceling. */
    private var stage by mutableIntStateOf(0)
    private var busy by mutableStateOf(false)

    init {
        setSheet {
            SheetChoiceRow(stringResource(AppR.string.history_time), timeLabels.toList(), timeIndex, { pick(it) }, enabled = stage == 0)
            SheetNote(stringResource(timeInfos[timeIndex]))
            if (busy) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            }
            SheetButtons(
                { onButton() },
                mainLabel = when (stage) { 1 -> AppR.string.cancel; 2 -> AppR.string.canceling; else -> AppR.string.apply },
                mainEnabled = stage != 2,
            )
        }
        show()
    }

    override fun canCancelFromScrim(): Boolean = stage == 0

    /** Time picked. */
    private fun pick(index: Int) {
        val newTimeIndex = index % 7
        if (timeIndex != newTimeIndex) {
            timeIndex = newTimeIndex
            historyTime = timeValues[newTimeIndex]
        }
    }

    /** Apply / Cancel. */
    private fun onButton() {
        if (stage == 1) {
            cancelDelete()
            return
        }
        if (mainBusy) return
        mainBusy = true
        mainHandler?.post {
            val listener = historyListener
            if (listener != null) {
                val historyTimeNow = historyTime
                if (BrowserSettings.historyTime.value == historyTimeNow) {
                    listener.onChanged(false)
                } else {
                    BrowserSettings.edit(appContext) { set(BrowserSettings.historyTime, historyTimeNow) }
                    if (BrowserSettings.historyTime.value == -1) {
                        listener.onChanged(false)
                    } else {
                        setCanceledOnTouchOutside(false)
                        stage = 1
                        deleteTask?.cancelled = true
                        deleteTask = null
                        val task = DialogTask(this)
                        deleteTask = task
                        task.start(appContext)
                    }
                }
            }
            mainBusy = false
        }
    }

    class DialogTask(dialogSetHistory: DialogSetHistory?) : MyAsyncTask() {
        @JvmField var dialogRef: WeakReference<*>? = WeakReference<Any?>(dialogSetHistory)

        init {
            dialogSetHistory?.busy = true
        }

        public override fun doInBackground() {
            val d = this.dialogRef!!.get() as DialogSetHistory?
            if (d == null || this.cancelled) return
            if (BrowserSettings.historyTime.value == 0) DbBookHistory.clearAll(d.appContext) else DbBookHistory.clearOld(d.appContext)
            DataBookHistory.instance(d.appContext).clear()
        }

        public override fun onCancelled() = done()

        public override fun onPostExecute() = done()

        private fun done() {
            val d = this.dialogRef!!.get() as DialogSetHistory? ?: return
            d.deleteTask = null
            d.busy = false
            d.historyListener?.onChanged(true)
        }
    }

    interface SetHistoryListener {
        fun onChanged(changed: Boolean)
    }

    /** Cancel while removing (or close). */
    fun cancelDelete() {
        if (deleteTask == null) {
            dismiss()
            return
        }
        busy = true
        stage = 2
        deleteTask?.cancelled = true
        deleteTask = null
    }

    override fun cancel() {
        cancelDelete()
    }

    override fun dismiss() {
        deleteTask?.cancelled = true
        deleteTask = null
        appContext = null
        historyListener = null
        super.dismiss()
    }

    companion object {
        @JvmField
        val timeValues: IntArray = intArrayOf(0, 1, 7, 30, 183, 365, -1)
        @JvmField
        val timeLabels: IntArray = intArrayOf(
            AppR.string.history_zero,
            AppR.string.history_1day,
            AppR.string.history_1week,
            AppR.string.history_1mon,
            AppR.string.history_6mon,
            AppR.string.history_1year,
            AppR.string.history_none
        )
        @JvmField
        val timeInfos: IntArray

        init {
            val i = AppR.string.history_info_zero
            val i2 = AppR.string.history_info_limit
            timeInfos = intArrayOf(i, i2, i2, i2, i2, i2, AppR.string.history_info_none)
        }
    }
}
