package com.mycompany.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.browser.MenuCatalog
import kotlin.math.roundToInt

/** Zone key of the action tray (the list under the preview): dropping a placed item there removes it. */
internal const val TRAY = "tray"

/** What is being dragged: an action [id] from [zone] (null for something that is not an item, e.g. a whole bar) at [index]. */
internal class DragItem(val id: Int, val zone: String, val index: Int)

/**
 * Drag and drop between the zones of an editor (bars, the menu grid, the tray), 2026-11-19. Zones and their item slots
 * report their bounds in root coordinates; the dragged item follows the finger as a floating copy ([DragGhost]); the
 * lists are only changed on drop ([onDrop] gets the item, the zone under the finger or null, and the insertion index),
 * so nothing moves under the finger while dragging.
 */
internal class DndState {
    var drag by mutableStateOf<DragItem?>(null)
        private set
    var pointer by mutableStateOf(Offset.Zero)
        private set
    private val zones = mutableStateMapOf<String, Rect>()
    private val slots = HashMap<String, HashMap<Int, Rect>>()
    var onDrop: (DragItem, String?, Int) -> Unit = { _, _, _ -> }
    /** Whether the item may be dropped onto the slot ([zone], index) to join it (button groups); off by default. */
    var mergeable: (DragItem, String, Int) -> Boolean = { _, _, _ -> false }

    fun start(item: DragItem, at: Offset) { drag = item; pointer = at }
    fun move(by: Offset) { pointer += by }
    fun cancel() { drag = null }
    fun end() {
        val item = drag ?: return
        val zone = zoneAt(pointer)
        drag = null
        onDrop(item, zone, if (zone == null) -1 else indexIn(zone, pointer))
    }

    fun setZone(key: String, rect: Rect) { zones[key] = rect }
    fun setSlot(zone: String, index: Int, rect: Rect) { slots.getOrPut(zone) { HashMap() }[index] = rect }
    /** Forgets slots past the end of a list that got shorter. */
    fun trim(zone: String, size: Int) { slots[zone]?.keys?.removeAll { it >= size } }

    /** The smallest zone under [p] (zones may lie inside others, e.g. a bar inside the preview). */
    fun zoneAt(p: Offset): String? = zones.entries.filter { it.value.contains(p) }.minByOrNull { it.value.width * it.value.height }?.key

    /** Where an item dropped at [p] goes in [zone]: before the first slot it is above or left of (rows read left to right). */
    fun indexIn(zone: String, p: Offset): Int {
        val rects = slots[zone]?.toSortedMap()?.values?.filter { it.height > 0f } ?: return 0
        val i = rects.indexOfFirst { p.y < it.bottom && (p.y < it.top || p.x < it.center.x) }
        return if (i < 0) rects.size else i
    }

    /** The zone currently under the finger and the index there, while dragging. */
    fun hover(): Pair<String, Int>? {
        if (drag == null) return null
        val z = zoneAt(pointer) ?: return null
        return z to indexIn(z, pointer)
    }

    /**
     * The slot of [zone] that [item] lands on (not between) at [p]: the finger over the middle half of the slot, and the
     * editor allows it ([mergeable]); null otherwise.
     */
    fun mergeSlot(item: DragItem, zone: String, p: Offset = pointer): Int? {
        val hit = slots[zone]?.entries?.firstOrNull { (_, r) -> r.height > 0f && r.contains(p) && kotlin.math.abs(p.x - r.center.x) < r.width / 4f } ?: return null
        return hit.key.takeIf { mergeable(item, zone, it) }
    }

    /** The slot the dragged item would join if dropped now, as (zone, index). */
    fun mergeTarget(): Pair<String, Int>? {
        val d = drag ?: return null
        val z = zoneAt(pointer) ?: return null
        return mergeSlot(d, z)?.let { z to it }
    }

    fun slotRect(zone: String, index: Int): Rect? = slots[zone]?.get(index)
    fun zoneRect(zone: String): Rect? = zones[zone]
    fun slotCount(zone: String): Int = slots[zone]?.size ?: 0
}

internal fun Modifier.dropZone(state: DndState, key: String): Modifier = this.onGloballyPositioned { state.setZone(key, it.boundsInRoot()) }

internal fun Modifier.dropSlot(state: DndState, zone: String, index: Int): Modifier = this.onGloballyPositioned { state.setSlot(zone, index, it.boundsInRoot()) }

/**
 * A long press picks the item up (with a haptic tick) and it can be dragged; a tap runs [onTap]. The item is drawn faded
 * while it is being dragged.
 */
@Composable
internal fun Modifier.dragSource(state: DndState, item: DragItem, onTap: (() -> Unit)? = null): Modifier {
    val coords = remember { arrayOfNulls<LayoutCoordinates>(1) }
    val latest by rememberUpdatedState(item)
    val tap by rememberUpdatedState(onTap)
    val haptic = LocalHapticFeedback.current
    val dragging = state.drag?.let { it.zone == item.zone && it.index == item.index && it.id == item.id } == true
    return this
        .onGloballyPositioned { coords[0] = it }
        .alpha(if (dragging) 0.3f else 1f)
        .pointerInput(state) {
            detectDragGesturesAfterLongPress(
                onDragStart = { o ->
                    val c = coords[0] ?: return@detectDragGesturesAfterLongPress
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    state.start(latest, c.localToRoot(o))
                },
                onDragEnd = { state.end() },
                onDragCancel = { state.cancel() },
            ) { change, amount -> change.consume(); state.move(amount) }
        }
        .pointerInput(Unit) { detectTapGestures(onTap = { tap?.invoke() }) }
}

/**
 * Draws where a drop would go in [zone]: a short primary bar at the left edge of the slot at the insertion index (or at the
 * right edge of the last slot). Layout does not change, so the target does not jump.
 */
@Composable
internal fun Modifier.dropMarker(state: DndState, zone: String): Modifier {
    val color = MaterialTheme.colorScheme.primary
    return this.drawWithContent {
        drawContent()
        val (z, index) = state.hover() ?: return@drawWithContent
        if (z != zone) return@drawWithContent
        val zr = state.zoneRect(zone) ?: return@drawWithContent
        // dropping onto a button (to group them): a ring around it instead of the insertion line
        state.mergeTarget()?.takeIf { it.first == zone }?.let { (_, slot) ->
            val r = state.slotRect(zone, slot) ?: return@let
            val inset = 2.dp.toPx()
            drawRoundRect(
                color, Offset(r.left - zr.left + inset, r.top - zr.top + inset),
                androidx.compose.ui.geometry.Size(r.width - 2 * inset, r.height - 2 * inset),
                androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()),
            )
            return@drawWithContent
        }
        val count = state.slotCount(zone)
        val r = (if (index < count) state.slotRect(zone, index) else state.slotRect(zone, count - 1))
        val w = 3.dp.toPx()
        if (r == null) {
            drawRect(color, Offset(4.dp.toPx(), 4.dp.toPx()), androidx.compose.ui.geometry.Size(w, size.height - 8.dp.toPx()))
            return@drawWithContent
        }
        val x = (if (index < count) r.left else r.right) - zr.left - w / 2
        drawRoundRect(
            color, Offset(x.coerceIn(0f, size.width - w), r.top - zr.top + 4.dp.toPx()),
            androidx.compose.ui.geometry.Size(w, (r.height - 8.dp.toPx()).coerceAtLeast(w)),
            androidx.compose.ui.geometry.CornerRadius(w / 2, w / 2),
        )
    }
}

/** The floating copy of the dragged item under the finger, drawn above everything in the editor's root box. */
@Composable
internal fun DragGhost(state: DndState, rootOrigin: () -> Offset, label: @Composable (DragItem) -> String) {
    val item = state.drag ?: return
    val text = label(item)
    Surface(
        shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest, shadowElevation = 10.dp,
        modifier = Modifier.zIndex(10f)
            .offset {
                val p = state.pointer - rootOrigin()
                IntOffset((p.x - 36.dp.toPx()).roundToInt(), (p.y - 64.dp.toPx()).roundToInt())
            }
            .size(72.dp, 64.dp)
            .graphicsLayer { scaleX = 1.05f; scaleY = 1.05f },
    ) {
        Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            EditorIcon(item.id, 24.dp)
            Text(text, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/**
 * The action tray under the preview: every action, grouped and searchable, as tiles to drag up into the preview (tap adds
 * it via [onTap]). While a placed item is dragged over the tray it turns into a remove zone ([removeHint]).
 */
@Composable
internal fun ActionTray(
    state: DndState,
    modifier: Modifier,
    used: Set<Int>,
    onTap: (Int) -> Unit,
    extraGroup: Pair<Int, IntArray>? = null,
    removeHint: (DragItem) -> String?,
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val groups = remember(extraGroup) { listOfNotNull(extraGroup) + MenuCatalog.groups }
    val labels = remember(groups) { groups.flatMap { it.second.toList() }.associateWith { MenuCatalog.label(context, it) } }
    val dragged = state.drag
    val hint = if (dragged != null && dragged.zone != TRAY) removeHint(dragged) else null
    val over = hint != null && state.hover()?.first == TRAY
    Box(modifier.dropZone(state, TRAY)) {
        Column(Modifier.fillMaxSize()) {
            Text(
                stringResource(R.string.drag_actions), style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp),
            )
            OutlinedTextField(
                value = query, onValueChange = { query = it }, singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                placeholder = { Text(stringResource(R.string.search_actions)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            )
            val needle = query.trim()
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val cols = (maxWidth.value / 84f).toInt().coerceIn(3, 8)
                LazyColumn(Modifier.fillMaxSize()) {
                    for ((title, ids) in groups) {
                        val shown = ids.filter { needle.isEmpty() || (labels[it] ?: "").contains(needle, ignoreCase = true) }
                        if (shown.isEmpty()) continue
                        item(key = "h$title") {
                            Text(
                                stringResource(title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 4.dp),
                            )
                        }
                        items(shown.chunked(cols), key = { row -> "r$title-${row.first()}" }) { row ->
                            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                                for (id in row) {
                                    Box(Modifier.weight(1f).height(72.dp).padding(2.dp)) {
                                        TrayTile(state, id, labels[id] ?: "", id in used) { onTap(id) }
                                    }
                                }
                                repeat(cols - row.size) { Box(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }
        if (hint != null) {
            Box(
                Modifier.fillMaxSize().background(
                    if (over) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f) else MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
                    RoundedCornerShape(16.dp),
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    hint, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
                    color = if (over) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp),
                )
            }
        }
    }
}

@Composable
private fun TrayTile(state: DndState, id: Int, label: String, used: Boolean, onTap: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxSize().dragSource(state, DragItem(id, TRAY, -1), onTap),
    ) {
        Box {
            Column(Modifier.fillMaxSize().padding(3.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (id == BLANK) {
                    Box(Modifier.size(22.dp).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)))
                } else {
                    EditorIcon(id, 22.dp)
                }
                Text(
                    label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified),
                    maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 3.dp),
                )
            }
            if (used) {
                Icon(
                    Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.TopEnd).padding(3.dp).size(14.dp).background(MaterialTheme.colorScheme.primary, CircleShape).padding(2.dp),
                )
            }
        }
    }
}

/** Remembers where the editor's root box sits, so the ghost (root coordinates) can be placed inside it. */
@Composable
internal fun rememberRootOrigin(): Pair<Modifier, () -> Offset> {
    val origin = remember { arrayOf(Offset.Zero) }
    return Modifier.onGloballyPositioned { origin[0] = it.positionInRoot() } to { origin[0] }
}

/** A thin dashed-looking drop area shown where an empty bar would be. */
@Composable
internal fun EmptyBarZone(state: DndState, zone: String, text: String, modifier: Modifier) {
    val active = state.drag != null && state.hover()?.first == zone
    Box(
        modifier.dropZone(state, zone)
            .border(1.5.dp, if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .background(if (active) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp))
    }
}
