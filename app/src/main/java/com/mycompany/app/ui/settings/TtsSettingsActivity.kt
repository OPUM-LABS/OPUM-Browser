package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.TtsSettings
import android.content.Intent
import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogEditIcon
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.InfoDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.TtsDetailDialog
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Text-to-speech settings. The detail dialog ([DialogSetTts]) and the icon colour editor ([DialogEditIcon]) are
 * still legacy dialogs shown through `DialogShow.direct`.
 */
class TtsSettingsActivity : OpumComposeActivity() {
    private var fromPopup = false
    private var ttsAtStart = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        fromPopup = intent.getBooleanExtra("EXTRA_POPUP", false)
        ttsAtStart = TtsSettings.ttsMode.value
        super.onCreate(savedInstanceState)
    }

    override fun finish() {
        if (fromPopup && ttsAtStart != TtsSettings.ttsMode.value) {
            ttsAtStart = TtsSettings.ttsMode.value
            setResult(RESULT_OK, Intent().putExtra("EXTRA_TYPE", 60))
        }
        super.finish()
    }

    @Composable
    override fun Content() {
        val context: Context = this
        val on = rememberPref({ TtsSettings.ttsMode.value }) { TtsSettings.edit(context) { set(TtsSettings.ttsMode, it) } }
        val stopIcon = rememberPref({ TtsSettings.ttsIcon.value }) { TtsSettings.edit(context) { set(TtsSettings.ttsIcon, it) } }
        var iconColor by remember { mutableIntStateOf(PrefEditor.withAlpha(EditorSettings.ttsColor.value, EditorSettings.ttsAlpha.value)) }
        var guide by remember { mutableStateOf(false) }
        var detail by remember { mutableStateOf(false) }
        var resetting by remember { mutableStateOf(false) }

        // the colour editor runs in the dialog host and changes the legacy values
        OnScreenResume { iconColor = PrefEditor.withAlpha(EditorSettings.ttsColor.value, EditorSettings.ttsAlpha.value) }

        SettingsScaffold(
            title = stringResource(R.string.tts_mode),
            onBack = { finish() },
            onReset = { resetting = true },
            actions = {
                IconButton(onClick = { guide = true }) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                }
            },
        ) {
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.tts_on),
                    checked = on.value,
                    onCheckedChange = on::set,
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.detail_setting),
                    onClick = { detail = true },
                )
            }
            settingsFooter(getString(R.string.tts_info_1))
            settingsHeader(getString(R.string.hd_screen_button))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.stop_icon),
                    checked = stopIcon.value,
                    onCheckedChange = stopIcon::set,
                )
            }
            if (stopIcon.value) {
                item {
                    SettingRow(
                        title = stringResource(R.string.icon_color),
                        onClick = { com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogEditIcon(host, 1, null) } },
                        trailing = {
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color(iconColor))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                            )
                        },
                    )
                }
            }
            settingsFooter(getString(R.string.stop_icon_info) + "\n" + getString(R.string.drag_move_guide))
        }

        if (detail) TtsDetailDialog(context) { detail = false }
        if (guide) {
            InfoDialog(
                title = stringResource(R.string.tts_mode),
                message = stringResource(R.string.tts_guide_1) + "\n" + stringResource(R.string.tts_guide_2),
                onDismiss = { guide = false },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    on.reload(TtsSettings.ttsMode.value)
                    stopIcon.reload(TtsSettings.ttsIcon.value)
                    iconColor = PrefEditor.withAlpha(EditorSettings.ttsColor.value, EditorSettings.ttsAlpha.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** Restores the defaults of this page. */
    private fun resetToDefaults(context: Context) {
        val color = MainConst.gradientColors[5]
        val pos = MainConst.presetPositions[5]
        if (TtsSettings.ttsMode.value) TtsSettings.reset(context, listOf(TtsSettings.ttsMode))
        if (!TextUtils.isEmpty(TtsSettings.ttsLang.value) || TtsSettings.ttsRate.value.compareTo(1.0f) != 0 || TtsSettings.ttsPitch.value.compareTo(1.0f) != 0) {
            TtsSettings.editLocal { set(TtsSettings.ttsLang, "") }; TtsSettings.editLocal { set(TtsSettings.ttsRate, 1.0f) }; TtsSettings.editLocal { set(TtsSettings.ttsPitch, 1.0f) }
            val prefs = PrefTts.get(context, false)
            prefs!!.removeKey("mTtsLang"); prefs.removeKey("mTtsRate"); prefs.removeKey("mTtsPitch"); prefs.save()
        }
        if (!TtsSettings.ttsIcon.value) TtsSettings.reset(context, listOf(TtsSettings.ttsIcon))
        if (EditorSettings.ttsAlpha.value == 0 && EditorSettings.ttsColor.value == color && EditorSettings.ttsPos.value.compareTo(pos) == 0) return
        EditorSettings.editLocal { set(EditorSettings.ttsAlpha, 0) }
        EditorSettings.editLocal { set(EditorSettings.ttsColor, color) }
        EditorSettings.editLocal { set(EditorSettings.ttsPos, pos) }
        PrefEditor.color = PrefEditor.withAlpha(color, 0)
        val prefs = PrefEditor.get(context)
        prefs!!.removeKey("mTtsAlpha"); prefs.removeKey("mTtsColor"); prefs.removeKey("mTtsPos"); prefs.save()
    }
}
