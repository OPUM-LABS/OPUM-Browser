package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.prefs.rememberPref

/** What the address bar shows for [type] (0 domain, 1 address, 2 title), as a small pill under the choice. */
@Composable
private fun AddressLabelPreview(type: Int) {
    val text = when (type) {
        1 -> "https://en.wikipedia.org/wiki/Web_browser"
        2 -> "Web browser – Wikipedia"
        else -> "en.wikipedia.org"
    }
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp).height(40.dp)
            .clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(if (type == 2) R.drawable.outline_public_black_24 else R.drawable.outline_lock_black_24), contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp),
        )
        Text(
            text, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/**
 * Settings › Toolbars & controls › Address bar: what the address bar shows and the buttons inside it
 * ([AddrIconSettingsActivity]).
 */
class AddressBarSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val addrLabel = rememberPref({ PdfSettings.addrType.value }) { PdfSettings.edit(context) { set(PdfSettings.addrType, it) } }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.address_bar),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.address_label),
                    options = addressLabels.map { stringResource(it) },
                    selectedIndex = addrLabel.value,
                    onSelect = { addrLabel.set(it) },
                )
            }
            // what the address bar shows with the choice above
            item { AddressLabelPreview(addrLabel.value) }
            screenLinkRow(R.string.address_icon) { it.startActivity(Intent(it, AddrIconSettingsActivity::class.java)) }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    addrLabel.reload(PdfSettings.addrType.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Labels of the address bar content (index = value). Used by the settings search index. */
        @JvmField
        val addressLabels: IntArray = intArrayOf(R.string.domain_name, R.string.url, R.string.title)

        /** Restores the label and the buttons inside the address bar. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (PdfSettings.addrType.value != 0) PdfSettings.reset(context, listOf(PdfSettings.addrType))
            if (!MainSettings.addrItems.isDefault) MainSettings.reset(context, listOf(MainSettings.addrItems))
        }
    }
}
