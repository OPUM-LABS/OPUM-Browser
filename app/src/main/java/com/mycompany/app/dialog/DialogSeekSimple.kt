package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppSlider
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.ui.browser.ComposeBottomDialog
import kotlin.math.roundToInt

/**
 * One whole-number setting on a slider with -/+ and Apply (and Reset for most). [kind] is the setting: 0 swipe sensitivity
 * (10-200 %), 1 multi (x2-30, warns above 20), 2 magnification (x1-9, warns above 5), 3 image brightness (20-100 %, with a
 * preview picture), 4 retries, 5 download limit, 6 time limit (hours), 7 recent searches, 8 recent records, 9 / 10 / 11
 * open and memory limits (Apply only), 12 memory retention (days); [initialValue] is the current value. Apply (or Reset, which goes
 * back to the kind's default) reports the value to the listener; closing otherwise changes nothing. Compose now
 * (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogSeekSimple(
    activity: Activity?,
    kind: Int,
    initialValue: Int,
    dialogSeekListener: DialogSeekListener?,
) : ComposeBottomDialog(activity) {
    private var listener: DialogSeekListener? = dialogSeekListener
    private val seekKind = kind

    private val min: Int
    private val max: Int
    /** Above this value kinds 1 and 2 turn red (heavy on resources). */
    private val warnAbove: Int
    private val suffix: String

    private var value by mutableIntStateOf(0)
    private var resetAsk by mutableStateOf(false)

    init {
        when (kind) {
            0 -> { min = 10; max = 200; warnAbove = Int.MAX_VALUE; suffix = "" }
            1 -> { min = 2; max = 30; warnAbove = 20; suffix = "" }
            2 -> { min = 1; max = 9; warnAbove = 5; suffix = "" }
            3 -> { min = 20; max = 100; warnAbove = Int.MAX_VALUE; suffix = "" }
            4 -> { min = 0; max = 100; warnAbove = Int.MAX_VALUE; suffix = "" }
            5 -> { min = 2; max = 100; warnAbove = Int.MAX_VALUE; suffix = "" }
            6 -> { min = 1; max = 5; warnAbove = Int.MAX_VALUE; suffix = " " + context.getString(R.string.time_hour) }
            7 -> { min = 1; max = 100; warnAbove = Int.MAX_VALUE; suffix = "" }
            8 -> { min = 1; max = 50; warnAbove = Int.MAX_VALUE; suffix = "" }
            12 -> { min = 1; max = 100; warnAbove = Int.MAX_VALUE; suffix = " " + context.getString(R.string.time_day) }
            else -> { min = 3; max = 100; warnAbove = Int.MAX_VALUE; suffix = "" }
        }
        value = initialValue.coerceIn(min, max)
        setSheet {
            val warn = value > warnAbove
            val warnColor = Color(-769226)
            Column3(warn, warnColor)
        }
        show()
    }

    private fun defaultValue(): Int = when (seekKind) {
        0 -> 100
        1 -> 10
        2 -> 1
        3 -> 70
        4 -> 10
        5, 6 -> 2
        7 -> 20
        8, 9, 10, 11 -> 5
        12 -> 2
        else -> min
    }

    private fun change(newValue: Int) {
        value = newValue.coerceIn(min, max)
    }

    private fun shown(v: Int): String = when (seekKind) {
        0, 3 -> "$v%"
        1, 2 -> "x$v"
        6, 12 -> "$v$suffix"
        else -> v.toString()
    }

    @androidx.compose.runtime.Composable
    private fun androidx.compose.foundation.layout.ColumnScope.Column3(warn: Boolean, warnColor: Color) {
        androidx.compose.foundation.layout.Column(Modifier.verticalScroll(rememberScrollState())) {
            if (seekKind == 1 || seekKind == 2) {
                Text(
                    stringResource(R.string.fast_down_guide) + "\n" + stringResource(R.string.dark_mode_info_2),
                    color = if (warn) warnColor else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                )
            }
            if (seekKind == 3) {
                val scale = value / 100f
                val matrix = ColorMatrix(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, 1f,
                        0f, scale, 0f, 0f, 1f,
                        0f, 0f, scale, 0f, 1f,
                        0f, 0f, 0f, 1f, 0f,
                    ),
                )
                Image(
                    painterResource(R.drawable.preview_matterhorn), contentDescription = null, contentScale = ContentScale.FillWidth,
                    colorFilter = ColorFilter.colorMatrix(matrix),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(12.dp)),
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(title()), style = MaterialTheme.typography.titleMedium)
                Text(shown(value), style = MaterialTheme.typography.titleMedium, color = if (warn) warnColor else MaterialTheme.colorScheme.onSurface)
            }
            Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { change(value - 1) }) { Icon(painterResource(R.drawable.outline_remove_black_24), contentDescription = null) }
                AppSlider(
                    value = value.toFloat(), onValueChange = { change(it.roundToInt()) },
                    valueRange = min.toFloat()..max.toFloat(), modifier = Modifier.weight(1f),
                    colors = if (warn) SliderDefaults.colors(thumbColor = warnColor, activeTrackColor = warnColor) else SliderDefaults.colors(),
                )
                IconButton(onClick = { change(value + 1) }) { Icon(painterResource(R.drawable.outline_add_black_24), contentDescription = null) }
            }
            if (seekKind == 2) {
                Text(
                    stringResource(R.string.ocr_zoom_info_1) + "\n" + stringResource(R.string.ocr_zoom_info_2),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                if (seekKind < 9 || seekKind > 12) TextButton(onClick = { resetAsk = true }) { Text(stringResource(R.string.reset)) }
                TextButton(onClick = { listener?.onValue(value); dismiss() }) { Text(stringResource(R.string.apply)) }
            }
            if (resetAsk) {
                AlertDialog(
                    onDismissRequest = { resetAsk = false },
                    text = { Text(stringResource(R.string.reset_setting)) },
                    confirmButton = {
                        TextButton(colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error), onClick = {
                            resetAsk = false
                            value = defaultValue().coerceIn(min, max)
                            listener?.onValue(value)
                        }) { Text(stringResource(R.string.reset)) }
                    },
                    dismissButton = { TextButton(onClick = { resetAsk = false }) { Text(stringResource(R.string.cancel)) } },
                )
            }
        }
    }

    private fun title(): Int = when (seekKind) {
        0 -> R.string.swipe_sense
        1 -> R.string.multi
        2 -> R.string.zoom_title
        3 -> R.string.img_bright
        4 -> R.string.retry_count
        5 -> R.string.down_limit
        6 -> R.string.time_limit
        7 -> R.string.recent_search
        8 -> R.string.recent_lang
        10 -> R.string.open_limit
        12 -> R.string.mem_keep
        else -> R.string.mem_limit
    }

    override fun dismiss() {
        listener = null
        super.dismiss()
    }

    companion object {
    }
}
