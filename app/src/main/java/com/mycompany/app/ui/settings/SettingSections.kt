package com.mycompany.app.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.webkit.WebView
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingsListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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

/*
 * Rows that moved between settings pages when the settings were regrouped (2026-11-19): the old "Advanced" and "Site"
 * pages were split up, text size and font went to Appearance. Each row keeps its own state and dialog, so a page only
 * adds the section. [version] is bumped by the page's reset so the rows read their values again.
 */

/**
 * A limit as one slider: its left end (below [minOn]) shows [offRes] ("Unlimited", "Never") and stores 0. Greyed out
 * with [reason] when not [enabled]. Used by Performance & memory and Tabs (the open limit).
 */
internal fun SettingsListScope.limitSlider(
    titleRes: Int,
    state: com.mycompany.app.ui.prefs.PrefState<Int>,
    max: Int,
    minOn: Int = 3,
    offRes: Int = R.string.history_none,
    enabled: Boolean = true,
    reason: String? = null,
    label: (Int) -> String = { it.toString() },
) {
    item {
        val off = stringResource(offRes)
        SettingSliderRow(
            title = stringResource(titleRes),
            value = if (state.value < minOn) minOn - 1 else state.value.coerceAtMost(max),
            range = (minOn - 1)..max,
            label = { if (it < minOn) off else label(it) },
            enabled = enabled,
            summary = if (enabled) null else reason,
            onValueChange = { state.set(if (it < minOn) 0 else it) },
        )
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
    if (EditorSettings.zoomAlpha.value != MainConst.BUTTON_ALPHA || EditorSettings.zoomColor.value != color || EditorSettings.zoomPos.value.compareTo(pos) != 0) {
        EditorSettings.editLocal { set(EditorSettings.zoomAlpha, MainConst.BUTTON_ALPHA) }; EditorSettings.editLocal { set(EditorSettings.zoomColor, color) }; EditorSettings.editLocal { set(EditorSettings.zoomPos, pos) }; PrefEditor.zoomArgb = PrefEditor.withAlpha(color, MainConst.BUTTON_ALPHA)
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
