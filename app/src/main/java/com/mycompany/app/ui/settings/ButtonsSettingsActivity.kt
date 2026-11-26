package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.FloatSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.EXTRA_FOCUS
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.wview.FloatButton

/**
 * Settings › Toolbars & controls › Buttons on the page (2026-11-26): every button that sits on a web page, with what it
 * does now. Each one is set up with its feature, so a row opens that page at the button (scrolled to and flashed). Below,
 * "Put buttons back in place" forgets where they were dragged; Reset restores all of them (on / off, colour, place).
 */
class ButtonsSettingsActivity : OpumComposeActivity() {

    /** Opens [page] at the row titled [row]. */
    private fun open(page: Class<*>, row: Int) {
        startActivity(Intent(this, page).putExtra(EXTRA_FOCUS, getString(row)))
    }

    @Composable
    override fun Content() {
        val context: Context = this
        var tick by remember { mutableIntStateOf(0) } // bumped on resume: the button pages change these
        var resetting by remember { mutableStateOf(false) }
        OnScreenResume { tick++ }

        val on = stringResource(R.string.btn_on)
        val off = stringResource(R.string.btn_off)
        fun onOff(value: Boolean) = if (value) on else off

        SettingsScaffold(
            title = stringResource(R.string.page_buttons),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item {
                val floats = "$tick".let { listOf(FloatSettings.flt1Icon.value, FloatSettings.flt2Icon.value, FloatSettings.flt3Icon.value).filter { it != 0 } }
                SettingRow(
                    title = stringResource(R.string.float_button),
                    summary = if (floats.isEmpty()) stringResource(R.string.not_used) else floats.joinToString(", ") { getString(MainUtil.menuIconAt(it)) },
                    onClick = { startActivity(Intent(context, FloatSettingsActivity::class.java)) },
                )
            }
            item {
                val place = "$tick".let { ZoneSettings.showUpPos.value.coerceIn(0, 3) }
                SettingRow(
                    title = stringResource(R.string.show_up),
                    summary = stringResource(MainConst.scrollPosNames[place]),
                    onClick = { open(FullScreenSettingsActivity::class.java, R.string.show_up) },
                )
            }
            item {
                val floating = "$tick".let { AlbumSettings.transPage.value }
                SettingRow(
                    title = stringResource(R.string.hd_trans_button),
                    summary = stringResource(if (floating) R.string.float_button else R.string.address_bar),
                    onClick = { open(TransSettingsActivity::class.java, R.string.icon_pos) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.btn_read_stop),
                    summary = "$tick".let { onOff(TtsSettings.ttsIcon.value) },
                    onClick = { open(TtsSettingsActivity::class.java, R.string.stop_icon) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.btn_text_zoom),
                    summary = "$tick".let { onOff(ZtriSettings.zoomIcon.value) },
                    onClick = { open(TextSettingsActivity::class.java, R.string.zoom_icon) },
                )
            }
            item {
                val saving = "$tick".let { SecretSettings.savePass.value }
                SettingRow(
                    title = stringResource(R.string.password_button),
                    summary = if (saving) "$tick".let { onOff(ZtriSettings.passIcon.value) } else stringResource(R.string.pass_off_reason),
                    enabled = saving,
                    onClick = { open(PasswordSettingsActivity::class.java, R.string.password_button) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.news_search_button),
                    summary = "$tick".let { onOff(ZtriSettings.newsIcon.value) },
                    onClick = { open(NewsSettingsActivity::class.java, R.string.search_icon) },
                )
            }
            item {
                val pos = "$tick".let { ZoneSettings.downPos.value.coerceIn(0, 3) }
                SettingRow(
                    title = stringResource(R.string.video_manager),
                    summary = stringResource(MediaSettingsActivity.positionLabels[pos]),
                    onClick = { open(MediaSettingsActivity::class.java, R.string.video_manager) },
                )
            }
            settingsFooter(getString(R.string.page_buttons_info))
            settingsDivider()
            item {
                SettingRow(
                    title = stringResource(R.string.buttons_put_back),
                    summary = stringResource(R.string.buttons_put_back_sum),
                    onClick = {
                        putBack(context)
                        MainUtil.showToast(context, R.string.buttons_put_back_done)
                    },
                )
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = { resetToDefaults(context); tick++ },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Forgets where every button was dragged: each goes back to its first spot (in the browser on its next look). */
        @JvmStatic
        fun putBack(context: Context) {
            FloatSettings.reset(
                context,
                listOf(
                    FloatSettings.flt1LtX, FloatSettings.flt1RtX, FloatSettings.flt1UpY, FloatSettings.flt1DnY,
                    FloatSettings.flt2LtX, FloatSettings.flt2RtX, FloatSettings.flt2UpY, FloatSettings.flt2DnY,
                    FloatSettings.flt3LtX, FloatSettings.flt3RtX, FloatSettings.flt3UpY, FloatSettings.flt3DnY,
                ),
            )
            ZtriSettings.reset(
                context,
                listOf(
                    ZtriSettings.newsLtX, ZtriSettings.newsRtX, ZtriSettings.newsUpY, ZtriSettings.newsDnY,
                    ZtriSettings.passLtX, ZtriSettings.passRtX, ZtriSettings.passUpY, ZtriSettings.passDnY,
                    ZtriSettings.trnsLtX, ZtriSettings.trnsRtX, ZtriSettings.trnsUpY, ZtriSettings.trnsDnY,
                    ZtriSettings.zoomLtX, ZtriSettings.zoomRtX, ZtriSettings.zoomUpY, ZtriSettings.zoomDnY,
                    ZtriSettings.comicLtX, ZtriSettings.comicRtX, ZtriSettings.comicUpY, ZtriSettings.comicDnY,
                    ZtriSettings.downLtX, ZtriSettings.downRtX, ZtriSettings.downUpY, ZtriSettings.downDnY,
                ),
            )
            TtsSettings.reset(context, listOf(TtsSettings.ttsLtX, TtsSettings.ttsRtX, TtsSettings.ttsUpY, TtsSettings.ttsDnY))
            FloatButton.placeRound++
        }

        /**
         * Restores every button on the page: the floating buttons, the back to top button, and the buttons of translate,
         * read aloud, text zoom, passwords, news and the video manager (on / off, colour, transparency), and their places.
         */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            FloatSettingsActivity.resetToDefaults(context)
            ZoneSettings.reset(context, listOf(ZoneSettings.showUpPos, ZoneSettings.downPos))
            EditorSettings.reset(
                context,
                listOf(
                    EditorSettings.upAlpha,
                    EditorSettings.trnsAlpha, EditorSettings.trnsColor, EditorSettings.trnsPos,
                    EditorSettings.ttsAlpha, EditorSettings.ttsColor, EditorSettings.ttsPos,
                    EditorSettings.zoomAlpha, EditorSettings.zoomColor, EditorSettings.zoomPos,
                    EditorSettings.passAlpha, EditorSettings.passColor, EditorSettings.passPos,
                    EditorSettings.newsAlpha, EditorSettings.newsColor, EditorSettings.newsPos,
                    EditorSettings.iconAlpha, EditorSettings.iconColor, EditorSettings.iconPos,
                ),
            )
            PrefEditor.trnsArgb = PrefEditor.withAlpha(EditorSettings.trnsColor.value, 0)
            PrefEditor.color = PrefEditor.withAlpha(EditorSettings.ttsColor.value, 0)
            PrefEditor.zoomArgb = PrefEditor.withAlpha(EditorSettings.zoomColor.value, 0)
            PrefEditor.passArgb = PrefEditor.withAlpha(EditorSettings.passColor.value, 0)
            PrefEditor.newsArgb = PrefEditor.withAlpha(EditorSettings.newsColor.value, 0)
            PrefEditor.iconArgb = PrefEditor.withAlpha(EditorSettings.iconColor.value, 0)
            AlbumSettings.reset(context, listOf(AlbumSettings.transIcon, AlbumSettings.transPage))
            TtsSettings.reset(context, listOf(TtsSettings.ttsIcon))
            ZtriSettings.reset(context, listOf(ZtriSettings.zoomIcon, ZtriSettings.passIcon, ZtriSettings.newsIcon))
            putBack(context)
        }
    }
}
