package com.mycompany.app.dialog

import android.content.DialogInterface
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ZoneSettings
import android.os.Build
import android.text.TextUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.dialog.DialogSetDown.SetDownListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Video overlay buttons (Settings > Media): a preview of the buttons shown over web videos and switches for them.
 * Normal videos ([youModeParam] = false): picture-in-picture (`ZoneSettings.showPip.value`), full screen, download and the video
 * player for full screen (menu: default / other app, the latter via `DialogSetDown` for video types; stored in
 * `AlbumSettings.playName`, only when full screen is off). YouTube ([youModeParam] = true): picture-in-picture and play speed. Apply (disabled with every button off) stores the changes as before. Compose now (on
 * [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogSetVideo(settingMedia: android.app.Activity?, youModeParam: Boolean) : ComposeBottomDialog(settingMedia) {
    @JvmField
    var hostActivity: android.app.Activity? = settingMedia
    @JvmField
    var appContext: android.content.Context? = context
    @JvmField
    var youMode: Boolean = youModeParam
    @JvmField
    var downDialog: DialogSetDown? = null
    @JvmField
    var playerLabel: String? = if (youModeParam) null else AlbumSettings.playName.value
    @JvmField
    var playerPackage: String? = if (youModeParam) null else AlbumSettings.playPkg.value
    @JvmField
    var playerClass: String? = if (youModeParam) null else AlbumSettings.playCls.value

    private var pip by mutableStateOf(if (youModeParam) ZoneSettings.youPip.value else ZoneSettings.showPip.value)
    private var full by mutableStateOf(if (youModeParam) false else ZoneSettings.showFull.value)
    private var down by mutableStateOf(if (youModeParam) false else ZoneSettings.showDown.value)
    private var speed by mutableStateOf(if (youModeParam) ZoneSettings.youRate.value else false)
    private var playerName by mutableStateOf(playerLabel)
    private var playerMenu by mutableStateOf(false)

    val downShown: Boolean get() = down

    init {
        setSheet {
            // preview of the overlay buttons
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (pip) PreviewButton(AppR.drawable.outline_picture_in_picture_alt_black_24)
                        if (youMode) {
                            if (speed) PreviewButton(AppR.drawable.outline_slow_motion_video_black_24)
                        } else {
                            if (full) PreviewButton(AppR.drawable.outline_fullscreen_black_24)
                            if (down) PreviewButton(AppR.drawable.outline_download_black_24)
                        }
                        if (!pip && !full && !down && !speed) {
                            Text(
                                stringResource(AppR.string.not_used),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            )
                        }
                    }
                }
            }
            HorizontalDivider()
            val pipInfo = null
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                SwitchRow(stringResource(AppR.string.pip_mode), pipInfo, pip) { pip = it }
                if (youMode) {
                    SwitchRow(stringResource(AppR.string.play_speed), null, speed) { speed = it }
                } else {
                    SwitchRow(stringResource(AppR.string.full_screen), null, full) { full = it }
                    SwitchRow(stringResource(AppR.string.download), null, down) { down = it }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    Box {
                        ListItem(
                            headlineContent = { Text(stringResource(AppR.string.video_player)) },
                            supportingContent = {
                                Text(if (!TextUtils.isEmpty(playerName)) playerName!! else stringResource(AppR.string.default_title))
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier.clickable(enabled = !full) { playerMenu = true },
                        )
                        com.mycompany.app.ui.components.BubbleMenu(expanded = playerMenu, onDismiss = { playerMenu = false }) {
                            DropdownMenuItem(text = { Text(stringResource(AppR.string.default_title)) }, onClick = { playerMenu = false; defaultPlayer() })
                            DropdownMenuItem(text = { Text(stringResource(AppR.string.other_app)) }, onClick = { playerMenu = false; otherPlayer() })
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { save() }, enabled = pip || full || down || speed) { Text(stringResource(AppR.string.apply)) }
            }
        }
        show()
    }

    @Composable
    private fun PreviewButton(res: Int) {
        FilledTonalIconButton(onClick = { }, modifier = Modifier.size(48.dp)) {
            Icon(painterResource(res), contentDescription = null)
        }
    }

    @Composable
    private fun SwitchRow(title: String, info: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = info?.let { { Text(it) } },
            trailingContent = { Switch(checked = checked, onCheckedChange = onChange) },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.clickable { onChange(!checked) },
        )
    }

    private fun defaultPlayer() {
        playerLabel = ""
        playerPackage = ""
        playerClass = ""
        playerName = null
    }

    private fun otherPlayer() {
        val activity = hostActivity ?: return
        if (downDialog != null) return
        val dialog = DialogSetDown(activity, "video/*", (activity as? com.mycompany.app.main.MainActivity)?.isLandscapeScreen() ?: MainUtil.isLandscape(activity), object : SetDownListener {
            override fun onPick(label: String?, packageName: String?, className: String?) {
                if (TextUtils.isEmpty(label) || TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) return
                playerLabel = label
                playerPackage = packageName
                playerClass = className
                playerName = label
            }
        })
        downDialog = dialog
        dialog.setOnDismissListener(DialogInterface.OnDismissListener {
            downDialog?.dismiss()
            downDialog = null
        })
    }

    /** Apply: stores the changed values (as before) and closes. */
    fun save() {
        if (youMode) {
            if (ZoneSettings.youPip.value != pip) {
                ZoneSettings.edit(appContext) { set(ZoneSettings.youPip, pip) }
            }
            if (ZoneSettings.youRate.value != speed) {
                ZoneSettings.edit(appContext) { set(ZoneSettings.youRate, speed) }
            }
        } else {
            if (ZoneSettings.showPip.value != pip) {
                ZoneSettings.edit(appContext) { set(ZoneSettings.showPip, pip) }
            }
            if (ZoneSettings.showFull.value != full) {
                ZoneSettings.edit(appContext) { set(ZoneSettings.showFull, full) }
            }
            if (ZoneSettings.showDown.value != down) {
                ZoneSettings.edit(appContext) { set(ZoneSettings.showDown, down) }
            }
            if (!MainUtil.sameObject(AlbumSettings.playName.value, playerLabel) || !MainUtil.sameObject(AlbumSettings.playPkg.value, playerPackage) || !MainUtil.sameObject(AlbumSettings.playCls.value, playerClass)) {
                AlbumSettings.editLocal { set(AlbumSettings.playName, playerLabel) }
                AlbumSettings.editLocal { set(AlbumSettings.playPkg, playerPackage) }
                AlbumSettings.editLocal { set(AlbumSettings.playCls, playerClass) }
                val ctx = appContext
                val pref = if (ctx != null) PrefAlbum.get(ctx, false) else null
                if (pref != null) {
                    pref.putString("mPlayName2", AlbumSettings.playName.value)
                    pref.putString("mPlayPkg2", AlbumSettings.playPkg.value)
                    pref.putString("mPlayCls2", AlbumSettings.playCls.value)
                    pref.saveAsync()
                }
            }
        }
        dismiss()
    }

    override fun dismiss() {
        val downDialogNow = downDialog
        downDialog = null
        downDialogNow?.dismiss()
        hostActivity = null
        super.dismiss()
    }

    companion object {
    }
}
