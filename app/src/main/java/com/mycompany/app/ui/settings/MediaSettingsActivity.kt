package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingsScaffold

/**
 * Settings › Media. Only links since 2026-11-25: Videos ([MediaVideoSettingsActivity]) and YouTube
 * ([MediaYouTubeSettingsActivity]), each with its manager button in the phone preview. Reset restores both.
 */
class MediaSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.media),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            screenLinkRow(R.string.media_videos) { it.startActivity(Intent(it, MediaVideoSettingsActivity::class.java)) }
            screenLinkRow(R.string.youtube_title) { it.startActivity(Intent(it, MediaYouTubeSettingsActivity::class.java)) }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = { resetToDefaults(context) },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Selectable button positions, in menu order (1 top bar, 2 bottom bar, 3 floating, 0 not used). */
        @JvmField
        val positionValues: IntArray = intArrayOf(1, 2, 3, 0)

        /** Label of each position value (index = value). Used by the settings search index too. */
        @JvmField
        val positionLabels: IntArray = intArrayOf(R.string.not_used, R.string.top_bar, R.string.bot_bar, R.string.float_button)

        /** Restores the defaults of the Media pages. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            MediaButtonActivity.resetVideos(context)
            MediaButtonActivity.resetYouTube(context)
        }
    }
}
