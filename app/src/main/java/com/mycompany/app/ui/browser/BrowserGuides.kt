package com.mycompany.app.ui.browser

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp

/**
 * A tip over the page, just above the bottom bar (2026-11-20; replaced the green one-time gesture card): the
 * [com.mycompany.app.ui.tips.TipCard] in the app theme, sliding in, closed with "Got it" (or its action), or on its own
 * after 12 s. Only the card takes touches; the page around it stays usable. [onGone] removes the view.
 */
class TipOverlay(
    context: Context,
    private val tip: com.mycompany.app.ui.tips.Tip,
    private val onAction: () -> Unit,
    private val onGone: (TipOverlay) -> Unit,
) : ComposeOverlay(context) {
    private var visible by mutableStateOf(false)
    private var closing = false

    init {
        setPage({ com.mycompany.app.ui.state.AppState.darkUi }) {
            androidx.compose.runtime.LaunchedEffect(Unit) {
                visible = true
                kotlinx.coroutines.delay(12000)
                close()
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = visible,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it / 2 },
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { it / 2 },
            ) {
                com.mycompany.app.ui.tips.TipCard(
                    tip, onDismiss = { close() }, onAction = onAction,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }

    /** Slides out, then [onGone]. */
    fun close() {
        if (closing) return
        closing = true
        visible = false
        postDelayed({ onGone(this) }, 300)
    }
}

/**
 * The one-handed mode notice at the top of the page body (2026-11-17; was a `RelativeLayout` with an arrow `View` and an
 * `AppCompatTextView`): an up arrow (70 % opaque) and "one-handed mode" in the bar colours of the page. [tint] sets
 * them: [light] = draw the light arrow (dark bar), [textColor] the text colour.
 */
class HandModeBar(context: Context) : ComposeOverlay(context) {
    private var arrowRes by mutableIntStateOf(R.drawable.outline_keyboard_arrow_up_black_24)
    private var textColor by mutableIntStateOf(-16777216)

    init {
        setPage({ false }) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                Icon(painterResource(arrowRes), null, tint = Color.Unspecified, modifier = Modifier.size(24.dp).alpha(0.7f))
                Text(
                    stringResource(R.string.onehand_noti), color = Color(textColor), fontSize = 16.sp,
                    modifier = Modifier.padding(top = with(LocalDensity.current) { MainApp.dp8.toDp() }),
                )
            }
        }
    }

    fun tint(light: Boolean, textColor: Int) {
        arrowRes = if (light) R.drawable.outline_keyboard_arrow_up_dark_24 else R.drawable.outline_keyboard_arrow_up_black_24
        this.textColor = textColor
    }
}
