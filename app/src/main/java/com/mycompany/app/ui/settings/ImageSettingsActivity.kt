package com.mycompany.app.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.store.ImageSettings
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.rememberPref

/** Settings of the image viewer (thumbnails, page-turn guide, volume keys, screen timeout, tap areas). */
class ImageSettingsActivity : OpumComposeActivity() {
    private var thumbAtStart = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        thumbAtStart = ImageSettings.showThumb.value
        super.onCreate(savedInstanceState)
    }

    override fun finish() {
        setResult(Activity.RESULT_OK, Intent().putExtra("EXTRA_THUMB", thumbAtStart != ImageSettings.showThumb.value))
        super.finish()
    }

    @Composable
    override fun Content() {
        val context: Context = this
        val thumb = rememberPref({ ImageSettings.showThumb.value }) { ImageSettings.edit(context) { set(ImageSettings.showThumb, it) } }
        val guide = rememberPref({ ImageSettings.showGuide.value }) { ImageSettings.edit(context) { set(ImageSettings.showGuide, it) } }
        val volume = rememberPref({ ImageSettings.volumeTurn.value }) { ImageSettings.edit(context) { set(ImageSettings.volumeTurn, it) } }
        val screenOff = rememberPref({ ImageSettings.screenOff.value }) { ImageSettings.edit(context) { set(ImageSettings.screenOff, it) } }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.setting),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_display))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.show_thumb),
                    checked = thumb.value,
                    onCheckedChange = thumb::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.show_page_guide),
                    checked = guide.value,
                    onCheckedChange = guide::set,
                )
            }
            settingsFooter(getString(R.string.show_thumb_info))
            settingsHeader(getString(R.string.hd_turning))
            item {
                SettingRow(
                    title = stringResource(R.string.tap_turn),
                    onClick = { startActivity(Intent(context, com.mycompany.app.ui.settings.ImageTapSettingsActivity::class.java)) },
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.volume_turn),
                    checked = volume.value,
                    onCheckedChange = volume::set,
                )
            }
            settingsHeader(getString(R.string.hd_screen))
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.screen_off),
                    options = MainConst.screenOffNames.map { stringResource(it) },
                    selectedIndex = screenOff.value,
                    onSelect = screenOff::set,
                )
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    thumb.reload(ImageSettings.showThumb.value)
                    guide.reload(ImageSettings.showGuide.value)
                    volume.reload(ImageSettings.volumeTurn.value)
                    screenOff.reload(ImageSettings.screenOff.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** Restores the defaults of this page: changed keys are removed. */
    private fun resetToDefaults(context: Context) {
        with(ImageSettings) {
            val page = listOf(showThumb, showGuide, volumeTurn, screenOff)
            val changedPage = page.filter { !it.isDefault }
            if (changedPage.isNotEmpty()) reset(context, changedPage)
            if (tapAreas.any { !it.isDefault }) reset(context, tapAreas)
        }
    }
}
