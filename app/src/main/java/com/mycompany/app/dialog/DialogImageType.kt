package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppListItem
import android.app.Activity
import com.mycompany.app.pref.store.AlbumSettings
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.data.DataUrl.ImgCntItem
import com.mycompany.app.dialog.DialogSetImage.ChangedListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Image gallery filter: which image types to list (JPG, PNG, GIF, WEBP, others, no extension; each with its count from
 * [imgCntItem]), an "all" check and "shown / total". Apply stores `AlbumSettings.imageType.value` (bits 2..64, all = 126) and calls
 * [changedListener] when the shown set changes. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogImageType(
    activity: Activity?,
    imgCntItem: ImgCntItem?,
    changedListener: ChangedListener?,
) : ComposeBottomDialog(activity) {
    @JvmField
    var appContext: Context? = context
    @JvmField
    var changeCallback: ChangedListener? = changedListener
    @JvmField
    var countItem: ImgCntItem? = imgCntItem
    @JvmField
    var totalCount: Int = imgCntItem?.let { it.jpgCount + it.pngCount + it.gifCount + it.webpCount + it.otherCount + it.noExtCount } ?: 0

    /** Selected type bits. */
    private var bits by mutableIntStateOf(AlbumSettings.imageType.value)

    init {
        val item = imgCntItem
        val others = context.getString(AppR.string.others)
        val noExt = context.getString(AppR.string.no_ext)
        val rows = listOf(
            Triple(2, "JPG", item?.jpgCount),
            Triple(4, "PNG", item?.pngCount),
            Triple(8, "GIF", item?.gifCount),
            Triple(16, "WEBP", item?.webpCount),
            Triple(32, others, item?.otherCount),
            Triple(64, noExt, item?.noExtCount),
        )
        setSheet {
            Row(
                Modifier.fillMaxWidth().clickable { bits = if (bits == 126) 0 else 126 }
                    .padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(AppR.string.image_type), style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                if (item != null) {
                    var shown = 0
                    rows.forEach { (bit, _, count) -> if (bits and bit == bit) shown += count ?: 0 }
                    Text(MainUtil.countLabel(shown, totalCount), style = MaterialTheme.typography.bodyLarge)
                }
                Checkbox(checked = bits == 126, onCheckedChange = { bits = if (it) 126 else 0 })
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                rows.forEach { (bit, label, count) ->
                    val on = bits and bit == bit
                    val toggle = { bits = if (on) bits and bit.inv() else bits or bit }
                    AppListItem(
                        headlineContent = { Text(if (count != null) "$label ($count)" else label) },
                        trailingContent = { Checkbox(checked = on, onCheckedChange = { toggle() }) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.clickable { toggle() },
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { apply() }, enabled = bits != 0) { Text(stringResource(AppR.string.apply)) }
            }
        }
        show()
    }

    private fun apply() {
        val old = AlbumSettings.imageType.value
        val now = bits
        if (old != now) {
            AlbumSettings.edit(appContext) { set(AlbumSettings.imageType, now) }
            if (changeCallback != null && MainUtil.imageCountChanged(countItem, old, AlbumSettings.imageType.value)) changeCallback!!.onChanged()
        }
        dismiss()
    }

    override fun dismiss() {
        changeCallback = null
        super.dismiss()
    }

    companion object {
    }
}
