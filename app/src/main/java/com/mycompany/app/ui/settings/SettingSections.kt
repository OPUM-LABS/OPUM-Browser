package com.mycompany.app.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.webkit.WebView
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingsListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.book.DbBookAgent
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.PrefState

/*
 * Rows that moved between settings pages when the settings were regrouped (2026-11-19): the old "Advanced" and "Site"
 * pages were split up, text size and font went to Appearance. Each row keeps its own state and dialog, so a page only
 * adds the section. [version] is bumped by the page's reset so the rows read their values again.
 */

@Composable
private fun <T> rememberPrefAt(version: Int, read: () -> T, write: (T) -> Unit): PrefState<T> =
    remember(version) { PrefState(read(), write) }

// ---- Performance: pre-raster and web debugging (from Advanced) ---------------------------------------------------

fun SettingsListScope.performanceSection(version: Int) {
    item {
        val context = LocalContext.current
        val preRaster = rememberPrefAt(version, { ZoneSettings.preRaster.value }) { ZoneSettings.edit(context) { set(ZoneSettings.preRaster, it) } }
        SettingSwitchRow(
            title = stringResource(R.string.pre_raster),
            summary = stringResource(R.string.smooth_scroll_sum),
            checked = preRaster.value,
            onCheckedChange = preRaster::set,
        )
    }
}

fun resetPerformance(context: Context) {
    if (ZoneSettings.preRaster.value) ZoneSettings.reset(context, listOf(ZoneSettings.preRaster))
}

/**
 * A limit as "Unlimited / Custom" segments plus a slider for the custom value. Values below [minOn] mean unlimited.
 * Shared by Performance & memory and Tabs (the open limit).
 */
internal fun SettingsListScope.limitRows(
    titleRes: Int,
    state: com.mycompany.app.ui.prefs.PrefState<Int>,
    range: IntRange,
    firstValue: Int,
    minOn: Int = 3,
    label: (Int) -> String = { it.toString() },
) {
    item {
        SettingSegmentedRow(
            title = stringResource(titleRes),
            options = listOf(stringResource(R.string.history_none), stringResource(R.string.user_defined)),
            selectedIndex = if (state.value < minOn) 0 else 1,
            onSelect = { state.set(if (it == 0) 0 else firstValue) },
        )
    }
    if (state.value >= minOn) {
        item {
            SettingSliderRow(title = stringResource(R.string.hd_count), value = state.value, range = range, label = label, onValueChange = state::set)
        }
    }
}

// ---- Appearance: text size and font (from Web content) -----------------------------------------------------------

internal fun fontName(context: Context): String {
    if (!ZoneSettings.userFont.value) return context.getString(R.string.font_default)
    val path = ZoneSettings.fontPath.value
    val name = if (!TextUtils.isEmpty(path) && path!!.startsWith("app://")) {
        val comma = path.lastIndexOf(',')
        if (comma > 0 && comma + 1 < path.length) path.substring(comma + 1) else null
    } else {
        MainUtil.uriFileName(context, path)
    }
    return if (TextUtils.isEmpty(name)) context.getString(R.string.no_title) else name!!
}

/** Restores the defaults of [TextSettingsActivity] (text size, the zoom button, the font). */
fun resetTextFont(context: Context) {
    val color = MainConst.gradientColors[5]
    val pos = MainConst.presetPositions[5]
    if (ZtriSettings.zoomIcon.value || ZtriSettings.zoomSize.value != 200) {
        ZtriSettings.editLocal { set(ZtriSettings.zoomIcon, false) }; ZtriSettings.editLocal { set(ZtriSettings.zoomSize, 200) }
        val p = PrefZtri.get(context)
        p!!.removeKey("mZoomIcon"); p.removeKey("mZoomSize"); p.save()
    }
    if (ZoneSettings.textSize.value != 100) ZoneSettings.reset(context, listOf(ZoneSettings.textSize))
    if (EditorSettings.zoomAlpha.value != 0 || EditorSettings.zoomColor.value != color || EditorSettings.zoomPos.value.compareTo(pos) != 0) {
        EditorSettings.editLocal { set(EditorSettings.zoomAlpha, 0) }; EditorSettings.editLocal { set(EditorSettings.zoomColor, color) }; EditorSettings.editLocal { set(EditorSettings.zoomPos, pos) }; PrefEditor.zoomArgb = PrefEditor.withAlpha(color, 0)
        val p = PrefEditor.get(context)
        p!!.removeKey("mZoomAlpha"); p.removeKey("mZoomColor"); p.removeKey("mZoomPos"); p.save()
    }
    if (ZoneSettings.userFont.value || ZoneSettings.fontBold.value || !TextUtils.isEmpty(ZoneSettings.fontPath.value)) {
        ZoneSettings.editLocal { set(ZoneSettings.userFont, false) }; ZoneSettings.editLocal { set(ZoneSettings.fontPath, "") }; ZoneSettings.editLocal { set(ZoneSettings.fontBold, false) }
        val p = PrefZone.get(context, false)
        p!!.removeKey("mUserFont"); p.removeKey("mFontPath"); p.removeKey("mFontBold"); p.save()
    }
}

// ---- Links to other screens -------------------------------------------------------------------------------------

/** A row that opens another settings screen. */
fun SettingsListScope.screenLinkRow(titleRes: Int, summary: String? = null, open: (Activity) -> Unit) {
    item {
        val activity = activityOf(LocalContext.current)
        SettingRow(title = stringResource(titleRes), summary = summary, onClick = { activity?.let(open) })
    }
}

/** The activity behind [context] (Compose may hand out a wrapped context). */
fun activityOf(context: Context): Activity? {
    var c: Context? = context
    while (c is android.content.ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
