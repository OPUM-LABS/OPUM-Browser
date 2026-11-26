package com.mycompany.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/** The largest text growth for tight grids and button rows (menu tiles, tab overview buttons): beyond it labels only get cut. */
const val TIGHT_FONT_SCALE = 1.3f

/**
 * Lets [content]'s text follow the system font size only up to [max]: at the largest sizes (up to 2×) short labels in
 * fixed-width cells shrink to "Read…", which is less readable than slightly smaller whole words.
 */
@Composable
fun CapFontScale(max: Float = TIGHT_FONT_SCALE, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    if (density.fontScale <= max) content()
    else CompositionLocalProvider(LocalDensity provides Density(density.density, max), content = content)
}

/** The font scale [CapFontScale] leaves, for cells whose height has to make room for two lines of label. */
@Composable
fun cappedFontScale(max: Float = TIGHT_FONT_SCALE): Float = LocalDensity.current.fontScale.coerceAtMost(max)
