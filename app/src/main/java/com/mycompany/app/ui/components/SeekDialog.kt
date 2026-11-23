package com.mycompany.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R

/**
 * What a seek dialog edits. This is the table of the old `DialogSeekSimple` modes (same ranges, units, defaults and
 * warnings), so a screen can swap the legacy dialog for [SeekDialog] by passing the same mode number.
 */
enum class SeekMode(
    val id: Int,
    val titleRes: Int,
    val min: Int,
    val max: Int,
    val default: Int,
    val unit: Unit = Unit.NONE,
    /** Values above this are drawn as a warning (resource heavy), or null. */
    val warnAbove: Int? = null,
) {
    SENSITIVITY(0, R.string.swipe_sense, 10, 200, 100, Unit.PERCENT),
    MULTI(1, R.string.multi, 2, 30, 10, Unit.TIMES, warnAbove = 20),
    ZOOM(2, R.string.zoom_title, 1, 9, 1, Unit.TIMES, warnAbove = 5),
    IMAGE_BRIGHT(3, R.string.img_bright, 20, 100, 70, Unit.PERCENT),
    RETRY(4, R.string.retry_count, 0, 100, 10),
    DOWN_LIMIT(5, R.string.down_limit, 2, 100, 2),
    TIME_LIMIT(6, R.string.time_limit, 1, 5, 2, Unit.HOURS),
    RECENT_SEARCH(7, R.string.recent_search, 1, 100, 20),
    RECENT_LANG(8, R.string.recent_lang, 1, 50, 5),
    PAGE_MEMORY(9, R.string.mem_limit, 3, 100, 5),
    OPEN_LIMIT(10, R.string.open_limit, 3, 100, 5),
    TAB_MEMORY(11, R.string.mem_limit, 3, 100, 5),
    KEEP_DAYS(12, R.string.mem_keep, 1, 100, 2, Unit.DAYS);

    enum class Unit { NONE, PERCENT, TIMES, HOURS, DAYS }

    companion object {
        fun of(id: Int): SeekMode = entries.firstOrNull { it.id == id } ?: PAGE_MEMORY
    }
}

/**
 * Material 3 replacement of `DialogSeekSimple`: a title, the current value, a slider with minus / plus buttons, the
 * explanations of the mode, and Reset / Apply. [onApply] gets the chosen value.
 */
@Composable
fun SeekDialog(mode: SeekMode, value: Int, onApply: (Int) -> Unit, onDismiss: () -> Unit) {
    var current by remember { mutableIntStateOf(value.coerceIn(mode.min, mode.max)) }
    val warn = mode.warnAbove?.let { current > it } == true
    val hours = stringResource(R.string.time_hour)
    val days = stringResource(R.string.time_day)
    val label = when (mode.unit) {
        SeekMode.Unit.PERCENT -> "$current%"
        SeekMode.Unit.TIMES -> "x$current"
        SeekMode.Unit.HOURS -> "$current $hours"
        SeekMode.Unit.DAYS -> "$current $days"
        SeekMode.Unit.NONE -> current.toString()
    }
    // a sheet since 2026-11-20 (was a centred dialog): same content, Reset / Apply at the bottom right
    OpumSheet(
        onDismiss = onDismiss,
        title = stringResource(mode.titleRes),
        actions = {
            TextButton(onClick = { current = mode.default }) { Text(stringResource(R.string.reset)) }
            TextButton(onClick = { onApply(current); close() }) { Text(stringResource(R.string.apply)) }
        },
    ) {
        Box(Modifier.padding(horizontal = 24.dp)) {
            Column {
                if (mode == SeekMode.IMAGE_BRIGHT) {
                    // sample picture shown with the chosen brightness, like the old dialog
                    val scale = current / 100f
                    val matrix = ColorMatrix(
                        floatArrayOf(
                            scale, 0f, 0f, 0f, 0f,
                            0f, scale, 0f, 0f, 0f,
                            0f, 0f, scale, 0f, 0f,
                            0f, 0f, 0f, 1f, 0f,
                        )
                    )
                    Image(
                        painter = painterResource(R.drawable.preview_matterhorn),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.colorMatrix(matrix),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                }
                Text(
                    label,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (warn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (current > mode.min) current-- }) {
                        Icon(painterResource(R.drawable.outline_remove_black_24), contentDescription = null)
                    }
                    AppSlider(
                        value = current.toFloat(),
                        onValueChange = { current = it.toInt().coerceIn(mode.min, mode.max) },
                        valueRange = mode.min.toFloat()..mode.max.toFloat(),
                        steps = if (mode.max - mode.min <= 20) mode.max - mode.min - 1 else 0,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { if (current < mode.max) current++ }) {
                        Icon(painterResource(R.drawable.outline_add_black_24), contentDescription = null)
                    }
                }
                if (mode == SeekMode.MULTI || mode == SeekMode.ZOOM) {
                    Text(
                        stringResource(R.string.fast_down_guide) + "\n" + stringResource(R.string.dark_mode_info_2),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (warn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (mode == SeekMode.ZOOM) {
                    Text(
                        stringResource(R.string.ocr_zoom_info_1) + "\n" + stringResource(R.string.ocr_zoom_info_2),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

/** Holds the request of a seek dialog so a screen can open one from a click handler: see [SeekDialogHost]. */
@Stable
class SeekDialogState {
    internal var request by mutableStateOf<Request?>(null)
        private set

    internal class Request(val mode: SeekMode, val value: Int, val onApply: (Int) -> Unit)

    /** Opens the dialog for the legacy mode number [mode] with [value]; [onApply] receives the chosen value. */
    fun show(mode: Int, value: Int, onApply: (Int) -> Unit) {
        request = Request(SeekMode.of(mode), value, onApply)
    }

    internal fun close() { request = null }
}

/** Put this once in the screen; it shows the dialog that [SeekDialogState.show] asked for. */
@Composable
fun SeekDialogHost(state: SeekDialogState) {
    val request = state.request ?: return
    SeekDialog(
        mode = request.mode,
        value = request.value,
        onApply = request.onApply,
        onDismiss = { state.close() },
    )
}
