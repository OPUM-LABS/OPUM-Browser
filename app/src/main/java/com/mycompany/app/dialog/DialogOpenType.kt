package com.mycompany.app.dialog

import android.app.Activity
import com.mycompany.app.ui.state.AppState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * "Open as": which kind of app should open the file at [path]: image, video, audio, document or any. Picking one hands the
 * path to `MainUtil.openWithApp` with that MIME type (a toast when there is no path) and closes the sheet. Compose now (on
 * [ComposeBottomDialog]); the constructor is as before ([theme] only chose the old dialog theme), it shows itself.
 */
class DialogOpenType(activity: Activity?, theme: Int, path: String?, chooser: Boolean) : ComposeBottomDialog(activity) {
    private var host: Activity? = activity
    private val path: String? = path
    private val chooser: Boolean = chooser

    private fun choose(mime: String?) {
        val activity = host
        if (activity != null) {
            if (path.isNullOrEmpty()) MainUtil.showToast(context, R.string.invalid_path)
            else MainUtil.openWithApp(activity, path, mime, true, chooser)
        }
        dismiss()
    }

    init {
        setSheet {
            val dark = AppState.darkUi
            val rows = listOf(
                Triple(if (dark) R.drawable.outline_image_dark_24 else R.drawable.outline_image_black_24, R.string.image, "image/*"),
                Triple(if (dark) R.drawable.baseline_play_arrow_dark_24 else R.drawable.baseline_play_arrow_black_24, R.string.video, "video/*"),
                Triple(if (dark) R.drawable.baseline_music_note_dark_24 else R.drawable.baseline_music_note_black_24, R.string.audio, "audio/*"),
                Triple(if (dark) R.drawable.outline_description_dark_24 else R.drawable.outline_description_black_24, R.string.doc, "text/*"),
                Triple(if (dark) R.drawable.outline_draft_dark_24 else R.drawable.outline_draft_black_24, R.string.others, null),
            )
            for ((icon, label, mime) in rows) {
                Row(
                    Modifier.fillMaxWidth().clickable { choose(mime) }.padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
                    Text(stringResource(label), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { dismiss() }) { Text(stringResource(R.string.close)) }
            }
        }
        show()
    }

    override fun dismiss() {
        host = null
        super.dismiss()
    }
}
