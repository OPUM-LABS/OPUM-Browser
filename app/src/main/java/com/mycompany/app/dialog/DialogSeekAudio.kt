package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppSlider
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.browser.ComposeBottomDialog
import kotlin.math.roundToInt

/**
 * The media volume slider (with -1 / +1 buttons). It follows the hardware volume keys while open and changes the system
 * volume live; closing reports the final volume to the listener when it differs from the one at opening. No scrim, so
 * the video behind stays visible. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogSeekAudio(activity: Activity?, dialogSeekListener: DialogSeekListener?) : ComposeBottomDialog(activity) {
    interface DialogSeekListener {
        fun onValue(newValue: Int)
    }

    private var listener: DialogSeekListener? = dialogSeekListener
    private val audio = context.getSystemService("audio") as AudioManager?
    private val max = audio?.getStreamMaxVolume(3) ?: 0
    private val start = audio?.getStreamVolume(3) ?: 0
    private var value by mutableIntStateOf(start)
    private var receiver: BroadcastReceiver? = null
    private val appContext: Context = context

    override val scrimAlpha: Float get() = 0f

    private fun change(newValue: Int) {
        val v = newValue.coerceIn(0, max)
        if (v == value) return
        value = v
        audio?.setStreamVolume(3, v, 0)
    }

    init {
        setSheet {
            Row(
                Modifier.fillMaxWidth().padding(top = 20.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.volume), style = MaterialTheme.typography.titleMedium)
                Text(value.toString(), style = MaterialTheme.typography.titleMedium)
            }
            Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { change(value - 1) }) { Icon(painterResource(R.drawable.outline_remove_black_24), contentDescription = null) }
                AppSlider(
                    value = value.toFloat(), onValueChange = { change(it.roundToInt()) },
                    valueRange = 0f..max.toFloat().coerceAtLeast(1f), steps = (max - 1).coerceAtLeast(0), modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { change(value + 1) }) { Icon(painterResource(R.drawable.outline_add_black_24), contentDescription = null) }
            }
        }
        val r = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val current = audio?.getStreamVolume(3) ?: return
                value = current.coerceIn(0, max)
            }
        }
        receiver = r
        val filter = IntentFilter()
        filter.addAction("android.media.VOLUME_CHANGED_ACTION")
        filter.addAction("android.intent.action.HEADSET_PLUG")
        ContextCompat.registerReceiver(appContext, r, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        show()
    }

    override fun dismiss() {
        val listenerNow = listener
        listener = null
        if (listenerNow != null && start != value) listenerNow.onValue(value)
        receiver?.let {
            try { appContext.unregisterReceiver(it) } catch (unused: Exception) { }
        }
        receiver = null
        super.dismiss()
    }

    companion object {
    }
}
