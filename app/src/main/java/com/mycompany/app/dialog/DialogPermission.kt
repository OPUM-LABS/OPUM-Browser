package com.mycompany.app.dialog

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSetDesk.SetDeskListener
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.components.Sym

/**
 * A site asks for permissions, worded like Chrome's prompt: "**host** wants to" and one line with an icon per requested
 * permission ([kinds]: the `DbBookPms` bits — [CAMERA], [MIC], [LOCATION], [PROTECTED] media), then three choices:
 * "Allow this time" (granted, not remembered: [onOnce] is called first), "Allow on every visit" and "Don't allow" (both
 * remembered for the site, as before; listener: true = allow). "Site settings" ([onSiteSettings]) opens the saved site
 * permissions. Shows itself; a second tap while a choice is being handled is ignored.
 */
class DialogPermission(
    activity: Activity?,
    host: String?,
    kinds: Int,
    setDeskListener: SetDeskListener?,
    private val onOnce: (() -> Unit)? = null,
    private val onSiteSettings: (() -> Unit)? = null,
) : ComposeBottomDialog(activity) {
    private var listener: SetDeskListener? = setDeskListener
    private var busy = false

    private fun choose(allow: Boolean, once: Boolean) {
        if (busy) return
        busy = true
        if (once) onOnce?.invoke()
        window?.decorView?.post {
            listener?.onChanged(allow)
            busy = false
        }
    }

    init {
        setSheet {
            Column(
                Modifier.weight(1f, fill = false).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val site = host?.removePrefix("www.").orEmpty()
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(site.ifEmpty { "?" }) }
                        append(" ")
                        append(stringResource(R.string.pms_wants_to))
                    },
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 3, overflow = TextOverflow.Ellipsis,
                )
                if ((kinds and CAMERA) != 0) Kind(R.drawable.outline_videocam_black_24, R.string.pms_camera, null)
                if ((kinds and MIC) != 0) Kind(R.drawable.outline_mic_black_24, R.string.pms_mic, null)
                if ((kinds and LOCATION) != 0) Kind(0, R.string.pms_location, null)
                if ((kinds and PROTECTED) != 0) Kind(R.drawable.outline_movie_filter_black_24, R.string.pms_protected, R.string.pms_protected_info)
            }
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = { choose(allow = true, once = false) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.pms_allow_always))
                }
                OutlinedButton(onClick = { choose(allow = true, once = true) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.pms_allow_once))
                }
                OutlinedButton(onClick = { choose(allow = false, once = false) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.pms_deny))
                }
                if (onSiteSettings != null) {
                    TextButton(onClick = { onSiteSettings.invoke() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(stringResource(R.string.pms_site_settings))
                    }
                }
            }
        }
        show()
    }

    @Composable
    private fun Kind(icon: Int, title: Int, info: Int?) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (icon != 0) {
                Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
            } else {
                Icon(Sym.LocationOn, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.padding(start = 16.dp)) {
                Text(stringResource(title), style = MaterialTheme.typography.bodyLarge)
                if (info != null) {
                    Text(stringResource(info), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    override fun dismiss() {
        listener = null
        super.dismiss()
    }

    companion object {
        /** Requested permissions, the bits `DbBookPms` stores. */
        const val CAMERA = 2
        const val MIC = 4
        const val LOCATION = 8
        const val PROTECTED = 16
    }
}
