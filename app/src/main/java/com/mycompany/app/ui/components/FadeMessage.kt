package com.mycompany.app.ui.components

import android.content.Context
import com.mycompany.app.ui.state.AppState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.mycompany.app.main.MainApp
import com.mycompany.app.ui.browser.ComposeOverlay

/**
 * A centred message over a legacy View screen that fades in ([show]) and out ([hide]) in 400 ms, like the old
 * `MyFadeText` it replaces (the reader's "nothing to read" text, 2026-11-17). While hidden it draws nothing and lets
 * touches through.
 */
class FadeMessage(context: Context, private val messageRes: Int) : ComposeOverlay(context) {
    private var shown by mutableStateOf(false)

    init {
        setPage({ AppState.darkUi }) {
            AnimatedVisibility(shown, enter = fadeIn(tween(400)), exit = fadeOut(tween(400))) {
                val pad = with(LocalDensity.current) { MainApp.dp40.toDp() }
                Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(messageRes), fontSize = 16.sp, textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }

    fun show() {
        shown = true
    }

    fun hide() {
        shown = false
    }
}
