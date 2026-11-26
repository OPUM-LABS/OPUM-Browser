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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.round
import kotlinx.coroutines.launch
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

/**
 * What is being dragged: an action [id] from [zone] at [index] (-1 for something that is not an item in a list, e.g. a
 * whole bar). [ghost]: whether a floating copy follows the finger (a whole bar shows its new place instead).
 */
internal class DragItem(val id: Int, val zone: String, val index: Int, val ghost: Boolean = true)

/** How a zone lays out its slots for the live preview: side by side ([cols] 0; slot [flex] takes the free width) or a grid. */
private class ZoneShape(val cols: Int, val flex: Int)

/** Where a slot is drawn while dragging: moved by [offset], [scaleX] for the slot taking the free width, [hidden] the dragged one. */
internal class LiveShift(val offset: Offset, val scaleX: Float = 1f, val hidden: Boolean = false) {
    companion object { val NONE = LiveShift(Offset.Zero) }
}

/**
 * Drag and drop between the zones of an editor (bars, the menu grid, the tray), 2026-11-19. Zones and their item slots
 * report their bounds in root coordinates; the dragged item follows the finger as a floating copy ([DragGhost]); the
 * lists are only changed on drop ([onDrop] gets the item, the zone under the finger or null, and the insertion index).
 *
 * Live preview (2026-11-26): while dragging, the other items slide aside ([liveShift]) to open a gap where the item would
 * land, and its old place closes; on release the copy glides into the gap ([landing]) and only then is the drop applied,
 * so everything is already where the new list puts it. The slots keep the places measured before the drag (they are not
 * measured again while dragging and the sliding is drawn on top), so the landing place cannot flip under the finger; it
 * also only changes once the finger is clearly past a slot's middle.
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
    /** Whether a drop of the item into the zone would be taken (a full bar takes no more): only then the gap opens. */
    var accepts: (DragItem, String) -> Boolean = { _, _ -> true }

    private val shapes = HashMap<String, ZoneShape>()
    /** A grid cell after the last slot that moves with the others (the menu's "All actions"). */
    private val tails = HashMap<String, Rect>()
    /** The last landing place shown, kept while the finger stays near it. */
    private var lastHover: Pair<String, Int>? = null
    /** Where the floating copy glides to on release (the gap's middle); the drop is applied when it got there. */
    var landing by mutableStateOf<Offset?>(null)
        private set
    private var pending: (() -> Unit)? = null
    /** Counts applied drops: the slots then jump to their new places without sliding (they are already drawn there). */
    var epoch by mutableIntStateOf(0)
        private set

    fun start(item: DragItem, at: Offset) { landed(); lastHover = null; drag = item; pointer = at }
    fun move(by: Offset) { if (landing == null) pointer += by }
    fun cancel() { pending = null; landing = null; lastHover = null; drag = null }
    fun end() {
        val item = drag ?: return
        val zone = zoneAt(pointer)
        val index = if (zone == null) -1 else hover()?.takeIf { it.first == zone }?.second ?: indexIn(zone, pointer)
        val target = if (item.ghost && zone != null) gapRect(zone)?.center else null
        val apply: () -> Unit = {
            pending = null; landing = null; lastHover = null; drag = null
            onDrop(item, zone, index)
            epoch += 1
        }
        if (target == null) apply() else { pending = apply; landing = target }
    }

    /** The copy reached the gap: apply the drop. */
    fun landed() { pending?.invoke() }

    /** Registers how [zone] lays out its slots (once per composition of the zone). */
    fun shape(zone: String, cols: Int = 0, flex: Int = -1) { shapes[zone] = ZoneShape(cols, flex) }
    fun setTail(zone: String, rect: Rect) { if (drag == null) tails[zone] = rect }

    fun setZone(key: String, rect: Rect) { zones[key] = rect }
    // the places from before the drag stay (the sliding preview must not move the targets)
    fun setSlot(zone: String, index: Int, rect: Rect) { if (drag == null) slots.getOrPut(zone) { HashMap() }[index] = rect }
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

    /**
     * The zone currently under the finger and the index there, while dragging. The index shown last stays while the
     * finger is within 15 % of a slot's width of it, so it does not flicker on a slot's middle.
     */
    fun hover(): Pair<String, Int>? {
        if (drag == null) return null
        val z = zoneAt(pointer) ?: return null
        val last = lastHover
        val margin = (slots[z]?.values?.firstOrNull()?.width ?: 0f) * 0.15f
        if (last != null && last.first == z && margin > 0f) {
            val a = indexIn(z, pointer - Offset(margin, 0f))
            val b = indexIn(z, pointer + Offset(margin, 0f))
            if (last.second in minOf(a, b)..maxOf(a, b)) return last
        }
        return (z to indexIn(z, pointer)).also { lastHover = it }
    }

    // ---- live preview ----

    /** The insertion index (old numbering) where a gap opens in [zone], or null (not over it, grouping, or not taken). */
    private fun gapIn(zone: String): Int? {
        val d = drag ?: return null
        if (!d.ghost || shapes[zone] == null) return null
        val (z, k) = hover() ?: return null
        if (z != zone || mergeTarget() != null || !accepts(d, zone)) return null
        return k
    }

    /** Whether the dragged item's old place closes: it is over a gap or over the tray (removing it). */
    private fun sourceCloses(): Boolean {
        val d = drag ?: return false
        if (d.index < 0 || !d.ghost || shapes[d.zone] == null) return false
        val z = hover()?.first ?: return false
        return if (z == TRAY) accepts(d, TRAY) else gapIn(z) != null
    }

    /** Where slot [i] of [zone] goes in the preview (old numbering in, new place out; -1 for the dragged item). */
    private fun virtualIndex(zone: String, i: Int): Int {
        val d = drag ?: return i
        val closing = d.zone == zone && sourceCloses()
        var j = i
        if (closing) { if (i == d.index) return -1; if (i > d.index) j-- }
        gapIn(zone)?.let { k -> if (j >= (if (closing && k > d.index) k - 1 else k)) j++ }
        return j
    }

    private fun gapIndex(zone: String): Int? {
        val d = drag ?: return null
        val k = gapIn(zone) ?: return null
        return if (d.zone == zone && sourceCloses() && k > d.index) k - 1 else k
    }

    /** A grid's cell [v]: a slot, the tail after the last slot, or one row below the cell [cols] before it. */
    private fun cell(zone: String, v: Int, cols: Int): Rect? {
        val own = slots[zone]
        own?.get(v)?.let { return it }
        val n = own?.size ?: 0
        if (v == n) tails[zone]?.let { return it }
        if (v < cols) return null
        val above = cell(zone, v - cols, cols) ?: return null
        val first = own?.get(0)
        val step = own?.get(cols)?.let { it.top - (first?.top ?: it.top) }?.takeIf { it > 0f } ?: above.height
        return above.translate(0f, step)
    }

    /** The preview places of a row: the items left to right (gap included, key -1), squeezed or with the free width. */
    private fun rowPlaces(zone: String): Map<Int, Rect>? {
        val shape = shapes[zone] ?: return null
        val own = slots[zone] ?: return null
        val n = own.size
        if (n == 0) return null
        val base = (0 until n).map { own[it] ?: return null }
        val left = base.first().left
        val avail = base.last().right - left
        val d = drag
        val order = ArrayList<Int>()
        for (i in 0 until n) if (virtualIndex(zone, i) >= 0) order.add(i)
        order.sortBy { virtualIndex(zone, it) }
        val gap = gapIndex(zone)
        if (gap != null) order.add(gap.coerceIn(0, order.size), -1)
        val from = d?.let { slots[it.zone]?.get(it.index) }?.takeIf { d.zone != TRAY && !(d.zone == zone && d.index == shape.flex) }
        val plain = base.filterIndexed { i, _ -> i != shape.flex }
        val gapW = from?.width ?: plain.map { it.width }.average().toFloat().takeIf { !it.isNaN() } ?: (avail / (n + 1))
        val widths = order.map { if (it < 0) gapW else base[it].width }.toMutableList()
        // (-1 in the order is the gap, not a free-width slot)
        val flexAt = if (shape.flex >= 0) order.indexOf(shape.flex) else -1
        val fixed = widths.sum() - (if (flexAt >= 0) widths[flexAt] else 0f)
        if (flexAt >= 0 && avail - fixed >= base[shape.flex].width * 0.35f) {
            widths[flexAt] = avail - fixed
        } else {
            val s = avail / widths.sum()
            for (w in widths.indices) widths[w] *= s
        }
        val out = HashMap<Int, Rect>()
        var x = left
        for ((pos, i) in order.withIndex()) {
            out[i] = Rect(x, base[0].top, x + widths[pos], base[0].bottom)
            x += widths[pos]
        }
        return out
    }

    /** The gap's place in [zone] (where the copy lands), or null when no gap is open there. */
    fun gapRect(zone: String): Rect? {
        val shape = shapes[zone] ?: return null
        val k = gapIndex(zone) ?: return null
        return if (shape.cols > 0) cell(zone, k, shape.cols) else rowPlaces(zone)?.get(-1)
    }

    /** How slot [i] of [zone] is drawn right now (the tail of a grid is slot `size`). */
    fun shiftOf(zone: String, i: Int): LiveShift {
        if (drag == null) return LiveShift.NONE
        val shape = shapes[zone] ?: return LiveShift.NONE
        val v = virtualIndex(zone, i)
        if (v < 0) return LiveShift(Offset.Zero, hidden = true)
        if (shape.cols > 0) {
            val from = cell(zone, i, shape.cols) ?: return LiveShift.NONE
            val to = cell(zone, v, shape.cols) ?: return LiveShift.NONE
            return LiveShift(to.topLeft - from.topLeft)
        }
        val from = slots[zone]?.get(i) ?: return LiveShift.NONE
        val to = rowPlaces(zone)?.get(i) ?: return LiveShift.NONE
        return LiveShift(Offset(to.center.x - from.center.x, 0f), if (i == shape.flex) to.width / from.width else 1f)
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

/** A slot's place (not cut by a scrolled parent, so slots out of sight keep their real place for the preview). */
internal fun Modifier.dropSlot(state: DndState, zone: String, index: Int): Modifier =
    this.onGloballyPositioned { state.setSlot(zone, index, Rect(it.positionInRoot(), androidx.compose.ui.geometry.Size(it.size.width.toFloat(), it.size.height.toFloat()))) }

/** The grid cell after the last slot that moves along (see [DndState.setTail]). */
internal fun Modifier.dropTail(state: DndState, zone: String): Modifier =
    this.onGloballyPositioned { state.setTail(zone, Rect(it.positionInRoot(), androidx.compose.ui.geometry.Size(it.size.width.toFloat(), it.size.height.toFloat()))) }

/**
 * Slides slot [index] of [zone] to its place in the live preview (after [dropSlot], so its measured place stays). After a
 * drop it jumps instead: the new list puts it exactly where it was drawn.
 */
@Composable
internal fun Modifier.liveShift(state: DndState, zone: String, index: Int): Modifier {
    val target by remember(state, zone, index) { derivedStateOf { state.shiftOf(zone, index) } }
    val move = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val scale = remember { Animatable(1f) }
    val seen = remember { intArrayOf(state.epoch) }
    val epoch = state.epoch
    LaunchedEffect(target.offset, target.scaleX, epoch) {
        if (epoch != seen[0]) {
            seen[0] = epoch
            move.snapTo(target.offset); scale.snapTo(target.scaleX)
        } else {
            launch { scale.animateTo(target.scaleX, spring(stiffness = Spring.StiffnessMediumLow)) }
            move.animateTo(target.offset, spring(stiffness = Spring.StiffnessMediumLow))
        }
    }
    val hidden = target.hidden
    return this.graphicsLayer {
        translationX = move.value.x; translationY = move.value.y
        scaleX = scale.value
        if (hidden) alpha = 0f
    }
}

/** Slides to wherever the layout puts it (the top bar moving between the top and the bottom of the phone). */
@Composable
internal fun Modifier.animatePlacement(): Modifier {
    val scope = rememberCoroutineScope()
    val anim = remember { arrayOfNulls<Animatable<IntOffset, androidx.compose.animation.core.AnimationVector2D>>(1) }
    val target = remember { arrayOf(IntOffset.Zero) }
    return this
        .onPlaced { target[0] = it.positionInParent().round() }
        .offset {
            val a = anim[0] ?: Animatable(target[0], IntOffset.VectorConverter).also { anim[0] = it }
            if (a.targetValue != target[0]) scope.launch { a.animateTo(target[0], spring(stiffness = Spring.StiffnessMediumLow)) }
            a.value - target[0]
        }
}

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
        // a whole bar shows its new place itself, no marker
        if (state.drag?.ghost != true) return@drawWithContent
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
        // the gap of the live preview shows the place; the line only stands in an empty zone
        if (count > 0 && state.gapRect(zone) != null) return@drawWithContent
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
    if (!item.ghost) return
    val text = label(item)
    // on release: glide from the finger into the gap (the copy's middle onto the gap's middle), then apply the drop
    val glide = remember(item) { Animatable(Offset.Zero, Offset.VectorConverter) }
    val landing = state.landing
    val density = androidx.compose.ui.platform.LocalDensity.current
    LaunchedEffect(landing) {
        if (landing == null) return@LaunchedEffect
        val lift = with(density) { 32.dp.toPx() }
        glide.animateTo(landing + Offset(0f, lift) - state.pointer, tween(160))
        state.landed()
    }
    Surface(
        shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest, shadowElevation = 10.dp,
        modifier = Modifier.zIndex(10f)
            .offset {
                val p = state.pointer + glide.value - rootOrigin()
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
