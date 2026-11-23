package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.pref.store.ZtwoSettings
import android.view.View
import android.widget.FrameLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogMenuMain.DownMenuListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.NewsMenuAdapter
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.view.MyWebBody

/**
 * The news topic menu of the RSS / news floating button: a card that grows out of the button ([anchorView]) over the page
 * ([webBody]), with the language button on top and the ten topics (NewsMenuAdapter's tables; the current one, ZtwoSettings.newsTopic.value,
 * on a coloured circle). A topic calls [DownMenuListener.onItem] with its index, the language button
 * [DownMenuListener.onSettings]; a tap outside closes it. Compose since 2026-11-17 (was `MyRoundLinear` + `MyLineFrame` +
 * a RecyclerView with `MyCircleView` rows); the place, size (220 x at most 568 dp), the grow / shrink animation
 * (200 ms, from the button) and [startClose] / the listener calls are as before.
 */
@Suppress("deprecation")
class DialogNewsMenu(
    var activity: MainActivity?,
    var webBody: MyWebBody?,
    var anchorView: View?,
    var alignLeft: Boolean,
    var listener: DownMenuListener?
) {
    /** The overlay added to the page while the menu is shown. */
    private var overlay: Overlay? = null
    private var closing by mutableStateOf(false)
    private var box: IntArray? = null      // left, top, width, height in the page
    private var pivot: IntArray? = null    // the button's centre inside the card

    private val dark: Boolean = MainUtil.barTint(0, true) != 0

    private inner class Overlay(context: Context) : ComposeOverlay(context) {
        init {
            setPage({ dark }) { Menu() }
        }
    }

    init {
        webBody?.post { open() }
    }

    private fun open() {
        val body = webBody ?: return
        val act = activity ?: return
        if (!place()) return
        try {
            val view = Overlay(act)
            overlay = view
            body.addView(view, FrameLayout.LayoutParams(-1, -1))
        } catch (unused: Exception) {
            startClose()
        }
    }

    /** Starts the closing animation; false when the menu is not up or already closing. */
    fun startClose(): Boolean {
        if (overlay == null || closing) return false
        closing = true
        return true
    }

    /** Where the card goes (beside the button, below or above it, kept inside the page). */
    private fun place(): Boolean {
        val body = webBody ?: return false
        val view = anchorView ?: return false
        val loc = IntArray(2)
        body.getLocationOnScreen(loc)
        val left = body.paddingLeft + loc[0]
        val top0 = body.paddingTop + loc[1]
        val rectWidth = body.rectWidth
        val rectHeight = body.rectHeight
        view.getLocationOnScreen(loc)
        val halfW = view.width / 2
        var halfH = view.height / 2
        val cx = (loc[0] - left) + halfW
        val cy = (loc[1] - top0) + halfH
        val w = Math.round(MainUtil.dpToPx(activity, 220.0f))
        val menuHeight = Math.round(MainUtil.dpToPx(activity, 568.0f))
        var index: Int
        var width: Int
        if (alignLeft) {
            index = cx; width = cx + w
        } else {
            index = cx - w; width = cx
        }
        var centerBottom: Int
        var bot: Int
        if (cy < rectHeight / 2) {
            centerBottom = cy + halfH; bot = centerBottom + menuHeight
        } else {
            halfH = -halfH
            bot = cy + halfH; centerBottom = bot - menuHeight
        }
        if (index < 0) {
            index = 0
            width = if (w > rectWidth) rectWidth else w
        }
        if (width > rectWidth) {
            index = (rectWidth - w).coerceAtLeast(0)
            width = rectWidth
        }
        if (centerBottom < 0) {
            if (menuHeight > rectHeight) {
                bot = rectHeight; centerBottom = 0
            } else {
                centerBottom = 0; bot = menuHeight
            }
        }
        var top = centerBottom
        if (bot > rectHeight) {
            top = (rectHeight - menuHeight).coerceAtLeast(0)
            bot = rectHeight
        }
        box = intArrayOf(index, top, width - index, bot - top)
        pivot = intArrayOf(cx - index, (cy - top) + halfH)
        return true
    }

    /** Removes the overlay and lets the browser know ([DownMenuListener.onClose]). */
    private fun teardown() {
        if (activity == null) return
        val view = overlay
        overlay = null
        if (view != null) webBody?.removeView(view)
        webBody = null
        listener?.onClose()
        listener = null
        activity = null
        anchorView = null
    }

    @Composable
    private fun Menu() {
        val area = box ?: return
        val origin = pivot ?: intArrayOf(0, 0)
        val scale = remember { Animatable(0f) }
        LaunchedEffect(closing) {
            if (!closing) {
                scale.animateTo(1f, tween(200, easing = LinearOutSlowInEasing))
            } else {
                scale.animateTo(0f, tween(200, easing = FastOutLinearInEasing))
                teardown()
            }
        }
        val density = LocalDensity.current
        val none = remember { MutableInteractionSource() }
        Box(Modifier.fillMaxSize().clickable(interactionSource = none, indication = null) { startClose() }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .offset { IntOffset(area[0], area[1]) }
                    .size(with(density) { area[2].toDp() }, with(density) { area[3].toDp() })
                    .graphicsLayer {
                        val scaleNow = scale.value
                        scaleX = scaleNow; scaleY = scaleNow; alpha = scaleNow
                        transformOrigin = TransformOrigin(
                            if (area[2] > 0) origin[0].toFloat() / area[2] else 0f,
                            if (area[3] > 0) origin[1].toFloat() / area[3] else 0f,
                        )
                    }
                    .clickable(interactionSource = none, indication = null) { },
            ) {
                Column {
                    Row(Modifier.fillMaxWidth().height(with(density) { MainApp.dp48.toDp() }).padding(horizontal = 2.dp)) {
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { listener?.onSettings() }) {
                            Icon(painterResource(R.drawable.outline_language_black_20), null)
                        }
                    }
                    HorizontalDivider()
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        for (index in 0 until 10) Topic(index)
                    }
                }
            }
        }
    }

    @Composable
    private fun Topic(topic: Int) {
        val current = topic == ZtwoSettings.newsTopic.value
        Row(
            Modifier.fillMaxWidth().height(52.dp).clickable { listener?.onItem(null, topic) }.padding(start = 10.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                if (current) {
                    Box(Modifier.size(32.dp).background(Color(NewsMenuAdapter.colors[topic]), CircleShape))
                    Icon(painterResource(NewsMenuAdapter.badges[topic]), null, tint = Color.Unspecified, modifier = Modifier.size(20.dp))
                } else {
                    Icon(painterResource(NewsMenuAdapter.icons[topic]), null, modifier = Modifier.size(20.dp))
                }
            }
            Text(
                stringResource(NewsMenuAdapter.labels[topic]), fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}
