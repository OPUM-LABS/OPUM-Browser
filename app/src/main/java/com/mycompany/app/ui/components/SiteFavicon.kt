package com.mycompany.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import java.util.regex.Pattern

/**
 * A site's icon in a round (or [shape]) frame that fills [modifier]'s size. Icons big enough are cropped to fill it, as
 * before; small ones (a 16 px favicon in a 40 dp circle) would be stretched several times and look smeared, so they are
 * drawn at most three times their own size, centred on [background].
 */
@Composable
fun SiteFavicon(bitmap: Bitmap, modifier: Modifier, background: Color, shape: Shape = CircleShape) {
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    BoxWithConstraints(modifier.clip(shape), contentAlignment = Alignment.Center) {
        val density = LocalDensity.current
        val frame = with(density) { minOf(maxWidth, maxHeight).toPx() }
        val side = maxOf(bitmap.width, bitmap.height).toFloat()
        if (side <= 0f || side * 1.5f >= frame) {
            Image(image, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, filterQuality = FilterQuality.High)
        } else {
            // at most three times its own pixels (still sharp; twice looked lost in the circle), never past 60 % of the frame, so it sits inside the circle
            val shown = with(density) { minOf(side * 3f, frame * 0.6f).toDp() }
            Box(Modifier.fillMaxSize().background(background), contentAlignment = Alignment.Center) {
                Image(image, null, Modifier.size(shown), contentScale = ContentScale.Fit, filterQuality = FilterQuality.High)
            }
        }
    }
}

private val hostPrefix = Regex("^(?:[a-z][a-z0-9+.-]*://)?(?:www\\d*|m|mobile)\\.", RegexOption.IGNORE_CASE)

/**
 * The letter for a site without an icon: the first letter of its name, skipping "www.", "m." and "mobile." (so
 * m.youtube.com shows "Y", not "M"). Null when there is no letter.
 */
fun siteLetter(text: String?, pattern: Pattern? = null): String? {
    if (text.isNullOrBlank()) return null
    val trimmed = text.trim()
    // only addresses lose the prefix: a title such as "M. Smith" keeps its letter
    val rest = if (trimmed.contains(' ')) trimmed else hostPrefix.find(trimmed)?.let { trimmed.substring(it.range.last + 1) }?.takeIf { it.isNotBlank() } ?: trimmed
    return com.mycompany.app.main.MainUtil.stripPunctuation(rest, pattern)
}
