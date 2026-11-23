package com.mycompany.app.web

import android.content.Context
import com.mycompany.app.ui.state.AppState
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeOverlay

/**
 * The translate bar: "Original" and the target language as two text buttons, a language / overflow icon, and a
 * progress ring over the language button while a translation loads. [applyMode] builds it for a place: 0 / 1 = a floating card
 * over a page (shadow, outline, a small logo bar), 2 = inline in a dialog (no logo, no card), 3 = a sheet with a larger
 * logo; [showState] sets the state (language, "translated" flag, loading), [animateShow] animates it in / out around the pivot ([pivotXPx], [pivotYPx])
 * and tells the listener when it has left. Compose now (on [ComposeOverlay]); the old constructor, members and
 * [TransCtrlListener] are kept for the four hosts (`WebViewActivity`, `DialogViewRead`, `DialogWebView`, `DialogViewTrans`).
 */
class WebTransControl(context: Context?) : ComposeOverlay(context!!) {
    /** Alive flag. */
    /** The place, see [applyMode]. */
    /** Dark colours although the app is not (the host page is dark). */
    @JvmField var userDark: Boolean = false
    /** Where the show / hide animation grows from (px, in this view). */
    @JvmField var pivotXPx: Int = 0
    @JvmField var pivotYPx: Int = 0
    @JvmField var ctrlListener: TransCtrlListener? = null

    interface TransCtrlListener {
        fun onClose()
        fun onOriginal()
        fun onMenu(view: View?)
        fun onTranslate()
    }

    private var built by mutableStateOf(false)
    private var mode by mutableIntStateOf(0)
    private var darkState by mutableStateOf(false)
    private var translated by mutableStateOf(false)
    private var language by mutableStateOf<String?>(null)
    private var loading by mutableStateOf(false)
    private var animRequest by mutableIntStateOf(0)
    private var animShow by mutableStateOf(true)
    private var anchor: View? = null
    private var pivotWidth = 1
    private var pivotHeight = 1

    init {
        setPage({ AppState.darkUi || darkState }) { Content() }
    }

    /** Builds the bar for place [newMode] (0..3, see the class comment). */
    fun applyMode(newMode: Int) {
        mode = newMode
        built = true
    }

    /** Releases everything. */
    fun release() {
        ctrlListener = null
        anchor = null
    }

    /** Re-reads the colours: they are state-driven now, nothing to do. */
    fun applyDark() {
        darkState = userDark
    }

    fun showState(newLanguage: String?, newTranslated: Boolean, newLoading: Int) {
        translated = newTranslated
        if (!MainUtil.sameObject(language, newLanguage)) language = newLanguage
        loading = newLoading == 1
    }

    /** Shows ([newAnimShow] true) or hides with a 200 ms grow / shrink; hiding ends with the view gone and the listener told. */
    fun animateShow(newAnimShow: Boolean) {
        animShow = newAnimShow
        animRequest++
    }

    fun setListener(transCtrlListener: TransCtrlListener?) {
        ctrlListener = transCtrlListener
    }

    @Composable
    private fun Content() {
        if (!built) return
        val density = LocalDensity.current
        val dark = AppState.darkUi || darkState
        val modeNow = mode
        val floating = modeNow == 0 || modeNow == 1
        val scale = remember { Animatable(if (animRequest == 0) 1f else 0f) }
        LaunchedEffect(animRequest) {
            if (animRequest == 0) return@LaunchedEffect
            if (animShow) {
                scale.snapTo(0f)
                setVisibility(View.VISIBLE)
                scale.animateTo(1f, tween(200))
            } else {
                scale.snapTo(1f)
                scale.animateTo(0f, tween(200))
                setVisibility(View.GONE)
                ctrlListener?.onClose()
            }
        }
        val originalColor = Color(if (translated) (if (dark) -4079167 else -10395295) else (if (dark) -328966 else -14784824))
        val translateColor = Color(if (translated) (if (dark) -328966 else -14784824) else (if (dark) -4079167 else -10395295))
        val iconColor = Color(if (dark) -328966 else -16777216)
        val logoRes = if (dark) R.drawable.trans_logo_regular_white else R.drawable.trans_logo_regular_color
        val cornerDp = with(density) { MainApp.dp16.toDp() }
        val a1 = with(density) { MainApp.dp8.toDp() }
        val heightDp = with(density) { MainApp.dp48.toDp() }
        val shape = RoundedCornerShape(cornerDp)
        val topInset = if (modeNow == 3) 40.dp else 24.dp
        val logoTop = if (modeNow == 3) cornerDp else a1
        Box(
            Modifier.fillMaxSize()
                .onSizeChanged { pivotWidth = it.width.coerceAtLeast(1); pivotHeight = it.height.coerceAtLeast(1) }
                .graphicsLayer {
                    scaleX = scale.value; scaleY = scale.value; alpha = scale.value
                    transformOrigin = TransformOrigin(pivotXPx.toFloat() / pivotWidth, pivotYPx.toFloat() / pivotHeight)
                },
        ) {
            val body: @Composable () -> Unit = {
                Box(Modifier.fillMaxSize()) {
                    if (modeNow != 2) {
                        Image(
                            painterResource(logoRes), contentDescription = null,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = logoTop).width(176.dp).height(cornerDp),
                            alpha = if (modeNow == 3) 1f else 1f,
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(top = if (modeNow == 2) 0.dp else topInset).height(heightDp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButtonArea(stringResource(R.string.original), originalColor, Modifier.weight(1f)) { ctrlListener?.onOriginal() }
                        Box(Modifier.weight(1f).height(heightDp)) {
                            if (loading) {
                                CircularProgressIndicator(
                                    Modifier.align(Alignment.Center).size(20.dp), strokeWidth = 2.dp, color = translateColor,
                                )
                            } else {
                                TextButtonArea(
                                    language.takeUnless { it.isNullOrEmpty() } ?: stringResource(R.string.translate),
                                    translateColor, Modifier.fillMaxSize(),
                                ) { ctrlListener?.onTranslate() }
                            }
                        }
                        Box(Modifier.size(heightDp), contentAlignment = Alignment.Center) {
                            AndroidView(
                                factory = { ctx -> View(ctx).also { anchor = it } },
                                modifier = Modifier.fillMaxSize(),
                            )
                            Box(
                                Modifier.fillMaxSize().clip(RoundedCornerShape(50)).clickable { ctrlListener?.onMenu(anchor) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painterResource(if (modeNow == 0) R.drawable.outline_more_vert_black_24 else R.drawable.outline_language_black_24),
                                    contentDescription = null, tint = iconColor.copy(alpha = if (modeNow == 2 || modeNow == 3) 0.7f else 1f),
                                )
                            }
                        }
                    }
                }
            }
            if (floating) {
                Surface(
                    Modifier.fillMaxSize(), shape = shape, shadowElevation = with(density) { MainApp.dp16.toDp() },
                    color = if (dark) Color.Black else Color.White,
                    border = BorderStroke(with(density) { MainApp.dp1.toDp() }, Color(if (dark) -1066044043 else -2139785867)),
                ) { body() }
            } else {
                body()
            }
        }
    }

    @Composable
    private fun TextButtonArea(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
        Box(modifier.height(with(LocalDensity.current) { MainApp.dp48.toDp() }).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            Text(
                label, color = color, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }

    companion object {
    }
}
