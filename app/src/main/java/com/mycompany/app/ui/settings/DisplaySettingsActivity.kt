package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingsScaffold

/**
 * Appearance (was "Display"). Only links since 2026-11-25: theme and colours (with the phone preview), dark mode of
 * web pages, the screen, text and font. Reset restores all of them.
 */
class DisplaySettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.set_appearance),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            screenLinkRow(R.string.appear_theme_page) { it.startActivity(Intent(it, ThemeSettingsActivity::class.java)) }
            screenLinkRow(R.string.dark_mode) { it.startActivity(Intent(it, DarkModeSettingsActivity::class.java)) }
            screenLinkRow(R.string.hd_screen) { it.startActivity(Intent(it, ScreenSettingsActivity::class.java)) }
            screenLinkRow(R.string.appear_text_page) { it.startActivity(Intent(it, TextSettingsActivity::class.java)) }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    refreshTheme()
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Theme values in menu order (0 light, 1 dark) and the label of each value (index = value). */
        @JvmField
        val themeValues: IntArray = intArrayOf(0, 1)

        @JvmField
        val themeLabels: IntArray = intArrayOf(R.string.theme_light, R.string.theme_dark)

        @JvmField
        val rotateLabels: IntArray = intArrayOf(R.string.system_name, R.string.view_port, R.string.view_land)

        /** Restores the defaults of the Appearance pages. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            ThemeSettingsActivity.resetToDefaults(context)
            DarkModeSettingsActivity.resetToDefaults(context)
            ScreenSettingsActivity.resetToDefaults(context)
            resetTextFont(context)
        }
    }
}

/**
 * Fold-out sample photo under "Image brightness": shows the photo dimmed the way web pages dim images in dark mode
 * (CSS `brightness(n%)` = every colour channel times n / 100), following the slider while it is dragged.
 */
@Composable
internal fun BrightnessPreview(percent: Int) {
    var open by rememberSaveable { mutableStateOf(false) }
    val turn by animateFloatAsState(if (open) 180f else 0f, label = "arrow")
    val f = percent / 100f
    val filter = remember(percent) { ColorFilter.colorMatrix(ColorMatrix().apply { setToScale(f, f, f, 1f) }) }
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(
            Modifier.clip(RoundedCornerShape(50)).clickable { open = !open }.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.preview), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Icon(
                Icons.Filled.KeyboardArrowDown, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp).size(20.dp).rotate(turn),
            )
        }
        AnimatedVisibility(visible = open) {
            Image(
                painterResource(R.drawable.preview_matterhorn), null,
                contentScale = ContentScale.Crop,
                alignment = BiasAlignment(0f, -0.45f), // summit and lake, not the meadow in the middle
                colorFilter = filter,
                modifier = Modifier.padding(top = 4.dp).fillMaxWidth().height(260.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black),
            )
        }
    }
}
