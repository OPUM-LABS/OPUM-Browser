package com.mycompany.app.dialog

import androidx.compose.foundation.Image
import com.mycompany.app.ui.state.AppState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Explains why the notification permission is wanted (status notifications of downloads, the ad filter, secret mode and
 * background playback) and where to change it. Compose now (on [ComposeBottomDialog]); constructor as before, it shows
 * itself, and OK closes it.
 */
class DialogGuideNoti(mainActivity: MainActivity?) : ComposeBottomDialog(mainActivity) {
    init {
        setSheet {
            val dark = AppState.darkUi
            Column2(dark)
        }
        show()
    }

    @androidx.compose.runtime.Composable
    private fun androidx.compose.foundation.layout.ColumnScope.Column2(dark: Boolean) {
        Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(if (dark) R.drawable.outline_notifications_active_dark_24 else R.drawable.outline_notifications_active_black_24),
                contentDescription = null, modifier = Modifier.size(24.dp),
            )
            Text(stringResource(R.string.noti_pms), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp))
        }
        val guide = stringResource(R.string.noti_pms_guide_1) + "\n" + stringResource(R.string.change_in_setting)
        androidx.compose.foundation.layout.Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            Text(guide, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
            val rows = listOf(
                (if (dark) R.drawable.outline_download_dark_24 else R.drawable.outline_download_black_24) to R.string.download,
                (if (dark) R.drawable.outline_verified_user_dark_24 else R.drawable.outline_verified_user_black_24) to R.string.ads_block,
                (if (dark) R.drawable.outline_secret_mode_dark_24 else R.drawable.outline_secret_mode_black_24) to R.string.secret_mode,
                (if (dark) R.drawable.baseline_play_arrow_dark_24 else R.drawable.baseline_play_arrow_black_24) to R.string.back_play,
            )
            for ((icon, label) in rows) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
                    Text(stringResource(label), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
            TextButton(onClick = { dismiss() }) { Text(stringResource(R.string.ok)) }
        }
    }
}
