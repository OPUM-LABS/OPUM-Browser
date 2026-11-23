package com.mycompany.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.mycompany.app.pref.store.BrowserSettings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Material 3 theme for Appearance > Theme: Colour (a scheme from the chosen colour) and the neutral Light / Dark
 * themes come from [AppColor]; System uses the dynamic colours (Android 12+) or the stock M3 colour schemes; the M3 type scale, and OPUM's own corner sizes ([OpumShapes]). The light/dark
 * decision comes from the app's own setting (see [OpumComposeActivity]). The seed follows the settings live, so open
 * screens and dialogs re-colour when it changes.
 */
@Composable
fun OpumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorTheme by BrowserSettings.colorTheme.flow.collectAsState()
    val themeColor by BrowserSettings.themeColor.flow.collectAsState()
    val wallColors by BrowserSettings.wallColors.flow.collectAsState()
    val choice = AppColor.choiceFor(colorTheme, themeColor, wallColors == 1)
    val colorScheme = remember(choice, darkTheme, dynamicColor) {
        when {
            choice != null -> AppColor.colorScheme(choice.seed, darkTheme, choice.style)
            dynamicColor ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }
    }
    MaterialTheme(colorScheme = colorScheme, shapes = OpumShapes, content = content)
}

/**
 * One set of corner sizes for the whole app (2026-11-20): menus and small controls 12 dp, chips /
 * text fields 12 dp, cards 16 dp, menus / popups 16 dp, sheets and dialogs 28 dp. Material components pick these up by
 * their size class; own drawing uses [MaterialTheme.shapes] or [OpumDimens].
 */
val OpumShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/** Spacing and sizes shared by sheets, dialogs, menus and lists. */
object OpumDimens {
    /** Corner radius of sheets (top corners) and dialogs. */
    val sheetCorner = 28.dp
    /** Corner radius of menus, popups, guide cards. */
    val popupCorner = 16.dp
    /** Height of one menu row. */
    val menuRow = 48.dp
}
